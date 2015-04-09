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

package pasta.domain.result;

import java.util.Date;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import pasta.domain.template.Arena;
import pasta.domain.template.Competition;

import java.util.List;

/**
 * Container for the results of an arena execution.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-01-23
 *
 */
@Entity
@Table (name = "competition_results")
public class CompetitionResult {
	
	public static final String RESULT_FILENAME = "results.csv";
	
	@Id
	@GeneratedValue
	private long id;
	
	@ManyToOne
	@JoinColumn (name = "competition")
	private Competition competition;
	
	@ManyToOne
	@JoinColumn (name = "arena_id")
	private Arena arena;
	
	@Column (name = "run_date")
	private Date runDate;
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "competition_result_id")
	private Set<CompetitionResultData> data = new TreeSet<CompetitionResultData>();
	
	@ElementCollection
	@CollectionTable(name="result_categories", joinColumns=@JoinColumn(name="competition_result_id"))
	@Column(name="category")
	private List<ResultCategory> categories = new LinkedList<ResultCategory>();
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	
	public Date getRunDate() {
		return runDate;
	}
	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}
	
	public Set<CompetitionResultData> getData() {
		return data;
	}
	public void setData(Set<CompetitionResultData> data) {
		this.data.clear();
		this.data.addAll(data);
	}
	
	public List<ResultCategory> getCategories() {
		return categories;
	}
	public void setCategories(List<ResultCategory> categories) {
		this.categories.clear();
		this.categories.addAll(categories);
	}
	
	public List<ResultCategory> getStudentVisibleCategories() {
		List<ResultCategory> results = new LinkedList<ResultCategory>();
		for(ResultCategory category : categories) {
			if(category.isStudentVisible()) {
				results.add(category);
			}
		}
		return results;
	}
}