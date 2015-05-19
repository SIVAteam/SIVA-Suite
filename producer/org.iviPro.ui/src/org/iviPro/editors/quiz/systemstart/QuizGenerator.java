package org.iviPro.editors.quiz.systemstart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.application.Application;
import org.iviPro.editors.quiz.std.MainModel;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.editors.quiz.std.TestManagementModel;
import org.iviPro.editors.scenegraph.subeditors.NodeQuizEditor;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.NodeManager;
import org.iviPro.model.quiz.Test;
import org.iviPro.model.quiz.TestManager;
import org.iviPro.model.quiz.User;

/**
 * Hauptklasse des MultipleChoiceEditor-Projekts.
 * 
 * @author Sabine Gatterman
 * @modified Stefan Zwicklbauer
 * 
 */
public class QuizGenerator {

	private static final String DBBOT = "User";
	private static Shell mainshell = null;
	private static Shell quizshell = null;
	private static Display display = null;
	private static MainModel mainModelInstance = null;

	// angemeldeter Benutzer
	private static User currentUser = setCurrentUser("admin");
	// aktueller Test
	private static Test currentTest;

	private static int currentTestId;
	private static NodeQuizEditor ed;

	public int show(int testId, NodeQuizEditor ed) {
		QuizGenerator.ed = ed;
		currentTestId = testId;
		if (!TestManager.getInstance().isUserTestAvailableWithId(testId)) {
			setMainModelInstance(new MainModel());
			initializeStandardTest(TestManager.getInstance().getKey() + 1);
			display = Display.getCurrent();
			mainshell = display.getActiveShell();
			if (checkIfProjectLoaded()) {
				quizshell = new Shell(mainshell, SWT.DIALOG_TRIM
						| SWT.APPLICATION_MODAL);
				new NodeModel(currentTest, false, false, ed);
				runGui();
			}
		} else {
			setMainModelInstance(new MainModel());
			initializeStandardTest(testId);
			display = Display.getCurrent();
			mainshell = display.getActiveShell();

			if (checkIfProjectLoaded()) {
				quizshell = new Shell(mainshell, SWT.DIALOG_TRIM
						| SWT.APPLICATION_MODAL);
				new NodeModel(currentTest, true, false, ed);
				runGui();
			}
		}
		return currentTest.getIdTest();

	}

	private static void initializeStandardTest(int amountTests) {
		if (NodeManager.getInstance().getAmountNodes(currentTestId) == 0) {
			currentTest = new Test(Application.getCurrentProject(),
					currentUser.getIdUser(), amountTests);
			int id = DbQueries.setTestData(currentTest);
			currentTest.setIdTest(id);
		} else {
			currentTest = new TestManagementModel()
					.loadTestWithTitel(currentTestId);
		}
	}

	private static boolean checkIfProjectLoaded() {
		if (Application.getCurrentProject() == null) {
			errorDialog("Bitte zuerst ein Projekt laden oder anlegen!");
			return false;
		}
		return true;
	}

	/**
	 * Fehlerdialog oeffnen.
	 * 
	 * @param msg
	 *            Nachrichten-String.
	 */
	public static void errorDialog(String msg) {
		MessageBox errorBox = new MessageBox(mainshell, SWT.OK);
		errorBox.setMessage(msg);
		errorBox.open();

	}

	/**
	 * Startet die graphische Oberflaeche.
	 */
	private static void runGui() {

		quizshell.setText("Multiple Choice Editor");
		quizshell.setLocation(100, 0);
		quizshell.setSize(1024, 800);
		quizshell.setBackground(getColorBackground());

		quizshell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event e) {

				if (mainModelInstance == null) {
					e.doit = true;
					;
				} else {

					/*
					 * Programm darf nicht beliebig geschlossen werden (z.B.
					 * erst wenn das Schreiben in die DB beendet ist)
					 */
					if (mainModelInstance.getNodeModelInstance() != null) {
						if (mainModelInstance.getNodeModelInstance()
								.getCanExit() == true)
							e.doit = true;
						else
							e.doit = false;

					}
				}

			}
		});

		quizshell.open();

		while (!quizshell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	/**
	 * Setter für aktuellen Benutzer.
	 * 
	 * @param current
	 *            Der Benutzername.
	 */
	public static User setCurrentUser(String current) {
		return DbQueries.getUserData(current);
	}

	/**
	 * Getter für aktuellen Benutzer.
	 * 
	 * @return Das Benutzer-Objekt
	 */
	public static User getCurrentUser() {
		return currentUser;
	}

	/**
	 * Getter für aktuelles Display-Objekt.
	 * 
	 * @return Das Display-Objekt.
	 */
	public static Display getDefaultDisplay() {
		return display;
	}

	/**
	 * Getter für die Shell.
	 * 
	 * @return Das Shell-Objekt.
	 */
	public static Shell getDefaultShell() {
		return quizshell;
	}

	/**
	 * Setter für aktuelles Test-Objekt.
	 * 
	 * @param currentTest
	 *            Das Test-Objekt.
	 */
	public static void setCurrentTest(Test currentTest) {
		QuizGenerator.currentTest = currentTest;
	}

	/**
	 * Getter für aktuelles Test-Objekt.
	 * 
	 * @return Das Test-Objekt.
	 */
	public static Test getCurrentTest() {
		return currentTest;
	}

	/**
	 * Getter für Hintergrundfarbe.
	 * 
	 * @return Die Hintergrundfarbe.
	 */
	public static Color getColorBackground() {
		return display.getSystemColor(SWT.COLOR_WHITE);
	}

	/**
	 * Getter für Instanz der Klasse MainModel.
	 * 
	 * @return Die Instanz.
	 */
	public static MainModel getMainModelInstance() {
		return mainModelInstance;
	}

	/**
	 * Setter für Instanz der Klasse MainModel.
	 * 
	 * @param mainModelInstance
	 *            Die Instanz
	 */
	public static void setMainModelInstance(MainModel mainModelInstance) {
		QuizGenerator.mainModelInstance = mainModelInstance;
	}

	/**
	 * Getter für Datenbank-Bot.
	 * 
	 * @return Der DB-Bot.
	 */
	public static String getDatabaseBotName() {
		return DBBOT;
	}

	public static NodeQuizEditor getNodeQuizEditor() {
		return ed;
	}
}
