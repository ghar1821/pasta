package pasta.domain.result;

import java.util.Comparator;
import java.util.Date;

import pasta.domain.template.Assessment;

public class DueDateComparator implements Comparator<Assessment> {
		@Override
		public int compare(Assessment o1, Assessment o2) {
			Date now = new Date();
			
			if(o1.getDueDate().before(now) && !o2.getDueDate().before(now)) {
				return 1;
			}
			if(o2.getDueDate().before(now) && !o1.getDueDate().before(now)) {
				return -1;
			}
			
			int diff = o1.getDueDate().compareTo(o2.getDueDate());
			if(diff != 0) {
				return diff;
			}
			
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}