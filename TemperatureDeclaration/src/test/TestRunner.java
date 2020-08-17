/*
 * Last edit by: Brandon Hu
 * Last edited date: 04-Feb-2020
 * version: v1.0.0.1
 * updates: This main class required for runnable jar
 */
package test;

import org.testng.TestNG;

/*
 * Purpose of this method is to execute the BAT as Java application
 */
public class TestRunner {
	private static TestNG testNg;

	public static void main(String[] args) {
		testNg = new TestNG();
		testNg.setTestClasses(new Class[] {DeclareTempReadingTest.class});		
		testNg.run();
	}
}