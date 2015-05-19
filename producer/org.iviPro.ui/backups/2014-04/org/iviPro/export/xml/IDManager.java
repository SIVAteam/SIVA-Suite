package org.iviPro.export.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.iviPro.export.ExportException;
import org.iviPro.export.ExportType;
import org.iviPro.export.Exporter;
import org.iviPro.model.Audio;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.Picture;
import org.iviPro.model.Scene;
import org.iviPro.model.Video;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;

/**
 * ID-Manager fuer den Export. Generiert die IDs fuer das XML-File und gibt die
 * IDs fuer bestimmte Exporte zurueck. Dies ist notwendig, damit man die
 * verschiedenen Objekte richtig referenzieren kann.
 * 
 * @author dellwo
 * 
 */
public class IDManager {

	/**
	 * Prefix fuer alle Trigger-IDs
	 */
	private static final String PREFIX_TRIGGER = "trigger-"; //$NON-NLS-1$
	/**
	 * Prefix fuer alle Label-IDs die auf einer Description eines Model-Objekts
	 * basieren.
	 */
	private static final String PREFIX_RES_LABEL_DESCRIPTION = "l_descr_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle Label-IDs die auf einem Title eines Model-Objekts
	 * basieren.
	 */
	private static final String PREFIX_RES_LABEL_TITLE = "l_title_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von Video-Ressourcen
	 */
	private static final String PREFIX_RES_VIDEO = "v_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von Audio-Ressourcen
	 */
	private static final String PREFIX_RES_AUDIO = "a_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von Richtext-Ressourcen
	 */
	private static final String PREFIX_RES_RICHTEXT = "rp_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von Image-Ressourcen
	 */
	private static final String PREFIX_RES_IMAGE = "i_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von Plaintext-Ressourcen
	 */
	private static final String PREFIX_RES_PLAINTEXT = "pt_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von Subtitle-Ressourcen
	 */
	private static final String PREFIX_RES_SUBTITLE = "pt_st_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von loadVideoAction Eintraegen
	 */
	private static final String PREFIX_LOAD_VIDEO_ACTION = "load-"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von showSelectionControl Eintraegen
	 */
	private static final String PREFIX_NODE_SELECTION = "select-"; //$NON-NLS-1$
	
	/**
	 * Prefix fuer alle IDs von showQuizControl Eintraegen
	 */
	private static final String PREFIX_NODE_QUIZ = "quiz-"; //$NON-NLS-1$

	/**
	 * Action-ID fuer alle Actions zum Anzeigen von INodeAnnotationLeaf-Objekten
	 */
	private static final String ACTIONID_SHOW_ANNOTATION = "show-"; //$NON-NLS-1$

	/**
	 * Action-ID fuer alle NodeEnd Objekte
	 */
	private static final String ACTIONID_ENDSIVA = "end-siva"; //$NON-NLS-1$

	/**
	 * Action-ID fuer alle Mark Nodes
	 */
	private static final String ACTIONID_NODEMARK = "show";
	
	/**
	 * Art des Exports (Flash/Silverlight/etc)
	 */
	private final ExportType exportType;

	/**
	 * ID-Counter, der fuer jeden Objekt-Typ die aktuelle ID speichert.
	 */
	private HashMap<Class<?>, Integer> idCounter = new HashMap<Class<?>, Integer>();

	/**
	 * Speichert die bereits bekannten IDs fuer Objekte.
	 */
	private HashMap<Object, String> knownIDs = new HashMap<Object, String>();

	/**
	 * Liste mit generierten Dateinamen.
	 */
	private final Set<FileCopyInfo> generatedFilenames;

	/**
	 * Erstellt einen neuen ID-Manager fuer den Export.
	 * 
	 * @param exportType
	 *            Art des Exports
	 * @param exportWithMilliseconds
	 *            Soll mit Millisekunden-Genauigkeit exportiert werden oder mit
	 *            Sekunden-Genauigkeit?
	 */
	public IDManager(ExportType exportType) {
		this.exportType = exportType;
		this.generatedFilenames = new TreeSet<FileCopyInfo>();
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
			// Objekt hat noch keine ID
			// => Generiere eine und speichere sie in der Liste der bekannten
			// IDs
			id = generateID(object);
			knownIDs.put(object, id);
		}
		if (object instanceof Scene) {
			id = PREFIX_RES_VIDEO + id;
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
		String id = object.getClass().getSimpleName() + "_" + idNum; //$NON-NLS-1$
		return id;
	}

	/**
	 * Gibt den Dateinamen des exportierten Videos zu einer angegebenen Szene
	 * an.
	 * 
	 * @param sceneNode
	 *            Die Szene
	 * @return Der Dateinamen des exportierten Videos fuer diese Szene.
	 */
	public String getFilename(Scene scene, Locale locale) {
		if (exportType == ExportType.HTML5) {
			return getID(scene)
			+ "-" + locale + "."; //$NON-NLS-1$
		} else {
			return getID(scene)
			+ "-" + locale + "." + exportType.getVideoExtension(); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gibt den Dateinamen eines zu exportierenden dateibasierten Objektes (z.B.
	 * Richtext, Bild, etc) an.
	 * 
	 * @param fileBasedObj
	 *            Das dateibasierte Objekt.
	 * @param language
	 *            Die Sprache des Objekts.
	 * @return Der Dateinamen des exportierten Videos fuer diese Szene.
	 * @throws ExportException
	 *             Falls der ID-Manager nicht weiss, wie er fur dieses
	 *             dateibasierte Objekt einen Dateinamen erzeugen soll.
	 */
	public String getFilename(IFileBasedObject fileBasedObj, Locale language,
			ExportParameters parameters) throws ExportException {
		String extension = null;
		String filepath = fileBasedObj.getFile(language).getValue()
				.getAbsolutePath();
		String filename = fileBasedObj.getFile(language).getValue().getName();
		if (filename.lastIndexOf('.') >= 0
				&& filename.lastIndexOf('.') < filename.length() - 1) {
			extension = filename.substring(filename.lastIndexOf('.'));
		}
		if (extension == null) {
			throw new ExportException("ID-Manager does not recognize " //$NON-NLS-1$
					+ "extension for file '" //$NON-NLS-1$
					+ filename + "'"); //$NON-NLS-1$
		}
		if (fileBasedObj instanceof Video) {
			extension = "." + exportType.getVideoExtension(); //$NON-NLS-1$
		} else if (fileBasedObj instanceof Audio) {
			extension = "." + exportType.getAudioExtension(); //$NON-NLS-1$
		}
		String generatedFilename = getID(fileBasedObj);

		// prüfe ob in den Parametern der Parameter ADDTOEXPORTFILE gesetzt ist
		// falls ja füge ihn zum
		// Dateinamen hinzu
		Object addObj = parameters.getValue(Exporter.EXPORT_ADDTOEXPORTFILE);
		if (addObj instanceof String) {
			generatedFilename += "-" + (String) addObj;
		}

		generatedFilename += "-" + language + extension; //$NON-NLS-1$

		FileCopyInfo copyInfo = new FileCopyInfo(filepath, generatedFilename,
				language, fileBasedObj, parameters);

		generatedFilenames.add(copyInfo);
		return generatedFilename;
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
		} else if (node instanceof NodeSelection) {
			return PREFIX_NODE_SELECTION + getID(node);
		} else if (node instanceof NodeMark) {
			return ACTIONID_NODEMARK + getID(node); 
		} else if (node instanceof NodeQuiz) {
			return PREFIX_NODE_QUIZ + getID(node); 
		} else {
			throw new ExportException(
					"ID-Manager can't create an Action-ID for objects of type " //$NON-NLS-1$
							+ node.getClass().getSimpleName());
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
	 * Gibt die ID fuer das Label des Titles eines bestimmten Model-Objekts
	 * zurueck
	 * 
	 * @param node
	 * @return
	 */
	public String getTitleLabelID(IAbstractBean obj) {
		return PREFIX_RES_LABEL_TITLE + getID(obj);
	}

	/**
	 * Gibt eine Liste von IDs zurück, die Reihenfolge entspricht der wie die Ressourcen gespeichert sind
	 * 
	 * @param annotationText
	 * @return
	 * @throws ExportException
	 */
	public List<String> getRessourceID(INodeAnnotationLeaf annotation)
			throws ExportException {
		List<String> ids = new ArrayList<String>();

		if (annotation instanceof NodeAnnotationText) {
			String id = PREFIX_RES_PLAINTEXT + getID(annotation);
			if (id != null) {
				ids.add(id);
			}
		} else if (annotation instanceof NodeAnnotationRichtext) {
			String id = PREFIX_RES_RICHTEXT + getID(annotation);
			if (id != null) {
				ids.add(id);
			}
		} else if (annotation instanceof NodeAnnotationAudio) {
			String id = PREFIX_RES_AUDIO + getID(annotation);
			if (id != null) {
				ids.add(id);
			}
		} else if (annotation instanceof NodeAnnotationPicture) {
			NodeAnnotationPicture nap = (NodeAnnotationPicture) annotation;
			// generiere für jedes Bild eine Ressourcen ID
			if (nap.getContentType() == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				for (Picture pic : nap.getPictureGallery().getPictures()) {								
					String picResID = PREFIX_RES_IMAGE + getID(pic);
					ids.add(picResID);
				}
			} else {
				String id = PREFIX_RES_IMAGE + getID(annotation);	
				if (id != null) {
					ids.add(id);
				}
			}
		} else if (annotation instanceof NodeAnnotationVideo) {
			String id = PREFIX_RES_VIDEO + getID(annotation);
			if (id != null) {
				ids.add(id);
			}
		} else if (annotation instanceof NodeAnnotationSubtitle) {
			String id = PREFIX_RES_SUBTITLE + getID(annotation);
			if (id != null) {
				ids.add(id);
			}
		}
		if (ids == null || ids.size() == 0) {
			throw new ExportException("ID-Manager can't create an " //$NON-NLS-1$
					+ "Ressource-ID for objects of type " //$NON-NLS-1$
					+ annotation.getClass().getSimpleName());
		}
		return ids;
	}

	/**
	 * Gibt die ID fuer die Button-Image-Ressource eines Selection-Controls
	 * zurueck oder null, falls das Control kein Button-Image hat.
	 * 
	 * @param control
	 * @return
	 * @throws ExportException
	 */
	public String getRessourceID(NodeSelectionControl control)
			throws ExportException {

		String id = null;
		if (control.getButtonImage() != null) {
			id = PREFIX_RES_IMAGE + getID(control.getButtonImage());
		}
		return id;
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
		id = PREFIX_RES_IMAGE + getID(control.getMinValue()+"-"+control.getMaxValue());
		return id;
	}

	/**
	 * Gibt die ID fuer das Label der Description eines bestimmten Model-Objekts
	 * zurueck
	 * 
	 * @param node
	 * @return
	 */
	public String getDescriptionLabelID(IAbstractBean obj) {
		return PREFIX_RES_LABEL_DESCRIPTION + getID(obj);
	}

	/**
	 * Gibt die ID der endSiva Action zurueck.
	 * 
	 * @return
	 */
	public String getEndActionID() {
		return ACTIONID_ENDSIVA;
	}

	public ExportType getExportType() {
		return exportType;
	}

	public Set<FileCopyInfo> getGeneratedFilenames() {
		return generatedFilenames;
	}
}
