package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import edu.ucsf.base.HashMap_AdditiveDouble;

public class FastQuantileRegressionSES{

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
	
	public FastQuantileRegressionSES(HashMap<String,Double> mapUnmergedResponses, HashMap<String,String> mapMerge, int iWindowSize, double dPercentile, int iNullIterations){
		
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