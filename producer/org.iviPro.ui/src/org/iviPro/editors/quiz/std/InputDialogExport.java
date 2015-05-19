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
 * Klasse zum Anzeigen eines Eingabedialogs beim Exportieren eines Tests.
 * 
 * @author Sabine Gattermann
 * 
 */
public class InputDialogExport extends Dialog {
    String value;

    /**
     * Konstruktor
     * 
     * @param parent
     *            Die Shell.
     */
    public InputDialogExport(Shell parent) {
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
    public InputDialogExport(Shell parent, int style) {
	super(parent, style);
    }

    /**
     * Oeffnet das Dialogfenster.
     * 
     * @return Der Name fuer den Export.
     */
    public String open() {
	Shell parent = getParent();
	final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER
		| SWT.APPLICATION_MODAL);

	shell.setLayout(new GridLayout(3, false));

	Label label = new Label(shell, SWT.NULL);
	label
		.setText(" Sie haben bereits einen Test mit diesem Titel exportiert! ");
	label.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
		true, true, 3, 2));

	Label l2 = new Label(shell, SWT.NULL);
	l2.setText(" neuer Export-Titel: ");

	final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
	GridData textGridData = new GridData();
	textGridData.widthHint = 200;
	text.setLayoutData(textGridData);

	final Button buttonOK = new Button(shell, SWT.PUSH);
	buttonOK.setText(" Exportieren ");
	buttonOK
		.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

	Button buttonCancel = new Button(shell, SWT.PUSH);
	buttonCancel.setText(" abbrechen ");
	GridData cancelGridData = new GridData();
	cancelGridData.horizontalSpan = 2;
	cancelGridData.grabExcessHorizontalSpace = true;
	buttonCancel.setLayoutData(cancelGridData);

	final Button buttonOverwrite = new Button(shell, SWT.PUSH);
	buttonOverwrite.setText(" bestehenden Test überschreiben ");
	GridData overwriteGridData = new GridData();
	overwriteGridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
	overwriteGridData.grabExcessHorizontalSpace = true;
	buttonOverwrite.setLayoutData(overwriteGridData);
	buttonOverwrite.addListener(SWT.Selection, new Listener() {
	    public void handleEvent(Event event) {
		value = ":dooverwrite:";
		shell.dispose();
	    }
	});

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
