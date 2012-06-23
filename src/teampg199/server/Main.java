package teampg199.server;

import java.io.File;
import java.io.FileNotFoundException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import teampg199.Game;

/**
 * Sets up the Game, Client Manager, and ActionChangeBuffer. Starts the game
 * loop.
 *
 * @author justin
 * @author JWill
 *
 */
public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		TranslatingBuffer buffer = new TranslatingBuffer();

		final WebSocketManagerApp wsManager = new WebSocketManagerApp(buffer);
		final HttpServer server = setupServer(wsManager);

		Game game = new Game(buffer, new File("justinsmap.za"));

		//DumbChangePrinter dumbPrinter = new DumbChangePrinter(System.out);
		//game.attachChangeSubscriber(dumbPrinter);

		try {
			server.start();
			while (true) {
				game.tick();
				wsManager.tick();
				Thread.sleep(50);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HttpServer setupServer(WebSocketManagerApp man) {
		HttpServer ret = HttpServer.createSimpleServer("", 9252);

		final WebSocketAddOn addon = new WebSocketAddOn();
		for (NetworkListener listener : ret.getListeners()) {
			listener.registerAddOn(addon);
		}

		WebSocketEngine.getEngine().register(man);
		return ret;
	}
}
