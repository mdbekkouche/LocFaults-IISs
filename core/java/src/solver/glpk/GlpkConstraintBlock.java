package solver.glpk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;

import validation.Validation.VerboseLevel;
import validation.system.ConstraintStore;
import validation.util.LPMatrixRow;

/** 
 * Stores the constraints generated from a Java instruction block for GLPK.
 * The constraints must be linear.
 * While new constraints are added to the store of constraints, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * @author Olivier Ponsini
 */
public class GlpkConstraintBlock implements ConstraintStore<Integer> {

	/**
	 * The current last row number of the GLPK LP matrix.
	 * It corresponds to the last constraint added (numbered from 1).
	 */
	protected int lastRow;
	/**
	 * This stack handles saving and restoring stores of constraints.
	 * The typical usage of the store of constraints is to add new constraints until
	 * it is needed to remove the last ones added. In order to do this, we just need to 
	 * record the index in the array of constraints {@link #constr} from which we started
	 * to add new constraints we may need to remove.
	 * 
	 * @see #save()
	 * @see #restore()
	 */
	private Stack<Integer> save; 
	/**
	 * Link to the GLPK solver.
	 */
	protected GlpkSolver solver; 
	/**
	 * This array contains the columns in a row of the GLPK problem matrix 
	 * whose coefficients are to be set. In our handling of rows, all columns 
	 * are always set, so the array simply contains all the column numbers and it
	 * does not change from one row to another (except if the size of a row in the problem matrix
	 * has increased). 
	 */
	private int[] indices;

	/**
	 * Constructs a store of constraints.
	 * 
	 */
	protected GlpkConstraintBlock(GlpkSolver solver) {
		this.solver = solver;
		this.indices = new int[0];
		this.save = new Stack<Integer>();
		this.lastRow = 0;
	}

	//----------------------------------
	// ConstraintBlock interface methods
	//----------------------------------

	@Override
	public int size() {
		return lastRow;
	}

	/**
	 * Adds an abstract constraint to this store of constraints.
	 * 
	 * @param abstractConstraint The constraint to be added to this store.
	 * @param vars The variables currently used in the CSP.
	 * @param arrayVars The array variables currently used in the CSP.
	 * 
	 * @return <code>True</code> if the constraint could be added; <code>false</code> otherwise.
	 */
	@Override
	public boolean add(LogicalExpression constraint, ExpressionVisitor visitor) {
		GlpkExpressionVisitor glpkVisitor = (GlpkExpressionVisitor)visitor;
		
		//Adds the constraints to the GLPK LP matrix.
		//Visiting a constraint may return a single row or 
		//an array of rows
		Object ctrs = constraint.structAccept(glpkVisitor);
		//We build here the array of indices because the constraint may have added one column to the LP 
		//matrix if it was an array assignment.
		createGLPKRowIndices(glpkVisitor.varSize());
		if (ctrs instanceof LPMatrixRow) {
			//It is a single row
			LPMatrixRow row = (LPMatrixRow)ctrs;
			solver.addCtr(
					row.bound(), 
					row.relOperator().ordinal(), 
					glpkVisitor.varSize(),  //we should always have vars.size == row.size()
					this.indices,
					row.getCoeffs());
			lastRow++;
		}
		else {
			//It is an ArrayList of rows
			for (LPMatrixRow row: (ArrayList<LPMatrixRow>)ctrs) {					
				solver.addCtr(
						row.bound(), 
						row.relOperator().ordinal(), 
						glpkVisitor.varSize(),  //we should always have vars.size == row.size()
						this.indices,
						row.getCoeffs());
				lastRow++;
			}
		}
		return true;
	}

	/**
	 * This creates an array of length <pre>size</pre> filled with values from 1 to <pre>size</pre>.
	 * This array represents the columns whose value must be set in the LP matrix problem.
	 * 
	 * @param size The array size: it must be equal to the number of columns 
	 *             in the GLPK problem matrix and at least >= 0. 
	 */
	private void createGLPKRowIndices(int size) {
		//We also could do this only if indices.length < size
		if (this.indices.length != size) {
			this.indices = new int[size]; 
			for (int i=0; i<size; i++) {
				indices[i]=i+1;
			}
		}
	}

	//--------------------------------------------------------
	//ConstraintBlock interface methods to handle backtracking
    //--------------------------------------------------------

	/**
	 * Restores the previous constraint store.
	 * All constraints added since the last call to {@link #save} are removed from this
	 * constraint store.
	 */
	@Override
	public void restore() {
		int lastRowToKeep = save.pop();
		int[] rows = new int[lastRow - lastRowToKeep];
		
		for (int i = lastRow; i > lastRowToKeep; i--) {
			rows[i - lastRowToKeep - 1] = i;
		}
		
		if (rows.length > 0) {
			solver.delRows(rows);
		}
		
		lastRow = lastRowToKeep;
	}

	/** 
	 * Saves the current constraint store to be restored later.
	 * Saving the store amounts to saving on a stack the row number of the current last
	 * constraint in the GLPK matrix. Since we can only append new constraints or restore a previous
	 * store, it is safe to just remember from which row the new constraints were added.
	 */
	@Override
	public void save() {
		save.push(lastRow);
	}

	/** 
	 * Prints all the elements.
	 */
	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.print(this.toString());
		}
	}

	@Override
	public String toString() {
		String s = "";
		for (int i=1; i<=lastRow; i++) {
			s += solver.rowToString(i) + "\n";
		}
		return s;
	}

	//--------------------------------------------------
	//Ierable interface
	//
	//This just iterates over the LP matrix row indices.
	//--------------------------------------------------
	
	@Override
	public Iterator<Integer> iterator() {
		return new RowIterator(this.lastRow);
	}

	private class RowIterator implements Iterator<Integer> {
		
		private int cur;
		private int last;
		
		public RowIterator(int last) {
			cur = 1;
			this.last = last;
		}
		
		@Override
		public boolean hasNext() {
			return cur <= last;
		}

		@Override
		public Integer next() {
			if (cur <= last) {
				return cur++;
			}
			else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	@Override
	public void reset() {
		lastRow = 0;
		indices = new int[0];
		save.clear();
	}

	@Override
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor,
			String name) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
