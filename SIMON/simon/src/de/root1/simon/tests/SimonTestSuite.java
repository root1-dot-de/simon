package de.root1.simon.tests;

import java.util.Enumeration;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class SimonTestSuite {
	
	public static Test suite() {

		  return new TestSuite(EmptyTest.class);

		}
	
	public static void main(String[] args) {
		
		Test test = suite();
		int testnum = test.countTestCases();
		System.out.println("Running "+testnum+" tests ...");
		TestResult result = new TestResult();
		test.run(result);
		
		System.out.println(); // empty line
		System.out.println("Finished with testing. Results:");
				
		int errorCount = result.errorCount();
		int failureCount = result.failureCount();
		int testCasesCount = test.countTestCases();
			
		System.out.println(" # Testcases..: "+testCasesCount);
		System.out.println(" # Errors.....: "+errorCount);
		System.out.println(" # Failures...: "+failureCount);
		System.out.println(); // empty line
		
		if (failureCount>0) {
			Enumeration<TestFailure> failures = result.failures();
			System.out.println("Failure details:");
			while (failures.hasMoreElements()){
				TestFailure testFailure = failures.nextElement();
				System.out.println(" -> "+testFailure);
			}
		}
		System.out.println(); // empty line
		if (errorCount>0) {
			Enumeration<TestFailure> errors = result.errors();
			System.out.println("Failure details:");
			while (errors.hasMoreElements()){
				TestFailure testFailure = errors.nextElement();
				System.out.println(" -> "+testFailure);
			}
		}

	}
}

