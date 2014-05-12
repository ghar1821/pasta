package pasta.domain.result;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class ArenaResult {
	Map<String, Map<String, String>> data;
	Collection<String> categories = new LinkedList<String>();
	Collection<String> studentVisibleCategories = new LinkedList<String>();
	
	public Map<String, Map<String, String>> getData() {
		return data;
	}
	public void setData(Map<String, Map<String, String>> data) {
		this.data = data;
	}
	public Collection<String> getCategories() {
		return categories;
	}
	public void setCategories(Collection<String> categories) {
		this.categories.clear();
		studentVisibleCategories.clear();
		for(String category: categories){
			if(category.startsWith("*")){
				studentVisibleCategories.add(category.replaceFirst("\\*", ""));
				this.categories.add(category.replaceFirst("\\*", ""));
			}
			else{
				this.categories.add(category);
			}
		}
	}
	public Collection<String> getStudentVisibleCategories() {
		return studentVisibleCategories;
	}
}