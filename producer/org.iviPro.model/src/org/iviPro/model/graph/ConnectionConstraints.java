package org.iviPro.model.graph;

import java.io.Serializable;

/**
 * Diese Klasse definiert die Beschraenkungen fuer Verbindungen eines Knotens
 * mit anderen Knoten.
 * 
 * @author dellwo
 * 
 */
public class ConnectionConstraints implements Serializable {

	/** Minimal erforderliche Gesamtzahl an ausgehenden Verbindungen. */
	private int minRequiredConnections;

	/** Maximal zulaessige Gesamtzahl an ausgehenden Verbindungen. */
	private int maxAllowedConnections;

	/**
	 * Array mit den ConnectionTargetDefinitions fuer diesen Knoten: Damit wird
	 * definiert, mit welchen anderen Knoten dieser Knoten verbunden werden
	 * darf.
	 */
	private ConnectionTargetDefinition[] connectionTargetDefinitions;

	/**
	 * Erstellt eine neue Beschraenkung fuer Verbindungen eines Knotens mit
	 * einem anderen Knoten.
	 * 
	 * @param minRequiredConnections
	 *            Minimal erforderliche Gesamtzahl an ausgehenden Verbindungen.
	 * @param maxAllowedConnections
	 *            Maximal zulaessige Gesamtzahl an ausgehenden Verbindungen.
	 * @param connectionTargetDefinitions
	 *            Menge an Ziel-Definitionen. Damit wird definiert, mit welchen
	 *            anderen Knoten dieser Knoten verbunden werden darf.
	 */
	public ConnectionConstraints(int minRequiredConnections,
			int maxAllowedConnections,
			ConnectionTargetDefinition[] connectionTargetDefinitions) {
		super();
		this.minRequiredConnections = minRequiredConnections;
		this.maxAllowedConnections = maxAllowedConnections;
		this.connectionTargetDefinitions = connectionTargetDefinitions;
	}

	/**
	 * Returns the most specific valid {@link ConnectionTargetDefinition} for the
	 * given <code>Class</code> parameter. Returns <code>null</code> if no definiton
	 * has been defined for the given <code>Class</code>.
	 * 
	 * @param targetClass <code>Class</code> of the target node for which 
	 * 						a definition is requested
	 * @return <code>ConnectionTargetDefinition</code> for the given 
	 * 			<code>Class</code>parameter
	 */
	public ConnectionTargetDefinition getTargetDefinition(
			Class<? extends IGraphNode> targetClass) {
		ConnectionTargetDefinition mostSpecificDef = null;
		for (ConnectionTargetDefinition definition : connectionTargetDefinitions) {
			if (definition.getTargetClass().isAssignableFrom(targetClass)) {
				// Check if the definition definition is more specific 
				if ((mostSpecificDef == null) 
						|| (mostSpecificDef.getTargetClass()
								.isAssignableFrom(definition.getTargetClass()))) {
					mostSpecificDef = definition;
				}
				
			}
		}
		return mostSpecificDef;
	}

	public int getMinRequiredConnections() {
		return minRequiredConnections;
	}

	public int getMaxAllowedConnections() {
		return maxAllowedConnections;
	}

	public ConnectionTargetDefinition[] getConnectionTargetDefinitions() {
		return connectionTargetDefinitions;
	}

}
