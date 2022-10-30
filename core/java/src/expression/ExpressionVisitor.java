
package expression;

import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalLiteral;
import expression.logical.AndExpression;
import expression.logical.NondetAssignment;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.IntegerLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.DoubleLiteral;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

/**
 * visiteur
 * @author lydie
 *
 */
public interface ExpressionVisitor {

	Object visit(Assignment assignment);
	
	Object visit(NondetAssignment assignment);

	Object visit(ArrayAssignment assignment);

	Object visit(ParenExpression expression);

	Object visit(Variable variable);

	Object visit(ArrayVariable variable);

	Object visit(ArrayElement tab);

	Object visit(MethodCall mc);

	Object visit(IntegerLiteral literal);

	Object visit(FloatLiteral literal);
	
	Object visit(DoubleLiteral literal);

	Object visit(LogicalLiteral literal);
	
	Object visit(BinaryExpression expression);

	Object visit(Comparator expression);
	
	Object visit(AndExpression expression);

	Object visit(OrExpression expression);

	Object visit(NotExpression expression);
	
	Object visit(JMLImpliesExpression expression);

	Object visit(JMLForAllExpression expression);

	Object visit(JMLExistExpression expression);
	
	Object visit(JMLAllDiff expression);
	
	Object visit(JMLOld expression);

}
