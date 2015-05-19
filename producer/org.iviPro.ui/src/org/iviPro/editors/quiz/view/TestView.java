package org.iviPro.editors.quiz.view;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.quiz.std.TestModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;

/**
 * Diese Klasse implementiert die grafische Oberflaeche der Test-Einstellungen.
 * 
 * @author Sabine Gattermann
 * 
 */
public class TestView implements Observer {

	private static final int DISABLE = 0;

	private static final String LABELNAME = "Titel:";
	private static final String LABELKATEGORIE = "Kategorie:";
	private static final String LABELBESCHREIBUNG = "Beschreibung:";
	private static final String LABELTESTAUFBAU = "Testaufbau:";
	private static final String LABELRANDOM = "Zufallsverteilung von Antworten:";
	private static final String BUTTONLINEAR = "linear";
	private static final String BUTTONVERZWEIGT = "verzweigt";
	private static final String LABELMAXPUNKTE = "max. Punkt \\ Frage:";
	private static final String LABELBEWERTUNG = "Bewertungssystem:";
	private static final String BUTTONMINUS = "negative Punkte möglich";
	private static final String BUTTONCREATE = "Test erstellen";
	private static final String BUTTONLOAD = "Test öffnen";
	private static final String BUTTONCANCEL = "abbrechen";
	private static final String BUTTONPLUS = "nur positive Punkte";
	private static final String BUTTONRANDOM = "(kann pro Aufgabe geändert werden)";

	private static final int FIELDWIDTH = 300;
	private static final int FIELDHEIGHT = 80;
	private static final int SMALLWIDTH = 20;

	// Status-Codes Observer-Pattern
	private static final int START = 1;
	private static final int TESTNAME_EXISTS_ERROR = 2;
	private static final int TESTNAME_EMPTY_ERROR = 3;
	private static final int POINTS_NEGATIVE_ERROR = 4;
	private static final int POINTS_PARSE_ERROR = 5;

	private int selectedAufbau;
	private static final int LINEARSELECTED = 0;
	private static final int VERZWEIGTSELECTED = 1;

	private int selectedBewertung;
	private static final int POSITIVSELECTED = 0;
	private static final int NEGATIVSELECTED = 1;
	private static final String LABELFEEDBACK = "Testauswertung";
	private static final String BUTTONFEEDBACKSOFORT = "nach jeder Frage";
	private static final String BUTTONFEEDBACKAMENDE = "am Ende des Tests";
	private static final int SOFORTSELECTED = 1;
	private static final int ENDESELECTED = 0;
	private static final String BUTTONTESTPOOLPRIVATE = "nein";
	private static final String BUTTONTESTPOOLPUBLIC = "ja";
	private static final int PUBLICSELECTED = 1;
	private static final int PRIVATESELECTED = 0;
	private static final String LABELTESTPOOL = "Test im Testpool veröffentlichen?";

	private Shell shell;
	private TestModel iFace;
	private Display display;
	private Composite compositeMain;
	private Text testName;
	private Text beschreibung;
	private Text maxPunkte;
	private Combo comboKategorie;
	private Button buttonPlus;
	private Button buttonMinus;
	private Button buttonLinear;
	private Button buttonVerzweigt;
	private Button buttonFeedbackSofort;
	private Button buttonRandom;
	private int selectedFeedback;
	private Button buttonTestpoolPrivate;
	private Button buttonTestpoolPublic;
	private int selectedTestpool;
	private boolean random;

	private boolean loadExistingTest;

	/**
	 * Konstruktor
	 * 
	 * @param shell
	 *            Die Shell.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public TestView(Shell shell, TestModel iFace) {
		this.shell = shell;
		this.iFace = iFace;
		display = shell.getDisplay();
		iFace.addObserver(this);
		if (iFace.getIdTest() == -1) {
			loadExistingTest = false;
		} else {
			loadExistingTest = true;
		}
		runTestView();
	}

	/**
	 * Startet den Aufbau der View.
	 */
	private void runTestView() {

		shell.setLayout(new GridLayout());

		// Basis-Composite
		compositeMain = new Composite(shell, SWT.NONE);
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.horizontalSpacing = 10;
		compositeMain.setLayout(mainLayout);
		GridData mainGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		compositeMain.setLayoutData(mainGridData);
		compositeMain.setBackground(QuizGenerator.getColorBackground());

		// Composite fuer Buttons, Felder und Labels
		Composite compositeCreateTest = new Composite(compositeMain, SWT.NONE);
		compositeCreateTest.setLayout(new GridLayout(1, false));
		compositeCreateTest.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				true, true));
		compositeCreateTest.setBackground(QuizGenerator.getColorBackground());

		// Eingabemaske
		initInputFields(compositeCreateTest);
		// Buttons
		initButtons(compositeCreateTest);
	}

	/**
	 * Erstellt die Eingabemaske.
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 */
	private void initInputFields(Composite parent) {

		// Basis-Composite
		Composite compositeFields = new Composite(parent, SWT.NONE);
		GridLayout fieldsLayout = new GridLayout(2, false);
		fieldsLayout.horizontalSpacing = 15;
		compositeFields.setLayout(fieldsLayout);
		compositeFields.setBackground(QuizGenerator.getColorBackground());

		// Name
		Label labelName = new Label(compositeFields, SWT.SHADOW_IN);
		labelName.setText(LABELNAME);
		labelName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		labelName.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		testName = new Text(compositeFields, SWT.BORDER);
		GridData nameGridData = new GridData();
		nameGridData.widthHint = FIELDWIDTH;
		testName.setLayoutData(nameGridData);
		testName.setText(iFace.getTitle());

		// Kategorie
		Label labelKategorie = new Label(compositeFields, SWT.SHADOW_IN);
		labelKategorie.setText(LABELKATEGORIE);
		labelKategorie
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		labelKategorie.setBackground(QuizGenerator.getColorBackground());

		comboKategorie = new Combo(compositeFields, SWT.DROP_DOWN);
		String[] items = iFace.getCategoryList();
		for (int i = 0; i < items.length; i++) {
			comboKategorie.add(items[i]);
		}

		comboKategorie.select(comboKategorie.indexOf(iFace.getCategory()));
		GridData comboGridData = new GridData();
		comboGridData.widthHint = FIELDWIDTH / 2;
		comboKategorie.setLayoutData(comboGridData);

		// Beschreibung
		Label labelBeschreibung = new Label(compositeFields, SWT.SHADOW_IN);
		labelBeschreibung.setText(LABELBESCHREIBUNG);
		labelBeschreibung.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		labelBeschreibung.setBackground(QuizGenerator.getColorBackground());

		beschreibung = new Text(compositeFields, SWT.MULTI | SWT.BORDER
				| SWT.WRAP | SWT.V_SCROLL);

		GridData beschreibungGridData = new GridData();
		beschreibungGridData.widthHint = FIELDWIDTH - 15;
		beschreibungGridData.heightHint = FIELDHEIGHT;
		beschreibung.setLayoutData(beschreibungGridData);
		beschreibung.setText(iFace.getDescription());

		// max Punkte
		Label labelMaxPunkte = new Label(compositeFields, SWT.SHADOW_IN
				| SWT.WRAP);
		labelMaxPunkte.setText(LABELMAXPUNKTE);
		labelMaxPunkte
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		labelMaxPunkte.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		maxPunkte = new Text(compositeFields, SWT.BORDER | SWT.RIGHT);
		GridData maxPunkteGridData = new GridData();
		maxPunkteGridData.widthHint = SMALLWIDTH;
		maxPunkteGridData.verticalAlignment = GridData.END;
		maxPunkte.setLayoutData(maxPunkteGridData);
		maxPunkte.setText(String.valueOf(iFace.getMaxPoints()));

		// Bewertungssystem
		Label labelBewertung = new Label(compositeFields, SWT.SHADOW_IN);
		labelBewertung.setText(LABELBEWERTUNG);
		labelBewertung
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		labelBewertung.setBackground(QuizGenerator.getColorBackground());

		Composite compositeRadioBewertung = new Composite(compositeFields,
				SWT.NONE);
		compositeRadioBewertung.setLayout(new GridLayout(2, false));
		compositeRadioBewertung.setBackground(QuizGenerator
				.getColorBackground());

		buttonPlus = new Button(compositeRadioBewertung, SWT.RADIO);
		buttonPlus.setText(BUTTONPLUS);
		buttonPlus.setBackground(QuizGenerator.getColorBackground());
		buttonPlus.addSelectionListener(sl);

		buttonMinus = new Button(compositeRadioBewertung, SWT.RADIO);
		buttonMinus.setText(BUTTONMINUS);
		buttonMinus.setBackground(QuizGenerator.getColorBackground());
		buttonMinus.addSelectionListener(sl);

		if (iFace.getEvaluationMethod() == POSITIVSELECTED) {
			buttonPlus.setSelection(true);
			selectedBewertung = POSITIVSELECTED;
		} else if (iFace.getEvaluationMethod() == NEGATIVSELECTED) {
			buttonMinus.setSelection(true);
			selectedBewertung = NEGATIVSELECTED;
		}
		if (loadExistingTest && iFace.getTestType() == VERZWEIGTSELECTED) {
			buttonMinus.setEnabled(false);
			buttonPlus.setEnabled(false);
		}

		// Testaufbau
		Label labelTestAufbau = new Label(compositeFields, SWT.SHADOW_IN);
		labelTestAufbau.setText(LABELTESTAUFBAU);
		labelTestAufbau.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		labelTestAufbau.setBackground(QuizGenerator.getColorBackground());

		Composite compositeRadioAufbau = new Composite(compositeFields,
				SWT.NONE);
		compositeRadioAufbau.setLayout(new GridLayout(4, false));
		compositeRadioAufbau.setBackground(QuizGenerator.getColorBackground());

		buttonLinear = new Button(compositeRadioAufbau, SWT.RADIO);
		buttonLinear.setData("buttonLinear");
		buttonLinear.setText(BUTTONLINEAR);
		buttonLinear.setBackground(QuizGenerator.getColorBackground());
		buttonLinear.addSelectionListener(sl);

		buttonVerzweigt = new Button(compositeRadioAufbau, SWT.RADIO);
		buttonVerzweigt.setText(BUTTONVERZWEIGT);
		buttonVerzweigt.setData("buttonVerzweigt");
		buttonVerzweigt.setBackground(QuizGenerator.getColorBackground());
		buttonVerzweigt.addSelectionListener(sl);

		if (iFace.getTestType() == LINEARSELECTED) {
			buttonLinear.setSelection(true);
			selectedAufbau = LINEARSELECTED;
		} else if (iFace.getTestType() == VERZWEIGTSELECTED) {
			buttonVerzweigt.setSelection(true);
			selectedAufbau = VERZWEIGTSELECTED;
		}

		// Random
		Label labelRandom = new Label(compositeFields, SWT.SHADOW_IN);
		labelRandom.setText(LABELRANDOM);
		labelRandom.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		labelRandom.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Composite compositeCheckRandom = new Composite(compositeFields,
				SWT.NONE);
		compositeCheckRandom.setLayout(new GridLayout(2, false));
		compositeCheckRandom.setBackground(QuizGenerator.getColorBackground());

		buttonRandom = new Button(compositeCheckRandom, SWT.CHECK);
		buttonRandom.setText(BUTTONRANDOM);
		buttonRandom.setBackground(QuizGenerator.getColorBackground());
		random = iFace.isRandom();
		buttonRandom.setSelection(random);
		buttonRandom.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				if (random == true)
					random = false;
				else
					random = true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		// Feedback
		Label labelFeedback = new Label(compositeFields, SWT.SHADOW_IN);
		labelFeedback.setText(LABELFEEDBACK);
		labelFeedback
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		labelFeedback.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Composite compositeRadioFeedback = new Composite(compositeFields,
				SWT.NONE);
		compositeRadioFeedback.setLayout(new GridLayout(4, false));
		compositeRadioFeedback
				.setBackground(QuizGenerator.getColorBackground());

		buttonRandom = new Button(compositeRadioFeedback, SWT.RADIO);
		buttonRandom.setText(BUTTONFEEDBACKAMENDE);
		buttonRandom.setData("buttonFeedbackAmEnde");
		buttonRandom.setBackground(QuizGenerator.getColorBackground());
		buttonRandom.addSelectionListener(sl);

		buttonFeedbackSofort = new Button(compositeRadioFeedback, SWT.RADIO);
		buttonFeedbackSofort.setData("buttonFeedbackSofort");
		buttonFeedbackSofort.setText(BUTTONFEEDBACKSOFORT);
		buttonFeedbackSofort.setBackground(QuizGenerator.getColorBackground());
		buttonFeedbackSofort.addSelectionListener(sl);

		if (iFace.getTimeOfFeedback() == SOFORTSELECTED) {
			buttonFeedbackSofort.setSelection(true);
			selectedFeedback = SOFORTSELECTED;
		} else if (iFace.getTimeOfFeedback() == ENDESELECTED) {
			buttonRandom.setSelection(true);
			selectedFeedback = ENDESELECTED;
		}

		// Testpool
		Label labelTestpool = new Label(compositeFields, SWT.SHADOW_IN);
		labelTestpool.setText(LABELTESTPOOL);
		labelTestpool
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		labelTestpool.setBackground(QuizGenerator.getColorBackground());

		Composite compositeRadioTestpool = new Composite(compositeFields,
				SWT.NONE);
		compositeRadioTestpool.setLayout(new GridLayout(4, false));
		compositeRadioTestpool
				.setBackground(QuizGenerator.getColorBackground());

		buttonTestpoolPrivate = new Button(compositeRadioTestpool, SWT.RADIO);
		buttonTestpoolPrivate.setData("buttonTestpoolPrivate");
		buttonTestpoolPrivate.setText(BUTTONTESTPOOLPRIVATE);
		buttonTestpoolPrivate.setBackground(QuizGenerator.getColorBackground());
		buttonTestpoolPrivate.addSelectionListener(sl);

		buttonTestpoolPublic = new Button(compositeRadioTestpool, SWT.RADIO);
		buttonTestpoolPublic.setText(BUTTONTESTPOOLPUBLIC);
		buttonTestpoolPublic.setData("buttonTestpoolPublic");
		buttonTestpoolPublic.setBackground(QuizGenerator.getColorBackground());
		buttonTestpoolPublic.addSelectionListener(sl);

		if (iFace.getTestInPublicTestpool() == PRIVATESELECTED) {
			buttonTestpoolPrivate.setSelection(true);
			selectedTestpool = PRIVATESELECTED;
		} else if (iFace.getTestInPublicTestpool() == PUBLICSELECTED) {
			buttonTestpoolPublic.setSelection(true);
			selectedTestpool = PUBLICSELECTED;
		}

		if (iFace.getStatus() == DISABLE) {
			buttonLinear.setEnabled(false);
			buttonVerzweigt.setEnabled(false);
			maxPunkte.setEnabled(false);
		}
	}

	SelectionListener sl = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {

		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if (arg0.widget.equals(buttonLinear) && buttonLinear.getSelection()) {
				selectedAufbau = LINEARSELECTED;
				buttonPlus.setEnabled(true);
				buttonMinus.setEnabled(true);
			} else if (arg0.widget.equals(buttonVerzweigt)
					&& buttonVerzweigt.getSelection()) {
				selectedAufbau = VERZWEIGTSELECTED;
				buttonPlus.setSelection(true);
				buttonPlus.setEnabled(false);
				buttonMinus.setSelection(false);
				buttonMinus.setEnabled(false);
			}

			if (arg0.widget.equals(buttonPlus) && buttonPlus.getSelection()) {
				selectedBewertung = POSITIVSELECTED;
			} else if (arg0.widget.equals(buttonMinus)
					&& buttonMinus.getSelection()) {
				selectedBewertung = NEGATIVSELECTED;
			}

			if (arg0.widget.equals(buttonRandom) && buttonRandom.getSelection()) {
				selectedFeedback = ENDESELECTED;
			} else if (arg0.widget.equals(buttonFeedbackSofort)
					&& buttonFeedbackSofort.getSelection()) {
				selectedFeedback = SOFORTSELECTED;
			}

			if (arg0.widget.equals(buttonTestpoolPrivate)
					&& buttonTestpoolPrivate.getSelection()) {
				selectedTestpool = PRIVATESELECTED;
			} else if (arg0.widget.equals(buttonTestpoolPublic)
					&& buttonTestpoolPublic.getSelection()) {
				selectedTestpool = PUBLICSELECTED;
			}
		}
	};

	/**
	 * Erstellt Buttons.
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 */
	private void initButtons(Composite parent) {

		// Basis-Composite
		Composite compositeButtons = new Composite(parent, SWT.NONE);
		compositeButtons.setLayout(new GridLayout(2, false));
		GridData buttonsGridData = new GridData();
		buttonsGridData.verticalAlignment = GridData.END;
		buttonsGridData.horizontalAlignment = GridData.END;
		compositeButtons.setLayoutData(buttonsGridData);
		compositeButtons.setBackground(QuizGenerator.getColorBackground());

		// Profil erstellen
		Button buttonCreate = new Button(compositeButtons, SWT.PUSH);
		if (loadExistingTest) {
			buttonCreate.setText(BUTTONLOAD);
		} else {
			buttonCreate.setText(BUTTONCREATE);
		}

		buttonCreate.setLayoutData(new GridData(SWT.BOTTOM));

		// Listener
		buttonCreate.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (loadExistingTest) {
					iFace.checkForEditedTestProperties(testName.getText(),
							comboKategorie.getText(), beschreibung.getText(),
							maxPunkte.getText(), selectedBewertung,
							selectedAufbau, random, selectedFeedback,
							selectedTestpool);
				} else {
					iFace.checkUserInputFirst(testName.getText(),
							comboKategorie.getText(), beschreibung.getText(),
							maxPunkte.getText(), selectedBewertung,
							selectedAufbau, random, selectedFeedback,
							selectedTestpool);
				}
			}
		});

		// abbrechen
		Button buttonCancel = new Button(compositeButtons, SWT.PUSH);
		buttonCancel.setText(BUTTONCANCEL);
		buttonCancel.setLayoutData(new GridData(SWT.BOTTOM));

		// Listener
		buttonCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.cancel();
				compositeMain.dispose();
				shell.layout();
			}
		});
	}

	/**
	 * Popup-Dialog
	 * 
	 * @param msg
	 *            Der Nachrichten-String.
	 */
	private void doMsgDialog(String msg) {
		MessageBox errorDialog = new MessageBox(shell, SWT.OK);
		errorDialog.setMessage(msg);
		errorDialog.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg1) {

		int setCase = Integer.parseInt(arg1.toString());

		switch (setCase) {
		case START:
			iFace.startTestGeneration(loadExistingTest);
			compositeMain.dispose();
			shell.layout();
			break;
		case TESTNAME_EXISTS_ERROR:
			doMsgDialog(iFace.getMsg());
			testName.setText("");
			if (loadExistingTest)
				testName.setText(iFace.getTitle());
			testName.setFocus();
			break;
		case TESTNAME_EMPTY_ERROR:
			doMsgDialog(iFace.getMsg());
			testName.setFocus();
			break;
		case POINTS_PARSE_ERROR:
			doMsgDialog(iFace.getMsg());
			maxPunkte.setText("");
			maxPunkte.setFocus();
			break;
		case POINTS_NEGATIVE_ERROR:
			doMsgDialog(iFace.getMsg());
			maxPunkte.setFocus();
			break;
		}
	}

}
