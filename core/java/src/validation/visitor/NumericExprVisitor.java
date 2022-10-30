package validation.visitor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import validation.system.xml.ValidationSystemXmlCallbacks;
import validation.util.ChildIterator;
import validation.util.Type;
import exception.AnalyzeException;
import expression.MethodCall;
import expression.ParenExpression;
import expression.numeric.NumericExpression;
import expression.numeric.DivExpression;
import expression.numeric.IntegerLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.DoubleLiteral;
import expression.numeric.MinusExpression;
import expression.numeric.PlusExpression;
import expression.numeric.TimeExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;

/** 
 * Classe pour parser les expressions numeriques
 */

public class NumericExprVisitor extends XMLVisitor {

	// <!ENTITY % ExprNum "(IDSExprIdent|%NumLiteral;|IDSExprPlus|IDSExprMinus|IDSExprTimes
	//                      |IDSExprParenthesis|IDSExprJMLResult)">
	//<!ENTITY % NumLiteral "(IDSExprDecimalIntegerLiteral|IDSExprDecimalFloatLiteral
	//                        |IDSExprDecimalDoubleLiteral)">
	 static protected NumericExpression parse(Node n, ValidationSystemXmlCallbacks p) 
	 	throws AnalyzeException  
	 {
		NumericExpression result = null;
		if (isParenthesis(n)) {
			ChildIterator child = new ChildIterator(n);
			Node next = child.next();
			result = new ParenExpression(parse(next,p));
		} 
		else if (isExprIdent(n)) {
			String name = parseIdent(n);
			result = p.getVar(name);    
		}
		else if (isMethodCall(n)) {
			Element elt = (Element)n;
			MethodCall mc = new MethodCall(elt.getAttribute("methodName"), 
										   Type.parseType(elt.getAttribute("returnType")));
			ChildIterator child = new ChildIterator(n);
			Node paramNode;
			while (child.hasMoreChild()) {
				paramNode = child.next();
				mc.addParam(parse(paramNode, p));
			}
			result = mc;
		}
		else if (isArithmeticOperator(n)){
			result = arithmeticOperator(n, p);
		}
		else if (isJMLResult(n)){
			//TODO : manage case where JMLResult is not an int
			result= p.getVar("JMLResult");
		}
		else if (isIntLiteral(n)){
			result = intLiteral(n);
		}	
		else if (isFloatLiteral(n)){
			result = floatLiteral(n);
		}	
		else if (isDoubleLiteral(n)){
			result = doubleLiteral(n);
		}	
		else if (isArrayElement(n)){
			result = arrayElement(n, p);
		}	
		else if (isArrayLength(n))
			result = arrayLength(n, p);
		else {
			System.err.println("XML Error on node: " + n);
			exception(1);
		}
		return result;
	}

	 //<!ELEMENT IDSExprPlus (%ExprNum;,(%ExprNum;)+)>
	 //<!ELEMENT IDSExprMinus (%ExprNum;,(%ExprNum;)+)>
	 //<!ELEMENT IDSExprTimes (%ExprNum;,(%ExprNum;)+)>
	 //<!ELEMENT IDSExprDiv (%ExprNum;,%ExprNum;)>
	static private NumericExpression arithmeticOperator(Node n, 
														ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		String operator = ((Element) n).getTagName();
		ChildIterator child = new ChildIterator(n);
		Node next = child.next();
		NumericExpression first = parse(next, p);
		NumericExpression second = null;
		while (child.hasMoreChild()) {
			second = parse(child.next(), p);
			if (isPlus(operator)) 
				first = new PlusExpression(first, second);
			else if (isMinus(operator)) 
				first = new MinusExpression(first, second);
			else if (isTimes(operator)) 
				first = new TimeExpression(first, second);
			else 
				first = new DivExpression(first, second);
		}

		if (second == null) 
			exception(1);
		return first;
	}

	// <!ELEMENT IDSExprDecimalIntegerLiteral EMPTY>
	// <!ATTLIST IDSExprDecimalIntegerLiteral value CDATA #IMPLIED>
	static private NumericExpression intLiteral(Node n) {
		return new IntegerLiteral(((Element) n).getAttribute("value"));
	}

	// <!ELEMENT IDSExprDecimalFloatLiteral EMPTY>
	// <!ATTLIST IDSExprDecimalFloatLiteral value CDATA #REQUIRED>
	static private NumericExpression floatLiteral(Node n) {
		return new FloatLiteral(((Element) n).getAttribute("value"));
	}

	// <!ELEMENT IDSExprDecimalDoubleLiteral EMPTY>
	// <!ATTLIST IDSExprDecimalDoubleLiteral value CDATA #REQUIRED>
	static private NumericExpression doubleLiteral(Node n) {
		return new DoubleLiteral(((Element) n).getAttribute("value"));
	}

	// access to an array element
	//<!ELEMENT IDSArrayExprIndex (%ExprNum;)+>
	//<!ATTLIST IDSArrayExprIndex name CDATA #IMPLIED>
	static private NumericExpression arrayElement(Node n, ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		String name = ((Element) n).getAttribute("name");
		ChildIterator child = new ChildIterator(n);
		ArrayVariable tab = p.getArrayVar(name);
		Node next = child.next();
		NumericExpression index = parse(next, p);
		if (index.type() != Type.INT)
			exception(2);
		// added 4th april
		if (!index.isConstant()) {
			//The NumericExpression cast should be safe since index has type INT
		   return  new ArrayElement(tab, index);
		}
		String eltName = tab.name() + "_" + index.constantNumber();
		if (p.getArrayElt().containsKey(eltName)) {
			return p.getArrayElt().get(eltName);
		}
		ArrayElement elt = new ArrayElement(tab, index);
		p.getArrayElt().put(eltName, elt);
		return elt;
	}
	
//	<!-- variable pour représenter la longueur du tableau -->
//	<!--  name  est le nom du tableau associé, -->
//	<!--  la longueur se trouve dans la var tableau elle-même  -->
//	<!ELEMENT LengthIdent EMPTY>
//	<!ATTLIST  LengthIdent
//	           name CDATA #IMPLIED>
	static private NumericExpression arrayLength(Node n, ValidationSystemXmlCallbacks p) 
		throws AnalyzeException  
	{
		String name = ((Element) n).getAttribute("name");
		return new IntegerLiteral((p.getArrayVar(name)).length() + "");
	}

	//----------------------------------------------------------
	// gestion des tag
	
	static protected boolean isIntLiteral(Node n) {
		return isTag(n, "IDSExprDecimalIntegerLiteral");
	}

	static protected boolean isFloatLiteral(Node n) {
		return isTag(n, "IDSExprDecimalFloatLiteral");
	}

	static protected boolean isDoubleLiteral(Node n) {
		return isTag(n, "IDSExprDecimalDoubleLiteral");
	}

	static protected boolean isArithmeticOperator(Node n) {
		return isTag(n,"IDSExprPlus") 
			|| isTag(n,"IDSExprMinus")
			|| isTag(n,"IDSExprTimes")
			|| isTag(n,"IDSExprDiv");
	}
	static protected boolean isPlus(String n) {
		return n.equals("IDSExprPlus") ;
	}
	static protected boolean isMinus(String n) {
		return n.equals("IDSExprMinus") ;
	}

	static protected boolean isTimes(String n) {
		return n.equals("IDSExprTimes") ;
	}
	static protected boolean isDiv(String n) {
		return n.equals("IDSExprDiv") ;
	}
	
	static protected boolean isJMLResult(Node n) {
		return isTag(n,"IDSExprJMLResult");
	}

	static protected boolean isArrayElement(Node n) {
		return isTag(n,"IDSArrayExprIndex");
	}
	static protected boolean isArrayLength(Node n) {
		return isTag(n,"LengthIdent");
	}
	//----------------------------------------------------------
	// exceptions
	static protected void exception(int n)  throws AnalyzeException{
		String s=" In NumericExprVisitor ";
		switch (n) {
		case 1: s+="integer operation ";break;
		case 2: s+="in array access, integer expression ";break;
		}
		throw new AnalyzeException(s + " expected");
	}

}
