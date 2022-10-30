package CFG;

import ilog.concert.IloException;

/**
 * The root of a CFG.
 * It is the only CFG node with both fathers equal to <code>null</code>.
 * It always exists and has no particular meaning except being the entry point,
 * through its left child, to all the CFG nodes.
 * It is numbered <pre>-1</pre>.
 * 
 * It is useful to have such a node to be able to add or remove the first "actual" CFG node 
 * as any other node.
 *	
 * @author Olivier Ponsini
 *
 */
public class RootNode extends CFGNode {

	public RootNode(String functionName) {
		super(-1,functionName);
	}
	
	public RootNode(RootNode n){
		super(n.nodeIdent,n.key,n.left,n.right,n.leftFather,n.rightFather);
	}

	public Object clone() {
		return new RootNode(this);
	}
	
	@Override
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}

	public CFGNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return this.left.accept(v);
	}
	
	@Override
	public boolean isEmpty() {
		return (this.left == null) || this.left.isEmpty();
	}

}
