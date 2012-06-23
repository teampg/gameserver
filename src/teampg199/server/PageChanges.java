package teampg199.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import teampg199.changeout.Change;
import teampg199.entity.dyn.EntityChange;
import teampg199.entity.stat.EntityManager.AddRemEntChange;
import teampg199.world.board.BoardChange;

/**
 * Holds Page changes.
 */
public class PageChanges implements Cloneable {
	private final Set<BoardChange> boardChanges;
	private final Set<EntityChange> entChanges;
	private final Set<AddRemEntChange> newEntChanges;

	public PageChanges() {
		boardChanges = new HashSet<>();
		entChanges = new HashSet<>();
		newEntChanges = new HashSet<>();
	}

	@Override
	public Object clone() {
		PageChanges dup = new PageChanges();
		dup.boardChanges.addAll(boardChanges);
		dup.entChanges.addAll(entChanges);
		dup.newEntChanges.addAll(newEntChanges);

		return dup;
	}

	public void merge(PageChanges newer) {
		Change.mergeAll(newer.boardChanges, boardChanges);
		Change.mergeAll(newer.entChanges, entChanges);
		Change.mergeAll(newer.newEntChanges, newEntChanges);
	}

	public boolean isEmpty() {
		return boardChanges.isEmpty() && entChanges.isEmpty() && newEntChanges.isEmpty();
	}

	public void addBoardChanges(Collection<BoardChange> changesToAdd) {
		Change.mergeAll(changesToAdd, boardChanges);
	}

	public void addEntityChanges(Collection<EntityChange> changesToAdd) {
		Change.mergeAll(changesToAdd, entChanges);
	}

	public void addNewEntityChanges(Collection<AddRemEntChange> changesToAdd) {
		Change.mergeAll(changesToAdd, newEntChanges);
	}

	public List<BoardChange> getBoardChanges() {
		return Lists.newArrayList(boardChanges);
	}

	public List<EntityChange> getEntityChanges() {
		return Lists.newArrayList(entChanges);
	}

	public List<AddRemEntChange> getNewEntityChanges() {
		return Lists.newArrayList(newEntChanges);
	}
}
