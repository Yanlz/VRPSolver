package jsprit.core.algorithm.ruin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.util.RandomUtils;

public class RuinSequential extends AbstractRuinStrategy {

	private Logger logger = LogManager.getLogger(RuinRadial.class);

	private VehicleRoutingProblem vrp;

	private double fractionOfAllNodes2beRuined;

	/**
	 * Constructs SequentialRadial.
	 * 
	 * @param vrp
	 * @param fraction2beRemoved
	 *            i.e. the share of jobs to be removed (relative to the total
	 *            number of jobs in vrp)
	 * @param jobDistance
	 *            i.e. a measure to define the distance between two jobs and
	 *            whether they are located close or distant to eachother
	 */
	public RuinSequential(VehicleRoutingProblem vrp, double fraction) {
		super(vrp);
		this.vrp = vrp;
		this.fractionOfAllNodes2beRuined = fraction;
		setRuinShareFactory(new RuinShareFactory() {
			@Override
			public int createNumberToBeRemoved() {
				return selectNuOfJobs2BeRemoved();
			}
		});
		logger.debug("initialise {}", this);
	}

	private int selectNuOfJobs2BeRemoved() {
		return (int) Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined);
	}

	/**
	 * Ruins the collection of vehicleRoutes, i.e. removes a share of jobs.
	 * First, it selects a job randomly. Second, it identifies its neighborhood.
	 * And finally, it removes the neighborhood plus the randomly selected job
	 * from the number of vehicleRoutes. All removed jobs are then returned as a
	 * collection.
	 */
	@Override
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
		if (vehicleRoutes.isEmpty()) {
			return Collections.emptyList();
		}
		int nOfJobs2BeRemoved = ruinShareFactory.createNumberToBeRemoved();
		if (nOfJobs2BeRemoved == 0) {
			return Collections.emptyList();
		}
		Job randomJob = RandomUtils.nextJob(vrp.getJobs().values(), random);
		return ruinRoutes(vehicleRoutes, randomJob, nOfJobs2BeRemoved);
	}

	/**
	 * Removes targetJob and its neighborhood and returns the removed jobs.
	 * 
	 * @deprecated will be private
	 */
	@Deprecated
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved) {
		List<Job> unassignedJobs = new ArrayList<Job>();
		int nNeighbors = nOfJobs2BeRemoved - 1;
		VehicleRoute routeToRuin = null;
		Iterator<VehicleRoute> vrIt = vehicleRoutes.iterator();
		while (vrIt.hasNext()) {
			VehicleRoute vr = vrIt.next();
			// i identifies the position of the job
			int i = 0;
			Iterator<Job> jobIt = vr.getTourActivities().getJobs().iterator();
			while (jobIt.hasNext()) {
				Job job = jobIt.next();
				if (job.getId().equals(targetJob.getId())) {
					nNeighbors = (int) Math.ceil(vr.getTourActivities().getJobs().size() * fractionOfAllNodes2beRuined);
					nNeighbors--;
					routeToRuin = vr;
					Iterator<Job> neighborhoodIterator = vr.getTourActivities().getJobs().iterator();
					Collection<Job> listOfJobs = new HashSet<Job>();
					Job jobToRemove = neighborhoodIterator.next();
					// go to the first job to ruin
					for (; i > 0; i--) {
						jobToRemove = neighborhoodIterator.next();
					}
					listOfJobs.add(jobToRemove);
					// ruin sequentially the jobs
					while (nNeighbors != 0) {

						// if the tour is over start from beginning
						if (neighborhoodIterator.hasNext()) {
							jobToRemove = neighborhoodIterator.next();
							listOfJobs.add(jobToRemove);
						} else {
							neighborhoodIterator = vr.getTourActivities().getJobs().iterator();
							jobToRemove = neighborhoodIterator.next();
							listOfJobs.add(jobToRemove);
						}
						nNeighbors--;
					}
					for (Job j : listOfJobs) {
						removeJob(j, vehicleRoutes);
						unassignedJobs.add(j);
					}
					break;
				}
				i++;
			}
			if (routeToRuin != null)
				break;
		}
		return unassignedJobs;
	}
}
