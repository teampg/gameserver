package teampg199.world;

import teampg199.entity.stat.EntityManager;
import teampg199.world.board.Board;

public class WorldPage {
	private final EntityManager fact;
	private final Board map;

	public WorldPage(Board map, EntityManager fact) {
		this.map = map;
		this.fact = fact;
	}

	public Board getMap() {
		return map;
	}

	public EntityManager getFact() {
		return fact;
	}
}
