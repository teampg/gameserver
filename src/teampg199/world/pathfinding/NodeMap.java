package teampg199.world.pathfinding;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import teampg.grid2d.point.BoundedPos;
import teampg.grid2d.point.Pos2D;
import teampg199.entity.Entity;
import teampg199.entity.stat.Empty;
import teampg199.world.board.Board;

class NodeMap {
	static final int BASE_COST = 1;

	private final Board realMap;
	private final Map<BoundedPos, Node> examinedNodes;
	private final Node start;

	// final immutable datatype; safe to make public
	public final BoundedPos goal;

	NodeMap(Board realMap, BoundedPos goal, BoundedPos startPos) {
		this.goal = goal;

		this.realMap = realMap;
		examinedNodes = new HashMap<>();

		start = new Node(startPos);
	}

	public Node getStartNode() {
		return start;
	}

	public Node getNode(BoundedPos at) {
		assert examinedNodes.containsKey(at);

		return examinedNodes.get(at);
	}

	public Integer getCost(BoundedPos at) {
		Entity entAtPos = realMap.get(at);

		if (!(entAtPos instanceof Empty)) {
			return null;
		}

		return BASE_COST;
	}

	public int getEstimatedCost(BoundedPos from, BoundedPos to) {
		return Pos2D.squareDistance(from, to) * BASE_COST;
	}

	public boolean hasNode(BoundedPos position) {
		return examinedNodes.containsKey(position);
	}

	/**
	 * Fetches directly adjacent nodes. Creates nodes if not previously
	 * existent. Ignores un-walkable positions.
	 */
	public Node[] getNeighbours(BoundedPos near) {
		// only move diagonal
		Set<BoundedPos> nearPoints = realMap.getPointsNear(near, 1);

		// remove non-walkable points
		for (Iterator<BoundedPos> iter = nearPoints.iterator(); iter.hasNext();) {
			BoundedPos p = iter.next();

			if (getCost(p) == null) {
				iter.remove();
			}
		}

		Node[] neighbours = new Node[nearPoints.size()];
		// for each neighboring point, fetch existing node or create a new one
		// if not already created
		{
			int i = 0;
			for (BoundedPos p : nearPoints) {
				// create node if not already made
				if (!examinedNodes.containsKey(p)) {
					neighbours[i] = new Node(p);
					i++;
					continue;
				}

				neighbours[i] = examinedNodes.get(p);
				i++;
			}
		}

		return neighbours;
	}

	/**
	 * Fetches all nodes in a diamond. Does not create new nodes.
	 */
	public Node[] findProximateNodes(BoundedPos near, int radius) {
		if (radius <= 0) {
			throw new IllegalArgumentException("Radius must be greater than 0");
		}

		Set<BoundedPos> diamondPoints = realMap.getPointsNear(near, radius);

		// only get existing nodes
		ArrayList<Node> diamondNodes = new ArrayList<>();
		for (BoundedPos p : diamondPoints) {
			if (hasNode(p)) {
				diamondNodes.add(getNode(p));
			}
		}

		// convert to array
		Node[] toRet = new Node[diamondNodes.size()];
		diamondNodes.toArray(toRet);

		return toRet;
	}

	class Node implements Comparable<Node> {
		private final BoundedPos pos;

		private int costFromStart;
		private int estTotalCost;

		private Node parent;

		private Node(BoundedPos position) {
			assert(!examinedNodes.containsKey(position));

			pos = position;

			if (start != null && !pos.equals(start.pos)) {
				assert getCost(pos) != null : "Non-walkable position: " + pos;
			}

			costFromStart = 0;
			estTotalCost = getEstimatedCost(pos, goal);

			examinedNodes.put(pos, this);
		}

		public BoundedPos getPos() {
			return pos;
		}

		public int getDistFromStart() {
			return costFromStart;
		}

		public void setParent(Node parent) {
			this.parent = parent;

			// my cost from start is parent plus my cost
			costFromStart = parent.costFromStart + getCost(pos);
			estTotalCost = costFromStart + getEstimatedCost(pos, goal);
		}

		public Node getParent() {
			return parent;
		}

		private int getTotalCost() {
			return estTotalCost;
		}

		@Override
		public int compareTo(Node other) {
			// lower dist is smaller
			return getTotalCost() - other.getTotalCost();
		}

		@Override
		public int hashCode() {
			return pos.hashCode();
		}

		@Override
		public String toString() {
			return "Node [pos=" + pos + ", distFromStart=" + costFromStart + ", totalCost="
					+ estTotalCost + "]";
		}

		@Override
		public boolean equals(Object what) {
			Node other = (Node) what;
			return pos.equals(other.pos);
		}
	}
}
