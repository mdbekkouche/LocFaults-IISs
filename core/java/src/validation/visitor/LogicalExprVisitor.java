package validation.visitor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import validation.system.xml.ValidationSystemXmlCallbacks;
import validation.util.ChildIterator;
import validation.util.Type;
import exception.AnalyzeException;
import expression.Expression;
import expression.ParenExpression;
import expression.logical.AndExpression;
import expression.logical.EqualExpression;
import expression.logical.InfEqualExpression;
import expression.logical.InfExpression;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.logical.SupEqualExpression;
import expression.logical.SupExpression;
import expression.variables.ArrayVariable;
import expression.variables.Variable;

//import expression.integer.IntegerLiteral;
//import expression.integer.JMLResult;
//import expression.integer.MinusExpression;
//import expression.integer.PlusExpression;
//import expression.integer.TimeExpression;
//import expression.integer.DivExpression;



/** classe pour parser les expressions logiques
 */

public class LogicalExprVisitor extends XMLVisitor {
	
	// <!ENTITY % ExprLogical "(IDSExprLogicalOr|IDSExprLogicalAnd|IDSExprLogicalNot
	// |IDSExprParenthesis|IDSExprEquality|IDSExprSup|IDSExprInf)">  
	// if b, then save sub-expressions in boolean table of p
	public static LogicalExpression parse(Node n, ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		LogicalExpression result = null;
		if (isParenthesis(n)) {
			ChildIterator child = new ChildIterator(n);
			Node next = child.next();
			result = new ParenExpression(parse(next,p));
		} 
		else  if (isLogicalRelation(n)){
			result = logicalRelation(n, p);
		}
		else if (isLogicalOperator(n)){
			result = logicalOperator(n, p);
		}
		else if (isLogicalNot(n)) {
			result = logicalNot(n, p);
		}
		else if (isLogicalJMLImplies(n)) {
			result = logicalJMLImplies(n, p);
		}
		else if (isJMLForAll(n)) {
			result = logicalJMLForAll(n, p);
		}
		else if (isJMLExist(n)) {
			result = logicalJMLExist(n, p);
		}
		else if (isJMLAllDiff(n)) {
			result = logicalJMLAllDiff(n, p);
		}
		else 
			exception(1);

		return result;
	}

	/* <!ELEMENT IDSExprLogicalNot (%ExprLogical;)> */
	static private LogicalExpression logicalNot(Node n, ValidationSystemXmlCallbacks p) 
	throws AnalyzeException  {
		ChildIterator child = new ChildIterator(n);
		Node next = child.next();
		LogicalExpression first = parse(next,p);
//		if (b) first = p.addBoolExpr(first);
		return new NotExpression(first);
	}

	// <!ELEMENT IDSExprLogicalOr (%ExprLogical;,(%ExprLogical;)+)>
	//   <!ELEMENT IDSExprLogicalAnd (%ExprLogical;,(%ExprLogical;)+)> 
	static private LogicalExpression logicalOperator(Node n, 
													 ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		String operator = ((Element) n).getTagName();
		ChildIterator child = new ChildIterator(n);
		Node next = child.next();
		LogicalExpression first = parse(next,p);
//		if (b) first = p.addBoolExpr(first);
		if (!child.hasMoreChild()) exception(1);
		LogicalExpression second = logicalOperatorAux(child,p, operator);
//		if (b) second = p.addBoolExpr(second);
		LogicalExpression result = null;
		if (isOr(operator))result = new OrExpression(first,second);
		else if (isAnd(operator)) result = new AndExpression(first,second);
//		if (b) result= p.addBoolExpr(result);
		return result;
	}

	// méthode récursive pour analyser tous les arguments
	// PRECOND : child a encore un fils
	static private LogicalExpression logicalOperatorAux(
			ChildIterator child,
			ValidationSystemXmlCallbacks p,  
			String op)
		throws AnalyzeException  
	{

		Node next = child.next();
		LogicalExpression result = null;
		LogicalExpression first = parse(next,p);
//		if (b) first = p.addBoolExpr(first);
		result = first;
		if (child.hasMoreChild()) {
//			next = child.next();
			LogicalExpression second = logicalOperatorAux(child,p,op);
//			if (b) second = p.addBoolExpr(second);
			if (isOr(op)) result = new OrExpression(first,second);
			else if (isAnd(op))result = new AndExpression(first,second);
//			if (b) result=p.addBoolExpr(result);
			return result;
		}
		return first;
	}

	//    <!-- couple d'expressions entieres pour les operations de comparaison --> 
	// <!ENTITY % Compare "(%ExprInt;,%ExprInt;)">
	// <!-- operateurs de comparaison -->
	// <!ELEMENT IDSExprEquality (%Compare;)>
	// <!ELEMENT IDSExprSup (%Compare;)>
	// <!ELEMENT IDSExprInf (%Compare;)>
	static private LogicalExpression logicalRelation(Node n, 
													 ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		String op = ((Element) n).getTagName();
		ChildIterator child = new ChildIterator(n);
		if (!child.hasMoreChild()) exception(2);
		Node next = child.next();
		Expression first = NumericExprVisitor.parse(next, p);
		if (!child.hasMoreChild()) exception(2);
		next = child.next();
		Expression second = NumericExprVisitor.parse(next, p);
		//TODO : gérer précisément le type Expression ou LogicalExpression

		return relation(p,op,first,second);
	}
	
	/** this method takes a comparison operator with its parameters
	 * It returns the corresponding LogicalExpression
	 */
	static private LogicalExpression relation(
			ValidationSystemXmlCallbacks p,
			String op,
			Expression first,
			Expression second) 
	{	
		LogicalExpression result=null;
		if (isEqual(op)) {
			result = new EqualExpression(first,second);
		}
		else if (isInf(op)) 
			result = new InfExpression(first,second);
		else if (isInfEqual(op)) 
			result = new InfEqualExpression(first,second);
		else if (isSup(op))
			result = new SupExpression(first,second);
		else if (isSupEqual(op))
			result = new SupEqualExpression(first,second);
		return result;
	}	

	static private LogicalExpression logicalJMLImplies(Node n, 
													   ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		ChildIterator child = new ChildIterator(n);
		if (!child.hasMoreChild()) exception(1);
		Node next = child.next();
		LogicalExpression first = parse(next,p);
//		if (b) first = p.addBoolExpr(first);
		if (!child.hasMoreChild()) exception(3);
		next = child.next();
		LogicalExpression second = parse(next,p);
//		if (b) second = p.addBoolExpr(second);
		return new JMLImpliesExpression(first,second);
	}
	
//	<!-- for all -->
//	<!-- la variable entière d'index, la condition à vérifier pour la variable d'index, -->
//	<!-- la condition à vérifier -->
//	<!ELEMENT IDSJMLForAll (IDSExprIdent,%ExprLogical;,%ExprLogical;)+>
	static private LogicalExpression logicalJMLForAll(Node n, 
													  ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		ChildIterator child = new ChildIterator(n);
		if (!child.hasMoreChild()) 
			exception(4);
		Node next = child.next();

		String indexType = parseType(next);
		Type type;
		//For compatibility with XML before introducing the type attribute 
		//for IDSExprIdent elements to handle floating types,
		//the absence of the type attribute implies an INT type.
		try {
			type = Type.parseType(indexType);
		} catch(AnalyzeException e) {
			type = Type.INT;
		}
		
		Variable index = p.addNewVar(parseIdent(next), type);
		if (!child.hasMoreChild()) 
			exception(5);
		next = child.next();
		LogicalExpression indexCond = parse(next,p);
		next = child.next();
		LogicalExpression condition = parse(next,p);
		return new JMLForAllExpression(index, indexCond, condition);
	}

//	<!-- for all -->
//	<!-- la variable d'index, la condition à vérifier pour la variable d'index, -->
//	<!-- la condition à vérifier -->
//	<!ELEMENT IDSJMLExist (IDSExprIdent,%ExprLogical;,%ExprLogical;)+>
	static private LogicalExpression logicalJMLExist(Node n, 
													 ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		ChildIterator child = new ChildIterator(n);
		if (!child.hasMoreChild()) 
			exception(4);
		Node next = child.next();
		
		String indexType = parseType(next);
		Type type;	
		//For compatibility with XML before introducing the type attribute 
		//for IDSExprIdent elements to handle floating types,
		//the absence of the type attribute implies an INT type.
		try {
			type = Type.parseType(indexType);
		} catch(AnalyzeException e) {
			type = Type.INT;
		}
		
		Variable index = p.addNewVar(parseIdent(next), type);
		if (!child.hasMoreChild()) 
			exception(5);
		next = child.next();
		LogicalExpression indexCond = parse(next,p);
		next = child.next();
		LogicalExpression condition = parse(next,p);	
		return new JMLExistExpression(index, indexCond, condition);
	}
			
//	private static int[] computeConstant(Expression e) {
//		int[] result = new int[2];
//		result[0]=0;
//		result[1]=-1;
//		if (e instanceof IntegerLiteral) {
//	    	result[0]=1;
//			result[1]=((IntegerLiteral)e).constantValue();
//		}
//		if (e instanceof MinusExpression) {
//			int[] a1 = computeConstant(((MinusExpression)e).getArg1());
//		    int[] a2 = computeConstant(((MinusExpression)e).getArg2());
//		    if (a1[0]==1&&a2[0]==1) {
//		    	result[0]=1;
//		    	result[1]=a1[1]-a2[1];
//		    }
//		}
//		if (e instanceof PlusExpression){
//			int[] a1 = computeConstant(((PlusExpression)e).getArg1());
//		    int[] a2 = computeConstant(((PlusExpression)e).getArg2());
//		    if (a1[0]==1&&a2[0]==1) {
//		    	result[0]=1;
//		    	result[1]=a1[1]+a2[1];
//		    }
//		}
//		if (e instanceof TimeExpression){
//			int[] a1 = computeConstant(((TimeExpression)e).getArg1());
//		    int[] a2 = computeConstant(((TimeExpression)e).getArg2());
//		    if (a1[0]==1&&a2[0]==1) {
//		    	result[0]=1;
//		    	result[1]=a1[1]*a2[1];
//		    }
//		}
//		if (e instanceof DivExpression){
//			int[] a1 = computeConstant(((DivExpression)e).getArg1());
//		    int[] a2 = computeConstant(((DivExpression)e).getArg2());
//		    if (a1[0]==1&&a2[0]==1) {
//		    	result[0]=1;
//		    	result[1]=a1[1]/a2[1];
//		    }
//		}
//		if (e instanceof JMLResult){
//			result[0]=1;
//		}
//		
//		return result;
//	}
	
//   AllDiff constraint
//	<!ELEMENT IDSJMLAllDiff  (IDSArrayExprIdent)>
	static private LogicalExpression logicalJMLAllDiff(Node n, 
													   ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		ChildIterator child = new ChildIterator(n);
		if (!child.hasMoreChild()) exception(7);
		Node next = child.next();
		ArrayVariable tab = p.getArrayVar(parseArrayIdent(next)[0]);
		return new JMLAllDiff(tab);
	}
	
	//----------------------------------------------------------
	// gestion des tag
	static protected boolean isLogicalOperator(Node n) {
		return isTag(n,"IDSExprLogicalAnd")||isTag(n,"IDSExprLogicalOr");
	}
	static protected boolean isOr(String n) {
		return n.equals("IDSExprLogicalOr") ;
	}
	static protected boolean isAnd(String n) {
		return n.equals("IDSExprLogicalAnd") ;
	}
	static protected boolean isLogicalNot(Node n) {
		return isTag(n,"IDSExprLogicalNot");
	}

	static protected boolean isLogicalRelation(Node n) {
		return isTag(n,"IDSExprSup")
		|| isTag(n,"IDSExprInf")
		||isTag(n,"IDSExprEquality")
		|| isTag(n,"IDSExprSupEqual")
		|| isTag(n,"IDSExprInfEqual");
	}

	static protected boolean isEqual(String n) {
		return n.equals("IDSExprEquality") ;
	}
	static protected boolean isSup(String n) {
		return n.equals("IDSExprSup") ;
	}
	static protected boolean isSupEqual(String n) {
		return n.equals("IDSExprSupEqual") ;
	}
	static protected boolean isInf(String n) {
		return n.equals("IDSExprInf") ;
	}
	static protected boolean isInfEqual(String n) {
		return n.equals("IDSExprInfEqual") ;
	}

	static protected boolean isLogicalJMLImplies(Node n) {
		return isTag(n,"IDSExprJMLImplies");
	}
	static protected boolean isJMLForAll(Node n) {
		return isTag(n,"IDSJMLForAll");
	}
	static protected boolean isJMLExist(Node n) {
		return isTag(n,"IDSJMLExist");
	}
	static protected boolean isJMLAllDiff(Node n) {
		return isTag(n,"IDSJMLAllDiff");
	}
	
	//----------------------------------------------------------
	// g�re les exceptions
	static protected void exception(int n)  throws AnalyzeException{
		String s=" In LogicalExprVisitor ";
		switch (n) {
		case 1: s+="logical operation ";break;
		case 2: s+="integer value  ";break;
		case 3: s+="second part of implies  ";break;
		case 4: s+="integer identifier ";break;
		case 5: s+="logical expression ";break;
		case 6: throw new AnalyzeException("can't enumerate on the condition in JMLForAll or JMLExist");
		case 7: s+="array identifier expected in AllDiff expression";
		}
		throw new AnalyzeException(s + " expected");
	}


}
