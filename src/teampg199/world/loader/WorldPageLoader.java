package teampg199.world.loader;


import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


import teampg.grid2d.point.AbsPos;
import teampg199.entity.Entity;
import teampg199.entity.dyn.acting.DrunkMob;
import teampg199.entity.dyn.acting.PushableBlock;
import teampg199.entity.dyn.acting.TrackingMob;
import teampg199.entity.dyn.acting.spawner.EnemySpawner;
import teampg199.entity.dyn.acting.spawner.PlayerSpawnShrine;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager;
import teampg199.entity.stat.StaticEntity;
import teampg199.entity.stat.Wall;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;
import teampg199.world.board.BoardImpl;

public class WorldPageLoader {
	private static final String DELIMITER = "";

	public static WorldPage load(File mapFile) throws FileNotFoundException {
		String text = new Scanner(mapFile, "UTF-8").useDelimiter("\\A").next();
		return load(text);
	}

	public static WorldPage load(String mapString) {
		EntityManager entityManager = new EntityManager();

		// initialize map
		Board map;
		{
			Dimension size = findMapStringDimensions(mapString);

			StaticEntity filler = entityManager.getInstance(Empty.class);

			map = new BoardImpl(size, filler);
		}

		WorldPage ret = new WorldPage(map, entityManager);

		// fill map with specified entities
		loadMapEntities(mapString, ret);

		return ret;
	}

	static void loadMapEntities(String mapString, WorldPage ret) {
		Scanner rowScanner = new Scanner(mapString);

		int y = 0;
		while (rowScanner.hasNextLine()) {
			String row = rowScanner.nextLine();

			Scanner cellScanner = new Scanner(row);
			cellScanner.useDelimiter(DELIMITER);

			int x = 0;
			while (cellScanner.hasNext()) {
				String cellSymbol = cellScanner.next();
				AbsPos cellPos = new AbsPos(x, y);

				Entity fromCell = getEntityForSymbol(cellSymbol, ret);
				if (fromCell instanceof Empty) {
					x++;
					continue;
				}

				ret.getMap().set(cellPos, fromCell);
				x++;
			}

			y++;
		}
	}

	static Entity getEntityForSymbol(String cellSymbol, WorldPage page) {
		switch (cellSymbol) {
		case ".":
			return page.getFact().getInstance(Empty.class);
		case "#":
			return page.getFact().getInstance(Wall.class);
		case "+":
			return new PushableBlock(page);
		case "G":
			return new TrackingMob(page);
		case "W":
			return new DrunkMob(page);
		case "S":
			return new PlayerSpawnShrine(page);
		case "E":
			return new EnemySpawner(page);
		default:
			// TODO make our own exception: IllegalMapFileFormatException
			throw new IllegalStateException("Couldn't read map string entry: "
					+ cellSymbol);
		}
	}

	// returns null if invalid
	static Dimension findMapStringDimensions(String mapString) {
		Scanner rowScanner = new Scanner(mapString);

		int rows = 0;
		int colsInPreviousRows = -1;
		while (rowScanner.hasNextLine()) {
			String line = rowScanner.nextLine();
			rows++;

			Scanner colScanner = new Scanner(line);
			colScanner.useDelimiter(DELIMITER);

			int cols = 0;
			while (colScanner.hasNext()) {
				colScanner.next();
				cols++;
			}

			if (colsInPreviousRows == -1) {
				colsInPreviousRows = cols;
				continue;
			}

			if (colsInPreviousRows != cols) {
				// TODO make our own exception: IllegalMapFileFormatException
				throw new IllegalStateException("Inconsistent row width: " + colsInPreviousRows + " " + cols + " at row #" + rows);
			}

			colsInPreviousRows = cols;
		}

		return new Dimension(colsInPreviousRows, rows);
	}
}
