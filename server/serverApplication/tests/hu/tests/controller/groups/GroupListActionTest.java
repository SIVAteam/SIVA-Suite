package hu.tests.controller.groups;

import static org.junit.Assert.*;
import hu.backingbeans.groups.GroupListBean;
import hu.backingbeans.groups.GroupListBean.GroupListEntryBean;
import hu.controller.groups.GroupListAction;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.SessionData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link GroupListAction#listGroups()}.
 */
public class GroupListActionTest {
    private static final String TEST_IN_SHA256 = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
    private static Configuration config;
    private static PgPersistenceProvider pers;

    private static Integer attendantId;
    private static Integer tutorId;
    private static Integer adminId;
    private static Integer dummyId;
    private static List<Integer> groupIds;

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
     * Prepare testing: Check that the database is clean and insert test data.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void prepare() throws Exception {
	config = new CfgMock();
	pers = new PgPersistenceProvider(config);

	List<User> existingUsers = new LinkedList<User>();
	existingUsers.add(pers.getUserStore().findByEmail("ev.attendant@mailinator.com"));
	existingUsers.add(pers.getUserStore().findByEmail("ev.tutor@mailinator.com"));
	existingUsers.add(pers.getUserStore().findByEmail("ev.dummy@mailinator.com"));
	existingUsers.add(pers.getUserStore().findByEmail("ev.admin@mailinator.com"));
	
	User attendant = new User(null);
	attendant.setBanned(false);
	attendant.setCountry(ECountry.Austria);
	attendant.setBirthday(new Date());
	attendant.setDeletable(true);
	attendant.setEmail("ev.attendant@mailinator.com");
	attendant.setFirstName("EvAttendant");
	attendant.setLastName("EvAttendant");
	attendant.setPasswordHash(TEST_IN_SHA256);
	attendant.setTitle("Tst");
	attendant.setUserType(EUserType.Participant);
	attendant.setGender(EGender.Female);
	attendant.setVisible(true);
	attendant.setStreet("Wandstraße");
	attendant.setCity("Einoede");
	attendant.setZip("013");
	attendant.setFax("0456234");
	attendant.setPhone("0456234");
	attendant.setWebsite("www.test.at");
	attendant.setVisible(true);

	User tutor = new User(null);
	tutor.setBanned(false);
	tutor.setCountry(ECountry.Austria);
	tutor.setBirthday(new Date());
	tutor.setDeletable(true);
	tutor.setEmail("ev.tutor@mailinator.com");
	tutor.setFirstName("EvTutor");
	tutor.setLastName("EvTutor");
	tutor.setPasswordHash(TEST_IN_SHA256);
	tutor.setTitle("Tst");
	tutor.setUserType(EUserType.Tutor);
	tutor.setGender(EGender.Male);
	tutor.setStreet("Ringstraße");
	tutor.setCity("Einoede");
	tutor.setZip("013");
	tutor.setFax("045623334");
	tutor.setPhone("045623335");
	tutor.setWebsite("www.test2.at");
	tutor.setVisible(false);

	User admin = new User(null);
	admin.setBanned(false);
	admin.setCountry(ECountry.Germany);
	admin.setBirthday(new Date());
	admin.setDeletable(true);
	admin.setEmail("ev.admin@mailinator.com");
	admin.setFirstName("EvAdmin");
	admin.setLastName("EvAdmin");
	admin.setPasswordHash(TEST_IN_SHA256);
	admin.setTitle("Tst");
	admin.setUserType(EUserType.Administrator);
	admin.setGender(EGender.Male);
	admin.setStreet("Wandgasse");
	admin.setCity("Musterstadt");
	admin.setZip("01234");
	admin.setFax("045623444");
	admin.setPhone("045623445");
	admin.setWebsite("www.test3.de");
	admin.setVisible(false);

	User dummy = new User(null);
	dummy.setBanned(false);
	dummy.setCountry(ECountry.Switzerland);
	dummy.setBirthday(new Date());
	dummy.setDeletable(true);
	dummy.setEmail("ev.dummy@mailinator.com");
	dummy.setFirstName("EvDummy");
	dummy.setLastName("EvDummy");
	dummy.setPasswordHash(TEST_IN_SHA256);
	dummy.setTitle("Tst");
	dummy.setUserType(EUserType.Tutor);
	dummy.setGender(EGender.Female);
	dummy.setStreet("Testweg");
	dummy.setCity("TestCity");
	dummy.setZip("12345");
	dummy.setFax("045623478");
	dummy.setPhone("045623477");
	dummy.setWebsite("www.dummy.de");
	dummy.setVisible(true);

	dummy = pers.getUserStore().create(dummy);
	admin = pers.getUserStore().create(admin);
	tutor = pers.getUserStore().create(tutor);
	attendant = pers.getUserStore().create(attendant);

	adminId = admin.getId();
	tutorId = tutor.getId();
	attendantId = attendant.getId();
	dummyId = dummy.getId();

	String[] titles = { "AAAinvisible", "AABinvisible", "AACGroup", "AADGroup", "Group01",
		"Group02", "Group03", "Group04", "Group05", "Group06", "Group07", "Group08",
		"Group09", "Group10", "Group11", "Group12", "Group13", "Group14", "Group15",
		"Group16", "Group17", "Group18", "Group19", "Group20", "Group21", "Group22",
		"Group23", "Group24", "Group25", "Group26", "Group27", "Group28", "Group29",
		"Group30", "ZZWGroup", "ZZXGroup", "ZZYinvisible", "ZZZinvisible" };
	groupIds = new ArrayList<Integer>(titles.length);

	Set<String> adminOwns = new HashSet<String>();
	adminOwns.add("AABinvisible");
	adminOwns.add("AADGroup");
	adminOwns.add("ZZWGroup");

	Set<String> adminAttends = new HashSet<String>();
	adminAttends.add("AAAinvisible");
	adminAttends.add("AACGroup");

	Set<String> tutorOwns = new HashSet<String>();
	tutorOwns.add("AAAinvisible");
	tutorOwns.add("AACGroup");
	tutorOwns.add("ZZXGroup");

	Set<String> tutorAttends = new HashSet<String>();
	tutorAttends.add("AABinvisible");
	tutorAttends.add("AADGroup");

	Set<String> attendantAttends = new HashSet<String>();
	attendantAttends.add("AAAinvisible");
	attendantAttends.add("AACGroup");
	attendantAttends.add("ZZWGroup");
	attendantAttends.add("ZZYinvisible");

	for (String title : titles) {
	    Group g = new Group(null);
	    g.setTitle(title);
	    g.setVisible(!title.contains("invisible"));
	    g = pers.getGroupStore().create(g, dummy);
	    groupIds.add(g.getId());

	    if (adminOwns.contains(title)) {
		pers.getGroupStore().addOwner(g, admin);
	    }
	    if (tutorOwns.contains(title)) {
		pers.getGroupStore().addOwner(g, tutor);
	    }
	    if (adminAttends.contains(title)) {
		pers.getGroupStore().addAttendant(g, admin);
	    }
	    if (tutorAttends.contains(title)) {
		pers.getGroupStore().addAttendant(g, tutor);
	    }
	    if (attendantAttends.contains(title)) {
		pers.getGroupStore().addAttendant(g, attendant);
	    }
	}
    }

    /**
     * Delete test data from the database.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void destroy() throws Exception {
	pers.getUserStore().delete(dummyId);
	pers.getUserStore().delete(adminId);
	pers.getUserStore().delete(tutorId);
	pers.getUserStore().delete(attendantId);
	for (int id : groupIds) {
	    pers.getGroupStore().delete(id);
	}
	pers.close();
    }

    /**
     * Test the list of visible groups for an anonymous user.
     */
    @Test
    public void testAnonVisible() {
	GroupListBean glb;

	// Test first page of visible groups tab in ascending order.
	glb = listGroups(null, 1, ESortDirection.ASC, "visible");

	boolean found = false;
	int page1 = 1;
	int page2 = 1;
	int index1 = 0;
	int index2 = 0;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(null, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(null, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}

	assertTrue(index1 < index2 || page1 < page2);

	glb = listGroups(null, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "AACGroup", false, false, false, false);
	glb = listGroups(null, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "AADGroup", false, false, false, false);
	for (int i = 0; i < 20; i++) {
	    matchRow(glb.getList().get(i), null, false, false, false, false);
	}

	// Test last page of visible groups tab in ascending order.

	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(null, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(null, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	assertTrue(index1 < index2 || page1 < page2);

	glb = listGroups(null, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "ZZXGroup", false, false, false, false);
	glb = listGroups(null, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "ZZWGroup", false, false, false, false);
	for (int i = 0; i < 14; i++) {
	    matchRow(glb.getList().get(i), null, false, false, false, false);
	}

	// Test first page of visible groups tab in descending order.
	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(null, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(null, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	assertTrue(index1 > index2 || page1 > page2);

	glb = listGroups(null, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "ZZXGroup", false, false, false, false);
	glb = listGroups(null, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "ZZWGroup", false, false, false, false);
	for (int i = 0; i < 20; i++) {
	    matchRow(glb.getList().get(i), null, false, false, false, false);
	}

	// Test last page of visible groups tab in descending order.
	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(null, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(null, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}

	assertTrue(index1 > index2 || page1 > page2);
	glb = listGroups(null, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "AACGroup", false, false, false, false);
	glb = listGroups(null, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "AADGroup", false, false, false, false);
	for (int i = 0; i < 14; i++) {
	    matchRow(glb.getList().get(i), null, false, false, false, false);
	}
    }

    /**
     * Test the list of visible groups for an authenticated user.
     */
    @Test
    public void testAttendantVisible() {
	GroupListBean glb;

	// Test first page of visible groups tab in ascending order.
	glb = listGroups(attendantId, 1, ESortDirection.ASC, "visible");
	boolean found = false;
	int page1 = 1;
	int page2 = 1;
	int index1 = 0;
	int index2 = 0;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(attendantId, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(attendantId, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}

	assertTrue(index1 < index2 || page1 < page2);
	 glb = listGroups(attendantId, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "AACGroup", true, false, false, false);
	 glb = listGroups(attendantId, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "AADGroup", false, true, false, false);
	for (int i = index2 + 1; i < glb.getList().size(); i++) {
	    matchRow(glb.getList().get(i), null, false, true, false, false);
	}

	// Test last page of visible groups tab in ascending order.
	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(attendantId, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(attendantId, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	assertTrue(index1 < index2 || page1 < page2);
	 glb = listGroups(attendantId, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "ZZXGroup", false, true, false, false);
	 glb = listGroups(attendantId, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "ZZWGroup", true, false, false, false);
	for (int i = 0; i < index1; i++) {
	    matchRow(glb.getList().get(i), null, false, true, false, false);
	}

	// Test first page of visible groups tab in descending order.
	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(attendantId, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(attendantId, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	assertTrue(index1 > index2 || page1 > page2);
	glb = listGroups(attendantId, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "ZZXGroup", false, true, false, false);
	glb = listGroups(attendantId, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "ZZWGroup", true, false, false, false);

	// Test second page of visible groups tab in descending order.
	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(attendantId, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(attendantId, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}

	assertTrue(index1 > index2 || page1 > page2);
	glb = listGroups(attendantId, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "AACGroup", true, false, false, false);
	glb = listGroups(attendantId, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "AADGroup", false, true, false, false);
    }

    /**
     * Test the list of visible groups for a tutor.
     */
    @Test
    public void testTutorVisible() {
	GroupListBean glb;

	// Test first page of visible groups tab in ascending order.
	glb = listGroups(tutorId, 1, ESortDirection.ASC, "visible");
	boolean found = false;
	int page1 = 1;
	int page2 = 1;
	int page0 = 1;
	int index1 = 0;
	int index2 = 0;
	int index0 = 0;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(tutorId, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(tutorId, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}

	found = false;
	while (!found) {
	    index0 = 0;
	    glb = listGroups(tutorId, page0, ESortDirection.ASC, "visible");
	    while (!found && index0 < glb.getList().size()) {
		if (glb.getList().get(index0).getGroup().getTitle().equals("AAAinvisible")) {
		    found = true;
		} else {
		    index0++;
		}
	    }
	    page0++;
	}

	assertTrue(index0 < index1 || page0 < page1);
	assertTrue(index1 < index2 || page1 < page2);
	glb = listGroups(tutorId, page0-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index0), "AAAinvisible", false, false, true, true);
	glb = listGroups(tutorId, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "AACGroup", false, false, true, true);
	glb = listGroups(tutorId, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "AADGroup", true, false, false, false);
	for (int i = index2 + 1; i < glb.getList().size(); i++) {
	    matchRow(glb.getList().get(i), null, false, true, false, false);
	}

	// Test last page of visible groups tab in ascending order.
	found = false;
	page1 = 1;
	page2 = 1;
	page0 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(tutorId, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(tutorId, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	
	assertTrue(index1 < index2 || page1 < page2);
	glb = listGroups(tutorId, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "ZZXGroup", false, false, true, true);
	glb = listGroups(tutorId, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "ZZWGroup", false, true, false, false);
	for (int i = 0; i < index1; i++) {
	    matchRow(glb.getList().get(i), null, false, true, false, false);
	}

	// Test first page of visible groups tab in descending order.
	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(tutorId, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(tutorId, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	
	assertTrue(index1 > index2 || page1 > page2);
	glb = listGroups(tutorId, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "ZZXGroup", false, false, true, true);
	glb = listGroups(tutorId, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "ZZWGroup", false, true, false, false);

	// Test last page of visible groups tab in descending order.
	found = false;
	page1 = 1;
	page2 = 1;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(tutorId, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(tutorId, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	found = false;
	while (!found) {
	    index0 = 0;
	    glb = listGroups(tutorId, page0, ESortDirection.DESC, "visible");
	    while (!found && index0 < glb.getList().size()) {
		if (glb.getList().get(index0).getGroup().getTitle().equals("AAAinvisible")) {
		    found = true;
		} else {
		    index0++;
		}
	    }
	    page0++;
	}

	assertTrue(index0 > index1 || page0 > page1);
	assertTrue(index1 > index2 || page1 > page2);
	glb = listGroups(tutorId, page0-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index0), "AAAinvisible", false, false, true, true);
	glb = listGroups(tutorId, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "AACGroup", false, false, true, true);
	glb = listGroups(tutorId, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "AADGroup", true, false, false, false);
    }

    /**
     * Test the list of attended groups for an authenticated user.
     */
    @Test
    public void testAttendantAttended() {
	String[] attended = { "AAAinvisible", "AACGroup", "ZZWGroup", "ZZYinvisible" };
	matchAttended(attendantId, attended, false);
    }

    /**
     * Test the list of attended groups for a tutor.
     */
    @Test
    public void testTutorAttended() {
	String[] attended = { "AABinvisible", "AADGroup" };
	matchAttended(tutorId, attended, false);
    }

    /**
     * Test the list of attended groups for an administrator.
     */
    @Test
    public void testAdminAttended() {
	String[] attended = { "AAAinvisible", "AACGroup" };
	matchAttended(adminId, attended, true);
    }

    /**
     * Test the list of owned groups for a tutor.
     */
    @Test
    public void testTutorOwned() {
	String[] attended = { "AAAinvisible", "AACGroup", "ZZXGroup" };
	matchOwned(tutorId, attended);
    }

    /**
     * Test the list of owned groups for an administrator.
     */
    @Test
    public void testAdminOwned() {
	String[] attended = { "AABinvisible", "AADGroup", "ZZWGroup" };
	matchOwned(adminId, attended);
    }

    /**
     * Test the list of all groups for an administrator.
     */
    @Test
    public void testAdminAll() {
	GroupListBean glb;

	// Test first page of all group tab in ascending order.
	glb = listGroups(adminId, 1, ESortDirection.ASC, "all");
	boolean found = false;
	int page1 = 1;
	int page2 = 1;
	int page3 = 1;
	int page0 = 1;
	int index3 = 0;
	int index1 = 0;
	int index2 = 0;
	int index0 = 0;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(adminId, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}

	found = false;
	while (!found) {
	    index3 = 0;
	    glb = listGroups(adminId, page3, ESortDirection.ASC, "visible");
	    while (!found && index3 < glb.getList().size()) {
		if (glb.getList().get(index3).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index3++;
		}
	    }
	    page3++;
	}

	found = false;
	while (!found) {
	    index0 = 0;
	    glb = listGroups(adminId, page0, ESortDirection.ASC, "visible");
	    while (!found && index0 < glb.getList().size()) {
		if (glb.getList().get(index0).getGroup().getTitle().equals("AAAinvisible")) {
		    found = true;
		} else {
		    index0++;
		}
	    }
	    page0++;
	}
	
	found = false;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(adminId, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AABinvisible")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	assertTrue(index0 < index1 || page0 < page1);
	assertTrue(index1 < index2 || page1 < page2);
	assertTrue(index2 < index3 || page2 < page3);
	glb = listGroups(adminId, page0-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index0), "AAAinvisible", true, false, false, true);
	glb = listGroups(adminId, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "AABinvisible", false, false, true, true);
	glb = listGroups(adminId, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "AACGroup", true, false, false, true);
	glb = listGroups(adminId, page3-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index3), "AADGroup", false, false, true, true);
	for (int i = index3+1; i < glb.getList().size(); i++) {
	    matchRow(glb.getList().get(i), null, false, true, false, true);
	}

	// Test last page of all groups tab in ascending order.
	page1 = 1;
	page2 = 1;
	page0 = 1;
	page3 = 1;
	found=false;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(adminId, page1, ESortDirection.ASC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}
	
	found = false;
	while (!found) {
	    index0 = 0;
	    glb = listGroups(adminId, page0, ESortDirection.ASC, "visible");
	    while (!found && index0 < glb.getList().size()) {
		if (glb.getList().get(index0).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index0++;
		}
	    }
	    page0++;
	}

	found = false;
	while (!found) {
	    index3 = 0;
	    glb = listGroups(adminId, page3, ESortDirection.ASC, "visible");
	    while (!found && index3 < glb.getList().size()) {
		if (glb.getList().get(index3).getGroup().getTitle().equals("ZZZinvisible")) {
		    found = true;
		} else {
		    index3++;
		}
	    }
	    page3++;
	}
	
	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(adminId, page2, ESortDirection.ASC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZYinvisible")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	
	assertTrue(index0 < index1 || page0 < page1);
	assertTrue(index1 < index2 || page1 < page2);
	assertTrue(index2 < index3 || page2 < page3);
	glb = listGroups(adminId, page3-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index3), "ZZZinvisible", false, true, false, true);
	glb = listGroups(adminId, page2-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index2), "ZZYinvisible", false, true, false, true);
	glb = listGroups(adminId, page1-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index1), "ZZXGroup", false, true, false, true);
	glb = listGroups(adminId, page0-1, ESortDirection.ASC, "visible");
	matchRow(glb.getList().get(index0), "ZZWGroup", false, false, true, true);
	for (int i = 0; i < 10; i++) {
	    matchRow(glb.getList().get(i), null, false, true, false, true);
	}

	// Test first page of all groups tab in descending order.
	page1 = 1;
	page2 = 1;
	page0 = 1;
	page3 = 1;
	found=false;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(adminId, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("ZZXGroup")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}
	
	found = false;
	while (!found) {
	    index0 = 0;
	    glb = listGroups(adminId, page0, ESortDirection.DESC, "visible");
	    while (!found && index0 < glb.getList().size()) {
		if (glb.getList().get(index0).getGroup().getTitle().equals("ZZWGroup")) {
		    found = true;
		} else {
		    index0++;
		}
	    }
	    page0++;
	}

	found = false;
	while (!found) {
	    index3 = 0;
	    glb = listGroups(adminId, page3, ESortDirection.DESC, "visible");
	    while (!found && index3 < glb.getList().size()) {
		if (glb.getList().get(index3).getGroup().getTitle().equals("ZZZinvisible")) {
		    found = true;
		} else {
		    index3++;
		}
	    }
	    page3++;
	}
	
	found = false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(adminId, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("ZZYinvisible")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}
	
	assertTrue(index0 > index1 || page0 > page1);
	assertTrue(index1 > index2 || page1 > page2);
	assertTrue(index2 > index3 || page2 > page3);

	glb = listGroups(adminId, page3-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index3), "ZZZinvisible", false, true, false, true);
	glb = listGroups(adminId, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "ZZYinvisible", false, true, false, true);
	glb = listGroups(adminId, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "ZZXGroup", false, true, false, true);
	glb = listGroups(adminId, page0-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index0), "ZZWGroup", false, false, true, true);

	// Test last page of all groups tab in descending order.
	page1 = 1;
	page2 = 1;
	page0 = 1;
	page3 = 1;
	found=false;
	while (!found) {
	    index2 = 0;
	    glb = listGroups(adminId, page2, ESortDirection.DESC, "visible");
	    while (!found && index2 < glb.getList().size()) {
		if (glb.getList().get(index2).getGroup().getTitle().equals("AACGroup")) {
		    found = true;
		} else {
		    index2++;
		}
	    }
	    page2++;
	}

	found = false;
	while (!found) {
	    index3 = 0;
	    glb = listGroups(adminId, page3, ESortDirection.DESC, "visible");
	    while (!found && index3 < glb.getList().size()) {
		if (glb.getList().get(index3).getGroup().getTitle().equals("AADGroup")) {
		    found = true;
		} else {
		    index3++;
		}
	    }
	    page3++;
	}

	found = false;
	while (!found) {
	    index0 = 0;
	    glb = listGroups(adminId, page0, ESortDirection.DESC, "visible");
	    while (!found && index0 < glb.getList().size()) {
		if (glb.getList().get(index0).getGroup().getTitle().equals("AAAinvisible")) {
		    found = true;
		} else {
		    index0++;
		}
	    }
	    page0++;
	}
	
	found = false;
	while (!found) {
	    index1 = 0;
	    glb = listGroups(adminId, page1, ESortDirection.DESC, "visible");
	    while (!found && index1 < glb.getList().size()) {
		if (glb.getList().get(index1).getGroup().getTitle().equals("AABinvisible")) {
		    found = true;
		} else {
		    index1++;
		}
	    }
	    page1++;
	}

	assertTrue(index0 > index1 || page0 > page1);
	assertTrue(index1 > index2 || page1 > page2);
	assertTrue(index2 > index3 || page2 > page3);
	 glb = listGroups(adminId, page0-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index0), "AAAinvisible", true, false, false, true);
	 glb = listGroups(adminId, page1-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index1), "AABinvisible", false, false, true, true);
	 glb = listGroups(adminId, page2-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index2), "AACGroup", true, false, false, true);
	 glb = listGroups(adminId, page3-1, ESortDirection.DESC, "visible");
	matchRow(glb.getList().get(index3), "AADGroup", false, false, true, true);
    }

    private static GroupListBean listGroups(Integer userId, Integer page,
	    ESortDirection sortDirection, String listShown) {
	SessionData session = new SessionData();
	session.setUserId(userId);

	GroupListBean glb = new GroupListBean();
	glb.setPage(page - 1);
	glb.setSortDirection(sortDirection);
	glb.setListShown(listShown);

	GroupListAction ctrl = new GroupListAction();
	ctrl.setConfiguration(config);
	ctrl.setGroupListBean(glb);
	ctrl.setPersistenceProvider(pers);
	ctrl.setSession(session);

	ctrl.listGroups();

	return glb;
    }

    private static void matchAttended(int userId, String[] titles, boolean editable) {
	GroupListBean glb;

	// Test attended groups tab in ascending order.
	glb = listGroups(userId, 1, ESortDirection.ASC, "attended");
	assertEquals(titles.length, glb.getList().size());
	for (int i = 0; i < titles.length; i++) {
	    matchRow(glb.getList().get(i), titles[i], true, false, false, editable);
	}

	// Test attended groups tab in descending order.
	glb = listGroups(userId, 1, ESortDirection.DESC, "attended");
	assertEquals(titles.length, glb.getList().size());
	for (int i = 0; i < titles.length; i++) {
	    matchRow(glb.getList().get(i), titles[(titles.length - 1) - i], true, false, false,
		    editable);
	}
    }

    private static void matchOwned(int userId, String[] titles) {
	GroupListBean glb;

	// Test attended groups tab in ascending order.
	glb = listGroups(userId, 1, ESortDirection.ASC, "owned");
	assertEquals(titles.length, glb.getList().size());
	for (int i = 0; i < titles.length; i++) {
	    matchRow(glb.getList().get(i), titles[i], false, false, true, true);
	}

	// Test attended groups tab in descending order.
	glb = listGroups(userId, 1, ESortDirection.DESC, "owned");
	assertEquals(titles.length, glb.getList().size());
	for (int i = 0; i < titles.length; i++) {
	    matchRow(glb.getList().get(i), titles[(titles.length - 1) - i], false, false, true,
		    true);
	}
    }

    private static void matchRow(GroupListEntryBean row, String title, boolean attended,
	    boolean attendable, boolean owned, boolean editable) {
	if (attended && owned) {
	    Assert.fail();
	}
	if (attended && attendable) {
	    Assert.fail();
	}
	if (owned && attendable) {
	    Assert.fail();
	}
	if (owned && !editable) {
	    Assert.fail();
	}

	if (title != null) {
	    assertEquals(title, row.getGroup().getTitle());
	}
	assertTrue(row.isShowVideosAvailable());

	if (attended) {
	    assertTrue(row.isCurrentUserSignedUp());
	    assertTrue(row.isSignOffAvailable());
	    assertTrue(!row.isSignUpAvailable());
	}

	if (attendable) {
	    assertTrue(!row.isCurrentUserSignedUp());
	    assertTrue(!row.isSignOffAvailable());
	    assertTrue(row.isSignUpAvailable());
	}

	if (owned) {
	    assertTrue(row.isCurrentUserSignedUp());
	    assertTrue(!row.isSignOffAvailable());
	    assertTrue(!row.isSignUpAvailable());
	}

	if (editable) {
	    assertTrue(row.isEditAvailable());
	    assertTrue(row.isDeleteAvailable());
	}
    }
}
