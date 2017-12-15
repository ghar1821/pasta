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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.util.ProjectProperties;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ReleaseRule implements Comparable<ReleaseRule>, Serializable {
	private static final long serialVersionUID = -2751801889498306673L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;
	
	protected abstract boolean isMet(PASTAUser user);
	
	public boolean isReleased(PASTAUser user, Assessment assessment) {
		// If they have ever submitted, it is released.
		if(ProjectProperties.getInstance().getResultDAO()
				.getLastestSubmission(user, assessment) != null) {
			return true;
		}
		return isMet(user);
	}
	
	public abstract String getShortDescription();
	public abstract String getDescription();
	
	@Override
	public int compareTo(ReleaseRule other) {
		return this.getShortDescription().compareTo(other.getShortDescription());
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return getClass().getName();
	}
}
