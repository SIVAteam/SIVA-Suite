package org.iviPro.editors.sceneeditor.components.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.editors.sceneeditor.components.Messages;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.utils.SivaTime;

public class CustomPopup {

	private static Shell popShell = null;
	private static Display display = null;
	private static Scene scene = null;
	
	private static CustomPopup instance = null; 
		
	private static Runnable timer = null;
	
	// gibt an wo das Popup plaziert werden soll
	private static int locationY = 0;
	
	private CustomPopup() {
		display = Display.getCurrent();
		
		// erstelle ein Runnable, dass die Shell schließt
		timer = new Runnable() {
			public void run() {
				disposePop();
			}
		};
	}
	
	public static synchronized CustomPopup getInstance(Scene cscene, int y) {
		scene = cscene;
		locationY = y;
		if (instance == null) {
			instance = new CustomPopup();
		}		
		instance.initPopUp();
		return instance;
	}

	public static void disposePop() {
		if (popShell != null) {
			if (!popShell.isDisposed()) {
				popShell.close();
				popShell.dispose();
			}
		}
	}

	/**
	 * setze das Layout des Dialogs
	 */
	private void setLayout() {
		GridLayout gridLayout = new GridLayout(1, false);
		popShell.setLayout(gridLayout);
	}
	
	/**
	 * initiiert das PopUp, wird jedes mal aufgerufen wenn eine Instanz angefordert wird
	 */
	private void initPopUp() {
		if (popShell != null) {
			disposePop();
		}		
		
		// verhindere, dass der alte Timer noch ausgeführt wird.
		display.timerExec(-1, timer);
		
		Point p = display.getCursorLocation();
		popShell = new Shell(display, SWT.BORDER);
		setLayout();
		createContent();						
		popShell.pack();
		popShell.layout();
		popShell.setLocation(p.x, p.y - locationY);
		popShell.open();
				
		// setze den Timer auf 10 Sekunden
		display.timerExec(10000, timer);		
	}

	/**
	 * erstelle den Content des Dialogs
	 * 
	 * @param shell
	 */
	private void createContent() {
		Composite labels = new Composite(popShell, SWT.LEFT);
		GridLayout labelsLayout = new GridLayout(2, false);
		labels.setLayout(labelsLayout);
		
		Label nameLabel = new Label(labels, SWT.LEFT);
		GridData nameLabelGD = new GridData();
		nameLabelGD.horizontalSpan = 2;
		nameLabel.setLayoutData(nameLabelGD);
		nameLabel.setText(Messages.CustomPopup_Label_Scene + scene.getTitle());

		Video vid = scene.getVideo();
		
		new SceneSEImage(popShell, SWT.CENTER, scene.getStart(), scene.getEnd(), vid, 100, 80, scene);
		
		Label startLabel = new Label(labels, SWT.LEFT);
		startLabel.setText(Messages.CustomPopup_Label_StartTime + SivaTime.getTimeString(scene.getStart()));
		Label endLabel = new Label(labels, SWT.LEFT);
		endLabel.setText(Messages.CustomPopup_Label_EndTime + SivaTime.getTimeString(scene.getEnd()));
		GridData gridDatSN = new GridData();
		gridDatSN.widthHint = 220;
	}
}
