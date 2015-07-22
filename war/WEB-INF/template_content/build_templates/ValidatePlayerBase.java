import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ValidatePlayerBase {
	public static void main(String[] args) {
		try {
			String username = args[0];
			File playerDir = new File(args[1]);
			PlayerValidator val = (PlayerValidator) Class.forName(args[2]).newInstance();
			String[] output = val.validate(username, playerDir);
			if(output != null) {
				if(output[0] == null) {
					throw new RuntimeException("Validator " + val.getClass().getSimpleName() + " did not provide player name.");
				}
				PrintWriter out = new PrintWriter(new File(args[3]));
				out.println(output[0]);
				out.close();
				if(output[1] != null) {
					out = new PrintWriter(new File(args[4]));
					out.println(output[1]);
					out.close();
				}
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}