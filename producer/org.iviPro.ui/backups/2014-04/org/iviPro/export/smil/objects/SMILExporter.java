package org.iviPro.export.smil.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.smil.VideoMovingInformation;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotationAction;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSceneSequence;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class SMILExporter {

	// Tags
	protected final static String TAG_ANIMATION = "animate";
	protected final static String TAG_AREA = "area";
	protected final static String TAG_AUDIO = "audio";
	protected final static String TAG_BODY = "body";
	protected final static String TAG_DATA = "data";
	protected final static String TAG_EXCLUSIVE = "excl";
	protected final static String TAG_HEAD = "head";
	protected final static String TAG_IMAGE = "img";
	protected final static String TAG_LAYOUT = "layout";
	protected final static String TAG_LINK = "a";
	protected final static String TAG_META = "meta";
	protected final static String TAG_NORMAL_TEXT = "text";
	protected final static String TAG_PAR = "par";
	protected final static String TAG_PRIORITYCLASS = "priorityClass";
	protected final static String TAG_REGION = "region";
	protected final static String TAG_ROOT_LAYOUT = "root-layout";
	protected final static String TAG_SEQ = "seq";
	protected final static String TAG_SET_VALUE = "setvalue";
	protected final static String TAG_SMIL = "smil";
	protected final static String TAG_STATE = "state";
	protected final static String TAG_SWITCH = "switch";
	protected final static String TAG_TEXT = "smilText";
	protected final static String TAG_VIDEO = "video";

	// Attributes
	protected final static String ATTR_ATTRIBUTENAME = "attributeName";
	protected final static String ATTR_BACKGROUND_COLOR = "backgroundColor";
	protected final static String ATTR_BACKGROUND_OPACITY = "backgroundOpacity";
	protected final static String ATTR_BEGIN = "begin";
	protected final static String ATTR_BOTTOM = "bottom";
	protected final static String ATTR_CALCMODE = "calcMode";
	protected final static String ATTR_CONTENT = "content";
	protected final static String ATTR_COORDINATES = "coords";
	protected final static String ATTR_DURATION = "dur";
	protected final static String ATTR_END = "end";
	protected final static String ATTR_EXPR = "expr";
	protected final static String ATTR_FILL = "fill";
	protected final static String ATTR_FIT = "fit";
	protected final static String ATTR_FROM = "from";
	protected final static String ATTR_HEIGHT = "height";
	protected final static String ATTR_H_REFERENCE = "href";
	protected final static String ATTR_ID = "xml:id";
	protected final static String ATTR_LEFT = "left";
	protected final static String ATTR_NAME = "name";
	protected final static String ATTR_PEERS = "peers";
	protected final static String ATTR_REGION = "region";
	protected final static String ATTR_RIGHT = "right";
	protected final static String ATTR_REF = "ref";
	protected final static String ATTR_RESTART = "restart";
	protected final static String ATTR_SHAPE = "shape";
	protected final static String ATTR_SOURCE = "src";
	protected final static String ATTR_SOURCE_PLAYSTATE = "sourcePlaystate";
	protected final static String ATTR_SYSTEMLANGUAGE = "systemLanguage";
	protected final static String ATTR_TARGETELEMENT = "targetElement";
	protected final static String ATTR_TITLE = "title";
	protected final static String ATTR_TOP = "top";
	protected final static String ATTR_TO = "to";
	protected final static String ATTR_VALUE = "value";
	protected final static String ATTR_VALUES = "values";
	protected final static String ATTR_WIDTH = "width";
	protected final static String ATTR_XML_NAMESPACE = "xmlns";

	// Values
	protected final static String VAL_ANIMATION_REGION_ID = "animation_region";
	protected final static String VAL_BUTTON_REGION_ID = "button_region";
	protected final static String VAL_BOT_REGION_ID = "bottom_region";
	protected final static String VAL_COLOR_SILVER = "silver";
	protected final static String VAL_DISCRETE = "discrete";
	protected final static String VAL_EIGHTY_PERCENT = "80%";
	protected final static String VAL_ENDNODE_ID = "NodeEnd_1";
	protected final static String VAL_HEIGHT = "height";
	protected final static String VAL_HUNDRED_PERCENT = "100%";
	protected final static String VAL_INDEFINITE = "indefinite";
	protected final static String VAL_LEFT = "left";
	protected final static String VAL_LEFT_REGION_ID = "left_region";
	protected final static String VAL_LEFT_QUIZ_OFFSET = "10";
	protected final static String VAL_MAIN_REGION_ID = "main_region";
	protected final static String VAL_MEET = "meet";
	protected final static String VAL_PAUSE = "pause";
	protected final static String VAL_REMOVE = "remove";
	protected final static String VAL_RIGHT_REGION_ID = "right_region";
	protected final static String VAL_ROOT_LAYOUT_ID = "root_layout";
	protected final static String VAL_SELECTION_OFFSET = "10";
	protected final static String VAL_SMIL_NAMESPACE = "http://www.w3.org/ns/SMIL";
	protected final static String VAL_TOC_BUTTON_BOTTOM = "10";
	protected final static String VAL_TOC_BUTTON_ID = "toc_button";
	protected final static String VAL_TOC_BUTTON_HEIGHT = "25";
	protected final static String VAL_TOC_BUTTON_RIGHT = "10";
	protected final static String VAL_TOC_BUTTON_WIDTH = "25";
	protected final static String VAL_TOC_HEIGHT = "20";
	protected final static String VAL_TOC_PIC_LENGTH = "32";
	protected final static String VAL_TOP = "top";
	protected final static String VAL_TOP_REGION_ID = "top_region";
	protected final static String VAL_TWENTY_PERCENT = "20%";
	protected final static String VAL_WHEN_NOT_ACTIVE = "whenNotActive";
	protected final static String VAL_WIDTH = "width";
	protected final static String VAL_ZERO = "0";
	protected final static String VAL_ZERO_PERCENT = "0%";

	// Value-Additions
	protected final static String ADDITION_END = "_end";
	protected final static String ADDITION_GET_POINTS = "_get_points";
	protected final static String ADDITION_MARKER = "_marker";
	protected final static String ADDITION_NO_POINTS = "_no_points";
	protected final static String ADDITION_POINTS = "_points";
	protected final static String ADDITION_SECONDS = "s";

	// Others
	protected final static String GOTO_CLICK_TEXT = "Please click here!";
	protected final static String ACTIVATE_ELEMENT_EVENT = ".activateEvent";
	protected final static String CLICK_EVENT = ".click";
	protected final static int MARK_SHAPE_BUTTON_SIZE = 75;
	protected final static String TOC_BUTTON_ADRESS = "../pix/toc_button.png";
	protected final static String GREATER_THEN_EQUALS = " >= ";
	protected final static String LESS_THEN_EQUALS = " <= ";

	private IAbstractBean exportObj = null;
	protected ExportParameters parameters;

	SMILExporter(IAbstractBean exportObj) {
		this.exportObj = exportObj;
		this.parameters = new ExportParameters();
	}

	abstract Class<? extends IAbstractBean> getExportObjectType();

	/**
	 * Start method to export the object of this class. If the object is not
	 * null and exportable by the SMIL-Exporter, the method of the actual
	 * model-type-exporter is called.
	 * 
	 * @param document
	 *            The document where the SMIL-file is to be built.
	 * @param idManager
	 *            Manages the ids all over the SMIL-file
	 * @param project
	 *            The project that is to be exported
	 * @param alreadyExported
	 *            The objects of the project that have already been exported.
	 * @throws ExportException
	 *             If the object of this class is null or nor exportable by the
	 *             SMIL-Exporter.
	 */
	public void exportObject(Document document, IDManager idManager,
			Project project, Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {
		Class<? extends IAbstractBean> relatedClass = getExportObjectType();

		if (exportObj == null) {
			throw new ExportException(
					"The exported Object in the SMIL-Exporter _"
							+ this.getClass().getSimpleName() + "_ was null!");
		} else if (!relatedClass.isAssignableFrom(exportObj.getClass())) {
			throw new ExportException("Given Object _"
					+ this.getClass().getSimpleName()
					+ "_ is not exportable by the SMIL-Exporter!");
		} else {
			if (!alreadyExported.contains(exportObj)) {
				alreadyExported.add(exportObj);
				exportObjectImpl(exportObj, document, idManager, project,
						alreadyExported, parent);
			} else {
				if (!(exportObj instanceof NodeEnd)) {
					createLink(document, idManager, parent);
				}
			}
		}

	}

	/**
	 * Abstract method that is called by subclasses of this class to do the
	 * export for the different model-types.
	 * 
	 * @param document
	 *            The document where the SMIL-file is to be built.
	 * @param idManager
	 *            Manages the ids all over the SMIL-file
	 * @param project
	 *            The project that is to be exported
	 * @param alreadyExported
	 *            The objects of the project that have already been exported.
	 * @throws ExportException
	 *             If the object of this class is null or nor exportable by the
	 *             SMIL-Exporter.
	 */
	protected abstract void exportObjectImpl(IAbstractBean exportObj,
			Document document, IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException;

	/**
	 * Method to retrieve the head-node of the given document.
	 * 
	 * @param doc
	 *            The document where the head-node is to be found.
	 * @return The head element.
	 * @throws ExportException
	 *             If the head-element cannot be found in the given document.
	 */
	protected Element getHead(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				TAG_HEAD);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			throw new ExportException(
					"Error: SMIL-body Element could not be found.");
		}
	}

	/**
	 * Method to retrieve the body node of the given document.
	 * 
	 * @param doc
	 *            The document where the body node is to be found.
	 * @return The body element.
	 * @throws ExportException
	 *             If the body element cannot be found in the given document.
	 */
	protected Element getBody(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				TAG_BODY);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			throw new ExportException(
					"Error: SMIL-body Element could not be found.");
		}
	}

	/**
	 * Method to retrieve the state-element, which can be found as a child of
	 * the head-element. If there is no state-element yet, return null.
	 * 
	 * @param doc
	 *            The document where the state-element is to be found.
	 * @return The element or null, if it is not present.
	 */
	protected Element getState(Document doc) {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				TAG_STATE);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			return null;
		}
	}

	/**
	 * Method to get the first seq-node of the body, which is contained in a
	 * parallel node there. This is the node, where all scenes have to be added.
	 * 
	 * @param doc
	 *            The SMIL-document for the export.
	 * @return The first seq-node of the body.
	 * @throws ExportException
	 *             If the first child of the body node is NOT a seq node or the
	 *             main sequential node is not a sequential one.
	 */
	protected Element getMainSeqElement(Document doc) throws ExportException {
		Element mainPar = (Element) getBody(doc).getFirstChild();

		if (mainPar.getTagName().equals(TAG_PAR)) {
			Element mainSeq = (Element) mainPar.getFirstChild();

			if (mainSeq.getTagName().equals(TAG_SEQ)) {
				return mainSeq;
			} else {
				throw new ExportException(
						"Error: First element of the body is NOT a sequential node.");
			}
		} else {
			throw new ExportException(
					"Error: First element in the body is NOT a parallel node.");
		}
	}

	/**
	 * Method to get the parallel element which represents the table of contents
	 * for the current project.
	 * 
	 * @param doc
	 *            The Document file where the SMIL-file is to be built.
	 * @return The element that represents the table of contents in the
	 *         SMIL-file.
	 * @throws ExportException
	 *             If the first element of the body is not a parallel one or the
	 *             element for the table of contents is not parallel.
	 */
	protected Element getTocElement(Document doc) throws ExportException {
		Element mainPar = (Element) getBody(doc).getFirstChild();

		if (mainPar.getTagName().equals(TAG_PAR)) {
			Element tocElement = (Element) mainPar.getLastChild();

			if (tocElement.getTagName().equals(TAG_PAR)) {
				return tocElement;
			} else {
				throw new ExportException(
						"Error: Last (second) element of the body is NOT a parallel node.");
			}
		} else {
			throw new ExportException(
					"Error: First element in the body is NOT a parallel node.");
		}
	}

	/**
	 * Method to search the scene elements (which are children of the main
	 * seq-element) for the one element with the given sceneID.
	 * 
	 * @param doc
	 *            The document which is to be searched for the element.
	 * @param sceneID
	 *            The sceneID to be found.
	 * @return The Element with the given sceneID.
	 * @throws ExportException
	 */
	protected Element getSceneElementByID(Document doc, String sceneID)
			throws ExportException {
		Element mainSeq = getMainSeqElement(doc);

		NodeList children = mainSeq.getChildNodes();

		if (children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				Element child = (Element) children.item(i);

				String childID = child.getAttribute(ATTR_ID);

				if (childID.equals(sceneID)) {
					return child;
				}
			}
		}

		// If there are no children or the desired node is not found, return
		// null
		return null;
	}

	/**
	 * Method to export all annotations of the NodeScene that is currently being
	 * exported.
	 * 
	 * @param scene
	 *            The scene that is currently being exported.
	 * @param doc
	 *            The SMIL-document for the entire export.
	 * @param idManager
	 *            The idManager for the export.
	 * @param project
	 *            The project to export.
	 * @param alreadyExported
	 *            The items that have already been exported.
	 * @throws ExportException
	 *             If one of the annotation children could not be exported
	 *             correctly.
	 */
	protected void exportAnnotations(NodeScene scene, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {
		Collection<IGraphNode> children = scene
				.getChildren(INodeAnnotationLeaf.class);

		for (IGraphNode annotationChild : children) {
			SMILExporter exporter = SMILExporterFactory
					.createSMILExporter(annotationChild);
			exporter.exportObject(doc, idManager, project, alreadyExported,
					parent);
		}

	}

	/**
	 * Method to export the markers of a given scene.
	 * 
	 * @param scene
	 *            The scene whose markers are to be exported.
	 * @param doc
	 *            The document for the current SMIL-Export.
	 * @param idManager
	 *            The IDManager of the current export.
	 * @param project
	 *            The project that is to be exported.
	 * @param alreadyExported
	 *            The nodes that have already been exported.
	 * @param parent
	 *            The parallel-element of the scene.
	 * @throws ExportException
	 */
	protected void exportMarkers(NodeScene scene, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {
		Collection<IGraphNode> children = scene.getChildren(NodeMark.class);

		for (IGraphNode markChild : children) {
			SMILExporter exporter = SMILExporterFactory
					.createSMILExporter(markChild);
			exporter.exportObject(doc, idManager, project, alreadyExported,
					parent);
		}
	}

	/**
	 * Method to export all successors of this currently exported scene.
	 * 
	 * @param scene
	 *            The scene that is currently exported.
	 * @param doc
	 *            The SMIL-document for the whole export.
	 * @param idManager
	 *            The SMILidManager for the export.
	 * @param project
	 *            The project that is exported.
	 * @param alreadyExported
	 *            The items that have already been exported.
	 * @throws ExportException
	 *             If one of the successors could not be exported correctly.
	 */
	protected void exportSuccessors(NodeScene scene, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {

		List<IGraphNode> children = new ArrayList<IGraphNode>();
		children.addAll(scene.getChildren(INodeAnnotationAction.class));
		children.addAll(scene.getChildren(NodeSceneSequence.class));
		children.addAll(scene.getChildren(NodeScene.class));

		for (IGraphNode child : children) {
			SMILExporter exporter = SMILExporterFactory
					.createSMILExporter(child);
			exporter.exportObject(doc, idManager, project, alreadyExported,
					null);

		}
	}

	/**
	 * Returns a list of export parameters for the currently used SMILExporter.
	 * 
	 * @return A list of export parameters.
	 */
	protected abstract ExportParameters getExportParameters();

	/**
	 * Method is called when there is an edge from a node to a node that has
	 * already been exported. In this case, the already exported node must not
	 * be exported again, but a link is created to link to it.
	 * 
	 * @param doc
	 *            The SMIL-document.
	 * @param idManager
	 *            The idManager for the export.
	 * @param parent
	 *            The parent for the created link.
	 * @throws ExportException
	 * @throws DOMException
	 */
	private void createLink(Document doc, IDManager idManager, Element parent)
			throws DOMException, ExportException {
		if ((exportObj instanceof NodeScene)
				|| (exportObj instanceof NodeSelection)
				|| (exportObj instanceof NodeQuiz)
				|| (exportObj instanceof NodeEnd)) {
			Element parallel = doc.createElement(TAG_PAR);
			parallel.setAttribute(ATTR_DURATION, VAL_INDEFINITE);
			Element link = doc.createElement(TAG_LINK);
			link.setAttribute(ATTR_H_REFERENCE,
					"#" + idManager.getID(exportObj));

			Element text = doc.createElement(TAG_TEXT);
			text.setTextContent(GOTO_CLICK_TEXT);
			text.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
			link.appendChild(text);
			parallel.appendChild(link);

			if (parent == null) {
				getMainSeqElement(doc).appendChild(parallel);
			} else {
				parent.appendChild(parallel);
			}
		}
	}

	/**
	 * Method is used to find the paths that are outgoing from this fork. These
	 * are needed in order to find the first node that is to be placed after the
	 * exclusive element of the selection.
	 * 
	 * @param node
	 *            The selection or quiz where the pathing is to be found.
	 * @return A list of lists, where every inner list represents one path of
	 *         the selection.
	 * @throws ExportException
	 */
	protected LinkedList<LinkedList<IGraphNode>> forkPaths(IGraphNode node,
			Set<IAbstractBean> alreadyExported) throws ExportException {
		if ((node instanceof NodeSelection) || node instanceof NodeQuiz) {

			LinkedList<LinkedList<IGraphNode>> paths = new LinkedList<LinkedList<IGraphNode>>();
			boolean intersectionFound = false;

			initializePaths(paths, node);

			// Fill the paths and check after every newly add node if all the
			// paths have an intersection
			IGraphNode intersection = null;

			while (!intersectionFound) {
				for (LinkedList<IGraphNode> list : paths) {
					IGraphNode lastOfPath = list.getLast();

					if (!alreadyExported.contains(lastOfPath)) {

						if (intersectionNode(paths, lastOfPath,
								alreadyExported, node)) {
							intersection = lastOfPath;
							intersectionFound = true;
							break;
						} else {
							if (!(lastOfPath instanceof NodeEnd)
									&& lastOfPath != node) {
								if (lastOfPath instanceof NodeScene) {
									// Just add the next node into the path
									IGraphNode child = onlyImportantChild((NodeScene) lastOfPath);
									if (child != list.getFirst()) {
										list.add(child);
									}
								} else if ((lastOfPath instanceof NodeSelection)
										|| (lastOfPath instanceof NodeQuiz)) {
									// Add the selection or quiz node together
									// with
									// its first node where the paths join
									IGraphNode pathJoiner = firstCommonPathNode(
											lastOfPath, alreadyExported);

									list.add(pathJoiner);

								} else {
									throw new ExportException(
											"Wrong child exported in selection: "
													+ lastOfPath.getTitle());
								}
							}
						}
					} else {

					}

				}
			}

			if (intersection != null) {
				clearPaths(paths, intersection, alreadyExported);

				// Append a list with only the intersection to the paths, so
				// that the intersection can be found afterwards
				LinkedList<IGraphNode> intersectionList = new LinkedList<IGraphNode>();
				intersectionList.add(intersection);
				paths.add(intersectionList);

				return paths;
			} else {
				throw new ExportException("No intersection of selection "
						+ node.getTitle() + " found!");
			}
		} else {
			throw new ExportException("Node " + node.getTitle()
					+ " is not a Selection or Quiz.");
		}

	}

	/**
	 * Method initializes the LinkedList that is built in order to get the paths
	 * of a selection. It therefore gets the first child of every
	 * NodeSelectionControl and adds it to a new LinkedList that is added to the
	 * followers.
	 * 
	 * @param followers
	 *            List of paths going out of the selection.
	 * @param selection
	 *            The selection that is to be exported.
	 * @throws ExportException
	 *             If one of the NodeSelectionControls wasn't built up
	 *             correctly, meaning that it has no legit follower.
	 */
	private void initializePaths(LinkedList<LinkedList<IGraphNode>> followers,
			IGraphNode node) throws ExportException {
		if (node instanceof NodeSelection) {
			List<IGraphNode> selectionControls = node
					.getChildren(NodeSelectionControl.class);
			for (IGraphNode control : selectionControls) {
				if (control.getChildren().size() == 0) {
					throw new ExportException("Fork control: "
							+ control.getTitle() + " has no successor.");
				}
				IGraphNode firstChild = control.getFirstChild();
				LinkedList<IGraphNode> forkPath = new LinkedList<IGraphNode>();
				forkPath.add(firstChild);
				followers.add(forkPath);
			}
		} else if (node instanceof NodeQuiz) {
			List<IGraphNode> quizControls = node
					.getChildren(NodeQuizControl.class);
			for (IGraphNode control : quizControls) {
				if (control.getChildren().size() == 0) {
					throw new ExportException("Quiz control: "
							+ control.getTitle() + " has no successor.");
				}
				IGraphNode firstChild = control.getFirstChild();
				LinkedList<IGraphNode> forkpath = new LinkedList<IGraphNode>();
				forkpath.add(firstChild);
				followers.add(forkpath);
			}
		} else {
			throw new ExportException("Node " + node.getTitle()
					+ " is not a selection nor quiz.");
		}
	}

	/**
	 * Gets the child of the given NodeScene, which has to be another NodeScene,
	 * a NodeSelection or NodeQuiz or a NodeEnd.
	 * 
	 * @param node
	 *            The node whose child is to be found.
	 * @return The child of the given scene.
	 * @throws ExportException
	 *             If the given NodeScene has more than one
	 *             non-annotation-child.
	 */
	protected IGraphNode onlyImportantChild(NodeScene node)
			throws ExportException {
		LinkedList<IGraphNode> followers = new LinkedList<IGraphNode>();

		followers.addAll(node.getChildren(NodeScene.class));
		followers.addAll(node.getChildren(NodeSelection.class));
		followers.addAll(node.getChildren(NodeQuiz.class));
		followers.addAll(node.getChildren(NodeEnd.class));

		IGraphNode child = null;

		if (followers.size() == 0 || followers.size() > 1) {
			throw new ExportException("Node " + node.getTitle()
					+ " has to many non-annotation-children.");
		} else {
			child = followers.get(0);
		}

		return child;
	}

	/**
	 * Method is used to find the first node that is contained in every path of
	 * the given NodeSelection or NodeQuiz.
	 * 
	 * @param node
	 *            The node whose common node is to be found.
	 * @return The first node that is contained in every path of the selection
	 *         or quiz.
	 * @throws ExportException
	 */
	protected IGraphNode firstCommonPathNode(IGraphNode node,
			Set<IAbstractBean> alreadyExported) throws ExportException {
		if (!((node instanceof NodeSelection) || (node instanceof NodeQuiz))) {
			throw new ExportException("Node " + node.getTitle()
					+ " is not a quiz or selection.");
		} else {
			IGraphNode commonNode = null;

			LinkedList<LinkedList<IGraphNode>> followers = forkPaths(node,
					alreadyExported);

			for (LinkedList<IGraphNode> path : followers) {
				IGraphNode lastNode = path.getLast();
				if (!alreadyExported.contains(lastNode)) {
					commonNode = lastNode;
				}
			}

			return commonNode;
		}
	}

	/**
	 * Method checks if the given node is contained in all the lists of the
	 * followers. If it is, the node is the intersection of the paths of the
	 * selection or quiz.
	 * 
	 * @param paths
	 *            The paths that have to be intersected.
	 * @param node
	 *            The node to be searched for.
	 * @param startNode
	 *            The selection or quiz.
	 * @return True, if the node is contained in every path.
	 */
	private boolean intersectionNode(LinkedList<LinkedList<IGraphNode>> paths,
			IGraphNode node, Set<IAbstractBean> alreadyExported,
			IGraphNode startNode) {
		boolean intersection = true;

		for (LinkedList<IGraphNode> path : paths) {
			if (!excludedPath(path, alreadyExported) && !path.contains(node)
					&& path.getLast() != startNode) {
				intersection = false;
				break;
			}
		}

		return intersection;
	}

	/**
	 * Checks if the given path has to be excluded from the intersection
	 * determination. A path is excluded, if he has a backward edge, so his last
	 * node has already been exported.
	 * 
	 * @param path
	 *            The path to be checked.
	 * @param alreadyExported
	 *            The nodes of the graph that have already been exported.
	 * @return True, if the last node of the path is already exported.
	 */
	private boolean excludedPath(LinkedList<IGraphNode> path,
			Set<IAbstractBean> alreadyExported) {
		return alreadyExported.contains(path.getLast());
	}

	/**
	 * The intersection node must not be the last node of every path, so the
	 * nodes after the intersection have to be cleared from it.
	 * 
	 * @param paths
	 *            The paths to be cleared.
	 * @param intersection
	 *            The intersection node, whose children have to be cleared from
	 *            the paths.
	 */
	private void clearPaths(LinkedList<LinkedList<IGraphNode>> paths,
			IGraphNode intersection, Set<IAbstractBean> alreadyExported) {
		for (LinkedList<IGraphNode> path : paths) {
			if (!alreadyExported.contains(path.getLast())) {
				int intersectionPosition = 0;

				for (int i = 0; i < path.size(); i++) {
					if (path.get(i) == intersection) {
						intersectionPosition = i;
						break;
					}
				}

				for (int i = path.size() - 1; i > intersectionPosition; i--) {
					path.remove(i);
				}
			}
		}
	}

	/**
	 * Method checks if there is a path with only one node, which also is the
	 * intersection. In this case, the child of the intersection and not the
	 * intersection itself has to be exported.
	 * 
	 * @param paths
	 *            The paths to check for the one-node-path.
	 * @param intersection
	 *            The intersection that has been found according to the given
	 *            paths.
	 * @return True, if there is a path with only one node that is also the
	 *         intersection.
	 */
	protected boolean oneNodePathIntersection(
			LinkedList<LinkedList<IGraphNode>> paths, IGraphNode intersection) {
		boolean oneNodePathIntersection = false;

		// Check every path but the last one, because the intersection has been
		// added to the paths list as a single node
		for (int i = 0; i < paths.size() - 1; i++) {
			LinkedList<IGraphNode> path = paths.get(i);
			if (path.size() == 1 && path.getFirst() == intersection) {
				oneNodePathIntersection = true;
				break;
			}
		}

		return oneNodePathIntersection;
	}

	/**
	 * Method tests if the given element has got an excl-child.
	 * 
	 * @param element
	 *            The element whose children are to be checked for an
	 *            excl-element.
	 * @return True, if there is an excl-child amongst the element's children.
	 */
	protected boolean containsExcl(Element element) {
		NodeList children = element.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Element child = (Element) children.item(i);

			if (child.getTagName().equals(TAG_EXCLUSIVE)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Method to append the annotations that pause the main-video accordingly.
	 * 
	 * @param parent
	 *            The par-element that contains the scene.
	 * @param doc
	 *            The document of the current SMIL-Export.
	 * @throws ExportException
	 */
	protected void handlePauseAnnotation(Element parent,
			Element annotationElement, Document doc) throws ExportException {
		// In order to pause the main video, there has to be added an excl and
		// priorityClass tag
		if (containsExcl(parent)) {
			// There has already been an annotation added, that pauses the
			// video, so the excl and priorityClass-tags don't have to be added
			// again
			Element priorityClass = (Element) parent.getElementsByTagName(
					TAG_EXCLUSIVE).item(0);
			priorityClass.appendChild(annotationElement);
		} else {
			// No annotation that pauses the main video has been added, so the
			// excl and priorityClass-tags have to be added
			Element priorityClass = doc.createElement(TAG_PRIORITYCLASS);
			priorityClass.setAttribute(ATTR_PEERS, VAL_PAUSE);

			// Change the parent of the switch that contains the scene-videos to
			// the priorityClass
			Element sceneSwitch = (Element) parent.getFirstChild();
			if (sceneSwitch.getTagName().equals(TAG_SWITCH)) {
				priorityClass.appendChild(sceneSwitch);
				priorityClass.appendChild(annotationElement);
			} else {
				throw new ExportException(
						"Fehler beim Export von einer Pause-Annotation.");
			}

			Element exclusive = doc.createElement(TAG_EXCLUSIVE);
			exclusive.appendChild(priorityClass);

			parent.appendChild(exclusive);
		}
	}

	/**
	 * Method transforms given amount of nanoseconds into following
	 * String-representation: "x.xxxxxxxxxs".
	 * 
	 * @param nanoseconds
	 *            The nanoseconds to calculate the String for.
	 * @return A String-representation for the given nanoseconds.
	 */
	protected String toNanoString(long nanoseconds) {
		String stringRepresentation = "";
		String onlyNanoseconds = Long.toString(nanoseconds);

		if (onlyNanoseconds.length() > 9) {
			String secondsPart = onlyNanoseconds.substring(0,
					onlyNanoseconds.length() - 9);
			String nanoPart = onlyNanoseconds.substring(
					onlyNanoseconds.length() - 9, onlyNanoseconds.length());

			stringRepresentation = secondsPart + "." + nanoPart
					+ ADDITION_SECONDS;
		} else {
			int zerosToAdd = 9 - onlyNanoseconds.length();
			for (int i = 0; i < zerosToAdd; i++) {
				onlyNanoseconds = "0" + onlyNanoseconds;
			}

			stringRepresentation = "0." + onlyNanoseconds + ADDITION_SECONDS;
		}

		return stringRepresentation;
	}

	/**
	 * Method is used in order to determine, if a video has to be moved because
	 * of the ratios of the video compared to the main frame. The SMIL-Player
	 * will be cropping the video according to its ratio so that it fits the
	 * region it is shown in. this is considered in order to move the video
	 * slightly down or to the right.
	 * 
	 * @param areaWidth
	 *            The width of the region where videos are shown.
	 * @param areaHeight
	 *            The height of the region where videos are shown.
	 * @param videoWidth
	 *            The width of the video.
	 * @param videoHeight
	 *            The height of the video.
	 * @return A map with only one element, if the video has to be moved, or an
	 *         empty map, if the video does not have to be moved.
	 */
	protected VideoMovingInformation calculateVideoMoving(int areaWidth,
			int areaHeight, int videoWidth, int videoHeight) {

		double areaRatio = areaWidth / areaHeight;
		double videoRatio = videoWidth / areaHeight;

		VideoMovingInformation info = null;

		if (videoRatio < areaRatio) {
			// Video is more narrow than the area, so there has to be an edge
			// left and right of it - the height of the video must be adapted
			// first
			double factor = (double) areaHeight / (double) videoHeight;

			double adaptedVideoWidth = factor * (double) videoWidth;

			double margin = (areaWidth - adaptedVideoWidth) / 2;

			info = new VideoMovingInformation(ATTR_LEFT, (int) margin);
		} else {
			// Area is more narrow than the video, so there has to be an edge
			// above and under the video - the width of the video must be
			// adapted first
			double factor = (double) areaWidth / (double) videoWidth;

			double adaptedVideoHeight = factor * videoHeight;

			double margin = (areaHeight - adaptedVideoHeight) / 2;

			info = new VideoMovingInformation(ATTR_TOP, (int) margin);
		}

		return info;
	}

	/**
	 * Method realizes the moving of a given video with the given moving in the
	 * map. This is done by setting the top or left attribute for the video.
	 * 
	 * @param video
	 *            The video-element that has to be moved.
	 * @param moving
	 *            The map that contains the information about the moving-action.
	 * @throws ExportException
	 */
	protected void realizeVideoMoving(Element video,
			VideoMovingInformation movingInfo) throws ExportException {
		if (video.getTagName().equals(TAG_VIDEO)) {
			// If the map is empty, no moving for the video has to be done
			if (movingInfo != null) {
				video.setAttribute(movingInfo.getAttribute(),
						Integer.toString(movingInfo.getMargin()));
			}
		} else {
			throw new ExportException(
					"Videomoving action not used on a video-element!");
		}
	}
}
