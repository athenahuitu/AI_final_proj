package pacman.controllers.examples;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.controllers.Controller;

/*
 * The Class RandomPacMan.
 */
public final class AwesomePacMan extends Controller<MOVE>
{
	public static final int DEPTH = 20; // Search depth
	
	// Node Cost
	private double EMPTY = 5; 
	private double PILL = 3;
	private double POWER = 2;
	private double CORNERCOST = 991;
	private double GHOST_IN_COST = 2001;
	private double GHOST_OUT_COST = 1751;
	
	private boolean powerPillTarget;
	
	public AwesomePacMan() {
		
	}
	
	private static final int DISTANCE_11 = 40;
	private static final int DISTANCE_9 = 30;

	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	public MOVE getMove(Game game,long timeDue)
	{
		powerPillTarget = false;
		int dest = getTarget(game);
		int source = game.getPacmanCurrentNodeIndex();
		if(!powerPillTarget) POWER = 100;
		MOVE move = AStar(source, dest, game);
		POWER = 1;
		return move;
	}
	
	/* TODO */
	/* Get the target accordingly */
	private int getTarget(Game game)
	{
		boolean existEdibleGhost = false;//Init: there is no edible ghost
		boolean powerPillIsTarget = false;//Init: powerpill is not the target
		
		int current=game.getPacmanCurrentNodeIndex();
		
		//get all active pills
		int[] activePills=game.getActivePillsIndices();
		
		//get all active power pills
		int[] activePowerPills=game.getActivePowerPillsIndices();
		
		//create two target arrays that includes all ACTIVE pills and power pills
		int[] targetPillsIndices=new int[activePills.length];
		int[] targetPowerPillsIndices=new int[activePowerPills.length];
		for(int i=0;i<activePills.length;i++)
			targetPillsIndices[i]=activePills[i];
		
		for(int i=0;i<activePowerPills.length;i++)
			targetPowerPillsIndices[i]=activePowerPills[i];		
		
		//get the nearest pill
		int nearestPill = game.getClosestNodeIndexFromNodeIndex(current,targetPillsIndices,DM.PATH);
		
		//get the nearest power pill
		int nearestPowerPill = game.getClosestNodeIndexFromNodeIndex(current,targetPowerPillsIndices,DM.PATH);
		
	
		
		int disPacEdibleGhost = Integer.MAX_VALUE; // pacman's distance to nearest edible ghost
		int disPacGhost = Integer.MAX_VALUE; // pacman's distance to nearest ghost
		int disGhostPower = Integer.MAX_VALUE; // Ghost nearest to powerpill
		GHOST minGhostPower = null, minGhost = null;
		int disPacGhostPower = Integer.MAX_VALUE; // pacman's distance to the ghost nearest to the nearest power pill
		
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost)>0){
				int distance=game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost));
				existEdibleGhost = true;
				if(distance < disPacEdibleGhost) {
					disPacEdibleGhost = distance;
					minGhost = ghost;
				}
			} else {
				int distance=game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), current, game.getGhostLastMoveMade(ghost));
				if(distance < disPacGhost)
					disPacGhost = distance;
				
				if(nearestPowerPill != -1) {
					int dist2 = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), nearestPowerPill);
					if(dist2 < disGhostPower) {
						disGhostPower = dist2;
						minGhostPower = ghost;
					}
				} else {
					disGhostPower = Integer.MAX_VALUE;
					minGhostPower = null;
				}
			}
		}
		if(minGhostPower != null)
			disPacGhostPower = game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(minGhostPower));
		else
			disPacGhostPower = 0;
		
		if(activePills.length == 0) {
			powerPillIsTarget = true;
			this.powerPillTarget = true;
			return nearestPowerPill;
		} 
		
//		//1
//		else if(disPacPower <= 5 && disPacGhost >= 4 && disPacGhost <= 8 && disPacGhostPower >= 7) {
//			ambush = true;
//			return current;
//		} 
//		
//		//2
//		else if(activePowerPills.length > 0 && !existEdibleGhost && ambush && 
//				(disPacGhost <= 4 || disPacGhostPower <=6) && disPacGhost != 0) {
//			ambush = false;
//			powerPillIsTarget = true;
//			this.powerPillTarget = true;
//			//System.out.println("2");
//			return nearestPowerPill;
//		}
//		
//		//3
//		else if(activePowerPills.length > 0 && !existEdibleGhost && disPacGhost <= 8 && disPacPower <=4
//				&& disPacGhost != 0) {
//			powerPillIsTarget = true;
//			this.powerPillTarget = true;
//			//System.out.println("3");
//			return nearestPowerPill;
//		}
//		
//		//4
//		else if(activePowerPills.length > 0 && !existEdibleGhost && disPacGhost <=8
//				&& disPacGhost != 0) {
//			powerPillIsTarget = true;
//			this.powerPillTarget = true;
//			//System.out.println("4 " + disPacGhost);
//			return nearestPowerPill;
//		}
//		
//		//5
//		else if(existEdibleGhost && disPacGhost <= 8 && disPacEdibleGhost <= 10) {
//			//System.out.println("5");
//			return game.getGhostCurrentNodeIndex(minGhost);
//		}
//		
//		//6
//		else if(activePills.length > 0 && disPacGhost <= 8) {
//			//System.out.println("6");
//			return nearestPill;
//		}
//		
//		//7
//		else if(existEdibleGhost && disPacGhost >= 9 && disPacEdibleGhost <= 10) {
//			//System.out.println("7");
//			return game.getGhostCurrentNodeIndex(minGhost);
//		}
//		
//		else if(activePills.length > 0){
//			//System.out.println("nearest pill");
//			return nearestPill;
//		}
//		else {
//			return nearestPowerPill;
//		}
		
		
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0)
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost),current,game.getGhostLastMoveMade(ghost))
						 < DISTANCE_11)
				{
						powerPillIsTarget = true;
						this.powerPillTarget = true;
				}
		}
		
		
		if(activePowerPills.length > 0  && powerPillIsTarget){
			//System.out.println("Go to power pill!!!!!!!!!!!!!!!!!!");
			return nearestPowerPill;
		}
		
		else if(existEdibleGhost && disPacEdibleGhost < DISTANCE_9){
			//System.out.println("Go to edible ghost");
			return game.getGhostCurrentNodeIndex(minGhost);
		}
		
		else if(activePills.length > 0){
			//System.out.println("Go to pill " + nearestPill);
			return nearestPill;
		}
		
		return -1;
		
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
			PathNode peekNode = pathHeap.poll();
//			//System.out.println( peekNode.path.toString() + " cost " + peekNode.cost);
			if(peekNode.current == dest || peekNode.path.size() == DEPTH) {
				return peekNode.path.getFirst();
			}
			branch(pathHeap, peekNode, dest, game);
		}
	}
	
	// check if the item is on the path
	public int isInPath(int[] path, int item) {
		for(int i = 0; i < path.length; i ++) {
			if(path[i] == item) return i;
		}
		return -1;
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
		double cornerCost;
		double[] ghostCost;
		
		public PathNode(MOVE move, int current) {
			path = new LinkedList<MOVE>();
			path.add(move);
			this.current = current;
			this.ghostCost = new double[4];
			this.nodeCost = 0;
			this.cornerCost = 0;
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
			this.cornerCost = prevNode.cornerCost;
			this.current = current;
			this.ghostCost = new double[4];
			for(int i  = 0; i < ghostCost.length; i ++) {
				ghostCost[i] = prevNode.ghostCost[i];
			}
		}
		
		/*
		 * compute cost according to our cost model
		 */
		
		public void computeCost(int dest,  Game game) {
			int distance = game.getShortestPathDistance(current, dest);
			double heuristic = 1 + (distance - 1) * PILL;
			
			// update node cost
			int[] pills=game.getPillIndices();
			int[] powerPills=game.getPowerPillIndices();
			int ind;
			if((ind = isInPath(pills, current)) != -1) {
				if(game.isPillStillAvailable(ind))
					nodeCost += PILL;
			} else if((ind = isInPath(powerPills, current)) != -1) {
				if(game.isPowerPillStillAvailable(ind)) {
					nodeCost += POWER;
				}
			} else {
				nodeCost += EMPTY;
			}
			
			// update ghost cost
			double ghostCost = computeCurGhostCost(game);
			// prev corner cost
			if(game.isJunction(current)) {
				cornerCost += CORNERCOST;
			}
			
			// Total cost
			cost = heuristic + nodeCost + ghostCost;
		}
		
		/* Compute ghost cost on current position */
		public double computeCurGhostCost(Game game) {
			double cost;
			for(GHOST ghost : GHOST.values()) {
				int index = getGhostIndex(ghost);
				// If ghost is edible or prev ghost cost is GHOST_IN_COST, no need to update
				if(game.isGhostEdible(ghost) || ghostCost[index] == GHOST_IN_COST ||
						game.getGhostLairTime(ghost) > 0) {
					continue;
				} 
				PredictGhost predictGhost = getPredictGhost(game, ghost);
				int ghostIndex = predictGhost.ghostIndex;
				MOVE lastMove = predictGhost.lastMove;
				if(ghostIndex == current) { // the ghost is exactly at current position
					ghostCost[index] = GHOST_IN_COST;
				}
				else {
					// The distance from ghost to current position, last move considered
					int distance = game.getShortestPathDistance(ghostIndex, current, lastMove);
					
					// if it's initial state
					if(distance == 0) {
						ghostCost[index] = 0;
					} else {
						cost = GHOST_OUT_COST / (distance );
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
		
		public PredictGhost getPredictGhost(Game game, GHOST ghost) {
			int pacman = current;
			for(int i = path.size() - 1; i >= 0; i --) {
				pacman = game.getNeighbour(pacman, path.get(i).opposite());	
			}
			
			int ghostIndex = game.getGhostCurrentNodeIndex(ghost);
			MOVE lastMove = game.getGhostLastMoveMade(ghost);
			for(int i = 0; i < path.size(); i ++) {
				lastMove = game.getNextMoveTowardsTarget(ghostIndex, pacman, lastMove, DM.PATH);
				ghostIndex = game.getNeighbour(ghostIndex, lastMove);
				pacman = game.getNeighbour(pacman, path.get(i));
			}
			
			return new PredictGhost(ghostIndex, lastMove);
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
	
	class PredictGhost {
		public int ghostIndex;
		MOVE lastMove;
		public PredictGhost(int ghostIndex, MOVE lastMove) {
			this.ghostIndex = ghostIndex;
			this.lastMove = lastMove;
		}
	}
}
