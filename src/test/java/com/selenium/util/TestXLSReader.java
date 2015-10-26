package com.selenium.util;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TestXLSReader {

	private XLSReader xlsReader;
	String sysPath = System.getProperty("user.dir") + "/src/main/resources/";

	@Before
	public void init() {
		xlsReader = new XLSReader(sysPath + "Summary of manual replacement orders_TEST.xlsx");
	}

//	@Test
	public void testGetRowCOunt() {
		int count = xlsReader.getRowCount("rejectedReplacementRequests");
		Assert.assertEquals(3, count);
	}

	@Test
	public void testGetCellDataWithColmnName() {
		int count = xlsReader.getRowCount("rejectedReplacementRequests");
		String columnName = "Customer ID";
		for (int i = 1; i < count; i++) {
			String cusId = xlsReader.getCellData("rejectedReplacementRequests", columnName, i + 1);
			System.out.println(cusId.replaceAll(".0", ""));
		}
	}
}
