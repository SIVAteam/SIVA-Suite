package org.iviPro.editors.quiz.view;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.quiz.interfaces.RegistationInterface;
import org.iviPro.editors.quiz.std.RegistrationModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.model.quiz.User;

/**
 * Diese Klasse implementiert die grafische Oberflaeche der Registrierung eines
 * neuen Benutzers bzw. des Aenderns von Profilinformationen.
 * 
 * @author Sabine Gattermann
 * 
 */
public class RegistrationView implements Observer {

    // Status-Codes Observer-Pattern

    // nicht alle Felder ausgefüllt
    private static final int FIELDS = 1;
    // Benutzername bereits vergeben
    private static final int USER = 2;
    // Passwortwiederholung falsch
    private static final int PASS = 3;
    // keine gültige Emailadresse
    private static final int MAIL = 4;
    // alles korrekt ausgefüllt
    private static final int SUCCESS = 5;
    // Passwort zu kurz
    private static final int PASSLENGTH = 6;
    // Warnung beim Loeschen des Profils
    private static final int WARNING = 7;

    // Beschriftungen
    private static final String LABELUSERNAME = "Benutzername:";
    private static final String LABELPASSWORD = "Passwort:";
    private static final String LABELPASSWORDAGAIN = "Passwort Wiederholung:";
    private static final String LABELVORNAME = "Vorname:";
    private static final String LABELLASTNAME = "Nachname:";
    private static final String LABELEMAIL = "Email:";
    private static final String BUTTONCREATE = "Profil anlegen";
    private static final String BUTTONCANCEL = "abbrechen";
    private static final String BUTTONSAVE = "Speichern";
    private static final String BUTTONDELETEPROFIL = "Profil löschen";

    // Echo-Char fuer Passworteigabe
    private static final char ECHO = '*';

    // Breite fuer Texteigabefelder
    private static final int FIELDWIDTH = 150;

    // Pfad zum Hintergrundbild
    private static final String PICTURE = "Systemfiles"
	    + System.getProperty("file.separator") + "cloud.gif";

    private Shell shell;
    private RegistationInterface iFace;

    // Texteingabefelder
    private Text userName;
    private Text password;

    // Basis-Composite
    private Composite compositeMain;
    private Text passwordAgain;
    private Text firstName;
    private Text lastName;
    private Text email;
    private Composite compositeLogin;

    /**
     * Konstruktor
     * 
     * @param shell
     *            Das Hauptfenster.
     * @param iFace
     *            Die aufrufende Model-Klasse.
     */
    public RegistrationView(Shell shell, RegistrationModel iFace) {

	this.shell = shell;
	this.iFace = iFace;
	iFace.addObserver(this);
	runRegistrationView();
    }

    /**
     * Startet den Aufbau der View.
     */
    private void runRegistrationView() {

	shell.setLayout(new GridLayout());

	// Basis-Composite
	compositeMain = new Composite(shell, SWT.NONE);
	GridLayout mainLayout = new GridLayout(1, false);
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
	Image image = new Image(shell.getDisplay(), PICTURE);
	background.setBackgroundImage(image);

	// Composite fuer Buttons, Felder und Labels
	compositeLogin = new Composite(background, SWT.NONE);
	compositeLogin.setLayout(new GridLayout(2, false));
	compositeLogin.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
		true));
	compositeLogin.setBackground(QuizGenerator.getColorBackground());

	// Labels und Felder
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
	if (iFace.getIsNewUser())
	    userName.setFocus();

	// Passwort
	Label labelPassword = new Label(compositeFields, SWT.SHADOW_IN);
	labelPassword.setText(LABELPASSWORD);
	labelPassword.setBackground(QuizGenerator.getColorBackground());

	password = new Text(compositeFields, SWT.BORDER);
	password.setEchoChar(ECHO);
	password.setTextLimit(12);
	GridData passwordGridData = new GridData();
	passwordGridData.widthHint = FIELDWIDTH;
	password.setLayoutData(passwordGridData);

	// Passwort Wiederholung
	Label labelPasswordAgain = new Label(compositeFields, SWT.SHADOW_IN);
	labelPasswordAgain.setText(LABELPASSWORDAGAIN);
	labelPasswordAgain.setBackground(QuizGenerator.getColorBackground());

	passwordAgain = new Text(compositeFields, SWT.BORDER);
	passwordAgain.setEchoChar(ECHO);
	passwordAgain.setTextLimit(12);
	GridData passwordAgainGridData = new GridData();
	passwordAgainGridData.widthHint = FIELDWIDTH;
	passwordAgain.setLayoutData(passwordAgainGridData);

	// Vorname
	Label labelVorname = new Label(compositeFields, SWT.SHADOW_IN);
	labelVorname.setText(LABELVORNAME);
	labelVorname.setBackground(QuizGenerator.getColorBackground());

	firstName = new Text(compositeFields, SWT.BORDER);
	GridData vornameGridData = new GridData();
	vornameGridData.widthHint = FIELDWIDTH;
	firstName.setLayoutData(vornameGridData);

	// Nachname
	Label labelNachname = new Label(compositeFields, SWT.SHADOW_IN);
	labelNachname.setText(LABELLASTNAME);
	labelNachname.setBackground(QuizGenerator.getColorBackground());

	lastName = new Text(compositeFields, SWT.BORDER);
	GridData nachnameGridData = new GridData();
	nachnameGridData.widthHint = FIELDWIDTH;
	lastName.setLayoutData(nachnameGridData);

	// Email
	Label labelEmail = new Label(compositeFields, SWT.SHADOW_IN);
	labelEmail.setText(LABELEMAIL);
	labelEmail.setBackground(QuizGenerator.getColorBackground());

	email = new Text(compositeFields, SWT.BORDER);
	GridData emailGridData = new GridData();
	emailGridData.widthHint = FIELDWIDTH;
	email.setLayoutData(emailGridData);

	// Aendern von Profildaten
	if (!iFace.getIsNewUser()) {
	    User user = iFace.getUserData();

	    userName.setText(user.getUserName());
	    userName.setEditable(false);

	    password.setText(user.getPassword());

	    passwordAgain.setText(user.getPassword());

	    firstName.setText(user.getFirstName());

	    lastName.setText(user.getLastName());

	    email.setText(user.getEmail());

	}
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

	// Profil erstellen
	Button buttonCreate = new Button(compositeButtons, SWT.PUSH);

	if (iFace.getIsNewUser())
	    buttonCreate.setText(BUTTONCREATE);
	else
	    buttonCreate.setText(BUTTONSAVE);
	buttonCreate.setLayoutData(new GridData(SWT.BOTTOM));

	// Listener: Profil erstellen
	buttonCreate.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		iFace.saveUser(userName.getText(), password.getText(),
			passwordAgain.getText(), firstName.getText(), lastName
				.getText(), email.getText());
	    }
	});

	// abbrechen
	Button buttonCancel = new Button(compositeButtons, SWT.PUSH);
	buttonCancel.setText(BUTTONCANCEL);
	buttonCancel.setLayoutData(new GridData(SWT.BOTTOM));

	// Listener: abbrechen
	buttonCancel.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		iFace.cancel();
		compositeMain.dispose();
		shell.layout();
	    }
	});

	if (!iFace.getIsNewUser()) {
	    // profil loeschen
	    Button buttonDeleteProfil = new Button(compositeLogin, SWT.PUSH);
	    buttonDeleteProfil.setText(BUTTONDELETEPROFIL);
	    GridData delGridData = new GridData();
	    delGridData.horizontalAlignment = SWT.BEGINNING;
	    delGridData.verticalAlignment = SWT.BOTTOM;
	    buttonDeleteProfil.setLayoutData(delGridData);

	    // Listener: profil loeschen
	    buttonDeleteProfil.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
		    iFace.warningDeleteProfil();
		}
	    });
	}
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

    /**
     * Oeffnet ein neues Dialog-Fenster und zeigt die uebergebene Nachricht an.
     * 
     * @param msg
     *            Die Nachricht.
     */
    private void doWarningDialog(String msg) {
	MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES
		| SWT.NO);
	box
		.setMessage("Profil und private Tests werden endgültig gelöscht!\n\n Löschvorgang fortsertzen?\n\n\n");
	if (box.open() == SWT.YES) {
	    iFace.deleteProfil();
	    compositeMain.dispose();
	    shell.layout();
	}

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
	case FIELDS:
	    doMsgDialog(iFace.getUIMsg());
	    break;
	case USER:
	    doMsgDialog(iFace.getUIMsg());
	    userName.setText("");
	    userName.setFocus();
	    break;
	case PASS:
	    doMsgDialog(iFace.getUIMsg());
	    password.setText("");
	    passwordAgain.setText("");
	    password.setFocus();
	    break;
	case MAIL:
	    doMsgDialog(iFace.getUIMsg());
	    break;
	case SUCCESS:
	    if (iFace.getIsNewUser())
		doMsgDialog(iFace.getUIMsg());
	    iFace.cancel();
	    compositeMain.dispose();
	    shell.layout();
	    break;
	case PASSLENGTH:
	    doMsgDialog(iFace.getUIMsg());
	    break;
	case WARNING:
	    doWarningDialog(iFace.getUIMsg());
	    break;
	}
    }

}
