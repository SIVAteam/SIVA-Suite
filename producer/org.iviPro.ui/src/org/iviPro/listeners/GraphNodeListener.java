package org.iviPro.listeners;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;

public class GraphNodeListener {

	private Graph graph;
	private IGraphNode node;
	private GraphNodeEventConsumer consumer;

	private PropertyChangeListener graphListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			onGraphChanged(evt);
		}
	};

	private PropertyChangeListener nodeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			onNodeChanged(evt);
		}
	};

	public GraphNodeListener(GraphNodeEventConsumer consumer) {
		this.consumer = consumer;
	}

	public void startListening(IGraphNode node, Graph graph) {
		this.graph = graph;
		this.node = node;
		graph.addPropertyChangeListener(graphListener);
		node.addPropertyChangeListener(nodeListener);
	}

	public void stopListening() {
		node.removePropertyChangeListener(nodeListener);
		graph.removePropertyChangeListener(graphListener);
		this.graph = null;
		this.node = null;
	}

	private void onNodeChanged(PropertyChangeEvent evt) {
		consumer.onGraphNodePropertyChanged(node, evt.getPropertyName(), evt
				.getOldValue(), evt.getNewValue());
	}

	private void onGraphChanged(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Graph.PROP_CONNECTION_ADDED)) {			
			IConnection conn = (IConnection) evt.getNewValue();
			if (conn.getSource() == node) {
				// Ein Kind wurde zu dem beobachteten Knoten
				// hinzugefuegt!
				consumer.onGraphChildAdded(node, conn.getTarget());
			} else if (conn.getTarget() == node) {
				// Ein Vater wurde zu dem beobachteten Knoten hinzugefuegt!
				consumer.onGraphParentAdded(node, conn.getSource());
			}
		} else if (evt.getPropertyName().equals(Graph.PROP_CONNECTION_REMOVED)) {
			IConnection conn = (IConnection) evt.getOldValue();
			if (conn.getSource() == node) {
				// Ein Kind wurde von dem beobachteten Knoten entfernt!
				consumer.onGraphChildRemoved(node, conn.getTarget());
			} else if (conn.getTarget() == node) {
				consumer.onGraphParentRemoved(node, conn.getSource());
			}
		}
	}
}
