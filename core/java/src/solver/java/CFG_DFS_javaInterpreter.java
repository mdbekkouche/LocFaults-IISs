package solver.java;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

import validation.util.Type;
import CFG.CFG;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.RequiresNode;
import CFG.SetOfCFG;
import CFG.DFS.DFSVerifier;
import CFG.simplification.Simplifier;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;
import exception.VariableValueException;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.Variable;
import expression.variables.Variable.Use;
import expression.logical.Assignment;
import validation.system.ValidationSystemCallbacks;
import validation.Validation;
import validation.Validation.VerboseLevel;

/**
 * Class to evaluate a java program and verify its pre and post condition
 * according to input values: depth first visit using Java virtual machine as solver.
 * 
 *  a CFG_DFS_javaInterpreter can be built from a CFG_DFS_javaLauncher
 *  or from main class ValidationLauncher
 * 
 * @author helen
 *
 */
public class CFG_DFS_javaInterpreter implements ValidationSystemCallbacks {
	
	
	/**
	 * All the methods' CFGs of the program to be evaluated.
	 */
	private SetOfCFG program;
	
	
	/** 
	 * to store variable values
	 */
	private JavaVarBlock values;
	
	/**
	 * Expression visitor that evaluates the expression. 
	 */
	private JavaExpressionVisitor exprVisitor;
	
	public CFG_DFS_javaInterpreter() throws IloException{
		long timeBeforeSimpl = System.currentTimeMillis();
		try {
			this.program = new CFGBuilder(Validation.pgmFileName, Validation.pgmMethodName,Validation.maxUnfoldings,Validation.maxArrayLength).convert();
		} catch(Java2CFGException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("\nInitial CFGs\n" +program);
		}

		Simplifier si = new Simplifier();
		si.simplify(program);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("\nCFG after simplification\n" + program);
			}
			System.out.println(ligne());
			System.out.println("Time for CFG construction and simplification");
			double time = (System.currentTimeMillis()-timeBeforeSimpl)/1000.0;
			System.out.println(time +"s");
			System.out.println(ligne());
		}
	}
	
	private static String ligne() {
		return "-----------------------------------------\n";
	}
	
	/** 
	 * To read input variables
	 */
	public void readParameters(CFG c,JavaVarBlock values){
		ArrayList<Variable> param = c.parameters();

//		System.out.println("symb table " + st);
		if (param.size() != 0) {
			if (c.firstNode() instanceof RequiresNode) {
				System.out.println("\nPrecondition of the method is: " 
						+ ((RequiresNode)c.firstNode()).getCondition() + "\n");
			}
			
			for (Variable v : param) {
				Type t = v.type();
				Scanner scan = new Scanner(System.in);
				System.out.println("Value of parameter (" + t + ")" + v.name() + ":");
				String val = scan.next();
				try {
					//We only expect numbers here since booleans are represented by "0" (false)
					//and "1" (true).
					values.setConstantValue(v, (Number)validation.util.Type.getValueFromString(val,t));
				} catch (VariableValueException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
//		System.out.println("value table after " + values);
	}
	

	
	/** print the last value of variables *
	 * 
	 */
	public void printFinalVariableValues(JavaVarBlock values) {
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Final values of useful variables:");
			System.out.println("---------------------------------");
			for (Variable v : values.vars()){
				Number val = values.getValue(v);
				if (val !=null)
					System.out.println(v + ": " + val);
				else 
					System.out.println(v + " has not been evaluated");
			}
		}
	}

	// execute 	the main method of the program
	public void validate() throws IloException {
		CFG method = Validation.pgmMethod(program);
		if (method != null) {
			System.out.println("\n\nStarting execution of method " 
					+ method.name() 
					+ " of program "
					+ Validation.pgmFileName 
					+ " using JVM");
			System.out.println(ligne()+"\n\n");
			long timeBeforeExec = System.currentTimeMillis();
			execute(method);
			System.out.println(ligne());
			System.out.println("Time for symbolic execution");
			double time = (System.currentTimeMillis()-timeBeforeExec)/1000.0;
			System.out.println(time +"s");
			System.out.println(ligne());
		}
		else {
			System.err.println("Error: method " 
					+ Validation.pgmMethodName 
					+ " is not defined in " 
					+ Validation.pgmFileName 
					+ "!");
			}
	}
	
	private String printGlobalVar(TreeSet<Variable> usefulVar) {
		String s = "[";
		for (Variable v : usefulVar) {
			if (v.use()==Use.GLOBAL ) {
				s+=v.name() + " ";
			}
		}
		s+="]";
		s+="Set of global variables: " + s;
		return s;
	}	
	
	private CFGNode getFirstNode(CFG c) {
		CFGNode cFirst = c.firstNode();
		if (program.hasFieldDeclaration()) {
			CFGNode first = program.getFieldDeclaration();
			first.setLeft(cFirst);
			cFirst.setLeftFather(first);
			c.setFirstNode(first);
			return first;
		}	
		return cFirst;
	}
	
	// execute one method
	public void execute(CFG c) throws IloException {
//		System.out.println("Execution of method " + c.name()+"\n");
		TreeSet<Variable> usefulVar = c.getUsefulVar();
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Set of variables: " + usefulVar);
		}
		values = new JavaVarBlock(usefulVar);
		//Validation.println(printGlobalVar(usefulVar),VerboseLevel.VERBOSE);
		readParameters(c, values);
		exprVisitor = new JavaExpressionVisitor(values);
		DFSVerifier cfgVisitor = new DFSVerifier(this);
		
		// add constraints of the static fields declaration if
		// they exist
		// NB : the block can be null if the field declaration node has been simplified
		if (program.hasFieldDeclaration()) {
			for (Assignment a: program.getFieldDeclaration().getBlock()) {
				addConstraint(a);
			}
		}
		// execute the method
		CFGNode first = getFirstNode(c);
		try {
			first.accept(cfgVisitor);
		} catch (CFGVisitException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		printFinalVariableValues(values);
	}
	
	public void addConstraint(LogicalExpression le) {
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Executing " + le);
		}
		le.structAccept(exprVisitor);
//		System.out.println("values " + values);
	}

	public boolean checkPostcond(LogicalExpression le) {
		System.out.println("\n************************");
		System.out.println("Ensures " + le);
		boolean isSatisfied = tryDecision(le);
		if (isSatisfied) {
			System.out.println("Ensures is satisfied!");
			System.out.println("************************\n\n");
		}
		return isSatisfied;
	}

	public boolean tryDecision(LogicalExpression le) {
//		System.out.println(le);
		return ((Integer)le.structAccept(exprVisitor)).intValue()==1;
	}

	public void save() {
		//Does nothing, no backtracking needed...
	}
	
	public void restore() {
		//Does nothing, no backtracking needed...
	}

	public void addVar(Variable v) {
		values.add(v);
	}

	public boolean checkAssertion(LogicalExpression le, String message) {
		System.out.println("\n************************");
		System.out.println("Assert " + message + ": " + le);
		boolean isSatisfied = tryDecision(le);
		if (isSatisfied) {
			System.out.println("Assertion is satisfied!");
			System.out.println("************************\n\n");
		}
		return isSatisfied;
	}

	@Override
	public boolean isFeasible() {
		return tryDecision(new LogicalLiteral(true));
	}

	@Override
	public boolean solve() {
		return true;
	}

}
