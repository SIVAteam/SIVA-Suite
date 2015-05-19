package org.iviPro.editors.quiz.view;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.editors.scenegraph.subeditors.NodeQuizEditor;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Test;

/**
 * Diese Klasse implementiert die graphische Umsetzung der Testerstellung. In
 * ihr werden die einzelnen View-Elemente verwaltet.
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeView implements Observer {

	private static final int DYNAMIC = 1;

	private NodeModel iFace;

	private Display display;

	private Shell shell;

	private Composite compositeBasis;

	private int insertAlgorithm;

	private boolean expandGraph = true;
	private boolean expandQuestionOverview = true;
	private boolean expandCondition = true;
	private boolean expandQuestion = true;
	private boolean expandSettings = true;

	protected Color backgroundColor;
	protected Color highlightColor;

	public static boolean closeInformation;
	public static boolean forceClose;
	private boolean isDisposed;

	protected static int savedScrollPosHorizontal = 0;
	protected static int savedScrollPosVertical = 0;

	/**
	 * Konstruktor
	 * 
	 * @param shell
	 *            Das Eltern-Fenster.
	 * @param iFace
	 *            Die aufrufende Model-Klasse.
	 */
	public NodeView(Shell shell, NodeModel iFace, NodeQuizEditor editor) {
		this.shell = shell;

		shell.setLayout(new FillLayout());

		this.iFace = iFace;
		display = shell.getDisplay();
		insertAlgorithm = iFace.getInsertAlgorithm();
		iFace.addObserver(this);
		shell.setText(iFace.getTestTitle());
		backgroundColor = new Color(display, 190, 190, 190);
		highlightColor = new Color(display, 211, 211, 211);
		closeInformation = false;
		isDisposed = false;
		forceClose = false;

		runQuestionView();
	}

	/**
	 * Startet den Aufbau der View.
	 */
	private void runQuestionView() {
		compositeBasis = new Composite(shell, SWT.NONE);
		compositeBasis.setLayout(new FillLayout());

		Composite composite = new Composite(compositeBasis, SWT.NONE);
		composite.setLayout(new FormLayout());

		Sash sash = new Sash(composite, SWT.VERTICAL);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0); // Attach to top
		data.bottom = new FormAttachment(100, 0); // Attach to bottom
		data.left = new FormAttachment(40, 0); // Attach 40% across
		sash.setLayoutData(data);

		SashForm sa = new SashForm(composite, SWT.HORIZONTAL);
		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(sash, 0);
		sa.setLayoutData(data);
		SashForm sa2 = new SashForm(composite, SWT.HORIZONTAL);
		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		data.left = new FormAttachment(sash, 0);
		data.right = new FormAttachment(100, 0);
		sa2.setLayoutData(data);

		ExpandBar barLeft = new ExpandBar(sa, SWT.V_SCROLL);
		ExpandBar barRight = new ExpandBar(sa2, SWT.V_SCROLL);

		initBarLeft(barLeft);
		initBarRight(barRight);

		barLeft.setSpacing(8);
		barRight.setSpacing(8);
		shell.layout();

	}

	/**
	 * Erstellt die linke ExpandBar.
	 */
	private void initBarLeft(ExpandBar x) {

		x.setBackground(backgroundColor);
		x.addExpandListener(new ExpandListener() {

			@Override
			public void itemExpanded(ExpandEvent arg0) {
				if (arg0.item.getData("position") == "0")
					expandGraph = true;
				if (arg0.item.getData("position") == "1")
					expandQuestionOverview = true;
				if (arg0.item.getData("position") == "2")
					expandCondition = true;
			}

			@Override
			public void itemCollapsed(ExpandEvent arg0) {
				if (arg0.item.getData("position") == "0")
					expandGraph = false;
				if (arg0.item.getData("position") == "1")
					expandQuestionOverview = false;
				if (arg0.item.getData("position") == "2")
					expandCondition = false;
			}
		});
		// Erstellt Verzweigungs-Graph
		new NodeViewPartZest(x, iFace);
		NodeViewPartZest.open(expandGraph);

		// Erstellt Fragen-Uebersicht
		new NodeViewPartQuestionOverview(x, iFace);
		NodeViewPartQuestionOverview.open(expandQuestionOverview);

		if (insertAlgorithm == DYNAMIC) {
			new NodeViewPartCondition(x, iFace);
			NodeViewPartCondition.open(expandCondition);
		}

	}

	/**
	 * Erstellt die rechte ExpandBar.
	 */
	private void initBarRight(ExpandBar x) {
		x.setBackground(backgroundColor);
		x.addExpandListener(new ExpandListener() {

			@Override
			public void itemExpanded(ExpandEvent arg0) {
				if (arg0.item.getData("position") == "0")
					expandSettings = true;
				if (arg0.item.getData("position") == "1")
					expandQuestion = true;
			}

			@Override
			public void itemCollapsed(ExpandEvent arg0) {
				if (arg0.item.getData("position") == "0")
					expandSettings = false;
				if (arg0.item.getData("position") == "1")
					expandQuestion = false;
			}
		});

		NodeViewPartQuestion ques = new NodeViewPartQuestion(x, iFace);

		new NodeViewPartSettings(x, iFace, ques);
		NodeViewPartSettings.open(expandSettings);

		NodeViewPartQuestion.open(expandQuestion);

		// Deaktiviert. Werden vorerst nicht benötigt

		// // Erstellt Media
		// new NodeViewPartMedia(x, iFace);
		// NodeViewPartMedia.open(expandMedia);
		// // Erstellt Link
		// new NodeViewPartLinks(x, iFace);
		// NodeViewPartLinks.open(expandLinks);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg1) {
		new Cursor(display, SWT.NONE);

		int setCase = Integer.parseInt(arg1.toString());
		
		System.out.println(arg1.getClass());
		System.out.println("setCase="+setCase+"->");
		if (setCase == 1) {
			saveNewQuestion();
		} else if (setCase == 2) {
			numberOfAnswers();
		} else if (setCase == 3) {
			correctAnswers();
		} else if (setCase == 4) {
			emptyQuestion();
		} else if (setCase == 5) {
			reloadEdge();
		} else if (setCase == 0) {
			reLayoutWithoutCloseInfo();
		} else if (setCase == 6) {
			condition();
		} else if (setCase == 7) {
			inconsistant();
		} else if (setCase == 8) {
			System.out.println("save");
			save();
		} else if (setCase == 30) {
			exportDialog();
		}
	}

	private void saveNewQuestion() {
		compositeBasis.dispose();
		runQuestionView();
	}

	private void numberOfAnswers() {
		if (!closeInformation) {
			MessageDialog dialogInfo = new MessageDialog(
					shell,
					"Warnung",
					null,
					"Der Test ist nicht vollständig. Bitte korrigieren sie ihre Eingabe!",
					MessageDialog.QUESTION, new String[] { "Fortfahren" }, 0);
			int answer = dialogInfo.open();

			switch (answer) {
			case -1:
				reLayout();
				break;
			case 0:
				reLayout();
				break;
			}
		} else {
			NodeViewPartQuestion.resetCorrectAnswers();
		}
	}

	private void correctAnswers() {
		if (!closeInformation) {
			MessageDialog dialogInfo = new MessageDialog(
					shell,
					"Warnung",
					null,
					"Der Test ist nicht vollständig. Bitte korrigieren sie ihre Eingabe!",
					MessageDialog.QUESTION, new String[] { "Fortfahren" }, 0);
			int answer = dialogInfo.open();

			switch (answer) {
			case -1:
				reLayout();
				break;
			case 0:
				reLayout();
				break;
			}
		} else {
			if (!isDisposed) {
				NodeViewPartQuestion.resetCorrectAnswers();
			}
		}
	}

	private void emptyQuestion() {
		if (!closeInformation) {
			MessageDialog dialogInfo = new MessageDialog(
					shell,
					"Warnung",
					null,
					"Der Test ist nicht vollständig. Bitte korrigieren sie ihre Eingabe!",
					MessageDialog.QUESTION, new String[] { "Fortfahren" }, 0);
			int answer = dialogInfo.open();

			switch (answer) {
			case -1:
				reLayout();
				break;
			case 0:
				reLayout();
				break;
			}
		} else {
			if (!isDisposed) {
				NodeViewPartQuestion.resetQuestion();
			}
		}
	}

	private void reloadEdge() {
	}

	private void reLayoutWithoutCloseInfo() {
		if (!isDisposed) {
			shell.layout();
		}
	}

	private void reLayout() {
		if (!isDisposed) {
			shell.layout();
		}
	}

	private void condition() {
	}

	private void inconsistant() {
		if (!closeInformation) {
			MessageDialog dialogInfo = new MessageDialog(
					shell,
					"Warnung",
					null,
					"Der Test ist nicht vollständig. Bitte korrigieren sie ihre Eingabe!",
					MessageDialog.QUESTION, new String[] { "Fortfahren" }, 0);
			int answer = dialogInfo.open();

			switch (answer) {
			case -1:
				reLayout();
				break;
			case 0:
				reLayout();
				break;
			}
		}
	}

	private void save() {
		if (!isDisposed && !closeInformation) {
			new Cursor(display, SWT.CURSOR_WAIT);
			String enteredTitle = NodeViewPartSettings.getTitle();
			Test test = DbQueries.getTestData(enteredTitle);
			System.out.println("CALLU/P===");
			if (test != null) {
				// titel nur auf einzigartigkeit prüfen/anpassen, wenn dieser wirklich geändert wurde
				if(!iFace.getTestTitle().equals(enteredTitle)){
					if(DbQueries.getTestData(enteredTitle)!=null) System.out.println("not null");
					iFace.setTitle(countAvailableTests(enteredTitle));
				}
			} else {
				iFace.setTitle(NodeViewPartSettings.getTitle());
			}
			iFace.setFeedback(NodeViewPartSettings.getFeedbackValue());
			iFace.updateTestData();
			iFace.saveNexit(NodeViewPartQuestion.getQuestionText(),
					NodeViewPartQuestion.getPointsOfQuestion(),
					NodeViewPartQuestion.getAnswersFromTextFields(),
					NodeViewPartQuestion.getAnswerCheckboxIsSelected(),
					NodeViewPartQuestion.getRandom(), new LinkedList<String>(),
					new LinkedList<String>(), new LinkedList<String>(),
					new LinkedList<String>());
		}
	}

	/**
	 * Nimmt den eingegebenen Titel und prüft auf Einzigartigkeit. Wenn der
	 * Titel bereits verwendet wird, wird an diesen eine Zahl angehängt.
	 * 
	 * @author unknown
	 * @refactoring Tristan Schneider
	 * @param title
	 *            eingegebener Titel
	 * @return titel mit numerischer erweiterung
	 */
	private String countAvailableTests(String title) {
		String newTitle = title;
		boolean notUnique = true;
		int i = 0;
		while (notUnique) {
			newTitle = title + "_" +  String.valueOf(i);
			Test test = DbQueries.getTestData(newTitle);
			if (test != null) {
				i++;
			} else {
				notUnique = false;
				return newTitle;
			}
		}
		return "";
	}

	private void exportDialog() {
		// new Cursor(display, SWT.CURSOR_WAIT);
		// iFace.updateDbNode(NodeViewPartQuestion.getQuestionText(),
		// NodeViewPartQuestion.getPointsOfQuestion(),
		// NodeViewPartQuestion.getAnswersFromTextFields(),
		// NodeViewPartQuestion.getAnswerCheckboxIsSelected(),
		// NodeViewPartQuestion.getRandom(),
		// NodeViewPartLinks.getLinksFromTextFields(),
		// NodeViewPartMedia.getVideoFromTextFields(),
		// NodeViewPartMedia.getAudioFromTextFields(),
		// NodeViewPartMedia.getImageFromTextFields(), SAVENODE);
		//
		// if (iFace.checkTestConsistance()) {
		// DirectoryDialog directoryDialog = new DirectoryDialog(shell);
		// directoryDialog.setFilterPath(selectedExportDir);
		// directoryDialog
		// .setMessage("Please select a directory and click OK");
		//
		// dir = directoryDialog.open();
		// if (dir != null) {
		// iFace.exportTest(dir, "");
		// }
		// }
	}
}