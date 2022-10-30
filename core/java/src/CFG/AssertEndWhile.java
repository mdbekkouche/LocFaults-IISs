package CFG;

import ilog.concert.IloException;


/**
 * represent nodes added after WhileNodes to make unwinding assertions :
 * if the loop condition is true but there is no more branch because the 
 * loop has not been enough unwound 
 * 
 * @author helen
 *
 */
public class AssertEndWhile extends AssertNode {
	
	public AssertEndWhile(int ident, String methodName) {
		super(ident, methodName);
	}
	
	public AssertEndWhile(AssertEndWhile n) {
		super(n);
	}
	
	public String toString() {
		return "LOOP unwinding assertion"  + "\n" + super.toString();
	}
	
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}
	

}
