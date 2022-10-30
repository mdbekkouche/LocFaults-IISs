package validation.visitor;

import expression.logical.LogicalExpression;

/** 
 * Class to represent a case of ensure part of JML specification
 * each case is a LogicalExpression
 * 
 * used by Strategy and ValidationSystem (addSpecification)
 * 
 * @author helen
 */
public class EnsureCase {
	
	public LogicalExpression exp;
	
	public EnsureCase(LogicalExpression e){
		exp = e;
	}

	public String toString() {
		return exp.toString();
	}
}
