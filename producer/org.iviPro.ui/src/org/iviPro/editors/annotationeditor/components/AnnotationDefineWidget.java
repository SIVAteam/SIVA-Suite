package org.iviPro.editors.annotationeditor.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.application.Application;
import org.iviPro.editors.annotationeditor.AnnotationEditor;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationContentType;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationFactory;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationGroup;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.editors.annotationeditor.components.contenteditors.PictureEditor;
import org.iviPro.editors.annotationeditor.components.positioneditors.OverlayEditor;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.editors.common.EditTime;
import org.iviPro.editors.common.ScreenAreaSelector;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.mediaaccess.player.controls.SivaScale;
import org.iviPro.model.BeanList;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.annotation.IMarkShape;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeMarkType;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.annotation.AnnotationSaveOperation;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;
import org.iviPro.utils.NumericInputListener;
import org.iviPro.utils.SivaTime;

public class AnnotationDefineWidget extends AbstractAnnotationDefineWidget {
	
	// der MediaPlayer
	private MediaPlayer mp;

	// Start und Endzeit, wird für die Eingabefelder verwendet
	// diese Variablen entsprechen dem temporären Inhalt der
	// zu editierenden Annotation
	/**
	 * Annotation times relative to scene
	 */
	private SivaTime tmpStart;
	private SivaTime tmpEnd;
	
	// Button zum Einstellen ob die Annotation ausblendbar sein soll.
	private Button disableableButton;
	
	// Button zum Einstellen ob das Video pausiert werden soll wenn die
	// Annotation kommt
	private Button pauseVideoButton;

	// die NodeScene an der die Annotation hängt
	private NodeScene nodeScene;

	// das zuletzt empfangene Event der Skala (wird verwendet um
	// festzustellen welche OverlayPathItems/Markierungen ausserhalb des
	// eingestellten Bereich liegen
	private SivaEvent lastScaleEvent;

	// der zum Define Widget gehörende Editor, zum Anlegen einer neuen
	// Annotation
	private AnnotationEditor editor;
		
	// die Form der Markierung, wird verwendet falls eine Markierungsannotation
	// angelegt wird
	private NodeMarkType markType;

	// Anzeigerdauer der Markierung
	private long tmpMarkDuration;

	// TODO Comment und Bereinigen
	private String buttonLabel;

	// alle Markierungen
	private List<IMarkShape> markShapes;

	// gibt an ob es sich um eine Markierungsannotation handelt
	private boolean isMarkAnnotation = false;

	// aktuell markierte Mark Button
	private Button currentMarkShapeButton;
	
	private INodeAnnotation contentAnnotation;

	public AnnotationDefineWidget(Composite parent, int style,
			final INodeAnnotation annotation, AnnotationType annotationType,
			MediaPlayer mp, CTabItem it, NodeScene nodeS,
			AnnotationEditor editor) {
		super(parent, style, annotation, annotationType, it, editor);
		contentAnnotation = AnnotationFactory
				.getContentAnnotation(annotation);
		this.mp = mp;
		this.nodeScene = nodeS;
		this.editor = editor;
		
		//einstellungen fuer eine markierungs annotation laden
		this.isMarkAnnotation = annotationType.getAnnotationGroup().equals(
				AnnotationGroup.MARK) ? true : false;
		if (isMarkAnnotation) {
			tmpMarkDuration = ((NodeMark) annotation).getDuration();
			markShapes = ((NodeMark) annotation).getShapes();
			buttonLabel = ((NodeMark) annotation).getButtonLabel();
		}
		if (markShapes == null) {
			this.markShapes = new LinkedList<IMarkShape>();
		}

		// die temporären Zeiten entsprechen zu Beginn der Start und Endzeit
		// der Annotation, allerdings der relativen Zeit zur Szene
		// => die Startzeit der Szene wird abgezogen
		tmpStart = new SivaTime(annotation.getStart()
				- nodeScene.getScene().getStart());
		tmpEnd = new SivaTime(annotation.getEnd()
				- nodeScene.getScene().getStart());
		createContent();
	}

	/**
	 * erstellt den Content, entspricht den aktuellen Einstellungsmöglichkeiten
	 * für alle Annotationen
	 */
	protected void createContent() {
		this.setLayout(new GridLayout(2, false));
		
		TopComponent top = new TopComponent(this, SWT.NONE);
		LeftComponent left = new LeftComponent(this, SWT.BORDER);
		contentEditorComposite = new RightComponent(this, SWT.NONE);
		
		addListeners(top, left);

		SivaEvent startEvent = new SivaEvent(null,
				SivaEventType.STARTTIME_CHANGED, tmpStart);
		SivaEvent endEvent = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED,
				tmpEnd);
		
		// Need to set end sash first. Otherwise start time would be later than
		// end time which would be ignored by setSash().
		top.scale.setSashes(endEvent);
		top.scale.setSashes(startEvent);		
		// setze den Markierungspunkt
		top.scale.addMarkPoint(tmpStart.getNano(), ""); //$NON-NLS-1$
		// initialisiere die konkrete Annotation
		initContentEditors();
	}
	
	/**
	 * Adding listeners to the components of the widget.
	 * @param top the top component of the widget
	 * @param left the left component of the widget
	 */
	private void addListeners(final TopComponent top, final LeftComponent left) {
		// Add listener to the time line component
		top.scale.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				lastScaleEvent = event;
				// setze die Media Zeit wenn auf die Skala geklickt wird
				mp.setMediaTime(lastScaleEvent);
				SivaEvent forwardEvent = null;
				// setze die Markierungspunkte auf dem Videoslider
				if (lastScaleEvent.getEventType().equals(
						SivaEventType.STARTTIME_CHANGED)) {
					forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_START, lastScaleEvent
							.getTime());
				} else if (lastScaleEvent.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_END, lastScaleEvent
							.getTime());
				}
				mp.forwardEvent(forwardEvent);
				left.editTime.setValue(lastScaleEvent);
				top.scale.addMarkPoint(lastScaleEvent.getTime().getNano(), ""); //$NON-NLS-1$
			}
		});

		// Add listener to annotation start button
		top.cutStart.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				if (mp.getMediaTime().compareTo(tmpEnd) >= 0) {
					Shell shell = new Shell(Display.getDefault());
					MessageDialog.openInformation(shell,
							Messages.AnnotationDefineWidget_Define,
							Messages.AnnotationDefineWidget_Start);
				} else {
					boolean change = checkTimeOverlay(mp.getMediaTime(), tmpEnd);
					if (change) {
						tmpStart = mp.getMediaTime();
						SivaEvent event = new SivaEvent(null,
								SivaEventType.STARTTIME_CHANGED, mp
								.getMediaTime());
						top.scale.setSashes(event);
						left.editTime.setValue(event);
						
						// Update the duration if this is a mark annotation
						if (isMarkAnnotation) {
							left.updateDuration();
						}
						SivaEvent forwardEvent = new SivaEvent(null,
								SivaEventType.MARK_POINT_START, mp
								.getMediaTime());
						mp.forwardEvent(forwardEvent);
					}
				}
			}
		});

		// Add listener to annotation end button
		top.cutEnd.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				// Endzeit darf nicht vor der Startzeit liegen
				if (mp.getMediaTime().compareTo(tmpStart) <= 0) {
					Shell shell = new Shell(Display.getDefault());
					MessageDialog.openInformation(shell,
							Messages.AnnotationDefineWidget_Define,
							Messages.AnnotationDefineWidget_End);
				} else {
					boolean change = checkTimeOverlay(tmpStart,
							mp.getMediaTime());
					if (change) {
						tmpEnd = mp.getMediaTime();
						SivaEvent event = new SivaEvent(null,
								SivaEventType.ENDTIME_CHANGED, mp
								.getMediaTime());
						top.scale.setSashes(event);
						left.editTime.setValue(event);
						SivaEvent forwardEvent = new SivaEvent(null,
								SivaEventType.MARK_POINT_END, mp.getMediaTime());
						mp.forwardEvent(forwardEvent);
					}
				}
			}
		});

		// Add listener for the save-and-create button
		top.saveCreateButton.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				executeSaveOperation();
				Point pt = new Point(top.saveCreateButton.getBounds().x - 54,
						top.saveCreateButton.getBounds().y);
				pt = top.saveCreateButton.toDisplay(pt);
				top.menu.setLocation(pt.x, pt.y);
				top.menu.setVisible(true);
			}
		});

		// Add listener to the component where annotation times can be set textually
		left.editTime.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				
				if (event.getEventType()
						.equals(SivaEventType.STARTTIME_CHANGED)) {
					boolean change = checkTimeOverlay(event.getTime(), tmpEnd);
					if (change) {
						tmpStart = event.getTime();
						mp.setMediaTime(event);
						top.scale.setSashes(event);
					} else {
						SivaEvent startEvent = new SivaEvent(null,
								SivaEventType.STARTTIME_CHANGED, tmpStart);
						left.editTime.setValue(startEvent);
					}
					// Update the duration if this is a mark annotation
					if (isMarkAnnotation) {
						left.updateDuration();
					}
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					boolean change = checkTimeOverlay(tmpStart, event.getTime());
					if (change) {
						tmpEnd = event.getTime();
						mp.setMediaTime(event);
						top.scale.setSashes(event);
					} else {
						SivaEvent endEvent = new SivaEvent(null,
								SivaEventType.ENDTIME_CHANGED, tmpEnd);
						left.editTime.setValue(endEvent);
					}
				}
				top.scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				updateDirty();
			}
		});
		
		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				// ignoriere die Events des MediaPlayers die von der Skala
				// ausgelöst wurden
				if (event.getSource() != null
						&& event.getSource().equals(top.scale)) {
					return;
				}
				if (event.getEventType()
						.equals(SivaEventType.MEDIATIME_CHANGED)) {
					top.scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				}
			}
		});
		
		/**
		 * mit dem Listener wird geprüft, ob mehrere Overlaypath-Objekte
		 * gleichzeitig gelöscht werden müssen, falls das löschen abgebrochen
		 * wird, wird die Skala auf die ursprüngliche Position gesetzt
		 */
		MouseListener scaleListener = new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {

			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				if (lastScaleEvent == null) {
					return;
				}
				if (lastScaleEvent.getEventType().equals(
						SivaEventType.STARTTIME_CHANGED)) {
					boolean change = checkTimeOverlay(lastScaleEvent.getTime(),
							tmpEnd);
					if (change) {
						tmpStart = lastScaleEvent.getTime();
						// Update the duration if this is a mark annotation
						if (isMarkAnnotation) {
							left.updateDuration();
						}
					} else {
						SivaEvent startEvent = new SivaEvent(null,
								SivaEventType.STARTTIME_CHANGED, tmpStart);
						top.scale.setSashes(startEvent);
						mp.setMediaTime(startEvent);
						left.editTime.setValue(startEvent);
					}
				} else if (lastScaleEvent.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					boolean change = checkTimeOverlay(tmpStart,
							lastScaleEvent.getTime());
					if (change) {
						tmpEnd = lastScaleEvent.getTime();
					} else {
						SivaEvent endEvent = new SivaEvent(null,
								SivaEventType.ENDTIME_CHANGED, tmpEnd);
						top.scale.setSashes(endEvent);
						mp.setMediaTime(endEvent);
						left.editTime.setValue(endEvent);
					}
				}
				updateDirty();
			}
		};
		top.scale.addSivaSliderMouseListener(scaleListener);

		/**
		 * No idea why here an async call is used to update the title.
		 * Still might be some hidden problem solution so I'll comment it out for
		 * later reference.
		 */
//		if (isMarkAnnotation) {
//			contentAnnotation.addPropertyChangeListener(new PropertyChangeListener() {
//				@Override
//				public void propertyChange(PropertyChangeEvent evt) {
//					if (isDisposed()) {
//						return;
//					}
//					if (!tabItem.isDisposed()) {
//						Display.getDefault().asyncExec(new Runnable() {
//							@Override
//							public void run() {
//								if (!tabItem.isDisposed()) {
//									tabItem.setText(annotation.getTitle());
//								}
//							}
//						});
//
//					}
//					// setze den Inhalt neu
//					initContentEditors();
//				}
//			});
//		}
		
		// refresh all gui elements when properties have been changed (also see
		// listener added to contentAnnotation below
		annotation.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isDisposed()) {
					return;
				}

				// man kann hier alles gleichzeitig setzen und muss nicht auf
				// einzelne
				// Properties prüfen, aber jedes Property dass geändert wird
				// muss auch
				// ein Event feuern (in der entspr. Modelklasse) z.B.
				// INodeAnnotation
				if (annotation != null) {
					tmpStart = new SivaTime(annotation.getStart()
							- nodeScene.getScene().getStart());
					tmpEnd = new SivaTime(annotation.getEnd()
							- nodeScene.getScene().getStart());
									
					SivaEvent eventStart = new SivaEvent(null,
							SivaEventType.STARTTIME_CHANGED, tmpStart);
					SivaEvent eventEnd = new SivaEvent(null,
							SivaEventType.ENDTIME_CHANGED, tmpEnd);
					top.scale.setSashes(eventStart);
					top.scale.setSashes(eventEnd);
					left.editTime.setValue(eventStart);
					left.editTime.setValue(eventEnd);

					// bei Audio und Subtitle Annotationen gibt es keine
					// Positionsauswahl
					if (!(annotationType.getContentType()
									.equals(AnnotationContentType.SUBTITLE))) {
						if (!areaSelector.isDisposed()) {
							tmpScreenArea = annotation.getScreenArea();
							areaSelector.setScreenArea(tmpScreenArea);
						}
						tmpOpItems = annotation.getOverlayPath();
					}

					if (disableableButton != null) {
						disableableButton.setSelection(annotation.isDisableable());
					}
					if (!annotationType.getContentType().equals(
							AnnotationContentType.SUBTITLE)) {
						pauseVideoButton.setSelection(annotation.isPauseVideo());
					}
					if (muteVideoButton != null) {
						muteVideoButton.setSelection(annotation.isMuteVideo());
					}

					if (!tabItem.isDisposed()) {
						tabItem.setText(annotation.getTitle());
					}
				}
			}
		});
		
		// some PropertyChangeEvents are created in the contentAnnotation
		contentAnnotation.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isDisposed()) {
					return;
				}
				
				tmpKeywords.setText(contentAnnotation.getKeywords());
				
				String description = (contentAnnotation.getDescription());
				tmpDescription.setText((description != null ? description : "")); //$NON-NLS-1$
				
				if (!tabItem.isDisposed()) {
					tabItem.setText(annotation.getTitle());
				}
				if (event.getPropertyName().equals(INodeAnnotation.PROP_SETCONTENT)) {
					initContentEditors();
				}
			}
		});
	}

	/**
	 * Checks if items of an overlay or mark path are timed outside the given
	 * time frame. If this is the case a dialog box is presented and the 
	 * respective path items are deleted if the user confirms to do so.
	 * Returns wether or not the procedure has been aborted by the user.
	 * 
	 * @param startTime start time of the time frame which should be checked
	 * @param endTime end time of the time frame which should be checked
	 * @return false if path items are outside the time frame and the user
	 * chose to cancel the procedure - true otherwise
	 */
	private boolean checkTimeOverlay(SivaTime startTime, SivaTime endTime) {
		if (!this.isMarkAnnotation
				&& (!(tmpScreenArea != null) 
						&& !tmpScreenArea.equals(ScreenArea.OVERLAY))) {
			return true;
		}
		
		// If new start time has become smaller, try to set initial overlay
		// path and mark items.
		if (startTime.compareTo(tmpStart) < 0) {
			if (!tmpOpItems.isEmpty()) {
				OverlayPathItem firstOPI = tmpOpItems.get(0);
				firstOPI.setTime(startTime.getNano());
				tmpOpItems.add(0, firstOPI);
			}
			if (!markShapes.isEmpty()) {
				IMarkShape firstShape = markShapes.get(0);
				firstShape.setTime(startTime.getNano());
				markShapes.add(0, firstShape);
			}
		}
		
		// Add the scene start time, since timestamps in overlay path items
		// and mark shapes are absolute.
		startTime = startTime.addTimeS(nodeScene.getScene().getStart());
		endTime = endTime.addTimeS(nodeScene.getScene().getEnd());
		
		// prüfe die OverlayPath Items
		LinkedList<OverlayPathItem> removeOPI = new LinkedList<OverlayPathItem>();
		// suche die OverlayPathItems die raus fallen
		for (OverlayPathItem opi : tmpOpItems) {
			SivaTime curItemTime = new SivaTime(opi.getTime());
			// prüfe den Bereich
			if (curItemTime.compareTo(startTime) < 0
					|| curItemTime.compareTo(endTime) > 0) {
				removeOPI.add(opi);
			}
		}

		// prüfe die Markierungen
		LinkedList<IMarkShape> removeShapes = new LinkedList<IMarkShape>();
		// suche die Markierungen die raus fallen
		for (IMarkShape shape : markShapes) {
			SivaTime curShapeTime = new SivaTime(shape.getTime());
			// prüfe den Bereich
			if (curShapeTime.compareTo(startTime) < 0
					|| curShapeTime.compareTo(endTime) > 0) {
				removeShapes.add(shape);
			}
		}

		// gib eine Warnung aus falls Overlay Path Items gelöscht werden würden
		if (removeOPI.size() > 0 || removeShapes.size() > 0) {
			boolean check = MessageDialog.openQuestion(getShell(),
					Messages.AnnotationEditor_RemoveOverlay_Warning,
					Messages.AnnotationEditor_RemoveOverlay_Question);
			if (check) {
				if (!tmpOpItems.isEmpty()) {
					OverlayPathItem firstOPI = tmpOpItems.get(0);
					tmpOpItems.removeAll(removeOPI);
					// if there is no path item at the start time after deletion
					// use the former first item
					if (tmpOpItems.isEmpty() 
							|| tmpOpItems.get(0).getTime() != startTime.getNano()) {
						firstOPI.setTime(startTime.getNano());
						tmpOpItems.add(0, firstOPI);
					}
				}
				if (!markShapes.isEmpty()) {
					IMarkShape firstShape = markShapes.get(0);
					markShapes.removeAll(removeShapes);
					// if there is no path item at the start time after deletion
					// use the former first item
					if (markShapes.isEmpty() 
							|| markShapes.get(0).getTime() != startTime.getNano()) {
						firstShape.setTime(startTime.getNano());
						markShapes.add(0, firstShape);
					}
				}
			}
			return check;
		}
		return true;
	}

	@Override
	public boolean executeSaveOperation() {
		// prüfe ob der Inhalt gesetzt wurde
		if (!checkContentSet()) {
			return false;
		}
		
		// Check if overlay path items are set and positional information for 
		// the start time of the annotation exists
		if (tmpScreenArea != null && tmpScreenArea.equals(ScreenArea.OVERLAY)) {
			if (tmpOpItems.isEmpty() 
					|| tmpOpItems.get(0).getTime() != tmpStart.addTimeS(
							nodeScene.getScene().getStart()).getNano()) {
				MessageBox messageBox = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.ICON_ERROR);
				messageBox.setMessage(Messages
						.AnnotationDefineWidget_Missing_Overlay_Path_Item);
				messageBox.open();
				return false;
			}
		}
		
		// In case a mark annotation should be saved, check if mark shapes are 
		// set and positional information for the start time of the annotation
		// exists. Needed since creation of a mark anno or changing the shape
		// does not create a default position for the shape yet (list is empty).
		if (isMarkAnnotation) { 
			if (markShapes.isEmpty() 
					|| markShapes.get(0).getTime() != tmpStart.addTimeS(
							nodeScene.getScene().getStart()).getNano()) {
				MessageBox messageBox = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.ICON_ERROR);
				messageBox.setMessage(Messages
						.AnnotationDefineWidget_Missing_Mark_Shape_Path_Item);
				messageBox.open();
				return false;
			}
		}
		
		List<INodeAnnotation> annos = nodeScene.getAnnotations();
		BeanList<INodeAnnotation> annoList = new BeanList<INodeAnnotation>(
				Application.getCurrentProject());
		annoList.addAll(annos);
		String prefix = Messages.AnnotationPrefix;
		if (isMarkAnnotation) {
			prefix = "Mark-" + prefix; //$NON-NLS-1$						
		}
		BeanNameGenerator nameGen = new BeanNameGenerator(tmpTitle.getText(),
				annotation, annoList, prefix);
		String newTitle = nameGen.generate();
		// falls die Namensgenerierung abgebrochen wurde, wird nicht gespeichert
		if (!nameGen.getCancelState()) {
			tmpTitle.setText(newTitle);
			if (isDirty()) {
				
				String description = tmpDescription.getText();
				if (description.isEmpty()) {
					description = null;
				}
				
				long start = tmpStart.getNano()
						+ nodeScene.getScene().getStart();
				long end = tmpEnd.getNano() + nodeScene.getScene().getStart();
				boolean disableable = false;
				if (disableableButton != null) {
					disableable = disableableButton.getSelection();
				}
				boolean mute = false;
				if (muteVideoButton != null) {
					mute = muteVideoButton.getSelection();
				}
				boolean pause = false;
				if (pauseVideoButton != null) {
					pause = pauseVideoButton.getSelection();
				}
				String keywords = ""; //$NON-NLS-1$
				if (tmpKeywords != null) {
					keywords = tmpKeywords.getText();
				}
							
				IAbstractOperation op = new AnnotationSaveOperation(annotation,
						annotationType, nodeScene, tmpTitle.getText(), description, 
						start, end, keywords, disableable, pause, mute, markType, 
						markShapes,	tmpMarkDuration, buttonLabel, editorContent, 
						tmpContentDescription, tmpThumbnailTime, tmpScreenArea, 
						tmpOpItems);
				try {
					OperationHistory.execute(op);
				} catch (ExecutionException e) {
					e.printStackTrace();
					return false;
				}
			}
		} else {
			return false;
		}
		updateDirty();
		return true;
	}

	@Override
	public boolean localIsDirty() {
		Graph graph = nodeScene.getGraph();

		// Die Annotation ist auf jeden Fall Dirty wenn sie noch nicht
		// gespeichert wurde
		if (!graph.containsConnection(nodeScene, annotation)) {
			return true;
		}
		
		if (tmpKeywords != null
				&& !contentAnnotation.getKeywords().equals(tmpKeywords.getText())) {
			return true;
		}
		
		if (disableableButton != null) {
			if (annotation.isDisableable() != disableableButton.getSelection()) {
				return true;
			}				
		}
		if (!annotationType.getContentType().equals(
				AnnotationContentType.SUBTITLE)) {
			if (annotation.isPauseVideo() != pauseVideoButton.getSelection()) {
				return true;
			}
		}

		// bei Markierungsannotationen müssen die Änderung des MarkType geprüft
		// werden
		// und die Markierungspositionen an sich
		if (isMarkAnnotation) {
			NodeMark nodeMark = (NodeMark) annotation;
			if (!nodeMark.getType().equals(this.markType)) {
				return true;
			}
			
			if (nodeMark.getDuration() != tmpMarkDuration) {
				return true;
			}

			if ((nodeMark.getButtonLabel() == null && buttonLabel != null) 
					||	(nodeMark.getButtonLabel() != null 
						&& !nodeMark.getButtonLabel().equals(buttonLabel))) {
				return true;
			}

			// prüfe Markierungspositionen
			if (nodeMark.getShapes().size() != markShapes.size()) {
				return true;
			}
			for (IMarkShape shape : nodeMark.getShapes()) {
				boolean foundSameTime = false;
				for (IMarkShape newShape : markShapes) {
					// suche nach Objekten mit der gleichen Zeit, falls keines
					// gefunden wurde
					// gab es eine Änderung
					// falls eines gefunden wurde vergleiche die Shapes
					if (shape.getTime() == newShape.getTime()) {
						foundSameTime = true;
						if (!shape.isShapeEqual(newShape)) {
							return true;
						}
					}
				}
				if (!foundSameTime) {
					return true;
				}
			}
		}

		if (annotation.getStart() != tmpStart.getNano()
				+ nodeScene.getScene().getStart()) {
			return true;
		}
		if (annotation.getEnd() != tmpEnd.getNano()
				+ nodeScene.getScene().getStart()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Component containing the time line where start and end times can be set
	 * by using sliders, the buttons which can be used to set start and end 
	 * times according to the actual position in the video player and the 
	 * buttons for saving the annotation.
	 * @author John
	 */
	private class TopComponent extends Composite {
		
		/** 
		 * Scale showing the timeline of the scene along with the sashes
		 * for the start and end time of the annotation.
		 */ 
		private SivaScale scale;
		/** Button used for setting annotation start time. */ 
		private Button cutStart;
		/** Button used for setting annotation end time. */ 
		private Button cutEnd;
		/** Button used for saving the annotation. */
		private  Button saveButton;
		/** 
		 * Button used for saving the current annotation and instantly
		 * creating a new one.
		 */
		private Button saveCreateButton;
		/** Menu displaying the options for creating a new annotation. */
		private Menu menu;
		
		TopComponent(Composite parent, int style) {
			super(parent, style);
			GridData scaleCompGD = new GridData();
			scaleCompGD.horizontalSpan = 2;
			this.setLayoutData(scaleCompGD);
			
			GridLayout scaleCompGL = new GridLayout(3, false);
			scaleCompGL.marginWidth = 0;
			this.setLayout(scaleCompGL);

			scale = new SivaScale(this, nodeScene.getScene()
					.getEnd() - nodeScene.getScene().getStart(), 520, 40, true,
					true, true);
			scale.setToolTip(Messages.AnnotationDefineWidgetScaleTooltip);
			
			// Container für die Buttons
			final Composite defineSceneButtons = new Composite(this, SWT.NONE);
			
			// Layout für die Buttons
			GridLayout layoutSceneControls = new GridLayout(3, false);
			layoutSceneControls.marginWidth = 0;
			layoutSceneControls.marginHeight = 0;
			layoutSceneControls.verticalSpacing = 0;
			layoutSceneControls.horizontalSpacing = 0;
			defineSceneButtons.setLayout(layoutSceneControls);

			// Button zur Definition des Annotationsstart
			cutStart = new Button(defineSceneButtons, SWT.CENTER);
			ImageHelper.setButtonImage(cutStart, Icons.ACTION_ANNOTATION_CUT_START);
			cutStart.setToolTipText(Messages.AnnotationDefineWidget_StartPosition);
			
			// Button zur Definition des Szenenendes
			cutEnd = new Button(defineSceneButtons, SWT.CENTER);
			ImageHelper.setButtonImage(cutEnd, Icons.ACTION_ANNOTATION_CUT_END);
			cutEnd.setToolTipText(Messages.AnnotationDefineWidget_EndPosition);
			
			// Button zum Abspeichern der Annotation
			saveButton = new Button(defineSceneButtons, SWT.CENTER);
			saveButton.setText(Messages.AnnotationDefineWidget_Text_SaveButton);
			GridData saveButtonGD = new GridData();
			saveButtonGD.horizontalIndent = 30;
			saveButton.setLayoutData(saveButtonGD);
			// Bild für den Button
			ImageHelper.setButtonImage(saveButton, Icons.ACTION_ANNOTATION_SAVE);
			saveButton.setToolTipText(
					Messages.AnnotationDefineWidget_Tooltip_SaveButton);
			saveButton.addListener(SWT.MouseDown, new Listener() {
				public void handleEvent(Event e) {
					executeSaveOperation();
				}
			});
			
			// Button zum Speichern der Szene und Anlegen einer neuen
			saveCreateButton = new Button(defineSceneButtons,
					SWT.CENTER);
			saveCreateButton
			.setText(Messages.AnnotationDefineWidget_Text_SaveCreateButton);
			GridData savecreButtonGD = new GridData();
			savecreButtonGD.horizontalIndent = 86;
			savecreButtonGD.horizontalSpan = 3;
			saveCreateButton.setLayoutData(savecreButtonGD);
			// Bild für den Button
			ImageHelper.setButtonImage(saveCreateButton,
					Icons.ACTION_ANNOTATION_SAVE);
			saveCreateButton.setToolTipText(
					Messages.AnnotationDefineWidget_Tooltip_SaveCreateButton);
			menu = new Menu(Display.getCurrent().getActiveShell(),
					SWT.POP_UP);
			saveCreateButton.setMenu(menu);
			
			// füge die Gruppen in das Auswahlmenü ein
			for (AnnotationGroup group : AnnotationGroup.values()) {
				if (group.inUse()) {
					MenuItem groupItem = new MenuItem(menu, SWT.CASCADE);
					groupItem.setText(group.getName());

					// das Untermenü mit der eigentlichen Annotationsauswahl
					Menu annoMenu = new Menu(menu);
					groupItem.setMenu(annoMenu);

					// füge die zur Gruppe gehörenden Annotationen als Untermenü ein
					for (final AnnotationType at : AnnotationType.values()) {
						// füge die Standard Annotationen ein
						if (at.inUse() && at.getAnnotationGroup().equals(group)) {
							final MenuItem mitem = new MenuItem(annoMenu, SWT.PUSH);
							mitem.setText(editor.getAnnotationMenuTitle(at
									.getName()));
							mitem.setData(at);
							mitem.addListener(SWT.Selection, new Listener() {
								@Override
								public void handleEvent(Event event) {
									editor.createNewAnnotation(at, mp);
								}
							});
						}
					}
				}
			}
		}
	}
	
	/**
	 * Component containing the content and the content editing functions for
	 * the annotation.
	 * @author John
	 *
	 */
	private class RightComponent extends Composite {
		RightComponent(Composite parent, int style) {
			super(parent, style);
			GridData rightGD = new GridData(SWT.FILL, SWT.FILL, true, true);
			this.setLayoutData(rightGD);
			GridLayout rightLayout = new GridLayout(1, false);
			this.setLayout(rightLayout);
		}
	}
	
	/**
	 * Component containing the text fields for editing the start and end 
	 * times and the annotation dependent settings like duration, screen area,
	 * or shape of marks.
	 * @author John
	 */
	private class LeftComponent extends Composite {
		
		/**
		 * Component containing textfields for start and end time selection
		 */
		private EditTime editTime;
		
		/**
		 * Components for setting duration
		 */
		private Text dHours;
		private Text dMins;
		private Text dSecs;
		private Text dMillis;
		
		/**
		 * Component containing the screen area selector
		 */
		private Composite contentPositionGroup;
		
		LeftComponent(Composite parent, int style) {
			super(parent, style);
			this.setLayout(new GridLayout(1, false));
			this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			
			addTitleComp(this);			
			addDescriptionComp(this);
			
			if (annotationType.getContentType().equals(
					AnnotationContentType.PICTURE)) {
				addPictureAnnotationComp(this);
			}
			
			addTimingComp(this);
			
			if (!(annotationType.getContentType()
							.equals(AnnotationContentType.SUBTITLE))) {
				addPositionEditor(this);
			}

			addKeywordComp(this);
			
			addToggleComp(this);
		}
		
		/**
		 * Adds a component for defining a unique title for an annotation by 
		 * which it can be identified internally.
		 * @param parent parent to which the component is added
		 */
		private void addTitleComp(Composite parent) {
			// Composite für den Namen
			Group titleGroup = new Group(parent, SWT.NONE);
			titleGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					false, false));
			titleGroup.setLayout(new GridLayout(1, false));
			titleGroup.setText(Messages.AnnotationDefineWidget_Label_Identifier);
			tmpTitle = new Text(titleGroup, SWT.SINGLE | SWT.BORDER);
			//tmpTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
			//		false, false));
			tmpTitle.setText(annotation.getTitle());

			tmpTitle.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
					// setze die Annotation auf dirty
					updateDirty();
				}
			});
			GridData titleGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
			titleGD.heightHint = 15;
			tmpTitle.setLayoutData(titleGD);
		}
		
		/**
		 * Adds a component for adding a description to an annotation.
		 * @param parent parent to which the component is added
		 */
		private void addDescriptionComp(Composite parent) {
			// Composite for description
			Group descriptionGroup = new Group(parent, SWT.NONE);
			descriptionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					false, false));
			descriptionGroup.setLayout(new GridLayout(1, false));
			descriptionGroup.setText(Messages.AnnotationDefineWidget_Label_Title);
			tmpDescription = new Text(descriptionGroup, SWT.SINGLE | SWT.BORDER);
			String description = contentAnnotation.getDescription();
			if (description == null) {
				description = ""; //$NON-NLS-1$
			}
			tmpDescription.setText(description);
			tmpDescription.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
					// setze die Annotation auf dirty
					updateDirty();
				}
			});
			GridData descriptionGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
			descriptionGD.heightHint = 15;
			tmpDescription.setLayoutData(descriptionGD);
		}

		/**
		 * Adds a component for switching between a single picture and a
		 * picture gallery for picture annotations. 
		 * @param parent parent to which the component is added
		 */
		private void addPictureAnnotationComp(Composite parent) {
			Group pictureGroup = new Group(parent, SWT.NONE);
			pictureGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, 
					false, false));
			pictureGroup.setLayout(new GridLayout(2, false));
			pictureGroup.setText(Messages.PictureOrPictureGalleryText);
			picButton = new Button(pictureGroup, SWT.RADIO);
			picButton.setText(Messages.PictureButton);
			galButton = new Button(pictureGroup, SWT.RADIO);
			galButton.setText(Messages.PictureGalleryButton);
			final Composite columnsComp = new Composite(pictureGroup, SWT.NONE);
			GridData columnsCompGD = new GridData();
			columnsCompGD.horizontalSpan = 2;
			columnsCompGD.heightHint = 20;
			columnsComp.setLayoutData(columnsCompGD);
			columnsComp.setLayout(new GridLayout(2, false));
			Label pictureAnnoColumnFieldLab = new Label(columnsComp, SWT.CENTER);
			pictureAnnoColumnFieldLab.setText(Messages.NumberColumns);
			pictureAnnoColumnField = new Text(columnsComp, SWT.SINGLE);
			if (((NodeAnnotationPicture) contentAnnotation).getContentType() 
					== NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				pictureAnnoColumnField.setEnabled(true);
				int col = ((NodeAnnotationPicture) contentAnnotation)
						.getPictureGallery().getNumberColumns();
				pictureAnnoColumnField.setText(Integer.toString(col));
			} else {
				pictureAnnoColumnField.setEnabled(false);
				pictureAnnoColumnField.setText("1"); //$NON-NLS-1$
			}
			GridData gd = new GridData();
			gd.widthHint = 40;
			pictureAnnoColumnField.setLayoutData(gd);
			pictureAnnoColumnField.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent ve) {
					String string = ve.text;
					char[] chars = new char[string.length()];
					string.getChars(0, chars.length, chars, 0);
					for (int i = 0; i < chars.length; i++) {
						if (!('0' <= chars[i] && chars[i] <= '9')) {
							ve.doit = false;
							return;
						}
					}
				}
			});
			pictureAnnoColumnField.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					PictureEditor picEditor = ((PictureEditor)contentEditor);
					if (picEditor != null && pictureAnnoColumnField != null) {
						if (pictureAnnoColumnField.getText().length() > 0) {
							Integer cols = Integer
									.parseInt(pictureAnnoColumnField.getText());
							picEditor.setColumns(cols);
							updateDirty();
						}
					}
				}

			});
			final MessageBox warnBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
			warnBox.setMessage(Messages.PictureEditor_SwitchWarningMsg);
			warnBox.setText(Messages.PictureEditor_SwitchWarningText);
			picButton.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
				}

				@Override
				public void mouseDown(MouseEvent arg0) {
					if (!picButton.getSelection()) {
						int res = warnBox.open();
						if (res == SWT.OK) {
							picButton.setSelection(true);
							galButton.setSelection(false);
							pictureAnnoColumnField.setEnabled(false);
							pictureAnnoColumnField.setText("1"); //$NON-NLS-1$
							Display.getCurrent().asyncExec(new Runnable() {
								@Override
								public void run() {
									PictureEditor picEditor = ((PictureEditor)contentEditor);
									picEditor.setPicAnnoContentType(NodeAnnotationPicture.CONTENT_PICTURE);
									picEditor.setContent(null);
									updateDirty();
								}
							});
						}
					}
				}

				@Override
				public void mouseUp(MouseEvent arg0) {

				}
			});
			galButton.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
				}

				@Override
				public void mouseDown(MouseEvent arg0) {
					if (!galButton.getSelection()) {
						int answer = warnBox.open();
						if (answer == SWT.OK) {
							galButton.setSelection(true);
							picButton.setSelection(false);
							pictureAnnoColumnField.setEnabled(true);
							if (editorContent instanceof PictureGallery) {
								pictureAnnoColumnField.setText("" //$NON-NLS-1$
										+ ((PictureGallery) editorContent)
										.getNumberColumns());
							} else {
								pictureAnnoColumnField.setText("" //$NON-NLS-1$
										+ PictureGallery.PICGAL_COLS_STD);
							}

							Display.getCurrent().asyncExec(new Runnable() {
								@Override
								public void run() {
									PictureEditor picEditor = ((PictureEditor)contentEditor);
									picEditor.setPicAnnoContentType(NodeAnnotationPicture.CONTENT_PICTUREGALLERY);
									picEditor.setContent(null);
									updateDirty();
								}
							});
						}
					}
				}

				@Override
				public void mouseUp(MouseEvent arg0) {
				}
			});
		}
		/** 
		 * Adds a component for editing timings of an annotation.
		 * @param parent parent to which the component is added
		 */
		private void addTimingComp(Composite parent) {
			Group timingGroup = new Group(parent, SWT.NONE);
			timingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, 
					false, false));
			timingGroup.setLayout(new GridLayout(1, false));
			timingGroup.setText(Messages.AnnotationDefineWidget_Timing);
			
			// erstelle das Composite zum Editieren der Zeit
			// der EditTime Editor arbeitet mit der relativen Zeit zur Szene
			// => die Startzeit der Szene muss noch abgezogen werden
			editTime = new EditTime(timingGroup, SWT.NONE,
					tmpStart, tmpEnd, nodeScene.getScene().getEnd()
					- nodeScene.getScene().getStart());
			
			if (isMarkAnnotation){
				addDurationComp(timingGroup);
			}
		}
		/**
		 * Adds a component for setting the duration during which a mark
		 * annotation should be shown after activating it by using the mark.
		 * @param parent parent to which the component is added
		 */
		private void addDurationComp(Composite parent) {
			//Create Container for duration
			Composite compDur = new Composite(parent, SWT.NONE);	
			GridLayout gridLayoutDur = new GridLayout(8, false);
			gridLayoutDur.marginWidth = 0;
			compDur.setLayout(gridLayoutDur);
			
			Label durationLabel = new Label(compDur, SWT.CENTER);
			durationLabel.setText(Messages.AnnotationDefineWidget_Label_Duration);
			durationLabel.setLayoutData(new GridData(48, 14));
			durationLabel.setAlignment(SWT.LEFT);
			
			//layoutdata
			GridData timeInputFieldsGD = new GridData();
			timeInputFieldsGD.widthHint = 20;
			timeInputFieldsGD.heightHint = 14;

			GridData doublePointGD = new GridData();
			doublePointGD.widthHint = 2;
			
			//einzelne Felder getrennt durch Doppelpunkte
			Label doublePoint;
			
			dHours = new Text(compDur, SWT.CENTER | SWT.BORDER);
			dHours.setLayoutData(timeInputFieldsGD);
			
			doublePoint = new Label(compDur, SWT.CENTER);
			doublePoint.setText(":"); //$NON-NLS-1$
			doublePoint.setLayoutData(doublePointGD);
			
			dMins = new Text(compDur, SWT.CENTER | SWT.BORDER);
			dMins.setLayoutData(timeInputFieldsGD);
			
			doublePoint = new Label(compDur, SWT.CENTER);
			doublePoint.setText(":"); //$NON-NLS-1$
			doublePoint.setLayoutData(new GridData(3,14));
			
			dSecs = new Text(compDur, SWT.CENTER | SWT.BORDER);
			dSecs.setLayoutData(timeInputFieldsGD);
			
			doublePoint = new Label(compDur, SWT.CENTER);
			doublePoint.setText("."); //$NON-NLS-1$
			doublePoint.setLayoutData(new GridData(3,14));
			
			dMillis = new Text(compDur, SWT.CENTER | SWT.BORDER);
			dMillis.setLayoutData(timeInputFieldsGD);
			
			//nur Zahlen erlauben + änderungen benachrichtigne(updatedirty())
			VerifyListener numericInputListener = new NumericInputListener();
			FocusListener checkDurationListener = new FocusListener() {

				@Override
				public void focusGained(org.eclipse.swt.events.FocusEvent e) {
					// TODO Auto-generated method stub
				}

				@Override
				public void focusLost(org.eclipse.swt.events.FocusEvent e) {
					updateDuration();
					updateDirty();											
				}
			};
			
			dHours.addVerifyListener(numericInputListener);
			dHours.addFocusListener(checkDurationListener);
			dMins.addVerifyListener(numericInputListener);
			dMins.addFocusListener(checkDurationListener);
			dSecs.addVerifyListener(numericInputListener);
			dSecs.addFocusListener(checkDurationListener);
			dMillis.addVerifyListener(numericInputListener);
			dMillis.addFocusListener(checkDurationListener);
			
			setDuration(tmpMarkDuration);
		}
		
		/**
		 * Adds a component for setting the screen area of an annotation and
		 * the position of marks. 
		 * @param parent parent to which the component is added
		 */
		private void addPositionEditor(Composite parent) {
			Group posEditorComposite = new Group(parent, SWT.NONE);
			posEditorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, 
					false, false));
			posEditorComposite.setLayout(new GridLayout(2, false));
			posEditorComposite.setText(Messages.AnnotationDefineWidget_Position_Label);

			// Screen Area wird für Audio/SubTitle Annotationen nicht benötigt
			// die Screen Area gibt die Position bzw. den Positionsverlaufs des
			// Inhalts an

			contentPositionGroup = new Composite(posEditorComposite,
					SWT.CENTER);
			contentPositionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					false, false));
			contentPositionGroup.setLayout(new GridLayout(1, false));
			Label label = new Label(contentPositionGroup, SWT.CENTER);
			label.setText(Messages.AnnotationDefineWidget_Label_Content_Position);

			// setze die Screen Area entsprechend der Annotation
			if (annotation.getScreenArea() != null) {
				tmpScreenArea = annotation.getScreenArea();
			}
			// setze die Overlay Path Items
			tmpOpItems = annotation.getOverlayPath();
			areaSelector = new ScreenAreaSelector(contentPositionGroup,
					SWT.NONE, tmpScreenArea);
			areaSelector
			.setToolTipText(Messages.AnnotationDefineWidget_AreaSelector_Tooltip);
			areaSelector.addSivaEventConsumer(new SivaEventConsumerI() {
				@Override
				public void handleEvent(SivaEvent event) {
					if (event.getEventType().equals(
							SivaEventType.SCREEN_AREA_CHANGED)) {
						tmpScreenArea = (ScreenArea) event.getValue();
						if (tmpScreenArea.equals(ScreenArea.OVERLAY)) {
							if(checkContentSet()){
								final OverlayEditor opEditor = new OverlayEditor(
										nodeScene, tmpStart, tmpEnd,
										tmpOpItems, editorContent, isMarkAnnotation);
								opEditor.addListener(SWT.Close, new Listener() {
									@Override
									public void handleEvent(Event event) {
										tmpOpItems = opEditor
												.getOverlayPathItems();
										updateDirty();
									}
								});
								opEditor.open();
								setFocus();
							}
						}

					}
					updateDirty();
				}
			});

			/**
			 * Der Button zum Starten des Editors für die Markierungsposition
			 * und Auswahlmöglichkeit für die Markierungsform für
			 * Markierungsannotationen
			 */
			if (isMarkAnnotation) {
				Composite markComp = new Composite(posEditorComposite,
						SWT.NONE);
				// richtige position setzen
				markComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
						true));
				markComp.setLayout(new GridLayout(1, false));
				Label markLabel = new Label(markComp, SWT.CENTER);
				markLabel
				.setText(Messages.AnnotationDefineWidget_Label_Markposition);
				// button zum öffnen des overlay editors erzeugen
				final Button editMarkPositionButton = new Button(markComp,
						SWT.CENTER);
				ImageHelper.setButtonImage(editMarkPositionButton,
						Icons.ACTION_EDITOR_IMAGE);
				editMarkPositionButton.addListener(SWT.MouseDown,
						new Listener() {
					public void handleEvent(Event e) {
						final OverlayEditor overlayEditor = new OverlayEditor(
								nodeScene, tmpStart, tmpEnd, markType,
								markShapes, buttonLabel);
						// wenn der Editor geschlossen wird, hole die
						// Markierungen
						overlayEditor.addListener(SWT.Close,
								new Listener() {
							@Override
							public void handleEvent(Event arg0) {
								markShapes = overlayEditor
										.getMarkShapes();
								buttonLabel = overlayEditor.getButtonLabel();
								updateDirty();
							}
						});
						overlayEditor.open();
					}
				});

				// setze den MarkType
				markType = ((NodeMark) annotation).getType();
				Group g = new Group(markComp, SWT.NONE);
				g.setLayout(new GridLayout(1, true));
				// marktype buttons aus enum erzeugen
				for (NodeMarkType curMarkType : NodeMarkType.values()) {
					final Button markShapeButton = new Button(g, SWT.RADIO);
					markShapeButton.setData(curMarkType);
					markShapeButton.setText(curMarkType.name());
					if (markType.equals(curMarkType)) {
						markShapeButton.setSelection(true);
					}
					markShapeButton.addMouseListener(new MouseListener() {
						@Override
						public void mouseDoubleClick(MouseEvent arg0) {

						}

						@Override
						public void mouseDown(MouseEvent arg0) {

						}

						@Override
						public void mouseUp(MouseEvent e) {
							NodeMarkType newMarkType = (NodeMarkType) markShapeButton
									.getData();
							// falls der gleiche Button wieder angeklickt wird,
							// mache nichts
							if (!newMarkType.equals(markType)) {
								boolean dialogOutcome = true;
								// warnhinweis anzeigen falls position oder
								// button groeße geaendert wurde
								if (markShapes.size() > 0) {
									dialogOutcome = MessageDialog
											.openQuestion(
													getShell(),
													Messages.AnnotationEditor_ChangeMark_Question,
													Messages.AnnotationEditor_ChangeMark_Warning);
								}
								// neuen raidobutton aktivieren auszer die
								// warnung wurde mit nein bestaetigt
								if (dialogOutcome) {
									markType = newMarkType;
									markShapes.clear();
									currentMarkShapeButton = (Button) e
											.getSource();
								} else {
									((Button) e.getSource())
									.setSelection(false);
									if (currentMarkShapeButton != null) {
										currentMarkShapeButton
										.setSelection(true);
									}
								}
								updateDirty();
							}
						}
					});
				}
			}
		}
		
		private void addKeywordComp(Composite parent) {
			// Eingabefeld für die Keywords
			Group kwGroup = new Group(parent, SWT.NONE);
			kwGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					false, false));
			kwGroup.setLayout(new GridLayout(1, false));
			kwGroup.setText(Messages.AnnotationDefineWidget_Label_Keywords);
			kwGroup.setToolTipText(Messages.AnnotationDefineWidget_Label_KeywordsTooltip);			
			tmpKeywords = new Text(kwGroup, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP
					| SWT.BORDER);
			GridData tmpKeywordsGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
			tmpKeywordsGD.heightHint = 50;
			tmpKeywords.setLayoutData(tmpKeywordsGD);
			tmpKeywords.setText(contentAnnotation.getKeywords());
			tmpKeywords.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
					// setze die Annotation auf dirty
					updateDirty();
				}
			});
		}
		
		private void addToggleComp(Composite parent) {			
			Group toggleGroup = new Group(parent, SWT.NONE);
			toggleGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					false, false));
			toggleGroup.setLayout(new GridLayout(1, false));
			toggleGroup.setText(Messages.AnnotationDefineWidget_Label_Toggles);
			
			Composite disableableContainer = new Composite(toggleGroup, SWT.NONE);
			GridLayout disableableContainerGL = new GridLayout(2, false);
			disableableContainerGL.marginWidth = 0;
			disableableContainer.setLayout(disableableContainerGL);
			disableableButton = new Button(disableableContainer, SWT.CENTER
					| SWT.CHECK);
			disableableButton.setSelection(annotation.isDisableable());
			disableableButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateDirty();
				}
			});
			Label disableableLabel = new Label(disableableContainer, SWT.CENTER);
			disableableLabel
			.setText(Messages.AnnotationDefineWidget_Label_Disableable);

			if (!annotationType.getContentType().equals(
					AnnotationContentType.SUBTITLE)) {
				Composite pauseContainer = new Composite(toggleGroup, SWT.NONE);
				GridLayout pauseContainerGL = new GridLayout(2, false);
				pauseContainerGL.marginWidth = 0;
				pauseContainer.setLayout(pauseContainerGL);
				pauseVideoButton = new Button(pauseContainer, SWT.CENTER
						| SWT.CHECK);
				pauseVideoButton.setSelection(annotation.isPauseVideo());
				pauseVideoButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						updateDirty();
					}
				});
				Label pauseVideoLabel = new Label(pauseContainer, SWT.CENTER);
				pauseVideoLabel
				.setText(Messages.AnnotationDefineWidget_Label_PauseVideo);
			}

			// mute toggle
			if (annotationType.getContentType().equals(AnnotationContentType.AUDIO)
					|| annotationType.getContentType().equals(
							AnnotationContentType.VIDEO)) {

				Composite muteContainer = new Composite(toggleGroup, SWT.NONE);
				GridLayout muteContainerGL = new GridLayout(2, false);
				muteContainerGL.marginWidth = 0;
				muteContainer.setLayout(muteContainerGL);
				muteVideoButton = new Button(muteContainer, SWT.CENTER 
						| SWT.CHECK);
				muteVideoButton.setSelection(annotation.isMuteVideo());
				muteVideoButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						updateDirty();
					}
				});
				Label muteVideoLabel = new Label(muteContainer, SWT.CENTER);
				muteVideoLabel
				.setText(Messages.AnnotationDefineWidget_Label_MuteVideo);
			}
		}
		
		/**
		 * Returns the duration stored in the duration text fields.
		 * Value is returned as nanoseconds.
		 * @return the currently set duration in ns
		 */
		private long getDuration() {
			SivaTime duration = new SivaTime(0);
			duration.setTime(dHours.getText(), dMins.getText(), dSecs.getText(), dMillis.getText(), "0"); //$NON-NLS-1$
			return duration.getNano();
		}
		
		/**
		 * Sets the content of the text fields showing the duration to the given
		 * time after converting it to the proper format. 
		 * @param time time to set the fields to		 */
		private void setDuration(long time) {
			SivaTime duration = new SivaTime(time);
			dHours.setText("" + duration.getCon_hours()); //$NON-NLS-1$
			dMins.setText("" + duration.getCon_minutes()); //$NON-NLS-1$
			dSecs.setText("" + duration.getCon_seconds()); //$NON-NLS-1$
			dMillis.setText("" + duration.getCon_milliseconds()); //$NON-NLS-1$
		}
		
		/**
		 * Updates the temporary duration of a mark annotation and the text 
		 * fields showing the set duration.  Also limits the duration to it's
		 * maximum value, which is (scene end - mark annotation start). 
		 */
		public void updateDuration() {
			if (isMarkAnnotation) {
				tmpMarkDuration = getDuration();
				long maxDuration = nodeScene.getScene().getEnd() 
						- (nodeScene.getScene().getStart() + tmpStart.getNano());
				if (tmpMarkDuration > maxDuration) {
					tmpMarkDuration = maxDuration;
					setDuration(maxDuration);
				}
			}
		}
	}
}
