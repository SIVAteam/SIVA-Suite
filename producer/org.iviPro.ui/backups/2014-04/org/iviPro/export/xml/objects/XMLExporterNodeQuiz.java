package org.iviPro.export.xml.objects;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.quiz.Answer;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterNodeQuiz extends IXMLExporter {

	XMLExporterNodeQuiz(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeQuiz.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {
		NodeQuiz quiz = (NodeQuiz) exportObj;
		List<NodeQuizControl> defaultControlList = quiz.getDefaultControl();
		String type = quiz.getButtonType().toString().toLowerCase();

		// Dann Label fuer Control exportieren
		String labelID = createTitleLabels(quiz, doc, idManager);

		Element actions = getActions(doc);
//		Element showSelControl = doc.createElement(TAG_SHOWSELECTIONCONTROL);
//		showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TYPE, type);
//		showSelControl.setAttribute(ATTR_ACTIONID, idManager.getActionID(quiz));
//		showSelControl.setAttribute(ATTR_REF_RES_ID, labelID);

//		if (defaultControl != null) {
//			showSelControl.setAttribute(
//					ATTR_SHOWSELECTIONCONTROL_DEFAULTCONTROL,
//					idManager.getID(defaultControl));
//			Date timeout = new Date(quiz.getTimeout() * 1000);
//			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
//			showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TIMEOUT,
//					"00:" + dateFormat.format(timeout));
//		}

//		actions.appendChild(showSelControl);

		Element posInfo = createPositionInfo(quiz, doc, idManager, project);
//		showSelControl.appendChild(posInfo);

		List<IGraphNode> controls = quiz.getChildren(NodeQuizControl.class);
		for (IGraphNode controlNode : controls) {
			NodeQuizControl control = (NodeQuizControl) controlNode;

			if (control.getChildren().size() == 0) {
				// Control hat keinen Nachfolger => Fehler
				throw new ExportException("Fork control '" + control.getTitle()
						+ "' has no successor.");
			}

			// Zuerst mit diesem Control verbundenen Knoten exportieren
			IGraphNode successor = control.getChildren().get(0);
			IXMLExporter exporter = ExporterFactory.createExporter(successor);
			exporter.exportObject(doc, idManager, project, alreadyExported);

			// Dann Label fuer Control exportieren
//			String controlLabelRefResID = createTitleLabels(control, doc,
//					idManager);
//			String controlImageRefResID = createImageButton(control, doc,
//					idManager);

			// Dann Control selbst exportieren
//			Element controlElement = doc.createElement(TAG_CONTROL);
//			String controlID = idManager.getID(control);
//			controlElement.setAttribute(ATTR_CONTROL_ID, controlID);
//			String controlActionID = idManager.getActionID(successor);
//			controlElement.setAttribute(ATTR_REF_ACTION_ID, controlActionID);
//			controlElement.setAttribute(ATTR_REF_RES_ID, controlLabelRefResID);
//			if (controlImageRefResID != null) {
//				controlElement.setAttribute(ATTR_REF_RES_ID_SECONDARY,
//						controlImageRefResID);
//			}

//			showSelControl.appendChild(controlElement);
		}
		createVideoRessourceEntry(quiz, doc, idManager, project);
		createVideoActionEntry(quiz, doc, idManager, project, defaultControlList);
		
	}

	@Override
	protected ExportParameters getExportParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	private String createImageButton(NodeQuizControl selectionControl,
			Document doc, IDManager idManager) throws ExportException {

		String resID = idManager.getRessourceID(selectionControl);
		// Ressource-Element anlegen.
		Element imgResource = doc.createElement(TAG_IMAGE);
		imgResource.setAttribute(ATTR_RES_ID, resID);

		String fileObject = selectionControl.getMinValue()+"-"+selectionControl.getMaxValue();
		return fileObject;
	}

	/**
	 * Erstellt Label-Einträge in der Ressourcen-Liste fuer das aktuelle Quiz
	 * des Knotens
	 * 
	 * 
	 * @param nodeScene
	 *            Szenen-Knoten fuer den der Eintrag erstellt wird
	 * @param doc
	 *            XML-Dokument in dem der Eintrag gemacht werden soll
	 * @param videoID
	 *            Die Video-ID im XML-Dokument
	 * @param videoFilename
	 *            Der Dateiname der Video-Ressource
	 * @throws ExportException
	 *             Falls beim Export in das XML-Dokument ein Fehler auftritt.
	 */
	private void createVideoRessourceEntry(NodeQuiz nodeQuiz, Document doc,
			IDManager idManager, Project project) throws ExportException {

		if (nodeQuiz.getTestId() == -1) {
			return;
		}

		// QuizTitel Ressource
		Element quizTitleLabel = doc.createElement("label");
		quizTitleLabel.setAttribute("resID", idManager.getID(nodeQuiz));
		quizTitleLabel.setIdAttribute("resID", true);

		for (Locale language : project.getLanguages()) {
			String curLangCode = LocalizedString.getSivaLangcode(language);
			Element content = doc.createElement(TAG_CONTENT);
			content.setAttribute(ATTR_REF_LANGCODE, curLangCode);
			content.setTextContent(DbQueries.getTestData(nodeQuiz.getTestId())
					.getTitle());
			quizTitleLabel.appendChild(content);
		}
		Element ressources = getRessources(doc);
		ressources.appendChild(quizTitleLabel);

		// Questions/Answer Ressource
		List<Node> nodeList = DbQueries.getNodeListByTest(nodeQuiz.getTestId());
		for (int i = nodeList.size() - 1; i >= 0; i--) {
			Node node = nodeList.get(i);
			Element quizQuestionLabel = doc.createElement("label");
			quizQuestionLabel.setAttribute("resID", idManager.getID(nodeQuiz)+ "_Question_" + (node.getPosition() + 1));
			quizQuestionLabel.setIdAttribute("resID", true);

			for (Locale language : project.getLanguages()) {
				String curLangCode = LocalizedString.getSivaLangcode(language);
				Element content = doc.createElement(TAG_CONTENT);
				content.setAttribute(ATTR_REF_LANGCODE, curLangCode);
				content.setTextContent(DbQueries.getQuestionByNode(node)
						.getQuestionText());
				quizQuestionLabel.appendChild(content);
			}
			ressources.appendChild(quizQuestionLabel);

			List<Answer> answerList = DbQueries.getAnswerByNode(node);
			for (Iterator<Answer> iterator2 = answerList.iterator(); iterator2
					.hasNext();) {
				Answer answer = (Answer) iterator2.next();
				Element quizAnswerLabel = doc.createElement("label");
				quizAnswerLabel.setAttribute("resID", idManager.getID(nodeQuiz) + "_Question_"+(node.getPosition() + 1)+"_Answer_"+answer.getPositionAnswer());
				quizAnswerLabel.setIdAttribute("resID", true);

				for (Locale language : project.getLanguages()) {
					String curLangCode = LocalizedString
							.getSivaLangcode(language);
					Element content = doc.createElement(TAG_CONTENT);
					content.setAttribute(ATTR_REF_LANGCODE, curLangCode);
					content.setTextContent(answer.getAnswerText());
					quizAnswerLabel.appendChild(content);
				}
				ressources.appendChild(quizAnswerLabel);
			}
		}
	}

	private void createVideoActionEntry(NodeQuiz nodeQuiz, Document doc,
			IDManager idManager, Project project, List<NodeQuizControl> controlList) throws ExportException {

		if (nodeQuiz.getTestId() == -1) {
			return;
		}

		Element actions = getActions(doc);
		Element showQuizLinear = doc.createElement(TAG_QUIZ_LINEAR);
		showQuizLinear.setAttribute("REFresID", idManager.getID(nodeQuiz));
		showQuizLinear
				.setAttribute("actionID", idManager.getID(showQuizLinear));
		Element path = doc.createElement(TAG_QUIZ_PATH);

		Element point = doc.createElement(TAG_QUIZ_POINT);
		point.setAttribute(TAG_QUIZ_YPOS, "-1.0");
		point.setAttribute(TAG_QUIZ_XPOS, "-1.0");
		point.setAttribute(TAG_QUIZ_YSIZE, "-1.0");
		point.setAttribute(TAG_QUIZ_XSIZE, "-1.0");
		point.setAttribute(TAG_QUIZ_TIME, "00:00:00.000");
		path.appendChild(point);
		showQuizLinear.appendChild(path);

		// Testproperties
		Element testProperties = doc.createElement(TAG_QUIZ_TESTPROPERTIES);
		testProperties.setAttribute(TAG_QUIZ_TIMEOFFEEDBACK, DbQueries
				.getTestData(nodeQuiz.getTestId()).getTimeOfFeedbackString());
		testProperties.setAttribute(TAG_QUIZ_MAXPOINTS, Integer
				.toString(DbQueries.getTestData(nodeQuiz.getTestId())
						.getMaxPoints()));
		showQuizLinear.appendChild(testProperties);
		
		// TaskList
		Element taskList = doc.createElement(TAG_QUIZ_TASKLIST);
		List<Node> nodeList = DbQueries.getNodeListByTest(nodeQuiz.getTestId());
		for (int i = nodeList.size() - 1; i >= 0; i--) {
			Node node = nodeList.get(i);
			Element task = doc.createElement(TAG_QUIZ_TASK);
			task.setAttribute("REFresID", idManager.getID(nodeQuiz)+"_Question_" + (node.getPosition() + 1));
			task.setAttribute("random", Boolean.toString(node.isRandom()));
			task.setAttribute("points", Integer.toString(node.getPoints()));
			task.setAttribute("taskId", "TID_"+Integer.toString(node.getIdTest())+"_"+(node.getPosition() + 1));
			
			List<Answer> answerList = DbQueries.getAnswerByNode(node);
			for (int j = 0; j < answerList.size() ; j++) {
				Answer answer = answerList.get(j);
				Element answerAlement = doc.createElement(TAG_QUIZ_ANSWER);
				answerAlement.setAttribute("REFresID", idManager.getID(nodeQuiz) + "_Question_"+(node.getPosition() + 1)+"_Answer_"+answer.getPositionAnswer());
				answerAlement.setAttribute("position", Integer.toString(j));
				answerAlement.setAttribute("answerID", "AID"+idManager.getID(nodeQuiz)+"_"+(node.getPosition() + 1)+"_"+answer.getPositionAnswer());
				answerAlement.setAttribute("isCorrect", Boolean.toString(answer.getIsCorrect()));
				task.appendChild(answerAlement);
			}
			taskList.appendChild(task);
		}
		Element pointRanges = doc.createElement(TAG_QUIZ_POINTRANGE);

		for (int i = 0; i < controlList.size(); i++) {
			Element range = doc.createElement(TAG_QUIZ_RANGE);
			//String split[] = controlList.get(i).getAmountPoints().split("-");
			range.setAttribute("minValue", Integer.toString(controlList.get(i).getMinValue()));
			range.setAttribute("maxValue", Integer.toString(controlList.get(i).getMaxValue()));
			IGraphNode successor = controlList.get(i).getChildren().get(0);
			range.setAttribute("REFactionID", idManager.getActionID(successor));
			range.setAttribute("rangeID", "RangeID_"+idManager.getID(nodeQuiz)+"_"+idManager.getID(controlList.get(i)));
			pointRanges.appendChild(range);
		}

		showQuizLinear.appendChild(taskList);
		showQuizLinear.appendChild(pointRanges);
		actions.appendChild(showQuizLinear);
	}
}
