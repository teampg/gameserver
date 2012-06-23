package teampg199.world.board;


import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;

import teampg.grid2d.GridInterface.Entry;
import teampg.grid2d.point.AbsPos;
import teampg.grid2d.point.Pos2D;
import teampg.grid2d.point.RelPos;
import teampg.grid2d.point.Pos2D.DistanceComparator;
import teampg199.entity.Entity;
import teampg199.entity.dyn.DynamicEntity;

/**
 * Tracks positions of Entities.
 *
 * @author JWill <Jackson.Williams at camosun.ca>
 */
public abstract class Board {
	/**
	 * Find Entity at some position relative to a DynamicEntity.
	 *
	 * @param nearEnt
	 *            Must be DynamicEntity because StaticEntity may exist in more
	 *            than one place on map.
	 * @param offset
	 *            {@link RelPos} offset from DynamicEntity near.
	 * @return Entity found at (near's position) + (offset)
	 */
	public abstract Entity get(AbsPos at);
	public abstract Entry<Entity> get(DynamicEntity at);
	public abstract void set(AbsPos at, Entity newValue);

	public abstract Entity get(DynamicEntity nearEnt, RelPos offset);
	public abstract void set(DynamicEntity nearEnt, RelPos offset,
			Entity newValue);

	public abstract Set<AbsPos> getPointsNear(AbsPos near, int radius); //TODO this should be getDiamond
	public abstract Set<AbsPos> getRing(AbsPos near, int radius); //TODO this should be getSquare

	public abstract List<Entry<Entity>> findMatchingEntities(Predicate<Entry<Entity>> matcher);

	public abstract void swap(DynamicEntity a, RelPos swapTarget);

	public abstract BoardInfo getInfo();

	public abstract boolean isInBounds(AbsPos p);

	public abstract List<BoardChange> popChanges();

	public static class BoardDistanceComparator implements Comparator<Entry<Entity>> {
		DistanceComparator comparer;

		public BoardDistanceComparator(AbsPos near) {
			comparer = new Pos2D.DistanceComparator(near);
		}

		@Override
		public int compare(Entry<Entity> a, Entry<Entity> b) {
			return comparer.compare(a.getPosition(), b.getPosition());
		}
	}

	public abstract AbsPos getPos(DynamicEntity toFindPosOf);
	public abstract boolean contains(Entity ent);
}
