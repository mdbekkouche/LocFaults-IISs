package CFG;

import ilog.concert.IloException;

import java.util.Collection;
import java.util.LinkedHashMap;

import expression.logical.Assignment;

/** class to store all the CFG of a program
 * each method has its proper CFG
 * 
 * @author helen
 *
 */

public class SetOfCFG {
	
	// maps method name to its CFG
	private LinkedHashMap<String, CFG> methods;
	
	// the name of the program
	private String name;
	
	// field declaration : must be executed before each method
	// to set values of global variables
	// NB: each declared field is supposed to be static
	private BlockNode fieldDeclarationNode;
	
	public SetOfCFG(String n) {
		methods = new LinkedHashMap<String, CFG>();
		name = n;
		// fields are declared in the main method
		fieldDeclarationNode = new BlockNode(0,"");
	}
	
	public SetOfCFG() {
		this("");
	}
	
	/** to set the name
	 * 
	 * @param n : name of the class represented by this CFG
	 */
	public void setName(String n) {
		name = n;
	}
	
	/** number of nodes of the whole CFG */
	public int size() {
		int s=0;
		for (CFG m :methods.values()) {
			s+= m.getNodeNumber();
		}
		return s;
	}
	///////////////////////////////////////////
	// to deal with methods of the setOfCFG  //
	///////////////////////////////////////////
	public void addMethod(String n, CFG m) {
		methods.put(n,m);
	}

	public CFG getMethod(String n){
		return methods.get(n);
	}

	public boolean isEmpty() {
		return methods.isEmpty();
	}
	
	/**
	 * @return The first method in the collection of {@link #methods}.
	 */
	public CFG getFirstMethod(){
		return methods.values().iterator().next();
		
	}
	
	/** to know if method name exists in this program*/
	public boolean hasMethod(String name) {
		return methods.containsKey(name);
	}
	
	/** return an enumerator on all the cfgs*/
	public Collection<CFG> methods() {
		return methods.values();
	}
	
	///////////////////////////////////////////
    // to deal with field declaration 
	///////////////////////////////////////////
	public void addFieldAssignment(Assignment a){
		fieldDeclarationNode.getBlock().add(a);
	}
	
	public boolean hasFieldDeclaration() {
		return !fieldDeclarationNode.getBlock().isEmpty();
	}
	
	public BlockNode getFieldDeclaration() {
		return fieldDeclarationNode;
	}
	
	///////////////////////////////////////////
    // printing methods
	///////////////////////////////////////////

	public String name() {
		return name;
	}
	
	private String etoiles() {
		return "\n*********************************************\n";
	}
	
	public String toString() {
		String s = etoiles() + "Class " + name + etoiles();
		// field declaration node
		if (hasFieldDeclaration()){
			s+="\nField declaration \n";
			s+="---------------------\n";
			s+=fieldDeclarationNode + "\n";
			s+="---------------------\n";
		}
		
		// methods
		for (CFG c : methods()){
			s += "\n" +  c + "\n";
		}
		return s;
	}
	
	/**
	 * Calculate the number of instructions in the CFG
	 */
	public int getNbrInstructions() {
		int nbrInsts = 0;
		for (CFG c : methods()){
			nbrInsts += c.getNbrInstructions();
		}
		return nbrInsts;
	}
	
	
	/**
	 * Display this SetOfCFG as source code.
	 * @throws IloException 
	 */
	public void dumpToCode() throws IloException {
		CodeDumper d = new CodeDumper();
		System.out.println("---------- code dumping ----------------");
		d.dump(this);
		System.out.println("---------- End of code dumping ----------------");
	}

}
