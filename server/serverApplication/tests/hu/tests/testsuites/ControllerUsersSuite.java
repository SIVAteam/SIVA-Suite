package hu.tests.testsuites;

import hu.tests.controller.users.UserActionTest;
import hu.tests.controller.users.UserListActionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UserActionTest.class, UserListActionTest.class })
public class ControllerUsersSuite {

}
