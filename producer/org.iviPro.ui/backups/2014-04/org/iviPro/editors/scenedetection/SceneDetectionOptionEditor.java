package org.iviPro.editors.scenedetection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.actions.undoable.ShotDetectionAction;
import org.iviPro.model.Video;
import org.iviPro.scenedetection.sd_main.AlgorithmSettings;

public class SceneDetectionOptionEditor {

	private Shell shell;

	private final IWorkbenchWindow window;

	private Video video;

	private Button parallelButton;

	private Button gradualButton;

	private Button mpeg7Button;

	private Button oneSidedFeature;

	private Button aggregateShortShots;

	private Combo videoSetting;

	private Combo keyframeChoosing;

	private Combo clusteringMechanism;

	public SceneDetectionOptionEditor(IWorkbenchWindow window, Video video) {
		this.window = window;
		this.video = video;
	}

	public void show() {

		// Create Window
		Display display = Display.getCurrent();
		Shell parentShell = display.getActiveShell();
		shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Scene Detection Options");

		// Windowsize
		Rectangle bounds = new Rectangle(0, 0, 380, 540);
		Rectangle parentBounds = parentShell.getBounds();
		int x = parentBounds.x + (parentBounds.width - bounds.width) / 2;
		int y = parentBounds.y + (parentBounds.height - bounds.height) / 2;
		bounds.x = x;
		bounds.y = y;
		shell.setBounds(bounds);

		// Layout
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.verticalSpacing = 10;
		shell.setLayout(layout);

		// Options Group
		final Group checkBoxes = new Group(shell, SWT.CENTER);
		GridData gd = new GridData();
		checkBoxes.setText("Options");
		GridLayout grid = new GridLayout(1, false);
		grid.marginLeft = 90;
		checkBoxes.setLayout(grid);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		checkBoxes.setLayoutData(gd);

		Composite comp = new Composite(checkBoxes, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		parallelButton = new Button(comp, SWT.CHECK);
		parallelButton.setText("Parallele Verarbeitung");
		parallelButton.setSelection(true);
		parallelButton.setLayoutData(gd);

		comp = new Composite(checkBoxes, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		gradualButton = new Button(comp, SWT.CHECK);
		gradualButton.setText("Zeitliche Übergänge");
		gradualButton.setSelection(true);
		gradualButton.setLayoutData(gd);

		comp = new Composite(checkBoxes, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		mpeg7Button = new Button(comp, SWT.CHECK);
		mpeg7Button.setText("MPEG7 Export");
		mpeg7Button.setSelection(true);
		mpeg7Button.setLayoutData(gd);

		// Expert Group
		final Group checkBoxesExpert = new Group(shell, SWT.CENTER);
		gd = new GridData();
		checkBoxesExpert.setText("Expert Options");
		grid = new GridLayout(1, false);
		grid.marginLeft = 90;
		checkBoxesExpert.setLayout(grid);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		checkBoxesExpert.setLayoutData(gd);

		comp = new Composite(checkBoxesExpert, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		oneSidedFeature = new Button(comp, SWT.CHECK);
		oneSidedFeature.setText("One Sided Feature");
		oneSidedFeature.setSelection(true);
		oneSidedFeature.setLayoutData(gd);

		comp = new Composite(checkBoxesExpert, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		aggregateShortShots = new Button(comp, SWT.CHECK);
		aggregateShortShots.setText("Aggregate short shots");
		aggregateShortShots.setSelection(true);
		gd = new GridData();
		aggregateShortShots.setLayoutData(gd);

		comp = new Composite(checkBoxesExpert, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		Label videoSettingText = new Label(comp, SWT.NONE);
		videoSettingText.setText("Video Eigenschaften:");
		videoSetting = new Combo(comp, SWT.NONE);
		videoSetting.add("Smooth", 0);
		videoSetting.add("Rough", 1);
		videoSetting.select(0);
		videoSetting.setLayoutData(gd);

		comp = new Composite(checkBoxesExpert, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		Label keyframeChoosingText = new Label(comp, SWT.NONE);
		keyframeChoosingText.setText("Keyframe Algorithmus:");
		keyframeChoosing = new Combo(comp, SWT.NONE);
		keyframeChoosing.add("PME", 0);
		keyframeChoosing.add("Easy", 1);
		keyframeChoosing.select(0);
		keyframeChoosing.setLayoutData(gd);

		comp = new Composite(checkBoxesExpert, SWT.CENTER);
		grid = new GridLayout(1, false);
		grid.marginTop = 5;
		comp.setLayout(grid);
		Label clusteringText = new Label(comp, SWT.NONE);
		clusteringText.setText("Clustering Threshold:");
		clusteringMechanism = new Combo(comp, SWT.NONE);
		clusteringMechanism.add("Adaptiv", 0);
		clusteringMechanism.add("Fix", 1);
		clusteringMechanism.select(0);
		clusteringMechanism.setLayoutData(gd);

		// BUTTONS
		Composite buttonPanel = new Composite(shell, SWT.NONE);
		buttonPanel.setLayout(new GridLayout(3, true));

		Button okButton = new Button(buttonPanel, SWT.PUSH);
		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ShotDetectionAction action = new ShotDetectionAction(window,
						video);
				action.setParallel(parallelButton.getSelection());
				action.setGradual(gradualButton.getSelection());
				action.setMpeg7(mpeg7Button.getSelection());
				AlgorithmSettings.getClassInstance().setOneSided(
						oneSidedFeature.getSelection());
				AlgorithmSettings.getClassInstance().setSmoothSettings(
						videoSetting.getText());
				AlgorithmSettings.getClassInstance().setKeyframePME(
						keyframeChoosing.getText());
				AlgorithmSettings.getClassInstance().setClusteringAdaptive(
						clusteringMechanism.getText());
				AlgorithmSettings.getClassInstance().setShortShots(
						aggregateShortShots.getSelection());

				shell.close();
				action.run();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		okButton.setText("Start");
		gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd.widthHint = 80;
		okButton.setLayoutData(gd);

		Button cancelButton = new Button(buttonPanel, SWT.CANCEL);
		cancelButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(gd);
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		gd.horizontalSpan = 4;
		buttonPanel.setLayoutData(gd);

		shell.setDefaultButton(okButton);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}
}
