package org.iviPro.editors.audioeditor.components.overview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.iviPro.application.Application;
import org.iviPro.editors.common.BeanComparator;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.editors.sceneeditor.components.Messages;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.theme.Colors;
import org.iviPro.utils.SivaTime;
import org.iviPro.views.mediarepository.MediaRepository;

/*
 * zeichnet alle definierten Audio-Parts des Audio-Files nanosekundengenau in einer Leiste,
 */
public class AudioPartOverview extends SivaComposite implements ISelectionProvider, SivaEventConsumerI {

	// der Movieplayer
	private MediaPlayer mp = null;
	
	// the audio file the part is based on
	private Audio audio;

	// die Breite des Inhalts der Scroll-Komponente
	private int widthContentScrollComp = 0;

	// minimale Höhe des Inhalts
	private int minContentScrollCompHeight = 215;

	// scrollbares Composite, welches die einzelnen Audio-Partsbalken hält
	private ScrolledComposite scrollComp = null;

	// hält die einzelnen Audio-Parts
	private Composite sceneHolder = null;

	// die zum Overview gehörende Site
	IWorkbenchSite site;
	
	// aktuelle Selektion
	private StructuredSelection selection = null;
	
	// das Media Repository
	IViewPart mediaRepository;

	// Liste der Listener, hören auf Selektionen
	private LinkedList<ISelectionChangedListener> listeners = new LinkedList<ISelectionChangedListener>();
	
	// gibt den aktuellen Sortiertyp an
	private BeanComparator bComparator = BeanComparator.getDefault();

	public AudioPartOverview(Composite parent, int style, IWorkbenchSite site,
			MediaPlayer mp, final int width) {
		super(parent, style);
		this.mp = mp;
		this.audio = (Audio) mp.getMediaObject();
		this.widthContentScrollComp = width;
		this.site = site;
		// hier wird auf das Audio-Parts Repository gehört
		this.mediaRepository = Application.getDefault().getView(MediaRepository.ID);
						
		if (mediaRepository != null) {
			if (mediaRepository instanceof MediaRepository) {
				// setze beim Starten die aktuelle Selektion
				
				Object newSel = ((MediaRepository) mediaRepository).getCurrentSelection();
				AudioPart selPart = null;
				if (newSel instanceof AudioPart) {
					selPart = (AudioPart) newSel;
				}
				if (selPart != null) {
					selection = new StructuredSelection(selPart);
				}
				
				((MediaRepository) mediaRepository).addSelectionListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {						
						if (isDisposed()) {
							return;
						}					
						Object newSel = ((MediaRepository) mediaRepository).getCurrentSelection();
						AudioPart selPart = null;
						if (newSel instanceof AudioPart) {
							selPart = (AudioPart) newSel;
						}
						if (selPart == null || !selPart.getAudio().equals(audio)) {
							return;
						} else {
							selection = new StructuredSelection(selPart);
						}
						Iterator<ISelectionChangedListener> it = listeners.iterator();						
						while (it.hasNext()) {
							ISelectionChangedListener curListener = it.next();
							curListener.selectionChanged(new SelectionChangedEvent(AudioPartOverview.this,
								selection));
						}
					}					
				});				
			}
		}
		
		// höre auf den Player
		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (isDisposed() || sceneHolder.isDisposed()) {
					return;
				}
				if (sceneHolder != null) {
					sceneHolder.redraw();
					sceneHolder.update();
				}
			}			
		});

		// GridData des SceneOverview
		setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));

		// Layout des SceneOverview
		GridLayout grid = new GridLayout(1, false);
		setLayout(grid);
				
		// Scrollcomp
		scrollComp = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL);
		scrollComp.setAlwaysShowScrollBars(true);
		scrollComp.setBackground(Colors.VIDEO_OVERVIEW_BG.getColor());
		scrollComp.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridData scrollGrid = new GridData(SWT.FILL, SWT.FILL, false, false);
		scrollGrid.widthHint = width;
		scrollGrid.heightHint = minContentScrollCompHeight;
		scrollComp.setLayoutData(scrollGrid);		
		scrollComp.getVerticalBar().setIncrement(10);
		
		if (mp.getMediaObject() instanceof Audio) {
			final Audio audio = (Audio) mp.getMediaObject();
			audio.getAudioParts().addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					AudioPart changedPart = null;
					if (evt.getNewValue() instanceof AudioPart) {
						changedPart = (AudioPart) evt.getNewValue();
					}
					if (changedPart != null) {
						StructuredSelection selection = new StructuredSelection(changedPart);
						setSelection(selection);
					}					
					
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!AudioPartOverview.this.isDisposed()) {
								updateView();
							}
						}
					});					
				}				
			});
			
			scrollComp.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					System.out.println("dasdsadsad");
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}

		// zeichne bereits definierte Audio-Parts
		updateView();
	}

	/**
	 * zeichnet die einzelnen Audio-Parts neu
	 */
	private void updateView() {

		// falls der Container bereits geschlossen wurde, weil z.B. das Projekt
		// geschlossen wurde
		// müssen die Audio-Parts nicht mehr aus der Darstellung entfernt werden
		if (isDisposed()) {
			return;
		}

		// Stelle ab der die Skalendaten gezeichnet werden
		final int startScale = 20;

		// Höhe ab wann die Audio-Parts eingezeichnet werden sollen
		final int startAudioParts = 30;

		// hier werden alle Audio-Parts gezeichnet
		Audio audio = (Audio) mp.getMediaObject();
		final List<AudioPart> audioParts = audio.getAudioParts();

		int newHeight = 0;
		// die Audio-Part Holder Größe muss gesetzt werden, damit die Scrollbar weiß,
		// welchen Bereich und wieviel sie scrollen muss
		newHeight = startAudioParts + 4 + audioParts.size() * 22;

		if (newHeight < minContentScrollCompHeight) {
			newHeight = minContentScrollCompHeight;
		}
		final int newHeightFin = newHeight;

		if (sceneHolder != null) {
			if (!sceneHolder.isDisposed()) {
				sceneHolder.dispose();
			}
			sceneHolder = null;
		}

		// hält die Audio-Parts
		sceneHolder = new Composite(scrollComp, SWT.CENTER);

		// Layout für den Audio-Partshalter festlegen
		GridLayout layoutSceneHolder = new GridLayout(1, true);
		layoutSceneHolder.marginWidth = 0;
		layoutSceneHolder.marginHeight = 0;
		layoutSceneHolder.marginTop = 40;
		sceneHolder.setLayout(layoutSceneHolder);

		// zeichne im Scene Holder eine Skala
		sceneHolder.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				// Anzahl der Zeitpunkte die eingezeichnet werden sollen
				int timePoints = 7;

				// Abstand der einzelnen Punkte
				int distanceTimePoints = widthContentScrollComp / timePoints;

				// Zeitspanne für einen timePoint
				long timeTimePointLong = mp.getDuration().getNano() / timePoints;

				// gib die aktuelle Anzahl von Audio-Parts aus
				e.gc.drawText(Messages.ScenesOverview_Label_NumScenes
						+ audioParts.size(), 0, 0);

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
				double percentCurTime = (percentNano * mp.getMediaTime().getNano());
				// 1 Prozent der Breite
				double percentWidth = (double) widthContentScrollComp / 100;
				int x = (int) (percentWidth * percentCurTime);
				e.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
				e.gc.setLineWidth(5);
				e.gc.drawLine(x, startScale + 15, x, newHeightFin);
			}
		});
		
		Collections.sort(audioParts, bComparator);
		
		// erstelle eine SingleSceneBar für jede Audio-Part
		for (AudioPart part : audioParts) {
			SingleAudioBar sab = new SingleAudioBar(sceneHolder, SWT.CENTER,
					this, part, mp, widthContentScrollComp);
			addDragAndDrop(sab, part);
		}

		sceneHolder.setSize(widthContentScrollComp, newHeight);
		scrollComp.setContent(sceneHolder);
	}
	
	/**
	 * fügt den einzelnen SingleScene Bars Drag and Drop Funktionaltitä zum
	 * Verbinden der Audio-Parts hinzu
	 * 
	 * @param ssb
	 * @param scene
	 */
	private void addDragAndDrop(final SingleAudioBar sab, final AudioPart part) {

		if (isDisposed() || sab.isDisposed()) {
			return;
		}

		// Drag and Drop zum Verbinden von 2 Audio-Parts
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

		// die Quelle die gezogen wird
		final DragSource source = new DragSource(sab, operations);
		source.setTransfer(types);
		final DragSourceAdapter dragSourceAdapter = new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {
				event.data = part.getTitle();
			}

			public void dragFinished(DragSourceEvent event) {
			}
		};
		source.addDragListener(dragSourceAdapter);

		source.addListener(SWT.DragDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				source.removeDragListener(dragSourceAdapter);
			}
		});

		// das Ziel auf das gezogen wird
		DropTarget target = new DropTarget(sab, operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				if (event.data instanceof String) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							// Audio-Parts zusammenfügen funktioniert aktuell nicht!
							// siehe Ticket #340 
							/*
							IAbstractOperation op = new SceneMergeOperation(scene.getTitle(), sourceSceneTitle, scene.getVideo()); 
							try {
								OperationHistory.execute(op);
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
							*/
						}						
					});
				}
			}
		});
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection newSelection) {
		this.selection = (StructuredSelection) newSelection;
		// die eigentliche Selektion übernimmt das Repository 
		// es wird hier benachrichtigt und der Scenes Overview ist wiederum ein
		// Listener auf das Rep, welcher die Listener auf den SceneOverview benachrichtigt ...
		if (mediaRepository != null) {
			if (mediaRepository instanceof MediaRepository) {
				if (selection.equals(StructuredSelection.EMPTY)) {
					((MediaRepository) mediaRepository).selectBean(null);
				} else {
					AudioPart selectedPart = (AudioPart) (((StructuredSelection) newSelection).getFirstElement());
					((MediaRepository) mediaRepository).selectBean(selectedPart);
				}
			}
		}
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
