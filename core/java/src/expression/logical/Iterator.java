package expression.logical;

import java.util.ArrayList;
import java.util.List;

import validation.util.Type;
import expression.Expression;
import expression.numeric.BinaryExpression;
import expression.numeric.NumericExpression;
import expression.numeric.IntegerLiteral;
import expression.variables.Variable;

/** class to represent logical iterators
* that means JMLExist and JMLForAll
 * @author helen
 *
 */
public abstract class Iterator extends AbstractLogicalExpression {
	protected Variable index;
	protected LogicalExpression bound; // of the form (and minExpression maxExpression)
	protected LogicalExpression condition;
	private List<IntegerLiteral> enumeration;
	
	protected Iterator(Variable i, LogicalExpression a1, LogicalExpression a2) {
		index = i;
		bound = a1;
		condition =  a2;
		linear = bound.isLinear() && condition.isLinear();
		enumeration = Iterator.enumerate(bound, index.name());
	}
		
	public LogicalExpression boundExpression() {
		return bound;
	}

	public LogicalExpression condition() {
		return condition;
	}

	public Variable index() {
		return index;
	}

	public List<IntegerLiteral> enumeration() {
		return enumeration;
	}
		
	@Override
    public String toString() {
		return index.toString()
		+ " ,  " + bound.toString()
		+ " :  " + condition.toString();
	}
	
	/**
	 * Computes all the integer values allowed by the {@link #bound} condition for 
	 * the iterated variable {@link #index}.
	 * This computation verifies that the bound condition is enumerable 
	 * (see {@link #enumerate(LogicalExpression, String)}. If the bound is not integral (type INT), 
	 * we consider the iterator not to be enumerable. 
	 * 
	 * @return The list of all integer literals that this iterated variable {@link #index} can take 
	 *         according to this {@link #bound} constraint; or <code>null</code> if the bound condition
	 *         is not enumerable.
	 */
	public List<IntegerLiteral> enumerate() {
		return Iterator.enumerate(bound, index.name());
	}
	
	/**
	 * Computes all the integer values allowed by the {@link #bound} condition for the iterated variable 
	 * {@link #index}.
	 * This computation verifies that the bound condition is enumerable, that is, it is of the form
	 * <pre>indexName [GT|GE] value AND indexName [LT|LE] value</pre> modulo commutativity.  
	 * 
	 * @param bound A condition on the possible values of the iterated variable.
	 * @param indexName The name of the iterated variable.
	 * @return Either <code>null</code> if the bound is not enumerable, or a list containing all 
	 *         the enumerated values as IntegerLiteral. The list is guaranteed to always contain at 
	 *         least one value if it is not <code>null</code>.
	 */
	public static List<IntegerLiteral> enumerate(LogicalExpression bound, String indexName) {
		ArrayList<IntegerLiteral> result = null;
		if(bound instanceof AndExpression) {
			AndExpression and = (AndExpression)bound;
			Integer minBound, maxBound;
			//Because of "and" commutativity, we must check if the min (resp. max) bound is specified 
			//as the first or the second argument.
			if((
					((minBound = getMinBound(and.arg1(), indexName)) != null)
					&&
					((maxBound = getMaxBound(and.arg2(), indexName)) != null)
				)
			   	||
			   	(				
			   		(((minBound = getMinBound(and.arg2(), indexName)) != null)
			        &&
			        ((maxBound = getMaxBound(and.arg1(), indexName)) != null))
			    ))
			{
				if(minBound <= maxBound) {
					result = new ArrayList<IntegerLiteral>();
					for(int i=minBound; i<=maxBound; i++) {
						result.add(new IntegerLiteral(i+""));
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Computes the minimum possible value allowed by the bound condition for the iterated variable.
	 * Expression is supposed to be of the form either <pre>indexName [GT|GE] value</pre> or 
	 * <pre>value [LT|LE] indexName</pre>, with indexName and <pre>value</pre> of type 
	 * {@link Type.INT}; otherwise, it is not a "min bound" and <code>null</code> is
	 * returned.
	 * 
	 * @param bound A condition on the possible values of the iterated variable.
	 * @param indexName The name of the iterated variable.
	 * @return An integer whose value is the minimum possible value allowed by the bound condition for 
	 *         the iterated variable; or <code>null</code> if the bound condition is not enumerable.
	 *      
	 * @see #enumerate(LogicalExpression, String)
	 */
	static Integer getMinBound(Expression bound, String indexName) {
		if (bound instanceof Comparator) {
			Comparator cmp = (Comparator)bound;
			if ((cmp.arg1().type() == Type.INT) && (cmp.arg2().type() == Type.INT)) {
				NumericExpression cst;
				if ((cst = getBoundConstant(cmp.arg1(), indexName, cmp.arg2())) != null) 
				{
					if (cmp instanceof SupEqualExpression)
						return cst.constantNumber().intValue();
					else if (cmp instanceof SupExpression)
						return cst.constantNumber().intValue() + 1;
				}
				else if ((cst = getBoundConstant(cmp.arg2(), indexName, cmp.arg1())) != null) 
				{
					if (cmp instanceof InfEqualExpression)
						return cst.constantNumber().intValue();
					else if(cmp instanceof InfExpression)
						return cst.constantNumber().intValue() + 1;
				}
			}
		}
		return null;
	}

	/**
	 * Computes the maximum possible value allowed by the bound condition for the iterated variable.
	 * Expression is supposed to be of the form either <pre>value [GT|GE] indexName</pre> or 
	 * <pre>indexName [LT|LE] value</pre>, with indexName and <pre>value</pre> of type 
	 * {@link Type.INT}; otherwise, it is not a "max bound" and <code>null</code> is
	 * returned.
	 * 
	 * @param bound A condition on the possible values of the iterated variable.
	 * @param indexName The name of the iterated variable.
	 * @return An integer whose value is the maximum possible value allowed by the bound condition for 
	 *         the iterated variable; or <code>null</code> if the bound condition is not enumerable.
	 *      
	 * @see #enumerate(LogicalExpression, String)
	 */
	static Integer getMaxBound(Expression bound, String indexName) {
		if (bound instanceof Comparator) {
			Comparator cmp = (Comparator)bound;
			if ((cmp.arg1().type() == Type.INT) && (cmp.arg2().type() == Type.INT)) {
				NumericExpression cst;
				if ((cst = getBoundConstant(cmp.arg1(), indexName, cmp.arg2())) != null) 
				{
					if (cmp instanceof InfEqualExpression)
						return cst.constantNumber().intValue();
					else if (cmp instanceof InfExpression)
						return cst.constantNumber().intValue() - 1;
				}
				else if((cst = getBoundConstant(cmp.arg2(), indexName, cmp.arg1())) != null) 
				{
					if (cmp instanceof SupEqualExpression)
						return cst.constantNumber().intValue();
					else if (cmp instanceof SupExpression)
						return cst.constantNumber().intValue() - 1;
				}
			}
		}
		return null;
	}
		
	/**
	 * Checks whether <code>indexVar</code> is the iterated variable (i.e. a variable whose name is 
	 * <code>indexName</code>), and <code>literal</code> is a constant numeric value. If so, 
	 * <code>literal</code> is returned; otherwise, <code>null</code> is returned.
	 *  
	 * @param indexVar The expression to check for the iterated variable.
	 * @param indexName The name of the iterated variable.
	 * @param literal The expression to check for a numeric constant value.
	 * @return
	 */
	private static NumericExpression getBoundConstant(
			Expression indexVar, 
			String indexName, 
			Expression literal)
	{
		if((indexVar instanceof Variable)
		   && ((Variable)indexVar).name().equals(indexName)
		   && (literal instanceof NumericExpression)
		   && (literal.isConstant()))
		{
			return (NumericExpression)literal;
		}
		else {
			return null;
		}
	}

	
	// to know if the bound condition includes JMLResult variable
	public boolean boundContainsJMLResult() {
		return boundContainsJMLResult(bound);
	}
	
	private static boolean boundContainsJMLResult(Expression e) {
		if (e instanceof Variable && ((Variable)e).root().equals("JMLResult"))
			return true;
		if (e instanceof Comparator) {
			Comparator c = (Comparator)e;
			return boundContainsJMLResult(c.arg1()) 
			       || boundContainsJMLResult(c.arg2());
		}
		if (e instanceof LogicalOperator) {
			LogicalOperator op = (LogicalOperator)e;
			if(op instanceof NotExpression)
				return boundContainsJMLResult(op.arg1());
			else
				return boundContainsJMLResult(op.arg1()) 
				       || boundContainsJMLResult(op.arg2());
		}
		if (e instanceof BinaryExpression) {
			BinaryExpression intExpr = (BinaryExpression)e;
			return boundContainsJMLResult(intExpr.arg1()) 
			       || boundContainsJMLResult(intExpr.arg2());
		}
		return false;
	}
	
	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		// TODO
		return this;
	}
}