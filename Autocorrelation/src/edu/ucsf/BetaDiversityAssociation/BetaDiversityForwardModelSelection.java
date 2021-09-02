package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
public class BetaDiversityForwardModelSelection{

	/**Data**/
	private BetaDiversityAssociationData dad1;
	
	public BetaDiversityForwardModelSelection(BetaDiversityAssociationData dad1) {
		this.dad1 = dad1;
	}

	public String header() {
		return "INITIAL_RANDOMIZATION,NUMBER_TAXA,TAXON_ADDED,PERFORMANCE_INCREASE";
	}

	public ArrayList<String> forwardModelSelection(){
		
		//lst1 = list of taxa to consider
		//set1 = set of starting taxa
		//set2 = set of remaining groups
		//set3 = set of selected groups
		//lstGroups = list of groups
		//fwd1 = current model
		//fwdBest = current best model
		//d1 = previous best model performance
		//bContinue = flag for whether a better model has been found
		//sbl1 = current output line
		//lstOut = output
		//map1 = current ses results
		
		HashMap<String,Double> mapSES;
		double d1;
		StringBuilder sbl1;
		boolean bContinue;
		ForwardModel fwd1;
		ForwardModel fwdBest;
		HashSet<String> set1;
		HashSet<String> set2;
		HashSet<String> set3;		
		ArrayList<String> lstGroups;
		ArrayList<String> lstOut;
		
		//loading set of starting taxa and list of taxa
		set1 = new HashSet<String>(dad1.mbd1.allTaxa());
		for(String s:dad1.mapGroups.keySet()) {
			set1.removeAll(dad1.mapGroups.get(s));
		}
		
		//loading list of groups
		lstGroups = new ArrayList<String>(dad1.mapGroups.keySet());
		
		//initializing output
		lstOut = new ArrayList<String>(lstGroups.size()*(dad1.iNullIterations+1)+1);
		lstOut.add("NUMBER_TAXA,TAXON_ADDED,SES,SES_INCREASE");
		
		//looping through null iterations		
		dad1.mbd1.updateIncludedTaxa(set1);
		set2 = new HashSet<String>(lstGroups);
		set3 = new HashSet<String>(lstGroups.size());
		fwdBest = null;
		d1 = 0;
		do {
			
			System.out.println("Model size " + (set3.size() + 1) + "...");
			
			bContinue = false;
			for(String sGroup:set2) {
				for(String sTaxon:dad1.mapGroups.get(sGroup)) {
					dad1.mbd1.addTaxon(sTaxon);
				}
				mapSES = dad1.rgn1.performanceSES(dad1.mbd1.metric(),"positive");
				fwd1 = new ForwardModel(sGroup, mapSES.get("observed"), mapSES.get("ses"));
				if(fwd1.isBetterThan(fwdBest)) {
					fwdBest = new ForwardModel(fwd1);
					bContinue = true;
					
					//******************************
					System.out.println(fwdBest);
					//******************************				
				}
				

				
				
				for(String sTaxon:dad1.mapGroups.get(sGroup)) {
					dad1.mbd1.removeTaxon(sTaxon);
				}
			}
			if(bContinue==true) {
				for(String sTaxon:dad1.mapGroups.get(fwdBest.sAddedGroup)) {
					dad1.mbd1.addTaxon(sTaxon);
				}
				set2.remove(fwdBest.sAddedGroup);
				set3.add(fwdBest.sAddedGroup);
				sbl1 = new StringBuilder();
				sbl1.append(set3.size());
				sbl1.append("," + fwdBest.sAddedGroup);
				sbl1.append("," + fwdBest.dPerformance + "," + (fwdBest.dPerformance-d1));
				d1 = fwdBest.dPerformance;
				lstOut.add(sbl1.toString());
			}
		}while(bContinue==true && set2.size()>0);
		return lstOut;
	}
	
	/*
	public ArrayList<String> forwardModelSelection0(){
		
		//lst1 = list of taxa to consider
		//set1 = set of starting taxa
		//set2 = set of remaining groups
		//set3 = set of selected groups
		//lstGroups = list of groups
		//fwd1 = current model
		//fwdBest = current best model
		//d1 = previous best model performance
		//bContinue = flag for whether a better model has been found
		//sbl1 = current output line
		//lstOut = output
		
		double d1;
		StringBuilder sbl1;
		boolean bContinue;
		ForwardModel fwd1;
		ForwardModel fwdBest;
		HashSet<String> set1;
		HashSet<String> set2;
		HashSet<String> set3;		
		ArrayList<String> lstGroups;
		ArrayList<String> lstOut;
		
		//loading set of starting taxa and list of taxa
		set1 = new HashSet<String>(dad1.mbd1.allTaxa());
		for(String s:dad1.mapGroups.keySet()) {
			set1.removeAll(dad1.mapGroups.get(s));
		}
		
		//loading list of groups
		lstGroups = new ArrayList<String>(dad1.mapGroups.keySet());
		
		//initializing output
		lstOut = new ArrayList<String>(lstGroups.size()*(dad1.iNullIterations+1)+1);
		lstOut.add("INITIAL_RANDOMIZATION,NUMBER_TAXA,TAXON_ADDED,PERFORMANCE,PERFORMANCE_INCREASE");
		
		//looping through null iterations
		//********************************************
		for(int i=0;i<dad1.iNullIterations;i++){
		//for(int i=-1;i<dad1.iNullIterations;i++){

		//********************************************
			
			System.out.println("Model selection " + (i+2) + " of " + (dad1.iNullIterations+1) + "...");				
			dad1.mbd1.updateIncludedTaxa(set1);
			set2 = new HashSet<String>(lstGroups);
			set3 = new HashSet<String>(lstGroups.size());
			fwdBest = null;
			d1 = 0;
			do {
				bContinue = false;
				for(String sGroup:set2) {
					for(String sTaxon:dad1.mapGroups.get(sGroup)) {
						dad1.mbd1.addTaxon(sTaxon);
					}
					fwd1 = new ForwardModel(sGroup, dad1.rgn1.regression(dad1.mbd1.metric(),"positive",i));
					if(fwd1.isBetterThan(fwdBest)) {
						fwdBest = new ForwardModel(fwd1);
						bContinue = true;
					}		
					for(String sTaxon:dad1.mapGroups.get(sGroup)) {
						dad1.mbd1.removeTaxon(sTaxon);
					}
				}
				if(bContinue==true) {
					for(String sTaxon:dad1.mapGroups.get(fwdBest.sAddedGroup)) {
						dad1.mbd1.addTaxon(sTaxon);
					}
					set2.remove(fwdBest.sAddedGroup);
					set3.add(fwdBest.sAddedGroup);
					sbl1 = new StringBuilder();
					if(i<0) {
						sbl1.append("observed");
					}else {
						sbl1.append("null_" + i);
					}
					sbl1.append("," + set3.size());
					sbl1.append("," + fwdBest.sAddedGroup);
					sbl1.append("," + fwdBest.dPerformance + "," + (fwdBest.dPerformance-d1));
					d1 = fwdBest.dPerformance;
					lstOut.add(sbl1.toString());
				}
			}while(bContinue==true && set2.size()>0);
		}
		return lstOut;
	}
	
	*/
		
	public class ForwardModel{
		
		/**Added predictor**/
		public String sAddedGroup;
		
		/**Performance**/
		public double dPerformance;
		
		/**Standardized effect size**/
		public double dSES;
		
		public ForwardModel(String sAddedGroup, double dPerformance, double dSES){
			this.sAddedGroup = sAddedGroup;
			this.dPerformance = dPerformance;
			this.dSES = dSES;
		}
		
		public ForwardModel(ForwardModel fwm1){
			this.sAddedGroup = fwm1.sAddedGroup;
			this.dPerformance = fwm1.dPerformance;	
			this.dSES = fwm1.dSES;
		}
		
		public boolean isBetterThan(ForwardModel fwm1) {
			if(fwm1==null || this.dSES>fwm1.dSES) {
				return true;
			}else {
				return false;
			}
		}
		
		public String toString() {
			return sAddedGroup + "," + dPerformance + "," + dSES;
		}
		
		
	}
	
}