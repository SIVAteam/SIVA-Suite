package org.iviPro.model.quiz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.iviPro.model.Project;

/**
 * Klasse zur Kommunikation mit einer Datenbank mittels SQL-Queries
 * 
 * @author Stefan Zwicklbauer
 */
public class DbQueries {

	/**
	 * Konstruktor der Klasse DB-Queries
	 * 
	 * @param database
	 *            Die Datenbank-Instanz.
	 */
	public DbQueries() {
	}

	public static void updateMaxPoints(Test test, int amountPoints) {
		NodeManager.getInstance().updateAmountPoints(test, amountPoints);
	}

	/**
	 * Loeschen eines Test-Knotens inkl. aller seiner Bestandteile.
	 * 
	 * @param nodeId
	 */
	public static void deleteNode(int nodeId) {

		LinkedList<Integer> questionIDs = NodeManager.getInstance()
				.getQuestionsIds(nodeId);
		LinkedList<Integer> answerIDs = AnswerManager.getInstance()
				.getAnswerListByNodeId(nodeId);
		LinkedList<Integer> infoIDs = NodeManager.getInstance()
				.getAddInfoIdsByNodeId(nodeId);
		LinkedList<Integer> conditionIDs = EdgeManager.getInstance()
				.getConditionIdsByNodeId(nodeId);

		NodeManager.getInstance().removeNode(nodeId);
		for (int i = 0; i < questionIDs.size(); i++) {
			QuestionManager.getInstance().removeQuestion(questionIDs.get(i));
		}

		for (int i = 0; i < answerIDs.size(); i++) {
			AnswerManager.getInstance().removeAnswer(answerIDs.get(i));
		}

		for (int i = 0; i < infoIDs.size(); i++) {
			AdditionalInfoManager.getInstance().removeInfo(infoIDs.get(i));
		}

		for (int i = 0; i < conditionIDs.size(); i++) {
			ConditionManager.getInstance().removeCondition(conditionIDs.get(i));
		}
	}

	/**
	 * Loeschen einer Kante.
	 * 
	 * @param idNodeSource
	 *            Die ID des Quellknotens.
	 * @param idCondition
	 *            Die ID der Bedingung.
	 */
	public static void deleteKante(int idNodeSource, int idCondition) {
		EdgeManager.getInstance().deleteKante(idNodeSource, idCondition);
	}

	/**
	 * Loeschen einer Kante.
	 * 
	 * @param currentEdge
	 *            Das Kanten.Objekt.
	 */
	public static void deleteEdge(Edge currentEdge) {
		EdgeManager.getInstance().deleteEdge(currentEdge);
	}

	/**
	 * Loeschen einer ZusatzInfo.
	 * 
	 * @param idInfo
	 *            Die ID einer ZusatzInfo.
	 * @param idNode
	 *            Die Knoten ID.
	 */
	public static void deleteAdditionalInfo(int idInfo, int idNode) {
		AdditionalInfoManager.getInstance().deleteAdditionalInfo(idInfo);
		NodeManager.getInstance().removeAdditionalInfoFromNode(idInfo, idNode);
	}

	/**
	 * Loscht einen Benutzer.
	 * 
	 * @param user
	 *            Das Benutzer-Objekt.
	 */
	public static void deleteUser(User user) {
		UserManager.getInstance().deleteUser(user);
	}

	/**
	 * Loescht einen Test und all seine Bestandteile.
	 * 
	 * @param id
	 *            Die Test ID
	 * @param category
	 *            Die Kategorie.
	 */
	public static void deleteTest(int id, String category) {

		NodeManager.getInstance().deleteNodesOfATest(id);
		TestManager.getInstance().deleteTest(id);
		CategoryManager.getInstance().deleteCategory(category);
	}

	/**
	 * Methode zum Abrufen der Daten eines übergebenen Benutzers aus der
	 * Datenbank
	 * 
	 * @param userName
	 *            Der zugehörige Benutzername.
	 * @return Die Benutzerdaten als Benutzer-Objekt.
	 */
	public static User getUserData(String userName) {
		return UserManager.getInstance().getUserData(userName);
	}

	/**
	 * Liefert ein Liste aller ZusatzInfos eines Knotens.
	 * 
	 * @param currentNode
	 *            Der Knoten.
	 * @return Die Liste der ZusatzInfos.
	 */
	public static LinkedList<AdditionalInfo> getMediaByNode(Node currentNode) {
		return currentNode.getNodeInfos();
	}

	/**
	 * Liefert ein Test-Objekt.
	 * 
	 * @param title
	 *            Der Testtitel.
	 * @return Das Test-Objekt.
	 */
	public static Test getTestData(String title) {
		return TestManager.getInstance().getTestObject(title);
	}

	/**
	 * Liefert ein Test-Objekt.
	 * 
	 * @param id
	 *            Die Test-ID.
	 * @return Das Test-Objekt.
	 */
	public static Test getTestData(int id) {
		return TestManager.getInstance().getTestObject(id);
	}

	/**
	 * Liefert den Benutznamen anhand der Benutzer-ID.
	 * 
	 * @param id
	 *            Die Benutzer-ID.
	 * @return Der Name.
	 */
	public static String getUsername(int id) {
		return UserManager.getInstance().getUsername(id);
	}

	/**
	 * Liefert das Frage-Objekt eines Knotens.
	 * 
	 * @param node
	 *            Der Knoten.
	 * @return Die Frage.
	 */
	public static Question getQuestionByNode(Node node) {
		return QuestionManager.getInstance().getQuestionByNode(node);
	}

	/**
	 * Liefert den Namen der Kategorie.
	 * 
	 * @param id
	 *            Die Kategorie-ID.
	 * @return Der Name.
	 */
	public static String getCategoryName(int id) {
		return CategoryManager.getInstance().getCategoryName(id);
	}

	/**
	 * Liefert die ID einer Kategorie anhand ihres Namens.
	 * 
	 * @param category
	 *            Der Name.
	 * @return Die ID.
	 */
	public static int getCategoryKey(String category) {
		return CategoryManager.getInstance().getCategoryKey(category);
	}

	/**
	 * Liefert ein Array mit allen Kategorie-Namen, die ein Benutzer vergeben
	 * hat.
	 * 
	 * @return Die Liste der Kategorie-Namen.
	 */
	public static String[] getUserCategorzList() {
		return CategoryManager.getInstance().getUserCategorzList();
	}

	/**
	 * Liefert eine Liste aller Tests eines Benutzers.
	 * 
	 * @return Die Liste der Tests.
	 */
	public static LinkedList<Test> getUserTests(User user) {
		return TestManager.getInstance().getUserTest(user.getIdUser());
	}

	/**
	 * Liefert eine Liste aller Antworten einen Knotens.
	 * 
	 * @param currentNode
	 *            Der Knoten.
	 * @return Die Liste der Antworten.
	 */
	public static LinkedList<Answer> getAnswerByNode(Node currentNode) {
		return AnswerManager.getInstance().getAnswerByNode(currentNode);
	}

	/**
	 * Liefert eine Liste von ZusatzInfos des Typs Link, die zum Knoten
	 * gehoeren.
	 * 
	 * @param currentNode
	 *            Der Knoten.
	 * @param project
	 *            Das gerade geöffnete Projekt.
	 * @return Die Liste der Links.
	 */
	public static LinkedList<AdditionalInfo> getLinkByNode(Node currentNode,
			Project project) {
		LinkedList<AdditionalInfo> linkList = new LinkedList<AdditionalInfo>();
		LinkedList<AdditionalInfo> addInfos = currentNode.getNodeInfos();
		for (int i = 0; i < addInfos.size(); i++) {
			AdditionalInfo link = new AdditionalInfo(project);
			link.setIdadditionalInfo(addInfos.get(i).getIdAdditionalInfo());
			link.setAddress(addInfos.get(i).getAddress());
			link.setType(addInfos.get(i).getType());
			link.setPosition(addInfos.get(i).getPosition());
			linkList.add(link);
		}
		return linkList;
	}

	/**
	 * Liefert ein Knoten-Objekt.
	 * 
	 * @param idNode
	 *            Die Knoten ID.
	 * @return Das Knoten-Objekt.
	 */
	public static Node getNodeData(int idNode) {
		return NodeManager.getInstance().getNodeData(idNode);
	}

	/**
	 * Lifert ein Knoten-Objekt.
	 * 
	 * @param test
	 *            Der zugehoerige Test.
	 * @param currentPosition
	 *            Die Position des Knotens.
	 * @return Das Konten-Objekt.
	 */
	public static Node getNodeData(Test test, int currentPosition) {
		return NodeManager.getInstance().getNodeData(test, currentPosition);
	}

	/**
	 * Liefert ein Bedingungs-Objekt.
	 * 
	 * @param idCondition
	 *            Die Bedingungs-ID.
	 * @return Die Bedingung.
	 */
	public static Condition getConditionData(int idCondition) {
		return ConditionManager.getInstance().getConditionData(idCondition);
	}

	/**
	 * Liefert alle Knoten eines Tests.
	 * 
	 * @param idTest
	 *            Die Test ID.
	 * @return Die Liste der Knoten.
	 */
	public static LinkedList<Node> getNodeListByTest(int idTest) {
		return NodeManager.getInstance().getNodeListByTest(idTest);
	}

	/**
	 * Liefert die Anzahl der Knoten eines Tests.
	 * 
	 * @param test
	 *            Der Test.
	 * @return Die Anzahl der Knoten.
	 */
	public static int getNumberOfNodes(Test test) {
		return TestManager.getInstance().getNumberOfNodes(test);
	}

	/**
	 * Liefert alle (ausgehenden) Kanten einer Menge von Knoten.
	 * 
	 * @param nodeList
	 *            Die Knoten-Liste (eines Tests).
	 * @return Die Kanten-Liste.
	 */
	public static LinkedList<Edge> getEdgeListByNodeList(
			LinkedList<Integer> nodeList) {
		LinkedList<Edge> edgeList = new LinkedList<Edge>();

		for (int i = 0; i < nodeList.size(); i++) {
			LinkedList<Edge> fromOneNode = getEdgeListByNode(nodeList.get(i),
					true);
			for (int j = 0; j < fromOneNode.size(); j++) {
				edgeList.add(fromOneNode.get(j));
			}
		}

		return edgeList;
	}

	/**
	 * Liefert die eingehenden oder ausgehenden Kanten eines Knotens.
	 * 
	 * @param idNode
	 *            Die Knoten ID.
	 * @param edgeIsOutGoing
	 *            true, falls ausgehende Kanten, false sonst.
	 * @return Die Kanten.
	 */
	public static LinkedList<Edge> getEdgeListByNode(int idNode,
			boolean edgeIsOutGoing) {
		return NodeManager.getInstance().getEdgeListByNode(idNode,
				edgeIsOutGoing);
	}

	/**
	 * Liefert eine Liste, die die PunkteBedingungswerte-Werte bzgl. eines
	 * bestimmten Lookbacks der ausgehenden Kanten eines Knotens enthalten
	 * 
	 * @param idNode
	 *            Die Knoten ID. Die Knoten ID.
	 * @param look
	 *            Der Lookback. Der Lookback.
	 * @return Die Liste der BedingungPunkte hinsichtlich des Lookbacks.
	 */
	public static LinkedList<Integer> getConditionOutgoingListByLookbackByNode(
			int idNode, int look) {
		LinkedList<Edge> lst = NodeManager.getInstance().getEdgeListByNode(
				idNode, true);
		LinkedList<Integer> points = new LinkedList<Integer>();
		for (Iterator<Edge> iterator = lst.iterator(); iterator.hasNext();) {
			Edge edge = (Edge) iterator.next();
			points.add(ConditionManager.getInstance()
					.getConditionOutgoingListByLookbackByNode(
							edge.getIdCondition()));
		}
		return points;
	}

	/**
	 * Liefert den vorherigen oder nachfolgenden Knoten.
	 * 
	 * @param currentNode
	 *            Der aktuelle Knoten.
	 * @param searchNextNode
	 *            true, falls nachfolgend, false sonst.
	 * @return Der Nachfolger-Knoten (bei true), der Vorgaenger-Knoten (bei
	 *         false).
	 */
	public static Node getNodeByNode(Node currentNode, boolean searchNextNode) {
		return NodeManager.getInstance().getNodeByNode(currentNode,
				searchNextNode);
	}

	/**
	 * Liefert alle oeffentlichen Tests des übergebenen Users.
	 * 
	 * @param user
	 *            Der Benutzer dessen Tests geöffnet werden sollen.
	 * 
	 * @return Die oeffentlichen Tests.
	 */
	public static LinkedList<Test> getPoolTests(User user) {
		return TestManager.getInstance().getPoolTests(user.getIdUser());
	}

	/**
	 * Liefert die Benutzernamen zur gegebenen Test-Liste (gleiche Reihenfolge).
	 * 
	 * @param poolList
	 *            Die Test-Liste.
	 * @return Die Liste der Benutzernamen.
	 */
	public static LinkedList<String> getUserNamesByTestList(
			LinkedList<Test> poolList) {
		return TestManager.getInstance().getUserNamesByTestList(poolList);
	}

	/**
	 * Liefert eine gespeicherte Datei.
	 * 
	 * @param filename
	 *            Der Datei-Name.
	 * @param path
	 *            Der Pfad unter dem die Datei gespeichert werden soll.
	 * @return Die Datei.
	 */
	public static File getMediaFile(String filename, String path)
			throws QuizException {
		File file = AdditionalInfoManager.getInstance()
				.getAdditionalInfoByMediaName(filename).getMediaBlob();
		try {
			FileInputStream is = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(path);
			byte[] buff = new byte[8192];
			int len;
			while (0 < (len = is.read(buff)))
				fos.write(buff, 0, len);
			fos.close();
			is.close();
		} catch (FileNotFoundException cause) {
			throw new QuizException("Die Datei " + filename
					+ " konnte nicht aus der Datenbank geladen werden!\n",
					cause);

		} catch (IOException cause) {
			throw new QuizException("Die Datei " + filename
					+ " konnte nicht aus der Datenbank geladen werden!\n",
					cause);
		}
		return file;
	}

	/**
	 * Liefert die ID einer Bedingung.
	 * 
	 * @param conditionLookback
	 *            Der Lookback.
	 * @param conditionPoints
	 *            Die PunkteBedingung.
	 * @return Die Bedingung.
	 */
	public static int getConditionId(int conditionLookback, int conditionPoints) {
		return ConditionManager.getInstance().getConditionId(conditionLookback,
				conditionPoints);
	}

	/**
	 * Liefert eine Titel-Liste der von einem Benutzer erstellten Tests.
	 * 
	 * @param databaseBotName
	 *            Der Benutzername.
	 * @return Die Titel-Liste.
	 */
	public static LinkedList<String> getTestTitlesByUsername(
			String databaseBotName) {
		return TestManager.getInstance().getTestTitlesByUsername(
				databaseBotName);
	}

	/**
	 * Importiert (bzw. kopiert) einen kompletten Test in einen angegebenen
	 * Benutzer-Account.
	 * 
	 * @param userId
	 *            Die ID des Benutzer-Accounts.
	 * @param testId
	 *            Die Test ID.
	 * @param newTitle
	 *            Der Titel.
	 * @return Den importierten Test.
	 */
	public static Test importTest(int userId, int testId, String newTitle,
			Project project) {

		Test testToImport = getTestData(testId);
		testToImport.setIdUser(userId);
		testToImport.setIdTest(-1);
		if (newTitle.length() > 0)
			testToImport.setTitle(newTitle);
		
		int newTestId = setTestData(testToImport);
		testToImport.setIdTest(newTestId);

		LinkedList<Node> nodesToImport = getNodeListByTest(testId);
		LinkedList<Node> newNodeList = new LinkedList<Node>();

		for (int i = 0; i < nodesToImport.size(); i++) {
			Node newNode = new Node(project);
			newNode.setIdTest(newTestId);
			newNode.setPosition(nodesToImport.get(i).getPosition());
			newNode.setPoints(nodesToImport.get(i).getPoints());
			newNode.setHasQuestion(nodesToImport.get(i).getHasQuestion());
			int idNewNode = setNodeData(newNode);
			newNode.setIdNode(idNewNode);
			newNodeList.add(newNode);

		}

		LinkedList<Integer> idListNodesToImport = new LinkedList<Integer>();

		for (int i = 0; i < nodesToImport.size(); i++) {
			idListNodesToImport.add(nodesToImport.get(i).getIdNode());
		}

		LinkedList<Edge> edgesToImport = getEdgeListByNodeList(idListNodesToImport);

		for (int i = 0; i < edgesToImport.size(); i++) {
			int posSource = NodeManager.getInstance().getPosition(
					edgesToImport.get(i).getIdNodeSource());
			int posDestination = NodeManager.getInstance().getPosition(
					edgesToImport.get(i).getIdNodeDestination());
			int idCondition = EdgeManager.getInstance().getConditionId(
					edgesToImport.get(i).getIdNodeSource(),
					edgesToImport.get(i).getIdNodeDestination());
			if (posSource != -1 && posDestination != -1 && idCondition != -1) {
				int idSource = NodeManager.getInstance().getIdByPosition(
						posSource, newTestId);
				int idDestination = NodeManager.getInstance().getIdByPosition(
						posDestination, newTestId);

				Edge newEdge = new Edge(project, idSource, idDestination,
						idCondition, getNodeData(idDestination));
				setEdgeData(newEdge);
			}
		}

		return testToImport;
	}

	/**
	 * Ueberprueft ob ein bestimmter Benutzername in der Datenbank enthalten
	 * ist.
	 * 
	 * @param user
	 *            Der gesuchte Benutzername.
	 * @return true, falls Benutzername in Datenbank, false sonst
	 */
	public static boolean isUserInDB(String user) {
		return UserManager.getInstance().isUserInDB(user);
	}

	/**
	 * Ueberprueft, ob in einem Benutzer-Account bereits ein Test mit
	 * angegebenen Titel existiert.
	 * 
	 * @param testName
	 *            Der Titel.
	 * @param userID
	 *            Die Benutzer-Account ID.
	 * @return true, falls schon vorhanden, false sonst.
	 */
	public static boolean isTestNameInPrivateDB(String testName, int userID) {
		return TestManager.getInstance()
				.isTestNameInPrivateDB(testName, userID);
	}

	/**
	 * Erstellt einen Benutzer in der Datenbank.
	 * 
	 * @param userName
	 *            Der Benutzername.
	 * @param password
	 *            Das Passwort.
	 * @param firstName
	 *            Der Vorname.
	 * @param lastName
	 *            Der Nachname.
	 * @param email
	 *            Die Emailadresse
	 * @return true, wenn erfolgreich, false im Fehlerfall
	 */
	public static boolean setUserData(String userName, String password,
			String firstName, String lastName, String email) {
		return UserManager.getInstance().setUserData(userName, password,
				firstName, lastName, email);
	}

	/**
	 * Speichert einen Test.
	 * 
	 * @param test
	 *            Der Test.
	 * @return Die von der DB generierte ID. -1 im Fehlerfall.
	 */
	public static int setTestData(Test test) {
		int id = TestManager.getInstance().setTestData(test);
		return id;
	}

	/**
	 * Speichert eine Kategorie.
	 * 
	 * @param category
	 *            Der Kategorie-Name.
	 * @return Die von der DB generierte ID. -1 im Fehlerfall.
	 */
	public static int setCategoryData(String category) {
		return CategoryManager.getInstance().setCategoryData(category);
	}

	/**
	 * Speichert einen Knoten.
	 * 
	 * @param node
	 *            Der Knoten.
	 * @return Die von der DB generierte ID. -1 im Fehlerfall.
	 */
	public static int setNodeData(Node node) {
		return NodeManager.getInstance().setNodeData(node);
	}

	/**
	 * Speichert eine Antwort.
	 * 
	 * @param answer
	 *            Die Antwort.
	 * @return Die von der DB generierte ID. -2 im Fehlerfall.
	 */
	public static int setAnswerData(Answer answer) {
		return AnswerManager.getInstance().setAnswerData(answer);
	}

	/**
	 * Speichert eine Frage.
	 * 
	 * @param question
	 *            Die Frage.
	 * @return Die von der DB generierte ID. -1 im Fehlerfall.
	 */
	public static int setQuestionData(Question question) {
		return QuestionManager.getInstance().setQuestionData(question);
	}

	/**
	 * Speichert eine Kante.
	 * 
	 * @param edge
	 *            Die Kante.
	 */
	public static void setEdgeData(Edge edge) {
		EdgeManager.getInstance().setEdgeData(edge);
	}

	/**
	 * Speichert eine Bedingung.
	 * 
	 * @param condition
	 *            Die Bedingung.
	 * @return Die von der DB generierte ID. -1 im Fehlerfall.
	 */
	public static int setConditionData(Condition condition) {
		return ConditionManager.getInstance().setConditionData(condition);
	}

	/**
	 * Speichert eine ZusatzInfo des Typs Media.
	 * 
	 * @param additionalInfo
	 *            Die ZusatzInfo (Typ Media).
	 * @param node
	 *            Der Knoten.
	 * @return Die von der DB generierte ID. -2 im Fehlerfall.
	 */
	public static int setAdditionalInfoMediaData(AdditionalInfo additionalInfo,
			Node node) {
		return AdditionalInfoManager.getInstance().addAdditionalInfo(
				additionalInfo, node);
	}

	/**
	 * Speichert eine ZusatzInfo vom Typ Link.
	 * 
	 * @param additionalInfo
	 *            Die ZUsatzInfo (Typ Link).
	 * @param node
	 *            Der Knoten.
	 * @return Die von der DB generierte ID. -2 im Fehlerfall.
	 */
	public static int setAdditionalInfoLinkData(AdditionalInfo additionalInfo,
			Node node) {
		return AdditionalInfoManager.getInstance().setAdditionalInfoLinkData(
				additionalInfo, node);
	}

	/**
	 * Erhoeht den Positions-Wert aller Nachfolger-Knoten um eins (Verschiebt
	 * die Position um eins nach rechts.).
	 * 
	 * @param idTest
	 *            Die Test ID.
	 * @param startPosition
	 *            Die Startposition.
	 * @param endPosition
	 *            Die Endposition.
	 */
	public static void shiftTestNodesRight(int idTest, int startPosition,
			int endPosition) {

		while (endPosition >= startPosition) {
			NodeManager.getInstance().shiftTestNodesRight(idTest, endPosition);
			endPosition--;
		}

	}

	/**
	 * Verringert den Positions-Wert aller Nachfolger-Knoten um eins (Verschiebt
	 * die Position um eins nach links.).
	 * 
	 * @param idTest
	 *            Die Test ID.
	 * @param startPosition
	 *            Die Startposition.
	 */
	public static void shiftTestNodesLeft(int idTest, int startPosition) {
		NodeManager.getInstance().shiftTestNodesLeft(idTest, startPosition);
	}

	/**
	 * Aktualisiert einen Test (ueber ID).
	 * 
	 * @param test
	 *            Der aktuelle Test.
	 */
	public static void updateTestData(Test test) {
		int categoryId = setCategoryData(test.getCategory());
		if (categoryId == -1) {
			categoryId = getCategoryKey(test.getCategory());
		}
		String cat = getCategoryName(categoryId);
		TestManager.getInstance().updateTestData(test, cat);
	}

	/**
	 * Aktualisiert eine Kante (ueber Quell- und Zielknoten-ID).
	 * 
	 * @param newCondition
	 *            Die neue Bedingung.
	 * @param idSource
	 *            Die Quellknoten ID.
	 * @param idDestination
	 *            Die Zielknoten ID.
	 */
	public static void updateEdgeCondition(Condition newCondition,
			int idSource, int idDestination) {
		int id_Bedingung = setConditionData(newCondition);
		EdgeManager.getInstance().updateEdgeCondition(newCondition, idSource,
				idDestination, id_Bedingung);
	}

	/**
	 * Aktualisiert einen Knoten (ueber ID).
	 * 
	 * @param node
	 *            Der aktuelle Knoten.
	 */
	public static void updateNodeData(Node node) {
		NodeManager.getInstance().updateNodeData(node);
	}

	/**
	 * Aktualisiert einen Benutzer (ueber Benutzername).
	 * 
	 * @param userName
	 *            Der Benutzername.
	 * @param password
	 *            Das Passwort.
	 * @param firstName
	 *            Der Vorname.
	 * @param lastName
	 *            Der Nachname.
	 * @param email
	 *            Die Emailadresse.
	 */
	public static void updateUserData(String userName, String password,
			String firstName, String lastName, String email) {
		UserManager.getInstance().setUserData(userName, password, firstName,
				lastName, email);
	}

	/**
	 * Aktualisiert die Position einer ZusatzInfo.
	 * 
	 * @param positionDeleted
	 *            Die Position auf die die ZUsatzInfo gesetzt werden soll.
	 * @param type
	 *            Der ZusatzInfo-Typ (0=Link, 1=Video, 2=Audio, 3=Image).
	 * @param idNode
	 *            Die Knoten ID.
	 */
	public static void updateAdditionalInfoPosition(int positionDeleted,
			int type, int idNode) {
		LinkedList<Integer> idList = AdditionalInfoManager.getInstance()
				.getIdUpdateFunction(positionDeleted, type, idNode);
		for (int i = 0; i < idList.size(); i++) {
			AdditionalInfoManager.getInstance()
					.decrementPosition(idList.get(i));
		}
	}

	public static void removeAnswersFromNodeId(int nodeId) {
		AnswerManager.getInstance().removeAnswersFromNode(nodeId);
	}
}
