package solver.ilocp.cfg;

import ilog.concert.IloIntVar;
import expression.variables.ConcreteVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

import solver.Solver;
import solver.Solver.SolverEnum;
import solver.ilocp.IloCPConstraintBlock;
import solver.ilocp.IloCPExpressionVisitor;
import solver.ilocp.IloCPIntArrayVarBlock;
import solver.ilocp.IloCPIntVarBlock;
import solver.ilocp.IloCPSolver;

import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.cfg.CfgCsp;
import validation.util.Type;

/**
 * This class uses ILOG CPLEX.
 * 
 * @author Olivier Ponsini
 *
 */
public class IloCPCfgCsp extends CfgCsp<IloCPSolver,
									 IloCPIntVarBlock,
									 IloCPIntArrayVarBlock,
									 IloCPConstraintBlock,
									 IloCPExpressionVisitor> 
{
				
	public IloCPCfgCsp(String name) {
		super(name);
		solver = (IloCPSolver)Solver.createSolver(SolverEnum.ILOG_CP_OPTIMIZER);
		ctrs = new IloCPConstraintBlock(solver.solver());
		vars = new IloCPIntVarBlock(solver);
		arrayVars = new IloCPIntArrayVarBlock(solver);
		visitor = new IloCPExpressionVisitor(solver.solver(), vars, arrayVars);
	}

	// methods to manage backtrack
	
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
		Variable var;

		for(ConcreteVariable<IloIntVar> v: vars) {
			var = new Variable(v.name(), Type.INT);
			try {
				var.setConstant(solver.getValue(v.concreteVar()));
				sol.add(var);
			} catch (Exception e) {
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println("Variable " + var + " is not part of the solution!");
				}
			}
		}
		return sol;
	}	

	public DomainBox domainBox() {
		DomainBox box = new DomainBox();
		for(ConcreteVariable<IloIntVar> v: vars) {
			box.add(new VariableDomain(new Variable(v.name(), Type.INT), solver.getValue(v.concreteVar())));
		}
		return box;
	}	

}
