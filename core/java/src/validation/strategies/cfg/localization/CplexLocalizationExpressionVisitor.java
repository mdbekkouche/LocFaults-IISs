package validation.strategies.cfg.localization;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;

import java.util.ArrayList;

import solver.cplex.CplexExpressionVisitor;
import solver.cplex.CplexIntArrayVarBlock;
import solver.cplex.CplexIntVarBlock;
import validation.util.Type;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.numeric.IntegerLiteral;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.Variable;


/**
 * Expression visitor for Cplex when doing localization.
 * It specializes CplexExpressionVisitor when handling array elements : 
 * localization is based on a counterexample, so indices of array accesses are always known.
 * 
 *   It also simplifies JMLImplies expressions to handle out-of-bound array accesses in 
 *   specifications like in 'x != -1 ==> t[x] == e'.
 * 
 * @author Mohammed Bekkouche
 *
 */
public class CplexLocalizationExpressionVisitor extends CplexExpressionVisitor {
	
	private RhsExpressionComputer rhsComputer;
	
	public CplexLocalizationExpressionVisitor(CplexIntVarBlock i, CplexIntArrayVarBlock t, RhsExpressionComputer rhsComputer) {
		super(i,t);
		this.rhsComputer = rhsComputer;
	}	

	
	private ArrayList<IloConstraint> arrayCopy(ArrayVariable dest, ArrayVariable src, int start, int end) 
		throws IloException 
	{
		ArrayList<IloConstraint> ctrs = new ArrayList<IloConstraint>();
		
		for (int i = start; i < end; i++) {
			IloIntVar eltSrc = intVar.get(src.name() + '[' + i + ']');
			IloIntVar eltDest = intVar.get(dest.name() + '[' + i + ']');
			ctrs.add(solver.eq(eltDest, eltSrc));
		}
		return ctrs;
	}
	
	public IloConstraint visit(Assignment assignment) {
       	if (fail)
			return null;
   		
       	IloConstraint c = null;
   	       	
    	if (assignment.rhs() instanceof ArrayVariable) { // Array copy
    		try {
    			ArrayVariable arrayDest = (ArrayVariable)assignment.lhs();
    			ArrayList<IloConstraint> ctrs = arrayCopy(arrayDest, (ArrayVariable)assignment.rhs(), 0, arrayDest.length());
    			c = ctrs.get(0);
    			for (int i = 1; i < ctrs.size(); i++) 
    				c = solver.and(c, ctrs.get(i));
    		} catch (IloException e) {
    			error(1);
    			e.printStackTrace();
    		}
    	}
    	else {
    		Variable var = assignment.lhs();
    		IloIntExpr cvar = intVar.getCplexVar(var);
    		IloIntExpr val = (IloIntExpr)assignment.rhs().structAccept(this);

    		//val may be an array element and thus be null with fail set to true
    		if(fail)
    			return null;

    		try {
    			if (var.type() == Type.BOOL) {
    				c = solver.ifThen((IloConstraint)val, solver.eq(cvar, this.cplexCstInt1));
    				c = solver.and(c, solver.ifThen(solver.not((IloConstraint)val), solver.eq(cvar, this.cplexCstInt0)));
    			}
    			else {
    				c = solver.eq(cvar, val);
    			}
    		} catch (IloException e) {
    			error(1);
    			e.printStackTrace();
    		}
    	}
    	
		return c;
	}

	
	public IloConstraint visit(ArrayAssignment ass) {
       	if (fail)
			return null;
       	
		IntegerLiteral indexExpr = (IntegerLiteral)ass.index().structAccept(rhsComputer);
		IloIntVar arrayElt = intVar.get(ass.lhs().name() + '[' + indexExpr.literal() + ']');
		IloIntExpr val = (IloIntExpr)ass.rhs().structAccept(this);
		
		if(fail)
			return null;

		ArrayList<IloConstraint> ctrs;
		IloConstraint c = null;
		try {
			if (ass.lhs().type() == Type.BOOL) {
				c = solver.ifThen((IloConstraint)val, solver.eq(arrayElt, this.cplexCstInt1));
				c = solver.and(c, solver.ifThen(solver.not((IloConstraint)val), solver.eq(arrayElt, this.cplexCstInt0)));
			}
			else {
				c = solver.eq(arrayElt, val);
			}
			int index = indexExpr.constantNumber().intValue();
			ctrs = arrayCopy(ass.array(), ass.previousArray(), 0, index);
			for (int i = 0; i < ctrs.size(); i++)
				c = solver.and(c, ctrs.get(i));
			ctrs = arrayCopy(ass.array(), ass.previousArray(), index + 1, ass.array().length());
			for (int i = 0; i < ctrs.size(); i++)
				c = solver.and(c, ctrs.get(i));		
			//ctrs.clear();
			//ctrs.add(c);
			return c;
		} catch (IloException e) {
			error(1);
			e.printStackTrace();
		}
		
		return null;
	}
	
	public IloIntVar visit(ArrayElement elt) {
		if (fail)
			return null;

		IntegerLiteral indexExpr = (IntegerLiteral)elt.index().structAccept(rhsComputer);
		return intVar.get(elt.array().name() + '[' + indexExpr.literal() + ']');
	}
		
	// Implicant is false, so implication is trivially true. Thus, this implication does 
	// not participate in the csp unsatisfiability. We could keep the implication, but 
	// sometimes the implicated expression contain array accesses that are out-of-bound if 
	// the implicant is false (e.g. consider the following CSP {x==-1; x !=-1 ==> t[x] = e}).
	// To avoid this, we replace the implication by a true constraint, which gives a logically
	// equivalent CSP.
	public IloConstraint visit(JMLImpliesExpression jmlImplies) {
		IloConstraint result = null;
		LogicalLiteral implicant = (LogicalLiteral)jmlImplies.arg1().structAccept(rhsComputer);
		if (implicant.constantBoolean()) {  // implicant is true, add the jmlImplies constraint
			IloConstraint arg1 = (IloConstraint)(new NotExpression(jmlImplies.arg1())).structAccept(this);
			IloConstraint arg2 = (IloConstraint)jmlImplies.arg2().structAccept(this);
			
			if(fail)
				return null;

			try {
				result = solver.or(arg1, arg2);
			} catch(IloException e) {
				System.err.println("Error while creating an 'or' constraint in "
						+ "Ilog Cplex for expression " + jmlImplies + "!");			
				e.printStackTrace();
				System.exit(37);								
			}
		}
		else { // Implicant is false, replace the implication by a true constraint, which gives a logically
			   // equivalent CSP.
			return this.cplexTrue;
		}
		return result;
	}

}
