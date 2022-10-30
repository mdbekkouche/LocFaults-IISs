package solver.glpk;

import expression.variables.ConcreteVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;
import solver.Solver.SolverEnum;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.xml.ValidationCSP;
import validation.system.xml.ValidationSystem;

/**
 * This class uses GLPK to solve CSPs over rationals.
 * 
 * @author Olivier Ponsini
 *
 */
public class GlpkValidationCSP extends ValidationCSP {
	
	/**
	 * Constructs a concrete CSP over rationals handled by GLPK.
	 */ 
	public GlpkValidationCSP(String name, ValidationSystem vs) {
		super(name, SolverEnum.GLPK, vs);
	}

	/**
	 * Creates:
	 *   - the store of constraints that will be handled by the concrete solver;
	 *   - the store of variables that will be handled by the concrete solver.
	 * 
	 * This method is called by a mother class constructor.
	 *  
	 */	
	@Override
	protected void initCSP() {
		constr = new GlpkConstraintBlock((GlpkSolver)cspSolver);
		varBlock = new GlpkVarBlock((GlpkSolver)cspSolver);
		arrayVarBlock = null;
		visitor = new GlpkExpressionVisitor(this);
	}	
	
	/**
	 * This creates the concrete LP problem with GLPK. 
	 */
	@Override
	public void startSearch() {
		cspSolver.startSearch();
	}
	
	/** to stop the search */
	@Override
	public void stopSearch(){
		cspSolver.stopSearch();
	}

	/**
	 * Sets new bounds to the variable domains.
	 * The parameter may contain domains for only some of the variables 
	 * (not necessarily all the variables handled by this solver), or even for 
	 * variables that are not handled by this solver (such domains are then ignored).
	 * In particular, if domains is empty, no change are applied to the current variable
	 * domains.
	 *   
	 * @param domains
	 */
	public void setDomains(DomainBox domains) {
		if (domains != null) {
			String varName;
			int col;
			VariableDomain domain;
			for (ConcreteVariable<Integer> v: (GlpkVarBlock)varBlock) {
				varName = v.name();
				col = v.concreteVar();
				domain = domains.get(varName);
				if (domain != null) {
					((GlpkSolver)cspSolver).setVarBounds(
							col, 
							!Double.isInfinite(domain.minValue().doubleValue()),
							domain.minValue().doubleValue(),	
							!Double.isInfinite(domain.maxValue().doubleValue()),
							domain.maxValue().doubleValue());
				}
			}
		}
	}
	
	@Override
	public Solution solution() {
		Solution sol = new Solution(this.getElapsedTime());
		Number val;
		Variable var;
		
		for (ConcreteVariable<Integer> v: (GlpkVarBlock)varBlock) {
			try {
				val = ((GlpkSolver)cspSolver).value(v.concreteVar());
				var = v.clone();
				var.setConstant(val);
				sol.add(var);
			} catch (Exception e) {
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println(e.toString());
				}
			}
		}
		return sol;
	}

	@Override
	public DomainBox domainBox() {
		DomainBox box = new DomainBox();
		Number val;
		
		for (ConcreteVariable<Integer> v: (GlpkVarBlock)varBlock) {
			try {
				val = ((GlpkSolver)cspSolver).value(v.concreteVar());
				box.add(new VariableDomain(v, val, val));
			} catch (Exception e) {
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println(e.toString());
				}
			}
		}
		return box;
	}

}
