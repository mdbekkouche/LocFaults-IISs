package validation.system;

import expression.logical.LogicalExpression;
import expression.ExpressionVisitor;

/** 
 * To store constraints of a program block.
 * The generic type <pre>T</pre> denotes the concrete constraint type (e.g. IloConstraint).
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public interface ConstraintStore<T> extends Store<T> {

	/** 
	 * Adds a constraint.
	 * The visitor is used to convert the logical expression into the concrete constraint
	 * that will be added to the store. 
	 */
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor);

	/** 
	 * Adds a constraint named <code>name</code>.
	 * The visitor is used to convert the logical expression into the concrete constraint
	 * that will be added to the store. 
	 */
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor, String name);
	
}
