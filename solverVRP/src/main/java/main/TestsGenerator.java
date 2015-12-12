package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

public class TestsGenerator {
	
	private static final int REPETITIONS = 10;
	private static final String XMLOUTFOLDER = "xmlTestFolder";
	
	private static final String t1 = "<?xml version=\"1.0\" ?>\n\n<algorithm xmlns=\"http://www.w3schools.com\"\n     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3schools.com algorithm_schema.xsd\">\n<iterations>1024</iterations>\n	<construction>\n<insertion name=\"bestInsertion\">\n<considerFixedCosts weight=\"1.0\">true</considerFixedCosts>\n</insertion>\n</construction>\n<strategy><memory>5</memory><searchStrategies>\n	<searchStrategy name=\"randomRuinAndRecreate\">	<selector name=\"";
	private static final String t2 = "\"/>\n<acceptor name=\"";
	private static final String t3 = "\"/><modules><module name=\"ruin_and_recreate\"><ruin name=\"";
	private static final String t4 = "\"><share>";
	private static final String t5 = "</share></ruin><insertion name=\"";
	private static final String t6 = "\"/></module></modules><probability>";
	private static final String t7 = "</probability></searchStrategy></searchStrategies></strategy></algorithm>";
	private static final String t3bis = "\"><alpha>0.1</alpha><warmup>100</warmup></acceptor><modules><module name=\"ruin_and_recreate\"><ruin name=\"";
	
	
	private ArrayList<OutputSheet> outputs;
	private ArrayList<String> instanceFiles;
	
	public TestsGenerator() {}

	public File generate(){
		outputs = new ArrayList<OutputSheet>();
		
		//Instance files
		instanceFiles = new ArrayList<String>();
//		instanceFiles.add("C101");
//		instanceFiles.add("C102");
//		instanceFiles.add("C103");
//		instanceFiles.add("C104");
//		instanceFiles.add("C105");
//		instanceFiles.add("C106");
//		instanceFiles.add("C107");
//		instanceFiles.add("C108");
//		instanceFiles.add("C109");
//		instanceFiles.add("C201");
//		instanceFiles.add("C202");
//		instanceFiles.add("C203");
//		instanceFiles.add("C204");
//		instanceFiles.add("C205");
//		instanceFiles.add("C206");
//		instanceFiles.add("C207");
//		instanceFiles.add("C208");
		instanceFiles.add("rC101");
//		instanceFiles.add("rC102");
//		instanceFiles.add("rC103");
//		instanceFiles.add("rC104");
//		instanceFiles.add("rC105");
//		instanceFiles.add("rC106");
//		instanceFiles.add("rC107");
//		instanceFiles.add("rC108");
		instanceFiles.add("rC201");
//		instanceFiles.add("rC202");
//		instanceFiles.add("rC203");
//		instanceFiles.add("rC204");
//		instanceFiles.add("rC205");
//		instanceFiles.add("rC206");
//		instanceFiles.add("rC207");
//		instanceFiles.add("rC208");
		
		//Ruins
		ArrayList<String> ruins = new ArrayList<String>();
		ruins.add("radialRuin");
		ruins.add("randomRuin");
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
//		acceptors.add("acceptNewRemoveWorst");
//		acceptors.add("greedyAcceptance");
		acceptors.add("schrimpfAcceptance");
		
		//Share
		ArrayList<String> shares = new ArrayList<String>();
//		shares.add("0.1");
//		shares.add("0.2");
//		shares.add("0.3");
//		shares.add("0.4");
		shares.add("0.5");
//		shares.add("0.6");
//		shares.add("0.7");
//		shares.add("0.8");
//		shares.add("0.9");
//		shares.add("1.0");

		int nTests = selectors.size() * acceptors.size() * ruins.size() * shares.size() 
				* insertions.size() * instanceFiles.size() * REPETITIONS;
		System.out.println("Total tests to create: " + nTests + " ETA " + (nTests * 2) 
				+ " minutes (" + (nTests / 30) + " hours)");

		File dir = new File(XMLOUTFOLDER);
		if (dir.exists()) {
			File[] directoryListing = dir.listFiles();
			for (File child : directoryListing)
				child.delete();
		}
		dir.mkdir();
		
		int testNo = 0;
		
		for (String instanceFile: instanceFiles) {
			OutputSheet output = new OutputSheet();
			int sheetNo = 0;
			for (String ruin: ruins)
				for (String selector: selectors)
					for (String insertion: insertions)
						for (String acceptor: acceptors) {
							output.createSheet(ruin, selector, insertion, acceptor, 
									instanceFile, shares, REPETITIONS, sheetNo);
							sheetNo++;

							for (String share: shares) {
								String testName = "test";
								if (testNo < 10) 
									testName += "0";
								if (testNo < 100)
									testName += "0";
								testName += testNo;
								
								createXml(testName, 1.0, selector, acceptor, ruin, 
										insertion, share);
								testNo++;
							}
						}
			outputs.add(output);
		}
		return dir;
	}
	
	public void createXml(String fileName,Double probability, String selector, 
			String acceptor, String ruin,String insertion , String share ){

		try {
			String xmlContent = t1 + selector + t2 + acceptor ;
			if (acceptor == "schrimpfAcceptance")
				xmlContent += t3bis;
			else
				xmlContent += t3;
			xmlContent +=  ruin + t4 + share + t5 + insertion + t6 + probability + t7;
			FileUtils.writeStringToFile(new File(XMLOUTFOLDER + "/" + fileName + ".xml"), xmlContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<OutputSheet> getOutputs() {
		return outputs;
	}
	
	public ArrayList<String> getInstanceFiles() {
		return instanceFiles;
	}

	public String getXMLFolder() {
		return XMLOUTFOLDER;
	}

	public int getRepetitions() {
		return REPETITIONS;
	}
}
