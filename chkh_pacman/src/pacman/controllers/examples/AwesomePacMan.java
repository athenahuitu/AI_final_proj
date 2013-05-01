package pacman.controllers.examples;

import java.util.Random;
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
	private Random rnd=new Random();
	private MOVE[] allMoves=MOVE.values();
	
	private static final int DISTANCE_11=11;
	private static final int DISTANCE_9=9;

	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	public MOVE getMove(Game game,long timeDue)
	{
		return allMoves[rnd.nextInt(allMoves.length)];
	}
	
	/* TODO */
	/* Get the destination accordingly */
	private int getDestination(Game game)
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

		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost)>0){
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				existEdibleGhost = true;
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
			
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<DISTANCE_11)
					powerPillIsTarget = true;
		}
		
		
		if(activePowerPills.length > 0 && !existEdibleGhost && powerPillIsTarget){
			return nearestPowerPill;
		}
		
		else if(existEdibleGhost && minDistance < DISTANCE_9){
			return game.getGhostCurrentNodeIndex(minGhost);
		}
		
		else if(activePills.length > 0){
			return nearestPill;
		}
		
		return -1;
	}
}
