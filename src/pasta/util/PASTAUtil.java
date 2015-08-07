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

package pasta.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import pasta.domain.FileTreeNode;
import pasta.domain.players.PlayerHistory;
import pasta.domain.template.Competition;
import pasta.domain.user.PASTAUser;

/**
 * Groups together commonly used methods.
 * <p>
 * All methods are static.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
@Component
public class PASTAUtil {
	protected static final Log logger = LogFactory.getLog(PASTAUtil.class);
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
	private static SimpleDateFormat readableSdf = new SimpleDateFormat("EEE dd MMMM yyyy 'at' HH:mm");

	/**
	 * Code used to extract a zip file.
	 * 
	 * @param zipFile the file to extract
	 * @throws ZipException
	 * @throws IOException
	 */
	static public void extractFolder(String zipFile) throws ZipException, IOException {
	    logger.info("Unzipping " + zipFile);
	    int BUFFER = 2048;
	    File file = new File(zipFile);

	    ZipFile zip = new ZipFile(file);
	    
	    String newPath = file.getParent();

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
	
	public static String[] listZipContents(File file) throws ZipException, IOException {
		ZipFile zip = new ZipFile(file);
		Enumeration zipFileEntries = zip.entries();
		List<String> fileList = new LinkedList<String>();
		// Process each entry
		while (zipFileEntries.hasMoreElements()){
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			if (!entry.isDirectory()){
				fileList.add(entry.getName());
			}
		}
		zip.close();
		return fileList.toArray(new String[fileList.size()]);
	}
	
	public static String[] listZipContents(String zipFile) throws ZipException, IOException {
	    File file = new File(zipFile);
	    return listZipContents(file);
	}
	
	public static String[] listDirectoryContents(String baseFilename) {
		File file = new File(baseFilename);
		return listDirectoryContents(file);
	}
	
	public static String[] listDirectoryContents(File file) {
		if(!file.isDirectory()) {
			return new String[] {file.getName()};
		}
		List<String> contents = new LinkedList<String>();
		listDirectory(contents, file.getAbsolutePath(), file);
		return contents.toArray(new String[contents.size()]);
	}
	private static void listDirectory(List<String> contents, String base, File current) {
		for(File f : current.listFiles()) {
			if(f.isDirectory()) {
				listDirectory(contents, base, f);
			} else {
				contents.add(f.getAbsolutePath().substring(base.length()));
			}
		}
	}

	/**
	 * Format date using the format yyyy-MM-dd'T'HH-mm-ss
	 * 
	 * @param toFormat the date to format
	 * @return the string representation of the date e.g. 2014-02-31T12-00-01
	 */
	public static String formatDate(Date toFormat){
		return sdf.format(toFormat);
	}
	
	/**
	 * Format date using the format EEE dd MMMM yyyy 'at' HH:mm
	 * 
	 * @param toFormat the date to format
	 * @return the string representation of the date e.g. Thu 30 April 2015 at 09:51
	 */
	public static String formatDateReadable(Date toFormat){
		return readableSdf.format(toFormat);
	}
	
	/**
	 * Parse date from the format yyyy-MM-dd'T'HH-mm-ss into java.util.Date
	 * 
	 * @param date the string representation of the date e.g. 2014-02-31T12-00-01
	 * @return the java.util.date object
	 * @throws ParseException if there is an error
	 */
	public static Date parseDate(String date) throws ParseException{
		return sdf.parse(date);
	}
	
	/**
	 * Helper method for {@link #generateFileTree(String)}
	 * 
	 * @param user the user
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date of the assessment
	 * @return the file tree node that is root for the file tree.
	 */
	public static FileTreeNode generateFileTree(PASTAUser user,
			long assessmentId, String assessmentDate) {
		return generateFileTree(ProjectProperties.getInstance().getSubmissionsLocation()
				+ user.getUsername()
				+ "/assessments/"
				+ assessmentId
				+ "/"
				+ assessmentDate
				+ "/submission");
	}
	
	/**
	 * Recursively generate the FileTreeNode based on a location.
	 * 
	 * @param location the location of the root
	 * @return the root node for the file system
	 */
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

	/**
	 * Read a file and store it as a string
	 * 
	 * @param location the location of the file
	 * @return the string content of the file
	 */
	public static String scrapeFile(String location) {
		return scrapeFile(new File(location));
	}
	
	/**
	 * Read a file and store it as a string
	 * 
	 * @param file the file to read
	 * @return the string content of the file
	 */
	public static String scrapeFile(File file) {
		StringBuilder sb = new StringBuilder();
		try {
			Scanner in = new Scanner(file);
			while(in.hasNextLine()){
				sb.append(in.nextLine()).append(System.lineSeparator());
			}
			in.close();
		} catch (FileNotFoundException e) {
			logger.error("Cannot scrape file.", e);
			return "";
		}
		return sb.toString();
	}
	
	/**
	 * Generate the file tree nodes for all submission attempts for a user and assessment.
	 * <p>
	 * Calls {@link #generateFileTree(String)} for all submission attempts.
	 * 
	 * @param user the user
	 * @param assessmentId the short (no whitespace) name of the assessment
	 * @return the map of file tree nodes for each submission with the submission
	 * date as a key.
	 */
	public static Map<String, FileTreeNode> generateFileTree(PASTAUser user, long assessmentId) {
		Map<String, FileTreeNode> allsubmissions = new TreeMap<String, FileTreeNode>();
		
		String[] allSubs = (new File(ProjectProperties.getInstance().getSubmissionsLocation()
				+ user.getUsername()
				+ "/assessments/"
				+ assessmentId).list());
		if(allSubs != null && allSubs.length > 0){
			for(String submission : allSubs){
				if(submission.matches("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d-\\d\\d-\\d\\d")){
					allsubmissions.put(submission, generateFileTree(user, assessmentId, submission));
				}
			}
		}
		
		return allsubmissions;
	}

	/**
	 * Generate the file tree for the players used by arena based competitions.
	 * 
	 * @param user the user
	 * @param competition the competition
	 * @param players a collection of players you are interested in.
	 * @return the map of file tree nodes, with the name of the player as the key.
	 */
	public static Map<String, FileTreeNode> generateFileTree(PASTAUser user,
			Competition competition, Collection<PlayerHistory> players) {
		Map<String, FileTreeNode> allPlayers = new TreeMap<String, FileTreeNode>();
		
		for(PlayerHistory player: players){
			if(player.getActivePlayer() != null){
				FileTreeNode node = generateFileTree(competition.getFileLocation()
						+ "/players/" + user.getUsername() + "/"
						+ player.getPlayerName()
						+ "/active/code/");
				if(node != null){
					allPlayers.put(player.getPlayerName(), node);
				}
			}
		}
		
		return allPlayers;
	}
	
	/**
	 * Zip up a file or folder.
	 * 
	 * @param zip the stream which holds the zip (so you can serve it straight to the user).
	 * @param file the root file/folder
	 * @param remove the string part of the path that should be removed (e.g. /user/PASTA/submissions/username/assessment....)
	 */
	public static void zip(ZipOutputStream zip, File file, String remove) {
		byte[] buffer = new byte[1024];
		if (file.isFile()) {
			// file - zip it
			try {
				// changing file separator to work with both windows and linux
				remove = remove.replace("/", "[\\\\/]");
				// clean up file name
				ZipEntry ze = new ZipEntry(file.getAbsolutePath().replaceAll(remove, ""));
				zip.putNextEntry(ze);
				FileInputStream in = new FileInputStream(file);
				int len;
				while ((len = in.read(buffer)) > 0) {
					zip.write(buffer, 0, len);
				}
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// directory - keep going
			for (File f : file.listFiles()) {
				zip(zip, f, remove);
			}
		}
	}
	
	public static Map<String, String> generateFileMap(File root) {
		return generateFileMap(root.getAbsolutePath());
	}
	
	public static Map<String, String> generateFileMap(String root) {
		File rootFile = new File(root);
		Map<String, String> filenames = new LinkedHashMap<String, String>();
		listFilesRecursive(root, rootFile, filenames);
		return filenames;
	}

	private static void listFilesRecursive(String root, File file, Map<String, String> filenames) {
		if(file.isFile()) {
			String path = file.getAbsolutePath();
			String shortened = path.substring(root.length());
			filenames.put(file.getAbsolutePath(), shortened);
		} else if(file.isDirectory()) {
			for(File child : file.listFiles()) {
				listFilesRecursive(root, child, filenames);
			}
		}
	}

	public static Date elapseTime(Date date, int calendarField, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(calendarField, amount);
		return cal.getTime();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<Class<? extends T>> getSubclasses(Class<T> clazz, String packageToSearch) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AssignableTypeFilter(clazz));
		Set<BeanDefinition> components = provider.findCandidateComponents(packageToSearch);
		List<Class<? extends T>> subclasses = new LinkedList<>();
		for (BeanDefinition component : components) {
		    try {
				subclasses.add((Class<? extends T>) Class.forName(component.getBeanClassName()));
			} catch (ClassNotFoundException e) { }
		}
		return subclasses;
	}

	public static boolean canDisplayFile(String location) {
		File file = new File(location);
		if(!file.exists()) {
			return false;
		}
		BufferedReader in = null;
		try {
			long num = 0;
			long ascii = 0;
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
			int c;
			while((c = in.read()) > 0) {
				if (Character.isWhitespace(c) || (c >= 33 && c <= 126)) {
					ascii++;
				}
				num++;
			}
			double ratio = (double)ascii / num;
			return ratio >= 0.95;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	public static String extractQualifiedName(String javaFileLocation) {
		return extractQualifiedName(new File(javaFileLocation));
	}
	public static String extractQualifiedName(File javaFile) {
		if(!javaFile.exists()) {
			return null;
		}
		String filename = javaFile.getName();
		if(!filename.toLowerCase().endsWith(".java")) {
			return null;
		}
		String classname = filename.substring(0, filename.lastIndexOf('.'));
		String thePackage = null;
		try (Scanner scn = new Scanner(javaFile)) {
			while(scn.hasNextLine()) {
				String line = scn.nextLine().trim();
				if(line.startsWith("package")) {
					line = line.substring("package".length()).trim();
					if(line.contains("/"))
						line = line.substring(0, line.indexOf('/'));
					thePackage = line;
					break;
				}
				if(line.startsWith("import")) {
					break;
				}
			}
		} catch (FileNotFoundException e) {
			return null;
		}
		if(thePackage != null) {
			return thePackage.replaceAll("[\\s;]+", "").concat(".").concat(classname);
		}
		return classname;
	}

	public static File getClassFileForQualifiedClassName(File base, String validatorName) {
		if(!validatorName.contains(".")) {
			File classFile = new File(base, validatorName + ".class");
			return classFile.exists() ? classFile : null;
		}
		String dir = validatorName.substring(0, validatorName.indexOf('.'));
		validatorName = validatorName.substring(dir.length() + 1);
		return getClassFileForQualifiedClassName(new File(base, dir), validatorName);
	}
	
	public static File getTemplateResource(String pathInProject) throws FileNotFoundException {
		File file = new File(ProjectProperties.getInstance().getProjectLocation() + pathInProject);
		if(file.exists() && !file.isDirectory()) {
			return file;
		}
		File copyFile = new File(ProjectProperties.getInstance().getServletContext().getRealPath("/WEB-INF/template_content/" + pathInProject));
		if(copyFile.exists()) {
			copy(copyFile, file);
			return file;
		}
		throw new FileNotFoundException("Template resource \"" + pathInProject + "\" not found.");
	}
	
	private static void copy(File from, File to) {
		if(from.isDirectory()) {
			to.mkdirs();
			for(File child : from.listFiles()) {
				copy(child, new File(to, child.getName()));
			}
		} else {
			if(to.exists()) {
				return;
			}
			logger.info("Copying template file " + from + " to " + to);
			try {
				FileUtils.copyFile(from, to);
			} catch (IOException e) {
				logger.error("Error copying " + from, e);
			}
		}
	}
	
	public static boolean isZipFile(File f) {
		// MAGIC bytes that indicate the start of a ZIP file
		byte[] MAGIC = { 'P', 'K', 0x3, 0x4 };
		boolean isZip = true;
		byte[] buffer = new byte[MAGIC.length];
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			raf.readFully(buffer);
			for (int i = 0; i < MAGIC.length; i++) {
				if (buffer[i] != MAGIC[i]) {
					isZip = false;
					break;
				}
			}
			raf.close();
		} catch (Throwable e) {
			isZip = false;
		}
		return isZip;
	}
}