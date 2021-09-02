package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.TreeMultimap;

import edu.ucsf.io.DataIO;

public class QuantileRegressionDataNew{
	
	/**Map from predictor names to formatted data**/
	private HashMap<String,FormattedData> mapFormattedData;
	
	public QuantileRegressionDataNew(
			HashBasedTable<Integer,String,Double> tblData,
			HashSet<String> setPredictors,
			String sResponse,
			int iWindowReshuffles,
			int iWindowSize,
			double dPercent){
		
		//iReshuffleableElements = number of elements to be reshuffled
		//mapXY = map from x values to y values with that value
		//mapXYUnique = map from x values to y values (assuming all unique x values)
		//lstWindows = list of windows
		//i1 = counter
		//iRows = number of rows of data
		//pct1 = percentile object
		//lstXSorted = sorted list of x values
		//rgdYSorted = sorted list of y values
		//dXSum = current sum of x values
		//setReshuffle = list of window indices that need to be recalculated on reshuffling
		//lst1 = current list of y values
		//mapReshuffle = map from x-values to start indices where reshuffling needs to occur
		//iReshufflesActual = actual number of reshuffles to perform
		
		int iReshufflesActual;
		int iReshufflableElements;
		HashMap<Double,Integer> mapReshuffle;
		List<Double> lst1;
		HashSet<Integer> setReshuffle;
		double dXSum;
		ArrayList<Double> lstXSorted;
		double[] rgdYSorted;
		int iRows;
		int i1;
		ListMultimap<Double,Double> mapXY;
		TreeMap<Double,Double> mapXYUnique;
		ArrayList<Window> lstWindows;
		Percentile pct1;
		
		//initializing variables
		mapFormattedData = new HashMap<String,FormattedData>(setPredictors.size());
		iRows = tblData.rowKeySet().size();
		pct1 = new Percentile(100.*dPercent);
		
		//looping through predictors and loading data for each
		for(String s:setPredictors) {
		
			//loading list of windows
			lstWindows = new ArrayList<Window>(iRows-iWindowSize);
			for(int i=0;i<iRows-iWindowSize+1;i++) {
				lstWindows.add(new Window(iWindowSize));
			}
			
			//**********************
			//System.out.println("");
			//double dTEMP = System.currentTimeMillis();
			//**********************

			if(this.duplicatedPredictorValues(s,tblData,iRows)==false) {
			
				//loading map from x values to y value for that x value
				mapXYUnique = new TreeMap<Double,Double>();
				for(int i=0;i<iRows;i++) {
					mapXYUnique.put(tblData.get(i,s),tblData.get(i,"response"));
				}
			
				//creating sorted lists
				lstXSorted = new ArrayList<Double>(iRows);
				rgdYSorted = new double[iRows];
				i1 = 0;
				for(double dX:mapXYUnique.keySet()) {		
					lstXSorted.add(dX);
					rgdYSorted[i1]=mapXYUnique.get(dX);
					i1++;
				}
				
				//putting values into windows
				dXSum = 0;
				for(int i=0;i<iWindowSize;i++) {
					dXSum+=lstXSorted.get(i);
				}
				lstWindows.get(0).putXSum(dXSum);
				lstWindows.get(0).putYPercentile(pct1.evaluate(rgdYSorted,0,iWindowSize));
				for(int i=1;i<lstWindows.size();i++){
					dXSum-=lstXSorted.get(i-1);
					dXSum+=lstXSorted.get(i+iWindowSize-1);
					lstWindows.get(i).putXSum(dXSum);
					lstWindows.get(i).putYPercentile(pct1.evaluate(rgdYSorted,i,iWindowSize));			
				}
				
			}else{
			
				//loading map from x values to y values for that x value
				mapXY = MultimapBuilder.treeKeys().arrayListValues().build();
				for(int i=0;i<iRows;i++) {
					mapXY.put(tblData.get(i,s),tblData.get(i,"response"));
				}
				
				//**********************
				//System.out.println("Time 1:" + (System.currentTimeMillis()-dTEMP));
				//dTEMP = System.currentTimeMillis();
				//**********************
				
				//creating sorted lists
				lstXSorted = new ArrayList<Double>(iRows);
				rgdYSorted = new double[iRows];
				i1 = 0;
				mapReshuffle = new HashMap<Double,Integer>(mapXY.size());
				iReshufflableElements = 0;
				for(double dX:mapXY.keySet()) {	
					lst1 = mapXY.get(dX);
					if(lst1.size()>1){
						Collections.shuffle(lst1);
						mapReshuffle.put(dX,i1);
						iReshufflableElements+=lst1.size();
					}
					for(Double d2:lst1) {
						lstXSorted.add(dX);
						rgdYSorted[i1]=d2;
						i1++;
					}
				}
				
				//**********************
				//System.out.println("Time 2:" + (System.currentTimeMillis()-dTEMP));
				//dTEMP = System.currentTimeMillis();
				//**********************
				
				//putting values into windows
				dXSum = 0;
				for(int i=0;i<iWindowSize;i++) {
					dXSum+=lstXSorted.get(i);
				}
				lstWindows.get(0).putXSum(dXSum);
				lstWindows.get(0).putYPercentile(pct1.evaluate(rgdYSorted,0,iWindowSize));
				for(int i=1;i<lstWindows.size();i++){
					dXSum-=lstXSorted.get(i-1);
					dXSum+=lstXSorted.get(i+iWindowSize-1);
					lstWindows.get(i).putXSum(dXSum);
					lstWindows.get(i).putYPercentile(pct1.evaluate(rgdYSorted,i,iWindowSize));			
				}
				
				//**********************
				//System.out.println("Time 3:" + (System.currentTimeMillis()-dTEMP));
				//dTEMP = System.currentTimeMillis();
				//**********************
				
				//saving additional y reshuffles
				setReshuffle = windowsThatNeedReshuffling(mapXY,iWindowSize,iRows);
				iReshufflesActual=iWindowReshuffles;
				if(iReshufflableElements<5) {
					if(iReshufflableElements==4) {
						iReshufflesActual=24;
					}else if(iReshufflableElements==3) {
						iReshufflesActual=6;
					}else if(iReshufflableElements==2) {
						iReshufflesActual=2;
					}else if(iReshufflableElements<=1) {
						iReshufflesActual=0;
					}
					iReshufflesActual = Math.min(iWindowReshuffles,iReshufflesActual);
				}else {
					iReshufflesActual = iWindowReshuffles;
				}
				
				//************************
				//System.out.println("Actual reshuffles: " + iReshufflesActual);
				//************************
				
				for(int k=1;k<iReshufflesActual;k++){
					for(Double d:mapReshuffle.keySet()){
						lst1 = mapXY.get(d);
						Collections.shuffle(lst1);
						i1 = mapReshuffle.get(d);
						for(Double d2:lst1) {
							rgdYSorted[i1]=d2;
							i1++;
						}
					}	
		
					//putting values into windows
					for(Integer i:setReshuffle){
						lstWindows.get(i).putYPercentile(pct1.evaluate(rgdYSorted,i,iWindowSize));
					}
				}
				
				//**********************
				//System.out.println("Time 4:" + (System.currentTimeMillis()-dTEMP));
				//**********************
			}
				
			//saving results
			mapFormattedData.put(s,new FormattedData(iRows));
			for(Window wnd1:lstWindows){
				mapFormattedData.get(s).add(wnd1.xMean(),wnd1.yPercentile());
			}
		}
	}

	private boolean duplicatedPredictorValues(String sPredictor, HashBasedTable<Integer,String,Double> tblData, int iRows) {
		
		//set1 = set of values
		//d1 = current value
		
		HashSet<Double> set1;
		double d1;
		
		set1 = new HashSet<Double>(iRows);
		for(int i=0;i<iRows;i++) {
			d1 = tblData.get(i,sPredictor);
			if(set1.contains(d1)){
				return true;
			}else {
				set1.add(d1);
			}
		}
		return false;
	}
	
	private HashSet<Integer> windowsThatNeedReshuffling(ListMultimap<Double,Double> mapXY, int iWindowSize, int iRows){
		
		//set1 = output
		//set2 = set of windows
		//i1 = counter
		//i2 = current duplicate size
		//iWindowStart = current window start
		//iWindowEnd = current window end
		//iIntervalStart = current interval start
		//iIntervalEnd = current interval end
		
		int iIntervalStart;
		int iIntervalEnd;
		int iWindowStart;
		int iWindowEnd;
		HashSet<Integer> set1;
		HashSet<Integer[]> set2;
		int i1;
		int i2;
		
		//loading intervals
		i1 = 0;
		set2 = new HashSet<Integer[]>(mapXY.size());
		for(double dX:mapXY.keySet()) {	
			i2 = mapXY.get(dX).size();
			if(i2>1){
				set2.add(new Integer[]{i1,i1+i2-1});
			}
			i1+=i2;
		}
		
		//looping through windows
		set1 = new HashSet<Integer>(iRows);
		for(int i=0;i<iRows-iWindowSize+1;i++) {
			iWindowStart = i;
			iWindowEnd = i+iWindowSize-1;
			
			//checking if each interval partially intersects
			for(Integer[] rgiInterval:set2){
				iIntervalStart = rgiInterval[0];
				iIntervalEnd = rgiInterval[1];
				if(iWindowEnd<iIntervalStart || iIntervalEnd<iWindowStart){
					continue;
				}
				if(iWindowStart<=iIntervalStart && iIntervalEnd<=iWindowEnd) {
					continue;
				}
				set1.add(iWindowStart);
				break;
			}
		}
		return set1;
	}
	
	public FormattedData getData(String sPredictor) {
		return mapFormattedData.get(sPredictor);
	}
	
	public class FormattedData{
		
		/**List of x data**/
		private ArrayList<Double> lstX;
		
		/**List of y data**/
		private ArrayList<Double> lstY;
		
		/**Sum of x values**/
		private double dSx;
		
		/**Sum of x values squared**/
		private double dSx2;
		
		/**Number of observations**/
		private double dN;
		
		public FormattedData(int iObservations){
			lstX = new ArrayList<Double>(iObservations);
			lstY = new ArrayList<Double>(iObservations);
			dSx = 0;
			dSx2 = 0;
			dN = 0;
		}
		
		public void add(double dX, double dY){
			lstX.add(dX);
			lstY.add(dY);
			dSx+=dX;
			dSx2+=(dX*dX);
			dN+=1;
		}
		
		public double x(int iIndex){
			return lstX.get(iIndex);
		}
	
		public double y(int iIndex){
			return lstY.get(iIndex);
		}
		
		public double sumX(){
			return dSx;
		}
		
		public double sumX2(){
			return dSx2;
		}
		
		public double n(){
			return dN;
		}
		
		public int size(){
			return lstX.size();
		}
	}

	public class Window{
		
		/**Mean x value**/
		private double dXMean;
	
		/**Y percentile estimate sum**/
		private double dYPercentileSum;
		
		/**Count of y reshufflings (for averaging)**/
		private double dNY;
		
		/**Window size**/
		private int iWindowSize;
		
		public Window(int iWindowSize){
			dXMean = 0;
			dYPercentileSum = 0;
			this.iWindowSize = iWindowSize;
			dNY=0;
		}

		public void putXSum(double dXSum){
			dXMean = dXSum/((double) iWindowSize);
		}
	
		public void putYPercentile(double dYPercentile){
			dYPercentileSum+=dYPercentile;
			dNY++;
		}
		
		public double xMean(){
			return dXMean;
		}
		
		public double yPercentile(){
			return dYPercentileSum/dNY;
		}
	}
	
	public class Window0{
		
		/**Mean x value**/
		private double dXMean;
	
		/**Y percentile (expected value in the case of duplicate x values)**/
		private double dYPercentileMean;
		
		/**List of raw y values**/
		private double rgdY[];
		
		/**Count of y reshufflings (for averaging)**/
		private double dNY;
		
		/**Count of x mean observations**/
		private double dNX;
		
		/**Current index in array of y values**/
		private int iYIndex;
		
		/**Window size**/
		private int iWindowSize;
		
		/**Percent for percentile**/
		private double dPercent;
		
		public Window0(int iWindowSize, double dPercent){
			dXMean = 0;
			dYPercentileMean = 0;
			rgdY = new double[iWindowSize];
			iYIndex = 0;
			dNX = 0;
			dNY = 0;
			this.iWindowSize = iWindowSize;
			this.dPercent = dPercent;
		}
		
		public void addX(double dX){
			dXMean+=dX;
			dNX+=1.;
		}
		
		public void addY(double dY){
			rgdY[iYIndex] = dY;
			iYIndex++;
			if(iYIndex==rgdY.length) {
				computePercentile();
			}
		}
		
		private void computePercentile(){
			
			//pct1 = percentile object
			
			Percentile pct1;
			
			pct1 = new Percentile();
			pct1.setData(rgdY);
			dYPercentileMean+=pct1.evaluate(dPercent);
			dNY+=1.;
			rgdY = new double[iWindowSize];
			iYIndex = 0;
		}
		
		public double xMean(){
			return dXMean/dNX;
		}
		
		public double yPercentile(){
			return dYPercentileMean/dNY;
		}
	}
}