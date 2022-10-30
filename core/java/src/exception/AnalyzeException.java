package exception;

// les exceptions lors de l'analyse du bench

public class AnalyzeException extends Exception {
    /**
     * This is a mandatory field since <code>Exception</code> implements the 
     * {@link java.io.Serializable} interface.
     * 
     * The value is a dummy one, as it is not used in this application.
     */
    static final long serialVersionUID = 0L;

    String message;
    
    public AnalyzeException(String s) {
	super();
	message = s;
    }

    public String toString() {
	return super.toString() + "XML PARSING ERROR : " + message;
    }
}
