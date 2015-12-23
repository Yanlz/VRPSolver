/*******************************************************************************
 * Copyright (C) 2016  David Diye C. Van T. Treunti
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import jsprit.core.algorithm.recreate.listener.InsertionListeners;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;




/**
 * 
 * @author David Diye C. Van T. Treunti
 * 
 */

public final class FixedBestInsertionConcurrent extends AbstractInsertionStrategy{
	
	static class Batch {
		List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
		
	}
	
	class Insertion {
		
		private final VehicleRoute route;
		
		private final InsertionData insertionData;

		public Insertion(VehicleRoute vehicleRoute, InsertionData insertionData) {
			super();
			this.route = vehicleRoute;
			this.insertionData = insertionData;
		}

		public VehicleRoute getRoute() {
			return route;
		}
		
		public InsertionData getInsertionData() {
			return insertionData;
		}
		
	}
	
	private static Logger logger = LogManager.getLogger(FixedBestInsertionConcurrent.class);
	
	private final static double NO_NEW_DEPARTURE_TIME_YET = -12345.12345;
	
	private final static Vehicle NO_NEW_VEHICLE_YET = null;
	
	private final static Driver NO_NEW_DRIVER_YET = null;
	
	private InsertionListeners insertionsListeners;
	
	private JobInsertionCostsCalculator bestInsertionCostCalculator;

	private int nuOfBatches;
	
	//set to 10 routes by default
	private int routesNo = 10;

	private ExecutorCompletionService<Insertion> completionService;

	@Deprecated
	public void setRandom(Random random) {
		super.random = random;
	}
	
	public FixedBestInsertionConcurrent(JobInsertionCostsCalculator jobInsertionCalculator, ExecutorService executorService, int nuOfBatches, VehicleRoutingProblem vehicleRoutingProblem) {
		super(vehicleRoutingProblem);
		this.insertionsListeners = new InsertionListeners();
		this.nuOfBatches = nuOfBatches;
		
		// number of fixed routes is defined by the system property added with JVM parameter
		// -Dfixedroutes=<number>
		if(System.getProperty("fixedroutes") != null) {
			routesNo = Integer.parseInt(System.getProperty("fixedroutes"));
		}
		
		bestInsertionCostCalculator = jobInsertionCalculator;
		completionService = new ExecutorCompletionService<Insertion>(executorService);
		logger.debug("initialise {}", this);
	}

	@Override
	public String toString() {
		return "[name=fixedBestInsertion]";
	}

	@Override
	public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
		List<Job> unassignedJobList = new ArrayList<Job>(unassignedJobs);
		Collections.shuffle(unassignedJobList, random);
		
		while (vehicleRoutes.size() < routesNo) {
			vehicleRoutes.add(VehicleRoute.emptyRoute());
		}
		
		List<Batch> batches = distributeRoutes(vehicleRoutes,nuOfBatches);
		for(final Job unassignedJob : unassignedJobList){
			Insertion bestInsertion = null;
			double bestInsertionCost = Double.MAX_VALUE;
			for(final Batch batch : batches){
				completionService.submit(new Callable<Insertion>() {

					@Override
					public Insertion call() throws Exception {
						return getBestInsertion(batch,unassignedJob);
					}

				});
			}
			try {
				for (int i = 0; i < batches.size(); i++) {
					Future<Insertion> futureIData = completionService.take();
					Insertion insertion = futureIData.get();
					if (insertion == null) continue;
					if (insertion.getInsertionData().getInsertionCost() < bestInsertionCost) {
						bestInsertion = insertion;
						bestInsertionCost = insertion.getInsertionData().getInsertionCost();
					}
				}
			} catch(InterruptedException e){
				Thread.currentThread().interrupt();
			}
			catch (ExecutionException e) {
				e.printStackTrace();
				logger.error("Exception", e);
				System.exit(1);
			}
			
			if(bestInsertion == null) badJobs.add(unassignedJob);
			else insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
		}
		return badJobs;
	}
	
	private Insertion getBestInsertion(Batch batch, Job unassignedJob) {
		Insertion bestInsertion = null;
		double bestInsertionCost = Double.MAX_VALUE;
		for(VehicleRoute vehicleRoute : batch.routes){
			InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost); 
			if(iData instanceof NoInsertionFound) {
				continue;
			}
			if(iData.getInsertionCost() < bestInsertionCost){
				bestInsertion = new Insertion(vehicleRoute,iData);
				bestInsertionCost = iData.getInsertionCost();
			}
		}
		return bestInsertion;
	}
	
	private List<Batch> distributeRoutes(Collection<VehicleRoute> vehicleRoutes, int nuOfBatches) {
		List<Batch> batches = new ArrayList<Batch>();
		for(int i=0;i<nuOfBatches;i++) batches.add(new Batch()); 
		
		int count = 0;
		for(VehicleRoute route : vehicleRoutes){
			if(count == nuOfBatches) count=0;
			batches.get(count).routes.add(route);
			count++;
		}
		return batches;
	}
	
	public void setRoutesNo(int routesNo) {
		this.routesNo = routesNo;
	}

}
