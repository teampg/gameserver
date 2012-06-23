package teampg199.entity;

import teampg.grid2d.point.RelPos;
import teampg199.entity.dyn.acting.ActingEntity;

/**
 * The game Entity. May have attributes, but generally speaking doesn't contain
 * behavior. This should be implemented in actions.
 *
 * @author JWill <Jackson.Williams at camosun.ca>
 */
public abstract class Entity {
	/**
	 * Called just before an entity tries to move into this entity.
	 *
	 * If this entity replaces itself with Empty, the mover will be able to move
	 * here.
	 *
	 * @param mover The entity trying to move into this entity.
	 * @param movementVector TODO
	 */
	public void nudge(ActingEntity mover, RelPos movementVector) {
	}
}
