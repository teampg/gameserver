package teampg199;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;


import com.google.common.base.Predicate;

import teampg.grid2d.GridInterface.Entry;
import teampg199.changeout.PageChangeBroadcaster;
import teampg199.changeout.PageChangeSubscriber;
import teampg199.entity.Entity;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.dyn.EntityChange;
import teampg199.entity.dyn.acting.spawner.PlayerSpawnShrine;
import teampg199.entity.stat.EntityManager.AddRemEntChange;
import teampg199.playerconnection.ConnectionManager;
import teampg199.server.TranslatingBuffer;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;
import teampg199.world.board.BoardChange;
import teampg199.world.loader.WorldPageLoader;

public class Game {
	WorldPage page;
	PageChangeBroadcaster caster;
	ConnectionManager connectionManager;

	public Game(TranslatingBuffer buffer, File mapFile)
			throws FileNotFoundException {
		// setup output loggers
		caster = new PageChangeBroadcaster();

		// setup board
		page = WorldPageLoader.load(mapFile);
		Board map = page.getMap();

		// find SpawnShrine on loaded map
		PlayerSpawnShrine spawner;
		{
			List<Entry<Entity>> foundSpawners = map
					.findMatchingEntities(new Predicate<Entry<Entity>>() {
						@Override
						public boolean apply(Entry<Entity> someEntry) {
							return someEntry.getContents() instanceof PlayerSpawnShrine;
						}
					});
			if (foundSpawners.size() != 1) {
				// TODO better exception
				throw new IllegalStateException(
						"Loaded board doesn't have exactly 1 spawn shrine");
			}
			spawner = (PlayerSpawnShrine) foundSpawners.get(0).getContents();
		}

		// initialize client connection manager
		connectionManager = new ConnectionManager(buffer, map.getInfo(),
				spawner);
		caster.connectChangeSubscriber(connectionManager);
	}

	public void tick() {
		// get actions from clients
		connectionManager.loadActions();

		// let entities do their things
		for (DynamicEntity e : page.getFact()) {
			//TODO FIXME HACK HACK
			if (!page.getMap().contains(e)) {
				continue;
			}

			e.tick();
		}

		/*
		 * DO END OF TURN
		 */
		// log add/rem entity changes
		{
			List<AddRemEntChange> addedEntChanges = page.getFact().tick();
			caster.addNewEntityChanges(addedEntChanges);
		}

		// log all entity changes
		for (DynamicEntity e : page.getFact()) {
			List<EntityChange> changes = e.popChanges();
			caster.addEntityChanges(changes);
		}

		// log board changes
		{
			List<BoardChange> mapChanges = page.getMap().popChanges();
			caster.addBoardChanges(mapChanges);
		}

		/*
		 * SEND OUTPUT
		 */
		// broadcast all changes logged this turn
		caster.broadcastChanges();
	}

	public void attachChangeSubscriber(PageChangeSubscriber sub) {
		caster.connectChangeSubscriber(sub);
	}
}
