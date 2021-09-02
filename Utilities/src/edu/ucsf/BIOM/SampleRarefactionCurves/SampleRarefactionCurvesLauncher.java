package edu.ucsf.BIOM.SampleRarefactionCurves;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class SampleRarefactionCurvesLauncher {
	
	
	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//map1 = map from metadata values to samples with that value
		//sField = metadata field
		//i1 = maximum number of samples to rarefy to
		//lst1 = current list of samples (unrarefied)
		//lst2 = current list of samples (rarefied)
		//lstOut = output
		//i2 = number of replicate resamples
		//map2 = returns set of taxa for given sample
		//map3 = list of non-zero values
		
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayListMultimap<String,String> map1;
		HashMultimap<String,String> map2;
		HashMap<String,Double> map3;
		String sField;
		int i1;
		int i2;
		ArrayList<String> lst1;
		ArrayList<String> lst2;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		i1 = arg1.getValueInt("iMaxSampleCount");
		i2 = arg1.getValueInt("iIterations");
		lstOut = new ArrayList<String>(i1/10*i2*100);
		lstOut.add("METADATA_VALUE,NUMBER_OF_SAMPLES,RICHNESS");
		
		//loading lists of taxa
		map2 = HashMultimap.create(bio1.axsSample.size(),bio1.axsObservation.size());
		for(String s:bio1.axsSample.getIDs()){
			map3 = bio1.getNonzeroValues(bio1.axsSample, s);
			for(String t:map3.keySet()){
				map2.put(s, t);
			}
		}
		
		//loading list of samples
		map1 = ArrayListMultimap.create(100,bio1.axsSample.size());
		if(!arg1.containsArgument("sMetadataField")){
			for(String s:bio1.axsSample.getIDs()){
				map1.put("NA", s);
			}
		}else{
			sField = arg1.getValueString("sMetadataField");
			for(String s:bio1.axsSample.getIDs()){
				map1.put(bio1.axsSample.getMetadata(s).get(sField), s);
			}
		}
		
		//rarefying
		for(String s:map1.keySet()){
			lst1 = new ArrayList<String>(map1.get(s));
			for(int i=10;i<=i1;i+=10){
				System.out.println("Rarefying " + s + ", " + i + " samples...");
				for(int k=0;k<i2;k++){
					lst2 = randomSubset(i,lst1);
					if(lst2!=null){
						lstOut.add(s + "," + i + "," + sampleSubsetRichness(lst2,map2));
					}//else{
					//	lstOut.add(s + "," + i + "," + "NA");
					//}
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static int sampleSubsetRichness(ArrayList<String> lstSamples, HashMultimap<String,String> mapNonzeroObservations){
		
		//set1 = set of taxa
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(1000);
		for(String s:lstSamples){
			for(String t:mapNonzeroObservations.get(s)){
				set1.add(t);
			}
		}
		return set1.size();
	}
	
	private static ArrayList<String> randomSubset(int iSize, ArrayList<String> lst1){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		if(iSize>lst1.size()){
			return null;
		}
		lstOut = new ArrayList<String>(iSize);
		Collections.shuffle(lst1);
		for(int i=0;i<iSize;i++){
			lstOut.add(lst1.get(i));
		}
		return lstOut;
	}
	

}
