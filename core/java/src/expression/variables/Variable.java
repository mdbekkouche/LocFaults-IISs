package expression.variables;


import java.util.Map;

import expression.Expression;
import expression.numeric.NumericExpression;
import expression.logical.LogicalExpression;
import expression.ExpressionVisitor;
import validation.Validation.VerboseLevel;
import validation.util.Type;

/**
 * Represents program variables which may be integers, floats 
 * (<code>float</code> and <code>double</code>), or booleans.
 * All variables are linear and can be both numeric or logical.
 * Two variables with the same name are equals.
 * 
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public class Variable implements NumericExpression, LogicalExpression, Cloneable, Comparable<Object> 
{
	/**
	 * Name of this variable.
	 */
	protected String name;
	/**
	 * The type of this numeric expression.
	 */
	protected Type type;
	/**
	 * Numeric variable's domain.
	 * If <code>null</code>, it means the variable ranges over the full type domain.
	 */
	private VariableDomain domain;
	
	
	/**
	 * variable before the current function call
	 * this is needed for \old operator
	 * this field is set when adding a variable in the symbol table
	 * for the first time: it is equals to the variable itSelf
	 * 
	 * it is also set using "setOld" for all global variables
	 * which are modified in a function
	 */
	private Variable old;
	
	/**
	 * 
	 * @author helen
	 * the possible ways to use a variable
	 * default use is local
	 */
	public enum Use {
	    /**
	     * Local variable of a method.
	     */
	    LOCAL,
	    /**
	     * Parameter of a method.
	     */
	    PARAMETER,
	    /**
	     * Attribute of a class.
	     */
	    GLOBAL,
	    /**
	     * Variable declared inside a JML \forall or \exists expression.  
	     */
	    JML_QUANTIFIED,
	    /**
	     * For variables created by the analysis.
	     */
	    META
	}

	/**
	 * the way this variable is used
	 */
	Use use;
		

	//---------------------
	// Constructors
	//---------------------

//	/** 
//	 * implicit constructor
//	 * required for \Old
//	 */
//	public Variable() {
//		super();
//	}
	
	/**
	 * Constructs a Variable of given name, type and numeric domain.
	 * 
	 * @param n Name of the variable.
	 * @param t Type of the variable.
	 * @param min Lower bound of the variable's domain
	 * @param max Upper bound of the variable's domain
	 */
	public Variable(String n, Type t, Number min, Number max) {
		name = n;
		type = t;
		use = Use.LOCAL;
		domain = new VariableDomain(this, min, max);
	}
	/**
	 * Constructs a Variable of given name, type, use and numeric domain.
	 * 
	 * @param n Name of the variable.
	 * @param t Type of the variable.
	 * @param min Lower bound of the variable's domain
	 * @param max Upper bound of the variable's domain
	 */
	public Variable(String n, Type t, Use u, Number min, Number max) {
		name = n;
		type = t;
		use = u;
		domain = new VariableDomain(this, min, max);
	}
	/**
	 * Constructs a Boolean Variable of given name, type and Boolean domain.
	 * 
	 * @param n Name of the variable.
	 * @param b Boolean value of this Boolean variable.
	 */
	public Variable(String n, Boolean b) {
		name = n;
		type = Type.BOOL;
		use = Use.LOCAL;
		domain = new VariableDomain(this, b);
	}
	/**
	 * Constructs a Variable of given name and type.
	 * 
	 * @param n Name of the variable.
	 * @param t Type of the variable.
	 * @param u set the use of the variable
	 */
	public Variable(String n, Type t, Use u) {
		name = n;
		type = t;
		use = u;
		domain = null;
	}
	/**
	 * Constructs a Variable of given name and type.
	 * 
	 * @param n Name of the variable.
	 * @param t Type of the variable.
	 */
	public Variable(String n, Type t) {
		this(n, t, Use.LOCAL);
	}
	/**
	 * Deep copy constructor.
	 * 
	 */
	protected Variable(Variable v) {
		this(v.name, v.type, v.use);
		if (v.domain != null) {
			this.domain = v.domain.clone();
			this.domain.setVariable(this);
		}
	}

	//---------------------
	// Accessors
	//---------------------

	/**
	 * @return the name of this variable.
	 */
	public String name() {
		return name;
	}

	/**
	 * @return whether this variable is a parameter of the validated method or not.
	 */
	public boolean isParameter() {
		return use == Use.PARAMETER;
	}
	
	/**
	 * @return whether this variable is a parameter 
	 *        of the method named n.
	 */
	public boolean isParameter(String n) {
		if (use == Use.PARAMETER) {
			int first = name.indexOf('_');
			int second = name.indexOf('_', first+1);
			if (second==-1) {
				System.err.println("problem when getting the name of a variable in a function call");
				System.exit(0);
			}
			// variable which is used in a function to represent
			// a global variable
			return name.substring(0, first).equals(n);
		}
		return false;
	}
	
	/**
	 * @return whether this variable is global or not.
	 */
	public boolean isGlobal() {
		return use == Use.GLOBAL;
	}

	/**
	 * @return whether this variable is a JML iteration variable (used in quantified expressions) or not.
	 */
	public boolean isQuantified() {
		return use == Use.JML_QUANTIFIED;
	}
	
	/** return the use of the variable
	 */
	public Use use() {
		return use;
	}
	
//	/** to set the use of the variable
//	 * required for assignments :
//	 *    v = nondet_in set the variable to nondet global or local
//	 *    v = expression set the variable to global or local
//	 */
//	public void setUse(Use u) {
//		use = u;
//	}
	
	
	/**
	 * @return the current domain of this variable.
	 */
	public VariableDomain domain() {
		return domain;
	}
		
	/**
	 * Set the domain of this variable to the interval <pre>[min, max]</pre>.
	 * 
	 * @param min Domain lower bound.
	 * @param max Domain upper bound.
	 */
	public void setDomain(Number min, Number max) {
		if (domain == null) {
			domain = new VariableDomain(this, min, max);
		}
		else {
			domain.setMinValue(min);
			domain.setMaxValue(max);
		}
	}

	/**
	 * Set the domain of this variable to the given boolean value.
	 * 
	 */
	public void setDomain(Boolean b) {
		if (domain == null) {
			domain = new VariableDomain(this, b);
		}
		else {
			domain.setBooleanValue(b);
		}
	}

	/**
	 * Set the domain of this variable to the given domain.
	 */
	public void setDomain(VariableDomain d) {
		domain = d;
	}

	/**
	 * Sets this variable to a constant value. 
	 * This variable will be marked as constant and its 
	 * domain will be set to the given value. 
	 * 
	 * @param v The value of this variable. It must either be a {@link Number} or
	 *          a {@link Boolean} according to this variable's type.
	 */
	public void setConstant(Object v) {
		if (type == Type.BOOL) {
			setDomain((Boolean)v);
		}
		else {
			setDomain((Number)v, (Number)v);
		}
	}
	

	/**
	 * Reset the domain of this variable to its type range.
	 */
	public void resetDomain() {
		domain = null;
	}
	
	/** to set the renaming before function call
	 * this is used for global variables when the FunctionCallNode is created
	 * 
	 * @param v: the variable before function call
	 */
	public void setOld(Variable v) {
		old = v;
	}
	
	public Variable oldValue() {
		return old;
	}
	
	//---------------------
	// Expression interface
	//---------------------
	
	@Override
	public boolean isLinear() {
		return true;
	}

	@Override
	public boolean isConstant() {
		return (domain != null) && domain.isSingleton();
	}
	
	@Override
	public Type type() {
		return type;
	}

	/* lydie */   
	@Override
	public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}
	
	//Pas de vérification de type à ce niveau !
	@Override
	public Expression substitute(Variable var, Expression val) {
		if (name.equals(var.name))
			return val;
		return this;
		}
	
	@Override
	// if the variable is a parameter, its last renaming is itself
	// i.e. it not have to be renamed in the post-condition
	public Variable computeLastRenaming(SymbolTable var) {
		if (isParameter())
			return this;
		return var.get(root());
	}
	
	//----------------------------
	// NumericExpression interface
	//----------------------------

	@Override
	public Number constantNumber() {
		return domain.minValue();
	}
	
	@Override
	public NumericExpression substitute(Variable var, NumericExpression val) {
		if (name.equals(var.name))
			return val;
		return this;
	}

	//----------------------------
	// LogicalExpression interface
	//----------------------------

	@Override
	public boolean isComparison() {
		return false;
	}
	
	@Override
	public boolean constantBoolean() {
		return domain.booleanValue();
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
	public int compareTo(Object var) {
		if (!(var instanceof Variable)) 
			return -1;
		Variable v = (Variable)var;
		return name.compareTo(v.name) ;
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
	public Variable clone() {
		return new Variable(this);
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
		return (o instanceof Variable)
			&& name.equals(((Variable)o).name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 * 
	 * Two equal variables, in the sense of #equals(Object), must have the same
	 * hash code.
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		if (VerboseLevel.DEBUG.mayPrint())
			return "(" + type + ")" + "[" + use + "]" + name;
		else
			return name;
	}

	/**
	 * @return the root of the variable's name, i.e. without the SSA suffix.
	 */
	public String root() {
		return root(name);
	}

	/**
	 * @return the root of the variable's name, i.e. without the SSA suffix.
	 */
	public static String root(String name) {
		int last = name.lastIndexOf('_');
		return name.substring(0, last);
	}
	 
	/**
	 * @return the SSA number attributed to this variable.
	 */
	public int ssaNumber() {
		int last = name.lastIndexOf('_');
		return new Integer(name.substring(last+1,name.length()));
	}

	/**
	 * Name of a variable is [global_]method_Callnb_sourceName_SSAnb.
	 * 
	 * @return the name of this variable as it was in the source code (without prefix and SSA number).
	 */
	public String sourceName() {
		String root = root(name);
		int first = root.indexOf('_');
		int second = root.indexOf('_', first+1);
		// variable which is used in a function to represent
		// a global variable
		if (root.substring(0, first).equals("global")) {
			int third = root.indexOf('_', second+1);
			return root.substring(third+1);
		}
		else {
			return root.substring(second+1);
		}
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		int first = name.indexOf('_');
		int second = name.indexOf('_', first+1);
		if (second==-1) {
			System.err.println("problem when getting the name of a variable in a function call");
			System.exit(0);
		}
		// variable which is used in a function to represent
		// a global variable
		if (name.substring(0, first).equals("global") && name.substring(first + 1, second).equals(functionName)) {
			int third = name.indexOf('_', second+1);
			return new Variable("global_" + prefix+name.substring(third+1),type,use);
		}
			
		if (name.substring(0, first).equals(functionName)) {
			return new Variable(prefix+name.substring(second+1),type,use);
		}
		return this;
	}
	
	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		Expression subst = substitutions.get(this);
		if (subst != null) {
			return subst;
		}
		else {
			return this;
		}
	}

}
