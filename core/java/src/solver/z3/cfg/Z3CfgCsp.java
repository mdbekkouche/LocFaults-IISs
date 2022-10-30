package solver.z3.cfg;

import exception.VariableValueException;
import expression.variables.ConcreteArrayVariable;
import expression.variables.ConcreteVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

import solver.Solver;
import solver.Solver.SolverEnum;
import solver.z3.Z3ArrayVarBlock;
import solver.z3.Z3ConstraintBlock;
import solver.z3.Z3ExpressionVisitor;
import solver.z3.Z3Solver;
import solver.z3.Z3VarBlock;

import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.cfg.CfgCsp;

/**
 * This class uses z3 to solve linear CSPs over reals (rationals).
 * 
 * @author Olivier Ponsini
 *
 */
public class Z3CfgCsp extends CfgCsp<Z3Solver,
									 Z3VarBlock,
									 Z3ArrayVarBlock,
									 Z3ConstraintBlock,
									 Z3ExpressionVisitor> 
{
				
	/**
	 * Constructs a concrete linear CSP on reals handled by z3.
	 */ 
	public Z3CfgCsp(String name) {
		super(name);
		solver = (Z3Solver)Solver.createSolver(SolverEnum.Z3);
		ctrs = new Z3ConstraintBlock(solver);
		vars = new Z3VarBlock(solver);
		arrayVars = new Z3ArrayVarBlock(solver);
		visitor = new Z3ExpressionVisitor(solver, vars, arrayVars);
	}

	// methods to manage backtrak
	
	/** Saves current CSP. */
	public void save() {
		ctrs.save();
		vars.save();
		arrayVars.save();
	
	}
	
	/** Restores current CSP. */
	public void restore() {
		ctrs.restore();
		vars.restore();
		arrayVars.restore();
	}
	
	public Solution solution() {
		Solution sol = new Solution(this.getElapsedTime());
		Object val;
		Variable var;

		try {
			for(ConcreteVariable<Long> v: vars) {
				val = solver.getValue(v.concreteVar(), v.type());
				var = v.clone();
				var.setConstant(val);
				sol.add(var);
			}
			for(ConcreteArrayVariable<Long> v: arrayVars) {
				for(int i=0; i < v.length(); i++) {
					val = solver.getValue(v.concreteArray(), i, v.type());
					var = new Variable(v.name() + "[" + i + "]", v.type(), v.use());
					var.setConstant(val);
					sol.add(var);
				}
			}
		} catch(VariableValueException e) {
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println(e.toString());
    		}
		}
		return sol;
	}	

	public DomainBox domainBox() {
		DomainBox box = new DomainBox();
		Object val;
		Variable var;
		try {
			for(ConcreteVariable<Long> v: vars) {
				val = solver.getValue(v.concreteVar(), v.type());
				box.add(new VariableDomain(v, val));
			}
			for(ConcreteArrayVariable<Long> v: arrayVars) {
				for(int i=0; i < v.length(); i++) {
					val = solver.getValue(v.concreteArray(), i, v.type());
					var = new Variable(v.name() + "[" + i + "]", v.type(), v.use());
					box.add(new VariableDomain(var, val));
				}
			}
		} catch(VariableValueException e) {
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(e.toString());
			}
		}
		return box;
	}	

}
