package expression;

import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;

import expression.numeric.NumericExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.Variable;

import validation.system.xml.ValidationCSP;


public abstract class SimpleArrayExpressionVisitor implements ExpressionVisitor {
	
	protected String solverName;
	protected ValidationCSP csp; 
	
	public SimpleArrayExpressionVisitor(String solver, ValidationCSP csp) {
		this.solverName = solver;
		this.csp = csp;
	}

	public Object visit(LogicalExpression constraint) {
		return constraint.structAccept(this);
	}

	public Object visit(ParenExpression expression) {
		return expression.arg1().structAccept(this);
	}

	/** 
	 * Arrays are implemented as a collection of variables with a specific naming scheme: 
	 * <pre>'arrayName'_i'indexValue'</pre>. Hence, an array assignment is reduced to a 
	 * variable assignment. 
	 */
	public Object visit(ArrayAssignment assignment) {
		NumericExpression indexExpr = assignment.index();
		if (!indexExpr.isConstant()) {
			System.err.println("Error (ArrayAssignment visit): non constant indices are not allowed with "
					+ solverName
					+ "!");
			System.exit(47);
			return null;
		}
		else { //index is a constant
			int index = indexExpr.constantNumber().intValue();
			if (!((0 <= index) && (index < assignment.array().length()))) {
				System.err.println(
						"Error (ArrayAssignment visit): index (" 
						+ index
						+ ") out of bounds ("
						+ assignment.array().length()
						+ ")!");
				System.exit(47);
				return null;
			}
			else { //index is a constant in the array range
				//Get the name without the, here useless, SSA part
				String varName = assignment.array().root() + "_i" + index;
				Variable var = csp.validationSystem.addVar(varName);
				//We know the corresponding scalar variable has been created
				return this.visit(new Assignment(var, assignment.rhs()));
			}
		}
	}

	/** 
	 * Arrays are implemented as a collection of variables with a specific naming scheme: 
	 * <pre>'arrayName'_i'indexValue'</pre>. Hence, reading an array element's value is reduced to  
	 * reading the corresponding scalar variable's value. 
	 */
	public Object visit(ArrayElement elt) {
		NumericExpression indexExpr = elt.index();
		if (!indexExpr.isConstant()) {
			System.err.println("Error (ArrayAssignment visit): non constant indices are not allowed with "
					+ solverName
					+ "!");
			System.exit(47);
			return null;
		}
		else { //index is a constant
			int index = indexExpr.constantNumber().intValue();
			ArrayVariable av = (ArrayVariable)elt.array();
			if (!((0 <= index) && (index < av.length()))) {
				System.err.println(
						"Error (ArrayAssignment visit): index (" 
						+ index
						+ ") out of bounds ("
						+ av.length()
						+ ")!");
				System.exit(47);
				return null;
			}
			else { //index is a constant in the array range
				//Get the name without the, here useless, SSA part
				String varName = elt.array().root() + "_i" + index;
				//We know the corresponding scalar variable has been created
				return this.visit(csp.validationSystem.getVar(varName));
			}
		}
	}

}
