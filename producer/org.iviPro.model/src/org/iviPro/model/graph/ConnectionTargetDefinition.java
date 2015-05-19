package org.iviPro.model.graph;

import java.io.Serializable;

/**
 * Diese Klasse definiert ein Ziel von ausgehenden Verbindungen eines Knotens
 * und deren Beschraenkungen (minimale/maximale Anzahl an solchen Verbindungen).
 * 
 * @author dellwo
 * 
 */
public class ConnectionTargetDefinition implements Serializable {

	/** Klasse des Ziel-Knotens */
	private final Class<? extends IGraphNode> targetClass;

	/**
	 * Minimal erforderliche Anzahl an Verbindungen zu Knoten der Zielklasse.
	 * Nur fuer restriktierte Ziele relevant.
	 */
	private final int minRequiredConnections;

	/**
	 * Maximal zulaessige Anzahl an Verbindungen zu Knoten der Zielklasse. Nur
	 * fuer restriktierte Ziele relevant.
	 */
	private final int maxAllowedConnections;

	/**
	 * Gibt an, ob das Ziel restriktiert ist oder nicht. Zu unrestriktierten
	 * Zielen duerfen beliebig viele Verbindungen aufgebaut werden und sie
	 * beeinflussen nicht die maximale Gesamt-Anzahl von Verbindungen eines
	 * Knotens.
	 */
	private final boolean unrestricted;

	/**
	 * Erstellt eine neue restriktierte Ziel-Definition. Zu diesem Ziel muss
	 * eine bestimmte Mindest-Anzahl an erforderlichen Verbindungen aufgebaut
	 * werden und es darf eine bestimmte Maximalzahl an Verbindungen nicht
	 * ueberschritten werden.
	 * 
	 * @param targetClass
	 *            Klasse der Ziel-Knoten
	 * @param minRequiredConnections
	 *            Minimal erforderliche Anzahl an Verbindungen zu Knoten der
	 *            Zielklasse.
	 * @param maxAllowedConnections
	 *            Maximal zulaessige Anzahl an Verbindungen zu Knoten der
	 *            Zielklasse.
	 */
	public ConnectionTargetDefinition(Class<? extends IGraphNode> targetClass,
			int minRequiredConnections, int maxAllowedConnections) {
		super();
		this.targetClass = targetClass;
		this.minRequiredConnections = minRequiredConnections;
		this.maxAllowedConnections = maxAllowedConnections;
		this.unrestricted = false;
	}

	/**
	 * Erstellt eine neue unrestriktierte Ziel-Definition. Zu diesem Ziel
	 * duerfen beliebig viele Verbindungen aufgebaut werden und diese
	 * Verbindungen beeinflussen nicht die maximale Gesamt-Anzahl von
	 * Verbindungen eines Knotens.
	 * 
	 * @param targetClass
	 *            Klasse der Ziel-Knoten.
	 */
	public ConnectionTargetDefinition(Class<? extends IGraphNode> targetClass) {
		super();
		this.targetClass = targetClass;
		this.minRequiredConnections = -1;
		this.maxAllowedConnections = -1;
		this.unrestricted = true;
	}

	/**
	 * Gibt die Klasse der Zielknoten dieser Ziel-Definition zurueck.
	 * 
	 * @return Klasse der Zielknoten dieser Ziel-Definition.
	 */
	public Class<? extends IGraphNode> getTargetClass() {
		return targetClass;
	}

	/**
	 * Gibt - bei restriktierten Zielen - die minimal erforderliche Anzahl an
	 * Verbindungen zu Knoten der Zielklasse zurueck. Bei unrestriktierten
	 * Zielen hat dieser Wert keine Bedeutung und es wird immer -1
	 * zurueckgegeben.
	 * 
	 * @return Minimal erforderliche Anzahl an Verbindungen zu Knoten der
	 *         Zielklasse.
	 */
	public int getMinRequiredConnections() {
		return minRequiredConnections;
	}

	/**
	 * Gibt - bei restriktierten Zielen - die maximal zulaessige Anzahl an
	 * Verbindungen zu Knoten der Zielklasse zurueck. Bei unrestriktierten
	 * Zielen hat dieser Wert keine Bedeutung und es wird immer -1
	 * zurueckgegeben.
	 * 
	 * @return Maximal zulaessige Anzahl an Verbindungen zu Knoten der
	 *         Zielklasse.
	 */
	public int getMaxAllowedConnections() {
		return maxAllowedConnections;
	}

	/**
	 * Gibt an, ob diese Ziel-Definition unrestriktiert ist. Zu unrestriktierten
	 * Zielen duerfen beliebig viele Verbindungen aufgebaut werden und diese
	 * Verbindungen beeinflussen nicht die maximale Gesamt-Anzahl von
	 * Verbindungen eines Knotens.
	 * 
	 * @return True, falls unrestriktiert, ansonsten false.
	 */
	public boolean isUnrestricted() {
		return unrestricted;
	}

}
