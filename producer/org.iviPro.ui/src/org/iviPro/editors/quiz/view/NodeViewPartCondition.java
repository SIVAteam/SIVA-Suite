package org.iviPro.editors.quiz.view;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.editors.quiz.std.NodeModel;

/**
 * Diese Klasse implementiert die graphische Darstellung und Verwaltung von
 * Kanten und Bedingungen.
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeViewPartCondition {

	private static Composite parent;
	private static NodeModel iFace;
	private static Composite compositeEdge;
	private static Combo comboEdgeSource;
	private static String[] indexEdgeSource;
	private static Combo comboEdgeDestination;
	private static int indexEdgeDestinationSize;
	private static String[] indexEdgeDestination;
	private static Combo comboEdgeLookback;
	private static String[] indexEdgeLookback;
	private static Combo comboEdgePoints;
	private static String[] indexEdgePounts;
	private static Button buttonSaveEdge;
	private static LinkedList<String> values;
	private static Color highlightColor;
	private static ExpandItem item;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public NodeViewPartCondition(Composite parent, NodeModel iFace) {
		NodeViewPartCondition.parent = parent;
		NodeViewPartCondition.iFace = iFace;
		highlightColor = new Color(parent.getDisplay(), 211, 211, 211);
		item = new ExpandItem((ExpandBar) parent, SWT.NONE, 2);
	}

	/**
	 * Oeffnet die graphische Ausgabe.
	 */
	public static void open(boolean expandIt) {

		compositeEdge = new Composite(parent, SWT.NONE);
		GridLayout row1Layout = new GridLayout(4, false);
		compositeEdge.setLayout(row1Layout);
		compositeEdge.setBackground(highlightColor);
		GridData row1GridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1);
		compositeEdge.setLayoutData(row1GridData);

		GridData singleLabelGridData = new GridData();
		singleLabelGridData.horizontalSpan = 4;
		singleLabelGridData.horizontalAlignment = SWT.BEGINNING;

		// Kante
		Label labelF1 = new Label(compositeEdge, SWT.NONE);
		labelF1.setText(" Kante: ");
		labelF1.setBackground(highlightColor);
		labelF1.setLayoutData(singleLabelGridData);

		Label labelF5 = new Label(compositeEdge, SWT.NONE);
		labelF5.setText(" von ");
		labelF5.setBackground(highlightColor);

		comboEdgeSource = new Combo(compositeEdge, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		int indexEdgeSourceSize = iFace.getNodeList().size();
		indexEdgeSource = new String[indexEdgeSourceSize];

		@SuppressWarnings("unused")
		// used in for-loop
		String selectedInComboEdgeSource = "";
		values = iFace.getEdgeNumbersForGui();
		for (int i = 0; i < indexEdgeSourceSize; i++) {
			indexEdgeSource[i] = "Frage " + (1 + i);
			comboEdgeSource.add(indexEdgeSource[i]);
			if (values.get(0).equals(String.valueOf(i))) {
				comboEdgeSource.select(i);
				selectedInComboEdgeSource = comboEdgeSource.getItem(i)
						.toString();
			}
		}

		Label labelF2 = new Label(compositeEdge, SWT.CENTER);
		labelF2.setText(" zu ");
		labelF2.setBackground(highlightColor);

		comboEdgeDestination = new Combo(compositeEdge, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		indexEdgeDestinationSize = iFace.getNodeList().size();
		indexEdgeDestination = new String[indexEdgeDestinationSize];

		for (int i = 0; i < indexEdgeDestinationSize; i++) {
			indexEdgeDestination[i] = "Frage " + (1 + i);
			comboEdgeDestination.add(indexEdgeDestination[i]);

			if (values.get(1).equals(String.valueOf(i))) {
				comboEdgeDestination.select(i);
			}

		}

		comboEdgeSource.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String selectedSource = comboEdgeSource.getItem(
						comboEdgeSource.getSelectionIndex()).toString();
				comboEdgeDestination.removeAll();
				for (int i = 0; i < indexEdgeDestinationSize; i++) {

					indexEdgeDestination[i] = "Frage " + (1 + i);
					if (!selectedSource.equals(indexEdgeDestination[i]))
						comboEdgeDestination.add(indexEdgeDestination[i]);

				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		comboEdgeDestination.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				String source = comboEdgeSource.getItem(
						comboEdgeSource.getSelectionIndex()).toString();
				String destination = comboEdgeDestination.getItem(
						comboEdgeDestination.getSelectionIndex()).toString();
				source = source.replace("Frage ", "");
				destination = destination.replace("Frage ", "");

				if (source.length() > 0 && destination.length() > 0) {

					String[] lookback = iFace.getPossibleLookback(Integer
							.parseInt(source) - 1);

					comboEdgeLookback.removeAll();
					comboEdgePoints.removeAll();
					comboEdgeLookback.setItems(lookback);

					LinkedList<String> existingEdge = iFace
							.checkForExistingEdge(Integer.parseInt(source) - 1,
									Integer.parseInt(destination) - 1);
					if (existingEdge.size() > 0) {

						comboEdgeLookback.select(comboEdgeLookback
								.indexOf(existingEdge.get(0)));
						Event event = new Event();
						event.type = SWT.Selection;
						event.widget = comboEdgeLookback;
						comboEdgeLookback.notifyListeners(SWT.Selection, event);
						comboEdgePoints.select(comboEdgePoints
								.indexOf(existingEdge.get(1)));
					} else {

						comboEdgeLookback.select(0);
						Event event = new Event();
						event.type = SWT.Selection;
						event.widget = comboEdgeLookback;
						comboEdgeLookback.notifyListeners(SWT.Selection, event);
					}
					comboEdgeLookback.layout();
					comboEdgePoints.layout();

				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// Bedingung
		Label labelB1 = new Label(compositeEdge, SWT.NONE);
		labelB1.setText(" Bedingung: ");
		labelB1.setBackground(highlightColor);
		labelB1.setLayoutData(singleLabelGridData);

		Label labelB7 = new Label(compositeEdge, SWT.NONE);
		labelB7.setText(" Lookback: ");
		labelB7.setBackground(highlightColor);

		comboEdgeLookback = new Combo(compositeEdge, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		if (comboEdgeSource.getSelectionIndex() >= 0) {
			indexEdgeLookback = iFace.getLookbackForConditionField(Integer
					.parseInt(comboEdgeDestination.getItem(
							comboEdgeDestination.getSelectionIndex()).replace(
							"Frage ", "")) - 1);
		} else {
			indexEdgeLookback = new String[0];

		}

		for (int i = 0; i < indexEdgeLookback.length; i++) {
			comboEdgeLookback.add(indexEdgeLookback[i]);
		}

		comboEdgeLookback.select(comboEdgeLookback.indexOf(String.valueOf(iFace
				.getCurrentCondition().getConditionLookback())));

		if (comboEdgeLookback.getSelectionIndex() == -1)
			comboEdgeLookback.select(0);

		if (comboEdgeLookback.getSelectionIndex() >= 0) {
			Event event = new Event();
			event.type = SWT.Selection;
			event.widget = comboEdgeLookback;
			comboEdgeLookback.notifyListeners(SWT.Selection, event);
		}

		Label labelB3 = new Label(compositeEdge, SWT.CENTER);
		labelB3.setText(" Punkte: ");
		labelB3.setBackground(highlightColor);

		comboEdgePoints = new Combo(compositeEdge, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		if (comboEdgeLookback.getItemCount() > 0
				&& (!comboEdgeLookback.getItem(0).toString().equals(""))) {
			iFace.setLookback(Integer.parseInt(comboEdgeLookback
					.getItem(comboEdgeLookback.getSelectionIndex())));
			comboEdgePoints.removeAll();
		}

		indexEdgePounts = iFace.getPointsForConditionField();
		if (indexEdgePounts.length > 0
				&& indexEdgePounts[0].replace(" ", "").length() > 0) {

			for (int i = 0; i < indexEdgePounts.length; i++) {
				comboEdgePoints.add(indexEdgePounts[i]);
			}

			int select = iFace.getCurrentCondition().getConditionPoints();
			if (select == -1) {
				comboEdgePoints.select(0);
			} else {
				comboEdgePoints.select(comboEdgePoints.indexOf(String
						.valueOf(select)));
			}

		} else {
			comboEdgePoints.setEnabled(false);
		}

		comboEdgeLookback.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int selected = Integer.parseInt(comboEdgeLookback
						.getItem(comboEdgeLookback.getSelectionIndex()));

				int q = Integer.parseInt(comboEdgeSource.getItem(
						comboEdgeSource.getSelectionIndex()).replace("Frage ",
						"")) - 1;
				int z = Integer.parseInt(comboEdgeDestination.getItem(
						comboEdgeDestination.getSelectionIndex()).replace(
						"Frage ", "")) - 1;

				String[] points = iFace.getPossiblePoints(q, z, selected);
				if (points.length > 0
						&& points[0].replace(" ", "").length() > 0) {
					comboEdgePoints.setItems(points);
					comboEdgePoints.select(0);
					comboEdgePoints.setEnabled(true);
					buttonSaveEdge.setEnabled(true);
					comboEdgePoints.layout();
				} else {
					String[] s = new String[1];
					s[0] = "";
					comboEdgePoints.setItems(s);
					comboEdgePoints.setEnabled(false);
					buttonSaveEdge.setEnabled(false);
					comboEdgePoints.layout();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		comboEdgePoints.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		if (iFace.getInsertAlgorithm() == 1) {

			buttonSaveEdge = new Button(compositeEdge, SWT.PUSH);
			buttonSaveEdge.setText(" Speichern ");
			buttonSaveEdge.setEnabled(comboEdgePoints.getEnabled());
			buttonSaveEdge.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {
					if (comboEdgeDestination.getSelectionIndex() != -1
							&& comboEdgeSource.getSelectionIndex() != -1
							&& comboEdgeLookback.getSelectionIndex() != -1
							&& comboEdgePoints.getSelectionIndex() != -1) {
						String destination = comboEdgeDestination.getItem(
								comboEdgeDestination.getSelectionIndex())
								.toString();
						destination = destination.replace("Frage", "");
						destination = destination.replace(" ", "");
						String comboFromCondition = comboEdgePoints
								.getItem(comboEdgePoints.getSelectionIndex())
								.toString().replace(" ", "");
						int condition = 0;
						if (comboFromCondition.length() > 0) {

							if (comboFromCondition.startsWith("D")) {
								condition = -1;
							} else {
								condition = Integer
										.parseInt(comboFromCondition);
							}
							iFace.addNewEdge(
									comboEdgeSource.getSelectionIndex(),
									Integer.parseInt(destination) - 1,
									Integer.parseInt(comboEdgeLookback
											.getItem(
													comboEdgeLookback
															.getSelectionIndex())
											.toString().replace(" ", "")),
									condition);
						} else {
							// do nothing
						}
					}
				}
			});

			Button buttonDel = new Button(compositeEdge, SWT.PUSH);
			buttonDel.setText(" Löschen ");
			buttonDel.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {

					iFace.deleteEdge(comboEdgeSource.getSelectionIndex(),
							comboEdgeDestination.getSelectionIndex());

				}
			});
		} else if (iFace.getInsertAlgorithm() == 0) {
			Button b = new Button(compositeEdge, SWT.PUSH);
			b.setText(" Bedingung speichern ");
			// falls vorher nichts ausgewaehlt werden kann, kann nichts
			// gespeichert werden
			b.setEnabled(comboEdgePoints.getEnabled());
			b.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {

					iFace.updateEdge(comboEdgeSource.getSelectionIndex(),
							comboEdgeDestination.getSelectionIndex(),
							comboEdgeLookback.getSelectionIndex(),
							comboEdgePoints.getSelectionIndex());

				}
			});
		}
		compositeEdge.setSize(compositeEdge.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		item.setText("Pfade:");
		item.setHeight(compositeEdge.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(compositeEdge);
		item.setData("position", "3");
		item.setExpanded(expandIt);
	}

	/**
	 * Baut das gesamte Composite (EdgeView) neu auf.
	 */
	public static void refresh(boolean expandIt) {
		compositeEdge.dispose();
		open(expandIt);
		compositeEdge.layout();
		item.getParent().layout();
	}

	/**
	 * Fuehrt ein View-Update der Bedingungspunkte-ComboBox durch.
	 */
	public static void conditionUpdate() {
		indexEdgePounts = iFace.getPointsForConditionField();
		comboEdgePoints.removeAll();
		for (int i = 0; i < indexEdgePounts.length; i++) {
			comboEdgePoints.add(indexEdgePounts[i]);
		}
		comboEdgePoints.layout();
		compositeEdge.layout();
		item.getParent().layout();
	}
}
