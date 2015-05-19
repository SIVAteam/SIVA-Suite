package org.iviPro.export.xml.objects;

import java.util.Locale;
import java.util.Set;
import org.iviPro.export.ExportException;
import org.iviPro.export.ExportType;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeStart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterProject extends IXMLExporter {

	XMLExporterProject(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project pro, Set<IAbstractBean> alreadyExported)
			throws ExportException {
		Project project = (Project) exportObj;

		// Create <siva> Element
		Element siva = createTagSiva(doc);
		doc.appendChild(siva);

		// Create empty <projectInformation>, <sceneList>, <ressources>,
		// <actions>. These will be filled later
		Element projectInfo = doc.createElement(TAG_PROJECTINFO);
		Element sceneList = doc.createElement(TAG_SCENELIST);
		Element ressources = doc.createElement(TAG_RESSOURCES);
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

		// Create <actions> Element
		createTagActions(actions, doc, idManager);


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
			IDManager idManager, Set<IAbstractBean> alreadyExported)
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

		// Export ScreenArea/Path
		Element posInfo = createPositionInfo(toc, doc, idManager,
				project);
		tocRoot.appendChild(posInfo);
		
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
		siva.setAttribute("xmlns:xsi", //$NON-NLS-1$
				"http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$
		siva.setAttribute("xsi:noNamespaceSchemaLocation", "sivaPlayer.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
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
			IDManager idManager, Set<IAbstractBean> alreadyExported,
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

		//String tocNodeLabelID = createTitleLabels(tocItem, doc, idManager);
		//tocNode.setAttribute(ATTR_REF_RES_ID, tocNodeLabelID);
		// Projektsettings in XML einfügen
		ProjectSettings settings = project.getSettings();
		
		String pName = settings.getProjectName();
		
		if (pName == null || pName.length() == 0) {
			pName = "NoProjectTitle";
		}
		
		IAbstractBean pNameBean = new IAbstractBean(pName, project) {};
		
		String pNameLabelID = createTitleLabels(pNameBean, doc, idManager);
		projectInfo.setAttribute(ATTR_VIDEONAMEREF, pNameLabelID);
		/* Old Projectname export
		Element projectName = doc.createElement(TAG_SETTINGS);
		projectName.setAttribute(ATTR_NAME, VAL_PROJECTNAME);
		projectName.setAttribute(ATTR_VALUE, pName);
		projectInfo.appendChild(projectName);
		*/
		Element startmode = doc.createElement(TAG_SETTINGS);
		String mode = settings.isFullscreen() ? VAL_FULL : VAL_WINDOWED;
		startmode.setAttribute(ATTR_NAME, VAL_STARTMODE);
		startmode.setAttribute(ATTR_VALUE, mode);
		projectInfo.appendChild(startmode);
		Element width = doc.createElement(TAG_SETTINGS);
		width.setAttribute(ATTR_NAME, VAL_WIDTH);
		width.setAttribute(ATTR_VALUE, String.valueOf(settings.getSizeWidth()));
		projectInfo.appendChild(width);
		Element height = doc.createElement(TAG_SETTINGS);
		height.setAttribute(ATTR_NAME, VAL_HEIGHT);
		height.setAttribute(ATTR_VALUE, String
				.valueOf(settings.getSizeHeight()));
		projectInfo.appendChild(height);
		Element sizeLeft = doc.createElement(TAG_SETTINGS);
		sizeLeft.setAttribute(ATTR_NAME, VAL_SIZELEFT);
		sizeLeft.setAttribute(ATTR_VALUE, String.valueOf(settings
				.getAreaLeftWidth()));
		projectInfo.appendChild(sizeLeft);
		Element sizeTop = doc.createElement(TAG_SETTINGS);
		sizeTop.setAttribute(ATTR_NAME, VAL_SIZETOP);
		sizeTop.setAttribute(ATTR_VALUE, String.valueOf(settings
				.getAreaTopHeight()));
		projectInfo.appendChild(sizeTop);
		Element sizeBottom = doc.createElement(TAG_SETTINGS);
		sizeBottom.setAttribute(ATTR_NAME, VAL_SIZEBOTTOM);
		sizeBottom.setAttribute(ATTR_VALUE, String.valueOf(settings
				.getAreaBottomHeight()));
		projectInfo.appendChild(sizeBottom);
		Element sizeRight = doc.createElement(TAG_SETTINGS);
		sizeRight.setAttribute(ATTR_NAME, VAL_SIZERIGHT);
		sizeRight.setAttribute(ATTR_VALUE, String.valueOf(settings
				.getAreaRightWidth()));
		projectInfo.appendChild(sizeRight);

		if (settings.isPrimaryColor()) {
			Element primaryColor = doc.createElement(TAG_SETTINGS);
			primaryColor.setAttribute(ATTR_NAME, VAL_PRIMARYCOLOR);
			primaryColor.setAttribute(ATTR_VALUE, String.valueOf(settings.getPrimaryColor()));
			projectInfo.appendChild(primaryColor);
		} else {
			Element designName = doc.createElement(TAG_SETTINGS);
			designName.setAttribute(ATTR_NAME, VAL_DESIGNNAME);
			designName.setAttribute(ATTR_VALUE, String.valueOf(settings.getDesignName()));
			projectInfo.appendChild(designName);
			
			Element designSchema = doc.createElement(TAG_SETTINGS);
			designSchema.setAttribute(ATTR_NAME, VAL_DESIGNSCHEMA);
			designSchema.setAttribute(ATTR_VALUE, String.valueOf(settings.getDesignSchema()));
			projectInfo.appendChild(designSchema);
			
			Element colorSchema = doc.createElement(TAG_SETTINGS);
			colorSchema.setAttribute(ATTR_NAME, VAL_COLORSCHEMA);
			colorSchema.setAttribute(ATTR_VALUE, String.valueOf(settings.getColorSchema()));
			projectInfo.appendChild(colorSchema);
			
			Element backgroundColor = doc.createElement(TAG_SETTINGS);
			backgroundColor.setAttribute(ATTR_NAME, VAL_BACKGROUNDCOLOR);
			backgroundColor.setAttribute(ATTR_VALUE, String.valueOf(settings.getBackgroundColor()));
			projectInfo.appendChild(backgroundColor);
			
			Element borderColor = doc.createElement(TAG_SETTINGS);
			borderColor.setAttribute(ATTR_NAME, VAL_BORDERCOLOR);
			borderColor.setAttribute(ATTR_VALUE, String.valueOf(settings.getBorderColor()));
			projectInfo.appendChild(borderColor);
			
			Element fontColor = doc.createElement(TAG_SETTINGS);
			fontColor.setAttribute(ATTR_NAME, VAL_FONTCOLOR);
			fontColor.setAttribute(ATTR_VALUE, String.valueOf(settings.getTextColor()));
			projectInfo.appendChild(fontColor);
			
			Element font = doc.createElement(TAG_SETTINGS);
			font.setAttribute(ATTR_NAME, VAL_FONT);
			font.setAttribute(ATTR_VALUE, String.valueOf(settings.getFont()));
			projectInfo.appendChild(font);
			
			Element fontSize = doc.createElement(TAG_SETTINGS);
			fontSize.setAttribute(ATTR_NAME, VAL_FONTSIZE);
			fontSize.setAttribute(ATTR_VALUE, String.valueOf(settings.getFontSize()));
			projectInfo.appendChild(fontSize);
		}
		
		boolean autoReloadTrigger = settings.isAutoreload();
		if (autoReloadTrigger) {
			Element autoReload = doc.createElement(TAG_SETTINGS);
			autoReload.setAttribute(ATTR_NAME, VAL_AUTORELOAD);
			autoReload.setAttribute(ATTR_VALUE, String.valueOf(settings.getAutoreloadTime()));
			projectInfo.appendChild(autoReload);
		}
		
		if (idManager.getExportType() == ExportType.HTML5) {
			String[] contenttypes = {"video/webm", "video/ogg", "video/mp4"};
			String[] fileformats = {"webm", "ogv", "mp4"};
			String[] videocodecs = {"vp8", "theora", "avc"};
			String[] audiocodecs = {"vorbis", "vorbis", "aac"};
			// Append ressourceSetting tag
			for (int i = 0; i < contenttypes.length; i++) {
				Element video = doc.createElement(ATTR_RESSOURCESETTING);
				video.setAttribute(ATTR_TYPE, VAL_VIDEO);
				video.setAttribute(ATTR_CONTENTTYPE, contenttypes[i]);
				video.setAttribute(ATTR_FILEFORMAT, fileformats[i]);
				video.setAttribute(ATTR_RES_VIDEOCODEC, videocodecs[i]);
				video.setAttribute(ATTR_RES_AUDIOCODEC, audiocodecs[i]);
				projectInfo.appendChild(video);
			}
			
			String[] audioContentTypes = {"audio/mp3", "audio/ogg", "audio/mp4"};
			String[] audioFileformats = {"mp3", "ogg", "mp4"};
			String[] audioAudioCodecs = {"mp3", "vorbis", "aac"};
			for (int i = 0; i < audioContentTypes.length; i++) {
				Element audio = doc.createElement(ATTR_RESSOURCESETTING);
				audio.setAttribute(ATTR_TYPE, VAL_AUDIO);
				audio.setAttribute(ATTR_CONTENTTYPE, audioContentTypes[i]);
				audio.setAttribute(ATTR_FILEFORMAT, audioFileformats[i]);
				audio.setAttribute(ATTR_RES_AUDIOCODEC, audioAudioCodecs[i]);
				projectInfo.appendChild(audio);
			}
		}

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

	/**
	 * Erstellt ein initiales &lt;actions&gt; Tag mit einer vordefinierten
	 * End-Siva-Action:<br>
	 * <br>
	 * 
	 * <pre>
	 *   &lt;actions&gt;
	 *      &lt;endSiva actionID=&quot;end-siva&quot;/&gt;
	 *   &lt;/actions&gt;
	 * </pre>
	 * 
	 * 
	 * @param doc
	 * @return
	 */
	private void createTagActions(Element actions, Document doc, IDManager idManager) {
		Element endSiva = doc.createElement(TAG_ENDSIVA);
		endSiva.setAttribute(ATTR_ACTIONID, idManager.getEndActionID());
		endSiva.setIdAttribute(ATTR_ACTIONID, true);
		actions.appendChild(endSiva);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return Project.class;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
