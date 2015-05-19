package org.iviPro.model.quiz;

import java.util.Iterator;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class ConditionManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;
	
	private static ConditionManager INSTANCE;

	private BeanList<Condition> conList;
	
	private ConditionManager(Project project) {
		super(project);
	}
	
	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new ConditionManager(project);
		}
	}
	
    public static ConditionManager getInstance() {
        return INSTANCE;
    }
	
	public void removeCondition(int idCondition) {
		for (Iterator<Condition> iterator = conList.iterator(); iterator.hasNext();) {
			Condition con = (Condition) iterator.next();
			if(con.getIdCondition() == idCondition) {
				iterator.remove();
			}
		}
	}
	
	public int setConditionData(Condition condition) {
		boolean isAvailable = false;
		int id = -1;
		for (Iterator<Condition> iterator = conList.iterator(); iterator.hasNext();) {
			Condition con = (Condition) iterator.next();
			if(con.getConditionLookback() == condition.getConditionLookback() && con.getConditionPoints() == condition.getConditionPoints()) {
				isAvailable = true;
				id = con.getIdCondition();
			}
		}
		if(!isAvailable) {
			condition.setIdCondition(++key);
			conList.add(condition);
			return key;
		} else {
			condition.setIdCondition(id);
			return id;
		}
	}
	
	public Condition getConditionData(int idCondition) {
		Condition condition = new Condition(project);
		for (Iterator<Condition> iterator = conList.iterator(); iterator.hasNext();) {
			Condition con = (Condition) iterator.next();
			if(con.getIdCondition() == idCondition) {
				return con;
			}
		}
		return condition;
	}
	
	public int getConditionOutgoingListByLookbackByNode(int idCondition) {
		for (Iterator<Condition> iterator = conList.iterator(); iterator.hasNext();) {
			Condition con = (Condition) iterator.next();
			if(con.getIdCondition() == idCondition) {
				return con.getConditionPoints();
			}
		}
		return -1;
	}
	
	public int getConditionId(int conditionLookback, int conditionPoints) {
		for (Iterator<Condition> iterator = conList.iterator(); iterator.hasNext();) {
			Condition con = (Condition) iterator.next();
			if(con.getConditionLookback() == conditionLookback && con.getConditionPoints() == conditionPoints) {
				return con.getIdCondition();
			}
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
    	conList = (BeanList<Condition>)list;	
	}

}
