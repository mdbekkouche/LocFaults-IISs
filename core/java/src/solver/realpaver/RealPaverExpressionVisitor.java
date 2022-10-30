package solver.realpaver;

import solver.realpaver.xml.RealPaverXmlCsp;
import validation.Validation.VerboseLevel;
import expression.Expression;
import expression.ParenExpression;
import expression.MethodCall;
import expression.SimpleArrayExpressionVisitor;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.JMLAllDiff;
import expression.logical.InfEqualExpression;
import expression.logical.NondetAssignment;
import expression.logical.SupEqualExpression;
import expression.logical.SupExpression;
import expression.logical.InfExpression;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.logical.AndExpression;
import expression.logical.OrExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.numeric.MinusExpression;
import expression.numeric.NumericExpression;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

/**
 * Creates <pre>Real Paver</pre> constraints from CPBPV ones.
 * The constraints are expressed as Strings in the real paver input format.
 * Real paver constraint grammar does not support for strict comparison operators, 
 * however we approximate here the constraint by the corresponding non strict operator.
 * Constraints with negation of equality operator are ignored.
 *  
 * @author Olivier Ponsini
 *
 */
public class RealPaverExpressionVisitor extends SimpleArrayExpressionVisitor {
	
	public RealPaverExpressionVisitor(RealPaverXmlCsp csp) {
		super("Real Paver", csp);
	}

	//--------------------------------------------------------------
	// Build new constraints.
	// The new constraints are returned as Strings.
	//--------------------------------------------------------------
	
	public String visit(Assignment assignment) {
		String var = assignment.lhs().name();
		String rightExpr = (String)assignment.rhs().structAccept(this);
		return var + " = " + rightExpr;
	}

	public String visit(AndExpression expression) {
		StringBuilder ctrs = new StringBuilder((String)expression.arg1().structAccept(this));
		ctrs.append(",\n");
		ctrs.append((String)expression.arg2().structAccept(this));			
		return ctrs.toString();
	}
	
	
	/* 
	 * Strict operators are relaxed to non strict ones.
	 * 
	 * @see expression.ExpressionVisitor#visit(expression.logical.Comparator)
	 */
	public String visit(Comparator expression) {
		String leftExpr = (String)expression.arg1().structAccept(this);
		String rightExpr = (String)expression.arg2().structAccept(this);
		StringBuilder ctr = new StringBuilder(leftExpr);
		switch(expression.opCode()) {
		case LT:
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("Warning: Real Paver replaces strict inequalities " +
				     	           "by non strict ones!"); 
			}
		case LEQ:
			ctr.append(" <= ");
			break;
		case GT:
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("Warning: Real Paver replaces strict inequalities " +
								   "by non strict ones!");
			}
		case GEQ:
			ctr.append(" >= ");
			break;
		case EQU:
			ctr.append(" = ");
			break;
		default:
			System.err.println("Error: Real Paver does not support this operator (" 
					+ expression.opCode() 
					+ ")!");
			System.exit(-1);
		}
		ctr.append(rightExpr);
		return ctr.toString();
	}

	private String negateComparator(Comparator cmp) {
		Expression a1 = cmp.arg1();
		Expression a2 = cmp.arg2();
		switch (cmp.opCode()) {
		case LT:
			return (String)new SupEqualExpression(a1, a2).structAccept(this);
		case GT: 
			return (String)new InfEqualExpression(a1, a2).structAccept(this);
		case LEQ: 
			return (String)new SupExpression(a1, a2).structAccept(this);
		case GEQ: 
			return (String)new InfExpression(a1, a2).structAccept(this);
		case EQU:  //return an empty string so that constraint will be ignored
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("Warning: Real Paver ignores 'not equals' constraints!"); 
			}
			return new String("0 = 0"); 
		default:
			System.err.println("Error: Real Paver cannot negate this operator (" + cmp + ")!");
			System.exit(-1);
			return null;
		}
	}

	public String visit(NotExpression expression) {
		LogicalExpression a1 = expression.arg1();
		if (a1 instanceof NotExpression) {
			return (String)((NotExpression)a1).arg1().structAccept(this);
		}
		else if (a1.isComparison()) {
			return negateComparator((Comparator)a1);
		}
		else if (a1 instanceof ParenExpression) {
			//We assume well-formed Java expressions: the not operator argument is a LogicalExpression
			LogicalExpression e = (LogicalExpression)((ParenExpression)a1).arg1();
			return (String)new NotExpression(e).structAccept(this);			
		}
		else if (a1 instanceof OrExpression) {
			OrExpression o = (OrExpression)a1;
			AndExpression a = new AndExpression(new NotExpression(o.arg1()), 
												new NotExpression(o.arg2()));
			return (String)a.structAccept(this);
		} 
		else if (a1 instanceof JMLForAllExpression) {
			JMLForAllExpression e = (JMLForAllExpression)a1;
			//If the bound is enumerable, it is not mandatory to negate it, but in the other case, 
			//we need to expand the implicit implication and negate it as a whole.
			if(e.enumeration() != null)
				return (String)(new JMLExistExpression(
								e.index(), 
								e.boundExpression(), 
								new NotExpression(e.condition()))).structAccept(this);
			else //TODO: Normally, at this level, a non enumerable quantifier could already be 
				 //       in the form (var ; true ; body) so we could just take the negation of body 
				 //       (it would then be useless to create this new 'and' expression).
				return (String)(new JMLExistExpression(
								e.index(), 
								new LogicalLiteral(true), 
								new AndExpression(
										e.boundExpression(), 
										new NotExpression(e.condition())))).structAccept(this);
		}  
		else if (a1 instanceof JMLExistExpression) {
			JMLForAllExpression e = (JMLForAllExpression)a1;
			//If the bound is enumerable, it is not mandatory to negate it, but in the other case, 
			//we need to expand the implicit implication and negate it as a whole.
			if(e.enumeration() != null)
				return (String)(new JMLForAllExpression(
								e.index(), 
								e.boundExpression(), 
								new NotExpression(e.condition()))).structAccept(this);
			else //TODO: Normally, at this level, a non enumerable quantifier could already be 
				 //       in the form (var ; true ; body) so we could just take the negation of body 
				 //       (it would then be useless to create this new 'and' expression).
				return (String)(new JMLForAllExpression(
								e.index(), 
								new LogicalLiteral(true), 
								new OrExpression(
										new NotExpression(e.boundExpression()), 
										new NotExpression(e.condition())))).structAccept(this);				
		}
		else if (a1 instanceof AndExpression) {
			System.err.println("Error: Negating a logical AND operator "
					+ "should create an OR operator which is not supported with Real Paver!");
			System.exit(44);
			return null;
		} 
		else {
			System.err.println("Error: Negating this expression: " 
					+ a1.getClass()
					+ " is not supported with Real Paver!");			
			System.exit(45);
			return null;
		}
	}
	
	public String visit(IntegerLiteral literal) {
		return literal.literal();
	}

	public String visit(FloatLiteral literal) {
		//This is used instead of literal.literal() because it ensures the format
		//will contain a decimal point as expected by Real Paver
		return literal.constantNumber().toString();
	}

	public String visit(DoubleLiteral literal) {
		//This is used instead of literal.literal() because it ensures the format
		//will contain a decimal point as expected by Real Paver
		return literal.constantNumber().toString();
	}

	public String visit(LogicalLiteral literal) {
		StringBuilder ctr = new StringBuilder("0");
		if (literal.constantBoolean()) {
			ctr.append(" <= ");
		}
		else {
			ctr.append(" >= ");			
		}
		ctr.append("1");
		return ctr.toString();
	}

	public String visit(Variable variable) {
		return variable.name();
	}

	/* Arithmetic expressions must be correctly parenthesized: here we chose to 
	 * parenthesize each arithmetic operation.
	 * @see expression.ExpressionVisitor#visit(expression.numeric.BinaryExpression)
	 */
	public String visit(BinaryExpression expression) {
		StringBuilder ctr = new StringBuilder("(");
		ctr.append((String)expression.arg1().structAccept(this));
		switch(expression.opCode()) {
		case ADD:
			ctr.append(" + ");
			break;
		case SUB:
			ctr.append(" - ");
			break;
		case MUL:
			ctr.append(" * ");
			break;
		case DIV:
			ctr.append(" / ");
			break;
		default:
			System.err.println("Error: Real Paver does not support this operator (" 
					+ expression.opCode() 
					+ ")!");
			System.exit(-1);			
		}
		ctr.append((String)expression.arg2().structAccept(this));
		ctr.append(")");
		return ctr.toString();
	}

	public String visit(MethodCall mc) {
		StringBuilder ctr = new StringBuilder();
		if (mc.unqualifiedName().equals("abs")) {	//Absolute value
			ctr.append("max(");
			NumericExpression param = (NumericExpression)mc.parameters().get(0);
			ctr.append((String)param.structAccept(this));
			ctr.append(", ");
			ctr.append((String)new MinusExpression(new IntegerLiteral(0), param).structAccept(this));
			ctr.append(")");
		}
		else if (mc.unqualifiedName().equals("pow")) {	//Power function
			NumericExpression base = (NumericExpression)mc.parameters().get(0);
			NumericExpression expo = (NumericExpression)mc.parameters().get(1);
			ctr.append("exp(");
			ctr.append((String)expo.structAccept(this));
			ctr.append("*log(");
			ctr.append((String)base.structAccept(this));
			ctr.append("))");
		}
		else {
			ctr.append(mc.unqualifiedName());
			ctr.append("(");
			for (Expression param: mc.parameters()) {
				ctr.append((String)param.structAccept(this));
				ctr.append(", ");
			}
			ctr.delete(ctr.length()-2, ctr.length());
			ctr.append(")");
		}
		return ctr.toString();
	}


	//-----------------------------------------
	// Unimplemented methods
	//-----------------------------------------

	public Object visit(JMLForAllExpression expression) {
		System.err.println("Error: the ForAll quantifier is not yet implemented with Real Paver!");
		System.exit(47);
		return null;
	}

	public Object visit(JMLExistExpression expression) {
		System.err.println("Error: the Exist quantifier is not yet implemented with Real Paver!");
		System.exit(47);
		return null;
	}

	public Object visit(ArrayVariable variable) {
		System.err.println("Error: array are not yet implemented with Real Paver!");
		System.exit(47);
		return null;
	}

	public Object visit(OrExpression expression) {		
		System.err.println("Error: logical operator OR not yet implemented with Real Paver solver!");	
		System.exit(41);
		return null;
	}

	public Object visit(JMLImpliesExpression expression) {
		System.err.println("Error: the IMPLY logical operator is not yet implemented with Real Paver!");
		System.exit(47);
		return null;
	}

	public Object visit(JMLAllDiff expression) {
		System.err.println("Error: the AllDiff constraint is not yet implemented with Real Paver!");
		System.exit(47);
		return null;
	}

	@Override
	public Object visit(NondetAssignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object visit(JMLOld expression) {
		return this.visit(expression.oldValue());
	}

}