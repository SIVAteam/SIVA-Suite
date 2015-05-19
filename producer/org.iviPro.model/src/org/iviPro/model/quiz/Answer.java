package org.iviPro.model.quiz;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zum Verwalten einer Antwort.
 * 
 * @author Sabine Gattermann
 * @modified Stefan Zwicklbauer
 * 
 * @uml.dependency supplier="org.iviPro.model.quiz"
 */
public class Answer extends IQuizBean {

	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property name="idAnswer"
	 */
	private int idAnswer;

	/**
	 * @uml.property name="answerText"
	 */
	private String answerText;

	/**
	 * @uml.property name="positionAnswer"
	 */
	private int positionAnswer;

	/**
	 * @uml.property name="iscorrect"
	 */
	private boolean iscorrect;

	/**
	 * @uml.property name="idNode"
	 */
	private int idNode;

	/**
	 * Konstruktor
	 * 
	 * @param idAnswer
	 *            Die ID.
	 * @param answerText
	 *            Der Text.
	 * @param positionAnswer
	 *            Die Position.
	 * @param isCorrect
	 *            Die Korrektheit.
	 * @param idNode
	 *            Die ID des Knotens.
	 */
	public Answer(Project project, int idAnswer, String answerText,
			int positionAnswer, boolean isCorrect, int idNode) {
		super(project);
		this.idAnswer = idAnswer;
		this.answerText = answerText;
		this.positionAnswer = positionAnswer;
		this.iscorrect = isCorrect;
		this.idNode = idNode;

	}

	/**
	 * Konstruktor
	 * 
	 * @param answerText
	 *            Der Text.
	 * @param positionAnswer
	 *            Die Position.
	 * @param isCorrect
	 *            Die Korrektheit.
	 * @param idKnoten
	 *            Die ID des Knotens.
	 */
	public Answer(Project project, String answerText, int positionAnswer,
			boolean isCorrect, int idKnoten) {
		super(project);
		this.idAnswer = -1;
		this.answerText = answerText;
		this.positionAnswer = positionAnswer;
		this.iscorrect = isCorrect;
		this.idNode = idKnoten;

	}

	/**
	 * Getter fuer Antwort-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdAnswer() {
		return idAnswer;
	}

	/**
	 * Setter fuer Antwort-ID.
	 * 
	 * @param idAnswer
	 *            Die ID.
	 */
	public void setIdAnswer(int idAnswer) {
		this.idAnswer = idAnswer;
	}

	/**
	 * Getter fuer AntwortText.
	 * 
	 * @return Der Text.
	 */
	public String getAnswerText() {
		return answerText;
	}

	/**
	 * Setter fuer AntwortText.
	 * 
	 * @param answerText
	 *            Der Text.
	 */
	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

	/**
	 * Getter fuer Position.
	 * 
	 * @return Die Position.
	 */
	public int getPositionAnswer() {
		return positionAnswer;
	}

	/**
	 * Setter fuer Position.
	 * 
	 * @param positionAnswer
	 *            Die Position.
	 */
	public void setPositionAnswer(int positionAnswer) {
		this.positionAnswer = positionAnswer;
	}

	/**
	 * Getter fuer Korrektheit.
	 * 
	 * @return true, falls korrekt, false sonst
	 */
	public boolean getIsCorrect() {
		return iscorrect;
	}

	/**
	 * Setter fuer Korrektheit.
	 * 
	 * @param isCorrect
	 *            true, alls korrekt, false sonst.
	 */
	public void setIsCorrect(boolean isCorrect) {
		this.iscorrect = isCorrect;
	}

	/**
	 * Setter fuer Knoten-ID.
	 * 
	 * @param idNode
	 *            Die ID.
	 */
	public void setIdNode(int idNode) {
		this.idNode = idNode;
	}

	/**
	 * Getter fuer Knoten-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdNode() {
		return idNode;
	}

}
