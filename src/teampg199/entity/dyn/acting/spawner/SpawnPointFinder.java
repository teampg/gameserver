package teampg199.entity.dyn.acting.spawner;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import teampg.grid2d.point.BoundedPos;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.stat.Empty;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

class SpawnPointFinder {
	Iterator<BoundedPos> iter;

	public SpawnPointFinder(WorldPage page, DynamicEntity spawnCenter, int spawnRadius) {
		Board map = page.getMap();
		BoundedPos center = map.getPos(spawnCenter);
		
		Set<BoundedPos> validSpawnSet = map
				.getPointsNear(center, spawnRadius);
		for (Iterator<BoundedPos> iter = validSpawnSet.iterator(); iter.hasNext();) {
			BoundedPos candidateSpawnPoint = iter.next();

			if (!(map.get(candidateSpawnPoint) instanceof Empty)) {
				iter.remove();
			}
		}

		// TODO, should this maybe be a stack?...
		List<BoundedPos> validSpawnPoints = new ArrayList<>(validSpawnSet);
		
		// randomize
		Collections.shuffle(validSpawnPoints);

		iter = validSpawnPoints.iterator();
	}

	public boolean hasNext() {
		return iter.hasNext();
	}

	public BoundedPos getSpawnPoint() {
		if (!iter.hasNext()) {
			throw new IllegalStateException("Not enough spawn points");
		}

		BoundedPos sp = iter.next();
		iter.remove();
		return sp;
	}
}
