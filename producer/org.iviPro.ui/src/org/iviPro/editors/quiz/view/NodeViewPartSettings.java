package org.iviPro.editors.quiz.view;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;

/**
 * Diese Klasse ist für die Einstellungen
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class NodeViewPartSettings {

	private static Composite parent;

	private static Composite compositeLinks;

	private static ExpandItem item;

	private static NodeModel iFace;

	private static Color highlightColor;

	private static NodeViewPartQuestion ques;

	private static Text titleText;

	private static Combo feedbackCombo;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public NodeViewPartSettings(Composite parent, NodeModel iFace,
			NodeViewPartQuestion ques) {
		NodeViewPartSettings.parent = parent;
		NodeViewPartSettings.iFace = iFace;
		NodeViewPartSettings.ques = ques;
		highlightColor = new Color(parent.getDisplay(), 211, 211, 211);
	}

	public static void open(boolean expandIt) {

		// Basis-Composite
		compositeLinks = new Composite(parent, SWT.NONE);
		GridLayout linkLayout = new GridLayout(3, false);
		compositeLinks.setLayout(linkLayout);
		GridData linkGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		linkGridData.minimumWidth = 500;
		compositeLinks.setLayoutData(linkGridData);
		compositeLinks.setBackground(highlightColor);

		// expand item für aktions-buttons
		// anmerkung: 0 gibt hier position im fenster an.
		ExpandItem itemAction = new ExpandItem((ExpandBar) parent, SWT.NONE, 0);
		itemAction.setText("Aktionen");
		itemAction.setHeight(40);

		Composite actionComposite = new Composite(parent, SWT.NONE);
		actionComposite.setLayout(new GridLayout(2, false));
		actionComposite.setLayoutData(new GridData(SWT.RIGHT));
		actionComposite.setOrientation(SWT.RIGHT_TO_LEFT);
		actionComposite.setBackground(highlightColor);

		// cancel button
		Button buttonCancel = new Button(actionComposite, SWT.PUSH);
		buttonCancel.setText("Cancel");
		buttonCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(iFace.getTest().isDefault()){
					QuizGenerator.getNodeQuizEditor().onDelete(iFace.getTest().getIdTest());
				}
				QuizGenerator.getDefaultShell().dispose();
			}
		});

		// save and close button
		Button buttonSave = new Button(actionComposite, SWT.PUSH);
		buttonSave.setText("Save and Close");
		buttonSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// speichert und schliesst den quizeditor. selbes verhalten wie
				// beim x-button des fensters, da selbes SWT-Event
				QuizGenerator.getDefaultShell().close();
			}
		});

		itemAction.setControl(actionComposite);
		itemAction.setExpanded(expandIt);

		// erstellt Maske
		Composite compositePoints = new Composite(compositeLinks, SWT.NONE);
		compositePoints.setLayout(new GridLayout(6, false));
		compositePoints.setLayoutData(new GridData(SWT.LEFT));
		compositePoints.setBackground(highlightColor);

		// Label: Punkte -- ausgerichtet nach Combo
		Label labelPoints = new Label(compositePoints, SWT.NONE);
		labelPoints.setText("Max. Punkte: ");
		labelPoints.setBackground(highlightColor);

		final Text maxPoints = new Text(compositePoints, SWT.NONE);
		maxPoints.setTextLimit(1);
		maxPoints.setText(String.valueOf(iFace.getMaxPoints()));
		maxPoints.setTabs(1);
		GridData gridData = new GridData(25, 16);
		maxPoints.setLayoutData(gridData);
		maxPoints.addVerifyListener(new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				String string = e.text;
				try {
					int number = Integer.valueOf(string);
					iFace.setAmountPoints(number);
					ques.updateComboPoints(number);
				} catch (NumberFormatException ex) {
					e.doit = false;
					MessageDialog.openError(parent.getShell(), "Eingabefehler",
							"Bitte geben sie eine Zahl zwischen 1 und 9 ein!");
				}
			}
		});

		// Label: Titel -- ausgerichtet nach Combo
		Label labelTitle = new Label(compositePoints, SWT.NONE);
		GridData data = new GridData();
		data.horizontalIndent = 15;
		labelTitle.setLayoutData(data);
		labelTitle.setText("Titel: ");
		labelTitle.setBackground(highlightColor);

		GridData gDataTitleText = new GridData(130, 16);
		titleText = new Text(compositePoints, SWT.NONE);
		titleText.setLayoutData(gDataTitleText);
		titleText.setText(iFace.getTestTitle());
		titleText.setTextLimit(20);

		// Label: Zeitpunkt der Auswertung -- ausgerichtet nach Combo
		Label feedbackTitle = new Label(compositePoints, SWT.NONE);
		feedbackTitle.setText("Zeitpunkt der Auswertung: ");
		feedbackTitle.setBackground(highlightColor);
		data = new GridData();
		data.horizontalIndent = 15;
		feedbackTitle.setLayoutData(data);

		feedbackCombo = new Combo(compositePoints, SWT.DROP_DOWN | SWT.BORDER
				| SWT.READ_ONLY);

		feedbackCombo.add("Ende des Tests");
		feedbackCombo.add("Nach jeder Frage");
		// feedbackCombo.setText("Nach jeder Frage");
		feedbackCombo.select(iFace.getFeedback());

		feedbackCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				iFace.setFeedback(feedbackCombo.getSelectionIndex());
			}
		});

		// anmerkung: 1 gibt hier position im fenster an.
		item = new ExpandItem((ExpandBar) parent, SWT.NONE, 1);

		item.setText("Testeinstellungen");
		item.setHeight(compositeLinks.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(compositeLinks);
		item.setData("position", "0");
		item.setExpanded(expandIt);
		parent.layout();
	}

	public static String getTitle() {
		return titleText.getText();
	}

	public static int getFeedbackValue() {
		return feedbackCombo.getSelectionIndex();
	}
}
