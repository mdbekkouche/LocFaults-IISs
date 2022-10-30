package CFG;

import ilog.concert.IloException;




// to represent the precondition node

public class RequiresNode extends ConditionalNode {

	public RequiresNode(int ident,String functionName) {
		super(ident,functionName);
	}
	
	public RequiresNode(RequiresNode n){
		super(n);
	}
	
	public Object clone() {
		return new RequiresNode(this);
	}

	public String toString() {
		return "Requires node"  + "\n" + super.toString();
	}
	
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}
	
	public RequiresNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}
//	public void accept(MarkedDFSVisitor v) throws CFGVisitException {
//		if (!v.isMarked(nodeIdent)) {
//			v.mark(nodeIdent);
//			v.visit(this);
//		}
//	}

}
