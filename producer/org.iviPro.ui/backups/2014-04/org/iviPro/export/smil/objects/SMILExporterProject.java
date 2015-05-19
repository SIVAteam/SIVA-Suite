package org.iviPro.export.smil.objects;

import java.util.List;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeStart;
import org.iviPro.model.graph.ScreenArea;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SMILExporterProject extends SMILExporter {

	private int tocTopOffset = 0;
	protected static int mainRegionHeight;
	protected static int mainRegionWidth;

	public SMILExporterProject(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return Project.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project pro,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {

		Project project = (Project) exportObj;

		// Create outermost tag smil with its children head and body
		Element smil = createSmilTag(doc);

		Element head = doc.createElement(TAG_HEAD);
		smil.appendChild(head);
		
		Element meta = doc.createElement(TAG_META);
		meta.setAttribute(ATTR_NAME, project.getTitle());
		head.appendChild(meta);

		Element body = doc.createElement(TAG_BODY);
		smil.appendChild(body);

		doc.appendChild(smil);

		// Get the settings
		ProjectSettings settings = project.getSettings();

		boolean fullScreen = settings.isFullscreen();

		int height = settings.getSizeHeight();
		int width = settings.getSizeWidth();

		int topHeight = (int) (settings.getAreaTopHeight() * settings
				.getSizeHeight());
		int bottomHeight = (int) (settings.getAreaBottomHeight() * settings
				.getSizeHeight());
		int leftWidth = (int) (settings.getAreaLeftWidth() * settings
				.getSizeWidth());
		int rightWidth = (int) (settings.getAreaRightWidth() * settings
				.getSizeWidth());

		int mainAreaHeight = height;
		int mainAreaWidth = width;

		mainRegionWidth = mainAreaWidth;
		mainRegionHeight = mainAreaHeight;
		
		if (!fullScreen) {
			mainAreaHeight -= (topHeight + bottomHeight);
			mainAreaWidth -= (leftWidth + rightWidth);
		}	

		// The layout of the siva project determines the layout section of the
		// SMIL-file
		Element layout = doc.createElement(TAG_LAYOUT);
		Element rootLayout = doc.createElement(TAG_ROOT_LAYOUT);
		head.appendChild(layout);
		rootLayout.setAttribute(ATTR_ID, VAL_ROOT_LAYOUT_ID);
		rootLayout.setAttribute(ATTR_HEIGHT, String.valueOf(height));
		rootLayout.setAttribute(ATTR_WIDTH, String.valueOf(width));
		rootLayout.setAttribute(ATTR_BACKGROUND_COLOR, VAL_COLOR_SILVER);
		String title = settings.getTitle();
		if (title != null && !title.equals("")) {
			rootLayout.setAttribute(ATTR_TITLE, title);
		}
		layout.appendChild(rootLayout);

		// Setup the main region
		Element mainRegion = doc.createElement(TAG_REGION);
		rootLayout.appendChild(mainRegion);
		mainRegion.setAttribute(ATTR_ID, VAL_MAIN_REGION_ID);
		mainRegion.setAttribute(ATTR_FIT, VAL_MEET);

		// Setup a region, where the animation will take place
		Element animationRegion = doc.createElement(TAG_REGION);
		rootLayout.appendChild(animationRegion);
		animationRegion.setAttribute(ATTR_ID, VAL_ANIMATION_REGION_ID);
		animationRegion.setAttribute(ATTR_FIT, VAL_MEET);
		animationRegion.setAttribute(ATTR_BACKGROUND_OPACITY, VAL_ZERO_PERCENT);
		
		// Setup an additional region, where buttons and other things can be displayed, without being stretched
		Element buttonRegion = doc.createElement(TAG_REGION);
		rootLayout.appendChild(buttonRegion);
		buttonRegion.setAttribute(ATTR_ID, VAL_BUTTON_REGION_ID);
		buttonRegion.setAttribute(ATTR_BACKGROUND_OPACITY, VAL_ZERO_PERCENT);
				
		if (fullScreen) {
			setRegionDimensions(mainRegion, VAL_ZERO, VAL_ZERO, VAL_ZERO, VAL_ZERO);
			setRegionDimensions(animationRegion, VAL_ZERO, VAL_ZERO, VAL_ZERO, VAL_ZERO);
			setRegionDimensions(buttonRegion, VAL_ZERO, VAL_ZERO, VAL_ZERO, VAL_ZERO);
		} else {
			setRegionDimensions(mainRegion, String.valueOf(topHeight), String.valueOf(leftWidth), String.valueOf(bottomHeight), String.valueOf(rightWidth));
			setRegionDimensions(animationRegion, String.valueOf(topHeight), String.valueOf(leftWidth), String.valueOf(bottomHeight), String.valueOf(rightWidth));
			setRegionDimensions(buttonRegion, String.valueOf(topHeight), String.valueOf(leftWidth), String.valueOf(bottomHeight), String.valueOf(rightWidth));
		}

		// Setup the 4 regions around the main frame
		if (topHeight > 0) {
			Element topRegion = createRegionElement(doc, 't', mainAreaWidth,
					mainAreaHeight, topHeight, leftWidth, bottomHeight,
					rightWidth, fullScreen);
			rootLayout.appendChild(topRegion);
		}

		if (leftWidth > 0) {
			Element leftRegion = createRegionElement(doc, 'l', mainAreaWidth,
					mainAreaHeight, topHeight, leftWidth, bottomHeight,
					rightWidth, fullScreen);
			rootLayout.appendChild(leftRegion);
		}

		if (bottomHeight > 0) {
			Element botRegion = createRegionElement(doc, 'b', mainAreaWidth,
					mainAreaHeight, topHeight, leftWidth, bottomHeight,
					rightWidth, fullScreen);
			rootLayout.appendChild(botRegion);
		}

		if (rightWidth > 0) {
			Element rightRegion = createRegionElement(doc, 'r', mainAreaWidth,
					mainAreaHeight, topHeight, leftWidth, bottomHeight,
					rightWidth, fullScreen);
			rootLayout.appendChild(rightRegion);
		}

		// The first parallel node of the body will hold the sequential node for
		// all the scenes and the node that will hold the TOC
		Element mainPar = doc.createElement(TAG_PAR);
		body.appendChild(mainPar);

		Element mainSeq = doc.createElement(TAG_SEQ);
		mainPar.appendChild(mainSeq);

		// Export the start node of this project
		NodeStart start = project.getSceneGraph().getStart();
		SMILExporter exporter = SMILExporterFactory.createSMILExporter(start);
		exporter.exportObject(doc, idManager, project, alreadyExported, null);
		
		// Export the global annotations
		BeanList<INodeAnnotationLeaf> globalAnnotations = project.getGlobalAnnotations();
		for(INodeAnnotationLeaf annotation : globalAnnotations) {
			SMILExporter annotationExporter = SMILExporterFactory.createSMILExporter(annotation);
			annotationExporter.exportObject(doc, idManager, project, alreadyExported, mainPar);
		}

		createTocElement(doc, idManager, pro);

		// Append as last child an empty child with the id for the end node, so
		// from every where in the file a jump to the end can be made
		Element end = doc.createElement(TAG_SEQ);
		end.setAttribute(ATTR_ID, VAL_ENDNODE_ID);
		mainSeq.appendChild(end);
	}
	
	/**
	 * Method to set the dimensions for a given region.
	 * 
	 * @param region	The region, whose attributes are to be set.
	 * @param top	The top attribute.
	 * @param bot	The bottom attribute.
	 * @param right	The right attribute.
	 * @param left	The left attribute.
	 */
	private void setRegionDimensions(Element region, String top, String left, String bot, String right) {
		region.setAttribute(ATTR_TOP, top);
		region.setAttribute(ATTR_LEFT, left);
		region.setAttribute(ATTR_BOTTOM, bot);
		region.setAttribute(ATTR_RIGHT, right);
	}

	/**
	 * Method to create the element holding the table of contents.
	 * 
	 * @param doc
	 *            The document file, where the SMIL-file is built.
	 * @param idManager
	 *            The idManager of the current project.
	 * @param project
	 *            The currently exported project.
	 * @throws ExportException
	 */
	private void createTocElement(Document doc, IDManager idManager,
			Project project) throws ExportException {

		TocItem toc = project.getTableOfContents();
		if (toc == null || toc.getChildren().isEmpty()) {
			// If there is no table of contents, the return here will result in
			// not creating an element for the toc at all, so it won't be
			// displayed in the final presentation
			return;
		}

		Element tocElement = doc.createElement(TAG_PAR);
		tocElement.setAttribute(ATTR_BEGIN, VAL_TOC_BUTTON_ID + ACTIVATE_ELEMENT_EVENT);
		tocElement.setAttribute(ATTR_END, VAL_TOC_BUTTON_ID + ACTIVATE_ELEMENT_EVENT);
		tocElement.setAttribute(ATTR_RESTART, VAL_WHEN_NOT_ACTIVE);

		// Position
		ScreenArea screenArea = null;
		screenArea = toc.getScreenArea();
		String tocRegion = screenArea.toString().toLowerCase();

		if (tocRegion.equals("overlay")) {
			tocRegion = VAL_MAIN_REGION_ID;
		} else {
			tocRegion += "_region";
		}

		// Export the actual table of content
		exportToc(doc, idManager, tocElement, tocRegion, toc.getChildren(), 0);

		Element lastLink = doc.createElement(TAG_LINK);
		Element lastText = doc.createElement(TAG_TEXT);
		lastText.setAttribute(ATTR_TOP, String.valueOf(tocTopOffset));
		lastText.setAttribute(ATTR_REGION, tocRegion);
		lastText.setAttribute(ATTR_BEGIN, VAL_TOC_BUTTON_ID + ACTIVATE_ELEMENT_EVENT);
		lastText.setAttribute(ATTR_END, VAL_TOC_BUTTON_ID + ACTIVATE_ELEMENT_EVENT);
		lastText.setAttribute(ATTR_RESTART, VAL_WHEN_NOT_ACTIVE);
		lastLink.appendChild(lastText);
		tocElement.appendChild(lastLink);

		// Append the toc-element into the current document
		getBody(doc).getFirstChild().appendChild(tocElement);
		
		createTocButton(doc);
	}

	private void exportToc(Document doc, IDManager idManager, Element tocRoot,
			String tocRegion, List<TocItem> tocs, int tocLeftOffset) {
		for (TocItem tocChild : tocs) {
			NodeScene scene = tocChild.getScene();

			String sceneID = idManager.getID(scene);
			String reference = "#" + sceneID;

			Element linkElement = doc.createElement(TAG_LINK);
			linkElement.setAttribute(ATTR_H_REFERENCE, reference);

			Element textElement = doc.createElement(TAG_TEXT);
			textElement.setAttribute(ATTR_REGION, tocRegion);
			textElement
					.setAttribute(ATTR_LEFT, Integer.toString(tocLeftOffset));
			textElement.setAttribute(ATTR_TOP, Integer.toString(tocTopOffset));
			tocTopOffset += 20;
			textElement.setAttribute(ATTR_HEIGHT, VAL_TOC_HEIGHT);
			textElement.setAttribute(ATTR_BEGIN, VAL_TOC_BUTTON_ID + ACTIVATE_ELEMENT_EVENT);
			textElement.setAttribute(ATTR_END, VAL_TOC_BUTTON_ID + ACTIVATE_ELEMENT_EVENT);
			textElement.setAttribute(ATTR_RESTART, VAL_WHEN_NOT_ACTIVE);
			textElement.setTextContent(tocChild.getTitle());

			linkElement.appendChild(textElement);
			tocRoot.appendChild(linkElement);

			List<TocItem> tocChildren = tocChild.getChildren();
			if (tocChildren.size() > 0) {
				int nextTocLeftOffset = tocLeftOffset + 20;
				exportToc(doc, idManager, tocRoot, tocRegion, tocChildren,
						nextTocLeftOffset);
			}
		}
	}
	
	/**
	 * Method to create the button that will toggle the table of contents tab.
	 * 
	 * @param doc	The document for the SMIL-file.
	 * @throws ExportException 
	 * @throws DOMException 
	 */
	private void createTocButton(Document doc) throws DOMException, ExportException {
		Element button = doc.createElement(TAG_IMAGE);
		button.setAttribute(ATTR_ID, VAL_TOC_BUTTON_ID);
		button.setAttribute(ATTR_SOURCE, TOC_BUTTON_ADRESS);
		button.setAttribute(ATTR_BOTTOM, VAL_TOC_BUTTON_BOTTOM);
		button.setAttribute(ATTR_RIGHT, VAL_TOC_BUTTON_RIGHT);
		button.setAttribute(ATTR_REGION, VAL_BUTTON_REGION_ID);
		button.setAttribute(ATTR_HEIGHT, VAL_TOC_BUTTON_HEIGHT);
		button.setAttribute(ATTR_WIDTH, VAL_TOC_BUTTON_WIDTH);
		
		getBody(doc).getFirstChild().appendChild(button);
	}

	/**
	 * Creates the outermost tag <code>smil</code> with its attribute.
	 * 
	 * @param doc
	 *            The document for the smil file.
	 * @return The <smil> tag element.
	 */
	private Element createSmilTag(Document doc) {
		Element smil = doc.createElement(TAG_SMIL);
		smil.setAttribute(ATTR_XML_NAMESPACE, VAL_SMIL_NAMESPACE);

		return smil;
	}

	/**
	 * Creates a region for the top, left, bottom or right area, specified by
	 * the given tagID.
	 * 
	 * @param doc
	 *            The document representing the SMIL-file.
	 * @param tag
	 *            A character representing one of four areas (t for top, l for
	 *            left, b for bottom and r for right).
	 * @param mainWidth
	 *            The width of the main area.
	 * @param mainHeight
	 *            The height of the main area.
	 * @param topHeight
	 *            The height for the top area.
	 * @param leftWidth
	 *            The width of the left area.
	 * @param bottomHeight
	 *            The height of the bottom area.
	 * @param rightWidth
	 *            The width of the right area.
	 * @return A region element representing a specific area in the SMIL-file.
	 */
	private Element createRegionElement(Document doc, char tag, int mainWidth,
			int mainHeight, int topHeight, int leftWidth, int bottomHeight,
			int rightWidth, boolean fullScreen) {

		Element region = doc.createElement(TAG_REGION);

		// The character that is switched over can be changed to Strings (the id
		// for the specific region!) with Java 7
		switch (tag) {
		case 't':
			region.setAttribute(ATTR_ID, VAL_TOP_REGION_ID);
			region.setAttribute(ATTR_TOP, VAL_ZERO);
			region.setAttribute(ATTR_LEFT, String.valueOf(leftWidth));
			region.setAttribute(ATTR_RIGHT, String.valueOf(rightWidth));
			if (fullScreen) {
				region.setAttribute(ATTR_BOTTOM,
						String.valueOf(mainHeight - topHeight));
			} else {
				region.setAttribute(ATTR_BOTTOM,
						String.valueOf(bottomHeight + mainHeight));
			}
			break;
		case 'l':
			region.setAttribute(ATTR_ID, VAL_LEFT_REGION_ID);
			region.setAttribute(ATTR_TOP, VAL_ZERO);
			region.setAttribute(ATTR_LEFT, VAL_ZERO);
			region.setAttribute(ATTR_BOTTOM, VAL_ZERO);
			if (fullScreen) {
				region.setAttribute(ATTR_RIGHT,
						String.valueOf(mainWidth - leftWidth));
			} else {
				region.setAttribute(ATTR_RIGHT,
						String.valueOf(rightWidth + mainWidth));
			}
			break;
		case 'b':
			region.setAttribute(ATTR_ID, VAL_BOT_REGION_ID);
			region.setAttribute(ATTR_LEFT, String.valueOf(leftWidth));
			region.setAttribute(ATTR_BOTTOM, VAL_ZERO);
			region.setAttribute(ATTR_RIGHT, String.valueOf(rightWidth));
			if (fullScreen) {
				region.setAttribute(ATTR_TOP,
						String.valueOf(mainHeight - bottomHeight));
			} else {
				region.setAttribute(ATTR_TOP,
						String.valueOf(topHeight + mainHeight));
			}
			break;
		case 'r':
			region.setAttribute(ATTR_ID, VAL_RIGHT_REGION_ID);
			region.setAttribute(ATTR_TOP, VAL_ZERO);
			region.setAttribute(ATTR_BOTTOM, VAL_ZERO);
			region.setAttribute(ATTR_RIGHT, VAL_ZERO);
			if (fullScreen) {
				region.setAttribute(ATTR_LEFT,
						String.valueOf(mainWidth - rightWidth));
			} else {
				region.setAttribute(ATTR_LEFT,
						String.valueOf(leftWidth + mainWidth));
			}
			break;
		}

		if (fullScreen) {
			region.setAttribute(ATTR_BACKGROUND_OPACITY, VAL_ZERO_PERCENT);
		}

		region.setAttribute(ATTR_FIT, VAL_MEET);

		return region;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
