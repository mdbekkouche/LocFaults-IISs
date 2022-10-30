package CFG.simplification;

import java.util.TreeSet;

import expression.Expression;
import expression.ExpressionVisitor;
import expression.MethodCall;
import expression.ParenExpression;
import expression.logical.AndExpression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NondetAssignment;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

/**
 * class to visit an expression and to collect the set of its variables
 * 
 * @author helen
 *
 */
public class VariableCollector implements ExpressionVisitor {

		
	public Object visit(LogicalExpression constraint) {
		return constraint.structAccept(this);
	}


	public Object visit(Assignment assignment) {
		//System.out.println("dans Assignment dans Variable collector " + assignment);
		Variable var = assignment.lhs();
		TreeSet<Variable> av = (TreeSet<Variable>)assignment.rhs().structAccept(this);
//		if (!av.isEmpty()) {
//			av.add(var);
////			usefulVar.add(var);
//		}

			av.add(var);
//			usefulVar.add(var);

		return av;
	}
	
	@Override
	public Object visit(NondetAssignment assignment) {
		TreeSet<Variable> av = new TreeSet<Variable>();
		av.add(assignment.lhs());
		return av;
	}


	public Object visit(ArrayAssignment assignment) {
		//System.err.println("dans ArrayAssignment dans Variable collector");
		Variable var = assignment.lhs();
		TreeSet<Variable> av = (TreeSet<Variable>)assignment.rhs().structAccept(this);
		av.add(var);
		av.add(assignment.previousArray());
		return av;
	}


	public Object visit(ParenExpression expression) {
		return expression.arg1().structAccept(this);
	}


	public Object visit(Variable variable) {
		TreeSet<Variable> av = new TreeSet<Variable>();
//		if (usefulVar.contains(variable)) 
		av.add(variable) ;
		return av;
	}


	public Object visit(ArrayVariable variable) {
		// should not be called because assignments between arrays are not handled (i.e; a=b)
		TreeSet<Variable> av = new TreeSet<Variable>();
		av.add(variable) ;
		return av;
	}


	public Object visit(ArrayElement tab) {
		TreeSet<Variable> av = new TreeSet<Variable>();
		av.add(tab.array());
		TreeSet<Variable> avIndex = (TreeSet<Variable>)tab.index().structAccept(this);
		if (avIndex!=null)
			av.addAll(avIndex);
		return av;
	}


	public Object visit(IntegerLiteral literal) {
		return new TreeSet<Variable>();
	}


	public Object visit(FloatLiteral literal) {
		return new TreeSet<Variable>();
	}


	public Object visit(DoubleLiteral literal) {
		return new TreeSet<Variable>();
	}


	public Object visit(LogicalLiteral literal) {
		return new TreeSet<Variable>();
	}

	public Object visit(MethodCall mc) {
		TreeSet<Variable> av = new TreeSet<Variable>();
		for (Expression param: mc.parameters()) {
			av.addAll((TreeSet<Variable>)param.structAccept(this));
		}
		return av;
	}

	public Object visit(BinaryExpression expression) {
		TreeSet<Variable> av1 = (TreeSet<Variable>)expression.arg1().structAccept(this);
		TreeSet<Variable> av2 = (TreeSet<Variable>)expression.arg2().structAccept(this);
		av1.addAll(av2);
		return av1;
	}

	public Object visit(Comparator expression) {
		TreeSet<Variable> av1 = (TreeSet<Variable>)expression.arg1().structAccept(this);
		//System.out.println("dans comparator 1 " + expression.arg1() + " " + av1);		
		TreeSet<Variable> av2 = (TreeSet<Variable>)expression.arg2().structAccept(this);
		//System.out.println("dans comparator 2 " + expression.arg2() + " " + av2);		
		av1.addAll(av2);
		return av1;
	}

	public Object visit(AndExpression expression) {
		TreeSet<Variable> av1 = (TreeSet<Variable>)expression.arg1().structAccept(this);
		TreeSet<Variable> av2 = (TreeSet<Variable>)expression.arg2().structAccept(this);
		av1.addAll(av2);
		return av1;	
	}

	public Object visit(OrExpression expression) {
		TreeSet<Variable> av1 = (TreeSet<Variable>)expression.arg1().structAccept(this);
		TreeSet<Variable> av2 = (TreeSet<Variable>)expression.arg2().structAccept(this);
		av1.addAll(av2);
		return av1;	
	}


	public Object visit(NotExpression expression) {
		return expression.arg1().structAccept(this);
	}


	public Object visit(JMLImpliesExpression expression) {
//		System.out.println(expression.arg1());
		TreeSet<Variable> av1 = (TreeSet<Variable>)expression.arg1().structAccept(this);
//		System.out.println("dans JMLImplies " + av1);
		TreeSet<Variable> av2 = (TreeSet<Variable>)expression.arg2().structAccept(this);
		av1.addAll(av2);
		return av1;
	}


	public Object visit(JMLForAllExpression expression) {
		TreeSet<Variable> av = (TreeSet<Variable>)expression.index().structAccept(this);
		av.addAll((TreeSet<Variable>)expression.boundExpression().structAccept(this));
		av.addAll((TreeSet<Variable>)expression.condition().structAccept(this));
		return av;
	}


	public Object visit(JMLExistExpression expression) {
		TreeSet<Variable> av = (TreeSet<Variable>)expression.index().structAccept(this);
		av.addAll((TreeSet<Variable>)expression.boundExpression().structAccept(this));
		av.addAll((TreeSet<Variable>)expression.condition().structAccept(this));
		return av;
	}


	public Object visit(JMLAllDiff expression) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object visit(JMLOld expression) {
		return expression.oldValue().structAccept(this);
	}


}
