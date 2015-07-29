package pasta.service.validation;

import java.io.File;
import java.util.Collection;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 29 Jul 2015
 */
public interface PASTASubmissionValidator {
	/**
	 * Gets any feedback on a student's submission.
	 * 
	 * Any feedback written will be displayed to the user (designed to be
	 * helpful information), and the submission will still be run/tested if
	 * applicable.
	 * 
	 * @param username
	 *            the username of the student
	 * @param submissionBase
	 *            the base directory of the student's submission
	 * @return a collection of all the feedback items for the submission; null
	 *         or empty list if none are to be given
	 */
	public Collection<ValidationFeedback> getFeedback(String username, File submissionBase);
	
	/**
	 * Gets any problems or errors with student's submission.
	 * 
	 * Any feedback written will be displayed to the user, and their submission
	 * will not be run/tested if applicable.
	 * 
	 * @param username
	 *            the username of the student
	 * @param submissionBase
	 *            the base directory of the student's submission
	 * @return a List of all the validation problems with the submission; null
	 *         or empty list if there are none
	 */
	public Collection<ValidationFeedback> getErrors(String username, File submissionBase);
}
