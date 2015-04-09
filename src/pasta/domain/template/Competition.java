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

package pasta.domain.template;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.PASTATime;
import pasta.domain.PASTAUser;
import pasta.util.ProjectProperties;

/**
 * Container class for a competition.
 * <p>
 * Contains all relevant competition information. <b>There is currently no way
 * to set the competition as tested or to test the competition within PASTA
 * similarly to how unit test modules can be tested.</b> String representation:
 * 
 * <pre>
 * {@code <competitionProperties>
 * 	<name>name</name>
 * 	<studentCreatableArena>true|false</studentCreatableArena>
 * 	<studentCreatableRepeatableArena>true|false</studentCreatableRepeatableArena>
 * 	<tutorCreatableRepeatableArena>true|false</tutorCreatableRepeatableArena>
 * 	<tested>true|false</tested>
 * 	<hidden>true|false</hidden>
 * 	<firstStartDate>yyyy-MM-ddThh-mm-dd</firstStartDate>
 * 	<frequency>?y?d?h?m?s?ms</frequency>
 * </competitionProperties>}
 * </pre>
 * <p>
 * File location on disk:
 * $projectLocation$/template/competition/$competitionName$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */
@Entity
@Table (name = "competitions")
public class Competition implements Serializable, Comparable<Competition> {

	private static final long serialVersionUID = 296084853850209418L;

	public final static SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	@Transient
	protected final Log logger = LogFactory.getLog(getClass());

	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	
	private boolean calculated;
	
	/** if null - calculated competition **/
	@OneToOne (mappedBy = "competition", cascade = CascadeType.ALL)
	@JoinColumn (name = "official_arena")
	private Arena officialArena = null;
	
	@OneToMany (cascade = CascadeType.ALL)
	@JoinTable(name="competition_outstanding_arenas",
		joinColumns=@JoinColumn(name = "competition_id"),
		inverseJoinColumns=@JoinColumn(name = "arena_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<Arena> outstandingArenas = new ArrayList<Arena>();
	
	@OneToMany (cascade = CascadeType.ALL)
	@JoinTable(name="competition_completed_arenas",
		joinColumns=@JoinColumn(name = "competition_id"),
		inverseJoinColumns=@JoinColumn(name = "arena_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<Arena> completedArenas = new ArrayList<Arena>();
	
	@Enumerated(EnumType.STRING)
	@Column (name = "student_permissions")
	private CompetitionPermissionLevel studentPermissions = CompetitionPermissionLevel.NONE;
	
	@Enumerated(EnumType.STRING)
	@Column (name = "tutor_permissions")
	private CompetitionPermissionLevel tutorPermissions = CompetitionPermissionLevel.CREATE;
	
	private boolean tested = false;
	private boolean hidden = false;

	@Column (name = "frequency")
	private PASTATime frequency = null;
	
	@Column (name = "first_start_date")
	private Date firstStartDate;
	
	@Lob
	private Serializable options;

	// getters and setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Serializable getOptions() {
		return options;
	}
	public void setOptions(Serializable options) {
		this.options = options;
	}

	public CompetitionPermissionLevel getStudentPermissions() {
		return studentPermissions;
	}

	public CompetitionPermissionLevel getTutorPermissions() {
		return tutorPermissions;
	}

	public void setCalculated(boolean calculated) {
		this.calculated = calculated;
	}

	public List<Arena> getOutstandingArenas() {
		return outstandingArenas;
	}

	public void setOutstandingArenas(List<Arena> outstandingArenas) {
		this.outstandingArenas.clear();
		this.outstandingArenas.addAll(outstandingArenas);
	}

	public List<Arena> getCompletedArenas() {
		return completedArenas;
	}

	public void setCompletedArenas(List<Arena> completedArenas) {
		this.completedArenas.clear();
		this.completedArenas.addAll(completedArenas);
	}

	@Deprecated public Arena getArena(String name) {
		if (name.replace(" ", "").toLowerCase().equals("officialarena")) {
			return officialArena;
		}
		for(Arena arena : completedArenas) {
			if(arena.getName().replace(" ", "").equals(name.replace(" ", ""))) {
				return arena;
			}
		}
		for(Arena arena : outstandingArenas) {
			if(arena.getName().replace(" ", "").equals(name.replace(" ", ""))) {
				return arena;
			}
		}
		return null;
	}
	
	public Arena getArena(Long id) {
		if(officialArena.getId() == id) {
			return officialArena;
		}
		for(Arena arena : completedArenas) {
			if(arena.getId() == id) {
				return arena;
			}
		}
		for(Arena arena : outstandingArenas) {
			if(arena.getId() == id) {
				return arena;
			}
		}
		return null;
	}

	public boolean isStudentCanCreateArena() {
		return studentPermissions != CompetitionPermissionLevel.NONE;
	}
	
	public boolean isTutorCanCreateArena() {
		return tutorPermissions != CompetitionPermissionLevel.NONE;
	}
	
	public boolean isStudentCanCreateRepeatableArena() {
		return studentPermissions == CompetitionPermissionLevel.CREATE_REPEATABLE;
	}
	
	public boolean isTutorCanCreateRepeatableArena() {
		return tutorPermissions == CompetitionPermissionLevel.CREATE_REPEATABLE;
	}

	public boolean isTested() {
		return tested;
	}

	public void setTested(boolean tested) {
		this.tested = tested;
	}

	public void setStudentPermissions(CompetitionPermissionLevel studentPermissions) {
		this.studentPermissions = studentPermissions;
	}

	public void setTutorPermissions(CompetitionPermissionLevel tutorPermissions) {
		this.tutorPermissions = tutorPermissions;
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

	// calculated methods
	public String getFirstStartDateStr() {
		return dateParser.format(firstStartDate);
	}

	public void setFirstStartDateStr(String firstStartDateStr) {
		try {
			firstStartDate = dateParser.parse(firstStartDateStr);
		} catch (ParseException e) {
			logger.error("Could not parse " + firstStartDateStr, e);
		}
	}

	public boolean isCalculated() {
		return calculated;
	}

	public String getFileAppropriateName() {
		return name.replaceAll("[^\\w]", "");
	}

	public boolean isCreateArena(PASTAUser user) {
		return user.isTutor() || isStudentCanCreateArena();
	}

	public boolean isCreateRepeatingArena(PASTAUser user) {
		return user.isInstructor() || (user.isTutor() && isTutorCanCreateRepeatableArena())
				|| (!user.isTutor() && isStudentCanCreateRepeatableArena());
	}

	public Date getNextRunDate() {
		if (frequency == null || firstStartDate == null) {
			return null;
		}
		return frequency.nextExecution(firstStartDate);
	}

	public Arena getOfficialArena() {
		return officialArena;
	}

	public void setOfficialArena(Arena arena) {
		officialArena = arena;
		officialArena.setCompetition(this);
	}

	public void completeArena(Arena arena) {
		outstandingArenas.remove(arena);
		completedArenas.add(arena);
	}

	public void addNewArena(Arena arena) {
		outstandingArenas.add(arena);
		arena.setCompetition(this);
	}
	
	public boolean isFinished() {
		return outstandingArenas.isEmpty();
	}

	public boolean isCompleted(Arena arena) {
		return completedArenas.contains(arena);
	}
	
	public boolean isCompleted(long arenaId) {
		for(Arena arena : completedArenas) {
			if(arena.getId() == arenaId) {
				return true;
			}
		}
		return false;
	}

	public String getFileLocation() {
		return ProjectProperties.getInstance().getCompetitionsLocation() + getFileAppropriateName();
	}
	
	public File getCodeLocation() {
		return new File(getFileLocation(), "code");
	}
	
	public boolean hasCode() {
		return getCodeLocation().exists();
	}
	/**
	 * Renaming of hasCode() for Spring
	 */
	public boolean isHasCode() {
		return hasCode();
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public int compareTo(Competition o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		Competition other = (Competition) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
