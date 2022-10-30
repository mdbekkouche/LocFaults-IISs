package solver.z3.xml;

import exception.VariableValueException;
import expression.variables.ConcreteArrayVariable;
import expression.variables.ConcreteVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;
import solver.Solver.SolverEnum;
import solver.z3.Z3ArrayVarBlock;
import solver.z3.Z3ConstraintBlock;
import solver.z3.Z3ExpressionVisitor;
import solver.z3.Z3Solver;
import solver.z3.Z3VarBlock;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.xml.ValidationCSP;
import validation.system.xml.ValidationSystem;

/**
 * This class uses z3 to solve linear CSPs over reals.
 * 
 * @author Olivier Ponsini
 *
 */
public class Z3Csp extends ValidationCSP {
	/**
	 * Constructs a concrete linear CSP on reals handled by z3.
	 */ 
	public Z3Csp(String name, ValidationSystem vs) {
		super(name, SolverEnum.Z3, vs);
	}

	/**
	 * Creates the stores of constraints and variables that will be handled by the concrete 
	 * solver.
	 * 
	 * This method is called by a mother class constructor.
	 *  
	 * @param s The solver associated with this CSP. It must be of type 
	 *          {@link solver.Z3Solver}.    
	 */	
	@Override
	protected void initCSP() {
		constr = new Z3ConstraintBlock((Z3Solver)cspSolver);
		varBlock = new Z3VarBlock((Z3Solver)cspSolver);
		arrayVarBlock = new Z3ArrayVarBlock((Z3Solver)cspSolver);
		visitor = new Z3ExpressionVisitor((Z3Solver)cspSolver, (Z3VarBlock)varBlock, (Z3ArrayVarBlock)arrayVarBlock);
	}	
	
	@Override
	public Solution solution() {
		Solution sol = new Solution(this.getElapsedTime());
		Object val;
		Variable var;

		try {
			for(ConcreteVariable<Long> v: (Z3VarBlock)varBlock) {
				val = ((Z3Solver)cspSolver).getValue(v.concreteVar(), v.type());
				var = v.clone();
				var.setConstant(val);
				sol.add(var);
			}
			for(ConcreteArrayVariable<Long> v: (Z3ArrayVarBlock)arrayVarBlock) {
				for(int i=0; i < v.length(); i++) {
					val = ((Z3Solver)cspSolver).getValue(v.concreteArray(), i, v.type());
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

	@Override
	public DomainBox domainBox() {
		DomainBox box = new DomainBox();
		Object val;
		Variable var;
		try {
			for(ConcreteVariable<Long> v: (Z3VarBlock)varBlock) {
				val = ((Z3Solver)cspSolver).getValue(v.concreteVar(), v.type());
				var = v.clone();
				var.setConstant(val);
				box.add(var.domain());
			}
			for(ConcreteArrayVariable<Long> v: (Z3ArrayVarBlock)arrayVarBlock) {
				for(int i=0; i < v.length(); i++) {
					val = ((Z3Solver)cspSolver).getValue(v.concreteArray(), i, v.type());
					var = new Variable(v.name() + "[" + i + "]", v.type(), v.use());
					var.setConstant(val);
					box.add(var.domain());
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
