package hu.tests.testsuites;

import hu.tests.facelets.NavigationTest;
import hu.tests.facelets.UrlManipulationTest;
import hu.tests.facelets.api.InstructionServletTest;
import hu.tests.facelets.api.TokenServletTest;
import hu.tests.facelets.api.VideoServletTest;
import hu.tests.facelets.common.ContactTest;
import hu.tests.facelets.groups.SignUpForGroupTest;
import hu.tests.facelets.testscenarios.additionalTests;
import hu.tests.facelets.users.CreateEditUserTest;
import hu.tests.facelets.users.EditProfileTest;
import hu.tests.facelets.users.LoginTest;
import hu.tests.facelets.users.RegistrationTest;
import hu.tests.facelets.users.RestrictionTest;
import hu.tests.facelets.videos.VideoTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CreateEditUserTest.class, EditProfileTest.class, LoginTest.class,
	RegistrationTest.class, VideoTest.class, RestrictionTest.class, NavigationTest.class,
	additionalTests.class, ContactTest.class, SignUpForGroupTest.class,
	UrlManipulationTest.class, InstructionServletTest.class, TokenServletTest.class, VideoServletTest.class })
public class CompleteFaceletTestSuite {

}
