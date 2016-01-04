package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.AbstractInsertionStrategy.Insertion;
import jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class FirstFixedBestInsertion extends AbstractInsertionStrategy {

	private static Logger logger = LogManager.getLogger(FixedBestInsertion.class);

	private JobInsertionCostsCalculator bestInsertionCostCalculator;
	
	//set to 10 routes by default
	private int routesNo = 10;

	public FirstFixedBestInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
		super(vehicleRoutingProblem);
		
		// number of fixed routes is defined by the system property added with JVM parameter
		// -Dfixedroutes=<number>
		if(System.getProperty("fixedroutes") != null) {
			routesNo = Integer.parseInt(System.getProperty("fixedroutes"));
		}
		
		bestInsertionCostCalculator = jobInsertionCalculator;
		logger.debug("initialise {}", this);
	}

	@Override
	public String toString() {
		return "[name=firstFixedBestInsertion]";
	}

	@Override
	public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
        List<Job> unassignedJobList = new ArrayList<Job>(unassignedJobs);
		Collections.shuffle(unassignedJobList, random);
		
		while (vehicleRoutes.size() < routesNo)
			vehicleRoutes.add(VehicleRoute.emptyRoute());
	
		Iterator<VehicleRoute> routeIterator = vehicleRoutes.iterator();
		VehicleRoute vehicleRoute = routeIterator.next();
		
		int nJobs = unassignedJobList.size();
		while (nJobs > (2 * routesNo)) {	
			Insertion bestInsertion = null;
			Job bestJob = null;
			double bestInsertionCost = Double.MAX_VALUE;
			
			for (Job unassignedJob : unassignedJobList) {
				InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost); 
				if(iData instanceof NoInsertionFound)
					continue;
				if(iData.getInsertionCost() < bestInsertionCost) {
					bestInsertion = new Insertion(vehicleRoute,iData);
					bestInsertionCost = iData.getInsertionCost();
					bestJob = unassignedJob;
				}
			}
			
			if (bestJob != null) {
				insertJob(bestJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
				nJobs--;
				unassignedJobList.remove(bestJob);
			}
			
			if (!routeIterator.hasNext())
				routeIterator = vehicleRoutes.iterator();
			vehicleRoute = routeIterator.next();	
		}
		
		for(Job unassignedJob : unassignedJobList){			
			Insertion bestInsertion = null;
			double bestInsertionCost = Double.MAX_VALUE;
			for(VehicleRoute route : vehicleRoutes){
				InsertionData iData = bestInsertionCostCalculator.getInsertionData(route, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost); 
				if(iData instanceof NoInsertionFound) {
					continue;
				}
				if(iData.getInsertionCost() < bestInsertionCost){
					bestInsertion = new Insertion(route,iData);
					bestInsertionCost = iData.getInsertionCost();
				}
			}
			if(bestInsertion == null) badJobs.add(unassignedJob);
            else insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
		}
		return badJobs;
	}
	
	public void setRoutesNo(int routesNo) {
		this.routesNo = routesNo;
	}

}
