package validation.visitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import solver.fplib.FplibByPiecesVS;
import solver.fplib.FplibNativeShaving2BVS;
import solver.fplib.FplibShaveResValidationSystem;
import solver.fplib.FplibShaving2BVS;
import solver.realpaver.xml.RealPaverByPiecesVS;
import solver.realpaver.xml.RealPaverShavingVS;
import solver.realpaver.xml.RealPaverXmlResultShaving;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.strategies.xml.CoverageStrategy;
import validation.system.xml.ValidationSystem;
import validation.system.xml.ValidationSystemXmlCallbacks;
import validation.util.ChildIterator;
import exception.AnalyzeException;

//main class to enable parsing and validation on the fly

//gestion des variables SSA : 
//les variables initiales servent à vérifier que la var existe et a tenir le compteur 
//des renomages
//à chaque renomage, on ajoute dans
//le système une variable avec le nom correspondant
//pour l'instant, pas de var intermédiaires ni de renomage dans JML

public class XMLVisitAndValidate extends XMLVisitor {

	private String name;

	// the system for validation which includes the CSPs
	private ValidationSystemXmlCallbacks constSyst ;
	// DOM structure of XML file
	private Document document; 


	// default constructor
	public XMLVisitAndValidate(File in) {
		name=in.getName();
		initDocument(in);
	}
	
	// gets DOM structure from XML file
	private void initDocument(File in) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);   
		factory.setNamespaceAware(false);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(in);
		} catch (SAXException sxe) {
			// Error generated during parsing
			System.err.println("Exception SAX!");
			Exception  x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();
			System.exit(7);
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			System.err.println("Exception config!");
			pce.printStackTrace();
			System.exit(7);
		} catch (IOException ioe) {
			// I/O error
			System.err.println("Can't read xml input file!");
			ioe.printStackTrace();
			System.exit(7);
		}
	}

	/** parse and verify "document"
	 * add constraints in ConstSyst and solve it at the end of each path
	 * @param byCases <code>true</code> if the post-condition must be negated and divided into several
	 *                disjunctive cases; <code>false</code> if the post-condition must be left as is.
	 * @throws AnalyzeException
	 * @throws ConstraintException
	 */
//	<!ELEMENT methodComment (#PCDATA)>
	//
//			<!-- signature = parametres -->
//			<!ELEMENT IDSSignature (IDSParameter+)>
//			<!ATTLIST IDSSignature returnType CDATA #IMPLIED
//			                    name CDATA #IMPLIED>
	public void validate(boolean byCases) throws AnalyzeException {
		//Validation system creation
		if (Validation.pathCoverage) {
			constSyst = new CoverageStrategy(name);
		}
		else {
			constSyst = ValidationSystem.createXmlValidationSystem(name);
		}
		
		// starting node of validation
		ChildIterator child = new ChildIterator(document);
		Node next = child.next();
		
//		<!-- fichier a analyser -->
//		<!ELEMENT method (methodComment+,IDSMethod)>
		if (!isMethod(next)) exception(1);
		child= new ChildIterator(next);
		next = child.next();
		
		// skip unused comment
		if (isMethodComment(next)) {
			//parseMethodComment(next,constSyst);
			next = child.next();
		}
		if (!isIDSMethod(next)) exception(2);
		child = new ChildIterator(next);
		
//		<!-- signature, specification, program -->
//		<!ELEMENT IDSMethod (IDSSignature,IDSMethodSpecification,IDSJavaBloc)>
		// parse method signature and add variables in constSyst for each parameter
		next = child.next();
		SignatureVisitor.visitMethodSignature(next, constSyst);
		
		// parse the JML specification requires clause and add corresponding constraints in constSyst
		// and parse the JML ensures clause and return its negation as a disjunction
		next=child.next();
		ArrayList<EnsureCase> spec = SpecificationVisitor.visitJML(next, constSyst, byCases);

		// here is the java block of the program
		// need to traverse it many times for each specification part
		constSyst.setParsingMethodBody();
		next = child.next();
		
		//CPBPV mode: either result domain shaving, path coverage, or conformity validation
		if (Validation.shaving) {
			if (Validation.solverCombination == solver.Solver.SolverCombination.FPLIB) {
				if (Validation.shave3B)
					((FplibShaveResValidationSystem)constSyst).shave3B(name, next);
				else { // 2B shaving
					if (Validation.piecewise) {
						((FplibByPiecesVS)constSyst).shave(
								Validation.shavingFileName, 
								Validation.fplibkBprecision, 
								Validation.fplib2Bprecision,
								next);
					}	
					else if (Validation.native2BShaving) {
						((FplibNativeShaving2BVS)constSyst).shave2B(
								Validation.shavingFileName, 
								Validation.fplib2Bprecision, 
								Validation.shavingAbsolutePrecision, 
								Validation.shavingRelativePrecision,
								next);
					}
					else {
						//((FplibShaveResValidationSystem)constSyst).shaveRes(name , next);
						((FplibShaving2BVS)constSyst).shave2B(Validation.shavingFileName, Validation.fplib2Bprecision, 
	 							                              Validation.shavingAbsolutePrecision, Validation.shavingRelativePrecision,
	 							                              next);
					}
				}
			}
			else if (Validation.solverCombination == solver.Solver.SolverCombination.REAL_PAVER) {
				if (Validation.shave3B)
					((RealPaverXmlResultShaving)constSyst).shave3B(name , next);
				else {
					if (Validation.piecewise) {
						((RealPaverByPiecesVS)constSyst).shave(
								Validation.shavingFileName, 
								next);					
					}
					else {
						((RealPaverShavingVS)constSyst).shave(
								Validation.shavingFileName, 
								Validation.shavingAbsolutePrecision, 
				                Validation.shavingRelativePrecision,
				                next);				
					}
				}
			}
			else {
				System.err.print("Error: Shaving mode is only supported with FPLIB and ");
				System.err.println("REAL_PAVER solver combinations!");
				System.exit(-1);
			}
		}
		else if (Validation.pathCoverage) {  //Path coverage
			cover(next, spec);
		}
		else {  //Conformity validation
			validateAllCases(next, spec);
		}
	}
	
	/** validate all the specification cases
	 * @param next : first node of the program
	 * @param spec : set of specification cases
	 * @throws ConstraintException 
	 * @throws AnalyzeException 
	 */
	private void cover(Node next, ArrayList<EnsureCase> spec) throws AnalyzeException  {
		if (VerboseLevel.QUIET.mayPrint()) {
			System.out.println("\nStarting path coverage of " + name);
			System.out.println("........................");
		}

		Node n = next.cloneNode(true);
		Solution sol= new Solution();
		validate(n, spec.get(0), sol);
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("No more paths to explore");
			System.out.println(constSyst.printStatus());
		}
	}

	/** validate all the specification cases
	 * @param next : first node of the program
	 * @param spec : set of specification cases
	 * @throws ConstraintException 
	 * @throws AnalyzeException 
	 */
	private void validateAllCases(Node next, ArrayList<EnsureCase> spec) throws AnalyzeException  {
		boolean ok = true;
		int nbSpecPart = 0;
		
		if (VerboseLevel.QUIET.mayPrint()) {
			System.out.println("\nStarting verification of " + name);
			System.out.println("........................");
		}

		for (EnsureCase s : spec) {
			nbSpecPart++;
			// TODO : attention aux champs data qui ne sont pas copiés par le clone
			//        attention au partage des références
			Node n = next.cloneNode(true);
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("\nStarting verification of part " 
					           + nbSpecPart 
					           + " of specification.");
				System.out.println("...................");
			}
			Solution sol= new Solution();
			validate(n,s,sol);
			if (!sol.isEmpty()) {
				if (VerboseLevel.TERSE.mayPrint()) {
					System.out.println("\n--------------------");
					System.out.println("Program is NOT conform with case\n"  
						           + "!" + s 
						           + "\nof the specification!");
				}
				ok = false;
			}
			else 
				if (VerboseLevel.TERSE.mayPrint()) {
					System.out.println("Program is conform with case\n"  
						           + "!" + s 
						           + "\nof the specification!");
				}
			constSyst.resetPath();
		}
		if (VerboseLevel.QUIET.mayPrint()) {
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("No more paths to explore");
			}
			if (ok) 
				System.out.println("Program is conform with its specification!");
			else 
				System.out.println("Program is NOT conform with some specification case!");
			System.out.println(constSyst.printStatus());
		}
	}

	
	/** parse the program starting from node n
	 * and validate it with respect to case spec of the JML specification
	 * @param child : current node
	 * @param spec : current specification case
	 * @return a Solution (possibly empty)
	 * @throws AnalyzeException
	 * @throws ConstraintException
	 * POSTCOND : n is on leaf of the program or on a n ode where a counter-example has been found
	 */
	private void validate(Node n, EnsureCase s, Solution result) throws AnalyzeException {
        // add constraint from s into the different solvers of constSyst
		// add integer constraints (linear or non linear) to completeCSP
        // add non linear constraints as boolean variables and set the associations in boolCSP
        // add linear constraints of s in the linearCSP 
		constSyst.save();
		constSyst.setPostcond(s);    
		Stack<ChildIterator> exitNodes = new Stack<ChildIterator>();
		JavaBlockVisitAndValidate.validateBlock(n, constSyst, result, exitNodes);
		constSyst.restore();
	}
	
}
