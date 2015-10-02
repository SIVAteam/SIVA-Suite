package org.iviPro.editors.annotationeditor.components.overview;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.actions.undoable.AnnotationDeleteAction;
import org.iviPro.application.Application;
import org.iviPro.listeners.GraphNodeEventConsumer;
import org.iviPro.listeners.GraphNodeListener;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;

/*
 * zeichnet eine einzelne Szene
 */
public class SingleAnnotationBar extends Composite implements PaintListener, GraphNodeEventConsumer {

	private int[] points = new int[2];

	private int width = 0;

	// die Höhe eines Szenebalkens
	private int height = 17;

	// die zugehörige Annotation
	private INodeAnnotation anno = null;

	// die aktuelle Selektion
	private INodeAnnotation selectedAnno = null;

	// der Knoten an dem die Scene hängt
	private NodeScene nodeScene;

	// der Annotationsüberblick
	private AnnotationOverview ov = null;

	// Mindestlänge der SingleAnnotationBar
	private int minWidth = 17;
	
	// hover
	private boolean hover = false;

	// Listener fuer Annotations- und Szenen-Knoten
	private GraphNodeListener nodeSceneListener;
	private GraphNodeListener nodeAnnoListener;

	public INodeAnnotation getAnno() {
		return anno;
	}

	public SingleAnnotationBar(final Composite parent, int style,
			INodeAnnotation barAnno, AnnotationOverview aov, NodeScene nodeS,
			int width, final MediaPlayer mp) {
		super(parent, style);
		this.anno = barAnno;
		this.width = width;
		this.nodeScene = nodeS;
		this.ov = aov;
		
		addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == 127) {
					new AnnotationDeleteAction(anno).run();
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {		
			}			
		});
		
		if (ov.getSelection() instanceof StructuredSelection) {
			this.selectedAnno = (INodeAnnotation) ((StructuredSelection) ov
					.getSelection()).getFirstElement();
		}
		setToolTipText(anno.getTitle());

		GridData ssBGrid = new GridData();
		ssBGrid.widthHint = this.width;
		ssBGrid.heightHint = this.height;
		setLayoutData(ssBGrid);
		
		this.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseEnter(MouseEvent e) {
				hover = true;
				redraw();
				update();
			}

			@Override
			public void mouseExit(MouseEvent e) {
				hover = false;
				redraw();
				update();
			}

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
				
		addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				SivaTime start = new SivaTime(anno.getStart() - nodeScene.getScene().getStart());
				SivaTime end = new SivaTime(anno.getEnd() - nodeScene.getScene().getStart());
				mp.playFromTo(start, end);
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				StructuredSelection selection = new StructuredSelection(anno);
				ov.setSelection(selection);

				// rechte Maustaste => öffne Menü, mit Löschmöglichkeit für die
				// Annotation
				MenuManager menuManager = new MenuManager();
				menuManager.add(new AnnotationDeleteAction(anno));
				SingleAnnotationBar.this.setMenu(menuManager
						.createContextMenu(SingleAnnotationBar.this));	
				SingleAnnotationBar.this.setFocus();
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
		});

		// die SingleSceneBar hört auf den Overview ob eine andere Annotation
		// ausgewählt wurde
		ov.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				// falls die Bar nicht mehr existiert
				if (isDisposed()) {
					return;
				}
				// TODO Auto-generated method stub
				if (event.getSelection() instanceof StructuredSelection) {
					if (((StructuredSelection) event.getSelection()).getFirstElement() instanceof INodeAnnotation) {
						selectedAnno = (INodeAnnotation) ((StructuredSelection) event
								.getSelection()).getFirstElement();
						redraw();
						update();
					} else {
						selectedAnno = null;
						redraw();
						update();
					}
				}
			}
			
		});

		// Listener auf Szenen- und Annotations-Knoten initialisieren
		Graph graph = Application.getCurrentProject().getSceneGraph();
		nodeSceneListener = new GraphNodeListener(this);
		nodeSceneListener.startListening(nodeScene, graph);
		nodeAnnoListener = new GraphNodeListener(this);
		nodeAnnoListener.startListening(anno, graph);

		addPaintListener(this);

		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				nodeSceneListener.stopListening();
				nodeAnnoListener.stopListening();
				removePaintListener(SingleAnnotationBar.this);
			}
		});
	}

	/*
	 * die Funktion berechnet den Startpunkt und die Länge der Szene, die
	 * gezeichnet werden soll.
	 */
	private void calcPaintStartEnd() {

		// die Start und Endzeit wird relativ zur Szene betrachtet d.h.
		// das Zeichnen beginnt bei 0 und geht bis Szenenlänge
		long start = anno.getStart() - nodeScene.getScene().getStart();
		long end = anno.getEnd() - nodeScene.getScene().getStart();

		// Dauer der Szene
		double duration = nodeScene.getScene().getEnd()
				- nodeScene.getScene().getStart();

		// prozentualer Anteil einer Nanosekunde
		double percentNano = 100 / duration;

		// prozentualer Anteil der Start bzw. Endzeit
		double percentStart = (percentNano * start);
		double percentEnd = (percentNano * end);

		// 1 Prozent der Breite
		double percentWidth = (double) width / 100;
		points[0] = (int) (percentWidth * percentStart);

		int newwidth = ((int) (percentWidth * percentEnd)) - points[0];
		if (newwidth < this.minWidth) {
			newwidth = this.minWidth;
		}
		points[1] = newwidth;
	}

	@Override
	public void paintControl(PaintEvent e) {

		// berechne die Start und Endpunkte die gezeichnet werden sollen
		calcPaintStartEnd();

		// zeichne die SingleAnnotationBar Fläche entsprechend dem
		// RoundRectangle
		int y = this.getBounds().y;
		setBounds(points[0], y, points[1], height);

		// falls diese Annotation gerade gewählt ist setzte sie auf aktiv
		if (selectedAnno != null
				&& selectedAnno.getTitle().equals(anno.getTitle()) || hover) {
			e.gc.setBackground(Colors.VIDEO_OVERVIEW_ITEM_BG_SELECTED
					.getColor());
		} else {
			e.gc.setBackground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());
		}

		// bei Unterschreitung der Mindestgröße wird ein Kreis gezeichnet
		if (points[1] <= this.minWidth) {
			e.gc.fillOval(0, 0, minWidth, minWidth);
			e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BORDER.getColor());
			e.gc.drawOval(0, 0, minWidth - 1, minWidth - 1);
			Image icon = getIcon(anno); 
			if (icon != null) {
				e.gc.drawImage(new Image(Display.getCurrent(), icon.getImageData().scaledTo(12, 12)), 3, 3);
			}
		} else {
			e.gc.fillRoundRectangle(0, 0, points[1], height, 10, 10);
			e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BORDER.getColor());
			e.gc.drawRoundRectangle(0, 0, points[1] - 1, height - 1, 10, 10);
			Image icon = getIcon(anno); 
			if (icon != null) {
				e.gc.drawImage(new Image(Display.getCurrent(), icon.getImageData().scaledTo(14, 14)), 4, 2);
			}
		}

		// offsetX: X Offset ab wo der Text gezeichnet wird
		int textOffsetX = 20;

		// setze die Beschreibung
		String desc = adjustDescriptionText(anno.getTitle() + " (id: " 
				+ anno.getNodeID() + ")", textOffsetX,
				4, e.gc, points[1], minWidth);
		e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_FONT.getColor());
		e.gc.drawText(desc, textOffsetX, 1);
		e.gc.dispose();
	}

	private String adjustDescriptionText(String str, int offsetXL,
			int offsetXR, GC gc, int paintWidth, int minPaintWidth) {
		String description = ""; //$NON-NLS-1$

		// der Text wird nur gesetzt wenn die Mindestgröße der Annotation Bar
		// gegeben ist
		if (paintWidth > minPaintWidth) {

			// Länge des Textes wenn er gezeichnet wird + 2 mal das Offset als
			// Platzhalter links und rechts
			int textDrawLength = gc.textExtent(str).x + offsetXL + offsetXR;

			// falls der Platz ausreicht, muss nichts gemacht werden
			if (textDrawLength < paintWidth) {
				return str;
			}

			// StringBuffer mit jeweils der halben Beschreibung
			StringBuffer strBuf1 = new StringBuffer(str.substring(0, str
					.length() / 2));
			StringBuffer strBuf2 = new StringBuffer(str
					.substring(str.length() / 2));
			// gibt an ob der String passt
			boolean strFits = false;
			while (!strFits) {
				description = strBuf1.toString() + " ... " + strBuf2.toString(); //$NON-NLS-1$
				if (gc.textExtent(description).x + offsetXL + offsetXR < paintWidth) {
					strFits = true;
				}
				// wenn beide Puffer leer laufen passt der String nicht rein =>
				// zeichne "..."
				if (strBuf1.length() == 0 && strBuf2.length() == 0) {
					return "..."; //$NON-NLS-1$
				}
				if (strBuf1.length() > 0) {
					strBuf1.deleteCharAt(strBuf1.length() - 1);
				}
				if (strBuf2.length() > 0) {
					strBuf2.deleteCharAt(0);
				}				
			}
		}
		return description;
	}

	private void updateDisplay() {
		// wenn sich ein property geändert hat zeichne die Bar neu
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!isDisposed()) {
					pack(true);
					setToolTipText(anno.getTitle());
				}
			}
		});
	}

	@Override
	public void onGraphChildAdded(IGraphNode node, IGraphNode newChild) {
		updateDisplay();
	}

	@Override
	public void onGraphChildRemoved(IGraphNode node, IGraphNode oldChild) {
		updateDisplay();
	}

	@Override
	public void onGraphNodePropertyChanged(IGraphNode node, String property,
			Object oldValue, Object newValue) {
		updateDisplay();
	}

	@Override
	public void onGraphParentAdded(IGraphNode node, IGraphNode newParent) {
		// TODO: Spielt es hier eine Rolle ob sich Vater geaendert hat?
	}

	@Override
	public void onGraphParentRemoved(IGraphNode node, IGraphNode oldParent) {
		// TODO: Spielt es hier eine Rolle ob sich Vater geaendert hat?
	}
	

	/**
	 * liefert für einen bestimmten Annotationstyp
	 * @param anno
	 * @return
	 */
	public Image getIcon(INodeAnnotation anno) {		
		if (anno instanceof NodeAnnotationAudio) {
			return Icons.OBJECT_MEDIA_AUDIO.getImage();
		}
		if (anno instanceof NodeAnnotationVideo) {
			return Icons.OBJECT_MEDIA_VIDEO.getImage();
		}
		if (anno instanceof NodeAnnotationPicture) {
			return Icons.OBJECT_MEDIA_PICTURE.getImage();
		}
		if (anno instanceof NodeAnnotationSubtitle) {
			return Icons.OBJECT_MEDIA_TEXT_SUBTITLE.getImage();
		}
		if (anno instanceof NodeAnnotationRichtext) {
			return Icons.OBJECT_MEDIA_TEXT_RICH.getImage();
		}
		if (anno instanceof NodeAnnotationPdf) {
			return Icons.OBJECT_MEDIA_PDF.getImage();
		}
		if (anno instanceof NodeMark) {
			return Icons.OBJECT_ANNOTATION.getImage();
		}
		return null;
	}	

}
