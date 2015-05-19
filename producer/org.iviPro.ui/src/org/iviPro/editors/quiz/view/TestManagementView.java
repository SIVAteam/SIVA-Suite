package org.iviPro.editors.quiz.view;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.iviPro.editors.quiz.std.InputDialogExport;
import org.iviPro.editors.quiz.std.TestManagementModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Test;

/**
 * Diese Klasse implementiert die grafische Oberflaeche des privaten, sowie des
 * oeffentlichen Test-Pools.
 * 
 * @author Sabine Gattermann
 * 
 */
public class TestManagementView implements Observer {

    // Status-Codes Observer-Pattern
    private static final int SHOWPRIVATETESTS = 2;
    private static final int SHOWTESTPOOL = 3;
    private static final int CANCEL = 0;
    private static final int LOADTEST = 1;
    private static final int RELOADPOOL = 2;
    private static final int CLOSE = -1;
    private static final int TEST_EXPORTED = 10;
    private static final int TEST_EXPORTED_ERR_EX = 11;
    private static final int TEST_EXPORTED_ERR_WR = 12;

    private TestManagementModel iFace;
    private Display display;
    private Shell shell;

    // Basis-Composite fuer Welcome-Menue
    private Composite compositeMain;
    private int doAction;

    private static final String BUTTONCANCEL = "zurück";
    private static final String BUTTONLOAD = "Test laden";
    private static final String BUTTONIMPORT = "Test importieren";
    private static final String BUTTONEXPORT = "Test exportieren";

    private Composite compositeShowTests;
    private Table table;
    private int status;
    private int columnsPosStartDetails;
    private String lastSelectedTitleForExport;

    private Color tableColor;
    protected String selectedDir;

    /**
     * Konstruktor
     * 
     * @param shell
     *            Das Hauptfenster.
     * @param iFace
     *            Die aufrufende Model-Klasse.
     */
    public TestManagementView(Shell shell, TestManagementModel iFace) {
	this.shell = shell;
	this.display = shell.getDisplay();
	this.iFace = iFace;
	iFace.addObserver(this);
	this.doAction = iFace.getDoAction();
	this.tableColor = new Color(display, 211, 211, 211);
	runGUI();
    }

    /**
     * Startet den Aufbau der View.
     */
    private void runGUI() {
	shell.setLayout(new GridLayout());

	if (doAction == SHOWPRIVATETESTS)
	    shell.setText("Multiple Choice Editor - Eigene Tests");
	else
	    shell.setText("Multiple Choice Editor - Test-Pool");
	// Basis-Composite
	compositeMain = new Composite(shell, SWT.NONE);
	GridLayout mainLayout = new GridLayout(1, false);
	mainLayout.horizontalSpacing = 10;
	compositeMain.setLayout(mainLayout);
	GridData mainGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
	compositeMain.setLayoutData(mainGridData);
	compositeMain.setBackground(QuizGenerator.getColorBackground());

	initShowTestsComposite(compositeMain);

    }

    /**
     * Erstellt das Composite mit der Auflistung der Tests (in einer Tabelle).
     * 
     * @param parent
     *            Das Eltern-Composite.
     */
    private void initShowTestsComposite(Composite parent) {

	compositeShowTests = new Composite(parent, SWT.NONE);

	GridLayout showTestsLayout = new GridLayout(1, true);
	compositeShowTests.setLayout(showTestsLayout);
	compositeShowTests.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		true));
	compositeShowTests.setBackground(QuizGenerator.getColorBackground());

	initTable(compositeShowTests);

	Button buttonDetails = new Button(compositeShowTests, SWT.PUSH);
	buttonDetails.setText(" Details einblenden ");
	GridData buttonDetailGridData = new GridData();
	buttonDetailGridData.horizontalAlignment = GridData.END;
	buttonDetails.setLayoutData(buttonDetailGridData);
	buttonDetails.addSelectionListener(new SelectionListener() {

	    @Override
	    public void widgetSelected(SelectionEvent arg0) {
		if (((Button) arg0.widget).getText().equals(
			" Details einblenden ")) {
		    for (int i = 1; i < table.getColumnCount(); i++) {
			table.getColumn(i).pack();
		    }
		    ((Button) arg0.widget).setText(" Details ausblenden ");

		} else {

		    for (int i = columnsPosStartDetails; i < table
			    .getColumnCount(); i++) {
			table.getColumn(i).setWidth(0);
		    }
		    for (int i = 1; i < columnsPosStartDetails; i++) {
			table.getColumn(i).pack();
		    }
		    ((Button) arg0.widget).setText(" Details einblenden ");

		}

		table.setSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		table.layout(true, true);
		compositeShowTests.setSize(compositeShowTests.computeSize(
			SWT.DEFAULT, SWT.DEFAULT));
		compositeShowTests.getParent().layout();
	    }

	    @Override
	    public void widgetDefaultSelected(SelectionEvent arg0) {

	    }
	});
	buttonDetails.setSelection(true);

	if (doAction == SHOWPRIVATETESTS) {
	    Button buttonDeleteTest = new Button(compositeShowTests, SWT.PUSH);
	    buttonDeleteTest.setText(" Test löschen ");
	    if (status == 0) {
		buttonDeleteTest.setEnabled(false);
	    }
	    GridData buttonDeleteTestGridData = new GridData();
	    buttonDeleteTestGridData.horizontalAlignment = GridData.END;
	    buttonDeleteTest.setLayoutData(buttonDeleteTestGridData);
	    buttonDeleteTest.addSelectionListener(new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent arg0) {
		    if (((Button) arg0.widget).getText().equals(
			    " Test löschen ")) {
			TableItem[] result = table.getSelection();
			if (result.length > 0) {
			    MessageBox box = new MessageBox(shell,
				    SWT.ICON_WARNING | SWT.YES | SWT.NO);
			    box
				    .setMessage("Der Test wird endgültig gelöscht!\n\nLöschvorgang fortsetzten?\n\n\n");
			    if (box.open() == SWT.YES) {
				iFace.deleteTest(Integer.parseInt(result[0]
					.getText(0)), result[0].getText(2));
				result[0].dispose();
				table.layout();
			    }
			} else {
			    MessageBox errorDialog = new MessageBox(shell,
				    SWT.OK);
			    errorDialog
				    .setMessage("Es wurde kein Test ausgewählt!");
			    errorDialog.open();
			}
		    }

		    table.setSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		    table.layout(true, true);
		    compositeShowTests.setSize(compositeShowTests.computeSize(
			    SWT.DEFAULT, SWT.DEFAULT));
		    compositeShowTests.getParent().layout();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {

		}
	    });
	}

	initButtons(compositeShowTests, table);
    }

    /**
     * Erstellt die Tabelle.
     * 
     * @param parent
     *            Das Eltern-Composite.
     */
    private void initTable(Composite parent) {
	table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

	table.setBackground(tableColor);
	table.setLinesVisible(true);
	table.setHeaderVisible(true);

	GridData tableGridData = new GridData();
	tableGridData.horizontalAlignment = GridData.CENTER;
	tableGridData.heightHint = 300;
	tableGridData.widthHint = shell.getClientArea().width - 55;
	table.setLayoutData(tableGridData);

	String[] titles = new String[0];
	if (doAction == SHOWTESTPOOL) {
	    String[] titlesInserts = { " id ", " Titel ", " Kategorie ",
		    " Autor ", " Beschreibung ", " Freigabe ",
		    " Fragenanzahl ", " Testaufbau ", " Punkte ",
		    " Bewertungssystem ", " Feedback " };
	    titles = titlesInserts;

	} else if (doAction == SHOWPRIVATETESTS) {
	    String[] titlesInserts = { " id ", " Titel ", " Kategorie ",
		    " Beschreibung ", " Freigabe ", " Fragenanzahl ",
		    " Testaufbau ", " Punkte ", " Bewertungssystem ",
		    " Feedback " };
	    titles = titlesInserts;
	}

	for (int i = 0; i < titles.length; i++) {

	    TableColumn column = new TableColumn(table, SWT.CENTER);
	    column.setText(titles[i]);

	    // Spalte mit id ausblenden
	    if (i == 0) {
		column.setWidth(0);
		column.setResizable(false);
	    }
	    if (i == 1) {
		column.setAlignment(SWT.LEFT);
		column.setWidth(200);
		column.setResizable(false);
	    }
	    if (i == 2) {
		column.setAlignment(SWT.LEFT);
		column.setWidth(50);
		column.setResizable(false);
	    }

	    if (doAction == SHOWPRIVATETESTS) {

		if (i == 3) {
		    column.setAlignment(SWT.LEFT);
		    column.setWidth(200);
		    column.setResizable(true);
		}
		if (i >= 5) {
		    column.setWidth(0);
		    column.setResizable(true);
		}
	    } else if (doAction == SHOWTESTPOOL) {
		if (i == 4) {
		    column.setAlignment(SWT.LEFT);
		    column.setWidth(200);
		    column.setResizable(true);
		}
		if (i >= 6) {
		    column.setWidth(0);
		    column.setResizable(true);
		}
	    }

	}

	LinkedList<Test> testList = new LinkedList<Test>();
	LinkedList<String> usernamesForTests = new LinkedList<String>();
	if (doAction == SHOWPRIVATETESTS) {
	    testList = iFace.getTests();
	    for (int i = 0; i < testList.size(); i++) {
		usernamesForTests.add("");
	    }
	} else if (doAction == SHOWTESTPOOL) {
	    testList = iFace.getPoolTestList();
	    usernamesForTests = iFace.getUserNamesForPoolTestList();

	}

	if (testList.size() == 0) {
	    TableItem item = new TableItem(table, SWT.NONE, 0);
	    item.setText(1, "Es wurde noch keinen Test erstellt!");
	    status = 0;

	} else {
	    status = 1;
	    for (int i = 0; i < testList.size(); i++) {
		insertTableItemValues(testList.get(i), i, usernamesForTests
			.get(i));
	    }
	}

	int x = 5;
	if (doAction == SHOWTESTPOOL)
	    x = 6;
	columnsPosStartDetails = x;

	for (int i = 1; i < columnsPosStartDetails; i++) {
	    table.getColumn(i).pack();
	}

	compositeShowTests.setSize(compositeShowTests.computeSize(SWT.DEFAULT,
		SWT.DEFAULT));
	compositeMain.layout();

    }

    /**
     * Erstellt eine neue Zeile.
     * 
     * @param test
     *            Der Test.
     * @param i
     *            Der Index.
     * @param username
     *            Der Name des Erstellers.
     * @return Die neue Zeile.
     */
    private TableItem insertTableItemValues(Test test, int i, String username) {

	TableItem item = new TableItem(table, SWT.NONE, i);
	int shifter = -1;
	if (doAction == SHOWPRIVATETESTS)
	    shifter = 0;
	else if (doAction == SHOWTESTPOOL)
	    shifter = 1;

	item.setText(0, String.valueOf(test.getIdTest()));
	item.setText(1, test.getTitle());
	item.setText(2, test.getCategory());
	if (doAction == SHOWTESTPOOL)
	    item.setText(3, username);
	String b = test.getDescription();
	if (b.length() > 100) {
	    b = b.substring(0, 100) + "...";
	}
	item.setText(3 + shifter, b);
	item.setText(4 + shifter, test.getPublicationStatusString());
	if (test.getIdTest() > 0) {
		item.setText(5 + shifter, String.valueOf(DbQueries.getNumberOfNodes(test)));
	}
	item.setText(6 + shifter, test.getTestTypeString());
	item.setText(7 + shifter, String.valueOf(test.getMaxPoints()));
	item.setText(8 + shifter, test.getEvaluationMethodString());
	item.setText(9 + shifter, test.getTimeOfFeedbackString());

	return item;

    }

    /**
     * Erstellt Buttons.
     * 
     * @param parent
     *            Das Eltern-Composite.
     * @param table
     *            Die Tabelle.
     */
    private void initButtons(Composite parent, final Table table) {

	// Basis-Composite
	Composite compositeButtons = new Composite(parent, SWT.NONE);
	compositeButtons.setLayout(new GridLayout(4, false));
	GridData buttonsGridData = new GridData();
	buttonsGridData.verticalAlignment = GridData.END;
	buttonsGridData.horizontalAlignment = GridData.CENTER;
	compositeButtons.setLayoutData(buttonsGridData);
	compositeButtons.setBackground(QuizGenerator.getColorBackground());

	// Test laden bzw. importieren
	Button buttonCreate = new Button(compositeButtons, SWT.PUSH);
	if (doAction == SHOWPRIVATETESTS)
	    buttonCreate.setText(BUTTONLOAD);
	else if (doAction == SHOWTESTPOOL)
	    buttonCreate.setText(BUTTONIMPORT);
	buttonCreate.setLayoutData(new GridData(SWT.BOTTOM));
	if (status == 0) {
	    buttonCreate.setEnabled(false);
	}

	// Listener
	buttonCreate.addSelectionListener(new SelectionAdapter() {

	    @Override
	    public void widgetSelected(SelectionEvent e) {

		TableItem[] result = table.getSelection();
		if (doAction == SHOWPRIVATETESTS) {
		    if (result.length > 0) {
//			iFace.loadTestWithTitel(result[0].getText(1));
		    } else {
			MessageBox errorDialog = new MessageBox(shell, SWT.OK);
			errorDialog
				.setMessage("Es wurde kein Test ausgewählt!");
			errorDialog.open();
		    }
		} else if (doAction == SHOWTESTPOOL) {
		    if (result.length > 0) {
			iFace.importTest(
				Integer.parseInt(result[0].getText(0)),
				result[0].getText(1));
		    } else {
			MessageBox errorDialog = new MessageBox(shell, SWT.OK);
			errorDialog
				.setMessage("Es wurde kein Test ausgewählt!");
			errorDialog.open();
		    }
		}

	    }
	});

	// Test laden bzw. importieren

	if (doAction == SHOWPRIVATETESTS) {
	    Button buttonExport = new Button(compositeButtons, SWT.PUSH);
	    buttonExport.setText(BUTTONEXPORT);
	    buttonExport.setLayoutData(new GridData(SWT.BOTTOM));

	    // Listener
	    buttonExport.addListener(SWT.Selection, new Listener() {

		public void handleEvent(Event event) {
		    DirectoryDialog directoryDialog = new DirectoryDialog(shell);

		    directoryDialog.setFilterPath(selectedDir);
		    directoryDialog
			    .setMessage("Please select a directory and click OK");

		    String dir = directoryDialog.open();
		    if (dir != null) {
			selectedDir = dir;

			TableItem[] result = table.getSelection();
			if (result.length > 0) {
			    lastSelectedTitleForExport = result[0].getText(1);
			    new Cursor(display, SWT.CURSOR_WAIT);
			    iFace.exportTest(selectedDir,
				    lastSelectedTitleForExport, "");

			} else {
			    MessageBox errorDialog = new MessageBox(shell,
				    SWT.OK);
			    errorDialog
				    .setMessage("Es wurde kein Test ausgewählt!");
			    errorDialog.open();
			}
		    }
		}
	    });

	}

	// abbrechen
	Button buttonCancel = new Button(compositeButtons, SWT.PUSH);
	buttonCancel.setText(BUTTONCANCEL);
	buttonCancel.setLayoutData(new GridData(SWT.BOTTOM));

	// Listener
	buttonCancel.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		iFace.cancel();
	    }
	});
    }

    /**
     * Oeffnet ein neues Dialog-Fenster und zeigt die uebergebene Nachricht an.
     * 
     * @param msg
     *            Die Nachricht.
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
	new Cursor(display, SWT.NONE);
	switch (setCase) {
	case LOADTEST:
	    compositeMain.dispose();
	    shell.layout();
	    break;
	case SHOWTESTPOOL:
	    doAction = SHOWTESTPOOL;
	    initShowTestsComposite(compositeMain);
	    compositeMain.layout();
	    shell.layout();
	    break;
	case RELOADPOOL:
	    doAction = SHOWTESTPOOL;
	    compositeShowTests.dispose();
	    initShowTestsComposite(compositeMain);
	    compositeMain.layout(true, true);
	    shell.layout();
	    break;
	case CANCEL:
	    compositeMain.dispose();
	    shell.layout();
	    break;
	case TEST_EXPORTED:
	    doMsgDialog(iFace.getMsg());
	    break;
	case TEST_EXPORTED_ERR_WR:
	    doMsgDialog(iFace.getMsg());
	    break;
	case TEST_EXPORTED_ERR_EX:
	    InputDialogExport dialog = new InputDialogExport(shell);
	    String newExportTitle = dialog.open();
	    if (newExportTitle.length() > 0) {
		iFace.exportTest(selectedDir, lastSelectedTitleForExport,
			newExportTitle);
	    }
	    break;
	case CLOSE:
	    compositeMain.dispose();
	    shell.layout();
	    break;

	}

    }
}
