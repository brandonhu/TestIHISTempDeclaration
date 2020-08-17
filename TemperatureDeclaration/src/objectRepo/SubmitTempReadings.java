package objectRepo;

import java.util.Calendar;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Element;

/**
 * This class stores all the locators and web elements for a login page which follows the page object model design pattern
 */
public class SubmitTempReadings {
	private WebDriver driver = null;
	private By emailLoc = null;
	private By locationInputLoc = null;
	private By locationLoc = null;
	private By locationTextLoc = null;
	private By temperatureLoc = null;
	private By feelWellLoc = null;
	private By sessionLoc = null;
	private By submitBtnLoc = null;
	private By submitFormLinkLoc = null;
	private int waitLocationLoadTime = 0;
	private boolean morningYes = false;
	
	/**
	 * @param driver - webdriver
	 * @param xmlElement - provides the xpath of web elements on login page 
	 */
	public SubmitTempReadings(WebDriver driver, Element xmlElement) {
		this.driver = driver;
		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
		
		emailLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc1").item(0).getTextContent());
		locationInputLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc2").item(0).getTextContent());
		locationLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc3").item(0).getTextContent());
		locationTextLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc4").item(0).getTextContent());
		temperatureLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc5").item(0).getTextContent());
		feelWellLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc6").item(0).getTextContent());
		if (timeOfDay >= 0 && timeOfDay < 12)
		{//AM
			sessionLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc7a").item(0).getTextContent());
			morningYes = true;
		}
		else
		{//PM
			sessionLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc7b").item(0).getTextContent());
		}
		submitBtnLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc8").item(0).getTextContent());
		submitFormLinkLoc = By.xpath(xmlElement.getElementsByTagName("tc1_loc9").item(0).getTextContent());
		try
		{
			waitLocationLoadTime = Integer.parseInt(xmlElement.getElementsByTagName("waitLocationLoadTime").item(0).getTextContent());
		}
		catch (NumberFormatException e)
		{
			waitLocationLoadTime = 3000;
		}
	}
	
	/**
	 * @return email field in the page
	 */
	public WebElement emailField()
	{
		return driver.findElement(emailLoc);
	}
	
	/**
	 * @return location input as a drop down on the page
	 */
	public WebElement locationInput()
	{
		return driver.findElement(locationInputLoc);
	}
	
	/**
	 * @return location dropdown list item in the page
	 */
	public WebElement locationItem()
	{
		return driver.findElement(locationLoc);
	}
	
	/**
	 * @return selected location displayy in dropdown field on the page
	 */
	public WebElement locationText()
	{
		return driver.findElement(locationTextLoc);
	}
	
	/**
	 * @return temperature field on the page
	 */
	public WebElement tempField()
	{
		return driver.findElement(temperatureLoc);
	}
	
	/**
	 * @return feel well or not radio button on the page
	 */
	public WebElement feelWellRadBtn()
	{
		return driver.findElement(feelWellLoc);
	}
	
	/**
	 * @return session radio button in the page
	 */
	public WebElement sessionRadBtn()
	{
		return driver.findElement(sessionLoc);
	}
	
	/**
	 * @return submit button on this page
	 */
	public WebElement submitBtn()
	{
		return driver.findElement(submitBtnLoc);
	}
	
	/**
	 * @return submit form link on next page
	 */
	public WebElement submitFormLink()
	{
		return driver.findElement(submitFormLinkLoc);
	}
	
	/**
	 * @return locator for email field
	 */
	public By emailLocator()
	{
		return emailLoc;
	}
	
	/**
	 * @return locator for location input as drop down list on the page
	 */
	public By locationInputLocator()
	{
		return locationInputLoc;
	}
	
	/**
	 * @return locator for location item in the dropdown list on the page
	 */
	public By locationLocator()
	{
		return locationLoc;
	}
	
	/**
	 * @return locator for location text displayed in the dropdown field on the page
	 */
	public By locationTxtLocator()
	{
		return locationTextLoc;
	}
	
	/**
	 * @return locator for temperature field on the page
	 */
	public By tempFieldLocator()
	{
		return temperatureLoc;
	}
	
	/**
	 * @return locator for feel well or not radio button on the page
	 */
	public By feelWellRadBtnLocator()
	{
		return feelWellLoc;
	}
	
	/**
	 * @return locator for session radio button in the page
	 */
	public By sessionRadBtnLocator()
	{
		return locationLoc;
	}
	
	/**
	 * @return locator for submit button field
	 */
	public By submitBtnLocator()
	{
		return submitBtnLoc;
	}
	
	/**
	 * @return locator for submit form link
	 */
	public By submitFormLinkLocator()
	{
		return submitFormLinkLoc;
	}
	
	/**
	 * @return amount of wait time for the location to be loaded in the dropdown box
	 */
	public int getLocationWaitTm()
	{
		return waitLocationLoadTime;
	}
	
	public boolean getSessionType()
	{
		return morningYes;
	}
}