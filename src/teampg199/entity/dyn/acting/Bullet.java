package teampg199.entity.dyn.acting;

import teampg.grid2d.point.RelPos;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

public class Bullet extends ActingEntity {
	private final RelPos vector;

	public Bullet(WorldPage page, RelPos movementVector) {
		super(page);

		vector = movementVector;
	}

	@Override
	public void tick() {
		Board map = getPage().getMap();
		EntityManager man = getPage().getFact();

		// try to move, and nudge target
		boolean hitSomething = ! moveIfEmpty(vector);

		// remove self if tried to move into non-empty
		if (hitSomething) {
			man.removeEntity(this);
			map.set(this, RelPos.ZERO, man.getInstance(Empty.class));
			return;
		}
	}

	@Override
	public String toString() {
		return "Bullet [vector=" + vector + "]";
	}
}
