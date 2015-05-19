package org.iviPro.model.quiz;

import java.util.Iterator;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class UserManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;
	
	private static UserManager INSTANCE;

	private BeanList<User> userList;
	
	private UserManager(Project project) {
		super(project);
	}

	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new UserManager(project);
		}
	}
	
    public static UserManager getInstance() {
        return INSTANCE;
    }
	
	public void updateUserData(String userName, String password,
			String firstName, String lastName, String email) {
		for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
			User us = (User) iterator.next();
			if(us.getUserName().equalsIgnoreCase(userName)) {
				us.setPassword(password);
				us.setFirstName(firstName);
				us.setLastName(lastName);
				us.setEmail(email);
			}
		}
	}
	
	public boolean setUserData(String userName, String password,
			String firstName, String lastName, String email) {
		User user = new User(project);
		user.setUserName(userName);
		user.setPassword(password);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		userList.add(user);
		return true;
	}
	
	public void deleteUser(User user) {
		for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
			User us = (User) iterator.next();
			if(us.getIdUser() == user.getIdUser()) {
				iterator.remove();
			}
		}
	}
	
	public User getUserData(String username) {
		for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
			User us = (User) iterator.next();
			if(us.getUserName().equalsIgnoreCase(username)) {
				return us;
			}
		}
		return new User(project);
	}
	
	public String getUsername(int id) {
		for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
			User us = (User) iterator.next();
			if(us.getIdUser() == id) {
				return us.getUserName();
			}
		}
		return "";
	}
	
	public int getUsername(String name) {
		for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
			User us = (User) iterator.next();
			if(us.getUserName().equalsIgnoreCase(name)) {
				return us.getIdUser();
			}
		}
		return -1;
	}
	
	public boolean isUserInDB(String user) {
		for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
			User us = (User) iterator.next();
			if(us.getUserName().equalsIgnoreCase(user)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
    	userList = (BeanList<User>)list;		
	}
}
