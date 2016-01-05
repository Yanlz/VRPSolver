package jsprit.core.algorithm.recreate;

import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FixedRegretInsertion extends AbstractInsertionStrategy {

    static class ScoredJob {

        private Job job;

        private double score;

        private InsertionData insertionData;

        private VehicleRoute route;

        private boolean newRoute;

        ScoredJob(Job job, double score, InsertionData insertionData, VehicleRoute route, boolean isNewRoute) {
            this.job = job;
            this.score = score;
            this.insertionData = insertionData;
            this.route = route;
            this.newRoute = isNewRoute;
        }

        public boolean isNewRoute() {
            return newRoute;
        }

        public Job getJob() {
            return job;
        }

        public double getScore() {
            return score;
        }

        public InsertionData getInsertionData() {
            return insertionData;
        }

        public VehicleRoute getRoute() {
            return route;
        }
    }

    static class BadJob extends ScoredJob {

        BadJob(Job job) {
            super(job, 0., null, null, false);
        }
    }

	static interface ScoringFunction {

		public double score(InsertionData best, Job job);

	}

	public static class DefaultScorer implements ScoringFunction {

        private VehicleRoutingProblem vrp;

        private double tw_param = - 0.5;

        private double depotDistance_param = + 0.1;

        private double minTimeWindowScore = - 100000;

        public DefaultScorer(VehicleRoutingProblem vrp) {
            this.vrp = vrp;
        }

        public void setTimeWindowParam(double tw_param){ this.tw_param = tw_param; }

        public void setDepotDistanceParam(double depotDistance_param){ this.depotDistance_param = depotDistance_param; }

        @Override
        public double score(InsertionData best, Job job) {
            double score;
            if(job instanceof Service){
                score = scoreService(best, job);
            }
            else if(job instanceof Shipment){
                score = scoreShipment(best,job);
            }
            else throw new IllegalStateException("not supported");
            return score;
        }

        private double scoreShipment(InsertionData best, Job job) {
            Shipment shipment = (Shipment)job;
            double maxDepotDistance_1 = Math.max(
                    getDistance(best.getSelectedVehicle().getStartLocation(),shipment.getPickupLocation()),
                    getDistance(best.getSelectedVehicle().getStartLocation(),shipment.getDeliveryLocation())
            );
            double maxDepotDistance_2 = Math.max(
                    getDistance(best.getSelectedVehicle().getEndLocation(),shipment.getPickupLocation()),
                    getDistance(best.getSelectedVehicle().getEndLocation(),shipment.getDeliveryLocation())
            );
            double maxDepotDistance = Math.max(maxDepotDistance_1,maxDepotDistance_2);
            double minTimeToOperate = Math.min(shipment.getPickupTimeWindow().getEnd()-shipment.getPickupTimeWindow().getStart(),
                    shipment.getDeliveryTimeWindow().getEnd()-shipment.getDeliveryTimeWindow().getStart());
            return Math.max(tw_param * minTimeToOperate,minTimeWindowScore) + depotDistance_param * maxDepotDistance;
        }

        private double scoreService(InsertionData best, Job job) {
            double maxDepotDistance = Math.max(
                    getDistance(best.getSelectedVehicle().getStartLocation(), ((Service) job).getLocation()),
                    getDistance(best.getSelectedVehicle().getEndLocation(), ((Service) job).getLocation())
            );
            return Math.max(tw_param * (((Service)job).getTimeWindow().getEnd() - ((Service)job).getTimeWindow().getStart()),minTimeWindowScore) +
                    depotDistance_param * maxDepotDistance;
        }


        private double getDistance(Location loc1, Location loc2) {
            return vrp.getTransportCosts().getTransportCost(loc1,loc2,0.,null,null);
        }

		@Override
		public String toString() {
			return "[name=defaultScorer][twParam="+tw_param+"][depotDistanceParam=" + depotDistance_param + "]";
		}

	}

    private static Logger logger = LogManager.getLogger(FixedRegretInsertion.class);

	private ScoringFunction scoringFunction;

    private JobInsertionCostsCalculator insertionCostsCalculator;

    private int routesNo;

    /**
	 * Sets the scoring function.
	 *
	 * <p>By default, the this.TimeWindowScorer is used.
	 *
	 * @param scoringFunction to score
	 */
	public void setScoringFunction(ScoringFunction scoringFunction) {
		this.scoringFunction = scoringFunction;
	}

	public FixedRegretInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
		super(vehicleRoutingProblem);
        this.scoringFunction = new DefaultScorer(vehicleRoutingProblem);
		this.insertionCostsCalculator = jobInsertionCalculator;
        this.vrp = vehicleRoutingProblem;
        
		if(System.getProperty("fixedroutes") != null) {
			routesNo = Integer.parseInt(System.getProperty("fixedroutes"));
		}
		logger.debug("initialise {}", this);
	}

	@Override
	public String toString() {
		return "[name=fixedRegretInsertion][additionalScorer="+scoringFunction+"]";
	}


	/**
	 * Runs insertion.
	 *
	 * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
	 *
	 */
	@Override
	public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
        List<Job> jobs = new ArrayList<Job>(unassignedJobs);
        
		while (routes.size() < routesNo)
			routes.add(VehicleRoute.emptyRoute());

        while (!jobs.isEmpty()) {
            List<Job> unassignedJobList = new ArrayList<Job>(jobs);
            List<Job> badJobList = new ArrayList<Job>();
            ScoredJob bestScoredJob = nextJob(routes, unassignedJobList, badJobList);
            if(bestScoredJob != null){
                if(bestScoredJob.isNewRoute()){
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(),bestScoredJob.getInsertionData(),bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
            }
            for(Job bad : badJobList) {
                jobs.remove(bad);
                badJobs.add(bad);
            }
        }
        return badJobs;
    }

    private ScoredJob nextJob(Collection<VehicleRoute> routes, List<Job> unassignedJobList, List<Job> badJobs) {
        ScoredJob bestScoredJob = null;
        for (Job unassignedJob : unassignedJobList) {
            ScoredJob scoredJob = getScoredJob(routes,unassignedJob,insertionCostsCalculator,scoringFunction);
            if(scoredJob instanceof BadJob){
                badJobs.add(unassignedJob);
                continue;
            }
            if(bestScoredJob == null) bestScoredJob = scoredJob;
            else{
                if(scoredJob.getScore() > bestScoredJob.getScore()){
                    bestScoredJob = scoredJob;
                }
            }
        }
        return bestScoredJob;
    }

    static ScoredJob getScoredJob(Collection<VehicleRoute> routes, Job unassignedJob, JobInsertionCostsCalculator insertionCostsCalculator, ScoringFunction scoringFunction) {
        InsertionData best = null;
        InsertionData secondBest = null;
        VehicleRoute bestRoute = null;

        double benchmark = Double.MAX_VALUE;
        for (VehicleRoute route : routes) {
            if (secondBest != null) {
                benchmark = secondBest.getInsertionCost();
            }
            InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, benchmark);
            if (iData instanceof InsertionData.NoInsertionFound) continue;
            if (best == null) {
                best = iData;
                bestRoute = route;
            } else if (iData.getInsertionCost() < best.getInsertionCost()) {
                secondBest = best;
                best = iData;
                bestRoute = route;
            } else if (secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())) {
                secondBest = iData;
            }
        }

        if(best == null){
            return new FixedRegretInsertion.BadJob(unassignedJob);
        }
        double score = score(unassignedJob, best, secondBest, scoringFunction);
        ScoredJob scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, false);
        return scoredJob;
    }


    static double score(Job unassignedJob, InsertionData best, InsertionData secondBest, ScoringFunction scoringFunction) {
        if(best == null){
            throw new IllegalStateException("cannot insert job " +  unassignedJob.getId());
        }
        double score;
        if(secondBest == null){ //either there is only one vehicle or there are more vehicles, but they cannot load unassignedJob
            //if only one vehicle, I want the job to be inserted with min iCosts
            //if there are more vehicles, I want this job to be prioritized since there are no alternatives
            score = Integer.MAX_VALUE - best.getInsertionCost() + scoringFunction.score(best, unassignedJob);
        }
        else{
            score = (secondBest.getInsertionCost()-best.getInsertionCost()) + scoringFunction.score(best, unassignedJob);
        }
        return score;
    }


}
