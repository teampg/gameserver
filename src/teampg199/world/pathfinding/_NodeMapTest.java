package teampg199.world.pathfinding;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


import teampg.grid2d.point.BoundedPos;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;
import teampg199.world.loader.WorldPageLoader;

public class _NodeMapTest {
	Board map;
	NodeMap nodeMap;

	@Before
	public void setUp() throws Exception {
		String testMapString =
				"#.." + "\n" +
				".##" + "\n";

		WorldPage page = WorldPageLoader.load(testMapString);

		map = page.getMap();
		nodeMap = new NodeMap(map, new BoundedPos(2,0), new BoundedPos(1,1));
	}

	@Test
	public void testGetNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testPutNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCost() {
		assertEquals(nodeMap.getCost(new BoundedPos(0,0)), null);
		assertEquals(nodeMap.getCost(new BoundedPos(1,0)), Integer.valueOf(NodeMap.BASE_COST));
		assertEquals(nodeMap.getCost(new BoundedPos(2,0)), Integer.valueOf(NodeMap.BASE_COST));

		assertEquals(nodeMap.getCost(new BoundedPos(0,1)), Integer.valueOf(NodeMap.BASE_COST));
		assertEquals(nodeMap.getCost(new BoundedPos(1,1)), null);
		assertEquals(nodeMap.getCost(new BoundedPos(2,1)), null);
	}

	@Test
	public void testGetEstimatedCost() {
		assertEquals(NodeMap.BASE_COST * 3, nodeMap.getEstimatedCost(new BoundedPos(0,0), new BoundedPos(2,1)));
	}
}
