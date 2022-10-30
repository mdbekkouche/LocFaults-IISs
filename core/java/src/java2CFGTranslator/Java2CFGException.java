package java2CFGTranslator;

/**
 * @author Hélène Collavizza
 * from an original student work by Eric Le Duff and Sébastien Derrien
 * Polytech'Nice Sophia Antipolis
 *
 */
public class Java2CFGException extends Exception {

	private static final long serialVersionUID = 1L;

	public Java2CFGException(String msg) {
		super(msg);
	}

	public String getError() {
	    return ("Error : " + getMessage() + "\n" + "CFG building cancelled");
	}
}
