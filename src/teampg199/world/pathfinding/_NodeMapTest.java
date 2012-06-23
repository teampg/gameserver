package teampg199.world.pathfinding;

import static org.junit.Assert.*;

import java.awt.Dimension;

import org.junit.Before;
import org.junit.Test;


import teampg.grid2d.point.BoundedPos;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;
import teampg199.world.loader.WorldPageLoader;

public class _NodeMapTest {
	Board map;
	NodeMap nodeMap;
	Dimension size;

	@Before
	public void setUp() throws Exception {
		String testMapString =
				"#.." + "\n" +
				".##" + "\n";

		WorldPage page = WorldPageLoader.load(testMapString);

		map = page.getMap();
		size = map.getInfo().getSize();
		
		nodeMap = new NodeMap(map, BoundedPos.of(2,0,size), BoundedPos.of(1,1,size));
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
		assertEquals(nodeMap.getCost(BoundedPos.of(0,0,size)), null);
		assertEquals(nodeMap.getCost(BoundedPos.of(1,0,size)), Integer.valueOf(NodeMap.BASE_COST));
		assertEquals(nodeMap.getCost(BoundedPos.of(2,0,size)), Integer.valueOf(NodeMap.BASE_COST));

		assertEquals(nodeMap.getCost(BoundedPos.of(0,1,size)), Integer.valueOf(NodeMap.BASE_COST));
		assertEquals(nodeMap.getCost(BoundedPos.of(1,1,size)), null);
		assertEquals(nodeMap.getCost(BoundedPos.of(2,1,size)), null);
	}

	@Test
	public void testGetEstimatedCost() {
		assertEquals(NodeMap.BASE_COST * 3, nodeMap.getEstimatedCost(BoundedPos.of(0,0,size), BoundedPos.of(2,1,size)));
	}
}
