package edu.ucsf.SpeciesSubsets;

import java.util.HashSet;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates the size of the union and intersection for specified samples.
 * @author jladau
 */


public class SpeciesSubsetsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = contains list of intersections and unions to consider
		//bio1 = biom table
		//rgsI = set of samples for intersection
		//rgsIc = set of samples for complement
		//rgsU = set of samples for union
		//rgsUc = set of samples for union complement
		//setI = intersection species
		//setU = union species
		//setUc = union complement species
		//set1 = output
		//set2 = current set of species
		//datOut = output
		//setMissing = set of samples that are missing (due to rarefaction)
		//setMissingCurrent = set of current missing samples (due to rarefaction)
		
		
		DataIO datOut;
		ArgumentIO arg1;
		DataIO dat1;
		BiomIO bio1;
		HashSet<String> set1;
		HashSet<String> set2;
		HashSet<String> setI;
		HashSet<String> setU;
		HashSet<String> setUc;
		String rgsI[];
		String rgsIc[] = null;
		String rgsU[] = null;
		String rgsUc[] = null;
		HashSet<String> setMissing;
		HashSet<String> setMissingCurrent;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sSetsPath"));
		datOut = new DataIO(arg1.getValueString("sSetsPath"));
		datOut.appendToLastColumn(0, "MISSING_SAMPLES,NUMBER_OF_TAXA");
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		setMissing = new HashSet<String>();
		
		//*************************
		//HashMap<String,Double> mapTEMP = bio1.getRichness();
		//for(String s:mapTEMP.keySet()){
		//	System.out.println(s + "," + mapTEMP.get(s));
		//}
		//*************************
		
		//looping through sets
		for(int i=1;i<dat1.iRows;i++){
			
			
			
			//initializing
			rgsI = getValue(i, "INTERSECTION", dat1);
			rgsIc = getValue(i, "INTERSECTION_COMPLEMENT", dat1);
			rgsU = getValue(i, "UNION", dat1);
			rgsUc = getValue(i, "UNION_COMPLEMENT", dat1);
			setI = new HashSet<String>();
			setU = new HashSet<String>();
			setUc = new HashSet<String>();
			
			//checking that all samples are present
			setMissingCurrent = new HashSet<String>();
			for(String sSample:rgsI){
				if(!bio1.axsSample.getIDs().contains(sSample)){
					setMissing.add(sSample);
					setMissingCurrent.add(sSample);
				}
			}
			for(String sSample:rgsIc){
				if(!bio1.axsSample.getIDs().contains(sSample)){
					setMissing.add(sSample);
					setMissingCurrent.add(sSample);
				}
			}
			for(String sSample:rgsU){
				if(!bio1.axsSample.getIDs().contains(sSample)){
					setMissing.add(sSample);
					setMissingCurrent.add(sSample);
				}
			}
			
			for(String sSample:rgsUc){
				if(!bio1.axsSample.getIDs().contains(sSample)){
					setMissing.add(sSample);
					setMissingCurrent.add(sSample);
				}
			}
			if(setMissingCurrent.size()>0){
				datOut.appendToLastColumn(i, Joiner.on(";").join(setMissingCurrent) + ",na");
				continue;
			}
			
			//loading sets of species
			for(String sSample:rgsI){
				set2 = new HashSet<String>(bio1.getNonzeroValues(bio1.axsSample, sSample).keySet());
				if(setI.size()==0){
					setI.addAll(set2);
				}else{
					setI = intersection(setI, set2);
				}
			}
			for(String sSample:rgsU){
				set2 = new HashSet<String>(bio1.getNonzeroValues(bio1.axsSample, sSample).keySet());
				if(setU.size()==0){
					setU.addAll(set2);
				}else{
					setU = union(setU, set2);
				}
			}
			for(String sSample:rgsUc){
				set2 = new HashSet<String>(bio1.getNonzeroValues(bio1.axsSample, sSample).keySet());
				if(setUc.size()==0){
					setUc.addAll(set2);
				}else{
					setUc = union(setUc, set2);
				}
			}
			
			//finding intersection
			set1 = new HashSet<String>(bio1.axsObservation.size());
			if(setU.size()==0 && setI.size()>0){
				set1.addAll(setI);
			}else if(setU.size()>0 && setI.size()==0){
				set1.addAll(setU);	
			}else if(setU.size()>0 && setI.size()>0){
				set1 = intersection(setU, setI);
			}
			for(String sTaxon:setUc){
				if(set1.contains(sTaxon)){
					set1.remove(sTaxon);
				}
			}
			for(String sSample:rgsIc){
				set2 = new HashSet<String>(bio1.getNonzeroValues(bio1.axsSample, sSample).keySet());
				for(String sTaxon:set2){
					if(set1.contains(sTaxon)){
						set1.remove(sTaxon);
					}
				}
			}
			
			//appending results
			datOut.appendToLastColumn(i, "na," + set1.size());
		}
		
		//outputting results
		DataIO.writeToFile(datOut.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("");
		System.out.println("**********************");
		System.out.println("Warning: the following samples are missing (likely due to rarefaction):");
		for(String s:setMissing){
			System.out.println(s);
		}
		System.out.println("**********************");
		System.out.println("");
		System.out.println("Done.");
	}
	
	private static HashSet<String> intersection(HashSet<String> set1, HashSet<String> set2){
		
		//setOut = output
		
		HashSet<String> setOut;
		
		setOut = new HashSet<String>(Math.min(set1.size(), set2.size()));
		if(set1.size()<set2.size()){
			for(String s:set1){
				if(set2.contains(s)){
					setOut.add(s);
				}
			}
		}else{
			for(String s:set2){
				if(set1.contains(s)){
					setOut.add(s);
				}
			}
		}
		return setOut;
	}

	private static HashSet<String> union(HashSet<String> set1, HashSet<String> set2){
		
		//setOut = output
		
		HashSet<String> setOut;
		
		setOut = new HashSet<String>(set1.size() + set2.size());
		for(String s:set1){
			setOut.add(s);
		}
		for(String s:set2){
			setOut.add(s);
		}
		return setOut;
	}
	
	
	private static String[] getValue(int iRow, String sHeader, DataIO dat1){
		
		//s1 = current value
		
		String s1;
		
		try{
			s1 = dat1.getString(iRow, sHeader);
		}catch(Exception e){
			s1 = "";
		}
		if(s1!="" && !s1.equals("null")){
			return s1.split(";");
		}else{
			return new String[0];
		}
		
	}
}
