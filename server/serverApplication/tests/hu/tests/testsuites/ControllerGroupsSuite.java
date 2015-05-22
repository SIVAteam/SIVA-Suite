package hu.tests.testsuites;

import hu.tests.controller.groups.GroupActionTest;
import hu.tests.controller.groups.GroupAttendanceActionTest;
import hu.tests.controller.groups.GroupListActionTest;
import hu.tests.controller.groups.GroupOwnershipActionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ GroupActionTest.class, GroupAttendanceActionTest.class,
        GroupListActionTest.class, GroupOwnershipActionTest.class })
public class ControllerGroupsSuite {

}
