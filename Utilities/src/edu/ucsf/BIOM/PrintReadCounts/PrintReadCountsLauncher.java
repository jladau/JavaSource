package edu.ucsf.BIOM.PrintReadCounts;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class PrintReadCountsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//dTime = start time
		//lstOut = output
		//lstAbund = current abundances
		//map1 = map of richnesses
		
		HashMap<String,Double> map1;
		ArrayList<Double> lstAbund;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading defaults
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", true);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", false);
		}
		arg1.updateArgument("bCheckRarefied", false);
		
		//loading otu table
		System.out.println("Loading data...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sDataPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//outputting results
		lstOut = new ArrayList<String>(bio1.axsObservation.getIDs().size()+1);
		lstOut.add("SAMPLE_ID,NUMBER_READS");
		map1 = bio1.sum(bio1.axsSample);
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
}
