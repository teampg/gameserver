package teampg199.world.loader;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import teampg.grid2d.point.BoundedPos;
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
	public static WorldPage load(File mapFile) throws FileNotFoundException {
		try (Scanner loadedMapFile = new Scanner(mapFile, "UTF-8")) {
			loadedMapFile.useDelimiter("\\A");

			String text = loadedMapFile.next();
			return load(text);
		}
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
			int y = 0;
			Iterable<String> allRows = Splitter.on("\n").trimResults().split(mapString);
			for (String row : allRows) {

				int x = 0;
				Iterable<String> splitRow = Splitter.fixedLength(1).trimResults().split(row);
				for (String cellSymbol : splitRow) {
					BoundedPos cellPos = BoundedPos.of(x, y, ret.getMap().getInfo().getSize());

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
			throw new IllegalStateException("Illegal symbol in map file: " + cellSymbol);
		}
	}

	// returns null if invalid
	static Dimension findMapStringDimensions(String mapString) {
		try (Scanner rowScanner = new Scanner(mapString)) {
			Iterable<String> allLines = Splitter.on("\n")
					.omitEmptyStrings()
					.split(mapString);

			int height = Iterables.size(allLines);
			int width = allLines.iterator().next().length();


			// ensure map is rectangular
			for (String line : allLines) {
				if (width != line.length()) {
					// TODO make our own exception: IllegalMapFileFormatException
					throw new IllegalStateException("Inconsistent row width; Map is not square");
				}
			}


			return new Dimension(width, height);
		}
	}
}
