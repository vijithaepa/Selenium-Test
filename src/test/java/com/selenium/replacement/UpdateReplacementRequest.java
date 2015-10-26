package com.selenium.replacement;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.selenium.IuseTest;
import com.selenium.util.XLSReader;

public class UpdateReplacementRequest extends IuseTest {

	// public UpdateReplacementRequest() throws IOException {
	// super();
	// // TODO Auto-generated constructor stub
	// }

	private static final String DATA_SHEET_NAME = "RejectedReplacements";


	private static final Log logger = LogFactory.getLog(IuseFulfillReplacementOrder.class);

	private final String sysPath = System.getProperty("user.dir") + "/src/main/resources/";

	private final String COLUMN_CUSTOMER_ID = "Customer ID";
	private final String COLUMN_ORDER_REF_NO = "Order Refence #";
	private final String COLUMN_OLD_MYKI = "old myki";
	private final String COLUMN_NEW_PAN = "new myki";
	private final String COLUMN_FULFILMENT_DATE = "fulfilment date";

	private String fulfilmentDate;
	private String cardPan;

	@Test
	public void testUpdateReplacemntRequest() throws InterruptedException {

		xlsReader = new XLSReader(sysPath + "Summary of manual replacement orders_TEST.xlsx");
		int count = xlsReader.getRowCount(DATA_SHEET_NAME);
		System.out.println("Input count : " + count);
		WebElement element;
		String cusId;

		adminLogin();
		for (int i = 2; i <= count; i++) {
			cusId = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_CUSTOMER_ID, i);
			System.out.println("Custoer ID : " + cusId);
			cusId = cusId.replace(".0", "");
			System.out.println("Custoer ID : " + cusId);

			// Click on Content menu
			if (!cusId.trim().isEmpty() && isElementExixst(driver, By.xpath(".//*[@id='toolbar-link-admin-content']"))) {
				driver.findElement(By.xpath(".//*[@id='toolbar-link-admin-content']")).click();
				Thread.sleep(5000);
				List<WebElement> elements = driver.findElements(By.tagName(ELEMENT_IFRAME));
				System.out.println("Looping all iframes");
				for (WebElement replacementListFrame : elements) {
					driver.switchTo().frame(replacementListFrame);

					if (isElementExixst(driver, By.id("edit-non-product-display-node-type"))
							&& isElementExixst(driver, By.xpath(".//*[@id='edit-field-customer-id-replacement-value']"))) {
						System.out.println("Elements are available");

						element = driver.findElement(By.id("edit-non-product-display-node-type"));
						new Select(element).selectByVisibleText("Request Replacement");
						Thread.sleep(5000);

						driver.findElement(By.xpath(".//*[@id='edit-field-customer-id-replacement-value']")).clear();
						driver.findElement(By.xpath(".//*[@id='edit-field-customer-id-replacement-value']")).sendKeys(cusId);
						Thread.sleep(3000);

						List<WebElement> rowElements = driver.findElements(By
								.xpath(".//*[@id='views-form-commerce-backoffice-content-system-1']/div/table/tbody/tr"));

						if (rowElements.size() == 1) {
							editReplacementOrder(replacementListFrame, i);
							Thread.sleep(3000);
						} else {
							logger.warn("more than one result or no element: " + rowElements.size());
						}
					}
					driver.switchTo().defaultContent();
				}
			}
		}
	}

	private void editReplacementOrder(WebElement replacementListFrame, int row) throws InterruptedException {
		driver.findElement(By.linkText("Edit")).click();
		Thread.sleep(3000);
		driver.switchTo().defaultContent();

		List<WebElement> newElements = driver.findElements(By.tagName(ELEMENT_IFRAME));
		By cond1 = By.xpath(".//*[@id='edit-field-myki-card-number-und-0-value']");
		By cond2 = By.xpath(".//*[@id='edit-field-replacement-reason-und']");
		// By cond3 = By.id("edit-field-replacement-reason-und");
		boolean isItemUpdated = false;

		for (WebElement replacementEdit : newElements) {
			driver.switchTo().frame(replacementEdit);
			// Check whether in the correct page
			if (!isItemUpdated && isElementExixst(driver, cond1) && isElementExixst(driver, cond2)) {
				updateReplacementOrder(row);
				isItemUpdated = true;
			}
			driver.switchTo().defaultContent();
		}
		// driver.switchTo().frame(replacementListFrame);
	}

	/**
	 * Update Replacement order to Fulfilled status.
	 */
	private void updateReplacementOrder(int row) {

		if (isRecordValid(row)) {
			driver.findElement(By.xpath(".//*[@id='edit-field-response-code-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-response-message-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-fulfilment-date-und-0-value-date']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-card-pan-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-date-rejected-und-0-value-date']")).clear();

//			fulfilmentDate = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_FULFILMENT_DATE, row);
			// cardPan = xlsReader.getCellData("RejectedReplacements", COLUMN_NEW_PAN, row);

			driver.findElement(By.xpath(".//*[@id='edit-field-response-code-und-0-value']")).sendKeys(RESPONSE_CODE_SUCCESS);
			driver.findElement(By.xpath(".//*[@id='edit-field-response-message-und-0-value']")).sendKeys(RESPONSE_MESSAGE_SUCCESS);
			driver.findElement(By.xpath(".//*[@id='edit-field-fulfilment-date-und-0-value-date']")).sendKeys(
					xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_FULFILMENT_DATE, row));
			driver.findElement(By.xpath(".//*[@id='edit-field-card-pan-und-0-value']")).sendKeys(
					xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_NEW_PAN, row));
			new Select(driver.findElement(By.xpath(".//*[@id='edit-field-replacement-status-und']"))).selectByVisibleText(STATUS_SUBMITTED);

			driver.findElement(By.xpath(".//*[@id='edit-submit']")).click();

			WebDriver augmentedDriver = new Augmenter().augment(driver);
			File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
			try {
				FileUtils.copyFile(screenshot, new File("C:\\tmp\\screenshot.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Click the save button
			// Open the record again
			// Validate the values
		} else {
			System.out.println("Not the valied record");

			driver.findElement(By.xpath(".//*[@id='edit-cancel']")).click();
		}

	}

	private boolean isRecordValid(int row) {
		WebElement element;
		String input;
		boolean isValidRecord = false;

		if (isElementExixst(driver, By.xpath(".//*[@id='edit-field-myki-card-number-und-0-value']"))) {
			element = driver.findElement(By.xpath(".//*[@id='edit-field-myki-card-number-und-0-value']"));
			input = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_OLD_MYKI, row);
			if (element.getAttribute("value").equalsIgnoreCase(input))
				isValidRecord = true;
		}

		if (isElementExixst(driver, By.xpath(".//*[@id='edit-field-ntt-order-ref-no-und-0-value']"))) {
			element = driver.findElement(By.xpath(".//*[@id='edit-field-ntt-order-ref-no-und-0-value']"));
			input = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_ORDER_REF_NO, row);
			if (element.getAttribute("value").equalsIgnoreCase(input)) {
				isValidRecord = true;
			} else {
				isValidRecord = false;
			}
		} else {
			isValidRecord = false;
		}
		if (isElementExixst(driver, By.xpath(".//*[@id='edit-field-replacement-status-und']"))) {
			element = driver.findElement(By.xpath(".//*[@id='edit-field-replacement-status-und']"));
			input = xlsReader.getCellData(DATA_SHEET_NAME, COLUMN_OLD_MYKI, row);
			String selected = (new Select(element)).getFirstSelectedOption().getText();
			if (selected.equalsIgnoreCase(STATUS_REJECTED)) {
				isValidRecord = true;
			} else {
				isValidRecord = false;
			}
		} else {
			isValidRecord = false;
		}

		return isValidRecord;
	}

	void waitForLoad(WebDriver driver) {
		ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(pageLoadCondition);
	}

}
