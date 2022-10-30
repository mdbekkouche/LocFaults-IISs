package CFG.DFS;

import CFG.CFGVisitException;


public class DFSException extends CFGVisitException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Specifies whether the exception should end the verification on a 
	 * non-conformity (<code>false</code>) or abort the verification with unknown status 
	 * (specification could not be validated nor invalidated, e.g. program was not 
	 * completely unfolded and no bug was found). 
	 */
	public boolean abort; 
	
	public DFSException(String arg0, boolean abort) {
		super("(in DFS): " + arg0);
		this.abort = abort;
	}

}
