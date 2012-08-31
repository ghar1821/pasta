package info1103.project.validation;

import info1103.project.domain.AllStudentAssessmentData;
import info1103.project.domain.Execution;
import info1103.project.domain.Submission;
import info1103.project.scheduler.ExecutionScheduler;
import info1103.project.util.ProjectProperties;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
/**
 * Submission validation class.
 * 
 * Checks to see if the code will compile
 * 
 * @author Alex
 *
 */
public class SubmissionValidator {
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Method to check if the submission can compile
	 * @param target - submission
	 * @param errors - error list
	 */
	public void validate(Object target, Errors errors) {
		Submission code = (Submission) target;
		
		// reject if there was no file
		if(code.getFile().getSize() == 0){
			errors.reject("Submission.NoFile");
			return;
		}
		if(!code.getFile().getOriginalFilename().endsWith("zip")){
			errors.reject("Submission.NotZip");
			return;
		}
		
		File latest = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/" + code.getUnikey() + "/"
				+ code.getAssessmentName() + "/latest");
		
		// if there already is a submission
		if(latest.exists()){
			// archive old version
			try {
				Scanner in = new Scanner(new File(latest.getAbsolutePath()+"/submission.info"));
				File newLocation = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/" + code.getUnikey() + "/"
						+ code.getAssessmentName() + "/history/" + in.nextLine().replace("submission-date:", "/")+"/");
				in.close();
				newLocation.getParentFile().mkdirs();
				
				logger.info(latest.renameTo(newLocation));
				
				
			} catch (FileNotFoundException e) {
				logger.error(e);
			}
			
		}
		
		// make latest folder
		latest.mkdirs();
		
		// copy file to location
		try {
			code.getFile().transferTo(new File(latest.getAbsolutePath() + "/"+code.getUnikey()+"-"+code.getAssessmentName().toLowerCase().replace(" ", "")+".zip"));
			
			// extract file
			try{
				extractFolder(latest.getAbsolutePath() + "/"+code.getUnikey()+"-"+code.getAssessmentName().toLowerCase().replace(" ", "")+".zip");
			}
			catch(ZipException e){
				// ignore it.
			}
			File zipFile = new File(latest.getAbsolutePath() + "/"+code.getUnikey()+"-"+code.getAssessmentName().toLowerCase().replace(" ", "")+".zip");
			
			PrintWriter out = new PrintWriter(latest.getAbsolutePath() + "/submission.info");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
			out.println("submission-date:"+df.format(new Date()));
		    out.close();
		    
		    // copy testing code across
			FileUtils.copyDirectory(new File(ProjectProperties.getInstance().getTemplateLocation()+
					"/"+code.getAssessmentName()+"/code"), latest);
	    		
			// compile
			ProcessBuilder compiler = new ProcessBuilder("bash", "-c", "ant build clean");
			if(ProjectProperties.getInstance().getJava6Location() != null){
				compiler.environment().put("JAVA_HOME", ProjectProperties.getInstance().getJava6Location());
			}
			compiler.redirectErrorStream(true);
			compiler.directory(latest);
			compiler.redirectErrorStream(true);
			Process compile = compiler.start();

			BufferedReader compileIn = new BufferedReader(new InputStreamReader(compile.getInputStream()));
			String line;
			String compileMessage = "Compiler Errors:\r\n";
			while ((line = compileIn.readLine()) != null) {
				compileMessage += line + "\r\n";
			}
			compileMessage += "\r\n\r\n ERROR CODE: " + compile.waitFor();
			
		    // if errors, return errors, dump ant output to compile.errors
			if (!compileMessage.contains("BUILD SUCCESSFUL")) {
				PrintWriter compileErrors = new PrintWriter(latest.getAbsolutePath() + "/compile.errors");
				compileErrors.println(compileMessage);
				compileErrors.close();
			}
			compile.destroy();
			
			//cleanup
			FileUtils.deleteDirectory(new File(latest.getAbsolutePath() + "/junit_jars"));
			FileUtils.deleteDirectory(new File(latest.getAbsolutePath() + "/bin"));
			FileUtils.deleteDirectory(new File(latest.getAbsolutePath() + "/test"));
			(new File(latest.getAbsolutePath() + "/build.xml")).delete();
			
			// schedule the execution
			ExecutionScheduler.getInstance().scheduleExecution(new Execution(code.getUnikey(), code.getAssessmentName(), new Date()));
			// update caching
			AllStudentAssessmentData.getInstance().updateStudent(code.getUnikey(), code.getAssessmentName());
			
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (InterruptedException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Code used to extract a zip file.
	 * @param zipFile - the file
	 * @throws ZipException
	 * @throws IOException
	 */
	static public void extractFolder(String zipFile) throws ZipException, IOException 
	{
	    System.out.println(zipFile);
	    int BUFFER = 2048;
	    File file = new File(zipFile);

	    ZipFile zip = new ZipFile(file);
	    String newPath = zipFile.substring(0, zipFile.lastIndexOf("/"));

	    new File(newPath).mkdir();
	    Enumeration zipFileEntries = zip.entries();

	    // Process each entry
	    while (zipFileEntries.hasMoreElements())
	    {
	        // grab a zip file entry
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        File destFile = new File(newPath, currentEntry);
	        //destFile = new File(newPath, destFile.getName());
	        File destinationParent = destFile.getParentFile();

	        // create the parent directory structure if needed
	        destinationParent.mkdirs();

	        if (!entry.isDirectory())
	        {
	            BufferedInputStream is = new BufferedInputStream(zip
	            .getInputStream(entry));
	            int currentByte;
	            // establish buffer for writing file
	            byte data[] = new byte[BUFFER];

	            // write the current file to disk
	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos,
	            BUFFER);

	            // read and write until last byte is encountered
	            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
	        }

	        if (currentEntry.endsWith(".zip"))
	        {
	            // found a zip file, try to open
	            extractFolder(destFile.getAbsolutePath());
	        }
	    }
	}
}
