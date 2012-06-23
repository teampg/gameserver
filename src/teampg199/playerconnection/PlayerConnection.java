package teampg199.playerconnection;

import java.util.ArrayList;
import java.util.List;

import teampg199.Logger;
import teampg199.entity.dyn.acting.player.ActionUpdate;
import teampg199.entity.dyn.acting.player.Player;
import teampg199.entity.dyn.acting.player.JoinCmd.JoinUpdate;
import teampg199.entity.dyn.acting.spawner.PlayerSpawnShrine;

public class PlayerConnection {
	public enum PlayerState {
		NOT_JOINED, SPAWNED_HAS_NOT_RECEIVED_BOARD, SPAWNED_HAS_BOARD
	};

	private PlayerState status;
	private List<ActionUpdate> privateUpdates;
	private List<Message> receivedMessages;
	private Player avatar;

	public PlayerConnection() {
		status = PlayerState.NOT_JOINED;
		privateUpdates = new ArrayList<>();
		receivedMessages = new ArrayList<>();

		Logger.log("#PLAYER-CONNECTION NEW# " + this);
	}

	public void stateSetSpawnedHasBoard() {
		assert (status == PlayerState.SPAWNED_HAS_NOT_RECEIVED_BOARD);
		status = PlayerState.SPAWNED_HAS_BOARD;
		Logger.log("#PLAYER-CONNECTION STATE-CHANGE# " + this );
	}

	public void stateExitNotJoined(PlayerSpawnShrine spawnAt,
			JoinUpdate joinStatus) {
		assert (status == PlayerState.NOT_JOINED);

		switch (joinStatus.getCompletionState()) {
		case FAILED_NO_SPACE:
			status = PlayerState.NOT_JOINED;
			break;
		case SUCCESS:
			status = PlayerState.SPAWNED_HAS_NOT_RECEIVED_BOARD;
			avatar = joinStatus.getAvatar();
			break;
		default:
			throw new IllegalStateException(
					"Undefined JoinUpdate completion state");
		}

		Logger.log("#PLAYER-CONNECTION STATE_CHANGE# " + this );
		privateUpdates.add(joinStatus);
	}

	public PlayerState getStatus() {
		return status;
	}

	Player getAvatar() {
		return avatar;
	}

	public void addActionUpdate(ActionUpdate update) {
		if (update instanceof JoinUpdate) {
			throw new IllegalArgumentException(
					"JoinUpdates should be sent through exitPendingSpawn to update PlayerConnection state");
		}

		privateUpdates.add(update);
	}

	PrivateUpdates popPrivateUpdates() {
		List<ActionUpdate> thisTurnUpdates = privateUpdates;
		privateUpdates = new ArrayList<>();

		List<Message> thisTurnMessages = receivedMessages;
		receivedMessages = new ArrayList<>();

		return new PrivateUpdates(thisTurnUpdates, thisTurnMessages);
	}

	@Override
	public String toString() {
		return "PlayerConnection [status=" + status + ", avatar=" + avatar
				+ "]";
	}

	public void receiveMessage(Player from, String text) {
		receivedMessages.add(new Message(from, text));
	}

	public class PrivateUpdates {
		private final List<ActionUpdate> actUpdates;
		private final List<Message> receivedMessages;

		private PrivateUpdates(List<ActionUpdate> actUpdates, List<Message> receivedMessages) {
			assert actUpdates != null;
			assert receivedMessages != null;

			this.actUpdates = actUpdates;
			this.receivedMessages = receivedMessages;
		}

		public List<ActionUpdate> getActionUpdates() {
			return actUpdates;
		}

		public List<Message> getReceivedMessages() {
			return receivedMessages;
		}

		public boolean isEmpty() {
			return actUpdates.isEmpty() && receivedMessages.isEmpty();
		}
	}

	public class Message {
		private final Player from;
		private final String text;

		private Message(Player from, String text) {
			this.from = from;
			this.text = text;
		}

		public Player getFrom() {
			return from;
		}

		public String getText() {
			return text;
		}
	}
}
