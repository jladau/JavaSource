package edu.ucsf.MultipleQuantileRegression0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class YData{

	/**List of data**/
	private ArrayList<YDatum> lstData;
	
	/**Number of columns**/
	private int iCols;
	
	public YData(int iRows, int iCols){
		lstData = new ArrayList<YDatum>(iRows);
		this.iCols = iCols;
	}
	
	public void addAll(String sResponseName, ArrayList<Double> lstY){	
		for(int i=0;i<lstY.size();i++){
			if(lstData.size()<i+1){
				lstData.add(new YDatum(iCols));
			}
			lstData.get(i).put(sResponseName,lstY.get(i));
		}
	}
	
	public void add(int iRow, String sResponseName, double dValue) {
		if(lstData.size()<iRow+1){
			lstData.add(new YDatum(iCols));
		}
		lstData.get(iRow).put(sResponseName,dValue);
	}
	
	public int rows() {
		return lstData.size();
	}
	
	public int columns(){
		return lstData.get(0).size();
	}
	
	public void add(YDatum ydm1){
		lstData.add(ydm1);
	}
	
	public YDatum get(int iRow){
		return lstData.get(iRow);
	}
	
	public double get(int iRow, String sKey) {
		return lstData.get(iRow).get(sKey);
	}
	
	public void shuffle(Random rnd1){
		Collections.shuffle(lstData,rnd1);
	}
	
	public HashSet<String> names(){
		return lstData.get(0).names();
	}
	
	public class YDatum{
	
		/**Map from y variable names to values**/
		private HashMap<String,Double> mapData;
		
		public YDatum(int iCols){
			mapData = new HashMap<String,Double>(iCols);
		}
		
		public void put(String sKey, double dValue){
			mapData.put(sKey,dValue);
		}
		
		public double get(String sKey){
			return mapData.get(sKey);
		}
		
		public int size(){
			return mapData.size();
		}
		
		public HashSet<String> names(){
			return new HashSet<String>(mapData.keySet());
		}
	}
}