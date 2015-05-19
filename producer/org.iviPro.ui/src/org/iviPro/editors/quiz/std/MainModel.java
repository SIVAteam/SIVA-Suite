package org.iviPro.editors.quiz.std;

import java.util.Observable;

import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.editors.quiz.interfaces.MainInterface;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;

/**
 * Diese Klasse modelliert die Funktionalitaet der Menueleiste.
 * 
 * @author Sabine Gattermann
 * 
 */
public class MainModel extends Observable implements MainInterface {

	// Status-Codes Observer-Pattern
	private static final int UPDATEMENU = 0;
	private static final int UPDATE = 1;
	private static final int SHOWPRIVATETESTS = 2;
	private static final int SHOWTESTPOOL = 3;
	private static final int CLOSE = 4;

	private static final String HELPFILEPATH = "McEditor - Handbuch.pdf";
	private static final String HELPFILETYPE = ".pdf";

	private Shell shell;
	private TestManagementModel testManagementModelInstance;
	private TestModel testModelInstance;
	private NodeModel nodeModelInstance;

	// Inhalt des Info-Fensters
	private final String INFOMESSAGE = "\n\n"
			+ "Multiple Choice Generator"
			+ "\n\n\n\n"
			+ "Programm zum Erstellen von Multiple-Choice Tests"
			+ "\n\n\n"
			+ "Autor: Sabine Gattermann"
			+ "\n\n"
			+ "Das Programm wurde 2010 im Rahmen einer Bachelor-Arbeit erstellt."
			+ "\n\n" + "Organisation: Universität Passau" + "\n"
			+ "Fakultät: Informatik / Mathematik" + "\n"
			+ "Lehrstuhl: Verteilte Informationssysteme" + "\n"
			+ "Lehrstuhlinhaber: Prof. Dr. Harald Kosch" + "\n"
			+ "Betreut von: Britta Meixner";

	/**
	 * Konstruktor
	 */
	public MainModel() {
		this.shell = QuizGenerator.getDefaultShell();
		// this.addObserver(new MainView(this.shell, this));
		QuizGenerator.setMainModelInstance(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#exit()
	 */
	@Override
	public void exit() {
		// ueberprueft, ob ein beenden moeglich ist
		if (nodeModelInstance != null) {
			if (nodeModelInstance.getCanExit() == true) {
				shell.dispose();
				System.exit(0);
			}

		} else {
			shell.dispose();
			System.exit(0);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#openOwenTests()
	 */
	@Override
	public void openOwenTests() {
		new TestManagementModel();
		setChanged();
		notifyObservers(UPDATE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#newTest()
	 */
	@Override
	public void newTest(int testId) {
		if (this.testManagementModelInstance != null) {
			this.testManagementModelInstance.close();
		}
		new TestModel(testId);
		setChanged();
		notifyObservers(UPDATE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#openTestPool()
	 */
	@Override
	public void openTestPool() {
		new TestManagementModel();
		setChanged();
		notifyObservers(UPDATE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#openProfil()
	 */
	@Override
	public void openProfil() {
		if (this.testManagementModelInstance != null) {
			this.testManagementModelInstance.close();
		}
		new RegistrationModel(false);
		setChanged();
		notifyObservers(UPDATE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#openHelp()
	 */
	@Override
	public void openHelp() {

		Program program = Program.findProgram(HELPFILETYPE);
		program.execute(HELPFILEPATH);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#getInfoMsg()
	 */
	@Override
	public String getInfoMsg() {
		return INFOMESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#close()
	 */
	@Override
	public void close() {
		new LoginModel();

		setChanged();
		notifyObservers(CLOSE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#getTestManagementModelInstance()
	 */
	@Override
	public TestManagementModel getTestManagementModelInstance() {
		return testManagementModelInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#getTestModelInstance()
	 */
	@Override
	public TestModel getTestModelInstance() {
		return testModelInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#getNodeModelInstance()
	 */
	@Override
	public NodeModel getNodeModelInstance() {
		return nodeModelInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#setNodeModelInstance(node.NodeModel)
	 */
	@Override
	public void setNodeModelInstance(NodeModel nodeModel) {
		this.nodeModelInstance = nodeModel;
		setChanged();
		notifyObservers(UPDATEMENU);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mainApplication.MainInterface#setTestManagementModelInstance(testManagement
	 * .TestManagementModel)
	 */
	@Override
	public void setTestManagementModelInstance(
			TestManagementModel testManagementModelInstance) {
		this.testManagementModelInstance = testManagementModelInstance;
		setChanged();
		notifyObservers(UPDATEMENU);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainApplication.MainInterface#setTestModelInstance(test.TestModel)
	 */
	@Override
	public void setTestModelInstance(TestModel testModelInstance) {
		this.testModelInstance = testModelInstance;
		setChanged();
		notifyObservers(UPDATEMENU);
	}

}
