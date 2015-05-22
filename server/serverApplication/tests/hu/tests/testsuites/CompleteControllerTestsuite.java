package hu.tests.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ControllerCommonSuite.class, ControllerGroupsSuite.class,
        ControllerUsersSuite.class, ControllerVideosSuite.class, })
public class CompleteControllerTestsuite {
}