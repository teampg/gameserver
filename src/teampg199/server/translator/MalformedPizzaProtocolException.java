package teampg199.server.translator;

public class MalformedPizzaProtocolException extends Exception {
	private static final long serialVersionUID = 3424060653044806375L;
	private final String reason;
	private final String malformedMessage;

	public MalformedPizzaProtocolException(String reason, String malformedMessage) {
		this.reason = reason;
		this.malformedMessage = malformedMessage;
	}

	public String getReason() {
		return reason;
	}

	public String getMalformedMessage() {
		return malformedMessage;
	}
}
