/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.graph.NodeEnd"
 */
public class Graph extends IAbstractBean {

	public static final String PROP_NODE_ADDED = "PROP_NODE_ADDED"; //$NON-NLS-1$
	public static final String PROP_NODE_REMOVED = "PROP_NODE_REMOVED"; //$NON-NLS-1$
	public static final String PROP_CONNECTION_ADDED = "PROP_CONNECTION_ADDED"; //$NON-NLS-1$
	public static final String PROP_CONNECTION_REMOVED = "PROP_CONNECTION_REMOVED"; //$NON-NLS-1$

	/**
	 * @uml.property name="start"
	 */
	private NodeStart start;

	/**
	 * @uml.property name="nodes"
	 */
	private List<IGraphNode> nodes;

	/**
	 * Set of connections in the graph.
	 * @uml.property name="connections"
	 */
	private Set<IConnection> connections;

	/**
	 * @uml.property name="end"
	 */
	private NodeEnd end;

	public Graph(LocalizedString title, Project project) {
		super(title, project);
		connections = new HashSet<IConnection>();
		nodes = new ArrayList<IGraphNode>();
		start = new NodeStart(project);
		addNode(start);
		end = new NodeEnd(project); //$NON-NLS-1$
		addNode(end);
	}

	/**
	 * Fuegt einen Knoten in den Graphen ein. Wenn der Knoten noch nicht in
	 * diesem Graphen existierte, wird ein PROP_NODE_ADDED PropertyChangeEvent
	 * geworfen.
	 * 
	 * @param node
	 *            Der einzufuegende Knoten.
	 */
	public void addNode(IGraphNode node) {
		if (!nodes.contains(node) && nodes.add(node)) {
			node.setGraph(this);
			firePropertyChange(PROP_NODE_ADDED, null, node);
		}
	}

	/**
	 * Entfernt einen Knoten aus dem Graphen. Wenn der Knoten im Graphen
	 * existierte und entfernt wurde, wird ein PROP_NODE_REMOVED
	 * PropertyChangeEvent geworfen. Außerdem werden alle Kanten dieses Knotens
	 * gelöscht, eingehende wie ausgehende.<br>
	 * TODO: Hier sollten eigentlich auch bestimmte Kinder wie z.B. Annotationen
	 * geloescht werden. Eventuell abhaengig vom Typ der Kante.
	 * 
	 * @param node
	 *            Der zu entfernende Knoten.
	 * @return True, wenn Knoten entfernt wurde. False sonst, z.b. wenn dieser
	 *         Knoten ueberhaupt nicht in dem Graph existierte.
	 */
	public boolean removeNode(IGraphNode node) {
		boolean wasRemoved = nodes.remove(node);
		if (wasRemoved) {
			// Alle Kanten dieses Knotens entfernen
			List<IConnection> connsToDelete = new ArrayList<IConnection>();
			connsToDelete.addAll(getConnectionsBySource(node));
			connsToDelete.addAll(getConnectionsByTarget(node));
			for (IConnection connToDelete : connsToDelete) {
				removeConnection(connToDelete);
			}
			node.setGraph(null);
			firePropertyChange(PROP_NODE_REMOVED, node, null);
		}
		return wasRemoved;
	}

	/**
	 * Fuegt eine Verbindung zwischen zwei Knoten im Graphen ein. Wenn eine
	 * solche Verbindung noch nicht existiert, dann wird sie eingefuegt und ein
	 * PROP_CONNECTION_ADDED Event geworfen.
	 * 
	 * @param connection
	 *            Die hinzuzufuegende Verbindung zwischen zwei Knoten.
	 * @throws IllegalArgumentException
	 *             Sowohl der Quell- als auch der Ziel-Knoten der Verbindnug
	 *             muss bereits im Graphen existieren, sonst wird eine
	 *             IllegalArgumentException geworfen.
	 */
	public void addConnection(IConnection connection)
			throws IllegalArgumentException {
		IGraphNode source = connection.getSource();
		IGraphNode target = connection.getTarget();
		if (nodes.contains(source) && nodes.contains(target)) {
			if (!connections.contains(connection)) {

				if (source.canCompleteOutgoingConnection(target)
						&& target.canCompleteIncomingConnection(source)) {
					// Beide Verbindungs-Policies erlauben es die Kante zu
					// erstellen
					connections.add(connection);
					firePropertyChange(PROP_CONNECTION_ADDED, null, connection);
				} else {
					throw new IllegalStateException("The source and/or target " //$NON-NLS-1$
							+ "nodes are refusing the connection."); //$NON-NLS-1$
				}

			} else {
				throw new IllegalArgumentException(
						"This connection already exists " //$NON-NLS-1$
								+ " in this graph."); //$NON-NLS-1$
			}
		} else {
			throw new IllegalArgumentException("At least one of the nodes " //$NON-NLS-1$
					+ "of this connection is not a member of this graph!"); //$NON-NLS-1$
		}
	}

	/**
	 * Entfernt eine Kante im Graphen. Wenn die Kante existiert und entfernt
	 * wurde, wird ein PROP_CONNECTION_REMOVED PropertyChangeEvent geworfen.
	 * 
	 * @param connection
	 *            Die zu entfernende Kante.
	 */
	public void removeConnection(IConnection connection) {
		if (connections.remove(connection)) {
			firePropertyChange(PROP_CONNECTION_REMOVED, connection, null);
		}
	}

	/**
	 * Loescht alle Kanten und Knoten im Graphen
	 */
	public void clear() {
		connections.clear();
		nodes.clear();
		addNode(start);
		addNode(end);
	}

	public List<IConnection> getConnections() {
		return new ArrayList<IConnection>(connections);
	}

	/**
	 * Gibt alle Knoten zurueck, die Bestandteil dieses Graphen sind.
	 * 
	 * @return Alle Knoten dieses Graphen.
	 */
	public List<IGraphNode> getNodes() {
		return nodes;
	}

	/**
	 * Gibt eine Liste von den Verbindungen im Graphen zurueck, deren
	 * Quellknoten der angegeben Knoten ist.
	 * 
	 * @param node
	 *            Der gewuenschte Quellknoten der Verbindungen.
	 * @return Menge der Verbindungen mit dem gegebenen Quellknoten.
	 */
	public List<IConnection> getConnectionsBySource(IGraphNode node) {
		List<IConnection> result = new ArrayList<IConnection>();
		for (IConnection conn : connections) {
			if (conn.getSource().equals(node)) {
				result.add(conn);
			}
		}
		return result;
	}

	/**
	 * Gibt eine Liste von den Verbindungen im Graphen zurueck, deren Zielknoten
	 * der angegeben Knoten ist.
	 * 
	 * @param node
	 *            Der gewuenschte Zielknoten der Kanten.
	 * @return Menge der Verbindungen mit dem gegebenen Zielknoten.
	 */
	public List<IConnection> getConnectionsByTarget(IGraphNode node) {
		List<IConnection> result = new ArrayList<IConnection>();
		for (IConnection conn : connections) {
			if (conn.getTarget().equals(node)) {
				result.add(conn);
			}
		}
		return result;
	}

	/**
	 * Gibt die Verbindung im Graphen zu einer gegebenen Quelle und einem
	 * gegebenen Ziel zurueck. Wenn im Graphen keine solche Verbindung
	 * existiert, wird null zurueck gegeben.
	 * 
	 * @param source
	 *            Quelle der Verbindung.
	 * @param target
	 *            Ziel der Verbindung.
	 * @return Verbindung mit angegebener Quelle und Ziel, oder null falls
	 *         dieser Graph keine solche Verbindung enthaelt.
	 */
	public IConnection getConnection(IGraphNode source, IGraphNode target) {
		for (IConnection conn : connections) {
			if (conn.getTarget().equals(target)
					&& conn.getSource().equals(source)) {
				return conn;
			}
		}
		return null;

	}

	/**
	 * Gibt an, ob es eine Verbindung zwischen den beiden Knoten gibt.
	 * 
	 * @param source
	 *            Der Quellknoten.
	 * @param target
	 *            Der Zielknoten.
	 * @return True, wenn so eine Verbindung existiert. False, sonst.
	 */
	public boolean containsConnection(IGraphNode source, IGraphNode target) {
		return getConnection(source, target) != null;
	}

	/**
	 * Sucht rekursiv alle Knoten im Graphen, die von einem bestimmten Typen
	 * sind.
	 * 
	 * @param type
	 *            Der Typ auf den die Knoten castbar sein müssen.
	 * @param searchSceneSequences
	 *            Gibt an, ob der in Knoten vom Typ NodeSceneSequence
	 *            eingebettete Graph mit durchsucht werden soll, oder ob die
	 *            Suche auf diesen Graph beschränkt sein soll.
	 */
	public List<IGraphNode> searchNodes(Class<? extends IGraphNode> type,
			boolean searchSceneSequences) {

		// Hashset mit allen bereits gefundenen Knoten
		HashSet<IGraphNode> alreadyFound = new HashSet<IGraphNode>();

		// Queue mit den noch abzuarbeitenden Knoten
		Queue<IGraphNode> workingNodes = new LinkedList<IGraphNode>();
		workingNodes.addAll(nodes);

		List<IGraphNode> result = new ArrayList<IGraphNode>();

		while (!workingNodes.isEmpty()) {
			IGraphNode current = workingNodes.poll();
			// Nur noch nicht abgearbeitete Knoten bearbeiten
			if (!alreadyFound.contains(current)) {
				alreadyFound.add(current);
				// Falls der gefundene Knoten vom angegebenen Typ ist, fuegen
				// wir ihn ins Ergebnis ein.
				if (type.isAssignableFrom(current.getClass())) {
					result.add(current);
				}
			}
		}
		return result;

	}

	/**
	 * Getter of the property <tt>start</tt>
	 * 
	 * @return Returns the start.
	 * @uml.property name="start"
	 */
	public NodeStart getStart() {
		return start;
	}

	// /**
	// * Getter of the property <tt>nodeList</tt>
	// *
	// * @return Returns the nodeList.
	// * @uml.property name="nodeList"
	// */
	// public Collection<IGraphNode> getNodeList() {
	// return nodeList;
	// }

	/**
	 * Getter of the property <tt>end</tt>
	 * 
	 * @return Returns the end.
	 * @uml.property name="end"
	 */
	public NodeEnd getEnd() {
		return end;
	}

	/**
	 * Sucht zu einem bestimmten Model-Objekt die davon direkt oder indirekt
	 * abhaengigen Graph-Knoten.
	 * 
	 * @param object
	 *            Das Model-Objekt.
	 * @param searchSequences
	 *            Gibt an, ob auch die in diesem Graphen enthaltenen
	 *            Szenen-Sequenzen durchsucht werden sollen (true) oder nicht
	 *            (false).
	 * @return Liste der von dem Model-Objekt direkt oder indirekt abhaengigen
	 *         Knoten.
	 */
	public Set<IGraphNode> searchDependentNodes(IAbstractBean object,
			boolean searchSequences) {
		Set<IGraphNode> result = new HashSet<IGraphNode>();

		// Alle direkt vom Medien-Objekt abhaengigen Knoten suchen
		List<IGraphNode> allNodes = searchNodes(IGraphNode.class,
				searchSequences);
		for (IGraphNode node : allNodes) {
			if (node.isDependentOn(object) && !result.contains(node)) {
				result.add(node);
				searchDependentNodes(node, result);
			}
		}

		return result;
	}

	/**
	 * Sucht zu einer Menge von bestimmten Model-Objekten die davon direkt oder
	 * indirekt abhaengigen Graph-Knoten.
	 * 
	 * @param objects
	 *            Die Model-Objekte.
	 * @param searchSequences
	 *            Gibt an, ob auch die in diesem Graphen enthaltenen
	 *            Szenen-Sequenzen durchsucht werden sollen (true) oder nicht
	 *            (false).
	 * @return Liste der von den gegebenen Model-Objekten direkt oder indirekt
	 *         abhaengigen Knoten.
	 */
	public Set<IGraphNode> searchDependentNodes(List<IAbstractBean> objects,
			boolean searchSequences) {
		Set<IGraphNode> result = new HashSet<IGraphNode>();

		// Alle direkt vom Medien-Objekt abhaengigen Knoten suchen
		List<IGraphNode> allNodes = searchNodes(IGraphNode.class,
				searchSequences);
		for (IGraphNode node : allNodes) {
			for (IAbstractBean object : objects) {
				if (node.isDependentOn(object) && !result.contains(node)) {
					result.add(node);
					searchDependentNodes(node, result);
				}
			}
		}

		return result;
	}

	/**
	 * Sucht die von einem Knoten direkt oder indirekt abhaengigen Knoten und
	 * fuegt diese in die uebergebene Ergebnismenge mit ein. Es wird nur
	 * innerhalb des Graphen des uebergebenen Knotens gesucht.
	 * 
	 * @param node
	 *            Der Knoten von dem die direkt oder indirekt abhaengigen Knoten
	 *            gesucht werden sollen.
	 * @param result
	 *            Die Ergebnismenge in die die gefundenen Knoten eingefuegt
	 *            werden sollen.
	 */
	public void searchDependentNodes(IGraphNode node, Set<IGraphNode> result) {

		// Alle direkt vom Medien-Objekt abhaengigen Knoten suchen
		Graph graph = node.getGraph();
		List<IConnection> outgoingConns = graph.getConnectionsBySource(node);
		for (IConnection outgoingConn : outgoingConns) {
			if (outgoingConn instanceof DependentConnection) {
				IGraphNode dependentNode = outgoingConn.getTarget();
				if (!result.contains(dependentNode)) {
					result.add(dependentNode);
					// Rekursion: Suche jetzt die von dem abhaengigen Knoten
					// wiederum abhaengigen Knoten.
					searchDependentNodes(dependentNode, result);
				}
			}
		}
	}

}
