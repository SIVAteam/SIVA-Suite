package org.iviPro.editors.annotationeditor.components.overview;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.application.Application;
import org.iviPro.editors.annotationeditor.components.Messages;
import org.iviPro.editors.common.BeanComparator;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.listeners.GraphNodeEventConsumer;
import org.iviPro.listeners.GraphNodeListener;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.theme.Colors;
import org.iviPro.utils.SivaTime;

/*
 * zeichnet alle definierten Szenen des Videos Nanosekundengenau in einer Leiste,
 * es werden sowohl verwendete wie auch nicht verwendete Szenen gezeichnet.
 */
public class AnnotationOverview extends Composite implements
		GraphNodeEventConsumer, ISelectionProvider, SivaEventConsumerI {

	private Composite annoHolder = null;
	private ScrolledComposite scrollComp = null;
	private NodeScene nodeScene;

	// minimale Höhe des Inhalts
	private int minContentScrollCompHeight = 290;

	// die Breite des Inhalts der Scroll-Komponente
	private int widthContentScrollComp = 0;

	// aktuelle Selektion
	private StructuredSelection selection = null;

	// Liste der Listener, hören auf Selektionen
	private LinkedList<ISelectionChangedListener> listeners = new LinkedList<ISelectionChangedListener>();

	// Liste der Annotationen die angezeigt werden sollen
	private List<INodeAnnotation> annotations;

	// gibt die aktuelle Sortierung an
	private BeanComparator bComparator = BeanComparator.getDefault();

	// Listener fuer Graphen und Szenen-Knoten
	private GraphNodeListener graphNodeListener;
	
	// der Movie Player
	private MediaPlayer mp;

	public AnnotationOverview(Composite parent, int style, NodeScene nodeS,
			int width, MediaPlayer mp) {
		super(parent, style);
		this.widthContentScrollComp = width;
		this.nodeScene = nodeS;
		this.mp = mp;
		
		// höre auf den Player
		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (annoHolder != null && !annoHolder.isDisposed()) {
					annoHolder.redraw();
					annoHolder.update();
				}
			}			
		});
		
		annotations = new LinkedList<INodeAnnotation>();

		setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		GridLayout grid = new GridLayout(1, false);
		setLayout(grid);

		scrollComp = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL);
		scrollComp.setAlwaysShowScrollBars(true);
		scrollComp.setBackground(Colors.VIDEO_OVERVIEW_BG.getColor());
		GridData scrollGrid = new GridData(SWT.FILL, SWT.FILL, false, true);
		scrollGrid.widthHint = width;
		scrollGrid.heightHint = minContentScrollCompHeight;
		scrollComp.setLayoutData(scrollGrid);
		
		scrollComp.addListener(SWT.Activate, new Listener() {
	        public void handleEvent(Event e) {
	        	scrollComp.setFocus();
	        }
	    });
		scrollComp.getVerticalBar().setIncrement(10);

		// Listener auf Szenen-Knoten initialisieren
		Graph graph = Application.getCurrentProject().getSceneGraph();
		graphNodeListener = new GraphNodeListener(this);
		graphNodeListener.startListening(nodeScene, graph);
		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// Listener auf Szenen-Knoten beenden
				graphNodeListener.stopListening();
			}
		});
		updateView();
	}

	@SuppressWarnings("unchecked")
	public void updateView() {

		// Stelle ab der die Skalendaten gezeichnet werden
		final int startScale = 20;

		// Höhe ab wann die Annotationen eingezeichnet werden sollen
		final int startAnnotations = 30;

		if (this.isDisposed()) {
			return;
		}

		annotations.clear();
		// füge alle INodeAnnotationLeafs (Audio, Picture) und Mark Annotationen hinzu
		annotations.addAll((List) nodeScene.getChildren(INodeAnnotationLeaf.class));
		annotations.addAll((List) nodeScene.getChildren(NodeMark.class));

		// berechne die neue Höhe des Scrollinhalts abhängig von der Anzahl der
		// Annotationen
		int newHeight = startAnnotations + 4 + annotations.size() * 22;
		if (newHeight < minContentScrollCompHeight) {
			newHeight = minContentScrollCompHeight;
		}
		final int newHeightFin = newHeight;

		if (annoHolder != null) {
			if (!annoHolder.isDisposed()) {
				annoHolder.dispose();
			}
			annoHolder = null;
		}

		annoHolder = new Composite(scrollComp, SWT.NONE);
		GridLayout layoutAnnoHolder = new GridLayout(1, false);
		layoutAnnoHolder.marginWidth = 0;
		layoutAnnoHolder.marginHeight = 0;
		layoutAnnoHolder.marginTop = 40;
		annoHolder.setLayout(layoutAnnoHolder);
		annoHolder.setBackground(Colors.VIDEO_OVERVIEW_BG.getColor());

		// zeichne im Scene Holder eine Skala
		annoHolder.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				// Anzahl der Zeitpunkte die eingezeichnet werden sollen
				int timePoints = 7;

				// Abstand der einzelnen Punkte
				int distanceTimePoints = widthContentScrollComp / timePoints;

				// Zeitspanne für einen timePoint
				long timeTimePointLong = (nodeScene.getScene().getEnd() - nodeScene
						.getScene().getStart()) / timePoints;

				// gib die aktuelle Anzahl von Annotationen aus
				e.gc.drawText(Messages.AnnotationOverview_Label_NumberOfAnnotations + annotations.size(), 0, 0);

				// Zeichne die timePoint Linien
				for (int i = 1; i < timePoints; i++) {
					int position = i * distanceTimePoints;

					// Zeitpunkt des aktuellen TimePoints
					String timeTimePoint = SivaTime
							.getTimeString(timeTimePointLong * i);
					e.gc.drawText(timeTimePoint, position
							- timeTimePoint.length() / 2 * 5, startScale);
					e.gc.drawLine(position, startScale + 15, position,
							newHeightFin);
				}
				
				// Zeichne die Videoposition ein
				
				// prozentualer Anteil einer Nanosekunde
				double percentNano = 100 / (double) mp.getDuration().getNano();
				// prozentualer Anteil der akt. Zeit
				double percentCurTime = (percentNano * mp.getRelativeTime().getNano());
				// 1 Prozent der Breite
				double percentWidth = (double) widthContentScrollComp / 100;
				int x = (int) (percentWidth * percentCurTime);
				e.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
				e.gc.setLineWidth(5);
				e.gc.drawLine(x, startScale + 15, x, newHeightFin);
			}
		});

		if (!annotations.isEmpty()) {
			Collections.sort(annotations, bComparator);
			for (INodeAnnotation annotation : annotations) {
				new SingleAnnotationBar(annoHolder, SWT.CENTER, annotation, this,
						nodeScene, widthContentScrollComp, mp);
			}
			setSelection(new StructuredSelection(annotations.get(0)));
		}		
		annoHolder.setSize(widthContentScrollComp, newHeight);
		scrollComp.setContent(annoHolder);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Returns a <code>StructuredSelection</code> containing the selected 
	 * <code>INodeAnnotation</code>.
	 */
	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Sets the selected element to the given selection. In the context of the
	 * <code>AnnotationOverview</code> this has to be a 
	 * <code>StructuredSelection</code> containing the selected annotation.
	 */
	@Override
	public void setSelection(ISelection newSelection) {
		if (newSelection instanceof StructuredSelection) {
			this.selection = (StructuredSelection) newSelection;
			Iterator<ISelectionChangedListener> it = listeners.iterator();
			while (it.hasNext()) {
				ISelectionChangedListener curListener = it.next();
				curListener.selectionChanged(new SelectionChangedEvent(this,
						selection));
			}
		}
	}

	private void updateViewAsync() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Check if the node scene is still part of the graph
				if (nodeScene.getGraph() != null ) {
					updateView();
				}
			}
		});

	}

	@Override
	public void onGraphChildAdded(IGraphNode node, IGraphNode newChild) {
		updateViewAsync();
	}

	@Override
	public void onGraphChildRemoved(IGraphNode node, IGraphNode oldChild) {
		updateViewAsync();
	}

	@Override
	public void onGraphNodePropertyChanged(IGraphNode node, String property,
			Object oldValue, Object newValue) {
		updateViewAsync();
	}

	@Override
	public void onGraphParentAdded(IGraphNode node, IGraphNode newParent) {
	}

	@Override
	public void onGraphParentRemoved(IGraphNode node, IGraphNode oldParent) {
	}

	@Override
	public void handleEvent(SivaEvent event) {
		if (event.getEventType().equals(SivaEventType.BEANCOMPARATOR_CHANGED)) {
			if (event.getValue() instanceof BeanComparator) {
				this.bComparator = (BeanComparator) event.getValue();
				updateView();
			}
		}
	}
}
