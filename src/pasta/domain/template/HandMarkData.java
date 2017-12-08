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
