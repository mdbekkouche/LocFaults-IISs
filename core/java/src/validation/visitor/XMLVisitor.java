package validation.visitor;


import org.w3c.dom.Element;
import org.w3c.dom.Node;

import exception.AnalyzeException;



/** parent class to manage parsing of XML nodes
* and to manage exceptions
* parsing methods are static to be mo efficient
*/

public class XMLVisitor {

	// identifiers for nodes
	public final static int BLOCK=1;
	public final static int VAR_DECL=2;
	public final static int VAR_ASSIGN=3;
	public final static int JAVA_RETURN=4;
	public final static int IF=5;
	public final static int WHILE=6;
	public final static int ARRAY_ASSIGN=7;
    public final static int ASSERT=8;
	public final static int ARRAY_DECL=9;
	public final static int METHOD_CALL=10;
	public final static int ASSUME=11;


	//------------------------------------------------------------
	// identifier parsing

    //<!ELEMENT IDSExprIdent EMPTY>
    //<!ATTLIST IDSExprIdent name CDATA #IMPLIED> 
    //<!ATTLIST IDSExprIdent type CDATA #IMPLIED> 
	static protected String parseIdent(Node n) throws AnalyzeException {
		if (!isExprIdent(n)) 
			exception(3);
		return ((Element) n).getAttribute("name");
	}

	static protected String parseType(Node n) throws AnalyzeException {
		if (!isExprIdent(n)) 
			exception(3);
		return ((Element) n).getAttribute("type");
	}
	
//	<!ELEMENT IDSArrayExprIdent>
//	<!ATTLIST IDSArrayExprIdent 
//	           length CDATA #IMPLIED
//	           name CDATA #IMPLIED>
	static protected String[] parseArrayIdent(Node n) throws AnalyzeException {
		String[] result = new String[2];
		result[0] = ((Element) n).getAttribute("name");
		result[1] = ((Element) n).getAttribute("length");
		return result;
	}
	

	//------------------------------------------------------------
	// methods to identify nodes
	static protected boolean isElement(Node n) {
		return (n.getNodeType()==Node.ELEMENT_NODE);
	}

	static protected boolean isTag(Node n, String s) {
		return isElement(n)&&(((Element) n).getTagName()).equals(s);
	}
	
	static protected String nodeName(Node n) {
		if (!isElement(n)) return "Not an element";
		else return ((Element) n).getTagName();
	}

	static protected boolean isMethod(Node n) {
		return isTag(n,"method");
	}

	protected static boolean isMethodComment(Node n) {
		return isTag(n,"methodComment");
	}

	static protected boolean isExprIdent(Node n) {
		return isTag(n,"IDSExprIdent");
	}

	static protected boolean isMethodCall(Node n) {
		return isTag(n,"IDSMethodCall");
	}

	static protected boolean isArrayIdent(Node n) {
		return isTag(n,"IDSArrayExprIdent");
	}

	static protected boolean isIDSMethod(Node n) {
		return isTag(n,"IDSMethod");
	}

	static protected boolean isIDSSignature(Node n) {
		return isTag(n,"IDSSignature");
	}
	
	static protected boolean isIDSJavaBloc(Node n) {
		return isTag(n,"IDSJavaBloc");
	}

	static protected boolean isParameter(Node n) {
		return isTag(n,"IDSParameter");
	}   

	static protected boolean isParenthesis(Node n) {
		return isTag(n,"IDSExprParenthesis");
	}

	// used when analyzing signature
	static protected boolean isInteger(String n) {
		return n.equals("int");
	}
	
	// tags used in JavaBlock

	//----------------------------------------------------------
	// tag handling

	static protected boolean isJavaReturn(Node n) {
		return isTag(n,"IDSJavaReturnStatment");
	}

	static protected boolean isIfStatment(Node n) {
		return isTag(n,"IDSJavaIfStatment");
	}
	static protected boolean isWhileStatment(Node n) {
		return isTag(n,"IDSJavaWhileStatment");
	}
	static protected boolean isWhileCond(Node n) {
		return isTag(n,"whileCond");
	}
	static protected boolean isWhileBlock(Node n) {
		return isTag(n,"whileBloc");
	}

	static protected boolean isIf(Node n) {
		return isTag(n,"if");
	}
	static protected boolean isCond(Node n) {
		return isTag(n,"cond");
	}
	static protected boolean isElse(Node n) {
		return isTag(n,"else");
	}

	static protected boolean isDeclVar(Node n) {
		return isTag(n,"IDSJavaDeclVarStatment");
	}
	static protected boolean isJavaAssign(Node n) {
		return isTag(n,"IDSJavaAssignmentStatment");
	}
	
	static protected int identifier(Node n) {
		if (isTag(n,"IDSJavaBloc")) return BLOCK;
		if (isTag(n,"IDSJavaReturnStatment")) return JAVA_RETURN;
		if (isTag(n,"IDSJavaIfStatment")) return IF;
		if (isTag(n,"IDSJavaWhileStatment")) return WHILE;
		if (isTag(n,"IDSJavaDeclVarStatment")) return VAR_DECL;
		if (isTag(n,"IDSJavaAssignmentStatment")) return VAR_ASSIGN;
		if (isTag(n,"IDSJavaArrayAssignmentStatment")) return ARRAY_ASSIGN;
        if (isTag(n,"IDSJMLAssert")) return ASSERT;
        if (isTag(n,"IDSJMLAssume")) return ASSUME;
		if (isTag(n,"IDSJavaDeclArrayStatment")) return ARRAY_DECL;
        if (isTag(n,"IDSMethodCall")) return METHOD_CALL;		
		return 0;
	}

	static protected boolean isSimple(Node n) {
		return (isTag(n,"IDSJavaDeclVarStatment")||
				isTag(n,"IDSJavaAssignmentStatment")||
				isTag(n,"IDSJavaArrayAssignmentStatment")||
				isTag(n,"IDSJMLAssert")||
				isTag(n,"IDSJMLAssume"));
	}

	
	//----------------------------------------------------------
	// methods to handle exceptions
	static protected void exception(int n)  throws AnalyzeException{
		String s=" In XMLVisitor ";
		switch (n) {
		case 1: s="comment or method";break;
		case 2: s="IDSMethod ";break;
		case 3: s="IDSExprIdent ";break; 
		case 4: s="IDSSignature ";break; 
		case 5: s="IDSArrayExprIdent ";break; 
		}
		throw new AnalyzeException(s + " expected");
	}


//	//----------------------------------------------------------
//	// affichage
//	// pour afficher le document DOM associ� au xml
//	public void displayDocument() {
//		afficher("",document);
//	}
//
//	// procédure récursive
//	public void afficher(String indent, Node n) {
//		switch (n.getNodeType()) {
//		case Node.COMMENT_NODE:
//			System.out.println(indent + "<!--" + n.getNodeValue() + " -->");
//			break;
//		case Node.TEXT_NODE:
//			System.out.println(indent + ((CharacterData) n).getData() );
//			break;
//		case Node.ELEMENT_NODE:
//			System.out.println(indent + "<" + ((Element) n).getTagName() + ">");
//			break;
//		}
//		NodeList fils = n.getChildNodes();
//		for(int i=0; (i < fils.getLength()); i++)
//			afficher(indent + " | ", fils.item(i));
//		if (n.getNodeType() == Node.ELEMENT_NODE)
//			System.out.println(indent + "</" + ((Element) n).getTagName() + ">");
//	}

	//----------------------------
//	public static  void main(String[] s) {
//		try {
//			String path="/home/helen/Recherche/Solver/Danocops/xml2ilog/";
//			XMLVisitor vx = new XMLVisitor(new File(path + "xml/essai1.xml"));
//			//vx.displayDocument();
//			vx.generate(3);
//		} catch (AnalyzeException a) {
//			System.out.println(a);
//		}catch (ConstraintException c) {
//			System.out.println(c);
//		}
//	}
	
}

