package org.iviPro.editors.quiz.view;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.quiz.interfaces.LoginInterface;
import org.iviPro.editors.quiz.std.LoginModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;

/**
 * Diese Klasse implementiert die grafische Ausgabe des Login-Menues.
 * 
 * @author Sabine Gattermann
 * 
 */
public class LoginView implements Observer {

	// Status-Codes Observer-Pattern
	private static final int LOGIN = 1;
	private static final int PASS_ERROR = 2;
	private static final int NO_USER_ERROR = 3;
	private static final int DB_CONNECTION_ERROR = 4;

	// Beschriftungen
	private static final String LABELUSERNAME = "Benutzername:";
	private static final String LABELPASSWORD = "Passwort:";
	private static final String BUTTONLOGIN = "Login";
	private static final String BUTTONREGISTER = "Profil anlegen";

	// Echo-Char fuer Passworteigabe
	private static final char ECHO = '*';

	// Breite fuer Texteigabefelder
	private static final int FIELDWIDTH = 150;

	// Pfad zum Hintergrundbild
	private static final String PICTURE = "Systemfiles"
			+ System.getProperty("file.separator") + "cloud.gif";

	private Shell shell;
	private LoginInterface iFace;

	// Texteingabefelder
	private Text userName;
	private Text password;

	// Basis-Composite
	private Composite compositeMain;

	/**
	 * Konstruktor
	 * 
	 * @param shell
	 *            Das Hauptfenster.
	 * @param iFace
	 *            Die aufrufende Model-Klasse.
	 */
	public LoginView(Shell shell, LoginModel iFace) {

		this.shell = shell;
		this.iFace = iFace;
		iFace.addObserver(this);
		runLoginView();
	}

	/**
	 * Startet den Aufbau der View.
	 */
	private void runLoginView() {

		shell.setLayout(new GridLayout());

		// Basis-Composite
		compositeMain = new Composite(shell, SWT.NONE);
		GridLayout mainLayout = new GridLayout(2, false);
		mainLayout.horizontalSpacing = 10;
		compositeMain.setLayout(mainLayout);
		GridData mainGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		compositeMain.setLayoutData(mainGridData);
		compositeMain.setBackground(QuizGenerator.getColorBackground());

		// Composite fuer Hintergrundbild
		Composite background = new Composite(compositeMain, SWT.NONE);
		background.setLayout(new GridLayout());
		GridData backgroundGridData = new GridData(SWT.CENTER, SWT.CENTER,
				true, true);
		backgroundGridData.heightHint = 600;
		backgroundGridData.widthHint = 800;
		background.setLayoutData(backgroundGridData);
		// Hintergrundbild setzen
		// Image image = new Image(shell.getDisplay(), PICTURE);
		// background.setBackgroundImage(image);

		// Composite fuer Buttons, Felder und Labels
		Composite compositeLogin = new Composite(background, SWT.NONE);
		compositeLogin.setLayout(new GridLayout(2, false));
		compositeLogin.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				true));
		compositeLogin.setBackground(QuizGenerator.getColorBackground());

		// Labels und Feldern
		initInputFields(compositeLogin);
		// Buttons
		initButtons(compositeLogin);
	}

	/**
	 * Erstellt Labels und Texteingabefelder.
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 */
	private void initInputFields(Composite parent) {

		// Basis-Composite
		Composite compositeFields = new Composite(parent, SWT.NONE);
		compositeFields.setLayout(new GridLayout(2, false));
		compositeFields.setBackground(QuizGenerator.getColorBackground());

		// Username
		Label labelUserName = new Label(compositeFields, SWT.SHADOW_IN);
		labelUserName.setText(LABELUSERNAME);
		labelUserName.setBackground(QuizGenerator.getColorBackground());

		userName = new Text(compositeFields, SWT.BORDER);
		GridData userNameGridData = new GridData();
		userNameGridData.widthHint = FIELDWIDTH;
		userName.setLayoutData(userNameGridData);
		userName.setFocus();

		// Passwort
		Label labelPassword = new Label(compositeFields, SWT.SHADOW_IN);
		labelPassword.setText(LABELPASSWORD);
		labelPassword.setBackground(QuizGenerator.getColorBackground());

		password = new Text(compositeFields, SWT.BORDER);
		password.setEchoChar(ECHO);
		GridData passwordGridData = new GridData();
		passwordGridData.widthHint = FIELDWIDTH;
		password.setLayoutData(passwordGridData);
		// Listerner fuer Enter-Taste bei Login
		password.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent arg0) {
				// not used

			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				// 13 == Enter/Return Taste
				if (arg0.keyCode == 13) {
					iFace.setUserLogin(userName.getText(), password.getText());
				}

			}
		});

	}

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
		compositeButtons.setLayoutData(buttonsGridData);
		compositeButtons.setBackground(QuizGenerator.getColorBackground());

		// Login
		Button buttonLogin = new Button(compositeButtons, SWT.PUSH);
		buttonLogin.setText(BUTTONLOGIN);
		buttonLogin.setLayoutData(new GridData(SWT.BOTTOM));

		// Listener
		buttonLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.setUserLogin(userName.getText(), password.getText());
			}
		});

		// Profil anlegen
		Button buttonRegister = new Button(compositeButtons, SWT.PUSH);
		buttonRegister.setText(BUTTONREGISTER);
		buttonRegister.setLayoutData(new GridData(SWT.BOTTOM));

		// Listener
		buttonRegister.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iFace.openRegistration();
				compositeMain.dispose();
				shell.layout();
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
		MessageBox wrongLogin = new MessageBox(shell, SWT.OK);
		wrongLogin.setMessage(msg);
		wrongLogin.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		int setCase = Integer.parseInt(arg1.toString());

		switch (setCase) {
		case LOGIN:
			iFace.openMainApplication();
			compositeMain.dispose();
			shell.layout();
			break;
		case PASS_ERROR:
			doMsgDialog(iFace.getMsg());
			password.setText("");
			password.setFocus();
			break;
		case NO_USER_ERROR:
			doMsgDialog(iFace.getMsg());
			userName.setText("");
			password.setText("");
			userName.setFocus();
			break;
		case DB_CONNECTION_ERROR:
			doMsgDialog(iFace.getMsg());
			break;
		}

	}

}
