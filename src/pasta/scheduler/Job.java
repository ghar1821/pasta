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

package pasta.scheduler;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Class that holds the details of jobs.
 * <p>
 * This is a the holding object that deals with the 'job' system
 * used in PASTA to ensure only one assessment is executed at a time.
 * 
 * Database schema is automatically created based on this class.
 * 
 * Database schema:
 * <pre>
 * 	Integer ID, 
 *  Text username (not null),
 *  Text assessmentName (not null),
 *  Data runDate (not null)
 * </pre>
 * 
 * For competitions, the username will be PASTACompetitionRunner.
 * For calculated competition, the assessmentName will be the name of the competition.
 * For arena based competition, the assessmentName will be competitionName#PASTAArena#arenaName(
 * e.g. BattleShipLeague#PASTAArena#OfficialArena).
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-12-04
 * 
 */
@Entity
@Table(name = "Jobs",
		uniqueConstraints = { @UniqueConstraint(columnNames={
				"USERNAME", "ASSESSMENT_NAME", "RUN_DATE"
		})})
public class Job implements Serializable{
	private Integer id;
	private String username;
	private String assessmentName;
	private Date runDate;
	
	public Job(){}
	
	public Job(String username, String assessmentName, Date runDate){
		this.username = username;
		this.assessmentName = assessmentName;
		this.runDate = runDate;
	}
	
	@Id
	@GeneratedValue
	@Column(name = "ID", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(name = "USERNAME", nullable = false)
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Column(name = "ASSESSMENT_NAME", nullable = false)
	public String getAssessmentName() {
		return assessmentName;
	}
	public void setAssessmentName(String assessmentName) {
		this.assessmentName = assessmentName;
	}
	
	@Column(name = "RUN_DATE", nullable = false)
	public Date getRunDate() {
		return runDate;
	}
	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}
}
