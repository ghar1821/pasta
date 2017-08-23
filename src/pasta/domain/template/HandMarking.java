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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.archive.Archivable;
import pasta.archive.InvalidRebuildOptionsException;
import pasta.archive.RebuildOptions;

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
@Entity
@Table (name = "hand_markings")
public class HandMarking implements Archivable<HandMarking>, Comparable<HandMarking> {

	private static final long serialVersionUID = 5276980986516750657L;

	@Id
	@GeneratedValue 
	private Long id;
	
	private String name;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(name="hand_marking_columns",
			joinColumns=@JoinColumn(name = "hand_marking_id"),
			inverseJoinColumns=@JoinColumn(name = "weighted_field_id"))
    @OrderBy("weight")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<WeightedField> columnHeader = new ArrayList<WeightedField>();
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(name="hand_marking_rows",
			joinColumns=@JoinColumn(name = "hand_marking_id"),
			inverseJoinColumns=@JoinColumn(name = "weighted_field_id"))
    @OrderColumn(name = "row_index")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<WeightedField> rowHeader = new ArrayList<WeightedField>();
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable (name="hand_marking_data_joins", 
		joinColumns=@JoinColumn(name = "hand_marking_id"),
		inverseJoinColumns=@JoinColumn(name = "hand_marking_data_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<HandMarkData> data = new ArrayList<HandMarkData>();
	
	
	public String getName() {
		return name;
	}
	
	public String getFileAppropriateName() {
		return name.replaceAll("[^\\w]+", "");
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WeightedField> getColumnHeader() {
		return columnHeader;
	}
	
	public void setColumnHeader(List<WeightedField> columnHeader) {
		this.columnHeader.clear();
		this.columnHeader.addAll(columnHeader);
	}

	public List<WeightedField> getRowHeader() {
		return rowHeader;
	}

	public void setRowHeader(List<WeightedField> rowHeader) {
		this.rowHeader.clear();
		this.rowHeader.addAll(rowHeader);
	}

	public List<HandMarkData> getData() {
		return data;
	}

	public void setData(List<HandMarkData> data) {
		removeAllData();
		for(HandMarkData datum : data) {
			addData(datum);
		}
	}

	private WeightedField getRow(long id) {
		for(WeightedField row : rowHeader) {
			if(row.getId() == id) {
				return row;
			}
		}
		return null;
	}
	
	private WeightedField getColumn(long id) {
		for(WeightedField col : columnHeader) {
			if(col.getId() == id) {
				return col;
			}
		}
		return null;
	}
	
	public boolean hasRow(long id) {
		return getRow(id) != null;
	}
	
	public double getRowWeight(long id) {
		WeightedField row = getRow(id);
		return row == null ? 0 : row.getWeight();
	}
	
	public boolean hasColumn(long id) {
		return getColumn(id) != null;
	}
	
	public double getColumnWeight(long id) {
		WeightedField col = getColumn(id);
		return col == null ? 0 : col.getWeight();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void addData(HandMarkData handMarkData) {
		getData().add(handMarkData);
	}
	
	public void addData(Collection<HandMarkData> handMarkData) {
		getData().addAll(handMarkData);
	}
	
	public boolean removeData(HandMarkData handMarkData) {
		return getData().remove(handMarkData);
	}
	
	public boolean removeData(Collection<HandMarkData> handMarkData) {
		boolean success = true;
		for(HandMarkData data : handMarkData) {
			success &= removeData(data);
		}
		return success;
	}
	
	public void removeAllData() {
		getData().clear();
	}

	public void addColumn(WeightedField column) {
		getColumnHeader().add(column);
	}
	
	public void addColumns(Collection<WeightedField> columns) {
		for(WeightedField column : columns) {
			addColumn(column);
		}
	}
	
	public boolean removeColumn(WeightedField column) {
		List<HandMarkData> toRemove = new LinkedList<>();
		for(HandMarkData data : getData()) {
			if(data.getColumn() == column) {
				toRemove.add(data);
			}
		}
		boolean success = true;
		for(HandMarkData data : toRemove) {
			success &= removeData(data);
		}
		return success && getColumnHeader().remove(column);
	}
	
	public boolean removeColumns(Collection<WeightedField> columns) {
		boolean success = true;
		for(WeightedField column : columns) {
			success &= removeColumn(column);
		}
		return success;
	}
	
	public void addRow(WeightedField row) {
		getRowHeader().add(row);
	}
	
	public void addRows(Collection<WeightedField> rows) {
		for(WeightedField row : rows) {
			addRow(row);
		}
	}
	
	public boolean removeRow(WeightedField row) {
		List<HandMarkData> toRemove = new LinkedList<>();
		for(HandMarkData data : getData()) {
			if(data.getRow() == row) {
				toRemove.add(data);
			}
		}
		boolean success = true;
		for(HandMarkData data : toRemove) {
			success &= removeData(data);
		}
		return success && getRowHeader().remove(row);
	}
	
	public boolean removeRows(Collection<WeightedField> rows) {
		boolean success = true;
		for(WeightedField row : rows) {
			success &= removeRow(row);
		}
		return success;
	}

	@Override
	public int compareTo(HandMarking other) {
		return this.name.compareTo(other.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HandMarking other = (HandMarking) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public HandMarking rebuild(RebuildOptions options) throws InvalidRebuildOptionsException {
		HandMarking clone = new HandMarking();
		LinkedList<WeightedField> newColumnHeaders = new LinkedList<>();
		for(WeightedField header : this.getColumnHeader()) {
			newColumnHeaders.add(header.rebuild(options));
		}
		clone.setColumnHeader(newColumnHeaders);
		LinkedList<HandMarkData> newData = new LinkedList<>();
		for(HandMarkData data : this.getData()) {
			newData.add(data.rebuild(options));
		}
		clone.setData(newData);
		clone.setName(this.getName());
		LinkedList<WeightedField> newRowHeader = new LinkedList<>();
		for(WeightedField header : this.getRowHeader()) {
			newRowHeader.add(header.rebuild(options));
		}
		clone.setRowHeader(newRowHeader);
		return clone;
	}
}
