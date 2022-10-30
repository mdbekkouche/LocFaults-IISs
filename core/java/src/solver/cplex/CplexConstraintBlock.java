package solver.cplex;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;

import validation.Validation.VerboseLevel;
import validation.system.AbstractConstraintStore;

import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;

/** a class to store constraints of a Java block
 * allow to perform backtrak on the blocks
 * @author helen
 *
 */
public class CplexConstraintBlock extends AbstractConstraintStore<IloConstraint> {
		
	// the concrete solver
	private IloCplex solver; 
	
	public CplexConstraintBlock(Cplex s) {
		this.solver = s.solver;
	}
	
	//--------------------------
	// methods to handle constraints

	/** add integer constraint */
	//if the constraint is not linear, it is not added 
	@Override
	public boolean add(LogicalExpression c, ExpressionVisitor visitor) {
		return add(c, visitor, null);
	}

	public boolean add(LogicalExpression c, ExpressionVisitor visitor, String name) {
		boolean added = false;
		
		if (c.isLinear()) {
			CplexExpressionVisitor cplexVisitor = (CplexExpressionVisitor)visitor;
			//Reset the internal fail state of the visitor so that a previous failed visit
			//won't stop this new visit.
			cplexVisitor.resetFailStatus();
			Object o = c.structAccept(cplexVisitor);
			if (cplexVisitor.fail) {
				System.err.println("It was not possible to add constraint " 
						+ c 
						+ " in linear CSP:");
				System.err.println(cplexVisitor.failReason);
			}
			else if (o != null) {
				constraintSet(o, name);
				added = true;
			}
		}
		return added;
	}
		
	private void constraintSet(Object o, String name) {
		//It must either be an IloConstraint or an ArrayList<IloConstraint>
		if (o instanceof ArrayList<?>) {
			ArrayList<?> cl = (ArrayList<?>)o;
			for (Object co : cl) {
				addConcrete((IloConstraint)co, name);
			}
		}
		else {
			addConcrete((IloConstraint)o, name);
		}
	}
		
	/** add a ilog constraint */
	private boolean addConcrete(IloConstraint c, String name) {
		try {
			if (name != null) {
				c.setName(name);
			}
			solver.add(c);
			add(c); 
			return true;
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	//--------------------------
	// methods to handle backtrak
	
	/** restore the previous block */
	@Override
	public void restore() {
//		if (!save.empty() && !save.peek().equals(constr.size())) {
			// restoring constraints
			int indexConstr = ((Integer)save.pop()).intValue();
			for (int i = elts.size()-1; i>indexConstr-1; i--) {
				IloConstraint c = elts.get(i);
				try {
					solver.remove(c);
				} catch (IloException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				elts.remove(i);
			}
//		}
	}
		
	// to remove constraints
	
	/** remove a ilog constraint */
	void removeIloConstraint(IloConstraint c){
		boolean ok=true;
		try {
			solver.remove(c);
		} catch (IloException e) {
			ok=false;
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Trying to remove constraint " 
					           + c 
					           + " not in constraint store.");
			}
		}
		if (ok)
			elts.remove(c); 
	}	
	
}