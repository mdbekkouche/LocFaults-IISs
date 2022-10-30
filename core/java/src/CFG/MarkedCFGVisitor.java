package CFG;

import ilog.concert.IloException;


public abstract class MarkedCFGVisitor implements CFGVisitor {
	
	// mark if the current node has already been renamed
	// useful to do not rename many time the same node after a conditional node
	private boolean[] marked;

	public MarkedCFGVisitor(int nodeNumber) {
		marked = new boolean[nodeNumber];
	}
	
	public void mark(int n){
		marked[n]=true;
	}
	
	public boolean isMarked(int n) {
		return marked[n];
	}

	public void visit(CFGNode n) throws CFGVisitException, IloException {
		int nodeIdent = n.getIdent();
		if (!isMarked(nodeIdent)) {
			mark(nodeIdent);
			n.accept(this);
		}
	}
}
