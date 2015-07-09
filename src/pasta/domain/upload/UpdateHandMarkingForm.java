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

package pasta.domain.upload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.list.LazyList;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

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
	
	@Min(0)
	private long id;
	
	@NotEmpty
	@Length(max=256)
	private String name;
	
	@Valid
	private List<WeightedField> newColumnHeader;
	@Valid
	private List<WeightedField> newRowHeader;
	@Valid
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
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
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
