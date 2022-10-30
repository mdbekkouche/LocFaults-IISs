package CFG;

import ilog.concert.IloException;

import java.util.ArrayList;

import expression.logical.Assignment;

/** class to represent a CFG node */

public class BlockNode extends CFGNode {

	// node instructions
	private ArrayList<Assignment> block;
	
	// constructors
	
	/** to build an initial node */
	public BlockNode(int ident, String methodName) {
		super(ident, methodName);
		block = new ArrayList<Assignment>();
	}
	
	/** to build a node with a block*/
	public BlockNode(int ident, String methodName, ArrayList<Assignment> block){
		super(ident, methodName);
		this.block = block;
	}
	
	/** copy of a node */
	public BlockNode(BlockNode n) {
		super(n.nodeIdent,n.key,n.left,n.right,n.leftFather,n.rightFather);
		block = (ArrayList<Assignment>)n.block.clone();
	}
	
	public Object clone() {
		return new BlockNode(this);
	}
	
	public void setBlock(ArrayList<Assignment> b) {
		block = b;
	}
	
	public ArrayList<Assignment> getBlock() {
		return block;
	}
	
	public void add(Assignment ass) {
		block.add(ass);
	}
	
	/** textual printing */
		
	// to print  node information
	public  String toString(){
		return "Instruction block: " + block.toString() + "\n" + super.toString(); 
	}
	
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}
	
	public BlockNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}
	
//	public void accept(MarkedDFSVisitor v) throws CFGVisitException {
//		System.out.println("coucou");
//		if (!v.isMarked(nodeIdent)) {
//			v.mark(nodeIdent);
//			v.visit(this);
//		}
//	}

	@Override
	public boolean isEmpty() {
		return block.isEmpty();
	}

}
