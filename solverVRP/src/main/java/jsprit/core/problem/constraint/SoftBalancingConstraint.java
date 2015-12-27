package jsprit.core.problem.constraint;

import javax.naming.spi.StateFactory;

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

public class SoftBalancingConstraint implements SoftRouteConstraint{
		private RouteAndActivityStateGetter stateManager;
	
		public SoftBalancingConstraint(RouteAndActivityStateGetter stateManager) {
			super();
			this.stateManager = stateManager;
		}
		
		@Override
		public double getCosts(JobInsertionContext insertionContext) {
			int routeSize = insertionContext.getRoute().getTourActivities().jobSize();
			double routeCost = ((routeSize+1)*routeSize)/2;
			return routeCost;
		}
}
