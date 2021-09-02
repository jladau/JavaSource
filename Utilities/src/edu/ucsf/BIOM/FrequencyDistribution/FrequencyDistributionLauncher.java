package edu.ucsf.BIOM.FrequencyDistribution;

import java.util.ArrayList;
import java.util.HashSet;

import edu.ucsf.base.EmpiricalDistribution_Cumulative;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/** 
 * Finds the frequency distribution of values table
 * @author jladau
 *
 */

public class FrequencyDistributionLauncher {

	
	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstOut = output
		//edf1 = distribution function
		//lst1 = values
		//lstOut = output
		//dMin = minimum value
		//dMax = maximum value
		//dStep = step size
		//iSteps = number of steps
		//d1 = current value
		//lstOut = output
		//setObs = set of observations to consider
		//setSamples = set of samples to consider
		
		HashSet<String> setObs;
		HashSet<String> setSamples;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		EmpiricalDistribution_Cumulative edf1;
		ArrayList<Double> lst1;
		double dMin;
		double dMax;
		double dStep;
		int iSteps;
		double d1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading otu table
		System.out.println("Loading data...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}	
		
		//loading sets of observations and samples
		if(arg1.containsArgument("setObservations")){
			setObs = arg1.getValueHashSet("setObservations");
		}else{
			setObs = bio1.axsObservation.getIDs();
		}
		if(arg1.containsArgument("setSamples")){
			setSamples = arg1.getValueHashSet("setSamples");
		}else{
			setSamples = bio1.axsSample.getIDs();
		}
		
		
		//loading values
		lst1 = new ArrayList<Double>(bio1.axsObservation.size()*bio1.axsSample.size());
		dMin = Double.MAX_VALUE;
		dMax = -Double.MAX_VALUE;
		for(String s:setObs){
			for(String t:setSamples){
				d1 = bio1.getValueByIDs(s, t);
				lst1.add(d1);
				if(d1<dMin){
					dMin = d1;
				}
				if(d1>dMax){
					dMax = d1;
				}
			}
		}
		
		//loading step variables
		iSteps= arg1.getValueInt("iSteps");
		dStep = (dMax-dMin)/((double) iSteps);
		dMax+=0.1;
		
		//finding empirical distribution
		edf1 = new EmpiricalDistribution_Cumulative(lst1);
		
		//outputting results
		lstOut = new ArrayList<String>(iSteps+1);
		lstOut.add("VALUE,EMPIRICAL_DISTRIBUTION_FUNCTION");
		for(double d=dMin;d<=dMax;d+=dStep){
			lstOut.add(d + "," + edf1.cumulativeProbability(d));
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}