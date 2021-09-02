package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import edu.ucsf.io.BiomIO;

public class MutableBetaDiversity0{

	/**Map from sample pairs to beta-diversity observations**/
	private HashMap<String,MutableBetaDiversityObservation> mapBeta;
	
	/**Map from taxon names to sample pair that it occurs in**/
	private HashMultimap<String,String> mapOccurrences;
	
	/**Set of currently included taxa**/
	private HashSet<String> setIncl;
	
	/**Set of currently exluded taxa**/
	private HashSet<String> setExcl;
	
	/**Abundance (across all samples) of each taxon**/
	private HashMap_AdditiveDouble<String> mapAbundance;
	
	public MutableBetaDiversity0(BiomIO bio1, ArrayList<String[]> lstSamplePairs) {
		
		//sSample1 = current first sample
		//sSample2 = current second sample
		//set1 = current set of occurring taxa
		//sSamplePair = current sample pair
		
		String sSamplePair;
		String sSample1;
		String sSample2;
		HashSet<String> set1;
		
		mapBeta = new HashMap<String,MutableBetaDiversityObservation>(lstSamplePairs.size());
		mapOccurrences = HashMultimap.create(bio1.axsObservation.size(),(int) 0.2*bio1.axsSample.size());
		mapAbundance = new HashMap_AdditiveDouble<String>(bio1.axsObservation.size());
		for(int i=0;i<lstSamplePairs.size();i++){
			sSample1 = lstSamplePairs.get(i)[0];
			sSample2 = lstSamplePairs.get(i)[1];
			sSamplePair = sSample1 + "," + sSample2;
			mapBeta.put(
					sSamplePair,
					new MutableBetaDiversityObservation(
							bio1.getNonzeroValues(bio1.axsSample,sSample1),
							bio1.getNonzeroValues(bio1.axsSample,sSample2),
							sSample1,
							sSample2));
			set1 = mapBeta.get(sSamplePair).includedTaxa();
			for(String sTaxon:set1) {
				mapOccurrences.put(sTaxon,sSamplePair);
			}
		}
		setIncl = new HashSet<String>(mapOccurrences.keySet());
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
	
	public double abundance(String sTaxon){
		if(sTaxon==null) {
			return Double.NaN;
		}else {
			return mapAbundance.get(sTaxon);
		}
	}
	
	public HashSet<String> includedAbundantTaxa(double dAbundance){
		
		//set1 = output
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(setIncl.size());
		for(String s:setIncl) {
			if(mapAbundance.get(s)>=dAbundance) {
				set1.add(s);
			}
		}
		return set1;
	}
	
	public HashSet<String> allTaxa(){
		return new HashSet<String>(mapAbundance.keySet());
	}
	
	
	public HashSet<String> mostAbundantTaxa(int iTaxa){
		
		//set1 = output
		//map1 = map from abundances to taxon names
		
		TreeMultimap<Double,String> map1;
		HashSet<String> set1;
		
		set1 = new HashSet<String>(iTaxa);
		map1 = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for(String s:mapAbundance.keySet()) {
			map1.put(mapAbundance.get(s),s);
		}
		for(Double d:map1.keySet()) {
			if(set1.size() + map1.get(d).size()<=iTaxa) {
				set1.addAll(map1.get(d));
			}else {
				return set1;
			}
		}
		return set1;
	}
	
	public HashSet<String> excludedTaxa(){
		return setExcl;
	}
	
	public HashMap<String,Double> brayCurtis(){
		
		//map1 = output
		
		HashMap<String,Double> map1;
		
		map1 = new HashMap<String,Double>(mapBeta.size());
		for(String s:mapBeta.keySet()){
			map1.put(s,mapBeta.get(s).brayCurtis());
		}
		return map1;
	}

	public HashMap<String,Double> brayCurtis(HashMap<String,String> mapMerge){
		
		//map1 = map from merged sample names to sum of merged values
		//map2 = map from merged sample names to count of merged values
		//map3 = output
		
		HashMap_AdditiveDouble<String> map1;
		HashMap_AdditiveDouble<String> map2;
		HashMap<String,Double> map3;
		
		
		map1 = new HashMap_AdditiveDouble<String>(mapMerge.size());
		map2 = new HashMap_AdditiveDouble<String>(mapMerge.size());
		map3 = new HashMap<String,Double>(mapMerge.size());
		for(String s:mapBeta.keySet()){
			map1.putSum(mapMerge.get(s),mapBeta.get(s).brayCurtis());
			map2.putSum(mapMerge.get(s),1.);
		}
		map3 = new HashMap<String,Double>(map1.size());
		for(String s:map1.keySet()){
			map3.put(s,map1.get(s)/map2.get(s));
		}
		return map3;
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
		
		public MutableBetaDiversityObservation(HashMap<String,Double> mapNonZeroSample1, HashMap<String,Double> mapNonZeroSample2, String sSample1, String sSample2){
			
			//d1 = first value
			//d2 = second value
			//dNum = numerator term
			//dDen = denominator term
			
			double d1;
			double d2;
			double dNum;
			double dDen;
			
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
				if(!mapAbundance.containsKey(s) || d1>mapAbundance.get(s)) {
					mapAbundance.put(s,d1);
				}
				if(mapNonZeroSample2.containsKey(s)){
					d2 = mapNonZeroSample2.get(s);
				}else{
					d2=0;
				}
				dNum=Math.abs(d1-d2);
				dDen=d1+d2;
				dNumSum+=dNum;
				dDenSum+=dDen;
				mapNumIncl.put(s,dNum);
				mapDenIncl.put(s,dDen);
			}
			for(String s:mapNonZeroSample2.keySet()){
				if(!mapNonZeroSample1.containsKey(s)){
					d2 = mapNonZeroSample2.get(s);
					if(!mapAbundance.containsKey(s) || d2>mapAbundance.get(s)) {
						mapAbundance.put(s,d2);
					}
					d1 = 0;
					dNum=Math.abs(d1-d2);
					dDen=d1+d2;
					dNumSum+=dNum;
					dDenSum+=dDen;
					mapNumIncl.put(s,dNum);
					mapDenIncl.put(s,dDen);
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
		
		public double brayCurtis(){
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