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
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;
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
		List<IAbstractBean> originalObjectsToDelete = getObjectsToDelete();

		// Diese Liste enthaelt zusaetzlich noch alle abhaengigen Objekte
		// die mitgeloesch werden mussen.
		// Derzeit ist beides das gleiche, da objectsToDelete nirgends
		// erweitert wird.
		List<IAbstractBean> objectsToDelete = new ArrayList<IAbstractBean>(
				originalObjectsToDelete);
		this.graphDependencies = calcGraphDependencies(objectsToDelete);

		// Pruefen ob Abhaengigkeiten vorhanden sind. Es duerfen keine
		// Graph-Abhaengigkeiten vorhanden sein und in objectsToDelete darf nur
		// das Medien-Objekt selbst drin sein.
		if (graphDependencies.isEmpty()) {
			// Keine Abhaengigkeiten => Gleich loeschen
			return redo(monitor, info);
		} else {
			// Abhaengigkeiten vorhanden => Vorher fragen ob diese geloescht
			// werden duerfen.
			StringBuilder listOfDependencies = new StringBuilder();
			for (IAbstractBean objectToDelete : objectsToDelete) {
				if (!originalObjectsToDelete.contains(objectToDelete)) {
					listOfDependencies.append(objectToDelete);
					listOfDependencies.append("\n"); //$NON-NLS-1$
				}
			}
			for (GraphDependency dependency : graphDependencies) {
				if (dependency.getNode() instanceof NodeScene) {
					listOfDependencies.append(dependency.getNode().getTitle() + "\n"); //$NON-NLS-1$
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
			dependencies.add(new GraphDependency(node));
		}
		return dependencies;
	}

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

		GraphDependency(IGraphNode node) {
			this.node = node;
			this.graph = node.getGraph();
			this.incomingConnections = new ArrayList<IConnection>();
			this.outgoingConnections = new ArrayList<IConnection>();
			outgoingConnections.addAll(graph.getConnectionsBySource(node));
			incomingConnections.addAll(graph.getConnectionsByTarget(node));
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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof GraphDependency) {
				return ((GraphDependency) obj).node == this.node;
			}
			return false;
		}

	}

}
