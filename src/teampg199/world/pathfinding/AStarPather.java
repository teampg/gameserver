package teampg199.world.pathfinding;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import teampg.grid2d.GridInterface.Entry;
import teampg.grid2d.point.BoundedPos;
import teampg199.entity.Entity;
import teampg199.world.board.Board;
import teampg199.world.pathfinding.NodeMap.Node;

/**
 * TODO: only 1 pather for the entire map. It tracks planned paths. Stores the
 * list of every board-point, associated with an array. For each point in the
 * grid, for each turn into the future, we know which turns it will be occupied.
 * Allows us to say, given a time and a point, how long you'd have to wait to
 * pass through that point.
 *
 * Also, use 'cost from start' instead of 'dist from start' -- cells where we
 * have to wait (eg) 2 turns will cost 3 times as much. Might also want to
 * assign a higher cost to moving near an unpredictably mobile entity, like a
 * Wanderer, or a Player.
 *
 * Don't calculate path every turn? Shouldn't have to -- unless I guess
 * something unpredictable (eg not using this pather) happens along our planned
 * path.
 *
 * Clean up structure. This is pretty gross. Separate files, anyone?
 *
 * @author jackson
 */
public abstract class AStarPather {
	private static final int ACCEPTABLE_PATH_INCOMPLETENESS = 10;

	private AStarPather() {
	}

	public static StarPath findPath(Entry<Entity> startEntry, Entry<Entity> goalEntry, Board realMap) {
		NodeMap map = new NodeMap(realMap, goalEntry.getPosition(), startEntry.getPosition());

		return findPathInternal(map);
	}

	static StarPath findPathInternal(NodeMap map) {
		PriorityQueue<Node> openSet = new PriorityQueue<>(50);
		openSet.add(map.getStartNode());

		List<Node> closedSet = new ArrayList<>();

		while (!openSet.isEmpty()) {
			Node current = openSet.poll();
			BoundedPos currentPos = current.getPos();

			// found path leading to goal
			if (currentPos.equals(map.goal)) {
				return new StarPath(current, map.goal);
			}

			closedSet.add(current);

			Node[] neighbors = map.getNeighbours(currentPos);

			for (Node target : neighbors) {
				Integer targetMoveIntoCost = map.getCost(target.getPos());

				if (closedSet.contains(target)) {
					continue;
				}

				int potentialCostFromStart = current.getDistFromStart() + targetMoveIntoCost;
				int targetCostFromStart = target.getDistFromStart();

				// if we haven't looked at this node yet OR found a better path to this node
				if (potentialCostFromStart < targetCostFromStart || !openSet.contains(target)) {
					openSet.add(target);

					target.setParent(current);
				}
			}
		}

		// TODO if no complete paths, try less complete
		return getBestIncompletePath(map);
	}

	static StarPath getBestIncompletePath(NodeMap map) {
		// even if closer to goal than ACCEPTABLE_INCOMPLETENESS, moving away is
		// not acceptable.
		int searchRadius;
		{
			searchRadius = ACCEPTABLE_PATH_INCOMPLETENESS;
			int actualDistFromGoal = map.getStartNode().getPos().distance(map.goal);

			if (actualDistFromGoal <= searchRadius) {
				searchRadius = actualDistFromGoal - 1;
			}
		}

		List<StarPath> candidateIncompletePaths = new ArrayList<>();

		// include option of not moving
		{
			StarPath sitStill = new StarPath(map.getStartNode(), map.goal);
			candidateIncompletePaths.add(sitStill);
		}

		// find all existing nodes near enough to goal
		Node[] possiblePathEnds = map.findProximateNodes(map.goal, ACCEPTABLE_PATH_INCOMPLETENESS);

		for (Node nodeToTry : possiblePathEnds) {
			// ignore start, already added it
			if (nodeToTry.getParent() == null) {
				continue;
			}

			candidateIncompletePaths.add(new StarPath(nodeToTry, map.goal));
		}

		// return best incomplete path
		StarPath bestDiscoveredPath = Collections.min(candidateIncompletePaths);
		return bestDiscoveredPath;
	}

	public static class StarPath implements Iterable<BoundedPos>, Comparable<StarPath> {
		private static final double INCOMPLETENESS_COST_PER_SQUARE = 10D;

		private final Stack<Node> points;
		private final BoundedPos goal;
		private final Node endOfTheLine;

		private final int distanceShortOfGoal;

		StarPath(Node endOfTheLine, BoundedPos goalPos) {
			points = getPath(endOfTheLine);
			this.endOfTheLine = endOfTheLine;
			goal = goalPos;

			distanceShortOfGoal = endOfTheLine.getPos().distance(goalPos);
		}

		private Stack<Node> getPath(Node endOfTheLine) {
			Stack<Node> nodesInPath = new Stack<>();

			Node cursor = endOfTheLine;
			// don't include start node
			while (cursor.getParent() != null) {
				nodesInPath.push(cursor);
				cursor = cursor.getParent();
			}

			return nodesInPath;
		}

		public BoundedPos getGoal() {
			return goal;
		}

		public boolean isEmpty() {
			return points.isEmpty();
		}

		public BoundedPos pop() {
			return points.pop().getPos();
		}

		public BoundedPos peek() {
			return points.peek().getPos();
		}

		public BoundedPos get(int at) {
			return points.get(at).getPos();
		}

		public int size() {
			return points.size();
		}

		public BoundedPos getEndPoint() {
			return endOfTheLine.getPos();
		}

		@Override
		public int compareTo(StarPath other) {
			int cost = endOfTheLine.getDistFromStart();
			int otherCost = other.endOfTheLine.getDistFromStart();

			cost += distanceShortOfGoal * INCOMPLETENESS_COST_PER_SQUARE;
			otherCost += other.distanceShortOfGoal * INCOMPLETENESS_COST_PER_SQUARE;

			return cost - otherCost;
		}

		@Override
		public Iterator<BoundedPos> iterator() {
			final Iterator<Node> iter = points.iterator();
			return new Iterator<BoundedPos>() {
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}

				@Override
				public BoundedPos next() {
					return iter.next().getPos();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Remove not implemented");
				}
			};
		}

		@Override
		public String toString() {
			String ret = "StarPath [goal=" + goal + ", distanceShortOfGoal=" + distanceShortOfGoal
					+ ", endOfTheLine=" + endOfTheLine + ", points=";

			for (Node n : points) {
				ret += "\n" + n;
			}

			return ret + "EOL\n\n";
		}

	}

}
