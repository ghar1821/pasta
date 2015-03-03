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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.PASTATime;
import pasta.domain.PASTAUser;
import pasta.util.PASTAUtil;
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
public class Competition {

	public final static SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	protected final Log logger = LogFactory.getLog(getClass());

	private String name;
	/** if null - calculated competition **/
	private Arena officialArena = null;
	private Map<String, Arena> outstandingArenas = new TreeMap<String, Arena>();
	private Map<String, Arena> completedArenas = new TreeMap<String, Arena>();
	private boolean studentCreatableArena;
	private boolean studentCreatableRepeatableArena;
	private boolean tutorCreatableRepeatableArena;
	private boolean tested;
	private boolean hidden;

	/** list of all linked assessments **/
	private Collection<Assessment> linkedAssessments = new TreeSet<Assessment>();

	private PASTATime frequency = null;
	private Date firstStartDate;

	// getters and setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Arena> getOutstandingArenas() {
		return outstandingArenas.values();
	}

	public void setOutstandingArenas(Map<String, Arena> arenas) {
		this.outstandingArenas = arenas;
	}

	public void setOutstandingArenas(Collection<Arena> arenas) {
		this.outstandingArenas.clear();
		for (Arena arena : arenas) {
			this.outstandingArenas.put(arena.getShortName(), arena);
		}
	}

	public Collection<Arena> getCompletedArenas() {
		return completedArenas.values();
	}

	public void setCompletedArenas(Map<String, Arena> arenas) {
		this.completedArenas = arenas;
	}

	public void setCompletedArenas(Collection<Arena> arenas) {
		this.completedArenas.clear();
		for (Arena arena : arenas) {
			this.completedArenas.put(arena.getShortName(), arena);
		}
	}

	public Arena getArena(String name) {
		if (name.replace(" ", "").toLowerCase().equals("officialarena")) {
			return officialArena;
		}
		if (completedArenas.containsKey(name.replace(" ", ""))) {
			return completedArenas.get(name.replace(" ", ""));
		}
		if (outstandingArenas.containsKey(name.replace(" ", ""))) {
			return outstandingArenas.get(name.replace(" ", ""));
		}
		return null;
	}

	public boolean isStudentCreatableArena() {
		return studentCreatableArena;
	}

	public void setStudentCreatableArena(boolean studentCreatableArena) {
		this.studentCreatableArena = studentCreatableArena;
	}

	public boolean isTested() {
		return tested;
	}

	public void setTested(boolean tested) {
		this.tested = tested;
	}

	public boolean isStudentCreatableRepeatableArena() {
		return studentCreatableRepeatableArena;
	}

	public void setStudentCreatableRepeatableArena(boolean studentCreatableRepeatableArena) {
		this.studentCreatableRepeatableArena = studentCreatableRepeatableArena;
	}

	public boolean isTutorCreatableRepeatableArena() {
		return tutorCreatableRepeatableArena;
	}

	public void setTutorCreatableRepeatableArena(boolean tutorCreatableRepeatableArena) {
		this.tutorCreatableRepeatableArena = tutorCreatableRepeatableArena;
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

	public Collection<Assessment> getLinkedAssessments() {
		return linkedAssessments;
	}

	public void setLinkedAssessments(Collection<Assessment> linkedAssessments) {
		this.linkedAssessments.clear();
		this.linkedAssessments.addAll(linkedAssessments);
	}

	// calculated methods
	public String getFirstStartDateStr() {
		return dateParser.format(firstStartDate);
	}

	public void setFirstStartDateStr(String firstStartDateStr) {
		try {
			firstStartDate = dateParser.parse(firstStartDateStr);
		} catch (ParseException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.error("Could not parse " + firstStartDateStr + "\r\n" + sw.toString());
		}
	}

	public boolean isCalculated() {
		return (outstandingArenas == null || outstandingArenas.size() == 0 || (outstandingArenas.size() == 0
				&& completedArenas != null && completedArenas.size() == 0));
	}

	public boolean isLive() {
		return linkedAssessments != null && !linkedAssessments.isEmpty();
	}

	public void addAssessment(Assessment assessment) {
		linkedAssessments.add(assessment);
	}

	public void removeAssessment(Assessment assessment) {
		linkedAssessments.remove(assessment);
	}

	public String getShortName() {
		return name.replace(" ", "");
	}

	public boolean isCreateArena(PASTAUser user) {
		return user.isTutor() || studentCreatableArena;
	}

	public boolean isCreateRepeatingArena(PASTAUser user) {
		return user.isInstructor() || (user.isTutor() && tutorCreatableRepeatableArena)
				|| (!user.isTutor() && studentCreatableRepeatableArena);
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
	}

	public void completeArena(Arena arena) {
		outstandingArenas.remove(arena.getName());
		completedArenas.put(arena.getName(), arena);
	}

	public void addNewArena(Arena arena) {
		outstandingArenas.put(arena.getName().replace(" ", ""), arena);
	}

	public boolean isCompleted(String arenaName) {
		return completedArenas.containsKey(arenaName);
	}

	public String toString() {
		String output = "<competitionProperties>" + System.getProperty("line.separator");
		output += "\t<name>" + name + "</name>" + System.getProperty("line.separator");
		output += "\t<studentCreatableArena>" + studentCreatableArena + "</studentCreatableArena>"
				+ System.getProperty("line.separator");
		output += "\t<studentCreatableRepeatableArena>" + studentCreatableRepeatableArena
				+ "</studentCreatableRepeatableArena>" + System.getProperty("line.separator");
		output += "\t<tutorCreatableRepeatableArena>" + tutorCreatableRepeatableArena
				+ "</tutorCreatableRepeatableArena>" + System.getProperty("line.separator");
		output += "\t<tested>" + tested + "</tested>" + System.getProperty("line.separator");
		output += "\t<hidden>" + hidden + "</hidden>" + System.getProperty("line.separator");
		if (firstStartDate == null) {
			firstStartDate = new Date();
			// make it next year (realistically afte the semester ended)
			firstStartDate.setTime(firstStartDate.getTime() + 31536000000l);
		}
		output += "\t<firstStartDate>" + PASTAUtil.formatDate(firstStartDate) + "</firstStartDate>"
				+ System.getProperty("line.separator");

		output += "\t<frequency>";
		if (frequency != null) {
			output += frequency;
		} else {
			output += "0s";
		}
		output += "</frequency>" + System.getProperty("line.separator") + "</competitionProperties>";
		return output;
	}

	public String getFileLocation() {
		return ProjectProperties.getInstance().getCompetitionsLocation() + getShortName();
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}
