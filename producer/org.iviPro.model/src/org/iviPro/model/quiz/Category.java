package org.iviPro.model.quiz;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * 
 * @author Stefan Zwicklbauer
 * 
 * @uml.dependency supplier="org.iviPro.model.quiz"
 */
public class Category extends IQuizBean {

	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property name="CategoryId"
	 */
	private int id;
	
	/**
	 * @uml.property name="CategoryName"
	 */
	private String name;
	
	public Category(Project project, int id, String name) {
		super(project);
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
}
