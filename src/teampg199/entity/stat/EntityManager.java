package teampg199.entity.stat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import teampg199.changeout.Change;
import teampg199.entity.Entity;
import teampg199.entity.dyn.DynamicEntity;

/**
 * Tracks creation/destruction of entities on board.
 *
 * @author Jackson Williams
 */
public class EntityManager implements Iterable<DynamicEntity> {
	private final List<StaticEntity> staticEnts;
	private final List<DynamicEntity> dynamicEnts;

	private final List<DynamicEntity> toAddAtEndOfTurn;
	private final Set<DynamicEntity> toRemoveAtEndOfTurn;

	private List<AddRemEntChange> changeList;

	public EntityManager() {
		changeList = new ArrayList<>();
		dynamicEnts = new ArrayList<>();
		staticEnts = new ArrayList<>();

		toAddAtEndOfTurn = new ArrayList<>();
		toRemoveAtEndOfTurn = new HashSet<>(5);
	}

	public StaticEntity getInstance(Class<? extends StaticEntity> type) {
		for (StaticEntity s : staticEnts) {
			if (s.getClass().equals(type)) {
				return s;
			}
		}

		/*
		 * Need every static entity in here... don't know of a better way to do
		 * this
		 */

		StaticEntity firstInstance = null;
		if (type.equals(Wall.class)) {
			firstInstance = new Wall();
		} else if (type.equals(Empty.class)) {
			firstInstance = new Empty();
		} else {
			throw new IllegalStateException(
					"Tried to create instance of non-implemented Static Entity");
		}

		pushChange(new AddRemEntChange(this, firstInstance,
				AddRemEntChange.Type.CREATED));
		staticEnts.add(firstInstance);
		return firstInstance;
	}

	public List<AddRemEntChange> tick() {
		for (DynamicEntity toAdd : toAddAtEndOfTurn) {
			assert (!dynamicEnts.contains(toAdd));
			dynamicEnts.add(toAdd);
			pushChange(new AddRemEntChange(this, toAdd,
					AddRemEntChange.Type.CREATED));
		}
		toAddAtEndOfTurn.clear();

		for (DynamicEntity toRem : toRemoveAtEndOfTurn) {
			assert (dynamicEnts.contains(toRem));
			dynamicEnts.remove(toRem);
			pushChange(new AddRemEntChange(this, toRem,
					AddRemEntChange.Type.DESTROYED));
		}
		toRemoveAtEndOfTurn.clear();

		List<AddRemEntChange> history = changeList;
		changeList = new ArrayList<>();
		return history;
	}

	@Override
	public Iterator<DynamicEntity> iterator() {
		return dynamicEnts.iterator();
	}

	/**
	 * Should be called in constructor of every entity.
	 */
	public void addEntity(DynamicEntity toAdd) {
		toAddAtEndOfTurn.add(toAdd);
	}

	/**
	 * Should be called whenever an entity is completely removed from the game.
	 */
	public void removeEntity(DynamicEntity toRem) {
		toRemoveAtEndOfTurn.add(toRem);
	}

	private final void pushChange(AddRemEntChange c) {
		changeList.add(c);
	}

	public static final class AddRemEntChange extends Change<EntityManager> {
		public enum Type {
			CREATED, DESTROYED
		};

		private final Entity createdEntity;
		private final Type type;

		private AddRemEntChange(EntityManager affected, Entity affectedEntity,
				Type t) {
			super(affected);
			createdEntity = affectedEntity;
			type = t;
		}

		public Entity getEntity() {
			return createdEntity;
		}

		public Type getChangeType() {
			return type;
		}

		@Override
		public String toString() {
			return "AddRemEntChange [createdEntity=" + createdEntity
					+ ", type=" + type + ", affected=" + affected + "]";
		}

		@Override
		public boolean equals(Object other) {
			Change otherChange = (Change) other;
			if (!(otherChange instanceof AddRemEntChange)) {
				return false;
			}

			AddRemEntChange o = (AddRemEntChange) other;

			return createdEntity == o.createdEntity;
		}

		@Override
		public Change<EntityManager> merge(Change<EntityManager> newer) {
			AddRemEntChange n = (AddRemEntChange) newer;

			// entity created then destroyed in one turn; no need for either
			// change
			// TODO write test for this edge case
			if ((n.type == Type.DESTROYED) && (type == Type.CREATED)) {
				return null;
			}

			throw new IllegalStateException(
					"Entity add/removed in the wrong order, or twice: first "
							+ type + ", then " + n.type);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(createdEntity);
		}
	}
}
