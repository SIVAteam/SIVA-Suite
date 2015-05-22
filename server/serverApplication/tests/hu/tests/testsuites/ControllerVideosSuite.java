package hu.tests.testsuites;

import hu.tests.controller.videos.VideoActionTest;
import hu.tests.controller.videos.VideoListActionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    VideoActionTest.class,
    VideoListActionTest.class
})
public class ControllerVideosSuite {

}
