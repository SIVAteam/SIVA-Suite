package org.iviPro.mediaaccess.player;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.controls.SivaSlider;
import org.iviPro.mediaaccess.player.controls.SivaVolumeSlider;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;

/**
 * Sichtbare Komponente (Frontend) des MediaPlayer kann für Videos + Audio
 * verwendet werden.
 * 
 * @author juhoffma
 */
public class MediaPlayerWidget {

	// das aktuelle aspect ratio
	private int aspectRatio;

	// Breite und Höhe des Videos (Breite wird automatisch berechnet,
	// Standardhöhe 240)
	private int width;
	private int height = 240;

	// der MediaPlayer
	private I_MediaPlayer mp;

	// das Composite für die Darstellung
	private Composite container;

	// gibt an ob die Media Controls angezeigt werden sollen
	private boolean showControls;

	// gibt an ob Markierpunkte verwendet werden
	private boolean supportMarkPoints;

	private SivaSlider slider;

	public MediaPlayerWidget(Composite parent, int style, I_MediaPlayer mp,
			boolean showControls, boolean supportMarkPoints) {
		this.mp = mp;
		this.showControls = showControls;
		this.supportMarkPoints = supportMarkPoints;
		initMediaPlayerWidget(parent, style);
	}

	public MediaPlayerWidget(Composite parent, int style, I_MediaPlayer mp,
			int height, boolean showControls, boolean supportMarkPoints) {
		this.mp = mp;
		this.height = height;
		this.showControls = showControls;
		this.supportMarkPoints = supportMarkPoints;
		initMediaPlayerWidget(parent, style);
	}

	// FIXME Methode überarbeiten
	// berechnet die Breite des Videos anhand der Höhe entsprechend dem akt.
	// Aspect Ratio
	// und zeichnet das Video Composite entsprechend
	private void adjustAspect() {
		width = 16 * height / 9;

	}

	private void initMediaPlayerWidget(Composite parent, int style) {

		container = new Composite(parent, SWT.RIGHT);
		GridLayout containerGL = new GridLayout(1, false);
		container.setLayout(containerGL);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (mp.getMediaObject() instanceof Video) {
			// videoShow entsprechend dem Aspect Ratio zeichnen
			adjustAspect();
			mp.createVisualPart(container, 0, 0, width, height);
		}

		// erstelle die Video Controls Play Button etc.
		if (showControls) {
			createMediaControl();
		}
	}

	public SivaSlider getSlider() {
		return this.slider;
	}

	/**
	 * erstellt die Video Controls
	 */
	private void createMediaControl() {
		// der Video Slider
		slider = new SivaSlider(container, mp.getMediaObject().getTitle(), mp
				.getDuration().getNano(), 340, 40, supportMarkPoints);

		// hält die Video Control-Elemente
		Composite controls = new Composite(container, SWT.CENTER);

		// GridData für die Controls
		GridData controlsGD = new GridData();
		controlsGD.horizontalAlignment = SWT.CENTER;
		controlsGD.grabExcessHorizontalSpace = true;
		controls.setLayoutData(controlsGD);

		// Layout für die Controls
		GridLayout controlsGL = new GridLayout(11, false);
		controlsGL.horizontalSpacing = 0;
		controlsGL.verticalSpacing = 0;
		controlsGL.marginWidth = 0;
		controlsGL.marginHeight = 0;
		controls.setLayout(controlsGL);

		// doppelter Linkspfeil
		final Button fastBackwards = new Button(controls, SWT.CENTER);
		fastBackwards.setToolTipText(Messages.MediaPlayer_Tooltip_FB);
		// Bild für den Button setzen
		ImageHelper
				.setButtonImage(fastBackwards, Icons.MEDIAPLAYER_REWIND_MUCH);
		fastBackwards.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				fastBackwards();
			}
		});
		fastBackwards.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (fastBackwards.isFocusControl()) {
					switch (e.keyCode) {
					case SWT.SPACE:
						fastBackwards();
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}
		});

		// Linkspfeil
		final Button backwards = new Button(controls, SWT.CENTER);
		backwards.setToolTipText(Messages.MediaPlayer_Tooltip_B);
		ImageHelper.setButtonImage(backwards, Icons.MEDIAPLAYER_REWIND_LITTLE);
		backwards.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				backwards();
			}

		});
		backwards.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (backwards.isFocusControl()) {
					switch (e.keyCode) {
					case SWT.SPACE:
						backwards();
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}
		});

		// Play Button
		final Button play = new Button(controls, SWT.CENTER);
		play.setToolTipText(Messages.MediaPlayer_Tooltip_Play);
		ImageHelper.setButtonImage(play, Icons.MEDIAPLAYER_PLAY);
		play.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				play(play);
			}
		});
		play.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (play.isFocusControl()) {
					switch (e.keyCode) {
					case SWT.SPACE:
						play(play);
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}
		});

		// Stop, beendet das Abspielen und setzt den Player auf den Startzustand
		// zurück
		final Button stop = new Button(controls, SWT.CENTER);
		stop.setToolTipText(Messages.MediaPlayer_Tooltip_Stop);
		ImageHelper.setButtonImage(stop, Icons.MEDIAPLAYER_STOP);
		stop.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				stop(play);
			}
		});
		stop.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (stop.isFocusControl()) {
					switch (e.keyCode) {
					case SWT.SPACE:
						stop(play);
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}
		});

		// Rechtspfeil, Video springt um 1 Sekunde weiter
		final Button forward = new Button(controls, SWT.CENTER);
		forward.setToolTipText(Messages.MediaPlayer_Tooltip_F);
		ImageHelper.setButtonImage(forward, Icons.MEDIAPLAYER_FORWARD_LITTLE);
		forward.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				forward();
			}
		});
		forward.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (forward.isFocusControl()) {
					switch (e.keyCode) {
					case SWT.SPACE:
						forward();
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}
		});

		// doppelter Rechtspfeil, Video springt um 5 Sekunden weiter
		final Button fastForward = new Button(controls, SWT.CENTER);
		fastForward.setToolTipText(Messages.MediaPlayer_Tooltip_FF);
		ImageHelper.setButtonImage(fastForward, Icons.MEDIAPLAYER_FORWARD_MUCH);
		fastForward.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				fastForward();
			}
		});
		fastForward.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (fastForward.isFocusControl()) {
					switch (e.keyCode) {
					case SWT.SPACE:
						fastForward();
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}
		});

		// Mute Button
		final Button muteButton = new Button(controls, SWT.CENTER);
		muteButton.setToolTipText(Messages.MediaPlayer_Tooltip_Mute);
		ImageHelper.setButtonImage(muteButton, Icons.MEDIAPLAYER_UNMUTE);

		// Volume Slider
		final SivaVolumeSlider volSlide = new SivaVolumeSlider(controls, 100,
				20);
		volSlide.setToolTipText(Messages.MediaPlayer_Tooltip_Volume);
		volSlide.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				mp.setVolume(event);
			}
		});

		volSlide.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (event.getEventType().equals(SivaEventType.VOLUME_CHANGED)) {
					ImageHelper.setButtonImage(muteButton,
							Icons.MEDIAPLAYER_UNMUTE);
				}
			}
		});

		muteButton.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				if (mp.isMute()) {
					ImageHelper.setButtonImage(muteButton,
							Icons.MEDIAPLAYER_UNMUTE);
					mp.unMute();
				} else {
					ImageHelper.setButtonImage(muteButton,
							Icons.MEDIAPLAYER_MUTE);
					mp.mute();
				}
			}
		});

		// der Listener auf das Video, hört auf Zeitänderungen des Videos
		// und auf Ende des Videos
		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (container.isDisposed() || slider.isDisposed()) {
					return;
				}
				if (event.getSource() != null
						&& event.getSource().equals(slider)) {
					return;
				}

				// falls das Video am Ende ist, zeige den Play button an
				if (event.getEventType()
						.equals(SivaEventType.VIDEO_END_REACHED)
						|| event.getEventType().equals(
								SivaEventType.VIDEO_STOPPED) || !mp.isActive()) {
					ImageHelper.setButtonImage(play, Icons.MEDIAPLAYER_PLAY);
				} else {
					if (mp.isActive()) {
						ImageHelper.setButtonImage(play,
								Icons.MEDIAPLAYER_PAUSE);
					}
				}

				slider.setSashes(event);

				// setze die markierten Punkte
				if (event.getEventType().equals(SivaEventType.MARK_POINT_START)) {
					slider.addMarkPoint(event.getTime().getNano(),
							SivaSlider.MARKER_STARTTIME);
				} else if (event.getEventType().equals(
						SivaEventType.MARK_POINT_END)) {
					slider.addMarkPoint(event.getTime().getNano(),
							SivaSlider.MARKER_ENDTIME);
				}
			}
		});

		slider.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (event.getEventType()
						.equals(SivaEventType.MEDIATIME_CHANGED)
						|| event.getEventType().equals(
								SivaEventType.STARTTIME_CHANGED)
						|| event.getEventType().equals(
								SivaEventType.ENDTIME_CHANGED)) {
					mp.setMediaTime(event);
				}
			}
		});
	}

	private void fastBackwards() {
		if (mp.getMediaObject() instanceof Video) {
			mp.backward();
		} else {
			mp.backward();
		}
	}

	private void backwards() {
		if (mp.getMediaObject() instanceof Video) {
			mp.frameBackward();
		} else {
			mp.backward();
		}
	}

	private void play(Button play) {
		if (!mp.isActive()) {
			ImageHelper.setButtonImage(play, Icons.MEDIAPLAYER_PAUSE);
			mp.play();
		} else {
			ImageHelper.setButtonImage(play, Icons.MEDIAPLAYER_PLAY);
			mp.pause();
		}
	}

	private void stop(Button play) {
		mp.stop();
		ImageHelper.setButtonImage(play, Icons.MEDIAPLAYER_PLAY);
	}

	private void forward() {
		if (mp.getMediaObject() instanceof Video) {
			mp.frameForward();
		} else {
			mp.forward();
		}
	}

	private void fastForward() {
		if (mp.getMediaObject() instanceof Video) {
			mp.forward();
		} else {
			mp.frameForward();
		}
	}
}
