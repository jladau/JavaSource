package edu.ucsf.BIOM.RarefyBySampleIDs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Rarefies sample IDs
 * @author jladau
 *
 */

public class RarefyBySampleIDsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom file
		//map1 = map from sample ids to metadata field to rarefy by
		//sField = metadata field
		//map2 = map from metadata field values to counts
		//map3 = map of current counts for each field
		//iDepth = rarefaction depth
		//lst1 = list of sample names in random order
		//set1 = set of samples to include
		//sValue = metadata value for current sample
		
		ArgumentIO arg1;
		BiomIO bio1;
		HashMap<String,String> map1;
		String sField;
		HashMap_AdditiveInteger<String> map2;
		HashMap_AdditiveInteger<String> map3;
		int iDepth;
		ArrayList<String> lst1;
		HashSet<String> set1;
		String sValue;
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"));
		sField = arg1.getValueString("sMetadataField");
		map1 = new HashMap<String,String>(bio1.axsSample.size());
		map2 = new HashMap_AdditiveInteger<String>(bio1.axsSample.size());
		map3 = new HashMap_AdditiveInteger<String>(bio1.axsSample.size());
		set1 = new HashSet<String>(bio1.axsSample.size());
		lst1 = new ArrayList<String>(bio1.axsSample.size());
		
		//loading maps for rarefying
		for(String s:bio1.axsSample.getIDs()){
			map1.put(
					s, 
					bio1.axsSample.getMetadata(s).get(sField));
			map2.putSum(map1.get(s), 1);
			lst1.add(s);
		}
		
		//loading rarefaction depth
		iDepth = arg1.getValueInt("iSampleRarefactionDepth");
		if(iDepth<0){
			iDepth = Integer.MAX_VALUE;
			for(String s:map2.keySet()){
				if(map2.get(s)<iDepth){
					iDepth = map2.get(s);
				}
			}
		}
		
		//finding list of samples to keep
		Collections.shuffle(lst1);
		for(String s:lst1){
			sValue = map1.get(s);
			if(map2.get(sValue)>=iDepth){
				if(!map3.containsKey(sValue) || map3.get(sValue)<iDepth){
					set1.add(s);
					map3.putSum(sValue, 1);
				}
			}
		}
		
		DataIO.writeToFile(new ArrayList<String>(set1), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
