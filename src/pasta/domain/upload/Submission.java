/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.upload;

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
	private PASTAUser submittingForUser;
	
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
	public PASTAUser getSubmittingForUser() {
		return submittingForUser;
	}
	public void setSubmittingForUser(PASTAUser submittingForUser) {
		this.submittingForUser = submittingForUser;
	}
}
