package java2CFGTranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.SortedMap;

import org.jmlspecs.checker.JmlCompilationUnit;
import org.jmlspecs.checker.JmlLexer;
import org.jmlspecs.checker.JmlMLLexer;
import org.jmlspecs.checker.JmlParser;
import org.jmlspecs.checker.JmlPredicate;
import org.jmlspecs.checker.JmlSLLexer;
import org.jmlspecs.checker.TokenStreamSelector;
import org.multijava.mjc.CModifier;
import org.multijava.mjc.Constants;
import org.multijava.mjc.JavadocLexer;
import org.multijava.mjc.Main;
import org.multijava.mjc.ParsingController;

import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.SymbolTable;

import java.util.HashMap;



/** main class to build an Expression from a JML specification
 * 
 * @version November 2010
 * @author Hélène Collavizza
 * from an original student work by Eric Le Duff and Sébastien Derrien
 * Polytech'Nice Sophia Antipolis
 */
public class Jml2CFG {

	private JmlCompilationUnit rootJml;
		
	// contains the expressions that represent the 
	// jml specifications of all methods in the file
	private HashMap<String, JmlPredicate[]> jmlSpec;
	
	/**
	 * Maps positions (line, column) in the source file with the corresponding JML
	 * clauses converted into valid expression.
	 * 
	 * used for Assert statement
	 */
	private SortedMap<AssertPosition, JmlPredicate> assertionJML;

	private ParsingController parsingController;
	private TokenStreamSelector lexingController;
	private JmlLexer jmlLexer;
	private JavadocLexer docLexer;
	private JmlMLLexer jmlMLLexer;
	private JmlSLLexer jmlSLLexer;
	public JmlParser parser;

	public Jml2CFG(File file) throws Java2CFGException {
		super();
		jmlSpec = new HashMap<String, JmlPredicate[]>();
		Main compiler = new Main( new CModifier( Constants.ACCESS_FLAG_ARRAY,Constants.ACCESS_FLAG_NAMES ) );
		Reader r = null;
		try {
			r = new FileReader(file);
		} catch (FileNotFoundException e) {
			throw new Java2CFGException("Cannot read " + file + ".");
		}
		parsingController = new ParsingController( r, null );
		lexingController = new TokenStreamSelector();
		boolean allowUniverses = true; 
		jmlLexer = new JmlLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		docLexer = new JavadocLexer( parsingController );
		jmlMLLexer = new JmlMLLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		jmlSLLexer = new JmlSLLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		try {
			lexingController.addInputStream( jmlLexer, "jmlTop" );
			lexingController.addInputStream( jmlMLLexer, "jmlML" );
			lexingController.addInputStream( jmlSLLexer, "jmlSL" );
			lexingController.addInputStream( docLexer, "javadoc" );
			lexingController.select( "jmlTop" );
			parsingController.addInputStream( lexingController, "jml" );
			parsingController.addInputStream( docLexer, "javadoc" );
			parsingController.selectInitial( "jml" );

			final boolean ACCEPT_MULTIJAVA = true;
			final boolean ACCEPT_RELAXEDMULTIJAVA = false;
			parser = 
				new JmlParser(compiler, 
						parsingController.initialOutputStream(),
						parsingController,
						false,
						ACCEPT_MULTIJAVA, 
						ACCEPT_RELAXEDMULTIJAVA,
						allowUniverses );
			rootJml  = (JmlCompilationUnit) parser.jCompilationUnit();
//TODO: 	parser.jAssertStatement(); //  fait avancer à la fin du fichier
		} catch (Exception e) {

			throw new Java2CFGException(errorDiagnostic(file));
		}
	}
	
	
	/** return a list of syntax constructs which are not yet parsed
	 * 
	 * @return
	 */
	private String errorDiagnostic(File file) {
		String message = "It was impossible to build the AST for file " + file + "\n";
		message+="Possible errors in the program: \n";
		message+="   - only primitive types and arrays of primitive types are allowed\n";
		message+="   - in \"if statements\", then and else blocks must be enclosed with {}\n";
		message+="   - \"switch statements\" are not allowed (use \"if else\" instead)\n";
		message+="   - \"for loops\" are not allowed (use \"while loops\" instead)\n";
		message+="   - JavaDoc comments are not allowed\n";
		message+="   - i++ or i-- are not permitted\n";
		message+="   - non deterministic values are only permitted in assigments\n";
		message+="     of the form: v=nondet-in();";
		message+="Possible error in JML specification: \n";
		message+="   - requires clause must precede ensures clause\n";
		message+="   - clauses must terminate with \";\"\n";
		message+="   see http://www.cs.ucf.edu/~leavens/JML/ for JML syntax";
		return message;
	}
	
	
	// ########### Function to start parsing
	public void parse(String className) throws Java2CFGException {
		Jml2CFGVisitor v = new Jml2CFGVisitor(jmlSpec, className);
		rootJml.accept(v);
		if (v.hasError()) 
			throw new Java2CFGException("\nJML specification contains statements that are not yet implemented.");
		assertionJML = v.getAssertionJML();
	}
	

	// ########### Functions for accessing specifications #####

	// return the JML specification of method s
	public JmlPredicate[] getJml(String s) {
		return jmlSpec.get(s);
	}
	
	// return the requires clause of specification of method s
	public JmlPredicate getRequires(String methodRootName) {
		return jmlSpec.get(methodRootName)[0];
	}
	
	// return the ensures clause of specification of method s
	public JmlPredicate getEnsures(String methodRootName) {
		return jmlSpec.get(methodRootName)[1];
	}

	// to return assertions in code
	public SortedMap<AssertPosition, JmlPredicate > getAssertionJML() {
		return assertionJML;
	}

	// to set the assertions 
	// this is necessary for a loop: assertions must be parsed 
	// for each unwinding of the loop
	public void setAssertion(SortedMap<AssertPosition, JmlPredicate> ass) {
		assertionJML=ass;
	}
}


