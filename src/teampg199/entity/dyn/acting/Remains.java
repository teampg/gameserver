package teampg199.entity.dyn.acting;

import teampg.grid2d.point.RelPos;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

public class Remains extends ActingEntity {
	private static final int DESTRUCT_TIME = 5;

	private int selfDestructTimer;

	protected Remains(WorldPage page, DynamicEntity corpse) {
		super(page);
		selfDestructTimer = DESTRUCT_TIME;
		// TODO use corpse for something
	}

	@Override
	public void tick() {
		if (selfDestructTimer > 0) {
			selfDestructTimer--;
			return;
		}

		Board map = getPage().getMap();
		EntityManager entities = getPage().getFact();

		// remove self
		entities.removeEntity(this);
		map.set(this, RelPos.ZERO, entities.getInstance(Empty.class));
	}
}
