package org.iviPro.newExport.descriptor.xml.objects;

import java.util.Locale;
import java.util.Set;

import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeStart;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.newExport.profile.Profile;
import org.iviPro.newExport.profile.VideoProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterProject extends IXMLExporter {

	private static final String CONTENT_TYPE_VIDEO = "video"; //$NON-NLS-1$
	private static final String CONTENT_TYPE_AUDIO = "audio"; //$NON-NLS-1$
	private static final String CONTENT_TYPE_DIVIDER = "/"; //$NON-NLS-1$

	private Project project;
	
	XMLExporterProject(Project exportObj) {
		super(exportObj);
		project = exportObj;
	}

	@Override
	protected void exportObjectImpl(Object exportObj, Document doc,
			IdManager idManager, Project pro, Set<Object> alreadyExported)
			throws ExportException {
		
		// Create <siva> Element
		Element siva = createTagSiva(doc);
		doc.appendChild(siva);

		// Create empty <projectInformation>, <sceneList>, <ressources>,
		// <actions>. These will be filled later
		Element projectInfo = doc.createElement(TAG_PROJECTINFO);
		Element sceneList = doc.createElement(TAG_SCENELIST);
		Element ressources = doc.createElement(TAG_RESOURCES);
		Element actions = doc.createElement(TAG_ACTIONS);
		siva.appendChild(projectInfo);
		siva.appendChild(sceneList);
		siva.appendChild(ressources);
		siva.appendChild(actions);
		// Create <tableOfContents> Element
		createTocRoot(project, doc, siva, idManager, alreadyExported);
		// Create <index> Element
		siva.appendChild(doc.createElement(TAG_INDEX));

		// Create <projectInformation> Element
		createTagProjectInformation(projectInfo, doc, idManager,
				alreadyExported, project);

//		// Create <actions> Element
//		createTagActions(actions, doc, idManager);

		// Am Schluss Start-Knoten exportieren.
		NodeStart startNode = project.getSceneGraph().getStart();
		IXMLExporter exporter = ExporterFactory.createExporter(startNode);
		exporter.exportObject(doc, idManager, project, alreadyExported);
	}

	/**
	 * Erstellt den XML-Knoten fuer das Inhaltsverzeichnis. Liefert null, wenn
	 * das Projekt kein Inhaltsverzeichnis oder ein leeres Inhaltsverzeichnis
	 * hat.
	 * 
	 * @param project
	 *            Das Projekt.
	 * @param doc
	 *            Das XML-Dokument
	 * @param siva
	 *            Das siva Wurzel-Element.
	 * @param idManager
	 *            Der zu verwendende ID-Manager
	 * @param alreadyExported
	 *            Liste mit den bereits exportierten Model-Elementen.
	 * @throws ExportException
	 *             Falls beim Export des Inhaltsverzeichnis ein Fehler auftritt.
	 */
	private void createTocRoot(Project project, Document doc, Element siva,
			IdManager idManager, Set<Object> alreadyExported)
			throws ExportException {

		// Create TOC only if TOC entries exist, because <tableOfContens>
		// Tag must not be empty.
		TocItem toc = project.getTableOfContents();
		// toc = createTocTestEntries(project, toc);
		if (toc == null || toc.getChildren().isEmpty()) {
			return;
		}

		// TOC Root-Element erstellen
		Element tocRoot = doc.createElement(TAG_TOC_ROOT);

		String tocRootLabelID = createTitleLabels(toc, doc, idManager);
		tocRoot.setAttribute(ATTR_REF_RES_ID, tocRootLabelID);

		siva.appendChild(tocRoot);
		// TODO Auskommentiert, da im XML nicht als required markiert
		// und da im Moment eh immer "ROOT" als title des Tocs gesetzt ist.
		// String tocRootResId = createTitleLabels(toc, doc, idManager);
		// tocRoot.setAttribute(ATTR_REF_RES_ID, tocRootResId);

		// Kinder exportieren
		for (TocItem tocItem : toc.getChildren()) {
			IXMLExporter exporter = ExporterFactory.createExporter(tocItem);
			exporter.exportObject(doc, idManager, project, alreadyExported);
		}

	}

	@SuppressWarnings("unused")
	private TocItem createTocTestEntries(Project project, TocItem toc) {
		if (toc == null) {
			toc = new TocItem("Table of Contents", project); //$NON-NLS-1$
		}
		TocItem item1 = new TocItem("1. Introduction", project); //$NON-NLS-1$
		TocItem item1_1 = new TocItem("1.1 Eclipse RCP Framework", project); //$NON-NLS-1$
		TocItem item1_2 = new TocItem("1.2 JMF and FFMPEG", project); //$NON-NLS-1$
		TocItem item2 = new TocItem("2. SIVA Producer", project); //$NON-NLS-1$
		TocItem item2_1 = new TocItem("2.1 Overview", project); //$NON-NLS-1$
		TocItem item2_2 = new TocItem("2.2 Model", project); //$NON-NLS-1$
		TocItem item2_3 = new TocItem("2.3 Editors", project); //$NON-NLS-1$
		TocItem item2_3_1 = new TocItem("2.3.1 Scene Editor", project); //$NON-NLS-1$
		TocItem item2_3_2 = new TocItem("2.3.2 Scene Graph", project); //$NON-NLS-1$
		TocItem item2_3_3 = new TocItem("2.3.3 Annotation Editor", project); //$NON-NLS-1$
		TocItem item3 = new TocItem("3. SIVA Player", project); //$NON-NLS-1$
		TocItem item3_1 = new TocItem("3.1 Overview", project); //$NON-NLS-1$
		TocItem item3_2 = new TocItem("3.2 XML scheme", project); //$NON-NLS-1$
		TocItem item4 = new TocItem("4. Evaluation", project); //$NON-NLS-1$
		TocItem item5 = new TocItem("5. Conclusion", project); //$NON-NLS-1$

		item1.getChildren().add(item1_1);
		item1.getChildren().add(item1_2);
		item2.getChildren().add(item2_1);
		item2.getChildren().add(item2_2);
		item2.getChildren().add(item2_3);
		item2_3.getChildren().add(item2_3_1);
		item2_3.getChildren().add(item2_3_2);
		item2_3.getChildren().add(item2_3_3);
		item3.getChildren().add(item3_1);
		item3.getChildren().add(item3_2);

		toc.getChildren().add(item1);
		toc.getChildren().add(item2);
		toc.getChildren().add(item3);
		toc.getChildren().add(item4);
		toc.getChildren().add(item5);

		return toc;

	}

	/**
	 * Create <siva> Element
	 * 
	 * @param doc
	 * @return
	 */
	private Element createTagSiva(Document doc) {
		Element siva = doc.createElement(TAG_SIVA);
		siva.setAttribute(ATTR_SIVA_NAMESPACE, VAL_SIVA_NAMESPACE);
		siva.setAttribute(ATTR_SIVA_SCHEMA, VAL_SIVA_SCHEMA);
		return siva;
	}

	/**
	 * Erstellt das &lt;projectInformation&gt; Tag:<br>
	 * <br>
	 * 
	 * <pre>
	 *   &lt;projectInformation&gt;
	 *      &lt;languages defaultLangCode=&quot;de-de&quot;&gt;
	 *         &lt;language langCode=&quot;de-de&quot;/&gt;
	 *      &lt;/languages&gt;
	 *   &lt;/projectInformation&gt;
	 * </pre>
	 * 
	 * @param doc
	 * @param project
	 * @return
	 * @throws ExportException
	 */
	private void createTagProjectInformation(Element projectInfo, Document doc,
			IdManager idManager, Set<Object> alreadyExported,
			Project project) throws ExportException {
		Element languages = doc.createElement(TAG_LANGUAGES);
		projectInfo.appendChild(languages);

		Locale defaultLang = project.getDefaultLanguage();
		languages.setAttribute(ATTR_DEFAULTLANGCODE,
				getSivaLangcode(defaultLang));
		for (Locale locale : project.getLanguages()) {
			Element language = doc.createElement(TAG_LANGUAGE);
			language.setAttribute(ATTR_LANGCODE, getSivaLangcode(locale));
			languages.appendChild(language);
		}

		// String tocNodeLabelID = createTitleLabels(tocItem, doc, idManager);
		// tocNode.setAttribute(ATTR_REF_RES_ID, tocNodeLabelID);
		// Projektsettings in XML einfügen
		ProjectSettings settings = project.getSettings();
		// Add title attribute
		IAbstractBean videoTitleBean = 
				new IAbstractBean(settings.getVideoTitle(), project){};
		String pNameLabelID = createTitleLabels(videoTitleBean, doc, idManager);
		projectInfo.setAttribute(ATTR_VIDEONAMEREF, pNameLabelID);

		// Resolution
		Element width = doc.createElement(TAG_SETTINGS);
		width.setAttribute(ATTR_NAME, VAL_RESOLUTIONWIDTH);
		width.setAttribute(ATTR_VALUE, String.valueOf(
				settings.getResolutionWidth()));
		projectInfo.appendChild(width);
		Element height = doc.createElement(TAG_SETTINGS);
		height.setAttribute(ATTR_NAME, VAL_RESOLUTIONHEIGHT);
		height.setAttribute(ATTR_VALUE,
				String.valueOf(settings.getResolutionHeight()));
		projectInfo.appendChild(height);
		
		// Annotation bar visibility
		Element annobarVisibility = doc.createElement(TAG_SETTINGS);
		annobarVisibility.setAttribute(ATTR_NAME, VAL_ANNOBARVISIBILITY);
		annobarVisibility.setAttribute(ATTR_VALUE, 
				settings.getAnnobarVisibility().toString());
		projectInfo.appendChild(annobarVisibility);
		
		// Annotation bar behavior
		Element annobarOverlay = doc.createElement(TAG_SETTINGS);
		annobarOverlay.setAttribute(ATTR_NAME, VAL_ANNOBAROVERLAY);
		annobarOverlay.setAttribute(ATTR_VALUE,
				String.valueOf(settings.isAnnobarOverlayEnabled()));
		projectInfo.appendChild(annobarOverlay);
		
		// Side bar widths		
		Element sizeLeft = doc.createElement(TAG_SETTINGS);
		sizeLeft.setAttribute(ATTR_NAME, VAL_NAVIGATIONWIDTH);
		sizeLeft.setAttribute(ATTR_VALUE,
				String.valueOf(settings.getNavigationBarWidth()));
		projectInfo.appendChild(sizeLeft);
		Element sizeRight = doc.createElement(TAG_SETTINGS);
		sizeRight.setAttribute(ATTR_NAME, VAL_ANNOTATIONWIDTH);
		sizeRight.setAttribute(ATTR_VALUE,
				String.valueOf(settings.getAnnotationBarWidth()));
		projectInfo.appendChild(sizeRight);
		
		// Colors
		Element primaryColor = doc.createElement(TAG_SETTINGS);
		primaryColor.setAttribute(ATTR_NAME, VAL_PRIMARYCOLOR);
		primaryColor.setAttribute(ATTR_VALUE,
				String.valueOf(settings.getPrimaryColor()));
		projectInfo.appendChild(primaryColor);
		Element secondaryColor = doc.createElement(TAG_SETTINGS);
		secondaryColor.setAttribute(ATTR_NAME, VAL_SECONDARYCOLOR);
		secondaryColor.setAttribute(ATTR_VALUE,
				String.valueOf(settings.getSecondaryColor()));
		projectInfo.appendChild(secondaryColor);
		
		// Player functions
		Element userDiary = doc.createElement(TAG_SETTINGS);
		userDiary.setAttribute(ATTR_NAME, VAL_USERDIARY);
		userDiary.setAttribute(ATTR_VALUE, Boolean.toString(
				settings.isUserDiaryEnabled()));
		projectInfo.appendChild(userDiary);
		
		// Video settings
		Element autoplay = doc.createElement(TAG_SETTINGS);
		autoplay.setAttribute(ATTR_NAME, VAL_AUTOSTART);
		autoplay.setAttribute(ATTR_VALUE, Boolean.toString(
				settings.isAutostartEnabled()));
		projectInfo.appendChild(autoplay);
		Element logging = doc.createElement(TAG_SETTINGS);
		logging.setAttribute(ATTR_NAME, VAL_LOGGING);
		logging.setAttribute(ATTR_VALUE, Boolean.toString(
				settings.isLoggingEnabled()));
		projectInfo.appendChild(logging);
		
		// Collaboration
		Element collaboration = doc.createElement(TAG_SETTINGS);
		collaboration.setAttribute(ATTR_NAME, VAL_COLLABORATION);
		collaboration.setAttribute(ATTR_VALUE, Boolean.toString(
				settings.isCollaborationEnabled()));
		projectInfo.appendChild(collaboration);
		Element serverUrl = doc.createElement(TAG_SETTINGS);
		serverUrl.setAttribute(ATTR_NAME, VAL_SERVERURL);
		serverUrl.setAttribute(ATTR_VALUE,
				String.valueOf(settings.getServerUrl()));
		projectInfo.appendChild(serverUrl);
		
		// FIXME: Why is this needed?! <-- EDIT: needed for player  
		addResourceSettings(doc, projectInfo, idManager.getProfile());

		// Project Ressources
		BeanList<INodeAnnotationLeaf> globalAnnos = project
				.getGlobalAnnotations();
		for (INodeAnnotationLeaf anno : globalAnnos) {
			// Export der globalen Annotation
			IXMLExporter exporter = ExporterFactory.createExporter(anno);
			exporter.exportObject(doc, idManager, project, alreadyExported);
			// Action-Referenz darauf in ProjectRessources ablegen.
			Element annoElem = doc.createElement(TAG_PROJECTRESSOURCES);
			String actionID = idManager.getActionID(anno);
			annoElem.setAttribute(ATTR_REF_ACTION_ID, actionID);
			projectInfo.appendChild(annoElem);
		}
	}

	private void addResourceSettings(Document document, Element projectInfo,
			Profile profile) {
		if (profile.getVideo().getVideoVariants().size() == 1) {
			for (VideoProfile videoProfile : profile.getVideo()
					.getVideoVariants().get(0).getVideoProfiles()) {
				addVideoResourceSetting(document, projectInfo, videoProfile);
			}
		}
		if (profile.getAudio().getAudioVariants().size() == 1) {
			for (AudioProfile audioProfile : profile.getAudio()
					.getAudioVariants().get(0).getAudioProfiles()) {
				addAudioResourceSetting(document, projectInfo, audioProfile);
			}
		}
	}

	private void addVideoResourceSetting(Document document,
			Element projectInfo, VideoProfile videoProfile) {
		Element resourceSettings = document
				.createElement(ATTR_RESSOURCESETTING);
		resourceSettings.setAttribute(ATTR_TYPE, VAL_VIDEO);
		resourceSettings.setAttribute(ATTR_CONTENTTYPE, CONTENT_TYPE_VIDEO
				+ CONTENT_TYPE_DIVIDER
				+ videoProfile.getVideoContainer().getTranscoderParameter());
		resourceSettings.setAttribute(ATTR_FILEFORMAT, videoProfile
				.getVideoContainer().getFileExtension());
		resourceSettings.setAttribute(ATTR_RES_VIDEOCODEC, videoProfile
				.getVideoCodec().getLabel());
		resourceSettings.setAttribute(ATTR_RES_AUDIOCODEC, videoProfile
				.getAudioCodec().getLabel());
		projectInfo.appendChild(resourceSettings);
	}

	private void addAudioResourceSetting(Document document,
			Element projectInfo, AudioProfile audioProfile) {
		Element resourceSettings = document
				.createElement(ATTR_RESSOURCESETTING);
		resourceSettings.setAttribute(ATTR_TYPE, VAL_AUDIO);
		resourceSettings.setAttribute(ATTR_CONTENTTYPE, CONTENT_TYPE_AUDIO
				+ CONTENT_TYPE_DIVIDER
				+ audioProfile.getAudioContainer().getTranscoderParameter());
		resourceSettings.setAttribute(ATTR_FILEFORMAT, audioProfile
				.getAudioContainer().getFileExtension());
		resourceSettings.setAttribute(ATTR_RES_AUDIOCODEC, audioProfile
				.getAudioCodec().getLabel());
		projectInfo.appendChild(resourceSettings);
	}

	/**
	 * Gibt den Language Code des SIVA XML-Formats fuer ein bestimmtes Locale
	 * zurueck.
	 * 
	 * @param locale
	 * @return
	 */
	private String getSivaLangcode(Locale locale) {
		LocalizedString helperStr = new LocalizedString(null, locale);
		return helperStr.getSivaLangcode();
	}

//	/**
//	 * Erstellt ein initiales &lt;actions&gt; Tag mit einer vordefinierten
//	 * End-Siva-Action:<br>
//	 * <br>
//	 * 
//	 * <pre>
//	 *   &lt;actions&gt;
//	 *      &lt;endSiva actionID=&quot;end-siva&quot;/&gt;
//	 *   &lt;/actions&gt;
//	 * </pre>
//	 * 
//	 * 
//	 * @param doc
//	 * @return
//	 */
//	private void createTagActions(Element actions, Document doc,
//			IdManager idManager) {
//		Element endSiva = doc.createElement(TAG_ENDSIVA);
//		endSiva.setAttribute(ATTR_ACTIONID, idManager.getEndActionID());
//		endSiva.setIdAttribute(ATTR_ACTIONID, true);
//		actions.appendChild(endSiva);
//	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return Project.class;
	}
}
