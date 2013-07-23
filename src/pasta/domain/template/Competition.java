package pasta.domain.template;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.PASTATime;
import pasta.domain.PASTAUser;
import pasta.util.ProjectProperties;

public class Competition {
	
	public final static SimpleDateFormat dateParser 
		= new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private String name;
	// if null - calculated competition
	private Collection<Arena> arenas = null;
	private boolean studentCreatableArena;
	private boolean studentCreatableRepeatableArena;
	private boolean tutorCreatableRepeatableArena;
	private boolean tested;
	
	private Collection<Assessment> linkedAssessments = new HashSet<Assessment>();
	
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
	public Collection<Assessment> getLinkedAssessments() {
		return linkedAssessments;
	}
	public void setLinkedAssessments(Collection<Assessment> linkedAssessments) {
		this.linkedAssessments.clear();
		this.linkedAssessments.addAll(linkedAssessments);
	}
	
	// calculated methods
	public String getFirstStartDateStr(){
		return dateParser.format(firstStartDate);
	}
	
	public void setFirstStartDateStr(String firstStartDateStr){
		try {
			firstStartDate = dateParser.parse(firstStartDateStr);
		} catch (ParseException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.error("Could not parse " + firstStartDateStr
					+ "\r\n" + sw.toString());
		}
	}
	
	public boolean isCalculated(){
		return (arenas == null);
	}
	
	public boolean isLive() {
		return linkedAssessments != null && !linkedAssessments.isEmpty();
	}
	
	public void addAssessment(Assessment assessment){
		linkedAssessments.add(assessment);
	}
	
	public void removeAssessment(Assessment assessment){
		linkedAssessments.remove(assessment);
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
		String output = "<competitionProperties>" + System.getProperty("line.separator");
		output += "\t<name>"+name+"</name>" + System.getProperty("line.separator");
		output += "\t<studentCreatableArena>"+studentCreatableArena+"</studentCreatableArena>" + System.getProperty("line.separator");
		output += "\t<studentCreatableRepeatableArena>"+studentCreatableRepeatableArena+"</studentCreatableRepeatableArena>" + System.getProperty("line.separator");
		output += "\t<tutorCreatableRepeatableArena>"+tutorCreatableRepeatableArena+"</tutorCreatableRepeatableArena>" + System.getProperty("line.separator");
		output += "\t<tested>"+tested+"</tested>" + System.getProperty("line.separator");
		if(firstStartDate == null){
			firstStartDate = new Date();
			// make it next year (realistically afte the semester ended)
			firstStartDate.setTime(firstStartDate.getTime()+31536000000l);
		}
		output += "\t<firstStartDate>"+ProjectProperties.formatDate(firstStartDate)+"</firstStartDate>" + System.getProperty("line.separator");
		
		output += "\t<frequency>";
		if(frequency != null){
			output += frequency;
		}
		else{
			output += "0s";
		}
		output += "</frequency>" + System.getProperty("line.separator");
			
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
