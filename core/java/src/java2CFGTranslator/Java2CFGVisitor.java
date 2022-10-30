package java2CFGTranslator;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.jmlspecs.checker.JmlPredicate;

import validation.Validation;
import validation.Validation.VerboseLevel;

import CFG.AssertEndWhile;
import CFG.AssertNode;
import CFG.BlockNode;
import CFG.CFG;
import CFG.CFGNode;
import CFG.EnsuresNode;
import CFG.FunctionCallNode;
import CFG.IfNode;
import CFG.RequiresNode;
import CFG.SetOfCFG;
import CFG.WhileNode;
import exception.AnalyzeException;
import expression.Expression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.numeric.IntegerLiteral;
import expression.numeric.NumericExpression;
import expression.variables.ArrayElement;
//import expression.variables.ArraySymbolTable;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import expression.variables.Variable.Use;

/** main class to visit a Java program (version <= 1.5)

 * 
 * uses JDT for parsing Java
 * see http://help.eclipse.org/help31/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/index.html
 * for description of the API and AST nodes
 *
 * See README.txt for current restrictions and details
 * about translation.
 * 
 * @version November 2010
 * @author Hélène Collavizza and Olivier Ponsini
 * from an original student work by Eric Le Duff and Sébastien Derrien
 * Polytech'Nice Sophia Antipolis
 */
public class Java2CFGVisitor extends ASTVisitor {	
	// maximum number of loop unwinding
	private int maxUnwound ; 
	
	/**
	 * Maximum length for arrays. Default is 10.
	 */
	private int maxArrayLength;
		
	/** set of CFG of the program
 	 * one CFG per function
 	 */
	private SetOfCFG program;
	
	/** name of the class 
	 */
	private String className;
	
	/** name of the current method which is parsed
	 * it has the form name_0
	 */
	private String methodName;
	
	/** name of the method that must be proven
	 */
	private String methodToProve;
		
	/** hashMap that maps each function of the program to
	 its current number of invocation*/
	private HashMap<String, Integer> functionCallNumber;
	
	/** maps each function to the last renaming of the variable 
	 * used in the function to represent the global variables
	 * 
	 * When there is a call to the function, each variable
	 * used in the function to represents a global variable
	 * is substituted according to the renaming
	 * of the global variable before the function call
	 * (see FunctionCallNode.renameGlobalVariable)
	 */
	private HashMap<String, HashMap<String,Integer>> globalVarInFunction;
	
	/** cfg of the method which is currently parsed*/
	private CFG currentCFG;
		
	/** identifiers for current node, current if, current while
	 */
	private CFGNodeIdent ident;
	
	// current CFG node 
	private BlockNode currentBlockNode;

	// stack of expressions which are currently explored
	private ExpressionStack expressions;

	// >0 when parsing an expression
	// when parsing expressions, simpleName nodes correspond to variables and must be parsed
	// it is necessary to increase (and decrease) expression before (after) parsing :
	//     - the expression returned by a method
	//     - the value of initialization of a variable
	//     - the condition of a while or if statement
	// because these expressions can be simple variables (e.g. return res; if (c)) 
	// and not prefix or infix expressions
	// expression is set using the private method "visitExpression"
	private int expression; 

	// the parser for the JML specification
	Jml2CFG jml;

	// to deal with arrays
	private boolean isArrayDeclaration;
	
	// to deal with global variables
	private boolean isGlobal;
	
	// type of the current variable 
	// required because variable type and variable name are not parsed in the same visitor
	// VariableDeclarationStatement or FieldDeclaration gives the type
	// VariableDeclarationFragment gives the variable name and the initializer
	private validation.util.Type varType;

	// to deal with negative numbers
	private boolean minus; 
	
	// all variables used in the program
	private SymbolTable variables;

	// class variables (i.e. global variables)
	private SymbolTable classSymbols;
			
	// to know if it is possible to have assert statements
	// this is not possible before entering the first instruction block
	private boolean assertIsPossible ;
	
	/**
	 * The root of the AST we are visiting.
	 * This is needed to map characters' position to line and columns in 
	 * the source file.
	 */
	private CompilationUnit compilUnit;

	/** to know if we are parsing the method to prove*/
	private boolean parsingMethodToProve;
	
	/////////////////////////////////////////////////////////////////////
	// constructors
	/////////////////////////////////////////////////////////////////////

	
	
	// private constructor to build a visitor from complete information
	protected Java2CFGVisitor(
			String methodName, String className, String methodToProve, SymbolTable scalVar, 
			int maxUnwound, int maxArrayLength, CFGNodeIdent id, boolean assertPoss, CompilationUnit root, Jml2CFG jml, SetOfCFG prog, 
			HashMap<String, Integer> functionCallNumber, HashMap<String, HashMap<String, Integer>> gvif, 
			boolean parsingMethodToProve)  
	{	
		super();
		this.methodName= methodName;
		this.className = className;
		this.methodToProve = methodToProve;
		this.variables = scalVar;
		this.maxUnwound = maxUnwound;	
		this.maxArrayLength = maxArrayLength;
		this.ident = id;
		this.assertIsPossible = assertPoss;
		this.compilUnit = root;
		this.jml = jml;
		this.program = prog;
		this.functionCallNumber = functionCallNumber;
		this.globalVarInFunction = gvif;
		// this is required the first time a method is parsed
		if (methodName != null)
			initCurrentCFG(methodName);
		this.expressions = new ExpressionStack();
		this.expression = 0;
		this.isArrayDeclaration = false;
		this.isGlobal = false;
		this.parsingMethodToProve = parsingMethodToProve;
	}	
	
	/** return the class name from the program file name
	 * 
	 * @param fileName
	 * @return fileName without .java suffix
	 */
	private static String className(String fileName) {
		int lastSlash = fileName.lastIndexOf('/');
		int point = fileName.lastIndexOf('.');
		return fileName.substring(lastSlash+1, point);
	}
	
	// to build a default visitor
	// assume that fileName and class name are the same
	public Java2CFGVisitor(String fileName, CompilationUnit root, String methodToProve, int maxUnwound, int maxArrayLength) 
		throws Java2CFGException 	{		
		this(null, className(fileName), methodToProve, new SymbolTable(),maxUnwound,maxArrayLength,
			 new CFGNodeIdent(), false, root, new Jml2CFG(new File(fileName)), new SetOfCFG(className(fileName)),
			 new HashMap<String,Integer>(), new HashMap<String,HashMap<String,Integer>>(), false);
	}
	
	// to build a visitor from another one
	public Java2CFGVisitor(Java2CFGVisitor jv) {
		this(jv.methodName,	jv.className, jv.methodToProve, jv.variables, 
			 jv.maxUnwound, jv.maxArrayLength,jv.ident, jv.assertIsPossible, jv.compilUnit, jv.jml,
			 jv.program, jv.functionCallNumber, jv.globalVarInFunction, jv.parsingMethodToProve);
	}
	
	// to build a visitor from another one but using specific symbol tables
	// This is used when visiting if and while nodes, to pass the symbol before the if node when parsing the else branch
	// (useful to synchronize SSA renamings) 
	public Java2CFGVisitor(SymbolTable scalVar,  Java2CFGVisitor jv) {
		this(jv.methodName, jv.className, jv.methodToProve, scalVar, 
			 jv.maxUnwound, jv.maxArrayLength,jv.ident, jv.assertIsPossible, jv.compilUnit,
			 jv.jml, jv.program, jv.functionCallNumber, jv.globalVarInFunction, jv.parsingMethodToProve);
	}
	
	
	/////////////////////////////////////////////////////////////
	// getters
	/////////////////////////////////////////////////////////////
	protected SetOfCFG getProgram() {
		return program;
	}

	
	/////////////////////////////////////////////////////////////
	// private tools
	/////////////////////////////////////////////////////////////
	
	/**
	 * @param : n, the name of the method, ended by "_0"
	 * @return : the root name (i.e. n without "_0")
	 */
	private String getRootName(String n) {
		return n.substring(0, n.length()-2);
	}
	
	/**
	 * to compute the key of nodes
	 * the key is either empty for the nodes of the method to prove
	 * or equals to the name of the function + its number of call
	 */
	private String getKeyOfNode(){
		if (methodName.equals(""))
			return "";
		String name = getRootName(methodName);
		if (name.equals(methodToProve))
			return "";
		if (functionCallNumber!=null && functionCallNumber.containsKey(name)) {
			return name + "_" + functionCallNumber.get(name);
		}
		return name + "_0";
	}
	
	/**
	 * to initialize a CFG
	 * @param methodName
	 */
	private void initCurrentCFG(String methodName){
		currentCFG = new CFG(getRootName(methodName), className);
		currentBlockNode = new BlockNode(ident.node++, getKeyOfNode());
		currentCFG.setFirstNode(currentBlockNode);
	}

	// true if an expression is being parsed
	private boolean isExpression() {
		return expression>0;
	}
	
	// true if the value of the expression is a non deterministic input
	private boolean isNondetValue(org.eclipse.jdt.core.dom.Expression value) {
		return (value instanceof MethodInvocation) && 
		((MethodInvocation)value).getName().toString().equals("nondet_in");
	}
	
	//  To parse the JML specification
	private void parseJML() throws Java2CFGException{
		jml.parse(className);
	}

	
	///////////////////////////////////////////////////////////////////
	// things to do before the visit of any node in order to parse 
	// JML assertions in the Java code
	///////////////////////////////////////////////////////////////////
	/** 
	 * Here goes what must be done each time a node is visited.
	 * Prior visiting a node, we make sure we have built the inlined JML annotation
	 * before this node.
	 * This includes, for instance, assert,  assumes and invariant clauses (only assert are handled).
	 * 
	 * @param node The node we are about to visit.
	 */
	public void preVisit(ASTNode node) {
		// TODO : handle assumes and invariant clauses

		if (assertIsPossible) {
			//System.out.println("dans preVisit " + node.getClass() + " " + node.getStartPosition());
			visitInlinedClauses(node.getStartPosition());
		}
	}	
	
	private void visitInlinedClauses(int curPosition) {
		Iterator<Entry<AssertPosition, JmlPredicate>> iter = jml.getAssertionJML().entrySet().iterator();
		// when there are successive assertions before this node
		// one builds one AssertCFGNode for each assertion

		while(iter.hasNext()) {
			Entry<AssertPosition, JmlPredicate> entry = iter.next();
			if(0 > entry.getKey().compareTo(compilUnit.getLineNumber(curPosition), 
					                        compilUnit.getColumnNumber(curPosition))) 
			{
				// parse the jmlNode to transform it as an expression
				Jml2CFGVisitor v = new Jml2CFGVisitor(methodName, className, variables);
				entry.getValue().accept(v);
				Expression cond = v.getLastExpression();

				//System.out.println("condition du assert " + cond);
				buildAssertNode(cond, entry.getKey().getLine());
				iter.remove();
			} 
			else 
				return;
		}
	}

	/**
	 * Removes all clauses located before the given position from the list of JML 
	 * inlined clauses.
	 * @param curPosition
	 */
	private void removeInlinedClauses(int curPosition) {
		Iterator<Entry<AssertPosition, JmlPredicate>> iter = jml.getAssertionJML().entrySet().iterator();
		while(iter.hasNext()) {
			Entry<AssertPosition, JmlPredicate> entry = iter.next();
			if(0 > entry.getKey().compareTo(compilUnit.getLineNumber(curPosition), 
					                        compilUnit.getColumnNumber(curPosition))) 
			{
				iter.remove();
			}
			else 
				return;
		}
	}
	
	/**
	 * build an assertion node
	 */
	private void buildAssertNode(Expression cond, int startLine) {
//		System.out.println("dans build assert " + jml);
//		System.out.println("dans build assert " + jml.getAssertionJML());
				
		// create an assertion node
		AssertNode asser = new AssertNode(ident.node++,getKeyOfNode());
		//				System.out.println("assert condition " + cond);
		asser.setCondition(cond);
		asser.startLine = startLine;
		linkLeft(currentBlockNode, asser);

		// create a new node to continue
		currentBlockNode = new BlockNode(ident.node++, getKeyOfNode());
		linkLeft(asser, currentBlockNode);

	}

	/** to finish the visit 
	 */
	public void  endVisit() {
	}
	
	///////////////////////////////////////////////////////////////
	// visit methods
	///////////////////////////////////////////////////////////////
	
	// ############ Le fichier Java #################	
	public boolean visit(CompilationUnit node) {
		// add JML annotations in the variable jml. they are added as JmlPredicate
		// - assert statements are stored in jml.assertionJML
		// - ensures and requires are stored in jml.requires and jml.ensures
		
		// these JmlPredicates are then translated as Expressions in visitInlinedClausses
		// and buildPrecond and buildPostcond
		try {
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("Parsing JML annotations...");
			}
			parseJML();
		} catch (Java2CFGException e) {
			System.err.println("Error found during JML parsing." +
			" Not able to build JML specification.");
			e.printStackTrace();
		}
		return true;
	}
	
	public void endVisit(CompilationUnit node) {
	}
	
	// ############ class declaration #################	
	/** visit the class declaration
	 * can provide access to the list of methods
	 */
	public boolean visit(TypeDeclaration node) {
		String name = node.getName().toString();
		className = name;
		// the name of the class is used as program name
		program.setName(name);
		// System.out.println(node.getFields()[0]); //tableau des types déclarés
		return true;
	}
	
	// ############ Class or instance variable declaration #################	
	// TODO : examine static modifier to either create global or local variables
	public boolean visit(FieldDeclaration node) {
		if (node.getType().isArrayType()) {
			isArrayDeclaration = true;
		}
		try {
			varType = validation.util.Type.parseType(node.getType().toString());
		} catch (AnalyzeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e);
		}
		isGlobal=true;
		return true;
	}
	
	public void endVisit(FieldDeclaration node) {
		isGlobal=false;
	}

	// ############ Method declaration node #################
	private void parseParameters(List<SingleVariableDeclaration> param) {
		SingleVariableDeclaration parameter;
		String parameterName;
		Type varType;
		Variable v;
		
		for (int i=0; i<param.size(); i++) {
			parameter = (SingleVariableDeclaration) param.get(i);
			parameterName = parameter.getName().toString();
			varType = parameter.getType();
			
			try {
				if (varType.isArrayType()) {
					// create an array variable of length maxArrayLength
					v = variables.addNewPrefixedArrayVar(
							methodName,
							parameterName, 
							validation.util.Type.parseType(varType.toString()),
							Use.PARAMETER,
							maxArrayLength);
				}
				else {
					v = variables.addNewPrefixedVar(
							methodName, 
							parameterName, 
							validation.util.Type.parseType(varType.toString()), 
							Use.PARAMETER);
				}
				currentCFG.addFormalParameter(v);
			} catch (AnalyzeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean visit(MethodDeclaration node) {
		//System.out.println("dans method");
		
		String methodRootName = node.getName().toString();
		
		// main method is ignored
		if (!methodRootName.equals("main") && !program.hasMethod(methodRootName)) {
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Starting conversion of method " + methodRootName + "...");
			}
			
			// first time one parses the function
			functionCallNumber.put(methodRootName, 0);
			globalVarInFunction.put(methodRootName, new HashMap<String, Integer>());
			this.methodName =  methodRootName +  "_0";			
			// This ensure node is the main ensure node (i.e. the one of the validated
			// method), if we are on the method specified through command line or by 
			// default if it is the first method of the file that is not the Java "main". 
			if (methodToProve == null) {
				methodToProve = methodRootName;
				Validation.pgmMethodName = methodRootName;
				parsingMethodToProve = true;
			}
			else {
				parsingMethodToProve = methodRootName.equals(methodToProve);
			}
			
			// to initialize the CFG and the current block
			initCurrentCFG(methodName);
			currentCFG.startLine =  compilUnit.getLineNumber(node.getStartPosition());
				
			// Store current symbol table that contains only class symbols
			classSymbols = (SymbolTable)variables.clone();
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("Class symbol table: " + classSymbols);
			}
			
			if (!parsingMethodToProve) {
				// For each global variable add a corresponding local one in the method's symbol table
				// TODO: global array variables
				for (String globalVarRootName: classSymbols.getNames()) {
					variables.addNewPrefixedVar("global_" + methodName, 
												globalVarRootName, 
												classSymbols.get(globalVarRootName).type(), 
												Use.GLOBAL);
				}
			}

			// to parse signature of the method
			// add all parameter names in the list of parameters
			// this list of parameters is used by the jml visitor		
			List<SingleVariableDeclaration> param = node.parameters();
			parseParameters(param);
			
			// return type
			Type rt = node.getReturnType2();
			try {
				validation.util.Type resType = validation.util.Type.parseType(rt.toString());
				currentCFG.setType(resType);
				if (rt.isPrimitiveType() && !rt.toString().equals("void")) { 
					variables.addNewPrefixedVar(methodName, "JMLResult", resType);
				}
			} catch (AnalyzeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				
			// parameters and JMLResult have been added in the symbol table
			// one can now parse the precondition (i.e. get its AST and translate it
			// as an expression)
			buildPreCondition(jml.getRequires(methodRootName));

			// after the declaration of the method, it is possible to have a JML assert 
			assertIsPossible = true;
			
			// continue parsing the core of the program
			return true;
		}

		return false;
	}
	
	/**
	 * Builds the <pre>requires</pre> clause as the first CFG node.
	 * If there is no require clause, the block contains the expression <pre>true</pre>.
	 * 
	 */ 
	private void buildPreCondition(JmlPredicate requires) {
		// require is node number 0
		RequiresNode reqNode = new RequiresNode(0, getKeyOfNode());
		// get the requires JmlPredicate and translate it as an Expression
		// here, the symbol table contains all the variable of the method
		Expression requiresExp;
		if (requires != null) {
			Jml2CFGVisitor jmlVisitor = new Jml2CFGVisitor(methodName, className, variables);
			requires.accept(jmlVisitor);
			requiresExp = (LogicalExpression)jmlVisitor.getLastExpression();
		}
		else {	// No precondition specified
			requiresExp = new LogicalLiteral(true);
		}
		
		// condition
		reqNode.setCondition(requiresExp);

		// next node is the current cfg first node
		linkLeft(reqNode, currentCFG.firstNode());
		//System.out.println(currentCFG.firstNode());
		currentCFG.setFirstNode(reqNode);
	}
	
	// to build the ensures clause as the last CFG node
	// if there is no ensures clause, the block contains the expression True
	private void buildPostCondition(Type rt,JmlPredicate ensures) {
		// require is last node in the current CFG
		EnsuresNode lastNode = new EnsuresNode(ident.node++, getKeyOfNode(), parsingMethodToProve);

		// get the ensures JmlPredicate and translate it as an Expression
		// here, the symbol table contains all the variable of the method
		Expression ensuresExp ;

		if (ensures != null) {
			Jml2CFGVisitor jmlVisitor = new Jml2CFGVisitor(methodName, className, variables);
			ensures.accept(jmlVisitor);
			ensuresExp = (LogicalExpression)jmlVisitor.getLastExpression();
		}
		else {	// No postcondition specified
			ensuresExp = new LogicalLiteral(true);
		}
		
		// if return node, need to rename JMLResult as the last renaming of Result
		if (rt.isPrimitiveType() && !rt.toString().equals("void")) { 
			Variable jmlResult = variables.get(methodName + "_JMLResult");
			Variable result = variables.get(methodName + "_Result");
//			System.out.println("dans java2CFG build postcond ens " + ens);
			ensuresExp = ensuresExp.substitute(jmlResult, result);
//			System.out.println("dans java2CFG build postcond ens après substitute" + ens);
		}
		
		// last renaming of the variables must been used in the post-condition
		// NOTA: only useful for arrays and global variables
		//ensuresExp = ensuresExp.computeLastRenaming(variables);
		lastNode.setCondition(ensuresExp);
		linkLeft(currentBlockNode, lastNode);
		currentCFG.setLast(lastNode);
	}
	
	// add variable Result and result if the method is not void
	public void endVisit(MethodDeclaration node) {	
		String name = node.getName().toString();
		// main method is ignored
		if (!name.equals("main")) {			
						
			// build the post condition
			buildPostCondition(node.getReturnType2(),jml.getEnsures(name));
			
			// set final values of the CFG
			currentCFG.setNodeNumber(ident.node);
			currentCFG.setScalarVar(variables);

			// add the current CFG in the set of CFG of the program
			program.addMethod(name, currentCFG);
			
			//System.out.println("CFG de "+ name + currentCFG);

			// Restore environment for parsing next function
			ident = new CFGNodeIdent();
			expression = 0;
			isArrayDeclaration = false;
			assertIsPossible = false;
			variables = classSymbols;
			parsingMethodToProve = false;
		}

	}
	
	/////////////////////////////////////////////////////////
	// private methods to handle variables
	/////////////////////////////////////////////////////////

	// to add a new variable
	private Variable addNewVariable(String name, validation.util.Type type, Use use) {
		if (use == Use.GLOBAL)
			return variables.addNewPrefixedVar(className, name, type, use);
		else
			return variables.addNewPrefixedVar(methodName, name, type, use);
	}

	
	// ###################### Method invocation #########################
	public boolean visit(MethodInvocation call) {
		String functName = call.getName().toString();
//				System.out.println("Java2CFGVisitor MethodInvocation, fonction name " + functName +
//						" scalar var "+ scalarVar.variableValues());

		// save current node
		CFGNode nodeBeforeCall = currentBlockNode;

		// visit the effective parameters
		this.expression++;
		ArrayList<Expression> effectiveParameters = new ArrayList<Expression>();
		for (Object o : call.arguments()){
			((ASTNode)o).accept(this);
			effectiveParameters.add(expressions.pop());
		}
		this.expression--;
		
		// make a FunctionCall node
		// We assume callee is always declared (and thus parsed) before caller.
		CFG funct  = program.getMethod(functName);
		int numberOfCall = functionCallNumber.get(functName);
		FunctionCallNode fcn = new FunctionCallNode(ident.node++, funct, effectiveParameters, numberOfCall);

		// to link the nodes
		linkLeft(nodeBeforeCall, fcn);
		currentBlockNode = new BlockNode(ident.node++, getKeyOfNode());
		linkLeft(fcn, currentBlockNode);
		fcn.setAfterNode(currentBlockNode);
		
		// to build the node that link global variables to their local representation
		fcn.makeGlobalVariableNodes(globalVarInFunction.get(functName), this.methodName, variables, 
									globalVarInFunction.get(getRootName(methodName)), this.parsingMethodToProve,
									currentBlockNode);
		
		// function has been invoked one more time
		functionCallNumber.put(functName, numberOfCall + 1);
						
		// if the return type is not void, push the returned expression
		// on the expression stack
		// the returned value is variable Result prefixed 
		// with the function name and its number of call
		if (fcn.returnType() != validation.util.Type.VOID) {
			Variable res = variables.addNewPrefixedVar(functName + "_" + numberOfCall, 
					                                   "Result", 
					                                   fcn.returnType());
			expressions.push(res);
		}
		return false;
	}
	
	// ###################### Javadoc #########################
	public boolean visit(Javadoc node) {
		return false;
		
	}
	
	// ####################### Block ############################
	/**
	 * Visit a block delimited with '{' and '}'
	 */
	public boolean visit(Block node) {
		//System.out.println("debut block");
		return true;
	}
	
	public void endVisit(Block node) {
		//This is in case the block is empty except for some JML clauses. 
		visitInlinedClauses(node.getStartPosition() + node.getLength());

		//System.out.println("fin block");
	}
	
	// ############ Variable declaration node #################
	
	public boolean visit(VariableDeclarationStatement node) {
		if (node.getType().isArrayType()) {
			isArrayDeclaration = true;
		}
		try {
			varType = validation.util.Type.parseType(node.getType().toString());
		} catch (AnalyzeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e);
		}

		return true;
	}
	
	public void endVisit(VariableDeclarationStatement node) {
		isArrayDeclaration = false;
	}
	
	// to visit an expression
	private Expression visitExpression(org.eclipse.jdt.core.dom.Expression expr) {
		expression++;
		expr.accept(this);
		expression--;
		return this.expressions.pop();
	}
	
	/** to build a new variable 
	 * @param varName : the variable name
	 * @param initializer : its initial value
	 */
	private void makeDeclaration(String varName, validation.util.Type type, org.eclipse.jdt.core.dom.Expression initializer, int startLine) {
		Assignment ass;
		Use use;
		
		if (isGlobal)
			use = Use.GLOBAL;
		else
			use = Use.LOCAL;
		
		if (initializer == null) {
			Variable v = addNewVariable(varName, type, use);
			Expression exp = null;
			exp = validation.util.Type.getLiteralFromString("0", type);
			ass = new expression.logical.Assignment(v, exp, startLine);
		}
		else {		
			// to deal with non deterministic inputs
			if (isNondetValue(initializer)) {
				Variable v = addNewVariable(varName, type, use);
				ass = new expression.logical.NondetAssignment(v, startLine);
			}
			else {
				Variable v = addNewVariable(varName, type, use);
				ass = new expression.logical.Assignment(v, visitExpression(initializer), startLine);
			}
		}
		
		if (isGlobal)
			program.addFieldAssignment(ass);
		else {
			// If the block has no start line number, set the start line of this node to 
			// the line of the first assignment in the block.
			if (currentBlockNode.startLine == 0) {
				currentBlockNode.startLine = startLine;
			}
			currentBlockNode.add(ass);
		}
	}

	
	// DeclarationFragment allows to get name and type of the Variable
	// declaration
	// if no initial value is given, variable takes default value 0
	public boolean visit(VariableDeclarationFragment node) {
		//System.out.println("dans declaration fragment");
		//System.out.println(node.getNodeType());

		if (!isArrayDeclaration){
			//System.out.println(varType);
			makeDeclaration(node.getName().toString(), varType, node.getInitializer(), compilUnit.getLineNumber(node.getStartPosition()));
			return false;
		}
		else {
			// no array declaration in opSem so just print a comment
			System.out.println("(* Array declaration of " + node.getName() + "*)");
//			scalarVar.addNewVar(node.getName() + ".length",validation.util.Type.INT , Use.GLOBAL);
			// arrVarr.add(node.getName().toString());
		}
		return true;
	}
	
	public void endVisit(VariableDeclarationFragment node) {
		//System.out.println("var " + scalarVar.getAllVarNames());
	}
	
	// #################### Expressions ##########################
	/**
	 * Infix expressions
	 */
	public boolean visit(InfixExpression node) {
		expression++;
		//System.out.println("dans infix " + expression);
				
		// parse arguments and push them into the expression stack
		node.getRightOperand().accept(this);
		node.getLeftOperand().accept(this);
		
		// get the expressions of the two arguments from the stack
		Expression first = expressions.pop();
		Expression second = expressions.pop();
		
		first = JavaNodesTools.buildExpression(node, first, second);

		// for more than 2 operands
		if (node.hasExtendedOperands()) {
			List<org.eclipse.jdt.core.dom.Expression> operandList = node.extendedOperands();
			for (org.eclipse.jdt.core.dom.Expression expr : operandList){
				expr.accept(this);
				second = expressions.pop();
				first = JavaNodesTools.buildExpression(node, first, second);
			}
		}
		// push the new expression into the stack
		expressions.push(first);
		return false;
	}

	/**
	 * Close infix operators
	 */
	public void endVisit(InfixExpression node) {
		expression--;
	}

	/**
	 * prefix expression not
	 */
	public boolean visit(PrefixExpression node) {
		expression++;
		if (JavaNodesTools.isUnaryMinus(node)) {
			minus = true;
			return true;
		}
		else {
			// parse the arguments and push it into the expression stack
			node.getOperand().accept(this);
			// get the expression of the argument from the stack
			Expression first = expressions.pop();
			Expression result = JavaNodesTools.buildExpression(node,first);
			// push the new expression into the stack
			expressions.push(result);
		}
		return false;
	}
	
	public void endVisit(PrefixExpression node) {
		minus = false;
		expression--;
	}
	
	
    /**
     * Returns the given positive number literal's type.
     * 
     * @param s A string representation of a positive number literal.
     * @return The type of s or {@link PrimitiveType.VOID} if s is not a
valid number literal. 
     */
    private String getNumberLiteralType(String s) {
    	if (s.matches("\\d+")) 
    		return "int";
    	else if(s.matches("(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?[fFdD]?"))	{
    		if (Character.toLowerCase(s.charAt(s.length()-1)) == 'f')
    			return "float";
    		else
    			return "double";
    	}
    	else
    		return "void";
    }
    
    /**
     * Un noeud de type Numeric
     */
    public boolean visit(NumberLiteral node) {
    	String value = minus ? "-"+node.toString():node.toString();
    	String type = getNumberLiteralType(node.getToken());

    	Expression e = null;
    	try {
			e = validation.util.Type.getLiteralFromString(value, validation.util.Type.parseType(type));
		} catch (AnalyzeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	if (e==null) {
    		System.err.println("Invalid number literal: " + node.getToken());
    		System.exit(7);
    	}
    	else 
    		expressions.push(e);
    	return false;
    }
	
	/** to visit boolean constants */
	public boolean visit(BooleanLiteral node){
		expressions.push(new LogicalLiteral(node.booleanValue()));
		return false;
	}
	
	private void addGlobalVarInFunction(Variable v) {
		HashMap<String, Integer> glob = globalVarInFunction.get(getRootName(methodName));
		glob.put(v.root(), v.ssaNumber());
	}
	
	/**
	 * Variable name must be printed only inside an expression 
	 */
	public boolean visit(SimpleName node) {
		//System.out.println("dans simple name " + node.toString());
		if (isExpression()) {
			String name = node.toString();
			Variable v = variables.getPrefixed(methodName, className, name);
//			System.out.println(" dans visit SimpleName" + v + methodName + className);
			// global variables are prefixed with "global_methodName"
			// in the CFG of all methods which are not the method to verify
			// NB: when methodName.length()==0, we are not parsing a method
			if (v.isGlobal() && !parsingMethodToProve) {
				v = variables.get("global_" + methodName + "_" + v.root());
				if (v.ssaNumber() == 0) { // SSA numbers > 0 are added through an assignment
					addGlobalVarInFunction(v);
				}
			}
			expressions.push(v);
		}
		return false;
	}

	public void endVisit(SimpleName node) {
		//System.out.println("fin simple name");
	}
	
	/**
	 * qualified variable name : only used for array length (a.length)
	 */
	public boolean visit(QualifiedName node) {
//		System.out.println("dans qualified name " + node.toString());
		if (isExpression() && node.getName().getIdentifier().equals("length")) {
			String arrayName = node.getQualifier().toString();
			ArrayVariable v = variables.getPrefixedArray(methodName, className, arrayName);
			expressions.push(new IntegerLiteral(v.length()));
		}
		return false;
	}

	public boolean visit(ParenthesizedExpression node) {
		return true;
	}
	
	public void endVisit(ParenthesizedExpression node) {
	}
	
	//	 ############  If Node #################
	/**
	 * Links father and child as left child and left father. 
	 */
	private void linkLeft(CFGNode father, CFGNode child) {
		father.setLeft(child);
		child.setLeftFather(father);
	}

	/**
	 * Links father and child as right child and left father.
	 * @param father
	 * @param child
	 */
	private void linkRight(CFGNode father, CFGNode child) {
		father.setRight(child);
		child.setLeftFather(father);
	}
	
	/**
	 * Return a if node with the condition associated to node.
	 */
	private IfNode buildIfNode(IfStatement node){
		// a new node is created 
		IfNode result = new IfNode(ident.iff++, ident.node++, getKeyOfNode());
		
		// visit condition
		//System.out.println("cond if " + node.getExpression());
		LogicalExpression cond = (LogicalExpression)visitExpression(node.getExpression());
		result.setCondition(cond);		
		return result;
	}

	/**
	 * @param unfoldings 
	 * @param node : current node
	 * @return : a while node with the condition associated to node.
	 */
	private WhileNode buildWhileNode(org.eclipse.jdt.core.dom.Expression cond, int startLine, int unfoldings) {
		WhileNode whileNode = new WhileNode(ident.whilee++, ident.node++, getKeyOfNode());
		whileNode.setCondition((LogicalExpression)this.visitExpression(cond));
		whileNode.startLine = startLine;
		whileNode.IDLine = startLine + ":" + (maxUnwound-unfoldings)+1;
		return whileNode;
	}

	/** 
	 * Synchronizes variable SSA renamings on branching nodes.
	 * At least the initial renaming of all variables that may appear in either block exists both in ssaAfterThen and 
	 * ssaAfterElse (since global variables are systematically added at the declaration of the method). The only exception 
	 * would be a variable declared inside one of the two blocks. But this is normally not supported and should be avoided!
	 *  
	 * For each program variable v:
	 *     Let r1 (resp. r2) be the current renaming of v in then part (resp. in else part)
	 *        if r1>r2: 
	 *            add constraint r1 = r2 in else part
	 *        else:
	 *            add constraints r2 = r1 in then part
	 * @return A symbol table that contains all the variables needed by both branches.
	 *            
	 */
	private SymbolTable synchronizeSSA(CFGNode thenNode, CFGNode elseNode, SymbolTable ssaAfterThen, SymbolTable ssaAfterElse)
	{	
		int ssaIf, ssaElse;
		Variable vIf, vElse;
		SymbolTable synchronizedTable = ssaAfterThen;
		
		for (String rootName: ssaAfterElse.getNames()) {
			vElse = ssaAfterElse.get(rootName);
			ssaElse = vElse.ssaNumber();
			vIf = ssaAfterThen.get(rootName);
			if (vIf != null) {
				ssaIf = vIf.ssaNumber();
				if (ssaIf > ssaElse) {
					// Add renaming constraint in else node
					((BlockNode)elseNode).getBlock().add(new Assignment(vIf, vElse, 0));
				}
				else {
					if (ssaIf < ssaElse) {
						// Add renaming constraints in then node
						((BlockNode)thenNode).getBlock().add(new Assignment(vElse, vIf, 0));
						synchronizedTable.addRenamings(rootName, ssaIf+1, ssaElse, ssaAfterElse);
					}
				}
			}
			else { // Variable was not found in the 'then' symbol table it must be local to the else block
				//TODO: Note that support of variable declarations anywhere else than at the beginning of a method body is
				//      experimental and incomplete, i.e. not recommended!
				//We add all the renamings existing in the else symbol table to the resulting symbol table
				synchronizedTable.addRenamings(rootName, 0, ssaElse, ssaAfterElse);
			}
		}		
		return synchronizedTable;
	}
	
	// to link the nodes of a if statement or a while statement
	private void linkConditionalNode(
			CFGNode before, CFGNode ifNode, CFGNode firstThenNode, CFGNode lastThenNode,
			CFGNode firstElseNode, CFGNode lastElseNode)
	{
		// link if node with node before if statement
		linkLeft(before, ifNode);
		// link if node and then node
		linkLeft(ifNode, firstThenNode);
		
		// to link if and else nodes
		ifNode.setRight(firstElseNode);
		firstElseNode.setLeftFather(ifNode);
		
		// start a new block for statements after if
		this.currentBlockNode = new BlockNode(ident.node++, getKeyOfNode());
		
		// to link thenNode and node after then branch
		lastThenNode.setLeft(currentBlockNode);
		currentBlockNode.setLeftFather(lastThenNode);
				
		// to set node after else branch
		lastElseNode.setLeft(currentBlockNode);
		currentBlockNode.setRightFather(lastElseNode);
	}
	
	/**
	 *  Build a CFG.IfNode with :
	 *    - condition
	 *    - then part
	 *    - else part : created even if there is no else in the initial statement.
	 *                  this is useful to synchronize SSA renaming in then and else parts
	 *    - end node  : this node remains  empty if the if 
	 *                  statement is the last statement of the block
	 *    
	 *    SSA renamings in then and else parts are synchronized
	 */
	public boolean visit(IfStatement node) { 
//		System.out.println("visit if " + node.getExpression());
		
		// Save block before if
		CFGNode nodeBeforeIf = currentBlockNode;
		//System.out.println("ssa before if "+scalarVar);
		
		// save SSA renaming before parsing then branch 
		SymbolTable ssaBeforeIf = (SymbolTable)variables.clone();

		// build the condition node
		CFGNode ifNode = buildIfNode(node);

		// Set the start line of this if node.
		ifNode.startLine = compilUnit.getLineNumber(node.getStartPosition());
		ifNode.startPosition = node.getExpression().getStartPosition();
		ifNode.length = node.getExpression().getLength();
						
		// to visit the then node
		Java2CFGVisitor thenVisitor = new Java2CFGVisitor(this);
		node.getThenStatement().accept(thenVisitor);
		SymbolTable ssaThen = thenVisitor.variables;
		// root node of then part
		CFGNode thenNode = thenVisitor.currentCFG.firstNode();
		// last node of then part
		CFGNode lastThenNode = 	thenVisitor.currentBlockNode;

		// to visit the else node
		CFGNode elseNode = null;
		CFGNode lastElseNode = null;
		// visit else part if it exists
		if (node.getElseStatement()!=null) {
			// restart with variable renamings before then
			//TODO : gérer les renomages SSA des 2 branches pour les tableaux !!!
			Java2CFGVisitor elseVisitor = new Java2CFGVisitor(ssaBeforeIf,  this);
			node.getElseStatement().accept(elseVisitor);
			ssaBeforeIf = elseVisitor.variables;
			// root node of else part
			elseNode = 	elseVisitor.currentCFG.firstNode();
			// last node of else part
			lastElseNode = 	elseVisitor.currentBlockNode;			
		}
		else {
			// elseNode is created even if there is no else branch 
			// to handle SSA renaming synchronization
			elseNode = new BlockNode(ident.node++, getKeyOfNode());
			lastElseNode = elseNode;
		}
		
		// System.out.println("ssaThen " + ssaThen + " ssaElse " + ssaElse);
		// handle SSA renaming
		// add nodes to have the same renaming number into then and else part
		variables = synchronizeSSA(lastThenNode, lastElseNode, ssaThen, ssaBeforeIf);
		
//		System.out.println("var fin if "+intVar);
		// to link the nodes and get the new current node
		linkConditionalNode(nodeBeforeIf, ifNode, thenNode, lastThenNode, elseNode, lastElseNode);
		return false;
		
	}

	public void endVisit(IfStatement node) {
		// cfg.setNodeNumber(nodeIdent); // required for printing
		// System.out.println("end visit if " + cfg);
	}
	
	// add an assertion which is violated if the maximum unwinding is too small
	private AssertEndWhile buildAssertEndWhileNode(org.eclipse.jdt.core.dom.Expression cond)
	{
		LogicalExpression condition = (LogicalExpression)this.visitExpression(cond);
		AssertEndWhile assertion = new AssertEndWhile(ident.node++, getKeyOfNode());
		assertion.setCondition(new NotExpression(condition));
		return assertion;
	}
	
	
	// ######################## While ###############################
	
	/** to unwind the while loop
	 * 
	 * @param unfoldings : number of unfoldings remaining
	 * @param body : while body
	 * @param cond : the while condition
	 */
	private void unwindWhile(org.eclipse.jdt.core.dom.Expression cond, 
							 Statement body,
							 int unfoldings, 
							 int startLine) 
	{
		if (unfoldings <= 0) {
			//Add the unwinding while assertion
			CFGNode assertion = buildAssertEndWhileNode(cond);
			linkLeft(this.currentBlockNode, assertion);
			this.currentBlockNode = new BlockNode(ident.node++, getKeyOfNode());
			linkLeft(assertion, this.currentBlockNode);
			//Remove all asserts within the loop body from the list of inlined JML clauses
			this.removeInlinedClauses(body.getStartPosition() + body.getLength());
		}
		else {
			WhileNode whileNode = this.buildWhileNode(cond, startLine,unfoldings);
			linkLeft(this.currentBlockNode, whileNode);
			
			// Then part corresponds to entering the loop one more time
			this.currentBlockNode = new BlockNode(ident.node++, getKeyOfNode());
			linkLeft(whileNode, this.currentBlockNode);

			// Else part corresponds to exiting the loop now
			BlockNode skipLoopNode = new BlockNode(ident.node++, getKeyOfNode());
			linkRight(whileNode, skipLoopNode);
			
			// Save symbol table and JML assertions before parsing the loop body
			SymbolTable symbolsBeforeUnfolding = (SymbolTable)variables.clone();
			SortedMap<AssertPosition, JmlPredicate> jmlBeforeUnfolding = new TreeMap<AssertPosition, JmlPredicate>(jml.getAssertionJML());
            
			
			
			// Visit the body
			body.accept(this);
			
			// Assertions within the loop must added at each unfolding:
			// restore the assertions as they were before the loop iteration 
			// for the next iteration
			jml.setAssertion(jmlBeforeUnfolding);
			
			
			
			this.unwindWhile(cond, body, unfoldings - 1, startLine);
			
			skipLoopNode.setLeft(this.currentBlockNode);
			this.currentBlockNode.setRightFather(skipLoopNode);
			synchronizeSSA(null, skipLoopNode, this.variables, symbolsBeforeUnfolding);
			
			BlockNode newNode = new BlockNode(ident.node++, getKeyOfNode());
			linkLeft(this.currentBlockNode, newNode);
			this.currentBlockNode = newNode;
		}
	}

	
	/**
	 *  Build a CFG.WhileNode node with :
	 *    - condition
	 *    - then part : loop block
	 *    - else part : created to synchronize SSA renamings when the loop
	 *                  is taken or not
	 *    - end node  : this node remains empty if the while 
	 *                  statement is the last statement of the block
	 *    
	 */
	public boolean visit(WhileStatement node) {
		// body and condition of the while
		Statement body=node.getBody();
		org.eclipse.jdt.core.dom.Expression cond = node.getExpression();
				
		// unwind the loop
		unwindWhile(cond, body, maxUnwound, compilUnit.getLineNumber(node.getStartPosition()));
		
		return false;
	}
	
	public void endVisit(WhileStatement node) {
	}

	// ################## Assignment ###################################
	/**
	 * get a variable from its name
	 * 
	 * If the variable is local or is in the method to prove, returns the
	 * variable as performed in SymbolTable
	 * 
	 * If the variable is global, replace the name of the global variable in a function
	 * with its name prefixed with "global_functionName"
	 */
	private Variable getGlobalVariableInFunction(String methodName, String className, String name) {
		Variable currentV = variables.getPrefixed(methodName, className, name);
//		System.out.println("currentV " + currentV + " " + getRootName(methodName) + " " + methodToProve + " " + name);
		
		// if the variable is not a global variable or if the method which is parsed
		// is the method to verify, the name of the variable is the usual ones
		if (!currentV.isGlobal() || parsingMethodToProve) {
			return variables.addPrefixedVar(methodName, className, name);
		}
		// when the variable is global and the function which is parsed is not
		// the method to verify, then the name of the global variable is
		// prefixed with "global_methodName"
		String globalNameInFunction = "global_" + methodName + "_" + className + "_" + name;
		Variable v = variables.addVar(globalNameInFunction);
		addGlobalVarInFunction(v);
		return v;
	}
	
//	private ArrayVariable getGlobalArrayVariableInFunction(String methodName, String className, String name) {
//		Variable currentV = variables.getPrefixed(methodName, className, name);
////		System.out.println("currentV " + currentV + " " + getRootName(methodName) + " " + methodToProve);
//		
//		// if the variable is not a global variable or if the method which is parsed
//		// is the method to verify, the name of the variable is the usual ones
//		if (!currentV.isGlobal() || parsingMethodToProve) {
//			return variables.addPrefixedArrayVar(methodName, className, name);
//		}
//		// when the variable is global and the function which is parsed is not
//		// the method to verify, then the name of the global variable is
//		// prefixed with "global_methodName"
//		String globalNameInFunction = "global_" + methodName + "_" + className + "_" + name;
//		ArrayVariable v = variables.addArrayVar(globalNameInFunction);
//		addGlobalVarInFunction(v);
//		return v;
//	}
	
	
	/* visit an assignment node
	 */
	public boolean visit(org.eclipse.jdt.core.dom.Assignment node) {
//		System.out.println("dans Assign " + node);
		
		// If the block has no start line number, set the start line of this node to 
		// the line of the first assignment in the block.
		int startLine = compilUnit.getLineNumber(node.getStartPosition());
		
		if (currentBlockNode.startLine == 0) {
			currentBlockNode.startLine = startLine;
			currentBlockNode.startPosition = node.getStartPosition();
			currentBlockNode.length = node.getLength();
		}
		
		if (node.getLeftHandSide().getNodeType() != org.eclipse.jdt.core.dom.Assignment.ARRAY_ACCESS) {
			// name of the variable
			String name = node.getLeftHandSide().toString();

			// the right hand side is nondet_in(), thus just re-define the variable as
			// a non deterministic variable
			if (isNondetValue(node.getRightHandSide())) {
				// add a renaming to the variable
//				Variable v = scalarVar.addPrefixedVar(methodName , className, name);
				Variable v = getGlobalVariableInFunction(methodName, className, name);
				currentBlockNode.add(new expression.logical.NondetAssignment(v, startLine)) ;
			}
			else {		
				// visit the right hand side and then re-define the variable with a new assignment
				Expression value = visitExpression(node.getRightHandSide());
				// add a renaming to the variable
//				Variable v = scalarVar.addPrefixedVar(methodName , className, name);
				Variable v = getGlobalVariableInFunction(methodName, className, name);
//				System.out.println("assignment " + v + " " + value);
				currentBlockNode.add(new expression.logical.Assignment(v, value, startLine)) ;
			}
			//System.out.println("block dans Assignment "+currentBlock);
		}
		else {
			//System.out.println("array assignment");
			ArrayAccess var = (ArrayAccess) node.getLeftHandSide();
			String name = var.getArray().toString();
			// this must be done BEFORE creating the new SSA renaming of the array variable!
			Expression index = visitExpression(var.getIndex());
			Expression value = visitExpression(node.getRightHandSide());
			
			// array before the assignment
			ArrayVariable currentV = variables.getPrefixedArray(methodName, className, name);
			// array after the assignment (new SSA renaming of the array variable)
			ArrayVariable v = (ArrayVariable) getGlobalVariableInFunction(methodName, className, name);
			//System.out.println(var.getArray()  + "[" + index + "]=" + value);
			
			currentBlockNode.add(new ArrayAssignment(v,currentV,(NumericExpression)index, value, startLine));
		}
		return false;
	}
	

	
	// #######################  return ##############################
	/* 
	 * visit the return statement
	 * add an assignment to variable "Result"
	 * "Result" is also used when parsing the JML specification
	 * to designate the JML variable \result
	 */
	public boolean visit(ReturnStatement node) {
		Variable v = addNewVariable("Result", currentCFG.returnType(), Use.META);
		if (node.getExpression() != null) {
			Expression value = visitExpression(node.getExpression());
			//System.out.println("return "+ v + " " + value);
			currentBlockNode.add(new expression.logical.Assignment(v, value, compilUnit.getLineNumber(node.getStartPosition()))) ;
			/*currentBlockNode.startPosition = node.getStartPosition();
			currentBlockNode.length = node.getLength();*/
			return false;
		}
		return true;
	}
	
	
	// ####################### Arrays ##############################
	public boolean visit(ArrayAccess node) {
		Expression index = visitExpression(node.getIndex());
//		System.out.println("array access " + node.getArray()  + "[" + index + "]");
		// TODO: global array variables
		// getPrefixedArray is used because the variable must not be renamed
		ArrayVariable t = variables.getPrefixedArray(methodName, className, node.getArray().toString());
		expressions.push(new ArrayElement(t, (NumericExpression)index));
		return false;
	}

	public void endVisit(ArrayAccess node) {
	}
	
////		public boolean visit(ArrayCreation node) {
////			print ("length=\"" + (node.dimensions()).get(0) + "\" id=\""+ count++ + "\">",1) ;
////		return false;
////	}
////	
////	public void endVisit(ArrayCreation node) {
////		
////	}


}
