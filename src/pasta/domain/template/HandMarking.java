/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.apache.commons.collections.map.LazyMap;

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
	private List<Tuple> columnHeader = LazyList.decorate(new ArrayList<Tuple>(),
			FactoryUtils.instantiateFactory(Tuple.class));;
	private List<Tuple> rowHeader = LazyList.decorate(new ArrayList<Tuple>(),
			FactoryUtils.instantiateFactory(Tuple.class));
	private Map<String, Map<String, String>> data = LazyMap.decorate(new TreeMap<String, TreeMap<String, String>>(), 
			FactoryUtils.instantiateFactory(HashMap.class));
	
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
