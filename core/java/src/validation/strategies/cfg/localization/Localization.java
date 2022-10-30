
package validation.strategies.cfg.localization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import CFG.CFG;
import CFG.SetOfCFG;
import expression.variables.Variable;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.ValidationStatus;

public class Localization {
	
	protected CorrectVisitor cv;
	
	protected ValidationStatus status;
	
	protected SetOfCFG program;
	protected CFG method;

	public Localization() {
		status = new ValidationStatus();

		buildCFG();
		
		try {
			FileWriter writer = new FileWriter(Validation.counterExampleFileName.replace(".ce",".faults"));
			BufferedWriter ou = new BufferedWriter(writer);
			
			HashMap<String, String> inputs = readCounterexample(Validation.counterExampleFileName);
			
			cv = new CorrectVisitor(inputs);
			System.out.println(cv.visitPath(method,program.getFieldDeclaration().getBlock()));
			String[] arguments = new String[6];
			arguments[0]=program.name();
			ou.write("Program_name: ".concat(arguments[0]));
			ou.write("\n");
			// TODO
			arguments[1]=typeParaMethod(method.name(),method.parameters());
			ou.write("Type_parameters_method: ".concat(arguments[1]));
			ou.write("\n");
			arguments[2]=String.valueOf(method.parameters().size());
			ou.write("Number_parameters_method: ".concat(arguments[2]));
			ou.write("\n");
			arguments[3]=Validation.pgmFileName;
			ou.write("Program_file_name: ".concat(arguments[3]));
			ou.write("\n");
			ou.write("Suspicious_instructions:");
			ou.write("\n");
			int i = 0;
			int sizeAssInsts = cv.getPathCEAssInsts().size();
			while (i!=sizeAssInsts) {
				int lineNbr = cv.getPathCEAssInsts().get(i).startLine();
				if (lineNbr!=0) {
					arguments[4]=suspInst(lineNbr);
					if (arguments[4].contains("=")||arguments[4].contains("return")) {
						arguments[4] = arguments[4].replaceAll(";.*", "");
						ou.write("Instruction: ".concat(arguments[4]));
						ou.write("\n");
						arguments[5]=String.valueOf(lineNbr);
						ou.write("Line_number: ".concat(arguments[5]));
						ou.write("\n");
					}
				}
				i++;
			}
			
			i = 0;
			int sizeCondInsts = cv.getPathCECondInsts().size();
			while (i!=sizeCondInsts) {
				int lineNbr = cv.getPathCECondInsts().get(i).startLine;
				if (lineNbr!=0) {
					arguments[4]=suspInst(lineNbr);
					arguments[4] = arguments[4].replaceAll("\\{.*", "");
					ou.write("Instruction: ".concat(arguments[4]));
					ou.write("\n");
					arguments[5]=String.valueOf(lineNbr);
					ou.write("Line_number: ".concat(arguments[5]));
					ou.write("\n");
				}
				i++;
			}
			
			List<String[]> programsExperiments = new ArrayList<String[]>();
			programsExperiments.add(arguments);
			
			System.out.println(cv.getPathCEAssInsts());
			System.out.println(arguments[4]);
			System.out.println(arguments[1]);
			
			ou.flush();
			ou.close();
			writer.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private String suspInst(Integer integer) {
		File sourceFile = new File(Validation.pgmFileName);
		
		/*****************/
		BufferedReader buffer;
		String line = new String();;
		try {
			buffer = new BufferedReader(new FileReader(sourceFile));
			int cpt = 0;
			while (cpt < integer) {
				line = buffer.readLine();
				cpt++;
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		line=line.replaceAll("\\s*", "");
		line=line.replaceAll("int", "int ");
		line=line.replaceAll("elseif", "if");
		line=line.replaceAll("return", "return ");
		return line;
	}

	private String typeParaMethod(String name, ArrayList<Variable> parameters) {
		String s = name+"(";
		int size = parameters.size();
		for (int i = 0; i < size; i++) {
			if (i!=size-1) {
				s = s + parameters.get(i).type().toString().toLowerCase() + ",";
			} else {
				s = s + parameters.get(i).type().toString().toLowerCase();
			}
		}
		s = s + ")";
		return s;
	}

	protected HashMap<String, String> readCounterexample(String fileName) 
			throws IOException 
		{
			BufferedReader counterexample = new BufferedReader(new FileReader(fileName));
			HashMap<String, String> inputs = new HashMap<String, String>();
			String line;
			String[] input;
			
			while ((line = counterexample.readLine()) != null) {
		    	if (Validation.verboseLevel == VerboseLevel.DEBUG) {
		    		System.out.println(line);
		    	}
		    	// Skip empty lines
		    	if (!line.isEmpty()) {
		    		input = line.split(" ");
		    		inputs.put(input[0], input[1]);
		    	}
			}
		    return inputs;
		}
	
	private void buildCFG() {
		status.cfgBuildingTime = System.currentTimeMillis();
		try {
			this.program = new CFGBuilder(Validation.pgmFileName, Validation.pgmMethodName, 
										  Validation.maxUnfoldings, Validation.maxArrayLength).convert();
		} catch(Java2CFGException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		status.cfgBuildingTime = System.currentTimeMillis() - status.cfgBuildingTime;

		if (this.program != null) {
			method = Validation.pgmMethod(program);
    		if (VerboseLevel.DEBUG.mayPrint()) {
    			System.out.println("\nInitial CFGs\n" +program);
    		}
		}
		else {
			System.err.println("Error: method " 
					+ Validation.pgmMethodName 
					+ " is not defined in " 
					+ Validation.pgmFileName 
					+ "!");
			System.exit(-1);
		}
	}
}
