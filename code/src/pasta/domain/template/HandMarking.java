package pasta.domain.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.apache.commons.collections.map.LazyMap;

public class HandMarking {

	private String name;
	private List<Tuple> columnHeader = LazyList.decorate(new ArrayList<Tuple>(),
			FactoryUtils.instantiateFactory(Tuple.class));;
	private List<Tuple> rowHeader = LazyList.decorate(new ArrayList<Tuple>(),
			FactoryUtils.instantiateFactory(Tuple.class));
	private Map<String, HashMap<String, String>> data = LazyMap.decorate(new HashMap<String, HashMap<String, String>>(), 
			FactoryUtils.instantiateFactory(HashMap.class));
	
	private HashMap<String, Double> columnHeaderMap = new HashMap<String, Double>();

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

	public Map<String, HashMap<String, String>> getData() {
		return data;
	}

	public void setData(Map<String, HashMap<String, String>> data) {
		this.data.clear();
		this.data.putAll(data);
	}
}
