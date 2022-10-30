package solver.fluctuat;

/**
 * Gives native access to Fluctuat library.
 * 
 * @author Olivier Ponsini
 *
 */
public class Fluctuat {

	static {
    	System.loadLibrary("jni_Fluctuat");
    }

	/**
	 * A class to store all information related to debugging status.
	 * @author ponsini
	 *
	 */
	public class DebugStatus {
		String point;
		String sourceFile;
		int line;
		int status;
		
		public String toString() {
			return "Point: " + point +
				"\nSource: " + sourceFile +
				"\nLine: "   + line +
				"\nStatus: " + status;
		}
		
//		public boolean isJunction() {
//			return point.startsWith("LabelControlPoint");
//		}

		public boolean isConditional() {
			return point.startsWith("ConditionalControlPoint");
		}

		public boolean isStartBlock() {
			return point.startsWith("StartBlockControlPoint");
		}

		public boolean isEndBlock() {
			return point.startsWith("EndBlockControlPoint");
		}
		
		public boolean isReturn(String fctName) {
			return point.startsWith("AssignControlPoint") && (point.indexOf(fctName + " = ", 26) != -1);
		}
		
		public boolean endOfAnalysis() {
			return status != 2; 
		}		
	}
	
	public class DebugResult {
		public double minDomain;
		public double maxDomain;
		public double minFloatDomain;
		public double maxFloatDomain;
		public double minError;
		public double maxError;
		public boolean status;
		
		public void set(double[] bounds) {
			this.minDomain = bounds[0];
			this.maxDomain = bounds[1];
			this.minFloatDomain = bounds[2];
			this.maxFloatDomain = bounds[3];
			this.minError = bounds[4];
			this.maxError = bounds[5];			
		}		
		
		public String toString() {
			return "Real: [" + minDomain + " , " + maxDomain + "]\n"
				+ "Float: [" + minFloatDomain + " , " + maxFloatDomain + "]\n"
				+ "Error: [" + minError + " , " + maxError + "]\n"
				+ "Status: " + status;
				
		}

	}
	
	/**
	 * Stores the pointer to Fluctuat library entry object (DebugInterface). 
	 */
	long fluctuat = 0L;
	
	/**
	 * Stores the debugging current status.
	 */
	DebugStatus status;

	/**
	 * Stores approximations computed for the variable specified in the last 
	 * call to debugFromCurrentRetrieveResult.
	 */
	DebugResult lastVarIntervals = new DebugResult();
		
	public Fluctuat(String cppFile, String rcFile, String fctName) {
		System.out.println("Creating fluctuat debug interface.");
		fluctuat = createDebugInterface();
		init(cppFile, rcFile, fctName);
	}

	public void init(String cppFile, String rcFile, String fctName) {
		System.out.println("Adding fluctuat source file: " + cppFile);
		debugAddSourceFile(fluctuat, cppFile);
		System.out.println("Adding fluctuat resource file: " + rcFile);
		debugSetResourceFile(fluctuat, rcFile);
		System.out.println("Adding fluctuat function breakpoint: " + fctName);
		debugAddFunctionBreakPoint(fluctuat, fctName, 0);
		System.out.println("Starting analysis...");
		debugAnalyze(fluctuat, fctName, 0, 0);
		status = new DebugStatus();
	}
	
	public void debugClearAnalyzer() {
		debugClearAnalyzer(fluctuat);
	}
	
	private void setDebugStatus(int debugExecution) {
		status.status = debugExecution;
		status.point = Fluctuat.getPointStatus();
		status.sourceFile = Fluctuat.getSourceStatus();
		status.line = Fluctuat.getLineStatus();		
	}
	
	public DebugStatus debugContinue(int count) {
		setDebugStatus(Fluctuat.debugContinue(fluctuat, count));
		return status;
	}

	public DebugStatus debugNext(int count) {
		setDebugStatus(Fluctuat.debugNext(fluctuat, count));
		return status;
	}

	public DebugStatus debugStep(int count) {
		setDebugStatus(Fluctuat.debugStep(fluctuat, count));
		return status;
	}

	public DebugStatus debugStepInstruction(int count) {
		setDebugStatus(Fluctuat.debugStepInstruction(fluctuat, count));
		return status;
	}

	public boolean debugAddBreakPoint(String cFile, int line) {
		return debugAddBreakPoint(fluctuat, cFile, line, 0);
	}

	public boolean debugDelBreakPoints() {
		return debugDelBreakPoints(fluctuat);
	}

	public DebugResult debugFromCurrentRetrieveResult(long variable) {
		double[] bounds = new double[6];
		lastVarIntervals.status = debugFromCurrentRetrieveResult(fluctuat, variable, bounds);
		lastVarIntervals.set(bounds);		
		return lastVarIntervals;
	}

	public DebugResult getVariableValues(String varName) {
		return debugFromCurrentRetrieveResult(debugGetVariable(fluctuat, varName));
	}

	public boolean setVariableValues(String varName, double min, double max) {
		double[] bounds = {min, max, min, max, 0, 0};
		return debugFromCurrentSetResult(fluctuat, debugGetVariable(fluctuat, varName), bounds);
	}
	
	/**
	 * To be called first.
	 * @return DebugInterface*
	 */
	public static native long createDebugInterface();
	
	/**
	 * To add source files that Fluctuat will analyze.
	 * 
	 * @param fluctuat DebugInterface*
	 * @param cppFile Name of pre-processed C file.
	 *
	 * @return DebugVerdict (enum { DVFail, DVOk })
	 */
	public static native boolean debugAddSourceFile(long fluctuat, String cppFile);

	/**
	 * To set the project resource file (.rc) in which Fluctuat will read its analysis parameters.
	 * This may be generated from the GUI.
	 * 
	 * @param fluctuat DebugInterface*
	 * @param rcFile Name of a Fluctuat project resource file (.rc file).
	 * 
	 * @return DebugVerdict (enum { DVFail, DVOk })
	 */
	public static native boolean debugSetResourceFile(long fluctuat, String rcFile);
	
	/**
	 * Adds a breakpoint when entering a function.
	 * 
	 * @param fluctuat DebugInterface*
	 * @param fctName Name of the function where to set the breakpoint.
	 * @param cond BreakCondition* Value was set to 0 (null) in the given usage example, but I do not known how it is used.
	 * 
	 * @return DebugVerdict (enum { DVFail, DVOk })
	 */
	public static native boolean debugAddFunctionBreakPoint(long fluctuat, String fctName, long cond);

	/**
	 * Adds a breakpoint at a specific line number.
	 * 
	 * @param fluctuat DebugInterface*
	 * @param fileName Name of the C file where to set the breakpoint.
	 * @param line Line number on which the breakpoint will be set.
	 * @param cond BreakCondition* Value was set to 0 (null) in the given usage example, but I do not known how it is used.
	 * 
	 * @return DebugVerdict (enum { DVFail, DVOk })
	 */
	public static native boolean debugAddBreakPoint(long fluctuat, String fileName, int line, long condition);

	/**
	 * Removes all breakpoints.
	 * 
	 * @param fluctuat DebugInterface*
	 * 
	 * @return DebugVerdict (enum { DVFail, DVOk })
	 */
	public static native boolean debugDelBreakPoints(long fluctuat);
	
	/**
	 * Starts the analysis.
	 * stopCallBack is a function pointer (typedef int (*DebugStopFunction)(void* stopCallBackMemory);). 
	 * If defined, the corresponding function is called on interpreted statements 
	 * and may stop the analysis if it returns <pre>true</pre>.
	 * 
	 * @param fluctuat DebugInterface*
	 * @param entryFct Name of the analysis entry function (top level function to be analyzed).
	 * @param stopCallBack Null, or a pointer to a call-back function.
	 * @param stopCallBackMemory void*
	 * 
	 * @return  DebugExecution (enum { DEFail, DEFinish, DECurrent })
	 */
	public static native int debugAnalyze(long fluctuat, String entryFct, long stopCallBack, long stopCallBackMemory);
	
	/**
	 * 
	 *  
	 * @param fluctuat DebugInterface*
	 * @param count 
	 * @return DebugExecution (enum { DEFail, DEFinish, DECurrent })
	 */
	public static native int debugContinue(long fluctuat, int count);

	/**
	 * 
	 *  
	 * @param fluctuat DebugInterface*
	 * @param count 
	 * @return DebugExecution (enum { DEFail, DEFinish, DECurrent })
	 */
	public static native int debugNext(long fluctuat, int count);
	
	/**
	 * 
	 *  
	 * @param fluctuat DebugInterface*
	 * @param count 
	 * @return DebugExecution (enum { DEFail, DEFinish, DECurrent })
	 */
	public static native int debugStep(long fluctuat, int count);

	/**
	 * 
	 *  
	 * @param fluctuat DebugInterface*
	 * @param count 
	 * @return DebugExecution (enum { DEFail, DEFinish, DECurrent })
	 */
	public static native int debugStepInstruction(long fluctuat, int count);

	/**
	 * Retrieve next line number to process.
	 * @return The line number of the next statement to process in the interactive analysis.
	 */
	public static native int getLineStatus();

	/**
	 * Retrieve next source file to process.
	 * @return The source file name containing the next statement to process in the interactive analysis.
	 */
	public static native String getSourceStatus();

	/**
	 * Retrieve next "point" to process.
	 * @return The "point" of the next statement to process in the interactive analysis.
	 */
	public static native String getPointStatus();

	/**
	 * @param fluctuat DebugInterface*
	 * @param varName Name of the variable which we ask information for.
	 * @return InternVariable*
	 */
	public static native long debugGetVariable(long fluctuat, String varName);
	
	/**
	 * @param fluctuat DebugInterface*
	 * @param variable InternVariable*
	 * @return DebugVerdict (enum { DVFail, DVOk })
	 */
	public static native boolean debugFromCurrentRetrieveResult(long fluctuat, long variable, double[] bounds);
	
	/**
	 * @param fluctuat DebugInterface*
	 * @param variable InternVariable*
	 * @return DebugVerdict (enum { DVFail, DVOk })
	 */
	public static native boolean debugFromCurrentSetResult(long fluctuat, long variable, double[] bounds);
	
	public static native void debugClearAnalyzer(long fluctuat);

}
