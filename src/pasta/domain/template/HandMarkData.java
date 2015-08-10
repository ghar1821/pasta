package pasta.domain.template;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table (name = "hand_marking_data")
public class HandMarkData implements Serializable, Comparable<HandMarkData> {

	private static final long serialVersionUID = -9016810010400907861L;
	
	@Id
	@GeneratedValue
	private long id;
	
	@OneToOne
    @JoinColumn (name = "column_id")
	private WeightedField column;
	
	@OneToOne
	@JoinColumn (name = "row_id")
    private WeightedField row;
	
	@Column (length = 4096)
	private String data;

	public HandMarkData() {
	}
	
	public HandMarkData(WeightedField column, WeightedField row, String data) {
		super();
		this.column = column;
		this.row = row;
		this.data = data;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		HandMarkData other = (HandMarkData) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "{" + this.id + ": " + (this.column == null ? "null" : this.column.getId()) + ", " + (this.row == null ? "null" : this.row.getId()) + ", " + this.data + "}";
	}
	
}
