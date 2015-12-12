package jsprit.core.algorithm.selector;

import java.util.Collection;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class SelectPseudoBest implements SolutionSelector {

	private static SelectPseudoBest selector;
	private static SelectBest selectorBest;
	private static SelectRandomly selectorRandomly;
	private static final int MAXCOUNTER = 10;
	private static int counter;
	private static Double lastBestCost;

	public static SelectPseudoBest getInstance() {
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

}
