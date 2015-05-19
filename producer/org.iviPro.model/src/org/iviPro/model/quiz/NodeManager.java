package org.iviPro.model.quiz;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class NodeManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;

	private static NodeManager INSTANCE;

	private BeanList<Node> nodeList;

	private NodeManager(Project project) {
		super(project);
	}

	public static void createInstance(Project project) {
		if (INSTANCE == null) {
			INSTANCE = new NodeManager(project);
		}
	}

	public static NodeManager getInstance() {
		return INSTANCE;
	}
	
	public void updateAmountPoints(Test test, int amountPoints) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node no = (Node) iterator.next();
			if(no.getIdTest() == test.getIdTest()) {
				if(no.getPoints() > amountPoints) {
					no.setPoints(amountPoints);
				}
			}
		}
	}

	public void removeNode(int nodeId) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == nodeId) {
				iterator.remove();
				break;
			}
		}
	}

	public LinkedList<Integer> getAddInfoIdsByNodeId(int nodeId) {
		LinkedList<Integer> lst = new LinkedList<Integer>();
		LinkedList<AdditionalInfo> values = null;
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == nodeId) {
				values = node.getNodeInfos();
			}
		}
		if (values != null) {
			for (int i = 0; i < values.size(); i++) {
				lst.add(values.get(i).getIdAdditionalInfo());
			}
		}
		return lst;
	}

	public LinkedList<Integer> getQuestionsIds(int nodeId) {
		LinkedList<Integer> lst = new LinkedList<Integer>();
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == nodeId) {
				lst.add(node.getHasQuestion());
			}
		}
		return lst;
	}

	public int getAmountNodes(int testId) {
		int amount = 0;
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdTest() == testId) {
				amount++;
			}
		}
		return amount;
	}

	public int setNodeData(Node node) {
		nodeList.add(node);
		return ++key;
	}

	public void removeAdditionalInfoFromNode(int idInfo, int idNode) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == idInfo) {
				node.removeAdditionalInfo(idInfo);
			}
		}
	}

	public void deleteNodesOfATest(int testId) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdTest() == testId) {
				LinkedList<Integer> questionIDs = NodeManager.getInstance()
						.getQuestionsIds(node.getIdNode());
				for (Iterator<Integer> iterator2 = questionIDs.iterator(); iterator2
						.hasNext();) {
					Integer integer = (Integer) iterator2.next();
					QuestionManager.getInstance().removeQuestion(integer);
				}

				List<Answer> answerList = AnswerManager.getInstance()
						.getAnswerByNode(node);
				for (Iterator<Answer> iterator2 = answerList.iterator(); iterator2
						.hasNext();) {
					Answer answer = (Answer) iterator2.next();
					AnswerManager.getInstance().removeAnswer(
							answer.getIdAnswer());
				}

				LinkedList<Integer> infoIDs = getAddInfoIdsByNodeId(node
						.getIdNode());
				for (Iterator<Integer> iterator2 = infoIDs.iterator(); iterator2
						.hasNext();) {
					Integer integer = (Integer) iterator2.next();
					AdditionalInfoManager.getInstance().removeInfo(integer);
				}

				LinkedList<Integer> conditionIDs = EdgeManager.getInstance()
						.getConditionIdsByNodeId(node.getIdNode());
				for (Iterator<Integer> iterator2 = conditionIDs.iterator(); iterator2
						.hasNext();) {
					Integer integer = (Integer) iterator2.next();
					ConditionManager.getInstance().removeCondition(integer);
				}
				iterator.remove();
			}
		}
	}

	public Node getNodeData(int idNode) {
		Node no = new Node(project);
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == idNode) {
				return node;
			}
		}
		return no;
	}

	public Node getNodeData(Test test, int currentPosition) {
		Node no = new Node(project);
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdTest() == test.getIdTest()
					&& node.getPosition() == currentPosition) {
				return node;
			}
		}
		return no;
	}

	public LinkedList<Node> getNodeListByTest(int idTest) {
		LinkedList<Node> lst = new LinkedList<Node>();
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdTest() == idTest) {
				lst.add(node);
			}
		}
		return lst;
	}

	public LinkedList<Edge> getEdgeListByNode(int idNode, boolean edgeIsOutgoing) {
		LinkedList<Edge> lst = new LinkedList<Edge>();
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == idNode) {
				if (edgeIsOutgoing) {
					LinkedList<Edge> adj = EdgeManager.getInstance()
							.getEdgesByNodeId(node.getIdNode());
					for (int i = 0; i < adj.size(); i++) {
						if (adj.get(i).getIdNodeSource() == idNode) {
							lst.add(adj.get(i));
						}
					}
				} else {
					LinkedList<Edge> adj = EdgeManager.getInstance()
							.getEdgesByNodeId(node.getIdNode());
					for (int i = 0; i < adj.size(); i++) {
						if (adj.get(i).getIdNodeDestination() == idNode) {
							lst.add(adj.get(i));
						}
					}
				}
			}
		}
		return lst;
	}

	public Node getNodeByNode(Node currentNode, boolean searchNextNode) {
		Node node = null;
		List<Node> testNodeList = getNodeListByTest(currentNode.getIdTest());
		if (searchNextNode) {
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < testNodeList.size(); i++) {
				if (testNodeList.get(i).getIdNode() > currentNode.getIdNode()
						&& min > testNodeList.get(i).getIdNode()) {
					min = i;
					node = testNodeList.get(i);
				}
			}
		} else {
			int max = Integer.MIN_VALUE;
			for (int i = 0; i < testNodeList.size(); i++) {
				if(testNodeList.get(i).getIdNode() < currentNode.getIdNode() && max < testNodeList.get(i).getIdNode()) {
					max = i;
					node = testNodeList.get(i);
				}
			}
		}
		return node;
	}

	public int getPosition(int nodeId) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == nodeId) {
				return node.getPosition();
			}
		}
		return -1;
	}

	public int getIdByPosition(int pos, int testId) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdTest() == testId && node.getPosition() == pos) {
				return node.getIdNode();
			}
		}
		return -1;
	}

	public void shiftTestNodesRight(int idTest, int endPosition) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdTest() == idTest && node.getPosition() == endPosition) {
				node.setPosition(node.getPosition() + 1);
				break;
			}
		}
	}

	public void shiftTestNodesLeft(int idTest, int startPosition) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdTest() == idTest
					&& node.getPosition() == startPosition) {
				node.setPosition(node.getPosition() - 1);
				break;
			}
		}
	}

	public void updateNodeData(Node no) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == no.getIdNode()
					&& node.getIdTest() == no.getIdTest()) {
				node.setPosition(no.getPosition());
				node.setPoints(no.getPoints());
				node.setHasQuestion(no.getHasQuestion());
				node.setRandom(no.isRandom());
			}
		}
	}

	public LinkedList<AdditionalInfo> getNodeByAddInfoID(int id) {
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() == id) {
				return node.getNodeInfos();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
		nodeList = (BeanList<Node>) list;
		// Set key!
		int maxKey = 0;
		for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.getIdNode() > maxKey) {
				maxKey = node.getIdNode();
			}
		}
		key = maxKey;
	}
}