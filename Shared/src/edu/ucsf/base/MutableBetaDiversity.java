package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.collect.HashMultimap;
import edu.ucsf.io.BiomIO;

public class MutableBetaDiversity{

	/**Map from sample pairs to beta-diversity observations**/
	private HashMap<String,MutableBetaDiversityObservation> mapBeta;
	
	/**Map from taxon names to sample pair that it occurs in**/
	private HashMultimap<String,String> mapOccurrences;
	
	/**Set of currently included taxa**/
	private HashSet<String> setIncl;
	
	/**Set of currently exluded taxa**/
	private HashSet<String> setExcl;
	
	/**Set of all taxa**/
	private HashSet<String> setAllTaxa;
	
	/**Alias map (for bootstrapping): keys are sample aliases, values are actual sample names**/
	private HashMap<String,String> mapSamplePairAliases;
	
	public MutableBetaDiversity(
			BiomIO bio1, 
			ArrayList<String> lstSamplePairs, 
			String sMetric, 
			HashMap<String,String> mapTaxonShortNames, 
			HashMap<String,String> mapSamplePairAliases) {
		
		//sSample1 = current first sample
		//sSample2 = current second sample
		//set1 = current set of occurring taxa
		//sSamplePair = current sample pair
		
		String sSamplePair;
		String sSample1;
		String sSample2;
		HashSet<String> set1;
		
		this.mapSamplePairAliases = mapSamplePairAliases;
		mapBeta = new HashMap<String,MutableBetaDiversityObservation>(lstSamplePairs.size());
		mapOccurrences = HashMultimap.create(bio1.axsObservation.size(),(int) 0.2*bio1.axsSample.size());
		for(int i=0;i<lstSamplePairs.size();i++){
			sSample1 = lstSamplePairs.get(i).split(",")[0];
			sSample2 = lstSamplePairs.get(i).split(",")[1];
			sSamplePair = sSample1 + "," + sSample2;
			mapBeta.put(
					sSamplePair,
					new MutableBetaDiversityObservation(
							bio1.getNonzeroValues(bio1.axsSample,sSample1),
							bio1.getNonzeroValues(bio1.axsSample,sSample2),
							sSample1,
							sSample2,
							sMetric,
							mapTaxonShortNames));
			set1 = mapBeta.get(sSamplePair).includedTaxa();
			for(String sTaxon:set1) {
				mapOccurrences.put(sTaxon,sSamplePair);
			}
		}
		setIncl = new HashSet<String>(mapOccurrences.keySet());
		setAllTaxa = new HashSet<String>(mapOccurrences.keySet());
		setExcl = new HashSet<String>(setIncl.size());
	}
	
	public void updateIncludedTaxa(HashSet<String> setTaxaToInclude){
		
		//set1 = current set of taxa
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(includedTaxa());
		for(String s:set1){
			if(!setTaxaToInclude.contains(s)){
				removeTaxon(s);
			}
		}
		set1 = new HashSet<String>(excludedTaxa());
		for(String s:set1){
			if(setTaxaToInclude.contains(s)){
				addTaxon(s);
			}
		}
	}
	
	public void addTaxon(String sTaxon){
		for(String sSamplePair:mapOccurrences.get(sTaxon)){
			mapBeta.get(sSamplePair).addTaxon(sTaxon);
		}
		setExcl.remove(sTaxon);
		setIncl.add(sTaxon);
	}
	
	public void addAllTaxa() {
		
		//set1 = set of all taxa
		
		HashSet<String> setTaxa;
		
		removeAllTaxa();
		setTaxa = new HashSet<String>(this.allTaxa());
		for(String s:setTaxa) {
			addTaxon(s);
		}
	}
	
	public void removeAllTaxa() {
		
		//set1 = set of included taxa
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(includedTaxa());
		for(String s:set1) {
			this.removeTaxon(s);
		}
	}
	
	public void removeTaxon(String sTaxon){
		for(String sSamplePair:mapOccurrences.get(sTaxon)){
			mapBeta.get(sSamplePair).removeTaxon(sTaxon);
		}
		setExcl.add(sTaxon);
		setIncl.remove(sTaxon);
	}
	
	public HashSet<String> includedTaxa(){
		return setIncl;
	}
	
	public HashSet<String> allTaxa(){
		return setAllTaxa;
	}
	
	public HashSet<String> excludedTaxa(){
		return setExcl;
	}
	
	public HashMap<String,Double> metric(){
		
		//map1 = output
		
		HashMap<String,Double> map1;
		
		if(mapSamplePairAliases==null) {
			map1 = new HashMap<String,Double>(mapBeta.size());
			for(String s:mapBeta.keySet()){
				map1.put(s,mapBeta.get(s).metric());
			}
		}else {
			map1 = new HashMap<String,Double>(mapSamplePairAliases.size());
			for(String s:mapSamplePairAliases.keySet()) {
				map1.put(s,mapBeta.get(mapSamplePairAliases.get(s)).metric());
			}
		}
		return map1;
	}
	
	public int size() {
		return mapBeta.size();
	}

	public class MutableBetaDiversityObservation{
		
		/**Map from taxon names to numerator contributions (included)**/
		private HashMap<String,Double> mapNumIncl;
		
		/**Map from taxon names to denominator contributions (included)**/
		private HashMap<String,Double> mapDenIncl;
		
		/**Map from taxon names to numerator contributions (excluded)**/
		private HashMap<String,Double> mapNumExcl;
		
		/**Map from taxon names to denominator contributions (excluded)**/
		private HashMap<String,Double> mapDenExcl;
		
		/**Current numerator sum**/
		private double dNumSum;
		
		/**Current denominator sum**/
		private double dDenSum;
		
		/**First sample name**/
		private String sSample1;
		
		/**Second sample name**/
		private String sSample2;
		
		public MutableBetaDiversityObservation(
				HashMap<String,Double> mapNonZeroSample1, 
				HashMap<String,Double> mapNonZeroSample2, 
				String sSample1, 
				String sSample2,
				String sMetric,
				HashMap<String,String> mapTaxonShortNames){
			
			//d1 = first value
			//d2 = second value
			//dNum = numerator term
			//dDen = denominator term
			//sShortName = short taxon name
			
			double d1;
			double d2;
			double dNum;
			double dDen;
			String sShortName;
			
			//***************************
			//if(!sSample1.contains(".O")) {
			//	System.out.println("HERE");
			//}
			//***************************
			
			this.sSample1=sSample1;
			this.sSample2=sSample2;
			dNumSum = 0;
			dDenSum = 0;
			mapNumExcl = new HashMap<String,Double>(mapNonZeroSample1.size() + mapNonZeroSample2.size());
			mapDenExcl = new HashMap<String,Double>(mapNonZeroSample1.size() + mapNonZeroSample2.size());
			mapNumIncl = new HashMap<String,Double>(mapNonZeroSample1.size() + mapNonZeroSample2.size());
			mapDenIncl = new HashMap<String,Double>(mapNonZeroSample1.size() + mapNonZeroSample2.size());
			for(String s:mapNonZeroSample1.keySet()){
				d1 = mapNonZeroSample1.get(s);
				if(mapNonZeroSample2.containsKey(s)){
					d2 = mapNonZeroSample2.get(s);
				}else{
					d2=0;
				}
				if(sMetric.equals("bray_curtis")) {
					dNum=Math.abs(d1-d2);
					dDen=d1+d2;
				}else if(sMetric.equals("signed_bray_curtis")) {
						dNum=d2-d1;
						dDen=d1+d2;
				}else if(sMetric.equals("jaccard")) {
					if(d1>0){
						d1=1;
					}
					if(d2>0){
						d2=1;
					}
					dNum=Math.abs(d1-d2);
					dDen=d1+d2;
				}else {
					dNum=Double.NaN;
					dDen=Double.NaN;
				}
				
				dNumSum+=dNum;
				dDenSum+=dDen;
				if(mapTaxonShortNames.containsKey(s)){
					sShortName = mapTaxonShortNames.get(s);
				}else {
					sShortName = s;
				}
				mapNumIncl.put(sShortName,dNum);
				mapDenIncl.put(sShortName,dDen);
			}
			for(String s:mapNonZeroSample2.keySet()){
				if(!mapNonZeroSample1.containsKey(s)){
					d2 = mapNonZeroSample2.get(s);
					d1 = 0;
					if(sMetric.equals("bray_curtis")) {
						dNum=Math.abs(d1-d2);
						dDen=d1+d2;
					}else if(sMetric.equals("signed_bray_curtis")) {
						dNum=d2-d1;
						dDen=d1+d2;
					}else if(sMetric.equals("jaccard")) {
						if(d2>0){
							d2=1;
						}
						dNum=Math.abs(d1-d2);
						dDen=d1+d2;
					}else {
						dNum=Double.NaN;
						dDen=Double.NaN;
					}
					dNumSum+=dNum;
					dDenSum+=dDen;
					if(mapTaxonShortNames.containsKey(s)){
						sShortName = mapTaxonShortNames.get(s);
					}else {
						sShortName = s;
					}
					mapNumIncl.put(sShortName,dNum);
					mapDenIncl.put(sShortName,dDen);
				}
			}
		}
		
		public HashSet<String> includedTaxa(){
			return new HashSet<String>(mapDenIncl.keySet());
		}
		
		public String sampleName(int iIndex) {
			if(iIndex==1){
				return sSample1;
			}else if(iIndex==2){
				return sSample2;
			}else{
				return null;
			}
		}
		
		public double metric(){
			if(dDenSum>0){	
				return dNumSum/dDenSum;
			}else{
				return 0;
			}
		}
		
		public void removeTaxon(String sTaxon) {
			
			//dNum = taxon numerator contribution
			//dDen = taxon denominator contribution
			
			double dNum;
			double dDen;
			
			if(!mapNumIncl.containsKey(sTaxon)){
				return;
			}
			dNum = mapNumIncl.get(sTaxon);
			dDen = mapDenIncl.get(sTaxon);
			dNumSum-=dNum;
			dDenSum-=dDen;
			mapNumIncl.remove(sTaxon);
			mapDenIncl.remove(sTaxon);
			mapNumExcl.put(sTaxon,dNum);
			mapDenExcl.put(sTaxon,dDen);
		}

		public void addTaxon(String sTaxon) {
			
			//dNum = taxon numerator contribution
			//dDen = taxon denominator contribution
			
			double dNum;
			double dDen;
			
			if(!mapNumExcl.containsKey(sTaxon)){
				return;
			}
			dNum = mapNumExcl.get(sTaxon);
			dDen = mapDenExcl.get(sTaxon);
			dNumSum+=dNum;
			dDenSum+=dDen;
			mapNumExcl.remove(sTaxon);
			mapDenExcl.remove(sTaxon);
			mapNumIncl.put(sTaxon,dNum);
			mapDenIncl.put(sTaxon,dDen);
		}
	}
}