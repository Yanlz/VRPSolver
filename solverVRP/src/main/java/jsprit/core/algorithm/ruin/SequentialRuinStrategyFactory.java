package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;

public class SequentialRuinStrategyFactory implements RuinStrategyFactory {

	private double fraction;

	public SequentialRuinStrategyFactory(double fraction, JobDistance jobDistance) {
		super();
		this.fraction = fraction;
	}

	@Override
	public RuinStrategy createStrategy(VehicleRoutingProblem vrp) {
		return new RuinSequential(vrp, fraction);
	}
}
