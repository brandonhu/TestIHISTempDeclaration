/*
 * Last edit by: Brandon Hu
 * Last edited date: 22-Jul-2020
 * Version: v1.0.0.1
 * Update: Added code for NEHR automation
 */
package baseProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
//import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import test.DeclareTempReadingTest;

/**
 * Base class handles browser setup, loading of configuration file(s) and other
 * reusable methods such as taking screenshots, sync issues and many more.
 * Benefits: - Avoid code duplication - Reuse code as much we can
 **/
public class Base {
	// selenium variables
	public static WebDriver driver = null;
	public static WebDriverWait wait = null;
	public static Actions action = null;
	public static String browserType = null;
	public int waitAlertTime = 0;
	public int loginWaitTime = 0;
	// properties file variables
	public Properties prop = null;
	// xml file variables
	public Element xmlElement = null;
	// log4j obj
	public static Logger log = null;
	// path
	public String path = "";
	// epos node path
	public static String nodeName = "";
	// data source origin from excel data source
	public Boolean tdExcelDataSrc = false;
	// store test case name
	public ArrayList<String> tcName = null;
	// store test data for use in the test cases
	public ArrayList<String> testData = null;
	// prop file name
	private String propFileName = "\\application.properties";

	/**
	 * Default constructor
	 */
	public Base() {
	}

	/**
	 * Initialize the web driver for test automation
	 */
	public WebDriver initDriver() throws IOException, SAXException, ParserConfigurationException {
		killWebDriver();//kill all zombie web drivers if any
		nodeName = "TestSuiteReport";// hold the node name of the test report
		int impWaitTime = 0;// default implicit wait time for web driver
		int expWaitTime = 0;// default explicit wait time for web driver
		path = System.getProperty("user.dir");//get the project directory
		// init user & application configurations
		initLog4j();
		initProperties();
		initTestDataSrc();
		initWebLocators();
		// for reportNG - prevent logger to not escape html characters
		//System.setProperty("org.uncommons.reportng.escape-output", "false");
		// create web driver
		setWebDriver();
		driver.manage().window().maximize(); // maximize the browser window
		action = new Actions(driver);// init action class
		try {// read the synchronization wait time
			impWaitTime = Integer.parseInt(xmlElement.getElementsByTagName("impWaitTime").item(0).getTextContent());
			expWaitTime = Integer.parseInt(xmlElement.getElementsByTagName("expWaitTime").item(0).getTextContent());
			waitAlertTime = Integer.parseInt(xmlElement.getElementsByTagName("waitAlertTime").item(0).getTextContent());
		} catch (NumberFormatException e) {
			log.info("The wait time was not entered with proper numbers.");
			// set longest wait time to prevent adverse effect on test run time, esp when network is slowed
			impWaitTime = 30;
			expWaitTime = 60;
			waitAlertTime = 10;
		}
		driver.manage().timeouts().implicitlyWait(impWaitTime, TimeUnit.SECONDS);// set global timer for the driver object instance
		wait = new WebDriverWait(driver, expWaitTime);// set driver explicit wait time
		//setLoginWaitTm();//set the pause time require for user to enter credentials
		return driver;
	}

	private void killWebDriver() throws IOException
	{
//		Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe /T");// kill all zombie IE webdriver
//		Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe /T");// kill all zombie Edge webdriver
		Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");// kill all zombie Chrome webdriver
	}
	/**
	 * Set the web driver
	 * 
	 */
	private void setWebDriver() {
		//to read from the excel file instead of reading from the data properties file
		testData = getTestData(tcName.get(0));//read from excel the browser type
		//String browserType = prop.getProperty("Browser").trim();
		browserType = testData.get(0).trim();
		log.info("Browser type selected: " + browserType);
		// init various web driver base on what is the browser type specify by the user
		if (browserType.equalsIgnoreCase("IE")) {
			driver = initIEDriver();
			log.debug("IE web driver is created successfully.");
			// reset the zoom settings for IE browser to 100% to allow selenium to correctly
			// detect the web elements
			try {
				driver.findElement(By.xpath("//html")).sendKeys(Keys.chord(Keys.CONTROL, "0"));
			} catch (NoSuchElementException e) {
				log.error("IE browser zoom settings cannot be set as 100% by BAT.");
			}
			log.debug("IE browser zoom settings is set as 100%");
		} 
		else if (browserType.equalsIgnoreCase("Edge")) {
			driver = initEdgeDriver();
			driver.manage().window().maximize();
			log.debug("Edge web driver is created successfully.");
			//log.debug("Base url: " + driver.getCurrentUrl());
		}
		else if (browserType.equalsIgnoreCase("chrome")) {
			driver = initChromeDriver();
			driver.manage().window().maximize();
			log.debug("Chrome web driver is created successfully.");
			//log.debug("Base url: " + driver.getCurrentUrl());
		}
//		else if (browserType.equalsIgnoreCase("Headless")) {
//			driver = new HtmlUnitDriver(true);// enable js capability
//		} 
//		else if (browserType.equalsIgnoreCase("Remote")) {// cross browser testing - selenium grid testing
//			DesiredCapabilities dc = new DesiredCapabilities();
//			dc.setBrowserName("internet explorer");// cross browser testing only supports IE
//			dc.setPlatform(Platform.WINDOWS);
//			try {
//				driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), dc);
//				log.debug("Cross browser web driver is created successfully.");
//			} catch (MalformedURLException e) {
//				log.error("URL is malformed");
//			}
//		} 
		else {
			driver = null;
			log.debug("Browser type: " + browserType + " defined is not supported by BAT.");
		}
	}

	/**
	 * initialize log4j
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void initLog4j() throws FileNotFoundException, IOException {
		System.setProperty("log4j.configurationFile", path + "\\resources\\log4j2.xml");
		log = LogManager.getLogger(DeclareTempReadingTest.class.getName());
	}

	/**
	 * load the properties file
	 * @throws IOException
	 */
	private void initProperties() throws IOException {
		prop = new Properties();
		FileInputStream testDataFile;
		testDataFile = new FileInputStream(path + propFileName);
		prop.load(testDataFile);
	}

	/**
	 * initialize if the test data source is to be read from properties file or excel
	 */
	private void initTestDataSrc() {
		if (prop.getProperty("TestDataSource").equalsIgnoreCase("Excel")) {
			tdExcelDataSrc = true;
			// init the test case array lists
			tcName = new ArrayList<String>();
			testData = new ArrayList<String>();
			getTCName(getTCCol());// retrieve all the test cases name from excel
		} else {
			tdExcelDataSrc = false;
		}
	}

	/**
	 * get test data sheet name from excel file
	 * @return excel sheet name
	 */
	private XSSFSheet getTDSheetName() {
		XSSFSheet sheet = null;
		try {
			String fisPath = path + "\\" + prop.getProperty("TestDataFileName");// test data file path
			System.out.println("test data file name: " + fisPath);
			FileInputStream fis = new FileInputStream(fisPath);
			// Read the test data from the excel file
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			int sheets = workbook.getNumberOfSheets();
			for (int i = 0; i < sheets; i++) {
				if (workbook.getSheetName(i).equalsIgnoreCase(prop.getProperty("TestDataSheetName"))) {
					sheet = workbook.getSheetAt(i);
					break;// stop searching
				}
			}
			workbook.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: The test data excel file is not accessible.\n" + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: The test data excel file is not accessible.\n" + e.getMessage());
		}
		return sheet;
	}

	/**
	 * get test case column from excel file
	 * @return the column index
	 */
	private int getTCCol() {
		Iterator<Row> rows = getTDSheetName().iterator();// sheet is collection of rows
		Row firstrow = rows.next();
		Iterator<Cell> ce = firstrow.cellIterator();// row is collection of cells
		int col = 0;
		while (ce.hasNext()) { // identify test case column
			Cell value = ce.next();
			if (value.getStringCellValue().equalsIgnoreCase("Test Case Name")) {
				return col;
			}
			col++;
		}
		return -1;
	}

	/**
	 * get test case name from excel file
	 */
	private void getTCName(int col) {
		Iterator<Row> rows = getTDSheetName().iterator();// sheet is collection of rows
		Row r = rows.next();// skip the first row which is the test cases table header row
		String testCaseName = "";
		while (rows.hasNext()) {
			r = rows.next();
			testCaseName = r.getCell(col).getStringCellValue();
			if (!(testCaseName.isEmpty() || testCaseName.contentEquals(""))) {
				tcName.add(testCaseName);
			}
		}
	}

	/**
	 * get test data from excel file
	 * @return list of test data read from the excel file
	 */
	public ArrayList<String> getTestData(String tcName) {
		ArrayList<String> td = new ArrayList<String>();
		Iterator<Row> rows = getTDSheetName().iterator();
		Row r = rows.next();// skip the first row which is the test cases table header row
		int col = getTCCol();
		// Col is identified then scan entire testcase col to identify specific test case name
		while (rows.hasNext()) {
			r = rows.next();
			if (r.getCell(col).getStringCellValue().equalsIgnoreCase(tcName)) {
				// Grab specific testcase row then pull all the data of that row and feed to
				// test suite
				Iterator<Cell> cv = r.cellIterator();
				Cell c = cv.next();// skip the test case name
				while (cv.hasNext()) {
					c = cv.next();
					switch (c.getCellType()) {// find out the cell type and process the value
					case STRING:
						String testData = c.getRichStringCellValue().getString();
						td.add(testData);
						log.debug("Test data is string type: " + testData);
						break;
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(c)) {
							try {
								SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
								testData = sdf.format(c.getDateCellValue());
								td.add(testData);
								log.debug("Test data is date type: " + testData);
							} catch (NullPointerException e)// - if the given pattern is null
							{
								log.error("NullPointerException: Given date pattern is null!");
							} catch (IllegalArgumentException e)// - if the given pattern is invalid
							{
								log.error("IllegalArgumentException: Given date pattern is invalid!");
							}
						} else {
							if (c.getNumericCellValue() % 1 == 0) // deduce if it is decimal value
							{
								testData = String.valueOf((int) c.getNumericCellValue());
								td.add(testData);
								log.debug("Test data is integer type: " + testData);
							} else {
								testData = String.valueOf(c.getNumericCellValue());
								td.add(testData);
								log.debug("Test data is decimal type: " + testData);
							}
						}
						break;
					case BOOLEAN:
						testData = String.valueOf(c.getBooleanCellValue());
						td.add(testData);
						log.debug("Test data is boolean type: " + testData);
						break;
					case FORMULA:
						testData = c.getCellFormula();
						td.add(testData);
						log.debug("Test data is formula type: " + testData);
						break;
					default:
						//log.debug("Default type (e.g. empty cells) not added as a test data");
						break;
					}
				}
			}
		}
		return td;
	}

	/**
	 * load the xml file containing the application web elements and configuration
	 * data (e.g. wait time)
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private void initWebLocators() throws SAXException, IOException, ParserConfigurationException {
		File xmlFile = new File(path + "\\weLocators.xml");
		// create instance for document builder factory
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
		Document doc = dbBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		// get node list
		NodeList xmlNodeList = doc.getChildNodes();
		// get first node in the xml file
		Node xmlNode = xmlNodeList.item(0);
		// assign node element
		xmlElement = (Element) xmlNode;
	}

	/**
	 * initialize the IE web driver for test automation url for the web site to be tested
	 * @return IE driver
	 */
	public InternetExplorerDriver initIEDriver() {
		// Launch the web driver with a fix port
		String wdpath = path + xmlElement.getElementsByTagName("iewebdriverpath").item(0).getTextContent();
		/*
		 * Commented this code to let selenium web driver random picks its own port by
		 * fixing to a port does not help as the web driver at times due to IE issue
		 * will failed to start InternetExplorerDriverService.Builder svcBuilder = new
		 * InternetExplorerDriverService.Builder(); try {
		 * svcBuilder.usingPort(Integer.parseInt(xmlElement.getElementsByTagName(
		 * "wdport").item(0).getTextContent())); } catch (NumberFormatException e) {
		 * log.info("The web driver port number was amended with improper port number."
		 * ); svcBuilder.usingPort(40537); }
		 */
		// svcBuilder.usingDriverExecutable(new File(wdpath));
		// InternetExplorerDriverService service = svcBuilder.build();
		// Configure IE driver capabilities
		DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
		// Configure logging for the IE web driver
		ieCapabilities.setCapability(InternetExplorerDriver.LOG_FILE, path + "\\logs\\IEDriver.log");
		ieCapabilities.setCapability(InternetExplorerDriver.LOG_LEVEL, "TRACE");
		// Enable this capability to accept all SSL certs by defaults.
		ieCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

		// declare the IE options object to store the IE capabilities
		InternetExplorerOptions options = new InternetExplorerOptions();
		options.requireWindowFocus();
		options.ignoreZoomSettings();
		//if set to accept means it will accept all alert by default
		options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
		options.merge(ieCapabilities);

		// create ie driver and return the web driver obj
		System.setProperty("webdriver.ie.driver", wdpath);
		// new InternetExplorerDriver(service, options); for fix port
		return new InternetExplorerDriver(options);// without fix port
	}
	
	// encounter edge exception: connection refuse due to edge driver version is wrong
	public EdgeDriver initEdgeDriver() {
		String wdpath = path + xmlElement.getElementsByTagName("edgewebdriverpath").item(0).getTextContent();
		// declare the Edge options object to store the IE capabilities
		EdgeOptions options = new EdgeOptions();
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		
		// create ie driver and return the web driver obj
		System.setProperty("webdriver.edge.driver", wdpath);
		return new EdgeDriver(options);
	}
	
	//Init the chrome web driver
	public ChromeDriver initChromeDriver() {
			String wdpath = path + xmlElement.getElementsByTagName("chromewebdriverpath").item(0).getTextContent();
			// create chrome driver and return the web driver obj
			System.setProperty("webdriver.chrome.driver", wdpath);
			return new ChromeDriver();
		}
	

	/**
	 * Wait till the DOM is ready state is complete
	 */
	public void waitUntilPageIsLoaded() {
		log.info("Loading DOM");
		wait.until(webDriver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").toString()
				.equals("complete"));
		log.info("DOM loaded successfully.");
	}

	/**
	 * Handle the institution selection using sikuli (image detection recognition)
	 * @return verify if the institution selection win dialog is handled
	 * @throws InterruptedException 
	 */

//	private void setLoginWaitTm()
//	{
//		try {// read the login wait time
//			loginWaitTime = Integer.parseInt(xmlElement.getElementsByTagName("loginWaitTime").item(0).getTextContent());
//		} 
//		catch (NumberFormatException e) {
//			loginWaitTime = 60000;//set to 1 min
//			log.info("The wait time was not entered with proper numbers, set to default value: " + loginWaitTime + " ms");
//		}
//	}

	/**
	 * Upon test case completion the test credentials will be removed
	 */
	public void removeTestCredentials() {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(path + "\\" + propFileName);
			// set the key field 'UserName=' to empty since username is entered by tester
			config.setProperty("UserName", "");
			// set the key field 'Password=' to empty since password is entered by tester
			config.setProperty("Password", "");
			// write to the properties file
			config.save();
			log.info("Credentials removed from properties file");
		} catch (SecurityException e) {
			log.error("Security manager exists and its checkPropertyAccess method doesn't allow access to the specified system property."
						+ "\nStacktrace:\n" + e.getMessage());
		} catch (NullPointerException e) {
			log.error("The key field in properties is not available\nStacktrace:\n" + e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("The key field in properties is empty!\nStacktrace:\n" + e.getMessage());
		} catch (ConfigurationException e) {
			log.error("Error modifying the property file!\nStacktrace:\n" + e.getMessage());
		} catch (Exception e) {
			log.error("Exception!\nStacktrace:\n" + e.getMessage());
		}
	}
	
	/**
	 * Archive the previous reportNG report
	 */
	public void backupTestReport() {
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy_HHmmss");// suffix to attach to archive report
		String bkupSuffix = nodeName + "_" + dateFormat.format(new Date());
		String testOpPath = path + "\\test-output\\";// generated testNG report path
		File testRpt = new File(testOpPath + "html\\overview.html");// generated test report
		File archiveFolderName = new File(testOpPath + "Archive_TestReports");// test report backup dir
		File reportDir = new File(testOpPath + "html");// report dir to be backup
		File archiveDir = new File(testOpPath + "Archive_TestReports\\" + bkupSuffix);
		try {
			if (!archiveFolderName.exists())// if archive folder does not exist, create the folder
			{
				if (archiveFolderName.mkdir()) {
					log.info("Archive test report folder created.");
				}
			}
			if (testRpt.isFile())// check if there is previous test report
			{
				log.debug("Test report exists.");
				FileUtils.copyDirectory(reportDir, archiveDir);// archive the entire report dir
				log.info("Archive test report to - " + archiveFolderName.toString());
			} else {
				log.info("No existing test report available! No test report is archived.");
			}
		} catch (SecurityException e) {
			log.error("Fail to create test report archive folder!");
		} catch (IOException e) {
			log.error("Fail to rename the test report or fail to archive the test report!");
		}
	}

	/**
	 * Snap screenshot when test fails to test report
	 */
	public void getTCFailScreenshot(String result) {
		if (path.equals("") || path.isEmpty())
		{
			path = System.getProperty("user.dir");
		}
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss aa");
		DateFormat filedf = new SimpleDateFormat("ddMMMyy_HHmmss");
		String timestamp = filedf.format(Calendar.getInstance().getTime());
		File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		String scpath = path + "\\screenshots\\" + result + "_" + timestamp + ".png";
		File screenShotName = new File(scpath);
		try {
			FileUtils.copyFile(src, screenShotName);
			log.info("Screenshot taken and stored in: " + screenShotName);
		} catch (IOException e) {
			String errMsg = "Fail to copy screenshot to path, please check the screenshots folder exists.";
			log.error(errMsg);
			log.debug("Stacktrace:\n" + e.getMessage());
		}
		// add screenshot name to reportNG
		Reporter.log("<a href=" + screenShotName + ">Test failure screenshot taken at " + dateFormat.format(new Date()));
		Reporter.log("<br><img src='" + screenShotName + "' height='400' width='800'/><br>");
	}

	/**
	 * Snap screenshot when test pass to test report
	 */
	public void getTCPassScreenshot(String result) {
		if (path.equals("") || path.isEmpty())
		{
			path = System.getProperty("user.dir");
		}
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss aa");
		DateFormat filedf = new SimpleDateFormat("ddMMMyy_HHmmss");
		String timestamp = filedf.format(Calendar.getInstance().getTime());
		String scpath = path + result + "_" + timestamp + ".png";
		File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		File screenShotName = new File(scpath);
		try {
			FileUtils.copyFile(src, screenShotName);
			log.info("Screenshot taken and stored in: " + screenShotName);
		} catch (IOException e) {
			String errMsg = "Fail to copy screenshot to path, please check the screenshots folder exists.";
			log.error(errMsg);
			log.debug("Stacktrace:\n" + e.getMessage());
		}
		// add screenshot name to reportNG
		Reporter.log("<a href=" + screenShotName + ">Test pass screenshot taken at " + dateFormat.format(new Date()));
		Reporter.log("<br><img src='" + screenShotName + "' height='400' width='800'/><br>");
	}
	
	public String getScreenShotPath(String testCaseName) 
	{
		if (path.equals("") || path.isEmpty())
		{
			path = System.getProperty("user.dir");
		}
		DateFormat filedf = new SimpleDateFormat("ddMMMyy_HHmmss");
		String timestamp = filedf.format(Calendar.getInstance().getTime());
		String scpath = path + testCaseName + "_" + timestamp + ".png";
		File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		File screenShotName = new File(scpath);
		try {
			FileUtils.copyFile(src, screenShotName);
			log.info("Screenshot taken and stored in: " + screenShotName);
		} catch (IOException e) {
			String errMsg = "Fail to copy screenshot to path, please check the screenshots folder exists.";
			log.error(errMsg);
			log.debug("Stacktrace:\n" + e.getMessage());
		}
		return scpath;
	}
}