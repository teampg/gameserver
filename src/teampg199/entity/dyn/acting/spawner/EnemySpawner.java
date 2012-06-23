package teampg199.entity.dyn.acting.spawner;

import teampg.grid2d.point.AbsPos;
import teampg199.entity.dyn.acting.ActingEntity;
import teampg199.entity.dyn.acting.TrackingMob;
import teampg199.world.WorldPage;

public class EnemySpawner extends ActingEntity {
	private static final int SPAWN_RADIUS = 1;
	private static final int SPAWN_COOLDOWN = 100;

	private int spawnCooldownTimer;


	public EnemySpawner(WorldPage page) {
		super(page);
		spawnCooldownTimer = SPAWN_COOLDOWN;
	}

	private boolean doSpawn() {
		SpawnPointFinder spawns = new SpawnPointFinder(getPage(), this, SPAWN_RADIUS);

		if (!spawns.hasNext()) {
			return false;
		}

		// get a spawn point
		AbsPos spawnPoint = spawns.getSpawnPoint();

		// make enemy entity
		TrackingMob mob = new TrackingMob(getPage());
		getPage().getMap().set(spawnPoint, mob);

		return true;
	}

	@Override
	public void tick() {
		if (spawnCooldownTimer > 0) {
			spawnCooldownTimer--;
			return;
		}

		if (doSpawn()) {
			spawnCooldownTimer = SPAWN_COOLDOWN;
		}
	}
}
