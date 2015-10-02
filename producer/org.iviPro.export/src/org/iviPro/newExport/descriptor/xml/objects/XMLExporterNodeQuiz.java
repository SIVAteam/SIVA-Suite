package org.iviPro.newExport.descriptor.xml.objects;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.quiz.Answer;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Node;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.descriptor.xml.IdManager.LabelType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterNodeQuiz extends IXMLExporter {

	private static final String ID_DELIMITER = "_"; //$NON-NLS-1$
	
	private NodeQuiz quiz;

	XMLExporterNodeQuiz(NodeQuiz exportObj) {
		super(exportObj);
		NodeQuiz quiz = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeQuiz.class;
	}

	// TODO: Use this method or delete/uncomment it...
	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {
		List<NodeQuizControl> defaultControlList = quiz.getDefaultControl();
		@SuppressWarnings("unused")
		String type = quiz.getButtonType().toString().toLowerCase();

		// Dann Label fuer Control exportieren
		@SuppressWarnings("unused")
		String labelID = createLabel(quiz, doc, idManager, 
				quiz.getTitles(), LabelType.TITLE);

		@SuppressWarnings("unused")
		Element actions = getActions(doc);
		// Element showSelControl = doc.createElement(TAG_SHOWSELECTIONCONTROL);
		// showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TYPE, type);
		// showSelControl.setAttribute(ATTR_ACTIONID,
		// idManager.getActionID(quiz));
		// showSelControl.setAttribute(ATTR_REF_RES_ID, labelID);

		// if (defaultControl != null) {
		// showSelControl.setAttribute(
		// ATTR_SHOWSELECTIONCONTROL_DEFAULTCONTROL,
		// idManager.getID(defaultControl));
		// Date timeout = new Date(quiz.getTimeout() * 1000);
		// SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
		// showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TIMEOUT,
		// "00:" + dateFormat.format(timeout));
		// }

		// actions.appendChild(showSelControl);
		@SuppressWarnings("unused")
		Element posInfo = createPositionInfo(quiz, doc, idManager, project);
		// showSelControl.appendChild(posInfo);

		List<IGraphNode> controls = quiz.getChildren(NodeQuizControl.class);
		for (IGraphNode controlNode : controls) {
			NodeQuizControl control = (NodeQuizControl) controlNode;

			if (control.getChildren().size() == 0) {
				throw new ExportException(String.format(
						"Fork control '%s' has no successor.", //$NON-NLS-1$
						control.getTitle()));
			}

			// Zuerst mit diesem Control verbundenen Knoten exportieren
			IGraphNode successor = control.getChildren().get(0);
			IXMLExporter exporter = ExporterFactory.createExporter(successor);
			exporter.exportObject(doc, idManager, project, alreadyExported);

			// Dann Label fuer Control exportieren
			// String controlLabelRefResID = createTitleLabels(control, doc,
			// idManager);
			// String controlImageRefResID = createImageButton(control, doc,
			// idManager);

			// Dann Control selbst exportieren
			// Element controlElement = doc.createElement(TAG_CONTROL);
			// String controlID = idManager.getID(control);
			// controlElement.setAttribute(ATTR_CONTROL_ID, controlID);
			// String controlActionID = idManager.getActionID(successor);
			// controlElement.setAttribute(ATTR_REF_ACTION_ID, controlActionID);
			// controlElement.setAttribute(ATTR_REF_RES_ID,
			// controlLabelRefResID);
			// if (controlImageRefResID != null) {
			// controlElement.setAttribute(ATTR_REF_RES_ID_SECONDARY,
			// controlImageRefResID);
			// }

			// showSelControl.appendChild(controlElement);
		}
		createVideoRessourceEntry(quiz, doc, idManager, project);
		createVideoActionEntry(quiz, doc, idManager, project,
				defaultControlList);

	}

	// TODO: Use this method or delete/uncomment it...
	@SuppressWarnings("unused")
	private String createImageButton(NodeQuizControl selectionControl,
			Document doc, IdManager idManager) throws ExportException {

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
			IdManager idManager, Project project) throws ExportException {

		if (nodeQuiz.getTestId() == -1) {
			return;
		}

		// QuizTitel Ressource
		Element quizTitleLabel = doc.createElement(TAG_LABEL);
		quizTitleLabel.setAttribute(ATTR_RES_ID, idManager.getID(nodeQuiz));
		quizTitleLabel.setIdAttribute(ATTR_RES_ID, true);

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
			Element quizQuestionLabel = doc.createElement(TAG_LABEL);
			quizQuestionLabel.setAttribute(
					ATTR_RES_ID,
					idManager.getID(nodeQuiz) + VAL_QUIZ_QUESTION
							+ (node.getPosition() + 1));
			quizQuestionLabel.setIdAttribute(ATTR_RES_ID, true);

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
				Element quizAnswerLabel = doc.createElement(TAG_LABEL);
				quizAnswerLabel.setAttribute(
						ATTR_RES_ID,
						idManager.getID(nodeQuiz) + VAL_QUIZ_QUESTION
								+ (node.getPosition() + 1) + VAL_QUIZ_ANSWER
								+ answer.getPositionAnswer());
				quizAnswerLabel.setIdAttribute(ATTR_RES_ID, true);

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
			IdManager idManager, Project project,
			List<NodeQuizControl> controlList) throws ExportException {

		if (nodeQuiz.getTestId() == -1) {
			return;
		}

		Element actions = getActions(doc);
		Element showQuizLinear = doc.createElement(TAG_QUIZ_LINEAR);
		showQuizLinear.setAttribute(ATTR_REF_RES_ID, idManager.getID(nodeQuiz));
		showQuizLinear.setAttribute(ATTR_ACTIONID,
				ATTR_QUIZ_ACTION_ID+idManager.getID(nodeQuiz));
		showQuizLinear.setAttribute(TAG_QUIZ_TIMEOFFEEDBACK, Integer.toString(DbQueries
		.getTestData(nodeQuiz.getTestId()).getTimeOfFeedback()));

		Element path = doc.createElement(TAG_QUIZ_PATH);

		Element point = doc.createElement(TAG_QUIZ_POINT);
		point.setAttribute(TAG_QUIZ_YPOS, VAL_POINT_NULL);
		point.setAttribute(TAG_QUIZ_XPOS, VAL_POINT_NULL);
		point.setAttribute(TAG_QUIZ_YSIZE, VAL_POINT_NULL);
		point.setAttribute(TAG_QUIZ_XSIZE, VAL_POINT_NULL);
		point.setAttribute(TAG_QUIZ_TIME, VAL_TIME_NULL);
		path.appendChild(point);
		showQuizLinear.appendChild(path);

		// Testproperties
//		Element testProperties = doc.createElement(TAG_QUIZ_TESTPROPERTIES);
//		testProperties.setAttribute(TAG_QUIZ_TIMEOFFEEDBACK, Integer.toString(DbQueries
//				.getTestData(nodeQuiz.getTestId()).getTimeOfFeedback()));
//		testProperties.setAttribute(TAG_QUIZ_MAXPOINTS, Integer
//				.toString(DbQueries.getTestData(nodeQuiz.getTestId())
//						.getMaxPoints()));
//		showQuizLinear.appendChild(testProperties);

		// TaskList
		Element taskList = doc.createElement(TAG_QUIZ_TASKLIST);
		taskList.setAttribute(ATTR_QUIZ_REF_TASK_ID_START, VAL_QUIZ_TASK_ID+nodeQuiz.getTestId()+ID_DELIMITER+"1");

		List<Node> nodeList = DbQueries.getNodeListByTest(nodeQuiz.getTestId());
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			Element task = doc.createElement(TAG_QUIZ_TASK);
			task.setAttribute(ATTR_REF_RES_ID, idManager.getID(nodeQuiz)
					+ VAL_QUIZ_QUESTION + (node.getPosition() + 1));
			task.setAttribute(ATTR_QUIZ_RANDOM,
					Boolean.toString(node.isRandom()));
			task.setAttribute(ATTR_QUIZ_POINTS,
					Integer.toString(node.getPoints()));
			task.setAttribute(ATTR_QUIZ_TASK_ID,
					VAL_QUIZ_TASK_ID + Integer.toString(node.getIdTest())
							+ ID_DELIMITER + (node.getPosition() + 1));
			if(i<nodeList.size()-1){
				task.setAttribute(ATTR_QUIZ_REF_TASK_ID_NEXT, VAL_QUIZ_TASK_ID + Integer.toString(nodeList.get(i+1).getIdTest())
						+ ID_DELIMITER + (nodeList.get(i+1).getPosition() + 1));
			}

			List<Answer> answerList = DbQueries.getAnswerByNode(node);
			for (int j = 0; j < answerList.size(); j++) {
				Answer answer = answerList.get(j);
				Element answerElement = doc.createElement(TAG_QUIZ_ANSWER);
				answerElement.setAttribute(
						ATTR_REF_RES_ID,
						idManager.getID(nodeQuiz) + VAL_QUIZ_QUESTION
								+ (node.getPosition() + 1) + VAL_QUIZ_ANSWER
								+ answer.getPositionAnswer());
				answerElement.setAttribute(ATTR_QUIZ_POSITION,
						Integer.toString(j));
				answerElement.setAttribute(ATTR_QUIZ_ANSWER_ID,
						VAL_QUIZ_ANSWER_ID + idManager.getID(nodeQuiz)
								+ ID_DELIMITER + (node.getPosition() + 1)
								+ ID_DELIMITER + answer.getPositionAnswer());
				answerElement.setAttribute(ATTR_QUIZ_IS_CORRECT,
						Boolean.toString(answer.getIsCorrect()));
				task.appendChild(answerElement);
			}
			taskList.appendChild(task);
		}
		Element pointRanges = doc.createElement(TAG_QUIZ_POINTRANGE);

		for (int i = 0; i < controlList.size(); i++) {
			Element range = doc.createElement(TAG_QUIZ_RANGE);
			//String split[] = controlList.get(i).getAmountPoints().split("-"); //$NON-NLS-1$
			range.setAttribute(ATTR_QUIZ_MIN_VALUE, Integer.toString(controlList.get(i).getMinValue()));
			range.setAttribute(ATTR_QUIZ_MAX_VALUE, Integer.toString(controlList.get(i).getMaxValue()));
			IGraphNode successor = controlList.get(i).getChildren().get(0);
			range.setAttribute(ATTR_REF_ACTION_ID,idManager.getActionID(successor));
			range.setAttribute(ATTR_QUIZ_RANGE_ID,VAL_QUIZ_RANGE_ID + idManager.getID(nodeQuiz)+ ID_DELIMITER+ idManager.getID(controlList.get(i)));
			pointRanges.appendChild(range);
		}

		showQuizLinear.appendChild(taskList);
		showQuizLinear.appendChild(pointRanges);
		actions.appendChild(showQuizLinear);
	}
}
