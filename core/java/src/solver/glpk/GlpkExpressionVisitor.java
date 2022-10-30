package solver.glpk;

import java.util.ArrayList;

import validation.system.xml.ValidationCSP;

import validation.util.OpCode;
import validation.util.LPMatrixRow;

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
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.logical.AndExpression;
import expression.logical.OrExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.PlusExpression;
import expression.numeric.MinusExpression;
import expression.numeric.TimeExpression;
import expression.numeric.DivExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

import exception.AnalyzeException;

/**
 * Creates <code>GLPK</code> LP matrix rows from CPBPV constraints.
 * 
 * @author Olivier Ponsini
 *
 */
public class GlpkExpressionVisitor extends SimpleArrayExpressionVisitor {
	
	private GlpkVarBlock varBlock;
	
	public GlpkExpressionVisitor(ValidationCSP csp) {
		super("GLPK", csp);
		this.varBlock = (GlpkVarBlock)csp.varBlock;
	}

	/**
	 * @return The number of variables in the CSP.
	 */
	public int varSize() {
		return varBlock.size();
	}
	//--------------------------------------------------------------
	// Build new constraints.
	// The new constraints are returned as GLPK matrix rows. 
	//--------------------------------------------------------------
	
	public ArrayList<LPMatrixRow> visit(AndExpression and) {
		ArrayList<LPMatrixRow> rows = new ArrayList<LPMatrixRow>();
		Object res;

		// arg1 could itself be an AND operator and return an array of constraints
		res = and.arg1().structAccept(this);
		if (res instanceof LPMatrixRow) {
			rows.add((LPMatrixRow)res);
		}
		else { // res must be an ArrayList<LPMatrixRow>
			rows.addAll((ArrayList<LPMatrixRow>)res);
		}

		// arg2 could itself be an AND operator and return an array of constraints
		res = and.arg2().structAccept(this);			
		if (res instanceof LPMatrixRow) {
			rows.add((LPMatrixRow)res);
		}
		else { // res must be an ArrayList<LPMatrixRow>
			rows.addAll((ArrayList<LPMatrixRow>)res);
		}
		
		return rows;
	}

	public LPMatrixRow visit(Assignment assignment) {
		//There won't be more coefficients than the total number of 
		//variables 
		LPMatrixRow row = new LPMatrixRow(varBlock.size());
		
		//This row is an equality
		try {
			row.setRelOperator(OpCode.EQU);
		} catch (AnalyzeException e) {
			e.printStackTrace();
			System.exit(42);
		}
		
		//Gets the column of the assigned variable; its coefficient is always 1.0
		int col = varBlock.concreteVar(assignment.lhs());
		row.setCoeff(col, 1.0);
		
		//We now consider the right-hand side and merge its coefficients in 'row'
		//We ensure we add the opposite values of the rhs to the lhs.
		LPMatrixRow rhs = (LPMatrixRow)assignment.rhs().structAccept(this);
		rhs.oppositeCoeff();
		row.add(rhs);
		
		return row;
	}

	public LPMatrixRow visit(Comparator cmp) {
		LPMatrixRow lhs = (LPMatrixRow)cmp.arg1().structAccept(this);
		LPMatrixRow rhs = (LPMatrixRow)cmp.arg2().structAccept(this);
		//We now merge the right-hand side coefficients and bound with those of 
		//the left-hand side of the (in)equality.
		//We ensure we add the opposite values of the rhs to the lhs, and the opposite
		//bound of the lhs to that of the rhs.
		lhs.oppositeBound();
		rhs.oppositeCoeff();
		lhs.add(rhs);
		
		//Set the relational operator
		try {
			lhs.setRelOperator(cmp.opCode());
		} catch (AnalyzeException e) {
			e.printStackTrace();
			System.exit(42);
		}
		
		return lhs;
	}

	private Object negateComparator(Comparator cmp) {
		Expression a1 = cmp.arg1();
		Expression a2 = cmp.arg2();
		switch (cmp.opCode()) {
		case LT:
			return new SupEqualExpression(a1,a2).structAccept(this);
		case GT:
			return new InfEqualExpression(a1,a2).structAccept(this);
		default:
			System.err.println("Error (negateComparator): operator (" + cmp + ") can not be negated with GLPK!");
			System.exit(43);
			return null;
		}
	}

	public Object visit(NotExpression expression) {
		LogicalExpression a1 = expression.arg1();
		if (a1 instanceof NotExpression) {
			return ((NotExpression)a1).arg1().structAccept(this);
		}
		else if (a1.isComparison()) {
			return negateComparator((Comparator)a1);
		}
		else if (a1 instanceof ParenExpression) {
			//We assume well-formed Java expressions: the not operator argument is a LogicalExpression
			LogicalExpression e = (LogicalExpression)((ParenExpression)a1).arg1();
			return new NotExpression(e).structAccept(this);			
		}
		else if (a1 instanceof OrExpression) {
			OrExpression o = (OrExpression)a1;
			AndExpression a = new AndExpression(new NotExpression(o.arg1()), 
												new NotExpression(o.arg2()));
			return a.structAccept(this);
		} 
		else if (a1 instanceof JMLForAllExpression) {
			JMLForAllExpression e = (JMLForAllExpression)a1;
			//If the bound is enumerable, it is not mandatory to negate it, but in the other case, 
			//we need to expand the implicit implication and negate it as a whole.
			if(e.enumeration() != null)
				return (new JMLExistExpression(
								e.index(), 
								e.boundExpression(), 
								new NotExpression(e.condition()))).structAccept(this);
			else //TODO: Normally, at this level, a non enumerable quantifier could already be 
				 //       in the form (var ; true ; body) so we could just take the negation of body 
				 //       (it would then be useless to create this new 'and' expression).
				return (new JMLExistExpression(
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
				return (new JMLForAllExpression(
								e.index(), 
								e.boundExpression(), 
								new NotExpression(e.condition()))).structAccept(this);
			else //TODO: Normally, at this level, a non enumerable quantifier could already be 
				 //       in the form (var ; true ; body) so we could just take the negation of body 
				 //       (it would then be useless to create this new 'or' expression).
				return (new JMLForAllExpression(
								e.index(), 
								new LogicalLiteral(true), 
								new OrExpression(
										new NotExpression(e.boundExpression()), 
										new NotExpression(e.condition())))).structAccept(this);				
		}
		else if (a1 instanceof AndExpression) {
			System.err.println("Error: Negating a logical AND operator "
					+ "should create an OR operator which is not supported with GLPK!");
			System.exit(44);
			return null;
		} 
		else {
			System.err.println("Error: Negating this expression: " 
					+ a1.getClass()
					+ " is not supported with GLPK!");			
			System.exit(45);
			return null;
		}
	}

	public LPMatrixRow visit(IntegerLiteral literal) {
		LPMatrixRow row = new LPMatrixRow(varBlock.size());
		row.setBound(literal.constantNumber().doubleValue());
		return row;
	}

	public LPMatrixRow visit(FloatLiteral literal) {
		LPMatrixRow row = new LPMatrixRow(varBlock.size());
		row.setBound(literal.constantNumber().doubleValue());
		return row;
	}

	public LPMatrixRow visit(DoubleLiteral literal) {
		LPMatrixRow row = new LPMatrixRow(varBlock.size());
		row.setBound(literal.constantNumber().doubleValue());
		return row;
	}

	public LPMatrixRow visit(LogicalLiteral literal) {
		if (literal.constantBoolean()) {
			return new LPMatrixRow(0);
		}
		else {
			LPMatrixRow incoherent = new LPMatrixRow(0);
			incoherent.setBound(1.0);
			try {
				incoherent.setRelOperator(OpCode.EQU);
			} catch (AnalyzeException e) {
				//Should not occur
			}
			return incoherent;
		}
	}

	public LPMatrixRow visit(Variable variable) {
		LPMatrixRow row = new LPMatrixRow(varBlock.size());
		row.setCoeff(varBlock.concreteVar(variable), 1.0);
		return row;
	}

	public LPMatrixRow visit(BinaryExpression expression) {
		switch (expression.opCode()) {
		case ADD:
			return visitPlus((PlusExpression)expression);
		case SUB:
			return visitMinus((MinusExpression)expression);
		case MUL:
			return visitTime((TimeExpression)expression);
		case DIV:
			return visitDiv((DivExpression)expression);
		default:
			System.err.println("Error: the " + 
								expression.opCode() + 
								" binary operator is not yet implemented with GLPK!");
			System.exit(47);
			return null;
		}
	}

	public LPMatrixRow visitPlus(PlusExpression expression) {
		LPMatrixRow arg1 = (LPMatrixRow)expression.arg1().structAccept(this);
		LPMatrixRow arg2 = (LPMatrixRow)expression.arg2().structAccept(this);
		
		arg1.add(arg2);
		return arg1;
		
//		//arg1 and arg2 
//		if (arg1.nbCoeff != 0) {
//			if (arg2 instanceof LPMatrixRow) {
//				((LPMatrixRow)arg1).add((LPMatrixRow)arg2);
//			}
//			else { //arg2 is a Double, we add it to the bound of arg1
//				((LPMatrixRow)arg1).add((Double)arg2);
//			}
//			return arg1;
//		}
//		else { //arg1 is a Double
//			if (arg2 instanceof LPMatrixRow) {
//				//arg2 is a row, we add arg1 to its bound
//				((LPMatrixRow)arg2).add((Double)arg1);
//				return arg2;
//			}
//			else { //arg1 and arg2 are Double
//				return Double.valueOf((Double)arg1 + (Double)arg2);
//			}
//		}
	}
	
	public LPMatrixRow visitMinus(MinusExpression expression) {
		LPMatrixRow arg1 = (LPMatrixRow)expression.arg1().structAccept(this);
		LPMatrixRow arg2 = (LPMatrixRow)expression.arg2().structAccept(this);
		
		arg2.opposite();
		arg1.add(arg2);
		return arg1;
		
//		//arg1 and arg2 can either be a constant (Double) or a partial 
//		//matrix row (LPMatrixRow)
//		if (arg1 instanceof LPMatrixRow) {
//			if (arg2 instanceof LPMatrixRow) {
//				LPMatrixRow oppositeArg2 = (LPMatrixRow)arg2;
//				oppositeArg2.opposite();
//				((LPMatrixRow)arg1).add(oppositeArg2);
//			}
//			else { //arg2 is a Double, we add its opposite to the bound of arg1
//				((LPMatrixRow)arg1).add(-(Double)arg2);
//			}
//			return arg1;
//		}
//		else { //arg1 is a Double
//			if (arg2 instanceof LPMatrixRow) {
//				//arg2 is a row, we build the opposite row and add arg1 to its bound
//				LPMatrixRow oppositeArg2 = (LPMatrixRow)arg2;
//				oppositeArg2.opposite();
//				oppositeArg2.add((Double)arg1);
//				return oppositeArg2;
//			}
//			else { //arg1 and arg2 are Double
//				return Double.valueOf((Double)arg1 - (Double)arg2);
//			}
//		}
	}
	
	public LPMatrixRow visitTime(TimeExpression expression) {
		LPMatrixRow arg1 = (LPMatrixRow)expression.arg1().structAccept(this);
		LPMatrixRow arg2 = (LPMatrixRow)expression.arg2().structAccept(this);
		
		if (arg1.constant()) {
			arg2.product(arg1.bound());
			return arg2;
		}
		else {
			if (arg2.constant()) {
				arg1.product(arg2.bound());
				return arg1;
			}
			else {
				//Rows product is not linear...
				System.err.println("Error (GLPK): trying to multiply two LP matrix rows, "
						           + "this is not linear!");
				System.exit(46);
				return null;
			}
		}
	}
//			else { //arg2 is a Double, we can do the product of a row by a scalar
//				((LPMatrixRow)arg1).product((Double)arg2);
//			}
//			return arg1;
//		}
//		else { //arg1 is a Double
//			if (arg2 instanceof LPMatrixRow) {
//				//arg2 is a row, we can do the product of a row by a scalar
//				((LPMatrixRow)arg2).product((Double)arg1);
//				return arg2;
//			}
//			else { //arg1 and arg2 are Double
//				return Double.valueOf((Double)arg1 * (Double)arg2);
//			}
//		}		
//	}
	
	public LPMatrixRow visitDiv(DivExpression expression) {
		LPMatrixRow arg1 = (LPMatrixRow)expression.arg1().structAccept(this);
		LPMatrixRow arg2 = (LPMatrixRow)expression.arg2().structAccept(this);

		if (arg2.constant()) {
			arg1.divide(arg2.bound());
			return arg1;
		}
		else {
			//Row division is not linear...
			System.err.println("Error (GLPK): trying to inverse an LP matrix row (" 
							    + expression + "). "
								+ "This is not linear!");
			System.exit(46);
			return null;
		}
	}
	
	//-----------------------------------------
	// Unimplemented methods
	//-----------------------------------------
	
	public Object visit(JMLForAllExpression expression) {
		System.err.println("Error: the ForAll quantifier is not yet implemented with GLPK!");
		System.exit(47);
		return null;
	}

	public Object visit(JMLExistExpression expression) {
		System.err.println("Error: the Exist quantifier is not yet implemented with GLPK!");
		System.exit(47);
		return null;
	}

	public Object visit(ArrayVariable variable) {
		System.err.println("Error: unexpected ArrayVariable while visiting an expression with GLPK!");
		System.exit(47);
		return null;
	}

	public Object visit(JMLAllDiff expression) {
		System.err.println("Error: the AllDiff constraint is not yet implemented with GLPK!");
		System.exit(47);
		return null;
	}

	public Object visit(JMLImpliesExpression expression) {
		System.err.println("Error: the JMLImplies logical operator is not yet implemented with GLPK!");
		System.exit(47);
		return null;
	}

	public Object visit(OrExpression expression) {
		System.err.println("Error: the OR logical operator is not yet implemented with GLPK!");
		System.exit(47);
		return null;
	}

	public Object visit(MethodCall mc) {
		System.err.println("Error : Method calls not allowed with GLPK !");
		System.err.println(mc);
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