package com.selenium;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.selenium.util.XLSReader;

public class PortalBase {

	final static Logger logger = Logger.getLogger(PortalBase.class);

	protected XLSReader xlsReader;
	protected WebDriver driver;
	protected static String DOMAIN;
	protected static Properties CONFIG = null;

	protected static final String STATUS_SUBMITTED = "Submitted";
	protected static final String STATUS_REJECTED = "Rejected";

	protected static final String RESPONSE_MESSAGE_SUCCESS = "Success";
	protected static final String RESPONSE_CODE_SUCCESS = "RC000";
	protected static final String ELEMENT_IFRAME = "iframe";

	public PortalBase() {
		setUp();
	}

	public void setUp() {
		CONFIG = new Properties();
		FileInputStream fn = null;
		try {
			// Running from eclipse
			fn = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/config.properties");

			CONFIG.load(fn);
		} catch (IOException e) {
			logger.error("failed to load from - /src/main/resources " + e.getCause() + "\tTry to load from /resources/");
		}

		try {
			if (fn == null) {
				// For stand alone app
				fn = new FileInputStream(System.getProperty("user.dir") + "/resources/config.properties");
				CONFIG.load(fn);
			}
		} catch (IOException e) {
			logger.error("failed to load from - /resources " + e.getCause());
			throw new RuntimeException("Cannot proceede. Cannot read the configurations");
		}
		String url = CONFIG.getProperty("env.url");
		DOMAIN = CONFIG.getProperty("env.domain");
		driver = new FirefoxDriver();
		driver.get(url);
	}

	public void adminLogin() {
		String userName = CONFIG.getProperty("login.user");
		String userPass = CONFIG.getProperty("login.password");

		WebElement element = driver.findElement(By.xpath(".//*[@id='edit-name']"));
		element.sendKeys(userName);
		element = driver.findElement(By.xpath(".//*[@id='edit-pass']"));
		element.sendKeys(userPass);
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

	public void tearDown() {
		logger.info("End time : " + new Date());
		driver.close();
	}
}
