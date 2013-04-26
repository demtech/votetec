import control.Controller;

public class Evoting {

	/* Main class. Start a controller and pass the location of 
	 * the configuration file.  */
	public static void main(String[] args) {
		String curDir = System.getProperty("user.dir") + "/";
		new Controller(curDir + "Evoting.conf");
	}
}