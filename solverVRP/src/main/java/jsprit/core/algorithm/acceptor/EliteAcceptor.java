package jsprit.core.algorithm.acceptor;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

/**
 * Elite acceptance by Paolo Caleffi (2015)
 * 
 * <p>The idea is to avoid relative minima during the research. <br> 
 * Every time we get stuck in a minimum, we erase a good part of the memory (except the elite solutions) 
 * in order to automatically accept new solutions, even if worse. <br>
 * In this way, we hope to get away from the local minimum and in a kind "restart" 
 * the reserch from any kind of solution we find</p>
 * 
 * <p>Takes as parameters: <br>
 * - an integer called "elite" between 1 and solutionMemory-1 <br>
 * - a double called "varianceThreshold" between 0 and 1 </p>
 * 
 * 
 * @author Paolo Caleffi
 *
 */

public class EliteAcceptor implements SolutionAcceptor, IterationStartsListener {

	private static Logger logger = LogManager.getLogger(SchrimpfAcceptance.class.getName());
	
	private final double varianceThreshold;
	
	private final int elite;
	
	private final int solutionMemory;
	
	private double variance;
	
	/**
	 * 
	 * @param solutionMemory number of slots to hold accepted solutions
	 * @param varianceThreshold at which variance degree we must erase the memory, must be between 0.0 and 1.0
	 * @param elite how many elite solution we must spare when we get under the varianceThreshold
	 */
	public EliteAcceptor (int solutionMemory, double varianceThreshold, int elite) {
		this.solutionMemory = solutionMemory;
		this.varianceThreshold = varianceThreshold;
		this.elite = elite;
		logger.debug("initialize {}", this);
	}
	
	/**
	 * <p>It behave like a GreedyAcceptor (if the proposed solution is better than the worse in memory,
	 * remove the latter one and accept the current solution)</p>
	 */
	@Override
	public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution) {
		boolean solutionAccepted = false;
		
		if (solutions.size() < solutionMemory) {
			solutions.add(newSolution);
			solutionAccepted = true;
		} else {
			VehicleRoutingProblemSolution worstSolution = null;
			for (VehicleRoutingProblemSolution s : solutions) {
				if (worstSolution == null) worstSolution = s;
				else if (s.getCost() > worstSolution.getCost()) worstSolution = s;
			}
			if(newSolution.getCost() < worstSolution.getCost()){
				solutions.remove(worstSolution);
				solutions.add(newSolution);
				solutionAccepted = true;
			}
		}
		
		return solutionAccepted;
	}

	
	@Override
	public String toString() {
		return "[name=EliteAcceptor]";
	}

	/**
	 * <p>At every iteration, it checks the variance between all values in memory.
	 * If variance is less or equal than varianceThreshold, all solutions in memory are similar in term of costs, 
	 * it removes all solutions from memory except than the elite</p>
	 */
	@Override
	public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		//calculate variance
		double[] results = new double[solutionMemory];
		int j = 0;
		for(VehicleRoutingProblemSolution s : solutions) {
			results[j++] = s.getCost();
		}
		StandardDeviation dev = new StandardDeviation();
		variance = dev.evaluate(results);
		
		//if variance is too low (and the memory is full), we cut the solutions
		if(variance < varianceThreshold && solutions.size() == solutionMemory) {
			ArrayList<VehicleRoutingProblemSolution> eliteSolutions = new ArrayList<VehicleRoutingProblemSolution>();
			for(VehicleRoutingProblemSolution s : solutions) {
				if(eliteSolutions.size() < elite) {
					eliteSolutions.add(s);
				}
				else {
					for(VehicleRoutingProblemSolution s1 : eliteSolutions) {
						if(s.getCost() < s1.getCost()) {
							eliteSolutions.remove(s1);
							eliteSolutions.add(s);
						}
					}
				}
			}
			
			solutions = eliteSolutions;
		}
	}
}
