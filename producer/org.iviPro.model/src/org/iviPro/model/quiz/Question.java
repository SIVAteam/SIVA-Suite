package org.iviPro.model.quiz;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zur Verwaltung einer Frage.
 * 
 * @author Sabine Gattermann
 * @modifed Stefan Zwicklbauer
 * 
 * @uml.dependency supplier="org.iviPro.model.quiz"
 */
public class Question extends IQuizBean {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @uml.property name="idQuestion"
	 */
	private int idQuestion;

	/**
	 * @uml.property name="QuestionText"
	 */
	private String questionText;

	/**
	 * Konstruktor.
	 * 
	 * @param idQuestion
	 *            Die Frage-ID.
	 * @param questionText
	 *            Der Fragetext.
	 */
	public Question(Project project, int idQuestion, String questionText) {
		super(project);
		this.idQuestion = idQuestion;
		this.questionText = questionText;
	}

	/**
	 * Konstruktor.
	 * 
	 * @param questionText
	 *            Der Fragetext.
	 */
	public Question(Project project, String questionText) {
		super(project);
		this.idQuestion = -1;
		this.questionText = questionText;
	}

	/**
	 * Standard-Konstruktor.
	 */
	public Question(Project project) {
		super(project);
		this.idQuestion = -1;
		this.questionText = "";
	}

	/**
	 * Getter fuer Frage-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdQuestion() {
		return idQuestion;
	}

	/**
	 * Setter fuer Frage-ID.
	 * 
	 * @param idQuestion
	 *            Die ID.
	 */
	public void setIdQuestion(int idQuestion) {
		this.idQuestion = idQuestion;
	}

	/**
	 * Getter fuer Fragetext.
	 * 
	 * @return Der Text.
	 */
	public String getQuestionText() {
		return questionText;
	}

	/**
	 * Setter fuer Fragetext.
	 * 
	 * @param questionText
	 *            Der Text.
	 */
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

}
