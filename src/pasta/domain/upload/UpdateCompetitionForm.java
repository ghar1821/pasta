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

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.PASTATime;
import pasta.domain.template.Competition;
import pasta.domain.template.CompetitionPermissionLevel;
import pasta.testing.options.CalculatedCompetitionOptions;
import pasta.testing.options.ScriptOptions;

/**
 * Form object for updating competition.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-01
 *
 */
public class UpdateCompetitionForm {
	
	private long id;
	private String name;
	private PASTATime frequency;
	private Date firstStartDate;
	private boolean hidden;
	private CompetitionPermissionLevel studentPermissions = CompetitionPermissionLevel.NONE;
	private CompetitionPermissionLevel tutorPermissions = CompetitionPermissionLevel.CREATE;
	
	private CommonsMultipartFile file;
	
	private boolean hasRun = true;
	private boolean hasBuild = false;
	private ScriptOptions runOptions;
	private ScriptOptions buildOptions;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	public UpdateCompetitionForm(Competition base) {
		this.id = base.getId();
		this.name = base.getName();
		this.frequency = base.getFrequency();
		if(this.frequency == null) {
			frequency = new PASTATime();
		}
		this.firstStartDate = base.getFirstStartDate();
		this.hidden = base.isHidden();
		this.studentPermissions = base.getStudentPermissions();
		this.tutorPermissions = base.getTutorPermissions();
		
		this.file = null;
		
		if(base.getOptions() instanceof CalculatedCompetitionOptions) {
			CalculatedCompetitionOptions options = (CalculatedCompetitionOptions)base.getOptions();
			this.hasRun = options.hasRunOptions();
			this.hasBuild = options.hasBuildOptions();
			this.runOptions = options.getRunOptions();
			this.buildOptions = options.getBuildOptions();
		}
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
	public PASTATime getFrequency() {
		return frequency;
	}
	public void setFrequency(PASTATime frequency) {
		this.frequency = frequency;
	}
	public Date getFirstStartDate() {
		return firstStartDate;
	}
	public void setFirstStartDate(Date firstStartDate) {
		this.firstStartDate = firstStartDate;
	}
	public boolean isHasRun() {
		return hasRun;
	}
	public void setHasRun(boolean hasRun) {
		this.hasRun = hasRun;
	}
	public boolean isHasBuild() {
		return hasBuild;
	}
	public void setHasBuild(boolean hasBuild) {
		this.hasBuild = hasBuild;
	}
	public ScriptOptions getRunOptions() {
		return runOptions;
	}
	public void setRunOptions(ScriptOptions runOptions) {
		this.runOptions = runOptions;
	}
	public ScriptOptions getBuildOptions() {
		return buildOptions;
	}
	public void setBuildOptions(ScriptOptions buildOptions) {
		this.buildOptions = buildOptions;
	}
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public CompetitionPermissionLevel getStudentPermissions() {
		return studentPermissions;
	}
	public void setStudentPermissions(CompetitionPermissionLevel studentPermissions) {
		this.studentPermissions = studentPermissions;
	}
	public CompetitionPermissionLevel getTutorPermissions() {
		return tutorPermissions;
	}
	public void setTutorPermissions(CompetitionPermissionLevel tutorPermissions) {
		this.tutorPermissions = tutorPermissions;
	}

	// calculated methods
	public void setFirstStartDateStr(String firstStartDateStr){
		if(firstStartDateStr == null || firstStartDateStr.isEmpty()) {
			firstStartDate = null;
		}
		try {
			firstStartDate = Competition.dateParser.parse(firstStartDateStr);
		} catch (ParseException e) {
			logger.error("Could not parse " + firstStartDateStr, e);
		}
	}
	public String getFirstStartDateStr(){
		//firstStartDate.setTime(firstStartDate.getTime()+31536000000l);
		if(firstStartDate == null) {
			return "";
		}
		return Competition.dateParser.format(firstStartDate);
	}
}
