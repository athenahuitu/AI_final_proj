package pacman.controllers.examples;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import pacman.game.Game;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.controllers.Controller;

/*
 * The Class RandomPacMan.
 */
public final class AwesomePacMan extends Controller<MOVE>
{
	public static final int DEPTH = 10; // Search depth
	
	// Node Cost
	public static final double EMPTY = 3; 
	public static final double PILL = 2;
	public static final double POWER = 1;
	public static final double GHOST_IN_COST = 2000;
	public static final double GHOST_OUT_COST = 500;
	
	private Random rnd=new Random();
	
	public AwesomePacMan() {
		
	}
	
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	public MOVE getMove(Game game,long timeDue)
	{
		int dest = getDestination(game);
		int source = game.getPacmanCurrentNodeIndex();
		MOVE move = AStar(source, dest, game);
		return move;
	}
	
	/* TODO */
	/* Get the destination accordingly */
	private int getDestination(Game game){
		int value[] = game.getPillIndices();
		int index = rnd.nextInt(value.length);
		return value[index];
	}
	
	/* Use A* to search for the best next move.
	 * Cost model: empty->3, pill->2, power pill->1
	 * Ghost cost:
	 * Corner cost:
	 */
	private MOVE AStar(int src, int dest, Game game) {
		HeapNodeComparator comparator	 = new HeapNodeComparator();
		PriorityQueue<PathNode> pathHeap = new PriorityQueue<PathNode>(10, comparator);
		
		// Branch on src node
		branch(pathHeap, src, dest, game);
		
		while(true) {
			PathNode minCostNode = pathHeap.poll();
			// Find dest
			if(minCostNode.current == dest || minCostNode.path.size() > DEPTH) {
				return minCostNode.path.getFirst();
			}
			branch(pathHeap, minCostNode, dest, game);
		}
	}
	
	// check if the item is on the path
	public boolean isInPath(int[] path, int item) {
		for(int i = 0; i < path.length; i ++) {
			if(path[i] == item) return true;
		}
		return false;
	}
	
	private int getGhostIndex(GHOST	ghost) {
		if(ghost == GHOST.BLINKY) return 0;
		if(ghost == GHOST.PINKY) return 1;
		if(ghost == GHOST.INKY) return 2;
		else return 3;
	}
	
	// Branch case1: heap is empty
	// Create all valid neighbor PathNode based on src, and add them into heap
	private void branch(PriorityQueue<PathNode> heap, int src, int dest, Game game) {
		for(MOVE move : MOVE.values()) {
			int neighbor = game.getNeighbour(src, move);
			if(neighbor == -1) continue;
			PathNode node = new PathNode(move, neighbor);
			node.computeCost(dest, game);
			heap.add(node);
		}
	}
	
	// Branch case2: heap is not empty
	private void branch(PriorityQueue<PathNode> heap, PathNode node, int dest, Game game) {
		for(MOVE move : MOVE.values()) {
			// If move is exactly the opposite to last move that takes PacMan to branch node
			// Then we don't need to branch on this move
			if(move.opposite() == node.path.getLast()) continue;
			int neighbor = game.getNeighbour(node.current, move);
			if(neighbor == -1) continue;
			PathNode newNode = new PathNode(node, move, neighbor);
			newNode.computeCost(dest, game);
			heap.add(newNode);
		}
	}
	
	class PathNode {
		LinkedList<MOVE> path;
		int current; // the index of this pathNode
		double cost; // total cost of this pathNode
		double nodeCost;
		double[] ghostCost;
		
		public PathNode(MOVE move, int current) {
			path = new LinkedList<MOVE>();
			path.add(move);
			this.current = current;
			this.ghostCost = new double[4];
			this.nodeCost = 0;
		}
		
		public PathNode(PathNode prevNode, MOVE move, int current) {
			// Copy the prevPath to current path
			Iterator<MOVE> it = prevNode.path.iterator();
			path = new LinkedList<MOVE>();
			while(it.hasNext()) {
				path.add(it.next());
			}
			path.add(move);
			
			this.nodeCost = prevNode.nodeCost;
			this.current = current;
			this.ghostCost = new double[4];
			for(int i  = 0; i < ghostCost.length; i ++) {
				ghostCost[i] = prevNode.ghostCost[i];
			}
		}
		
		/*
		 * compute cost according our cost model
		 */
		public void computeCost(int dest,  Game game) {
			int distance = game.getShortestPathDistance(current, dest);
			double heuristic = POWER + (distance - 1) * PILL;
			
			// update node cost
			if(game.isPillStillAvailable(current)) 
				nodeCost += PILL;
			else if(game.isPowerPillStillAvailable(current))
				nodeCost += POWER;
			else 
				nodeCost += EMPTY;
			
			// update ghost cost
			double ghostCost = computeCurGhostCost(game);
			// prev corner cost
			// Total cost
			cost = heuristic + nodeCost + ghostCost;
		}
		
		/* Compute ghost cost on current position */
		public double computeCurGhostCost(Game game) {
			double cost;
			for(GHOST ghost : GHOST.values()) {
				int index = getGhostIndex(ghost);
				// If ghost is edible or prev ghost cost is GHOST_IN_COST, no need to update
				if(game.isGhostEdible(ghost) || ghostCost[index] == GHOST_IN_COST) {
					continue;
				} if(index == current) { // the ghost is exactly at current position
					ghostCost[index] = GHOST_IN_COST;
				}
				else {
					int ghostIndex = game.getGhostCurrentNodeIndex(ghost);
					MOVE ghostMove = game.getGhostLastMoveMade(ghost);
					// The distance from ghost to current position, last move considered
					int distance = game.getShortestPathDistance(ghostIndex, current, ghostMove);
					
					// if it's initial state
					if(distance == 0) {
						ghostCost[index] = 0;
					} else {
						cost = GHOST_OUT_COST / distance;
						if(cost > ghostCost[index]) ghostCost[index] = cost;
					}
				}
			}
			
			// Add up ghost cost and return
			double totalCost = 0;
			for(int i = 0; i < ghostCost.length; i ++) {
				totalCost += ghostCost[i];
			}
			return totalCost;
		}
		
	}
	
	/**
	 * A help class for PriorityQueue's comparator
	 */
	class HeapNodeComparator implements Comparator<PathNode>{
		@Override
		public int compare(PathNode o1, PathNode o2) {
			if(o1.cost > o2.cost) return 1;
			if(o1.cost < o2.cost) return -1;
			return 0;
		}
	}
}
