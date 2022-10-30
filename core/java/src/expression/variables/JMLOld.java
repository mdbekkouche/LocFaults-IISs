package expression.variables;


import java.util.Map;

import expression.Expression;
import expression.numeric.NumericExpression;
import expression.logical.LogicalExpression;
import expression.ExpressionVisitor;
import validation.util.Type;

/**
 * Represents old value of program variables which may be integers, floats 
 * (<code>float</code> and <code>double</code>), or booleans.
 * All variables are linear and can be both numeric or logical.
 * Two variables with the same name are equals.
 * 
 * NOTA: in JML, \old applies to an expression but when we parse 
 * \old JML we distribute the \old among its arguments. For example, 
 * \old(var1 + var2) is built as the expression Plus(JMLOld(var1),JMLOld(var2))
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 * To make things easier, JMLOld inherits from Variable
 * note that we really need to have the reference to the variable itself 
 * because we do not use the SymbolTable in the visitors
 * Thus the renaming of the variable before function call is not sufficient
 */
public class JMLOld extends Variable {
	/**
	 * the variable on which is applied the \old operator.
	 */
	private Variable variable;

	//---------------------
	// Constructors
	//---------------------

	/**
	 * constructs an old expression from a variable
	 */
	public JMLOld(Variable v) {
		super(v);
		variable = v;
	}

	/**
	 * Deep copy constructor.
	 * 
	 */
	protected JMLOld(JMLOld v) {
		this(v.variable.clone());
	}

	//---------------------
	// Accessors
	//---------------------

	// this function needs to be redefined because the old value of
	// the variable inherited from class Variable is not set
	public Variable oldValue() {
		return variable.oldValue();
	}

	//---------------------
	// Expression interface
	//---------------------
	

	/* lydie */   
	@Override
	public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}
	
	//Pas de vérification de type à ce niveau !
	@Override
	public Expression substitute(Variable v, Expression val) {
		return new JMLOld((Variable)this.variable.substitute(v, val));
	}
	
	@Override
	// the last renaming of an old expression is the variable 
	// before function call
	public Variable computeLastRenaming(SymbolTable var) {
		return variable.oldValue();
	}
	
	//----------------------------
	// NumericExpression interface
	//----------------------------
	
	@Override
	public NumericExpression substitute(Variable v, NumericExpression val) {
		return new JMLOld((Variable)variable.substitute(v, val));
	}

	
	//---------------------
	// Comparable interface
	//---------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * 
	 * Comparison is based on the variable's name.
	 */
	@Override
	public int compareTo(Object v) {
		if (!(v instanceof JMLOld)) 
			return -1;
		JMLOld vo = (JMLOld)v;
		return vo.variable.compareTo(variable) ;
	}
	
	//---------------------
	// Cloneable interface
	//---------------------

	
	/* 
	 * Deep copy.
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public JMLOld clone() {
		return new JMLOld(this);
	}
	
	
	//---------------------
	// Others
	//---------------------
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * Two variables are equals if they have the same name.
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof JMLOld)
			&& variable.equals(((JMLOld)o).variable);
	}


	@Override
	public String toString() {
		return "\\old(" + variable + ")"  ;
	}


	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return new JMLOld((Variable)variable.setPrefixInFunction(functionName, prefix));
	}
	
	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		return new JMLOld((Variable)variable.substituteAll(substitutions));
	}

//	@Override
//	public boolean isConstant() {
//		return variable.isConstant();
//	}
//
//	@Override
//	public boolean isLinear() {
//		return true;
//	}
//
//	@Override
//	public Type type() {
//		return variable.type();
//	}

}
