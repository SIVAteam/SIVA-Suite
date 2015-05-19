package org.iviPro.editors.sceneeditor.components;

import java.awt.Dimension;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.I_MediaPlayer;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.VideoFrameConsumer;
import org.iviPro.mediaaccess.videograb.VideoGrabSystem;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Colors;
import org.iviPro.utils.ImageHelper;
import org.iviPro.utils.SivaTime;

public class VideoTimelineWidget extends Composite {

	private Logger logger = Logger.getLogger(VideoTimelineWidget.class);

	// hält die Timeline
	ScrolledComposite timeLineScrollPane = null;

	// die Timeline
	Timeline timeline = null;

	private I_MediaPlayer mp;

	public VideoTimelineWidget(Composite parent, int style, final I_MediaPlayer mp) {
		super(parent, style);

		// setze den Movieplayer
		this.mp = mp;

		// GridData für das VideoTimeline Widget
		GridData videoTimelineWidgetDG = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		videoTimelineWidgetDG.widthHint = 432;
		setLayoutData(videoTimelineWidgetDG);

		// Layout für das VideoTimelineWidget
		setLayout(new GridLayout(1, false));

		// das ScrollPane innerhalb dem die Timeline gescrolled werden kann
		timeLineScrollPane = new ScrolledComposite(this, SWT.H_SCROLL
				| SWT.BORDER);

		// GridData des ScrollPane
		GridData timeLineScrollPaneGD = new GridData(SWT.CENTER, SWT.CENTER,
				true, false, 1, 1);
		timeLineScrollPaneGD.widthHint = 620;
		timeLineScrollPane.setLayoutData(timeLineScrollPaneGD);

		// Layout des ScrollPane
		GridLayout timeLineScrollPaneGL = new GridLayout(1, false);
		timeLineScrollPane.setLayout(timeLineScrollPaneGL);

		// erzeuge die anzuzeigende Timeline und füge sie dem ScrollPane hinzu
		timeline = new Timeline(timeLineScrollPane, SWT.NONE, mp);
		timeLineScrollPane.setContent(timeline);
		
		mp.addSivaEventConsumer(new SivaEventConsumerI() {

			@Override
			public void handleEvent(SivaEvent event) {
				if (timeline.isDisposed() || timeLineScrollPane.isDisposed()) {
					return;
				}

				// prozentualer Fortschritt des Videos
				double curPosPercent = 100d/(mp.getDuration().getNano()) * mp.getMediaTime().getNano();

				// Position der Timeline entspricht prozentual der Position
				int pos = (int) Math.floor(((timeline.getSize().x / 100) * curPosPercent));

				// setze das aktuelle Bild des Videos als Mitte der Timeline
				// hierzu reicht das zusätzliche verschieben um die Hälte der
				// Scrollpane Größe
				// und die maximale Thumbnail Größe
				int dif = 0;
				if (timeline.getThumbnail(0) != null) {
					Point size = new Point(0, 0);
					if (Thumbnail.thumbSize == null) {
						size.x = Thumbnail.MAX_THUMB_SIZE.width;
						size.y = Thumbnail.MAX_THUMB_SIZE.height + 2
								* Timeline.PADDING_OVERALL;
					} else {
						dif = Thumbnail.thumbSize.width;
					}
				}
				pos = pos - timeLineScrollPane.getSize().x / 2 + dif;
				timeLineScrollPane.setOrigin(pos, 0);
				timeline.redraw();		
			}
		});
	}

	/**
	 * Klasse die das Timeline-Widget kapselt.
	 * 
	 * @author dellwo
	 * 
	 */
	private class Timeline extends Composite implements VideoFrameConsumer,
			PaintListener {

		/**
		 * Tag zum Markieren der Grabbing-Jobs die zur Timeline gehoeren.
		 */
		private final String JOB_TAG = "TIMELINE_JOB,"; //$NON-NLS-1$

		/**
		 * Abstand zwischen den Thumbnails
		 */
		private static final int THUMB_MARGIN = 4;

		/**
		 * Innenabstand der gesamten Timeline
		 */
		private static final int PADDING_OVERALL = 8;

		/**
		 * Das SWT Display
		 */
		private Display display = Display.getCurrent();

		/**
		 * Ein Array mit den Thumbnails
		 */
		private Thumbnail[] thumbnails;

		private Video video = null;

		/**
		 * Anzahl der Thumbnails in der Timeline
		 */
		private int totalFrames = 0;

		/**
		 * zeitliche Distanz zwischen den Frames
		 */
		private long dist = 0;

		/**
		 * gibt an ob direkt auf ein Thumb geklickt wurde
		 */
		private boolean thumbClicked = false;

		/**
		 * Erstellt ein neues Timeline-Widget
		 * 
		 * @param parent
		 *            Parent-Komponente der Timeline
		 * @param style
		 *            SWT Styles fuer die Timeline
		 */
		public Timeline(Composite parent, int style, final I_MediaPlayer mp) {
			super(parent, style);

			// Videolänge in Nanosekunden
			long videoLength = mp.getDuration().getNano();
			
			// falls kein Video
			if (!(mp.getMediaObject() instanceof Video)) {
				return;
			}
			video = (Video) mp.getMediaObject();

			// Sinnvollen Abstand zwischen den Frames in der Timeline berechnen
			// in Nanosekunden
			dist = calculateFrameDistance(videoLength);

			// Berechne Gesamtzahl der Frames und lege die Platzhalter dafuer
			// an.
			totalFrames = (int) (videoLength / dist);

			// Layout
			GridLayout layout = new GridLayout(totalFrames, true);
			layout.marginHeight = PADDING_OVERALL;
			layout.horizontalSpacing = THUMB_MARGIN;
			layout.verticalSpacing = 0;
			layout.marginWidth = PADDING_OVERALL;
			setLayout(layout);
			setBackground(Colors.VIDEO_TIMELINE_BG.getColor());

			addPaintListener(this);

			thumbnails = new Thumbnail[totalFrames];
			for (int i = 0; i < totalFrames; i++) {
				Thumbnail thumb = new Thumbnail(this, SWT.NONE);
				thumb.setSize(thumb.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				thumb.setForeground(Colors.VIDEO_TIMELINE_THUMB_BORDER
						.getColor());

				final long time = i * dist;
				String timeString = SivaTime.getTimeString(time);

				// gibt den Thumbnail Index aus
				thumb.setToolTipText(Messages.VideoTimelineWidget_Timeline_ThumbnailFrame
								+ i
								+ Messages.VideoTimelineWidget_Timeline_Time
								+ timeString);

				// füge jedem Thumbnail einen MouseListener hinzu
				// aktuell lässt ein Doppelklick das Video auf die entsprechende
				// Stelle springen
				thumb.addMouseListener(new MouseListener() {
					public void mouseDown(MouseEvent e) {
						// nothing
					}

					public void mouseUp(MouseEvent e) {
						// nothing
					}

					public void mouseDoubleClick(MouseEvent e) {
						thumbClicked = true;
						SivaEvent event = new SivaEvent(null, SivaEventType.MEDIATIME_CHANGED, new SivaTime(time));
						mp.setMediaTime(event);
					}
				});

				thumbnails[i] = thumb;
			}
			setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
			layout();

			for (int i = 0; i < totalFrames; i++) {
				VideoGrabSystem.instance().grabFrame(video, i * dist, Thumbnail.MAX_THUMB_SIZE, JOB_TAG, this, i, false);
			}			
		}

		/**
		 * liefert ein Thumbnail
		 * 
		 * @param i
		 *            Index des Thumbnails
		 */
		public Thumbnail getThumbnail(int i) {
			return thumbnails[i];
		}

		@Override
		public void paintControl(PaintEvent e) {
			e.gc.setBackground(Colors.VIDEO_TIMELINE_MARKER.getColor());

			// die Breite der Timeline teilt sich auf die gesamte Videolänge auf
			int widthTimeline = timeline.getSize().x;

			// berechnet den aktuellen Frame mit Zwischenwerten
			double curFrame = ((double) mp.getMediaTime().getNano() / dist);

			// der Hintergrund für das aktive Thumbnail entspricht der Größe des
			// Thumbnails
			int widthThumb = 0;
			int heightThumb = timeLineScrollPane.getSize().y + PADDING_OVERALL;

			if (Thumbnail.thumbSize != null) {
				widthThumb = (int) Thumbnail.thumbSize.getWidth();
			}

			int pos = 0;
			if (thumbClicked) {
				// der aktuelle Frame als exakter Wert, entspricht den
				// Thumbnails
				int curFrameInt = (int) (mp.getMediaTime().getNano() / dist);
				pos = (int) ((widthTimeline / totalFrames) * curFrameInt)
						+ PADDING_OVERALL;
				thumbClicked = false;
			} else {
				// die Position berechnet sich aus der Gesamtzahl der Frames
				// berechnet exakt die Position eines Thumbnails entsprechend
				// dem aktuellen Frame
				pos = (int) ((widthTimeline / totalFrames) * curFrame)
						+ PADDING_OVERALL;
			}

			e.gc.fillRoundRectangle(pos - 2, 0, widthThumb + 4, heightThumb
					+ PADDING_OVERALL, 8, 4);
			e.gc.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
		 */
		@Override
		public Point computeSize(int hint, int hint2, boolean changed) {
			Point size = new Point(0, 0);
			if (Thumbnail.thumbSize == null) {
				size.x = Thumbnail.MAX_THUMB_SIZE.width;
				size.y = Thumbnail.MAX_THUMB_SIZE.height + 2 * PADDING_OVERALL;
			} else {
				size.x = this.getChildren().length
						* (Thumbnail.thumbSize.width + THUMB_MARGIN) + 2
						* PADDING_OVERALL;
				size.y = Thumbnail.thumbSize.height + 2 * PADDING_OVERALL;
			}
			logger.debug("computeSize: " + size); //$NON-NLS-1$
			return size;
		}

		/**
		 * Berechnet einen sinnvollen Abstand zwischen zwei Thumbnails fuer eine
		 * bestimmte Video-Laenge.
		 * 
		 * @param durationInNanos
		 *            Die Laenge des Videos in Nano-Sekunden
		 * @return Abstand zwischen den Thumbnails in Nano-Sekunden.
		 */
		private long calculateFrameDistance(long videoLength) {
			double lengthInSec = videoLength / 1000000000.0;
			// Tolle Formel die halbwegs brauchbare Abstaende liefert.
			double interval = 1.0 + Math.pow(Math.log(lengthInSec + 20)
					- Math.log(20), 3) / 5;
			// Skip auf 1 mindestens festlegen.
			if (interval > lengthInSec) {
				interval = lengthInSec;
			}
			//$NON-NLS-1$
			logger.info("Video-Laenge: " + lengthInSec + "s -   " //$NON-NLS-1$ //$NON-NLS-2$
					+ "Thumbnail-Abstand: " + Math.round(interval * 100) //$NON-NLS-1$
					/ 100.0 + "s - Anzahl Thumbs: " + (int) (lengthInSec / interval)); //$NON-NLS-1$

			long result = (long) (interval * 1000000000L);
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Widget#dispose()
		 */
		@Override
		public void dispose() {
			logger.info("Disposing Timeline for " + video);
			super.dispose();
		}

		@Override
		public void consumeGrabingJob(FrameGrabingJob job) {
			
			String tag = job.getTag();
			if (!tag.startsWith(JOB_TAG)) {
				return;
			}
			// Parse die Frame-Nummer aus dem Tag, um das richtige Thumbnail zu
			// bekommen
			int frameNumber = job.getIndex(); //$NON-NLS-1$
			logger.debug("Consumed timeline frame #" + frameNumber); //$NON-NLS-1$

			// Fuege das Bild in das Thumbnail ein.
			final Thumbnail thumbnail = thumbnails[frameNumber];

			final Image swtImg = ImageHelper.getSWTImage(job.getImage());

			display.asyncExec(new Runnable() {

				@Override
				public void run() {
					if (timeline.isDisposed() || timeLineScrollPane.isDisposed()) {
						return;
					}
					thumbnail.setImage(swtImg);

					// Falls thumbSize noch nicht bekannt ist, setzen wir sie
					// jetzt
					if (Thumbnail.thumbSize == null) {
						Thumbnail.thumbSize = new Dimension(
								swtImg.getBounds().width,
								swtImg.getBounds().height);
						/*
						 * Timeline.this.setSize(Timeline.this.computeSize(
						 * SWT.DEFAULT, SWT.DEFAULT)); Timeline.this.layout();
						 */
						for (int i = 0; i < thumbnails.length; i++) {
							if (!thumbnails[i].isDisposed()) {
								thumbnails[i].pack(true);
							}
						}
						if (!Timeline.this.isDisposed()) {
							Timeline.this.pack(true);
							Timeline.this.layout();
						}
					}
				}
			});			
		}
	}
}
