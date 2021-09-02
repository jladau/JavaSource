package edu.ucsf.Nestedness.EnvironmentPhylogenyHeatmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class EnvironmentPhylogenyHeatmapLauncher {

	public static void main(String rgsArgs[]) throws Exception{

		//arg1 = arguments
		//bio1 = biom object
		//map1 = map from taxonomic strings and observation ids to minimum richnesses
		//rgs1 = current taxon
		//rtx1 = ranked sortable taxa
		//map3 = map from sample ids to aloofnesses
		//lstOut = output
		//sbl1 = current output line
		//s1 = current observation information
		//rgs2 = taxonomic ranks
		//lst1 = list of sample ranks
		
		String rgs2[];
		String s1;
		StringBuilder sbl1;
		ArrayList<String> lstOut;
		String rgs1[];
		ArgumentIO arg1;
		BiomIO bio1;
		HashMap<String,Double> map1;
		RankedSortableTaxa rtx1;
		HashMap<String,Double> map3;
		ArrayList<Double> lst1;
		
		//loading variables
		rgs2 = new String[]{"kingdom","phylum","class","order","family","genus","otu"};
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);

		//loading defaults
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", false);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", true);
		}
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
		}
		
		//loading data
		System.out.println("Loading data...");
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());		
		
		//collapsing table on sample metadata key
		if(arg1.containsArgument("sSampleMetadataKey")){
			bio1.collapse(arg1.getValueString("sSampleMetadataKey"), bio1.axsSample, false);
		}
			
		//filtering out taxa without full taxonomic information
		bio1.filter(taxaFullTaxonomy(bio1), bio1.axsObservation);
		
		//loading sample ranks
		map3 = sampleAloofnesses(bio1, arg1.getValueString("sNestednessTaxonRank"));
		
		//loading minimum richnesses
		System.out.println("Loading minimum richnesses...");
		map1 = maximumValues(bio1, map3);
		
		//ranking taxa
		rtx1 = new RankedSortableTaxa(bio1.axsObservation.size());
		for(String s:bio1.axsObservation.getIDs()){
			rgs1 = bio1.axsObservation.getMetadata(s).get("taxonomy").split(";");
			rtx1.addTaxon(new SortableTaxon(rgs1, loadValues(rgs1,map1,s), s, arg1.getValueString("sNestednessTaxonRank")));
		}
		rtx1.sort();
		
		//outputting results
		lstOut = new ArrayList<String>(bio1.axsObservation.size()*bio1.axsSample.size());
		lstOut.add("OBSERVATION_ID_SHORT,"
				+ "KINGDOM_ID,KINGDOM_RANK,"
				+ "PHYLUM_ID,PHYLUM_RANK,"
				+ "CLASS_ID,CLASS_RANK,"
				+ "ORDER_ID,ORDER_RANK,"
				+ "FAMILY_ID,FAMILY_RANK,"
				+ "GENUS_ID,GENUS_RANK,"
				+ "OTU_ID,OTU_RANK,"
				+ "ALOOFNESS_MIN,ALOOFNESS_MAX");
		for(String s:bio1.axsObservation.getIDs()){
			sbl1 = new StringBuilder();
			sbl1.append(s.substring(0, 9));
			for(String t:rgs2){
				sbl1.append("," + rtx1.taxon(s, t) + "," + rtx1.rank(s, t));
			}
			s1 = sbl1.toString();
			lst1 = new ArrayList<Double>(bio1.axsSample.size());
			for(String t:bio1.axsSample.getIDs()){
				if(bio1.getValueByIDs(s, t)>0){
					lst1.add(map3.get(t));
				}
			}
			lstOut.add(s1 + "," + minimum(lst1) + "," + maximum(lst1));
		}
		/*
		lstOut = new ArrayList<String>(bio1.axsObservation.size()*bio1.axsSample.size());
		lstOut.add("OBSERVATION_ID,"
				+ "KINGDOM_ID,KINGDOM_RANK,"
				+ "PHYLUM_ID,PHYLUM_RANK,"
				+ "CLASS_ID,CLASS_RANK,"
				+ "ORDER_ID,ORDER_RANK,"
				+ "FAMILY_ID,FAMILY_RANK,"
				+ "GENUS_ID,GENUS_RANK,"
				+ "OTU_ID,OTU_RANK,"
				+ "SAMPLE_ID,SAMPLE_RANK");
		for(String s:bio1.axsObservation.getIDs()){
			sbl1 = new StringBuilder();
			sbl1.append(s);
			for(String t:rgs2){
				sbl1.append("," + rtx1.taxon(s, t) + "," + rtx1.rank(s, t));
			}
			s1 = sbl1.toString();
			for(String t:bio1.axsSample.getIDs()){
				if(bio1.getValueByIDs(s, t)>0){
					lstOut.add(s1 + "," + t + "," + map3.get(t));
				}
			}
		}
		*/
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static HashSet<String> taxaFullTaxonomy(BiomIO bio1){
		
		//set1 = output
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(bio1.axsObservation.getIDs().size());
		for(String s:bio1.axsObservation.getIDs()){
			if(!bio1.axsObservation.getMetadata(s).get("taxonomy").split(";")[5].equals("g__")){	

				//********************************
				//System.out.println(bio1.axsObservation.getMetadata(s).get("taxonomy"));
				//********************************
				
				
				set1.add(s);
			}
		}
		return set1;
		
	}
	
	
	private static double maximum(ArrayList<Double> lst1){
		
		//d1 = output
		
		double d1;
		
		d1 = -Double.MAX_VALUE;
		for(double d:lst1){
			if(d>d1){
				d1 = d;
			}
		}
		return d1;
	}
	
	private static double minimum(ArrayList<Double> lst1){
		
		//d1 = output
		
		double d1;
		
		d1 = Double.MAX_VALUE;
		for(double d:lst1){
			if(d<d1){
				d1 = d;
			}
		}
		return d1;
	}	
	
	private static HashMap<String,Double> sampleAloofnesses(BiomIO bio1, String sTaxonRank){
		
		//map1 = map from samples to richness
		//map2 = map from values to values
		//map3 = output, map from samples to values 
		//lst1 = list of richnesses
		//bio2 = collapsed table
		
		HashMap<String,Double> map1;
		HashMap<Double,Double> map2;
		HashMap<String,Double> map3;
		ArrayList<Double> lst1;
		BiomIO bio2;
		
		//collapsing on appropriate rank
		if(sTaxonRank!=null){
			bio2 = bio1.collapse(sTaxonRank, bio1.axsObservation, true);
		}else{
			bio2 = bio1;
		}
		
		//loading richnesses
		map1 = bio2.getRichness();
				
		//loading ranks
		lst1 = new ArrayList<Double>(map1.size());
		for(String s:map1.keySet()){
			lst1.add(map1.get(s));
		}
		Collections.sort(lst1);
		map2 = new HashMap<Double,Double>(map1.size());
		for(int i=0;i<lst1.size();i++){
			//**********************************
			map2.put(lst1.get(i), lst1.get(i));
			//map2.put(lst1.get(i), lst1.size()-i);
			//**********************************
		}
		
		//loading output
		map3 = new HashMap<String,Double>(map1.size());
		for(String s:map1.keySet()){
			map3.put(s, 1./map2.get(map1.get(s)));
		}
		return map3;
	}
	
	private static double[] loadValues(String[] rgsTaxonomy, HashMap<String,Double> mapMinRichnesses, String sObservationID){
		
		//rgd1 = output
		//sbl1 = string builder
		
		double rgd1[];
		StringBuilder sbl1;
		
		rgd1 = new double[rgsTaxonomy.length];
		sbl1 = new StringBuilder();
		for(int i=0;i<rgsTaxonomy.length;i++){
			if(i>0){
				sbl1.append(";");
			}
			sbl1.append(rgsTaxonomy[i]);
			if(i==rgsTaxonomy.length-1){
				rgd1[i]=mapMinRichnesses.get(sObservationID + "," + sbl1.toString());
			}else{
				rgd1[i]=mapMinRichnesses.get(sbl1.toString());
			}
		}
		return rgd1;
	}
	
	/**
	 * Finds the aloofness of the most aloof environment that each taxon occurs in
	 * @return Map from each observation id, taxon string to the aloofness of the most rich environment
	 */
	private static HashMap<String,Double> maximumValues(BiomIO bio1, HashMap<String,Double> mapAloofnesses){
		
		//map2 = output
		//d1 = current sample rank
		//map3 = map from 
		//s1 = current observation id, taxonomy
		//rgsLevels = taxonomic levels to consider
		//map4 = map with initial minimum values
		//map5 = map with current minimum values
		//sTaxonString = current taxonomic string
		
		HashMap<String,Double> map4;
		String rgsLevels[];
		HashMap<String,Double> map2;
		double d1;
		String s1;
		String sTaxonString;
		
		//initializing levels
		rgsLevels = new String[]{"genus","family","order","class","phylum","kingdom"};
		
		//loading values at the otu level
		map2 = new HashMap<String,Double>(bio1.axsObservation.size()*7);
		map4 = new HashMap<String,Double>(bio1.axsObservation.size());
		for(String s:bio1.axsObservation.getIDs()){
			sTaxonString = bio1.axsObservation.getMetadata(s).get("taxonomy");
			s1 = s + "," + sTaxonString;
			map2.put(s1, -Double.MAX_VALUE);
			
			//looping through samples
			for(String t:bio1.axsSample.getIDs()){
				if(bio1.getValueByIDs(s, t)>0){
					d1 = mapAloofnesses.get(t);
					if(d1>map2.get(s1)){
						map2.put(s1, d1);
					}
				}
			}
			
			//updating taxonomy map
			if(!map4.containsKey(sTaxonString)){	
				map4.put(sTaxonString, map2.get(s1));
			}else{
				if(map4.get(sTaxonString)<map2.get(s1)){
					map4.put(sTaxonString, map2.get(s1));
				}
			}
		}
		
		//loading values for other taxonomic levels
		for(String u:rgsLevels){
			for(String s:map4.keySet()){
				sTaxonString = truncateTaxonomicString(s, u);
				if(map2.containsKey(sTaxonString)){
					if(map4.get(s)>map2.get(sTaxonString)){
						map2.put(sTaxonString, map4.get(s));
					}
				}else{
					map2.put(sTaxonString, map4.get(s));
				}
			}
		}
		
		//returning result
		return map2;
	}
	
	private static String truncateTaxonomicString(String sTaxonomicString, String sRank){
		
		//rgs1 = taxonomic string in array format
		//sbl1 = string builder
		//i1 = index to iterate to
		
		String[] rgs1;
		StringBuilder sbl1;
		int i1;
		
		rgs1 = sTaxonomicString.split(";");
		if(sRank.equals("kingdom")){
			i1=1;
		}else if(sRank.equals("phylum")){
			i1=2;
		}else if(sRank.equals("class")){
			i1=3;
		}else if(sRank.equals("order")){
			i1=4;
		}else if(sRank.equals("family")){
			i1=5;
		}else if(sRank.equals("genus")){
			i1=6;
		}else{
			i1=7;
		}
		sbl1 = new StringBuilder();
		for(int i=0;i<i1;i++){
			if(i>0){
				sbl1.append(";");
			}
			sbl1.append(rgs1[i]);
		}
		return sbl1.toString();
	}	
}