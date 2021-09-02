package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.HashMap_AdditiveDouble;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class LogisticRegression implements Regression{

	/**Response variable map from merged sample pairs to thresholded values**/
	private HashMap<String,Double> mapResponses;
	
	/**Current Predictors (merged)**/
	private HashMap<String,Double> mapPredictors;
	
	/**List of randomized responses (merged and thresholded)**/
	private ArrayList<HashMap<String,Double>> lstRandomizedResponseMaps;
	
	/**Map from sample pairs to merged sample ids**/
	private HashMap<String,String> mapMerge;
	
	/**Threshold for binarization**/
	private double dThreshold;
	
	/**Unmerged responses**/
	private HashMap<String,Double> mapUnmergedResponses;
	
	/**Random number generator**/
	private Random rnd1;
	
	public LogisticRegression(HashMap<String,Double> mapUnmergedResponses, HashMap<String,String> mapMerge, double dThreshold, int iNullIterations){
		
		//lst1 = list of merged sample names
		//lst2 = list of merged sample values
		
		ArrayList<String> lst1;
		ArrayList<Double> lst2;

		//saving values
		this.dThreshold = dThreshold;
		this.mapMerge = mapMerge;
		
		//merging response values
		this.mapResponses = thresholdResponse(mergeData(mapUnmergedResponses));
		
		//loading randomized response maps
		lstRandomizedResponseMaps = new ArrayList<HashMap<String,Double>>(iNullIterations);
		lst1 = new ArrayList<String>(mapResponses.size());
		lst2 = new ArrayList<Double>(mapResponses.size());
		for(String s:mapResponses.keySet()) {
			lst1.add(s);
			lst2.add(mapResponses.get(s));
		}
		for(int i=0;i<iNullIterations;i++){
			lstRandomizedResponseMaps.add(new HashMap<String,Double>(lst1.size()));
			Collections.shuffle(lst2);
			for(int k=0;k<lst1.size();k++) {
				lstRandomizedResponseMaps.get(i).put(lst1.get(k),lst2.get(k));
			}
		}
		this.mapUnmergedResponses=mapUnmergedResponses;
		rnd1 = new Random(System.currentTimeMillis());
	}
	
	/**Returns transformed (e.g., quantile) data**/
	public ArrayList<String> printTransformedData(HashMap<String,Double> mapUnmergedPredictors){
		return new ArrayList<String>();
	}
	
	/**Returns map from analysis type (null or observed) to performance measure**/
	public HashMap<String,Double> performance(HashMap<String,Double> mapUnmergedPredictors, String sDirection){
		
		//map1 = output
		//d1 = current value
		
		HashMap<String,Double> map1;
		double d1;
		
		mapPredictors = mergeData(mapUnmergedPredictors);
		map1 = new HashMap<String,Double>(lstRandomizedResponseMaps.size()+1);
		d1 = calculatePerformanceStatistic(mapResponses, sDirection);
		if(!Double.isNaN(d1)){
			map1.put("observed",d1);
		}else {
			return map1;
		}
		for(int i=0;i<lstRandomizedResponseMaps.size();i++){
			d1 = calculatePerformanceStatistic(lstRandomizedResponseMaps.get(i), sDirection);
			if(!Double.isNaN(d1)){
				map1.put("null_" + i,d1);
			}
		}
		return map1;
	}
	
	/**Returns standardized effect size for performance measure**/
	public HashMap<String,Double> performanceSES(HashMap<String,Double> mapUnmergedPredictors, String sDirection){
		
		//map1 = map of values
		//dSx = sum of null values
		//dSx2 = sum of null values squared
		//dN = number of null values
		//dObs = observed value
		//d1 = current value
		//map2 = output
		
		HashMap<String,Double> map1;
		HashMap<String,Double> map2;
		double dSx;
		double dSx2;
		double dN;
		double dObs;
		double d1;
		
		map2 = new HashMap<String,Double>(2);
		map1 = performance(mapUnmergedPredictors, sDirection);
		if(!map1.containsKey("observed")) {
			map2.put("observed",Double.NaN);
			map2.put("ses",0.);
			return map2;
		}
		dSx = 0.;
		dSx2 = 0.;
		dN = 0.;
		dObs = map1.get("observed");
		for(String s:map1.keySet()){
			if(s.startsWith("null_")) {
				d1 = map1.get(s);
				dSx += d1;
				dSx2 += d1*d1;
				dN++;
			}
		}
		map2.put("observed",dObs);
		map2.put("ses",ExtendedMath.standardizedEffectSize(dObs,dSx,dSx2,dN));
		return map2;
	}
	
	/**Returns performance measure for observed analysis or specified null iteration**/
	public double performance(HashMap<String,Double> mapUnmergedPredictors, String sDirection, int iNullIteration){
		
		//d1 = current value
		
		double d1;
		
		mapPredictors = mergeData(mapUnmergedPredictors);
		if(iNullIteration>0) {
			d1 = calculatePerformanceStatistic(lstRandomizedResponseMaps.get(iNullIteration), sDirection);
		}else {
			d1 = calculatePerformanceStatistic(mapResponses, sDirection);
		}
		return d1;
	}
	
	/**Returns predicted values**/
	public ArrayList<String> predictedValues(HashMap<String,Double> mapUnmergedPredictors){

		//lstAttr = list of attributes
		//lst1 = reponse values
		//ist1 = instances object
		//log1 = logistic regression object
		//lstPred = list of predictors
		//lstResp = list of responses
		//lstSamples = list of samples
		//lstOut = output
		//rgd1 = predicted values
		
		ArrayList<String> lstOut;
		ArrayList<Double> lstPred;
		ArrayList<Double> lstResp;
		ArrayList<String> lstSamples;
		ArrayList<Attribute> lstAttributes;
		ArrayList<String> lst1;
		Instances ist1;
		Logistic log1;
		double rgd1[][];
		
		mapPredictors = mergeData(mapUnmergedPredictors);

		try {
		
			//loading attributes list
			lstAttributes = new ArrayList<Attribute>(2);
			lstAttributes.add(new Attribute("beta-diversity"));
			lst1 = new ArrayList<String>(2);
			lst1.add("no");
			lst1.add("yes");
			lstAttributes.add(new Attribute("y1", lst1));
			
			//loading instances
			ist1 = new Instances("data", lstAttributes, mapResponses.size());
			lstPred = new ArrayList<Double>(mapPredictors.size());
			lstResp = new ArrayList<Double>(mapPredictors.size());
			lstSamples = new ArrayList<String>(mapPredictors.size());
			for(String s:this.mapPredictors.keySet()) {
				ist1.add(new DenseInstance(1., new double[] {mapPredictors.get(s), 1-mapResponses.get(s)}));
				lstPred.add(mapPredictors.get(s));
				lstResp.add(mapResponses.get(s));
				lstSamples.add(s);
			}
			ist1.setClassIndex(1);
			
			//running regression and finding auc
			log1 = new Logistic();
			log1.buildClassifier(ist1);
			lstOut = new ArrayList<String>(lstPred.size());
			lstOut.add("SAMPLE_ID,PREDICTOR,RESPONSE,PREDICTION");
			rgd1 = log1.distributionsForInstances(ist1);
			for(int i=0;i<ist1.size();i++) {
				lstOut.add(lstSamples.get(i) + "," + lstPred.get(i) + "," + lstResp.get(i) + "," + rgd1[i][0]);
			}
			return lstOut;
		}catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}

	}
	
	private double calculatePerformanceStatistic(HashMap<String,Double> mapResponses, String sDirection){
		
		//lstAttr = list of attributes
		//lst1 = reponse values
		//ist1 = instances object
		//log1 = logistic regression object
		//evl1 = evaluation object
		//trc1 = threshold curve
		//ist2 = instances for calculating auc
		
		ArrayList<Attribute> lstAttributes;
		ArrayList<String> lst1;
		Instances ist1;
		Instances ist2;
		Logistic log1;
		Evaluation evl1;
		ThresholdCurve trc1;
		
		try {
		
			//loading attributes list
			lstAttributes = new ArrayList<Attribute>(2);
			lstAttributes.add(new Attribute("beta-diversity"));
			lst1 = new ArrayList<String>(2);
			lst1.add("no");
			lst1.add("yes");
			lstAttributes.add(new Attribute("y1", lst1));
			
			//loading instances
			ist1 = new Instances("data", lstAttributes, mapResponses.size());
			for(String s:this.mapPredictors.keySet()) {
				ist1.add(new DenseInstance(1., new double[] {mapPredictors.get(s), 1-mapResponses.get(s)}));
			}
			ist1.setClassIndex(1);
			
			//running regression and finding auc
			log1 = new Logistic();
			evl1 = new Evaluation(ist1);
			evl1.crossValidateModel(log1, ist1, 10, rnd1);
		    trc1 = new ThresholdCurve();
		    ist2 = trc1.getCurve(evl1.predictions(), 0);
		    return ThresholdCurve.getROCArea(ist2);
		
		}catch(Exception e) {
			e.printStackTrace();
			return 0.;
		}
	}
	
	/**Number of null iterations**/
	public int nullIterations() {
		return lstRandomizedResponseMaps.size();
	}
	
	/**Data used for fitting model, merged by merging category**/
	public ArrayList<String> printMergedData(HashMap<String,Double> mapUnmergedPredictors){
		
		//map1 = map from sample names to merged predictors
		//lstOut = output
		
		ArrayList<String> lstOut;
		HashMap<String,Double> map1;
		
		map1 = mergeData(mapUnmergedPredictors);
		lstOut = new ArrayList<String>(map1.size()+1);
		lstOut.add("SAMPLE_1,SAMPLE_2,PREDICTOR,RESPONSE");
		for(String s:map1.keySet()) {
			lstOut.add(s + "," + map1.get(s) + "," + mapResponses.get(s));
		}
		return lstOut;
	}
	
	/**Raw data used for fitting model**/
	public ArrayList<String> printUnmergedData(HashMap<String,Double> mapUnmergedPredictors){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(mapUnmergedPredictors.size()+1);
		lstOut.add("SAMPLE_1,SAMPLE_2,PREDICTOR,RESPONSE");
		for(String s:mapUnmergedPredictors.keySet()) {
			lstOut.add(s + "," + mapUnmergedPredictors.get(s) + "," + this.mapUnmergedResponses.get(s));
		}
		return lstOut;
	}
	
	private HashMap<String,Double> thresholdResponse(HashMap<String,Double> mapResponse){
		
		//map1 = output
		
		HashMap<String,Double> map1;
		
		map1 = new HashMap<String,Double>(mapResponse.size());
		for(String s:mapResponse.keySet()) {
			if(mapResponse.get(s)>=this.dThreshold) {
				map1.put(s,1.);
			}else {
				map1.put(s,0.);
			}
		}
		return map1;
	}
	
	/**Coefficient estimates (both null and observed)**/
	public HashMap<String,Double> coefficients(HashMap<String,Double> mapUnmergedPredictors){
		return null;
	}
	
	private HashMap<String,Double> mergeData(HashMap<String,Double> mapData){
		
		//map1 = map from merged sample names to sum of merged values
		//map2 = map from merged sample names to count of merged values
		//mapOut = output
		
		HashMap_AdditiveDouble<String> map1;
		HashMap_AdditiveDouble<String> map2;
		HashMap<String,Double> mapOut;
		
		//merging response values
		map1 = new HashMap_AdditiveDouble<String>(mapMerge.size());
		map2 = new HashMap_AdditiveDouble<String>(mapMerge.size());
		for(String s:mapData.keySet()){
			map1.putSum(mapMerge.get(s),mapData.get(s));
			map2.putSum(mapMerge.get(s),1.);
		}
		mapOut = new HashMap<String,Double>(map1.size());
		for(String s:map1.keySet()){
			mapOut.put(s,map1.get(s)/map2.get(s));
		}
		return mapOut;
	}
}