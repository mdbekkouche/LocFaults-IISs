package validation.visitor;

import java.util.ArrayList;

import org.w3c.dom.Node;

import validation.system.xml.ValidationSystemXmlCallbacks;
import validation.util.ChildIterator;
import validation.util.Strategy;
import validation.util.Type;
import exception.AnalyzeException;
import expression.logical.LogicalExpression;

/** class to parse JML specification
 */

public class SpecificationVisitor extends XMLVisitor {

	/** visit the JML specification
	 * @param n
	 * @param byCases <code>true</code> if the post-condition must be negated and divided into several
	 *                disjunctive cases; <code>false</code> if the post-condition must be left as is.
	 * @return the negation of ensures clauses
	 * @throws AnalyzeException
	 * @throws ConstraintException
	 * POSTCOND : constSyst contains constraint of requires clause
	 *            if b, all boolean expressions are stored in the boolTable of p
	 */
	static protected ArrayList<EnsureCase> visitJML(Node n, 
													ValidationSystemXmlCallbacks p,
													boolean byCases)
		throws AnalyzeException 
	{
		if (!isSpecification(n)) 
			exception(1);
		ChildIterator child = new ChildIterator(n);
		Node next = child.next();
		if (isRequires(next)) {
			parseRequires(next, p);
			next = child.next();
		}
		if (!isEnsures(next)) 
			exception(2);
		return parseEnsures(next, p, byCases);
	}

	// parse requires clause
	// <!ELEMENT IDSRequiresClause (%ExprLogical;)>
	private static void parseRequires(Node n, ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		ChildIterator child = new ChildIterator(n);
		Node next = child.next();
		p.setParsingPrecond(true);
		LogicalExpression requires = LogicalExprVisitor.parse(next, p);
		p.setParsingPrecond(false);
		p.addCNFPrecond(requires);
	}

	// parse ensures clause
	// <!ELEMENT IDSEnsuresClause (%ExprLogical;)>
	/**
	 * @param n
	 * @param p
	 * @param byCases <code>true</code> if the post-condition must be negated and divided into several
	 *                disjunctive cases; <code>false</code> if the post-condition must be left as is.
	 * @return
	 * @throws AnalyzeException
	 */
	private static ArrayList<EnsureCase> parseEnsures(Node n, 
													  ValidationSystemXmlCallbacks p, 
													  boolean byCases) 
		throws AnalyzeException  
	{
		// add variable JMLResult,b
		// TODO : le type d�pend de l'entete de la methode
		if (p.returnType() != Type.VOID)
			p.addNewVar("JMLResult", p.returnType());
		// build the negation of the specification as a disjunctive form
		ChildIterator child = new ChildIterator(n);
		ArrayList<EnsureCase> result= new ArrayList<EnsureCase>();
		if(child.hasMoreChild()) {
			Node next = child.next();
			LogicalExpression exp = LogicalExprVisitor.parse(next, p);
		
			if (byCases) {
				//Negate post-condition and divide it into cases at disjunctions.
				//Cases are added to result.
				Strategy.disjunctiveNegate(exp, result);
			}
			else {
				//Post-condition is left as in the JML clause.
				result.add(new EnsureCase(exp));
			}
		}
		return result;
	}
	

	// tag identifiers
	static protected boolean isSpecification(Node n) {
		return isTag(n,"IDSMethodSpecification");
	}

	static protected boolean isRequires(Node n) {
		return  isTag(n,"IDSRequiresClause");
	}

	static protected boolean isEnsures(Node n) {
		return isTag(n,"IDSEnsuresClause");
	}


	//----------------------------------------------------------
	//  exceptions
	static protected void exception(int n)  throws AnalyzeException{
		String s="  ";
		switch (n) {
		case 1: s="IDSMethodSpecification ";break;
		case 2: s="IDSEnsuresClause ";break;
		}
		throw new AnalyzeException(s + " expected");
	}


}
