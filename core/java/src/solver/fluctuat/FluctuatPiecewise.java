package solver.fluctuat;

import java.util.Stack;

import validation.Validation;
import validation.Validation.VerboseLevel;

public class FluctuatPiecewise {
	
	public Fluctuat fluctuat;
	
	private Stack<Boolean> junctionInScope;
	/**
	 * Name of the C file that is analyzed.
	 */
	private String cFile;
	/**
	 * Name of the preprocessed file (.i) that corresponds to the analyzed C file.
	 */
	private String cppFile;
	/**
	 * Name of the resource file (.rc) for this analysis.
	 */
	private String rcFile;
	/**
	 * Name of the function that is analyzed.
	 */
	private String fctName;


	public FluctuatPiecewise(String fctName) {
		junctionInScope = new Stack<Boolean>();
		// Push value for the scope of the function 
		junctionInScope.push(false);
		String name = Validation.pgmFileName.split(".xml", 2)[0];
		cFile = name + ".c";
		cppFile = name + ".i";
		rcFile = name + ".rc";
		this.fctName = fctName;
		fluctuat = new Fluctuat(cppFile, rcFile, fctName);
	}
	
	/**
	 * @param line
	 * @return <code>true</code> if synchronizing was needed and successful; <code>false</code> otherwise.
	 */
	public boolean synchronize(int line) {
		if (fluctuat.status.line < line) {
			// Fluctuat analysis has not yet reached this line
			
			// We first remove all breakpoints because several pseudo-statements (e.g. end of block, label) 
			// may be associated with the same line as a previous breakpoint and debugContinue will not step 
			// over them for reaching the breakpoint newly set (instead, it will stop again on each pseudo-statement 
			// associated with the previous breakpoint)
			fluctuat.debugDelBreakPoints();
			
			fluctuat.debugAddBreakPoint(cFile, line);
			fluctuat.debugContinue(1);
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Synchro: CPBPV line = " + line + " Fluctuat line = " + fluctuat.status.line);
			}
			return true;
		}
		else if (fluctuat.status.line > line) {
			// Fluctuat analysis has already passed this line, we need to 
			// reinitialize the analysis
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Synchro: reinitializing Fluctuat analysis.");
			}
			fluctuat.debugClearAnalyzer();
			fluctuat.init(cppFile, rcFile, fctName);
			fluctuat.debugAddBreakPoint(cFile, line);
			fluctuat.debugContinue(1);
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Synchro: CPBPV line = " + line + " Fluctuat line = " + fluctuat.status.line);
			}
			return true;
		}
		else
			return false;
	}
	
}
