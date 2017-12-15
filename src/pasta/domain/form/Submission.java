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

package pasta.domain.form;

import java.util.Date;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.user.PASTAUser;

/**
 * Form object for a submission.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-16
 *
 */
public class Submission {
	private CommonsMultipartFile file;
	private Long assessment;
	private PASTAUser submittingUser;
	private boolean groupSubmission;
	private Date submissionDate;

	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public Long getAssessment() {
		return assessment;
	}
	public void setAssessment(Long assessment) {
		this.assessment = assessment;
	}
	
	public PASTAUser getSubmittingUser() {
		return submittingUser;
	}
	public void setSubmittingUser(PASTAUser submittingUser) {
		this.submittingUser = submittingUser;
	}
	
	public boolean isGroupSubmission() {
		return groupSubmission;
	}
	public void setGroupSubmission(boolean groupSubmission) {
		this.groupSubmission = groupSubmission;
	}
	
	public Date getSubmissionDate() {
		return submissionDate;
	}
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
}
