package org.iviPro.preview;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.editors.imageeditor.ImageEditWidget;
import org.iviPro.mediaaccess.player.I_MediaPlayer;
import org.iviPro.mediaaccess.player.PlayerFactory;
import org.iviPro.mediaaccess.player.controls.SivaSlider;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.VideoFrameConsumer;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.Picture;
import org.iviPro.model.Scene;
import org.iviPro.model.Video;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.imageeditor.ImageObject;
import org.iviPro.utils.ImageHelper;
import org.iviPro.utils.SivaTime;


/**
 * Abstrakte Klasse für den Vorschauplayer, beinhaltet die Ansteuerung der
 * Annotationen und die Funktionalität
 * 
 * @author langa
 * 
 */
public abstract class AbstractPlayer extends Composite implements
		VideoFrameConsumer {

	private Logger logger = Logger.getLogger(AbstractPlayer.class);

	// Szenen und Videodaten
	NodeScene nodeScene;
	Scene scene;
	Video video;
	// Jobtitle für Framegrabbing
	final String JOB_TITLE = "PREVIEW_PLAYER"; //$NON-NLS-1$
	// Listen mit Start- und Endzeit von Annotationen
	List<NodeTimeContainer> start = new LinkedList<NodeTimeContainer>();
	List<NodeTimeContainer> end = new LinkedList<NodeTimeContainer>();
	// Breite/Höhe des Videos
	int videoWidth;
	int videoHeight;
	// Videocomposite
	Composite center;
	// Hashmap mit Overlayannotationen und zugehörigen Composites
	HashMap<INodeAnnotation, Decorations> overlays = new HashMap<INodeAnnotation, Decorations>();
	// Verbindung zum OS für Threadausführung
	Display display;
	// Timerthread zur Annotationssteuerung
	Thread timer;
	// Thread zum Abspielen des Videos
	Thread videoPlayer;
	// Unterscheidung Play/Pause und Abbruchbedingung für Threads
	volatile boolean playing;
	// Gibt an, ob die Wiedergabe neu gestartet wird (z.B. nach Sprung in der Zeit)
	volatile boolean restart = false;
	// Zähler für Threads, synchronisiert, nur ein Thread zählt hoch
	volatile int i;
	// Startzeit für Threads, benötigt bei Klick auf Zeitleiste
	int currentStartTime = 0;
	// Dauer des Videos
	long duration;
	// Videogröße
	Dimension videoSize;
	// Composite für die Anzeige des Endes
	Composite playbackEndComposite;
	// Play/Pauseknopf
	Button playPause;
	// Zeitleiste
	SivaSlider slider;
	
	
	Composite videoComposite;
	I_MediaPlayer mp;
	
	/**
	 * Initialisiert das Layout des Vorschauplayers
	 */
	abstract void initLayout();

	/**
	 * Startet eine Annotation im entsprechenden Player
	 */
	abstract void startAnnotation(INodeAnnotation annotation);

	/**
	 * Beendet eine Annotation im entsprechenden Player
	 */
	abstract void stopAnnotation(INodeAnnotation annotation);

	/**
	 * Beendet alle Annotationen in allen Bereichen
	 */
	abstract void removeAllAnnotations();

	AbstractPlayer(Composite parent, int style, NodeScene scene) {
		super(parent, style);
		this.nodeScene = scene;
		this.scene = nodeScene.getScene();
		this.video = scene.getScene().getVideo();
		// Annotationen umkopieren / In Start-/Endlisten einordnen
		List<INodeAnnotation> original = nodeScene.getAnnotations();
		for (INodeAnnotation a : original) {
			if (a instanceof NodeAnnotationText
					|| a instanceof NodeAnnotationRichtext
					|| a instanceof NodeAnnotationPicture) {
				start.add(new NodeTimeContainer((a.getStart() / 1000000), a));
				end.add(new NodeTimeContainer((a.getEnd() / 1000000), a));

			}
		}
		display = Display.getCurrent();
		initLayout();
		
		//center.setLayout(new FormLayout());
		// das Composite, dass das Video häl		
		mp = PlayerFactory.getPlayer((IMediaObject) video, scene.getScene().getStart(), scene.getScene().getEnd());
		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			
			@Override
			public void handleEvent(SivaEvent event) {
				slider.setSashes(event);
			}
		});
		
		mp.createVisualPart(center,0,0,450,200); //TODO größe anpassen?
		run();
	}
	
	// @SuppressWarnings("deprecation")
	@Override
	public void dispose() {
		// Threads killen
		logger.debug("Disposing AbstractPlayer"); //$NON-NLS-1$
		playing = false;
		// if (timer != null) {
		// timer.stop();
		// }
		// if (videoPlayer != null) {
		// videoPlayer.stop();
		// }
		super.dispose();
	}

	void run() {
		// Videodauer berechnen
		duration = (scene.getEnd() - scene.getStart()) / 1000000;
		// Videogröße für Framegrabbing
		videoSize = new Dimension(videoWidth, videoHeight);

		// Wiedergabe starten
		startPlayback();
	}

	void startPlayback() {
		// Zähler auf aktuelle Startzeit stellen (nach Sprung in der Zeit)
		i = currentStartTime;
		// Wenn die Wiedergabe wieder aufgenommen wird bei einem Zeitpunkt
		// größer 0, dann müssen die richtigen Annotationen angezeigt werden.
		if (currentStartTime > 0) {
			setAnnotationsForTime();
		}
		playing = true;

		mp.play();
		/*
		 * Threads für die Anzeige der Annotationen und die Wiedergabe des 
		 * Videos. Der Timerthread zeigt die Annotationen an und zählt die
		 * Zählvariable hoch.
		 * Der Videothread holt alle 100ms einen Frame.
		 */
		timer = new Thread() {
			{
				start();
			}

			@Override
			public void run() {
				while (playing && i < duration) {
					long startTime = System.currentTimeMillis();
					// Start- und Endliste durchlaufen und Annotationen steuern
					for (final NodeTimeContainer c : end) {
						if (Math.abs(c.time - i) <= 50) {

							display.syncExec(new Runnable() {

								@Override
								public void run() {
									if (AbstractPlayer.this.isDisposed()) {
										return;
									} else {
										logger.debug(i
												+ ": Stopping Annotation " //$NON-NLS-1$
												+ c.annotation.getTitle());
										stopAnnotation(c.annotation);
									}

								}
							});
						}
					}
					for (final NodeTimeContainer c : start) {
						if (Math.abs(c.time - i) <= 50) {

							display.syncExec(new Runnable() {

								@Override
								public void run() {
									if (AbstractPlayer.this.isDisposed()) {
										return;
									} else {
										logger.debug(i
												+ ": Starting Annotation " //$NON-NLS-1$
												+ c.annotation.getTitle());
										startAnnotation(c.annotation);
									}

								}
							});
						}
					}

					// Über Overlayannotationen iterieren und diese steuern
					Iterator overlayIterator = overlays.keySet().iterator();
					while (overlayIterator.hasNext()) {
						INodeAnnotation anno = (INodeAnnotation) overlayIterator
								.next();
						List<OverlayPathItem> path = anno.getOverlayPath();
						// Nach Play/Pause bzw. Sprung in der Zeitleiste
						// für die Annotation die letzte Position des Overlays
						// bestimmen
						if (restart) {
							OverlayPathItem min = path.get(0);
							for (OverlayPathItem o : path) {
								int curDist = (i - (int) (o.getTime() / 1000000));
								if (curDist > 0
										&& curDist < (i - (int) (min.getTime() / 1000000))) {
									min = o;
								}
							}
//							drawOverlay(anno, min);
						} else {
							for (OverlayPathItem o : path) {
								if (Math.abs((o.getTime() / 1000000) - i) <= 50) {
									logger.debug(i
											+ ": Showing/Changing Overlay: " + anno.getTitle()); //$NON-NLS-1$
//									drawOverlay(anno, o);
									break;
								}
							}
						}
					}
					long runtime = System.currentTimeMillis() - startTime;
					i = i + 100;
					try {
						synchronized (this) {
							if (runtime < 100) {
								wait(100);
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					restart = false;
				}
				super.run();
			};
		};
		
	}

	/**
	 * Löscht alle vorherigen Overlays und sucht alle Annotationen, die
	 * zum aktuellen Zeitpunkt gestartet aber noch nicht beendet worden sind.
	 */
	void setAnnotationsForTime() {
		List<INodeAnnotation> annotations = new LinkedList<INodeAnnotation>();
		Iterator overlayIterator = overlays.keySet().iterator();
		while (overlayIterator.hasNext()) {
			INodeAnnotation anno = (INodeAnnotation) overlayIterator.next();
			Composite comp = overlays.get(anno);
			if (comp != null) {
				comp.dispose();
			}
		}
		overlays.clear();
		for (NodeTimeContainer c : start) {
			if (c.time < currentStartTime) {
				annotations.add(c.annotation);
			}
		}
		for (NodeTimeContainer c : end) {
			if (c.time < currentStartTime) {
				annotations.remove(c.annotation);
			}
		}
		for (INodeAnnotation a : annotations) {
			startAnnotation(a);
		}
		restart = true;
	}

	void stopPlayback() {
		// Stopt die Threads
		mp.pause();
		playing = false;
	}

	void showEnd() {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				if (isDisposed()) {
					return;
				}
				playbackEndComposite = new Composite(center, SWT.None);

				playbackEndComposite.setBounds(new Rectangle(0, 0, center
						.getBounds().width, center.getBounds().height));
				playbackEndComposite.setBackground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_WHITE));
				playbackEndComposite.addPaintListener(new PaintListener() {

					@Override
					public void paintControl(PaintEvent e) {
						e.gc.drawText(Messages.AbstractPlayer_SceneEndText,
								100, 100);

					}
				});

				Iterator overlayIterator = overlays.keySet().iterator();

				while (overlayIterator.hasNext()) {
					INodeAnnotation anno = (INodeAnnotation) overlayIterator
							.next();
					Decorations comp = overlays.get(anno);
					// overlays.remove(anno);
					if (comp != null) {
						comp.dispose();
					}
				}
				// Alle Annotationen entfernen
				removeAllAnnotations();
				// Slider ganz ans Ende setzen (Rundungsfehler)
				SivaEvent event = new SivaEvent(null,
						SivaEventType.MEDIATIME_CHANGED, new SivaTime(
								duration * 1000000));
				slider.setSashes(event);
			}
		});
	}
	
	@Override
	public void consumeGrabingJob(FrameGrabingJob job) {
		if (!(job.getTag().startsWith(JOB_TITLE))) {
			return;
		}
		final Image swtImg = ImageHelper.getSWTImage(job.getImage());
		final Image vidImg = new Image(Display.getCurrent(), swtImg
				.getImageData().scaledTo(videoWidth, videoHeight));

		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				if (isDisposed()) {
					return;
				}
				if (vidImg != null && center != null && !center.isDisposed())
					center.setBackgroundImage(vidImg);
			}
		});		
	}

	/**
	 * Annotation in die Liste der anzuzeigenenden Overlays einfügen
	 * 
	 * @param annotation
	 */
	void insertOverlay(INodeAnnotation annotation) {
		overlays.put(annotation, null);
	}

	/**
	 * Annotation aus der Lister der anzuzeigenden Overlays entfernenn
	 * 
	 * @param annotation
	 */
	void removeOverlay(INodeAnnotation annotation) {
		Decorations comp = overlays.get(annotation);
		comp.dispose();
		overlays.remove(annotation);
	}

	/**
	 * Zur Annotation gehörendes Overlays anzeigen und positionieren
	 * 
	 * @param annotation
	 * @param path
	 */
	private void drawOverlay(final INodeAnnotation annotation,
			final OverlayPathItem path) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				if (isDisposed()) {
					return;
				}
				Decorations comp = overlays.get(annotation);
				if (comp == null) {

					comp = new Decorations(center, SWT.CENTER);
					comp.setBackground(display.getSystemColor(SWT.COLOR_RED));
					overlays.put(annotation, comp);
					
					//videoComposite.dispose();
					//createVidComp();
				}
				int compWidth = (int) (path.getWidth() * videoWidth);
				int compHeight = (int) (path.getHeight() * videoHeight);
				int x = (int) (path.getX() * videoWidth);
				int y = (int) (path.getY() * videoHeight);
				System.out.println("Comp data: " + x + "/" + y + " - " + compWidth + "*" + compHeight);
				if (!comp.isDisposed()) {
					//comp.setBounds(x, y, compWidth, compHeight);
					comp.setLayoutData(new FormData(compWidth, compHeight));
					comp.setLayout(new GridLayout());
					System.out.println("Layouting comp/center");
					FormData positionFD = (FormData) comp.getLayoutData();
					positionFD.left = new FormAttachment(0, x);
					positionFD.top = new FormAttachment(0, y);					
					positionFD.width = compWidth;
					positionFD.height = compHeight;	
					comp.setLayoutData(positionFD);
					center.layout();
					Control content;
					Rectangle bounds = comp.getBounds();
					if (annotation instanceof NodeAnnotationText) {
						//Textanzeige im simplen Label
						content = new Label(comp, SWT.WRAP);
						//content.setBounds(0, 0, bounds.width, bounds.height);
						content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
						((Label) content).setText(annotation.getDescription());
					} else if (annotation instanceof NodeAnnotationRichtext) {
						// Richtextdarstellung in Browserkomponente anzeigen und Links verarbeiten
						try {
							content = new Browser(comp, SWT.TOP);
//							content.setBounds(0, 0, bounds.width, bounds.height);
							content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
							String html = "<html><head></head><body style='overflow:hidden'>" + ((NodeAnnotationRichtext) annotation).getRichtext().getContent() + "</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$
							((Browser) content).setText(AnnoComposite.externalizeLinks(html));
						} catch (SWTError e) {
							logger.error("Browser could not be initialized\n", e); //$NON-NLS-1$
						}
					} else if (annotation instanceof NodeAnnotationPicture) {
						Picture picture = ((NodeAnnotationPicture) annotation).getPicture();
						Image img = new Image(
								Display.getCurrent(), picture.getImageData());

						// Overlayobjekte des Bildes zeichnen
						GC gc = new GC(img);
						gc.setAlpha(255);
						for (ImageObject o : picture.getObjects()) {
							ImageEditWidget.drawObject(gc,
									o, img);
						}
						gc.dispose();

						// Bild für die Anzeige skalieren
						int imagewidth = img.getBounds().width;
						int imageheight = img.getBounds().height;
						int width = getBounds().width;
						int height = getBounds().height;
						if (width <= height) {
							imageheight = (int) ((width * 1.0f / imagewidth * 1.0f) * imageheight);
							imagewidth = width;
						} else {
							imagewidth = (int) ((height * 1.0f / imageheight * 1.0f) * imagewidth);
							imageheight = height;
						}
						if (imagewidth > 0 && imageheight > 0) {
							final Image scaledImage = new Image(Display.getCurrent(), img.getImageData().scaledTo(imagewidth, imageheight));

						content = new Composite(comp, SWT.None);
						content.setBounds(0, 0, bounds.width, bounds.height);
						content.addPaintListener(new PaintListener() {
							
							@Override
							public void paintControl(PaintEvent e) {
								e.gc.drawImage(scaledImage, 0, 0);
								e.gc.dispose();
							}
						});
						}

					}
					


//					redraw();
//					comp.redraw();
				}
			}
		});

	}
}
