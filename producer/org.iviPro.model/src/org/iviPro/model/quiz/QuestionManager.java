package org.iviPro.model.quiz;

import java.util.Iterator;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class QuestionManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;
	
	private static QuestionManager INSTANCE;

	private BeanList<Question> quesList;
	
	private QuestionManager(Project project) {
		super(project);
	}
	
	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new QuestionManager(project);
		}
	}
	
    public static QuestionManager getInstance() {
        return INSTANCE;
    }
	
	public void removeQuestion(int questionId) {
		for (Iterator<Question> iterator = quesList.iterator(); iterator.hasNext();) {
			Question ques = (Question) iterator.next();
			if(ques.getIdQuestion() == questionId) {
				iterator.remove();
			}
		}
	}
	
	public int setQuestionData(Question question) {
		boolean isAvailable = false;
		int id = -1;
		for (Iterator<Question> iterator = quesList.iterator(); iterator.hasNext();) {
			Question ques = (Question) iterator.next();
			if(ques.getQuestionText().equalsIgnoreCase(question.getQuestionText())) {
				isAvailable = true;
				id = ques.getIdQuestion();
			}
		}
		if(!isAvailable) {
			question.setIdQuestion(++key);
			quesList.add(question);
			return key;
		} else {
			question.setIdQuestion(id);
			return id;
		}
	}
	
	public Question getQuestionByNode(Node node) {
		int quesId = node.getHasQuestion();
		for (Iterator<Question> iterator = quesList.iterator(); iterator.hasNext();) {
			Question ques = (Question) iterator.next();
			if(ques.getIdQuestion() == quesId) {
				return ques;
			}
		}
		return new Question(project);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
    	quesList = (BeanList<Question>)list;	
    	
    	//Set key!
    	int maxKey = 0;
    	for (Iterator<Question> iterator = quesList.iterator(); iterator.hasNext();) {
    		Question ques = (Question) iterator.next();
			if(ques.getIdQuestion() > maxKey) {
				maxKey = ques.getIdQuestion();
			}
		}
    	key = maxKey;
	}
}
