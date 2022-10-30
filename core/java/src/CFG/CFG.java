package CFG;


import java.util.ArrayList;
import java.util.TreeSet;

import CFG.DFS.ClonerAndRenamerVisitor;

import validation.util.Type;
//import expression.variables.ArraySymbolTable;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

/** to represent a CFG associated to a Java method
 * A CFG is :
 *    - a root that always refers to an empty first node
 *    - a last node
 *    - a set of variables
 *    - a set of useful variables
 *    - a return type
 *    
 */

public class CFG {
	
	// the name of the java method from which the CFG is built
	private String name;
	
	// the name of the class that contains the method associted with this CFG
	private String className;

	/**
	 * Line number of the method declaration.
	 */
	public int startLine;
	
	// the CFG root
	private RootNode root;
	
	// the CFG last node
	private CFGNode last;
	
	// type of the function associated with this CFG
	private Type returnType;

	// the scalar variables
	private SymbolTable variables ;
	
//	// the array variables
//	private ArraySymbolTable arrayVar ;

	// the useful variables
	// contains both scalar and array variables
	// (i.e. renamed variables that remain after CFG simplification)
	private TreeSet<Variable> usefulVar;
	
//	// the useful variables
//	// (i.e. renamed variables that remain after CFG simplification)
//	private TreeSet<ArrayVariable> usefulArrayVar;
	
	// list of formal parameters 
	// this is needed to make the link between formal parameters and
	// effective parameters when executing a function call
	// effective parameters
	// (if one read usefulScalarVar and search for parameters,
	// effective parameters are not in the right order)
	private ArrayList<Variable> formalParameters;
	
	// number of nodes of the CFG
	private int nodeNumber;
	
	// to mark conditional nodes during bottom-up exploration
	// if childAccess = 0, then children have not been explored
	// if childAccess = 1, then the left branch has been explored
	// if childAccess = 2, then both branches have been explored and thus the node father can also 
	// be explored
	private int[] bottomUpAccess;
	
	public CFG(String name) {
		this(name, "");
	}
	
	public CFG(String name, String className) {
		this.name = name;
		this.className = className;
		this.root = new RootNode(name);
		this.last = null;
		this.nodeNumber = 0;
		this.variables = new SymbolTable();
		this.usefulVar = null;
//		this.arrayVar = new ArraySymbolTable();
//		this.usefulArrayVar = null;
		this.returnType = null;
		this.formalParameters = new ArrayList<Variable>();
	}
	
	
	// build a CFG from a CFG of a FunctionCallNode, and a renamer visitor
	// which renames variables of the CFG according to the call number
	// NOTA : make a deep copy of the CFG
	public CFG(CFG c, ClonerAndRenamerVisitor rn) {
		
		this(c.name, c.className);
		
		this.nodeNumber = c.nodeNumber;
		this.usefulVar = new TreeSet<Variable>();
		this.returnType = c.returnType;
		this.formalParameters = c.formalParameters;
		CFGNode newFirst = null;
		try {
			newFirst = c.root.accept(rn);
		} catch (CFGVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setFirstNode(newFirst);
		this.last = rn.last();
		for (Variable v : rn.getUsefulVar()) {
			if (!this.usefulVar.contains(v)) {
				this.usefulVar.add(v);
			}
		}
	}
	
	public String name(){
		return name;
	}
	
	public String className(){
		return name;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public void setType(Type t){
	    returnType = t;
	}
	
	public Type returnType() {
		return returnType;
	}
	
	public void setFirstNode(CFGNode currentNode){
		root.left = currentNode;
		currentNode.leftFather = root;
	}
	
	public void setLast(CFGNode currentNode){
		last = currentNode;
	}
	
	public void setNodeNumber(int n){
		nodeNumber = n;
	}
	
	public void setScalarVar(SymbolTable st){
		variables = st;
	}
	
	public SymbolTable getScalarVar(){
		return variables;
	}
	
//	public void setArrayVar(ArraySymbolTable st){
//		arrayVar = st;
//	}
//	
//	public ArraySymbolTable getArrayVar(){
//		return arrayVar;
//	}
	
	public int getNodeNumber() {
		return nodeNumber;
	}
	
	public CFGNode firstNode() {
		return root.left;
	}
	
	public CFGNode last() {
		return last;
	}
	
	/**
	 * Sets the node that should be explored after this CFG's last 
	 * node (normally an EnsureNode).
	 * It is stored in the last node's left child.
	 * 
	 * This is useful when this CFG corresponds to a method called from 
	 * another method. This then provides a way back to the calling method's CFG.
	 * 
	 * @param node The node to continue with after this CFG.
	 */
	public void setContinueWith(CFGNode node) {
		last.left = node;
	}
	
	public void initBottomUpAccess() {
		bottomUpAccess = new int[nodeNumber];
	}
	
	public void bottomUpMark(int n){
		bottomUpAccess[n]++;
	}
	
	public boolean isBottomUpMarked(int n) {
		return bottomUpAccess[n] == 2;
	}
	
	// getter and setter for useful variables
	/**
	 * Gives the set of variables needed to validate the current property on this CFG.
	 * 
	 * @return The variables contained in the current property cone of influence, 
	 *         if it has been computed; all the CFG variables otherwise.
	 */
	public TreeSet<Variable> getUsefulVar() {
		//usefulScalarVar is only set if the CFG is simplified whereas scalarVar
		//is always set when building the CFG
		if (usefulVar == null) {
			usefulVar = new TreeSet<Variable>();
			for (Variable v: variables.getAllVariables().values())
				usefulVar.add(v);
//			for (Variable v: arrayVar.getAllVariables().values())
//				usefulVar.add(v);
		}
		return usefulVar;
	}
	
	public void setUsefulVar(TreeSet<Variable> tsv) {
		usefulVar = tsv;
	}
	

	/** to get variables which are parameters */
	// TODO : CFG only contains its own local variables 
	//        (and not the variables of the whole program)
	public ArrayList<Variable> parameters() {
		return formalParameters;
	}

	
	public void addFormalParameter(Variable v){
		formalParameters.add(v);
	}
	
	// recursive version
	private String toStringRec(CFGNode node, boolean[] marked) {
		String s = "";
		if (node != null && !marked[node.nodeIdent]) {  
			s += "\n---------------------\n";
			s += node.toString()+"\n";
			s += "---------------------\n";
			marked[node.nodeIdent] = true;
			if (node != this.last) {
				s += toStringRec(node.left, marked);
				s += toStringRec(node.right, marked);
			}
		}
		return s;
	}
	
	
	public String toString() {
		if (nodeNumber == 0) {
			return "There is 0 node in this CFG";
		}
		// initialize marks before printing
		boolean[] marked = new boolean[nodeNumber];
		String etoile = "\n**********************************************\n";
		return etoile + "Method " + name + etoile  + toStringRec(this.firstNode(), marked);
	}
	
}
