package main;

import java.io.File;
import java.io.IOException;
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

import org.apache.commons.io.FileUtils;

public class VariableTestGenerator {
	private static final int REPETITIONS = 10;
	private static final String XMLOUTFOLDER = "xmlTestFolder";

	private static final String tagIterations = "<?xml version=\"1.0\" ?>\n\n<algorithm xmlns=\"http://www.w3schools.com\"\n     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3schools.com algorithm_schema.xsd\">\n<iterations>";
	private static final String	tagMemory = "</iterations>\n	<construction>\n		<insertion name=\"bestInsertion\">\n			<considerFixedCosts weight=\"1.0\">true</considerFixedCosts>\n		</insertion>\n	</construction>\n		<strategy>\n		<memory>";
	private static final String tagSearchStrategies = "</memory>\n		<searchStrategies>";
	private static final String tagStrategyName = "\n	<searchStrategy name=\"";
	private static final String tagSelector = "AndRecreate\">\n				<selector name=\"";
	private static final String tagAcceptor = "\"/>\n	  			<acceptor name=\"";
	private static final String tagAlpha = "\">  \n 					<alpha>";
	private static final String	tagWarmup = "</alpha>  \n  					<warmup>";
	private static final String closeSchrimpf = "</warmup> \n  				</acceptor>";
	private static final String closeAcceptor = "\"/>";
	private static final String tagRuin = "\n				<modules>\n					<module name=\"ruin_and_recreate\">\n						<ruin name=\"";
	private static final String tagShare = "\">\n							<share>";
	private static final String tagInsertion = "</share>\n						</ruin>\n						<insertion name=\"";
	private static final String tagProbability = "\"/>\n					</module>\n				</modules>\n				<probability>";
	private static final String closeStrategy = "</probability>			</searchStrategy>";
	private static final String closeStrategies = "</searchStrategies>		\n	</strategy>\n</algorithm>";

	private ArrayList<OutputSheet> outputs;
	private ArrayList<String> instanceFiles;

	private final GUI gui;

	public VariableTestGenerator(){
		outputs = new ArrayList<OutputSheet>();

		//Instance files
		instanceFiles = new ArrayList<String>();
		instanceFiles.add("C101");
		instanceFiles.add("C102");
		instanceFiles.add("C103");
		instanceFiles.add("C104");
		instanceFiles.add("C105");
		instanceFiles.add("C106");
		instanceFiles.add("C107");
		instanceFiles.add("C108");
		instanceFiles.add("C109");
		instanceFiles.add("C201");
		instanceFiles.add("C202");
		instanceFiles.add("C203");
		instanceFiles.add("C204");
		instanceFiles.add("C205");
		instanceFiles.add("C206");
		instanceFiles.add("C207");
		instanceFiles.add("C208");
		instanceFiles.add("rC101");
		instanceFiles.add("rC102");
		instanceFiles.add("rC103");
		instanceFiles.add("rC104");
		instanceFiles.add("rC105");
		instanceFiles.add("rC106");
		instanceFiles.add("rC107");
		instanceFiles.add("rC108");
		instanceFiles.add("rC201");
		instanceFiles.add("rC202");
		instanceFiles.add("rC203");
		instanceFiles.add("rC204");
		instanceFiles.add("rC205");
		instanceFiles.add("rC206");
		instanceFiles.add("rC207");
		instanceFiles.add("rC208");

		//Ruins
		ArrayList<String> ruins = new ArrayList<String>();
		ruins.add("randomRuin");
		ruins.add("radialRuin");
		ruins.add("clusterRuin");

		//Selectors
		ArrayList<String> selectors = new ArrayList<String>();
		selectors.add("selectBest");
		selectors.add("selectRandomly");

		//Insertions
		ArrayList<String> insertions = new ArrayList<String>();
		insertions.add("bestInsertion");
		insertions.add("regretInsertion");

		//Acceptors
		ArrayList<String> acceptors = new ArrayList<String>();
		acceptors.add("acceptNewRemoveWorst");
		acceptors.add("greedyAcceptance");
		acceptors.add("schrimpfAcceptance");

		gui = new GUI(this, instanceFiles, ruins, selectors, insertions, acceptors);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.show();
			}
		});
	}

	public void generate(String instance, String iterations, String memory, 
			Strategy strategy1, Strategy strategy2, ArrayList<Double> range) {
		File dir = new File(XMLOUTFOLDER);
		if (dir.exists()) {
			File[] directoryListing = dir.listFiles();
			for (File child : directoryListing)
				child.delete();
		}
		dir.mkdir();
		
		int testNo = 0;
		for (double value: range) {
			String xml = startXml(iterations,memory);
			if (strategy1.isVariable) {
				strategy1.setVariableValue(value);
				if (strategy1.getVariableName().equals("Probability"))
					strategy2.probability = ((double) Math.round(Double.sum(1.0, -value)*10d) / 10d);			
			} else {
				strategy2.setVariableValue(value);
				if (strategy2.getVariableName().equals("Probability"))
					strategy1.probability = ((double) Math.round(Double.sum(1.0, -value)*10d) / 10d);	
			}
			xml+= addStrategyXml(strategy1, 1);
			xml+= addStrategyXml(strategy2, 2);
			xml+= endXml();
			String testName = "test";
			
			if (testNo < 10) 
				testName += "0";
			if (testNo < 100)
				testName += "0";
			testName += testNo;
			try {
				FileUtils.writeStringToFile(new File(XMLOUTFOLDER + "\\" + testName + ".xml"), xml);
			} catch (IOException e) {
				e.printStackTrace();
			}
			testNo++;
		}
		
		AnotherOutputSheet sheet = new AnotherOutputSheet();
		sheet.createVariableSheet(strategy1, strategy2, instance, REPETITIONS, range,
				iterations, memory);
		
		start(dir, instance, sheet);
	}
	
	public String startXml(String iterations, String memory) {
		return tagIterations + iterations + tagMemory + memory + tagSearchStrategies;
	}

	public String addStrategyXml(Strategy strategy, int num) {
		String string = tagStrategyName + strategy.ruin + num + tagSelector + strategy.selector + 
				tagAcceptor + strategy.acceptor;

		if (strategy.acceptor == "schrimpfAcceptance")
			string+= tagAlpha + strategy.alpha + tagWarmup + (int)strategy.warmup + closeSchrimpf;
		else
			string += closeAcceptor;

		string += tagRuin + strategy.ruin + tagShare + strategy.share + tagInsertion + 
				strategy.insertion + tagProbability + strategy.probability + closeStrategy;
		return string;
	}

	public String endXml(){
		return closeStrategies;
	}

	public void start(File dir, String instance, AnotherOutputSheet sheet) {
		Examples.createOutputFolder();
		gui.dispose();
		File[] directoryListing = dir.listFiles();			
		if (directoryListing != null) {	
			long startTestTime = System.currentTimeMillis();
			int testNo = 0;
			for (File child : directoryListing) {
				System.out.println(child.getName());
				OROoptions options = new OROoptions(XMLOUTFOLDER + "/" + child.getName(),
						instance + ".txt", REPETITIONS);
				
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
					sheet.addRepetition(solution, eTime, r, testNo);				
				}
				testNo++;
			}
			System.out.println("Done in " + ((System.currentTimeMillis() - startTestTime) / 1000.0) + " seconds");
			sheet.write();
		}
	}

	private static void setTimeLimit(VehicleRoutingAlgorithm vra, long timeMilliSec) {
		TimeTermination tterm = new TimeTermination(timeMilliSec);
		vra.setPrematureAlgorithmTermination(tterm);
		vra.addListener(tterm);
	}

	public static void main(String[] args) {
		new VariableTestGenerator();
	}
}
