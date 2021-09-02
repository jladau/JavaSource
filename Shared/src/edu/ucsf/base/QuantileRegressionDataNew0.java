package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;

import edu.ucsf.io.DataIO;

public class QuantileRegressionDataNew0{
	
	/**Map from predictor names to formatted data**/
	private HashMap<String,FormattedData> mapFormattedData;
	
	public QuantileRegressionDataNew0(
			HashBasedTable<Integer,String,Double> tblData,
			HashSet<String> setPredictors,
			String sResponse,
			int iWindowReshuffles,
			int iWindowSize,
			double dPercent){
		
		//dX = current x value
		//dY = current y value
		//mapXY = map from x values to y values with that value
		//lstWindows = list of windows
		//mapWindows = map from raw data indices to windows including index (windows identified by index)
		//i1 = counter
		//iRows = number of rows of data
		
		int iRows;
		int i1;
		double dX;
		double dY;
		TreeMap<Double,ArrayList<Double>> mapXY;
		ArrayList<Window> lstWindows;
		HashMultimap<Integer,Integer> mapWindows;
		
		//initializing variables
		mapFormattedData = new HashMap<String,FormattedData>(setPredictors.size());
		iRows = tblData.rowKeySet().size();
		
		//loading window index map
		mapWindows = HashMultimap.create(iRows,iWindowSize);
		for(int i=0;i<iRows-iWindowSize+1;i++){
			for(int j=i;j<i+iWindowSize;j++){
				mapWindows.put(j,i);
			}
		}
		
		//loading list of windows
		lstWindows = new ArrayList<Window>(iRows-iWindowSize);
		for(int i=0;i<iRows-iWindowSize+1;i++) {
			lstWindows.add(new Window(iWindowSize,dPercent));
		}
		
		//looping through predictors and loading data for each
		for(String s:setPredictors) {
		
			//******************
			//System.out.println("Loading data for " + s + "...");
			//******************
			
			//**********************
			//double dTEMP = System.currentTimeMillis();
			//**********************
			
			//loading map from x values to indices for that x value
			mapXY = new TreeMap<Double,ArrayList<Double>>();
			for(int i=0;i<iRows;i++) {
				dX = tblData.get(i,s);
				dY = tblData.get(i,"response");
				if(!mapXY.containsKey(dX)) {
					mapXY.put(dX,new ArrayList<Double>(iRows));
				}
				mapXY.get(dX).add(dY);
			}
			
			//**********************
			//System.out.println("TIME 1: " + (System.currentTimeMillis()-dTEMP));
			//**********************
			
			//**********************
			//dTEMP = System.currentTimeMillis();
			//**********************
			
			//putting values into windows
			i1 = 0;
			for(Entry<Double,ArrayList<Double>> ery1:mapXY.entrySet()) {	
				if(ery1.getValue().size()>1) {
					Collections.shuffle(ery1.getValue());
				}
				for(Double d2:ery1.getValue()) {
					for(int iWindowIndex:mapWindows.get(i1)){
						lstWindows.get(iWindowIndex).addX(ery1.getKey());
						lstWindows.get(iWindowIndex).addY(d2);
					}
					i1++;
				}
			}
			
			//**********************
			//System.out.println("TIME 2: " + (System.currentTimeMillis()-dTEMP));
			//**********************
			
			//**********************
			//dTEMP = System.currentTimeMillis();
			//**********************
			
			
			//putting additional reshuffles into y
			for(int k=1;k<iWindowReshuffles;k++){
				i1 = 0;
				for(Entry<Double,ArrayList<Double>> ery1:mapXY.entrySet()) {	
					if(ery1.getValue().size()>1) {
						Collections.shuffle(ery1.getValue());
					}	
					
					//TODO this is taking too long here
					for(Double d2:ery1.getValue()) {
						for(int iWindowIndex:mapWindows.get(i1)){
							lstWindows.get(iWindowIndex).addY(d2);
						}
						i1++;
					}
				}	
			}
			
			//**********************
			//System.out.println("TIME 3: " + (System.currentTimeMillis()-dTEMP));
			//**********************
			
			//**********************
			//dTEMP = System.currentTimeMillis();
			//**********************
			
			
			//saving results
			mapFormattedData.put(s,new FormattedData(iRows));
			for(Window wnd1:lstWindows){
				mapFormattedData.get(s).add(wnd1.xMean(),wnd1.yPercentile());
			}
			
			//**********************
			//System.out.println("TIME 4: " + (System.currentTimeMillis()-dTEMP));
			//**********************
			
		}
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
		
		public Window(int iWindowSize, double dPercent){
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