package teampg199.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import teampg199.entity.dyn.acting.player.ActionCommand;
import teampg199.playerconnection.PlayerConnection.PrivateUpdates;
import teampg199.server.translator.MalformedPizzaProtocolException;
import teampg199.server.translator.Translator;
import teampg199.world.board.BoardInfo;

public class TranslatingBuffer {
	private List<Integer> addedThisTurn;
	private List<Integer> removedThisTurn;

	private final Multimap<Integer, ActionCommand> actions;
	private final Map<Integer, TurnUpdates> changes;

	private final Translator translator;

	public TranslatingBuffer() {
		translator = new Translator();

		addedThisTurn = new ArrayList<>();
		removedThisTurn = new ArrayList<>();

		actions = HashMultimap.create();
		changes = new HashMap<>();
	}

	/*-*************
	 * CLIENT-SIDE *
	 ***************/

	public void add(Integer clientID) {
		addedThisTurn.add(clientID);
	}

	public void remove(Integer clientID) {
		removedThisTurn.add(clientID);

		// don't send things to disconnected players
		actions.put(clientID, null);
		changes.put(clientID, null);
	}

	public void pushAction(Integer clientID, String msg)
			throws JsonParseException, JsonMappingException, IOException,
			MalformedPizzaProtocolException {
		actions.put(clientID, translator.toAction(msg));
	}

	public String popChanges(Integer clientID) {
		TurnUpdates turnUpdates = changes.get(clientID);
		if (turnUpdates == null || turnUpdates.isEmpty()) {
			return "";
		}

		String jsonChanges;
		try {
			jsonChanges = translator.toJSON(turnUpdates);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("JSON Mapping failed");
		}

		changes.put(clientID, null);

		return jsonChanges;
	}

	/*-*************
	 * SERVER-SIDE *
	 ***************/

	public List<Integer> getAdded() {
		List<Integer> poppedAdded = addedThisTurn;
		addedThisTurn = new ArrayList<>();

		return poppedAdded;
	}

	public List<Integer> getRemoved() {
		List<Integer> poppedRemoved = removedThisTurn;
		removedThisTurn = new ArrayList<>();

		return poppedRemoved;
	}

	/**
	 * Sends global and private changes to the specified client
	 *
	 * @param pageChanges
	 *            Global changes
	 * @param privateUpdates
	 *            Private action updates
	 * @param clientID
	 *            ID of the websocket to send changes to
	 */
	public void pushChanges(PageChanges pageChanges,
			PrivateUpdates privateUpdates, BoardInfo newBoard,
			Integer clientID) {
		changes.put(clientID, new TurnUpdates(pageChanges, privateUpdates,
				newBoard));
	}

	public Collection<ActionCommand> popActions(Integer clientID) {
		return actions.removeAll(clientID);
	}

	public static class TurnUpdates {
		private final PageChanges pageChanges;
		private final PrivateUpdates privateUpdates;
		private final BoardInfo newBoard;

		public TurnUpdates(PageChanges pageChanges,
				PrivateUpdates privateUpdates, BoardInfo newBoard) {
			this.pageChanges = pageChanges;
			this.privateUpdates = privateUpdates;
			this.newBoard = newBoard;
		}

		public boolean isEmpty() {
			return pageChanges.isEmpty() && privateUpdates.isEmpty()
					&& (newBoard == null);
		}

		public BoardInfo getBoardInfo() {
			return newBoard;
		}

		public PageChanges getPageChanges() {
			return pageChanges;
		}

		public PrivateUpdates getPrivateUpdates() {
			return privateUpdates;
		}
	}
}
