package pasta.domain.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.list.LazyList;

import pasta.domain.template.HandMarkData;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedField;

/**
 * Form object to update a hand marking template.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-02
 *
 */
public class UpdateHandMarkingForm {
	
	private Long id;
	
	private String name;
	
	private List<WeightedField> newColumnHeader;
	private List<WeightedField> newRowHeader;
	private List<HandMarkData> newData;
	
	public UpdateHandMarkingForm(HandMarking base) {
		this.id = base.getId();
		this.name = base.getName();
		
		this.newColumnHeader = LazyList.lazyList(new ArrayList<WeightedField>(),
				FactoryUtils.instantiateFactory(WeightedField.class));
		this.newRowHeader = LazyList.lazyList(new ArrayList<WeightedField>(),
				FactoryUtils.instantiateFactory(WeightedField.class));
		this.newData = LazyList.lazyList(new ArrayList<HandMarkData>(),
				FactoryUtils.instantiateFactory(HandMarkData.class));
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<WeightedField> getNewColumnHeader() {
		return newColumnHeader;
	}
	public void setNewColumnHeader(Collection<WeightedField> columnHeader) {
		this.newColumnHeader.clear();
		this.newColumnHeader.addAll(columnHeader);
	}
	public List<WeightedField> getNewRowHeader() {
		return newRowHeader;
	}
	public void setNewRowHeader(Collection<WeightedField> rowHeader) {
		this.newRowHeader.clear();
		this.newRowHeader.addAll(rowHeader);
	}
	public List<HandMarkData> getNewData() {
		return newData;
	}
	public void setNewData(Collection<HandMarkData> data) {
		this.newData.clear();
		this.newData.addAll(data);
	}
	
}
