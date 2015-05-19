package org.iviPro.editors.quiz.std;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.iviPro.application.Application;
import org.iviPro.editors.quiz.std.MultipleChoiceTestType.TaskList;
import org.iviPro.editors.quiz.std.TestNodeType.AdditionalInformationList;
import org.iviPro.editors.quiz.std.TestNodeType.AnswerList;
import org.iviPro.editors.quiz.std.TestNodeType.SucceedingTaskList;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.model.quiz.AdditionalInfo;
import org.iviPro.model.quiz.Answer;
import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.QuizException;
import org.iviPro.model.quiz.Test;

/**
 * Die Klasse ExportTest exportiert einen bestehenden Test. Die Struktur des
 * Tests wird dazu in ein XML-Dokument transformiert, ZuatzInfo-Objekte werden
 * ebenfalls exportiert.
 * 
 * @author Sabine Gattermann
 * 
 */
public class ExportTest {

	// JAXB Test-Objekt
	private MultipleChoiceTestType quiz;
	// Pfadvariablen
	private final String FILESEPERATOR = System.getProperty("file.separator");
	private final String MAINDIR = "Exportierte Tests";
	private String testpath;

	/**
	 * Konstruktor
	 */
	public ExportTest() {
	}

	/**
	 * Methode zum Exportieren eines Tests.
	 * 
	 * @param testname
	 *            Der Testtitel.
	 * @param newTestNameForExportDir
	 *            Der alternative Exporttitel.
	 * @return 0, falls erfolgreich, -1, falls Export-Ordner bereits existiert,
	 *         -2, falls JAXB-Fehler auftritt
	 */
	public int export(String dir, String testname,
			String newTestNameForExportDir) {
		this.quiz = generateXmlObjectToExport(testname);

		String dirName = testname;
		// alternativer Exportname?
		if (newTestNameForExportDir.length() != 0
				&& !newTestNameForExportDir.equals(":dooverwrite:")) {
			dirName = newTestNameForExportDir;
		}
		checkDir(MAINDIR);
		testpath = dir + FILESEPERATOR + dirName;

		if (!checkDir(testpath)) {
			if (!newTestNameForExportDir.equals(":dooverwrite:"))
				return -1;
		}

		String xmlPath = testpath + FILESEPERATOR + testname + ".xml";

		// XML Erzeugen
		File file = new File(xmlPath);
		// FileWriter erzeugen.
		try {
			FileWriter fw = new FileWriter(file);
			JAXBContext jc;

			try {
				jc = JAXBContext.newInstance("exportTest.jaxb");

				Marshaller marshaller = jc.createMarshaller();

				marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");

				marshaller.marshal(quiz, fw);

			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			fw.flush();
			fw.close();
		} catch (IOException e) {
			return -2;
		}

		// Exportieren der ZusatzInfos
		if (exportAdditionalInfosFromDB(testname) == false)
			return -2;

		return 0;
	}

	/**
	 * Methode zum Erstellen des JAXB-Test-Objekts
	 * 
	 * @param testName
	 *            Der Testtitel.
	 * @return Das JAXB-TestObjekt.
	 */
	private MultipleChoiceTestType generateXmlObjectToExport(String testName) {
		MultipleChoiceTestType xmlTest = new MultipleChoiceTestType();
		Test test = DbQueries.getTestData(testName);

		xmlTest.setTestProperties(generateTestProperties(test));

		xmlTest.setTaskList(generateNodeList(test, xmlTest));

		return xmlTest;

	}

	/**
	 * Methode zum Erstellen der KnotenListe fuer das JAXB-Test-Objekt.
	 * 
	 * @param test
	 *            Der Test.
	 * @param quiz
	 *            Das JAXB-Test-Objekt.
	 * @return Die JAXB-Knotenliste.
	 */
	private TaskList generateNodeList(Test test, MultipleChoiceTestType quiz) {
		LinkedList<Node> nodeList = DbQueries.getNodeListByTest(test
				.getIdTest());
		TaskList xmlNodeList = new TaskList();

		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			TestNodeType xmlNode = new TestNodeType();
			xmlNode.setPosition(node.getPosition());
			xmlNode.setQuestionText(parseNewLineToHTML(DbQueries
					.getQuestionByNode(node).getQuestionText()));
			xmlNode.setId("ID_" + String.valueOf(node.getIdNode()));
			xmlNode.setPoints(node.getPoints());
			xmlNode.setAnswerList(generateAnswerList(node, xmlNode));
			xmlNode.setSucceedingTaskList(generateEdgeList(node, xmlNode));
			xmlNode.setAdditionalInformationList(generateAdditionalInfoList(
					node, xmlNode));
			xmlNode.setRandom(node.isRandom());

			xmlNodeList.getTask().add(xmlNode);

		}

		return xmlNodeList;
	}

	private String parseNewLineToHTML(String orgStr) {
		String parsedStr = "";
		parsedStr = orgStr.replace("\n", "<br>");
		return parsedStr;
	}

	/**
	 * Methode zum Erstellen der TestEigenschaften fuer das JAXB-Test-Objekt.
	 * 
	 * @param test
	 * @return Die JAXB-TestEigenschaften.
	 */
	private TestPropertiesType generateTestProperties(Test test) {

		TestPropertiesType xmlTestProperties = new TestPropertiesType();
		String author = DbQueries.getUsername(test.getIdUser());

		xmlTestProperties.setAuthor(author);
		xmlTestProperties.setEvaluationMethod(test.getEvaluationMethod());
		xmlTestProperties.setEmail(DbQueries.getUserData(author).getEmail());
		xmlTestProperties.setTimeOfFeedback(test.getTimeOfFeedback());
		xmlTestProperties.setCategory(test.getCategory());
		xmlTestProperties.setMaxPoints(test.getMaxPoints());
		xmlTestProperties.setTestTyp(test.getTestType());
		xmlTestProperties.setTitle(test.getTitle());

		return xmlTestProperties;
	}

	/**
	 * Methode zum Erstellen der ZusatzInfoListe eines JAXB-Knoten-Objekt.
	 * 
	 * @param node
	 *            Der Knoten.
	 * @param xmlNode
	 *            Der JAXB-Knoten.
	 * @return Die JAXB-ZusatzInfoListe.
	 */
	private AdditionalInformationList generateAdditionalInfoList(Node node,
			TestNodeType xmlNode) {
		AdditionalInformationList xmlAdditionalInfoList = new AdditionalInformationList();
		LinkedList<AdditionalInfo> aditionalInfoList = DbQueries
				.getMediaByNode(node);
		aditionalInfoList.addAll(DbQueries.getLinkByNode(node,
				Application.getCurrentProject()));

		for (int i = 0; i < aditionalInfoList.size(); i++) {
			AdditionalInfo additionalInfo = aditionalInfoList.get(i);
			TestAdditionalInfoType xmlAditionalInfo = new TestAdditionalInfoType();

			xmlAditionalInfo.setId("ID_"
					+ String.valueOf(additionalInfo.getIdAdditionalInfo()));
			xmlAditionalInfo.setMediaTyp(additionalInfo.getType());
			xmlAditionalInfo.setPosition(additionalInfo.getPosition());

			if (additionalInfo.getType() == 0) { // Links
				xmlAditionalInfo.setContent(additionalInfo.getAddress());
			} else if (additionalInfo.getType() >= 1) { // Dateien
				xmlAditionalInfo.setContent(additionalInfo.getMediaName());
			}

			xmlAdditionalInfoList.getAdditionalInformation().add(
					xmlAditionalInfo);
		}

		return xmlAdditionalInfoList;
	}

	/**
	 * Methode zum Erstellen der KantenListe eines JAXB-Knoten-Objekt.
	 * 
	 * @param node
	 *            Der Knoten.
	 * @param xmlNode
	 *            Der JAXB-Knoten.
	 * @return Die JAXB-KantenListe.
	 */
	private SucceedingTaskList generateEdgeList(Node node, TestNodeType xmlNode) {
		SucceedingTaskList xmlEdgeList = new SucceedingTaskList();
		LinkedList<Edge> edgeList = DbQueries.getEdgeListByNode(
				node.getIdNode(), true);

		for (int i = 0; i < edgeList.size(); i++) {
			Edge edge = edgeList.get(i);
			TestEdgeType xmlEdge = new TestEdgeType();

			xmlEdge.setTargetId("ID_"
					+ String.valueOf(edge.getIdNodeDestination()));
			Condition condition = DbQueries.getConditionData(edge
					.getIdCondition());
			xmlEdge.setConditionLookback(condition.getConditionLookback());
			xmlEdge.setConditionPoints(condition.getConditionPoints());

			xmlEdgeList.getSucceedingTask().add(xmlEdge);
		}

		return xmlEdgeList;
	}

	/**
	 * Methode zum Erstellen der AntwortListe eines JAXB-Knoten-Objekt.
	 * 
	 * @param node
	 *            Der Knoten.
	 * @param xmlNode
	 *            Der JAXB-Knoten.
	 * @return Die JAXB-AntwortListe.
	 */
	private AnswerList generateAnswerList(Node node, TestNodeType xmlNode) {
		AnswerList xmlAnswerList = new AnswerList();
		LinkedList<Answer> answerList = DbQueries.getAnswerByNode(node);

		for (int i = 0; i < answerList.size(); i++) {
			Answer answer = answerList.get(i);
			TestAnswerType xmlAnswer = new TestAnswerType();

			xmlAnswer.setAnswerText(answer.getAnswerText());
			xmlAnswer.setId("ID_" + String.valueOf(answer.getIdAnswer()));
			xmlAnswer.setIsCorrekt(answer.getIsCorrect());
			xmlAnswer.setPosition(answer.getPositionAnswer());

			xmlAnswerList.getAnswer().add(xmlAnswer);
		}

		return xmlAnswerList;
	}

	/**
	 * Erstellt den Ordner in den die Dateien des zu exportierene Test
	 * geschrieben werden. Falls der Ordner bereits existiert, wird abgebrochen.
	 * 
	 * @param name
	 *            Der Testtilel.
	 * @return true, falls der Ordner erstellt wurde, false, falls der Ornder
	 *         bereits existiert oder ein Fehler aufgetreten ist.
	 */
	private boolean checkDir(String name) {
		String dirName = name;
		File f = new File(dirName);
		if (f.isDirectory()) {
			// Test existiert --> ueberschreiben oder neuer name?
			if (!name.equals(MAINDIR)) {
				return false;
			}
		} else {
			f.mkdir();
		}
		return true;
	}

	/**
	 * Methode zum Exportieren von ZusatzInfo-Objekten aus der Datenbank.
	 * 
	 * @param testname
	 *            Der Testtitel.
	 */
	private boolean exportAdditionalInfosFromDB(String testname) {
		int idTest = DbQueries.getTestData(testname).getIdTest();
		LinkedList<Node> node = DbQueries.getNodeListByTest(idTest);

		// Links
		LinkedList<AdditionalInfo> links = new LinkedList<AdditionalInfo>();
		for (int i = 0; i < node.size(); i++) {
			links.addAll(DbQueries.getLinkByNode(node.get(i),
					Application.getCurrentProject()));
		}

		// Media
		LinkedList<AdditionalInfo> media = new LinkedList<AdditionalInfo>();
		for (int i = 0; i < node.size(); i++) {
			media.addAll(DbQueries.getMediaByNode(node.get(i)));
		}

		// Dateien werden direkt in der Klasse DbQueries erstellt
		for (int i = 0; i < media.size(); i++) {
			String filename = testpath + FILESEPERATOR
					+ media.get(i).getMediaName();
			try {
				DbQueries.getMediaFile(media.get(i).getMediaName(), filename);
			} catch (QuizException e) {
				QuizGenerator.errorDialog(e.getMessage());
			}
		}
		return true;
	}

}
