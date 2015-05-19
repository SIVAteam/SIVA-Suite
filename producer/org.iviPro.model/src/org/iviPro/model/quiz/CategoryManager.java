package org.iviPro.model.quiz;

import java.util.Iterator;
import java.util.LinkedList;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class CategoryManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;
	
	private static CategoryManager INSTANCE;

	private BeanList<Category> catList;
	
	private CategoryManager(Project project) {
		super(project);
	}
	
	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new CategoryManager(project);
		}
	}
	
    public static CategoryManager getInstance() {
        return INSTANCE;
    }
	
	public int setCategoryData(String category) {
		for (Iterator<Category> iterator = catList.iterator(); iterator.hasNext();) {
			Category cat = (Category) iterator.next();
			if(cat.getName().equalsIgnoreCase(category)) {
				return -1;
			}
		}
		Category cat = new Category(project, ++key, category);
		catList.add(cat);
		return key;
	}
	
	public void deleteCategory(String string) {
		for (Iterator<Category> iterator = catList.iterator(); iterator.hasNext();) {
			Category cat = (Category) iterator.next();
			if(cat.getName().equalsIgnoreCase(string)) {
				iterator.remove();
			}
		}
	}
	
	public String getCategoryName(int catId) {
		for (Iterator<Category> iterator = catList.iterator(); iterator.hasNext();) {
			Category cat = (Category) iterator.next();
			if(cat.getId() == catId) {
				return cat.getName();
			}
		}
		return "";
	}
	
	public int getCategoryKey(String title) {
		for (Iterator<Category> iterator = catList.iterator(); iterator.hasNext();) {
			Category cat = (Category) iterator.next();
			if(cat.getName().equalsIgnoreCase(title)) {
				return cat.getId();
			}
		}
		return -1;
	}
	
	public String[] getUserCategorzList() {
		LinkedList<String> titles = new LinkedList<String>();
		for (Iterator<Category> iterator = catList.iterator(); iterator.hasNext();) {
			Category cat = (Category) iterator.next();
			titles.add(cat.getName());
		}
		String[] titlesarr = new String[titles.size()];
		titles.toArray(titlesarr);
		return titlesarr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
		catList = (BeanList<Category>)list;	
	}
}
