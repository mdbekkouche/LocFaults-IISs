package CFG.DPVS;

import CFG.CFGVisitException;


public class DPVSException extends CFGVisitException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DPVSException(String arg0) {
		super("during DPVS visit : " + arg0);
	}


}
