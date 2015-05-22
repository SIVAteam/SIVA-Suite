package hu.tests.testsuites;

import hu.tests.facelets.api.InstructionServletTest;
import hu.tests.facelets.api.TokenServletTest;
import hu.tests.facelets.api.VideoServletTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ InstructionServletTest.class, TokenServletTest.class, VideoServletTest.class })
public class FaceletsApiTestsuite {

}
