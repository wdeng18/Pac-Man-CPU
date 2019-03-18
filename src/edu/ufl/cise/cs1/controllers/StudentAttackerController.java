package edu.ufl.cise.cs1.controllers;

import game.controllers.AttackerController;
import game.models.Defender;
import game.models.Game;
import game.models.Node;

import java.util.List;
import java.util.ListIterator;


public final class StudentAttackerController implements AttackerController {

	Node myPreviousNode;
	int myMoveCount = 0; //Debugging purposes
	static final int JUNCTION_POTENTIAL = 150; //150
	static final int NEW_MOVE_POTENTIAL = 500; //500
	static final int DISTANCE_PILL_POTENTIAL = 1000; //1000
	static final int DISTANCE_POWER_PILL_POTENTIAL = 2990; //2990
	static final int DISTANCE_TO_KILL_POTENTIAL = 6020; //3000 //6000 = 5363  6020 = 5413
	static final int DISASTER_POTENTIAL = -4000; //-4000

	public void init(Game game) {
	}

	public void shutdown(Game game) {
	}

	public int update(Game game, long timeDue) {

		//Chooses a random LEGAL action if required.
		List<Integer> possibleDirs = game.getAttacker().getPossibleDirs(true);
		ListIterator<Integer> integerListIterator = possibleDirs.listIterator();

		int mxPV = -2000000, mxMV = 100;

		while (integerListIterator.hasNext()) {
			int mv = integerListIterator.next();
			int pv = potentialValue(game, mv);
			//Compares moves with potential value
			if (pv > mxPV) {
				mxPV = pv;
				mxMV = mv;
			}
		}

		//Update previous location
		bookKeep(game);

		//myMoveCount++; Debug
		//System.out.print(game.getAttacker().getLocation());
		//System.out.print(" moveCount:" + myMoveCount + " pv:" + mxPV + " pills:");
		//System.out.println(game.getPillList().size());

		//Action
		return mxMV;
	}

	//Checks if the location/node still has a power pill
	boolean isAvailablePowerPill(Node n, List<Node> powerPill) {
		ListIterator<Node> powerPillListIterator = powerPill.listIterator();
		int nX = n.getX(), nY = n.getY();
		while (powerPillListIterator.hasNext()) {
			Node nn = powerPillListIterator.next();
			if (nn.getX() == nX && nn.getY() == nY)
				return true;
		}
		return false;
	}

	//Tracking Previous Move
	void bookKeep(Game game) {
		Node n = game.getAttacker().getLocation();
		myPreviousNode = n;
	}

	//Tracking New Move
	boolean isNewMove(Node n) {
		if (myPreviousNode == null)
			return true;
		if (n.getX() == myPreviousNode.getX() && n.getY() == myPreviousNode.getY())
			return false;
		else
			return true;
	}

	//Distance for Pill
	int distancePill(List<Node> lst, Node n) {
		ListIterator<Node> nodeListIterator = lst.listIterator();
		int shortest = 32767;
		while (nodeListIterator.hasNext()) {
			int d = n.getPathDistance(nodeListIterator.next());
			if (d < shortest)
				shortest = d;
		}
		return shortest;
	}

	//Distance for Defenders
	int distanceDefender(List<Defender> lst, Node n, boolean vulnerableDefender) {
		ListIterator<Defender> defenderListIterator = lst.listIterator();
		int shortest = 32767;
		while (defenderListIterator.hasNext()) {
			Defender defender = defenderListIterator.next();
			if (defender.isVulnerable() == vulnerableDefender) {
				Node locationDefender = defender.getLocation();
				int d = n.getPathDistance(locationDefender);
				if (d < shortest) {
					shortest = d;
				}
			}
		}
		return shortest;
	}

	//Judgement for which actions to take, High PV = best option
	int potentialValue(Game game, int mv) {
		Node movedNode = game.getAttacker().getLocation().getNeighbor(mv);
		int pv = -8000;
		int d;

		if (movedNode.isJunction()){
			pv += JUNCTION_POTENTIAL;
		}

		if (isNewMove(movedNode))
			pv += NEW_MOVE_POTENTIAL;

		if (isAvailablePowerPill(movedNode, game.getPowerPillList()) == false) {
			d = distancePill(game.getPillList(), movedNode);
			pv += DISTANCE_PILL_POTENTIAL / (d + 1);
		}

		int dv = distanceDefender(game.getDefenders(), movedNode, true);
		pv += (10 * (DISTANCE_TO_KILL_POTENTIAL/(dv + 1)));

		int dn = distanceDefender(game.getDefenders(), movedNode, false);
		if (dn < 7 && dn >= 4) {
			d = distancePill(game.getPowerPillList(), movedNode);
			pv += DISTANCE_POWER_PILL_POTENTIAL/(d + 1);
		}

		if (dn > 0 && dn < 4)
			pv += DISASTER_POTENTIAL;

		//System.out.println(" myMoveCount " + myMoveCount + " DN " + dn + " PV " + pv);

		return pv;
	}
}