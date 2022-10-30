package validation.util;

import exception.AnalyzeException;

/**
 * Operators' code to interface with external solvers.
 * The order of the enumeration corresponds to the code value.
 */
public enum OpCode {
	/**
	 * Less than comparison operator.
	 */
	LT,  
	/**
	 * Less than or equal comparison operator.
	 */
	LEQ, 
	/**
	 * Equal operator.
	 */
	EQU, 
	/**
	 * Greater than or equal comparison operator.
	 */
	GEQ, 
	/**
	 * Greater than comparison operator.
	 */
	GT,
	/**
	 * Numerical addition operator.
	 */
	ADD, 
	/**
	 * Numerical substraction operator.
	 */
	SUB,
	/**
	 * Numerical multiplication operator.
	 */
	MUL,
	/**
	 * Numerical division operator.
	 */
	DIV,
	/**
	 * Logical AND operator.
	 */
	AND,
	/**
	 * Logical OR operator.
	 */
	OR,
	/**
	 * Logical implication operator.
	 */
	IMPLIES,
	/**
	 * Logical negation operator.
	 */
	NOT,
	/**
	 * Not equal operator.
	 */
	NEQ;
	
	/**
	 * Returns the operator code corresponding to the given XML operator name.
	 * 
	 * @param op An XML operator name.
	 * @return The operator code corresponding to the given XML operator name.
	 * @throws AnalyzeException
	 */
	public static OpCode convertFromXML(String op) 
		throws AnalyzeException
	{
		if (op.equals("IDSExprPlus")) return ADD;
		if (op.equals("IDSExprMinus")) return SUB;
		if (op.equals("IDSExprTimes")) return MUL;
		if (op.equals("IDSExprDiv")) return DIV;
		if (op.equals("IDSExprSup")) return GT;
		if (op.equals("IDSExprSupEqual")) return GEQ; 
		if (op.equals("IDSExprInf")) return LT;
		if (op.equals("IDSExprInfEqual")) return LEQ;
		if (op.equals("IDSExprEquality")) return EQU;
		if (op.equals("IDSExprLogicalAnd")) return AND;
		if (op.equals("IDSExprLogicalOr")) return OR;
		if (op.equals("IDSExprJMLImplies")) return IMPLIES;
		if (op.equals("IDSExprLogicalNot")) return NOT;
		
		throw new AnalyzeException("Unknown operator (" + op + ")");
	}
}
