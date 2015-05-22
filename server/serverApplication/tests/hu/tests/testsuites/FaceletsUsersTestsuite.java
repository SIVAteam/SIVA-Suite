package hu.tests.testsuites;

import hu.tests.facelets.users.CreateEditUserTest;
import hu.tests.facelets.users.EditProfileTest;
import hu.tests.facelets.users.LoginTest;
import hu.tests.facelets.users.RegistrationTest;
import hu.tests.facelets.users.RestrictionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CreateEditUserTest.class, EditProfileTest.class,
        LoginTest.class, RegistrationTest.class, RestrictionTest.class })
public class FaceletsUsersTestsuite {

}
