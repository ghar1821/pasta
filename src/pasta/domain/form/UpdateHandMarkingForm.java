/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
