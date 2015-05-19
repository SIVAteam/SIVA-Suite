/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.graph.GraphChildList"
 */
public abstract class IGraphNode extends IAbstractBean implements Comparable<IGraphNode> {

	public static final String PROP_POSITION = "position"; //$NON-NLS-1$
	public static final String PROP_CHANGEZOOMLEVEL = "changezoomlevel"; //$NON-NLS-1$

	private final ConnectionConstraints connectionConstraints;

	/**
	 * Unique identifier for nodes in the scene graph.
	 */
	private int nodeID;
	
	/**
	 * @uml.property name="position"
	 */
	private Point position;
	
	private Point pos_semzoomlvl1 = new Point(0,0);
	private Point pos_semzoomlvl2 = new Point(0,0);
	private Point pos_semzoomlvl3 = new Point(0,0);

	/**
	 * Der Graph in dem der Knoten liegt
	 */
	private Graph graph;
	
	/**
	 * Das semantische Zoomlevel des Knoten
	 */
	private int semZoomlevel = 1;
	
	/**
	 * In case a validation of the node fails, this field contains a
	 * message explaining the validation problem.
	 */
	private String validationError = "";

	/**
	 * @uml.property name="parent"
	 * @param position
	 */
	public IGraphNode(LocalizedString title, Point position, Project project,
			ConnectionConstraints connectionConstraints) {
		super(title, project);
		nodeID = project.getNodeIDGen().getNextID();
		this.connectionConstraints = connectionConstraints;
		this.position = position;
		semZoomlevel = 1;
	}

	/**
	 * 
	 * @param position
	 */
	public IGraphNode(String title, Point position, Project project,
			ConnectionConstraints connectionConstraints) {
		super(title, project);
		nodeID = project.getNodeIDGen().getNextID();
		this.connectionConstraints = connectionConstraints;
		this.position = position;
		semZoomlevel = 1;
	}
	
	/**
	 * Compares <code>this</code> node and the parameter <code>otherNode</code>
	 * with respect to their node id. In the context of a scene graph this 
	 * comparison is consistent with equals as each node can be uniquely 
	 * identified by its id.
	 */
	@Override
	public int compareTo(IGraphNode otherNode) {
		if (this.getNodeID() < otherNode.getNodeID()) {
			return -1;
		} else if (this.getNodeID() > otherNode.getNodeID()) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Returns the unique identifier for this node.
	 * @return unique ID
	 */
	public int getNodeID() {
		return nodeID;
	}	

	/**
	 * Gibt an, ob der Graph-Knoten direkt abhaengig von dem angegebenen Objekt
	 * ist. Beispielsweise ist ein Richtext-Annotation direkt abhaengig von dem
	 * zugehoerigen Richtext oder ein Szenen-Knoten direkt abhaenig von der
	 * Szene.
	 * 
	 * @param object
	 *            Das Objekt zu dem die Abhaengigkeit geprueft werden soll.
	 * @return True, wenn der Graph-Knoten abhaengig von dem Objekt ist, false
	 *         sonst.
	 */
	public abstract boolean isDependentOn(IAbstractBean object);

/**
 *  Offenbar nicht mehr verwendet
 */
//	/**
//	 * Gibt an, ob dieser Knoten direkt abhaengig von dem gegebenen Knoten ist.
//	 * Dies ist der Fall, wenn von dem anderen Knoten eine abhaengige Verbindung
//	 * zu diesem Knoten fuehrt.
//	 * 
//	 * @param node
//	 *            Der andere Knoten
//	 * @return True, falls dieser Knoten direkt abhaengig von dem anderen Knoten
//	 *         ist, ansonsten false.
//	 */
//	public boolean isDependentOn(IGraphNode node) {
//		// Falls zu diesem Knoten eine abhaengige Verbindung von dem gegebenen
//		// Knoten fuehrt, ist dieser Knoten abhaengig von dem gegegebenen
//		// Knoten.
//		if (graph != null) {
//			IConnection connection = graph.getConnection(node, this);
//			if (connection instanceof DependentConnection) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	void setGraph(Graph graph) {
		this.graph = graph;
	}

	public Graph getGraph() {
		return graph;
	}

	/**
	 * Returns a list of all parent nodes of this node.
	 * @return list of parent nodes
	 * @throws IllegalStateException if the node is not part of a graph
	 */
	public List<IGraphNode> getParents() {
		List<IGraphNode> result = new ArrayList<IGraphNode>();
		if (graph == null) {
			throw new IllegalStateException("This node is not part of a graph." //$NON-NLS-1$
					+ " Add the node to a graph before invoking this method"); //$NON-NLS-1$
		} else {
			List<IConnection> incomingConns = graph
					.getConnectionsByTarget(this);
			for (IConnection conn : incomingConns) {
				result.add(conn.getSource());
			}
		}
		return result;
	}

	public List<IGraphNode> getParents(Class<? extends IGraphNode> parentType) {
		List<IGraphNode> result = new ArrayList<IGraphNode>();
		if (graph == null) {
			throw new IllegalStateException("This node is not part of a graph." //$NON-NLS-1$
					+ " Add the node to a graph before invoking this method"); //$NON-NLS-1$
		} else {
			List<IConnection> incomingConns = graph
					.getConnectionsByTarget(this);
			for (IConnection conn : incomingConns) {
				IGraphNode parent = conn.getSource();
				if (parentType == null) {
					result.add(parent);
				} else if (parentType.isAssignableFrom(parent.getClass())) {
					result.add(parent);
				}

			}
		}
		return result;
	}

	//
	// public void setParent(IGraphNode parent) throws IllegalArgumentException
	// {
	// if (graph == null) {
	//			throw new IllegalStateException("This node is not part of a graph." //$NON-NLS-1$
	//					+ " Add the node to a graph before invoking this method"); //$NON-NLS-1$
	// } else {
	// IConnection conn = new DefaultConnection(parent, this, project);
	// graph.addConnection(conn);
	// }
	// }

	/**
	 * Getter of the property <tt>position</tt>
	 * 
	 * @return Returns the position.
	 * @uml.property name="position"
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Setter of the property <tt>position</tt>
	 * 
	 * @param position
	 *            The position to set.
	 * @uml.property name="position"
	 */
	public void setPosition(Point position) {
		Point oldValue = this.position;
		this.position = position;
		firePropertyChange(PROP_POSITION, oldValue, position);
	}

	/**
	 * Gibt die Kinder dieses Knotens zurueck, d.h. die Knoten, die an einer
	 * ausgehenden Kante dieses Knotens haengen.
	 * 
	 * @return
	 */
	public List<IGraphNode> getChildren() {
		return getChildren(null);
	}

	/**
	 * Gibt nur die Kinder des Knotens - d.h. die Knoten, die an einer
	 * ausgehenden Kante des Knotens haengen - zurueck, die von einer bestimmten
	 * Klasse sind. Es reicht hierzu, wenn sich das Kind auf die Klasse casten
	 * laesst.
	 * 
	 * @param childType
	 *            Der Typ der gesuchten Kinder.
	 * @return Alle Kinder von diesem Typ.
	 */
	public List<IGraphNode> getChildren(Class<?> childType) { //extends IGraphNode
		List<IGraphNode> result = new ArrayList<IGraphNode>();
		if (graph == null) {
			throw new IllegalStateException("This node is not part of a graph." //$NON-NLS-1$
					+ " Add the node to a graph before invoking this method"); //$NON-NLS-1$
		} else {
			List<IConnection> outgoingConns = graph
					.getConnectionsBySource(this);
			for (IConnection conn : outgoingConns) {
				IGraphNode child = conn.getTarget();
				if (childType == null) {
					result.add(child);
				} else if (childType.isAssignableFrom(child.getClass())) {
					result.add(child);
				}
			}
		}
		return result;
	}

	/**
	 * Gibt das erste Kind dieses Knotens im Graph zurueck, das von einem
	 * bestimmten Typ ist. Falls der Knoten gar keine Kinder dieses Typs hat,
	 * wird null zurueck gegeben.
	 * 
	 * @param type
	 *            Der Typ des gesuchten Kindes.
	 * @return Erstes Kind des angegebenen Typs dieses Knotens oder null, falls
	 *         es kein solches gibt.
	 */
	public IGraphNode getFirstChild(Class<? extends IGraphNode> type) {
		List<IGraphNode> matchingChilds = getChildren(type);
		Iterator<IGraphNode> it = matchingChilds.iterator();
		if (it.hasNext()) {
			return it.next();
		} else {
			return null;
		}
	}

	/**
	 * Gibt das erste Kind dieses Knotens im Graph zurueck oder null, falls der
	 * Knoten gar keine Kinder hat.
	 * 
	 * @return Erstes Kind dieses Knotens oder null, falls es kein solches gibt.
	 */
	public IGraphNode getFirstChild() {
		return getFirstChild(IGraphNode.class);
	}

	/**
	 * 
	 * @param child
	 * @return
	 */
	public boolean hasChild(IGraphNode child) {
		return getChildren().contains(child);
	}

	/**
	 * Ermittelt, ob eine eingehende Verbindung von einer bestimmten Quelle
	 * abschliessend erstellt werden kann oder ob dies gegen die Constraints
	 * fuer Verbindungen von diesem Knoten verstossen wuerde.
	 * 
	 * @param source
	 *            Die Quelle der Verbindung.
	 * @return True, wenn Verbindung erstellt werden kann, false sonst.
	 */
	public boolean canCompleteIncomingConnection(IGraphNode source) {
		/**
		 * For now it's sufficient to check that selection and quiz controls
		 * just have a single parent. If we need more complex checks, we might
		 * need to introduce a second list of ConnectionConstraints for incoming
		 * connections.
		 */
		if ((this instanceof NodeSelectionControl
				|| this instanceof NodeQuizControl)
					&& this.getParents().size() > 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Test whether or not this node can be connected to the new given source.
	 * @param newSource the new source to which a connection should be established
	 * @return true if a connection is allowed - false otherwise
	 */
	public boolean canReconnectIncomingConnection(IGraphNode newSource) {
		return newSource.canCompleteOutgoingConnection(this);		
	}
	
	/**
	 * Test whether or not this node can be connected to the new given target.
	 * @param previousTarget the target this node was connected to before
	 * @param newSource the new target to which a connection should be established
	 * @return true if a connection is allowed - false otherwise
	 */
	public boolean canReconnectOutgoingConnection(IGraphNode previousTarget,
			IGraphNode newTarget) {
		/**
		 * For now it's sufficient to check that selection and quiz controls
		 * just have a single parent. If we need more complex checks, we might
		 * need to introduce a second list of ConnectionConstraints for incoming
		 * connections.
		 */
		if ((newTarget instanceof NodeSelectionControl
				|| newTarget instanceof NodeQuizControl)
					&& newTarget.getParents().size() > 0) {
			return false;
		}
		
		// Wenn keine Verbindungs-Definition zum neuen Ziel existiert,
		// dann weise Verbindung zurueck.
		ConnectionTargetDefinition newTargetConstraint = connectionConstraints
				.getTargetDefinition(newTarget.getClass());
		if (newTargetConstraint == null) {
			return false;
		}

		// Wenn bereits bestehende Verbindung zum neuen Knoten existiert, kann
		// keine Verbindung dorthin doppelt erstellt werden.
		if (graph.containsConnection(this, newTarget)) {
			return false;
		}

		// Zu unrestriktierten Zielen darf auf jeden Fall eine Verbindung
		// aufgebaut werden.
		if (newTargetConstraint.isUnrestricted()) {
			return true;
		}

		// Gesamt-Verbindungsanzahl sollte keine Rolle spielen, da beim
		// Reconnect eines Targets sich ja die gesamte Anzahl an Verbindungen
		// bei diesem Knoten hier (der Quelle) nicht erhoeht.

		// Es muss also nur geprueft werden, ob die maximale Verbindungsanzahl
		// zu der neuen Zielklasse durch den Reconnect verletzt werden wuerde.
		List<IConnection> outgoingConns = graph.getConnectionsBySource(this);
		int numTargetClassConnections = 0;
		for (IConnection conn : outgoingConns) {
			if (newTarget.getClass().equals(conn.getClass())) {
				// Die alte Verbindung brauchen wir nicht mitzaehlen, da die ja
				// sowieso ersetzt wird.
				if (!conn.getTarget().equals(previousTarget)) {
					numTargetClassConnections++;
				}
			}
		}
		if (numTargetClassConnections >= newTargetConstraint
				.getMaxAllowedConnections()) {
			return false;
		}

		// Alles schein in Ordnung zu sein -> Verbindung akzeptieren.
		return true;
	}

	/**
	 * Ermittelt, ob eine weitere ausgehende Verbindung zu einem beliebigen Ziel
	 * erstellt werden kann. Dies ist nicht mehr der Fall, wenn die maximale
	 * Anzahl an ausgehenden Verbindungen fuer diesen Knoten erreicht wurde.
	 * 
	 * @param restrictedOnly
	 *            Wenn true, werden nur restriktierte Verbindungen betrachtet.
	 * @return True, wenn eine neue Verbindung von diesem Knoten aus moeglich
	 *         ist, ansonsten false.
	 */
	public boolean canCreateOutgoingConnection(boolean restrictedOnly) {
		int maxConns = connectionConstraints.getMaxAllowedConnections();
		int existingConns = 0;
		List<IConnection> connections = graph.getConnectionsBySource(this);
		boolean containsUnrestrictedTargets = false;
		for (IConnection conn : connections) {
			IGraphNode target = conn.getTarget();
			ConnectionTargetDefinition targetDef = connectionConstraints
					.getTargetDefinition(target.getClass());
			if (targetDef.isUnrestricted()) {
				containsUnrestrictedTargets = true;
			} else {
				existingConns++;
			}
		}
		// Neue Verbindung erlaubt, wenn unrestricted Ziele beruecksichtigt
		// werden sollen.
		if (!restrictedOnly && containsUnrestrictedTargets) {
			return true;
		}
		// Ansonsten darf die maximale Anzahl an Kanten noch nicht erreicht
		// sein.
		return (existingConns < maxConns);
	}

	/**
	 * Ermittelt, ob eine ausgehende Verbindung zu einem bestimmten Ziel
	 * abschliessend erstellt werden kann oder ob dies gegen die Constraints
	 * fuer Verbindungen von diesem Knoten verstossen wuerde.
	 * 
	 * @param target
	 *            Das Ziel der Verbindung.
	 * @return True, wenn Verbindung erstellt werden kann, false sonst.
	 */
	public boolean canCompleteOutgoingConnection(IGraphNode target) {

		// Wenn keine Verbindungs-Definition zur Ziel-Klasse existiert,
		// dann weise Verbindung zurueck.
		ConnectionTargetDefinition targetConstraint = connectionConstraints
				.getTargetDefinition(target.getClass());
		if (targetConstraint == null) {
			return false;
		}

		// Wenn bereits bestehende Verbindung zu diesem Knoten existiert, kann
		// keine neue neue erstellt werden.
		if (graph.containsConnection(this, target)) {
			return false;
		}

		// Zu unrestriktierten Zielen darf auf jeden Fall eine Verbindung
		// aufgebaut werden.
		if (targetConstraint.isUnrestricted()) {
			return true;
		}

		// Nicht akzeptieren, wenn maximale Gesamt-Verbindungsanzahl bereits
		// erreicht ist.
		List<IConnection> outgoingConns = graph.getConnectionsBySource(this);
		int totalConnections = 0;
		for (IConnection conn : outgoingConns) {
			if (!connectionConstraints.getTargetDefinition(
					conn.getTarget().getClass()).isUnrestricted()) {
				// Nur restriktierte Ziele zaehlen. Unrestriktierte darf es ja
				// beliebig viele geben.
				totalConnections++;
			}
		}
		if (totalConnections >= connectionConstraints
				.getMaxAllowedConnections()) {
			return false;
		}

		// Nicht akzpetieren, wenn maximale Verbindungsanzahl zur Zielklasse
		// bereits erreicht ist.
		int numTargetClassConnections = 0;
		for (IConnection conn : outgoingConns) {
			if (target.getClass().equals(conn.getClass())) {
				numTargetClassConnections++;
			}
		}
		if (numTargetClassConnections >= targetConstraint
				.getMaxAllowedConnections()) {
			return false;
		}

		// Alles schein in Ordnung zu sein -> Verbindung akzeptieren.
		return true;
	}

	/**
	 * Gibt die Constraint-Definitionen zurueck, die festlegen, mit welchen
	 * Knoten dieser Knoten verbunden werden darf/muss.
	 * 
	 * @return Verbindungs-Constraints dieses Knotens.
	 */
	public ConnectionConstraints getConnectionConstraints() {
		return connectionConstraints;
	}

	@Override
	public String toString() {
		if (graph == null) {
			return super.toString() + "{n/a}"; //$NON-NLS-1$
		} else {
			return super.toString() + "{" + getChildren().size() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	//Setter für semantisches Zoomlevel
	public void setSemZoomlevel(int zoomlevel) {
		this.semZoomlevel = zoomlevel;
		firePropertyChange(PROP_CHANGEZOOMLEVEL, null, null);
	}

	//Getter für semantisches Zoomlevel
	public int getSemZoomlevel() {
		return semZoomlevel;
	}
	
	//Getter für Position bei semantischen Zoomlevel 1
	public Point getPos_semzoomlvl1() {
		return pos_semzoomlvl1;
	}

	//Setter für Position bei semantischen Zoomlevel 1
	public void setPos_semzoomlvl1(Point pos_semzoomlvl1) {
		this.pos_semzoomlvl1 = pos_semzoomlvl1;
	}

	//Getter für Position bei semantischen Zoomlevel 2
	public Point getPos_semzoomlvl2() {
		return pos_semzoomlvl2;
	}

	//Setter für Position bei semantischen Zoomlevel 2
	public void setPos_semzoomlvl2(Point pos_semzoomlvl2) {
		this.pos_semzoomlvl2 = pos_semzoomlvl2;
	}

	//Getter für Position bei semantischen Zoomlevel 3
	public Point getPos_semzoomlvl3() {
		return pos_semzoomlvl3;
	}

	//Setter für Position bei semantischen Zoomlevel 3
	public void setPos_semzoomlvl3(Point pos_semzoomlvl3) {
		this.pos_semzoomlvl3 = pos_semzoomlvl3;
	}
	
	/**
	 * Validates whether or not the node meets all constraints necessary for
	 * it to be well defined and ready for export.
	 * This includes correct initialization of required fields as well as
	 * correct embedding in the graph structure, i.e. existence of needed
	 * parent and child connections.
	 * @return true if all constraints are met - false otherwise
	 */
	public boolean validateNode() {
		return true;
	}
	
	/**
	 * Returns an error message associated with an unsuccessful validation
	 * attempt or an empty string in case the validation succeeded. 
	 * @return error message describing the validation problem
	 */
	public String getValidationError() {
		return validationError;
	}	
}
