package expression.variables;

import expression.ExpressionVisitor;
import validation.Validation.VerboseLevel;
import validation.util.Type;

/**
 * Represents an array type variable.
 * The type of the array elements can be boolean, int, float, or double.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public class ArrayVariable extends Variable implements Cloneable {

	private int length; // number of element
	
	/* a new array variable */
	public ArrayVariable(String n, Type t, int l, Use u) {
		super(n,t,u);
		length = l;
	}

	public ArrayVariable(ArrayVariable v) {
		super(v);
		length = v.length;
	}

	/* a new array variable */
	public ArrayVariable(String n, Type t, int l) {
		this(n, t, l, Use.LOCAL);
	}

	
	public int length() {
		return length;
	}


	@Override
	public boolean equals(Object o){
		if (!(o instanceof ArrayVariable))
			return false;
		ArrayVariable v = (ArrayVariable)o;
		return super.equals(o) && (length==v.length);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + length ;
	}

	@Override
	public String toString() {
		if (VerboseLevel.DEBUG.mayPrint())
			return "(" + type + "[" + length + "]" + ")" + name;
		else
			return name;
	}

	/* lydie */   
	public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	public ArrayVariable computeLastRenaming(SymbolTable varr) {
		//System.out.println("array var last renaming " + varr.get(root()));
		return (ArrayVariable) varr.get(root());
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
	public ArrayVariable clone() {
		return new ArrayVariable(this);
	}

}
