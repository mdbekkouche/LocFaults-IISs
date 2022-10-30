package validation.system;

import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;

/**
 * A constraint block where constraints are stored as logical expressions.
 * This is useful when constraints are not added on-the-fly to the solver.
 * 
 * 
 * @author Olivier Ponsini
 *
 */
public class ExpressionCtrStore extends AbstractConstraintStore<LogicalExpression> {

	@Override
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor) {
		return add(ctr);
	}

	@Override
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor,
			String name) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
