package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.HashMap;

public class BetaDiversitySignificance{

	/**Data object**/
	public BetaDiversityAssociationData dad1;
	
	//TODO clean up data printing code
	
	public BetaDiversitySignificance(BetaDiversityAssociationData dad1) {
		this.dad1=dad1;
	}
	
	public ArrayList<String> significancePerformanceCovariates(){
		
		//lstOut = output
		//lst1 = current significance output
		//iTotal = total covariates
		//i1 = current covariate
		
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		int iTotal;
		int i1;
		
		lstOut = new ArrayList<String>((dad1.rgn1.nullIterations()+1)*dad1.rgdThresholds.length*dad1.mapCovariates.size());
		lstOut.add("COVARIATE,THRESHOLD,RANDOMIZATION,VALUE");
		i1 = 1;
		iTotal = dad1.mapCovariates.keySet().size();
		for(String sCovariate:dad1.mapCovariates.keySet()){
			System.out.println("Analyzing` covariate " + i1 + " of " + iTotal + "...");
			i1++;
			lst1 = this.significancePerformance(dad1.mapCovariates.get(sCovariate), "none");
			for(int i=1;i<lst1.size();i++){
				lstOut.add(sCovariate + "," + lst1.get(i));
			}
		}
		return lstOut;
	}

	public ArrayList<String> significanceSlopeSelectedTaxa() {
		
		//lstTaxa = list of taxa to consider
		
		ArrayList<String> lstTaxa;
		
		//loading list of groups
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		
		//loading taxa
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}
		return significanceSlope(dad1.mbd1.metric(), "positive");
	}

	
	
	public ArrayList<String> significancePerformanceSelectedTaxa() {
		
		//lstTaxa = list of taxa to consider
		
		ArrayList<String> lstTaxa;
		
		//loading list of groups
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		
		//loading taxa
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}
		return significancePerformance(dad1.mbd1.metric(), "positive");
	}

	/*
	
	public ArrayList<String> printDataCovariates(){
		
		//lstOut = output
		//lst1 = current significance output
		//iTotal = total covariates
		//i1 = current covariate
		
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		
		lstPercentiles = new ArrayList<Double>(1);
		lstPercentiles.add(dad1.dPercentile);
		lstOut = new ArrayList<String>(1000*lstPercentiles.size()*dad1.mapCovariates.size());
		lstOut.add("COVARIATE,PERCENTILE,SAMPLE,PREDICTOR,RESPONSE");
		for(String sCovariate:dad1.mapCovariates.keySet()){
			lst1 = this.printData(dad1.mapCovariates.get(sCovariate));
			for(int i=1;i<lst1.size();i++){
				lstOut.add(sCovariate + "," + lst1.get(i));
			}
		}
		return lstOut;
	}
	
	
	public ArrayList<String> printDataAllTaxa(){
		
		dad1.mbd1.addAllTaxa();
		return printData(dad1.mbd1.metric());
	}

	public ArrayList<String> printDataSelectedTaxa(){
		
		//lstTaxa = list of taxa to consider
		
		ArrayList<String> lstTaxa;
		
		//loading list of groups
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		
		//loading taxa
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}
		return printData(dad1.mbd1.metric());
	}
	*/
	
	public ArrayList<String> printAllData(String sMerged){
		
		//lstTaxa = list of taxa to consider
		//lstOut = output
		//lst1 = list of data
		//mapBeta = map from merged ids to beta diversity values
		//mapCov = map from merged ids to covariate values
		//mapResp = map from merged ids to response values
		//rgs1 = current line in split format
		
		String rgs1[];
		ArrayList<String> lstTaxa;
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		HashMap<String,String> mapBeta;
		HashMap<String,StringBuilder> mapCov;
		HashMap<String,String> mapResp;
		
		//loading bray-curtis output
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}
		if(sMerged.equals("merged")){	
			lst1 = dad1.rgn1.printMergedData(dad1.mbd1.metric());
		}else{
			lst1 = dad1.rgn1.printUnmergedData(dad1.mbd1.metric());
		}
		mapBeta = new HashMap<String,String>(lst1.size());
		mapResp = new HashMap<String,String>(lst1.size());
		for(int i=1;i<lst1.size();i++){
			rgs1 = lst1.get(i).split(",");
			if(sMerged.equals("merged")){	
				mapBeta.put(rgs1[0],rgs1[1]);
				mapResp.put(rgs1[0],rgs1[2]);
			}else {
				mapBeta.put(rgs1[0] + "," + rgs1[1], rgs1[2]);
				mapResp.put(rgs1[0] + "," + rgs1[1], rgs1[3]);
			}
		}
		
		//loading covariate output
		mapCov = new HashMap<String,StringBuilder>(lst1.size());
		mapCov.put("header",new StringBuilder());
		for(String sCovariate:dad1.mapCovariates.keySet()){
			if(sMerged.equals("merged")){	
				lst1 = dad1.rgn1.printMergedData(dad1.mapCovariates.get(sCovariate));
			}else{
				lst1 = dad1.rgn1.printUnmergedData(dad1.mapCovariates.get(sCovariate));
			}	
			for(int i=1;i<lst1.size();i++){
				rgs1 = lst1.get(i).split(",");
				if(sMerged.equals("merged")){	
					if(!mapCov.containsKey(rgs1[0])) {
						mapCov.put(rgs1[0],new StringBuilder());
					}
					mapCov.get(rgs1[0]).append("," + rgs1[1]);
				}else {
					if(!mapCov.containsKey(rgs1[0] + "," + rgs1[1])) {
						mapCov.put(rgs1[0] + "," + rgs1[1],new StringBuilder());
					}
					mapCov.get(rgs1[0] + "," + rgs1[1]).append("," + rgs1[2]);
				}
			}
			mapCov.get("header").append("," + sCovariate);
		}
		
		//outputting results
		lstOut = new ArrayList<String>(mapBeta.size()+1);
		if(sMerged.equals("merged")){	
			if(mapCov.size()>1){
				lstOut.add("SAMPLE_ID,RESPONSE,BETA_DIVERSITY" + mapCov.get("header"));
			}else {
				lstOut.add("SAMPLE_ID,RESPONSE,BETA_DIVERSITY");
			}
		}else {
			if(mapCov.size()>1){
				lstOut.add("SAMPLE_ID_1,SAMPLE_ID_2,RESPONSE,BETA_DIVERSITY" + mapCov.get("header"));
			}else {
				lstOut.add("SAMPLE_ID_1,SAMPLE_ID_2,RESPONSE,BETA_DIVERSITY");
			}
		}
		for(String s:mapBeta.keySet()) {
			if(mapCov.size()>1) {	
				lstOut.add(s + "," + mapResp.get(s) + "," + mapBeta.get(s) + mapCov.get(s));
			}else {
				lstOut.add(s + "," + mapResp.get(s) + "," + mapBeta.get(s));
			}
		}
		
		//loading response output
		return lstOut;
	}
	
	public ArrayList<String> printTransformedData(){
		
		//lstTaxa = list of taxa to consider
		//rgn1 = current regression object
		//lstOut = output
		//lst1 = current output
		
		Regression rgn1;
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		ArrayList<String> lstTaxa;
		
		//loading list of groups
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		
		//loading taxa
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}
		
		lstOut = new ArrayList<String>(dad1.rgdThresholds.length*dad1.mapResponses.size()+1);
		lstOut.add("THRESHOLD,PREDICTOR,RESPONSE");
		for(Double dThreshold:dad1.rgdThresholds){
			rgn1 = dad1.regressionNewThreshold(dThreshold);
			lst1 = rgn1.printTransformedData(dad1.mbd1.metric());
			for(int i=1;i<lst1.size();i++) {
				lstOut.add(dThreshold + "," + lst1.get(i));
			}
		}
		return lstOut;	
	}

	public ArrayList<String> printPredictions(){
		
		//lstTaxa = list of taxa to consider
		//rgn1 = current regression object
		//lstOut = output
		//lst1 = current output
		
		Regression rgn1;
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		ArrayList<String> lstTaxa;
		
		//loading list of groups
		lstTaxa = new ArrayList<String>(dad1.mapGroups.values());
		
		//loading taxa
		dad1.mbd1.removeAllTaxa();
		for(String s:lstTaxa) {
			dad1.mbd1.addTaxon(s);
		}
		
		lstOut = new ArrayList<String>(dad1.rgdThresholds.length*dad1.mapResponses.size()+1);
		lstOut.add("THRESHOLD,SAMPLE_ID,PREDICTOR,RESPONSE,PREDICTION");
		for(Double dThreshold:dad1.rgdThresholds){
			rgn1 = dad1.regressionNewThreshold(dThreshold);
			lst1 = rgn1.predictedValues(dad1.mbd1.metric());
			for(int i=1;i<lst1.size();i++) {
				lstOut.add(dThreshold + "," + lst1.get(i));
			}
		}
		return lstOut;	
	}
	
	private ArrayList<String> printData(HashMap<String,Double> mapUnmergedPredictors){
		
		//lstOut = output
		//lst1 = current data output being added
		//fqr1 = current regression object
		
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		Regression rgn1;
		
		lst1 = dad1.rgn1.printMergedData(mapUnmergedPredictors);
		lstOut = new ArrayList<String>(lst1.size()*(this.dad1.rgdThresholds.length+1)+1);
		lstOut.add("THRESHOLD,SAMPLE,PREDICTOR,RESPONSE");
		for(int i=1;i<lst1.size();i++) {
			lstOut.add("na," + lst1.get(i));
		}
		
		//TODO fix this
		/*
		for(Double dThreshold:dad1.lstThresholds){
			rgn1 = dad1.regressionNewThreshold(dThreshold);
			lst1 = rgn1.printQuantileData(mapUnmergedPredictors);
			for(int i=1;i<lst1.size();i++){
				lstOut.add(dThreshold + ",na," + lst1.get(i));
			}
		}
		*/
		return lstOut;
	}

	
	private ArrayList<String> significanceSlope(HashMap<String,Double> mapUnmergedPredictors, String sDirection){
		
		//rgn1 = current regression object
		//map1 = current output
		//lstOut = output
		
		Regression rgn1;
		HashMap<String,Double> map1;
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(dad1.rgdThresholds.length*(dad1.rgn1.nullIterations()+1)+1);
		lstOut.add("THRESHOLD,RANDOMIZATION,VALUE");
		for(Double dThreshold:dad1.rgdThresholds){
			rgn1 = dad1.regressionNewThreshold(dThreshold);
			map1 = rgn1.coefficients(mapUnmergedPredictors);
			for(String s:map1.keySet()){
				if(s.contains("slope")){
					lstOut.add(dThreshold + "," + s.split(",")[0] + "," + map1.get(s));
				}
			}
		}
		return lstOut;
	}
	
	private ArrayList<String> significancePerformance(HashMap<String,Double> mapUnmergedPredictors, String sDirection){
		
		//rgn1 = current regression object
		//map1 = current output
		//lstOut = output
		
		Regression rgn1;
		HashMap<String,Double> map1;
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(dad1.rgdThresholds.length*(dad1.rgn1.nullIterations()+1)+1);
		lstOut.add("THRESHOLD,RANDOMIZATION,VALUE");
		for(Double dThreshold:dad1.rgdThresholds){
			rgn1 = dad1.regressionNewThreshold(dThreshold);
			map1 = rgn1.performance(mapUnmergedPredictors, sDirection);
			for(String s:map1.keySet()){
				if(!s.contains("intercept")){
					lstOut.add(dThreshold + "," + s.split(",")[0] + "," + map1.get(s));
				}
			}
		}
		return lstOut;
	}
}