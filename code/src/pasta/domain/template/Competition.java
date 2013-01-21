package pasta.domain.template;

import java.util.Collection;
import java.util.Date;

import pasta.domain.PASTATime;
import pasta.domain.PASTAUser;
import pasta.util.ProjectProperties;

public class Competition {
	private String name;
	// if null - calculated competition
	private Collection<Arena> arenas = null;
	private boolean studentCreatableArena;
	private boolean studentCreatableRepeatableArena;
	private boolean tutorCreatableRepeatableArena;
	private boolean tested;
	
	private PASTATime frequency = null;
	private Date firstStartDate;
	
	// getters and setters
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Collection<Arena> getArenas() {
		return arenas;
	}
	public void setArenas(Collection<Arena> arenas) {
		this.arenas = arenas;
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
	public void setStudentCreatableRepeatableArena(
			boolean studentCreatableRepeatableArena) {
		this.studentCreatableRepeatableArena = studentCreatableRepeatableArena;
	}
	public boolean isTutorCreatableRepeatableArena() {
		return tutorCreatableRepeatableArena;
	}
	public void setTutorCreatableRepeatableArena(
			boolean tutorCreatableRepeatableArena) {
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
	
	// calculated methods
	public boolean isCalculated(){
		return (arenas == null);
	}
	
	public String getShortName(){
		return name.replace(" ", "");
	}
	
	public boolean isCreateArena(PASTAUser user){
		return user.isTutor() || studentCreatableArena;
	}
	
	public boolean isCreateRepeatingArena(PASTAUser user){
		return user.isInstructor() || 
				(user.isTutor() && tutorCreatableRepeatableArena) || 
				(!user.isTutor() && studentCreatableRepeatableArena);
	}
	
	public Date getNextRunDate(){
		if(frequency == null || firstStartDate == null){
			return null;
		}
		return frequency.nextExecution(firstStartDate);
	}
	
	public String toString(){
		String output = "<competitionProperties>";
		
		output += "\t<name>"+name+"</name>" + System.getProperty("line.separator");
		output += "\t<studentCreatableArena>"+studentCreatableArena+"</studentCreatableArena>" + System.getProperty("line.separator");
		output += "\t<studentCreatableRepeatableArena>"+studentCreatableRepeatableArena+"</studentCreatableRepeatableArena>" + System.getProperty("line.separator");
		output += "\t<tutorCreatableRepeatableArena>"+tutorCreatableRepeatableArena+"</tutorCreatableRepeatableArena>" + System.getProperty("line.separator");
		output += "\t<tested>"+tested+"</tested>" + System.getProperty("line.separator");
		if(isCalculated()){
			output += "\t<firstStartDate>"+ProjectProperties.formatDate(firstStartDate)+"</firstStartDate>" + System.getProperty("line.separator");
			output += "\t<frequency>"+frequency+"</frequency>" + System.getProperty("line.separator");
		}
		if(arenas != null){
			output += "\t<arenas>" + System.getProperty("line.separator");
				for(Arena currArena: arenas){
					output += "\t\t" + currArena + System.getProperty("line.separator");
				}
			output += "\t</arenas>" + System.getProperty("line.separator");
		}
		
		output += "</competitionProperties>";
		return output;
	}
	
	public String getFileLocation() {
		return ProjectProperties.getInstance().getProjectLocation()+"/template/competition/"+getShortName();
	}
}
