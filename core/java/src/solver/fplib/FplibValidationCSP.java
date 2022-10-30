package solver.fplib;

import java.util.ArrayList;

import expression.logical.LogicalExpression;
import expression.variables.DomainBox;
import expression.variables.ConcreteVariable;
import expression.variables.Variable;
import expression.variables.VariableDomain;
import solver.Solver.SolverEnum;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.ExpressionCtrStore;
import validation.system.xml.ValidationCSP;
import validation.system.xml.ValidationSystem;
import validation.util.Type;

/**
 * This class uses fplib to solve CSPs over floats.
 * 
 * @author Olivier Ponsini
 *
 */
public class FplibValidationCSP extends ValidationCSP {
	/**
	 * Constructs a concrete CSP on floats handled by fplib.
	 */ 
	public FplibValidationCSP(String name, ValidationSystem vs) {
		super(name, SolverEnum.FPLIB, vs);
	}

	/**
	 * Keeps a pointer to the current Fplib solver's csp.
	 * It is "null" (<code>0L</code>) when there 's no current csp.
	 */
	private long nativeCSP;
	
	/**
	 * Creates:
	 *   - the store of constraints that will be handled by the concrete solver;
	 *   - the store of variables that will be handled by the concrete solver.
	 * 
	 * This method is called by a mother class constructor.
	 */	
	@Override
	protected void initCSP() {
		constr = new ExpressionCtrStore();
		varBlock = new FplibVarBlock();
		arrayVarBlock = null;
		visitor = new FplibExpressionVisitor(this);
		nativeCSP = 0L;
	}

	/**
	 * Adds a variable to this CSP.
	 * 
	 * The variable is added to the associated variable block, but also to the 
	 * native Fplib csp if it has been created.
	 */
	@Override
	public void addVar(Variable v) {
		if (nativeCSP != 0L) { 
			//An Fplib csp is already in use, add this new variable to it
			((FplibVarBlock)varBlock).add(
					new ConcreteVariable<Long>(
							v, 
							((FplibSolver)cspSolver).addVar(nativeCSP, v)));
		}
		else {
			varBlock.add(v);
		}
	}

	/** 
	 * Creates the concrete CSP and enables the search process.
	 */
	@Override
	public void startSearch() {
		//Solver initialization
		nativeCSP = ((FplibSolver)cspSolver).init();
		addConcreteVars(nativeCSP, (FplibVarBlock)varBlock);

		//Creates the concrete model with all the previously added variables
		long model = FplibSolver.fplibCreateModel(nativeCSP);
		addConcreteCtrs(model, (ExpressionCtrStore)constr);

		//extracts the model and creates the solver
		((FplibSolver)cspSolver).createSolver(model);
		cspSolver.startSearch();
	}

	/**
	 * Creates new concrete variables for all variables in the given variable block.
	 * If a variable was already linked to a concrete one, this link is lost.
	 * 
	 * @param concreteCSP
	 * @param vars
	 */
	protected void addConcreteVars(long concreteCSP, FplibVarBlock vars) {
		//Adds variables to the solver's environment.
		for (ConcreteVariable<Long> v: vars) {
			v.setConcreteVar(((FplibSolver)cspSolver).addVar(concreteCSP, v));
		}
	}

	/**
	 * Adds all the given constraints to the given Fplib model.
	 * Problem variables should have already been created with 
	 * {@link #fplibAddBoundedVar(String, int, boolean, double, boolean, double)} or 
	 * {@link #fplibAddVar(String, int)}. 
	 *  
	 * @param constraintSet The set of constraints to add to this model.
	 */
	public void addConcreteCtrs(long model, Iterable<LogicalExpression> constraintSet) {		
		for (LogicalExpression c: constraintSet) {
			//Adds the constraints to the fplib model.
			//Visiting a constraint may return a single concrete constraint or 
			//an array of concrete constraints
			Object ctrs = c.structAccept(visitor);
			if (ctrs instanceof Long) {
				//It is a single concrete constraint
				FplibSolver.fplibAddCtr(model, (Long)ctrs);
			}
			else {
				//It is an ArrayList of concrete constraints
				for (Long ctr: (ArrayList<Long>)ctrs) {					
					FplibSolver.fplibAddCtr(model, ctr);
				}
			}
		}		
	}
	
	/** 
	 * Frees all the current Fplib solver's resources.
	 */
	@Override
	public void stopSearch(){
		cspSolver.stopSearch();
		((FplibSolver)cspSolver).release();
		nativeCSP = 0L;
	}
	
	/**
	 * Sets new domains for the concrete variables currently handled by Fplib.
	 * This modifies the concrete variables domains.
	 * 
	 * Be careful that all the given domains must be sub-domains 
	 * of the current ones (for each variable, the new domain must 
	 * be included in the current variable domain). Failing to comply 
	 * with this rule will lead to erroneous solver behaviors.
	 * 
	 * If it is needed to set a variable domain D that is not included in 
	 * the current one, one may restore a previously saved valid set of 
	 * domains in which D is a sub-domain of (is included in) the domain of 
	 * the corresponding variable.  
	 * 
	 * The parameter may contain domains for only some of the variables 
	 * (not necessarily all the variables handled by this solver), or even for 
	 * variables that are not handled by this solver (such domains are then ignored).
	 * In particular, if domains is empty, no change are applied to the current variable
	 * domains.
	 * 
	 * @param domains
	 */
	public void setFplibDomains(DomainBox domains) {
		if (domains != null) {
			String varName;
			long var;
			VariableDomain domain;
			FplibVarBlock vars = (FplibVarBlock)varBlock;
			for (ConcreteVariable<Long> v: vars) {
				varName = v.name();
				var = v.concreteVar();
				domain = domains.get(varName);
				if (domain != null) {
					FplibSolver.fplibSetVarBounds(
							var, 
							domain.minValue().doubleValue(),	
							domain.maxValue().doubleValue());
				}
			}
		}
	}

	/**
	 * Sets new domain for a given concrete variable currently handled by Fplib.
	 * This modifies the concrete variable domain.
	 * 
	 * Be careful that the given domain must be sub-domain 
	 * of the current one (the new domain must 
	 * be included in the current variable domain). Failing to comply 
	 * with this rule will lead to erroneous solver behaviors.
	 * 
	 * If it is needed to set a variable domain D that is not included in 
	 * the current one, one may restore a previously saved valid set of 
	 * domains in which D is a sub-domain of (is included in) the domain of 
	 * the corresponding variable.  
	 * 
	 * @param domains
	 */
	public void setFplibDomain(VariableDomain domain) {
		if (domain != null) {
			long fplibVar = ((FplibVarBlock)varBlock).get(domain.name()).concreteVar();
			FplibSolver.fplibSetVarBounds(
					fplibVar, 
					domain.minValue().doubleValue(),	
					domain.maxValue().doubleValue());
			
		}
	}

	
	/**
	 * Set domains of this CSP to the current value of the domains in Fplib concrete solver.
	 */
	public void setDomainsToCurrentSolution() {
		long fplibVar;
		Type type;
		Number inf, sup;
		
		for (ConcreteVariable<Long> v: (FplibVarBlock)varBlock) {
			try {
				fplibVar = v.concreteVar();
				type = v.type();
				inf = FplibSolver.infValue(fplibVar, type);
				sup = FplibSolver.supValue(fplibVar, type);
				v.setDomain(inf, sup);
			} catch (Exception e) {
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println(e.toString());			
				}
			}
		}				
	}
	
	
	/**
	 * Updates domains in the current CSP to the given domains and reset the other domains.
	 * This does not modify any concrete variable domain.
	 * 
	 * A CSP domain present in <pre>domains</pre> is updated to the range in <pre>domains</pre>.
	 * A CSP domain not present in <pre>domains</pre> is reset to its type full domain.
	 * 
	 * @param domain The new domains for the domains' associated variables.
	 */
	public void resetDomains(DomainBox domains) {
		VariableDomain newDomain;
		for (Variable v: (FplibVarBlock)varBlock) {
			newDomain = domains.get(v.name());
			if (newDomain != null) {
				v.setDomain(newDomain.minValue(), newDomain.maxValue());
			}
			else {
				v.resetDomain();
			}
		}		
	}

	/**
	 * Sets new domains for the given variables in the current CSP.
	 * This does not modify any concrete variable domain.
	 * 
	 * Only domains' variable existing in the current CSP will be updated.
	 * 
	 * @param domain The new domains for the domains' associated variables.
	 */
	public void setDomains(DomainBox domains) {
		for (VariableDomain d: domains)
			setDomain(d);
	}

	/**
	 * Sets new domain bounds for the given variable in the current CSP.
	 * This does not modify the concrete variable domains.
	 * 
	 * The given domain's variable must exist in the current CSP for its domain to be
	 * updated; otherwise no domain is modified nor created.
	 * 
	 * @param domain The new domain for the domain's associated variable.
	 */
	public void setDomain(VariableDomain newDomain) {
		if (newDomain != null) {
			FplibVarBlock vars = (FplibVarBlock)varBlock;
			Variable v = vars.get(newDomain.name());
			if (v != null) {
				v.setDomain(newDomain.minValue(), newDomain.maxValue());				
			}
			else {
				System.err.println("Warning (setDomain): variable " 
						+ newDomain.variable() 
						+ " does not exist in the current CSP!");
			}
		}
	}
	
	@Override
	public  Solution solution() {
		Solution sol = new Solution(this.getElapsedTime());
		long fplibVar;
		Number inf, sup;
		Variable var;
		for (ConcreteVariable<Long> v: (FplibVarBlock)varBlock) {
			try {
				fplibVar = v.concreteVar();
				inf = FplibSolver.infValue(fplibVar, v.type());
				sup = FplibSolver.supValue(fplibVar, v.type());
				var = v.clone();
				var.setDomain(inf, sup);
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
	public  DomainBox domainBox() {
		DomainBox box = new DomainBox();
		long fplibVar;
		Type type;
		Number inf, sup;
		
		for (ConcreteVariable<Long> v: (FplibVarBlock)varBlock) {
			try {
				fplibVar = v.concreteVar();
				type = v.type();
				inf = FplibSolver.infValue(fplibVar, type);
				sup = FplibSolver.supValue(fplibVar, type);
				box.add(new VariableDomain(v, inf, sup));
			} catch (Exception e) {
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println(e.toString());			
				}
			}
		}
		return box;
		
	}

}
