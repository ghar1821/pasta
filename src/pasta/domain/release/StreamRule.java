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
