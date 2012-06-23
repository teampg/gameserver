package teampg199.changeout;

import teampg199.server.PageChanges;

/**
 * Receives changes broadcasted by {@link PageChangeBroadcaster}. Does something
 * with the changes. <br />
 * Examples are a websocket update broadcaster that sends in JSON, and a log
 * writer that only writes interesting changes.
 *
 * @author JWill <Jackson.Williams at camosun.ca>
 */
public interface PageChangeSubscriber {

	void addChanges(PageChanges ch);
}
