package teampg199.entity.dyn.acting;

import teampg.grid2d.point.RelPos;
import teampg199.entity.Entity;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.stat.Empty;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

public abstract class ActingEntity extends DynamicEntity {
	private final WorldPage page;

	protected ActingEntity(WorldPage page) {
		super(page.getFact());
		this.page = page;
	}

	protected WorldPage getPage() {
		return page;
	}

	/**
	 * Tries to move this entity. If entity at destination isn't Empty, don't
	 * move.
	 *
	 * @param moveDir
	 *            Where to move to
	 * @return true if moved, else false
	 */
	public boolean moveIfEmpty(RelPos moveDir) {
		Board map = page.getMap();

		Entity entAtTarget = map.get(this, moveDir);
		
		if (entAtTarget == null) {
			return false;
		}

		// let the target entity know it's being moved into
		entAtTarget.nudge(this, moveDir);

		// check if entity still there
		Entity entAtTargetAfterNudge = map.get(this, moveDir);

		// is it clear? if so, do the move
		if (entAtTargetAfterNudge instanceof Empty) {
			map.swap(this, moveDir);
			return true;
		}

		return false;
	}
}
