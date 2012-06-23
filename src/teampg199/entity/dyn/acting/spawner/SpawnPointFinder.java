package teampg199.entity.dyn.acting.spawner;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import teampg.grid2d.point.AbsPos;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.stat.Empty;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

class SpawnPointFinder {
	Iterator<AbsPos> iter;

	public SpawnPointFinder(WorldPage page, DynamicEntity spawnCenter, int spawnRadius) {
		Board map = page.getMap();
		AbsPos center = map.getPos(spawnCenter);
		
		Set<AbsPos> validSpawnSet = map
				.getPointsNear(center, spawnRadius);
		for (Iterator<AbsPos> iter = validSpawnSet.iterator(); iter.hasNext();) {
			AbsPos candidateSpawnPoint = iter.next();

			if (!(map.get(candidateSpawnPoint) instanceof Empty)) {
				iter.remove();
			}
		}

		// TODO, should this maybe be a stack?...
		List<AbsPos> validSpawnPoints = new ArrayList<>(validSpawnSet);
		
		// randomize
		Collections.shuffle(validSpawnPoints);

		iter = validSpawnPoints.iterator();
	}

	public boolean hasNext() {
		return iter.hasNext();
	}

	public AbsPos getSpawnPoint() {
		if (!iter.hasNext()) {
			throw new IllegalStateException("Not enough spawn points");
		}

		AbsPos sp = iter.next();
		iter.remove();
		return sp;
	}
}
