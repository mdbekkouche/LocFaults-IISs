package expression;

import java.util.ArrayList;
import java.util.Map;

import validation.util.Type;
import expression.logical.LogicalExpression;
import expression.numeric.NumericExpression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

/**
 * Describes a method call with a method name and a possibly empty list of 
 * expressions for the actual parameters.
 * 
 * The return value can be numeric or boolean.
 * This expression is never considered as constant.
 *  
 * @author Olivier Ponsini
 *
 */
public class MethodCall implements NumericExpression, LogicalExpression {
	/**
	 * The name of the method as used in the java program.
	 */
	private String methodName;
	/**
	 * List of effective method call parameters.
	 */
	private ArrayList<Expression> parameters;	
	/**
	 * The return type of the method of this call.
	 */
	protected Type type;

	//---------------------
	// Constructors
	//---------------------

	public MethodCall(String methodName, ArrayList<Expression> params, Type returnType) {
		this.methodName = methodName;
		this.type = returnType;
		this.parameters = params;
	}

	public MethodCall(String methodName, Type returnType) {
		this(methodName, new ArrayList<Expression>(), returnType);
	}

	//---------------------
	// Accessors
	//---------------------

	/**
	 * @return The name as used in the Java program call 
	 *         (may contain a qualification expression).
	 */
	public String methodName() {
		return methodName;
	}
	
	/**
	 * @return The expression that qualifies the method name in this call.
	 */
	public String qualification() {
		int pointPos = methodName.lastIndexOf('.');
		if (pointPos > 0)
			return methodName.substring(0, methodName.lastIndexOf('.'));
		else
			return "";
	}

	/**
	 * @return the name of the method (without any qualification expression).
	 */
	public String unqualifiedName() {
		//If methodName does not contain '.', lastIndexOf returns -1 and
		//we return the whole methodName string.
		return methodName.substring(methodName.lastIndexOf('.') + 1);
	}
	
	public ArrayList<Expression> parameters() {
		return parameters;
	}
	
	public void addParam(Expression param) {
		parameters.add(param);
	}
	
	//---------------------
	// Expression interface
	//---------------------
	
	/* (non-Javadoc)
	 * @see expression.AbstractExpression#isLinear()
	 * 
	 * TODO: see how this can be decided 
	 */
	public boolean isLinear() {
		return false;
	}


	public boolean isConstant() {
		return false;
	}
	

	public Type type() {
		return type;
	}


	public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see expression.numeric.AbstractNumericExpression#substitute(expression.variables.Variable, expression.Expression)
	 * 
	 * This must be overriden as default behavior in AbstractNumericExpression forbids substitution by
	 * anything but NumericExpression.
	 * 
	 */

	public MethodCall substitute(Variable var, Expression val) {
		MethodCall substitutedMC =  new MethodCall(methodName, type);
		for (Expression expr: parameters) {
			substitutedMC.addParam(expr.substitute(var, val));
		}
		return substitutedMC;
	}


	public MethodCall computeLastRenaming(SymbolTable var) {
		MethodCall renamedMC =  new MethodCall(methodName, type);
		for (Expression expr: parameters) {
			renamedMC.addParam(expr.computeLastRenaming(var));
		}
		return renamedMC;
	}
	
	//----------------------------
	// NumericExpression interface
	//----------------------------

	public Number constantNumber() {
		return null;
	}
	

	public MethodCall substitute(Variable var, NumericExpression val) {
		return this.substitute(var, (Expression)val);
	}
	
	//----------------------------
	// LogicalExpression interface
	//----------------------------


	public boolean isComparison() {
		return false;
	}
	

	public boolean constantBoolean() {
		return false;
	}

	//----------------------------
	// Others 
	//----------------------------

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(methodName);
		s.append('(');
		for (Expression expr: parameters) {
			s.append(expr.toString());
			s.append(',');
		}
		s.deleteCharAt(s.length()-1);
		return s.append(')').toString();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof MethodCall)
			&& methodName.equals(((MethodCall)o).methodName)
			&& parameters.equals(((MethodCall)o).parameters);
	}

	@Override
	public int hashCode() {
		return methodName.hashCode();
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		MethodCall substitutedMC =  new MethodCall(methodName, type);
		for (Expression expr: parameters) {
			substitutedMC.addParam(expr.setPrefixInFunction(functionName, prefix));
		}
		return substitutedMC;
	}

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		MethodCall substitutedMC =  new MethodCall(methodName, type);
		for (Expression expr: parameters) {
			substitutedMC.addParam(expr.substituteAll(substitutions));
		}
		return substitutedMC;
	}

}
