package teampg199.entity.dyn.acting.player;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;


import teampg.grid2d.GridInterface.Entry;
import teampg.grid2d.point.Pos2D;
import teampg.grid2d.point.RelPos;
import teampg199.Logger;
import teampg199.entity.Entity;
import teampg199.entity.dyn.acting.ActingEntity;
import teampg199.entity.dyn.acting.Bullet;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager;
import teampg199.playerconnection.PlayerConnection;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;

public class Player extends ActingEntity {
	private final String name;

	private final List<ActionCommand> actionsToExecute;
	private final PlayerConnection connection;

	private static final int MOVE_COOLDOWN_LENGTH = 3;
	private static final int SHOOT_COOLDOWN_LENGTH = 7;
	private static final double MOVE_DISTANCE = 1.5;
	private static final double SHOOT_MAX_MAGNITUDE = 1.5;

	private int moveCooldown;
	private int shootCooldown;

	public Player(WorldPage page, PlayerConnection connection, String name) {
		super(page);
		this.name = name;
		actionsToExecute = new ArrayList<>();
		this.connection = connection;

		moveCooldown = 0;
		shootCooldown = 0;
	}

	public String getName() {
		return name;
	}

	public void addAction(ActionCommand desiredAction) {
		actionsToExecute.add(desiredAction);
	}

	public void addActions(Collection<ActionCommand> desiredActions) {
		actionsToExecute.addAll(desiredActions);
	}

	@Override
	public void tick() {
		if (moveCooldown > 0) {
			moveCooldown--;
		}
		if (shootCooldown > 0) {
			shootCooldown--;
		}

		for (ActionCommand act : actionsToExecute) {
			if (act instanceof MoveCmd) {
				doMove((MoveCmd) act);
			} else if (act instanceof DieCmd) {
				doDie((DieCmd) act);
				// don't do any more actions
				break;
			} else if (act instanceof MsgCmd) {
				doMsg((MsgCmd) act);
			} else if (act instanceof ShootCmd) {
				doShoot((ShootCmd) act);
			} else if (act instanceof JoinCmd) {
				assert false : "Shouldn't receive a join from an already connected player -- TODO protocol error";
			} else {
				throw new IllegalStateException(
						"Unimplemented Action in Player");
			}
		}

		actionsToExecute.clear();
	}

	private void doMsg(MsgCmd act) {
		Board map = getPage().getMap();

		// get all players on this board
		List<Entry<Entity>> playerEntries;
		{
			Predicate<Entry<Entity>> isAPlayer = new Predicate<Entry<Entity>>() {
				@Override
				public boolean apply(Entry<Entity> entry) {
					return (entry.getContents() instanceof Player);
				}
			};
			playerEntries = map.findMatchingEntities(isAPlayer);
		}

		// send message
		for (Entry<Entity> playerEntry : playerEntries) {
			// don't send to self
			if (playerEntry.getContents() == this) {
				continue;
			}

			((Player) playerEntry.getContents()).connection.receiveMessage(this,
					act.getText());
		}
	}

	private void doDie(DieCmd act) {
		EntityManager entMan = getPage().getFact();
		entMan.removeEntity(this);

		Board map = getPage().getMap();
		map.set(this, RelPos.ZERO, entMan.getInstance(Empty.class));
	}

	private void doMove(MoveCmd moveCmd) {
		if (moveCooldown > 0) {
			// fail move
			return;
		}

		// illegal move distance
		if (Double.compare(Pos2D.magnitude(moveCmd.getRelativePosition()),
				MOVE_DISTANCE) > 0) {
			Logger.log("ACTION-VALIDATION: ILLEGAL MOVE: " + moveCmd);
			return;
		}

		moveIfEmpty(moveCmd.getRelativePosition());
		moveCooldown = MOVE_COOLDOWN_LENGTH;
	}

	private void doShoot(ShootCmd shootCmd) {
		if (shootCooldown > 0) {
			// fail shoot
			return;
		}

		// illegal shoot distance
		if (Double.compare(Pos2D.magnitude(shootCmd.getDirection()),
				SHOOT_MAX_MAGNITUDE) > 0) {
			Logger.log("ACTION-VALIDATION: ILLEGAL SHOOT: " + shootCmd);
			return;
		}

		Board board = getPage().getMap();
		EntityManager entities = getPage().getFact();

		RelPos shootingDirection = shootCmd.getDirection();
		Entity atNextPos = board.get(this, shootingDirection);

		// if pressed right up again an entity, hurt it but don't overwrite with
		// bullet
		if (!(atNextPos instanceof Empty)) {
			Bullet fakeBullet = new Bullet(getPage(), RelPos.ZERO);
			atNextPos.nudge(fakeBullet, shootingDirection);

			entities.removeEntity(fakeBullet);
			return;
		}

		Bullet bullet = new Bullet(getPage(), shootCmd.getDirection());
		board.set(this, shootingDirection, bullet);
		shootCooldown = SHOOT_COOLDOWN_LENGTH;
	}

	@Override
	public String toString() {
		return "Player [name=" + name + "]";
	}
}
