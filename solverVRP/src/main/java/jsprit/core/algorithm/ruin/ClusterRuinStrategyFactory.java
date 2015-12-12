package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;

public class ClusterRuinStrategyFactory implements RuinStrategyFactory{

	private Double fraction2beRemoved;
	
	private JobDistance jobDistance;
	
	public ClusterRuinStrategyFactory(Double fraction2beRemoved, JobDistance jobDistance) {
		super();
		this.fraction2beRemoved = fraction2beRemoved;
		this.jobDistance = jobDistance;
	}
	@Override
	public RuinStrategy createStrategy(VehicleRoutingProblem vrp) {
		
		JobNeighborhoods jobNeighborhoods = new JobNeighborhoodsFactory().createNeighborhoods(vrp, new AvgServiceAndShipmentDistance(vrp.getTransportCosts()), (int) (vrp.getJobs().values().size() * 0.5));
        jobNeighborhoods.initialise();
		int noJobsToMemorize = (int) Math.ceil(vrp.getJobs().values().size()*fraction2beRemoved);

		return new RuinClusters(vrp,noJobsToMemorize,jobNeighborhoods);
	}
}
/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/