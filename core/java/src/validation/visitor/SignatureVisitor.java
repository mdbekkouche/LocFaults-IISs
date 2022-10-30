package validation.visitor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import validation.system.xml.ValidationSystemXmlCallbacks;
import validation.util.ChildIterator;
import validation.util.Type;
import exception.AnalyzeException;
import expression.variables.Variable.Use;



/** class to parse methods headers : comment and signature
 */

public class SignatureVisitor extends XMLVisitor {

	/** visit the method signature
	 * add variables in constSyst for all parameters
	 * @param next
	 * POSTCOND : next is the first node of JML specification
	 */
	static protected void visitMethodSignature(Node next, ValidationSystemXmlCallbacks p)
		throws AnalyzeException  
	{
		// parse and add method parameters in constSyst
		// <!ELEMENT IDSSignature (IDSParameter+)>
		//	<!ATTLIST IDSSignature returnType CDATA #IMPLIED
		//                    name CDATA #IMPLIED>
		if (!isIDSSignature(next)) 
			exception(4);
		p.setReturnType(Type.parseType(((Element) next).getAttribute("returnType")));
		ChildIterator child = new ChildIterator(next);
		next = child.next();
		// 0 or more parameters
		while(isParameter(next)) {
			parseParameter(next, p);
			next= child.next();
		}
	}


	// parse parameters
	// <!ELEMENT IDSParameter (IDSExprIdent)>
	// <!ATTLIST IDSParameter type CDATA #IMPLIED>
	static private void parseParameter(Node n, ValidationSystemXmlCallbacks p) 
		throws AnalyzeException 
	{
		Type type = Type.parseType(((Element) n).getAttribute("type"));
		ChildIterator child = new ChildIterator(n);
		n = child.next();
		if (isExprIdent(n)){
			String name =  parseIdent(n);
			//Create a new variable and flag it as an input variable
			p.addNewVar(name, type, Use.PARAMETER);
		}
		else if (isArrayIdent(n)) {
			String[] infos = parseArrayIdent(n);
			p.addNewArrayVar(infos[0], new Integer(infos[1]).intValue(), type, Use.PARAMETER);
		}
		else throw new AnalyzeException("only simple variables or arrays are handled");
	}

}
