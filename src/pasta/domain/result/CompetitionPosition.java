package pasta.domain.result;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table (name = "competition_positions")
public class CompetitionPosition implements Serializable, Comparable<CompetitionPosition> {

	private static final long serialVersionUID = -5107233817080094964L;

	@Id
	@GeneratedValue
	private long id;
	
	private int position;
	
	@ElementCollection
	@CollectionTable(name="competition_user_results", joinColumns=@JoinColumn(name="competition_position_id"))
	@Column(name="user_result")
	private List<CompetitionUserMark> userResults;

	public CompetitionPosition() {
		userResults = new LinkedList<CompetitionUserMark>();
	}
	
	public CompetitionPosition(int position) {
		this();
		this.position = position;
	}
	
	public void addUserResult(CompetitionUserMark result) {
		this.userResults.add(result);
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}

	public List<CompetitionUserMark> getUserResults() {
		return userResults;
	}
	public void setUserResults(List<CompetitionUserMark> userResults) {
		this.userResults.clear();
		this.userResults.addAll(userResults);
	}

	@Override
	public int compareTo(CompetitionPosition o) {
		return this.getPosition() - o.getPosition();
	}
}
