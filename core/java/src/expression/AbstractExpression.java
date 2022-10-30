package expression;

import expression.logical.LogicalLiteral;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.variables.VariableDomain;

/**
 * This abstract class is an implementation of {@link Expression} that gathers
 * what is common to {@link numeric.AbstractNumericExpresion numeric expressions} and 
 * {@link logical.AbstractLogicalExpresion logical expressions}. 
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public abstract class AbstractExpression implements Expression {
    /**
     * Marks the expression as linear.
     */
    protected boolean linear;


    public boolean isLinear() {
    	return linear;
    }

	/**
	 * A utility method to construct a literal object from a given singleton domain.
	 * 
	 * @param domain This domain must be a singleton, and its value and type will be 
	 *               used to create the corresponding (i.e. same type, same value)
	 *               expression literal.
	 *  
	 * 
	 * @return A Literal object (LogicalLiteral, IntegerLiteral, FloatLiteral, 
	 *         or DoubleLiteral) corresponding to the given domain type and value. 
	 */
	public static Expression createLiteral(VariableDomain domain) {
		switch(domain.type()) {
		case BOOL:
			return new LogicalLiteral(domain.booleanValue());
		case INT:
			return new IntegerLiteral(domain.minValue().intValue());
		case FLOAT:
			return new FloatLiteral(domain.minValue().floatValue());
		case DOUBLE:
			return new DoubleLiteral(domain.minValue().doubleValue());
		default:
			System.err.println("Error (createLiteral): unsupported numeric type (" 
					+ domain.type() 
					+ ")!");
			return null;
		}
	}



}
