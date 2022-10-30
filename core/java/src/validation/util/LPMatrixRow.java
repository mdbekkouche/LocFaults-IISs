package validation.util;

import exception.AnalyzeException;

/**
 * This class represents an LP Matrix row in standard form.
 * That is:
 * <pre>
 *                                               | >= |
 * coeffs[0]*X0 + ... + coeffs[size-1]*X(size-1) | <= | bound
 *                                               | == |
 * </pre>
 * 
 * This representation assumes we know the number of columns beforehand. 
 * Zero coefficients does not save space in this representation (this 
 * should not bother us given the size of the problem we consider). 
 * 
 * Operations on rows assume rows of same size (number of columns).
 * 
 * @author Olivier Ponsini
 *
 */
public class LPMatrixRow {

	/**
	 * An array containing the coeffients of this row. 
	 * coeffs[i] corresponds to coefficient in column i+1 of the row (because row 
	 * columns are numbered from 1 in GLPK).
	 */
	private double[] coeffs;
	/**
	 * The bound of this row.
	 * That is the constant part of the (in)equality.
	 */
	private double bound;
	/**
	 * The relational operator of the (in)equality.
	 * It must either be <= ({@link #OpCode.LEQ}), or >= ({@link #OpCode.GEQ}), 
	 * or == ({@link OpCode.EQU}).
	 */
	private OpCode relOperator;
	/**
	 * True if all the coefficients are equal to 0.
	 * The bound may have any value.
	 */
	private boolean constant;
	
	/**
	 * Builds an LP Matrix row.
	 * Initially, bound and row coefficients are set to 0.
	 * 
	 * @param nbCol number of columns of this row. 
	 *              It must be greater or equal to 0.  
	 */
	public LPMatrixRow(int nbCol) {
		if (nbCol < 0) 
			nbCol = 0;
		
		//Java ensures the array is filled with zeroes (0.0d).
		this.coeffs = new double[nbCol];
		
		this.bound = 0;
		constant = true;
	}
	
	public boolean constant() {
		return constant;
	}

	public double bound() {
		return bound;
	}
	
	public void setBound(double bound) {
		this.bound = bound;
	}
	
	public OpCode relOperator() {
		return relOperator;
	}
	
	public void setRelOperator(OpCode op) throws AnalyzeException {
		if (op == OpCode.EQU || op == OpCode.LEQ || op == OpCode.GEQ) {
			this.relOperator = op;
		}
		else
			throw new AnalyzeException("LPMatrixRow error: " + 
					                   op.toString() + 
					                   " is not a standard LP relational operator!");
	}
	
	/**
	 * Sets the coefficient in this row's column <pre>col</pre> to <pre>value</pre>.
	 * 
	 * @param col The column of the coefficient to set. Columns are numbered from 1.
	 * @param value The value of the given coefficient.
	 */
	public void setCoeff(int col, double value) {
		this.coeffs[col-1] = value;
		if (value != 0)
			constant = false;
	}
		
	/**
	 * Return the coefficients of this row.
	 * The actual number of elements in the array is {@link #size()}.
	 * 
	 * Beware that the returned array is not a copy and should not be modified by the caller.
	 * 
	 * @return The array of coefficient values.
	 */
	public double[] getCoeffs() {
		return this.coeffs;
	}
	
	public int size() {
		return this.coeffs.length;
	}
	
	/**
	 * Sets all the coefficients of this row to their opposite value 
	 * (<pre>a</pre> becomes <pre>-a</pre>).
	 */
	public void oppositeCoeff() {
		for (int i=0; i<this.coeffs.length; i++) {
			this.coeffs[i] = -this.coeffs[i];
		}
	}
	
	/**
	 * Sets the bound to its opposite value (<pre>bound</pre> becomes <pre>-bound</pre>
	 */
	public void oppositeBound() {
		this.bound = -this.bound;
	}

	/**
	 * Sets both all the coefficients and the bound of this row to their opposite value 
	 * (<pre>a</pre> becomes <pre>-a</pre>).
	 */
	public void opposite() {
		this.oppositeBound();
		this.oppositeCoeff();
	}
	
	/**
	 * Adds the coefficients and bound values from <pre>r</pre> into this row.
	 * 
	 * @param r A row from the same matrix as this row, i.e. it must have the 
	 *          same size (number of columns).
	 */
	public void add(LPMatrixRow r) {
		this.bound += r.bound;
		if (!r.constant) {
			if (this.constant) { //r is not constant but this row is, we just copy the coeffs of r
				System.arraycopy(r.coeffs, 0, this.coeffs, 0, this.coeffs.length);
			}
			else { //neither this row nor r are constant 
				for (int i=0; i<this.coeffs.length; i++) {
					this.coeffs[i] += r.coeffs[i];
				}
			}
			constant = false;
		}
	}

	/**
	 * Adds the given value to the bound of this row.
	 * 
	 * @param d A double value to add to this row's bound.
	 */
	public void add(Double d) {
		this.bound += d;
	}

	/**
	 * Does a scalar product with this row.
	 * Row's coefficients and bound are multiplied by the given scalar value.
	 * 
	 * @param scalar A scalar value to multiply this row with.
	 */
	public void product(Double scalar) {
		this.bound *= scalar;
		if (!this.constant) { //Some coeffs are not zero 
			for (int i=0; i<this.coeffs.length; i++) {
				this.coeffs[i] *= scalar;
			}
		}		
	}

	/**
	 * Divides this row by a scalar.
	 * Row's coefficients and bound are divided by the given scalar value.
	 * This may be preferable to product by the inverse for precision reasons. 
	 * 
	 * @param scalar A scalar value to divide this row with.
	 */
	public void divide(Double scalar) {
		this.bound /= scalar;
		if (!this.constant) { //Some coeffs are not zero 
			for (int i=0; i<this.coeffs.length; i++) {
				this.coeffs[i] /= scalar;
			}
		}		
	}
	
	public String toString() {
	return "Operator=" + relOperator 
			+ " bound=" + bound 
			+ " size=" + this.coeffs.length
			+ " values=" + coeffs;
	}

}
