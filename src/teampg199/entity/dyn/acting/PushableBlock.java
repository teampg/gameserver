package teampg199.entity.dyn.acting;

import teampg.grid2d.point.RelPos;
import teampg199.world.WorldPage;

public class PushableBlock extends ActingEntity {

	public PushableBlock(WorldPage page) {
		super(page);
	}

	@Override
	public void nudge(ActingEntity mover, RelPos movementVector) {
		// don't move if pusher is a bullet
		if (mover instanceof Bullet) {
			return;
		}

		moveIfEmpty(movementVector);
	}
}
