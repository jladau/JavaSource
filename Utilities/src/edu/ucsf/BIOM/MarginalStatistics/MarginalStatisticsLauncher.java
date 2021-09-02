package edu.ucsf.BIOM.MarginalStatistics;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class MarginalStatisticsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//dTime = start time
		//lstOut = output
		//map1 = map of richnesses
		//map2 = map with counts (integer)
		//axs1 = axis to use
		
		HashMap<String,Double> map1 = null;
		HashMap<String,Integer> map2;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		BiomIO.Axis axs1 = null;
		
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
		
		//loading axis
		if(arg1.getValueString("sAxis").equals("sample")){
			axs1 = bio1.axsSample;
		}else if(arg1.getValueString("sAxis").equals("observation")){
			axs1 = bio1.axsObservation;
		}
		
		//outputting results
		lstOut = new ArrayList<String>(axs1.getIDs().size()+1);
		if(arg1.getValueString("sAxis").equals("observation")){
			lstOut.add("OBSERVATION_ID,VALUE");
		}else if(arg1.getValueString("sAxis").equals("sample")){
			lstOut.add("SAMPLE_ID,VALUE");
		}
		if(arg1.getValueString("sMode").equals("sum")){
			map1 = bio1.sum(axs1);
		}else if(arg1.getValueString("sMode").equals("shannon")){
			map1 = bio1.getShannon();	
		}else if(arg1.getValueString("sMode").equals("mean")){
			map1 = bio1.getMeans(axs1);
		}else if(arg1.getValueString("sMode").equals("nonzerocount")){
			map2 = bio1.getNonzeroCounts(axs1);
			map1 = new HashMap<String,Double>(map2.size());
			for(String s:map2.keySet()){
				map1.put(s, (double) map2.get(s));
			}
		}else if(arg1.getValueString("sMode").equals("stdev")){
			map1 = bio1.getStandardDeviations(axs1);
		}else if(arg1.getValueString("sMode").equals("sterror")){
			map1 = bio1.getStandardErrors(axs1);
		}			
		
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
