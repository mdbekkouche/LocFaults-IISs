package java2CFGTranslator;

/**
 * class to encapsulate identifiers of a CFG
 * 
 * @author helen
 *
 */
public class CFGNodeIdent {
	
	// node identifier
	int node;
	
	// if node identifier
	int iff;

	// while node identifier
	int whilee;

	public CFGNodeIdent() {
		node=1;
		iff=0;
		whilee=0;
	}
	
	public CFGNodeIdent(CFGNodeIdent id) {
		node=id.node;
		iff=id.iff;
		whilee=id.whilee;
	}
	
	public CFGNodeIdent(int n, int i, int w) {
		node=n;
		iff=i;
		whilee=w;
	}
	
	public String toString() {
		return "node " + node + " if " + iff + " while " + whilee; 
	}
}
