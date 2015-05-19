package org.iviPro.editors.quiz.view;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.editors.quiz.std.ZestEdge;
import org.iviPro.editors.quiz.std.ZestNode;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;

/**
 * Diese Klasse implementiert die graphische Darstellung des Zest-Graphen (als
 * ExpandBarItem).
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeViewPartZest {

	private static final int UPDATEANDLOAD = 22;
	private static Composite parent;
	private static NodeModel iFace;
	private static Graph graph;
	private static LinkedList<ZestNode> zestNodeList;
	private static String lastSelectedNode;
	private static String lastSelectedEdge;
	private static int width;
	private static int height;
	private static int counterForResizing = 6;
	private static ScrollBar scrollBarHorizontal;
	private static ScrollBar scrollBarVertical;
	private static ZestNode curr;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public NodeViewPartZest(Composite parent, NodeModel iFace) {
		NodeViewPartZest.parent = parent;
		NodeViewPartZest.iFace = iFace;
		// berechnet die groesse des graphen entsprechend der knotenanzahl
		width = 400;
		height = 400;
		int addVertical = 0;
		int addHorizontal = 0;
		counterForResizing = iFace.getNumberOfNodesInTest();
		if (counterForResizing > 5) {
			int moreVertical = counterForResizing - 5;
			addVertical = 35 * moreVertical;
			if (NodeViewPartZest.iFace.getInsertAlgorithm() == 1) {
				int moreHorizontal = (counterForResizing - 5) / 5;
				addHorizontal = 35 * moreHorizontal;
			}
			width = width + addHorizontal;
			height = height + addVertical;
		}
	}

	/**
	 * Oeffnet die graphische Ausgabe in einer ExpandBar.
	 * 
	 * @param expandIt
	 *            Der Indikator (true/false) ob ExpandBarItem expandiert ist.
	 */
	public static void open(boolean expandIt) {
		Composite basisComposite = new Composite(parent, SWT.NONE);

		FillLayout basisCompositeLayout = new FillLayout(SWT.HORIZONTAL);

		basisComposite.setLayout(basisCompositeLayout);
		GridData basisGridData = new GridData(GridData.CENTER, GridData.CENTER,
				false, true);
		basisComposite.setLayoutData(basisGridData);
		basisComposite.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WHITE));

		// scrollendes Composite
		ScrolledComposite scrolledComposite = new ScrolledComposite(
				basisComposite, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setAlwaysShowScrollBars(false);
		scrolledComposite.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		scrolledComposite.setLayout(new GridLayout(1, false));

		scrollBarHorizontal = scrolledComposite.getHorizontalBar();
		scrollBarVertical = scrolledComposite.getVerticalBar();

		scrollBarHorizontal.setIncrement(30);
		scrollBarVertical.setIncrement(30);

		scrollBarHorizontal.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// Speichere Position
				NodeView.savedScrollPosHorizontal = scrollBarHorizontal
						.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		scrollBarVertical.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// Speichere Position
				NodeView.savedScrollPosVertical = scrollBarVertical
						.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		// Graph
		graph = new Graph(scrolledComposite, SWT.NONE);

		GridData graphGridData = new GridData();
		graphGridData.horizontalAlignment = GridData.END;
		graphGridData.grabExcessHorizontalSpace = true;

		graph.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);

		graph.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				/*
				 * wenn aktueller knoten nochmals angeklickt wird, lade ihn
				 * NICHT neu! dadurch kann er dann auch per mouse verschoben
				 * werden.
				 */

				// klick auf hintergrund filtern
				if ((((Graph) e.widget).getSelection()).size() > 0) {
					if ((((Graph) e.widget).getSelection().get(0)).getClass()
							.equals(ZestEdge.class)) {
						// neue kante laden
						if (!(String.valueOf((((Graph) e.widget).getSelection()
								.get(0))).equals(String
								.valueOf(lastSelectedEdge)))) {
							lastSelectedEdge = String
									.valueOf(((Graph) e.widget).getSelection()
											.get(0));
							update(e);
						}
					} else {
						if (!(String.valueOf((((Graph) e.widget).getSelection()
								.get(0))).equals(lastSelectedNode))) {
							// neuen knoten laden
							if (iFace.checkTestValidToChange(
									NodeViewPartQuestion.getQuestionText(),
									NodeViewPartQuestion.getPointsOfQuestion(),
									NodeViewPartQuestion
											.getAnswersFromTextFields(),
									NodeViewPartQuestion
											.getAnswerCheckboxIsSelected(),
									NodeViewPartQuestion.getRandom(),
									NodeViewPartLinks.getLinksFromTextFields(),
									NodeViewPartMedia.getVideoFromTextFields(),
									NodeViewPartMedia.getAudioFromTextFields(),
									NodeViewPartMedia.getImageFromTextFields())) {
								lastSelectedNode = String
										.valueOf(((Graph) e.widget)
												.getSelection().get(0));
								update(e);
							}
						}
					}
				}
			}

			/**
			 * 
			 * @param e
			 *            Das SelectionEvent.
			 */
			@SuppressWarnings("unchecked")
			private void update(SelectionEvent e) {

				// knoten

				// klick auf knoten oder kante
				if (((((Graph) e.widget).getSelection()).get(0)).getClass()
						.equals(ZestNode.class)) {

					List<ZestEdge> connections = ((ZestNode) ((((Graph) e.widget)
							.getSelection().get(0)))).getTargetConnections();

					if (connections.size() > 0) {
						ZestEdge con = connections.get(0);
						Event event = new Event();
						event.type = SWT.Selection;
						event.keyCode = 0;
						event.widget = con;
						con.notifyListeners(SWT.Selection, event);
					} else {
						iFace.setEdgeNumbersForGui(-1, -1, null, false);

					}

					// speichere daten des vorherigen knotens
					iFace.updateDbNode(NodeViewPartQuestion.getQuestionText(),
							NodeViewPartQuestion.getPointsOfQuestion(),
							NodeViewPartQuestion.getAnswersFromTextFields(),
							NodeViewPartQuestion.getAnswerCheckboxIsSelected(),
							NodeViewPartQuestion.getRandom(),
							NodeViewPartLinks.getLinksFromTextFields(),
							NodeViewPartMedia.getVideoFromTextFields(),
							NodeViewPartMedia.getAudioFromTextFields(),
							NodeViewPartMedia.getImageFromTextFields(),
							UPDATEANDLOAD);

					// lade aktuellen knoten
					iFace.initDbNode(((ZestNode) (((Graph) e.widget)
							.getSelection()).get(0)).getNode().getIdNode());

				} else if (((((Graph) e.widget).getSelection()).get(0))
						.getClass().equals(ZestEdge.class)) {

					List<ZestEdge> connections = (graph.getConnections());

					for (int i = 0; i < connections.size(); i++) {
						if (!connections.get(i).equals(e.widget))
							connections.get(i).unhighlight();
					}

					Event event = new Event();
					event.type = SWT.Selection;
					event.keyCode = 1;
					event.widget = (ZestEdge) (((Graph) e.widget)
							.getSelection()).get(0);
					((ZestEdge) (((Graph) e.widget).getSelection()).get(0))
							.notifyListeners(SWT.Selection, event);
				}
			}

		});

		// extra Thread um Graphen aufzubauen, sonst GUI-Problem...
		(new Runnable() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						insertGraphNodesEdges(parent);
					}
				});

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

			}
		}).run();

		graph.setFocus();
		basisComposite.setSize(350, 300);
		scrolledComposite.setSize(350, 300);

		/*
		 * Scrollbar positionieren: falls der aktuelle knoten gesetzt ist,
		 * scrolle an dessen position, sonst scrolle an letzte bekannte (in der
		 * NodeView-Hauptklasse gespeicherte) Position.
		 */
		if (curr != null) {
			scrollBarHorizontal.setSelection(curr.getLocation().x);
			scrollBarVertical.setSelection(curr.getLocation().y);
		} else {
			scrollBarHorizontal.setSelection(NodeView.savedScrollPosHorizontal);
			scrollBarVertical.setSelection(NodeView.savedScrollPosVertical);

		}

		// zusaetliche Scrollbars des Graphen falls sie benoetigt werden
		graph.setHorizontalScrollBarVisibility(FigureCanvas.AUTOMATIC);
		graph.setVerticalScrollBarVisibility(FigureCanvas.AUTOMATIC);

		// falls Graph kleiner als Parent --> vergroessere Graph
		if (width < graph.getParent().getSize().x)
			width = graph.getParent().getSize().x;
		graph.setSize(width, height);

		graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);

		scrolledComposite.setContent(graph);

		graph.layout(true);
		scrolledComposite.layout(true);
		basisComposite.layout(true);

		ExpandItem item = new ExpandItem((ExpandBar) parent, SWT.NONE, 0);
		item.setText("Testaufbau");
		item.setHeight(300);
		item.setControl(basisComposite);
		item.setData("position", "0");
		item.setExpanded(expandIt);
	}

	/**
	 * Erstellt Verzweigungs-Graph der einzelnen Fragen.
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 */
	private static void insertGraphNodesEdges(Composite parent) {
		zestNodeList = new LinkedList<ZestNode>();
		LinkedList<Node> nodes = iFace.getNodeList();
		LinkedList<Edge> edges = iFace.getEdgeList();
		LinkedList<Integer> idNodeList = new LinkedList<Integer>();

		graph.setRedraw(false);

		curr = null;
		GraphItem[] toSel = new GraphItem[1];
		for (int i = 0; i < nodes.size(); i++) {
			ZestNode zestNode = new ZestNode(graph, SWT.NONE, nodes.get(i));
			zestNodeList.add(zestNode);
			idNodeList.add(nodes.get(i).getIdNode());
			if (iFace.getCurrentNode().getIdNode() == nodes.get(i).getIdNode()) {
				zestNode.highlight();
				curr = zestNode;
				toSel[0] = zestNode;
			}
		}

		LinkedList<GraphConnection> zestEdgeList = new LinkedList<GraphConnection>();

		for (int i = 0; i < edges.size(); i++) {
			int fromNode = edges.get(i).getIdNodeSource();
			int toNode = edges.get(i).getIdNodeDestination();

			int x = idNodeList.indexOf(fromNode);
			int y = idNodeList.indexOf(toNode);

			ZestEdge zestEdge = new ZestEdge(graph,
					ZestStyles.CONNECTIONS_DIRECTED, zestNodeList.get(x),
					zestNodeList.get(y), edges.get(i),
					iFace.getInsertAlgorithm());

			zestEdgeList.add(zestEdge);

			zestEdge.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {
					// kante
					boolean layoutOnlyEdge = false;
					if (e.keyCode == 1 && iFace.getInsertAlgorithm() == 1) {
						layoutOnlyEdge = true;
					}
					((ZestEdge) (e.widget)).highlight();
					// alle werte an model-klasse
					iFace.setEdgeNumbersForGui(
							((ZestNode) ((ZestEdge) e.widget).getSource())
									.getNode().getPosition(),
							((ZestNode) ((ZestEdge) e.widget).getDestination())
									.getNode().getPosition(),
							((ZestEdge) e.widget).getEdge(), layoutOnlyEdge);
				}
			});

			if (iFace.getCurrentNode().getIdNode() == edges.get(i)
					.getIdNodeDestination()) {
				Event e = new Event();
				e.item = zestEdge;
				e.type = SWT.Selection;
				e.keyCode = 0;
				zestEdge.notifyListeners(SWT.Selection, e);

			}
		}

		graph.setSelection(toSel);
		graph.setRedraw(true);

		graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		graph.setSize(width, height);
		graph.layout();

	}

}
