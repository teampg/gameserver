package teampg199.server.translator;

import teampg199.entity.dyn.acting.Bullet;
import teampg199.entity.dyn.acting.DrunkMob;
import teampg199.entity.dyn.acting.PushableBlock;
import teampg199.entity.dyn.acting.Remains;
import teampg199.entity.dyn.acting.TrackingMob;
import teampg199.entity.dyn.acting.player.JoinCmd;
import teampg199.entity.dyn.acting.player.JoinCmd.JoinUpdate;
import teampg199.entity.dyn.acting.player.Player;
import teampg199.entity.dyn.acting.spawner.EnemySpawner;
import teampg199.entity.dyn.acting.spawner.PlayerSpawnShrine;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager.AddRemEntChange;
import teampg199.entity.stat.Wall;

import com.google.common.collect.ImmutableMap;

/**
 * Knows names Protocol expects to see, given some gameserver class.
 *
 * @author jackson
 */
public class ProtocolNameMap {
	private static final ImmutableMap<Object, String> names = new ImmutableMap.Builder<Object, String>()
			.put(Player.class, "player")
			.put(PlayerSpawnShrine.class, "spawn_shrine")
			.put(Wall.class, "wall")
			.put(Empty.class, "empty")
			.put(TrackingMob.class, "enemy")
			.put(Bullet.class, "bullet")
			.put(PushableBlock.class, "pushable_block")
			.put(DrunkMob.class, "wanderer")
			.put(Remains.class, "corpse")
			.put(EnemySpawner.class, "enemy_spawner")

			.put(JoinCmd.class, "join")

			.put(JoinUpdate.class, "join")

			.put(AddRemEntChange.Type.CREATED, "added")
			.put(AddRemEntChange.Type.DESTROYED, "removed")
			.build();

	public static String get(Object forObject) {
		assert(names.containsKey(forObject));
		return names.get(forObject);
	}

}
