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

import edu.ucsf.io.DataIO;

public class QuantileRegressionData1{
	
	/**Table from start points, predictor names to windows**/
	private HashBasedTable<Integer,String,Window> tblWindows;
	
	/**Windowed x map**/
	private HashMap<String,ArrayList<Double>> mapWindowedX;
	
	/**Response map**/
	private HashMap<String,ArrayList<Double>> mapResponses;
	
	/**Window size**/
	private int iWindowSize;

	/**Number of rows of data**/
	private int iRows;
	
	/**Map from predictor names to sum of x values**/
	private HashMap<String,Double> mapSx;
	
	/**Map from predictor names to sum of x values squared**/
	private HashMap<String,Double> mapSx2;
	
	/**Number of observations**/
	private double dN;
	
	/**Number of windows**/
	private int iWindows;
	
	/**Responses**/
	private HashSet<String> setResponses;
	
	/**Predictors**/
	private HashSet<String> setPredictors;
	
	/**Active response**/
	private String sActiveResponse;
	
	/**Active predictor**/
	private String sActivePredictor;
	
	/**Map from predictor names to treemap from predictor values to indices (indices in random order for identical predictor values)**/
	private HashMap<String,TreeMap<Double,ArrayList<Integer>>> mapX;
	
	/**Map from predictor names to lists of sorted indices**/
	private HashMap<String,ArrayList<Integer>> mapIndicesSorted;
	
	/**Current permutation**/
	private ArrayList<Integer> lstPermutation;
	
	public QuantileRegressionData1(
			HashBasedTable<Integer,String,Double> tblData,
			HashSet<String> setPredictors,
			HashSet<String> setResponses,
			int iWindowSize){
		
		//dSx = current sum of x values
		//dSx2 = current sume of x values squared
		//d1 = current x value
		//lstXSorted = x values, sorted
		//lstIndicesSorted = indices, sorted
		//rnd1 = random number generator
		
		ArrayList<Double> lstXSorted;
		double dSx;
		double dSx2;
		double d1;
		Random rnd1;
		
		this.setResponses = setResponses;
		this.setPredictors = setPredictors;
		this.iWindowSize = iWindowSize;
		this.iRows = tblData.rowKeySet().size();
		rnd1 = new Random();
		tblWindows = HashBasedTable.create(iRows-iWindowSize+1,setPredictors.size());
		mapWindowedX = new HashMap<String,ArrayList<Double>>(setPredictors.size());
		mapSx = new HashMap<String,Double>(setPredictors.size());
		mapSx2 = new HashMap<String,Double>(setPredictors.size());
		mapResponses = new HashMap<String,ArrayList<Double>>(setResponses.size());
		for(String s:setResponses){
			mapResponses.put(s,new ArrayList<Double>(iRows));
			for(int i=0;i<iRows;i++){
				mapResponses.get(s).add(tblData.get(i,s));
			}
		}
		
		mapIndicesSorted = new HashMap<String,ArrayList<Integer>>(setPredictors.size());
		mapX = new HashMap<String,TreeMap<Double,ArrayList<Integer>>>(setPredictors.size());
		for(String s:setPredictors){
			dSx = 0;
			dSx2 = 0;
			
			//sorting x value and corresponding indices
			mapX.put(s,new TreeMap<Double,ArrayList<Integer>>());
			for(int i=0;i<iRows;i++) {
				d1 = tblData.get(i,s);
				if(!mapX.get(s).containsKey(d1)) {
					mapX.get(s).put(d1,new ArrayList<Integer>(iRows));
				}
				mapX.get(s).get(d1).add(i);
			}
			lstXSorted = new ArrayList<Double>(iRows);
			
			mapIndicesSorted.put(s,new ArrayList<Integer>(iRows));
			for(Entry<Double,ArrayList<Integer>> ery1:mapX.get(s).entrySet()) {	
				if(ery1.getValue().size()>1) {
					Collections.shuffle(ery1.getValue());
				}
				for(Integer i:ery1.getValue()){
					lstXSorted.add(ery1.getKey());
					mapIndicesSorted.get(s).add(i);
				}
			}
			mapWindowedX.put(s,new ArrayList<Double>(iRows-iWindowSize+1));
			for(int i=0;i<lstXSorted.size()-iWindowSize+1;i++){
				tblWindows.put(i,s,new Window(lstXSorted, i,iWindowSize));
				dSx+=tblWindows.get(i,s).xMean();
				dSx2+=(tblWindows.get(i,s).xMean()*tblWindows.get(i,s).xMean());
				mapWindowedX.get(s).add(tblWindows.get(i,s).xMean());
			}
			mapSx.put(s,dSx);
			mapSx2.put(s,dSx2);
		}
		iWindows = tblWindows.rowKeySet().size();
		dN = (double) iWindows;
	}
	
	public void reshuffleWithinXCategories(){
		
		//****************************
		//int iTEMP=0;
		//****************************
		
		mapIndicesSorted.put(sActivePredictor,new ArrayList<Integer>(iRows));
		for(Entry<Double,ArrayList<Integer>> ery1:mapX.get(sActivePredictor).entrySet()) {	
			if(ery1.getValue().size()>1) {
				Collections.shuffle(ery1.getValue());				
				
				//*******************************
				//System.out.println(ery1.getValue().size());
				//*******************************
			}
			for(Integer i:ery1.getValue()){
				mapIndicesSorted.get(sActivePredictor).add(i);
			}
		}
		
		//*****************************
		//System.out.println(iTEMP);
		//*****************************
		
	}

	public void loadYWindows(){
		
		//lstY = current list of Y values
		
		ArrayList<Double> lstY;
		
		lstY = mapResponses.get(sActiveResponse);
		for(int i=0;i<iWindows;i++){
			tblWindows.get(i,sActivePredictor).loadYWindow(lstY,lstPermutation);
		}
	}
	
	public void loadYWindows(ArrayList<Integer> lstPermutation, String sPredictor, String sResponse){
		
		//lstY = current list of Y values
		
		ArrayList<Double> lstY;
		
		sActivePredictor=sPredictor;
		sActiveResponse=sResponse;
		this.lstPermutation = lstPermutation;
		lstY = mapResponses.get(sResponse);
		for(int i=0;i<iWindows;i++){
			tblWindows.get(i,sPredictor).loadYWindow(lstY,lstPermutation);
		}

	}
	
	public HashSet<String> responses(){
		return setResponses;
	}
	
	public HashSet<String> predictors(){
		return setPredictors;
	}
	
	
	public ArrayList<Double> windowedY(double dPercent){
	
		//lst1 = output
		
		ArrayList<Double> lst1;
		
		lst1 = new ArrayList<Double>(iWindows);
		for(int i=0;i<iWindows;i++){
			lst1.add(tblWindows.get(i,sActivePredictor).yQuantile(dPercent));
		}
		return lst1;
	}
	
	public ArrayList<Double> windowedX(){
		return mapWindowedX.get(sActivePredictor);
	}
	
	public double sumX(){
		return mapSx.get(sActivePredictor);
	}
	
	public double sumX2(){
		return mapSx2.get(sActivePredictor);
	}
	
	public double n(){
		return dN;
	}
	
	public class Window{
		
		/**Mean x value**/
		private double dXMean;
	
		/**List of y values**/
		private ArrayList<Double> lstYWindow;
		
		/**List of y values**/
		private double[] rgdYWindow;
		
		/**Window size**/
		private double dWindowSize;
		
		/**Window start**/
		private int iIndexStart;
		
		/**Window end**/
		private int iIndexEnd;
		
		/**Percentile object**/
		private Percentile pct1;
		
		public Window(ArrayList<Double> lstXSorted, int iWindowStart, int iWindowSize){	
			dXMean = 0;
			dWindowSize = (double) iWindowSize;
			iIndexStart = iWindowStart;
			iIndexEnd = iWindowStart+iWindowSize-1;
			for(int i=iIndexStart;i<=iIndexEnd;i++){
				dXMean+=lstXSorted.get(i);
			}
			dXMean=dXMean/((double) iWindowSize);
		}
		
		public double xMean(){
			return dXMean;
		}
		
		public void loadYWindow(ArrayList<Double> lstY, ArrayList<Integer> lstPermutation){
					
			//iIndex = current index
			//i1 = counter
			
			int iIndex;
			int i1;
			
			rgdYWindow = new double[iWindowSize];
			i1 = 0;
			for(int i=iIndexStart;i<=iIndexEnd;i++){
				iIndex = mapIndicesSorted.get(sActivePredictor).get(i);
				rgdYWindow[i1]=lstY.get(lstPermutation.get(iIndex));
				i1++;
			}
			pct1 = new Percentile();
			pct1.setData(rgdYWindow);
		}

		public double yQuantile(double dPercent){
			return pct1.evaluate(100*dPercent);
		}	
	}
}