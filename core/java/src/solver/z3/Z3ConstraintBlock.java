package solver.z3;

import java.util.ArrayList;

import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;
import expression.logical.NondetAssignment;

import solver.z3.Z3Solver;
import validation.system.AbstractConstraintStore;

/** 
 * Stores the constraints generated from a Java instruction block for the z3 SMT solver.
 * The constraints should be linear.
 * While new constraints are added to the store of constraints, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * The set of concrete constraints stored as a list of z3_ast C++ pointers converted to
 * 64 bit long integers (so as to handle 64 bit architectures).
 * 
 * @author Olivier Ponsini
 */
public class Z3ConstraintBlock extends AbstractConstraintStore<Long> {

	private Z3Solver z3Solver;
	
	/**
	 * Constructs a store of constraints.
	 * 
	 */
	public Z3ConstraintBlock(Z3Solver solver) {
		z3Solver = solver;
	}

	@Override
	public String ctrToString(Long ctr) {
		return z3Solver.z3AstToString(ctr);
	}

	//----------------------------------
	// ConstraintBlock interface methods
	//----------------------------------

	/**
	 * Adds an abstract constraint to this store of constraints.
	 * 
	 * @param abstractConstraint The constraint to be added to this store.
	 * @param visitor
	 */
	@Override
	public boolean add(LogicalExpression abstractConstraint, ExpressionVisitor visitor) {
		//System.out.println("CTR=" + abstractConstraint);
		if (abstractConstraint instanceof NondetAssignment) {
			// A NondetAssignment does not produce any new constraint, the lhs variable has already been added to 
			// the variable block with its domain initialized to the full range of its type.
			return false;
		}
		Object concreteCtr = abstractConstraint.structAccept(visitor);
		//Need to check if we obtain a list of constraint or a unique constraint
		if (concreteCtr instanceof Long) {
			Long ctr = (Long)concreteCtr;
			add(ctr);
			z3Solver.z3AddCtr(ctr);			
		}
		else {
			for (Long ctr: (ArrayList<Long>)concreteCtr) {
				add(ctr);
				z3Solver.z3AddCtr(ctr);
			}
		}
		return true;
	}

	//--------------------------------------------------------
	//ConstraintBlock interface methods to handle backtracking
    //--------------------------------------------------------

	/**
	 * Restores the previous constraint store.
	 * All constraints added since the last call to {@link #save} are removed from this
	 * constraint store. The constraints are also removed from the z3 solver model.
	 */
	@Override
	public void restore() {
		//restore the previous concrete context
		z3Solver.z3PopContext();
		
		//remove the discarded constraints from this store
		super.restore();
	}

	/** 
	 * Saves the current constraint store to be restored later.
	 * Saving the store amounts to saving on a stack the index following the current last
	 * constraint in the store. Since we can only append new constraints or restore a previous
	 * store, it is safe to just remember from which index the new constraints were added.
	 * 
	 * This also saves the concrete context on the z3 solver side.
	 */
	@Override
	public void save() {
		z3Solver.z3PushContext();
		super.save();
	}

	@Override
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor,
			String name) {
		// TODO Auto-generated method stub
		return false;
	}

}
