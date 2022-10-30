package validation.util;

import exception.AnalyzeException;
import exception.VariableValueException;
import expression.Expression;
import expression.logical.LogicalLiteral;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;

/**
 * Enumerates the simple Java types that may be found in analyzed Java programs.
 * 
 * @author Olivier Ponsini
 *
 */
public enum Type {
	BOOL,
	INT,
	FLOAT,
	DOUBLE,
	VOID;
		
	/**
	 * Returns the smallest type suitable to fit a value of any two given types.
	 * This method can be used to compute the type of an arithmetic expression made up 
	 * of binary operators.
	 * 
	 * @param t1 A type. 
	 * @param t2 A type.
	 * @return The smallest type suitable to fit a value of type <pre>t1</pre> or of 
	 *         type <pre>t2</pre>.
	 */
	public static Type mostGeneralType(Type t1, Type t2) {
		//Comparison order flollows from enumeration order: BOOL < INT < FLOAT...
		if (t1.compareTo(t2) > 0)
			return t2;
		else
			return t1;
	}
	
	/**
	 * Returns the enumerated Type object corresponding to the given string 
	 * representation of a type.
	 *  
	 * @param type A string representation of a type (lowercase).
	 * @return The enumerated Type object corresponding to the string 
	 *         representation <pre>type</pre>.
	 */
	public static Type parseType(String type) throws AnalyzeException {
		if (type.equals("boolean")||type.equals("boolean[]")) 
			return BOOL;
		else if (type.equals("int")||type.equals("int[]"))
			return INT;
		else if (type.equals("float")||type.equals("float[]"))
			return FLOAT;
		else if (type.equals("double")||type.equals("double[]"))
			return DOUBLE;
		else if (type.equals("void"))
			return VOID;
		else {
			throw new AnalyzeException("Error (parseType): unknown type (" + type + ")!");
		}
	}

	/**
	 * Returns the interpretation of <code>val</code> as an Object. 
	 * 
	 * @param val The string representation of a numeric or boolean value.
	 * @param t The type of the value.
	 * @return The interpretation of <code>val</code> as a Number or a Boolean according to type <code>t</code>.
	 * @throws VariableValueException
	 */
	public static Object getValueFromString(String val, Type t) 
		throws VariableValueException 
	{
		try {
			switch(t) {
			case BOOL:
				return Boolean.valueOf(val);			
			case INT:
				return Integer.valueOf(val);
			case FLOAT:
			case DOUBLE:
				//The value returned by z3 may be a fraction
				int pos = val.indexOf('/');
				if (pos == -1) {
					//Not a fraction
					return Double.valueOf(val);
				}
				else {
					//It is a fraction
					return Double.valueOf(val.substring(0, pos)) / 
						   Double.valueOf(val.substring(pos+1));
				}
			default:
				throw new VariableValueException("unused type (" + t + ") for a variable!"); 
			}
		} catch(java.lang.NumberFormatException e) {
			throw new VariableValueException(e.toString());
		}
	}

	/**
	 * Returns the interpretation of <code>val</code> as a literal expression. 
	 * 
	 * @param val The string representation of a numeric value.
	 * @param t The type of the value.
	 * @return The interpretation of <code>val</code> as a Number of type <code>t</code>.
	 */
	public static Expression getLiteralFromString(String val, Type t) {
		if (val == null) {
			System.err.println("Error (getLiteralFromString): null value!");
			System.err.println(Thread.currentThread().getStackTrace());
			return null;			
		}
		else {
			switch(t) {
			case INT:
				return new IntegerLiteral(val);
			case FLOAT:
				return new FloatLiteral(val);
			case DOUBLE:
				return new DoubleLiteral(val);
			case BOOL : 
				if (val.equals("0") || val.equalsIgnoreCase("false"))
					return new LogicalLiteral(false);
				return new LogicalLiteral(true);
			default:
				System.err.println("Error (getLiteralFromString): unknown type (" + t + ")!");
				System.err.println(Thread.currentThread().getStackTrace());
				System.exit(-1);
				return null;
			}
		}
	}
	
	public static Class<?>getPrimitiveClass(Type t) {
		switch(t) {
		case BOOL:
			return Boolean.TYPE;
		case INT:
			return Integer.TYPE;
		case FLOAT:
			return Float.TYPE;
		case DOUBLE:
			return Double.TYPE;
		case VOID:
			return Void.TYPE;
		default:
			System.err.println("Error (getPrimitiveClass): Unknown type (" + t + ")!");
			System.exit(-1);
		}
		//Should never be reached...
		return null;
	}
}
