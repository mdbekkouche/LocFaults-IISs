package validation.system.cfg;

import ilog.concert.IloException;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;

import CFG.CFG;
import CFG.SetOfCFG;
import CFG.simplification.ConstantPropagator;
import CFG.simplification.Simplifier;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.ValidationStatus;
import validation.system.ValidationSystemCallbacks;
import expression.logical.LogicalExpression;
import expression.variables.Variable;

/**
 * This is a validation system that relies on a single solver and is not tied to previous 
 * XML validation systems. 
 * 
 * {@link #csp} attribute must be set in derived classes.
 * 
 * @author Olivier Ponsini
 *
 */
public abstract class CfgSingleCspValidation implements ValidationSystemCallbacks {
	
	/**
	 * All the methods' CFGs of the program to be verified.
	 */
	protected SetOfCFG program;
	/**
	 * The method to prove.
	 */
	protected CFG method;
	/**
	 * The CSP.
	 * 
	 */
	protected CfgCsp csp; 
	/**
	 * Number of the currently explored path.
	 */ 
	protected int pathNumber;
	/**
	 * Status of the validation.
	 */ 
	protected ValidationStatus status;
	
	public  CfgSingleCspValidation() {	
		pathNumber = 1;
		status = new ValidationStatus();
		csp = createCSP();
		status.addStatus(csp.getStatus());
	}	
	
	protected abstract CfgCsp createCSP();
	
	protected boolean buildCFG() {
		status.cfgBuildingTime = System.currentTimeMillis();
		try {
			this.program = new CFGBuilder(Validation.pgmFileName, Validation.pgmMethodName, 
					                      Validation.maxUnfoldings,Validation.maxArrayLength).convert();
		} catch(Java2CFGException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		status.cfgBuildingTime = System.currentTimeMillis() - status.cfgBuildingTime;

		if (this.program != null) {
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("\nInitial CFGs\n" + program);
			}

			method = Validation.pgmMethod(program);
			if (method != null)
				return true;
		}
		
		return false;
	}

	protected void simplifyCFG() throws IloException {
		status.cfgSimplificationTime = System.currentTimeMillis();

		ConstantPropagator cp = new ConstantPropagator();
		cp.propagate(program);

		//program.dumpToCode();
		
		long cstPropagTime = 0;
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("\nTime for CFG construction: " + status.cfgBuildingTime + " ms\n");
			cstPropagTime = System.currentTimeMillis() - status.cfgSimplificationTime;
			System.out.println("\nTime for constant propagation: " + cstPropagTime + " ms\n");
			System.out.println("Nb nodes = " + cp.nbNodes);
			System.out.println("Nb assignments = " + cp.nbAssignments + "\n");
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println(program);
			}
   		}
		
		Simplifier si = new Simplifier();
		si.simplify(program);

		//program.dumpToCode();

		status.cfgSimplificationTime = System.currentTimeMillis() - status.cfgSimplificationTime;
		
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("\nTime for cone-of-influence: " + (status.cfgSimplificationTime-cstPropagTime) + " ms\n");
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("\nCFG after simplification\n" + program);
			}
		}
	}
	
	public void addPath(){
		pathNumber++;
	}

	public int pathNumber(){
		return pathNumber;
	}
	
	public String printStatus() {
		return status.toString();
	}
	
	@Override
	public void addVar(Variable v) {
		csp.addVar(v);
	}

	@Override
	public void addConstraint(LogicalExpression c) {
		csp.addConstraint(c);
	}

	@Override
	public void save() {
		csp.save();
	}

	@Override
	public void restore() {
		csp.restore();
	}
		
	public String toString() {
		return csp.vars.toString();
	}
}
