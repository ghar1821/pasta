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

package pasta.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.moss.MossResults;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Moss manager.
 * <p>
 * Manages interaction between controller and data.
 * This class works as an abstraction layer between the controller 
 * and the underlying data models. This class contains the majority
 * of the logic code dealing with objects and their interactions.
 * 
 * Requires the moss scripts to be copied into the pasta folder
 * (where all of the assessment and submission data is kept), in a 
 * folder labeled "moss". Within that folder, there should be another 
 * folder entitled "template" and within that there should be an ant
 * script called "build.xml" which has a target called "run" and 
 * also accepts two parameters. The first is "assessment" which is
 * the assessment name, the second is "defaultLocation" which points
 * to the folder which holds all of the student submissions.
 * 
 * The manager assumes that once the script has executed, the URL
 * of the moss results is written into "location.txt". This URL
 * is then scraped for the required data.
 * 
 * This manager does no caching.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-24
 *
 */
@Service("mossManager")
@Repository
public class MossManager {
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(MossManager.class);
	
	/**
	 * Method to initiate the execution of a moss script
	 * <p>
	 * <ol>
	 * 	<li>Copy the template moss into assessmentName/date</li>
	 * 	<li>Load up build.xml</li>
	 * 	<li>Set "assessment" and "defaultLocation" properties with the correct values.</li>
	 * 	<li>Execute target "run"</li>
	 * 	<li>Wait for script to upload files to the moss server and get back a response</li>
	 * 	<li>Read "location.txt" to get the URL that holds the moss results.</li>
	 * 	<li>Go to the URL specified and parse the page</li>
	 * 	<li>Store the similarity list to the file assessmentName/date.csv</li>
	 * 	<li>Delete the directory created in step 1.</li>
	 * </ol>
	 * 
	 * <b>NOTE: Step 9 - the deletion of the folder - will not occur if there is an error in
	 * the process. If the folder remains, there was an error and it should contain
	 * sufficient information to debug any issues that have arisen.</b>
	 * 
	 * If the system is set to use a proxy, this is where it's done.
	 * 
	 * @param assessment
	 */
	public void runMoss(String assessment){
		String location = ProjectProperties.getInstance().getProjectLocation()
				+ "/moss/" + assessment + "/"
				+ PASTAUtil.formatDate(new Date());
		// copy moss somewhere safe for it to run (own folder. ->
		// moss/assessmentName/dateRun)
		try {
			FileUtils.copyDirectory(new File(ProjectProperties.getInstance()
					.getProjectLocation() + "/moss/template"), new File(
					location + "/"));
			// run moss
			File buildFile = new File(location + "/build.xml");

			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			Project project = new Project();

			project.setUserProperty("ant.file", buildFile.getAbsolutePath());
			project.setUserProperty("assessment", assessment);
			project.setUserProperty("defaultLocation", ProjectProperties
					.getInstance().getProjectLocation() + "/submissions");
			project.setBasedir(location + "/");

			project.init();

			project.addReference("ant.projectHelper", projectHelper);
			projectHelper.parse(project, buildFile);

			try {
				project.executeTarget("run");
			} catch (BuildException e) {
				logger.error("Could not run moss " + assessment);
			}
			// wait for output
			// read url
			Scanner locationIn = new Scanner(new File(location
					+ "/location.txt"));
			
			String mossUrl = locationIn.nextLine();
			
			locationIn.close();
			
			// read html page
			URL url = new URL(mossUrl);
			
			InputStream is;
			if(ProjectProperties.getInstance().usingProxy()){
				logger.info("Executing moss using proxy");
				URLConnection conn = url.openConnection(ProjectProperties.getInstance().getProxy());
				is = conn.getInputStream();
			}
			else{
				logger.info("Executing moss without a proxy");
				is = url.openStream();
			}
	        
	         // throws an IOException
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			String webpage = "";
	        while ((line = br.readLine()) != null) {
	            webpage += line;
	        }
			
			is.close();

			// process results (web page - csv)
			// strip top
			webpage = webpage.replaceFirst("<HTML>.*?<TD>", "");
			// strip bottom
			webpage = webpage.replaceFirst("</TABLE>.*", "");
			// start tearing into the body
			webpage = webpage.replaceAll("<A HREF=.*?>", "");
			webpage = webpage.replaceAll("</A>", "");
			webpage = webpage.replaceAll(" ALIGN=right", "");
			webpage = webpage.replaceAll("<TR><TD>", "\r\n");
			webpage = webpage.replaceAll("<TD>", ",");
			webpage = webpage.replaceAll("/ \\(", ",");
			webpage = webpage.replaceAll("%\\)", "");
			webpage = webpage.replaceAll(" +", "");

			// save results
			PrintWriter out = new PrintWriter(new File(location + ".csv"));
			out.println(mossUrl);
			out.print(webpage);
			out.close();

			// clean up
			FileUtils.deleteDirectory(new File(location + "/"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Get the results of a moss execution
	 * <p>
	 * Read $ProjectLocation$/moss/$assessmentName$/$date$.csv and store
	 * it as a {@link pasta.domain.moss.MossResults}.
	 * 
	 * @param assessment the short name (no whitespace) of the assessment
	 * @param date the date (format yyyy-MM-dd'T'HH-mm-ss) 
	 * @return the correct moss results (will never return null)
	 */
	public MossResults getMossRun(String assessment, String date) {
		MossResults results = new MossResults();

		try {
			Scanner in = new Scanner(new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/moss/"
					+ assessment
					+ "/"
					+ date
					+ ".csv"));
			
			// first line is always a link
			results.setLink(in.nextLine());
			
			// read in the results
			while(in.hasNextLine()){
				String[] values = in.nextLine().split(",");
				results.addPairing(values[0], values[2], 
						Integer.parseInt(values[1]), Integer.parseInt(values[3]),
						Integer.parseInt(values[4]));
			}
			
			in.close();
			
			results.setDate(PASTAUtil.parseDate(date).toString());
			
		} catch (FileNotFoundException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;
	}
	
	/**
	 * Get the list of moss results.
	 * 
	 * @param assessment the short name (no whitespace) of the assessment
	 * @return a map key: date (format yyyy-MM-dd'T'HH-mm-ss), value: date (format Date.toString()) 
	 */
	public Map<String,String> getMossList(String assessment) {
		Map<String,String> mossList = new TreeMap<String,String>();
		
		String[] filenames = new File(ProjectProperties.getInstance().getProjectLocation()
				+"/moss/"+assessment).list(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".csv");
					}
				});
		
		if(filenames != null){
			for(String file: filenames){
				try {
					mossList.put(file.replace(".csv", ""),PASTAUtil.parseDate(file.replace(".csv", "")).toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return mossList;
	}
}
