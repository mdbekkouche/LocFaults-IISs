package java2CFGTranslator;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

import expression.Expression;
import expression.logical.AndExpression;
import expression.logical.EqualExpression;
import expression.logical.InfEqualExpression;
import expression.logical.InfExpression;
import expression.logical.LogicalExpression;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.logical.SupEqualExpression;
import expression.logical.SupExpression;
import expression.numeric.DivExpression;
import expression.numeric.NumericExpression;
import expression.numeric.MinusExpression;
import expression.numeric.PlusExpression;
import expression.numeric.TimeExpression;

/**  primitive functions on Java nodes */

public class JavaNodesTools {

	public static boolean isPlus(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.PLUS);
	}
	
	public static boolean isMinus(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.MINUS);
	}
	
	public static boolean isTimes(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.TIMES);
	}
	
	public static boolean isDivide(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.DIVIDE);
	}

	public static boolean isGreater(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.GREATER);
	}
	
	public static boolean isGreaterEqual(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.GREATER_EQUALS);
	}
	
	public static boolean isEqual(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.EQUALS);
	}
	
	public static boolean isNotEqual(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.NOT_EQUALS);
	}
	
	public static boolean isLess(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.LESS);
	}
	
	public static boolean isLessEqual(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.LESS_EQUALS);
	}
	
	public static boolean isOr(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.CONDITIONAL_OR);
	}

	public static boolean isAnd(InfixExpression node){
		return node.getOperator().equals(InfixExpression.Operator.CONDITIONAL_AND);
	}
	
	public static boolean isNot(PrefixExpression node){
		return node.getOperator().equals(PrefixExpression.Operator.NOT);
	}
	
	public static boolean isUnaryMinus(PrefixExpression node){
		return node.getOperator().equals(PrefixExpression.Operator.MINUS);
	}

	// create a unary expression from type node and one argument
	public static Expression buildExpression(PrefixExpression node, Expression first) {
		if (isNot(node)) {
			return new NotExpression((LogicalExpression)first);
		}
		System.err.println("ERREUR");
		return null;
	}

	// create a binary expression from type node and two arguments
	public static Expression buildExpression(InfixExpression node, Expression first, Expression second) {
		// comparison operators
		if (isEqual(node)) {
			return new EqualExpression(first, second);
		}	
		else if (isGreaterEqual(node)){
			return new SupEqualExpression(first, second);
		}
		else if (isGreater(node)){
			return new SupExpression(first, second);
		}
		else if (isLess(node)){
			return new InfExpression(first, second);
		}
		else if (isLessEqual(node)){
			return new InfEqualExpression(first, second);
		}
		// integer operators
		else if (isPlus(node)) {
			return new PlusExpression((NumericExpression)first, (NumericExpression)second);
		}
		else if (isMinus(node)) {
			return new MinusExpression((NumericExpression)first, (NumericExpression)second);
		}
		else if (isTimes(node)) {
			return new TimeExpression((NumericExpression)first, (NumericExpression)second);
		}
		else if (isDivide(node)) {
			return new DivExpression((NumericExpression)first, (NumericExpression)second);
		}
		// or and 
		else if (isOr(node)){
			return new OrExpression((LogicalExpression)first, (LogicalExpression)second);
		}
		else if (isAnd(node)){
			return new AndExpression((LogicalExpression)first, (LogicalExpression)second);
		}
		// not equals
		else if (isNotEqual(node)) {
				return new NotExpression(new EqualExpression(first, second));
		}
		else {
			System.err.println("Error: unknown operator in java expression (" + node +")!");
			System.exit(-1);
			return null;
		}
	}
}

