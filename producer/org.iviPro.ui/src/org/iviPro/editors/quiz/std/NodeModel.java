package org.iviPro.editors.quiz.std;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Observable;

import org.iviPro.application.Application;
import org.iviPro.editors.quiz.algorithm.DynamicAlgorithm;
import org.iviPro.editors.quiz.algorithm.InsertAlgorithm;
import org.iviPro.editors.quiz.algorithm.LinearAlgorithm;
import org.iviPro.editors.quiz.interfaces.NodeInterface;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.editors.quiz.view.NodeView;
import org.iviPro.editors.scenegraph.subeditors.NodeQuizEditor;
import org.iviPro.model.quiz.AdditionalInfo;
import org.iviPro.model.quiz.Answer;
import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.Question;
import org.iviPro.model.quiz.Test;

/**
 * NodeModel verwaltet die eigentliche Testgenerierung.
 * 
 * @author Sabine Gattermann.
 * 
 */
public class NodeModel extends Observable implements NodeInterface {

	private static final int SAVENEXIT = 0;
	private static final int SAVENNEWQUESTION = 1;
	private static final int NUMBEROFANSWERS = 2;
	private static final int CORRECTANSWERS = 3;
	private static final int EMPTYQUESTION = 4;
	private static final int RELOADEDGE = 5;
	private static final int CONDITION = 6;
	private static final int INCONSISTANT = 7;
	private static final int SAVE = 8;
	private static final int SAVENODE = 9;
	private static final String NUMBEROFANSWERS_ERR_MSG = "Bitte geben Sie mindestens zwei Antworten an!";
	private static final String CORECTANSWERS_ERR_MSG = "Bitte geben Sie mindestens EINE KORREKTE Antwort an!";
	private static final String QUESTIONEMPTY_ERR_MSG = "Bitte geben Sie die Frage an!";

	private static final int UPDATEANDEXIT = 20;
	private static final int UPDATEANDNEW = 21;
	private static final int UPDATEANDLOAD = 22;
	private static final int UPDATEANDINSERT = 23;

	private static final int DYNAMIC = 1;
	private static final int LINEAR = 0;

	private static final int EXPORT_DIALOG = 30;
	private static final int TEST_EXPORTED = 31;
	private static final int TEST_EXPORTED_ERR_EX = 32;
	private static final int TEST_EXPORTED_ERR_WR = 33;

	private static final int DEFAULTNUMBEROFSHOWNANSWERS = 5;

	private Test test;
	public Node currentNode;
	public Edge currentEdge;
	public Condition currentCondition;
	public int idNodePredecessor;
	public int currentPosition;
	private LinkedList<String> answersText;
	private LinkedList<Boolean> answersBoolean;
	private String message;
	private LinkedList<Answer> answersToSave;
	private boolean loadTestFromDB;
	private Question currentQuestion;
	private LinkedList<Answer> currentAnswerList;
	private LinkedList<AdditionalInfo> currentLinkList;
	private LinkedList<AdditionalInfo> currentVideoList;
	private LinkedList<AdditionalInfo> currentAudioList;
	private LinkedList<AdditionalInfo> currentImageList;
	private int numberOfNodesInTest;
	protected LinkedList<Node> nodeList;
	private LinkedList<Edge> edgeList;

	private Condition dummyCondition;
	private LinkedList<Condition> conditionList;
	private InsertAlgorithm insertAlgorithm;
	private boolean inputIsOkay = true;
	private boolean isWritten = true;
	private String exportMsg;
	private boolean randomAnswers;

	/**
	 * Konstruktor
	 * 
	 * @param test
	 *            Der Test.
	 * @param loadFromDB
	 *            true, falls Test aus DB geladen werden soll, false sonst.
	 */
	public NodeModel(Test test, boolean loadFromDB, boolean randomAnswers,
			NodeQuizEditor ed) {
		super();
		QuizGenerator.getMainModelInstance().setNodeModelInstance(this);
		this.test = test;
		this.loadTestFromDB = loadFromDB;
		this.randomAnswers = randomAnswers;
		currentPosition = 0;
		setNumberOfNodesInTest(0);
		nodeList = new LinkedList<Node>();
		edgeList = new LinkedList<Edge>();
		idNodePredecessor = -1;
		currentEdge = new Edge(Application.getCurrentProject(), 0, 0, 0,
				DbQueries.getNodeData(0));

		if (test.getTestType() == DYNAMIC)
			insertAlgorithm = new DynamicAlgorithm(this, this.test);
		else if (test.getTestType() == LINEAR)
			insertAlgorithm = new LinearAlgorithm(this, this.test);
		dummyCondition = insertAlgorithm.getDummyCondition();

		currentCondition = dummyCondition;

		conditionList = new LinkedList<Condition>();
		currentLinkList = new LinkedList<AdditionalInfo>();
		currentVideoList = new LinkedList<AdditionalInfo>();
		currentAudioList = new LinkedList<AdditionalInfo>();
		currentImageList = new LinkedList<AdditionalInfo>();

		if (!this.loadTestFromDB) {
			initNewNode();
		} else {
			initDbNode();
		}
		getNodesEdgesConditionsFromDB();

		insertAlgorithm.calculateEdge();
		this.addObserver(new NodeView(QuizGenerator.getDefaultShell(), this, ed));
	}

	/**
	 * Abfrage der Knoten-, Kanten- und BedingungsListen aus der Datenbank.
	 */
	private void getNodesEdgesConditionsFromDB() {
		nodeList = DbQueries.getNodeListByTest(test.getIdTest());
		// sortieren nach position der frage. id und position sind nicht immer gleich
		Collections.sort(nodeList, new Comparator<Node>() {
			@Override
			public int compare(Node n1, Node n2) {
				return Integer.valueOf(n1.getPosition()).compareTo(Integer.valueOf(n2.getPosition()));
			}
		});
		LinkedList<Integer> idListNodeList = new LinkedList<Integer>();
		for (int i = 0; i < nodeList.size(); i++) {
			idListNodeList.add(nodeList.get(i).getIdNode());
		}
		edgeList = DbQueries.getEdgeListByNodeList(idListNodeList);

		for (int i = 0; i < edgeList.size(); i++) {
			Condition b = DbQueries.getConditionData(edgeList.get(i)
					.getIdCondition());
			if (!conditionList.contains(b) && b.getIdCondition() > 0)
				conditionList.add(b);
		}

		if (currentEdge != null) {
			currentCondition = DbQueries.getConditionData(currentEdge
					.getIdCondition());
		}
	}

	public void setTitle(String title) {
		this.test.setTitle(title);
	}

	public void setAmountPoints(int amountPoints) {
		this.test.setMaxPoints(amountPoints);
		// Alle anderen Nodes durchiterieren und updaten
		DbQueries.updateMaxPoints(test, amountPoints);
	}

	public void setFeedback(int feedback) {
		this.test.setTimeOfFeedback(feedback);
	}
	
	public Test getTest(){
		return this.test;
	}

	/**
	 * Erstellt einen neuen Knoten und setzt ihn als aktuellen Knoten.
	 */
	public void initNewNode() {
		int predecessorPosition = -1;
		if (currentNode != null)
			predecessorPosition = currentNode.getPosition();
		currentNode = new Node(Application.getCurrentProject());
		currentNode.setPosition(currentPosition);
		currentNode.setIdTest(test.getIdTest());
		currentNode.setHasQuestion(DbQueries.setQuestionData(new Question(
				Application.getCurrentProject(), "")));
		currentNode.setRandom(randomAnswers);

		currentQuestion = new Question(Application.getCurrentProject());
		currentAnswerList = new LinkedList<Answer>();
		for (int i = 0; i < DEFAULTNUMBEROFSHOWNANSWERS; i++) {
			currentAnswerList.add(new Answer(Application.getCurrentProject(),
					"", i, false, -1));
		}
		currentLinkList = new LinkedList<AdditionalInfo>();
		currentVideoList = new LinkedList<AdditionalInfo>();
		currentAudioList = new LinkedList<AdditionalInfo>();
		currentImageList = new LinkedList<AdditionalInfo>();

		currentNode.setIdNode(DbQueries.setNodeData(currentNode));

		setNumberOfNodesInTest(getNumberOfNodesInTest() + 1);

		if (getNumberOfNodesInTest() > 1) {
			/*
			 * LinkedList<Integer> existingEdges = DbQueries
			 * .getConditionOutgoingListByLookbackByNode( idNodePredecessor,
			 * test.getMaxPunkte());
			 */
			Condition condition = dummyCondition;

			if (insertAlgorithm.getType() == DYNAMIC) {
				// sucht nach noch moeglicher kanten-bedingung!
				/*
				 * if (existingEdges.contains(-1)) { for (int j = 0; j <=
				 * test.getMaxPunkte(); j++) { if (!existingEdges.contains(j)) {
				 * b.setBedingung(j);
				 * b.setIdBedingung(DbQueries.setConditionData(b)); } } }
				 */

				String[] possibleStr = insertAlgorithm.getPossiblePoints(
						predecessorPosition, currentNode.getPosition(), 1);

				LinkedList<Integer> possibleInt = new LinkedList<Integer>();

				for (int i = 0; i < possibleStr.length; i++) {
					if (possibleStr[i].equals("D")) {
						possibleInt.add(-1);
					} else {
						possibleInt.add(Integer.parseInt(possibleStr[i]));
					}
				}
				condition.setConditionPoints(possibleInt.get(0));
				condition.setIdCondition(DbQueries.setConditionData(condition));
			}

			currentEdge = new Edge(Application.getCurrentProject(),
					idNodePredecessor, currentNode.getIdNode(),
					condition.getIdCondition(),
					DbQueries.getNodeData(currentNode.getIdNode()));
			DbQueries.setEdgeData(currentEdge);
			currentCondition = DbQueries.getConditionData(currentEdge
					.getIdCondition());
			insertAlgorithm.setEdgeNumbersForGui(predecessorPosition,
					currentNode.getPosition(), currentEdge);
		}

		idNodePredecessor = currentNode.getIdNode();
		getNodesEdgesConditionsFromDB();

		insertAlgorithm.calculateEdge();

	}

	/**
	 * Ruft einen Knoten aus der DB ab.
	 * 
	 * @param id
	 *            Die Knoten-ID.
	 */
	public void initDbNode(int id) {
		currentNode = DbQueries.getNodeData(id);
		currentQuestion = DbQueries.getQuestionByNode(currentNode);
		currentQuestion.setQuestionText(currentQuestion.getQuestionText()
				.replace("&quot;", "\""));
		currentAnswerList = DbQueries.getAnswerByNode(currentNode);

		// vorsicht: gui-fenstergroesse abhängig von #antworten
		// ggf. auffuellen mit leeren antworten
		if (currentAnswerList.size() < DEFAULTNUMBEROFSHOWNANSWERS) {
			for (int i = currentAnswerList.size(); i < DEFAULTNUMBEROFSHOWNANSWERS; i++) {
				currentAnswerList.add(new Answer(Application
						.getCurrentProject(), "", i, false, -1));
			}
		}
		currentLinkList = DbQueries.getLinkByNode(currentNode,
				Application.getCurrentProject());

		LinkedList<AdditionalInfo> allMediaList = DbQueries
				.getMediaByNode(currentNode);
		currentVideoList = new LinkedList<AdditionalInfo>();
		currentAudioList = new LinkedList<AdditionalInfo>();
		currentImageList = new LinkedList<AdditionalInfo>();

		for (int i = 0; i < allMediaList.size(); i++) {
			if (allMediaList.get(i).getType() == 1)
				currentVideoList.add(allMediaList.get(i));
			else if (allMediaList.get(i).getType() == 2)
				currentAudioList.add(allMediaList.get(i));
			else if (allMediaList.get(i).getType() == 3)
				currentImageList.add(allMediaList.get(i));
		}

		setNumberOfNodesInTest(DbQueries.getNumberOfNodes(test));

		LinkedList<Edge> edges = DbQueries.getEdgeListByNode(id, false);

		if (edges.size() > 0) {
			currentEdge = edges.get(0);
			currentCondition = DbQueries.getConditionData(currentEdge
					.getIdCondition());
			insertAlgorithm.setEdgeNumbersForGui(
					DbQueries.getNodeData(currentEdge.getIdNodeSource())
							.getPosition(),
					DbQueries.getNodeData(currentEdge.getIdNodeDestination())
							.getPosition(), currentEdge);

		} else {
			currentEdge = null;
		}
		currentPosition = currentNode.getPosition();

		// insertAlgorithmus.calculateEdge(); \\TODO lookback wird ohne zeile
		// korrekt geliefert

		getNodesEdgesConditionsFromDB();

		setChanged();
		notifyObservers(SAVENNEWQUESTION);

	}

	/**
	 * Laedt den nach der aktuellen Position bestimmten Knoten aus der DB und
	 * setzt ihn als aktuellen Knoten.
	 */
	private void initDbNode() {

		currentNode = DbQueries.getNodeData(test, currentPosition);
		currentQuestion = DbQueries.getQuestionByNode(currentNode);
		currentQuestion.setQuestionText(currentQuestion.getQuestionText()
				.replace("&quot;", "\""));
		currentAnswerList = DbQueries.getAnswerByNode(currentNode);
		// vorsicht: gui-fenstergroesse abhängig von #antworten
		// ggf. auffuellen mit leeren antworten
		if (currentAnswerList.size() < DEFAULTNUMBEROFSHOWNANSWERS) {
			for (int i = currentAnswerList.size(); i < DEFAULTNUMBEROFSHOWNANSWERS; i++) {
				currentAnswerList.add(new Answer(Application
						.getCurrentProject(), "", i, false, -1));
			}
		}
		currentLinkList = DbQueries.getLinkByNode(currentNode,
				Application.getCurrentProject());
		LinkedList<AdditionalInfo> allMediaListe = DbQueries
				.getMediaByNode(currentNode);
		currentVideoList = new LinkedList<AdditionalInfo>();
		currentAudioList = new LinkedList<AdditionalInfo>();
		currentImageList = new LinkedList<AdditionalInfo>();

		for (int i = 0; i < allMediaListe.size(); i++) {
			if (allMediaListe.get(i).getType() == 1) {
				currentVideoList.add(allMediaListe.get(i));
			} else if (allMediaListe.get(i).getType() == 2) {
				currentAudioList.add(allMediaListe.get(i));
			} else if (allMediaListe.get(i).getType() == 3) {
				currentImageList.add(allMediaListe.get(i));
			}
		}

		LinkedList<Edge> edgeList = DbQueries.getEdgeListByNode(
				currentNode.getIdNode(), false); // TODO war auf true gesetzt.
													// warum??
		// zur absicherung von nullpointer: position abfragen (bei zyklus auf
		// pos 0 beim laden sonst nullpointer)
		if (currentNode.getPosition() > 0 && edgeList.size() > 0) {
			currentEdge = edgeList.get(0);
			insertAlgorithm.setEdgeNumbersForGui(
					DbQueries.getNodeData(currentEdge.getIdNodeSource())
							.getPosition(),
					DbQueries.getNodeData(currentEdge.getIdNodeDestination())
							.getPosition(), currentEdge);
			currentCondition = DbQueries.getConditionData(currentEdge
					.getIdCondition());
		}

		setNumberOfNodesInTest(DbQueries.getNumberOfNodes(test));

	}

	/**
	 * Getter fuer PunkteListe der Frage.
	 * 
	 * @return Die Liste der Punkte.
	 */
	public String[] getPointsList() {
		String[] pointList = new String[test.getMaxPoints()];
		for (int i = 0; i < test.getMaxPoints(); i++) {
			pointList[i] = Integer.toString(i + 1);
		}
		return pointList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#saveNexit(java.lang.String, int,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList)
	 */
	@Override
	public void saveNexit(String questionText, int points,
			LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
			boolean random, LinkedList<String> links,
			LinkedList<String> videos, LinkedList<String> audios,
			LinkedList<String> images) {
		inputIsOkay = checkInput(questionText, points, answerList,
				boolAnswerList, links, videos);
		if (inputIsOkay || NodeView.forceClose) {
			updateDbNode(questionText, points, answerList, boolAnswerList,
					random, links, videos, audios, images, UPDATEANDEXIT);
		}
	}

	/**
	 * Testet die Konsistenz der Kanten bevor die Testbearbeitung beendet werden
	 * kann.
	 * 
	 * @return true, alls konsistent, false sonst.
	 */
	public boolean checkTestConsistance() {
		NodeModelHelpClass consistanceChecker = new NodeModelHelpClass();
		int result = consistanceChecker.checkConsistence(test);

		if (result != 0) {
			for (int i = 0; i < nodeList.size(); i++) {
				if (result == nodeList.get(i).getIdNode()) {
					int inconsistantNodeNr = nodeList.get(i).getPosition() + 1;
					message = " Der Test kann nicht gespeichert werden, da der Testablauf inkonsistent ist! \n\n"
							+ " Fügen Sie bitte zusätzliche Bedingungen bei Frage "
							+ inconsistantNodeNr + " ein. \n\n";
					return false;
				}
			}

		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#addNewNode(java.lang.String, int,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList)
	 */
	@Override
	public void addNewNode(String questionText, int points,
			LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
			boolean random, LinkedList<String> links,
			LinkedList<String> videos, LinkedList<String> audios,
			LinkedList<String> images) {
		if (checkInput(questionText, points, answerList, boolAnswerList, links,
				videos)) {
			if (getNumberOfNodesInTest() - currentPosition <= 1) {
				updateDbNode(questionText, points, answerList, boolAnswerList,
						random, links, videos, audios, images, UPDATEANDNEW);

			} else if (getNumberOfNodesInTest() - currentPosition > 1) {
				updateDbNode(questionText, points, answerList, boolAnswerList,
						random, links, videos, audios, images, UPDATEANDINSERT);
			}

		}
	}

	/**
	 * Ueberprueft ob die Benutzereingaben korrekt sind.
	 * 
	 * @param questionText
	 *            Der FrageText.
	 * @param points
	 *            Die Punkte.
	 * @param answerList
	 *            Die Liste der Antworten.
	 * @param boolAnswerList
	 *            Die true/false-Liste der Antworten.
	 * @param links
	 *            Die Link-Liste.
	 * @param files
	 *            Die Media-Liste.
	 * @return true, falls okay, false sonst.
	 */
	private boolean checkInput(String questionText, int points,
			LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
			LinkedList<String> links, LinkedList<String> files) {

		boolean answersOkay = false;

		String tmp = questionText;
		tmp = tmp.replace(" ", "");
		tmp = tmp.replace("<b>", "");
		tmp = tmp.replace("</b>", "");
		tmp = tmp.replace("\n", "");

		boolean questionEmpty = true;

		if (tmp.length() > 0)
			questionEmpty = false;

		if (questionEmpty) {
			message = QUESTIONEMPTY_ERR_MSG;
			setChanged();
			notifyObservers(EMPTYQUESTION);
			return false;
		} else {
			answersOkay = checkAnswers(answerList, boolAnswerList);
			return answersOkay;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#updateDbNode(java.lang.String, int,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList, int)
	 */
	@Override
	public void updateDbNode(String questionText, int points,
			LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
			boolean random, LinkedList<String> links,
			LinkedList<String> videos, LinkedList<String> audios,
			LinkedList<String> images, int doAction) {

		isWritten = false;
		boolean inputOk = true;
		if (doAction == UPDATEANDLOAD || doAction == SAVENODE
				|| doAction == SAVENNEWQUESTION) {
			inputOk = checkInput(questionText, points, answerList,
					boolAnswerList, links, videos);
		}
		if (inputOk) {
			// save or update frage
			Question question = new Question(Application.getCurrentProject(),
					questionText.replace("\"", "&quot;"));
			int idQuestion = DbQueries.setQuestionData(question);
			currentNode.setHasQuestion(idQuestion);

			// save knoten
			currentNode.setPoints(points);
			currentNode.setRandom(random);
			DbQueries.updateNodeData(currentNode);

			if (answersToSave != null) {
				// Bugfix! Remove all answer belonging to node idNode and
				// reinsert new answers!
				DbQueries.removeAnswersFromNodeId(currentNode.getIdNode());
				for (int i = 0; i < answersToSave.size(); i++) {
					if (!answersToSave.get(i).getAnswerText().equals("")) {
						answersToSave.get(i).setIdNode(currentNode.getIdNode());
						DbQueries.setAnswerData(answersToSave.get(i));
					}
				}
			}

			// save links
			for (int i = 0; i < links.size(); i++) {
				AdditionalInfo currentLink = new AdditionalInfo(
						Application.getCurrentProject(), links.get(i), (i + 1));
				DbQueries.setAdditionalInfoLinkData(currentLink, currentNode);
			}

			// save media: video
			for (int i = 0; i < videos.size(); i++) {
				int lastSepIndex = 1;
				lastSepIndex = videos.get(i).lastIndexOf(
						System.getProperty("file.separator"));

				String name = "";
				if (lastSepIndex > 0) {
					// andernfalls ist das video bereits gespeichert!!
					name = videos.get(i).substring(lastSepIndex + 1);
					AdditionalInfo currentVideo = new AdditionalInfo(
							Application.getCurrentProject(), name, new File(
									videos.get(i)), (i + 1), 1);
					DbQueries.setAdditionalInfoMediaData(currentVideo,
							currentNode);
				}

			}

			// save media: audio
			for (int i = 0; i < audios.size(); i++) {
				int lastSepIndex = 1;
				lastSepIndex = audios.get(i).lastIndexOf(
						System.getProperty("file.separator"));

				String name = "";
				if (lastSepIndex > 0) {
					// andernfalls ist die audio bereits gespeichert!!
					name = audios.get(i).substring(lastSepIndex + 1);
					AdditionalInfo currentAudio = new AdditionalInfo(
							Application.getCurrentProject(), name, new File(
									audios.get(i)), (i + 1), 2);
					DbQueries.setAdditionalInfoMediaData(currentAudio,
							currentNode);
				}

			}

			// save media: image
			for (int i = 0; i < images.size(); i++) {
				int lastSepIndex = 1;
				lastSepIndex = images.get(i).lastIndexOf(
						System.getProperty("file.separator"));

				String name = "";
				if (lastSepIndex > 0) {
					// andernfalls ist das bild bereits gespeichert!!
					name = images.get(i).substring(lastSepIndex + 1);
					AdditionalInfo currentImage = new AdditionalInfo(
							Application.getCurrentProject(), name, new File(
									images.get(i)), (i + 1), 3);
					DbQueries.setAdditionalInfoMediaData(currentImage,
							currentNode);
				}

			}

			if (insertAlgorithm.getType() == LINEAR) {
				insertAlgorithm.updateEdgeCondition();
			}

			idNodePredecessor = currentNode.getIdNode();

			isWritten = true;

			if (doAction == UPDATEANDLOAD) {
				// view ruft das laden des db-knotens per initDbNode(id) auf!
			} else if (doAction == UPDATEANDNEW) {
				if (checkInput(questionText, points, answerList,
						boolAnswerList, links, videos)) {
					currentPosition++;
					initNewNode();
					getNodesEdgesConditionsFromDB();
					setChanged();
				}
				notifyObservers(SAVENNEWQUESTION);
			} else if (doAction == UPDATEANDEXIT) {
				getNodesEdgesConditionsFromDB();
				if (checkTestConsistance()) {
					setChanged();
					notifyObservers(SAVENEXIT);
					QuizGenerator.getMainModelInstance().setNodeModelInstance(
							null);
					new MainModel();
				} else {
					setChanged();
					notifyObservers(INCONSISTANT);
				}
			} else if (doAction == UPDATEANDINSERT) {
				insertAlgorithm.insertNode();
				getNodesEdgesConditionsFromDB();
				setChanged();
				notifyObservers(SAVENNEWQUESTION);
			} else if (doAction == SAVENODE) {
				// Daten wurden nur gespeichert
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentPosition()
	 */
	@Override
	public int getCurrentPosition() {
		return currentPosition + 1;
	}

	/**
	 * Ueberprueft, ob Antworten korrekt eigegeben wurden
	 * 
	 * @param text
	 *            Die Antwort-Liste.
	 * @param bool
	 *            Die Korrekt-Liste.
	 * @return true, falls okay, false sonst.
	 */
	private boolean checkAnswers(LinkedList<String> text,
			LinkedList<Boolean> bool) {
		answersText = text;
		answersBoolean = bool;

		answersToSave = new LinkedList<Answer>();
		int counterAnswersNotEmpty = 0;
		int counterAtLeastOneCorrect = 0;

		for (int i = 0; i < answersText.size(); i++) {
			String tmp = answersText.get(i);
			tmp = tmp.replace('\n', ' ');
			tmp = tmp.replace('\\', ' ');
			tmp = tmp.replace('\r', ' ');
			tmp = tmp.replace('\t', ' ');
			tmp = tmp.replaceAll(" ", "");
			if (tmp.length() > 0) {
				counterAnswersNotEmpty++;
				answersToSave.add(new Answer(Application.getCurrentProject(),
						answersText.get(i), counterAnswersNotEmpty,
						answersBoolean.get(i), currentNode.getIdNode()));
				if (answersBoolean.get(i).equals(true)) {
					counterAtLeastOneCorrect++;
				}
			}
		}

		if (counterAnswersNotEmpty < 2) {
			message = NUMBEROFANSWERS_ERR_MSG;
			setChanged();
			notifyObservers(NUMBEROFANSWERS);
			return false;
		} else if (counterAtLeastOneCorrect == 0) {
			message = CORECTANSWERS_ERR_MSG;
			setChanged();
			notifyObservers(CORRECTANSWERS);
			return false;
		} else {
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getMessage()
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentKnoten()
	 */
	@Override
	public Node getCurrentNode() {
		return currentNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentFrage()
	 */
	@Override
	public Question getCurrentQuestion() {
		return currentQuestion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#setRandomAnswers(boolean)
	 */
	public void setRandomAnswers(boolean randomAnswers) {
		currentNode.setRandom(randomAnswers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#isRandomAnswers()
	 */
	public boolean isRandomAnswers() {
		return currentNode.isRandom();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentAntwortenListe()
	 */
	@Override
	public LinkedList<Answer> getCurrentAnswerList() {
		return currentAnswerList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentLinkListe()
	 */
	@Override
	public LinkedList<AdditionalInfo> getCurrentLinkList() {
		return currentLinkList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentVideoListe()
	 */
	@Override
	public LinkedList<AdditionalInfo> getCurrentVideoList() {
		return currentVideoList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentAudioListe()
	 */
	@Override
	public LinkedList<AdditionalInfo> getCurrentAudioList() {
		return currentAudioList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentImageListe()
	 */
	@Override
	public LinkedList<AdditionalInfo> getCurrentImageList() {
		return currentImageList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#showNodeBefore(java.lang.String, int,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList)
	 */
	@Override
	public void showNodeBefore(String questionText, int points,
			LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
			boolean random, LinkedList<String> links,
			LinkedList<String> videos, LinkedList<String> audios,
			LinkedList<String> images) {
		if (checkInput(questionText, points, answerList, boolAnswerList, links,
				videos)) {

			updateDbNode(questionText, points, answerList, boolAnswerList,
					random, links, videos, audios, images, UPDATEANDLOAD);

			Node nextNode = DbQueries.getNodeByNode(currentNode, false);
			if (nextNode.getIdNode() != -1) {
				currentNode = nextNode;
				initDbNode(currentNode.getIdNode());
			} else if (currentNode.getPosition() >= 1) {
				currentPosition--;
				initDbNode();
			}

			getNodesEdgesConditionsFromDB();

			setChanged();
			notifyObservers(UPDATEANDLOAD);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#showNodeNext(java.lang.String, int,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList,
	 * java.util.LinkedList, java.util.LinkedList, java.util.LinkedList)
	 */
	@Override
	public void showNodeNext(String questionText, int points,
			LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
			boolean random, LinkedList<String> links,
			LinkedList<String> videos, LinkedList<String> audios,
			LinkedList<String> images) {

		if (checkInput(questionText, points, answerList, boolAnswerList, links,
				videos)) {
			updateDbNode(questionText, points, answerList, boolAnswerList,
					random, links, videos, audios, images, UPDATEANDLOAD);
			Node nextNode = DbQueries.getNodeByNode(currentNode, true);
			if (nextNode.getIdNode() != -1) {
				currentNode = nextNode;
				initDbNode(currentNode.getIdNode());
			} else if (getNumberOfNodesInTest() - currentNode.getPosition() >= 2) {

				currentPosition++;
				initDbNode();
			}

			getNodesEdgesConditionsFromDB();

			setChanged();
			notifyObservers(UPDATEANDLOAD);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#enableLastNode()
	 */
	@Override
	public boolean enableLastNode() {
		if (currentPosition > 0) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#enableNextNode()
	 */
	@Override
	public boolean enableNextNode() {
		if (getNumberOfNodesInTest() - currentPosition > 1) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#deleteNode()
	 */
	@Override
	public void deleteNode() {
		insertAlgorithm.deleteNode();
		getNodesEdgesConditionsFromDB();
		initDbNode();
		setChanged();
		notifyObservers(SAVENNEWQUESTION);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getEdgeList()
	 */
	@Override
	public LinkedList<Edge> getEdgeList() {
		return edgeList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getNodeList()
	 */
	@Override
	public LinkedList<Node> getNodeList() {
		return nodeList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getEdgeNumbersForGui()
	 */
	@Override
	public LinkedList<String> getEdgeNumbersForGui() {
		if (currentPosition == 0) {
			LinkedList<String> list = new LinkedList<String>();
			list.add(" ");
			list.add(" ");
			return list;
		}
		return insertAlgorithm.getEdgeNumbersForGui();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#setEdgeNumbersForGui(int, int,
	 * utilities.beans.Kante, boolean)
	 */
	@Override
	public void setEdgeNumbersForGui(int from, int to, Edge edge,
			boolean doOnlyLayoutEdge) {

		insertAlgorithm.setEdgeNumbersForGui(from, to, edge);
		if (doOnlyLayoutEdge == true) {
			setChanged();
			notifyObservers(RELOADEDGE);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#addNewEdge(int, int, int, int)
	 */
	@Override
	public void addNewEdge(int positionSourceInNodeList,
			int positionDestinationInNodeList, int conditionLookback,
			int conditionPoints) {

		insertAlgorithm.insertEdge(positionSourceInNodeList,
				positionDestinationInNodeList, conditionLookback,
				conditionPoints);
		getNodesEdgesConditionsFromDB();
		setChanged();
		notifyObservers(SAVENNEWQUESTION);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#deleteEdge(int, int)
	 */
	@Override
	public void deleteEdge(int positionSourceInNodeList,
			int positionDestinationInNodeList) {

		insertAlgorithm.deleteEdge(positionSourceInNodeList,
				positionDestinationInNodeList);
		getNodesEdgesConditionsFromDB();
		setChanged();
		notifyObservers(SAVENNEWQUESTION);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getInsertAlgorithm()
	 */
	@Override
	public int getInsertAlgorithm() {
		return insertAlgorithm.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#updateEdge(int, int, int, int)
	 */
	@Override
	public void updateEdge(int positionSourceInNodeList,
			int positionDestinationInNodeList, int conditionPoints,
			int conditionLookback) {
		insertAlgorithm.updateEdge(positionSourceInNodeList,
				positionDestinationInNodeList, conditionPoints,
				conditionLookback);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getLookbackBedingung()
	 */
	@Override
	public String[] getLookbackForConditionField(int positionNode) {
		return insertAlgorithm.getConditionLookbackForField(nodeList
				.get(positionNode));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getPunkteBedingung()
	 */
	@Override
	public String[] getPointsForConditionField() {
		if (currentPosition == 0) {
			String[] r = new String[0];
			return r;
		}
		return insertAlgorithm.getConditionPointsForField();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#setLookback(int)
	 */
	@Override
	public void setLookback(int lookback) {
		insertAlgorithm.setLookback(lookback);
		setChanged();
		notifyObservers(CONDITION);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getPossibleLookback(int)
	 */
	@Override
	public String[] getPossibleLookback(int nodeNumber) {
		return insertAlgorithm.getPossibleLookback(nodeNumber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getPossiblePoints(int)
	 */
	@Override
	public String[] getPossiblePoints(int positionNode1, int positionNode2,
			int lookback) {
		return insertAlgorithm.getPossiblePoints(positionNode1, positionNode2,
				lookback);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getTestTitle()
	 */
	@Override
	public String getTestTitle() {
		return test.getTitle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getQuestions()
	 */
	@Override
	public LinkedList<Question> getQuestions() {
		LinkedList<Question> allQuestions = new LinkedList<Question>();
		for (int i = 0; i < nodeList.size(); i++) {
			allQuestions.add(DbQueries.getQuestionByNode(nodeList.get(i)));
		}
		return allQuestions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCurrentBedingung()
	 */
	@Override
	public Condition getCurrentCondition() {
		return currentCondition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#exit()
	 */
	@Override
	public void exit() {
		setChanged();
		notifyObservers(SAVE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#getCanExit()
	 */
	@Override
	public boolean getCanExit() {
		NodeView.closeInformation = false;
		exit();
		boolean doExit = inputIsOkay && isWritten;
		return doExit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#canInsertAnotherEdge()
	 */
	@Override
	public boolean canInsertAnotherEdge() {
		return insertAlgorithm.canInsertAnotherEdge(currentNode.getIdNode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.NodeInterface#deleteMedia(int)
	 */
	@Override
	public void deleteMedia(int id) {
		DbQueries.deleteAdditionalInfo(id, currentNode.getIdNode());
	}

	/**
	 * Veranlasst das Öffnen des Test-Export-Dialogs.
	 */
	public void exportTestDialog() {
		setChanged();
		notifyObservers(EXPORT_DIALOG);

	}

	/**
	 * Exportiert einen Test.
	 * 
	 * @param dir
	 *            Der Export-Pfad.
	 * @param alternativExportName
	 *            Der alternative Export-Name.
	 */
	public void exportTest(String dir, String alternativExportName) {
		ExportTest exporter = new ExportTest();
		int result = exporter
				.export(dir, test.getTitle(), alternativExportName);

		if (result == 0) {
			exportMsg = "Der Test wurde erfolgreich exportiert!";
			setChanged();
			notifyObservers(TEST_EXPORTED);
		} else if (result == -1) {
			setChanged();
			notifyObservers(TEST_EXPORTED_ERR_EX);
		} else {
			exportMsg = "Fehler! \nTest konnte nicht exportiert werden!";
			setChanged();
			notifyObservers(TEST_EXPORTED_ERR_WR);
		}
	}

	/**
	 * Übergibt eine Export-Status-Nachricht.
	 * 
	 * @return Die Export-Status-Nachricht
	 */
	public String getExportMsg() {
		return exportMsg;
	}

	/**
	 * Setter für Aufgabenanzahl im Test.
	 * 
	 * @param numberOfNodesInTest
	 *            Die Aufgabenanzahl.
	 */
	public void setNumberOfNodesInTest(int numberOfNodesInTest) {
		this.numberOfNodesInTest = numberOfNodesInTest;
	}

	/**
	 * Getter für Aufgabenanzahl im Test.
	 * 
	 * @return Die Aufgabenanzahl.
	 */
	public int getNumberOfNodesInTest() {
		return numberOfNodesInTest;
	}

	public int getMaxPoints() {
		return test.getMaxPoints();
	}

	public int getFeedback() {
		return test.getTimeOfFeedback();
	}

	@Override
	public LinkedList<String> checkForExistingEdge(int positionNode1,
			int positionNode2) {

		LinkedList<Edge> out = DbQueries.getEdgeListByNode(
				nodeList.get(positionNode1).getIdNode(), true);
		LinkedList<String> result = new LinkedList<String>();
		for (int i = 0; i < out.size(); i++) {
			if (out.get(i).getIdNodeDestination() == nodeList
					.get(positionNode2).getIdNode()) {
				Condition condition = DbQueries.getConditionData(out.get(i)
						.getIdCondition());
				result.add(String.valueOf(condition.getConditionLookback()));

				if (condition.getConditionPoints() == -1)
					result.add("D");
				else
					result.add(String.valueOf(condition.getConditionPoints()));
			}
		}
		return result;
	}

	public void updateTestData() {
		DbQueries.updateTestData(test);
	}

	public boolean checkTestValidToChange(String questionText, int points,
			LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
			boolean random, LinkedList<String> links,
			LinkedList<String> videos, LinkedList<String> audios,
			LinkedList<String> images) {
		if (checkInput(questionText, points, answerList, boolAnswerList, links,
				new LinkedList<String>())) {
			return true;
		} else {
			return false;
		}
	}
}
