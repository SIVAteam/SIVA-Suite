package org.iviPro.editors.quiz.std;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Klasse zum Anzeigen eines Eingabedialogs beim Importieren eines Tests.
 * 
 * @author Sabine Gattermann
 * 
 */
public class InputDialogImport extends Dialog {
    String value;

    /**
     * Konstruktor
     * 
     * @param parent
     *            Die Shell.
     */
    public InputDialogImport(Shell parent) {
	super(parent);
    }

    /**
     * Konstruktor
     * 
     * @param parent
     *            Die Shell.
     * @param style
     *            Der Style.
     */
    public InputDialogImport(Shell parent, int style) {
	super(parent, style);
    }

    /**
     * Offnet das Dialogfenster.
     * 
     * @return Der Name fuer den Import.
     */
    public String open() {
	Shell parent = getParent();
	final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER
		| SWT.APPLICATION_MODAL);

	shell.setLayout(new GridLayout(2, false));

	Label label = new Label(shell, SWT.NULL);
	label
		.setText(" Sie haben bereits einen Test mit diesem Titel gespeichert.\n Bitte geben Sie einen anderen Titel an: ");

	final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
	GridData textGridData = new GridData();
	textGridData.widthHint = 200;
	text.setLayoutData(textGridData);

	final Button buttonOK = new Button(shell, SWT.PUSH);
	buttonOK.setText(" Importieren ");
	buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	Button buttonCancel = new Button(shell, SWT.PUSH);
	buttonCancel.setText(" abbrechen ");

	text.addListener(SWT.Modify, new Listener() {
	    public void handleEvent(Event event) {
		try {
		    value = text.getText();
		    buttonOK.setEnabled(true);
		} catch (Exception e) {
		    buttonOK.setEnabled(false);
		}
	    }
	});

	buttonOK.addListener(SWT.Selection, new Listener() {
	    public void handleEvent(Event event) {
		shell.dispose();
	    }
	});

	buttonCancel.addListener(SWT.Selection, new Listener() {
	    public void handleEvent(Event event) {
		value = "";
		shell.dispose();
	    }
	});

	shell.addListener(SWT.Traverse, new Listener() {
	    public void handleEvent(Event event) {
		if (event.detail == SWT.TRAVERSE_ESCAPE)
		    event.doit = false;
	    }
	});

	text.setText("");
	shell.pack();
	shell.open();

	Display display = parent.getDisplay();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch())
		display.sleep();
	}

	return value;
    }

}
