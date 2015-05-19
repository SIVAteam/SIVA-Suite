package org.iviPro.model;

public abstract class IQuizContainerBean extends IAbstractBean {

	private static final long serialVersionUID = 1L;

	protected int key;
	
	public IQuizContainerBean(Project project) {
		super("", project);
		key = 0;
	}
	
	public int getKey() {
		return key;
	}
	
	public void setKey(int key) {
		this.key = key;
	}
	
	abstract public void setBeanList(BeanList<? extends IQuizBean> list);
}
