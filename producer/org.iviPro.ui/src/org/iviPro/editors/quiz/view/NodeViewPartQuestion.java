package org.iviPro.editors.quiz.view;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.model.quiz.Answer;

/**
 * Diese Klasse implementiert die graphische Darstellung Frage-Panels.
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeViewPartQuestion {

	// entspricht 'Zz' bei implementiertem Nummerierungs-Algorithmus (fuer
	// Antworten) [a-z,Aa-Zz]
	private static final int MAXANSWERINDEX = 702;

	private static NodeModel iFace;
	private static Combo comboPoints;
	private static String[] points;
	private static Composite compositeQuestion;
	private static StyledText textQuestion;
	private static LinkedList<Text> answerTextFields;
	private static LinkedList<Button> answerCheckboxFields;
	private static LinkedList<Boolean> answerCheckboxIsSelected;
	private static int indexAnswers;
	private static ScrolledComposite sc;
	private static Composite compositeAnswers;
	private static Button buttonAddAnswer;

	private static final String BUTTONDELETE = "Frage löschen";
	private static final String BUTTONNEWNODE = "neue Frage";
	private static Color highlightColor;

	private static Composite parent;

	private static Button buttonEmphasize;

	private static Button randomButton;

	private static LinkedList<Integer> stylingStartPosition;

	private static LinkedList<Integer> stylingEndPosition;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public NodeViewPartQuestion(Composite parent, NodeModel iFace) {
		// super();
		NodeViewPartQuestion.parent = parent;
		NodeViewPartQuestion.iFace = iFace;
		highlightColor = new Color(parent.getDisplay(), 211, 211, 211);
	}

	/**
	 * Oeffnet die graphische Ausgabe.
	 */
	public static void open(boolean expandIt) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(highlightColor);
		GridLayout layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 5;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);

		// Punkte-ComboBox
		initButtons(composite);
		// Feld für Frage
		initQuestion(composite);
		// Composite fuer Antworten
		initAnswers(composite);
		// Add-Answer-Button
		// initAddButton(composite);

		// anmerkung: 2 gibt hier position im fenster an.
		ExpandItem item = new ExpandItem((ExpandBar) parent, SWT.NONE, 2);
		item.setText("Frage " + iFace.getCurrentPosition());
		item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

		item.setControl(composite);
		item.setData("position", "1");
		item.setExpanded(expandIt);
	}

	public void updateComboPoints(int points) {
		String oldVal = comboPoints.getText();
		comboPoints.removeAll();
		for (int i = 1; i <= points; i++) {
			comboPoints.add(String.valueOf(i));
		}
		if (Integer.parseInt(oldVal) > points) {
			comboPoints.setText(Integer.toString(points));
		} else {
			comboPoints.setText(oldVal);
		}
		comboPoints.redraw();
	}

	/**
	 * Zuruecksetzen der korrekt-CheckBoxes
	 */
	public static void resetCorrectAnswers() {
		for (int i = 0; i < answerCheckboxFields.size(); i++) {
			answerCheckboxFields.get(i).setSelection(false);
			answerCheckboxIsSelected.set(i, false);
		}
	}

	private static String initStylingList(String text) {

		String dbStr = text;
		stylingStartPosition = new LinkedList<Integer>();
		stylingEndPosition = new LinkedList<Integer>();

		while (dbStr.contains("<b>") && dbStr.contains("</b>")) {

			int s = dbStr.indexOf("<b>");
			stylingStartPosition.add(s);
			dbStr = dbStr.replaceFirst("<b>", "");

			int e = dbStr.indexOf("</b>");
			stylingEndPosition.add(e);
			dbStr = dbStr.replaceFirst("</b>", "");

		}
		return dbStr;
	}

	private static void styleMyText(StyledText text) {

		StyleRange[] ranges = new StyleRange[stylingStartPosition.size()];
		for (int i = 0; i < stylingStartPosition.size(); i++) {
			int l = stylingEndPosition.get(i) - stylingStartPosition.get(i);
			ranges[i] = new StyleRange(stylingStartPosition.get(i), l, null,
					null, SWT.BOLD);
		}
		text.replaceStyleRanges(0, text.getText().length(), ranges);

	}

	/*
	 * Set a style
	 */
	static void setStyle(Widget widget) {
		Point sel = textQuestion.getSelectionRange();
		if ((sel == null) || (sel.y == 0))
			return;
		StyleRange style;

		for (int i = sel.x; i < sel.x + sel.y; i++) {
			StyleRange range = textQuestion.getStyleRangeAtOffset(i);
			if (range != null) {

				style = (StyleRange) range.clone();
				style.start = i;
				style.length = 1;
			} else {

				style = new StyleRange(i, 1, null, null, SWT.NORMAL);

			}
			if (widget == buttonEmphasize) {
				style.fontStyle ^= SWT.BOLD;
			}/*
			 * else if (widget == italicButton) { style.fontStyle ^= SWT.ITALIC;
			 * } else if (widget == underlineButton) { style.underline =
			 * !style.underline; } else if (widget == strikeoutButton) {
			 * style.strikeout = !style.strikeout; }
			 */
			textQuestion.setStyleRange(style);
		}
		textQuestion.setSelectionRange(sel.x + sel.y, 0);

	}

	@SuppressWarnings("unused")
	private static String printStyleRanges(StyleRange[] styleRanges) {

		if (styleRanges == null)
			return "null";
		else if (styleRanges.length == 0)
			return "[]";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < styleRanges.length; i++) {
			sb.append(styleRanges[i] + "\n");
		}

		return sb.toString();
	}

	/**
	 * Erstellt Label und Combo fuer Punkt-Angabe
	 * 
	 * @param parent
	 *            Das Eltern-Composite
	 */
	private static void initButtons(Composite parent) {
		// Basis-Composite
		Composite compositeBasis = new Composite(parent, SWT.NONE);
		compositeBasis.setBackground(highlightColor);
		compositeBasis.setLayout(new GridLayout(6, true));

		GridData row1LeftGridData = new GridData();
		row1LeftGridData.horizontalAlignment = SWT.FILL;
		row1LeftGridData.grabExcessHorizontalSpace = true;
		row1LeftGridData.grabExcessVerticalSpace = false;
		compositeBasis.setLayoutData(row1LeftGridData);

		Composite compositePoints = new Composite(compositeBasis, SWT.NONE);
		compositePoints.setLayout(new GridLayout(2, false));
		compositePoints.setLayoutData(new GridData(SWT.LEFT));
		compositePoints.setBackground(highlightColor);

		// Label: Punkte -- ausgerichtet nach Combo
		Label labelPoints = new Label(compositePoints, SWT.NONE);
		labelPoints.setText("Punkte");
		labelPoints.setBackground(highlightColor);

		comboPoints = new Combo(compositePoints, SWT.DROP_DOWN | SWT.READ_ONLY);
		points = iFace.getPointsList();
		for (int i = 0; i < points.length; i++) {
			comboPoints.add(points[i]);
		}

		comboPoints.select(iFace.getCurrentNode().getPoints() - 1);

		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setStyle(event.widget);
			}
		};
		buttonEmphasize = new Button(compositeBasis, SWT.PUSH);
		buttonEmphasize.setText("Fett");
		buttonEmphasize.addSelectionListener(listener);
		buttonEmphasize.setLayoutData(new GridData(SWT.RIGHT));

		Composite compositeNavigationButtons = new Composite(compositeBasis,
				SWT.NONE);
		GridLayout naviLayout = new GridLayout(2, true);

		compositeNavigationButtons.setLayout(naviLayout);
		compositeNavigationButtons.setBackground(highlightColor);

		GridData naviGridData = new GridData();
		naviGridData.horizontalSpan = 2;
		naviGridData.horizontalAlignment = SWT.CENTER;
		compositeNavigationButtons.setLayoutData(naviGridData);

		// Pfeil-Button: gehe zur vorherigen Frage
		Button buttonPrevious = new Button(compositeNavigationButtons,
				SWT.ARROW | SWT.LEFT);

		buttonPrevious.setEnabled(iFace.enableLastNode());

		GridData previousGridData = new GridData();
		previousGridData.horizontalAlignment = SWT.CENTER;
		buttonPrevious.setLayoutData(previousGridData);

		buttonPrevious.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.showNodeBefore(NodeViewPartQuestion.getQuestionText(),
						NodeViewPartQuestion.getPointsOfQuestion(),
						NodeViewPartQuestion.getAnswersFromTextFields(),
						NodeViewPartQuestion.getAnswerCheckboxIsSelected(),
						NodeViewPartQuestion.getRandom(),
						NodeViewPartLinks.getLinksFromTextFields(),
						NodeViewPartMedia.getVideoFromTextFields(),
						NodeViewPartMedia.getAudioFromTextFields(),
						NodeViewPartMedia.getImageFromTextFields());
			}
		});

		// Pfeil-Button: gehe zu nächster Frage
		Button buttonNext = new Button(compositeNavigationButtons, SWT.ARROW
				| SWT.RIGHT);
		buttonNext.setEnabled(iFace.enableNextNode());

		GridData nextGridData = new GridData();
		nextGridData.horizontalAlignment = SWT.CENTER;
		buttonNext.setLayoutData(nextGridData);

		buttonNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.showNodeNext(NodeViewPartQuestion.getQuestionText(),
						NodeViewPartQuestion.getPointsOfQuestion(),
						NodeViewPartQuestion.getAnswersFromTextFields(),
						NodeViewPartQuestion.getAnswerCheckboxIsSelected(),
						NodeViewPartQuestion.getRandom(),
						NodeViewPartLinks.getLinksFromTextFields(),
						NodeViewPartMedia.getVideoFromTextFields(),
						NodeViewPartMedia.getAudioFromTextFields(),
						NodeViewPartMedia.getImageFromTextFields());
			}
		});

		// Button: neue Frage
		Button buttonNew = new Button(compositeBasis, SWT.PUSH);
		buttonNew.setText(BUTTONNEWNODE);
		buttonNew.setEnabled(iFace.canInsertAnotherEdge());

		GridData newGridData = new GridData();
		newGridData.horizontalAlignment = SWT.RIGHT;
		buttonNew.setLayoutData(newGridData);

		// Listener
		buttonNew.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				iFace.addNewNode(NodeViewPartQuestion.getQuestionText(),
						NodeViewPartQuestion.getPointsOfQuestion(),
						NodeViewPartQuestion.getAnswersFromTextFields(),
						NodeViewPartQuestion.getAnswerCheckboxIsSelected(),
						NodeViewPartQuestion.getRandom(),
						NodeViewPartLinks.getLinksFromTextFields(),
						NodeViewPartMedia.getVideoFromTextFields(),
						NodeViewPartMedia.getAudioFromTextFields(),
						NodeViewPartMedia.getImageFromTextFields());

			}
		});

		// Button: Loeschen
		Button buttonDelete = new Button(compositeBasis, SWT.PUSH);
		buttonDelete.setText(BUTTONDELETE);
		if (iFace.getNodeList().size() < 2)
			buttonDelete.setEnabled(false);

		GridData deleteGridData = new GridData();
		deleteGridData.horizontalAlignment = SWT.LEFT;
		buttonDelete.setLayoutData(deleteGridData);

		// Listener
		buttonDelete.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.deleteNode();
			}
		});

	}

	/**
	 * Erstellt Eingabefeld fuer die Frage.
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 */
	private static void initQuestion(Composite parent) {
		// Basis-Composite
		compositeQuestion = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(1, true);
		compositeQuestion.setLayout(layout);
		compositeQuestion.setBackground(highlightColor);

		GridData answersGridData = new GridData(SWT.FILL, SWT.FILL, true, true,
				0, 0);
		answersGridData.heightHint = 100;
		answersGridData.widthHint = 400;
		compositeQuestion.setLayoutData(answersGridData);

		// Text: Frage
		textQuestion = new StyledText(compositeQuestion, SWT.WRAP | SWT.MULTI
				| SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textQuestion.setBackground(QuizGenerator.getColorBackground());

		GridData textGridData = new GridData(SWT.CENTER, SWT.CENTER, true,
				true, 0, 0);
		textGridData.widthHint = 300;
		textGridData.heightHint = 100;
		textQuestion.setLayoutData(new GridData(GridData.FILL_BOTH));
		textQuestion.setText(initStylingList(iFace.getCurrentQuestion()
				.getQuestionText()));
		styleMyText(textQuestion);
		textQuestion.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT
						|| e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});

	}

	/**
	 * Erstellt das Composite fuer Antworten
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 */
	private static void initAnswers(Composite parent) {
		answerTextFields = new LinkedList<Text>();
		answerCheckboxFields = new LinkedList<Button>();
		answerCheckboxIsSelected = new LinkedList<Boolean>();
		indexAnswers = 1;

		// ScrolledComposite zwecks zusätzlicher Fragen
		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL);

		sc.setLayout(new FillLayout(SWT.HORIZONTAL));

		sc.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 0, 0));

		sc.setAlwaysShowScrollBars(false);
		sc.setBackground(highlightColor);
		;

		// Basis-Composite (wird gescrolled)
		compositeAnswers = new Composite(sc, SWT.NONE);

		GridData compositeAnswersGridData = new GridData(SWT.FILL, SWT.FILL,
				true, true, 0, 0);
		compositeAnswersGridData.widthHint = 400;
		compositeAnswers.setLayoutData(compositeAnswersGridData);

		GridLayout answerLayout = new GridLayout(4, false);
		compositeAnswers.setLayout(answerLayout);
		compositeAnswers.setBackground(highlightColor);
		LinkedList<Answer> answers = iFace.getCurrentAnswerList();

		// erstellt Maske mit (initial 5) Antwort-Feldern
		for (int i = 0; i < answers.size(); i++) {
			Label l1 = new Label(compositeAnswers, SWT.NONE);
			l1.setText(calcAnswerIndex());

			final Text t = new Text(compositeAnswers, SWT.V_SCROLL | SWT.MULTI
					| SWT.WRAP);
			GridData g = new GridData();
			g.heightHint = 30;
			g.grabExcessHorizontalSpace = true;
			g.horizontalAlignment = SWT.FILL;
			t.setLayoutData(g);
			t.setData("position", (char) indexAnswers);
			answerTextFields.add(t);
			t.setText(answers.get(i).getAnswerText());

			t.addKeyListener(addAnswerLineListerner);

			final Button b = new Button(compositeAnswers, SWT.CHECK);
			answerCheckboxFields.add(b);
			answerCheckboxIsSelected.add(answers.get(i).getIsCorrect());
			b.addSelectionListener(cl);
			b.setSelection(answers.get(i).getIsCorrect());
			Label l2 = new Label(compositeAnswers, SWT.NONE);
			l2.setText("korrekt");
			l2.setBackground(highlightColor);

			t.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_TAB_NEXT
							|| e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
						e.doit = true;
					}
				}
			});

		}
		compositeAnswers.layout();
		compositeAnswers.setSize(500,
				compositeAnswers.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		sc.setContent(compositeAnswers);

		Composite randomComposite = new Composite(parent, SWT.NONE);

		GridLayout randomLayout = new GridLayout(2, false);
		randomComposite.setLayout(randomLayout);
		randomComposite.setLayoutData(new GridData(SWT.END, SWT.FILL, true,
				true, 0, 0));
		randomButton = new Button(randomComposite, SWT.CHECK);
		randomButton.setSelection(iFace.isRandomAnswers());

		Label randomLabel = new Label(randomComposite, SWT.NONE);
		randomLabel.setText("zufällige Verteilung der Antworten");
		parent.layout();

	}

	static KeyListener addAnswerLineListerner = new KeyListener() {

		@Override
		public void keyReleased(KeyEvent arg0) {

		}

		@Override
		public void keyPressed(KeyEvent arg0) {
			if (arg0.getSource().equals(answerTextFields.getLast())) {

				// neue Zeile einfuegen
				Label l1 = new Label(compositeAnswers, SWT.NONE);
				l1.setText(calcAnswerIndex());

				final Text t = new Text(compositeAnswers, SWT.V_SCROLL
						| SWT.MULTI | SWT.WRAP);
				GridData g = new GridData();
				g.heightHint = 30;
				g.grabExcessHorizontalSpace = true;
				g.horizontalAlignment = SWT.FILL;
				t.setLayoutData(g);
				t.setData("position", (char) indexAnswers);
				answerTextFields.add(t);
				t.addKeyListener(addAnswerLineListerner);
				t.addTraverseListener(new TraverseListener() {
					public void keyTraversed(TraverseEvent e) {
						if (e.detail == SWT.TRAVERSE_TAB_NEXT
								|| e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
							e.doit = true;
						}
					}
				});

				Button b = new Button(compositeAnswers, SWT.CHECK);
				answerCheckboxFields.add(b);
				answerCheckboxIsSelected.add(false);
				b.addSelectionListener(cl);
				Label l2 = new Label(compositeAnswers, SWT.NONE);
				l2.setText("korrekt");
				l2.setBackground(highlightColor);

				// Eltern-Updaten
				compositeAnswers.getParent().layout(
						new Control[] { l1, t, b, l2 });
				compositeAnswers.layout();
				// Neue Groesse berechnen
				compositeAnswers
						.setSize(500, compositeAnswers.computeSize(SWT.DEFAULT,
								SWT.DEFAULT).y);
				// Focus auf Textfeld der neuen Zeile
				t.setFocus();
				// 'auto-Scroll' auf neue Zeile
				sc.setShowFocusedControl(true);

				if (indexAnswers > MAXANSWERINDEX) {
					buttonAddAnswer.setEnabled(false);
				}
				answerTextFields.get(answerTextFields.size() - 2).setFocus();

			}
		}
	};

	/**
	 * Erstellt Button, bei dessen Klicken eine weitere Antwortzeile eingefuegt
	 * wird.
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 */
	@SuppressWarnings("unused")
	private static void initAddButton(Composite parent) {
		// Button: Add
		buttonAddAnswer = new Button(parent, SWT.PUSH);
		buttonAddAnswer.setText("Antwort hinzufügen");
		buttonAddAnswer.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true,
				true, 1, 1));
		buttonAddAnswer.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {

				// neue Zeile einfuegen
				Label l1 = new Label(compositeAnswers, SWT.NONE);
				l1.setText(calcAnswerIndex());
				Text t = new Text(compositeAnswers, SWT.V_SCROLL);
				t.setData("antwort" + indexAnswers);
				GridData g = new GridData();
				g.heightHint = 30;
				g.grabExcessHorizontalSpace = true;
				g.horizontalAlignment = SWT.FILL;
				t.setLayoutData(g);
				answerTextFields.add(t);
				Button b = new Button(compositeAnswers, SWT.CHECK);
				answerCheckboxFields.add(b);
				answerCheckboxIsSelected.add(false);
				b.addSelectionListener(cl);
				Label l2 = new Label(compositeAnswers, SWT.NONE);
				l2.setText("korrekt");
				l2.setBackground(highlightColor);

				// Eltern-Updaten
				compositeAnswers.getParent().layout(
						new Control[] { l1, t, b, l2 });
				compositeAnswers.layout();
				// Neue Groesse berechnen
				compositeAnswers.setSize(
						500,
						compositeAnswers.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				// Focus auf Textfeld der neuen Zeile
				t.setFocus();
				// 'auto-Scroll' auf neue Zeile
				sc.setShowFocusedControl(true);

				if (indexAnswers > MAXANSWERINDEX) {
					buttonAddAnswer.setEnabled(false);
				}
				answerTextFields.get(answerTextFields.size() - 2).setFocus();
			}
		});

	}

	/**
	 * Berechnet den naechsten Antwort-Index. (a bis zz)
	 * 
	 * @return Der Antwort-Index.
	 */
	private static String calcAnswerIndex() {
		String index = null;
		int numberOfChars = 26;
		if (indexAnswers <= numberOfChars) {
			int head = 'a' - 1 + indexAnswers;
			index = " " + (char) head + " ";
		} else {
			int x = indexAnswers / numberOfChars;
			int y = indexAnswers % numberOfChars;

			if (y == 0) { // manueller shift notwendig!!!
				x--;
				y = numberOfChars;
			}
			int head = 'A' - 1 + x;
			int body = 'a' - 1 + y;

			index = " " + (char) head + "" + (char) body + " ";

		}
		indexAnswers++;
		return index;
	}

	/**
	 * Listener fuer Button der neue Antwortmoeglichkeit erzeugt.
	 */
	static SelectionListener cl = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			for (int p = 0; p < answerCheckboxFields.size(); p++) {
				if (arg0.getSource().equals(answerCheckboxFields.get(p))) {
					answerCheckboxIsSelected.set(p,
							!answerCheckboxIsSelected.get(p));
				}

			}

		}

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {

		}
	};

	/**
	 * Getter fuer die eingegebenen Antworten.
	 * 
	 * @return Die Liste eingegebener Antworten.
	 */
	public static LinkedList<String> getAnswersFromTextFields() {
		int count = answerTextFields.size();
		LinkedList<String> answers = new LinkedList<String>();

		for (int i = 0; i < count; i++) {
			answers.add(i, answerTextFields.get(i).getText());
		}

		return answers;
	}

	/**
	 * Getter fuer die korrekt-CheckBoxes (fuer die eine Antwort eingegeben
	 * wurde).
	 * 
	 * @return Die Liste der relevanten korrekt-CheckBoxes.
	 */
	public static LinkedList<Boolean> getAnswerCheckboxIsSelected() {
		return answerCheckboxIsSelected;
	}

	/**
	 * Getter fuer die korrekt-CheckBoxes (fuer die eine Antwort eingegeben
	 * wurde).
	 * 
	 * @return Die Liste der relevanten korrekt-CheckBoxes.
	 */
	public static LinkedList<Boolean> setAnswerCheckboxIsSelected() {
		return answerCheckboxIsSelected;
	}

	/**
	 * Getter fuer den Fragetext.
	 * 
	 * @return Der Fragetext.
	 */
	public static String getQuestionText() {

		int numLines = textQuestion.getLineCount();
		StyleRange[] ranges = textQuestion.getStyleRanges();

		String q = "";
		for (int i = 0; i < numLines; i++) {
			// zeile nicht leer oder zeile ist letzte zeile
			if (!textQuestion.getLine(i).isEmpty())
				q = q + textQuestion.getLine(i) + "\n";
			else if (i == numLines - 1 && textQuestion.getLine(i).isEmpty()) {
			} else {
				// falls nächste zeile nicht leer
				if (!textQuestion.getLine(i + 1).isEmpty())
					q = q + textQuestion.getLine(i) + "\n";

			}
		}

		// Bold-Styling auf HTML parsen
		for (int i = ranges.length - 1; i >= 0; i--) {
			int s = ranges[i].start;
			int l = ranges[i].length;
			int endIndex = s + l;
			q = q.substring(0, endIndex) + "</b>" + q.substring(endIndex);
			q = q.substring(0, s) + "<b>" + q.substring(s);
		}

		return q;
	}

	/**
	 * Setzt das Fragetextfeld zurueck.
	 */
	public static void resetQuestion() {
		textQuestion.setText("");
		textQuestion.setFocus();
	}

	/**
	 * Getter fuer die in der View gewaehlten Punkte der Frage.
	 * 
	 * @return Die Punkte.
	 */
	public static int getPointsOfQuestion() {
		return Integer.parseInt(comboPoints.getText());
	}

	/**
	 * Getter fuer Status der Randomisierungs.
	 * 
	 * @return true, aktiviert, false sonst.
	 */
	public static boolean getRandom() {
		return randomButton.getSelection();
	}
}
