package exception;

// les exceptions lors de la création de contraintes linaires
public class CplexException extends Exception {
    /**
     * This is a mandatory field since <code>Exception</code> implements the 
     * {@link java.io.Serializable} interface.
     * 
     * The value is a dummy one, as it is not used in this application.
     */
    static final long serialVersionUID = 0L;

    String message;
    
    public CplexException(String s) {
	super();
	message = s;
    }

    public String toString() {
	return super.toString() + "CPLEX CONSTRUCTION ERROR : " + message;
    }
}
