package com.selenium.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.selenium.IuseTest;
import com.selenium.util.XLSReader;

public class TestFulfillSubmittedOrders extends IuseTest {

	final static Logger logger = Logger.getLogger(TestFulfillSubmittedOrders.class);

	public static Properties CONFIG = null;
	private final String sysPath = System.getProperty("user.dir") + "\\src\\main\\resources\\";
	private static final String DATA_SHEET_NAME = "Sample Sheet";

	private final String COLUMN_CUSTOMER_ID = "Customer ID";
	private final String COLUMN_ORDER_NO = "Order No";
	private final String COLUMN_ORDER_REF_NO = "NTT Order Reference No";
	private final String COLUMN_CARD_PAN = "NTSCardPan";
	private final String COLUMN_FULFILMENT_DATE = "Fulfillment Date";

	@Test
	public void testFulfillSubmittedOrder() throws InterruptedException {
		xlsReader = new XLSReader(sysPath + "updated old submitted orders not fulfilled in the iuse portal.xlsx");
		int count = xlsReader.getRowCount(DATA_SHEET_NAME);
		System.out.println("Input count : " + count);
		WebElement element;
		String cusId;
		String orderNo;
		List<WebElement> elements;
		List<WebElement> orderEditElements;

		adminLogin();

		driver.navigate().to("localhost/iuse/admin/commerce/orders");
		Thread.sleep(4000);
		for (int i = 2; i <= count; i++) {
			orderNo = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_ORDER_NO, i);
			if (isElementExixst(driver, By.xpath(".//*[@id='edit-combine']")) && isElementExixst(driver, By.xpath(".//*[@id='edit-status']"))) {
				// Select Filter by
				new Select(driver.findElement(By.xpath(".//*[@id='edit-status']"))).selectByVisibleText("Submitted (Completed)");
				Thread.sleep(3000);
				// add order number on filter
				driver.findElement(By.xpath(".//*[@id='edit-combine']")).clear();
				driver.findElement(By.xpath(".//*[@id='edit-combine']")).sendKeys(orderNo);
				Thread.sleep(3000);

				List<WebElement> rowElements = driver.findElements(By
						.xpath(".//*[@id='views-form-commerce-backoffice-orders-admin-page']/div/table/tbody/tr"));
				// List<WebElement> rowElements = driver.findElements(By
				// .xpath(".//*[@id='views-form-commerce-backoffice-content-system-1']/div/table/tbody/tr"));

				if (rowElements.size() == 1 && isElementExixst(driver, By.xpath(".//*[@id='edit-views-bulk-operations-0']"))) {
					// Select Operation and click on Edit
					driver.findElement(By.xpath(".//*[@id='ctools-button-1']/div[1]")).click();
					Thread.sleep(800);
					driver.findElement(By.xpath(".//*[@id='ctools-button-1']/div[2]/ul/li[2]/a")).click();
					Thread.sleep(3000);

					try {
						updateOrder(i);
						logger.info("Order " + orderNo + " successfully updated");
					} catch (RuntimeException e) {
						logger.warn("An error on updating order : " + orderNo + "\n" + e.getMessage());
					}
				} else {
					logger.warn("Found (" + rowElements.size() + ") resultsmore. Should be only 1 result for order : " + orderNo);
				}
			} else {
				logger.error("Cannot find the Orders List page");
			}
		}
	}

	/**
	 * Update Replacement order to Fulfilled status.
	 */
	private void updateOrder(int row) {

		if (isRecordValid(row)) {
			driver.findElement(By.xpath(".//*[@id='edit-field-card-pan-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-response-code-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-response-message-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-fulfilment-date-und-0-value-date']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-date-rejected-und-0-value-date']")).clear();

			String fulfilDate = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_FULFILMENT_DATE, row);
			try {
				fulfilDate = getFormattedDate(fulfilDate);
			} catch (ParseException e) {
				throw new RuntimeException("Date format exception on : " + fulfilDate);
			}
			new Select(driver.findElement(By.xpath(".//*[@id='edit-status']"))).selectByVisibleText(STATUS_SUBMITTED);
			driver.findElement(By.xpath(".//*[@id='edit-field-card-pan-und-0-value']")).sendKeys(fulfilDate);
			driver.findElement(By.xpath(".//*[@id='edit-field-response-code-und-0-value']")).sendKeys(RESPONSE_CODE_SUCCESS);
			driver.findElement(By.xpath(".//*[@id='edit-field-response-message-und-0-value']")).sendKeys(RESPONSE_MESSAGE_SUCCESS);
			driver.findElement(By.xpath(".//*[@id='edit-field-fulfilment-date-und-0-value-date']")).sendKeys(
					xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_FULFILMENT_DATE, row));

			driver.findElement(By.xpath(".//*[@id='edit-submit']")).click();
			// driver.findElement(By.xpath(".//*[@id='edit-actions']/a")).click();
			// Click the save button
			// Open the record again
			// Validate the values
		} else {
			driver.findElement(By.xpath(".//*[@id='edit-actions']/a")).click();
			throw new RuntimeException("Not the valid record : Order ref no, Customer ID and status not matching...");
		}

	}

	private boolean isRecordValid(int row) {

		boolean isValidRecord = false;

		// NTT Order Ref no, Customer ID, Status
		if (isElementExixst(driver, By.xpath(".//*[@id='edit-field-ntt-order-ref-no-und-0-value']"))
				&& isElementExixst(driver, By.xpath(".//*[@id='edit-field-customer-und-0-value']"))
				&& isElementExixst(driver, By.xpath(".//*[@id='edit-status']"))) {

			WebElement refNo = driver.findElement(By.xpath(".//*[@id='edit-field-ntt-order-ref-no-und-0-value']"));
			WebElement customer = driver.findElement(By.xpath(".//*[@id='edit-field-customer-und-0-value']"));
			String selectedStatus = (new Select(driver.findElement(By.xpath(".//*[@id='edit-status']")))).getFirstSelectedOption().getText();

			String inputRefNo = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_ORDER_REF_NO, row);
			String inpuCustomer = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_CUSTOMER_ID, row);

			if (refNo.getAttribute("value").equalsIgnoreCase(inputRefNo.trim()) && refNo.getAttribute("value").equalsIgnoreCase(inputRefNo.trim())
					&& selectedStatus.equalsIgnoreCase(STATUS_SUBMITTED))
				isValidRecord = true;
		}

		return isValidRecord;
	}

	public String getFormattedDate(String dsrcDte) throws ParseException {

		// Local host Date Format: 2015-10-14

		// Format: 14/10/2015 - 4:45:26pm
		SimpleDateFormat inutFormat = new SimpleDateFormat("d/m/yyyy h:m:s a");
		SimpleDateFormat formFormatter = new SimpleDateFormat("yyyy-MM-dd"); // TODO :get from Config

		String dateS = "12/2/2015 9:59:53 AM";
		Date date = inutFormat.parse(dsrcDte);
//		System.out.println(date);
//		System.out.println(formFormatter.format(date));

		return formFormatter.format(date);

	}
}
