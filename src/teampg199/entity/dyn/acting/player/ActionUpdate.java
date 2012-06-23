package teampg199.entity.dyn.acting.player;

public class ActionUpdate {
	protected final ActionCommand forAction;

	protected ActionUpdate(ActionCommand forAction) {
		this.forAction = forAction;
	}

	public ActionCommand getForAction() {
		return forAction;
	}
}
