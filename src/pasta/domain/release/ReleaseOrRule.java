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

import pasta.domain.PASTAUser;

/**
 * A rule that is met once at least one 'child' rule is met.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_release_or")
public class ReleaseOrRule extends ReleaseRule implements Serializable {
	private static final long serialVersionUID = 5297885875905087743L;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable (name = "rules_release_or_content", 
		joinColumns=@JoinColumn(name = "rule_id"),
		inverseJoinColumns=@JoinColumn(name = "or_rule_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<ReleaseRule> rules;
	
	public ReleaseOrRule() {
		this.rules = new LinkedList<ReleaseRule>();
	}
	public ReleaseOrRule(ReleaseRule... rules) {
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
			if(rule.isMet(user)) {
				return true;
			}
		}
		return false;
	}
	
	public List<ReleaseRule> getRules() {
		return rules;
	}
	public void setRules(List<ReleaseRule> rules) {
		this.rules = rules;
	}
	
	@Override
	public String getShortDescription() {
		return "OR - Union of rules";
	}
	@Override
	public String getDescription() {
		return "The assessment will be released if at least"
				+ " one of the given sub-rules are met.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release if ");
		boolean first = true;
		for(ReleaseRule rule : rules) {
			if(first) first = false; else sb.append(" OR ");
			sb.append('(').append(rule.toString()).append(')');
		}
		return sb.toString();
	}
}
