package edu.ucsf.BIOM.SubsampleSampleIDs;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * This code finds the sample IDs after bootstrap resampling.
 * @author jladau
 *
 */

public class SubsampleSampleIDsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//bio1 = biom object
		//map1 = map listing each sample ID and whether it was excluded or included.
		//lstOut = output
		
		ArgumentIO arg1;
		BiomIO bio1;
		HashMap<String,String> map1;
		ArrayList<String> lstOut;
		
		//updating iBootstrapRandomSeed argument
		for(int i=0;i<rgsArgs.length;i++){
			rgsArgs[i]=rgsArgs[i].replace("iRandomSampleSubsetSize","iRandomSampleSubsetSize1");
		}
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		if(!arg1.containsArgument("bPrintData")){
			arg1.updateArgument("bPrintData", false);
		}
		
		//loading biom object
		System.out.println("Loading BIOM file...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//loading exclusion/inclusion
		try {
			if(arg1.containsArgument("iRandomSampleSubsetSize1") 
					&& arg1.getValueInt("iRandomSampleSubsetSize1")!=-9999
					&& arg1.containsArgument("iRandomSubsetSeed") 
					&& arg1.getValueInt("iRandomSubsetSeed")!=-9999){	
						map1 = bio1.takeRandomSubset(arg1.getValueInt("iRandomSampleSubsetSize1"), arg1.getValueInt("iRandomSubsetSeed"), bio1.axsSample);
					}else{
						map1 = new HashMap<String,String>();
						for(String s:bio1.axsSample.getIDs()){
							map1.put(s, "included");
						}
					}
		} catch (Exception e) {
			map1=null;
			e.printStackTrace();
		}
		
		//outputting results
		lstOut = new ArrayList<String>(map1.size()+1);
		lstOut.add("SAMPLE_ID,SAMPLE_STATUS");
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}