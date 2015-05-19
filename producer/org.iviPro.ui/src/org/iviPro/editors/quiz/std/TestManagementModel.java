package org.iviPro.editors.quiz.std;

import java.util.LinkedList;
import java.util.Observable;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.application.Application;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Test;

/**
 * Klasse fuer die Verwaltung von Tests.
 * 
 * @author Sabine Gattermann
 * 
 */
public class TestManagementModel extends Observable implements
		org.iviPro.editors.quiz.interfaces.TestManagementInterface {

	// Status-Codes Observer-Pattern
	private static final int CANCEL = 0;
	private static final int LOADTEST = 1;
	private static final int RELOADPOOL = 2;
	private static final int CLOSE = -1;
	private static final int TEST_EXPORTED = 10;
	private static final int TEST_EXPORTED_ERR_EX = 11;
	private static final int TEST_EXPORTED_ERR_WR = 12;

	private LinkedList<Test> testList;
	private LinkedList<Test> poolList;
	private LinkedList<String> userNamesForPoolTestList;
	private int doAction;

	private String msg = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#getPoolTestList()
	 */
	@Override
	public LinkedList<Test> getPoolTestList() {
		poolList = DbQueries.getPoolTests(QuizGenerator.getCurrentUser());
		userNamesForPoolTestList = DbQueries.getUserNamesByTestList(poolList);
		return poolList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#getUserNamesForPoolTestList()
	 */
	@Override
	public LinkedList<String> getUserNamesForPoolTestList() {
		return userNamesForPoolTestList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#getTests()
	 */
	@Override
	public LinkedList<Test> getTests() {
		testList = DbQueries.getUserTests(QuizGenerator.getCurrentUser());
		return testList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * testManagement.TestManagementInterface#loadTestWithTitel(java.lang.String
	 * )
	 */
	@Override
	public Test loadTestWithTitel(int testId) {
		Test testToLoad = DbQueries.getTestData(testId);
		// QuizGenerator.getMainModelInstance().setTestManagementModelInstance(
		// null);
		// new TestModel(testToLoad);
		setChanged();
		notifyObservers(LOADTEST);
		return testToLoad;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#deleteTest(int,
	 * java.lang.String)
	 */
	@Override
	public void deleteTest(int id, String category) {
		DbQueries.deleteTest(id, category);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#importTest(int,
	 * java.lang.String)
	 */
	@Override
	public void importTest(int id, String testName) {
		if (DbQueries.isTestNameInPrivateDB(testName, QuizGenerator
				.getCurrentUser().getIdUser())) {
			boolean nameOK = false;
			while (nameOK == false) {
				Shell s = new Shell(Display.getCurrent());
				InputDialogImport dialog = new InputDialogImport(s);
				String newTitle = dialog.open();
				if (newTitle.length() > 0
						&& !testName.equals(newTitle)
						&& DbQueries.isTestNameInPrivateDB(newTitle,
								QuizGenerator.getCurrentUser().getIdUser()) == false) {
					Test testToImport = DbQueries.importTest(QuizGenerator
							.getCurrentUser().getIdUser(), id, newTitle,
							Application.getCurrentProject());
					QuizGenerator.setCurrentTest(testToImport);
					nameOK = true;
				} else if (newTitle.length() == 0) {
					nameOK = true;
				}
			}

		} else {
			Test testToImport = DbQueries.importTest(QuizGenerator
					.getCurrentUser().getIdUser(), id, "", Application
					.getCurrentProject());
			QuizGenerator.setCurrentTest(testToImport);
		}
		poolList = DbQueries.getPoolTests(QuizGenerator.getCurrentUser());
		userNamesForPoolTestList = DbQueries.getUserNamesByTestList(poolList);
		setChanged();
		notifyObservers(RELOADPOOL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#cancel()
	 */
	@Override
	public void cancel() {
		QuizGenerator.getMainModelInstance().setTestManagementModelInstance(
				null);
		new MainModel();
		setChanged();
		notifyObservers(CANCEL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#getDoAction()
	 */
	@Override
	public int getDoAction() {
		return doAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#exportTest(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void exportTest(String path, String title, String newTitleForExport) {
		ExportTest exporter = new ExportTest();
		int result = exporter.export(path, title, newTitleForExport);

		if (result == 0) {
			msg = "Der Test wurde erfolgreich exportiert!";
			setChanged();
			notifyObservers(TEST_EXPORTED);
		} else if (result == -1) {
			setChanged();
			notifyObservers(TEST_EXPORTED_ERR_EX);
		} else {
			msg = "Fehler! \nTest konnte nicht exportiert werden!";
			setChanged();
			notifyObservers(TEST_EXPORTED_ERR_WR);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#getMsg()
	 */
	@Override
	public String getMsg() {
		return msg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testManagement.TestManagementInterface#close()
	 */
	@Override
	public void close() {
		QuizGenerator.getMainModelInstance().setTestManagementModelInstance(
				null);
		setChanged();
		notifyObservers(CLOSE);
	}

}
