import java.io.File;
public interface PlayerValidator {
	/**
	 * Validate a given player.
	 *
	 * @param username
	 *		the username of the user submitting the player
	 * @param playerDir
	 * 		the base directory of the user's submission
	 *
	 * @return an array of strings. Index 0 is the proposed 
	 * player's name. This should be extracted from the 
	 * submission somehow (e.g. the filename of the player).
	 * Index 1 is the reason why this player is invalid 
	 * (will be displayed to the user), or null if the player
	 * is valid.
	 */
	public String[] validate(String username, File playerDir);
}