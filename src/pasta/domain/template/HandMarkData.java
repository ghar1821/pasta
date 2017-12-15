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

package pasta.domain.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;

@Entity
@Table (name = "hand_marking_data")
@VerboseName(value = "hand-marking datum", plural = "hand-marking data")
public class HandMarkData extends BaseEntity implements Comparable<HandMarkData> {

	private static final long serialVersionUID = -9016810010400907861L;
	
	@OneToOne
    @JoinColumn (name = "column_id")
	private WeightedField column;
	
	@OneToOne
	@JoinColumn (name = "row_id")
    private WeightedField row;
	
	@Column (length = 4096)
	private String data;
	
	@ManyToOne
	@JoinColumn(name="hand_marking_id", nullable = true)
	private HandMarking handMarking;

	public HandMarkData() {
	}
	
	public HandMarkData(WeightedField column, WeightedField row, String data) {
		super();
		this.column = column;
		this.row = row;
		this.data = data;
	}
	
	public WeightedField getColumn() {
		return column;
	}
	public void setColumn(WeightedField column) {
		this.column = column;
	}

	public WeightedField getRow() {
		return row;
	}
	public void setRow(WeightedField row) {
		this.row = row;
	}

	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	public HandMarking getHandMarking() {
		return handMarking;
	}
	public void setHandMarking(HandMarking handMarking) {
		this.handMarking = handMarking;
	}

	@Override
	public int compareTo(HandMarkData other) {
		int diff = this.column.compareTo(other.column);
		if(diff != 0) {
			return diff;
		}
		diff = this.row.compareTo(other.row);
		if(diff != 0) {
			return diff;
		}
		return this.data.compareTo(other.data);
	}
	
	@Override
	public String toString() {
		return "{" + this.getId() + ": " + (this.column == null ? "null" : this.column.getId()) + ", " + (this.row == null ? "null" : this.row.getId()) + ", " + this.data + "}";
	}
}
