package validation.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import validation.Validation.VerboseLevel;

import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

public class Shaving {

	public enum Side {
		LEFT,
		RIGHT;
	}

	public static class FluctuatFileInfo {
		public DomainBox fluctuatDomains;
		public Map<String, ShaveInfo> shavedVarsInfo;
	}
	
	public static class ShaveInfo {
		public VariableDomain unionDomain;
		public long splitFactor;
		
		public ShaveInfo(String varName) {
			unionDomain = new VariableDomain(new Variable(varName, Type.DOUBLE), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
			splitFactor = 2;
		}
		
		public String toString() {
			return "domain: " + unionDomain.toString() + " split factor: " + splitFactor;
		}
	}
	
	/**
	 * File format:
	 * 'NbShavedVar' nbShavedVar
	 * 'Var' varName
	 * 'Domain' varName val val
	 * 
	 * Set the domains of all variables computed by fluctuat in the DomainBox fluctuatDomains
	 * Set the list of variables to shave in shavedVarsInfo
	 * 
	 */
	public static FluctuatFileInfo loadFluctuatFile(String filename) {
		FluctuatFileInfo fluctuatInfo = new FluctuatFileInfo();
		String line, token, varName;
		Scanner scan;
		int nbShavedVar;
		double inf, sup;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			try {
				
				// Look for the line starting with "NbShavedVar" skipping every other lines
				while((line = in.readLine()) != null) {
					if (VerboseLevel.DEBUG.mayPrint()) {
						System.out.println(line);
					}
					scan = new Scanner(line);
					if (scan.next().compareTo("NbShavedVar") == 0) {
						nbShavedVar = scan.nextInt();
						fluctuatInfo.fluctuatDomains = new DomainBox();
						fluctuatInfo.shavedVarsInfo = new HashMap<String, ShaveInfo>(nbShavedVar,1.0F);
						break;
					}
				}
				while((line = in.readLine()) != null) {
					if (VerboseLevel.DEBUG.mayPrint()) {
						System.out.println(line);
					}
					scan = new Scanner(line);
					if (scan.hasNext()) {
						token = scan.next();
						if (token.compareTo("Var") == 0) {
							varName = scan.next();
							fluctuatInfo.shavedVarsInfo.put(varName, new ShaveInfo(varName));
						}
						else if (token.compareTo("Domain") == 0) {
							varName = scan.next();
							inf = scan.nextDouble();
							sup = scan.nextDouble();
							fluctuatInfo.fluctuatDomains.add(new VariableDomain(new Variable(varName, Type.DOUBLE), inf, sup));		
						}
					}
				}	
			} finally {
				in.close();
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found (" + filename + ")!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException!");
			e.printStackTrace();
		}
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("fluctuat domains:");
			System.out.println(fluctuatInfo.fluctuatDomains);
			System.out.println("Var to be shaved:");
			System.out.println(fluctuatInfo.shavedVarsInfo);			
		}
		
		return fluctuatInfo;
	}

	//TODO: Il faut aussi traiter les intervalles dégénérés [-oo,-oo] et [+oo,+oo]
	public static double splitSize(Variable v, long splitFactor) {
		double splitSize;
		double inf = v.domain().minValue().doubleValue();
		double sup = v.domain().maxValue().doubleValue();
		
		if (inf == Double.NEGATIVE_INFINITY || sup == Double.POSITIVE_INFINITY) {
			if (sup == Double.POSITIVE_INFINITY) {
				sup = Double.MAX_VALUE;
			}
			else if (sup == Double.NEGATIVE_INFINITY) {
				sup = -Double.MAX_VALUE;
			}

			if (inf == Double.POSITIVE_INFINITY) {
				inf = Double.MAX_VALUE;
			}
			else if (inf == Double.NEGATIVE_INFINITY) {
				inf = -Double.MAX_VALUE;
			}
			splitSize = sup/splitFactor - inf/splitFactor;
		}
		else {
			splitSize = (sup - inf) / splitFactor;
		}
		return splitSize;
	}

	public static VariableDomain splitDomain(Side side, VariableDomain domain, double splitSize) {
		VariableDomain splitDomain = domain.clone();
		double inf = domain.minValue().doubleValue();
		double sup = domain.maxValue().doubleValue();
		
		if (side == Side.LEFT) {
			splitDomain.setMaxValue(inf + splitSize);
		}
		else {
			splitDomain.setMinValue(sup - splitSize);
		}
		
		return splitDomain;
	}
	
	public static void updateShavedDomain(Side side, VariableDomain shaved, VariableDomain split) {
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Domain is shaved!");
		}
		
		if (side == Side.LEFT) {
			shaved.setMinValue(Math.nextUp(split.maxValue().doubleValue()));
		}
		else {
			shaved.setMaxValue(Math.nextAfter(split.minValue().doubleValue(), Double.NEGATIVE_INFINITY));
		}
	}

}
