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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for TestNodeType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TestNodeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="questionText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="answerList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="answer" type="{}TestAnswerType" maxOccurs="unbounded" minOccurs="2"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="succeedingTaskList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="succeedingTask" type="{}TestEdgeType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="additionalInformationList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="additionalInformation" type="{}TestAdditionalInfoType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/all>
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="position" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="points" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="random" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestNodeType", propOrder = {

})
public class TestNodeType {

    @XmlElement(required = true)
    protected String questionText;
    @XmlElement(required = true)
    protected TestNodeType.AnswerList answerList;
    @XmlElement(required = true)
    protected TestNodeType.SucceedingTaskList succeedingTaskList;
    @XmlElement(required = true)
    protected TestNodeType.AdditionalInformationList additionalInformationList;
    @XmlAttribute(name = "Id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(required = true)
    protected int position;
    @XmlAttribute(required = true)
    protected int points;
    @XmlAttribute(required = true)
    protected boolean random;

    /**
     * Gets the value of the questionText property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getQuestionText() {
	return questionText;
    }

    /**
     * Sets the value of the questionText property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setQuestionText(String value) {
	this.questionText = value;
    }

    /**
     * Gets the value of the answerList property.
     * 
     * @return possible object is {@link TestNodeType.AnswerList }
     * 
     */
    public TestNodeType.AnswerList getAnswerList() {
	return answerList;
    }

    /**
     * Sets the value of the answerList property.
     * 
     * @param value
     *            allowed object is {@link TestNodeType.AnswerList }
     * 
     */
    public void setAnswerList(TestNodeType.AnswerList value) {
	this.answerList = value;
    }

    /**
     * Gets the value of the succeedingTaskList property.
     * 
     * @return possible object is {@link TestNodeType.SucceedingTaskList }
     * 
     */
    public TestNodeType.SucceedingTaskList getSucceedingTaskList() {
	return succeedingTaskList;
    }

    /**
     * Sets the value of the succeedingTaskList property.
     * 
     * @param value
     *            allowed object is {@link TestNodeType.SucceedingTaskList }
     * 
     */
    public void setSucceedingTaskList(TestNodeType.SucceedingTaskList value) {
	this.succeedingTaskList = value;
    }

    /**
     * Gets the value of the additionalInformationList property.
     * 
     * @return possible object is {@link TestNodeType.AdditionalInformationList }
     * 
     */
    public TestNodeType.AdditionalInformationList getAdditionalInformationList() {
	return additionalInformationList;
    }

    /**
     * Sets the value of the additionalInformationList property.
     * 
     * @param value
     *            allowed object is
     *            {@link TestNodeType.AdditionalInformationList }
     * 
     */
    public void setAdditionalInformationList(
	    TestNodeType.AdditionalInformationList value) {
	this.additionalInformationList = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getId() {
	return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setId(String value) {
	this.id = value;
    }

    /**
     * Gets the value of the position property.
     * 
     */
    public int getPosition() {
	return position;
    }

    /**
     * Sets the value of the position property.
     * 
     */
    public void setPosition(int value) {
	this.position = value;
    }

    /**
     * Gets the value of the points property.
     * 
     */
    public int getPoints() {
	return points;
    }

    /**
     * Sets the value of the points property.
     * 
     */
    public void setPoints(int value) {
	this.points = value;
    }

    /**
     * Gets the value of the random property.
     * 
     */
    public boolean isRandom() {
	return random;
    }

    /**
     * Sets the value of the random property.
     * 
     */
    public void setRandom(boolean value) {
	this.random = value;
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
     *         &lt;element name="additionalInformation" type="{}TestAdditionalInfoType" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "additionalInformation" })
    public static class AdditionalInformationList {

	protected List<TestAdditionalInfoType> additionalInformation;

	/**
	 * Gets the value of the additionalInformation property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list
	 * will be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the additionalInformation property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAdditionalInformation().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link TestAdditionalInfoType }
	 * 
	 * 
	 */
	public List<TestAdditionalInfoType> getAdditionalInformation() {
	    if (additionalInformation == null) {
		additionalInformation = new ArrayList<TestAdditionalInfoType>();
	    }
	    return this.additionalInformation;
	}

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
     *         &lt;element name="answer" type="{}TestAnswerType" maxOccurs="unbounded" minOccurs="2"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "answer" })
    public static class AnswerList {

	@XmlElement(required = true)
	protected List<TestAnswerType> answer;

	/**
	 * Gets the value of the answer property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list
	 * will be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the answer property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAnswer().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link TestAnswerType }
	 * 
	 * 
	 */
	public List<TestAnswerType> getAnswer() {
	    if (answer == null) {
		answer = new ArrayList<TestAnswerType>();
	    }
	    return this.answer;
	}

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
     *         &lt;element name="succeedingTask" type="{}TestEdgeType" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "succeedingTask" })
    public static class SucceedingTaskList {

	protected List<TestEdgeType> succeedingTask;

	/**
	 * Gets the value of the succeedingTask property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list
	 * will be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the succeedingTask property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSucceedingTask().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link TestEdgeType }
	 * 
	 * 
	 */
	public List<TestEdgeType> getSucceedingTask() {
	    if (succeedingTask == null) {
		succeedingTask = new ArrayList<TestEdgeType>();
	    }
	    return this.succeedingTask;
	}

    }

}
