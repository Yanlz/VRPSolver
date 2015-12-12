package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.termination.TimeTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.instance.reader.SolomonReader;
import jsprit.util.Examples;
import main.OROoptions.CONSTANTS;
import main.OROoptions.PARAMS;

public class AutomaticTrenting {

	public AutomaticTrenting() {
		Examples.createOutputFolder();

		TestsGenerator generator = new TestsGenerator();
		File dir = generator.generate();
		ArrayList<String> instanceFiles = generator.getInstanceFiles();
		ArrayList<OutputSheet> outputs = generator.getOutputs();
		
		File[] directoryListing = dir.listFiles();		
		double startTesting = System.currentTimeMillis();
		long averageTime = 0;
		if (directoryListing != null) {
			
			int sheetNo = 0, shareNo = 0, fileNo = 0, testNo = 0;
			int sharesPerSheet = outputs.get(fileNo).getSharesPerSheet();
			int sheetsPerFile = directoryListing.length / instanceFiles.size() / sharesPerSheet;
			for (File child : directoryListing) {
				System.out.println(child.getName());
				OROoptions options = new OROoptions(generator.getXMLFolder() + "/" + child.getName(),
						instanceFiles.get(fileNo) + ".txt", generator.getRepetitions());		
				for(int r=0; r<(int)options.get(CONSTANTS.REPETITION); r++) {
					// Time tracking
					
					long startTime = System.currentTimeMillis();
					// Create a vrp problem builder
					VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
					// A solomonReader reads solomon-instance files, and stores the required information in the builder.
					new SolomonReader(vrpBuilder).read("input/" + options.get(PARAMS.INSTANCE));
					VehicleRoutingProblem vrp = vrpBuilder.build();
					// Create the instance and solve the problem
					VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, 
							(int)options.get(CONSTANTS.THREADS), (String)options.get(CONSTANTS.CONFIG));
					setTimeLimit(vra, (long)options.get(CONSTANTS.TIME));
					// Solve the problem
					Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
					// Extract the best solution
					VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);
					
					long eTime = System.currentTimeMillis() - startTime;
					// Print solution on a file
					outputs.get(fileNo).writeSheet(solution, eTime, sheetNo, r, shareNo);
					
					averageTime = (averageTime == 0)? eTime : (averageTime * testNo + eTime) / (testNo + 1);
					long eta = averageTime * (directoryListing.length - testNo);
					testNo++;
					System.out.println("ETA " + (eta / 60000.0) + " minutes (" + (eta / 3600000.0) + " hours)");
				}

				shareNo++;
				sheetNo += shareNo / sharesPerSheet;
				fileNo += sheetNo / sheetsPerFile;
				sheetNo = sheetNo % sheetsPerFile;
				shareNo = shareNo % sharesPerSheet;	
			}
			for (OutputSheet output: outputs)
				output.write();
			System.out.println("Total time " + ((System.currentTimeMillis() - startTesting) / 60000.0) + " minutes");
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
			// But I don't give a fuck.
		}	
	}
	
	private static void setTimeLimit(VehicleRoutingAlgorithm vra, long timeMilliSec) {
		TimeTermination tterm = new TimeTermination(timeMilliSec);
		vra.setPrematureAlgorithmTermination(tterm);
		vra.addListener(tterm);
	}
	
	public static void main(String[] args) {
		new AutomaticTrenting();
	}


}
