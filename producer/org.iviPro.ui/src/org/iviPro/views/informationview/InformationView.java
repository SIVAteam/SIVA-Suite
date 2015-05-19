package org.iviPro.views.informationview;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;
import org.iviPro.views.IAbstractView;
import org.iviPro.views.mediarepository.MediaTreeLeaf;
import org.iviPro.views.scenerepository.SceneTreeLeaf;

/**
 * Diese Klasse spiegelt die Ansicht Information wieder. In ihr werden je nach
 * ausgewähltem Objekt zusätzliche Informationen in einer Liste angezeigt.
 * 
 * @author Florian Stegmaier
 */
public class InformationView extends IAbstractView implements ISelectionListener,
		ApplicationListener {
	private static Logger logger = Logger.getLogger(InformationView.class);
	public static final String ID = InformationView.class.getName();

	public static final String SCENE_SCREENNAME = Messages.InformationView_Screenname;
	public static final String SCENE_START = Messages.InformationView_Start;
	public static final String SCENE_END = Messages.InformationView_End;
	public static final String SCENE_MEDIA = Messages.InformationView_Media;
	public static final String SCENE_DURATION = Messages.InformationView_Duration;
	public static final String SCENE_ID = Messages.InformationView_Id;
	public static final String SCENE_KEYWORDS = Messages.InformationView_Keywords;
	public static final String MEDIA_DURATION = Messages.InformationView_MediaDuration;
	public static final String MEDIA_CODEC = Messages.InformationView_MediaCodec;
	public static final String MEDIA_DIMENSION = Messages.InformationView_MediaDimension;
	public static final String MEDIA_SCENES = Messages.InformationView_MediaScenes;
	public static final String MEDIA_FRAMERATE = Messages.InformationView_MediaFrameRate;
	private static final String MEDIA_SCREENNAME = Messages.InformationView_Medianame;
	private static final String MEDIA_PATH = Messages.InformationView_Path;
	private static final String MEDIA_DATATYPE = Messages.InformationView_Datatype;
	private static InformationContainer informationContainer;

	private TableViewer tableViewer;

	public InformationView() {
		Application.getDefault().addApplicationListener(this);		
	}

	@Override
	protected Image getDefaultImage() {
		return Icons.VIEW_INFORMATION.getImage();
	}

	/**
	 * Erstellung der View und der betreffenden Elemente, w.z.B. der
	 * TableViewer.
	 */
	@Override
	public void createPartControlImpl(Composite parent) {

		// Erstellung des TableViewers und setzen benötgiter Parameter
		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);

		InformationContentProvider icp = new InformationContentProvider();
		InformationLabelProvider ilp = new InformationLabelProvider();

		ilp.createColumns(tableViewer.getTable());

		tableViewer.setContentProvider(icp);
		tableViewer.setLabelProvider(ilp);

		informationContainer = new InformationContainer();
		tableViewer.setInput(informationContainer);	
		getViewSite().getPage().addSelectionListener(this);
	}

	@Override
	public void setFocus() {
	}

	/**
	 * In dieser Methode wird das aktuell selektierte Objekt ermittelt und die
	 * Informationen daraus geladen und für die Anzeige aufbereitet.
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		logger.debug("Setting information about newly selected object"); //$NON-NLS-1$
		// alte Sachen raus
		informationContainer.clearList();
		Locale lang = Application.getCurrentLanguage();

		IStructuredSelection incoming = (IStructuredSelection) selection;

		// Schauen was wir für ein Objekt haben und welche Informationen wir
		// davon brauchen.
		if (incoming.getFirstElement() instanceof SceneTreeLeaf) {

			Scene scene = ((SceneTreeLeaf) incoming.getFirstElement()).getScene();
			informationContainer.addInfoToList(new Information(
					SCENE_SCREENNAME, scene.getTitle(lang)));
			informationContainer.addInfoToList(new Information(SCENE_START,
					new SivaTime(scene.getStart()).toString()));
			informationContainer.addInfoToList(new Information(SCENE_END,
					new SivaTime(scene.getEnd()).toString()));
			informationContainer.addInfoToList(new Information(SCENE_DURATION,
					new SivaTime(scene.getEnd() - scene.getStart()).toString()));
			informationContainer.addInfoToList(new Information(SCENE_MEDIA,
					scene.getVideo().getTitle(lang)));
			informationContainer.addInfoToList(new Information(SCENE_KEYWORDS,
					scene.getKeywords()));			
		} else if (incoming.getFirstElement() instanceof MediaTreeLeaf) {

			final IAbstractBean mediaObj = ((MediaTreeLeaf) incoming.getFirstElement())
					.getMediaObject();

			informationContainer.addInfoToList(new Information(
					MEDIA_SCREENNAME, mediaObj.getTitle(lang)));
			
			informationContainer.addInfoToList(new Information(MEDIA_DATATYPE,
					mediaObj.getClass().getSimpleName()));
			if (mediaObj instanceof IMediaObject) {
				informationContainer.addInfoToList(new Information(MEDIA_PATH,
					((IMediaObject) mediaObj).getFile().getAbsolutePath()));
			} else
			if (mediaObj instanceof AudioPart) {
				informationContainer.addInfoToList(new Information(MEDIA_PATH,
						((AudioPart) mediaObj).getAudio().getFile().getAbsolutePath()));	
			}
						
			//Audiospezifisch 
			if (mediaObj instanceof Audio) {
				informationContainer.addInfoToList(new Information(MEDIA_CODEC, ((IMediaObject) mediaObj).getFile().getAbsolutePath().substring(((IMediaObject) mediaObj).getFile().getAbsolutePath().lastIndexOf(".")+1))); //$NON-NLS-1$
			}
			
			// Bildspezifisch
			if (mediaObj instanceof Picture) {
				final Picture pic = (Picture) mediaObj;
				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {	
						if (pic != null) {
							int width = pic.getDimension().width;
							int height = pic.getDimension().height;
							informationContainer.addInfoToList(new Information(MEDIA_DIMENSION, width + "*" + height));							 //$NON-NLS-1$
							informationContainer.addInfoToList(new Information(MEDIA_CODEC, pic.getFile().getAbsolutePath().substring(pic.getFile().getAbsolutePath().lastIndexOf(".")+1))); //$NON-NLS-1$
							tableViewer.setInput(informationContainer);							
						}
					}					
				});
			}
			
			// Videospezifisch
			if (mediaObj instanceof Video) {					
				Video video = (Video) mediaObj;
				informationContainer.addInfoToList(new Information(
						MEDIA_DIMENSION, (int) video.getDimension().getWidth() + "*" + (int) video.getDimension().getHeight())); //$NON-NLS-1$
				informationContainer.addInfoToList(new Information(
						MEDIA_DURATION, new SivaTime(video.getDuration()).toString()));
				informationContainer.addInfoToList(new Information(
						MEDIA_CODEC, "" + video.getCodec())); //$NON-NLS-1$
				informationContainer.addInfoToList(new Information(
						MEDIA_FRAMERATE, "" + video.getFrameRate() + "fps")); //$NON-NLS-1$ //$NON-NLS-2$
				informationContainer.addInfoToList(new Information(
						MEDIA_SCENES, "" + video.getScenes().size())); //$NON-NLS-1$
			}
		}

		tableViewer.setInput(informationContainer);		
	}

	@Override
	public void onProjectClosed(Project project) {
		getViewSite().getPage().removeSelectionListener(this);
	}

	@Override
	public void onProjectOpened(Project project) {
		getViewSite().getPage().addSelectionListener(this);
		informationContainer.clearList();
	}
	
	@Override
	public void dispose() {
		getViewSite().getPage().removeSelectionListener(this);
		super.dispose();
	}
}
