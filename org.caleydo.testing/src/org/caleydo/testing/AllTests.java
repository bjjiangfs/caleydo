package org.caleydo.testing;

import org.caleydo.testing.collection.VirtualArrayTester;
import org.caleydo.testing.unit.command.data.filter.NominalStringCCollectionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.caleydo.testing.command.data.filter");
		// $JUnit-BEGIN$
		// suite.addTestSuite(CmdDataFiterMinMaxTest.class);
		// suite.addTestSuite(CmdDataFilterMathTest.class);
//		suite.addTestSuite(NominalStringCCollectionTest.class);
		// $JUnit-END$
		suite.addTestSuite(VirtualArrayTester.class);
		return suite;
	}

}