/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
