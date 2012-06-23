package teampg199.world.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Dimension;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import teampg.grid2d.point.BoundedPos;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager;

public class _BoardImplTest {
	Board map;
	EntityManager fact;

	@Before
	public void setUp() throws Exception {
		fact = new EntityManager();
		map = new BoardImpl(new Dimension(3, 4), fact.getInstance(Empty.class));
	}

	@Test
	public void testSetBoundedPosEntity() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPointsNear() {
		// TODO make sure 'near' is included in results.  Make sure radius of 1 returns just near.  Rad of 0 should throw illegalargument exception
		fail("Not yet implemented");
	}

	@Test
	public void testGetPointsNearIncludesNear() {
		BoundedPos near = BoundedPos.of(2,2,map.getInfo().getSize());
		Set<BoundedPos> pointsNear = map.getPointsNear(near, 0);

		assertEquals(1, pointsNear.size());
		assertTrue(pointsNear.contains(near));

		pointsNear = map.getPointsNear(near, 1);
		assertTrue(pointsNear.contains(near));

		pointsNear = map.getPointsNear(near, 2);
		assertTrue(pointsNear.contains(near));

		pointsNear = map.getPointsNear(near, 3);
		assertTrue(pointsNear.contains(near));

		pointsNear = map.getPointsNear(near, 4);
		assertTrue(pointsNear.contains(near));
	}

	@Test
	public void testGetRing() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindMatchingEntities() {
		fail("Not yet implemented");
	}

	@Test
	public void testSwap() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsInBounds() {
		fail("Not yet implemented");
	}

	@Test
	public void testPopChanges() {
		fail("Not yet implemented");
	}

	@Test
	public void testContains() {
		fail("Not yet implemented");
	}

}
