package edu.ucsf.base;

import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.io.BiomIO;

/**
 * Implementation of Dufrene-Legendre indicator species analysis; see https://www.rdocumentation.org/packages/labdsv/versions/2.0-1/topics/indval
 * @author jladau
 *
 */

public class PseudoIndicatorTaxa {

	/**BiomIO object**/
	private BiomIO bio1;
	
	/**Map from sample IDs to groups**/
	private HashMap<String,String> mapGroups;
	
	/**Map giving the indicator value for each taxon**/
	private HashMap<String,IndicatorValue> mapIndicator;
	
	public PseudoIndicatorTaxa(BiomIO bio1, HashMap<String,String> mapGroups){
		this.bio1 = bio1;
		this.mapGroups = mapGroups;
	}
	
	public HashSet<String> taxa(){
		return new HashSet<String>(mapIndicator.keySet());
	}
	
	public IndicatorValue get(String sTaxon){
		return mapIndicator.get(sTaxon);
	}
	
	public void loadValues(){
		
		//ind1 = current indicator value
		//i1 = counter
		//i2 = total number of taxa
		
		IndicatorValue ind1;
		int i1;
		int i2;
		
		mapIndicator = new HashMap<String,IndicatorValue>(bio1.axsObservation.size());
		i1 = 0;
		i2 = bio1.axsObservation.size();
		for(String sTaxon:bio1.axsObservation.getIDs()){
			i1++;
			System.out.println("Finding indicator value for taxon " + i1 + " of " + i2 + "...");
			ind1 = loadValue(sTaxon);
			mapIndicator.put(sTaxon, ind1);
		}
	}
	
	public double indicatorValue(String sTaxon, String sGroup, String sType){
		
		if(sType.equals("prevalence")){
			if(mapIndicator.get(sTaxon).mapPrevalence.containsKey(sGroup)){
				return mapIndicator.get(sTaxon).mapPrevalence.get(sGroup);
			}else{
				return 0;
			}
		}else if(sType.equals("abundance")){
			if(mapIndicator.get(sTaxon).mapAbundance.containsKey(sGroup)){
				return mapIndicator.get(sTaxon).mapAbundance.get(sGroup);
			}else {
				return 0;
			}
		}else{
			return Double.NaN;
		}
	}
	
	private IndicatorValue loadValue(String sTaxon){
		
		//map1 = map from samples to current abundances
		//sGroup = current group
		//ind1 = output
		
		IndicatorValue ind1;
		HashMap<String,Double> map1;
		String sGroup;
		
		map1 = bio1.getNonzeroValues(bio1.axsObservation, sTaxon);
		ind1 = new IndicatorValue(sTaxon, bio1.axsSample.size());
			
		for(String sSample:map1.keySet()){
			sGroup = mapGroups.get(sSample);
			ind1.mapAbundance.putSum(sGroup,map1.get(sSample));
			ind1.mapPrevalence.putSum(sGroup,1.);
			ind1.mapCountA.putSum(sGroup,1.);
		}
		ind1.normalizeValues();
		return ind1;
	}
	
	public class IndicatorValue{
		
		/**Map from class to prevalence**/
		public HashMap_AdditiveDouble<String> mapPrevalence;
		
		/**Map from class to mean abundance where occurs**/
		public HashMap_AdditiveDouble<String> mapAbundance;
		
		/**Map from class to abundance count**/
		public HashMap_AdditiveDouble<String> mapCountA;
		
		/**Abundance percentile**/
		public double dAbundancePercentile;
		
		/**Prevalence percentile**/
		public double dPrevalencePercentile;
		
		public IndicatorValue(String sTaxon, int iSamples){
			mapPrevalence = new HashMap_AdditiveDouble<String>(iSamples);
			mapAbundance = new HashMap_AdditiveDouble<String>(iSamples);
			mapCountA = new HashMap_AdditiveDouble<String>(iSamples);
		}
		
		public void normalizeValues(){
			
			//set1 = set of keys
			
			HashSet<String> set1;
			
			set1 = new HashSet<String>(mapPrevalence.keySet());
			for(String s:set1){
				mapPrevalence.put(s,mapPrevalence.get(s)/bio1.axsSample.size());
				mapAbundance.put(s,mapAbundance.get(s)/mapCountA.get(s));
			}	
		}
	}
}