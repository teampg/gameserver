package teampg199.entity.dyn;

import java.util.ArrayList;
import java.util.List;

import teampg199.entity.Entity;
import teampg199.entity.stat.EntityManager;

/**
 * A type of entity that can change. Changes are logged, and at end of each turn
 * are popped with tick().
 *
 * @author JWill <Jackson.Williams at camosun.ca>
 */
public abstract class DynamicEntity extends Entity {

	protected List<EntityChange> changeList;

	protected DynamicEntity(EntityManager man) {
		changeList = new ArrayList<>();

		man.addEntity(this);
	}

	protected final void pushChange(EntityChange c) {
		changeList.add(c);
	}

	/**
	 * Called at start of each turn. Can do anything extender wants.
	 */
	public void tick() {
	}

	/**
	 * Called at end of each turn. Performs end-turn cleanup, pops changeList.
	 * Overriders should end with "return super.popChanges();"
	 *
	 * @return Changes to this entity made during the current turn.
	 * @see EntityChange
	 */
	public List<EntityChange> popChanges() {
		List<EntityChange> history = changeList;
		changeList = new ArrayList<>(5);
		return history;
	}
}
