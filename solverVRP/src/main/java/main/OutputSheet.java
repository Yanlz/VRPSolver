package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class OutputSheet {
	
	private static final int HEADER_ROW_SIZE = 6;
	private static final int HEADER_COL_SIZE = 5;
	private static final int HEADER_ROW_START = 3;

	private int repetitionsRowStart;
	private int sharesPerSheet;
	private String instanceFile;
	
	private HSSFWorkbook workbook;

	public OutputSheet() {
		workbook = new HSSFWorkbook();
	}

	public void createSheet(String ruin, String selector, String insertion,
			String acceptor, String instanceFile, ArrayList<String> shares, int repetitions, int counter) {
		
		this.instanceFile = instanceFile;
		sharesPerSheet = shares.size();
		
		Sheet sheet = workbook.createSheet("Comb. " + counter);
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("Ruin");
		row.createCell(1).setCellValue("Selector");
		row.createCell(2).setCellValue("Insertion");
		row.createCell(3).setCellValue("Acceptor");
		row.createCell(6).setCellValue("File");
		row.createCell(7).setCellValue("Optimal cost");
		row.createCell(8).setCellValue("Fleet size");
		row.createCell(9).setCellValue("Capacity");
		
		row = sheet.createRow(1);
		row.createCell(0).setCellValue(ruin);
		row.createCell(1).setCellValue(selector);
		row.createCell(2).setCellValue(insertion);
		row.createCell(3).setCellValue(acceptor);
		
		Sheet inputInfo = read();
		Row inputRow = inputInfo.getRow(findRow(inputInfo, instanceFile + ".txt"));
		row.createCell(6).setCellValue(instanceFile);
		row.createCell(7).setCellValue(inputRow.getCell(1).getNumericCellValue());
		row.createCell(8).setCellValue(inputRow.getCell(2).getNumericCellValue());
		row.createCell(9).setCellValue(inputRow.getCell(3).getNumericCellValue());
		
		int i = 0;
		
		//Headers
		row = sheet.createRow(HEADER_ROW_START);
		for (i = 0; i < shares.size(); i++) {
			row.createCell(HEADER_COL_SIZE * i).setCellValue("Share" + shares.get(i));
			row.createCell(HEADER_COL_SIZE * i + 1).setCellValue("Cost");
			row.createCell(HEADER_COL_SIZE * i + 2).setCellValue("Time");
			row.createCell(HEADER_COL_SIZE * i + 3).setCellValue("Routes");
		}
		
		repetitionsRowStart = HEADER_ROW_START + HEADER_ROW_SIZE + 1;
		
		// Mean
		row = sheet.createRow(HEADER_ROW_START + 1);
		for (i = 0; i < shares.size(); i++) {
			row.createCell(HEADER_COL_SIZE * i).setCellValue("Mean");
			for (int j = 1; j < HEADER_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + HEADER_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula("Average(" + cellString + (repetitionsRowStart + 1) + ":" 
						+ cellString + (repetitionsRowStart + repetitions) + ")");
			}		
		}
		
		//Min
		row = sheet.createRow(HEADER_ROW_START + 2);
		for (i = 0; i < shares.size(); i++) {
			row.createCell(HEADER_COL_SIZE * i).setCellValue("Min");
			for (int j = 1; j < HEADER_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + HEADER_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula("Min(" + cellString + (repetitionsRowStart + 1) + ":" 
						+ cellString + (repetitionsRowStart + repetitions) + ")");
			}		
		}
		
		//Max
		row = sheet.createRow(HEADER_ROW_START + 3);
		for (i = 0; i < shares.size(); i++) {
			row.createCell(HEADER_COL_SIZE * i).setCellValue("Max");
			for (int j = 1; j < HEADER_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + HEADER_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula("Max(" + cellString + (repetitionsRowStart + 1) + ":" 
						+ cellString + (repetitionsRowStart + repetitions) + ")");
			}		
		}
		
		//Delta (max-min)
		row = sheet.createRow(HEADER_ROW_START + 4);
		for (i = 0; i < shares.size(); i++) {
			row.createCell(HEADER_COL_SIZE * i).setCellValue("Delta");
			for (int j = 1; j < HEADER_COL_SIZE - 1; j++) {
				Cell cell = row.createCell(j + HEADER_COL_SIZE * i);
				String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
				cell.setCellFormula(cellString + (cell.getRowIndex()) + "-" 
						+ cellString + (cell.getRowIndex() - 1));
			}		
		}
		
		//Error %
		row = sheet.createRow(HEADER_ROW_START + 5);
		for (i = 0; i < shares.size(); i++) {
			row.createCell(HEADER_COL_SIZE * i).setCellValue("Error %");
			Cell cell = row.createCell(1 + HEADER_COL_SIZE * i);
			String cellString = CellReference.convertNumToColString(cell.getColumnIndex());
			cell.setCellFormula("(" + cellString + (cell.getRowIndex() - 2) + "-" 
					+ sheet.getRow(1).getCell(7) + ")/" + sheet.getRow(1).getCell(7));		
		}
		
		//Repetitions
		for (i = 0; i < repetitions; i++) {
			row = sheet.createRow(repetitionsRowStart + i);
			for (int j = 0; j < shares.size(); j++)
				row.createCell(HEADER_COL_SIZE * j).setCellValue("Repet. " + (i + 1));
		}
	}
	
	public void writeSheet(VehicleRoutingProblemSolution solution,
			long eTime, int sheetNo, int repetitionNo, int shareNo) {
		
		Sheet sheet = workbook.getSheetAt(sheetNo);
		Row row = sheet.getRow(repetitionsRowStart + repetitionNo);
		
		row.createCell(HEADER_COL_SIZE * shareNo + 1).setCellValue(solution.getCost());
		row.createCell(HEADER_COL_SIZE * shareNo + 2).setCellValue(eTime / 1000.0);
		row.createCell(HEADER_COL_SIZE * shareNo + 3).setCellValue(solution.getRoutes().size());		
	}
	
	public void write() {       
		try {
			FileOutputStream out = new FileOutputStream(new File("output\\output" + instanceFile
					+ ".xls"));
			workbook.write(out);
			out.close();	
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public int getSharesPerSheet() {
		return sharesPerSheet;
	}
	
	//one sheet
	
	public OutputSheet(String instanceFile, int repetitions) {
		workbook = new HSSFWorkbook();
		this.instanceFile = instanceFile;
		
		Sheet sheet = workbook.createSheet("Output");
		int rowNo = repetitions;
		String cellIndex;
		
		Row row = sheet.createRow(rowNo);
		row.createCell(0).setCellValue("Max");
		for (int i = 1; i < 4; i++) {	
			cellIndex = CellReference.convertNumToColString(i);
			row.createCell(i).setCellFormula("Max(" + cellIndex + "1:" + cellIndex + repetitions + ")");
		}
		
		rowNo++;
		row = sheet.createRow(rowNo);
		row.createCell(0).setCellValue("Min");
		for (int i = 1; i < 4; i++) {	
			cellIndex = CellReference.convertNumToColString(i);
			row.createCell(i).setCellFormula("Min(" + cellIndex + "1:" + cellIndex + repetitions + ")");
		}
		
		rowNo++;
		
		row = sheet.createRow(rowNo);
		row.createCell(0).setCellValue("Mean");
		for (int i = 1; i < 4; i++) {	
			cellIndex = CellReference.convertNumToColString(i);
			row.createCell(i).setCellFormula("Average(" + cellIndex + "1:" + cellIndex + repetitions + ")");
		}
		rowNo++;
		
		row = sheet.createRow(rowNo);
		row.createCell(0).setCellValue("Delta");
		for (int i = 1; i < 4; i++) {	
			cellIndex = CellReference.convertNumToColString(i);
			row.createCell(i).setCellFormula(cellIndex + (rowNo - 2) + "-" + cellIndex + (rowNo - 1));
		}
		rowNo++;
		
		Sheet inputInfo = read();
		double optimal = inputInfo.getRow(findRow(inputInfo, instanceFile)).getCell(1).getNumericCellValue();
		
		row = sheet.createRow(rowNo);
		row.createCell(0).setCellValue("Error (min)");
		cellIndex = CellReference.convertNumToColString(1);
		row.createCell(1).setCellFormula("(" + cellIndex + (rowNo - 2) + "-" + optimal + ")/" + optimal);
		row.createCell(4).setCellValue(optimal);
		rowNo++;
		
		row = sheet.createRow(rowNo);
		row.createCell(0).setCellValue("Error (mean)");
		row.createCell(1).setCellFormula("(" + cellIndex + (rowNo - 2) + "-" + optimal + ")/" + optimal);
		row.createCell(4).setCellValue(optimal);
		
	}
	
	public void addOneRepetition(VehicleRoutingProblemSolution solution, long eTime, int repetition){
		Row row = workbook.getSheet("Output").createRow(repetition);
		row.createCell(0).setCellValue(repetition + 1);
		row.createCell(1).setCellValue(solution.getCost());
		row.createCell(2).setCellValue(eTime / 1000.0);
		row.createCell(3).setCellValue(solution.getRoutes().size());
	}
	
	public void writeSingleSheet() {
		try {
			FileOutputStream out = new FileOutputStream(new File("output\\output.xls"));
			workbook.write(out);
			out.close();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
