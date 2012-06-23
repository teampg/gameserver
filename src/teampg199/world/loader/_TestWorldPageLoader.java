package teampg199.world.loader;

import static org.junit.Assert.*;


import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;


import teampg.grid2d.point.AbsPos;
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

		Entity entAt00 = map.get(new AbsPos(0,0));
		assertTrue(entAt00 instanceof Empty);
		assertTrue(map.get(new AbsPos(1,0)) instanceof PlayerSpawnShrine);
		assertTrue(map.get(new AbsPos(2,0)) instanceof Empty);
		assertTrue(map.get(new AbsPos(3,0)) instanceof Empty);

		assertTrue(map.get(new AbsPos(0,1)) instanceof Empty);
		assertTrue(map.get(new AbsPos(1,1)) instanceof Wall);
		assertTrue(map.get(new AbsPos(2,1)) instanceof Wall);
		assertTrue(map.get(new AbsPos(3,1)) instanceof Wall);

		assertTrue(map.get(new AbsPos(0,2)) instanceof TrackingMob);
		assertTrue(map.get(new AbsPos(1,2)) instanceof Empty);
		assertTrue(map.get(new AbsPos(2,2)) instanceof Empty);
		assertTrue(map.get(new AbsPos(3,2)) instanceof PushableBlock);
	}

	@Test
	public void testLoadFromFile() throws FileNotFoundException {
		WorldPage loaded = WorldPageLoader.load(new File("testMap"));
		Board map = loaded.getMap();

		Entity entAt00 = map.get(new AbsPos(0,0));
		assertTrue(entAt00 instanceof Empty);
		assertTrue(map.get(new AbsPos(1,0)) instanceof PlayerSpawnShrine);
		assertTrue(map.get(new AbsPos(2,0)) instanceof Empty);
		assertTrue(map.get(new AbsPos(3,0)) instanceof Empty);

		assertTrue(map.get(new AbsPos(0,1)) instanceof Empty);
		assertTrue(map.get(new AbsPos(1,1)) instanceof Wall);
		assertTrue(map.get(new AbsPos(2,1)) instanceof Wall);
		assertTrue(map.get(new AbsPos(3,1)) instanceof Wall);

		assertTrue(map.get(new AbsPos(0,2)) instanceof TrackingMob);
		assertTrue(map.get(new AbsPos(1,2)) instanceof Empty);
		assertTrue(map.get(new AbsPos(2,2)) instanceof Empty);
		assertTrue(map.get(new AbsPos(3,2)) instanceof PushableBlock);
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
