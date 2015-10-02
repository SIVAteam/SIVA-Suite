package org.iviPro.newExport.descriptor.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.AbstractNodeSelection;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.Video;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.Profile;
import org.iviPro.newExport.resources.PictureResourceDescriptor;
import org.iviPro.newExport.resources.ProjectResources;
import org.iviPro.newExport.resources.ResourceDescriptor;
import org.iviPro.newExport.resources.TimedResourceDescriptor;
import org.iviPro.newExport.resources.VideoResourceDescriptor;
import org.iviPro.newExport.resources.VideoThumbnailDescriptor;

/**
 * ID-Manager fuer den Export. Generiert die IDs fuer das XML-File und gibt die
 * IDs fuer bestimmte Exporte zurueck. Dies ist notwendig, damit man die
 * verschiedenen Objekte richtig referenzieren kann.
 * 
 * @author dellwo
 * 
 */
public class IdManager implements IdDefinition {
	/**
	 * Enum used to determine the type of a label.
	 * @author John
	 *
	 */
	public enum LabelType {
		TITLE,
		DESCRIPTION,
		SUMMARY,
		BUTTON
	}
	
	private final String THUMBNAIL_SUFFIX = "_thumb";
	private final String THUMBNAIL_EXTENSION = ".jpg";

	private final Profile profile;

	private final ProjectResources projectResources;

	/**
	 * ID-Counter, der fuer jeden Objekt-Typ die aktuelle ID speichert.
	 */
	private HashMap<Class<?>, Integer> idCounter;

	/**
	 * Speichert die bereits bekannten IDs fuer Objekte.
	 */
	private HashMap<Object, String> knownIDs;

	/**
	 * Erstellt einen neuen ID-Manager fuer den Export.
	 * 
	 * @param exportType
	 *            Art des Exports
	 * @param exportWithMilliseconds
	 *            Soll mit Millisekunden-Genauigkeit exportiert werden oder mit
	 *            Sekunden-Genauigkeit?
	 */
	public IdManager(Profile profile, ProjectResources projectResources) {
		this.profile = profile;
		this.projectResources = projectResources;
		this.idCounter = new HashMap<Class<?>, Integer>();
		this.knownIDs = new HashMap<Object, String>();
	}

	public Profile getProfile() {
		return profile;
	}

	public ProjectResources getProjectResources() {
		return projectResources;
	}

	/**
	 * Gibt zu einem Objekt die entsprechende ID zurueck.
	 * 
	 * @param object
	 *            Das Objekt fuer das die ID generiert werden soll.
	 * @return ID des Objekts.
	 */
	public String getID(Object object) {
		String id = knownIDs.get(object);
		if (id == null) {
			id = generateID(object);
			knownIDs.put(object, id);
		}
		return id;
	}

	/**
	 * Generiert eine ID fuer ein Objekt und zaehlt den ID-Counter fuer diesen
	 * Objekttyp hoch.
	 * 
	 * @param object
	 *            Das Objekt
	 * @return
	 */
	private String generateID(Object object) {
		Class<?> objectClass = object.getClass();
		// Falls fuer diese Klasse noch kein ID-Counter besteht, erstellen wir
		// einen und initialisieren ihn mit 1
		if (!idCounter.containsKey(objectClass)) {
			idCounter.put(objectClass, 1);
		}
		// ID fuer diese Klasse auslesen und Zaehler hochzaehlen.
		int idNum = idCounter.get(objectClass);
		idCounter.put(objectClass, idNum + 1);

		// ID mit Klassenname als Prefix zurueck geben.
		String id = object.getClass().getSimpleName() + ENTITY_TYPE_SUFFIX
				+ idNum;
		return id;
	}

	private String getExtension(String fileName) throws ExportException {
		int extensionDelimiterIndex = fileName.lastIndexOf(EXTENSION_SEPARATOR);
		if (extensionDelimiterIndex >= 0
				&& extensionDelimiterIndex < fileName.length() - 1) {
			return fileName.substring(extensionDelimiterIndex);
		} else {
			throw new ExportException(
					String.format(
							Messages.Exception_ExtractingExtensionFailed,
							fileName));
		}
	}

	public String getVideoThumbnailName(String id, Locale locale, Video video, 
			long time) {
		String target = id + THUMBNAIL_SUFFIX + ID_SEPARATOR + locale;
		
		projectResources.getVideoThumbnails().add(
				new VideoThumbnailDescriptor(target + THUMBNAIL_EXTENSION,
						video, time));
		return profile.getGeneral().getImageDirectory() + File.separator
				+ target + THUMBNAIL_EXTENSION;
	}
	
	public String getImageFileName(String id, File sourceFile, Locale locale,
			Picture picture) throws ExportException {
		String target = id + ID_SEPARATOR + locale;
		String extension = getExtension(sourceFile.getName());

		projectResources.getPictures().add(
				new PictureResourceDescriptor(sourceFile, target + extension,
						picture));
		return profile.getGeneral().getImageDirectory() + File.separator
				+ target + extension;
	}

	public String getRichPageFileName(String id, File sourceFile, Locale locale)
			throws ExportException {
		String target = id + ID_SEPARATOR + locale;
		String extension = getExtension(sourceFile.getName());

		projectResources.getRichPages().add(
				new ResourceDescriptor(sourceFile, target + extension));
		return profile.getGeneral().getRichPageDirectory() + File.separator
				+ target + extension;
	}
	
	public String getPdfDocumentFileName(String id, File sourceFile, Locale locale)
			throws ExportException {
		String target = id + ID_SEPARATOR + locale;
		String extension = getExtension(sourceFile.getName());
		
		projectResources.getPdfDocuments().add(
				new ResourceDescriptor(sourceFile, target + extension));
		return profile.getGeneral().getPdfDirectory() +
				File.separator + target + extension;		
	}

	public String getAudioFileName(String id, File sourceFile, Locale locale, 
			long startTime, long endTime) {
		String target = id + ID_SEPARATOR
				+ locale;
		String extension = EMPTY;
		if (profile.getGeneral().isExportAudioExtensions()) {
			extension = EXTENSION_SEPARATOR
					+ profile.getAudio().getAudioVariants().get(0)
							.getAudioProfiles().get(0).getAudioContainer()
							.getFileExtension(); //$NON-NLS-1$
		}

		projectResources.getAudios().add(
				new TimedResourceDescriptor(sourceFile, target, startTime,
						endTime));
		return profile.getGeneral().getAudioDirectory() + File.separator
				+ target + extension;
	}

	public String getVideoFileName(String id, File sourceFile, Locale locale,
			long startTime, long endTime, Video video) {
		String target = id + ID_SEPARATOR + locale;
		String extension = EMPTY;
		if (profile.getGeneral().isExportVideoExtensions()) {
			extension = EXTENSION_SEPARATOR
					+ profile.getVideo().getVideoVariants().get(0)
							.getVideoProfiles().get(0).getVideoContainer()
							.getFileExtension();
		}

		projectResources.getVideos().add(
				new VideoResourceDescriptor(sourceFile, target, startTime,
						endTime, video));
		return profile.getGeneral().getVideoDirectory() + File.separator
				+ target + extension;
	}

	/**
	 * Gibt fuer einen bestimmten Graph-Knoten die entsprechende Action-ID
	 * zurueck. Beispielsweise fuer ein NodeScene-Objekt wird der entsprechende
	 * Action-ID der loadVideoScene-Action zurueck gegeben oder fuer einen
	 * NodeEnd-Knoten der entsprechende endSiva-ActionID.
	 * 
	 * @param nodeScene
	 *            Die Szene
	 * @return ID der entsprechenden loadVideoAction
	 * @throws ExportException
	 *             Wenn der ID-Manager nicht weiss, wie er eine entsprechende
	 *             ActionID fuer einen Knoten-Typ generieren soll.
	 */
	public String getActionID(IGraphNode node) throws ExportException {
		if (node instanceof NodeScene) {
			return PREFIX_LOAD_VIDEO_ACTION + getID(node);
		} else if (node instanceof NodeEnd) {
			return ACTIONID_ENDSIVA;
		} else if (node instanceof INodeAnnotationLeaf) {
			return ACTIONID_SHOW_ANNOTATION + getID(node);
		} else if (node instanceof AbstractNodeSelection) {
			return PREFIX_NODE_SELECTION + getID(node);
		} else if (node instanceof NodeMark) {
			return ACTIONID_NODEMARK + getID(node);
		} else if (node instanceof NodeQuiz) {
			return PREFIX_NODE_QUIZ + getID(node);
		} else if (node instanceof NodeRandomSelection) {
			return PREFIX_NODE_RANDOMSELECTION + getID(node);
		} else {
			throw new ExportException(
					String.format(
							Messages.Exception_CreatingActionIdFailed,
							node.getClass().getSimpleName()));
		}
	}

	/**
	 * Gibt die ID fuer den Trigger einer Annotation zurueck.
	 * 
	 * @param annoatation
	 * @return
	 */
	public String getTriggerID(INodeAnnotation annoatation) {
		return PREFIX_TRIGGER + getID(annoatation);
	}

	/**
	 * Gibt die ID fuer die Button-Image-Ressource eines Selection-Controls
	 * zurueck oder null, falls das Control kein Button-Image hat.
	 * 
	 * @param control
	 * @return
	 * @throws ExportException
	 */
	public String getRessourceID(NodeQuizControl control)
			throws ExportException {

		String id = null;
		id = PREFIX_RES_IMAGE + getID(control.getMinValue()+"-"+control.getMaxValue()); //$NON-NLS-1$
		return id;
	}

	/**
	 * Returns the ID for a label used in the context of the given object.
	 * @param type of the label
	 * @param obj context object 
	 * @return ID of the label containing a prefix, the type, and the context object
	 */
	public String getLabelId(LabelType type, IAbstractBean obj) {
		return type.name().toLowerCase() + "_" + getID(obj);
	}
	
	/**
	 * Gibt die ID der endSiva Action zurueck.
	 * 
	 * @return
	 */
	public String getEndActionID() {
		return ACTIONID_ENDSIVA;
	}
}
