package hu.tests.testsuites;

import hu.tests.controller.common.AuthenticationActionTest;
import hu.tests.controller.common.ContactActionTest;
import hu.tests.controller.common.InstallationActionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AuthenticationActionTest.class,
	ContactActionTest.class, 
	InstallationActionTest.class })
public class ControllerCommonSuite {

}
