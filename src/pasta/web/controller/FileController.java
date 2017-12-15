package pasta.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.UserPermissionLevel;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.service.GroupManager;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;
import pasta.web.WebUtils;

/**
 * Controller class for file viewing and downloading functions.
 * <p>
 * Handles mappings of $PASTAUrl$/mark/...
 * <p>
 * 
 * @author Martin McGrane
 * @version 1.0
 * @since 23 Sep 2016
 */
@Controller
@RequestMapping("/")
public class FileController {

	public FileController() {
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private GroupManager groupManager;

	/**
	 * $PASTAUrl$/viewFile/loadFile - GET
	 * <p>
	 * View a file. If the user has authenticated and is a tutor, serve up the
	 * document, otherwise do nothing. <b>Not sure if it's actually being used</b>
	 * 
	 * @param fileName the path to the file.
	 * @param response the http response being used to serve the content
	 */
	@RequestMapping(value = "viewFile/loadFile", method = RequestMethod.GET)
	public void getFile(@RequestParam("owner") String owner, 
			@RequestParam("file_name") String fileName, HttpServletResponse response) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		fileName = fileName.replace("\"", "");
		File file = getCorrectFile(owner, fileName);
		logger.debug("Loading content of file: " + fileName + " owned by " + owner);
		if(!PASTAUtil.canDisplayFile(file.getAbsolutePath())) {
			try {
				// get your file as InputStream
				InputStream is = new FileInputStream(file.getAbsolutePath());
				// copy it to response's OutputStream
				IOUtils.copy(is, response.getOutputStream());
				response.flushBuffer();
				is.close();
			} catch (IOException ex) {
				throw new RuntimeException("IOError writing file to output stream", ex);
			}
		}
	}

	/**
	 * $PASTAUrl$/downloadFile
	 * <p>
	 * Download a file If the user has authenticated and is a tutor, serve up the
	 * document, otherwise do nothing.
	 * 
	 * @param fileName the path to the file.
	 * @param response the http response being used to serve the content
	 */
	@RequestMapping(value = "downloadFile", method = RequestMethod.GET)
	public void downloadFile(@RequestParam("owner") String owner, 
			@RequestParam("file_name") String fileName, HttpServletResponse response) {

		fileName = fileName.replace("\"", "");
		File file = getCorrectFile(owner, fileName);

		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			logger.info("Request was made for an invalid file: '" + file.toString() + "'");
			return;
		}
		PASTAUser user = (PASTAUser) RequestContextHolder.currentRequestAttributes().getAttribute("user",
				RequestAttributes.SCOPE_SESSION);
		if (!testFileReadingIsAllowed(user, file)) {
			throw new InsufficientAuthenticationException("You do not have sufficient access to do that");
		}
		logger.debug("User " + user + " is downloading file: " + file);
		try {
			// get your file as InputStream
			InputStream is = new FileInputStream(file);
			// copy it to response's OutputStream
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", "attachment; filename=" + FilenameUtils.getName(fileName));
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
			is.close();
		} catch (IOException ex) {
			logger.info("IOException thrown", ex);
			return;
		}
	}

	/**
	 * $PASTAUrl$/viewFile/ - GET
	 * <p>
	 * View a file. If the user has not authenticated: redirect to login. If the
	 * user is not a tutor: redirect to home. ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>location</td>
	 * <td>The location of the disk of the file</td>
	 * </tr>
	 * <tr>
	 * <td>fileEnding</td>
	 * <td>The file ending of the file you're viewing</td>
	 * </tr>
	 * <tr>
	 * <td>fileContents</td>
	 * <td>The contents of the file, with the &gt; and &lt; escaped</td>
	 * </tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * <li>assessment/mark/viewFile</li>
	 * </ul>
	 * 
	 * @param location the path to the file location
	 * @param model the model being used
	 * @param response the response that's not really being used
	 * @return "redirect:/login/" or "redirect:/home/"
	 */
	@RequestMapping(value = "viewFile/", method = RequestMethod.POST)
	public String viewFile(@ModelAttribute("user") PASTAUser user, @RequestParam("location") String location,
			@RequestParam("owner") String owner, Model model, HttpServletResponse response) {
		
		File file = getCorrectFile(owner, location);
		String fileEnding = FilenameUtils.getExtension(location).toLowerCase();
		// if(fileEnding.equalsIgnoreCase("pdf")) {
		// TODO: figure out a way to redirect to pdfs
		// logger.warn("Redirecting to: redirect:" + location);
		// return "redirect:" + location;
		// }

		model.addAttribute("filename", file.getName());
		model.addAttribute("location", location);
		model.addAttribute("fullLocation", file.getAbsolutePath());
		model.addAttribute("owner", owner);
		model.addAttribute("fileEnding", fileEnding.toLowerCase());

		if (testFileReadingIsAllowed(user, file)) {
			if (PASTAUtil.canDisplayFile(file.getAbsolutePath())) {
				model.addAttribute("fileContents", PASTAUtil.scrapeFile(file.getAbsolutePath()));
				String ext = FilenameUtils.getExtension(file.getName());
				if(Arrays.asList("txt", "csv", "tsv").contains(ext)) {
					ext = "hljs no-highlight"; // "Hack" to stop syntax highlighting on these types while still appearing as a code block
				} else if(ext.equals("m")) {
					ext = "matlab";
				}
				model.addAttribute("fileType", ext);
			}
		} else {
			throw new InsufficientAuthenticationException("You do not have sufficient access to do that");
		}
		return "assessment/mark/viewFile";
	}

	private File getCorrectFile(String owner, String location) {
		File file;
		if (owner.equals("unitTest") || owner.equals("assessment")) {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
			if (owner.equals("unitTest")) {
				file = new File(ProjectProperties.getInstance().getUnitTestsLocation() + location);
			} else  { // if (owner.equals("assessment"))
				file = new File(ProjectProperties.getInstance().getAssessmentValidatorLocation() + location);
			}
		} else {
			file = new File(ProjectProperties.getInstance().getSubmissionsLocation() + location);
		}
		return file;
	}

	/**
	 * Test if the given user has permission to read the file
	 * 
	 * @param user
	 * @param file
	 * @return true if user has permission to read the file
	 */
	private boolean testFileReadingIsAllowed(PASTAUser user, File file) {
		if (user.isTutor()) {
			return true;
		}
		// Allow access to user's own directory and that for any groups they are in.
		Set<File> allowedDirectories = new TreeSet<>();
		allowedDirectories
				.add(new File(ProjectProperties.getInstance().getSubmissionsLocation() + user.getUsername()));
		for (PASTAGroup group : groupManager.getAllUserGroups(user)) {
			allowedDirectories
					.add(new File(ProjectProperties.getInstance().getSubmissionsLocation() + group.getUsername()));
		}
		File parentFile = null;
		try {
			parentFile = file.getCanonicalFile();
		} catch (IOException e) {
			logger.info(user.getUsername() + " attempted to read invalid file <" + file.toString() + ">");
			return false;
		}

		while (parentFile != null) {
			if (allowedDirectories.contains(parentFile)) {
				return true;
			}
			parentFile = parentFile.getParentFile();
		}
		return false;
	}
}
