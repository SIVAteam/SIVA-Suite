package org.iviPro.export.smil.objects;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Picture;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SMILExporterNodeSelection extends SMILExporter {

	SMILExporterNodeSelection(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeSelection.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {

		NodeSelection selection = (NodeSelection) exportObj;
		NodeSelectionControl defControl = selection.getDefaultControl();

		// Exclusive-element containing the paths
		Element exclusive = doc.createElement(TAG_EXCLUSIVE);

		LinkedList<LinkedList<IGraphNode>> paths = forkPaths(selection,
				alreadyExported);

		int defaultPosition = getDefaultPathNumber(selection, defControl);
		
		exportSelectionPaths(paths, doc, idManager, project, alreadyExported,
				exclusive, defaultPosition);

		// Parallel-element containing the choices
		Element parallel = doc.createElement(TAG_PAR);
		parallel.setAttribute(ATTR_ID, idManager.getID(selection));
		parallel.setAttribute(ATTR_DURATION, VAL_INDEFINITE);
		int timeout = selection.getTimeout();
		if (timeout > 0) {
			parallel.setAttribute(ATTR_DURATION, String.valueOf(timeout) + ADDITION_SECONDS);
		}

		// Title of the selection
		Element title = doc.createElement(TAG_TEXT);
		title.setTextContent(selection.getTitle());
		title.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
		title.setAttribute(ATTR_HEIGHT, VAL_TOC_HEIGHT);
		title.setAttribute(ATTR_TOP, VAL_ZERO);
		parallel.appendChild(title);

		List<IGraphNode> selectionControls = selection
				.getChildren(NodeSelectionControl.class);
		int linkOffset = 20;
		if (selectionControls.size() + 1 == paths.size()) {
			for (int i = 0; i < paths.size() - 1; i++) {
				IGraphNode firstNode = paths.get(i).getFirst();
				NodeSelectionControl control = (NodeSelectionControl) selectionControls
						.get(i);

				Picture pic = control.getButtonImage();

				Element link = doc.createElement(TAG_LINK);
				link.setAttribute(ATTR_H_REFERENCE,
						"#" + idManager.getID(firstNode));
				if (pic == null) {
					Element text = doc.createElement(TAG_TEXT);
					text.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
					text.setAttribute(ATTR_HEIGHT, VAL_TOC_HEIGHT);
					text.setAttribute(ATTR_TOP, String.valueOf(linkOffset));
					text.setAttribute(ATTR_LEFT, VAL_SELECTION_OFFSET);
					linkOffset += Integer.valueOf(VAL_TOC_HEIGHT);
					text.setTextContent(control.getTitle());
					link.appendChild(text);
				} else {
					Element pictureElement = doc.createElement(TAG_IMAGE);
					Locale defLanguage = project.getDefaultLanguage();
					String filename = "../pix/"
							+ idManager.getFilename(pic, defLanguage,
									getExportParameters());
					pictureElement.setAttribute(ATTR_SOURCE, filename);
					pictureElement
							.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
					pictureElement
							.setAttribute(ATTR_HEIGHT, VAL_TOC_PIC_LENGTH);
					pictureElement.setAttribute(ATTR_WIDTH, VAL_TOC_PIC_LENGTH);
					pictureElement.setAttribute(ATTR_TOP,
							String.valueOf(linkOffset));
					pictureElement
							.setAttribute(ATTR_LEFT, VAL_SELECTION_OFFSET);
					link.appendChild(pictureElement);

					Element text = doc.createElement(TAG_TEXT);
					text.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
					text.setAttribute(ATTR_HEIGHT, VAL_TOC_HEIGHT);
					text.setAttribute(ATTR_TOP, String.valueOf(linkOffset));
					int textWithPicOffset = Integer
							.valueOf(VAL_SELECTION_OFFSET)
							+ Integer.valueOf(VAL_TOC_PIC_LENGTH) + 2;
					text.setAttribute(ATTR_LEFT,
							String.valueOf(textWithPicOffset));
					linkOffset += Integer.valueOf(VAL_TOC_HEIGHT);
					text.setTextContent(control.getTitle());
					link.appendChild(text);
				}
				parallel.appendChild(link);
			}
		}

		// Append an empty link to "close" the link list
		Element emptyLink = doc.createElement(TAG_LINK);
		Element emptyText = doc.createElement(TAG_TEXT);
		emptyText.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
		emptyText.setAttribute(ATTR_TOP, String.valueOf(linkOffset));
		emptyLink.appendChild(emptyText);
		parallel.appendChild(emptyLink);

		// Append the parallel-element in front of the exclusive
		if (parent == null) {
			getMainSeqElement(doc).appendChild(parallel);
			getMainSeqElement(doc).appendChild(exclusive);
		} else {
			parent.appendChild(parallel);
			parent.appendChild(exclusive);
		}

		IGraphNode intersection = paths.getLast().getLast();
		IGraphNode furtherExportNode = null;
		if (!oneNodePathIntersection(paths, intersection)
				|| intersection instanceof NodeEnd) {
			furtherExportNode = intersection;
		} else {
			if (intersection instanceof NodeScene) {
				furtherExportNode = onlyImportantChild((NodeScene) intersection);
			} else if ((intersection instanceof NodeSelection)
					|| (intersection instanceof NodeQuiz)) {
				furtherExportNode = firstCommonPathNode(intersection,
						alreadyExported);
			}
		}
		SMILExporter exporter = SMILExporterFactory
				.createSMILExporter(furtherExportNode);
		exporter.exportObject(doc, idManager, project, alreadyExported, parent);
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

	/**
	 * Method to export all the nodes on the path, also giving them a special
	 * father, the exclusive-node of the selection.
	 * 
	 * @param paths
	 *            The paths that are to be exported.
	 * @param father
	 *            The exclusive-element for the selection.
	 * @throws ExportException
	 */
	private void exportSelectionPaths(LinkedList<LinkedList<IGraphNode>> paths,
			Document doc, IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent, int defaultPosition)
			throws ExportException {
		for (int i = 0; i < paths.size() - 1; i++) {
			LinkedList<IGraphNode> path = paths.get(i);
			Element sequential = doc.createElement(TAG_SEQ);
			if(i == defaultPosition) {
				sequential.setAttribute(ATTR_BEGIN, "0s");
			}

			int pathSize = path.size();
			if (!alreadyExported.contains(path.getLast())) {
				pathSize -= 1;
			}

			// Export all but the last node
			for (int j = 0; j < pathSize; j++) {
				IGraphNode node = path.get(j);
				SMILExporter exporter = SMILExporterFactory
						.createSMILExporter(node);
				exporter.exportObject(doc, idManager, project, alreadyExported,
						sequential);
			}
			parent.appendChild(sequential);
		}
	}

	/**
	 * Method checks the NodeSelectionControl-children of the given selection
	 * and returns the number of the default control, if there is one. If there
	 * is no default path, -1 is returned.
	 * 
	 * @param selection
	 *            The selection whose default position is to be found.
	 * @return The number of the selection or -1 if there is no default
	 *         selection.
	 */
	private int getDefaultPathNumber(NodeSelection selection, NodeSelectionControl defaultControl) {
		int defaultPosition = -1;
		
		List<IGraphNode> children = selection.getChildren(NodeSelectionControl.class);
		
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i) == defaultControl) {
				defaultPosition = i;
				break;
			}
		}
		
		return defaultPosition;
	}

}
