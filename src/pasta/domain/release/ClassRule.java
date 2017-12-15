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
 * A rule that is met if the given user is in one of the valid classes.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_class")
public class ClassRule extends ReleaseRule implements Serializable {
	private static final long serialVersionUID = -8403915820681489781L;

	@ElementCollection
	@CollectionTable(name = "rules_class_content", joinColumns = @JoinColumn(name = "rule_id"))
	@Column(name = "class")
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<String> classes;
	
	public ClassRule() {
		this.classes = new TreeSet<String>();
	}
	public ClassRule(String... classes) {
		this.classes = new TreeSet<String>(Arrays.asList(classes));
	}
	
	public void addClass(String className) {
		this.classes.add(className);
	}
	
	@Override
	protected boolean isMet(PASTAUser user) {
		if(classes.contains(user.getTutorial())) {
			return true;
		}
		return classes.contains(user.getFullTutorial());
	}
	
	public Set<String> getClasses() {
		return classes;
	}
	public void setClasses(Set<String> classes) {
		this.classes.clear();
		this.classes.addAll(classes);
	}
	
	@Override
	public String getShortDescription() {
		return "Release to a set of classes";
	}
	@Override
	public String getDescription() {
		return "The assessment will be released if a user is a member of one of the listed classes.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release to classes ").append(classes.toString());
		return sb.toString();
	}
}
