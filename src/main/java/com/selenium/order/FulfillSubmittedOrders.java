package com.selenium.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.selenium.PortalBase;
import com.selenium.util.XLSReader;

public class FulfillSubmittedOrders extends PortalBase {

	private static int TIME_WAITING = 4000;

	final static Logger logger = Logger.getLogger(FulfillSubmittedOrders.class);
	private final String dataSheetPath;
	private final String dataSheetName;
	private final String dataSheetPage;

	// public static Properties CONFIG = null;
	private final String sysPath;
	private static String DATA_SHEET_NAME;

	// private final String COLUMN_CUSTOMER_ID = "Customer ID";
	private final String COLUMN_ORDER_NO = "Order No";
	private final String COLUMN_ORDER_REF_NO = "NTT Order Reference No";
	private final String COLUMN_CARD_PAN = "NTSCardPan";
	private final String COLUMN_FULFILMENT_DATE = "Fulfillment Date";

	public FulfillSubmittedOrders() {
		super();

		TIME_WAITING = Integer.parseInt(CONFIG.getProperty("action.waiting"));
		dataSheetPath = CONFIG.getProperty("inpud.datasheet.path");
		dataSheetName = CONFIG.getProperty("inpud.datasheet.file");
		dataSheetPage = CONFIG.getProperty("inpud.datasheet.sheet");

		sysPath = System.getProperty("user.dir") + dataSheetPath;
		DATA_SHEET_NAME = dataSheetPage;

	}

	public void fulfillSubmittedOrder() throws InterruptedException {
		xlsReader = new XLSReader(sysPath + dataSheetName);
		int count = xlsReader.getRowCount(DATA_SHEET_NAME);
		logger.info("Input Order count : " + count);
		logger.info("Start time : " + new Date());
		WebElement element;
		// String cusId;
		String orderNo;
		// List<WebElement> elements;
		// List<WebElement> orderEditElements;

		adminLogin();

		driver.navigate().to(DOMAIN + "/iuse/admin/commerce/orders");
		Thread.sleep(TIME_WAITING+1000);
		for (int i = 2; i <= count; i++) {
			orderNo = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_ORDER_NO, i);
			logger.info("Start processing on Order (row " + i + ") " + orderNo + " ################################");
			if (isElementExixst(driver, By.xpath(".//*[@id='edit-combine']")) && isElementExixst(driver, By.xpath(".//*[@id='edit-status']"))) {
				// Select Filter by
				new Select(driver.findElement(By.xpath(".//*[@id='edit-status']"))).selectByVisibleText("Submitted (Completed)");
				Thread.sleep(TIME_WAITING);
				// add order number on filter
				driver.findElement(By.xpath(".//*[@id='edit-combine']")).clear();
				driver.findElement(By.xpath(".//*[@id='edit-combine']")).sendKeys(orderNo);
				Thread.sleep(TIME_WAITING);

				List<WebElement> rowElements = driver.findElements(By
						.xpath(".//*[@id='views-form-commerce-backoffice-orders-admin-page']/div/table/tbody/tr"));

				if (rowElements.size() == 1 && isElementExixst(driver, By.xpath(".//*[@id='edit-views-bulk-operations-0']"))) {
					// Select Operation and click on Edit
					driver.findElement(By.xpath(".//*[@id='ctools-button-1']/div[1]")).click();
					Thread.sleep(1000);
					driver.findElement(By.xpath(".//*[@id='ctools-button-1']/div[2]/ul/li[2]/a")).click();
					Thread.sleep(TIME_WAITING);

					try {
						updateOrder(i);
						Thread.sleep(TIME_WAITING);
						if (isElementExixst(driver, By.xpath(".//*[@id='console']/div"))) {
							element = driver.findElement(By.xpath(".//*[@id='console']/div"));
							if (element.getText().contains("Order saved")) {
								logger.info("Order " + orderNo + " successfully updated");
							} else {
								logger.info("Order " + orderNo + " failed." + element.getText());
								driver.navigate().to(DOMAIN + "/iuse/admin/commerce/orders");
							}
						}
					} catch (RuntimeException e) {
						logger.warn("An error on updating order : " + orderNo + "\n" + e.getMessage());
					}
				} else if (rowElements.size() > 1) {
					logger.info("Having more than one results for " + orderNo);
					for (int j = 1; j < rowElements.size() + 1; j++) {
						element = driver.findElement(By.xpath(".//*[@id='views-form-commerce-backoffice-orders-admin-page']/div/table/tbody/tr[" + j
								+ "]/td[2]"));
						if (orderNo.equals(element.getText())) {
							// Select Operation and click on Edit
							driver.findElement(By.xpath(".//*[@id='ctools-button-" + j + "']/div[1]")).click();
							Thread.sleep(1000);
							driver.findElement(By.xpath(".//*[@id='ctools-button-" + j + "']/div[2]/ul/li[2]/a")).click();
							Thread.sleep(TIME_WAITING);

							try {
								updateOrder(i);
								Thread.sleep(TIME_WAITING);
								if (isElementExixst(driver, By.xpath(".//*[@id='console']/div"))) {
									element = driver.findElement(By.xpath(".//*[@id='console']/div"));
									if (element.getText().contains("Order saved")) {
										logger.info("Order " + orderNo + " successfully updated");
									} else {
										logger.info("Order " + orderNo + " failed." + element.getText());
										driver.navigate().to(DOMAIN + "/iuse/admin/commerce/orders");
									}
								}
							} catch (RuntimeException e) {
								logger.warn("An error on updating order : " + orderNo + "\n" + e.getMessage());
							}
							j = rowElements.size() + 1;
						}
					}
				} else {
					logger.warn("Found no results for : " + orderNo);
				}
			} else {
				logger.error("Cannot find the Orders List page for : " + orderNo);
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
			String cardPan = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_CARD_PAN, row);
			try {
				fulfilDate = getFormattedDate(fulfilDate);
			} catch (ParseException e) {
				throw new RuntimeException("Date format exception on : " + fulfilDate);
			}
			logger.info("Card pan and Fulfillment date for order : " + cardPan + ", " + fulfilDate);

			new Select(driver.findElement(By.xpath(".//*[@id='edit-status']"))).selectByVisibleText(STATUS_SUBMITTED);
			driver.findElement(By.xpath(".//*[@id='edit-field-card-pan-und-0-value']")).sendKeys(cardPan);
			driver.findElement(By.xpath(".//*[@id='edit-field-response-code-und-0-value']")).sendKeys(RESPONSE_CODE_SUCCESS);
			driver.findElement(By.xpath(".//*[@id='edit-field-response-message-und-0-value']")).sendKeys(RESPONSE_MESSAGE_SUCCESS);
			driver.findElement(By.xpath(".//*[@id='edit-field-fulfilment-date-und-0-value-date']")).sendKeys(fulfilDate);

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
			// WebElement customer = driver.findElement(By.xpath(".//*[@id='edit-field-customer-und-0-value']"));
			String selectedStatus = (new Select(driver.findElement(By.xpath(".//*[@id='edit-status']")))).getFirstSelectedOption().getText();

			String inputRefNo = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_ORDER_REF_NO, row);
			// String inpuCustomer = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_CUSTOMER_ID, row);

			if (refNo.getAttribute("value").equalsIgnoreCase(inputRefNo.trim()) && refNo.getAttribute("value").equalsIgnoreCase(inputRefNo.trim())
					&& selectedStatus.equalsIgnoreCase(STATUS_SUBMITTED))
				isValidRecord = true;
		}

		return isValidRecord;
	}

	public String getFormattedDate(String dsrcDte) throws ParseException {

		String formDateFormat = CONFIG.getProperty("fulfilment.date.format");

		//Reading format (Regional format)
		SimpleDateFormat inutFormat = new SimpleDateFormat("d/M/yyyy h:m:s a");
		
		SimpleDateFormat formFormatter = new SimpleDateFormat(formDateFormat);

		Date date = inutFormat.parse(dsrcDte);
		String outDate = formFormatter.format(date);
		return outDate.toLowerCase();
	}

	public static void main(String[] args) throws Exception {

		FulfillSubmittedOrders handler = new FulfillSubmittedOrders();
		handler.fulfillSubmittedOrder();
		handler.tearDown();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		driver.close();
	}
}
