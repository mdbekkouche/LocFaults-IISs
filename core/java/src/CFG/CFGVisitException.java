package CFG;

public class CFGVisitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CFGVisitException(String arg0) {
		super("CFG error " + arg0);
	}
}
