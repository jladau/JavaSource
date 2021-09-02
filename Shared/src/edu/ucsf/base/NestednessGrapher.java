package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import com.google.common.collect.HashMultimap;
import edu.ucsf.io.BiomIO;
public class NestednessGrapher {

	public static ArrayList<String> getNestednessGraph(BiomIO bio1, String rgsSampleMetadataFields[]){
	
		//dTime = start time
		//mapTotalSample = sample totals
		//mapTotalObs = observation totals
		//mapRankSample = returns the rank of a given sample
		//mapRankObs = returns the rank of a given observation
		//iSampleRank = current sample rank
		//iOccurrences = total occurrences
		//lstOut = output
		//sbl1 = current output line
		//sbl2 = current metadata
		//mapCode = numeric codes for metadata
		
		HashMap<String,Integer> mapRankSample;
		HashMap<String,Integer> mapRankObs;
		HashMap<String,Integer> mapTotalSample;
		HashMap<String,Integer> mapTotalObs;
		HashMap<String,Integer> mapCode;
		int iSampleRank;
		int iOccurrences;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		StringBuilder sbl2;
		
		//loading totals
		mapTotalSample = bio1.getNonzeroCounts(bio1.axsSample);
		mapTotalObs = bio1.getNonzeroCounts(bio1.axsObservation);
		
		//removing zeros
		mapTotalObs.values().removeAll(Collections.singleton(0));
		mapTotalSample.values().removeAll(Collections.singleton(0));
		
		//loading occurrences
		iOccurrences = 0;
		for(int i:mapTotalSample.values()){
			iOccurrences+=i;
		}
		
		//loading ranks
		mapRankSample = getRanks(mapTotalSample);
		mapRankObs = getRanks(mapTotalObs);
	
		//loading metadata codes
		mapCode = getMetadataNumericCodes(bio1.axsSample,rgsSampleMetadataFields);
		
		//outputting results
		lstOut = new ArrayList<String>(iOccurrences);
		sbl1 = new StringBuilder("SAMPLE_RANK,OBSERVATION_RANK,SAMPLE_ID,OBSERVATION_ID");
		if(rgsSampleMetadataFields!=null){
			for(String s:rgsSampleMetadataFields){
				sbl1.append("," + s);
			}
		}
		sbl1.append(",METADATA_NUMERIC_CODE");
		lstOut.add(sbl1.toString());
		for(String sSample:mapTotalSample.keySet()){
			iSampleRank = mapRankSample.get(sSample);
			for(String sObs:mapTotalObs.keySet()){
				if(bio1.getValueByIDs(sObs, sSample)>0){
					sbl1 = new StringBuilder();
					sbl1.append(iSampleRank + "," + mapRankObs.get(sObs) + "," + sSample + "," + sObs);
					sbl2 = new StringBuilder();
					if(rgsSampleMetadataFields!=null){
						for(String s:rgsSampleMetadataFields){
							if(sbl2.length()>0){
								sbl2.append(",");
							}
							sbl2.append(bio1.axsSample.getMetadata(sSample).get(s));
						}
					}
					lstOut.add(sbl1.toString() + "," + sbl2.toString() + "," + mapCode.get(sbl2.toString()));
				}
			}
		}
		
		return lstOut;
		
	}
	
	private static HashMap<String,Integer> getMetadataNumericCodes(BiomIO.Axis axs1, String rgsMetadataFields[]){
		
		//sbl1 = current metadata
		//mapOut = output
		//i1 = current counter
		
		StringBuilder sbl1;
		int i1;
		HashMap<String,Integer> mapOut;
		
		i1 = 1;
		mapOut = new HashMap<String,Integer>();
		for(String s:axs1.getIDs()){
			sbl1 = new StringBuilder();
			if(rgsMetadataFields!=null){
				for(String t:rgsMetadataFields){
					if(sbl1.length()>0){
						sbl1.append(",");
					}
					sbl1.append(axs1.getMetadata(s).get(t));
				}
			}else{
				sbl1.append("na");
			}
			if(!mapOut.containsKey(sbl1.toString())){
				mapOut.put(sbl1.toString(), i1);
				i1++;
			}
		}	
		return mapOut;
	}
	
	private static HashMap<String,Integer> getRanks(HashMap<String,Integer> mapTotal){
		
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
		
		rgi1 = new int[mapTotal.size()];
		iCounter = 0;
		for(int i:mapTotal.values()){
			rgi1[iCounter] = i;
			iCounter++;
		}
		
		Arrays.sort(rgi1);
		map1 = HashMultimap.create();
		for(String s:mapTotal.keySet()){
			map1.put(mapTotal.get(s), s);
		}
		
		mapOut = new HashMap<String,Integer>(mapTotal.size());
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
}