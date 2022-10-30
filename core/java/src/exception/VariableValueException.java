package exception;

/**
 * This exception is used when retrieving the value of a variable in the CPBPV set of variables
 * leads to an error on the solver side.
 * This usually occurs when a variable has not been extracted from the model by the solver because
 * it has no influence on the CSP.  
 * 
 * @author ponsini
 *
 */
public class VariableValueException extends Exception {
    /**
     * This is a mandatory field since <code>Exception</code> implements the 
     * {@link java.io.Serializable} interface.
     * 
     * The value is a dummy one, as it is not used in this application.
     */
    static final long serialVersionUID = 0L;

    String message;
    
    public VariableValueException(String s) {
    	super();
    	message = s;
    }

    public String toString() {
    	return super.toString() + message;
    }
}
