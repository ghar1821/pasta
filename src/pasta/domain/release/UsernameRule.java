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
}
