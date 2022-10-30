package validation.util;

import java.util.ArrayList;

import validation.visitor.EnsureCase;

import expression.logical.AndExpression;
import expression.logical.OrExpression;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;

/* une classe pour appliquer diff�rentes strat�gies */
public class Strategy {
	public static String negate(String c) {
		return c;
	}
	public static LogicalExpression negate(LogicalExpression c) {
		return new NotExpression(c);
	}


	public static void disjunctiveNegate(LogicalExpression exp, ArrayList<EnsureCase> result) {
//		System.out.println("add disjunct " + e);

		if (exp instanceof AndExpression) {
			AndExpression a = (AndExpression)exp;
			disjunctiveNegate(((AndExpression)a).arg1(), result);
			disjunctiveNegate(((AndExpression)exp).arg2(), result);
		} 
//		else
//		// take the negation so gives a JMLExists
//		if (e instanceof JMLForAllExpression) {
//			JMLForAllExpression a = (JMLForAllExpression)e;
//			JMLForAllExpression abs = (JMLForAllExpression)abstractE;
//			LogicalExpression expr = a.condition();
//			LogicalExpression abstractExpr = abs.condition();
////			System.out.println("subst " + expr);
//
//			IntegerVariable varIndex = a.index();
//			ArrayList<IntegerLiteral> val = a.enumerate();
//			// TODO : le And des expressions substituées
//			for (int i=a.minBound();i<a.maxBound();i++) {
//				IntegerLiteral valIndex = val.get(i);
//					result.add(new EnsureCase(negateCase((LogicalExpression)expr.substitute(varIndex,valIndex)),negateCase(abs.substitute(varIndex,valIndex))));
//			}
//		}
//		else if (e instanceof JMLExistExpression) {
//			JMLExistExpression a = (JMLExistExpression)e;
//			JMLExistExpression abs = (JMLExistExpression)abstractE;
//			LogicalExpression expr = a.condition();
//			System.out.println("subst " + expr);
//
//			LogicalExpression abstractExpr = abs.condition();
//			IntegerVariable varIndex = a.index();
//			ArrayList<IntegerLiteral> val = a.enumerate();
//			for (int i=a.minBound();i<a.maxBound();i++) {
//				IntegerLiteral valIndex = val.get(i);
//				result.add(new EnsureCase(negateCase((LogicalExpression)expr.substitute(varIndex,valIndex)),negateCase(abs.substitute(varIndex,valIndex))));
//			}
//		}
		else result.add(new EnsureCase(negateCase(exp)));
	}
			
	public static LogicalExpression negateCase(LogicalExpression e){
		// returns the expression itself
		if (e instanceof NotExpression) {
			return ((NotExpression)e).arg1();
		}
		// a=>b is (not a or b) so returns return (a and not b)
		else if (e instanceof JMLImpliesExpression){
			LogicalExpression a1 = ((JMLImpliesExpression)e).arg1();
			LogicalExpression a2 = ((JMLImpliesExpression)e).arg2();
			return new AndExpression(a1,negateCase(a2));
		}
		else if (e instanceof JMLForAllExpression) {
			JMLForAllExpression j = (JMLForAllExpression)e;
			//Check whether enumerable. If it is, then we only need to negate the condition; 
			//otherwise, we have to develop the implicit imply expression and negate it. 
			if(j.enumeration() != null) 
				return new JMLExistExpression(
								j.index(),
								j.boundExpression(),
								negateCase(j.condition()));
			else
				return new JMLExistExpression(
								j.index(), 
						        new LogicalLiteral(true), 
						        new AndExpression(
						        		j.boundExpression(), 
						        		negateCase(j.condition()))); 
		}
		else if (e instanceof JMLExistExpression) {
			JMLExistExpression j = (JMLExistExpression)e;
			//Check whether enumerable. If it is, then we only need to negate the condition; 
			//otherwise, we have to develop the implicit 'and' expression and negate it. 
			if(j.enumeration() != null) 
				return new JMLForAllExpression(
								j.index(),
								j.boundExpression(),
								negateCase(j.condition()));
			else
				return new JMLForAllExpression(
								j.index(), 
						        new LogicalLiteral(true), 
						        new OrExpression(
						        		negateCase(j.boundExpression()), 
						                negateCase(j.condition()))); 
		}
		else if (e instanceof OrExpression){
			OrExpression or = (OrExpression)e;
			return new AndExpression(negateCase(or.arg1()),
					                 negateCase(or.arg2()));
		}
		else if (e instanceof AndExpression){
			AndExpression and = (AndExpression)e;
			return new OrExpression(negateCase(and.arg1()),
					                negateCase(and.arg2()));
		}
		// returns the negation
		else return new NotExpression(e);
	}

}

