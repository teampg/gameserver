package teampg199.world.loader;

import static org.junit.Assert.*;


import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;


import teampg.grid2d.point.BoundedPos;
import teampg199.entity.Entity;
import teampg199.entity.dyn.acting.PushableBlock;
import teampg199.entity.dyn.acting.TrackingMob;
import teampg199.entity.dyn.acting.spawner.PlayerSpawnShrine;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager;
import teampg199.entity.stat.Wall;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

public class _TestWorldPageLoader {
	private static final String TEST_MAP = "maps/testMap.za";

	String validMapString;
	WorldPage page;

	@Before
	public void setUp() throws Exception {
		validMapString = ".S..\n" +
						 ".###\n" +
						 "G..+";

		page = new WorldPage(null, new EntityManager());
	}

	@Test
	public void testLoadFromString() {
		WorldPage loaded = WorldPageLoader.load(validMapString);
		Board map = loaded.getMap();
		Dimension mapSize = map.getInfo().getSize();

		assertTrue(map.get(BoundedPos.of(0,0,map.getInfo().getSize())) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(1,0,mapSize)) instanceof PlayerSpawnShrine);
		assertTrue(map.get(BoundedPos.of(2,0,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(3,0,mapSize)) instanceof Empty);

		assertTrue(map.get(BoundedPos.of(0,1,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(1,1,mapSize)) instanceof Wall);
		assertTrue(map.get(BoundedPos.of(2,1,mapSize)) instanceof Wall);
		assertTrue(map.get(BoundedPos.of(3,1,mapSize)) instanceof Wall);

		assertTrue(map.get(BoundedPos.of(0,2,mapSize)) instanceof TrackingMob);
		assertTrue(map.get(BoundedPos.of(1,2,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(2,2,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(3,2,mapSize)) instanceof PushableBlock);
	}

	@Test
	public void testLoadFromFile() throws FileNotFoundException {
		WorldPage loaded = WorldPageLoader.load(new File(TEST_MAP));
		Board map = loaded.getMap();
		Dimension mapSize = map.getInfo().getSize();

		assertTrue(map.get(BoundedPos.of(0,0,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(1,0,mapSize)) instanceof PlayerSpawnShrine);
		assertTrue(map.get(BoundedPos.of(2,0,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(3,0,mapSize)) instanceof Empty);

		assertTrue(map.get(BoundedPos.of(0,1,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(1,1,mapSize)) instanceof Wall);
		assertTrue(map.get(BoundedPos.of(2,1,mapSize)) instanceof Wall);
		assertTrue(map.get(BoundedPos.of(3,1,mapSize)) instanceof Wall);

		assertTrue(map.get(BoundedPos.of(0,2,mapSize)) instanceof TrackingMob);
		assertTrue(map.get(BoundedPos.of(1,2,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(2,2,mapSize)) instanceof Empty);
		assertTrue(map.get(BoundedPos.of(3,2,mapSize)) instanceof PushableBlock);
	}

	@Test
	public void testFindMapStringDimensions() {
		Dimension expected = new Dimension(4, 3);
		Dimension result = WorldPageLoader
				.findMapStringDimensions(validMapString);

		assertEquals(expected, result);
	}

	@Test
	public void testGetEntityForSymbol() {
		{
			Entity expected = page.getFact().getInstance(Empty.class);
			Entity actual = WorldPageLoader.getEntityForSymbol(".", page);

			assertEquals(expected, actual);
		}

		{
			Entity expected = page.getFact().getInstance(Wall.class);
			Entity actual = WorldPageLoader.getEntityForSymbol("#", page);

			assertEquals(expected, actual);
		}

		{
			Entity expected = new PlayerSpawnShrine(page);
			Entity actual = WorldPageLoader.getEntityForSymbol("S", page);

			assertTrue(expected instanceof PlayerSpawnShrine);
			assertTrue(actual instanceof PlayerSpawnShrine);
		}
	}
}
