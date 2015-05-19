package org.iviPro.editors.annotationeditor.components.positioneditors;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.iviPro.application.Application;
import org.iviPro.editors.annotationeditor.components.Messages;
import org.iviPro.editors.annotationeditor.components.positioneditors.OverlayFactory.MarkButtonFigure;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.I_MediaPlayer;
import org.iviPro.mediaaccess.player.PlayerFactory;
import org.iviPro.mediaaccess.player.controls.SivaScale;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.annotation.EllipseShape;
import org.iviPro.model.annotation.IMarkShape;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.annotation.PolygonShape;
import org.iviPro.model.annotation.PolygonShape.Position;
import org.iviPro.model.annotation.PositionalShape;
import org.iviPro.model.graph.NodeMarkType;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.utils.NumericInputListener;
import org.iviPro.utils.SivaTime;
import org.iviPro.utils.widgets.SizedTextWithUnit;

public class OverlayEditor {

	// maximale laenge der button beschriftung
	private static final int MASK_BUTTON_LABEL_MAX_LENGTH = 30;
	
	// Aufnahmeinterval in ns
	private static final long RECINTERVAL = 200000000;

	// Die Shell des Overlay Editors
	private Shell shell;
	private int shellWidth;
	private int shellHeight;

	// die Toolbar für die Buttons
	private ToolBar bar;

	// die Breite und Höhe des Videos
	private int videoWidth;
	private int videoHeight;
	private Composite videoComposite;
	//private Frame vidFrame;

	// max Breite und Höhe des Videos
	private int maxVidWidth = 800;
	private int maxVidHeight = 600;

	// die Overlay Figure die für das verändern/aufzeichnen der Bewegungen
	// verwendet wird
	private OverlayFigure overlayFigure;

	// das Hauptvideo
	private Video video;

	// Start und Endzeit der Annotation bezogen auf das gesamte Video
	// == Startzeit Szene + Start/Endzeit der Annotation
	private SivaTime startTimeInVid;
	private SivaTime endTimeInVid;

	// Start und Endzeit der Annotation bezogen auf die Szene
	private SivaTime startTimeInScene;
	private SivaTime endTimeInScene;

	// der Media Player
	private I_MediaPlayer mp;

	// das Canvas zum Zeichnen der Figures
	private OverlayCanvas canvas;

	/**
	 * Time line widget.
	 */
	private SivaScale scale;

	/**
	 * The time in nanoseconds which references the last recorded figure
	 * during a recording process. This is not necessarily the time of the last
	 * figure with regard to the actual media time. But it is the time of the 
	 * last figure with regard to the starting time of the actual recording
	 * process. 
	 */
	private long lastRecord;

	// gibt an ob der Aufnahmemodus aktiv ist
	private boolean recordMode;

	// gibt an ob aktuell aufgenommen wird
	private boolean recordActive;

	// eine Treemap zum Speichern der Figures
	// Key ist die Videozeit (die reale Zeit im gesamten Video, die relativen
	// Zeiten werden erst beim Export berechnet)
	private TreeMap<Long, TimeFigure> figMap = new TreeMap<Long, TimeFigure>();

	// übergebene OverlayPathItems bzw. MarkShapes
	private List<OverlayPathItem> pathItems;
	private boolean editOverlayPath;
	private List<IMarkShape> markShapes;
	private boolean editMark;
	private NodeMarkType markType;

	private boolean isGlobalAnno;

	// Selektionsrectangle für Multiple Auswahl von Markierungen (Löschen)
	private Rectangle selectionRectangle;
	// der Punkt der auf der Skala geklickt wurde, von diesem aus wird beim
	// Ziehen der Maus
	// das selectionRectangle berechnet
	private Point selectionRectangleSourcePoint;

	// die aktuell selektierte Time Figure, diese ist gesetzt wenn genau eine
	// Time Figure
	// selektiert ist
	private TimeFigure selectedTimeFigure;

	// TreeMap von selektierten TimeFigures, bei Mehrfachauswahl, diese
	// TimeFigures dienen
	// zum Löschen der TimeFigures
	private TreeMap<Long, TimeFigure> selectedTimeFigures = new TreeMap<Long, TimeFigure>();

	// gibt an ob ctrl gedrückt wird, wird für multiple Selektion verwendet
	private boolean ctrlPressed;

	// gibt an ob der Preview aktiv ist
	private boolean previewActive;

	// preview Index, der Index der zuletzt beim Preview abgespielten Figure
	private int previewIndex;

	// der Content bei Overlay Annotationen
	private IAbstractBean content;

	/**
	 * Media Player für den Content von Video Annotationen
	 */
	private I_MediaPlayer mpVSO;

	private Image scaledContImage;

	private double contentAspectRatio = 1;

	// button beschriftung einer mark annotation
	private String buttonLabel;
	private Text txtButtonLabel;
	private Composite toolBarComposite;
	
	private boolean isMark;
	private ToolItem previewButton;
	private ToolItem aspectSwitch;
	
	//Labels für Posiiton und Größe des Overlays
	private Text posXField;
	private Text posYField;
	private Text widthField;
	private Text heightField;

	/**
	 * zum Editieren eines OverlayPath Items = globale Annotationen ohne Video,
	 * ohne Skala Falls das Item null ist wird automatisch das erste Item
	 * erstellt; Startszene aus Graph wird für Ansicht verwendet
	 * 
	 * @param nodeScene
	 * @param startTime
	 * @param endTime
	 * @param pathItems
	 */
	public OverlayEditor(List<OverlayPathItem> pathItems, IAbstractBean content) {
		// erzeuge eine Shell für den Overlay Editor
		this.shell = new Shell(Display.getCurrent().getActiveShell(),
				SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.editOverlayPath = true;
		this.pathItems = pathItems;
		this.isGlobalAnno = true;
		this.content = content;
		this.lastRecord = 0;
		
		// First child after NodeStart has always to be a scene according to graph
		// definition
		Scene scene = ((NodeScene)Application.getCurrentProject().getSceneGraph()
				.getStart().getChildren(NodeScene.class).get(0)).getScene();
		this.video = scene.getVideo();
		this.startTimeInVid = new SivaTime(scene.getStart());
		this.endTimeInVid = new SivaTime(scene.getEnd());
		this.startTimeInScene = new SivaTime(scene.getStart());
		this.endTimeInScene = new SivaTime(scene.getEnd());
		
	}

	/**
	 * zum Editieren von OverlayPath Items Falls die Liste leer oder null ist,
	 * wird automatisch die erste Figure erstellt
	 * 
	 * @param nodeScene
	 * @param startTime annotation start time relative to scene start time
	 * @param endTime annotation end time relative to scene end time
	 * @param pathItems
	 */
	public OverlayEditor(NodeScene nodeScene, SivaTime startTime,
			SivaTime endTime, List<OverlayPathItem> pathItems, IAbstractBean content, boolean isMark) {
		// erzeuge eine Shell für den Overlay Editor
		this.shell = new Shell(Display.getCurrent().getActiveShell(),
				SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.video = nodeScene.getScene().getVideo();
		this.startTimeInScene = startTime;
		this.endTimeInScene = endTime;
		this.startTimeInVid = startTime.addTimeS(nodeScene.getScene()
				.getStart());
		this.endTimeInVid = endTime.addTimeS(nodeScene.getScene().getStart());
		this.lastRecord = startTimeInVid.getNano();
		this.pathItems = pathItems;
		this.editOverlayPath = true;
		this.content = content;
		this.isMark = isMark;
	}

	/**
	 * zum Editieren von Markierungen Falls die Liste leer oder null ist, wird
	 * automatisch die erste Figure erstellt
	 * 
	 * @param nodeScene
	 * @param startTime
	 * @param endTime
	 * @param markType
	 * @param shapes
	 */
	public OverlayEditor(NodeScene nodeScene, SivaTime startTime,
			SivaTime endTime, NodeMarkType markType, List<IMarkShape> shapes,
			String buttonLabel) {
		// erzeuge eine Shell für den Overlay Editor
		this.shell = new Shell(Display.getCurrent().getActiveShell(),
				SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.video = nodeScene.getScene().getVideo();
		this.startTimeInScene = startTime;
		this.endTimeInScene = endTime;
		this.startTimeInVid = startTime.addTimeS(nodeScene.getScene()
				.getStart());
		this.endTimeInVid = endTime.addTimeS(nodeScene.getScene().getStart());
		this.markShapes = shapes;
		this.editMark = true;
		this.markType = markType;
		if (buttonLabel == null && markType.equals(NodeMarkType.BUTTON)) {
			this.buttonLabel = Messages.OverlayFactory_Default_ButtonText;
		} else {
			this.buttonLabel = buttonLabel;
		}
	}

	public void open() {

		// der Anzeige-Text der Shell wird gesetzt
		shell.setText(Messages.PositionSelector_WindowsTitle_DefineOverlayPosition);
		videoWidth = video.getDimension().width;
		videoHeight = video.getDimension().height;
		
		// Seitenverhältnis anpassen
		adjustAspect();

		// passe Shellgröße dem Video an
		shellWidth = videoWidth + 20;
		shellHeight = videoHeight + 190;
		shell.setSize(shellWidth, shellHeight);
		shell.setLayout(new GridLayout(1, false));

		// initialisiere den MediaPlayer und die Skala
		// der MediaPlayer für die Anzeige des Videos
		mp = PlayerFactory.getPlayer(video, startTimeInVid.getNano(),endTimeInVid.getNano());
			
		if(!isMark){
			scale = new SivaScale(shell,endTimeInScene.subTime(startTimeInScene),
					videoWidth - 10, 40, false, false, false);
		}else{ //Fenster kleiner machen, da die Zeitleiste fehlt
			shellHeight -= 50;
			shell.setSize(shellWidth, shellHeight);
		}
		
		// erstelle die Toolbar
		createToolbar();

		// erstelle eine extra Shell für das Overlay um repainting des Videos zu
		// vermeiden
		// (schwarze Flecken)
		// die Shell ist größer wie das Video und wird direkt auf die
		// Koordinaten des
		// Videos gesetzt
		final Shell overlayShell = new Shell(shell, SWT.NO_TRIM); 
			//| SWT.ON_TOP); annoying and should not be necessary
		
		// The added values have to be considered when relocating
		overlayShell.setSize(videoWidth + 12 , videoHeight + 12);
		overlayShell.setLayout(new GridLayout(1, false));
		
		// das OverlayCanvas zum Zeichnen der Overlays
		if (content == null) {
			canvas = new OverlayCanvas(overlayShell, 0, 0, videoWidth,
					videoHeight);
		} else {
			canvas = new OverlayCanvas(overlayShell, 0, 0, videoWidth,
					videoHeight, true);
			setContent(content);
		}
		overlayShell.open();
		overlayShell.setVisible(false);

		shell.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent paramControlEvent) {
				Display.getCurrent().syncExec(new Runnable() {
					@Override
					public void run() {
						org.eclipse.swt.graphics.Point locationD = videoComposite
								.toDisplay(0, 0);
						overlayShell.setLocation(locationD.x - 6,
								locationD.y - 6);
						if (!overlayShell.isVisible()) {
							overlayShell.setVisible(true);
						}
					}
				});
			}

			@Override
			public void controlResized(ControlEvent paramControlEvent) {
				controlMoved(paramControlEvent);
			}
		});

		// erstelle das Video Composite
		// das Composite zur Anzeige des Videos
		videoComposite = new Composite(shell, SWT.EMBEDDED | SWT.BORDER);
		GridData videoCompositeGD = new GridData();
		videoCompositeGD.widthHint = videoWidth;
		videoCompositeGD.heightHint = videoHeight;
		videoComposite.setLayoutData(videoCompositeGD);
		
		mp.createVisualPart(videoComposite,0,0,videoWidth,videoHeight);

		// initialisiere die TimeFigures (aus OverlayPathItems bzw. IMarkShapes)
		if (this.editMark) {
			createTimeFiguresFromMarkShapes();
		} else if (this.editOverlayPath) {
			createTimeFiguresFromOverlayPath();
		}
		
		// setze das selektierte Item am Anfang auf das erste
		this.selectedTimeFigure = this.figMap.firstEntry().getValue();

		// registriere Listener
		addListener();
		
		if (content != null) {
			if (content instanceof Scene || content instanceof Video || content instanceof Picture) {
				this.overlayFigure.setKeepAspect(true);
			}
		}
		createPositionandSizeLabel();
				
		// Shell öffnen und Größe berechnen
		shell.open();
				
		// Nun wird gewartet, bis der Dialog geschlossen wird
		Display displayParent = shell.getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!displayParent.readAndDispatch()) {
				displayParent.sleep();
			}
		}
	
	}

	/**
	 * erstelle die Toolbar
	 */
	private void createToolbar() {
		// Container fuer die ToolBar und das Textfeld fuer die
		// Button-Beschriftung
		toolBarComposite = new Composite(shell, SWT.NONE);
		GridLayout toolBarLayout = new GridLayout(3, false);
		toolBarLayout.marginHeight = 0;
		toolBarComposite.setLayout(toolBarLayout);
		toolBarComposite.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));
		//toolBarComposite.setBackground(new Color(null, 100,0,0));
		// Create toolbar
		bar = new ToolBar(toolBarComposite, SWT.NONE);
		//bar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		if (!isGlobalAnno && !isMark) {
			
			// Toolbar für Aufnahme Buttons
			final ToolItem recordModeButton = new ToolItem(bar, SWT.CHECK);
			recordModeButton.setText(Messages.OverlayPathEditor_7);
			recordModeButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					recordModeButtonAction();
				}
			});

			// Button für Vorschau
			previewButton = new ToolItem(bar, SWT.CHECK);
			previewButton.setText(Messages.OverlayPathEditor_8);
			previewButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
							
					if (!previewActive) {
						recordMode = false;
						recordModeButton.setSelection(false);
						recordActive = false;
						previewActive = true;
						calcPreviewIndex();
						mp.play();
						if (mpVSO != null) {
							mpVSO.play();
						}
					} else {
						previewActive = false;
						mp.pause();
						if (mpVSO != null) {
							mpVSO.pause();
						}
					}
				}
			});
			
			// Button zum Löschen von TimeFigures
			ToolItem deleteButton = new ToolItem(bar, SWT.PUSH);
			deleteButton.setText(Messages.OverlayPathEditor_5);
			deleteButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					removeTimeFigures();
				}
			});
		}

		// Button zum Speichern der aktuellen Position
		ToolItem recordButton = new ToolItem(bar, SWT.PUSH);
		recordButton.setText(Messages.OverlayPathEditor_2);
		recordButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				recordPosition();
			}
		});
		
		/*
		 * Textfeld und Knopf fuer die Beschriftung eines Buttons erstellen, falls der
		 * MarkType einem Button entspricht
		 */
		if (markType == NodeMarkType.BUTTON) {
			// create label
			Label separator = new Label(toolBarComposite, SWT.VERTICAL | SWT.SEPARATOR);
			GridData sepData = new GridData();
			Composite buttonComp = new Composite(toolBarComposite, SWT.NONE);
			buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			buttonComp.setLayout(new GridLayout(3,false));
			Label lblButtonLabel = new Label(buttonComp, SWT.NONE);
			lblButtonLabel.setText(Messages.OverlayPathEditor_Label_ButtonText);
			// Need to correct size of separator line
			sepData.heightHint = lblButtonLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			separator.setLayoutData(sepData);
			// create button
			txtButtonLabel = new Text(buttonComp, SWT.SINGLE | SWT.BORDER);
			// nur editierbar machen wenn als mod button ausgewählt wurde
			txtButtonLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
			txtButtonLabel.setText(buttonLabel);
			// maximale laenge der button beschriftung
			txtButtonLabel.setTextLimit(MASK_BUTTON_LABEL_MAX_LENGTH);
			Button applyButton = new Button(buttonComp, SWT.PUSH);
			applyButton.setText(Messages.OverlayPathEditor_Button_ButtonText);
			applyButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateButton();
				}
			});
			
		}

		if (content != null) {
			if (content instanceof Scene || content instanceof Video
					|| content instanceof Picture) {
				aspectSwitch = new ToolItem(bar, SWT.CHECK);
				aspectSwitch.setText(Messages.OverlayPathEditor_13);
				aspectSwitch.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						boolean val = aspectSwitch.getSelection();
						overlayFigure.setKeepAspect(val);
					}
				});				
				aspectSwitch.setSelection(true); 
				
				final ToolItem aspectReset = new ToolItem(bar, SWT.PUSH);
				aspectReset.setText(Messages.OverlayPathEditor_14);
				aspectReset.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						overlayFigure.adjustAspect();
					}
				});

				
					
			}
		}

		if (this.markType != null && this.markType.equals(NodeMarkType.POLYGON)) {
			final ToolItem addPointButton = new ToolItem(bar, SWT.PUSH);
			addPointButton.setText(Messages.OverlayPathEditor_15);
			addPointButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					overlayFigure.increasePolygonPoints();
				}
			});

			final ToolItem removePointButton = new ToolItem(bar, SWT.PUSH);
			removePointButton.setText(Messages.OverlayPathEditor_16);
			removePointButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					overlayFigure.decreasePolygonPoints();
				}
			});
		}
		
	}
	
	/**
	 * Updates the size and text of the button used in mark annotations. 
	 */
	private void updateButton(){
		MarkButtonFigure button = ((MarkButtonFigure)overlayFigure.getContentFigure());
		button.setText(getButtonLabel());
		button.updateDimension();
		overlayFigure.calculateRegion();
		updateButtonFigures(getButtonLabel()); 
	}
	
	/**
	 * Updates the button figures of all <code>TimeFigures</code> to be labeled
	 * with the given text and sets their bounds as to fit to the text size.
	 * @param text label which should be used in the button figures stored
	 * in the <code>TimeFigures</code>
	 */
	private void updateButtonFigures(String text) {
		for (TimeFigure figure : figMap.values()) {
			MarkButtonFigure button = ((MarkButtonFigure)figure.getFigure());
			button.setText(text);
			button.updateDimension();
		}
	}

	private void recordModeButtonAction(){
		if (!recordMode) {
			recordMode = true;
			previewActive = false;
			previewButton.setSelection(false);
			mp.pause();
			if (mpVSO != null) {
				mpVSO.pause();
			}
		} else {
			recordMode = false;
			recordActive = false;
			mp.pause();
			if (mpVSO != null) {
				mpVSO.pause();
			}
		}
	}
	
	private void preview() {
		if (shell.isDisposed() || canvas.isDisposed()) {
			return;
		}
		TimeFigure next = null;
		ArrayList<TimeFigure> figures = new ArrayList<TimeFigure>(
				figMap.values());
		if (previewIndex >= figures.size()) {
			return;
		}
		if (previewIndex == -1) {
			calcPreviewIndex();
		}
		for (int i = previewIndex; i < figures.size(); i++) {
			// suche die nächste Figure und zeige sie an
			TimeFigure curFig = figures.get(i);
			long medTime = mp.getMediaTime().getNano();
			// falls die nächste Figure im erlaubten Zeitrahmen ist, nimm sie
			// her
			if (curFig.getTime() > medTime
					&& curFig.getTime() < medTime + 300000000L) {
				previewIndex = i;
				next = curFig;
				break;
			} else
			// falls die Zeit der Figur kleiner ist erhöhe den Index
			if (curFig.getTime() < medTime) {
				previewIndex++;
			}
		}
		if (next != null) {
			overlayFigure = OverlayFactory.getInstance().createOF(canvas,
					next.getFigure());
			canvas.setFigure(overlayFigure);
			this.selectedTimeFigure = next;
		}
	}

	private void calcPreviewIndex() {
		ArrayList<TimeFigure> figures = new ArrayList<TimeFigure>(
				figMap.values());
		// liefert den Vorgänger zur aktuellen Zeit d.h. das nächste Element ist
		// einen Index höher
		previewIndex = searchPredecessorIndex(figures, mp.getRelativeTime()
				.getNano()) + 1;
	}

	private void removeTimeFigures() {
		if (selectedTimeFigure != null) {
			if (figMap.firstEntry().getValue().getTime() != selectedTimeFigure
					.getTime()) {
				figMap.remove(selectedTimeFigure.getTime());
				selectedTimeFigure = null;
				lastRecord = figMap.floorKey(mp.getMediaTime().getNano());
			}
		}
		if (selectedTimeFigures.size() > 0) {
			for (TimeFigure figure : selectedTimeFigures.values()) {
				if (figMap.firstEntry().getValue().getTime() != figure
						.getTime()) {
					figMap.remove(figure.getTime());
				}
			}
			selectedTimeFigures.clear();
			lastRecord = figMap.floorKey(mp.getMediaTime().getNano());
		}
		scale.getSlider().redraw();
	}

	/**
	 * setzt die Listener
	 */
	private void addListener() {

		videoComposite.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent paramDisposeEvent) {
				if (!shell.isDisposed()) {
					if (mp != null) {
						mp.finish();
					}
					if (mpVSO != null) {
						mpVSO.finish();
					}
				}
			}
		});

		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent paramKeyEvent) {
				if (paramKeyEvent.keyCode == 262144) {
					ctrlPressed = true;
				}
			}

			@Override
			public void keyReleased(KeyEvent paramKeyEvent) {
				ctrlPressed = false;
			}
		});

		if (scale != null) {
			scale.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent paramKeyEvent) {
					if (paramKeyEvent.keyCode == 262144) {
						ctrlPressed = true;
					}
				}

				@Override
				public void keyReleased(KeyEvent paramKeyEvent) {
					ctrlPressed = false;
				}
			});
		}

		if (canvas != null) {

			// Listener auf das Canvas zum Starten/Beenden der Aufnahme
			canvas.addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
				}

				@Override
				public void mouseDown(MouseEvent arg0) {				
					if (overlayFigure.isDragActive() && recordMode) {
						recordActive = true;
						// Clear selection
						selectedTimeFigures.clear();
						scale.getSlider().redraw();
						mp.play();
						if (mpVSO != null) {
							mpVSO.play();
						}
					}
				}

				@Override
				public void mouseUp(MouseEvent arg0) {
					if (recordMode) {
						recordActive = false;
						mp.pause();
						if (mpVSO != null) {
							mpVSO.pause();
						}
					}
				}
			});

			canvas.addMouseMoveListener(new MouseMoveListener() {
				@Override
				public void mouseMove(MouseEvent event) {
					//Positionsanzeige und Größe updaten
					updateOverlayBounds();
					
					if (!overlayFigure.isDragActive()) {
						return;
					}
										
					if (recordMode && recordActive) {
						selectedTimeFigure = null;
						long recTime = mp.getMediaTime().getNano();
						if (recTime - lastRecord > RECINTERVAL) {
							overwriteFigures(recTime);
							lastRecord = recTime;
							recordPosition();
						}
					}
				}
			});
		}

		if (scale != null) {
			scale.getSlider().addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent pe) {

					if (selectionRectangle != null) {
						pe.gc.drawRectangle(selectionRectangle);
					}
					for (TimeFigure figure : figMap.values()) {
						Color background = (selectedTimeFigure != null && (figure
								.compareTo(selectedTimeFigure) == 0)) ? Display
								.getCurrent().getSystemColor(SWT.COLOR_RED)
								: Display.getCurrent().getSystemColor(
										SWT.COLOR_BLUE);
						if (selectedTimeFigures.containsKey(figure.getTime())) {
							background = Display.getCurrent().getSystemColor(
									SWT.COLOR_RED);
						}
						pe.gc.setBackground(background);
						pe.gc.fillRectangle(figure.getMarkPosition());
					}
				}
			});

			scale.getSlider().addMouseMoveListener(new MouseMoveListener() {

				@Override
				public void mouseMove(MouseEvent me) {
					if (selectionRectangleSourcePoint != null) {
						int width = Math.abs(selectionRectangleSourcePoint.x
								- me.x);
						int height = Math.abs(selectionRectangleSourcePoint.y
								- me.y);
						int x = me.x < selectionRectangleSourcePoint.x ? me.x
								: selectionRectangleSourcePoint.x;
						int y = me.y < selectionRectangleSourcePoint.y ? me.y
								: selectionRectangleSourcePoint.y;
						selectionRectangle = new Rectangle(x, y, width, height);
						scale.getSlider().redraw();
					}
				}
			});

			scale.getSlider().addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
				}				

				@Override
				public void mouseDown(MouseEvent me) {
					/* Media player time has already been updated by listener 
					   in AbstractSivaSlider. */						
					
					if (previewActive) {
						previewIndex = -1;
					}
					selectionRectangleSourcePoint = new Point(me.x, me.y);
					if (!ctrlPressed) {
						selectedTimeFigures.clear();
					} else {
						if (selectedTimeFigure != null) {
							selectedTimeFigures.put(
									selectedTimeFigure.getTime(),
									selectedTimeFigure);
						}
					}
					selectedTimeFigure = null;
					boolean figSelected = false;
					for (TimeFigure tfig : figMap.values()) {
						if (tfig.getMarkPosition().contains(me.x, me.y)) {
							lastRecord = tfig.getTime();
							// setze eine Kopie dieser Figur, da sonst die
							// Figure geändert wird, selbst
							// wenn die Zeit versetzt wird
							// eine Änderung an einem best. Zeitpunkt erfordert
							// somit explizites neu speichern
							overlayFigure.setContentFigure(OverlayFactory
									.getInstance().getCopy(tfig.getFigure()));
							if (!ctrlPressed) {
								selectedTimeFigure = tfig;
							} else {
								selectedTimeFigures.put(tfig.getTime(), tfig);
							}
							figSelected = true;
							break;
						}
					}
					
					/* Set the shown figure to the last figure if no existing 
					   figure has been selected. */
					if (!figSelected) {
						lastRecord = figMap.floorKey(mp.getMediaTime().getNano());
						TimeFigure lastFigure = figMap.get(lastRecord);
						overlayFigure.setContentFigure(OverlayFactory
								.getInstance().getCopy(lastFigure.getFigure()));
					}
					// falls nur eine Figur selektiert wird, lösche die
					// Selektionsmap und
					// setze die einzelne TimeFigure
					if (ctrlPressed && selectedTimeFigures.size() == 1) {
						selectedTimeFigure = selectedTimeFigures.firstEntry()
								.getValue();
						selectedTimeFigures.clear();
					}
					scale.getSlider().redraw();
				}

				@Override
				public void mouseUp(MouseEvent arg0) {
					// suche ausgewählte TimeFigures
					for (TimeFigure curTimeFigure : figMap.values()) {
						if (selectionRectangle != null
								&& !selectionRectangle.isEmpty()) {
							if (curTimeFigure.getMarkPosition().intersects(
									selectionRectangle)) {
								selectedTimeFigures.put(
										curTimeFigure.getTime(), curTimeFigure);
							}
						}
					}
					if (selectedTimeFigures.size() > 0) {
						selectedTimeFigure = null;
					}

					// setze den Source Point + das Rectangle wieder auf null
					selectionRectangleSourcePoint = null;
					selectionRectangle = null;
					scale.getSlider().redraw();
				}
			});

			// füge der Scala/Slider einen Consumer hinzu
			// die Scala setzt
			scale.addSivaEventConsumer(new SivaEventConsumerI() {
				@Override
				public void handleEvent(SivaEvent event) {

					if (shell.isDisposed()) {
						return;
					}
					if (event.getEventType().equals(SivaEventType.ENDTIME_CHANGED)) {
						selectedTimeFigure = null;
					}

					if (event != null && event.getTime() != null) {
						// die Zeit kommt von der Skala
						mp.setMediaTime(new SivaEvent(null,
								SivaEventType.MEDIATIME_CHANGED, event
										.getTime()));
						if (mpVSO != null) {
							mpVSO.setMediaTime(new SivaEvent(null,
									SivaEventType.MEDIATIME_CHANGED, event
											.getTime()));
						}
					}
				}
			});
		}
		if (mp != null) {
			mp.addSivaEventConsumer(new SivaEventConsumerI() {
				@Override
				public void handleEvent(SivaEvent event) {
					if (recordMode && recordActive) {
						scale.setSashes(event);
						overwriteFigures(event.getTime().getNano() 
								+ startTimeInVid.getNano());
					} else if (previewActive) {
						preview();
						scale.setSashes(event);
					}
				}
			});
		}
	}
	
	/**
	 * Removes figures between the last record time and the given time. 
	 * This can be used to clear old records while rerecording.
	 * <p>
	 * <b>Note:</b> Needs absolute time as parameter since figures use 
	 * absolute times, too. 
	 * @param actualTime absolute time in nanoseconds until which figures will be 
	 * cleared
	 */
	private void overwriteFigures(long actualTime) {
		long lastFigureTime;
		/* 
		 * Since the figMap contains at least one TimeFigure which can not be
		 * deleted, NullPointerException should not occur.
		 */
		while ((lastFigureTime = figMap.floorKey(actualTime)) 
				> lastRecord) {
			figMap.remove(lastFigureTime);
		}
	}

	/**
	 * passt das Seitenverhältnis an für das Hauptvideo
	 */
	private void adjustAspect() {
		// Change video format to use the width of the overlay canvas
		float factor = maxVidWidth/(float)videoWidth;
		videoWidth = maxVidWidth;
		videoHeight = (int) (videoHeight * factor);
		
		// Shrink video if height is still too large
		if (videoHeight > maxVidHeight) {
			factor = maxVidHeight/(float)videoHeight;
			videoHeight = maxVidHeight;
			videoWidth = (int) (videoHeight * factor);
		}
	}

	/**
	 * Creates a <code>TimeFigure</code> for the actual position in the media
	 * player and adds it to the figure map. If a single figure is selected in
	 * the time line it will be overriden.
	 * 
	 */
	private void recordPosition() {
		// während der Aufnahme ist keine TimeFigure selektiert
		// falls eine selektiert ist, überschreibe die TimeFigure wenn
		// auf "Speichere Position" geklickt wird
		if (isGlobalAnno) {
			TimeFigure timeFigure = new TimeFigure(overlayFigure, 0);
			this.figMap.put(timeFigure.getTime(), timeFigure);
		} else if (selectedTimeFigure != null) {
			TimeFigure timeFigure = new TimeFigure(overlayFigure,
					selectedTimeFigure.getTime());
			timeFigure.setMarkPosition(calculateMarkPosition(selectedTimeFigure
					.getTime()));
			this.figMap.put(selectedTimeFigure.getTime(), timeFigure);
			lastRecord = selectedTimeFigure.getTime();
		} else if (mp != null && this.overlayFigure != null) {
			long time = mp.getMediaTime().getNano();
			TimeFigure timeFigure = new TimeFigure(overlayFigure, time);

			// suche Vorgänger TimeFigure und prüfe ob sich die Position
			// verändert hat
			ArrayList<TimeFigure> figures = new ArrayList<TimeFigure>(
					this.figMap.values());
			int preIndex = searchPredecessorIndex(figures, time);
			TimeFigure pre = figures.get(preIndex);
			//TimeFigure pre = figMap.floorEntry(time).getValue();
			if (!pre.equalFigure(timeFigure) && pre.compareTo(timeFigure) != 0) {

				timeFigure.setMarkPosition(calculateMarkPosition(time));
				this.figMap.put(time, timeFigure);
				selectedTimeFigure = timeFigure;

				// prüfe TimeFigures nach der neuen TimeFigure ob sie durch die
				// neue bereits definiert sind				
				figures = new ArrayList<TimeFigure>(this.figMap.values());
				ArrayList<Long> toDelete = new ArrayList<Long>();
				// suche ab der neuen TimeFigure
				if (preIndex + 2 <= figures.size()) {
					for (int i = preIndex + 2; i < figures.size(); i++) {
						if (figures.get(i).equalFigure(timeFigure)) {
							toDelete.add(figures.get(i).getTime());
						} else {
							break;
						}
					}
				}
				for (Long key : toDelete) {
					this.figMap.remove(key);
				}
				scale.getSlider().redraw();
				scale.getSlider().update();
			}
			lastRecord = time;
		}
	}

	/**
	 * Simple Suche nach dem Index der Figure die vor der aktuellen liegt
	 * 
	 * @param figures
	 * @param time
	 * @return
	 */
	private int searchPredecessorIndex(ArrayList<TimeFigure> figures, long time) {

		if (figures.size() == 1) {
			return 0;
		}

		TimeFigure[] array = new TimeFigure[figures.size()];
		figures.toArray(array);

		// nicht gerade elegant ... Index sollte vorberechnet werden
		// z.B. Variante Binärsuche
		for (int i = array.length - 1; i >= 0; i--) {
			if (array[i].getTime() < time) {
				return i;
			}
		}
		return 0;
	}

	// KOORDINATENTRANSFORMATION + MARKIERUNGSPOSITION
	/**
	 * berechnet aus einem relativen Wert den reellen Wert (Koordinate)
	 * 
	 * @param relative
	 *            der relative Wert
	 * @param ref
	 *            der Bezugswert
	 * @return Koordinate
	 */
	private int getCoordFromRelative(float relative, int ref) {
		return (int) (relative * ref / 1f);
	}

	/**
	 * berechnet aus einer Koordinate den relativen Wert
	 * 
	 * @param coord
	 *            die Koordinate
	 * @param ref
	 *            der Bezugswert
	 * @return relativer Wert
	 */
	private float getRelativeFromCoord(int coord, int ref) {
		return 1f / ref * coord;
	}

	/**
	 * Gibt null zurueck falls es sich nicht um einen Button handelt andernfalls
	 * wird die Beschriftung des Buttons zurueckgegeben.
	 * 
	 * @return Button Beschriftung als String
	 */
	public String getButtonLabel() {
		if (txtButtonLabel != null) {
			return txtButtonLabel.getText();
		}
		return null;
	}

	private Rectangle calculateMarkPosition(long time) {
		if (isGlobalAnno) {
			return new Rectangle(0, 0, 0, 0);
		}
		long duration = endTimeInVid.subTime(startTimeInVid);
		int x = (int) ((double) (videoWidth - 10) / duration * (time - startTimeInVid.getNano() ));
		return new Rectangle(x, 18, 4, 25);
	}

	public void addListener(int eventType, Listener listener) {
		shell.addListener(eventType, listener);
	}

	// TIMEFIGURES <=> MARKSHAPES
	private void createTimeFiguresFromMarkShapes() {
		// initialisiere die TimeFigures, falls keine vorhanden sind wird
		// automatisch
		// eine Standard Figure erstellt
		if (markShapes != null && markShapes.size() > 0) {
			Figure figure = null;
			long time = 0;
			switch (markType) {
			case POLYGON:
				for (IMarkShape shape : markShapes) {
					PolygonShape polygon = (PolygonShape) shape;
					time = polygon.getTime();
					PointList pointList = new PointList();
					for (Position position : polygon.getVertices()) {
						int x = getCoordFromRelative(position.getX(),
								videoWidth);
						int y = getCoordFromRelative(position.getY(),
								videoHeight);
						pointList.addPoint(x, y);
					}
					figure = OverlayFactory.getInstance()
							.createPointListFigure(pointList,
									OverlayType.POLYGON);
					TimeFigure newTimeFigure = new TimeFigure(figure, time);
					newTimeFigure.setMarkPosition(calculateMarkPosition(time));
					this.figMap.put(time, newTimeFigure);
				}
				break;
			case ELLIPSE:
				for (IMarkShape shape : markShapes) {
					EllipseShape ellipse = (EllipseShape) shape;
					time = ellipse.getTime();
					int x = getCoordFromRelative(ellipse.getX(), videoWidth);
					int y = getCoordFromRelative(ellipse.getY(), videoHeight);
					int width = getCoordFromRelative(ellipse.getLengthA(),
							videoWidth);
					int height = getCoordFromRelative(ellipse.getLengthB(),
							videoHeight);
					figure = OverlayFactory.getInstance()
							.createRectangleBoundFigure(x, y, width, height,
									OverlayType.ELLIPSE);
					TimeFigure newTimeFigure = new TimeFigure(figure, time);
					newTimeFigure.setMarkPosition(calculateMarkPosition(time));
					this.figMap.put(time, newTimeFigure);
				}
				break;
			case BUTTON:
				for (IMarkShape shape : markShapes) {
					PositionalShape button = (PositionalShape) shape;
					time = button.getTime();
					int x = getCoordFromRelative(button.getX(), videoWidth);
					int y = getCoordFromRelative(button.getY(), videoHeight);
					figure = OverlayFactory.getInstance()
							.createButtonFigure(x, y, buttonLabel);
					TimeFigure newTimeFigure = new TimeFigure(figure, time);
					newTimeFigure.setMarkPosition(calculateMarkPosition(time));
					this.figMap.put(time, newTimeFigure);
				}
				break;
			}
			TimeFigure first = this.figMap.get(this.startTimeInVid.getNano());
			// falls die Start Figure existitiert setze sie auf die aktuelle
			// ansonsten erzeuge sie noch
			if (first != null) {
				overlayFigure = OverlayFactory.getInstance().createOF(canvas,
						first.getFigure());
				canvas.setFigure(overlayFigure);
				this.selectedTimeFigure = first;
			} else {
				createFirstOverlayFigure();
			}
		} else {
			createFirstOverlayFigure();
		}
	}

	/**
	 * erstellt die OverlayFigure zur Zeit 0 relativ zur Annotation d.h. die
	 * erste OverlayFigure = Standard Overlay Figure z.B. wenn keine
	 * Markierungen/Overlays übergeben werden oder durch Zeitverschiebung
	 * Markierungen gelöscht wurden.
	 */
	private void createFirstOverlayFigure() {
		if (editMark) {
			switch (markType) {
			case POLYGON:
				overlayFigure = OverlayFactory.getInstance().createDefaultOF(
						canvas, OverlayType.POLYGON);
				break;
			case ELLIPSE:
				overlayFigure = OverlayFactory.getInstance().createDefaultOF(
						canvas, OverlayType.ELLIPSE);
				break;
			case BUTTON:
				overlayFigure = OverlayFactory.getInstance().createDefaultOF(
						canvas, OverlayType.BUTTON);
				break;
			}
		} else if (editOverlayPath) {
			overlayFigure = OverlayFactory.getInstance().createDefaultOF(
					canvas, OverlayType.RECTANGLE);			
		}
		this.overlayFigure.setAspectRatio(contentAspectRatio);
		overlayFigure.adjustAspect();
		canvas.setFigure(overlayFigure);
		long time = 0;
		if (this.startTimeInVid != null) {
			time = this.startTimeInVid.getNano();
		}
		TimeFigure newTimeFigure = new TimeFigure(overlayFigure, time);
		newTimeFigure.setMarkPosition(calculateMarkPosition(time));
		if (mp != null) {
			this.figMap.put(time, newTimeFigure);
		} else {
			this.figMap.put(0l, newTimeFigure);
		}
		this.selectedTimeFigure = newTimeFigure;

		if (scale != null) {
			scale.getSlider().redraw();
			scale.getSlider().update();
		}
	}

	/**
	 * erstellt aus den TimeFigures MarkShapes
	 * 
	 * Returns a list of shapes used to describe the look and the movement path
	 * of the shapes of a mark annotation.
	 * @return
	 */
	public ArrayList<IMarkShape> getMarkShapes() {
		ArrayList<IMarkShape> shapes = new ArrayList<IMarkShape>();
		if (this.editMark) {
			switch (this.markType) {
			case POLYGON:
				for (TimeFigure timeFigure : this.figMap.values()) {
					if (timeFigure.getFigure() instanceof Polygon) {
						Polygon curPoly = (Polygon) timeFigure.getFigure();
						PolygonShape markPolygon = new PolygonShape("", //$NON-NLS-1$
								Application.getCurrentProject());
						markPolygon.setTime(timeFigure.getTime());
						ArrayList<Position> positions = new ArrayList<Position>();
						PointList points = curPoly.getPoints();
						for (int i = 0; i < points.size(); i++) {
							Point curPoint = points.getPoint(i);
							Position newPosition = markPolygon.new Position();
							newPosition.setX(getRelativeFromCoord(curPoint.x,
									videoWidth));
							newPosition.setY(getRelativeFromCoord(curPoint.y,
									videoHeight));
							positions.add(newPosition);
						}
						markPolygon.setVertices(positions);
						shapes.add(markPolygon);
					}
				}
				return shapes;
			case ELLIPSE:
				for (TimeFigure timeFigure : this.figMap.values()) {
					if (timeFigure.getFigure() instanceof Ellipse) {
						Ellipse ellipse = (Ellipse) timeFigure.getFigure();
						EllipseShape markEllipse = new EllipseShape("", //$NON-NLS-1$
								Application.getCurrentProject());
						markEllipse.setTime(timeFigure.getTime());
						markEllipse.setLengthA(getRelativeFromCoord(
								ellipse.getBounds().width, videoWidth));
						markEllipse.setLengthB(getRelativeFromCoord(
								ellipse.getBounds().height, videoHeight));
						markEllipse.setX(getRelativeFromCoord(
								ellipse.getBounds().x, videoWidth));
						markEllipse.setY(getRelativeFromCoord(
								ellipse.getBounds().y, videoHeight));
						shapes.add(markEllipse);
					}
				}
				return shapes;
			case BUTTON:
				for (TimeFigure timeFigure : this.figMap.values()) {
					if (timeFigure.getFigure() instanceof MarkButtonFigure) {
						MarkButtonFigure button = (MarkButtonFigure) timeFigure
								.getFigure();
						PositionalShape markButton = new PositionalShape("", //$NON-NLS-1$
								Application.getCurrentProject());
						markButton.setTime(timeFigure.getTime());
						markButton.setX(getRelativeFromCoord(
								button.getBounds().x, videoWidth));
						markButton.setY(getRelativeFromCoord(
								button.getBounds().y, videoHeight));
						shapes.add(markButton);
					}
				}
				return shapes;
			}
		}
		return shapes;
	}

	// TIMEFIGURES <=> OVERLAYPATH
	/**
	 * erstellt aus den TimeFigures OverlayPathItems
	 * 
	 * Returns a list of items used to describe the movement path of an overlay
	 * annotation.
	 * @return list of movement path items 
	 */
	public ArrayList<OverlayPathItem> getOverlayPathItems() {
		ArrayList<OverlayPathItem> opis = new ArrayList<OverlayPathItem>();
		if (this.editOverlayPath) {
			for (TimeFigure timeFigure : this.figMap.values()) {
				if (timeFigure.getFigure() instanceof RectangleFigure) {
					RectangleFigure rectangle = (RectangleFigure) timeFigure
							.getFigure();
					long time = timeFigure.getTime();
					float x = getRelativeFromCoord(rectangle.getBounds().x,
							videoWidth);
					float y = getRelativeFromCoord(rectangle.getBounds().y,
							videoHeight);
					float width = getRelativeFromCoord(
							rectangle.getBounds().width, videoWidth);
					float height = getRelativeFromCoord(
							rectangle.getBounds().height, videoHeight);
					OverlayPathItem opi = new OverlayPathItem(x, y, width,
							height, time, Application.getCurrentProject());
					opis.add(opi);
				}
			}
		}
		return opis;
	}

	private void createTimeFiguresFromOverlayPath() {
		// initialisiere die TimeFigures, falls keine vorhanden sind wird
		// automatisch
		// eine Standard Figure erstellt
		if (pathItems != null && pathItems.size() > 0) {
			for (OverlayPathItem item : pathItems) {
				long time = item.getTime();
				int x = getCoordFromRelative(item.getX(), videoWidth);
				int y = getCoordFromRelative(item.getY(), videoHeight);
				int width = getCoordFromRelative(item.getWidth(), videoWidth);
				int height = getCoordFromRelative(item.getHeight(), videoHeight);
				Figure figure = OverlayFactory.getInstance()
						.createRectangleBoundFigure(x, y, width, height,
								OverlayType.RECTANGLE);
				TimeFigure newTimeFigure = new TimeFigure(figure, time);
				newTimeFigure.setMarkPosition(calculateMarkPosition(time));
				this.figMap.put(time, newTimeFigure);
			}
			TimeFigure first = null;
			if (this.startTimeInVid != null) {
				first = this.figMap.get(this.startTimeInVid.getNano());
			} else {
				first = this.figMap.get(0l);
			}
			// falls die Start Figure existitiert setze sie auf die aktuelle
			// ansonsten erzeuge sie noch
			if (first != null) {
				overlayFigure = OverlayFactory.getInstance().createOF(canvas,
						first.getFigure());
				canvas.setFigure(overlayFigure);
				selectedTimeFigure = first;
			} else {
				createFirstOverlayFigure();
			}
		} else {
			createFirstOverlayFigure();
		}
		this.overlayFigure.setAspectRatio(contentAspectRatio);
	}

	/*
	 * setzt den Inhalt des Overlays
	 */
	private void setContent(IAbstractBean content) {
		if (content instanceof RichText) {
			Browser viewer = new Browser(canvas.getContentComposite(),
					SWT.CENTER);
			viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			StringBuilder html = new StringBuilder();
			// verstecke die Scrollbalken
			html.append("<body style='overflow:hidden'>"); //$NON-NLS-1$
			html.append(((RichText) content).getContent());
			html.append("</body>"); //$NON-NLS-1$
			viewer.setText(html.toString());
			viewer.setEnabled(false);
		} else if (content instanceof Video || content instanceof Scene) {
			Video vid = null;
			if (content instanceof Video) {
				vid = (Video) content;
				mpVSO = PlayerFactory.getPlayer(vid, 0, mp.getEndTime()
						.getNano());
			} else if (content instanceof Scene) {
				Scene scene = (Scene) content;
				vid = scene.getVideo();
				SivaTime endTime = new SivaTime(0);
				if (mp != null) {
					endTime = new SivaTime(scene.getStart()).addTimeS(mp
							.getEndTime());
				}
				mpVSO = PlayerFactory.getPlayer(vid, scene.getStart(),
						endTime.getNano());
			}
//			if (mpVSO != null) {
//				mpVSO.mute();
//
//				// setze kurz die Zeit um, damit das Videobild aktualisiert wird
//				mpVSO.setMediaTime(new SivaEvent(null,
//						SivaEventType.MEDIATIME_CHANGED, mpVSO.getMediaTime()
//								.addTimeS(new SivaTime(1))));
//				mpVSO.setMediaTime(new SivaEvent(null,
//						SivaEventType.MEDIATIME_CHANGED, mpVSO.getMediaTime()
//								.addTimeS(new SivaTime(-1))));
//			}
			
			this.contentAspectRatio = vid.getDimension().getWidth()
					/ vid.getDimension().getHeight();

			// das Video wird direkt per SWT_AWT Bridge eingebunden
			// Component awtVideoComponent = mpVSO.getVisualComponent();
			mpVSO.createVisualPart(canvas.getContentComposite(),0,0,160,90);//TODO manu size?
			
			// sorgt dafür, dass die MausEvents einfach durchgehen und
			// an das dahinter liegende SWT Composite übergeben werden.
		}

		if (content instanceof Picture) {
			Picture picture = ((Picture) content);
			final Image imgContent = picture.getImage();
			final Composite contentComposite = canvas.getContentComposite();

			this.contentAspectRatio = picture.getDimension().getWidth()
					/ picture.getDimension().getHeight();

			contentComposite.addControlListener(new ControlListener() {

				@Override
				public void controlMoved(ControlEvent arg0) {
					// bei Bewegung muss das Bild nicht neu skaliert werden
				}

				@Override
				public void controlResized(ControlEvent arg0) {
					if (scaledContImage != null) {
						scaledContImage.dispose();
					}
					scaledContImage = new Image(Display.getCurrent(),
							imgContent.getImageData().scaledTo(
									contentComposite.getBounds().width,
									contentComposite.getBounds().height));
					contentComposite.setBackgroundImage(scaledContImage);
				}
			});
		}
	}
	
	/**
	 * Creates the label on bottom of the <code>OverlayEditor</code> showing
	 * position and size information for the actual annotation or mark shape
	 */
	private void createPositionandSizeLabel(){
		//erstelle Positionanzeige für Overlay
		Composite comp = new Composite(shell, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL,
				SWT.TOP, true, false));
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.horizontalSpacing = 10;
		comp.setLayout(compLayout);
		
		GridLayout groupLayout = new GridLayout(2, false);
		groupLayout.horizontalSpacing = 10;
		
		GridLayout subcompLayout = new GridLayout(2, false);
		subcompLayout.marginWidth = 0;
		subcompLayout.marginHeight = 0;
		
		Group posGroup = new Group(comp, SWT.NONE);
		posGroup.setLayout(groupLayout);
		posGroup.setText("Position");
		
		boolean posEditable = !(markType != null 
				&& markType.equals(NodeMarkType.POLYGON));
		boolean sizeEditable = !(markType != null 
				&& (markType.equals(NodeMarkType.BUTTON) 
					|| markType.equals(NodeMarkType.POLYGON)));
		
		// X-position
		Composite posXComp = new Composite(posGroup, SWT.NONE);
		posXComp.setLayout(subcompLayout);

		Label posXLabel = new Label(posXComp, SWT.NONE);
		posXLabel.setText(Messages.OverlayPathEditor_PositionLabel_x);
		SizedTextWithUnit posXText = new SizedTextWithUnit(posXComp, 
				SWT.SINGLE | SWT.RIGHT, 5, Messages.OverlayEditor_Unit_Pixel);
		posXField = posXText.getTextField();
		posXField.setEnabled(posEditable);
		
		// Y-position
		Composite posYComp = new Composite(posGroup, SWT.NONE);
		posYComp.setLayout(subcompLayout);

		Label posYLabel = new Label(posYComp, SWT.NONE);
		posYLabel.setText(Messages.OverlayPathEditor_PositionLabel_y);
		SizedTextWithUnit posYText = new SizedTextWithUnit(posYComp, 
				SWT.SINGLE | SWT.RIGHT, 5, Messages.OverlayEditor_Unit_Pixel);
		posYField = posYText.getTextField();
		posYField.setEnabled(posEditable);
				
		Group sizeGroup = new Group(comp, SWT.NONE);
		sizeGroup.setLayout(groupLayout);
		sizeGroup.setText("Size");
		// Width
		Composite widthComp = new Composite(sizeGroup, SWT.NONE);
		widthComp.setLayout(subcompLayout);

		Label widthLabel = new Label(widthComp, SWT.NONE);
		widthLabel.setText(Messages.OverlayPathEditor_PositionLabel_Width);
		SizedTextWithUnit widthText = new SizedTextWithUnit(widthComp, 
				SWT.SINGLE | SWT.RIGHT, 5, Messages.OverlayEditor_Unit_Pixel);
		widthField = widthText.getTextField();
		widthField.setEnabled(sizeEditable);

		// Height
		Composite heightComp = new Composite(sizeGroup, SWT.NONE);
		heightComp.setLayout(subcompLayout);

		Label heightLabel = new Label(heightComp, SWT.NONE);
		heightLabel.setText(Messages.OverlayPathEditor_PositionLabel_Height);
		SizedTextWithUnit heightText = new SizedTextWithUnit(heightComp, 
				SWT.SINGLE | SWT.RIGHT, 5, Messages.OverlayEditor_Unit_Pixel);
		heightField = heightText.getTextField();
		heightField.setEnabled(sizeEditable);
		
		NumericInputListener numeric = new NumericInputListener();
		posXField.addVerifyListener(numeric);
		posYField.addVerifyListener(numeric);
		widthField.addVerifyListener(numeric);
		heightField.addVerifyListener(numeric);
		AffirmOnTraverseListener traverse = new AffirmOnTraverseListener();
		posXField.addTraverseListener(traverse);
		posYField.addTraverseListener(traverse);
		widthField.addTraverseListener(traverse);
		heightField.addTraverseListener(traverse);
		AffirmOnFocusListener focus = new AffirmOnFocusListener();
		posXField.addFocusListener(focus);
		posYField.addFocusListener(focus);
		widthField.addFocusListener(focus);
		heightField.addFocusListener(focus);
			
		updateOverlayBounds();
	}

	/**
	 * Adjusts the value contained in the height field to match the actual
	 * width and aspect ratio.
	 */
	private void adjustHeight() {
		if (aspectSwitch != null && aspectSwitch.getSelection()) {
			int height = (int)Math.round(Double.parseDouble(widthField.getText())
						/ contentAspectRatio);
			heightField.setText(String.valueOf(height));
		}
	}
	
	/**
	 * Adjusts the value contained in the width field to match the actual
	 * height and aspect ratio.
	 */
	private void adjustWidth() {
		if (aspectSwitch != null && aspectSwitch.getSelection()) {
			int width = (int)Math.round(Double.parseDouble(heightField.getText())
						* contentAspectRatio);
			widthField.setText(String.valueOf(width));
		}
	}
	
	/**
	 * Updates the shown figure to match the size and position specified by the
	 * text fields.
	 */
	private void updateFigure() {
		int x = Integer.parseInt(posXField.getText());
		int y = Integer.parseInt(posYField.getText());
		int width = Integer.parseInt(widthField.getText());
		int height = Integer.parseInt(heightField.getText());
		overlayFigure.setContentBounds(x, y, width, height);
		updateOverlayBounds();	
	}
	
	/**
	 * Updates the positional information in the text fields to match the
	 * actual size and position of the figure.
	 */
	private void updateOverlayBounds() {
		org.eclipse.draw2d.geometry.Rectangle loc = overlayFigure
				.getContentFigure().getBounds();

		posXField.setText(String.valueOf(loc.x));
		posYField.setText(String.valueOf(loc.y));
		widthField.setText(String.valueOf(loc.width));
		heightField.setText(String.valueOf(loc.height));
	}
	
	/**
	 * Updates figure and positional information when focus on
	 * on the listening element is lost.
	 */
	private class AffirmOnFocusListener implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void focusLost(FocusEvent e) {
			Text source = (Text) e.getSource();
			if (source.getText().isEmpty()) {
				updateOverlayBounds();
			} else if (source == widthField) {
				adjustHeight();
			} else if (source == heightField) {
				adjustWidth();
			}
			updateFigure();
		}
	}
	
	/**
	 * Updates figure and positional information when enter key on listening
	 * element is pressed.
	 */
	private class AffirmOnTraverseListener implements TraverseListener {

		@Override
		public void keyTraversed(TraverseEvent e) {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				Text source = (Text) e.getSource();
				if (source.getText().isEmpty()) {
					updateOverlayBounds();
				} else if (e.getSource() == widthField) {
					adjustHeight();
				} else if (e.getSource() == heightField) {
					adjustWidth();
				}
				updateFigure();
			}
		}

	
	}
}