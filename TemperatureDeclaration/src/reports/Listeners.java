/*
 * Last edit by: Brandon Hu
 * Last edited date: 23-Jul-2020
 * version: v1.0
 * updates: New code to capture screenshots for NEHR test scenarios in the test report
 */
package reports;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import baseProperties.Base;
import baseProperties.ExtentReporterNG;

/**
 * This class is listener for the test suite for test results such test failure or test success 
 */
public class Listeners implements ITestListener {
	Base base = new Base();
	ExtentReports extent = ExtentReporterNG.getExtentReport();
	ExtentTest test;
	
	@Override
	public void onFinish(ITestContext arg0) {
		extent.flush();//flush the extent report
//		extent.close();
	}

	@Override
	public void onStart(ITestContext arg0) {
		
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		
	}

	@Override
	//Test fail take screenshot
	public void onTestFailure(ITestResult result) {
		if (result.getStatus() == ITestResult.FAILURE)
		{
			test.log(LogStatus.FAIL, "Test Failed! \nStacktrace:" + result.getThrowable() + test.addScreenCapture(base.getScreenShotPath(result.getName())));//logs of the failure
		}
		base.getTCFailScreenshot(result.getName());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		if (result.getStatus() == ITestResult.SKIP)
		{
			test.log(LogStatus.SKIP, "Test Skipped: " + result.getThrowable());//logs of the failure
		}
	}

	@Override
	public void onTestStart(ITestResult result) {
		System.out.println("On test start");
		test = extent.startTest(result.getMethod().getMethodName(),result.getMethod().getDescription());
	}

	@Override 
	//Test success take screenshot
	public void onTestSuccess(ITestResult result) {
		test.log(LogStatus.PASS, "Test Passed" + test.addScreenCapture(base.getScreenShotPath(result.getName())));
		base.getTCPassScreenshot(result.getName());
	}
}