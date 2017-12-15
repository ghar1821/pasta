package pasta.archive.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container class for the hand marking assessment module.
 * <p>
 * Contains all of the information required for the hand marking.
 * Very similar to a 2 dimensional array where some elements are
 * missing. Has a row and column header which are used as keys and
 * data for the element.
 * 
 * The column and row headers are pairings of a String name and a
 * double weight.
 * 
 * The weighting for hand marking is not relative. It should all add up
 * to 1 (100%), but it is possible to have a marking template that adds
 * up to 125%, giving students multiple avenues to getting full marks.
 * The final mark is capped at 100%.
 * 
 * <p>
 * File location on disk: $projectLocation$/template/handMarking/$handMarkingName$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
public class HandMarking {

	private String name;
	private List<Tuple> columnHeader = new ArrayList<Tuple>();
	private List<Tuple> rowHeader = new ArrayList<Tuple>();
	private Map<String, Map<String, String>> data = new TreeMap<String, Map<String, String>>();
	
	private Map<String, Double> columnHeaderMap = new TreeMap<String, Double>();

	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return name.replace(" ", "");
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Tuple> getColumnHeader() {
		return columnHeader;
	}
	
	public Map<String, Double> getColumnHeaderAsMap() {
		return columnHeaderMap;
	}

	public void setColumnHeader(List<Tuple> columnHeader) {
		this.columnHeader.clear();
		this.columnHeaderMap.clear();
		for(Tuple column: columnHeader){
			this.columnHeader.add(column);
			this.columnHeaderMap.put(column.getName(), column.getWeight());
		}
	}

	public List<Tuple> getRowHeader() {
		return rowHeader;
	}

	public void setRowHeader(List<Tuple> rowHeader) {
		this.rowHeader.clear();
		this.rowHeader.addAll(rowHeader);
	}

	public Map<String, Map<String, String>> getData() {
		return data;
	}

	public void setData(Map<String, Map<String, String>> data) {
		this.data.clear();
		this.data.putAll(data);
	}
}