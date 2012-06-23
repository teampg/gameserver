package teampg199.server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import teampg199.Logger;
import teampg199.server.translator.MalformedPizzaProtocolException;

/**
 *
 * Manages the Clients
 *
 * @author Justin Rempel
 * @author Jackson Williams
 * @author Brody Holden
 */
public class WebSocketManagerApp extends WebSocketApplication {
	private static final String RECEIVED_INVALID_MESSAGE = "{\"error\":\"malformed_protocol\"}";
	private static final String GENERIC_READING_ERROR = "{\"error\":\"parsing\"}";

	private int uniqueIdentifier;

	private final Map<WebSocket, Integer> clientIDs;
	private final TranslatingBuffer buffer;

	public WebSocketManagerApp(TranslatingBuffer buffer) {
		uniqueIdentifier = 0;
		this.buffer = buffer;
		clientIDs = new ConcurrentHashMap<>();
	}

	@Override
	public void onConnect(WebSocket socket) {
		super.onConnect(socket);
		clientIDs.put(socket, uniqueIdentifier++);
		buffer.add(clientIDs.get(socket));
	}

	@Override
	public void onClose(WebSocket socket, DataFrame frame) {
		super.onClose(socket, frame);
		removeClient(socket);
	}

	private synchronized void removeClient(WebSocket toDC) {
		assert !toDC.isConnected() : toDC;

		// if already removed once, don't again
		/* TODO is this needed/does this fix bug:
		 * java.lang.NullPointerException
		at teampg199.playerconnection.ConnectionManager.loadActions(ConnectionManager.java:46)
		 */
		if (!clientIDs.containsKey(toDC)) {
			return;
		}

		buffer.remove(clientIDs.get(toDC));
		clientIDs.remove(toDC);
	}

	@Override
	public void onMessage(WebSocket socket, String msg) {
		Logger.log("#WS-MSG C" + clientIDs.get(socket) + "->WSS# " + msg);
		try {
			buffer.pushAction(clientIDs.get(socket), msg);
		} catch (IOException e) {
			Logger.log("TRANSLATOR ERROR PARSING " + e.getMessage());
			socket.send(GENERIC_READING_ERROR);
		} catch (MalformedPizzaProtocolException e) {
			Logger.log("#WS-ERROR C" + clientIDs.get(socket) + "->WSS "
					+ e.getReason() + "# " + msg);
			socket.send(RECEIVED_INVALID_MESSAGE);
		} catch (AssertionError | IllegalStateException e) {
			Logger.log("#FATAL ERROR ON MESSAGE# " + msg + " ### "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void tick() {
		for (Map.Entry<WebSocket, Integer> kvp : clientIDs.entrySet()) {
			WebSocket clientSocket = kvp.getKey();
			Integer clientID = kvp.getValue();


			String personalUpdate = buffer.popChanges(clientID);
			
			if (personalUpdate.isEmpty()) {
				continue;
			}

			if (!clientSocket.isConnected()) {
				removeClient(clientSocket);
				continue;
			}
			clientSocket.send(personalUpdate);
		}
	}

	@Override
	public boolean isApplicationRequest(HttpRequestPacket request) {
		return true;
	}
}