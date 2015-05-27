package pasta.domain.release;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.PASTAUser;

/**
 * A rule that is met if the given user is in one of the valid streams.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_stream")
public class StreamRule extends ReleaseRule implements Serializable {
	private static final long serialVersionUID = -5739815741393650873L;

	@ElementCollection
	@CollectionTable(name = "rules_stream_content", joinColumns = @JoinColumn(name = "rule_id"))
	@Column(name = "stream")
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<String> streams;
	
	public StreamRule() {
		this.streams = new TreeSet<String>();
	}
	public StreamRule(String... streams) {
		this.streams = new TreeSet<String>(Arrays.asList(streams));
	}
	
	public void addStream(String streamName) {
		this.streams.add(streamName);
	}
	
	@Override
	protected boolean isMet(PASTAUser user) {
		return streams.contains(user.getStream());
	}
	
	public Set<String> getStreams() {
		return streams;
	}
	public void setStreams(Set<String> streams) {
		this.streams.clear();
		this.streams.addAll(streams);
	}
	
	@Override
	public String getShortDescription() {
		return "Release to a set of streams";
	}
	@Override
	public String getDescription() {
		return "The assessment will be released if a user is a member of one of the listed streams.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release to streams ").append(streams.toString());
		return sb.toString();
	}
}
