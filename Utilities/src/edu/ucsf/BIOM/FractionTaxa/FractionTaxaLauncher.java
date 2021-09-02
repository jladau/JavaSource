package edu.ucsf.BIOM.FractionTaxa;

import java.util.ArrayList;
import java.util.HashSet;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Finds the fraction of taxa meeting a specified criterion in each sample
 * @author jladau
 *
 */


public class FractionTaxaLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//sKey = observation field to check
		//sValue = observation value to tally
		//lstOut = output
		//set1 = set of observations with values matching requested value
		//set2 = set of observations at sample
		//i1 = count of elements from set2 in set1
		
		ArgumentIO arg1;
		BiomIO bio1;
		String sKey;
		String sValue;
		ArrayList<String> lstOut;
		HashSet<String> set1;
		HashSet<String> set2;
		int i1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		sKey = arg1.getValueString("sMetadataField");
		sValue = arg1.getValueString("sMetadataValue");
		lstOut = new ArrayList<String>(bio1.axsSample.size()+1);
		lstOut.add("SAMPLE,COUNT,RICHNESS,FRACTION");
		
		//loading map from observation ids to metadata values
		set1 = new HashSet<String>(bio1.axsObservation.size());
		for(String s:bio1.axsObservation.getIDs()){
			if(bio1.axsObservation.getMetadata(s).get(sKey).equals(sValue)){
				set1.add(s);
			}
		}
		
		//looping through samples
		for(String s:bio1.axsSample.getIDs()){
			
			//loading list of observations for sample
			set2 = new HashSet<String>(bio1.getNonzeroValues(bio1.axsSample,s).keySet());
			
			//finding number of shared elements
			i1 = 0;
			if(set1.size()<set2.size()){
				for(String t:set1){
					if(set2.contains(t)){
						i1++;
					}
				}
			}else{
				for(String t:set2){
					if(set1.contains(t)){
						i1++;
					}
				}
			}
			lstOut.add(s + "," + i1 + "," + set2.size() + "," + ((double) i1)/((double) set2.size()));
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");	
	}	
}
