/*
 * Last edit by: Brandon Hu
 * Last edited date: 22-Jul-2020
 * Version: v1.0.0.1
 * Update: Added code for NEHR automation
 */
package test;

//common java imports
import java.io.IOException;
import java.net.ConnectException;
//import java.text.ParseException;
//xml imports
import javax.xml.parsers.ParserConfigurationException;
//dom imports
import org.xml.sax.SAXException;

//own class import
import baseProperties.Base;
import objectRepo.*;
//log4j
import org.apache.logging.log4j.LogManager;
//selenium imports
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
//testNg imports
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * This is the test suite class which contains all 10 test cases
 */
public class DeclareTempReadingTest extends Base {
	int tcNo = 0; //position 0 holds the browser type data
	/**
	 * Before start of test suite initialize the driver
	 */
	@BeforeSuite
	public void init() {
		try {
			initDriver();// initialize all the web driver pre-configuration
		} catch (SessionNotCreatedException e) {
			String errMsg = "Protected mode settings are not the same for all zones, please enable/disable Protected mode for all zones using IE internet options."
					+ e.getMessage();
			log.error(errMsg);
			Assert.fail(errMsg);
		} catch (ConnectException e) {
			String errMsg = "Exception: " + e.getMessage();
			log.debug("Stacktrace:\n" + e);
			log.error(errMsg);
			Assert.fail(errMsg);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			String errMsg = "Fail to initialize the web driver due to test data properties and web elements locator file(s) issue: "
					+ e.getMessage();
			log.error(errMsg);
			Assert.fail(errMsg);
		} catch (WebDriverException e) {
			log.error("Fail to initialize the web driver due to web driver exception: " + e.getMessage());
			log.debug("Retry to re-initialize the web driver");
			init();// reinit the web driver
		} catch (Exception e) {
			String errMsg = "Exception during initialization of web driver: " + e.getMessage();
			log.error(errMsg);
			Assert.fail(errMsg);
		}
	}
	
	/**
	 * Test case 1 - Submit temperature reading
	 */
	@Test(description = "Submit temperature reading", priority = 1)
	public void tc01_SubmitTempReading() {
		tcNo += 1;//init test case number here
		log.info("Test case " + tcNo + " - Submit temperature reading");
		testData = getTestData(tcName.get(tcNo));
		try {
			//step 1: access to site
			driver.get(testData.get(0));
			//step 2: wait for the DOM obj be loaded completely
			waitUntilPageIsLoaded();
			//step 3: get page title
			log.info("Landed on: " + driver.getTitle());
			//init page object model
			SubmitTempReadings page = new SubmitTempReadings(driver, xmlElement);
			//step 4: enter the email
			page.emailField().sendKeys(testData.get(1));
			log.info("Enter the email.");
			//step 5: click on the location drop down list
			page.locationInput().click();
			//locationList.selectByVisibleText(testData.get(2));
			log.info("Location drop down is clicked.");
			//Thread.sleep(page.getLocationWaitTm());
			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(page.submitBtnLocator()));
			action.moveToElement(page.locationItem()).click().build().perform();
			//Thread.sleep(page.getLocationWaitTm());
			String selectedLocation = page.locationText().getText();
			if (selectedLocation.equalsIgnoreCase(testData.get(2)))
			{
				log.info("Location selected: " + selectedLocation);
			}
			else
			{
				Assert.fail("Location selected is not as per test data: " + testData.get(2));
			}
			//step 6: enter the temperature
			page.tempField().sendKeys(testData.get(3));
			//wait.until(ExpectedConditions.textToBePresentInElementValue(page.tempFieldLocator(), testData.get(3)));
			log.info("Temperature entered: " + testData.get(3));
			//step 7: choose i feel well
			page.feelWellRadBtn().click();
			log.info("Selected: " + page.feelWellRadBtn().getText());
			//step 8: click on the session radio button
			page.sessionRadBtn().click();
			if (page.getSessionType())
			{
				log.info("Session is selected AM");
			}
			else
			{
				log.info("Session is selected PM");
			}
			//step 9: click on the submit button
			page.submitBtn().click();
			log.info("Submit button is clicked.");
			waitUntilPageIsLoaded();
			wait.until(ExpectedConditions.titleIs("FormSG"));
			wait.until(ExpectedConditions.visibilityOfElementLocated(page.submitFormLinkLocator()));
			log.info("Next page loaded with the link: " + page.submitFormLink().getText());
			Thread.sleep(page.getLocationWaitTm());
			//step 10: landed on next page
			log.info("Test Case " + tcNo + " completed and landed on: " + driver.getTitle());
		} catch (JavascriptException e) {
			String errMsg = "Unexpected javascript exception: " + e.getMessage();
			log.debug("Stacktrace:\n" + e);
			log.error(errMsg);
			Assert.fail(errMsg);
			e.printStackTrace();
		} catch (TimeoutException e) {
			String errMsg = "WebDriver couldn't locate the element due to timeout: " + e.getMessage();
			log.debug("Stacktrace:\n" + e);
			log.error(errMsg);
			Assert.fail(errMsg);
		} catch (StaleElementReferenceException e) {
			String errMsg = "WebDriver couldn't locate the element due to DOM refreshed: " + e.getMessage();
			log.debug("Stacktrace:\n" + e);
			log.error(errMsg);
			Assert.fail(errMsg);
		} catch (NullPointerException e) {
			String errMsg = "WebDriver couldn't locate the element due to invalid xml tagname for the web locators: " + e.getMessage();
			log.debug("Stacktrace:\n" + e);
			log.error(errMsg);
			Assert.fail(errMsg);
		}
		catch (Exception e) {
			String errMsg = "Exception: " + e.getMessage();
			log.debug("Stacktrace:\n" + e);
			log.error(errMsg);
			Assert.fail(errMsg);
		} finally {
			testData.clear();// remove the test data after each test
		}
	}
	
	/**
	 * Quit the test suite and this method will always be invoked
	 */
	@AfterSuite(alwaysRun = true)
	public void quitDriver() {
		if (driver != null) {
			driver.close();// close the current focus window
			driver.quit();// close the driver.dispose and ends the webdriver session gracefully
			driver = null;// set web driver to null back to initial state
			log.info("Close the browser and all tests executed.");
			removeTestCredentials();// remove the test credentials in prop file
			backupTestReport();// backup the previous reportNG
			LogManager.shutdown();// close the log
		} else {
			log.error("Web driver instance already closed.");
		}
	}
}