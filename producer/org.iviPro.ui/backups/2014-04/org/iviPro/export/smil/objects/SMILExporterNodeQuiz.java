package org.iviPro.export.smil.objects;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.quiz.Answer;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.Question;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SMILExporterNodeQuiz extends SMILExporter {

	SMILExporterNodeQuiz(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeQuiz.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document document,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {

		NodeQuiz quiz = (NodeQuiz) exportObj;
//		String defLanguage = project.getDefaultLanguage().getLanguage();
		String quizID = idManager.getID(quiz);
		String quizPoints = quizID + ADDITION_POINTS;
		String quizRegion = quiz.getScreenArea().toString().toLowerCase();
		if (quizRegion.equals("overlay")) {
			quizRegion = VAL_MAIN_REGION_ID;
		} else {
			quizRegion += "_region";
		}

		// Check, if there is already a state-element. If not, create it
		Element state = getState(document);
		if (state == null) {
			state = document.createElement(TAG_STATE);
			getHead(document).appendChild(state);
			Element data = document.createElement(TAG_DATA);
			data.setAttribute(ATTR_XML_NAMESPACE, "");
			state.appendChild(data);
		}
		Element data = (Element) state.getFirstChild();
		Element points = document.createElement(quizPoints);
		points.setTextContent("0");
		data.appendChild(points);

		// Create a sequential-element for the questions
		Element quizSequential = document.createElement(TAG_SEQ);
		quizSequential.setAttribute(ATTR_ID, quizID);

		// Create a tag that resets the points for this quiz
		Element resetter = document.createElement(TAG_SET_VALUE);
		resetter.setAttribute(ATTR_REF, quizPoints);
		resetter.setAttribute(ATTR_VALUE, VAL_ZERO);
		quizSequential.appendChild(resetter);

		// Create the questions and answers
		List<Node> nodeList = DbQueries.getNodeListByTest(quiz.getTestId());

		for (int i = nodeList.size() - 1; i >= 0; i--) {
			Node node = nodeList.get(i);

			// Create a parallel-node that will display all possible answers to
			// the question
			Element questionPar = document.createElement(TAG_PAR);
			quizSequential.appendChild(questionPar);
			questionPar.setAttribute(ATTR_DURATION, VAL_INDEFINITE);

			// Create an exclusive-node that will be used to give the awarded
			// points
			Element questionExcl = document.createElement(TAG_EXCLUSIVE);
			quizSequential.appendChild(questionExcl);

			Element getPoints = document.createElement(TAG_SEQ);
			getPoints.setAttribute(ATTR_ID, quizID + ADDITION_GET_POINTS + "_"
					+ i);
			Element setValue = document.createElement(TAG_SET_VALUE);
			setValue.setAttribute(ATTR_REF, quizPoints);
			setValue.setAttribute(ATTR_VALUE,
					quizPoints + " + " + node.getPoints());
			getPoints.appendChild(setValue);

			Element noPoints = document.createElement(TAG_SEQ);
			noPoints.setAttribute(ATTR_ID, quizID + ADDITION_NO_POINTS + "_"
					+ i);

			questionExcl.appendChild(getPoints);
			questionExcl.appendChild(noPoints);

			/*
			 * Multilanguage is not yet implemented for the answers and
			 * questions of the quiz. So the following code is commented out, in
			 * order to use the standard text for them.
			 */

			// Question Text
			Question question = DbQueries.getQuestionByNode(node);
			// Collection<LocalizedString> questionTexts = question.getTitles();
			// String defQuestionString = "";
			// if (!questionTexts.isEmpty()) {
			// // If the quiz supports multi-language, which is not yet
			// // implemented in the SIVA-model
			// Element questionSwitch = document.createElement(TAG_SWITCH);
			// for (LocalizedString questionText : questionTexts) {
			// if (questionText.getSivaLangcode().equals(defLanguage)) {
			// defQuestionString = questionText.getValue();
			// }
			// Element questionTextElement = document
			// .createElement(TAG_TEXT);
			// questionTextElement.setAttribute(ATTR_REGION, quizRegion);
			// questionTextElement.setAttribute(ATTR_SYSTEMLANGUAGE,
			// questionText.getSivaLangcode());
			// questionTextElement.setTextContent(questionText.getValue());
			// questionSwitch.appendChild(questionTextElement);
			// }
			// // Default question language
			// Element defQuestion = document.createElement(TAG_TEXT);
			// defQuestion.setAttribute(ATTR_REGION, quizRegion);
			// defQuestion.setTextContent(defQuestionString);
			// questionSwitch.appendChild(defQuestion);
			//
			// questionPar.appendChild(questionSwitch);
			// } else {
			// Standard quiz with only one language
			Element questionText = document.createElement(TAG_TEXT);
			questionText.setAttribute(ATTR_REGION, quizRegion);
			questionText.setTextContent(question.getQuestionText());
			questionPar.appendChild(questionText);
			// }

			// Export the answers
			List<Answer> answerList = DbQueries.getAnswerByNode(node);
			if (node.isRandom()) {
				Collections.shuffle(answerList);
			}
			int answerOffset = 20;
			for (Answer answer : answerList) {
				Element link = document.createElement(TAG_LINK);
				if (answer.getIsCorrect()) {
					link.setAttribute(ATTR_H_REFERENCE, "#" + quizID
							+ ADDITION_GET_POINTS + "_" + i);
				} else {
					link.setAttribute(ATTR_H_REFERENCE, "#" + quizID
							+ ADDITION_NO_POINTS + "_" + i);
				}

				// Collection<LocalizedString> answerTitles =
				// answer.getTitles();
				// if (!answerTitles.isEmpty()) {
				// // Multilanguage answers
				// Element answerSwitch = document.createElement(TAG_SWITCH);
				// String defAnswerText = "";
				// for (LocalizedString answerTextString : answerTitles) {
				// if (answerTextString.getSivaLangcode().equals(
				// defLanguage)) {
				// defAnswerText = answerTextString.getValue();
				// }
				// Element answerText = document.createElement(TAG_TEXT);
				// answerText.setAttribute(ATTR_REGION, quizRegion);
				// answerText
				// .setAttribute(ATTR_LEFT, VAL_LEFT_QUIZ_OFFSET);
				// answerText.setAttribute(ATTR_TOP,
				// Integer.toString(answerOffset));
				// answerText.setTextContent(answerTextString.getValue());
				// }
				// answerOffset += 20;
				//
				// // Default-language answer
				// Element defAnswer = document.createElement(TAG_TEXT);
				// defAnswer.setAttribute(ATTR_REGION, quizRegion);
				// defAnswer.setAttribute(ATTR_LEFT, VAL_LEFT_QUIZ_OFFSET);
				// defAnswer.setAttribute(ATTR_TOP,
				// Integer.toString(answerOffset));
				// defAnswer.setTextContent(defAnswerText);
				// answerSwitch.appendChild(defAnswer);
				//
				// link.appendChild(answerSwitch);
				// questionPar.appendChild(link);
				// } else {
				// Standard quiz with only one language
				Element answerText = document.createElement(TAG_TEXT);
				answerText.setAttribute(ATTR_REGION, quizRegion);
				answerText.setAttribute(ATTR_LEFT, VAL_LEFT_QUIZ_OFFSET);
				answerText.setAttribute(ATTR_TOP,
						Integer.toString(answerOffset));
				answerOffset += 20;
				answerText.setTextContent(answer.getAnswerText());

				link.appendChild(answerText);
				questionPar.appendChild(link);
				// }
			}

			// Add an empty link to "close" the list of answers
			Element lastLink = document.createElement(TAG_LINK);
			Element lastLinkText = document.createElement(TAG_TEXT);
			lastLinkText.setAttribute(ATTR_REGION, quizRegion);
			lastLinkText.setAttribute(ATTR_TOP, Integer.toString(answerOffset));
			lastLink.appendChild(lastLinkText);
			questionPar.appendChild(lastLink);
		}

		if (parent == null) {
			getMainSeqElement(document).appendChild(quizSequential);
		} else {
			parent.appendChild(quizSequential);
		}

		List<IGraphNode> controlChildren = quiz
				.getChildren(NodeQuizControl.class);

		// Export of the paths
		LinkedList<LinkedList<IGraphNode>> paths = forkPaths(quiz,
				alreadyExported);

		exportQuizPaths(paths, parent, document, idManager, project,
				alreadyExported, controlChildren, quizPoints, quizID);

		// Create a sequential node that is used to reset the points of this
		// quiz and as target after every path
		Element end = document.createElement(TAG_SEQ);
		end.setAttribute(ATTR_ID, quizID + ADDITION_END);

		if (parent == null) {
			getMainSeqElement(document).appendChild(end);
		} else {
			parent.appendChild(end);
		}

		IGraphNode intersection = paths.getLast().getLast();
		IGraphNode furtherExportNode = null;
		if (!oneNodePathIntersection(paths, intersection)
				|| intersection instanceof NodeEnd) {
			furtherExportNode = intersection;
		} else {
			if (intersection instanceof NodeScene) {
				furtherExportNode = onlyImportantChild((NodeScene) intersection);
			} else if ((intersection instanceof NodeSelection)
					|| (intersection instanceof NodeQuiz)) {
				furtherExportNode = firstCommonPathNode(intersection,
						alreadyExported);
			}
		}
		SMILExporter exporter = SMILExporterFactory
				.createSMILExporter(furtherExportNode);
		exporter.exportObject(document, idManager, project, alreadyExported,
				parent);

	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

	/**
	 * Method to export the created paths of the quiz.
	 * 
	 * @param paths
	 *            The paths that are to be exported.
	 * @param parent
	 *            The parental node of the quiz.
	 * @param doc
	 *            The document of the SMIL-file.
	 * @param idManager
	 *            The IDManager of the current project.
	 * @param project
	 *            The project that is currently exported.
	 * @param alreadyExported
	 *            The list of nodes that have already been exported.
	 * @param controls
	 *            The NodeQuizControls of the exported quiz.
	 * @throws ExportException
	 */
	private void exportQuizPaths(LinkedList<LinkedList<IGraphNode>> paths,
			Element parent, Document doc, IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, List<IGraphNode> controls,
			String quizPoints, String quizID) throws ExportException {
		for (int i = 0; i < paths.size() - 1; i++) {
			LinkedList<IGraphNode> path = paths.get(i);
			NodeQuizControl control = (NodeQuizControl) controls.get(i);
			//String[] points = control.getAmountPoints().split("-");
			String pointAttr = quizPoints + GREATER_THEN_EQUALS + control.getMinValue()
					+ " and " + quizPoints + LESS_THEN_EQUALS + control.getMaxValue();

			Element sequential = doc.createElement(TAG_SEQ);
			sequential.setAttribute(ATTR_EXPR, pointAttr);

			int pathSize = path.size();
			if (!alreadyExported.contains(path.getLast())) {
				pathSize -= 1;
			}

			// Export all but the last node
			for (int j = 0; j < pathSize; j++) {
				IGraphNode node = path.get(j);
				SMILExporter exporter = SMILExporterFactory
						.createSMILExporter(node);
				exporter.exportObject(doc, idManager, project, alreadyExported,
						sequential);
			}

			// Append a link that directs to the end-sequential node, that will
			// reset the points of this quiz and also jump around scenes that
			// should not be shown
			Element linkToEnd = doc.createElement(TAG_LINK);
			linkToEnd.setAttribute(ATTR_H_REFERENCE, "#" + quizID
					+ ADDITION_END);
			Element linkToEndText = doc.createElement(TAG_TEXT);
			linkToEndText.setAttribute(TAG_REGION, VAL_MAIN_REGION_ID);
			linkToEndText.setAttribute(ATTR_DURATION, VAL_INDEFINITE);
			linkToEndText.setTextContent(GOTO_CLICK_TEXT);
			linkToEnd.appendChild(linkToEndText);
			sequential.appendChild(linkToEnd);

			if (parent == null) {
				Element mainSeq = getMainSeqElement(doc);
				mainSeq.appendChild(sequential);
			} else {
				parent.appendChild(sequential);
			}
		}
	}

}
