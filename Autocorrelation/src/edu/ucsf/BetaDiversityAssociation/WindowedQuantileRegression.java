package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.HashMap_AdditiveDouble;

public class WindowedQuantileRegression implements Regression{

	//TODO define nieghborhood objects and output accordingly
	
	/**Response variable map from merged sample pairs to values**/
	private HashMap<String,Double> mapResponses;
	
	/**List of randomized responses**/
	private ArrayList<HashMap<String,Double>> lstRandomizedResponseMaps;
	
	/**Map from sample pairs to merged sample ids**/
	private HashMap<String,String> mapMerge;
	
	/**Null (randomized) response vectors**/
	private ArrayList<ResponseVector> lstRandomizedResponses;
	
	/**Current predictor vector**/
	private PredictorVector pvc1;
	
	/**Current (non-randomized) response vector**/
	private ResponseVector rsp1;
	
	/**Window size**/
	private int iWindowSize;
	
	/**Percentile object**/
	private Percentile pct1;
	
	private Percentile pct0;
	
	/**Unmerged responses**/
	private HashMap<String,Double> mapUnmergedResponses;
	
	public WindowedQuantileRegression(HashMap<String,Double> mapUnmergedResponses, HashMap<String,String> mapMerge, int iWindowSize, double dPercentile, boolean bRange, int iNullIterations){
		
		//lst1 = list of merged sample names
		//lst2 = list of merged sample values
		
		ArrayList<String> lst1;
		ArrayList<Double> lst2;

		//saving values
		this.mapMerge = mapMerge;
		this.iWindowSize = iWindowSize;
		pct1 = new Percentile(dPercentile);
		if(bRange==true) {
			pct0 = new Percentile(100.-dPercentile);
		}else {
			pct0 = null;
		}
		
		//merging response values
		this.mapResponses = mergeData(mapUnmergedResponses);
		
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
		
		//lstRandomizedResponses = new ArrayList<ResponseVector>(iNullIterations);
		//lst1 = new ArrayList<String>(mapResponses.keySet());
		//for(int i=0;i<iNullIterations;i++){
		//	Collections.shuffle(lst1);
		//	lstRandomizedResponses.add(new ResponseVector(mapResponses, lst1, iWindowSize, dPercentile));
		//}
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
	
	/**Returns transformed (e.g., quantile) data**/
	public ArrayList<String> printTransformedData(HashMap<String,Double> mapUnmergedPredictors) {
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		loadPredictor(mapUnmergedPredictors);
		lstOut = new ArrayList<String>(pvc1.size()+1);
		lstOut.add("PREDICTOR,RESPONSE");
		for(int k=0;k<pvc1.size();k++) {
			lstOut.add(pvc1.get(k) + "," + rsp1.get(k));
		}
		return lstOut;
	}
	
	/**Returns map from analysis type (null or observed) to performance measure**/
	public HashMap<String,Double> performance(HashMap<String,Double> mapUnmergedPredictors, String sDirection){
		
		//map1 = output
		//d1 = current value
		
		HashMap<String,Double> map1;
		double d1;
		
		loadPredictor(mapUnmergedPredictors);
		map1 = new HashMap<String,Double>(lstRandomizedResponses.size()+1);
		d1 = calculatePerformanceStatistic(rsp1, sDirection);
		if(!Double.isNaN(d1)){
			map1.put("observed",calculatePerformanceStatistic(rsp1, sDirection));
		}else {
			return map1;
		}
		for(int i=0;i<lstRandomizedResponses.size();i++){
			d1 = calculatePerformanceStatistic(lstRandomizedResponses.get(i), sDirection);
			if(!Double.isNaN(d1)){
				map1.put("null_" + i,d1);
			}
		}
		
		return map1;
	}
	
	/**Returns performance measure for observed analysis or specified null iteration**/
	public double performance(HashMap<String,Double> mapUnmergedPredictors, String sDirection, int iNullIteration){
		
		//d1 = current value
		
		double d1;
		
		loadPredictor(mapUnmergedPredictors);
		if(iNullIteration>=0) {	
			d1 = calculatePerformanceStatistic(lstRandomizedResponses.get(iNullIteration), sDirection);
		}else {	
			d1 = calculatePerformanceStatistic(rsp1, sDirection);
		}
		return d1;
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
	
	/**Coefficient estimates (both null and observed)**/
	public HashMap<String,Double> coefficients(HashMap<String,Double> mapUnmergedPredictors){
		
		//map1 = output
		
		HashMap<String,Double> map1;
		
		loadPredictor(mapUnmergedPredictors);
		map1 = new HashMap<String,Double>(lstRandomizedResponses.size()+1);
		for(int i=0;i<lstRandomizedResponses.size();i++){
			map1.put("null_" + i + ",slope",ExtendedMath.slope(pvc1.array(),lstRandomizedResponses.get(i).array()));
			map1.put("null_" + i + ",intercept",ExtendedMath.intercept(pvc1.array(),lstRandomizedResponses.get(i).array()));
		}
		map1.put("observed,slope",ExtendedMath.slope(pvc1.array(),rsp1.array()));
		map1.put("observed,intercept",ExtendedMath.intercept(pvc1.array(),rsp1.array()));
		return map1;
	}
	
	/**Returns predicted values**/
	public ArrayList<String> predictedValues(HashMap<String,Double> mapUnmergedPredictors){
		
		//lstOut = output
		//dSlope = slope
		//dIntercept = intercept
		//lstPredictors = predictors
		//lstResponses = responses
		//map1 = map from sample names to merged predictors
		
		HashMap<String,Double> map1;		
		ArrayList<String> lstOut;
		ArrayList<Double> lstPredictors;
		ArrayList<Double> lstResponses;
		double dSlope;
		double dIntercept;
		
		loadPredictor(mapUnmergedPredictors);
		lstPredictors = pvc1.array();
		lstResponses = rsp1.array();
		
		dSlope = ExtendedMath.slope(lstPredictors,lstResponses);
		dIntercept = ExtendedMath.intercept(lstPredictors,lstResponses);
		lstOut = new ArrayList<String>(lstPredictors.size()+1);
		
		lstOut.add("THRESHOLD,SAMPLE_ID,PREDICTOR,RESPONSE,PREDICTION");
		map1 = mergeData(mapUnmergedPredictors);
		
		for(String s:map1.keySet()) {
			lstOut.add(s + "," + map1.get(s) + "," + mapResponses.get(s) + "," + (dIntercept + dSlope*map1.get(s)));
		}
		return lstOut;
	}
	
	private double calculatePerformanceStatistic(ResponseVector rspY, String sDirection){
		
		//rgd1 = current fit
		//rgd2 = current sums of squares
		//rgd3 = cumulative sums of squares
		//dN = total number of observations
		//lstObs = list of left out observations
		//loo1 = regression variable totals
		//bPositive = flag for whether positive
		//bNegative = flag for whether negative
		
		double rgd1[];
		double rgd2[];
		double rgd3[];
		double dN;
		ArrayList<LeftOutObservation> lstObs;
		LeftOutObservation loo1;
		boolean bPositive;
		boolean bNegative;
		
		lstObs = new ArrayList<LeftOutObservation>(pvc1.size());
		lstObs.add(new LeftOutObservation(rspY, 0));
		for(int i=1;i<pvc1.size();i++){
			lstObs.add(new LeftOutObservation(rspY, lstObs.get(i-1),i));
		}
		loo1 = new LeftOutObservation(rspY, -1, 0, pvc1.size()-1);
		rgd3 = new double[3];
		if(sDirection.equals("positive")){
			bPositive = true;
		}else {
			bPositive = false;
		}
		if(sDirection.equals("negative")){
			bNegative = true;
		}else {
			bNegative = false;
		}
		
		for(int k=0;k<pvc1.size();k++) {
			
			//estimating regression coefficients
			rgd1 = coefficientEstimates(k, loo1, lstObs);
			
			//adjusting for direction
			if(bPositive && rgd1[1]<0){
				return 0;
			}
			if(bNegative && rgd1[1]>0){
				return 0;
			}
			
			//finding sums of squares
			rgd2 = crossValidationSS(rgd1, k, rspY);
			
			//adding to cumulative sum of squares
			for(int i=0;i<3;i++){
				rgd3[i]+=rgd2[i];
			}
		}

		//finding r^2
		dN = (double) pvc1.size();
		return 1. - rgd3[0]/(rgd3[2]-rgd3[1]*rgd3[1]/dN);
	}
	
	private double[] crossValidationSS(double[] rgdCoeffEstimates, int iLeftOutIndex, ResponseVector rspY) {
		
		//rgd1 = output; sum of squares due to error (0), sum of y values (1), sum of y values squared (2)
		//d1 = current predicted value
		//d2 = current observed value
		
		double rgd1[];
		double d1;
		double d2;
		
		rgd1 = new double[3];
		d1 = rgdCoeffEstimates[0] + rgdCoeffEstimates[1]*pvc1.get(iLeftOutIndex);
		d2 = rspY.get(iLeftOutIndex);
		rgd1[0]+= (d1-d2)*(d1-d2);
		rgd1[1]+=d2;
		rgd1[2]+=d2*d2;
		return rgd1;
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
	
	private void loadPredictor(HashMap<String,Double> mapUnmergedPredictors) {
		
		//map1 = map from sample names to merged predictors
		//lstOrderedNames = sample names ordered by increasing predictor value
		//mapSort = map from predictor values to sample names (for sorting)
		//lst1 = current set of sample names
		
		ListMultimap<Double,String> mapSort;
		HashMap<String,Double> map1;
		ArrayList<String> lstOrderedNames;
		
		map1 = mergeData(mapUnmergedPredictors);
		mapSort = MultimapBuilder.treeKeys().arrayListValues().build();
		for(String s:map1.keySet()) {
			mapSort.put(map1.get(s),s);
		}
		lstOrderedNames = new ArrayList<String>(map1.size());
		for(Double dX:mapSort.keySet()) {
			lstOrderedNames.addAll(mapSort.get(dX));	
		}
		pvc1 = new PredictorVector(map1, lstOrderedNames, iWindowSize);
		rsp1 = new ResponseVector(mapResponses, lstOrderedNames, iWindowSize);
		lstRandomizedResponses = new ArrayList<ResponseVector>(lstRandomizedResponseMaps.size());
		for(int i=0;i<lstRandomizedResponseMaps.size();i++) {
			lstRandomizedResponses.add(new ResponseVector(lstRandomizedResponseMaps.get(i), lstOrderedNames, iWindowSize));
		}
	}
	
	private double[] coefficientEstimates(int iLeftOutIndex, LeftOutObservation loo1, ArrayList<LeftOutObservation> lst1) {
		
		//dSxy = sum of x*y
		//dSx = sum of x
		//dSy = sum of y
		//dSx2 = sum of x^2
		//dN = number of observations
		//rgd1 = output: intercept (0) and slope (1) estimates
		
		double rgd1[];
		double dSxy;
		double dSx;
		double dSx2;
		double dSy;
		double dN;
		
		dSxy = loo1.sXY()-lst1.get(iLeftOutIndex).sXY();
		dSx = loo1.sX()-lst1.get(iLeftOutIndex).sX();
		dSy = loo1.sY()-lst1.get(iLeftOutIndex).sY();
		dSx2 = loo1.sX2()-lst1.get(iLeftOutIndex).sX2();
		dN = (double) (loo1.size()-lst1.get(iLeftOutIndex).size());
		rgd1 = new double[2];
		rgd1[1] = (dN*dSxy-dSx*dSy)/(dN*dSx2-dSx*dSx);
		rgd1[0] = dSy/dN - rgd1[1]*dSx/dN;
		return rgd1;
	}
	
	private class LeftOutObservation{
		
		/**Sum of x in intersecting windows**/
		private double dSx=0;
		
		/**Sum of x^2 in intersecting windows**/
		private double dSx2=0;
		
		/**Sum of y in intersecting windows**/
		private double dSy=0;
		
		/**Sum of y in intersecting windows**/
		private double dSy2=0;
		
		/**Sum of x*y in intersecting windows**/
		private double dSxy=0;
		
		/**Start index of intersecting windows**/
		private int iStartIndex;
		
		/**End index of intersecting windows**/
		private int iEndIndex;
		
		public LeftOutObservation(ResponseVector rspY, int iIndex, int iStartIndex, int iEndIndex){
			
			this.iStartIndex = iStartIndex;
			this.iEndIndex = iEndIndex;
			
			for(int i=iStartIndex;i<=iEndIndex;i++) {
				this.putSum("sX",pvc1.get(i));
				this.putSum("sX2",pvc1.get(i)*pvc1.get(i));
				this.putSum("sY",rspY.get(i));
				this.putSum("sY2",rspY.get(i)*rspY.get(i));
				this.putSum("sXY",pvc1.get(i)*rspY.get(i));			
			}
		}
		
		public LeftOutObservation(ResponseVector rspY, int iIndex){
			initialize(iIndex);
		
			for(int i=iStartIndex;i<=iEndIndex;i++) {
				this.putSum("sX",pvc1.get(i));
				this.putSum("sX2",pvc1.get(i)*pvc1.get(i));
				this.putSum("sY",rspY.get(i));
				this.putSum("sY2",rspY.get(i)*rspY.get(i));
				this.putSum("sXY",pvc1.get(i)*rspY.get(i));			
			}
		}
		
		public LeftOutObservation(ResponseVector rspY, LeftOutObservation looPrevious, int iIndex) {
			
			//i1 = current index
			
			int i1;
			
			initialize(iIndex);
			
			put("sX",looPrevious.sX());
			put("sX2",looPrevious.sX2());
			put("sY",looPrevious.sY());
			put("sY2",looPrevious.sY2());
			put("sXY",looPrevious.sXY());
			
			if(looPrevious.startIndex()<this.startIndex()) {
				i1 = looPrevious.startIndex();
				this.putDifference("sX",pvc1.get(i1));
				this.putDifference("sX2",pvc1.get(i1)*pvc1.get(i1));
				this.putDifference("sY",rspY.get(i1));
				this.putDifference("sY2",rspY.get(i1)*rspY.get(i1));
				this.putDifference("sXY",pvc1.get(i1)*rspY.get(i1));
			}
			if(looPrevious.endIndex()<this.endIndex()) {
				i1 = this.endIndex();
				this.putSum("sX",pvc1.get(i1));
				this.putSum("sX2",pvc1.get(i1)*pvc1.get(i1));
				this.putSum("sY",rspY.get(i1));
				this.putSum("sY2",rspY.get(i1)*rspY.get(i1));
				this.putSum("sXY",pvc1.get(i1)*rspY.get(i1));
			}
		}
		
		public int startIndex() {
			return iStartIndex;
		}
		
		public int endIndex() {
			return iEndIndex;
		}
		
		public int size() {
			return iEndIndex-iStartIndex+1;
		}
		
		private void initialize(int iIndex) {
			
			this.iStartIndex = Math.max(0, iIndex - iWindowSize +1);
			this.iEndIndex = Math.min(iIndex + iWindowSize -1, pvc1.size()-1);
		}
		
		public void putDifference(String sVariable, double dValue) {
			if(sVariable.equals("sX")) {
				dSx -= dValue;
			}else if(sVariable.equals("sY")) {
				dSy -= dValue;
			}else if(sVariable.equals("sX2")) {
				dSx2 -= dValue;
			}else if(sVariable.equals("sY2")) {
				dSy2 -= dValue;
			}else if(sVariable.equals("sXY")) {
				dSxy -= dValue;
			}
		}
		
		public void putSum(String sVariable, double dValue) {
			if(sVariable.equals("sX")) {
				dSx += dValue;
			}else if(sVariable.equals("sY")) {
				dSy += dValue;
			}else if(sVariable.equals("sX2")) {
				dSx2 += dValue;
			}else if(sVariable.equals("sY2")) {
				dSy2 += dValue;
			}else if(sVariable.equals("sXY")) {
				dSxy += dValue;
			}
		}
		
		public void put(String sVariable, double dValue){
			if(sVariable.equals("sX")) {
				dSx = dValue;
			}else if(sVariable.equals("sY")) {
				dSy = dValue;
			}else if(sVariable.equals("sX2")) {
				dSx2 = dValue;
			}else if(sVariable.equals("sY2")) {
				dSy2 = dValue;
			}else if(sVariable.equals("sXY")) {
				dSxy = dValue;
			} 
		}
		
		public double sX() {
			return dSx;
		}
		
		public double sY() {
			return dSy;
		}
		
		public double sX2() {
			return dSx2;
		}
		
		public double sY2() {
			return dSy2;
		}
		
		public double sXY() {
			return dSxy;
		}	
	}
	
	private class PredictorVector{
		
		/**List of predictor values (ordered)**/
		private ArrayList<Double> lstWindowedPredictors;
	
		public PredictorVector(HashMap<String,Double> mapPredictors,  ArrayList<String> lstOrderedNames, int iWindowSize) {
			
			//map1 = map from window ids to sums of values
			//iStart = current starting index
			//iEnd = current ending index
			//d1 = current value
			//d2 = total number of observations in window
			//d3 = current value
			
			HashMap_AdditiveDouble<Integer> map1;
			int iStart;
			int iEnd;
			double d1;
			double d2;
			double d3;
			
			//loading sums for each window
			map1 = new HashMap_AdditiveDouble<Integer>(lstOrderedNames.size()-iWindowSize+1);
			for(int i=0;i<lstOrderedNames.size();i++) {
				d1 = mapPredictors.get(lstOrderedNames.get(i));
				iStart = Math.max(0,i-iWindowSize+1);
				iEnd = Math.min(i,lstOrderedNames.size()-iWindowSize);
				for(int j=iStart;j<=iEnd;j++){
					map1.putSum(j,d1);
				}
			}
			
			//outputting results
			d2 = (double) iWindowSize;
			lstWindowedPredictors = new ArrayList<Double>(map1.size());
			for(int i=0;i<map1.size();i++){
				d3 = map1.get(i)/d2;
				lstWindowedPredictors.add(d3);
			}
		}
		
		public double get(int iIndex){
			return lstWindowedPredictors.get(iIndex);
		}
		
		public int size() {
			return lstWindowedPredictors.size();
		}
		
		public ArrayList<Double> array(){
			return lstWindowedPredictors;
		}
	}
		
	private class ResponseVector{
		
		/**List of response values (ordered)**/
		private ArrayList<Double> lstWindowedResponses;

		public ResponseVector(HashMap<String,Double> mapResponses, ArrayList<String> lstOrderedNames, int iWindowSize) {
			
			//rgd1 = values in order
			//d1 = current percentile
			//d2 = current lower percentile (if applicable)
			
			double rgd1[];
			double d1;
			double d2;
			
			rgd1 = new double[lstOrderedNames.size()];
			for(int i=0;i<lstOrderedNames.size();i++) {
				rgd1[i]=mapResponses.get(lstOrderedNames.get(i));
			}
			lstWindowedResponses = new ArrayList<Double>(rgd1.length);
			for(int i=0;i<=rgd1.length-iWindowSize;i++){
				d1 = pct1.evaluate(rgd1,i,iWindowSize);
				if(pct0!=null) {
					d2 = pct0.evaluate(rgd1,i,iWindowSize);
				}else {
					d2 = 0;
				}
				lstWindowedResponses.add(Math.abs(d1-d2));
			}
		}
		
		public double get(int iIndex) {
			return lstWindowedResponses.get(iIndex);
		}
		
		public ArrayList<Double> array(){
			return lstWindowedResponses;
		}
	}
}