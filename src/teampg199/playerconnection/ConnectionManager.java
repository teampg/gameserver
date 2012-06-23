package teampg199.playerconnection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import teampg199.changeout.PageChangeSubscriber;
import teampg199.entity.dyn.acting.player.ActionCommand;
import teampg199.entity.dyn.acting.player.JoinCmd;
import teampg199.entity.dyn.acting.player.DieCmd;
import teampg199.entity.dyn.acting.spawner.PlayerSpawnShrine;
import teampg199.server.TranslatingBuffer;
import teampg199.server.PageChanges;
import teampg199.world.board.BoardInfo;

public class ConnectionManager implements PageChangeSubscriber {
	TranslatingBuffer buffer;
	PlayerSpawnShrine spawner;

	GameStateTracker gameState;

	Map<Integer, PlayerConnection> players;

	public ConnectionManager(TranslatingBuffer buffer,
			BoardInfo infoForThisBoard, PlayerSpawnShrine spawnAt) {
		this.buffer = buffer;
		spawner = spawnAt;

		gameState = new GameStateTracker(infoForThisBoard);

		players = new HashMap<>();
	}

	/**
	 * Receives action requests from buffer, gives them to PlayerConnection's
	 * avatar or SpawnShrine.
	 *
	 * Adds new connections, parts disconnected
	 */
	public void loadActions() {
		updatePlayerList();

		for (Map.Entry<Integer, PlayerConnection> kvp : players.entrySet()) {
			// load desired actions for this connection from buffer
			Collection<ActionCommand> desiredActions;
			{
				int connectionID = kvp.getKey();
				desiredActions = buffer.popActions(connectionID);
			}

			if (desiredActions.size() == 0) {
				continue;
			}

			// execute actions differently depending on if player is spawned
			PlayerConnection connection = kvp.getValue();
			switch (connection.getStatus()) {
			case NOT_JOINED:
				// when not joined, can only join
				// TODO filter illegal actions for this state (eg not join)
				assert (desiredActions.size() == 1);
				assert (desiredActions.iterator().next() instanceof JoinCmd);

				JoinCmd j = (JoinCmd) desiredActions.iterator().next();
				spawner.doSpawn(connection, j);
				break;
			case SPAWNED_HAS_BOARD:
			case SPAWNED_HAS_NOT_RECEIVED_BOARD:
				// TODO filter illegal actions for this state (eg join)
				connection.getAvatar().addActions(desiredActions);
				break;
			default:
				throw new IllegalStateException(
						"Undefined PlayerConnection state");
			}
		}
	}

	private void updatePlayerList() {
		// add newly connected players
		for (Integer added : buffer.getAdded()) {
			players.put(added, new PlayerConnection());
		}

		// part disconnected players
		for (Integer removed : buffer.getRemoved()) {
			PlayerConnection connection = players.get(removed);

			switch (connection.getStatus()) {
			case NOT_JOINED:
				// nothing else to do; player hasn't been added anywhere
				break;
			case SPAWNED_HAS_BOARD:
			case SPAWNED_HAS_NOT_RECEIVED_BOARD:
				// must remove from board
				connection.getAvatar().addAction(new DieCmd());
				break;
			default:
				throw new IllegalStateException(
						"Undefined PlayerConnection state");
			}

			players.remove(removed);
		}
	}


	/**
	 * Called at end of each turn.  Given global changes for the turn,
	 * passes them to client connections, along with their
	 * PlayerConnection's private updates.
	 */
	@Override
	public void addChanges(PageChanges globalChanges) {
		// track current game state, to send to new connectors
		gameState.addTurnChanges(globalChanges);

		PageChanges blankGlobalChanges = new PageChanges();

		for (Map.Entry<Integer, PlayerConnection> kvp : players.entrySet()) {
			int connectionID = kvp.getKey();
			PlayerConnection connection = kvp.getValue();

			switch (connection.getStatus()) {
			case NOT_JOINED:
				buffer.pushChanges(blankGlobalChanges,
						connection.popPrivateUpdates(),
						null, connectionID);
				break;
			case SPAWNED_HAS_NOT_RECEIVED_BOARD:
				// send full board, initially
				buffer.pushChanges(gameState.getCurrentGameState(),
						connection.popPrivateUpdates(),
						gameState.getBoardInfo(), connectionID);
				connection.stateSetSpawnedHasBoard();
				break;
			case SPAWNED_HAS_BOARD:
				buffer.pushChanges(globalChanges,
						connection.popPrivateUpdates(),
						null, connectionID);
				break;
			default:
				throw new IllegalStateException(
						"Undefined Player Connection State");
			}
		}
	}
}
