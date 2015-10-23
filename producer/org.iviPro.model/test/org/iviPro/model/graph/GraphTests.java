/**
 * 
 */
package org.iviPro.model.graph;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.eclipse.draw2d.geometry.Point;
import org.iviPro.model.BeanList;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author dellwo
 * 
 */
public class GraphTests {

	private static Project project;

	private static String SCENE_01 = "Scene-01"; //$NON-NLS-1$
	private static String SCENE_02 = "Scene-02"; //$NON-NLS-1$
	private static String SCENE_03 = "Scene-03"; //$NON-NLS-1$

	private static String SCENE_NODE_01A = "Scene-01a"; //$NON-NLS-1$
	private static String SCENE_NODE_02A = "Scene-02a"; //$NON-NLS-1$
	private static String SCENE_NODE_03A = "Scene-03a"; //$NON-NLS-1$
	private static String SCENE_NODE_01B = "Scene-01b"; //$NON-NLS-1$

	private static String SCENE_SEQUENCE_01 = "Scene-Sequence-01"; //$NON-NLS-1$
	private static String SCENE_SEQUENCE_02 = "Scene-Sequence-02"; //$NON-NLS-1$
	private static String SCENE_SEQUENCE_03 = "Scene-Sequence-03"; //$NON-NLS-1$

	private static String MARK_01 = "Mark-01"; //$NON-NLS-1$
	private static String MARK_02 = "Mark-02"; //$NON-NLS-1$
	private static String MARK_03 = "Mark-03"; //$NON-NLS-1$

	private static String ANNOTATION_01 = "Annotation-01"; //$NON-NLS-1$
	private static String ANNOTATION_02 = "Annotation-02"; //$NON-NLS-1$
	private static String ANNOTATION_03 = "Annotation-03"; //$NON-NLS-1$

	private static String SELECTION_01 = "Selection-01"; //$NON-NLS-1$
	private static String SELECTION_CONTROL_01a = "Selection-Control-01a"; //$NON-NLS-1$
	private static String SELECTION_CONTROL_01b = "Selection-Control-01b"; //$NON-NLS-1$

	private static String SELECTION_02 = "Selection-02"; //$NON-NLS-1$
	private static String SELECTION_CONTROL_02a = "Selection-Control-02a"; //$NON-NLS-1$
	private static String SELECTION_CONTROL_02b = "Selection-Control-02b"; //$NON-NLS-1$

	private static String SELECTION_03 = "Selection-03"; //$NON-NLS-1$
	private static String SELECTION_CONTROL_03a = "Selection-Control-03a"; //$NON-NLS-1$
	private static String SELECTION_CONTROL_03b = "Selection-Control-03b"; //$NON-NLS-1$

	private static int EXPECTED_NODE_COUNT = 2; // Start-/Endknoten
	private static int EXPECTED_NODEMARK_COUNT = 0;
	private static int EXPECTED_NODESCENE_COUNT = 0;
	private static int EXPECTED_NODESCENESEQUENCE_COUNT = 0;
	private static int EXPECTED_NODESELECTION_COUNT = 0;
	private static int EXPECTED_NODESELECTIONCONTROL_COUNT = 0;
	private static int EXPECTED_NODEANNOTATION_COUNT = 0;

	/**
	 * Wird vor allen Tests aufgerufen.
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		project = new Project(new LocalizedString(
				"Test project", Locale.getDefault()), new File("")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Wird nach allen Tests aufgerufen.
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testCreateVideo() {
		Video video = new Video(new File("Video-01.avi"), project); //$NON-NLS-1$
		project.getMediaObjects().add(video);
		Assert.assertEquals(1, project.getMediaObjects().size());
	}

	@Test
	public void testCreateScenes() {
		Video video = (Video) project.getMediaObjects().get(0);
		BeanList<Scene> scenes = video.getScenes();
		int expected = scenes.size();

		scenes.add(new Scene(SCENE_01, video, project));
		expected++;
		Assert.assertEquals(expected, scenes.size());

		scenes.add(new Scene(SCENE_02, video, project));
		expected++;
		Assert.assertEquals(expected, scenes.size());

		scenes.add(new Scene(SCENE_03, video, project));
		expected++;
		Assert.assertEquals(expected, scenes.size());

		scenes.add(new Scene(SCENE_01, video, project));
		expected++;
		Assert.assertEquals(expected, scenes.size());

	}

	@Test
	public void testAddNodes() {
		Graph graph = project.getSceneGraph();
		Scene scene1 = project.getScene(SCENE_01, project.getCurrentLanguage());
		Scene scene2 = project.getScene(SCENE_02, project.getCurrentLanguage());
		Scene scene3 = project.getScene(SCENE_03, project.getCurrentLanguage());

		// Fuege Szenen-Knoten hinzu
		graph.addNode(new NodeScene(SCENE_NODE_01A, new Point(), scene1,
				project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODESCENE_COUNT++;
		graph.addNode(new NodeScene(SCENE_NODE_02A, new Point(), scene2,
				project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODESCENE_COUNT++;
		graph.addNode(new NodeScene(SCENE_NODE_03A, new Point(), scene3,
				project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODESCENE_COUNT++;
		graph.addNode(new NodeScene(SCENE_NODE_01B, new Point(), scene1,
				project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODESCENE_COUNT++;

		// Fuege Selektions- und Selektions-Control-Knoten hinzu
		graph.addNode(new NodeSelection(SELECTION_01, project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODESELECTION_COUNT++;
		graph.addNode(new NodeSelectionControl(SELECTION_CONTROL_01a, project));
		EXPECTED_NODESELECTIONCONTROL_COUNT++;
		EXPECTED_NODE_COUNT++;
		graph.addNode(new NodeSelectionControl(SELECTION_CONTROL_01b, project));
		EXPECTED_NODESELECTIONCONTROL_COUNT++;
		EXPECTED_NODE_COUNT++;
		graph.addNode(new NodeSelection(SELECTION_02, project));
		EXPECTED_NODESELECTION_COUNT++;
		EXPECTED_NODE_COUNT++;
		graph.addNode(new NodeSelectionControl(SELECTION_CONTROL_02a, project));
		EXPECTED_NODESELECTIONCONTROL_COUNT++;
		EXPECTED_NODE_COUNT++;
		graph.addNode(new NodeSelectionControl(SELECTION_CONTROL_02b, project));
		EXPECTED_NODESELECTIONCONTROL_COUNT++;
		EXPECTED_NODE_COUNT++;
		graph.addNode(new NodeSelection(SELECTION_03, project));
		EXPECTED_NODESELECTION_COUNT++;
		EXPECTED_NODE_COUNT++;
		graph.addNode(new NodeSelectionControl(SELECTION_CONTROL_03a, project));
		EXPECTED_NODESELECTIONCONTROL_COUNT++;
		EXPECTED_NODE_COUNT++;
		graph.addNode(new NodeSelectionControl(SELECTION_CONTROL_03b, project));
		EXPECTED_NODESELECTIONCONTROL_COUNT++;
		EXPECTED_NODE_COUNT++;

		// Fuege Marker-Knoten hinzu
		graph.addNode(new NodeMark(new LocalizedString(MARK_01, project), project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODEMARK_COUNT++;
		graph.addNode(new NodeMark(new LocalizedString(MARK_02, project), project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODEMARK_COUNT++;
		graph.addNode(new NodeMark(new LocalizedString(MARK_03, project), project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODEMARK_COUNT++;

		// Fuege Annotations-Knoten hinzu
		graph.addNode(new NodeAnnotationText(
				new LocalizedString(ANNOTATION_01, project), project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODEANNOTATION_COUNT++;
		graph.addNode(new NodeAnnotationText(
				new LocalizedString(ANNOTATION_02, project), project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODEANNOTATION_COUNT++;
		graph.addNode(new NodeAnnotationText(
				new LocalizedString(ANNOTATION_03, project), project));
		EXPECTED_NODE_COUNT++;
		EXPECTED_NODEANNOTATION_COUNT++;

		// Am Schluss muss die erwartete Anzahl an Knoten im Graphen stimmen
		Assert.assertEquals(EXPECTED_NODESELECTION_COUNT, graph.searchNodes(
				NodeSelection.class).size());
		Assert.assertEquals(EXPECTED_NODESELECTIONCONTROL_COUNT, graph
				.searchNodes(NodeSelectionControl.class).size());
		Assert.assertEquals(EXPECTED_NODESCENE_COUNT, graph.searchNodes(
				NodeScene.class).size());
		Assert.assertEquals(EXPECTED_NODEMARK_COUNT, graph.searchNodes(
				NodeMark.class).size());
		Assert.assertEquals(EXPECTED_NODEANNOTATION_COUNT, graph
				.searchNodes(INodeAnnotationLeaf.class).size());
		Assert.assertEquals(EXPECTED_NODE_COUNT, graph.getNodes().size());

	}

	@Test
	public void testSearchSceneNodes() {
		Graph graph = project.getSceneGraph();
		NodeScene sceneNode;
		// Teste ob Gesamtzahl an Knoten erwarteter Anzahl entspricht.
		Assert.assertEquals(EXPECTED_NODE_COUNT, graph.getNodes().size());
		// Teste ob Gesamtzahl an Szenen-Knoten erwarteter Anzahl entspricht
		Assert.assertEquals(EXPECTED_NODESCENE_COUNT, graph.searchNodes(
				NodeScene.class).size());
		// Suche Szenen-Knoten 1a
		sceneNode = searchSceneNode(SCENE_NODE_01A);
		Assert.assertNotNull(sceneNode);
		Assert.assertEquals(sceneNode.getTitle(), SCENE_NODE_01A);
		// Suche Szenen-Knoten 2a
		sceneNode = searchSceneNode(SCENE_NODE_02A);
		Assert.assertNotNull(sceneNode);
		Assert.assertEquals(sceneNode.getTitle(), SCENE_NODE_02A);
		// Suche Szenen-Knoten 3a
		sceneNode = searchSceneNode(SCENE_NODE_03A);
		Assert.assertNotNull(sceneNode);
		Assert.assertEquals(sceneNode.getTitle(), SCENE_NODE_03A);
		// Suche Szenen-Knoten 1b
		sceneNode = searchSceneNode(SCENE_NODE_01B);
		Assert.assertNotNull(sceneNode);
		Assert.assertEquals(sceneNode.getTitle(), SCENE_NODE_01B);

	}

	@Test
	public void testAddEdgeWithNonexistingNode() {
		Graph graph = project.getSceneGraph();
		int numEdges = graph.getConnections().size();
		Video video = (Video) project.getMediaObjects().get(0);
		Scene scene = video.getScenes().get(0);
		IGraphNode existingNode = graph.getNodes().get(0);
		NodeScene nonExistingNode = new NodeScene(new LocalizedString(
				"Scene-NonExisting", project), new Point(), scene, project); //$NON-NLS-1$
		Assert.assertTrue(addEdge(graph, existingNode, nonExistingNode, true) //
				instanceof IllegalArgumentException);
		Assert.assertTrue(addEdge(graph, nonExistingNode, existingNode, true) //
				instanceof IllegalArgumentException);
		Assert.assertEquals(numEdges, graph.getConnections().size());
	}

	@Test
	public void testConnectionsWithNodeStart() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Zu testender Knoten
		NodeStart testNode = nodeStart;

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

		// Falsche Source-Knoten-Typen bei eingehenden Verbindungen
		Assert.assertNotNull(addEdge(graph, nodeStart, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeEnd, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeScene1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelection1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelCtrl1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeMark1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeAnnoLeaf1, testNode, true));

		// Falsche Ziel-Typen fuer ausgehende Verbindungen
		Assert.assertNotNull(addEdge(graph, testNode, nodeStart, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelection2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelCtrl2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeMark2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeAnnoLeaf2, true));

		// Erlaubte eingehende Verbindungen
		// -> Keine eingehenden Verbindungen erlaubt

		// Erlaubte ausgehende Verbindungen
		Assert.assertNull(addEdge(graph, testNode, nodeScene2, true));

		// Maximale Anzahl erlaubter ausgehender Verbindungen verletzt
		Assert.assertNull(addEdge(graph, testNode, nodeScene1, false));
		Assert.assertNotNull(addEdge(graph, testNode, nodeScene2, false));

	}

	@Test
	public void testConnectionsWithNodeEnd() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Zu testender Knoten
		NodeEnd testNode = nodeEnd;

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

		// Falsche Source-Knoten-Typen bei eingehenden Verbindungen
		Assert.assertNotNull(addEdge(graph, nodeStart, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeEnd, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelection1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelCtrl1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeMark1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeAnnoLeaf1, testNode, true));

		// Falsche Ziel-Typen fuer ausgehende Verbindungen
		Assert.assertNotNull(addEdge(graph, testNode, nodeStart, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelection2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelCtrl2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeMark2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeScene2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeAnnoLeaf2, true));

		// Erlaubte eingehende Verbindungen
		Assert.assertNull(addEdge(graph, nodeScene1, testNode, true));

		// Erlaubte ausgehende Verbindungen
		// -> Keine ausgehenden Verbindungen erlaubt

		// Maximale Anzahl erlaubter ausgehender Verbindungen verletzt
		// -> Keine ausgehenden Verbindungen erlaubt
	}

	@Test
	public void testConnectionsWithNodeScene() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Zu testender Knoten
		NodeScene testNode = searchSceneNode(SCENE_NODE_03A);

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

		// Falsche Source-Knoten-Typen bei eingehenden Verbindungen
		Assert.assertNotNull(addEdge(graph, nodeEnd, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelection1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeAnnoLeaf1, testNode, true));

		// Falsche Ziel-Typen fuer ausgehende Verbindungen
		Assert.assertNotNull(addEdge(graph, testNode, nodeStart, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelCtrl2, true));

		// Erlaubte eingehende Verbindungen
		Assert.assertNull(addEdge(graph, nodeScene1, testNode, true));
		Assert.assertNull(addEdge(graph, nodeSelCtrl1, testNode, true));
		Assert.assertNull(addEdge(graph, nodeStart, testNode, true));
		Assert.assertNull(addEdge(graph, nodeMark1, testNode, true));

		// Erlaubte ausgehende Verbindungen
		Assert.assertNull(addEdge(graph, testNode, nodeScene2, true));
		Assert.assertNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNull(addEdge(graph, testNode, nodeSelection2, true));
		Assert.assertNull(addEdge(graph, testNode, nodeMark2, true));
		Assert.assertNull(addEdge(graph, testNode, nodeAnnoLeaf2, true));

		// Maximale Anzahl erlaubter Verbindungen verletzt
		Assert.assertNull(addEdge(graph, testNode, nodeScene2, false));
		Assert.assertNotNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeScene2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelection2, true));
		// Unrestriktiert
		Assert.assertNull(addEdge(graph, testNode, nodeMark1, false));
		Assert.assertNull(addEdge(graph, testNode, nodeMark2, false));
		Assert.assertNull(addEdge(graph, testNode, nodeAnnoLeaf1, false));
		Assert.assertNull(addEdge(graph, testNode, nodeAnnoLeaf2, false));
	}

	@Test
	public void testConnectionsWithNodeSceneSequence() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

	}

	@Test
	public void testConnectionsWithNodeSelection() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Zu testender Knoten
		NodeSelection testNode = searchSelectionNode(SELECTION_03);

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

		// Falsche Source-Knoten-Typen bei eingehenden Verbindungen
		Assert.assertNotNull(addEdge(graph, nodeStart, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeEnd, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelection1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeMark1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeAnnoLeaf1, testNode, true));

		// Falsche Ziel-Typen fuer ausgehende Verbindungen
		Assert.assertNotNull(addEdge(graph, testNode, nodeStart, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelection2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeMark2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeAnnoLeaf2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeScene2, true));

		// Erlaubte eingehende Verbindungen
		Assert.assertNull(addEdge(graph, nodeScene1, testNode, true));
		Assert.assertNull(addEdge(graph, nodeSelCtrl1, testNode, true));

		// Erlaubte ausgehende Verbindungen
		Assert.assertNull(addEdge(graph, testNode, nodeSelCtrl2, true));

		// Maximale Anzahl erlaubter ausgehender Verbindungen testen
		Assert.assertNull(addEdge(graph, testNode, nodeSelCtrl2, false));
		Assert.assertNull(addEdge(graph, testNode, nodeSelCtrl1, false));
		Assert.assertNull(addEdge(graph, testNode,
				searchSelectionControlNode(SELECTION_CONTROL_03a), false));

	}

	@Test
	public void testConnectionsWithNodeSelectionControl() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Zu testender Knoten
		NodeSelectionControl testNode = searchSelectionControlNode(SELECTION_CONTROL_03a);

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

		// Falsche Source-Knoten-Typen bei eingehenden Verbindungen
		Assert.assertNotNull(addEdge(graph, nodeStart, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeEnd, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeMark1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeAnnoLeaf1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeScene1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelCtrl1, testNode, true));

		// Falsche Ziel-Typen fuer ausgehende Verbindungen
		Assert.assertNotNull(addEdge(graph, testNode, nodeStart, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeMark2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeAnnoLeaf2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelCtrl2, true));

		// Erlaubte eingehende Verbindungen
		Assert.assertNull(addEdge(graph, nodeSelection1, testNode, true));

		// Erlaubte ausgehende Verbindungen
		Assert.assertNull(addEdge(graph, testNode, nodeScene2, true));
		Assert.assertNull(addEdge(graph, testNode, nodeSelection2, true));

		// Maximale Anzahl erlaubter ausgehender Verbindungen testen
		Assert.assertNull(addEdge(graph, testNode, nodeScene2, false));
		Assert.assertNotNull(addEdge(graph, testNode, nodeScene1, false));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelection1, false));

	}

	@Test
	public void testConnectionsWithNodeMark() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Zu testender Knoten
		NodeMark testNode = searchMarkNode(MARK_03);

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

		// Falsche Source-Knoten-Typen bei eingehenden Verbindungen
		Assert.assertNotNull(addEdge(graph, nodeStart, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeEnd, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeMark1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeAnnoLeaf1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelCtrl1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelection1, testNode, true));

		// Falsche Ziel-Typen fuer ausgehende Verbindungen
		Assert.assertNotNull(addEdge(graph, testNode, nodeStart, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeMark2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeAnnoLeaf2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelCtrl2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelection2, true));

		// Erlaubte eingehende Verbindungen
		Assert.assertNull(addEdge(graph, nodeScene1, testNode, true));

		// Erlaubte ausgehende Verbindungen
		Assert.assertNull(addEdge(graph, testNode, nodeScene2, true));

		// Maximale Anzahl erlaubter ausgehender Verbindungen testen
		Assert.assertNull(addEdge(graph, testNode, nodeScene1, false));
		Assert.assertNotNull(addEdge(graph, testNode, nodeScene2, false));

	}

	@Test
	public void testConnectionsWithNodeAnnotation() {
		Graph graph = project.getSceneGraph();

		// Knoten die im Test verwendet werden
		NodeStart nodeStart = graph.getStart();
		NodeEnd nodeEnd = graph.getEnd();
		NodeScene nodeScene1 = searchSceneNode(SCENE_NODE_01A);
		NodeScene nodeScene2 = searchSceneNode(SCENE_NODE_02A);
		NodeSelection nodeSelection1 = searchSelectionNode(SELECTION_01);
		NodeSelection nodeSelection2 = searchSelectionNode(SELECTION_02);
		NodeSelectionControl nodeSelCtrl1 = searchSelectionControlNode(SELECTION_CONTROL_01a);
		NodeSelectionControl nodeSelCtrl2 = searchSelectionControlNode(SELECTION_CONTROL_02b);
		NodeMark nodeMark1 = searchMarkNode(MARK_01);
		NodeMark nodeMark2 = searchMarkNode(MARK_02);
		INodeAnnotationLeaf nodeAnnoLeaf1 = searchNodeAnnotationLeaf(ANNOTATION_01);
		INodeAnnotationLeaf nodeAnnoLeaf2 = searchNodeAnnotationLeaf(ANNOTATION_02);

		// Zu testender Knoten
		INodeAnnotationLeaf testNode = searchNodeAnnotationLeaf(ANNOTATION_03);

		// Am Anfang darf es keine Verbindungen geben
		removeAllEdges(graph);
		Assert.assertEquals(0, graph.getConnections().size());

		// Falsche Source-Knoten-Typen bei eingehenden Verbindungen
		Assert.assertNotNull(addEdge(graph, nodeStart, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeEnd, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeMark1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeAnnoLeaf1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelCtrl1, testNode, true));
		Assert.assertNotNull(addEdge(graph, nodeSelection1, testNode, true));

		// Falsche Ziel-Typen fuer ausgehende Verbindungen
		Assert.assertNotNull(addEdge(graph, testNode, nodeStart, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeEnd, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeMark2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeAnnoLeaf2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelCtrl2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeSelection2, true));
		Assert.assertNotNull(addEdge(graph, testNode, nodeScene2, true));

		// Erlaubte eingehende Verbindungen
		Assert.assertNull(addEdge(graph, nodeScene1, testNode, true));

		// Erlaubte ausgehende Verbindungen
		// -> Keine ausgehenden Verbindungen erlaubt

	}

	@Test
	public void test() {
		Graph graph = project.getSceneGraph();
		NodeStart startNode = graph.getStart();
		NodeEnd endNode = graph.getEnd();
		NodeScene sceneNode1a = searchSceneNode(SCENE_NODE_01A);
		NodeScene sceneNode2a = searchSceneNode(SCENE_NODE_02A);

		// Test: End -> Start
		Assert.assertFalse("Start-Knoten darf keine eingehenden " //$NON-NLS-1$
				+ "Verbindungen akzeptieren!", startNode //$NON-NLS-1$
				.canCompleteIncomingConnection(endNode));
		Assert.assertFalse("End-Knoten darf keine ausgehenden " //$NON-NLS-1$
				+ "Verbindungen akzeptieren!", endNode //$NON-NLS-1$
				.canCompleteOutgoingConnection(startNode));

		// Test: Start -> SceneNode1
		Assert.assertTrue("Start-Knoten muss eine Verbindung zu " //$NON-NLS-1$
				+ "NodeScene akzeptieren!", startNode //$NON-NLS-1$
				.canCompleteOutgoingConnection(sceneNode1a));
		graph.addConnection(new DefaultConnection(startNode, sceneNode1a,
				project));

		// Test: Start -> SceneNode2
		Assert.assertFalse("Start-Knoten darf nur eine Verbindung zu " //$NON-NLS-1$
				+ "NodeScene akzeptieren!", startNode //$NON-NLS-1$
				.canCompleteOutgoingConnection(sceneNode2a));

		// SceneNode1 -> SceneNode2
		Assert.assertTrue("Szenen-Knoten muss eine Verbindung zu " //$NON-NLS-1$
				+ "NodeScene akzeptieren!", sceneNode1a //$NON-NLS-1$
				.canCompleteOutgoingConnection(sceneNode2a));
		Assert.assertTrue("Szenen-Knoten muss eine Verbindung von " //$NON-NLS-1$
				+ "NodeScene akzeptieren!", sceneNode2a //$NON-NLS-1$
				.canCompleteIncomingConnection(sceneNode1a));
		graph.addConnection(new DefaultConnection(sceneNode1a, sceneNode2a,
				project));

		// SceneNode1 -> End-Node
		Assert.assertFalse("Szenen-Knoten darf nur eine ausgehende Verbindung " //$NON-NLS-1$
				+ "akzeptieren!", sceneNode1a //$NON-NLS-1$
				.canCompleteOutgoingConnection(endNode));
		Assert.assertFalse(
				"End-Knoten darf keine Verbindung von Szenen-Knoten akzeptieren," //$NON-NLS-1$
						+ "der keine ausgehenden Verbindungen mehr zulaesst.", //$NON-NLS-1$
				endNode.canCompleteIncomingConnection(sceneNode1a));

		// SceneNode2 -> End-Node
		Assert.assertTrue("Szenen-Knoten muss eine ausgehende Verbindung " //$NON-NLS-1$
				+ "zu Node-End akzeptieren!", sceneNode2a //$NON-NLS-1$
				.canCompleteOutgoingConnection(endNode));
		Assert.assertTrue(
				"End-Knoten muss eine Verbindung von Szenen-Knoten akzeptieren," //$NON-NLS-1$
						+ "der noch ausgehende Verbindungen zulaesst.", endNode //$NON-NLS-1$
						.canCompleteIncomingConnection(sceneNode2a));
		graph
				.addConnection(new DefaultConnection(sceneNode2a, endNode,
						project));
	}

	/**
	 * Wird vor jedem Test aufgerufen
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Wird nach jedem Test aufgerufen.
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Hilfs-Methode: Sucht Szenen-Knoten mit bestimmten Titel
	 * 
	 * @param title
	 *            Der Titel des gesuchten Knotens.
	 * @return Gefundener Szenen-Knoten oder null, wenn es keinen Szenen-Knoten
	 *         mit diesem Titel gibt.
	 */
	private NodeScene searchSceneNode(String title) {
		Graph graph = project.getSceneGraph();
		for (IGraphNode node : graph.searchNodes(NodeScene.class)) {
			if (node instanceof NodeScene) {
				if (node.getTitle().equals(title)) {
					return (NodeScene) node;
				}
			}
		}
		return null;
	}

	/**
	 * Hilfs-Methode: Sucht Selektions-Knoten mit bestimmten Titel
	 * 
	 * @param title
	 *            Der Titel des gesuchten Knotens.
	 * @return Gefundener Selektions-Knoten oder null, wenn es keinen
	 *         Selektions-Knoten mit diesem Titel gibt.
	 */
	private NodeSelection searchSelectionNode(String title) {
		Graph graph = project.getSceneGraph();
		for (IGraphNode node : graph.searchNodes(NodeSelection.class)) {
			if (node instanceof NodeSelection) {
				if (node.getTitle().equals(title)) {
					return (NodeSelection) node;
				}
			}
		}
		return null;
	}

	/**
	 * Hilfs-Methode: Sucht Selektions-Kontrol-Knoten mit bestimmten Titel
	 * 
	 * @param title
	 *            Der Titel des gesuchten Knotens.
	 * @return Gefundener Selektions-Kontrol-Knoten oder null, wenn es keinen
	 *         Selektions-Kontrol-Knoten mit diesem Titel gibt.
	 */
	private NodeSelectionControl searchSelectionControlNode(String title) {
		Graph graph = project.getSceneGraph();
		for (IGraphNode node : graph.searchNodes(NodeSelectionControl.class)) {
			if (node instanceof NodeSelectionControl) {
				if (node.getTitle().equals(title)) {
					return (NodeSelectionControl) node;
				}
			}
		}
		return null;
	}

	/**
	 * Hilfs-Methode: Sucht Marker-Knoten mit bestimmten Titel
	 * 
	 * @param title
	 *            Der Titel des gesuchten Knotens.
	 * @return Gefundener Marker-Knoten oder null, wenn es keinen Marker-Knoten
	 *         mit diesem Titel gibt.
	 */
	private NodeMark searchMarkNode(String title) {
		Graph graph = project.getSceneGraph();
		for (IGraphNode node : graph.searchNodes(NodeMark.class)) {
			if (node instanceof NodeMark) {
				if (node.getTitle().equals(title)) {
					return (NodeMark) node;
				}
			}
		}
		return null;
	}

	/**
	 * Hilfs-Methode: Sucht Annotations-Knoten mit bestimmten Titel
	 * 
	 * @param title
	 *            Der Titel des gesuchten Knotens.
	 * @return Gefundener Annotations-Knoten oder null, wenn es keinen
	 *         Annotations-Knoten mit diesem Titel gibt.
	 */
	private INodeAnnotationLeaf searchNodeAnnotationLeaf(String title) {
		Graph graph = project.getSceneGraph();
		for (IGraphNode node : graph.searchNodes(INodeAnnotationLeaf.class)) {
			if (node instanceof INodeAnnotationLeaf) {
				if (node.getTitle().equals(title)) {
					return (INodeAnnotationLeaf) node;
				}
			}
		}
		return null;
	}

	/**
	 * Fuegt eine Kante zwischen zwei Knoten in einen Graphen ein.
	 * 
	 * @param graph
	 *            Der Graph.
	 * @param source
	 *            Der ausgehende Knoten.
	 * @param target
	 *            Der eingehende Knoten.
	 * @param removeAfter
	 *            Wenn true, dann wird die Kante sofort wieder geloescht,
	 *            nachdem sie erfolgreich erstellt wurde.
	 * @return Die Exception falls die Kante nicht erstellt werden konnte oder
	 *         null, wenn die Kante erfolgreich erstellt wurde
	 */
	private Exception addEdge(Graph graph, IGraphNode source,
			IGraphNode target, boolean removeAfter) {
		try {
			int sizeBefore = graph.getConnections().size();
			IConnection newEdge = new DefaultConnection(source, target, project);
			graph.addConnection(newEdge);
			Assert.assertEquals(sizeBefore + 1, graph.getConnections().size());
			if (removeAfter) {
				IConnection edge = graph.getConnection(source, target);
				Assert.assertEquals(edge, newEdge);
				removeEdge(graph, source, target);
				Assert.assertEquals(sizeBefore, graph.getConnections().size());
			}
			return null;
		} catch (IllegalArgumentException e) {
			return e;
		} catch (IllegalStateException e) {
			return e;
		}
	}

	/**
	 * Entfernt eine Kante zwischen zwei Knoten in einem Graphen.
	 * 
	 * @param graph
	 *            Der Graph.
	 * @param source
	 *            Der ausgehende Knoten.
	 * @param target
	 *            Der eingehende Knoten.
	 * @return Die Exception falls die Kante nicht entfernt werden konnte oder
	 *         null, wenn die Kante erfolgreich entfernt wurde.
	 */
	private Exception removeEdge(Graph graph, IGraphNode source,
			IGraphNode target) {
		try {
			int sizeBefore = graph.getConnections().size();
			graph.removeConnection(new DefaultConnection(source, target,
					project));
			Assert.assertEquals(sizeBefore - 1, graph.getConnections().size());
			return null;
		} catch (IllegalArgumentException e) {
			return e;
		} catch (IllegalStateException e) {
			return e;
		}
	}

	/**
	 * Entfernt alle Kanten aus dem Graphen.
	 * 
	 * @param graph
	 *            Der Graph
	 */
	private void removeAllEdges(Graph graph) {
		List<IConnection> edges = graph.getConnections();
		int numEdges = edges.size();
		for (IConnection edge : edges) {
			graph.removeConnection(edge);
			numEdges--;
			Assert.assertEquals(numEdges, graph.getConnections().size());
		}
		Assert.assertEquals(0, graph.getConnections().size());
	}

}
