package org.iviPro.model.quiz;

import java.util.Iterator;
import java.util.LinkedList;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class TestManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;
	
	private static TestManager INSTANCE;

	private BeanList<Test> testList;
	
	private TestManager(Project project) {
		super(project);
	}
	
	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new TestManager(project);
		}
	}
	
    public static TestManager getInstance() {
        return INSTANCE;
    }
    
    public boolean isUserTestAvailableWithId(int testId) {
    	for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test test = (Test) iterator.next();
			if(test.getIdTest() == testId) {
				return true;
			}
		}
    	return false;
    }
	
	public int setTestData(Test test) {
		testList.add(test);
		return ++key;
	}
	
	public void deleteTest(int id) {
		
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getIdTest() == id) {
				NodeManager.getInstance().deleteNodesOfATest(te.getIdTest());
				iterator.remove();
				System.out.println("Deleteted Test!");
			}
		}
	}
	
	public Test getTestObject(String title) {
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getTitle().equalsIgnoreCase(title)) {
				return te;
			}
		}
		return null;
	}
	
	public Test getTestObject(int id) {
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getIdTest() == id) {
				return te;
			}
		}
		return null;
	}
	
	public LinkedList<Test> getUserTest(int userId) {
		LinkedList<Test> lst = new LinkedList<Test>();
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getIdUser() == userId) {
				lst.add(te);
			}
		}
		return lst;
	}
	
	public int getNumberOfNodes(Test test) {
		LinkedList<Node> nodelst = NodeManager.getInstance().getNodeListByTest(test.getIdTest());
		return nodelst.size();
	}
	
	public LinkedList<Test> getPoolTests(int userId) {
		LinkedList<Test> lst = new LinkedList<Test>();
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getPublicationStatus() == 1) {
				lst.add(te);
			} else if(te.getPublicationStatus() == 0 && te.getIdUser() == userId) {
				lst.add(te);
			}
		}
		return lst;
	}
	
	public LinkedList<String> getUserNamesByTestList(LinkedList<Test> poolList) {
		LinkedList<String> lst = new LinkedList<String>();
		for (Iterator<Test> iterator = poolList.iterator(); iterator.hasNext();) {
			Test test = (Test) iterator.next();
			lst.add(UserManager.getInstance().getUsername(test.getIdUser()));
		}
		return lst;
	}
	
	public LinkedList<String> getTestTitlesByUsername(String username) {
		LinkedList<String> lst = new LinkedList<String>();
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(UserManager.getInstance().getUsername(username) == te.getIdUser()) {
				lst.add(te.getTitle());
			}
		}
		return lst;
	}
	
	public boolean isTestNameInPrivateDB(String testName, int userID) {
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getIdUser() == userID && te.getTitle().equalsIgnoreCase(testName)) {
				return true;
			}
		}
		return false;
	}
	
	public void updateTestData(Test test, String cat) {
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getIdTest() == test.getIdTest()) {
				te.setIdUser(test.getIdUser());
				te.setTitle(test.getTitle());
				te.setCategory(cat);
				te.setDescription(test.getDescription());
				te.setMaxPoints(test.getMaxPoints());
				te.setEvaluationMethod(test.getEvaluationMethod());
				te.setTestType(test.getTestType());
				te.setTimeOfFeedback(test.getTimeOfFeedback());
				te.setPublicationStatus(test.getPublicationStatus());
			}
		}
	}
	
	public int getTestIdByTitle(String title) {
		for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test te = (Test) iterator.next();
			if(te.getTitle().equalsIgnoreCase(title)) {
				return te.getIdTest();
			}
		}
		return -1;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
    	testList = (BeanList<Test>)list;	
    	
    	//Set key!
    	int maxKey = 0;
    	for (Iterator<Test> iterator = testList.iterator(); iterator.hasNext();) {
			Test test = (Test) iterator.next();
			if(test.getIdTest() > maxKey) {
				maxKey = test.getIdTest();
			}
		}
    	key = maxKey;
	}
}
