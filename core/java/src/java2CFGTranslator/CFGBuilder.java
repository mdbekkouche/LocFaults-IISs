package java2CFGTranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import validation.Validation.VerboseLevel;

import CFG.SetOfCFG;

/** main class to build the CFG of a Java file with a JML specification
 * 
 * 
 * @author Hélène Collavizza
 * from an original student work by Eric Le Duff and Sébastien Derrien
 * Polytech'Nice Sophia Antipolis
 */
public class CFGBuilder extends ASTVisitor {

	public static int MAX_UNWOUND=2;
	public static int MAX_ARRAY_LENGTH=5;

	
	CompilationUnit root;// the AST
	String filename; // the name of the file
	String methodToProve;;
	File fileToConvert;
	private int maxUnwound ; // maximum number of loop unwinding
	private int maxArrayLength ;
	
	public CFGBuilder(String fileName, String methodToProve) throws Java2CFGException {
		this(fileName, methodToProve, MAX_UNWOUND, MAX_ARRAY_LENGTH);
	}
	
	public CFGBuilder(String fileName, String methodToProve, int maxUnwound, int maxArrayLength) throws Java2CFGException {
		super();
		this.maxUnwound = maxUnwound;
		this.maxArrayLength = maxArrayLength;
		this.filename = fileName;
		this.methodToProve = methodToProve;
		fileToConvert = new File(fileName);
		long fileNbChars = fileToConvert.length();
		if (fileNbChars > Integer.MAX_VALUE) {
			System.err.println("Error: File is too big to be loaded!");
			System.exit(-1);
		}
		char[] source = new char[(int)fileNbChars];
		FileReader r;
		try {
			r = new FileReader(fileToConvert);
			r.read(source);
		} catch (FileNotFoundException e) {
			throw new Java2CFGException("No such file : " + fileToConvert);
		} catch (IOException e) {
			throw new Java2CFGException("Cannot read file : " + fileToConvert);
		}
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source);
		parser.setResolveBindings(true);
		root = (CompilationUnit) parser.createAST(null);
		
	}
	
	
	public SetOfCFG convert() throws Java2CFGException{
		Java2CFGVisitor j = new Java2CFGVisitor(filename, root, methodToProve, maxUnwound,maxArrayLength);
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\nStarting conversion of file: " + filename);
			System.out.println("Loops are unwound "+ maxUnwound + " times.");
		}
		root.accept(j);
		j.endVisit();
		return j.getProgram();
	}
}
