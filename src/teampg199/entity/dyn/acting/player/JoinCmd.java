package teampg199.entity.dyn.acting.player;

public class JoinCmd extends ActionCommand {
	private final String desiredName;

	public JoinCmd(String desiredName) {
		this.desiredName = desiredName;
	}

	public String getName() {
		return desiredName;
	}

	public static class JoinUpdate extends ActionUpdate {
		public enum CompletionState {SUCCESS, FAILED_NO_SPACE};
		private final CompletionState status;
		private final Player avatar;

		public JoinUpdate(ActionCommand forAction, Player avatar) {
			super(forAction);
			status = CompletionState.SUCCESS;
			this.avatar = avatar;
		}

		public JoinUpdate(ActionCommand forAction, CompletionState failureReason) {
			super(forAction);
			assert(failureReason != CompletionState.SUCCESS);
			status = failureReason;
			avatar = null;
		}

		public CompletionState getCompletionState() {
			return status;
		}

		public Player getAvatar() {
			return avatar;
		}
	}
}
