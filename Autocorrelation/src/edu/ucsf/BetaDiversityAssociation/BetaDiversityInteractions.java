package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;

import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.PowerSet;

public class BetaDiversityInteractions{

	/**Data**/
	private BetaDiversityAssociationData dad1;
	
	public BetaDiversityInteractions(BetaDiversityAssociationData dad1) {
		this.dad1 = dad1;
	}
	
	public ArrayList<String> twoFactorInteractions(){

		//pst1 = power set object
		//lstPowerSet = power set of taxa
		//mapGroups2 = current groups map
		//s1 = current first group name
		//s2 = current second group name		
		//lst1 = output
		//bContinue = flag for whether to continue
		//lstEffects = effects
		
		boolean bContinue;
		String s1;
		String s2;
		ArrayList<ArrayList<String>> lstPowerSet;
		PowerSet pst1;
		HashMultimap<String,String> mapGroups2;
		ArrayList<String> lst1;
		ArrayList<Effect> lstEffects;
		
		//loading power set of taxa
		pst1 = new PowerSet(new ArrayList<String>(new HashSet<String>(dad1.mapGroups.keys())));
		lstPowerSet = new ArrayList<ArrayList<String>>((int) Math.pow(2,dad1.mapGroups.size()));
		do {
			pst1.next();
			lstPowerSet.add(pst1.currentSubset());
		}while(pst1.hasNext());
		
		//initializing output
		lst1 = new ArrayList<String>(lstPowerSet.size()*(lstPowerSet.size()-1)/2*dad1.rgn1.nullIterations()+1);
		lst1.add("RANDOMIZATION,FACTOR_1,FACTOR_2,FACTOR_1_SIZE,FACTOR_2_SIZE,INTERACTION");
		
		//loading effects
		lstEffects = loadEffects();
		
		//looping through all pairs of subsets
		for(int i=1;i<lstPowerSet.size();i++){
			s1 = Joiner.on(" + ").join(lstPowerSet.get(i));
					
			for(int j=0;j<i;j++){
				
				//checking for overlap
				//TODO note that these checks are going to be slow
				bContinue = false;
				if(lstPowerSet.get(i).size()<lstPowerSet.get(j).size()) {
					for(String s:lstPowerSet.get(i)) {
						if(lstPowerSet.get(j).contains(s)) {
							bContinue=true;
							break;
						}
					}
				}else {
					for(String s:lstPowerSet.get(j)) {
						if(lstPowerSet.get(i).contains(s)) {
							bContinue=true;
							break;
						}
					}
				}
				if(bContinue==true) {
					continue;
				}
				
				//finding interactions
				s2 = Joiner.on(" + ").join(lstPowerSet.get(j));
				
				mapGroups2 = HashMultimap.create(2,10);
				for(String s:lstPowerSet.get(i)){
					mapGroups2.put(s1,s);
				}
				for(String s:lstPowerSet.get(j)){
					mapGroups2.put(s2,s);
				}
				lst1.addAll(twoFactorInteractionsInternal(mapGroups2, lstEffects));
			}
		}
		
		//returning result
		return lst1;
	}
	
	private ArrayList<String> twoFactorInteractionsInternal(HashMultimap<String,String> mapGroups2, ArrayList<Effect> lstEffects){
		
		//tfa1 = two factor experiment
		//lstOut = output
		//rgs1 = factors
		//rgi1 = factor sizes
		
		String[] rgs1;
		int[] rgi1;
		ArrayList<String> lstOut;
		TwoFactorExperiment tfa1;
		
		tfa1 = new TwoFactorExperiment(lstEffects, mapGroups2);
		lstOut = new ArrayList<String>(lstEffects.get(0).size()*lstEffects.size()+1);
		rgs1 = tfa1.factors();
		rgi1 = tfa1.factorSizes();
		for(String sRandomization:tfa1.randomizations()) {
			lstOut.add(sRandomization + "," + rgs1[0] + "," + rgs1[1] + "," + rgi1[0] + "," + rgi1[1] + "," + tfa1.interaction(sRandomization));
		}
		return lstOut;		
	}
	
	public ArrayList<String> coefficients(){
		
		//lstTaxa = list of all taxa
		//lstOut = output
		//map1 = map of estimates
		//s1 = joined list of taxa
		
		ArrayList<String> lstOut;
		ArrayList<String> lstTaxa;
		HashMap<String,Double> map1;
		String s1;
		
		//loading list of groups
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		
		//loading taxa
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}

		//initializing output
		lstOut = new ArrayList<String>(lstTaxa.size()*2+1);
		lstOut.add("TAXA,RANDOMIZATION,VARIABLE,ESTIMATE");
		s1 = Joiner.on(" + ").join(lstTaxa);
		map1 = dad1.rgn1.coefficients(dad1.mbd1.metric());
		for(String s:map1.keySet()){
			lstOut.add(s1 + "," + s + "," + map1.get(s));
		}
		return lstOut;
	}
	
	public ArrayList<String> effects(){
		
		//lst1 = list of effects
		//lstOut = output
		
		ArrayList<Effect> lst1;
		ArrayList<String> lstOut;
		
		lst1 = loadEffects();
		lstOut = new ArrayList<String>(lst1.get(0).size()*lst1.size()+1);
		lstOut.add("RANDOMIZATION,TAXA,REMAINING_COMMUNITY,NUMBER_INDIVIDUAL_TAXA,PERFORMANCE");
		for(Effect eft1:lst1) {
			lstOut.addAll(eft1.print());
		}
		return lstOut;
	}
	
	private ArrayList<Effect> loadEffects(){
		
		//sGroup = current group
		//lstGroups = list of groups
		//pst1 = power set object
		//rgs1 = current group being added or removed
		//lstEffects = list of effects
		
		ArrayList<Effect> lstEffects;
		String rgs1[];
		PowerSet pst1;
		String sGroup;
		ArrayList<String> lstGroups;
		
		//loading list of groups
		lstGroups = new ArrayList<String>(dad1.mapGroups.keySet());
		
		//initializing output
		lstEffects = new ArrayList<Effect>((int) Math.pow(2,lstGroups.size())*dad1.rgn1.nullIterations());
		
		//looping through power set
		dad1.mbd1.removeAllTaxa();
		pst1 = new PowerSet(lstGroups);
		do {
			rgs1 = pst1.next();
			sGroup = rgs1[0];
			if(rgs1[1].equals("add")){
				for(String sTaxon:dad1.mapGroups.get(sGroup)) {
					dad1.mbd1.addTaxon(sTaxon);
				}	
			}else if(rgs1[1].equals("remove")) {
				for(String sTaxon:dad1.mapGroups.get(sGroup)) {
					dad1.mbd1.removeTaxon(sTaxon);
				}
			}
			lstEffects.add(new Effect(
					pst1.currentSubset(),
					dad1.rgn1.performance(dad1.mbd1.metric(), "positive")));
		}while(pst1.hasNext());
		return lstEffects;
	}
	
	private class TwoFactorExperiment{
		
		/**Map from randomizations to interactions**/
		private HashMap_AdditiveDouble<String> mapInteractions;
		
		/**First factor**/
		private String sFactor1;
		
		/**Number of taxa in first factor**/
		private int iFactor1;
		
		/**Number of taxa in second factor**/
		private int iFactor2;
		
		/**Second factor**/
		private String sFactor2;
		
		public TwoFactorExperiment(ArrayList<Effect> lstEffects, HashMultimap<String,String> mapGroups) {
			
			//i1 = number of effects considered
			//lst1 = ordered list of factors
			
			ArrayList<String> lst1;
			int i1;
			
			//initializing interactions map
			mapInteractions = new HashMap_AdditiveDouble<String>(lstEffects.get(0).size());
			
			//loading factors
			lst1 = new ArrayList<String>(mapGroups.keySet());
			sFactor1 = lst1.get(0);
			sFactor2 = lst1.get(1);
			iFactor1 = mapGroups.get(sFactor1).size();
			iFactor2 = mapGroups.get(sFactor2).size();
			
			i1 = 0;
			for(Effect efc1:lstEffects){
				if(sameGroups(efc1, mapGroups)) {
					for(String sRandomization:efc1.randomizations()) {
						mapInteractions.putSum(sRandomization,efc1.effect(sRandomization));
					}
					i1++;
				}
				for(String s:mapGroups.keySet()){
					if(sameGroups(efc1, mapGroups.get(s))) {
						for(String sRandomization:efc1.randomizations()) {
							mapInteractions.putSum(sRandomization,-efc1.effect(sRandomization));
						}
						i1++;
						break;
					}
				}
				if(i1==3) {
					break;
				}
			}
		}
		
		private boolean sameGroups(Effect efc1, Set<String> setGroups) {
			
			if(efc1.numberGroups()!=setGroups.size()) {
				return false;
			}
			for(String s:efc1.groups()) {
				if(!setGroups.contains(s)) {
					return false;
				}
			}
			return true;
		}
		
		private boolean sameGroups(Effect efc1, HashMultimap<String,String> mapGroups) {
			
			//set1 = set to check
			
			Set<String> set1;
			
			set1 = new HashSet<String>(100);
			for(String s:mapGroups.keySet()){
				set1.addAll(mapGroups.get(s));
			}
			return sameGroups(efc1,set1);
		}
		
		public String[] factors(){
			return new String[] {sFactor1, sFactor2};
		}
		
		public int[] factorSizes() {
			return new int[] {iFactor1, iFactor2};
		}
		
		public double interaction(String sRandomization) {
			return mapInteractions.get(sRandomization);
		}
		
		public Set<String> randomizations(){
			return mapInteractions.keySet();
		}
	}
	
	private class Effect{
		
		/**Flag for whether remaining community is included**/
		private boolean bRemainingCommunity;
		
		/**Groups that are included**/
		private ArrayList<String> lstGroups;
		
		/**Map from randomizations to effect sizes**/
		private HashMap<String,Double> mapEffects;
		
		public Effect(ArrayList<String> lstGroups, HashMap<String,Double> mapEffects){
			this.lstGroups = lstGroups;
			if(lstGroups.contains("remaining_community")) {
				bRemainingCommunity=true;
				lstGroups.remove("remaining_community");
			}else {
				bRemainingCommunity=false;
			}
			this.mapEffects = mapEffects;
		}
		
		public String toString() {
			return Joiner.on(" + ").join(lstGroups);
		}
		
		/*
		public boolean remainingCommunity() {
			return bRemainingCommunity;
		}
		*/
		
		public ArrayList<String> print() {
			
			//lst1 = output
			
			ArrayList<String> lst1;
			
			lst1 = new ArrayList<String>(mapEffects.size());
			for(String s:mapEffects.keySet()){
				lst1.add(s + "," + Joiner.on(" + ").join(lstGroups) + "," + bRemainingCommunity + "," + lstGroups.size() + "," + mapEffects.get(s));
			}
			return lst1;
		}
		
		public Set<String> randomizations(){
			return mapEffects.keySet();
		}
		
		public double effect(String sRandomization) {
			return mapEffects.get(sRandomization);
		}
		
		public ArrayList<String> groups(){
			return lstGroups;
		}
		
		public int numberGroups() {
			return lstGroups.size();
		}
		
		public int size() {
			return mapEffects.size();
		}
	}

	
	/*
	public ArrayList<String> printData(){
		
		//iTaxa = number of taxa
		//iSamples = number of samples
		//lstOut = output
		//lstTaxa = list of taxa
		//map1 = current beta-diversity map
		//mapLongNames = map from short names to long names
		//sTaxon = current taxon
		
		HashMap<String,String> mapLongNames;
		HashMap<String,Double> map1;

		ArrayList<String> lstTaxa;
		int iTaxa;
		int iSamples;
		ArrayList<String> lstOut;
		String sTaxon;
		
		//loading long names map
		mapLongNames = new HashMap<String,String>(dad1.mapShortNames.size());
		for(String s:dad1.mapShortNames.keySet()) {
			mapLongNames.put(dad1.mapShortNames.get(s),s);
		}
		
		//initializing output
		iTaxa=0;
		for(String s:dad1.mapGroups.keySet()) {
			iTaxa+=dad1.mapGroups.get(s).size();
		}
		iSamples = dad1.bio1.axsSample.size();
		lstOut = new ArrayList<String>(iTaxa*iSamples + dad1.mbd1.size()*(int) (Math.pow(2,iTaxa)-1) + 1);
		lstOut.add("VARIABLE,TAXA,SAMPLE_1,SAMPLE_2,VALUE");
		
		//outputting relative abundance
		for(String sGroup:dad1.mapGroups.keySet()) {
			for(String s:dad1.mapGroups.get(sGroup)) {
				sTaxon = mapLongNames.get(s);
				for(String sSample:dad1.bio1.axsSample.getIDs()) {
					lstOut.add("relative_abundance," + s + "," + sSample + ",na," + dad1.bio1.getValueByIDs(sTaxon,sSample));
				}
			}
		}
		
		//loading list of groups
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		
		//loading taxa
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}
		map1 = dad1.mbd1.metric();
		for(String s:map1.keySet()) {
			lstOut.add("bray_curtis," + Joiner.on(" + ").join(lstTaxa) + "," + s + "," + map1.get(s));
		}
		return lstOut;
	}
	*/

	/*
	public ArrayList<String> printDataAllSubsets(){
		
		//iTaxa = number of taxa
		//iSamples = number of samples
		//lstOut = output
		//sGroupCurrent = current group
		//lstGroups = list of groups
		//pst1 = power set object
		//rgs1 = current group being added or removed
		//map1 = current beta-diversity map
		//mapLongNames = map from short names to long names
		//sTaxon = current taxon
		
		HashMap<String,String> mapLongNames;
		HashMap<String,Double> map1;
		String rgs1[];
		PowerSet pst1;
		String sGroupCurrent;
		ArrayList<String> lstGroups;
		int iTaxa;
		int iSamples;
		ArrayList<String> lstOut;
		String sTaxon;
		
		//loading long names map
		mapLongNames = new HashMap<String,String>(dad1.mapShortNames.size());
		for(String s:dad1.mapShortNames.keySet()) {
			mapLongNames.put(dad1.mapShortNames.get(s),s);
		}
		
		//initializing output
		iTaxa=0;
		for(String s:dad1.mapGroups.keySet()) {
			iTaxa+=dad1.mapGroups.get(s).size();
		}
		iSamples = dad1.bio1.axsSample.size();
		lstOut = new ArrayList<String>(iTaxa*iSamples + dad1.mbd1.size()*(int) (Math.pow(2,iTaxa)-1) + 1);
		lstOut.add("VARIABLE,TAXA,SAMPLE_1,SAMPLE_2,VALUE");
		
		//outputting relative abundance
		for(String sGroup:dad1.mapGroups.keySet()) {
			for(String s:dad1.mapGroups.get(sGroup)) {
				sTaxon = mapLongNames.get(s);
				for(String sSample:dad1.bio1.axsSample.getIDs()) {
					lstOut.add("relative_abundance," + s + "," + sSample + ",na," + dad1.bio1.getValueByIDs(sTaxon,sSample));
				}
			}
		}
		
		//loading list of groups
		lstGroups = new ArrayList<String>(dad1.mapGroups.keySet());
		
		//looping through power set
		dad1.mbd1.removeAllTaxa();
		pst1 = new PowerSet(lstGroups);
		do{
			rgs1 = pst1.next();
			sGroupCurrent = rgs1[0];
			if(rgs1[1].equals("add")){
				for(String s:dad1.mapGroups.get(sGroupCurrent)) {
					dad1.mbd1.addTaxon(s);
				}	
			}else if(rgs1[1].equals("remove")) {
				for(String s:dad1.mapGroups.get(sGroupCurrent)) {
					dad1.mbd1.removeTaxon(s);
				}
			}
			map1 = dad1.mbd1.metric();
			for(String s:map1.keySet()) {
				lstOut.add("bray_curtis," + Joiner.on(" + ").join(pst1.currentSubset()) + "," + s + "," + map1.get(s));
			}
		}while(pst1.hasNext());
		return lstOut;
	}
	*/
}