package com.selenium.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSReader {

	final static Log logger = LogFactory.getLog(XLSReader.class);

	private XSSFWorkbook workbook;
	private XSSFSheet sheet;

	private XLSReader() {
		
	}
	public XLSReader(String path) {
		// this.path = path;
		FileInputStream fis;
		try {
			fis = new FileInputStream(path);
			workbook = new XSSFWorkbook(fis);
			// sheet = workbook.getSheetAt(0);
			fis.close();
		} catch (IOException e) {
			logger.error("Error reading input data sheet : " + e.getCause());
			throw new RuntimeException("Cannot proceede. Cannot read the input data sheet");
		}
	}

	public XLSReader(String path, String sheetName) {
		// this.path = path;
		FileInputStream fis;
		try {
			fis = new FileInputStream(path);
			workbook = new XSSFWorkbook(fis);
			sheet = workbook.getSheet(sheetName);
			fis.close();
		} catch (IOException e) {
			logger.error("Error reading input data sheet : " + e.getCause());
		}
	}

	/**
	 * Returns the row count.
	 * 
	 * @param sheetName
	 * @return
	 */
	public int getRowCount(String sheetName) {
		int index = workbook.getSheetIndex(sheetName);
		if (index == -1)
			return 0;
		else {
			sheet = workbook.getSheetAt(index);
			int number = sheet.getLastRowNum() + 1;
			return number;
		}

	}

	public XSSFSheet getSheet(final String sheet) {
		return workbook.getSheet(sheet);
	}

	/**
	 * Returns data in a cell by row number and column Name.
	 * 
	 * @param sheetName
	 * @param colName
	 * @param rowNum
	 * @return
	 */
	public String getCellData(String sheetName, String colName, int rowNum) {

		try {
			if (rowNum <= 0)
				return "";

			int sheetIndex = workbook.getSheetIndex(sheetName);
			int col_Num = -1;
			if (sheetIndex == -1)
				return "";

			sheet = workbook.getSheetAt(sheetIndex);
			XSSFRow row = sheet.getRow(0);
			for (int i = 0; i < row.getLastCellNum(); i++) {
				if (null != row.getCell(i) && row.getCell(i).getStringCellValue().trim().equals(colName.trim()))
					col_Num = i;
			}
			if (col_Num == -1)
				return "";

			sheet = workbook.getSheetAt(sheetIndex);
			row = sheet.getRow(rowNum - 1);
			if (row == null)
				return "";
			XSSFCell cell = row.getCell(col_Num);

			if (cell == null)
				return "";
			// System.out.println(cell.getCellType());
			if (cell.getCellType() == Cell.CELL_TYPE_STRING)
				return cell.getStringCellValue();
			else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC || cell.getCellType() == Cell.CELL_TYPE_FORMULA) {

//				String cellText = String.valueOf(cell.getNumericCellValue());
				String cellText = new BigDecimal(cell.getNumericCellValue()).toPlainString();
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					// format in form of M/D/YY
					double d = cell.getNumericCellValue();

					Calendar cal = Calendar.getInstance();
					cal.setTime(HSSFDateUtil.getJavaDate(d));
					cellText = (String.valueOf(cal.get(Calendar.YEAR)));
					int month = cal.get(Calendar.MONTH) + 1;
					cellText = cal.get(Calendar.DAY_OF_MONTH) + "/" + month + "/" + cellText + " " + cal.get(Calendar.HOUR) + ":"
							+ cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

					if(cal.get(Calendar.AM_PM) == 0){
						cellText += " am";
					} else {
						cellText += " pm";
					}
					// cellText = (String.valueOf(cal.get(Calendar.YEAR))).substring(2);
					// cellText = cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + 1 + "/" + cellText;

					// System.out.println(cellText);

				}
				return cellText;
			} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
				return "";
			else
				return String.valueOf(cell.getBooleanCellValue());

		} catch (Exception e) {

			e.printStackTrace();
			return "row " + rowNum + " or column " + colName + " does not exist in xls";
		}

	}

	/**
	 * Returns data in a cell by row number and column number.
	 * 
	 * @param sheetName
	 * @param colnum
	 * @param rowNum
	 * @return
	 */
	public String getCellData(String sheetName, int colnum, int rowNum) {

		return null;
	}

	public String getFormattedDate(String dsrcDte) throws ParseException {

//		String formDateFormat = CONFIG.getProperty("fulfilment.date.format");

		// Format: 14/10/2015 - 4:45:26pm
		SimpleDateFormat inutFormat = new SimpleDateFormat("dd/mm/yyyy h:m:s a");
		SimpleDateFormat formFormatter = new SimpleDateFormat("dd/mm/yyyy h:m:s a");

		Date date = inutFormat.parse(dsrcDte);
		String dt = formFormatter.format(date);
		return dt;
	}
	
	public static void main(String[] args) throws ParseException {
		
		XLSReader x = new XLSReader();
		System.out.println(x.getFormattedDate("01/02/2015 06:15:32 AM"));
	}
	
}
