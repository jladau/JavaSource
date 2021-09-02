package edu.ucsf.ConstrainedRegression;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.DataIO;



public class ConstrainedLinearModel{


	/**Map from predictor names to minimum constraints**/
	private HashMap<String,Double> mapMinimum;
	
	/**Map from predictor names to maximum constraints**/
	private HashMap<String,Double> mapMaximum;
	
	/**Map from row numbers to response variables**/
	private HashMap<Integer,Double> mapResponse;
	
	/**Table from row numbers, predictor names to predictor values**/
	private HashBasedTable<Integer,String,Double> tblPredictors;
	
	/**Map from predictor names to current estimates**/
	private HashMap<String,Double> mapEstimates;
	
	/**Predictors in fixed order**/
	private ArrayList<String> lstPredictors;
	
	/**Map from predictor name to sum squared values**/
	private HashMap<String,Double> mapX2;
	
	public ConstrainedLinearModel(
			HashMap<Integer,Double> mapResponse, 
			HashBasedTable<Integer,String,Double> tblPredictors, 
			HashMap<String,Double> mapMinimum, 
			HashMap<String,Double> mapMaximum) {
		
		//d1 = current sum of squares
		
		double d1;
		
		this.mapMinimum=mapMinimum;
		this.mapMaximum=mapMaximum;
		this.tblPredictors=tblPredictors;
		this.mapResponse=mapResponse;
		lstPredictors = new ArrayList<String>(mapMinimum.keySet());
		mapX2 = new HashMap<String,Double>(lstPredictors.size());
		for(String s:lstPredictors){
			d1 = 0;
			for(Integer i:tblPredictors.rowKeySet()){
				d1+=tblPredictors.get(i,s)*tblPredictors.get(i,s);
			}
			mapX2.put(s,d1);
		}
		
		
	}
	
	public void fitModel(double dTolerance){
		
		//d0 = previous SSE
		//d1 = current SSE
		
		double d0;
		double d1;
		
		//initializing estimates
		mapEstimates = new HashMap<String,Double>(lstPredictors.size());
		for(String s:lstPredictors){
			mapEstimates.put(s,0.5*(mapMinimum.get(s)+mapMaximum.get(s)));
		}
		
		//*******************************************
		//DataIO datTEMP = new DataIO("/home/jladau/Desktop/coefficient-estimates.csv");
		//mapEstimates = new HashMap<String,Double>(lstPredictors.size());
		//for(int i=1;i<datTEMP.iRows;i++){
		//	mapEstimates.put(datTEMP.getString(i,"REGION_ID"),datTEMP.getDouble(i,"COEFFICIENT_ESTIMATE"));
		//}
		//*******************************************
		
		
		
		
		
		
		//looping through coordinates
		d1 = sse();
		d0 = Double.MAX_VALUE;
		for(int k=0;k<1000;k++) {
			for(String s:lstPredictors){
				updatePredictor(s);
				d0 = d1;
				d1 = sse();
				if(Math.abs(d1-d0)<=dTolerance){
					break;
				}
			}
		}
		
		//***************************
		System.out.println(d1);
		//***************************
		
		if(Math.abs(d1-d0)>dTolerance){
			System.out.println("Warning: least squares minimization did not converge.");
		}
	}
	
	public double testSse(HashMap<String,Double> mapEstimates){
		this.mapEstimates = mapEstimates;
		return sse();
	}
	
	
	public HashMap<String,Double> coefficientEstimates(){
		return mapEstimates;
	}
	
	private double sse(){
		
		//mapPredicted = predicted values
		//d1 = output
		
		HashMap<Integer,Double> mapPredicted;
		double d1;
		
		mapPredicted = predictedValues();
		d1 = 0;
		for(Integer i:mapPredicted.keySet()){
			d1+=(mapPredicted.get(i)-mapResponse.get(i))*(mapPredicted.get(i)-mapResponse.get(i));
		}
		return d1;
	}
	
	private HashMap<Integer,Double> predictedValues(){
		
		//map1 = output
		//d1 = current predicted value
		
		HashMap<Integer,Double> map1;
		double d1;
		
		map1 = new HashMap<Integer,Double>(mapResponse.size());
		for(Integer i:mapResponse.keySet()){
			d1 = 0;
			for(String s:lstPredictors){
				d1+=mapEstimates.get(s)*tblPredictors.get(i,s);
			}
			map1.put(i,d1);
		}
		return map1;
	}
	
	private void updatePredictor(String sPredictor){
		
		//dZi = current value of z
		//dNumerator = numerator sum
		//dBetaHat = current estimate
		
		double dZi;
		double dNumerator;
		double dBetaHat;
		
		//looping through rows
		dNumerator=0;
		for(Integer i:mapResponse.keySet()){
			dZi = mapResponse.get(i);
			for(String s:lstPredictors){
				if(!s.equals(sPredictor)){
					dZi-=mapEstimates.get(s)*tblPredictors.get(i,s);
				}
			}
			dNumerator += tblPredictors.get(i,sPredictor)*dZi;
		}
		dBetaHat = dNumerator/mapX2.get(sPredictor);
		if(dBetaHat<mapMinimum.get(sPredictor)){
			dBetaHat = mapMinimum.get(sPredictor);
		}
		if(dBetaHat>mapMaximum.get(sPredictor)){
			dBetaHat = mapMaximum.get(sPredictor);
		}
		mapEstimates.put(sPredictor,dBetaHat);
	}	
}
