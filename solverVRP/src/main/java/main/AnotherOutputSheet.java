package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class AnotherOutputSheet {
	
	private static final int STRATEGY_ROW = 0;
	private static final int FILEINFO_ROW = 4;
	private static final int VARIABLE_HEADER_ROW = 7;
	private static final int VARIABLE_COL_SIZE = 5;
	private static final int REPETITIONS_ROW = 17;
	
	private int meanRow;
	private int minRow;
	private int	maxRow;
	
	private class Position {
		protected int row;
		protected int col;
		
		public Position(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
		public String getCellString(){
			return CellReference.convertNumToColString(col) + "" + (row+1);
		}
	}
	
	private Position exactOptPosition;
	private Position heurOptPosition;
	
	private String instanceFile;
	
	private HSSFWorkbook workbook;
	
	public AnotherOutputSheet() {
		workbook = new HSSFWorkbook();
	}
	
	public void createVariableSheet(Strategy s1, Strategy s2, String instance, int repetitions,
			ArrayList<Double> range, String iterations, String memory) {
		
		this.instanceFile = instance;
		Sheet sheet = workbook.createSheet("Output");
		
		//Strategies
		Row row = sheet.createRow(STRATEGY_ROW);
		row.createCell(0).setCellValue("Ruin");
		row.createCell(1).setCellValue("Selector");
		row.createCell(2).setCellValue("Insertion");
		row.createCell(3).setCellValue("Acceptor");
		row.createCell(4).setCellValue("Share");
		row.createCell(5).setCellValue("Probability");
		row.createCell(6).setCellValue("Alpha");
		row.createCell(7).setCellValue("Warmup");		
		
		row = sheet.createRow(STRATEGY_ROW + 1);
		row.createCell(0).setCellValue(s1.ruin);
		row.createCell(1).setCellValue(s1.selector);
		row.createCell(2).setCellValue(s1.insertion);
		row.createCell(3).setCellValue(s1.acceptor);
		row.createCell(4).setCellValue(s1.share);
		row.createCell(5).setCellValue(s1.probability);
		if (s1.acceptor.equals("schrimpfAcceptance")) {
			row.createCell(6).setCellValue(s1.alpha);
			row.createCell(7).setCellValue(s1.warmup);
		}
		if (s1.isVariable)
			row.getCell(s1.variable + 4).setCellValue("Variable");
		
		row = sheet.createRow(STRATEGY_ROW + 2);
		row.createCell(0).setCellValue(s2.ruin);
		row.createCell(1).setCellValue(s2.selector);
		row.createCell(2).setCellValue(s2.insertion);
		row.createCell(3).setCellValue(s2.acceptor);
		row.createCell(4).setCellValue(s2.share);
		row.createCell(5).setCellValue(s2.probability);	
		if (s2.acceptor.equals("schrimpfAcceptance")) {
			row.createCell(6).setCellValue(s2.alpha);
			row.createCell(7).setCellValue(s2.warmup);
		}
		if (s2.isVariable)
			row.getCell(s2.variable + 4).setCellValue("Variable");
		
		//File info
		row = sheet.createRow(FILEINFO_ROW);
		row.createCell(0).setCellValue("File");
		row.createCell(1).setCellValue("Exact optimal");
		row.createCell(2).setCellValue("Heur optimal");
		row.createCell(3).setCellValue("Fleet size");
		row.createCell(4).setCellValue("Capacity");
		row.createCell(5).setCellValue("Iterations");
		row.createCell(6).setCellValue("Memory");
		
		exactOptPosition = new Position(FILEINFO_ROW + 1, 1);
		heurOptPosition = new Position(FILEINFO_ROW + 1, 2);
		row = sheet.createRow(FILEINFO_ROW + 1);
		Sheet inputInfo = read();
		Row inputRow = inputInfo.getRow(findRow(inputInfo, instanceFile + ".txt"));
		row.createCell(0).setCellValue(instanceFile);	
		row.createCell(1).setCellValue(inputRow.getCell(1).getNumericCellValue());
		row.createCell(2).setCellValue(inputRow.getCell(4).getNumericCellValue());
		row.createCell(3).setCellValue(inputRow.getCell(2).getNumericCellValue());
		row.createCell(4).setCellValue(inputRow.getCell(3).getNumericCellValue());
		row.createCell(5).setCellValue(iterations);
		row.createCell(6).setCellValue(memory);
		
		
		//Header for the variable
		String variableName;
		if (s1.isVariable)
			variableName = s1.getVariableName();
		else
			variableName = s2.getVariableName();
		
		int i = 0;	
		row = sheet.createRow(VARIABLE_HEADER_ROW);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue(range.get(i)+ " " + variableName);
			row.createCell(VARIABLE_COL_SIZE * i + 1).setCellValue("Cost");
			row.createCell(VARIABLE_COL_SIZE * i + 2).setCellValue("Time");
			row.createCell(VARIABLE_COL_SIZE * i + 3).setCellValue("Routes");
		}
		
		//Mean
		meanRow = VARIABLE_HEADER_ROW + 1;
		row = sheet.createRow(meanRow);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Mean");
			for (int j = 1; j < VARIABLE_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + VARIABLE_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula("Average(" + cellString + (REPETITIONS_ROW + 1) + ":" 
						+ cellString + (REPETITIONS_ROW + repetitions) + ")");
			}		
		}
		
		//Min
		minRow = VARIABLE_HEADER_ROW + 2;
		row = sheet.createRow(minRow);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Min");
			for (int j = 1; j < VARIABLE_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + VARIABLE_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula("Min(" + cellString + (REPETITIONS_ROW + 1) + ":" 
						+ cellString + (REPETITIONS_ROW + repetitions) + ")");
			}		
		}
		
		//Max
		maxRow = VARIABLE_HEADER_ROW + 3;
		row = sheet.createRow(maxRow);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Max");
			for (int j = 1; j < VARIABLE_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + VARIABLE_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula("Max(" + cellString + (REPETITIONS_ROW + 1) + ":" 
						+ cellString + (REPETITIONS_ROW + repetitions) + ")");
			}		
		}
		
		//Delta (max-min)
		row = sheet.createRow(VARIABLE_HEADER_ROW + 4);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Delta");
			for (int j = 1; j < VARIABLE_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + VARIABLE_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula(cellString + (maxRow + 1) + "-" 
						+ cellString + (minRow +1));
			}		
		}
		
		//Error (min-exact)
		row = sheet.createRow(VARIABLE_HEADER_ROW + 5);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Error %");
			Cell cell = row.createCell(1 + VARIABLE_COL_SIZE * i);
			String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
			cell.setCellFormula("(" + cellString + (minRow+1) + "-" + exactOptPosition.getCellString() + 
					")/" + exactOptPosition.getCellString());	
			row.createCell(VARIABLE_COL_SIZE * i + 2).setCellValue("(min-exact)");
		}
		
		//Error (mean-exact)
		row = sheet.createRow(VARIABLE_HEADER_ROW + 6);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Error %");
			Cell cell = row.createCell(1 + VARIABLE_COL_SIZE * i);
			String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
			cell.setCellFormula("(" + cellString + (meanRow+1) + "-" + exactOptPosition.getCellString() + 
					")/" + exactOptPosition.getCellString());
			row.createCell(VARIABLE_COL_SIZE * i + 2).setCellValue("(mean-exact)");
		}
		
		//Error (min-jsprit)
		row = sheet.createRow(VARIABLE_HEADER_ROW + 7);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Error %");
			Cell cell = row.createCell(1 + VARIABLE_COL_SIZE * i);
			String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
			cell.setCellFormula("(" + cellString + (minRow+1) + "-" + heurOptPosition.getCellString() + 
					")/" + heurOptPosition.getCellString());	
			row.createCell(VARIABLE_COL_SIZE * i + 2).setCellValue("(min-jsprit)");
		}
		
		//Error (mean-jsprit)
		row = sheet.createRow(VARIABLE_HEADER_ROW + 8);
		for (i = 0; i < range.size(); i++) {
			row.createCell(VARIABLE_COL_SIZE * i).setCellValue("Error %");
			Cell cell = row.createCell(1 + VARIABLE_COL_SIZE * i);
			String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
			cell.setCellFormula("(" + cellString + (meanRow+1) + "-" + heurOptPosition.getCellString() + 
					")/" + heurOptPosition.getCellString());	
			row.createCell(VARIABLE_COL_SIZE * i + 2).setCellValue("(mean-jsprit)");
		}	
		
		//Repetitions
		for (i = 0; i < repetitions; i++) {
			row = sheet.createRow(REPETITIONS_ROW + i);
			for (int j = 0; j < range.size(); j++)
				row.createCell(VARIABLE_COL_SIZE * j).setCellValue("Rep. " + (i + 1));
		}
	}
	
	public void addRepetition(VehicleRoutingProblemSolution solution, long eTime, int repetition, int test){
		Row row = workbook.getSheetAt(0).getRow(REPETITIONS_ROW + repetition);
		row.createCell(VARIABLE_COL_SIZE * test + 1).setCellValue(solution.getCost());
		row.createCell(VARIABLE_COL_SIZE * test + 2).setCellValue(eTime / 1000.0);
		row.createCell(VARIABLE_COL_SIZE * test + 3).setCellValue(solution.getRoutes().size());
	}
	
	public Sheet read() {
		try {
			FileInputStream in = new FileInputStream(new File("inputInfo.xls"));
			HSSFWorkbook wb = new HSSFWorkbook(in);
			Sheet sheet = wb.getSheetAt(0);
			wb.close();
			in.close();
			return sheet;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	private int findRow(Sheet sheet, String cellContent) {
		for (Row row : sheet) {
			if (row.getCell(0).getRichStringCellValue().getString().trim().equals(cellContent)) {
				return row.getRowNum();  
			}
		}
	    return -1;
	}
	
	public void write() {
		try {
			FileOutputStream out = new FileOutputStream(new File("output\\output.xls"));
			workbook.write(out);
			out.close();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
