package com.selenium.replacement;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.selenium.util.XLSReader;

/**
 * Update Rejected Replacement requests to fulfilled status.
 * 
 * @author vijitha
 *
 */
public class IuseFulfillReplacementOrder {

	private static final String ELEMENT_IFRAME = "iframe";

	private static final Log logger = LogFactory.getLog(IuseFulfillReplacementOrder.class);

	private final String sysPath = System.getProperty("user.dir") + "/src/main/resources/";

	private final String COLUMN_CUSTOMER_ID = "Customer ID";
	private final String COLUMN_ORDER_REF_NO = "Order Refence #";
	private final String COLUMN_OLD_MYKI = "old myki";
	private final String COLUMN_NEW_PAN = "new myki";
	private final String COLUMN_FULFILMENT_DATE = "fulfilment date";

	private WebDriver driver = new FirefoxDriver();

	// Initializing the property file reading
	public static Properties CONFIG = null;

	@Before
	public void setUp() throws Exception {
		driver.get("http://localhost/iuse");
		CONFIG = new Properties();
		FileInputStream fn = new FileInputStream(System.getProperty("user.dir") + "//src//test/resources/config.properties");
		CONFIG.load(fn);
	}

	@Test
	public void testEditReplaceOrder() throws InterruptedException {

		XLSReader xlsReader = new XLSReader(sysPath + "Summary of manual replacement orders_TEST.xlsx");

		adminLogin();

		WebElement element = driver.findElement(By.xpath(".//*[@id='toolbar-link-admin-content']"));
		element.click();

		List<WebElement> elements = driver.findElements(By.tagName(ELEMENT_IFRAME));

		for (WebElement replacementListFrame : elements) {

			driver.switchTo().frame(replacementListFrame);

			if ("Content | iUSE".equalsIgnoreCase(driver.getTitle())) {

				element = driver.findElement(By.id("edit-non-product-display-node-type"));
				new Select(element).selectByVisibleText("Request Replacement");
				Thread.sleep(5000);
				int count = xlsReader.getRowCount("RejectedReplacements");
				for (int i = 1; i < count; i++) {

					String cusId = xlsReader.getCellData("RejectedReplacements", COLUMN_CUSTOMER_ID, i + 1);
					System.out.println("Cust ID : " + cusId);
					System.out.println("Driver Title : " + driver.getTitle());
					By byCondition = By.xpath(".//*[@id='edit-field-customer-id-replacement-value']");
					By contentMenu = By.xpath(".//*[@id='toolbar-link-admin-content']");
					System.out.println("Current URL : " + driver.getCurrentUrl());
					if (isElementExixst(driver, contentMenu)) {
						System.out.println("Element is exists, get Active element");
					}

					if (isElementExixst(driver, byCondition)) {
						System.out.println("Element is available");
						driver.findElement(By.xpath(".//*[@id='edit-field-customer-id-replacement-value']")).clear();
						driver.findElement(By.xpath(".//*[@id='edit-field-customer-id-replacement-value']")).sendKeys(cusId.replaceAll(".0", ""));
						Thread.sleep(5000);
					} else {
						System.out.println("Element is NOT available");
						// List<WebElement> newFrames = driver.findElements(By.tagName(ELEMENT_IFRAME));
						// for (WebElement webElement : newFrames) {
						// driver.switchTo().frame(webElement);
						// System.out.println(driver.getTitle());
						// driver.switchTo().defaultContent();
						// }
					}
					// driver.manage().timeouts().implicitlyWait(10, // TimeUnit.HOURS);
					List<WebElement> rowElements = driver.findElements(By
							.xpath(".//*[@id='views-form-commerce-backoffice-content-system-1']/div/table/tbody/tr"));

					if (rowElements.size() == 1) {
						editReplacementOrder(replacementListFrame);
						Thread.sleep(3000);
					} else {
						logger.warn("more than one result or no element: " + rowElements.size());
					}
				}
			}
			driver.switchTo().defaultContent();
		}
	}

	private void editReplacementOrder(WebElement replacementListFrame) throws InterruptedException {
		driver.findElement(By.linkText("Edit")).click();
		Thread.sleep(3000);
		driver.switchTo().defaultContent();

		List<WebElement> newElements = driver.findElements(By.tagName(ELEMENT_IFRAME));
		By cond1 = By.xpath(".//*[@id='edit-field-myki-card-number-und-0-value']");
		By cond2 = By.xpath(".//*[@id='edit-field-replacement-reason-und']");
		// By cond3 = By.id("edit-field-replacement-reason-und");
		boolean isItemFound = false;

		for (WebElement replacementEdit : newElements) {
			driver.switchTo().frame(replacementEdit);
			// Check whether in the correct page
			if (!isItemFound && isElementExixst(driver, cond1) && isElementExixst(driver, cond2)) {
				updateReplacementOrder();
			}
			driver.switchTo().defaultContent();
		}
		// driver.switchTo().frame(replacementListFrame);
	}

	/**
	 * Update Replacement order to Fulfilled status.
	 */
	private void updateReplacementOrder() {

		if (isRecord()) {
			driver.findElement(By.xpath(".//*[@id='edit-field-response-code-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-response-message-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-fulfilment-date-und-0-value-date']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-card-pan-und-0-value']")).clear();
			driver.findElement(By.xpath(".//*[@id='edit-field-date-rejected-und-0-value-date']")).clear();

			driver.findElement(By.xpath(".//*[@id='edit-field-response-code-und-0-value']")).sendKeys("RC000");
			driver.findElement(By.xpath(".//*[@id='edit-field-response-message-und-0-value']")).sendKeys("Success");
			driver.findElement(By.xpath(".//*[@id='edit-field-fulfilment-date-und-0-value-date']")).sendKeys("07/10/2015 - 10:00:15pm");
			driver.findElement(By.xpath(".//*[@id='edit-field-card-pan-und-0-value']")).sendKeys("123488557937486");
			new Select(driver.findElement(By.xpath(".//*[@id='edit-field-replacement-status-und']"))).selectByVisibleText("Submitted");

			driver.findElement(By.xpath(".//*[@id='edit-cancel']")).click();

			// Click the save button
			// Open the record again
			// Validate the values
		} else {
			throw new RuntimeException("Not the valied record");
		}

	}

	private boolean isRecord() {

		return true;
	}

	public boolean isElementExixst(final WebDriver driver, final By by) {

		boolean isExists;
		try {
			driver.findElement(by);
			isExists = true;
		} catch (Throwable t) {
			isExists = false;
		}

		return isExists;
	}

	public void adminLogin() {
		WebElement element = driver.findElement(By.xpath(".//*[@id='edit-name']"));
		element.sendKeys("admin");
		element = driver.findElement(By.xpath(".//*[@id='edit-pass']"));
		element.sendKeys("admin123");
		element = driver.findElement(By.xpath(".//*[@id='edit-submit']"));
		element.click();
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

	@After
	public void tearDown() throws Exception {
		// driver.close();
	}
}
