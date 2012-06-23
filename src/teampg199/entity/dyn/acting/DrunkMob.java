package teampg199.entity.dyn.acting;


import java.util.Random;


import teampg.grid2d.point.RelPos;
import teampg199.entity.stat.EntityManager;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

public class DrunkMob extends ActingEntity {
	private static final int MOVE_COOLDOWN_TIME = 5;
	private static Random gen;

	private int moveCooldown;

	public DrunkMob(WorldPage page) {
		super(page);

		moveCooldown = 0;
		gen = new Random();
	}


	@Override
	public void nudge(ActingEntity mover, RelPos movementVector) {
		// hit by bullet
		if (mover instanceof Bullet) {
			Board map = getPage().getMap();
			EntityManager entities = getPage().getFact();

			// kill self
			entities.removeEntity(this);
			map.set(this, RelPos.ZERO, new Remains(getPage(), this));
		}
	}

	@Override
	public void tick() {
		if (moveCooldown > 0) {
			moveCooldown--;
			return;
		}

		RelPos wantsToMove = RelPos.of(gen.nextInt(3) - 1, gen.nextInt(3) - 1);

		// try to move
		if (moveIfEmpty(wantsToMove)) {
			moveCooldown = MOVE_COOLDOWN_TIME;
		}
	}
}
