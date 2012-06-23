package teampg199.world.board;


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;

import teampg.grid2d.RectGrid;
import teampg.grid2d.GridInterface.Entry;
import teampg.grid2d.point.AbsPos;
import teampg.grid2d.point.Pos2D;
import teampg.grid2d.point.RelPos;
import teampg199.entity.Entity;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.stat.StaticEntity;

public class BoardImpl extends Board {
	private final BoardInfo info;
	private final RectGrid<Entity> grid;
	private final Hashtable<DynamicEntity, AbsPos> dynEntPosIndex;
	private List<BoardChange> turnChanges;

	public BoardImpl(Dimension size, StaticEntity toFill) {
		info = new BoardInfo(size);

		grid = new RectGrid<>(size);
		dynEntPosIndex = new Hashtable<>(size.width * size.height/ 2);
		turnChanges = new ArrayList<>();


		// fill without adding changes
		for(int x = 0; x < size.width; x++){
			for(int y = 0; y < size.height; y++){
				grid.set(new AbsPos(x, y), toFill);
			}
		}
	}

	@Override
	public Entity get(AbsPos target) {
		if (!isInBounds(target)) {
			return null; //TODO change to wall or something
		}

		return grid.get(target);
	}

	@Override
	public Entry<Entity> get(DynamicEntity at) {
		return grid.get(at);
	}

	@Override
	public void set(AbsPos target, Entity newOccupant) {
		// remove overwritten entity from index
		Entity entAtTarget = grid.get(target);
		if (entAtTarget instanceof DynamicEntity) {
			dynEntPosIndex.remove(entAtTarget);
		}

		// set new cell occupant, and add it to index
		grid.set(target, newOccupant);
		if (newOccupant instanceof DynamicEntity) {
			dynEntPosIndex.put((DynamicEntity) newOccupant, target);
		}

		// log the change
		BoardChange addedEntChange = new BoardChange(this, target, newOccupant);
		turnChanges.add(addedEntChange);
	}

	@Override
	public Entity get(DynamicEntity nearEnt, RelPos offset) {
		AbsPos from = dynEntPosIndex.get(nearEnt);
		AbsPos target = Pos2D.offset(from, offset);

		if (!isInBounds(target)) {
			return null;
		}

		Entity found = grid.get(target);
		return found;
	}

	@Override
	public void set(DynamicEntity nearEnt, RelPos offset, Entity newOccupant) {
		AbsPos from = dynEntPosIndex.get(nearEnt);
		AbsPos target = Pos2D.offset(from, offset);

		this.set(target, newOccupant);
	}

	@Override
	public List<Entry<Entity>> findMatchingEntities(Predicate<Entry<Entity>> matcher) {
		List<Entry<Entity>> foundMatches = new ArrayList<>();

		//TODO refactor grid collection to use Entry instead of Points list
		for (Entry<Entity> entry : grid.getEntries()) {
			if (matcher.apply(entry)) {
				foundMatches.add(entry);
			}
		}

		return foundMatches;
	}

	@Override
	/**
	 * Includes near.
	 */
	public Set<AbsPos> getPointsNear(AbsPos near, int radius) {
		Set<AbsPos> pointsNear = new HashSet<>();

		int xi = near.x();
		int yi = near.y();

		int minXToTest = (xi - radius);
		int maxXToTest = (xi + radius);

		int minYToTest = (yi - radius);
		int maxYToTest = (yi + radius);

		for (int x = minXToTest; x <= maxXToTest; x++) {
			for (int y = minYToTest; y <= maxYToTest; y++) {
				AbsPos pos = new AbsPos(x, y);

				// don't consider points that are out of bounds
				if (!isInBounds(pos)) {
					continue;
				}

				// inside radius?
				if (near.distance(pos) > radius) {
					continue;
				}

				pointsNear.add(pos);
			}
		}

		return pointsNear;
	}

	@Override
	public Set<AbsPos> getRing(AbsPos from, int radius) {
		return (Set<AbsPos>) Pos2D.removeOutOfRectBounds(from.getRing(radius), 0, info.getSize().width - 1, info.getSize().height - 1, 0);
	}

	@Override
	public boolean isInBounds(AbsPos p) {
		return grid.isInBounds(p);
	}

	@Override
	public void swap(DynamicEntity a, RelPos bOffsetFromA) {
		AbsPos aPos = dynEntPosIndex.get(a);
		AbsPos bPos = Pos2D.offset(aPos, bOffsetFromA);
		Entity b = grid.get(bPos);

		// update index for new positions
		if (b instanceof DynamicEntity) {
			dynEntPosIndex.put((DynamicEntity) b, aPos);
		}
		dynEntPosIndex.put(a, bPos);

		// set new positions
		grid.set(bPos, a);
		grid.set(aPos, b);

		// log the change
		BoardChange aMove = new BoardChange(this, bPos, a);
		turnChanges.add(aMove);
		BoardChange bMove = new BoardChange(this, aPos, b);
		turnChanges.add(bMove);
	}

	@Override
	public List<BoardChange> popChanges() {
		List<BoardChange> poppedChanges = turnChanges;
		turnChanges = new ArrayList<>();

		return poppedChanges;
	}

	@Override
	public BoardInfo getInfo() {
		return info;
	}

	@Override
	public AbsPos getPos(DynamicEntity toFind) {
		return grid.get(toFind).getPosition();
	}

	@Override
	public boolean contains(Entity ent) {
		return grid.contains(ent);
	}
}
