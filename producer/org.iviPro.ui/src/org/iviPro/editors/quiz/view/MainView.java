package org.iviPro.editors.quiz.view;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.editors.quiz.std.MainModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;

/**
 * Diese Klasse implementiert die grafische Ausgabe des Hauptmenues.
 * 
 * @author Sabine Gatterman
 * 
 */
public class MainView implements Observer {

	// Status-Codes Observer-Pattern
	private static final int UPDATEMENU = 0;
	private static final int UPDATE = 1;
	private static final int CLOSE = 4;

	private MainModel iFace;
	private Shell shell;

	// Basis-Composite fuer Welcome-Menue
	private Composite compositeMain;

	// Menue-Leiste
	private Menu mainMenuField;
	// Menue 1 (Datei)
	private MenuItem fileMenuItem;
	private MenuItem newFileMenuItem;
	private MenuItem openFileMenuItem;
	private MenuItem exportFileMenuItem;
	private MenuItem saveFileMenuItem;
	private MenuItem profilMenuItem;
	private MenuItem exitMenuItem;
	// Menue 3 (Hilfe)
	private MenuItem openHelpMenuItem;
	private MenuItem helpMenuItem;
	private MenuItem aboutMenuItem;

	private Composite compositeWelcome;

	// Beschriftungen Menue 1 (Datei)
	private static final String M1 = " Datei ";
	private static final String M1NEW = "Test erstellen";
	private static final String M1OPEN = "Eigene Tests";
	private static final String M1EXPORT = "Test exportieren";
	private static final String M1SAVE = "Speichern && Schließen";
	private static final String M1PROFIL = "Profil bearbeiten";
	private static final String M1EXIT = "Beenden";
	// Beschriftungen Menue 2 (Hilfe)
	private static final String M3 = " ? ";
	private static final String M3HELP = "Hilfe anzeigen";
	private static final String M3INFO = "Info";

	// Button-Beschriftungen Welcome-Menue
	private static final String NEW_TEST = "Test erstellen";
	private static final String LOAD_TEST = "Eigene Tests";
	private static final String OPEN_POOL = "Test-Pool";
	private static final String EXIT = "Beenden";

	// Pfad zum Hintergrundbild
	private static final String PICTURE = "Systemfiles"
			+ System.getProperty("file.separator") + "cloud.gif";

	// Button-Groesse Welcome-Menue
	private static final int BUTTONHEIGTHHINT = 40;
	private static final int BUTTONWIDTHHINT = 150;

	/**
	 * Konstruktor
	 * 
	 * @param shell
	 *            Das Hauptfenster.
	 * @param iFace
	 *            Die aufrufende Model-Klasse.
	 */
	public MainView(Shell shell, MainModel iFace) {
		this.shell = shell;
		this.iFace = iFace;

		shell.setBackground(QuizGenerator.getColorBackground());
		iFace.addObserver(this);
		runGUI();
	}

	/**
	 * Startet den Aufbau der View.
	 */
	private void runGUI() {
		shell.setLayout(new GridLayout());

		// Feld fuer Menue-Leiste
		mainMenuField = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mainMenuField);

		// Menue-Leiste
		initFileMenu();
		initHelpMenu();

		// Basis-Composite
		compositeMain = new Composite(shell, SWT.NONE);
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.horizontalSpacing = 10;
		compositeMain.setLayout(mainLayout);
		GridData mainGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		compositeMain.setLayoutData(mainGridData);
		compositeMain.setBackground(QuizGenerator.getColorBackground());

		// Welcome-Menue
		initWelcomeComposite();

	}

	/**
	 * Erstellt das Menue 'Datei'.
	 */
	private void initFileMenu() {

		fileMenuItem = new MenuItem(mainMenuField, SWT.CASCADE);
		fileMenuItem.setText(M1);

		Menu fileMenu = new Menu(fileMenuItem);

		newFileMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		newFileMenuItem.setText(M1NEW);
		newFileMenuItem.addSelectionListener(sl);
		if (iFace.getTestModelInstance() != null
				|| iFace.getNodeModelInstance() != null)
			newFileMenuItem.setEnabled(false);
		else
			newFileMenuItem.setEnabled(true);

		openFileMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		openFileMenuItem.setText(M1OPEN);
		openFileMenuItem.addSelectionListener(sl);
		if (iFace.getTestModelInstance() != null
				|| iFace.getNodeModelInstance() != null
				|| iFace.getTestManagementModelInstance() != null)
			openFileMenuItem.setEnabled(false);
		else
			openFileMenuItem.setEnabled(true);

		new MenuItem(fileMenu, SWT.SEPARATOR);

		exportFileMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		exportFileMenuItem.setText(M1EXPORT);
		exportFileMenuItem.addSelectionListener(sl);
		if (iFace.getNodeModelInstance() != null)
			exportFileMenuItem.setEnabled(true);
		else
			exportFileMenuItem.setEnabled(false);

		new MenuItem(fileMenu, SWT.SEPARATOR);

		saveFileMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		saveFileMenuItem.setText(M1SAVE);
		saveFileMenuItem.setEnabled(false);
		saveFileMenuItem.addSelectionListener(sl);
		if (iFace.getNodeModelInstance() != null)
			saveFileMenuItem.setEnabled(true);
		else
			saveFileMenuItem.setEnabled(false);

		new MenuItem(fileMenu, SWT.SEPARATOR);

		profilMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		profilMenuItem.setText(M1PROFIL);
		profilMenuItem.addSelectionListener(sl);

		if (iFace.getTestModelInstance() != null
				|| iFace.getNodeModelInstance() != null)
			profilMenuItem.setEnabled(false);
		else
			profilMenuItem.setEnabled(true);

		new MenuItem(fileMenu, SWT.SEPARATOR);

		exitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		exitMenuItem.setText(M1EXIT);
		exitMenuItem.addSelectionListener(sl);

		if (iFace.getTestModelInstance() != null
				|| iFace.getNodeModelInstance() != null
				|| iFace.getTestManagementModelInstance() != null)
			exitMenuItem.setEnabled(false);
		else
			exitMenuItem.setEnabled(true);

		fileMenuItem.setMenu(fileMenu);

	}

	/**
	 * Erstellt das Menue 'Hilfe'.
	 */
	private void initHelpMenu() {

		helpMenuItem = new MenuItem(mainMenuField, SWT.CASCADE);
		helpMenuItem.setText(M3);

		Menu helpMenu = new Menu(helpMenuItem);

		openHelpMenuItem = new MenuItem(helpMenu, SWT.PUSH);
		openHelpMenuItem.setText(M3HELP);
		openHelpMenuItem.addSelectionListener(sl);

		new MenuItem(helpMenu, SWT.SEPARATOR);

		aboutMenuItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutMenuItem.setText(M3INFO);
		aboutMenuItem.addSelectionListener(sl);

		helpMenuItem.setMenu(helpMenu);

	}

	private void updateMenu() {
		fileMenuItem.dispose();
		helpMenuItem.dispose();
		initFileMenu();
		initHelpMenu();
	}

	/**
	 * Erstellt das Welcome-Menue.
	 */
	private void initWelcomeComposite() {

		shell.setText("Multiple Choice Editor");

		// Composite fuer Hintergrundbild
		Composite background = new Composite(compositeMain, SWT.NONE);
		background.setLayout(new GridLayout());
		GridData backgroundGridData = new GridData(SWT.CENTER, SWT.CENTER,
				true, true);
		backgroundGridData.heightHint = 600;
		backgroundGridData.widthHint = 800;
		background.setLayoutData(backgroundGridData);
		// Hintergrundbild setzen
		Image image = new Image(shell.getDisplay(), PICTURE);
		background.setBackgroundImage(image);

		// Composite fuer Buttons
		compositeWelcome = new Composite(background, SWT.NONE);
		GridLayout welcomeLayout = new GridLayout(1, false);
		welcomeLayout.verticalSpacing = 20;
		compositeWelcome.setLayout(welcomeLayout);
		compositeWelcome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				true, true));
		compositeWelcome.setBackground(QuizGenerator.getColorBackground());

		// Button: neuer Test
		Button buttonNewTest = new Button(compositeWelcome, SWT.PUSH);
		buttonNewTest.setText(NEW_TEST);
		GridData newTestGridData = new GridData();
		newTestGridData.horizontalAlignment = SWT.CENTER;
		newTestGridData.heightHint = BUTTONHEIGTHHINT;
		newTestGridData.widthHint = BUTTONWIDTHHINT;
		buttonNewTest.setLayoutData(newTestGridData);
		buttonNewTest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				iFace.newTest(QuizGenerator.a);

			}

		});

		// Button: Test öffnen
		Button buttonOpenTest = new Button(compositeWelcome, SWT.PUSH);
		buttonOpenTest.setText(LOAD_TEST);
		GridData loadTestGridData = new GridData();
		loadTestGridData.horizontalAlignment = SWT.CENTER;
		loadTestGridData.heightHint = BUTTONHEIGTHHINT;
		loadTestGridData.widthHint = BUTTONWIDTHHINT;
		buttonOpenTest.setLayoutData(loadTestGridData);
		buttonOpenTest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.openOwenTests();
			}

		});

		// Button: Pool öffnen
		Button buttonOpenPool = new Button(compositeWelcome, SWT.PUSH);
		buttonOpenPool.setText(OPEN_POOL);
		GridData openPoolGridData = new GridData();
		openPoolGridData.horizontalAlignment = SWT.CENTER;
		openPoolGridData.heightHint = BUTTONHEIGTHHINT;
		openPoolGridData.widthHint = BUTTONWIDTHHINT;
		buttonOpenPool.setLayoutData(openPoolGridData);
		buttonOpenPool.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.openTestPool();
			}

		});

		// Button: Beenden
		Button buttonExit = new Button(compositeWelcome, SWT.PUSH);
		buttonExit.setText(EXIT);
		GridData exitGridData = new GridData();
		exitGridData.horizontalAlignment = SWT.CENTER;
		exitGridData.heightHint = BUTTONHEIGTHHINT;
		exitGridData.widthHint = BUTTONWIDTHHINT;
		buttonExit.setLayoutData(exitGridData);
		buttonExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.exit();
			}

		});

	}

	/*
	 * Listener fuer die Menue-Leiste
	 */
	SelectionListener sl = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {

		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {

			// Menu1 --> Datei
			if (arg0.widget.equals(newFileMenuItem)) {
//				iFace.newTest();
			}
			if (arg0.widget.equals(openFileMenuItem)) {
				iFace.openOwenTests();
			}
			if (arg0.widget.equals(saveFileMenuItem)) {
				iFace.getNodeModelInstance().exit();
			}
			if (arg0.widget.equals(exportFileMenuItem)) {
				iFace.getNodeModelInstance().exportTestDialog();
			}
			if (arg0.widget.equals(profilMenuItem)) {

				iFace.openProfil();
			}
			if (arg0.widget.equals(exitMenuItem)) {
				iFace.exit();
			}

			// Menu3 --> Hilfe
			if (arg0.widget.equals(openHelpMenuItem)) {
				iFace.openHelp();
			}
			if (arg0.widget.equals(aboutMenuItem)) {
				MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION
						| SWT.OK);
				box.setMessage(iFace.getInfoMsg());
				box.setText(shell.getText() + " - Info");
				box.open();
			}

		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg1) {

		int setCase = Integer.parseInt(arg1.toString());

		switch (setCase) {
		case UPDATEMENU:
			updateMenu();
			shell.layout();
			break;
		case UPDATE:
			compositeMain.dispose();
			shell.layout();
			break;
		case CLOSE:
			compositeMain.dispose();
			mainMenuField.dispose();
			shell.layout();
			break;
		}
	}

}
