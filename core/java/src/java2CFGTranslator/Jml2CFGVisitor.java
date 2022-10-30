package java2CFGTranslator;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jmlspecs.checker.*;
import org.multijava.mjc.JAddExpression;
import org.multijava.mjc.JArrayAccessExpression;
import org.multijava.mjc.JArrayDimsAndInits;
import org.multijava.mjc.JArrayInitializer;
import org.multijava.mjc.JArrayLengthExpression;
import org.multijava.mjc.JAssertStatement;
import org.multijava.mjc.JAssignmentExpression;
import org.multijava.mjc.JBitwiseExpression;
import org.multijava.mjc.JBlock;
import org.multijava.mjc.JBooleanLiteral;
import org.multijava.mjc.JBreakStatement;
import org.multijava.mjc.JCastExpression;
import org.multijava.mjc.JCatchClause;
import org.multijava.mjc.JCharLiteral;
import org.multijava.mjc.JClassBlock;
import org.multijava.mjc.JClassDeclaration;
import org.multijava.mjc.JClassExpression;
import org.multijava.mjc.JClassFieldExpression;
import org.multijava.mjc.JClassOrGFImport;
import org.multijava.mjc.JCompilationUnit;
import org.multijava.mjc.JCompoundAssignmentExpression;
import org.multijava.mjc.JCompoundStatement;
import org.multijava.mjc.JConditionalAndExpression;
import org.multijava.mjc.JConditionalExpression;
import org.multijava.mjc.JConditionalOrExpression;
import org.multijava.mjc.JConstructorBlock;
import org.multijava.mjc.JConstructorDeclaration;
import org.multijava.mjc.JContinueStatement;
import org.multijava.mjc.JDivideExpression;
import org.multijava.mjc.JDoStatement;
import org.multijava.mjc.JEmptyStatement;
import org.multijava.mjc.JEqualityExpression;
import org.multijava.mjc.JExplicitConstructorInvocation;
import org.multijava.mjc.JExpressionListStatement;
import org.multijava.mjc.JExpressionStatement;
import org.multijava.mjc.JFieldDeclaration;
import org.multijava.mjc.JForStatement;
import org.multijava.mjc.JFormalParameter;
import org.multijava.mjc.JIfStatement;
import org.multijava.mjc.JInitializerDeclaration;
import org.multijava.mjc.JInstanceofExpression;
import org.multijava.mjc.JInterfaceDeclaration;
import org.multijava.mjc.JLabeledStatement;
import org.multijava.mjc.JLocalVariableExpression;
import org.multijava.mjc.JMethodCallExpression;
import org.multijava.mjc.JMethodDeclaration;
import org.multijava.mjc.JMinusExpression;
import org.multijava.mjc.JModuloExpression;
import org.multijava.mjc.JMultExpression;
import org.multijava.mjc.JNameExpression;
import org.multijava.mjc.JNewAnonymousClassExpression;
import org.multijava.mjc.JNewArrayExpression;
import org.multijava.mjc.JNewObjectExpression;
import org.multijava.mjc.JNullLiteral;
import org.multijava.mjc.JOrdinalLiteral;
import org.multijava.mjc.JPackageImport;
import org.multijava.mjc.JPackageName;
import org.multijava.mjc.JParenthesedExpression;
import org.multijava.mjc.JPhylum;
import org.multijava.mjc.JPostfixExpression;
import org.multijava.mjc.JPrefixExpression;
import org.multijava.mjc.JRealLiteral;
import org.multijava.mjc.JRelationalExpression;
import org.multijava.mjc.JReturnStatement;
import org.multijava.mjc.JShiftExpression;
import org.multijava.mjc.JStatement;
import org.multijava.mjc.JStringLiteral;
import org.multijava.mjc.JSuperExpression;
import org.multijava.mjc.JSwitchGroup;
import org.multijava.mjc.JSwitchLabel;
import org.multijava.mjc.JSwitchStatement;
import org.multijava.mjc.JSynchronizedStatement;
import org.multijava.mjc.JThisExpression;
import org.multijava.mjc.JThrowStatement;
import org.multijava.mjc.JTryCatchStatement;
import org.multijava.mjc.JTryFinallyStatement;
import org.multijava.mjc.JTypeDeclarationStatement;
import org.multijava.mjc.JTypeDeclarationType;
import org.multijava.mjc.JTypeNameExpression;
import org.multijava.mjc.JUnaryExpression;
import org.multijava.mjc.JUnaryPromote;
import org.multijava.mjc.JVariableDeclarationStatement;
import org.multijava.mjc.JVariableDefinition;
import org.multijava.mjc.JWhileStatement;
import org.multijava.mjc.MJGenericFunctionDecl;
import org.multijava.mjc.MJMathModeExpression;
import org.multijava.mjc.MJTopLevelMethodDeclaration;
import org.multijava.mjc.MJWarnExpression;

import validation.util.Type;
import exception.AnalyzeException;
import expression.Expression;
import expression.ParenExpression;
import expression.logical.AndExpression;
import expression.logical.EqualExpression;
import expression.logical.InfEqualExpression;
import expression.logical.InfExpression;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.logical.SupEqualExpression;
import expression.logical.SupExpression;
import expression.numeric.DivExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.numeric.MinusExpression;
import expression.numeric.NumericExpression;
import expression.numeric.PlusExpression;
import expression.numeric.TimeExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import expression.variables.Variable.Use;


/** to visit a JML specification and build an expression
 * 
 * uses jml-release.jar for parsing JML specifications
 * see http://sourceforge.net/project/showfiles.php?group_id=65346&package_id=62764&release_id=594123
 * for downloading JML tools
 * 
 * see http://www.cs.ucf.edu/~leavens/JML/ for information about JML
 * 
 *
 * @version November 2010
 * @author Hélène Collavizza, Olivier Ponsini
 * from an original student work by Eric Le Duff and Sébastien Derrien
 * Polytech'Nice Sophia Antipolis
 */

public class Jml2CFGVisitor implements JmlVisitor {

	private String methodName;

	private String className;

	// jml.get("foo") returns the Expression which corresponds
	// to the jml specification of method foo
	private HashMap<String, JmlPredicate[]> jml;
	
	// JLM require clause
	private JmlPredicate require;

	// JLM ensure clause
	private JmlPredicate ensure;
	
	/**
	 * Maps positions (line, column) in the source file with the corresponding JML
	 * clauses 
	 */
	private SortedMap<AssertPosition, JmlPredicate> assertionJML;
	
	// stack of expressions which are currently explored
	private ExpressionStack expressions;
		
	// set of variables declared as parameters of the method
	// TODO : when parameter variables appear in ensures statements,
	//  then their old values should be used (i.e \old is implicit)
	// see http://www.eecs.ucf.edu/~leavens/JML//OldReleases/jmlrefman.pdf p 77
	private SymbolTable symbols;
	
	// to deal with negative numbers
	private boolean minus = false;
	
	// to know if we are parsing an expression inside the \old operator
	private boolean isOld = false;
	
	// set of variables declared in JML ForAll or Exists
	// when parsing JML ForAll or Exists, the variable
	// associated with a quantifier does not depend 
	// on the current state
	// so we do not generate a lambda expression on state.	
	//private ArrayList<String> quantifiedVar;
	
	// becomes true when the JML specification contains nodes
	// which are not yet handled
	private boolean error = false; 

    // private constructor 
	// param is the list of parameters that have been found
	// when parsing the method signature
	private Jml2CFGVisitor(String mn, String className, SymbolTable symbols, HashMap<String, JmlPredicate[]> spec) {
		methodName = mn;
		this.className = className;
		jml = spec;
		expressions = new ExpressionStack();
		this.symbols = symbols;
		assertionJML = new TreeMap<AssertPosition, JmlPredicate>();
	}
	
	/** constructor used when parsing the pre and post conditions
	 * 
	 * @param spec : set of specification associated with methods
	 * @param cn : name of the class (used for global variables)
	 */
	public Jml2CFGVisitor(HashMap<String, JmlPredicate[]> spec, String cn) {
		this("", cn, new SymbolTable(), spec);
	}
	
	/** constructor used to visit assert statements
	 * 
	 * @param mn : method name
	 * @param cm : class name
	 * @param symbols : parameters
	 */
	public Jml2CFGVisitor(String mn, String cm, SymbolTable symbols){
		this(mn, cm, symbols, new HashMap<String, JmlPredicate[]>());
	}
	
	
	
	// ########### Functions for accessing specifications #####
	
	// to return the set of jml specifications for all methods of a class
	public Map<String, JmlPredicate[]> getJml() {
		return jml;
	}
	
	// to return assertions in code
	// map a position in the java code to the JmlPredicate of the assertion
	// the JmlPredicate is translated as an Expression in Java2CFGVisitor because we need
	// to know the current symbol table (for SSA renaming)
	public SortedMap<AssertPosition, JmlPredicate> getAssertionJML() {
		return assertionJML;
	}
	
	public boolean hasError() {
		return error;
	}

	// to return the expression on the top of the stack and pop it
	// this is required for assertions which are translated as Expressions inside Java2CFG
	// (because we need the current symbol table)
	public Expression getLastExpression() {
		return expressions.pop();
	}
	
	// general functions to visit the JML specification
	// ================================================
	
	// to visit the specification of all methods
	public void visitJmlClassDeclaration(JmlClassDeclaration self) {
		// to add field names into the symbol table
		for (org.multijava.mjc.JFieldDeclarationType decl : self.fields())
				decl.accept(this);
		acceptAll(self.methods() );
	}

	public void visitJmlCompilationUnit(JmlCompilationUnit self) {
		JTypeDeclarationType[] tdecls = self.combinedTypeDeclarations();
		for (int i = 0; i < tdecls.length ; i++) {
		    tdecls[i].accept(this);
		}
	}
	
	public void visitJmlSpecification(JmlSpecification self) {
		JmlSpecCase[] specCases = self.specCases();
		visitSpecCases(specCases);
	}
	
	// private method to visit all specification cases
    private void visitSpecCases(JmlSpecCase[] specCases) {
		if (specCases != null) {
		    for (int i = 0; i < specCases.length; i++) {
		    	specCases[i].accept(this);
		    }
		}
	}

    // private method to visit all elements of an ArrayList
	private void acceptAll(ArrayList<?> all ) {	
    	if (all != null) {
    		for (int i = 0; i < all.size(); i++) {
				JPhylum j = (JPhylum) all.get(i);
				j.accept(this);
			}
    	}
    }
	
	// add a specification for the current method
    public void visitJmlMethodDeclaration(JmlMethodDeclaration self ) {
 		//System.out.println("jml method declaration " + self.ident());

    	// name used to prefix local variables
    	methodName = self.ident() + "_0";
    	
		// if there is a specification
    	JmlPredicate[] spec = new JmlPredicate[2];
    	if (self.hasSpecification()) {
    		// parse the specification with the current function name and 
    		// symbol table
    		require = null;
    		ensure = null;
    		// use to store the ensure and requires clauses as jmlPredicates
     		self.methodSpecification().accept(this);
     		spec[0] = require;
     		spec[1] = ensure;
     	}       	    	
    	
    	// add the specification into the table
     	jml.put(self.ident(), spec);
	
       	// Now parse the method body to deal with assertions inside the Java program
       	expressions = new ExpressionStack();
    	self.body().accept(this);
    }
    
	//---------------------------------------------------------------------------
	//JAVA CONSTRUCTS
	//---------------------------------------------------------------------------	

    // this is required to deal with assertions inside java program
    
	public void visitBlockStatement(JBlock self) {
		for(JStatement s: self.body()) {
			s.accept(this);
		}
	}

	public void visitWhileStatement(JWhileStatement self) {
		self.body().accept(this);
	}

	public void visitIfStatement(JIfStatement self) {
		self.thenClause().accept(this);
		if(self.elseClause() != null)
			self.elseClause().accept(this);
	}
    	
	public void visitJmlGenericSpecBody(JmlGenericSpecBody self) {
		JmlSpecBodyClause[] specClauses = self.specClauses();
		JmlGeneralSpecCase[] specCases = self.specCases();
		
		if (specClauses != null) {
		    for (int i = 0; i < specClauses.length; i++) {
			specClauses[i].accept(this);
		    }
		}
		if (specCases != null)
		    visitSpecCases(specCases);
	}

	public void visitJmlGenericSpecCase(JmlGenericSpecCase self) {
		JmlSpecVarDecl[] specVarDecls = self.specVarDecls();
		JmlRequiresClause[] specHeader = self.specHeader();
		JmlSpecBody specBody = self.specBody();
		
		if (specVarDecls != null) {
		    for (int i = 0; i < specVarDecls.length; i++) {
		    	specVarDecls[i].accept(this);
		    }
		}

		if (specHeader != null) {
		    for (int i = 0; i < specHeader.length; i++) {
			specHeader[i].accept(this);
		    }
		}

		if (specBody != null)
		    specBody.accept(this);
	}
	
	// requires clause
	public void visitJmlRequiresClause(JmlRequiresClause self) {
		require = self.predOrNot();
	}
	
	// ensure clause
	public void visitJmlEnsuresClause(JmlEnsuresClause self) {
		ensure = self.predOrNot();	
	}
	
	// \result variable in JML specification
	// TODO: case where result is an array 
	public void visitJmlResultExpression(JmlResultExpression self) {
		expressions.push(symbols.get(methodName + "_JMLResult"));
	}
	
	public void visitJmlPredicate(JmlPredicate self) {
		self.specExpression().accept(this);
	}
	
	public void visitJmlSpecExpression(JmlSpecExpression self) {
		self.expression().accept(this);
	}
	
	public void visitParenthesedExpression(JParenthesedExpression self) {
		self.expr().accept(this);
		expressions.push(new ParenExpression(expressions.pop()));
	}
	
	// relational expressions
	public void visitJmlRelationalExpression(JmlRelationalExpression self) {
		Expression result = null;

		self.left().accept(this);
		self.right().accept(this);
		//System.out.println(self + " " + self.oper());

		Expression right = expressions.pop();
		Expression left = expressions.pop();
		
		switch(self.oper()) {
			case 14: 
				result=new InfExpression(left,right); 
				break;
			case 15: 
				result=new InfEqualExpression(left,right); 
				break;
			case 16: 
				result=new SupExpression(left,right); 
				break;
			case 17: 
				result=new SupEqualExpression(left,right); 
				break;
			case 30: 
				result=new JMLImpliesExpression((LogicalExpression)left, 
												(LogicalExpression)right); 
				break;
		}	
		expressions.push(result);
	}
	
	public void visitEqualityExpression(JEqualityExpression self) {
		self.left().accept(this);
		self.right().accept(this);
		
		Expression right = expressions.pop();
		Expression left = expressions.pop();

		LogicalExpression result = new EqualExpression(left,right);
		if (self.oper() == JEqualityExpression.OPE_NE) {
			result = new NotExpression(result);
		}	
		expressions.push(result);
	}
	
	// logical expressions
	public void visitConditionalOrExpression(JConditionalOrExpression self) {
		//System.out.println(self.getType());
		self.left().accept(this);
		self.right().accept(this);
		LogicalExpression right = (LogicalExpression)expressions.pop();
		LogicalExpression left = (LogicalExpression)expressions.pop();
		Expression result = new OrExpression(left, right);
		expressions.push(result);
	}
	
	public void visitConditionalAndExpression(JConditionalAndExpression self) {
		self.left().accept(this);
		self.right().accept(this);
		LogicalExpression right = (LogicalExpression)expressions.pop();
		LogicalExpression left = (LogicalExpression)expressions.pop();
		Expression result = new AndExpression(left,right);
		expressions.push(result);
	}
	
	// integer expressions
		
	public void visitOrdinalLiteral(JOrdinalLiteral self) {
		String val = minus ? "-"+self.toString(): self.toString();
		expressions.push(new IntegerLiteral(val));
	}
	
	public void visitAddExpression(JAddExpression self) {
		self.left().accept(this);
		self.right().accept(this);
		NumericExpression right = (NumericExpression)expressions.pop();
		NumericExpression left = (NumericExpression)expressions.pop();
		expressions.push(new PlusExpression(left, right));
	}
	
	public void visitDivideExpression(JDivideExpression self) {
		self.left().accept(this);
		self.right().accept(this);
		NumericExpression right = (NumericExpression)expressions.pop();
		NumericExpression left = (NumericExpression)expressions.pop();
		expressions.push(new DivExpression(left,right));
	}
	
	public void visitMinusExpression(JMinusExpression self) {
		self.left().accept(this);
		self.right().accept(this);
		NumericExpression right = (NumericExpression)expressions.pop();
		NumericExpression left = (NumericExpression)expressions.pop();
		expressions.push(new MinusExpression(left,right));
	}
	
	public void visitMultExpression(JMultExpression self) {
		self.left().accept(this);
		self.right().accept(this);
		NumericExpression right = (NumericExpression)expressions.pop();
		NumericExpression left = (NumericExpression)expressions.pop();
		expressions.push(new TimeExpression(left,right));		
	}
		
	// parse unary expression: 
	// it can be not or - operator
	public void visitUnaryExpression(JUnaryExpression self) {
		if (self.oper()==2) 
			 minus = true;
		self.expr().accept(this);
		if (minus)
			minus = false;
		else {
			LogicalExpression previous = (LogicalExpression)expressions.pop();
			expressions.push(new NotExpression(previous));
		}
	}

	// variables
	public void visitJmlVariableDefinition(JmlVariableDefinition self) {
//		expressions.push(new Variable(methodName + "_" + self.ident(), validation.util.Type.INT));
		System.err.println("Error: JML ghost variables (" + self.ident() + ") are not yet supported!");
		System.exit(-1);
	}

	/**
	 * @param name
	 * @return the variable of name n
	 * 
	 * if isOld is true, it returns the first renaming of n, else it returns
	 * the current renaming of n
	 */
	private Variable getPrefixedVar(String n){
		if (isOld) 
			// we get the first version of the variable
			return symbols.getPrefixed(methodName, className,n,0);
		// we get the current version of the variable
		return symbols.getPrefixed(methodName, className, n);

	}
	
	// visit a variable name
	// if it is an access to the array length, then it is
	// evaluated on "state" when parsing the precondition
	// else it is evaluated on state1
	public void visitNameExpression(JNameExpression self) {
		// access to array length
		if (self.getPrefix() != null && self.getName() == "length") {
			ArrayVariable av = symbols.getPrefixedArray(methodName, className, self.getPrefix().toString());
			expressions.push(new IntegerLiteral(av.length()));
		}
		else {
			//System.out.println("var name "+self.getName());
//			System.out.println("dans visitNameJML " + methodName + "var name "+self.getName());
			expressions.push(getPrefixedVar(self.getName()));
		}
	}
	
	// \old statement
	// to use the value of the variable before execution of the method	
	// self.specExpression() is the JmlSpecExpression inside the \old statement
	// NOTA: the \old operator is distributed on the expression
	// For example,  \old(var1 + var2) is built as the expression Plus(JMLOld(var1),JMLOld(var2))
	public void visitJmlOldExpression(JmlOldExpression self) {
		isOld = true;
        self.specExpression().accept(this);
        isOld=false;
	}

	// to visit JML forall and exists statements
	public void visitJmlSpecQuantifiedExpression(JmlSpecQuantifiedExpression self) {

		// quantified-var-decl
		JVariableDefinition[] tab = self.quantifiedVarDecls();
		if (tab.length!=1) {
			System.out.println("Error: JML quantifier for more than one variable");
			error=true;
		}
		else {
			if (!self.hasPredicate()) {
				System.out.println("Error: no bound expression in JML quantifier");
				error=true;
			}
			else {
				// the variable
//				System.out.println(tab[0].ident());
				Variable var=null;
				try {
					if (symbols.containsKey(tab[0].ident()))
						throw new AnalyzeException("JML quantifier varialbe already exist in the program");
					var = symbols.addNewPrefixedVar(methodName, tab[0].ident(),Type.parseType(tab[0].getType().toString()), Use.JML_QUANTIFIED);
				} catch (AnalyzeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
				// bound satisfied by the quantified variable
//				System.out.println(self.predicate());
				self.predicate().accept(this);
				Expression bound = expressions.pop();
				// the expression to be satisfied
				self.specExpression().accept(this);
				Expression cond = expressions.pop();
				// quantifier
				Expression jmlQuantifier;
				if (self.isForAll()) {
					 jmlQuantifier = new JMLForAllExpression(var, (LogicalExpression)bound, (LogicalExpression)cond);
				}
				else {
					 jmlQuantifier = new JMLExistExpression(var, (LogicalExpression)bound, (LogicalExpression)cond);
				}
				expressions.push(jmlQuantifier);
			}
		}
}


	
	// array access
	public void visitArrayAccessExpression(JArrayAccessExpression self) {
		//System.out.println("array access in JML visitor");
		String name;
		// for statements such as \old(a)[i]
		if (self.prefix() instanceof JmlOldExpression){
			this.isOld = true;
			name = ((JmlOldExpression)self.prefix()).specExpression().toString();
			// visit the array index
			self.accessor().accept(this);
			NumericExpression index = (NumericExpression)expressions.pop();
			expressions.push(new ArrayElement((ArrayVariable)getPrefixedVar(name), index));
			this.isOld = false;
		}
		else {
			// name of the array
			name = self.prefix().toString();
			// visit the array index
			self.accessor().accept(this);
			NumericExpression index = (NumericExpression)expressions.pop();
			expressions.push(new ArrayElement((ArrayVariable)getPrefixedVar(name), index));
		}
	}
	
	
	// add a link between the position in the program and the jml predicate
	public void visitJmlAssertStatement(JmlAssertStatement self) {
		// TODO : garder le message
//		if(self.hasThrowMessage()) {
//			write("<IDSJMLAssert message=" + self.throwMessage() + ">");

		assertionJML.put(new AssertPosition(self.getTokenReference().line(), 
				                      self.getTokenReference().column()),
				                      self.predicate());
	}
	
	public void visitRealLiteral(JRealLiteral self) {
		String val = minus ? "-"+ self.toString() : self.toString();
		
		if (self.getType().getTypeID() == JRealLiteral.TID_FLOAT) {
			expressions.push(new FloatLiteral(val));
		}
		else if (self.getType().getTypeID() == JRealLiteral.TID_DOUBLE) {
			expressions.push(new DoubleLiteral(val));
		}
		else {
			System.err.println("Unknown JML real type: " + self);
			System.exit(8);
		}
	}
	
	public void visitBooleanLiteral(JBooleanLiteral self) {
		expressions.push(new LogicalLiteral(self.booleanValue()));
	}
	
	//====================================================
	// here are non implemented methods 
	// to parse all possible nodes in JML required by 
	// JmlVisitor interface
	
	//====================================================
	// java statements which are not yet implemented but do not throw an error.
	// one needs to visit these statements when there some assertions inside the 
	// java code 
	// ===================================================
	
	public void visitJmlFieldDeclaration(JmlFieldDeclaration self) {
		// Left blank intentionally
	}

	public void visitReturnStatement(JReturnStatement self) {
//		System.out.println("Return statement is ignored by JML visitor..."); 
	}
	
	public void visitVariableDeclarationStatement(JVariableDeclarationStatement self) {
//		System.out.println("Variable declaration is ignored by JML visitor..."); 
	}

	public void visitJmlAssignmentStatement(JmlAssignmentStatement self) {
//		System.out.println("JmlAssignmentStatement is ignored by JML visitor..."); 
	}
	
	// usefull for main that contains a method call like
	// System.out.println
	public void visitExpressionStatement(JExpressionStatement self) {
//		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
	}
	
	public void visitMethodCallExpression(JMethodCallExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
	}
	

	//====================================================
	// java statements which are not yet implemented and throw an error.
	// ===================================================
	public void visitFieldExpression (JClassFieldExpression self) {
		System.err.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlConstructorDeclaration(JmlConstructorDeclaration self) {
		System.err.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitArrayDimsAndInit(JArrayDimsAndInits self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}
		
	public void visitArrayInitializer(JArrayInitializer self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitArrayLengthExpression(JArrayLengthExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlAbruptSpecBody(JmlAbruptSpecBody self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlAbruptSpecCase(JmlAbruptSpecCase self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlAccessibleClause(JmlAccessibleClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlAssignableClause(JmlAssignableClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlAssumeStatement(JmlAssumeStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlAxiom(JmlAxiom self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlBehaviorSpec(JmlBehaviorSpec self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlBreaksClause(JmlBreaksClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlCallableClause(JmlCallableClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlCapturesClause(JmlCapturesClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlClassBlock(JmlClassBlock self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlClassOrGFImport(JmlClassOrGFImport self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlCodeContract(JmlCodeContract self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlConstraint(JmlConstraint self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}


	public void visitJmlConstructorName(JmlConstructorName self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlContinuesClause(JmlContinuesClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlDebugStatement(JmlDebugStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlDeclaration(JmlDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlDivergesClause(JmlDivergesClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlDurationClause(JmlDurationClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlDurationExpression(JmlDurationExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlElemTypeExpression(JmlElemTypeExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlExample(JmlExample self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlExceptionalBehaviorSpec(JmlExceptionalBehaviorSpec self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlExceptionalExample(JmlExceptionalExample self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlExceptionalSpecBody(JmlExceptionalSpecBody self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlExceptionalSpecCase(JmlExceptionalSpecCase self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlExpression(JmlExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlExtendingSpecification(JmlExtendingSpecification self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlForAllVarDecl(JmlForAllVarDecl self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlFormalParameter(JmlFormalParameter self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlFreshExpression(JmlFreshExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlGeneralSpecCase(JmlGeneralSpecCase self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlGuardedStatement(JmlGuardedStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlHenceByStatement(JmlHenceByStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInGroupClause(JmlInGroupClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInformalExpression(JmlInformalExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInformalStoreRef(JmlInformalStoreRef self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInitiallyVarAssertion(JmlInitiallyVarAssertion self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInterfaceDeclaration(JmlInterfaceDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInvariant(JmlInvariant self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInvariantForExpression(JmlInvariantForExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlInvariantStatement(JmlInvariantStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlIsInitializedExpression(JmlIsInitializedExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlLabelExpression(JmlLabelExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlLetVarDecl(JmlLetVarDecl self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlLockSetExpression(JmlLockSetExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlLoopInvariant(JmlLoopInvariant self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlLoopStatement(JmlLoopStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlMapsIntoClause(JmlMapsIntoClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlMaxExpression(JmlMaxExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlMeasuredClause(JmlMeasuredClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlMethodName(JmlMethodName self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlMethodSpecification(JmlMethodSpecification self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlModelProgram(JmlModelProgram self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlMonitorsForVarAssertion(JmlMonitorsForVarAssertion self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlName(JmlName self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNode(JmlNode self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNonNullElementsExpression(JmlNonNullElementsExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNondetChoiceStatement(JmlNondetChoiceStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNondetIfStatement(JmlNondetIfStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNormalBehaviorSpec(JmlNormalBehaviorSpec self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNormalExample(JmlNormalExample self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNormalSpecBody(JmlNormalSpecBody self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNormalSpecCase(JmlNormalSpecCase self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNotAssignedExpression(JmlNotAssignedExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlNotModifiedExpression(JmlNotModifiedExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlOnlyAssignedExpression(JmlOnlyAssignedExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlPackageImport(JmlPackageImport self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlReachExpression(JmlReachExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlReadableIfVarAssertion(JmlReadableIfVarAssertion self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlRedundantSpec(JmlRedundantSpec self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlRefinePrefix(JmlRefinePrefix self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlRepresentsDecl(JmlRepresentsDecl self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlReturnsClause(JmlReturnsClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSetComprehension(JmlSetComprehension self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSetStatement(JmlSetStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSignalsClause(JmlSignalsClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSignalsOnlyClause(JmlSignalsOnlyClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSpaceExpression(JmlSpaceExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSpecBody(JmlSpecBody self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSpecStatement(JmlSpecStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlSpecVarDecl(JmlSpecVarDecl self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlStoreRef(JmlStoreRef self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlStoreRefExpression(JmlStoreRefExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlStoreRefKeyword(JmlStoreRefKeyword self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlTypeExpression(JmlTypeExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlTypeOfExpression(JmlTypeOfExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlUnreachableStatement(JmlUnreachableStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlVariantFunction(JmlVariantFunction self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlWhenClause(JmlWhenClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlWorkingSpaceClause(JmlWorkingSpaceClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlWorkingSpaceExpression(JmlWorkingSpaceExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlWritableIfVarAssertion(JmlWritableIfVarAssertion self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitAssertStatement(JAssertStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitAssignmentExpression(JAssignmentExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitBitwiseExpression(JBitwiseExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitBreakStatement(JBreakStatement self) {
		System.out.println("Visiting method for " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitCastExpression(JCastExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitCatchClause(JCatchClause self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitCharLiteral(JCharLiteral self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitClassBlock(JClassBlock self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitClassDeclaration(JClassDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitClassExpression(JClassExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitClassOrGFImport(JClassOrGFImport self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitCompoundAssignmentExpression(JCompoundAssignmentExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;	
	}

	public void visitCompoundStatement(JCompoundStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitConditionalExpression(JConditionalExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitConstructorBlock(JConstructorBlock self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitConstructorDeclaration(JConstructorDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitContinueStatement(JContinueStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitDoStatement(JDoStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitEmptyStatement(JEmptyStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitExplicitConstructorInvocation(JExplicitConstructorInvocation self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitExpressionListStatement(JExpressionListStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitFieldDeclaration(JFieldDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}


	public void visitForStatement(JForStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitFormalParameters(JFormalParameter self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitGenericFunctionDecl(MJGenericFunctionDecl self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitInitializerDeclaration(JInitializerDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitInstanceofExpression(JInstanceofExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitInterfaceDeclaration(JInterfaceDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitLabeledStatement(JLabeledStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitLocalVariableExpression(JLocalVariableExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitMathModeExpression(MJMathModeExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitMethodDeclaration(JMethodDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitModuloExpression(JModuloExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitNewAnonymousClassExpression(JNewAnonymousClassExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitNewArrayExpression(JNewArrayExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitNewObjectExpression(JNewObjectExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitNullLiteral(JNullLiteral self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitPackageImport(JPackageImport self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitPackageName(JPackageName self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitPostfixExpression(JPostfixExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitPrefixExpression(JPrefixExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitRelationalExpression(JRelationalExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitShiftExpression(JShiftExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitStringLiteral(JStringLiteral self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitSuperExpression(JSuperExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitSwitchGroup(JSwitchGroup self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitSwitchLabel(JSwitchLabel self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitSwitchStatement(JSwitchStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitSynchronizedStatement(JSynchronizedStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitThisExpression(JThisExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitThrowStatement(JThrowStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitTopLevelMethodDeclaration(MJTopLevelMethodDeclaration self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitTryCatchStatement(JTryCatchStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitTryFinallyStatement(JTryFinallyStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitTypeDeclarationStatement(JTypeDeclarationStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitTypeNameExpression(JTypeNameExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitUnaryPromoteExpression(JUnaryPromote self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitVariableDefinition(JVariableDefinition self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitWarnExpression(MJWarnExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlMethodNameList(JmlMethodNameList self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlOnlyAccessedExpression(JmlOnlyAccessedExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlOnlyCalledExpression(JmlOnlyCalledExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlOnlyCapturedExpression(JmlOnlyCapturedExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlPreExpression(JmlPreExpression self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlPredicateKeyword(JmlPredicateKeyword self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitCompilationUnit(JCompilationUnit self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}

	public void visitJmlRefiningStatement(JmlRefiningStatement self) {
		System.out.println("Visiting method for  " + self.getClass() + " is not yet implemented"); 
		error=true;
	}
}


