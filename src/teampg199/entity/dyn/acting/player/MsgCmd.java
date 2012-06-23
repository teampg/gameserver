package teampg199.entity.dyn.acting.player;

public class MsgCmd extends ActionCommand {
	private final String text;

	public MsgCmd(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
