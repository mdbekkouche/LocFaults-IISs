package CFG.simplification;

import ilog.concert.IloException;

import java.util.TreeSet;

import validation.Validation;
import validation.Validation.VerboseLevel;

import expression.variables.Variable;
import CFG.BlockNode;
import CFG.CFG;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.SetOfCFG;

/** 
 * call a bottom-up visit of the CFG to simplify the CFG :
 *     - remove empty nodes  
 *     - mark as deleted all nodes whose variables do not depend from 
 *       variables of the postcondition
 * 
 * @author helen
 *
 */


public class Simplifier {
	
	
	// TODO :
	// sur FooWithFunction, pb car enlève le noeud bidon_Result_0 = ...
	// le pb vient qu'on réinitialise les usefulvar pour chaque CFG 
	// alors que ça doit être global au programme
	
	// autre problème : le ensuresNode de call est null au lieu de true (vrai aussi 
	// pour AbsMinus)
	
	// list of variables which depend from the postcondition
	private TreeSet<Variable> usefulScalarVar;

	public Simplifier(){
		usefulScalarVar = new TreeSet<Variable>();
	}
	
	static String removedNodeMessage(int n){
		String message="";
		if (n==0)
			message += "No node has";
		else if (n==1)
			message += "One node has";
		else 
			message += n + " nodes have";
		message +=  " been removed";
		return message;
	}
	
	/** main simplification method to simplify a program
	 * @param program
	 * @throws IloException 
	 */
	public void simplify(SetOfCFG program) throws IloException {
		CFG c = program.getMethod(Validation.pgmMethodName);
		if (c==null) {
			System.out.println("method " + Validation.pgmMethodName + " to verify doesn't exist in the program," +
					" please check the parameters of launching" );
			System.exit(0);
		}
		simplify(c, program.getFieldDeclaration());
	}
	
	/** 
	 * simplify the CFG of the main method.
	 * if the main method contains some calls to other methods,
	 * these methods are simplified on the fly (i.e. in simplifierVisitor(FunctionCallNode))
	 * 
	 * the field declaration node is simplified in the requires node :
	 * it must be simplified in the last
	 * 
	 *
	 * @param c : the main method of the program
	 * @param fieldDeclarations : field declaration of the program
	 * @throws IloException 
	 */
	private void simplify(CFG c, BlockNode fieldDeclarations) throws IloException {
		if (VerboseLevel.TERSE.mayPrint()) {
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("-------------------------------");
				System.out.println("\nSimplification of the CFGs of the method "
									+ c.name() + "...");
				System.out.println("Initial CFG has " + c.getNodeNumber() + " nodes");
			}
		}
		CFGNode l = c.last();
		SimplifierVisitor sv = new SimplifierVisitor(c, usefulScalarVar);
		try {			
			l.accept(sv);
			// simplify the field declaration if it is not empty
			// the field declaration node must be the last node to
			// be simplified
			if (!fieldDeclarations.isEmpty()) {
				sv.visitAssignments(fieldDeclarations.getBlock());
			}			
			//TODO: next statement should be uncommented to update the number of node of the 
			//      simplified CFG. However, it can't be done right now because DPVSInformationVisitor
			//      reads this field to know the greatest node number of this CFG!
			//c.setNodeNumber(c.getNodeNumber() - sv.removedNodes);
			c.setUsefulVar(sv.usefulVar);
		} catch (CFGVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Simplified CFG has " + c.getNodeNumber() + " nodes");
			System.out.println(removedNodeMessage(sv.removedNodes));
			if (sv.removedAssign != 0)
				System.out.println(sv.removedAssign + " assignments have been removed");
			//System.out.println("useful vars " + sv.usefulScalarVar);
			System.out.println("End of simplification of method "+ c.name());
			System.out.println("-------------------------------");
		}
	}
}
