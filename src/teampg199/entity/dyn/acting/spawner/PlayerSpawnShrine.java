package teampg199.entity.dyn.acting.spawner;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import teampg.grid2d.point.BoundedPos;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.dyn.acting.ActingEntity;
import teampg199.entity.dyn.acting.player.JoinCmd;
import teampg199.entity.dyn.acting.player.JoinCmd.JoinUpdate;
import teampg199.entity.dyn.acting.player.Player;
import teampg199.playerconnection.PlayerConnection;
import teampg199.world.WorldPage;

import com.google.common.collect.ImmutableList;

public class PlayerSpawnShrine extends ActingEntity {
	private static final int SPAWN_RADIUS = 5;

	public PlayerSpawnShrine(WorldPage page) {
		super(page);
	}

	public void doSpawn(PlayerConnection playerConnection, JoinCmd joinCmd) {
		ValidNameFinder names = new ValidNameFinder(getPage());
		SpawnPointFinder spawns = new SpawnPointFinder(getPage(), this, SPAWN_RADIUS);

		if (!spawns.hasNext()) {
			JoinUpdate failedNoRoom = new JoinUpdate(joinCmd,
					JoinUpdate.CompletionState.FAILED_NO_SPACE);
			playerConnection.stateExitNotJoined(this, failedNoRoom);
			return;
		}

		// get a spawn point
		BoundedPos spawnPoint = spawns.getSpawnPoint();

		// get a name
		String assignedName = names.getSimilarValidName(joinCmd.getName());

		// make player entity
		Player avatar = new Player(getPage(), playerConnection, assignedName);
		getPage().getMap().set(spawnPoint, avatar);

		// note success
		JoinUpdate succeeded = new JoinUpdate(joinCmd, avatar);
		playerConnection.stateExitNotJoined(this, succeeded);
	}

	@Override
	public void tick() {
	}

	private static class ValidNameFinder {
		private final Collection<String> ILLEGAL_NAMES = new ImmutableList.Builder<String>()
				.add("console").build();
		private final String DEFAULT_NAME = "Guest";
		private final Set<String> usedNames;

		public ValidNameFinder(WorldPage page) {
			// Get all names already in use
			Set<String> usedNames = new HashSet<>();
			for (DynamicEntity d : page.getFact()) {
				if (d instanceof Player) {
					Player p = (Player) d;
					usedNames.add(p.getName());
				}
			}

			this.usedNames = usedNames;
		}

		public String getSimilarValidName(String proposedName) {
			String fixedName = proposedName.replace(' ', '_');

			for (String illegalName : ILLEGAL_NAMES) {
				if (fixedName.equalsIgnoreCase(illegalName)) {
					fixedName = DEFAULT_NAME;
					break;
				}
			}

			if (usedNames.contains(fixedName)) {
				String altName = getAlternateNameFor(fixedName);
				usedNames.add(altName);
				return altName;
			}

			usedNames.add(fixedName);
			return fixedName;
		}

		private String getAlternateNameFor(String toFindAltFor) {
			int nameSuffix = 0;
			String nextNameToTry;
			do {
				nameSuffix++;
				nextNameToTry = toFindAltFor + "_" + nameSuffix;
			} while (usedNames.contains(nextNameToTry));

			return nextNameToTry;
		}
	}
}
