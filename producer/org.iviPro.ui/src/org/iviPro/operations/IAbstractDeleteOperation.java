package org.iviPro.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.DependentConnection;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeScene;

/**
 * Abstrakte Basisklasse fuer Loesch-Operationen. Sie bietet Logik, um z.B. die
 * von dem zu loeschenden Objekt abhaengigen Objekte im Graphen mit zu loeschen.
 * 
 * @author dellwo
 * 
 */
public abstract class IAbstractDeleteOperation extends IAbstractOperation {

	/**
	 * Liste der von dem zu loeschenden Objekt abhaengigen Knoten im
	 * Szenen-Graph. Diese muessen ebenfalls geloescht werden.
	 */
	private List<GraphDependency> graphDependencies;
	protected List<INodeAnnotationLeaf> globalAnnoDependencies;
	protected List<IAbstractBean> dependencies;
	protected List<IAbstractBean> references;
	
	/**
	 * Das Projekt, wo nach abhaengigen Objekten gesucht wird.
	 */
	private final Project project;

	/**
	 * Erstellt eine neue Abstrakte Loesch-Operation.
	 * 
	 * @param label
	 *            Das Label der Operation z.B. fuer das undo/redo.
	 * @param project
	 *            Das Projekt in dem nach Abhaengigekeiten gesucht werden soll.
	 * @throws IllegalArgumentException
	 *             Falls dass Medien-Objekt null ist oder kein Projekt geoeffnet
	 *             ist.
	 */
	public IAbstractDeleteOperation(String label, Project project)
			throws IllegalArgumentException {
		super(label);
		if (label == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		this.project = project;
	}

	@Override
	public abstract boolean canExecute();

	@Override
	public abstract String getErrorMessage(Exception e);

	protected abstract List<IAbstractBean> getObjectsToDelete();

	@Override
	public final IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// Diese Liste enthaelt die urspruenglich zu loeschenden Objekte,
		// so wie sie die implementierende Klasse zur Verfuegung stellt.
		List<IAbstractBean> objectsToDelete  = getObjectsToDelete();
		graphDependencies = calcGraphDependencies(objectsToDelete);
		globalAnnoDependencies = calcGlobalDependencies(objectsToDelete);
		dependencies = (calcAdditionalDependencies(objectsToDelete));
		dependencies.addAll(globalAnnoDependencies);
		references = calcAdditionalReferences(objectsToDelete);
		
		if (graphDependencies.isEmpty() && dependencies.isEmpty() && references.isEmpty()) {
			return redo(monitor, info);
		} else {
			// Abhaengigkeiten vorhanden => Vorher fragen ob diese geloescht
			// werden duerfen.
			StringBuilder listOfDependencies = new StringBuilder();
			if (!graphDependencies.isEmpty() || !dependencies.isEmpty()) {
				listOfDependencies.append(Messages.IAbstractDeleteOperation_QuestionDeleteDependentObj_Dependencies)
				.append("\n");				
				for (GraphDependency dependency : graphDependencies) {
					if (!dependency.isSubDependency()) {
						IGraphNode node = dependency.getNode();
						listOfDependencies.append("- ").append(node.getBeanTag())//$NON-NLS-1$
						.append(": ") //$NON-NLS-1$
						.append(node.getTitle())
						.append(" (id: ") //$NON-NLS-1$
						.append(node.getNodeID())
						.append(")");
												
						if (node instanceof INodeAnnotation) {
							NodeScene parentScene = ((INodeAnnotation)node).getParentScene();
							if (parentScene != null) {
								listOfDependencies.append(" [Scene: ")
								.append(parentScene.getTitle())
								.append(" (id: ")
								.append(parentScene.getNodeID())
								.append(")]");
							}
						}						
						listOfDependencies.append("\n"); //$NON-NLS-1$
					}
				}
				for (IAbstractBean bean : dependencies) {
					addDependencyEntry(bean, listOfDependencies);
				}
			}
									
			if (!references.isEmpty()) {
				listOfDependencies.append("\n").append(Messages.IAbstractDeleteOperation_QuestionDeleteDependentObj_Uses)//$NON-NLS-1$
				.append("\n"); //$NON-NLS-1$
				for (IAbstractBean bean : references) {
					addDependencyEntry(bean, listOfDependencies);
				}
			}
			
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox
					.setMessage(Messages.IAbstractDeleteOperation_QuestionDeleteDependentObj_Part1
							+ "\n\n" //$NON-NLS-1$
							+ listOfDependencies
							+ "\n" //$NON-NLS-1$
							+ Messages.IAbstractDeleteOperation_QuestionDeleteDependentObj_Part2);
			messageBox
					.setText(Messages.IAbstractDeleteOperation_QuestionDeleteDependentObj_Title);
			
			int option = messageBox.open();
			// Fortfahren, wenn Benutzer der Loeschung abhaengiger Objekte
			// zugestimmt hat, ansonsten abbrechen
			if (option == SWT.YES) {
				return redo(monitor, info);
			} else {
				return Status.CANCEL_STATUS;
			}
		}
	}

	/**
	 * Berechnet die Abhaengigkeiten im Graphen von den zu loeschenden Objekten.
	 * 
	 * @param objectsToDelete
	 *            Die zu loeschenden Objekte.
	 * @return
	 */
	private final List<GraphDependency> calcGraphDependencies(
			List<IAbstractBean> objectsToDelete) {
		List<GraphDependency> dependencies = new ArrayList<GraphDependency>();
		Graph graph = project.getSceneGraph();
		Set<IGraphNode> dependentNodes = graph.searchDependentNodes(
				objectsToDelete, true);
		for (IGraphNode node : dependentNodes) {
			boolean subDependency = false;
			for (IConnection conn : graph.getConnectionsByTarget(node)) {
				if (conn instanceof DependentConnection 
						&& dependentNodes.contains(conn.getSource())){
					subDependency = true;
				}
			}
			dependencies.add(new GraphDependency(node, subDependency));
		}
		return dependencies;
	}
	
	/**
	 * Returns global annotations depending on the given list of beans.
	 * @param objectsToDelete list of beans
	 * @return list of global annotations depending on the beans
	 */
	private final List<INodeAnnotationLeaf> calcGlobalDependencies(List<IAbstractBean> objectsToDelete) {
		List<INodeAnnotationLeaf> dependentAnnotations = new ArrayList<INodeAnnotationLeaf>();
		for (IAbstractBean object : objectsToDelete) {
			for (INodeAnnotationLeaf annotation : project.getGlobalAnnotations()) {
				if (annotation.isDependentOn(object)) {
					dependentAnnotations.add(annotation);
				}
			}
		}
		return dependentAnnotations;
	}
	
	private void addDependencyEntry(IAbstractBean bean, StringBuilder listOfDependencies) {
		listOfDependencies.append("- ") //$NON-NLS-1$
			.append(bean.getBeanTag())
			.append(": ") //$NON-NLS-1$
			.append(bean.getTitle());
		if (bean instanceof IGraphNode) {
			IGraphNode node = (IGraphNode)bean;
			listOfDependencies.append(" (id: ") //$NON-NLS-1$
				.append(node.getNodeID())
				.append(")"); //$NON-NLS-1$
			if (node instanceof INodeAnnotationLeaf) {
				listOfDependencies.append(" [global]");
			}
		}
		listOfDependencies.append("\n"); //$NON-NLS-1$
	}
	
	/**
	 * Returns a list of subclass specific dependencies for the given list of objects.
	 * <p/><b>Note:</b> may not return <code>null</code>
	 * @param objectsToDelete objects which are deleted
	 * @return list of dependencies of the given objects - may not be null
	 */
	protected abstract List<IAbstractBean> calcAdditionalDependencies(List<IAbstractBean> objectsToDelete);
	
	/**
	 * Returns a list of subclass specific references for the given list of objects.
	 * <p/><b>Note:</b> may not return <code>null</code>
	 * @param objectsToDelete objects which are deleted
	 * @return list of references to the given objects
	 */
	protected abstract List<IAbstractBean> calcAdditionalReferences(List<IAbstractBean> objectsToDelete);
			
	protected abstract boolean deleteObjects();

	protected abstract void restoreObjects();

	@Override
	public final IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// Erst abhaengig Objekte loeschen...
		for (GraphDependency dependency : graphDependencies) {
			Graph graph = dependency.getGraph();
			// Abhaengigen Knoten aus seinem Graphen loeschen
			// Kanten muessen nicht extra geloescht werden, da sie automatisch
			// bei removeNode() mit geloescht werden.
			graph.removeNode(dependency.getNode());
		}
		for (INodeAnnotationLeaf anno : globalAnnoDependencies) {
			project.getGlobalAnnotations().remove(anno);
		}
		// .. dann die eigentlich zu loeschenden Objekte loeschen
		// falls es schief geht oder der User entscheidet abzubrechen (z.B. weil
		// AudioParts eines Audio-File)
		// gelöscht werden) stelle die abhängigkeiten wieder her
		if (!deleteObjects()) {
			recreateDependencies();
		}
		return Status.OK_STATUS;
	}

	@Override
	public final IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// Erst wieder die urspruenglichen Basis-Objekte wiederherstellen...
		restoreObjects();
		recreateDependencies();
		for (INodeAnnotationLeaf anno : globalAnnoDependencies) {
			project.getGlobalAnnotations().add(anno);
		}
		return Status.OK_STATUS;
	}

	private void recreateDependencies() {
		// ...dann die abhaengigen Knoten
		for (GraphDependency dependency : graphDependencies) {
			Graph graph = dependency.getGraph();
			graph.addNode(dependency.getNode());
		}
		// ... und ihre Kanten.
		for (GraphDependency dependency : graphDependencies) {
			Graph graph = dependency.getGraph();
			for (IConnection connection : dependency.getIncomingConnections()) {
				if (graph.getConnection(connection.getSource(),
						connection.getTarget()) == null) {
					graph.addConnection(connection);
				}
			}
			for (IConnection connection : dependency.getOutgoingConnections()) {
				if (graph.getConnection(connection.getSource(),
						connection.getTarget()) == null) {
					graph.addConnection(connection);
				}
			}
		}
	}

	/**
	 * Klasse die einen von dem geloeschten Medien-Objekt abhaengigen Knoten
	 * speichert, mit allen dazugehoerigen Verbindungen und dem zugehoerigen
	 * Graphen.
	 * 
	 * @author dellwo
	 * 
	 */
	private final class GraphDependency {

		private final Graph graph;
		private final IGraphNode node;
		private final List<IConnection> incomingConnections;
		private final List<IConnection> outgoingConnections;
		private final boolean subDependency;

		GraphDependency(IGraphNode node, boolean subDependency) {
			this.node = node;
			this.graph = node.getGraph();
			this.incomingConnections = new ArrayList<IConnection>();
			this.outgoingConnections = new ArrayList<IConnection>();
			outgoingConnections.addAll(graph.getConnectionsBySource(node));
			incomingConnections.addAll(graph.getConnectionsByTarget(node));
			this.subDependency = subDependency;
		}

		Graph getGraph() {
			return graph;
		}

		IGraphNode getNode() {
			return node;
		}

		List<IConnection> getIncomingConnections() {
			return incomingConnections;
		}

		List<IConnection> getOutgoingConnections() {
			return outgoingConnections;
		}
		
		/**
		 * Returns whether or not this dependency is already covered by another dependency.
		 * @return true if this is just a sub dependency of another dependency
		 */
		boolean isSubDependency() {
			return subDependency;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof GraphDependency) {
				return ((GraphDependency) obj).node == this.node;
			}
			return false;
		}

	}
}
