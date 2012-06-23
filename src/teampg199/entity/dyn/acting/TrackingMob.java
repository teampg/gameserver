package teampg199.entity.dyn.acting;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import teampg.grid2d.GridInterface.Entry;
import teampg.grid2d.point.BoundedPos;
import teampg.grid2d.point.Pos2D;
import teampg.grid2d.point.RelPos;
import teampg199.entity.Entity;
import teampg199.entity.dyn.DynamicEntity;
import teampg199.entity.dyn.acting.player.Player;
import teampg199.entity.stat.EntityManager;
import teampg199.world.WorldPage;
import teampg199.world.board.Board;
import teampg199.world.board.Board.BoardDistanceComparator;
import teampg199.world.pathfinding.AStarPather;
import teampg199.world.pathfinding.AStarPather.StarPath;

import com.google.common.base.Predicate;

public class TrackingMob extends ActingEntity {
	private enum State {
		LOOKING_FOR_TARGET, TRACKING, ADJACENT_TO_PLAYER
	};

	private static final Random GEN = new Random();
	
	private static final int MAX_MOVE_COOLDOWN = 40;
	private static final int MIN_MOVE_COOLDOWN = 5;
	private static final int AGRO_RANGE = 10;

	// INSTANCE VARS
	private State status;
	private int tickCooldown;

	// TRACKING extended state
	private DynamicEntity trackedEntity;

	public TrackingMob(WorldPage page) {
		super(page);

		tickCooldown = 0;

		status = State.LOOKING_FOR_TARGET;
	}

	@Override
	public void nudge(ActingEntity mover, RelPos movementVector) {
		// hit by bullet
		if (mover instanceof Bullet) {
			Board map = getPage().getMap();
			EntityManager entities = getPage().getFact();

			// kill self
			entities.removeEntity(this);
			map.set(this, RelPos.ZERO, new Remains(getPage(), this));
		}
	}

	@Override
	public void tick() {
		Board map = getPage().getMap();
		Entry<Entity> myPos = map.get(this);

		if (tickCooldown > 0) {
			tickCooldown--;
			return;
		}
		tickCooldown = GEN.nextInt(MAX_MOVE_COOLDOWN - MIN_MOVE_COOLDOWN) + MIN_MOVE_COOLDOWN;

		// TODO don't calculate this every turn... bit hacky. Should probably
		// listen to changes for removing the target.
		if (trackedEntity == null || !map.contains(trackedEntity)) {
			status = State.LOOKING_FOR_TARGET;
		}

		switch (status) {
		case LOOKING_FOR_TARGET:
			trackedEntity = findTarget();

			if (trackedEntity == null) {
				status = State.LOOKING_FOR_TARGET;
				break;
			}

			if (myPos.distance(map.get(trackedEntity)) == 1) {
				status = State.ADJACENT_TO_PLAYER;
				break;
			}

			status = State.TRACKING;
			break;
		case ADJACENT_TO_PLAYER:
			// TODO attack player when adjacent
			status = State.LOOKING_FOR_TARGET;
			tick();
			break;
		case TRACKING:
			Entry<Entity> targetPlayer = map.get(trackedEntity);
			StarPath tripPlan = AStarPather.findPath(myPos, targetPlayer, map);

			// arrived at destination... TODO wait or something
			if (tripPlan.isEmpty()) {
				status = State.LOOKING_FOR_TARGET;
				break;
			}

			BoundedPos targetMove = tripPlan.pop();

			doMove(targetMove);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void doMove(BoundedPos target) {
		assert status == State.TRACKING : status;
		Board map = getPage().getMap();
		BoundedPos myPos = map.getPos(this);

		// try to do move
		boolean moveSucceeded = moveIfEmpty(Pos2D.absToRel(myPos, target));

		// if we can't move...
		if (moveSucceeded == false) {
			return;
		}
	}

	private DynamicEntity findTarget() {
		Board map = getPage().getMap();
		BoundedPos myPos = map.getPos(this);

		// find and sort by proximity all players within AGRO_RANGE
		List<Entry<Entity>> sortedPlayers;
		{
			{
				Predicate<Entry<Entity>> isAPlayer = new Predicate<Entry<Entity>>() {
					@Override
					public boolean apply(Entry<Entity> entry) {
						// TODO change back to player!!
						return (entry.getContents() instanceof DrunkMob) || (entry.getContents() instanceof Player);
					}
				};
				sortedPlayers = map.findMatchingEntities(isAPlayer);
			}

			// no players at all?
			if (sortedPlayers.isEmpty()) {
				return null;
			}

			// order by proximity to this (descending order of proximity)
			Comparator<Entry<Entity>> byProximity = new BoardDistanceComparator(
					myPos);
			Collections.sort(sortedPlayers, byProximity);

			// ignore players that are outside AGRO_RANGE
			{
				// find point in sorted list where distance to this is bigger
				// than AGRO_RANGE
				int indexWhereTooBigEntriesStart = 0;
				for (Entry<Entity> candidatePlayer : sortedPlayers) {
					if (candidatePlayer.distance(myPos) > AGRO_RANGE) {
						break;
					}
					indexWhereTooBigEntriesStart++;
				}

				sortedPlayers = sortedPlayers.subList(0,
						indexWhereTooBigEntriesStart);
			}

		}
		// no players within agro range?
		if (sortedPlayers.isEmpty()) {
			return null;
		}

		// find easiest player to reach
		StarPath shortest = findEasiestPlayerToPathTo(sortedPlayers);

		// no possible complete or partial path
		if (shortest.isEmpty()) {
			return null;
		}

		return (DynamicEntity) map.get(shortest.getGoal());

	}

	/**
	 * Find which player is the easiest to path to, as dictated by Path.compareTo()
	 *
	 * @param goals
	 *            List of goals to be considered.
	 * @return Best path
	 */
	private StarPath findEasiestPlayerToPathTo(List<Entry<Entity>> goals) {
		Board map = getPage().getMap();
		Entry<Entity> myPos = map.get(this);

		List<AStarPather.StarPath> possiblePaths = new ArrayList<>();
		for (Entry<Entity> goalPos : goals) {
			StarPath plannedPath = AStarPather.findPath(myPos, goalPos, map);

			// destination unreachable
			if (plannedPath == null) {
				continue;
			}

			possiblePaths.add(plannedPath);
		}

		// should never happen; each goal must provide its empty path
		assert !possiblePaths.isEmpty();

		StarPath bestPath = Collections.min(possiblePaths);
		return bestPath;
	}
}
