package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import edu.ucsf.base.HashMap_AdditiveDouble;

public class FastQuantileRegression0{

	/**Null (randomized) response vectors**/
	private ArrayList<ResponseVector> lstRandomizedResponses;
	
	/**Response variable map from merged sample pairs to values**/
	private HashMap<String,Double> mapResponses;
	
	/**Map from sample pairs to merged sample ids**/
	private HashMap<String,String> mapMerge;
	
	/**Current predictor vector**/
	private PredictorVector pvc1;
	
	/**Current (non-randomized) response vector**/
	private ResponseVector rsp1;
	
	/**Window size**/
	private int iWindowSize;
	
	/**Percentile**/
	private double dPercentile;
	
	/**Indices for cross validation shuffling**/
	private ArrayList<Integer> lstIndices = null;
	
	public FastQuantileRegression0(HashMap<String,Double> mapUnmergedResponses, HashMap<String,String> mapMerge, int iWindowSize, double dPercentile, int iNullIterations){
		
		//lst1 = list of merged sample names (in randomized order)
		
		ArrayList<String> lst1;
		
		//saving values
		this.mapMerge = mapMerge;
		this.iWindowSize = iWindowSize;
		this.dPercentile = dPercentile;
		
		//merging response values
		this.mapResponses = mergeData(mapUnmergedResponses);
		
		//loading randomized response values
		lstRandomizedResponses = new ArrayList<ResponseVector>(iNullIterations);
		lst1 = new ArrayList<String>(mapResponses.keySet());
		for(int i=0;i<iNullIterations;i++){
			Collections.shuffle(lst1);
			lstRandomizedResponses.add(new ResponseVector(mapResponses, lst1, iWindowSize, dPercentile));
		}
		lstIndices = null;
	}
	
	public FastQuantileRegression0(HashMap<String,Double> mapUnmergedResponses, HashMap<String,String> mapMerge, int iWindowSize, double dPercentile){
		
		//saving values
		this.mapMerge = mapMerge;
		this.iWindowSize = iWindowSize;
		this.dPercentile = dPercentile;
		
		//merging response values
		this.mapResponses = mergeData(mapUnmergedResponses);
	}

	public double regressionCrossValidation(HashMap<String,Double> mapUnmergedPredictors, double dTestSplit){
		
		//iStart = current start index
		//iEnd = current end index
		//i1 = split size
		//rgd1 = current fit
		//rgd2 = current sums of squares
		//rgd3 = cumulative sums of squares
		//dN = total number of observations
		
		int iStart;
		int iEnd;
		int i1;
		double rgd1[];
		double rgd2[];
		double rgd3[];
		double dN;
		
		loadPredictor(mapUnmergedPredictors);
		if(lstIndices==null){
			lstIndices = new ArrayList<Integer>(pvc1.size());
			for(int i=0;i<pvc1.size();i++) {
				lstIndices.add(i);
			}
		}
		Collections.shuffle(lstIndices);
		i1 = (int) Math.floor(dTestSplit*lstIndices.size());
		iStart = 0;
		iEnd = i1-1;
		rgd3 = new double[3];
		do {
			
			//estimating regression coefficients
			rgd1 = regression(lstIndices, iStart, iEnd);
			
			//finding sums of squares
			rgd2 = crossValidationSS(rgd1, lstIndices, iStart, iEnd);
			
			//adding to cumulative sum of squares
			for(int i=0;i<3;i++){
				rgd3[i]+=rgd2[i];
			}
			
			//updating indices
			iStart = iEnd + 1;
			iEnd = iStart + i1 - 1;
		}while(iEnd<=lstIndices.size());

		//finding r^2
		dN = (double) lstIndices.size();
		return 1. - rgd3[0]/(rgd3[2]-rgd3[1]*rgd3[1]/dN);
	}
	
	public HashMap<String,Double> regression(HashMap<String,Double> mapUnmergedPredictors, String sStatistic){
		
		//map1 = output
		
		HashMap<String,Double> map1;
		
		loadPredictor(mapUnmergedPredictors);
		map1 = new HashMap<String,Double>(lstRandomizedResponses.size()+1);
		for(int i=0;i<lstRandomizedResponses.size();i++){
			map1.put("null_" + i,calculateStatistic(lstRandomizedResponses.get(i), sStatistic));
		}
		map1.put("observed",calculateStatistic(rsp1,sStatistic));
		return map1;
	}

	public double regressionSES(HashMap<String,Double> mapUnmergedPredictors, String sStatistic){
		
		//dObs = observed statistic value
		//dN = number of null statistics
		//dSx = sum of null statistics
		//dSx2 = sum of null statistics squared
		//d1 = current null value
		//dNullMean = null mean
		//dNullStDev = null standard deviation
		
		double dObs;
		double dN;
		double dSx;
		double dSx2;
		double d1;
		double dNullMean;
		double dNullStDev;
		
		loadPredictor(mapUnmergedPredictors);
		dObs = calculateStatistic(rsp1,sStatistic);
		dN = (double) lstRandomizedResponses.size();
		dSx = 0.;
		dSx2 = 0.;
		for(int i=0;i<lstRandomizedResponses.size();i++){
			d1 = calculateStatistic(lstRandomizedResponses.get(i), sStatistic);
			dSx += d1;
			dSx2 += (d1*d1);
		}
		dNullMean = dSx/dN;
		dNullStDev = Math.sqrt((dSx2-dSx*dSx/dN)/(dN-1.));
		return (dObs-dNullMean)/dNullStDev;
	}
	
	private double[] crossValidationSS(double[] rgdCoeffEstimates, ArrayList<Integer> lstIndices, int iStart, int iEnd) {
		
		//rgd1 = output; sum of squares due to error (0), sum of y values (1), sum of y values squared (2)
		//d1 = current predicted value
		//d2 = current observed value
		//i1 = current index
		
		double rgd1[];
		double d1;
		double d2;
		int i1;
		
		rgd1 = new double[3];
		for(int i=iStart;i<=iEnd;i++){
			i1 = lstIndices.get(i);
			d1 = rgdCoeffEstimates[0] + rgdCoeffEstimates[1]*pvc1.get(i1);
			d2 = rsp1.get(i1);
			rgd1[0]+= (d1-d2)*(d1-d2);
			rgd1[1]+=d2;
			rgd1[2]+=d2*d2;
		}
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
		pvc1 = new PredictorVector(map1,lstOrderedNames,iWindowSize);
		rsp1 = new ResponseVector(mapResponses,lstOrderedNames,iWindowSize,dPercentile);
		
	}
	
	private double[] regression(ArrayList<Integer> lstIndices, int iStart, int iEnd) {
		
		//dSxy = sum of x*y
		//dSx = sum of x
		//dSy = sum of y
		//dSx2 = sum of x^2
		//dN = number of observations
		//rgd1 = output: intercept (0) and slope (1) estimates
		//i1 = current index
		
		int i1;
		double rgd1[];
		double dSxy;
		double dSx;
		double dSx2;
		double dSy;
		double dN;
		
		dSxy = 0;
		dSx = 0;
		dSy = 0;
		dSx2 = 0;
		dN = (double) (lstIndices.size()-(iEnd - iStart + 1));
		for(int k=0;k<iStart;k++) {
			i1 = lstIndices.get(k);
			dSxy+=(rsp1.get(i1)*pvc1.get(i1));
			dSx+=pvc1.get(i1);
			dSx2+=(pvc1.get(i1)*pvc1.get(i1));
			dSy+=rsp1.get(i1);	
		}
		for(int k=iEnd+1;k<lstIndices.size();k++) {
			i1 = lstIndices.get(k);	
			dSxy+=(rsp1.get(i1)*pvc1.get(i1));
			dSx+=pvc1.get(i1);
			dSx2+=(pvc1.get(i1)*pvc1.get(i1));
			dSy+=rsp1.get(i1);	
		}
		rgd1 = new double[2];
		rgd1[1] = (dN*dSxy-dSx*dSy)/(dN*dSx2-dSx*dSx);
		rgd1[0] = dSy/dN - rgd1[1]*dSx/dN;
		return rgd1;
	}
	
	
	private double calculateStatistic(ResponseVector rspY, String sStatistic){
		
		//dSxy = sum of x*y
		
		double dSxy;
		
		dSxy = 0;
		for(int i=0;i<rspY.size();i++){
			dSxy+=(rspY.get(i)*pvc1.get(i));
		}
		if(sStatistic.equals("slope")){
			return (rspY.n()*dSxy-pvc1.sX()*rspY.sY())/(rspY.n()*pvc1.sX2()-pvc1.sX()*pvc1.sX());
		}else if(sStatistic.equals("pearson")){
			return (rspY.n()*dSxy-pvc1.sX()*rspY.sY())/(Math.sqrt(rspY.n()*pvc1.sX2()-pvc1.sX()*pvc1.sX())*Math.sqrt(rspY.n()*rspY.sY2()-rspY.sY()*rspY.sY()));
		}else {
			return Double.NaN;
		}
	}
	
	private class PredictorVector{
		
		/**List of predictor values (ordered)**/
		private ArrayList<Double> lstWindowedPredictors;
		
		/**Sum of values**/
		private double dSx;
		
		/**Sum of values squared**/
		private double dSx2;
	
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
			dSx = 0;
			dSx2 = 0;
			for(int i=0;i<map1.size();i++){
				d3 = map1.get(i)/d2;
				dSx += d3;
				dSx2 += (d3*d3);
				lstWindowedPredictors.add(d3);
			}
		}
		
		public double sX(){
			return dSx;
		}
		
		public double sX2() {
			return dSx2;
		}
		
		public double get(int iIndex){
			return lstWindowedPredictors.get(iIndex);
		}
		
		public int size() {
			return lstWindowedPredictors.size();
		}
	}
	
	
	private class ResponseVector{
		
		/**List of response values (ordered)**/
		private ArrayList<Double> lstWindowedResponses;
		
		/**Sum of values**/
		private double dSy;
		
		/**Sum of values squared**/
		private double dSy2;

		/**Number of observations**/
		public double dN;
		
		public ResponseVector(HashMap<String,Double> mapResponses, ArrayList<String> lstOrderedNames, int iWindowSize, double dPercentile) {
			
			//rgd1 = values in order
			//pct1 = percentile object
			//d1 = current percentile
			
			double rgd1[];
			Percentile pct1;
			double d1;
			
			rgd1 = new double[lstOrderedNames.size()];
			for(int i=0;i<lstOrderedNames.size();i++) {
				rgd1[i]=mapResponses.get(lstOrderedNames.get(i));
			}
			pct1 = new Percentile(dPercentile);
			lstWindowedResponses = new ArrayList<Double>(rgd1.length-iWindowSize+1);
			dSy = 0;
			dSy2 = 0;
			for(int i=0;i<=rgd1.length-iWindowSize;i++){
				d1 = pct1.evaluate(rgd1,i,iWindowSize);
				lstWindowedResponses.add(d1);
				dSy+=d1;
				dSy2+=(d1*d1);
			}
			dN = (double) lstWindowedResponses.size();
		}
		
		public double sY() {
			return dSy;
		}
		
		public double n() {
			return dN;
		}
		
		public double sY2() {
			return dSy2;
		}
		
		public int size() {
			return lstWindowedResponses.size();
		}
		
		public double get(int iIndex) {
			return lstWindowedResponses.get(iIndex);
		}
	}
}