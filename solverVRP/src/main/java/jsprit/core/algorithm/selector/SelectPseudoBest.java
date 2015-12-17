package jsprit.core.algorithm.selector;

import java.util.Collection;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class SelectPseudoBest implements SolutionSelector {

	private static SelectPseudoBest selector =null;
	private static SelectBest selectorBest=null;
	private static SelectRandomly selectorRandomly=null;
	private static final int MAXCOUNTER = 10;
	private static int counter;
	private static Double lastBestCost;

	public static SelectPseudoBest getInstance() {
		lastBestCost=0.0;
		counter=0;
		if (selector == null) {
			selector = new SelectPseudoBest();
			selectorBest = new SelectBest();
			selectorRandomly = new SelectRandomly();
			return selector;
		}
		return selector;
	}

	@Override
	public VehicleRoutingProblemSolution selectSolution(Collection<VehicleRoutingProblemSolution> solutions) {

		// TODO Auto-generated method stub
		VehicleRoutingProblemSolution tmp = counter == MAXCOUNTER ? selectorRandomly.selectSolution(solutions)
				: selectorBest.selectSolution(solutions);

		counter = counter == MAXCOUNTER ? 0 : (tmp.getCost() == lastBestCost ? counter + 1 : counter - 1);

		lastBestCost = tmp.getCost();

		return tmp;
	}
	@Override
	public String toString() {
		return "[name=selectBest]";
	}
}
