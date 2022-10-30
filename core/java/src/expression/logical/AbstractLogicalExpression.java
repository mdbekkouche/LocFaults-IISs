package expression.logical;

import expression.AbstractExpression;
import validation.util.Type;

/**
 * An abstract implementation of {@link LogicalExpression logical expressions}.
 * Type is always {@link Type.BOOL}.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public abstract class AbstractLogicalExpression extends AbstractExpression implements LogicalExpression {
	/**
	 * The value of this logical expression.
	 * If <code>null</code>, the expression is not constant.
	 */
	Boolean constantBoolean = null;
	
	@Override
	public boolean isConstant() {
		return constantBoolean != null;
	}

	/**
	 * Returns the value of this logical expression as a boolean.
	 * The returned value is only meaningful if the expression is constant 
	 * (which must be checked by calling {@link #isConstant()}. 
	 * 
	 * @return The value of this logical expression as a <code>boolean</code>.
	 */
	@Override
	public boolean constantBoolean() {
		return constantBoolean;
	}

	@Override
	public Type type() {
    	return Type.BOOL;
    }
    
    @Override
    public boolean isComparison() {
		return false;
	}
    
}
