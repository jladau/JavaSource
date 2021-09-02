package edu.ucsf.BIOM.Richnesses;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

@Deprecated
public class RichnessesLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//dTime = start time
		//lstOut = output
		//map1 = map of richnesses
		
		HashMap<String,Double> map1;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		
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
		
		//outputting results
		lstOut = new ArrayList<String>(bio1.axsObservation.getIDs().size()+1);
		lstOut.add("SAMPLE_ID,RICHNESS");
		map1 = bio1.getRichness();
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
