package solver.ilocp;

import java.util.ArrayList;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.cp.IloCP;

import validation.system.AbstractConstraintStore;

import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;

/** 
 * Stores the constraints generated from a Java instruction block for Ilog CP Optimizer.
 * The constraints can be linear or non linear.
 * While new constraints are added to the store of constraints, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public class IloCPConstraintBlock extends AbstractConstraintStore<IloConstraint> {
	
	/**
	 * The concrete Ilog CP Optimizer solver.
	 */
	private IloCP concreteSolver;
	
	/**
	 * The constructor of a store of constraints.
	 * 
	 * @param cs The concrete solver to be used with this store of constraints.
	 */
	public IloCPConstraintBlock(IloCP cs) {
		this.concreteSolver = cs;
	}
	
	//----------------------------------
	// ConstraintBlock interface methods
	//----------------------------------

	/**
	 * Adds an abstract constraint to the concrete solver.
	 * A single abstract {@link constraint.Constraint constraint} can 
	 * correspond to several concrete constraints in the concrete solver.
	 * 
     */
	@Override
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor) {
		return add(ctr, visitor, null);
	}
		
	@Override
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor, String name) {
		Object concreteConstraints = ctr.structAccept(visitor);
		return addAllConcreteConstraints(concreteConstraints, name);		
	}

	/**
	 * Adds all the concrete constraints contained in <code>constraintSet</code> to 
	 * this store of constraints and to the one of the concrete solver (Ilog CP Optimizer).
	 * 
	 * @param constraintSet This object represents the set of constraints to be added to the store.
	 *                      It can either be:
	 *                      <ul>
	 *                        <li><code>null</code>, <pre>JMLForAll</pre> constraint;</li>
	 *                        <li>an <code>ArrayList</code> of {@link ilog.concert.IloConstraint IloConstraint}s</li>
	 *                        <li>a single {@link ilog.concert.IloConstraint IloConstraint}</li>
	 *                      </ul>
	 */
	private boolean addAllConcreteConstraints(Object constraintSet, String name) {
		boolean added = true;
		if (constraintSet instanceof ArrayList) {
			ArrayList cl = (ArrayList)constraintSet;
			int i = 0;
			for (Object ctr: cl) {
				added &= addConcrete((IloConstraint)ctr, name + "_" + i++);
			}
		}
		else {
			added = addConcrete((IloConstraint)constraintSet, name);
		}
		return added;
	}

	/**
	 * Adds a concrete constraint to this store of constraints and 
	 * to the concrete solver own store of constraints.
	 * The concrete solver is the Ilog CP Optimizer solver and the concrete
	 * constraint is modeled through the Ilog Concert API class {@link ilog.concert.IloConstraint IloConstraint}.
	 * 
	 * @param c The constraint to be added as an Ilog Concert constraint.
	 */
	private boolean addConcrete(IloConstraint c, String name) {
		try {
			if (name != null) {
				c.setName(name);
			}
			concreteSolver.add(c);
			//The constraint is appended to the end of the ArrayList, its index is
			//then the one of the last member in the list.
			add(c);
			return true;
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//--------------------------------------------------------
	//ConstraintBlock interface methods to handle backtracking
    //--------------------------------------------------------

	/**
	 * Restores the previous constraint store.
	 * All constraints added since the last call to {@link #save} are removed from this
	 * constraint store and from the store of the concrete solver too.
	 */
	@Override
	public void restore() {
		IloConstraint c; 
		int idxLastConstrToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = elts.size()-1; i >= idxLastConstrToRemove; i--) {
			c = elts.get(i);
			try {
				concreteSolver.remove(c);
			} catch (IloException e) {
				e.printStackTrace();
			}
			elts.remove(i);
		}
	}

		
}