package teampg199.changeout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import teampg199.entity.dyn.EntityChange;
import teampg199.entity.stat.EntityManager.AddRemEntChange;
import teampg199.server.PageChanges;
import teampg199.world.board.BoardChange;

/**
 * Sends change objects to all attached {@link PageChangeSubscriber}.
 *
 * @author JWill <Jackson.Williams at camosun.ca>
 * @see EntityChange
 * @see BoardChange
 */
public class PageChangeBroadcaster {
	private final List<PageChangeSubscriber> subscribers;
	private PageChanges changes;

	public PageChangeBroadcaster() {
		subscribers = new ArrayList<>();
		changes = new PageChanges();
	}

	public void connectChangeSubscriber(PageChangeSubscriber sub) {
		subscribers.add(sub);
	}

	public void broadcastChanges() {
		for (PageChangeSubscriber sub : subscribers) {
			sub.addChanges((PageChanges) changes.clone());
		}

		changes = new PageChanges();
	}

	public void addBoardChanges(Collection<BoardChange> changesToAdd) {
		changes.addBoardChanges(changesToAdd);
	}

	public void addEntityChanges(Collection<EntityChange> changesToAdd) {
		changes.addEntityChanges(changesToAdd);
	}

	public void addNewEntityChanges(Collection<AddRemEntChange> changesToAdd) {
		changes.addNewEntityChanges(changesToAdd);
	}
}
