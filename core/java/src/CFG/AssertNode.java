package CFG;

import ilog.concert.IloException;




// to represent an assertion inside the code

public class AssertNode extends ConditionalNode{

	// number of this assertion in the program
	// the number is set when the assertion is discovered during DFS 
	int assertNumber;
	
	public AssertNode(int ident, String methodName) {
		super(ident,methodName);
	}
	
	public AssertNode(AssertNode n) {
		super(n);
	}

	public Object clone() {
		return new AssertNode(this);
	}
	
	public String toString() {
		return "Assert node"  + "\n" + super.toString();
	}
	
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		try {
			v.visit(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public AssertNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}
	
//	public void accept(MarkedDFSVisitor v) throws CFGVisitException {
//		if (!v.isMarked(nodeIdent)) {
//			v.mark(nodeIdent);
//			v.visit(this);
//		}
//	}
}
