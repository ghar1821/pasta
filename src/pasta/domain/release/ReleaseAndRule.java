package pasta.domain.release;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.archive.InvalidRebuildOptionsException;
import pasta.archive.RebuildOptions;
import pasta.domain.user.PASTAUser;

/**
 * A rule that is met once all 'child' rules are met.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_release_and")
public class ReleaseAndRule extends ReleaseRule implements Serializable {
	private static final long serialVersionUID = -726785216525347073L;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable (name = "rules_release_and_content", 
			joinColumns=@JoinColumn(name = "rule_id"),
			inverseJoinColumns=@JoinColumn(name = "and_rule_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<ReleaseRule> rules;
	
	public ReleaseAndRule() {
		this.rules = new LinkedList<ReleaseRule>();
	}
	public ReleaseAndRule(ReleaseRule... rules) {
		this.rules = new LinkedList<ReleaseRule>(Arrays.asList(rules));
	}
	
	public void addRule(ReleaseRule rule) {
		this.rules.add(rule);
	}
	
	@Override
	protected boolean isMet(PASTAUser user) {
		if(rules == null) {
			return false;
		}
		for(ReleaseRule rule : rules) {
			if(!rule.isMet(user)) {
				return false;
			}
		}
		return true;
	}
	
	public List<ReleaseRule> getRules() {
		return rules;
	}
	public void setRules(List<ReleaseRule> rules) {
		this.rules = rules;
	}
	
	@Override
	public String getShortDescription() {
		return "AND - Intersection of rules";
	}
	@Override
	public String getDescription() {
		return "The assessment will be released if "
				+ "all of the given sub-rules are met.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release if ");
		boolean first = true;
		for(ReleaseRule rule : rules) {
			if(first) first = false; else sb.append(" AND ");
			sb.append('(').append(rule.toString()).append(')');
		}
		return sb.toString();
	}
	
	@Override
	public ReleaseRule rebuild(RebuildOptions options) throws InvalidRebuildOptionsException {
		LinkedList<ReleaseRule> newRules = new LinkedList<>();
		for(ReleaseRule rule : this.getRules()) {
			newRules.add(rule.rebuild(options));
		}
		return new ReleaseAndRule((ReleaseRule[]) newRules.toArray());
	}
}
