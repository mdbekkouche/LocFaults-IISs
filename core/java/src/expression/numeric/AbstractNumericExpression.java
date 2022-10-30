package expression.numeric;

import expression.AbstractExpression;
import validation.util.Type;
import expression.variables.Variable;
import expression.Expression;

/**
 * An abstract implementation of {@link NumerciExpression numeric expressions}.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public abstract class AbstractNumericExpression extends AbstractExpression implements NumericExpression {
	/**
	 * The value of this numeric expression.
	 * If <code>null</code>, the expression is not constant.
	 */
	protected Number constantNumber = null;
	/**
	 * The type of this numeric expression.
	 */
	protected Type type;
	
	/**
	 * @return The type of this numeric expression.
	 */
	@Override
	public Type type() {
		return type;
	}
	
	/**
	 * Returns the value of this numeric expression as a Number.
	 * The returned value is only meaningful if the expression is constant 
	 * (which must be checked by calling {@link #isConstant()}. 
	 * 
	 * @return The value of this numeric expression as a Number.
	 */
	@Override
	public Number constantNumber() {
		return constantNumber;
	}
		
	@Override
	public boolean isConstant() {
		return constantNumber != null;
	}
	
	/* (non-Javadoc)
	 * @see expression.Expression#substitute(expression.variables.Variable, expression.Expression)
	 * 
	 * It is an error to try to substitute 
	 */
	@Override
	public Expression substitute(Variable var, Expression val) {
		if (val instanceof NumericExpression) {
			return this.substitute(var, (NumericExpression)val);
		}
		else {
			System.err.println("Error (AbstractNumericExpression.substitute): "
					+ "trying to replace a numeric variable by a non numeric expression!");
			System.exit(-1);
			return null;
		}
	}     

}