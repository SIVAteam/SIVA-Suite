//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.10.07 at 11:01:35 AM MESZ 
//

package org.iviPro.editors.quiz.std;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="testProperties" type="{}TestPropertiesType"/>
 *         &lt;element name="taskList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="task" type="{}TestNodeType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "MultipleChoiceTestType")
public class MultipleChoiceTestType {

    @XmlElement(required = true)
    protected TestPropertiesType testProperties;
    @XmlElement(required = true)
    protected MultipleChoiceTestType.TaskList taskList;

    /**
     * Gets the value of the testProperties property.
     * 
     * @return possible object is {@link TestPropertiesType }
     * 
     */
    public TestPropertiesType getTestProperties() {
	return testProperties;
    }

    /**
     * Sets the value of the testProperties property.
     * 
     * @param value
     *            allowed object is {@link TestPropertiesType }
     * 
     */
    public void setTestProperties(TestPropertiesType value) {
	this.testProperties = value;
    }

    /**
     * Gets the value of the taskList property.
     * 
     * @return possible object is {@link MultipleChoiceTestType.TaskList }
     * 
     */
    public MultipleChoiceTestType.TaskList getTaskList() {
	return taskList;
    }

    /**
     * Sets the value of the taskList property.
     * 
     * @param value
     *            allowed object is {@link MultipleChoiceTestType.TaskList }
     * 
     */
    public void setTaskList(MultipleChoiceTestType.TaskList value) {
	this.taskList = value;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="task" type="{}TestNodeType" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "task" })
    public static class TaskList {

	@XmlElement(required = true)
	protected List<TestNodeType> task;

	/**
	 * Gets the value of the task property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list
	 * will be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the task property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getTask().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link TestNodeType }
	 * 
	 * 
	 */
	public List<TestNodeType> getTask() {
	    if (task == null) {
		task = new ArrayList<TestNodeType>();
	    }
	    return this.task;
	}

    }

}
