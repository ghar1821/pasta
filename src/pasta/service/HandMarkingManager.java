package pasta.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.form.NewHandMarkingForm;
import pasta.domain.form.UpdateHandMarkingForm;
import pasta.domain.template.HandMarkData;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;
import pasta.repository.AssessmentDAO;
import pasta.repository.HandMarkingDAO;


/**
 * Hand marking manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
@Service("handMarkingManager")
@Repository
public class HandMarkingManager {
	
	@Autowired
	private AssessmentDAO assDao;
	@Autowired
	private HandMarkingDAO handMarkingDao;
	
	@Autowired
	private ApplicationContext context;
	
	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(HandMarkingManager.class);
	
	/**
	 * Helper method
	 * 
	 * @see pasta.repository.AssessmentDAO#getHandMarkingList()
	 * @return collection of all of the hand marking templates
	 */
	public Collection<HandMarking> getHandMarkingList() {
		return handMarkingDao.getAllHandMarkings();
	}

	/**
	 * Helper method
	 * 
	 * @param handMarkingId the id of the hand marking template
	 * @see pasta.repository.AssessmentDAO#getHandMarking(long)
	 * @return the hand marking template with that id, null if it does not exist
	 */
	public HandMarking getHandMarking(long handMarkingId) {
		return handMarkingDao.getHandMarking(handMarkingId);
	}
	
	public WeightedHandMarking getWeightedHandMarking(long id) {
		return handMarkingDao.getWeightedHandMarking(id);
	}

	/**
	 * Remove a hand marking template
	 * 
	 * @param handMarkingId the id of the hand marking template
	 */
	public void removeHandMarking(long handMarkingId) {
		assDao.unlinkHandMarking(handMarkingId);
		handMarkingDao.delete(handMarkingDao.getHandMarking(handMarkingId));
	}

	/**
	 * New hand marking template
	 * 
	 * @param form the new hand marking form
	 * @return 
	 * @see pasta.repository.AssessmentDAO#newHandMarking(NewHandMarkingForm)
	 */
	public HandMarking newHandMarking(NewHandMarkingForm form){
		HandMarking newTemplate = new HandMarking();
		newTemplate.setName(form.getName());

		newTemplate.addColumn(new WeightedField("Poor", 0));
		newTemplate.addColumn(new WeightedField("Acceptable", 0.5));
		newTemplate.addColumn(new WeightedField("Excellent", 1));

		newTemplate.addRow(new WeightedField("Formatting", 0.2));
		newTemplate.addRow(new WeightedField("Code Reuse", 0.4));
		newTemplate.addRow(new WeightedField("Variable Naming", 0.4));

		for (WeightedField column : newTemplate.getColumnHeader()) {
			for (WeightedField row : newTemplate.getRowHeader()) {
				newTemplate.addData(new HandMarkData(column, row, ""));
			}
		}

		handMarkingDao.saveOrUpdate(newTemplate);
		return newTemplate;
	}

	/**
	 * Update the hand marking. The incoming hand marking form object may have rows
	 * or columns with unique negative ID's. If this is the case, this method
	 * will create new database objects for the new rows and columns.
	 */
	public void updateHandMarking(HandMarking template, UpdateHandMarkingForm form) {
		Map<Long, WeightedField> replacements = new TreeMap<Long, WeightedField>();
		
		// Replace negative id rows and columns with new ones from database
		for(ListIterator<WeightedField> iter : Arrays.asList(
				form.getNewColumnHeader().listIterator(), form.getNewRowHeader().listIterator()
			)) {
			while(iter.hasNext()) {
				WeightedField oldField = iter.next();
				if(oldField.getId() < 0) {
					WeightedField newField = handMarkingDao.createNewWeightedField();
					newField.setName(oldField.getName());
					newField.setWeight(oldField.getWeight());
					replacements.put(oldField.getId(), newField);
					iter.set(newField);
				}
			}
		}
		
		
		// Update data objects in form according to replacements
		Iterator<HandMarkData> it = form.getNewData().iterator();
		while(it.hasNext()) {
			HandMarkData datum = it.next();
			if(datum == null || (datum.getColumn() == null && datum.getRow() == null)) {
				it.remove();
				continue;
			}
			
			if(replacements.containsKey(datum.getColumn().getId())) {
				datum.setColumn(replacements.get(datum.getColumn().getId()));
			}
			if(replacements.containsKey(datum.getRow().getId())) {
				datum.setRow(replacements.get(datum.getRow().getId()));
			}
		}
		
		// Update real template headers from form
		Map<Long, WeightedField> updateableColumns = new HashMap<>();
		Map<Long, WeightedField> updateableRows = new HashMap<>();
		{
			Collection<WeightedField> toAdd = CollectionUtils.subtract(form.getNewColumnHeader(), template.getColumnHeader());	
			Collection<WeightedField> toRemove = new LinkedList<>();
			
			// Determine which columns were deleted, and update the ones that are staying
			for(WeightedField oldCol : template.getColumnHeader()) {
				boolean found = false;
				for(WeightedField newCol : form.getNewColumnHeader()) {
					if(oldCol.getId() == newCol.getId()) {
						oldCol.setName(newCol.getName());
						oldCol.setWeight(newCol.getWeight());
						found = true;
						break;
					}
				}
				if(found) {
					updateableColumns.put(oldCol.getId(), oldCol);
				} else {
					toRemove.add(oldCol);
				}
			}
			
			// Remove deleted columns and add newly added ones
			template.removeColumns(toRemove);
			template.addColumns(toAdd);

			toAdd = CollectionUtils.subtract(form.getNewRowHeader(), template.getRowHeader());	
			toRemove = new LinkedList<>();
			
			for(WeightedField oldRow : template.getRowHeader()) {
				boolean found = false;
				for(WeightedField newRow : form.getNewRowHeader()) {
					if(oldRow.getId() == newRow.getId()) {
						oldRow.setName(newRow.getName());
						oldRow.setWeight(newRow.getWeight());
						found = true;
						break;
					}
				}
				if(found) {
					updateableRows.put(oldRow.getId(), oldRow);
				} else {
					toRemove.add(oldRow);
				}
			}
			
			template.removeRows(toRemove);
			template.addRows(toAdd);
			Collections.sort(template.getRowHeader(), new SortByCustomIDList(form.getNewRowHeader()));
		}
		
		// Update real template data from form
		{
			Collection<HandMarkData> toAdd = CollectionUtils.subtract(form.getNewData(), template.getData());
			Collection<HandMarkData> toRemove = new LinkedList<>();
			
			for(HandMarkData oldData : template.getData()) {
				boolean found = false;
				for(HandMarkData newData : form.getNewData()) {
					if(oldData.getId() == newData.getId()) {
						oldData.setColumn(updateableColumns.get(newData.getColumn().getId()));
						oldData.setRow(updateableRows.get(newData.getRow().getId()));
						oldData.setData(newData.getData());
						found = true;
						break;
					}
				}
				if(!found) {
					toRemove.add(oldData);
				}
			}
			
			template.removeData(toRemove);
			template.addData(toAdd);
		}
		
		// Update rows to be out of a total of 1
		double total = 0;
		double idealTotal = 1.0;
		for(WeightedField row : template.getRowHeader()) {
			total += row.getWeight();
		}
		if(Math.abs(total - idealTotal) > 10 * Math.ulp(idealTotal)) {
			for(WeightedField row : template.getRowHeader()) {
				row.setWeight(row.getWeight() / total);
			}
		}
		
		handMarkingDao.saveOrUpdate(template);
	}

	/**
	 * This Comparator is used to sort a list of WeightedField objects according
	 * to ID such that their order mimics the order of a second list.
	 */
	private class SortByCustomIDList implements Comparator<WeightedField> {

		private HashMap<Long, Integer> idPositions;
		
		/**
		 * Create a comparator that sorts WeightedFields according to where they
		 * appear in the provided list.
		 * 
		 * @param listToCopy
		 *            the list to copy the order of
		 */
		public SortByCustomIDList(List<WeightedField> listToCopy) {
			idPositions = new HashMap<Long, Integer>();
			for(int i = 0; i < listToCopy.size(); i++) {
				idPositions.put(listToCopy.get(i).getId(), i);
			}
		}
		
		@Override
		public int compare(WeightedField o1, WeightedField o2) {
			Integer pos1 = idPositions.get(o1.getId());
			Integer pos2 = idPositions.get(o2.getId());
			if(pos1 == null) {
				return pos2 == null ? 0 : -1;
			}
			return pos1.compareTo(pos2);
		}
	}
}
