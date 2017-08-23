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

import pasta.archive.InvalidRebuildOptionsException;
import pasta.archive.RebuildOptions;
import pasta.domain.user.PASTAUser;

/**
 * A rule that is met if the given username is in the list of valid usernames.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_usernames")
public class UsernameRule extends ReleaseRule implements Serializable {
	private static final long serialVersionUID = 1673729114700386626L;
	
	@ElementCollection
	@CollectionTable(name = "rules_usernames_content", joinColumns = @JoinColumn(name = "rule_id"))
	@Column(name = "username")
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<String> usernames;
	
	public UsernameRule() {
		this.usernames = new TreeSet<String>();
	}
	public UsernameRule(String... usernames) {
		this.usernames = new TreeSet<String>(Arrays.asList(usernames));
	}
	
	public void addUsername(String username) {
		this.usernames.add(username);
	}
	
	@Override
	protected boolean isMet(PASTAUser user) {
		return usernames.contains(user.getUsername());
	}
	
	public Set<String> getUsernames() {
		return usernames;
	}
	public void setUsernames(Set<String> usernames) {
		this.usernames.clear();
		this.usernames.addAll(usernames);
	}
	
	@Override
	public String getShortDescription() {
		return "Release to a set of usernames";
	}
	@Override
	public String getDescription() {
		return "The assessment will be released if a user is in the list of released usernames.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release to users ").append(usernames.toString());
		return sb.toString();
	}
	
	@Override
	public ReleaseRule rebuild(RebuildOptions options) throws InvalidRebuildOptionsException {
		return new UsernameRule((String[]) this.getUsernames().toArray());
	}
}
