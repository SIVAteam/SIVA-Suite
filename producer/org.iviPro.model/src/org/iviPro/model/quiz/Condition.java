package org.iviPro.model.quiz;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zum Verwalten einer Bedingung.
 * 
 * @author Sabine Gattermann
 * @modified Stefan Zwicklbauer
 * 
 * @uml.dependency supplier="org.iviPro.model.quiz"
 */
public class Condition extends IQuizBean {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property name="ConditionId"
	 */
	private int idCondition;
	
	/**
	 * Die punkte der bedingung
	 * 
	 * @uml.property name="ConditionPoints"
	 */
	private int conditionPoints;
	
	/**
	 * @uml.property name="ConditionLoopback"
	 */
	private int conditionLookback;

	/**
	 * Konstruktor
	 * 
	 * @param idCondition
	 *            Die Bedingungs-ID.
	 * @param conditionPoints
	 *            Die Bedingung (in Punkten).
	 * @param conditionLookback
	 *            Der Lookback.
	 */
	public Condition(Project project, int idCondition, int conditionPoints, int conditionLookback) {
		super(project);
		this.idCondition = idCondition;
		this.conditionPoints = conditionPoints;
		this.conditionLookback = conditionLookback;
	}

	/**
	 * Konstruktor
	 * 
	 * @param conditionPoints
	 *            Die Bedingung.
	 * @param conditionLookback
	 *            Der Lookback.
	 */
	public Condition(Project project, int conditionPoints, int conditionLookback) {
		super(project);
		this.idCondition = -1;
		this.conditionPoints = conditionPoints;
		this.conditionLookback = conditionLookback;
	}

	/**
	 * Standard-Konstruktor.
	 */
	public Condition(Project project) {
		super(project);
		this.idCondition = -1;
		this.conditionPoints = 0;
		this.conditionLookback = 0;
	}

	/**
	 * Getter fuer Bedingungs-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdCondition() {
		return idCondition;
	}

	/**
	 * Setter fuer Bedingungs-ID.
	 * 
	 * @param idCondition
	 *            Die ID.
	 */
	public void setIdCondition(int idCondition) {
		this.idCondition = idCondition;
	}

	/**
	 * Getter fuer Bedingung (in Punkten).
	 * 
	 * @return Die Bedingung.
	 */
	public int getConditionPoints() {
		return conditionPoints;
	}

	/**
	 * Setter fuer Bedingung (in Punkten)
	 * 
	 * @param conditionPoints
	 *            Die Bedingung.
	 */
	public void setConditionPoints(int conditionPoints) {
		this.conditionPoints = conditionPoints;
	}

	/**
	 * Getter fuer Lookback.
	 * 
	 * @return Der Lookback.
	 */
	public int getConditionLookback() {
		return conditionLookback;
	}

	/**
	 * Setter fuer Lookback.
	 * 
	 * @param conditionLookback
	 *            Der Lookback.
	 */
	public void setConditionLookback(int conditionLookback) {
		this.conditionLookback = conditionLookback;
	}

}
