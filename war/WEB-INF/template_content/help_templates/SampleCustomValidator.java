// Necessary imports
import pasta.service.validation.ValidationFeedback;
import pasta.service.validation.PASTASubmissionValidator;
import java.util.Collection;
import java.io.File;

// Imports for this example
import java.util.LinkedList;

public class MyValidator implements PASTASubmissionValidator {
	// This feedback is given to all users, and does not prevent the submission from being accepted
	public Collection<ValidationFeedback> getFeedback(String username, File submissionBase) {
		Collection<ValidationFeedback> feedback = new LinkedList<>();
		list.add(new ValidationFeedback("Your username is " + username));
		if(new File(submissionBase, "extra_work.txt").exists()) {
			list.add(new ValidationFeedback("Good Work!",
					"It looks like you've done the extra work."));
		}
		return feedback;
	}
	
	// If this feedback is not null or empty, it will prevent the user's submission from being accepted.
	public Collection<ValidationFeedback> getErrors(String username, File submissionBase) {
		Collection<ValidationFeedback> errors = new LinkedList<>();
		if(!new File(submissionBase, "expected_work.txt").exists()) {
			list.add(new ValidationFeedback("Files Expected",
					"You haven't included expected_work.txt in your submission."));
		}
		return list;
	}
}