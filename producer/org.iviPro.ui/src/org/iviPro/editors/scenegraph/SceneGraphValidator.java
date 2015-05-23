package org.iviPro.editors.scenegraph;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.application.Application;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeStart;

public class SceneGraphValidator {

	private LinkedList<QuizButton> buttonList;

	private IGraphNode troubleSpot;
	private String errorMessage;

	public boolean validateSceneGraph() {
		Graph sceneGraph = Application.getCurrentProject()
				.getSceneGraph();

		TreeSet<IGraphNode> nodes = new TreeSet<IGraphNode>(sceneGraph.getNodes());
		
		Queue<IGraphNode> workQueue = new LinkedList<IGraphNode>();
		workQueue.add(sceneGraph.getStart());
		
		while (!workQueue.isEmpty()) {
			IGraphNode actualNode = workQueue.remove();
			// Check whether node has been processed before
			if (nodes.contains(actualNode)) {
				if (!actualNode.validateNode()) {
					errorMessage = actualNode.getValidationError();
					return false;
				} else {
					if (actualNode.getChildren().size() == 0 &&
							!(actualNode instanceof NodeEnd
									|| actualNode instanceof INodeAnnotationLeaf)) {
						errorMessage = "The end node is not reachable from node:\n\n " 
								+ actualNode.getTitle() + " (id:" + actualNode.getNodeID() +")";
						return false;
					}
					workQueue.addAll(actualNode.getChildren());
					nodes.remove(actualNode);
				}
			}
		}
		
		if (!nodes.isEmpty()) {
			StringBuilder unreachable = new StringBuilder();
			for (IGraphNode node : nodes) {
				unreachable.append(node.getTitle()).append(" (id:").append(node.getNodeID()).append(")").append("\n");
			}
			errorMessage = "The following elements are not reachable from the start node:\n\n" + unreachable.toString();
			return false;
		}
		
		return true;
		
		
		
		
//		
//		int visitableNodeCount = 0;
//		for (IGraphNode iGraphNode : allNodes) {
//			if (iGraphNode instanceof NodeScene
//					|| iGraphNode instanceof AbstractNodeSelection
//					|| iGraphNode instanceof AbstractNodeSelectionControl
//					|| iGraphNode instanceof NodeStart
//					|| iGraphNode instanceof NodeEnd
//					|| iGraphNode instanceof NodeQuiz
//					|| iGraphNode instanceof NodeQuizControl
//					|| iGraphNode instanceof NodeRandomSelection
//					|| iGraphNode instanceof NodeResume) {
//				visitableNodeCount++;
//			}
//		}
//
//		IGraphNode start = completeSceneGraph.getStart();
//		IGraphNode end = completeSceneGraph.getEnd();
//		boolean valid = true;
//		LinkedList<IGraphNode> worked = new LinkedList<IGraphNode>();
//		LinkedList<IGraphNode> toVisit = new LinkedList<IGraphNode>();
//
//		if (start.getChildren().isEmpty()) {
//			// nothing connected to the graph start
//			troubleSpot = start;
//			return false;
//		}
//		for (IGraphNode iGraphNode : start.getChildren()) {
//			toVisit.add(iGraphNode);
//			worked.add(start);
//		}
//
//		while (valid && !toVisit.isEmpty()) {
//			// check elements while no violation detecteds
//			IGraphNode checkingNode = toVisit.getFirst();
//			if (!worked.contains(checkingNode)) {
//				worked.add(checkingNode);
//				List<IGraphNode> children = checkingNode.getChildren();
//
//				if (!children.isEmpty()) {
//					boolean hasGraphChild = false;
//					for (IGraphNode iGraphNode : children) {
//						if (iGraphNode instanceof NodeScene
//								|| iGraphNode instanceof AbstractNodeSelection
//								|| iGraphNode instanceof AbstractNodeSelectionControl
//								|| iGraphNode instanceof NodeQuiz
//								|| iGraphNode instanceof NodeQuizControl
//								|| iGraphNode instanceof NodeRandomSelection
//								|| iGraphNode instanceof NodeResume
//								|| iGraphNode instanceof NodeStart
//								|| iGraphNode instanceof NodeEnd) {
//							if (!worked.contains(iGraphNode)) {
//								toVisit.add(iGraphNode);
//							}
//							hasGraphChild = true;
//						}
//					}
//					if (!hasGraphChild) {
//						valid = false;
//					}
//				} else {
//					valid = checkingNode.equals(end);
//					if (!valid) {
//						troubleSpot = checkingNode;
//					}
//				}
//			}
//			toVisit.remove(checkingNode);
//		}
//		// Check SceneGraph Control after Quizbutton!
//		worked = new LinkedList<IGraphNode>();
//		toVisit = new LinkedList<IGraphNode>();
//		if (start.getChildren().isEmpty()) {
//			return false;
//		}
//		for (IGraphNode iGraphNode : start.getChildren()) {
//			toVisit.add(iGraphNode);
//			worked.add(start);
//		}
//
//		while (valid && !toVisit.isEmpty()) {
//			IGraphNode checkingNode = toVisit.getFirst();
//			if (!worked.contains(checkingNode)) {
//				worked.add(checkingNode);
//				List<IGraphNode> children = checkingNode.getChildren();
//				if (!children.isEmpty()) {
//					boolean hasGraphChild = false;
//					for (IGraphNode iGraphNode : children) {
//						if (iGraphNode instanceof NodeScene
//								|| iGraphNode instanceof AbstractNodeSelection
//								|| iGraphNode instanceof AbstractNodeSelectionControl
//								|| iGraphNode instanceof NodeQuiz
//								|| iGraphNode instanceof NodeQuizControl
//								|| iGraphNode instanceof NodeRandomSelection
//								|| iGraphNode instanceof NodeResume
//								|| iGraphNode instanceof NodeStart
//								|| iGraphNode instanceof NodeEnd) {
//							if (!worked.contains(iGraphNode)) {
//								toVisit.add(iGraphNode);
//							}
//							if (iGraphNode instanceof NodeQuiz) {
//								
//								// Überprüft ob gültiges Quiz ausgewählt
//								NodeQuiz quiz = (NodeQuiz) iGraphNode;
//								if(quiz.getTestId()==-1 || DbQueries.getTestData(quiz.getTestId())==null){
//									troubleSpot = quiz;
//									return false;
//								}
//								this.buttonList = new LinkedList<SceneGraphValidator.QuizButton>();
//								List<IGraphNode> ch = iGraphNode.getChildren();
//								for (Iterator<IGraphNode> iterator = ch
//										.iterator(); iterator.hasNext();) {
//									IGraphNode no = (IGraphNode) iterator
//											.next();
//									if (!(no instanceof NodeQuizControl)) {
//										troubleSpot = no;
//										valid = false;
//									} else {
//										((NodeQuiz) iGraphNode)
//												.setDefaultControl((NodeQuizControl) no);
//										NodeQuizControl node = (NodeQuizControl) no;
//										// String points[] =
//										// node.getAmountPoints().split("-");
//										buttonList.add(new QuizButton(node
//												.getMinValue(), node
//												.getMaxValue()));
//										if (!checkButtons()) {
//											troubleSpot = node;
//											valid = false;
//										}
//									}
//								}
//							}
//							hasGraphChild = true;
//						}
//					}
//					if (!hasGraphChild) {
//						valid = false;
//					}
//				} else {
//					valid = checkingNode.equals(end);
//					if (!valid) {
//						troubleSpot = checkingNode;
//					}
//				}
//			}
//			toVisit.remove(checkingNode);
//		}
//
//		if (visitableNodeCount != worked.size()) {
//			valid = false;
//		}
//
//		return valid;
//	}
//
//	private boolean checkButtons() {
//		Collections.sort(buttonList);
//		for (int i = 0; i < buttonList.size() - 1; i++) {
//			if (!((buttonList.get(0).getEndNumber() + 1) == buttonList.get(1)
//					.getStartNumber())) {
//				return false;
//			}
//		}
//		return true;
	}
	
	/**
	 * Open a dialog on the given shell showing information about the validation
	 * status of the scene graph of the current project. If the graph could not
	 * be validated successfully the dialog offers further details about the
	 * problem.
	 *  
	 * @param shell {@link Shell} in which the dialog should be opened
	 */
	public void requestValidationDialog(Shell shell) {
		MessageDialog messageDialog;
		if (validateSceneGraph()) {
			messageDialog = new MessageDialog(shell,
					Messages.SceneGraphValidator_Dialog_Title, null, 
					Messages.SceneGraphValidator_ValidSceneGraph, 
					MessageDialog.INFORMATION,
					new String[] { Messages.SceneGraphValidator_Ok_Button }, 0); 
		} else {
			messageDialog = new MessageDialog(shell,
					Messages.SceneGraphValidator_Dialog_Title, null,
					Messages.SceneGraphValidator_InvalidScenegraph,
					MessageDialog.ERROR, 
					new String[] {
							Messages.SceneGraphValidator_Ok_Button,
							Messages.SceneGraphValidator_MoreInformation_Button
							}, 0);
		}
		// if more info requested
		if (messageDialog.open() != 0) {
			messageDialog = new MessageDialog(shell,
					Messages.SceneGraphValidator_Dialog_Title, null,  //$NON-NLS-1$
					errorMessage, MessageDialog.INFORMATION,
					new String[] { Messages.SceneGraphValidator_Ok_Button }, 0);
			messageDialog.open();
		} 
	}
	
	/**
	 * Returns an error message associated with the validation problem if a
	 * scene graph can not be validated by {@link #validateSceneGraph()}.
	 * An empty string will be returned if no problem occurred.
	 * @return error message describing the validation problem
	 */
	private String getErrorMessage() {
		String message;
		if (troubleSpot instanceof NodeStart) {
			message = Messages.SceneGraphValidator_StartNotConnected_Error;
		} else if (troubleSpot instanceof NodeQuizControl) {
			message = Messages.SceneGraphValidator_QuizAlternative_Error;
		} else if (troubleSpot instanceof NodeQuiz) {
			message = Messages.SceneGraphValidator_Quiz_Error;
		} else if (troubleSpot instanceof NodeScene) {
			message = Messages.SceneGraphValidator_Scene_Error;
		} else if (troubleSpot == null) {
			message = Messages.SceneGraphValidator_Unknown_Error;
		} else {
			message = troubleSpot.getTitle();
		}
		return message;
	}

	class QuizButton implements Comparable<QuizButton> {

		private int startNumber;

		private int endNumber;

		QuizButton(int startNumber, int endNumber) {
			this.startNumber = startNumber;
			this.endNumber = endNumber;
		}

		@Override
		public int compareTo(QuizButton o) {
			if (startNumber < o.getStartNumber()) {
				return -1;
			} else if (startNumber == o.getStartNumber()) {
				return 0;
			} else {
				return 1;
			}
		}

		int getStartNumber() {
			return startNumber;
		}

		int getEndNumber() {
			return endNumber;
		}
	}
}
