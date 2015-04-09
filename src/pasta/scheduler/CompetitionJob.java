/*
Copyright (c) 2015, Joshua Stretton
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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import pasta.domain.result.CompetitionMarks;
import pasta.domain.result.CompetitionResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Competition;

@Entity
@Table(name = "competition_jobs",
		uniqueConstraints = { @UniqueConstraint(columnNames={
				"competition_id", "arena_id", "run_date"
		})})
public class CompetitionJob extends Job implements Serializable {

	private static final long serialVersionUID = 2058301754166837748L;
	
	@ManyToOne
	@JoinColumn (name = "competition_id", nullable = false)
	private Competition competition;
	
	@ManyToOne
	@JoinColumn (name = "arena_id")
	private Arena arena;
	
	@ManyToOne
	@JoinColumn (name = "competition_result_id")
	private CompetitionResult result;
	
	@ManyToOne
	@JoinColumn (name = "competition_percentages_id")
	private CompetitionMarks marks;
	
	public CompetitionJob(){}
	
	public CompetitionJob(Competition competition, Arena arena, CompetitionMarks percentages, CompetitionResult result, Date runDate) {
		super(runDate);
		this.competition = competition;
		this.arena = arena;
		this.marks = percentages;
		this.result = result;
	}

	public Competition getCompetition() {
		return competition;
	}
	public void setCompetition(Competition competition) {
		this.competition = competition;
	}

	public Arena getArena() {
		return arena;
	}
	public void setArena(Arena arena) {
		this.arena = arena;
	}

	public CompetitionMarks getMarks() {
		return marks;
	}
	public void setMarks(CompetitionMarks marks) {
		this.marks = marks;
	}

	public CompetitionResult getResult() {
		return result;
	}
	public void setResult(CompetitionResult result) {
		this.result = result;
	}
	
	public boolean isCalculated() {
		return arena == null;
	}
}
