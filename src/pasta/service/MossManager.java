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

@Service("mossManager")
@Repository
/**
 * Moss manager.
 * 
 * Manages interaction between controller and moss data.
 * 
 * @author Alex
 *
 */
public class MossManager {
	
//	private ExecutionScheduler scheduler;
//	
//	@Autowired
//	public void setMyScheduler(ExecutionScheduler myScheduler) {
//		this.scheduler = myScheduler;
//	}
	
	@Autowired
	private ApplicationContext context;

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(MossManager.class);
	
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
