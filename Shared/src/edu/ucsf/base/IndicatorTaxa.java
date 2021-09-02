package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.io.BiomIO;

/**
 * Implementation of Dufrene-Legendre indicator species analysis; see https://www.rdocumentation.org/packages/labdsv/versions/2.0-1/topics/indval
 * @author jladau
 *
 */

public class IndicatorTaxa {

	/**BiomIO object**/
	private BiomIO bio1;
	
	/**Map from sample IDs to groups**/
	private HashMap<String,String> mapGroups;
	
	/**Map giving the number of samples in each class**/
	private HashMap_AdditiveInteger<String> mapN;
	
	/**Map giving the indicator value for each taxon**/
	private HashMap<String,IndicatorValue> mapIndicator;
	
	public IndicatorTaxa(BiomIO bio1, HashMap<String,String> mapGroups){
		this.bio1 = bio1;
		this.mapGroups = mapGroups;
		mapN = new HashMap_AdditiveInteger<String>(mapGroups.size());
		for(String s:mapGroups.keySet()){
			mapN.putSum(mapGroups.get(s), 1);
		}
	}
	
	public HashSet<String> taxa(){
		return new HashSet<String>(mapIndicator.keySet());
	}
	
	public IndicatorValue get(String sTaxon){
		return mapIndicator.get(sTaxon);
	}
	
	public void loadValues(int iIterations){
		
		//lstSamples = list of samples (for randomizing)
		//ind1 = current indicator value
		//ind2 = randomized value being added
		//i1 = counter
		//i2 = total number of taxa
		
		ArrayList<String> lstSamples;
		IndicatorValue ind1;
		IndicatorValue ind2;
		int i1;
		int i2;
		
		mapIndicator = new HashMap<String,IndicatorValue>(bio1.axsObservation.size());
		lstSamples = new ArrayList<String>(bio1.axsSample.getIDs());
		i1 = 0;
		i2 = bio1.axsObservation.size();
		for(String sTaxon:bio1.axsObservation.getIDs()){
			i1++;
			System.out.println("Finding indicator value for taxon " + i1 + " of " + i2 + "...");
			ind1 = loadValue(sTaxon, null);
			for(int i=0;i<iIterations;i++){
				Collections.shuffle(lstSamples);
				ind2 = loadValue(sTaxon, lstSamples);
				ind1.updatePValue(ind2);
			}
			mapIndicator.put(sTaxon, ind1);
		}
		
	}
	
	public IndicatorValue indicatorValue(String sTaxon){
		return mapIndicator.get(sTaxon);
	}
	
	private IndicatorValue loadValue(String sTaxon, ArrayList<String> lstRandomizedSamples){
		
		//map1 = map from samples to current abundances
		//mapF = map giving presence-absence sum
		//mapA = map giving frequency sum
		//sGroup = current group
		//d1 = current value (for normalization)
		//d2 = total value for normalization of abundances
		//d3 = current product
		//dMax = current maximum value
		//sMax = current maximum class
		//i1 = counter
		
		HashMap<String,Double> map1;
		HashMap_AdditiveDouble<String> mapA;
		HashMap_AdditiveDouble<String> mapF;
		String sGroup;
		double d1;
		double d2;
		double d3;
		double dMax;
		String sMax;
		int i1;
		
		map1 = bio1.getNonzeroValues(bio1.axsObservation, sTaxon);
		mapF = new HashMap_AdditiveDouble<String>(mapN.size());
		mapA = new HashMap_AdditiveDouble<String>(mapN.size());	
		if(lstRandomizedSamples == null){
			for(String sSample:map1.keySet()){
				sGroup = mapGroups.get(sSample);
				mapF.putSum(sGroup, 1.);
				mapA.putSum(sGroup, map1.get(sSample));
			}
		}else{
			i1 = 0;
			for(String sSample:map1.keySet()){
				sGroup = mapGroups.get(lstRandomizedSamples.get(i1));
				i1++;
				mapF.putSum(sGroup, 1.);
				mapA.putSum(sGroup, map1.get(sSample));
			}
		}
		d2 = 0;
		for(String s:mapN.keySet()){
			if(mapF.containsKey(s)){
				d1 = mapF.get(s);
				mapF.put(s, d1/mapN.get(s));
				d1 = mapA.get(s);
				mapA.put(s, d1/mapN.get(s));
				d2 += d1/mapN.get(s);
			}
		}
		sMax = null;
		dMax = -Double.MAX_VALUE;
		for(String s:mapN.keySet()){
			if(mapF.containsKey(s)){
				d1 = mapA.get(s);
				mapA.put(s, d1/d2);
				d3 = mapF.get(s)*mapA.get(s);
				if(d3>dMax){
					dMax = d3;
					sMax = s;
				}
			}
			
		}
		return new IndicatorValue(dMax, sMax);
	}
	
	public class IndicatorValue{
		
		/**Observed value**/
		private double dValue;
		
		/**Class with maximum value**/
		private String sClass;
		
		/**Number of randomized values greater than observed value**/
		private int iGreaterThan;
		
		/**Number of randomized values**/
		private int iRandomizations;
		
		public IndicatorValue(double dValue, String sClass){
			this.dValue = dValue;
			this.sClass = sClass;
			iGreaterThan = 0;
			iRandomizations = 0;
		}
		
		public void updatePValue(IndicatorValue ind1){
			iRandomizations++;
			if(ind1.value()>this.value()){
				iGreaterThan++;
			}
		}
		
		public double value(){
			return dValue;
		}
		
		public String classification(){
			return sClass;
		}
		
		public double pvalue(){
			return ((double) iGreaterThan)/((double) iRandomizations);
		}
		
		public String toString(){
			return value() + "," + classification() + "," + pvalue();
		}
	}
}