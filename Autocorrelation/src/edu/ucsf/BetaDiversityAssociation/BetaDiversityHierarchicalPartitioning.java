package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.HashMap_AdditiveInteger;

public class BetaDiversityHierarchicalPartitioning{

	/**Data**/
	private BetaDiversityAssociationData dad1;
	
	public BetaDiversityHierarchicalPartitioning(BetaDiversityAssociationData dad1) {
		this.dad1 = dad1;
	}

	public String header() {
		return "INITIAL_RANDOMIZATION,TAXON,MARGINAL_PERFORMANCE_INCREASE";
	}
	
	public ArrayList<String> partition(){
		
		//lstOut = output
		//map1 = output from hierarchical partitioning (no significance values)
		//map2 = output from hierarchical partitioning (significance values)
		//setGroupsToConsider = set of taxa for which to find significance
		
		HashMap<String,Double> map1;
		HashMap<String,Double> map2;
		ArrayList<String> lstOut;
		HashSet<String> setGroupsToConsider;
		
		//initial analysis: no significance values
		map1 = hierarchicalPartitioning(dad1.regressionNoNull(), dad1.setGroupsToConsider);
		
		//loading list of groups to find significance for
		setGroupsToConsider= new HashSet<String>(map1.size());
		for(String s:map1.keySet()) {
			if(map1.get(s)>=dad1.dObservedValueThreshold){
				setGroupsToConsider.add(s.split(",")[1]);
			}
		}
		
		//secondary analysis: significance values
		map2 = hierarchicalPartitioning(dad1.rgn1, setGroupsToConsider);
		
		//outputting results
		lstOut = new ArrayList<String>(map1.size() + map2.size());
		lstOut.add(dad1.hir1.header());
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		for(String s:map2.keySet()){
			if(!map1.containsKey(s)) {
				lstOut.add(s + "," + map2.get(s));
			}
		}
		return lstOut;
	}

	/*
	private HashMap<String,Double> hierarchicalPartitioning(Regression rgn1){
		
		//lst1 = list of taxa to consider
		//map1 = map giving the changes in ses for each taxon
		//map2 = map giving the counts for each taxon
		//map3 = map from run types (observed or null) to current cross validation r^2 values
		//map4 = map from run types (observed or null) to new cross validation r^2 values
		//set1 = set of starting taxa
		//sGroup = current group
		//lstGroups = list of groups
		
		String sGroup;
		HashSet<String> set1;
		ArrayList<String> lstGroups;
		HashMap_AdditiveDouble<String> map1;
		HashMap_AdditiveInteger<String> map2;
		HashMap<String,Double> map3;
		HashMap<String,Double> map4;
		
		//loading set of starting taxa and list of taxa
		set1 = new HashSet<String>(dad1.mbd1.allTaxa());
		for(String s:dad1.mapGroups.keySet()) {
			set1.removeAll(dad1.mapGroups.get(s));
		}
		
		//loading list of groups
		lstGroups = new ArrayList<String>(dad1.mapGroups.keySet());
		
		//initializing output
		map1 = new HashMap_AdditiveDouble<String>(lstGroups.size()*(rgn1.nullIterations()+2));
		map2 = new HashMap_AdditiveInteger<String>(lstGroups.size()*(rgn1.nullIterations()+2));
		
		//looping through reorderings
		for(int i=0;i<dad1.iPartitioningOrders;i++){
			System.out.println("Analyzing partitioning ordering " + (i+1) + " of " + dad1.iPartitioningOrders + "...");
			Collections.shuffle(lstGroups);				
			dad1.mbd1.updateIncludedTaxa(set1);
			map3 = rgn1.regression(dad1.mbd1.metric(), "positive");
			for(int k=0;k<lstGroups.size();k++) {
				sGroup = lstGroups.get(k);
				for(String sTaxon:dad1.mapGroups.get(sGroup)) {
					dad1.mbd1.addTaxon(sTaxon);
				}
				map4 = rgn1.regression(dad1.mbd1.metric(), "positive");
				if(map4.size()>0) {
					for(String s:map3.keySet()) {
						map1.putSum(s + "," + sGroup,map4.get(s)-map3.get(s));
						map2.putSum(s + "," + sGroup,1);	
					}
				}
				map3 = new HashMap<String,Double>(map4);
			}
		}
		for(String s:map2.keySet()) {
			map1.put(s,map1.get(s)/map2.get(s));
		}
		return map1;			
	}
	*/
	
	private HashMap<String,Double> hierarchicalPartitioning(Regression rgn1, HashSet<String> setGroupsToConsider){
		
		//lst1 = list of taxa to consider
		//map1 = map giving the changes in ses for each taxon
		//map2 = map giving the counts for each taxon
		//map3 = map from run types (observed or null) to current cross validation r^2 values
		//map4 = map from run types (observed or null) to new cross validation r^2 values
		//set1 = set of starting taxa
		//sGroup = current group
		//lstGroups = list of groups
		//bPrevious = flag for whether map3 is for previous group (to prevent recalculation)
		
		boolean bPrevious;
		String sGroup;
		HashSet<String> set1;
		ArrayList<String> lstGroups;
		HashMap_AdditiveDouble<String> map1;
		HashMap_AdditiveInteger<String> map2;
		HashMap<String,Double> map3=null;
		HashMap<String,Double> map4;
		
		//loading set of starting taxa and list of taxa
		set1 = new HashSet<String>(dad1.mbd1.allTaxa());
		for(String s:dad1.mapGroups.keySet()) {
			set1.removeAll(dad1.mapGroups.get(s));
		}
		
		//loading list of groups
		lstGroups = new ArrayList<String>(dad1.mapGroups.keySet());
		
		//initializing output
		map1 = new HashMap_AdditiveDouble<String>(lstGroups.size()*(rgn1.nullIterations()+2));
		map2 = new HashMap_AdditiveInteger<String>(lstGroups.size()*(rgn1.nullIterations()+2));
		
		//outputting cross validation for all taxa
		dad1.mbd1.updateIncludedTaxa(dad1.mbd1.allTaxa());
		map4 = rgn1.performance(dad1.mbd1.metric(), "positive");
		for(String s:map4.keySet()){
			map1.put(s + ",all_taxa",map4.get(s));
			map2.put(s + ",all_taxa",1);
		}
	
		//looping through reorderings
		for(int i=0;i<dad1.iPartitioningOrders;i++){
			System.out.println("Analyzing partitioning ordering " + (i+1) + " of " + dad1.iPartitioningOrders + "...");
			Collections.shuffle(lstGroups);				
			dad1.mbd1.updateIncludedTaxa(set1);
			for(int k=0;k<lstGroups.size();k++) {
				sGroup = lstGroups.get(k);
				bPrevious = false;
				if(setGroupsToConsider.contains(sGroup)) {
					if(bPrevious==false){
						map3 = rgn1.performance(dad1.mbd1.metric(), "positive");
					}
					for(String sTaxon:dad1.mapGroups.get(sGroup)) {
						dad1.mbd1.addTaxon(sTaxon);
					}
					map4 = rgn1.performance(dad1.mbd1.metric(), "positive");
					for(String s:map3.keySet()) {
						if(map4.containsKey(s)) {
							map1.putSum(s + "," + sGroup,map4.get(s)-map3.get(s));
						}else {
							map1.putSum(s + "," + sGroup,0.-map3.get(s));
						}
						map2.putSum(s + "," + sGroup,1);	
					}
					map3 = new HashMap<String,Double>(map4);
					bPrevious = true;
				}else {
					for(String sTaxon:dad1.mapGroups.get(sGroup)) {
						dad1.mbd1.addTaxon(sTaxon);
					}
					bPrevious = false;
				}
			}
		}
		for(String s:map2.keySet()) {
			map1.put(s,map1.get(s)/map2.get(s));
		}
		return map1;			
	}

	/*
	public HashMap<String,Double> hierarchicalPartitioningCVOrderSpecific(int iReorderings, HashMultimap<String,String> mapGroups){
		
		//lst1 = list of taxa to consider
		//map1 = map giving the changes in ses for each taxon
		//map2 = map giving the counts for each taxon
		//map3 = map from run types (observed or null) to current cross validation r^2 values
		//map4 = map from run types (observed or null) to new cross validation r^2 values
		//set1 = set of starting taxa
		//sGroup = current group
		//lstGroups = list of groups
		
		String sGroup;
		HashSet<String> set1;
		ArrayList<String> lstGroups;
		HashMap_AdditiveDouble<String> map1;
		HashMap_AdditiveInteger<String> map2;
		HashMap<String,Double> map3;
		HashMap<String,Double> map4;
		
		//loading set of starting taxa and list of taxa
		set1 = new HashSet<String>(mbd1.allTaxa());
		for(String s:mapGroups.keySet()) {
			set1.removeAll(mapGroups.get(s));
		}
		
		//loading list of groups
		lstGroups = new ArrayList<String>(mapGroups.keySet());
		
		//initializing output
		map1 = new HashMap_AdditiveDouble<String>(lstGroups.size()*(fqr1.nullIterations()+2));
		map2 = new HashMap_AdditiveInteger<String>(lstGroups.size()*(fqr1.nullIterations()+2));
		
		//looping through reorderings
		for(int i=0;i<iReorderings;i++){
			System.out.println("Analyzing reordering " + (i+1) + " of " + iReorderings + "...");
			Collections.shuffle(lstGroups);				
			mbd1.updateIncludedTaxa(set1);
			map3 = fqr1.regression(mbd1.metric());
			for(int k=0;k<lstGroups.size();k++) {
				sGroup = lstGroups.get(k);
				for(String sTaxon:mapGroups.get(sGroup)) {
					mbd1.addTaxon(sTaxon);
				}	
				map4 = fqr1.regression(mbd1.metric());
				if(k==0) {
					for(String s:map4.keySet()) {
						map1.putSum(k + "," + s + "," + sGroup,map4.get(s));
						map2.putSum(k + "," + s + "," + sGroup,1);	
					}
				}else {
					for(String s:map3.keySet()) {
						map1.putSum(k + "," + s + "," + sGroup,map4.get(s)-map3.get(s));
						map2.putSum(k + "," + s + "," + sGroup,1);	
					}
				}
				map3 = new HashMap<String,Double>(map4);
			}
		}
		for(String s:map2.keySet()) {
			map1.put(s,map1.get(s)/map2.get(s));
		}
		return map1;			
	}
	*/
}