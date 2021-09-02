package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.DataIO;

public class QuantileRegressionData{
	
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
	
	public QuantileRegressionData(
			HashBasedTable<Integer,String,Double> tblData,
			HashSet<String> setPredictors,
			HashSet<String> setResponses,
			int iWindowSize){
		
		//dSx = current sum of x values
		//dSx2 = current sume of x values squared
		//mapX = map from current predictor values to indices (indices in random order for identical predictor values)
		//d1 = current x value
		//lstXSorted = x values, sorted
		//lstIndicesSorted = indices, sorted
		//rnd1 = random number generator
		
		
		ArrayList<Double> lstXSorted;
		ArrayList<Integer> lstIndicesSorted;
		double dSx;
		double dSx2;
		TreeMap<Double,ArrayList<Integer>> mapX;
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
		for(String s:setPredictors){
			dSx = 0;
			dSx2 = 0;
			
			//sorting x value and corresponding indices
			mapX = new TreeMap<Double,ArrayList<Integer>>();
			for(int i=0;i<iRows;i++) {
				d1 = tblData.get(i,s);
				if(!mapX.containsKey(d1)) {
					mapX.put(d1,new ArrayList<Integer>(iRows));
				}
				mapX.get(d1).add(i);
			}
			lstXSorted = new ArrayList<Double>(iRows);
			lstIndicesSorted = new ArrayList<Integer>(iRows);
			for(Entry<Double,ArrayList<Integer>> ery1:mapX.entrySet()) {	
				if(ery1.getValue().size()>1) {
					Collections.shuffle(ery1.getValue());
				}
				for(Integer i:ery1.getValue()){
					lstXSorted.add(ery1.getKey());
					lstIndicesSorted.add(i);
				}
			}
			mapWindowedX.put(s,new ArrayList<Double>(iRows-iWindowSize+1));
			for(int i=0;i<lstXSorted.size()-iWindowSize+1;i++){
				tblWindows.put(i,s,new Window(lstXSorted, lstIndicesSorted,i,iWindowSize));
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
	
	public void loadYWindows(ArrayList<Integer> lstPermutation, String sPredictor, String sResponse){
		
		//lstY = current list of Y values
		
		ArrayList<Double> lstY;
		
		lstY = mapResponses.get(sResponse);
		for(int i=0;i<iWindows;i++){
			tblWindows.get(i,sPredictor).loadYWindow(lstY,lstPermutation);
		}
		sActivePredictor=sPredictor;
		sActiveResponse=sResponse;
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
	
		/**List of indices**/
		private ArrayList<Integer> lstIndices;
	
		/**List of y values**/
		private ArrayList<Double> lstYWindow;
		
		/**Window size**/
		private double dWindowSize;
		
		public Window(ArrayList<Double> lstXSorted, ArrayList<Integer> lstIndicesSorted, int iWindowStart, int iWindowSize){	
			dXMean = 0;
			lstIndices = new ArrayList<Integer>(iWindowSize);
			dWindowSize = (double) iWindowSize;
			for(int i=iWindowStart;i<iWindowStart+iWindowSize;i++){
				dXMean+=lstXSorted.get(i);
				lstIndices.add(lstIndicesSorted.get(i));
			}
			dXMean=dXMean/((double) lstIndices.size());
		}
		
		public double xMean(){
			return dXMean;
		}	
		
		public void loadYWindow(ArrayList<Double> lstY, ArrayList<Integer> lstPermutation){
			
			lstYWindow = new ArrayList<Double>(iWindowSize);
			for(int i:lstIndices){
				lstYWindow.add(lstY.get(lstPermutation.get(i)));
			}
			Collections.sort(lstYWindow);
		}
		
		public double yQuantile(double dPercent){
			return lstYWindow.get((int) Math.floor(dWindowSize*dPercent));
		}	
	}
}