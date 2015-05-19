package org.iviPro.model.quiz;

import java.util.Iterator;
import java.util.LinkedList;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public final class AdditionalInfoManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;
	
	private static AdditionalInfoManager INSTANCE;

	private BeanList<AdditionalInfo> addList;
	
	private AdditionalInfoManager(Project project) {
		super(project);
	}
	
	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new AdditionalInfoManager(project);
		}
	}
	
    public static AdditionalInfoManager getInstance() {
        return INSTANCE;
    }
    
	public void removeInfo(int infoId) {
		for (Iterator<AdditionalInfo> iterator = addList.iterator(); iterator.hasNext();) {
			AdditionalInfo info = (AdditionalInfo) iterator.next();
			if(info.getIdAdditionalInfo() == infoId) {
				iterator.remove();			
			}
		}
	}
	
	public int setAdditionalInfoLinkData(AdditionalInfo additionalInfo,
			Node node) {
		for (Iterator<AdditionalInfo> iterator = addList.iterator(); iterator.hasNext();) {
			AdditionalInfo info = (AdditionalInfo) iterator.next();
			if(info.getAddress().toString().equalsIgnoreCase(additionalInfo.getAddress().toString()) && info.getType() == additionalInfo.getType()) {
				return info.getIdAdditionalInfo();				
			}
		}
		additionalInfo.setIdadditionalInfo(++key);
		addList.add(additionalInfo);
		return key;
	}
	
	public int addAdditionalInfo(AdditionalInfo info, Node node) {
		info.setIdadditionalInfo(++key);
		addList.add(info);
		return key;
	}
	
	public void deleteAdditionalInfo(int idInfo) {
		for (Iterator<AdditionalInfo> iterator = addList.iterator(); iterator.hasNext();) {
			AdditionalInfo info = (AdditionalInfo) iterator.next();
			if(info.getIdAdditionalInfo() == idInfo) {
				iterator.remove();
			}
		}
	}
	
	public AdditionalInfo getAdditionalInfoByMediaName(String name) {
		for (Iterator<AdditionalInfo> iterator = addList.iterator(); iterator.hasNext();) {
			AdditionalInfo info = (AdditionalInfo) iterator.next();
			if(info.getMediaName().equalsIgnoreCase(name)) {
				return info;
			}
		}
		return null;
	}
	
	public AdditionalInfo getAdditionalInfoById(int id) {
		for (Iterator<AdditionalInfo> iterator = addList.iterator(); iterator.hasNext();) {
			AdditionalInfo info = (AdditionalInfo) iterator.next();
			if(info.getIdAdditionalInfo() == id) {
				return info;
			}
		}
		return null;
	}
	
	public void updateAdditionalInfoPosition(int positionDeleted,
			int type, int idNode) {
		
	}
	
	public LinkedList<Integer> getIdUpdateFunction(int positionDeleted,
			int type, int idNode) {
		LinkedList<Integer> lst = new LinkedList<Integer>();
		LinkedList<AdditionalInfo> addInfo = NodeManager.getInstance().getNodeByAddInfoID(idNode);
		for (Iterator<AdditionalInfo> iterator = addInfo.iterator(); iterator.hasNext();) {
			AdditionalInfo info = (AdditionalInfo) iterator.next();
			if(info.getType() == type && info.getPosition() == positionDeleted) {
				lst.add(info.getIdAdditionalInfo());
			}
		}
		return lst;
	}
	
	public void decrementPosition(int id) {
		for (Iterator<AdditionalInfo> iterator = addList.iterator(); iterator.hasNext();) {
			AdditionalInfo info = (AdditionalInfo) iterator.next();
			if(info.getIdAdditionalInfo() == id) {
				info.setPosition(info.getPosition() - 1);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
		addList = (BeanList<AdditionalInfo>)list;			
	}
}