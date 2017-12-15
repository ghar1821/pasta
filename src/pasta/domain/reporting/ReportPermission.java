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

package pasta.domain.reporting;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pasta.domain.UserPermissionLevel;
import pasta.domain.template.Assessment;

@Entity
@Table(name = "report_permissions")
public class ReportPermission {

	@Embeddable
	public static class ReportPermissionId implements Serializable {
		private static final long serialVersionUID = -8920999142837201432L;

		@ManyToOne
		@JoinColumn(name="assessment", nullable=false)
		private Assessment assessment;
		
		@Column(name = "permission_level", nullable = false)
		@Enumerated(EnumType.STRING)
		private UserPermissionLevel permissionLevel;
		
		@ManyToOne
		@JoinColumn(name="report", nullable=false)
		private Report report;
		
		public ReportPermissionId() { }
		
		public ReportPermissionId(Report report, Assessment assessment, UserPermissionLevel level) {
			this.report = report;
			this.assessment = assessment;
			this.permissionLevel = level;
		}
		
		public Assessment getAssessment() {
			return assessment;
		}

		public void setAssessment(Assessment assessment) {
			this.assessment = assessment;
		}

		public UserPermissionLevel getPermissionLevel() {
			return permissionLevel;
		}

		public void setPermissionLevel(UserPermissionLevel permissionLevel) {
			this.permissionLevel = permissionLevel;
		}

		public Report getReport() {
			return report;
		}

		public void setReport(Report report) {
			this.report = report;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((assessment == null) ? 0 : assessment.hashCode());
			result = prime * result + ((permissionLevel == null) ? 0 : permissionLevel.hashCode());
			result = prime * result + ((report == null) ? 0 : report.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReportPermissionId other = (ReportPermissionId) obj;
			if (assessment == null) {
				if (other.assessment != null)
					return false;
			} else if (!assessment.equals(other.assessment))
				return false;
			if (permissionLevel != other.permissionLevel)
				return false;
			if (report == null) {
				if (other.report != null)
					return false;
			} else if (!report.equals(other.report))
				return false;
			return true;
		}
	}
	
	@EmbeddedId
	private ReportPermissionId id;
	
	@Column(nullable = true)
	private Boolean allow;
	
	public ReportPermission() {
		this.id = null;
		this.allow = null;
	}
	public ReportPermission(Report report, Assessment assessment, UserPermissionLevel level) {
		this.id = new ReportPermissionId(report, assessment, level);
		this.allow = null;
	}
	public ReportPermission(Report report, Assessment assessment, UserPermissionLevel level, Boolean allow) {
		this(report, assessment, level);
		this.allow = allow;
	}
	
	public Assessment getAssessment() {
		return id.getAssessment();
	}

	public UserPermissionLevel getPermissionLevel() {
		return id.getPermissionLevel();
	}

	public Report getReport() {
		return id.getReport();
	}
	
	public void setAssessment(Assessment assessment) {
		id.setAssessment(assessment);
	}

	public void setPermissionLevel(UserPermissionLevel permissionLevel) {
		id.setPermissionLevel(permissionLevel);
	}

	public void setReport(Report report) {
		id.setReport(report);
	}
	
	public Boolean getAllow() {
		return allow;
	}

	public void setAllow(Boolean allow) {
		this.allow = allow;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportPermission other = (ReportPermission) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
