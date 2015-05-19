package org.iviPro.model.quiz;

import java.util.Iterator;
import java.util.LinkedList;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class AnswerManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;
	
	private static AnswerManager INSTANCE;

	private BeanList<Answer> answerList;
	
	private AnswerManager(Project project) {
		super(project);
	}
	
	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new AnswerManager(project);
		}
	}
	
    public static AnswerManager getInstance() {
        return INSTANCE;
    }
	
	public void removeAnswer(int ansId) {
		for (Iterator<Answer> iterator = answerList.iterator(); iterator.hasNext();) {
			Answer answer = (Answer) iterator.next();
			if(answer.getIdAnswer() == ansId) {
				iterator.remove();
			}
		}
	}
	
	public void removeAnswersFromNode(int nodeId) {
		for (Iterator<Answer> iterator = answerList.iterator(); iterator.hasNext();) {
			Answer ans = (Answer) iterator.next();
			if(ans.getIdNode() == nodeId) {
				iterator.remove();
//				removeAnswersFromNode(nodeId);
			}
		}
	}
	
	public LinkedList<Integer> getAnswerListByNodeId(int nodeId) {
		LinkedList<Integer> lst = new LinkedList<Integer>();
		for (Iterator<Answer> iterator = answerList.iterator(); iterator.hasNext();) {
			Answer answer = (Answer) iterator.next();
			if(answer.getIdNode() == nodeId) {
				lst.add(answer.getIdAnswer());
			}
		}
		return lst;
	}
	
	public int setAnswerData(Answer answer) {
		boolean isAvailable = false;;
		for (Iterator<Answer> iterator = answerList.iterator(); iterator.hasNext();) {
			Answer ans = (Answer) iterator.next();
			if(ans.getIdNode() == answer.getIdNode() && ans.getAnswerText().equalsIgnoreCase(answer.getAnswerText()) && ans.getIsCorrect() == answer.getIsCorrect()  && ans.getPositionAnswer() == answer.getPositionAnswer()) {
				isAvailable = true;
			}
		}
		if(!isAvailable) {
			answer.setIdAnswer(++key);
			answerList.add(answer);
			return key;
		} else {
			return -1;
		}
	}
	
	public LinkedList<Answer> getAnswerByNode(Node node) {
		LinkedList<Answer> lst = new LinkedList<Answer>();
		for (Iterator<Answer> iterator = answerList.iterator(); iterator.hasNext();) {
			Answer answer = (Answer) iterator.next();
			if(answer.getIdNode() == node.getIdNode()) {
				lst.add(answer);
			}
		}
		return lst;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
		answerList = (BeanList<Answer>)list;
		
    	//Set key!
    	int maxKey = 0;
    	for (Iterator<Answer> iterator = answerList.iterator(); iterator.hasNext();) {
    		Answer answer = (Answer) iterator.next();
			if(answer.getIdAnswer() > maxKey) {
				maxKey = answer.getIdAnswer();
			}
		}
    	key = maxKey;
	}
}
