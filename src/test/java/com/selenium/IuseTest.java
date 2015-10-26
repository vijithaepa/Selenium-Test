package com.selenium;

import java.io.FileInputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.selenium.util.XLSReader;

public abstract class IuseTest {

	protected XLSReader xlsReader;
	protected WebDriver driver = new FirefoxDriver();
	public static Properties CONFIG = null;

	protected static final String STATUS_SUBMITTED = "Submitted";
	protected static final String STATUS_REJECTED = "Rejected";
	
	protected static final String RESPONSE_MESSAGE_SUCCESS = "Success";
	protected static final String RESPONSE_CODE_SUCCESS = "RC000";
	protected static final String ELEMENT_IFRAME = "iframe";
	
	@Before
	public void setUp() throws Exception {
		driver.get("http://localhost/iuse");
		CONFIG = new Properties();
		FileInputStream fn = new FileInputStream(System.getProperty("user.dir") + "//src//test/resources/config.properties");
		CONFIG.load(fn);
	}

	public void adminLogin() {
		WebElement element = driver.findElement(By.xpath(".//*[@id='edit-name']"));
		element.sendKeys("admin");
		element = driver.findElement(By.xpath(".//*[@id='edit-pass']"));
		element.sendKeys("admin123");
		element = driver.findElement(By.xpath(".//*[@id='edit-submit']"));
		element.click();
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

	@After
	public void tearDown() throws Exception {
		// driver.close();
	}
}
