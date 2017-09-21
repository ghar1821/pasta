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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import pasta.domain.FileTreeNode;
import pasta.domain.user.PASTAGroup;
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
	
	
	public static String[] listDirectoryContents(String baseFilename, boolean namesOnly) {
		File file = new File(baseFilename);
		return listDirectoryContents(file, namesOnly);
	}
	public static String[] listDirectoryContents(File file) {
		return listDirectoryContents(file, false);
	}
	public static String[] listDirectoryContents(File file, boolean namesOnly) {
		if(!file.isDirectory()) {
			return new String[] {file.getName()};
		}
		List<String> contents = new LinkedList<String>();
		listDirectory(contents, file.getAbsolutePath(), file, namesOnly);
		return contents.toArray(new String[contents.size()]);
	}
	private static void listDirectory(List<String> contents, String base, File current, boolean namesOnly) {
		for(File f : current.listFiles()) {
			if(f.isDirectory()) {
				listDirectory(contents, base, f, namesOnly);
			} else {
				contents.add(namesOnly ? f.getName() : f.getAbsolutePath().substring(base.length()));
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
	 * Generate a file tree for a specific submission
	 * 
	 * @param user the user
	 * @param assessmentId the id of the assessment
	 * @param assessmentDate the date of the submission
	 * @return the file tree node that is root for the file tree.
	 */
	public static FileTreeNode generateFileTree(PASTAUser user,
			long assessmentId, String assessmentDate) {
		File file = new File(ProjectProperties.getInstance().getSubmissionsLocation(),
				user.getUsername()
				+ "/assessments/"
				+ assessmentId
				+ "/"
				+ assessmentDate
				+ "/submission");
		String owner = user.getUsername();
		if(user.isGroup()) {
			owner = ((PASTAGroup) user).getName();
		}
		return generateFileTree(file, owner);
	}
		
	/**
	 * Generate a file tree rooted at the given directory/file.
	 * 
	 * This file tree is either part of a submission directory or is not going
	 * to be used to display the file tree in a user page. If neither of these
	 * is true, set an owner using {@link #generateFileTree(File, String)}.
	 * 
	 * @param root
	 *            the root of the file tree; can be a directory or a file.
	 * @return a file tree
	 */
	public static FileTreeNode generateFileTree(File root) {
		return fileTree(root, null);
	}
	
	/**
	 * Generate a file tree rooted at the given directory/file.
	 * 
	 * @param root
	 *            the root of the file tree; can be a directory or a file.
	 * @param owner
	 *            a string out of:
	 *            <ul>
	 *            <li><code>"unitTest"</code>: this file tree is part of the
	 *            unit test content directory
	 *            <li><code>"assessment"</code>: this file tree is part of the
	 *            assessment content directory
	 *            <li>anything else: this file tree is part of the submissions
	 *            directory
	 *            </ul>
	 * @return a file tree
	 */
	public static FileTreeNode generateFileTree(File root, String owner) {
		return fileTree(root, owner);
	}
	
	private static FileTreeNode fileTree(File node, String owner) {
		File[] subDirectories = node.listFiles();
		if(subDirectories == null || subDirectories.length == 0) {
			FileTreeNode current = new FileTreeNode(node, null, owner);
			if(node.isDirectory()){
				current.setLeaf(false);
			}
			return current;
		}
		List<FileTreeNode> children = new LinkedList<FileTreeNode>();
		for(File subDirectory: subDirectories){
			children.add(fileTree(subDirectory, owner));
		}
		return new FileTreeNode(node, children, owner);
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
			Scanner in = new Scanner(new FileInputStream(file));
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
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
			int c;
			while((c = in.read()) > 0) {
				if (Character.isWhitespace(c) || (c >= 33 && c <= 126)) {
					ascii++;
				}
				num++;
			}
			if(num == 0) {
				return true;
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
		if(!javaFile.exists() || javaFile.isDirectory()) {
			return null;
		}
		String filename = javaFile.getName();
		if(!filename.toLowerCase().endsWith(".java")) {
			return null;
		}
		String classname = filename.substring(0, filename.lastIndexOf('.'));
		String thePackage = null;
		try (Scanner scn = new Scanner(new FileInputStream(javaFile))) {
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
		// build templates require up to date lib directory
		if(pathInProject.contains("build_template")) {
			try {
				getTemplateResource("lib/");
			} catch (FileNotFoundException e) {
				// ignore: no lib folder to copy
			}
		}
		
		File file = new File(ProjectProperties.getInstance().getProjectLocation() + pathInProject);
		File copyFile = new File(ProjectProperties.getInstance().getServletContext().getRealPath("/WEB-INF/template_content/" + pathInProject));
		if(!file.isDirectory() && !isOutOfDate(file, copyFile)) {
			return file;
		}
		if(copyFile.exists()) {
			copy(copyFile, file);
			return file;
		}
		throw new FileNotFoundException("Template resource \"" + pathInProject + "\" not found.");
	}
	
	private static boolean isOutOfDate(File file, File reference) {
		return (!file.exists() 
				|| reference.lastModified() > file.lastModified());
	}
	
	private static void copy(File from, File to) {
		if(from.isDirectory()) {
			to.mkdirs();
			for(File child : from.listFiles()) {
				copy(child, new File(to, child.getName()));
			}
		} else {
			if(!isOutOfDate(to, from)) {
				return;
			}
			logger.info("Copying template file " + from + " to " + to);
			try {
				Files.createDirectories(to.getParentFile().toPath());
				Files.copy(from.toPath(), to.toPath(), 
						StandardCopyOption.COPY_ATTRIBUTES, 
						StandardCopyOption.REPLACE_EXISTING);
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

	public static Map<File, String> mapJavaFilesToQualifiedNames(File file) {
		Map<File, String> results = new HashMap<File, String>();
		mapJavaFilesToQualifiedNames(results, file);
		return results;
	}
	private static void mapJavaFilesToQualifiedNames(Map<File, String> map, File current) {
		for(File f : current.listFiles()) {
			if(f.isDirectory()) {
				mapJavaFilesToQualifiedNames(map, f);
			} else {
				String name = extractQualifiedName(f);
				if(name != null) {
					map.put(f, name);
				}
			}
		}
	}
	
	public static String truncate(String str, int maxLength) {
		if(str == null || str.length() <= maxLength) {
			return str;
		}
		return str.substring(0, maxLength - 3) + "...";
	}
	
	public static String dateDiff(Date d1, Date d2) {
		if(d2.before(d1)) {
			Date temp = d1;
			d1 = d2;
			d2 = temp;
		}
		long diff = d2.getTime() - d1.getTime();
		String time = "";
		long dayDiff = TimeUnit.MILLISECONDS.toDays(diff);
		if(dayDiff > 0) {
			time += dayDiff + " day" + (dayDiff == 1 ? "" : "s");
			diff -= TimeUnit.DAYS.toMillis(dayDiff);
		}
		long hoursDiff = TimeUnit.MILLISECONDS.toHours(diff);
		if(hoursDiff > 0) {
			if(!time.isEmpty()) time += ", ";
			time += hoursDiff + " hr" + (hoursDiff == 1 ? "" : "s");
			diff -= TimeUnit.HOURS.toMillis(hoursDiff);
		}
		long minsDiff = TimeUnit.MILLISECONDS.toMinutes(diff);
		if(minsDiff > 0) {
			if(!time.isEmpty()) time += ", ";
			time += minsDiff + " min" + (minsDiff == 1 ? "" : "s");
			diff -= TimeUnit.MINUTES.toMillis(minsDiff);
		}
		return time;
	}
	
	/**
	 * Scan a Java source code file for @TestDescription annotations.
	 * 
	 * If the input file is a directory, recursively scan the directory for 
	 * java source files with @TestDescription annotations.
	 * 
	 * @param sourceCode the file that contains the annotations
	 * @return a map where keys are test method names and values are the value of 
	 * the @TestDescription annotations for those methods
	 */
	public static Map<String, String> extractTestDescriptions(File sourceCode) {
		return extractMethodAnnotationValues("TestDescription", "value", Function.identity(), sourceCode);
	}
	
	/**
	 * Scan a Java source code file for @Test annotations and get timeouts.
	 * 
	 * If the input file is a directory, recursively scan the directory for 
	 * java source files with @Test annotations.
	 * 
	 * @param sourceCode the file that contains the annotations
	 * @return a map where keys are test method names and values are the timeout of 
	 * the @Test annotations for those methods (or <code>null</code> if no timeout is found)
	 */
	public static Map<String, Long> extractTestTimeouts(File sourceCode) {
		return extractMethodAnnotationValues("Test", "timeout", Long::parseLong, sourceCode);
	}
	
	/**
	 * Given a Java source code File, search for any methods with the given
	 * annotation name, and extract the value of the given parameter for each
	 * method.
	 * 
	 * @param annotation
	 *            the annotation name; e.g. for @Test use "Test"
	 * @param valueName
	 *            the parameter name whose values you want to extract
	 * @param converter
	 *            a function to convert the parameter value from String to the
	 *            target type
	 * @param sourceCode
	 *            the source file to extract from. If this is a directory, files
	 *            will be scanned recursively to find annotations.
	 * @return a map where the keys are method names (which were annotated with
	 *         the given annotation) and the values are the corresponding
	 *         parameter value for those annotations, or null if the annotation
	 *         did not have a value for the parameter
	 */
	public static <R> Map<String, R> extractMethodAnnotationValues(String annotation, String valueName, Function<String, R> converter, File sourceCode) {
		Map<String, R> results = new HashMap<>();
		
		if(sourceCode == null || !sourceCode.exists()) {
			return results;
		}
		
		// Scan recursively
		if(sourceCode.isDirectory()) {
			for(File child : sourceCode.listFiles()) {
				results.putAll(extractMethodAnnotationValues(annotation, valueName, converter, child));
			}
			return results;
		} else if(!sourceCode.getName().endsWith(".java")) {
			return results;
		}
		
		String contents = scrapeFile(sourceCode);
		
		Pattern methodRegex = Pattern.compile("[^a-zA-Z0-9_]?([a-zA-Z0-9_]+)\\s*\\(");
		
		int index = 0;
		while(index < contents.length()) {
			int atIndex = contents.indexOf('@', index);
			if(atIndex < 0) {
				break;
			}
			
			int methodBracketIndex = contents.indexOf('(', index);
			R annValue = null;
			boolean hadAnnotation = false;
			while(atIndex >= 0 && methodBracketIndex > atIndex) {
				SourceAnnotation ann = extractAnnotation(contents, atIndex);
				if(ann.name.equals(annotation)) {
					hadAnnotation = true;
					if(ann.bracketContent.get(valueName) != null) {
						annValue = converter.apply(ann.bracketContent.get(valueName));
					}
				}
				index = atIndex + ann.strLength;
				atIndex = contents.indexOf('@', index);
				methodBracketIndex = contents.indexOf('(', index);
			}
			if(methodBracketIndex < 0) {
				break;
			}
			Matcher methodMatcher = methodRegex.matcher(contents.substring(0, methodBracketIndex+1));
			if(methodMatcher.find(index)) {
				String method = methodMatcher.group(1);
				if(hadAnnotation) {
					results.put(method, annValue);
				}
				index = methodMatcher.end();
			} else {
				break;
			}
		}
		return results;
	}
	
	/**
	 * Given a string and a positional index indicating the position of an '@'
	 * character, get details about the following Java annotation
	 */
	private static SourceAnnotation extractAnnotation(String str, int atIndex) {
		SourceAnnotation ann = new SourceAnnotation();
		Pattern idPattern = Pattern.compile("[a-zA-Z0-9_]+");
		int index = atIndex+1;
		Matcher matcher = idPattern.matcher(str);
		if(matcher.find(index)) {
			index = matcher.end();
		} else {
			return ann;
		}
		ann.name = str.substring(atIndex+1, index);
		ann.strLength = index - atIndex;
		int bracketIndex = str.indexOf('(', index);
		if(!str.substring(index, bracketIndex).trim().isEmpty()) {
			return ann;
		}
		char c = 0;
		StringBuilder bracketContent = new StringBuilder();
		index = bracketIndex+1;
		char prev = 0;
		boolean inString = false;
		while(index < str.length()) {
			c = str.charAt(index++);
			if(c == ')' && !inString) {
				break;
			} else if(c == '"' && !inString) {
				inString = true;
			} else if(c == '"' && inString && prev != '\\') {
				inString = false;
			}
			if(c == ',' && !inString) {
				ann.put(bracketContent.toString());
				bracketContent = new StringBuilder();
			} else {
				bracketContent.append(c);
			}
			prev = c;
		}
		if(bracketContent.length() > 0) {
			ann.put(bracketContent.toString());
		}
		ann.strLength = index - atIndex;
		return ann;
	}
}

class SourceAnnotation {
	String name = "";
	Map<String, String> bracketContent = new HashMap<String, String>();
	int strLength = 0;
	void put(String content) {
		int eqIndex = content.indexOf('=');
		int strIndex = content.indexOf('"');
		String key = "value";
		String value = content;
		if(eqIndex >= 0 && (strIndex < 0 || eqIndex < strIndex)) {
			key = content.substring(0, eqIndex).trim();
			value = content.substring(eqIndex + 1);
		}
		value = value.trim();
		if(value.startsWith("\"")) {
			value = value.substring(1);
		}
		if(value.endsWith("\"")) {
			value = value.substring(0, value.length()-1);
		}
		bracketContent.put(key, value);
	}
}