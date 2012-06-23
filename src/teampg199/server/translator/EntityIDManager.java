package teampg199.server.translator;

import java.util.HashMap;
import java.util.Map;

import teampg199.entity.Entity;

class EntityIDManager {
	private final Map<Entity, Integer> loggedEntities;
	private int nextID;

	public EntityIDManager() {
		loggedEntities = new HashMap<>();
		nextID = 0;
	}

	public int getID(Entity entity) {
		if (!loggedEntities.containsKey(entity)) {
			loggedEntities.put(entity, getNewID());
		}
		return loggedEntities.get(entity);
	}

	private int getNewID() {
		return nextID++;
	}
}
