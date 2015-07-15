package pasta.domain.upload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 15 Jul 2015
 */
public class UpdateGroupsForm {
	
	private Map<Long, List<String>> groupMembers;
	
	public UpdateGroupsForm() {
		this.groupMembers = new HashMap<Long, List<String>>();
	}

	public Map<Long, List<String>> getGroupMembers() {
		return groupMembers;
	}

	public void setGroupMembers(Map<Long, List<String>> groupMembers) {
		this.groupMembers = groupMembers;
	}
}
