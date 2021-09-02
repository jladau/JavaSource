package edu.ucsf.BIOM.HeatMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Generates a heat map from OTU table
 * @author jladau
 */


public class HeatMapLauncher {

	public static void main(String rgsArgs[]){
		
		//bio1 = biom io object
		//arg1 = arguments
		//mapSampleRanks = map from sample IDs to numeric value
		//mapObsRanks = map from observation IDs to numeric value
		//lstOut = output
		//dat1 = current ranks
		
		ArrayList<String> lstOut;
		BiomIO bio1;
		ArgumentIO arg1;
		HashMap<String,Integer> mapSampleRanks;
		HashMap<String,Integer> mapObsRanks;
		DataIO datRanks;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		try {
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"), arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//loading ranks
		if(!arg1.containsArgument("sSampleRanksPath")){
			mapSampleRanks = getRanks(bio1, arg1.getValueString("sRankingMode"), bio1.axsSample);
		}else{
			datRanks = new DataIO(arg1.getValueString("sSampleRanksPath"));
			mapSampleRanks = new HashMap<String,Integer>(datRanks.iRows-1);
			for(int i=1;i<datRanks.iRows;i++){
				mapSampleRanks.put(datRanks.getString(i, "SAMPLE_ID"), datRanks.getInteger(i, "RANK"));
			}
			
		}
		if(!arg1.containsArgument("sObservationRanksPath")){
			mapObsRanks = getRanks(bio1, arg1.getValueString("sRankingMode"), bio1.axsObservation);
		}else{
			datRanks = new DataIO(arg1.getValueString("sObservationRanksPath"));
			mapObsRanks = new HashMap<String,Integer>(datRanks.iRows-1);
			for(int i=1;i<datRanks.iRows;i++){
				mapObsRanks.put(datRanks.getString(i, "OBSERVATION_ID"), datRanks.getInteger(i, "RANK"));
			}
		}
			
		//writing output
		lstOut = new ArrayList<String>(bio1.axsObservation.size()*bio1.axsSample.size()+1);
		lstOut.add("SAMPLE_ID,SAMPLE_NUMERIC_ID,OBSERVATION_ID,OBSERVATION_NUMERIC_ID,VALUE,OCCURRENCE");
		for(String s:bio1.axsObservation.getIDs()){
			if(mapObsRanks.containsKey(s)){
				for(String t:bio1.axsSample.getIDs()){
					if(mapSampleRanks.containsKey(t)){
						lstOut.add(
							t + "," +
							mapSampleRanks.get(t) + "," +
							s + "," +
							mapObsRanks.get(s) + "," +
							bio1.getValueByIDs(s, t) + "," +
							Math.signum(bio1.getValueByIDs(s, t)));
					}
				}
			}
		}
		
		//appending zeros for observations and samples that had zero occurrences
		for(String s:mapObsRanks.keySet()){
			if(!bio1.axsObservation.getIDs().contains(s)){
				for(String t:mapSampleRanks.keySet()){
					lstOut.add(
						t + "," +
						mapSampleRanks.get(t) + "," +
						s + "," +
						mapObsRanks.get(s) + "," +
						"0,0");
				}
			}
		}
		for(String t:mapSampleRanks.keySet()){
			if(!bio1.axsSample.getIDs().contains(t)){
				for(String s:mapObsRanks.keySet()){
					lstOut.add(
						t + "," +
						mapSampleRanks.get(t) + "," +
						s + "," +
						mapObsRanks.get(s) + "," +
						"0");
				}
			}
		}
		
		
		
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static HashMap<String,Integer> getRanks(BiomIO bio1, String sMode, BiomIO.Axis axs1){
		
		//map1 = map from keys to ranking values (integer)
		//map2 = map from keys to ranking values (double)
		//set1 = set of keys
		//mapOut = output
		//i1 = counter
		
		HashMap<String,Integer> map1;
		HashMap<String,Double> map2;
		TreeSet<String> set1;
		HashMap<String,Integer> mapOut = null;
		int i1;
		
		if(sMode.equals("nonzero_count")){
			map1 = bio1.getNonzeroCounts(axs1);
			mapOut = getRanksInteger(map1);
		}else if(sMode.equals("sum")){
			map2 = bio1.sum(axs1);
			mapOut = getRanksDouble(map2);
		}else if(sMode.equals("name")){
			set1 = new TreeSet<String>(axs1.getIDs());
			mapOut = new HashMap<String,Integer>(set1.size());
			i1 = 0;
			for(String s:set1){
				i1++;
				mapOut.put(s, i1);
			}
		}
		return mapOut;
	}
	
	private static HashMap<String,Integer> getRanksInteger(HashMap<String,Integer> mapValues){
		
		//map1(iTotal) = returns sKey for given total
		//rgi1 = sorted list of totals
		//iCounter = counter
		//mapOut = output
		//s1 = current element
		
		HashMultimap<Integer,String> map1;
		HashMap<String,Integer> mapOut;
		int rgi1[];
		int iCounter;
		String s1 = null;
		
		rgi1 = new int[mapValues.size()];
		iCounter = 0;
		for(int i:mapValues.values()){
			rgi1[iCounter] = i;
			iCounter++;
		}
		
		Arrays.sort(rgi1);
		map1 = HashMultimap.create();
		for(String s:mapValues.keySet()){
			map1.put(mapValues.get(s), s);
		}
		
		mapOut = new HashMap<String,Integer>(mapValues.size());
		for(int i=0;i<rgi1.length;i++){
			for(String s:map1.get(rgi1[i])){
				s1 = s;
				break;
			}
			mapOut.put(s1, i+1);
			map1.remove(rgi1[i], s1);
		}
		
		return mapOut;	
	}
	
	private static HashMap<String,Integer> getRanksDouble(HashMap<String,Double> mapValues){
		
		//map1(iTotal) = returns sKey for given total
		//rgd1 = sorted list of totals
		//iCounter = counter
		//mapOut = output
		//s1 = current element
		
		HashMultimap<Double,String> map1;
		HashMap<String,Integer> mapOut;
		double rgd1[];
		int iCounter;
		String s1 = null;
		
		rgd1 = new double[mapValues.size()];
		iCounter = 0;
		for(double d:mapValues.values()){
			rgd1[iCounter] = d;
			iCounter++;
		}
		
		Arrays.sort(rgd1);
		map1 = HashMultimap.create();
		for(String s:mapValues.keySet()){
			map1.put(mapValues.get(s), s);
		}
		
		mapOut = new HashMap<String,Integer>(mapValues.size());
		for(int i=0;i<rgd1.length;i++){
			for(String s:map1.get(rgd1[i])){
				s1 = s;
				break;
			}
			mapOut.put(s1, i+1);
			map1.remove(rgd1[i], s1);
		}
		
		return mapOut;	
	}
	
}
