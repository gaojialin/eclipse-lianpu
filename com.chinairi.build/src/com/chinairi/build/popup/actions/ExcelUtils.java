package com.chinairi.build.popup.actions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelUtils {
	public static String getValue(Cell cell) {
		if(cell != null){
			switch (cell.getCellTypeEnum()) {
			case STRING:
				return cell.getRichStringCellValue().getString();

			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue().toString();
				} else {
					return new Double(cell.getNumericCellValue()).toString();
				}
			case FORMULA:
				return cell.getCellFormula();
			default:
				return "";
			}
		}
		return "";
	}
	/**
	 * 获取单元格值
	 * @param sheet
	 * @param rowIndex
	 * @param cellIndex
	 * @return
	 */
	public static String getValue(Sheet sheet, int rowIndex,int cellIndex){
		return getValue(sheet.getRow(rowIndex).getCell(cellIndex));
	}
}
