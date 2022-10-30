package CFG;

import ilog.concert.IloException;


/**
 *  to represent the postcondition node
 * @author helen
 *
 */

public class EnsuresNode extends ConditionalNode {

	private boolean isMainEnsure;
	
	public EnsuresNode(int ident,String methodName) {
		super(ident,methodName);
		isMainEnsure = false;
	}
	
	public EnsuresNode(EnsuresNode n) {
		super(n);
		isMainEnsure = n.isMainEnsure;
	}

	public EnsuresNode(int ident, String methodName, boolean isMain) {
		super(ident, methodName);
		isMainEnsure = isMain;
	}
	
	public Object clone() {
		return new EnsuresNode(this);
	}
	
	public boolean isMainEnsure() {
		return isMainEnsure;
	}

	public String toString() {
		return "Ensures node"  + "\n" + super.toString();
	}
	
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}
	
	public EnsuresNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}
	
}
