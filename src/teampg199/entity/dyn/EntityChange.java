package teampg199.entity.dyn;

import teampg199.changeout.Change;
import teampg199.entity.Entity;

public abstract class EntityChange extends Change<Entity> {
	protected EntityChange(Entity affected) {
		super(affected);
	}
}
