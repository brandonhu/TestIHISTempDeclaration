package baseProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.relevantcodes.extentreports.DisplayOrder;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.NetworkMode;

public class ExtentReporterNG {
	static ExtentReports extent;
	
	public static ExtentReports getExtentReport()
	{
		DateFormat filedf = new SimpleDateFormat("ddMMMyy_HHmmss");
		String timestamp = filedf.format(Calendar.getInstance().getTime());
		String path = System.getProperty("user.dir") + "\\extentreports\\index_" + timestamp + ".html";
		System.out.println("Extent report generated path: " + path);
//		ExtentSparkReporter reporter = new ExtentSparkReporter(path);
//		reporter.config().setReportName("Test Extent Report");
//		reporter.config().setDocumentTitle("Test Results");
//		
		extent = new ExtentReports(path, true, DisplayOrder.NEWEST_FIRST, NetworkMode.OFFLINE);
//		extent.attachReporter(reporter);
		//set tester name
//		extent.setSystemInfo("Tester", "Tester Name");//role and name
		return extent;
	}
}
