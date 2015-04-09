/*
Copyright (c) 2015, Josh Stretton
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import pasta.domain.template.Competition;

/**
 * Container class for the results of a competition.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
@Entity
@Table (name = "competition_percentages")
public class CompetitionMarks implements Serializable, Comparable<CompetitionMarks> {

	public static final String MARKS_FILENAME = "marks.csv";
	
	private static final long serialVersionUID = -3430695372654543083L;

	@Id
	@GeneratedValue
	private long id;
	
	@Column (name = "run_date")
	private Date runDate;
	
	@ManyToOne
	@JoinColumn (name = "competition_id")
	private Competition competition;
	
	/**
	 * Note that the index of the position in the list is what determines the
	 * order or rank and the position object holds the position merely for
	 * convenience.
	 */
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "competition_percentage_id")
	@OrderColumn (name = "position_index")
	private List<CompetitionPosition> positions;
	
	public CompetitionMarks() {
		this.positions = new ArrayList<CompetitionPosition>();
	}
	
	public void updatePositions(List<CompetitionUserMark> users){
		if(!users.isEmpty()){
			// clear
			clearPositions();
			
			// sort
			Collections.sort(users);
			
			// array up
			Double bestSoFar = users.get(0).getPercentage();
			int currPos = 0;
			CompetitionPosition position = getPosition(currPos+1);
			
			for(int i=0; i<users.size(); ++i){
				if(bestSoFar.compareTo(users.get(i).getPercentage()) != 0){
					position = getPosition(++currPos);
					bestSoFar = users.get(i).getPercentage();
				}
				position.addUserResult(users.get(i));
			}
		}
	}
	
	private void clearPositions() {
		for(int i = 0; i < positions.size(); i++) {
			positions.get(i).getUserResults().clear();
		}
	}

	public CompetitionPosition getPosition(int position) {
		if(positions.get(position - 1) == null) {
			for(int i = 0; i < position; i++) {
				if(positions.get(i) == null) {
					positions.add(new CompetitionPosition(i+1));
				}
			}
		}
		return positions.get(position - 1);
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public Date getRunDate() {
		return runDate;
	}
	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}

	public Competition getCompetition() {
		return competition;
	}
	public void setCompetition(Competition competition) {
		this.competition = competition;
	}

	public List<CompetitionPosition> getPositions() {
		return positions;
	}
	public void setPositions(List<CompetitionPosition> positions) {
		this.positions.clear();
		this.positions.addAll(positions);
	}

	@Override
	public int compareTo(CompetitionMarks o) {
		int diff = this.getCompetition().compareTo(o.getCompetition());
		if(diff != 0) {
			return diff;
		}
		return this.getRunDate().compareTo(o.getRunDate());
	}
}
