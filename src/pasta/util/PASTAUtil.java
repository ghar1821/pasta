package pasta.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import pasta.domain.FileTreeNode;
import pasta.login.DBAuthValidator;
import pasta.login.DummyAuthValidator;
import pasta.login.FTPAuthValidator;
import pasta.login.ImapAuthValidator;
import pasta.repository.LoginDAO;

@Component
/**
 * The project properties.
 * 
 * Uses singleton pattern.
 * 
 * @author Alex
 *
 */
public class PASTAUtil {
	protected final Log logger = LogFactory.getLog(getClass());
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

	/**
	 * Code used to extract a zip file.
	 * @param zipFile - the file
	 * @throws ZipException
	 * @throws IOException
	 */
	static public void extractFolder(String zipFile) throws ZipException, IOException {
	    System.out.println(zipFile);
	    int BUFFER = 2048;
	    File file = new File(zipFile);

	    ZipFile zip = new ZipFile(file);
	    
	    String newPath = zipFile.substring(0, zipFile.lastIndexOf("/"));

	    new File(newPath).mkdir();
	    Enumeration zipFileEntries = zip.entries();

	    // Process each entry
	    while (zipFileEntries.hasMoreElements()){
	        // grab a zip file entry
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        File destFile = new File(newPath, currentEntry);
	        //destFile = new File(newPath, destFile.getName());
	        File destinationParent = destFile.getParentFile();

	        // create the parent directory structure if needed
	        destinationParent.mkdirs();

	        if (!entry.isDirectory()){
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
	            fos.close();
	            is.close();
	        }

	        if (currentEntry.endsWith(".zip")){
	            // found a zip file, try to open
	            extractFolder(destFile.getAbsolutePath());
	        }
	    }
	    zip.close();
	}
	
	public static String formatDate(Date toFormat){
		return sdf.format(toFormat);
	}
	
	public static Date parseDate(String date) throws ParseException{
		return sdf.parse(date);
	}
	
	public static FileTreeNode generateFileTree(String username,
			String assessmentName, String assessmentDate) {
		return generateFileTree(ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/assessments/"
				+ assessmentName
				+ "/"
				+ assessmentDate
				+ "/submission");
	}
	
	public static FileTreeNode generateFileTree(String location){
		File[] subDirectories = new File(location).listFiles();
		if(subDirectories == null || subDirectories.length == 0){
			FileTreeNode current = new FileTreeNode(location, null);
			if(new File(location).isDirectory()){
				current.setLeaf(false);
			}
			return current;
		}
		List<FileTreeNode> children = new LinkedList<FileTreeNode>();
		for(File subDirectory: subDirectories){
			children.add(generateFileTree(subDirectory.getAbsolutePath()));
		}
		return new FileTreeNode(location, children);
	}

	public static String scrapeFile(String location) {
		String file = "";
		try {
			Scanner in = new Scanner(new File(location));
			while(in.hasNextLine()){
				file+=in.nextLine() + System.getProperty("line.separator");
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public static HashMap<String, FileTreeNode> genereateFileTree(String username, String assessmentName) {
		HashMap<String, FileTreeNode> allsubmissions = new HashMap<String, FileTreeNode>();
		
		String[] allSubs = (new File(ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/assessments/"
				+ assessmentName).list());
		if(allSubs != null && allSubs.length > 0){
			for(String submission : allSubs){
				if(submission.matches("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d-\\d\\d-\\d\\d")){
					allsubmissions.put(submission, generateFileTree(username, assessmentName, submission));
				}
			}
		}
		
		return allsubmissions;
	}
	
}