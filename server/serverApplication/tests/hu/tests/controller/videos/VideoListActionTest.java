package hu.tests.controller.videos;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.videos.VideoListBean;
import hu.backingbeans.videos.VideoListBean.VideoListEntryBean;
import hu.controller.videos.VideoListAction;
import hu.model.EParticipationRestriction;
import hu.model.ESortColumnVideo;
import hu.model.ESortDirection;
import hu.model.EVideoType;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.SessionData;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link VideoListAction}.
 */
public class VideoListActionTest {

    private static Configuration config;
    private static IPersistenceProvider pers;
    private static Integer userId;
    private static Integer tutorId;
    private static Integer adminId;
    private static Integer groupId;
    private static Integer groupIdB;

    private static class CfgMock extends Configuration {
	public CfgMock() {
	    super();
	}

	@Override
	public Integer getInteger(String key) {
	    if (key.equals("max_rows_per_table")) {
		return 20;
	    } else {
		return super.getInteger(key);
	    }
	}
    }

    /**
     * Initialize the testing environment.
     * 
     * @throws Exception
     *             if a failure occurs.
     */
    @BeforeClass
    public static void prepare() throws Exception {
	config = new CfgMock();
	pers = new PgPersistenceProvider(config);

	// Create user.
	User user = createUser("User", "VideoListActionTest",
		"VideoListActionTest.user@mailinator.com", EUserType.Participant);
	userId = user.getId();

	// Create tutor user.
	User tutor = createUser("Tutor", "VideoListActionTest",
		"VideoListActionTest.tutor@mailinator.com", EUserType.Tutor);
	tutorId = tutor.getId();

	// Create admin user.
	User admin = createUser("Admin", "VideoListActionTest",
		"VideoListActionTest.admin@mailinator.com", EUserType.Administrator);
	adminId = admin.getId();

	// Create groups to contain the videos.
	Group group = new Group(null);
	group.setTitle("VideoListActionTest Group");
	group.setVisible(true);
	group = pers.getGroupStore().create(group, tutor);
	groupId = group.getId();

	Group groupB = new Group(null);
	groupB.setTitle("VideoListActionTest GroupB");
	groupB.setVisible(true);
	groupB = pers.getGroupStore().create(groupB, admin);
	groupIdB = groupB.getId();

	// Add an group attendant.
	pers.getGroupStore().addAttendant(group, user);

	// Create start and stop times for videos.

	GregorianCalendar oldDate = new GregorianCalendar(2012, 1, 1);

	GregorianCalendar thirtyHoursBefore = new GregorianCalendar();
	thirtyHoursBefore.add(GregorianCalendar.HOUR_OF_DAY, -30);

	GregorianCalendar twelveHoursBefore = new GregorianCalendar();
	twelveHoursBefore.add(GregorianCalendar.HOUR_OF_DAY, -12);

	GregorianCalendar twelveHoursLater = new GregorianCalendar();
	twelveHoursLater.add(GregorianCalendar.HOUR_OF_DAY, 12);

	// Create videos to fill list.
	for (int i = 0; i < 25; i++) {
	    Group e = (i % 2 == 0) ? group : groupB;
	    createVideo(String.format("AA VideoListActionTest Video %02d", i), e,
		    oldDate.getTime(), null, adminId);
	}

	// Create a video that is only visible for the tutor and admin.
	createVideo("VideoListActionTest Tutor Video", group, null, null, tutorId);

	// Create a video that is only visible to an group attendant.
	createVideo("VideoListActionTest Attendant Video", group, thirtyHoursBefore.getTime(),
		null, EParticipationRestriction.GroupAttendants, tutorId);

	// Create a video that is only visible to an admin.
	createVideo("VideoListActionTest Admin Video", groupB, null, null, adminId);

	// Create a video that was recently started.
	createVideo("VideoListActionTest Started Video", group, twelveHoursBefore.getTime(), null,
		adminId);

	// Create a video that is ending soon.
	createVideo("VideoListActionTest Ending Video", group, thirtyHoursBefore.getTime(),
		twelveHoursLater.getTime(), adminId);

	// Create a video that ended.
	createVideo("VideoListActionTest Ended Video", group, thirtyHoursBefore.getTime(),
		twelveHoursBefore.getTime(), adminId);
    }

    private static User createUser(String firstName, String lastName, String email, EUserType type)
	    throws Exception {
	User user = new User(null);
	user.setBanned(false);
	user.setBirthday(new Date());
	user.setDeletable(true);
	user.setEmail(email);
	user.setFirstName(firstName);
	user.setGender(EGender.Male);
	user.setLastName(lastName);
	user.setPassword("test");
	user.setUserType(type);
	user.setStreet("Mustergasse");
	user.setZip("45056");
	user.setCity("Musterberg");
	user.setPhone("09001456");
	user.setFax("08001456");
	user.setWebsite("www.musterweb.de");
	user.setCountry(ECountry.Germany);
	user = pers.getUserStore().create(user);
	return user;
    }

    private static Video createVideo(String title, Group group, Date start, Date stop, int authorId)
	    throws Exception {
	return createVideo(title, group, start, stop, EParticipationRestriction.Public, authorId);
    }

    private static Video createVideo(String title, Group group, Date start, Date stop,
	    EParticipationRestriction participation, int authorId) throws Exception {
	Video q = new Video(null);
	q.setParticipationRestriction(participation);
	q.setTitle(title);
	q.setAuthorId(authorId);
	q.setDirectory("/xhtml/videos/listVideos");
	q = pers.getVideoStore().create(q, group);

	if (start != null) {
	    q.setStart(start);
	}
	if (stop != null) {
	    q.setStop(stop);
	}

	if (start != null || stop != null) {
	    q = pers.getVideoStore().save(q);
	}
	return q;
    }

    /**
     * Clean up the application environment after testing.
     * 
     * @throws Exception
     *             if a failure occurs.
     */
    @AfterClass
    public static void cleanup() throws Exception {
	cleanDatabase();
	pers.close();
    }

    /**
     * Delete test data from the database.
     * 
     * @throws Exception
     *             if a failure occurs.
     */
    private static void cleanDatabase() throws Exception {

	if (adminId != null) {
	    pers.getUserStore().delete(adminId);
	} else {
	    User admin = pers.getUserStore()
		    .findByEmail("VideoListActionTest.admin@mailinator.com");
	    if (admin != null) {
		pers.getUserStore().delete(admin);
	    }
	}

	if (tutorId != null) {
	    pers.getUserStore().delete(tutorId);
	} else {
	    User tutor = pers.getUserStore()
		    .findByEmail("VideoListActionTest.tutor@mailinator.com");
	    if (tutor != null) {
		pers.getUserStore().delete(tutor);
	    }
	}

	if (userId != null) {
	    pers.getUserStore().delete(userId);
	} else {
	    User user = pers.getUserStore().findByEmail("VideoListActionTest.user@mailinator.com");
	    if (user != null) {
		pers.getUserStore().delete(user);
	    }
	}

	if (groupId != null) {
	    pers.getGroupStore().delete(groupId);
	}

	if (groupIdB != null) {
	    pers.getGroupStore().delete(groupIdB);
	}

	if (groupId == null || groupIdB == null) {
	    List<Group> groups = pers.getGroupStore().getAll(pers.getUserStore().findById(1));
	    for (Group e : groups) {
		if (e.getTitle().contains("VideoListActionTest")) {
		    pers.getGroupStore().delete(e);
		    break;
		}
	    }
	}
    }

    /**
     * List all {@link Video}s as an anonymous user.
     */
    @Test
    public void testAnonAll() {
	List<Video> qs = new ArrayList<Video>();
	int page = 0;
	while (qs.size() < 28) {
	    List<VideoListEntryBean> list = fetchList(null, null, page, ESortColumnVideo.Title,
		    ESortDirection.ASC, null);
	    for (VideoListEntryBean entry : list) {
		if (entry.getVideo().getTitle().contains("VideoListActionTest")) {
		    qs.add(entry.getVideo());
		}
	    }
	    page++;
	}

	for (int i = 0; i < 25; i++) {
	    assertEquals(String.format("AA VideoListActionTest Video %02d", i), qs.get(i)
		    .getTitle());
	}
    }

    /**
     * List all active {@link Video}s as an anonymous user.
     */
    @Test
    public void testAnonActive() {
	List<Video> qs = new ArrayList<Video>();
	int page = 0;
	while (qs.size() < 20) {
	    List<VideoListEntryBean> list = fetchList(null, null, page, ESortColumnVideo.Title,
		    ESortDirection.ASC, null);
	    for (VideoListEntryBean entry : list) {
		if (entry.getVideo().getTitle().contains("VideoListActionTest")) {
		    qs.add(entry.getVideo());
		}
	    }
	    page++;
	}

	for (int i = 0; i < 20; i++) {
	    assertEquals(String.format("AA VideoListActionTest Video %02d", i), qs.get(i)
		    .getTitle());
	}
    }

    /**
     * List all inactive {@link Video}s as an anonymous user.
     */
    @Test
    public void testAnonInactive() {
	List<Video> qs = new ArrayList<Video>();
	int page = 0;
	while (qs.size() < 1) {
	    List<VideoListEntryBean> list = fetchList(null, EVideoType.Inactive, page,
		    ESortColumnVideo.Title, ESortDirection.ASC, null);
	    for (VideoListEntryBean entry : list) {
		if (entry.getVideo().getTitle().contains("VideoListActionTest")) {
		    qs.add(entry.getVideo());
		}
	    }
	    page++;
	}

	assertEquals("VideoListActionTest Ended Video", qs.get(0).getTitle());
    }

    /**
     * List all {@link Video}s of an {@link Group} as an anonymous user.
     */
    @Test
    public void testAnonGroup() {
	List<VideoListEntryBean> list = fetchList(null, null, 0, ESortColumnVideo.Title,
		ESortDirection.ASC, groupId);
	assertEquals(16, list.size());

	List<Video> qs = new ArrayList<Video>();
	for (VideoListEntryBean entry : list) {
	    qs.add(entry.getVideo());
	}

	for (int i = 0; i < 12; i++) {
	    assertEquals(String.format("AA VideoListActionTest Video %02d", i * 2), qs.get(i)
		    .getTitle());
	}
    }

    /**
     * List all {@link Video}s as a registered user.
     */
    @Test
    public void testUserAll() {
	List<Video> qs = new ArrayList<Video>();
	int page = 0;
	while (qs.size() < 29) {
	    List<VideoListEntryBean> list = fetchList(userId, null, page, ESortColumnVideo.Title,
		    ESortDirection.ASC, null);
	    for (VideoListEntryBean entry : list) {
		if (entry.getVideo().getTitle().contains("VideoListActionTest")) {
		    qs.add(entry.getVideo());
		}
	    }
	    page++;
	}

	for (int i = 0; i < 25; i++) {
	    assertEquals(String.format("AA VideoListActionTest Video %02d", i), qs.get(i)
		    .getTitle());
	}

	boolean groupQuestFound = false;
	for (Video q : qs) {
	    if (q.getTitle().equals("VideoListActionTest Attendant Video")) {
		groupQuestFound = true;
		break;
	    }
	}
	assertTrue(groupQuestFound);
    }

    /**
     * List all {@link Video}s as a registered tutor.
     */
    @Test
    public void testTutorAll() {
	List<Video> qs = new ArrayList<Video>();
	int page = 0;
	while (qs.size() < 30) {
	    List<VideoListEntryBean> list = fetchList(tutorId, null, page, ESortColumnVideo.Title,
		    ESortDirection.ASC, null);
	    for (VideoListEntryBean entry : list) {
		if (entry.getVideo().getTitle().contains("VideoListActionTest")) {
		    qs.add(entry.getVideo());
		}
	    }
	    page++;
	}

	for (int i = 0; i < 25; i++) {
	    assertEquals(String.format("AA VideoListActionTest Video %02d", i), qs.get(i)
		    .getTitle());
	}

	boolean groupQuestFound = false;
	boolean tutorQuestFound = false;
	for (Video q : qs) {
	    if (q.getTitle().equals("VideoListActionTest Attendant Video")) {
		groupQuestFound = true;
	    }
	    if (q.getTitle().equals("VideoListActionTest Tutor Video")) {
		tutorQuestFound = true;
	    }
	}
	assertTrue(groupQuestFound);
	assertTrue(tutorQuestFound);
    }

    /**
     * List all {@link Video}s as a registered administrator.
     */
    @Test
    public void testAdminAll() {
	List<Video> qs = new ArrayList<Video>();
	int page =0;
	while(qs.size()<31) {
	    List<VideoListEntryBean> list = fetchList(adminId, null, page, ESortColumnVideo.Title,
		    ESortDirection.ASC, null);
	    for (VideoListEntryBean entry : list) {
		if (entry.getVideo().getTitle().contains("VideoListActionTest")) {
		qs.add(entry.getVideo());
		}
	    }
	    page++;
	}

	for (int i = 0; i < 25; i++) {
	    assertEquals(String.format("AA VideoListActionTest Video %02d", i), qs.get(i)
		    .getTitle());
	}

	boolean groupQuestFound = false;
	boolean tutorQuestFound = false;
	boolean adminQuestFound = false;
	boolean endingQuestFound = false;
	boolean endedQuestFound = false;
	boolean newQuestFound = false;
	for (Video q : qs) {
	    if (q.getTitle().equals("VideoListActionTest Attendant Video")) {
		groupQuestFound = true;
	    }
	    if (q.getTitle().equals("VideoListActionTest Tutor Video")) {
		tutorQuestFound = true;
	    }
	    if (q.getTitle().equals("VideoListActionTest Admin Video")) {
		adminQuestFound = true;
	    }
	    if (q.getTitle().equals("VideoListActionTest Ending Video")) {
		endingQuestFound = true;
	    }
	    if (q.getTitle().equals("VideoListActionTest Ended Video")) {
		endedQuestFound = true;
	    }
	    if (q.getTitle().equals("VideoListActionTest Started Video")) {
		newQuestFound = true;
	    }
	}
	assertTrue(groupQuestFound);
	assertTrue(tutorQuestFound);
	assertTrue(adminQuestFound);
	assertTrue(endingQuestFound);
	assertTrue(endedQuestFound);
	assertTrue(newQuestFound);
    }

    private static List<VideoListEntryBean> fetchList(Integer currentUser, EVideoType type,
	    int page, ESortColumnVideo sortColumn, ESortDirection sortDirection, Integer group) {
	VideoListBean qlb = new VideoListBean();
	qlb.setPage(page);
	qlb.setSortColumn(sortColumn);
	qlb.setSortDirection(sortDirection);
	qlb.setType(type);

	GroupBean eb = new GroupBean();
	eb.setId(group);

	FacesContext fctxMock = mock(FacesContext.class);
	UIViewRoot uvr = mock(UIViewRoot.class);
	when(fctxMock.getViewRoot()).thenReturn(uvr);
	when(uvr.getLocale()).thenReturn(Locale.ENGLISH);

	SessionData session = new SessionData();
	session.setUserId(currentUser);

	VideoListAction ctrl = new VideoListAction();
	ctrl.setConfiguration(config);
	ctrl.setGroupBean(eb);
	ctrl.setMock(fctxMock);
	ctrl.setPersistenceProvider(pers);
	ctrl.setVideoListBean(qlb);
	ctrl.setSessionData(session);

	ctrl.listVideosForUser();

	return qlb.getList();
    }
}