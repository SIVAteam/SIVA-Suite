package org.iviPro.preview;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.application.Application;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.mediaaccess.player.controls.SivaSlider;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;

/**
 * Vorschau für den Player im Fenstermodus
 * 
 * @author langa
 * 
 */
public class WindowedPlayer extends AbstractPlayer {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(WindowedPlayer.class);

	// Annotationsbereiche
	private AnnoComposite top;
	private AnnoComposite left;
	private AnnoComposite right;
	private AnnoComposite bottom;

	public WindowedPlayer(Composite parent, int style, NodeScene scene) {
		super(parent, style, scene);
	}

	public void dispose() {
		// logger.debug("Disposing WindowedPlayer");
		playing = false;
		super.dispose();
	}

	@Override
	void initLayout() {
		ProjectSettings settings = Application.getCurrentProject()
				.getSettings();
		this.setBackground(Colors.EDITOR_BG.getColor());
		int width = settings.getSizeWidth();
		int height = settings.getSizeHeight();

		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		this.setLayout(layout);
		GridData layoutData = new GridData();
		layoutData.widthHint = 2 * width;
		layoutData.heightHint = 2 * height;
		this.setLayoutData(layoutData);

		// Größen der Annotationsbereiche berechenen
		int widthLeft = (int) (width * settings.getAreaLeftWidth());
		int widthRight = (int) (width * settings.getAreaRightWidth());
		int heightTop = (int) (height * settings.getAreaTopHeight());
		int heightBottom = (int) (height * settings.getAreaBottomHeight());

		// Bereiche initalisieren
		if (widthLeft == 0) {
			left = new AnnoComposite(this, SWT.None);
		} else {
			left = new AnnoComposite(this, SWT.BORDER);
		}
		left.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridData leftGrid = new GridData();
		leftGrid.heightHint = height;
		leftGrid.widthHint = widthLeft;
		leftGrid.verticalSpan = 3;
		left.setLayoutData(leftGrid);

		if (heightTop == 0) {
			top = new AnnoComposite(this, SWT.None);
		} else {
			top = new AnnoComposite(this, SWT.BORDER);
		}
		top.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridData topGrid = new GridData();
		topGrid.widthHint = width - widthLeft - widthRight;
		topGrid.heightHint = heightTop;
		top.setLayoutData(topGrid);

		if (widthRight == 0) {
			right = new AnnoComposite(this, SWT.None);
		} else {
			right = new AnnoComposite(this, SWT.BORDER);
		}
		right.setBackground(Display.getCurrent()
				.getSystemColor(SWT.COLOR_WHITE));
		GridData rightGrid = new GridData();
		rightGrid.widthHint = widthRight;
		rightGrid.heightHint = height;
		rightGrid.verticalSpan = 3;
		right.setLayoutData(rightGrid);

		center = new Composite(this, SWT.EMBEDDED);
		GridLayout centerLayout = new GridLayout(1, false);
		centerLayout.verticalSpacing = 0;
		centerLayout.horizontalSpacing = 0;
		//center.setLayout(centerLayout);
		videoHeight = height - heightTop - heightBottom;
		videoWidth = 4 * videoHeight / 3;
		if (videoWidth > (width - widthLeft - widthRight)) {
			videoWidth = width - widthLeft - widthRight;
			videoHeight = 3 * videoWidth / 4;
		}
		GridData vidGridData = new GridData(videoWidth, videoHeight);
		center.setLayoutData(vidGridData);

		if (heightBottom == 0) {
			bottom = new AnnoComposite(this, SWT.None);
		} else {
			bottom = new AnnoComposite(this, SWT.BORDER);
		}
		bottom.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		GridData bottomGrid = new GridData();
		bottomGrid.widthHint = width - widthLeft - widthRight;
		bottomGrid.heightHint = heightBottom;
		bottom.setLayoutData(bottomGrid);

		Composite playerControl = new Composite(this, SWT.None);
		GridData controlData = new GridData();
		controlData.horizontalSpan = 3;
		playerControl.setLayoutData(controlData);
		playerControl.setLayout(new GridLayout(3, false));
		playPause = new Button(playerControl, SWT.CENTER);
		ImageHelper.setButtonImage(playPause, Icons.MEDIAPLAYER_PAUSE);
		playPause.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (playing) {
					currentStartTime = i;
					stopPlayback();
					ImageHelper.setButtonImage(playPause,
							Icons.MEDIAPLAYER_PLAY);
				} else {
					startPlayback();
					ImageHelper.setButtonImage(playPause,
							Icons.MEDIAPLAYER_PAUSE);
				}
			}
		});
		slider = new SivaSlider(playerControl, "preview", scene.getEnd() //$NON-NLS-1$
				- scene.getStart(), 500, 40, false);
		slider.addSivaEventConsumer(new SivaEventConsumerI() {

			@Override
			public void handleEvent(SivaEvent event) {
				currentStartTime = (int) (event.getTime().getNano() / 1000000);
				mp.setMediaTime(event);
				stopPlayback();
				startPlayback();
			}
		});

	}

	public void startAnnotation(INodeAnnotation annotation) {
		ScreenArea field = annotation.getScreenArea();

		switch (field) {
		case RIGHT:
			right.showAnnotation(annotation);
			break;
		case LEFT:
			left.showAnnotation(annotation);
			break;
		case TOP:
			top.showAnnotation(annotation);
			break;
		case BOTTOM:
			bottom.showAnnotation(annotation);
			break;
		case OVERLAY:
			insertOverlay(annotation);
			break;
		default:
			break;
		}
	}

	public void stopAnnotation(INodeAnnotation annotation) {
		ScreenArea field = annotation.getScreenArea();

		switch (field) {
		case RIGHT:
			right.removeAnnotation();
			break;
		case LEFT:
			left.removeAnnotation();
			break;
		case TOP:
			top.removeAnnotation();
			break;
		case BOTTOM:
			bottom.removeAnnotation();
			break;
		case OVERLAY:
			removeOverlay(annotation);
			break;
		default:
			break;
		}
	}

	@Override
	void removeAllAnnotations() {
		right.removeAnnotation();
		left.removeAnnotation();
		top.removeAnnotation();
		bottom.removeAnnotation();
	}
}
