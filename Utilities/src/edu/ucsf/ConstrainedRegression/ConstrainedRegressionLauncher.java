package edu.ucsf.ConstrainedRegression;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class ConstrainedRegressionLauncher{
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//mapMinimum = map from predictor names to minimum constraints
		//mapMaximum = map from predictor names to maximum constraints
		//mapResponse = map from row numbers to response variables
		//tblPredictors = table from row numbers, predictor names to predictor values
		//lstPred = list of predictors
		//sResponse = response
		//clm1 = constrained linear model
		//mapEstimates = map from coefficient names to estimates
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<String,Double> mapMinimum;
		HashMap<String,Double> mapMaximum;
		HashMap<Integer,Double> mapResponse;
		HashMap<String,Double> mapEstimates;
		HashBasedTable<Integer,String,Double> tblPredictors;
		ArrayList<String> lstPred;
		String sResponse;
		ConstrainedLinearModel clm1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstPred = arg1.getValueArrayList("lstPredictors");
		sResponse = arg1.getValueString("sResponse");
		
		//loading data and constraint tables
		mapResponse = new HashMap<Integer,Double>(dat1.iRows);
		tblPredictors = HashBasedTable.create(dat1.iRows,lstPred.size());
		for(int i=1;i<dat1.iRows;i++){
			mapResponse.put(i,dat1.getDouble(i,sResponse));
			for(String s:lstPred){
				tblPredictors.put(i,s,dat1.getDouble(i,s));
			}
		}
		mapMinimum = new HashMap<String,Double>(lstPred.size());
		mapMaximum = new HashMap<String,Double>(lstPred.size());
		for(String s:lstPred){
			mapMinimum.put(s,0.);
			mapMaximum.put(s,10000.);
		}
		
		//fitting model
		clm1 = new ConstrainedLinearModel(mapResponse, tblPredictors, mapMinimum, mapMaximum);
		clm1.fitModel(0.0000000001);
		mapEstimates = clm1.coefficientEstimates();
		
		//*********************************
		System.out.println("");
		for(String s:mapEstimates.keySet()){
			System.out.println(s + "," + mapEstimates.get(s));
		}
		System.out.println("");	
		
		//HashMap<String,Double> mapTest = new HashMap<String,Double>();
		//mapTest.put("X1",0.2);
		//mapTest.put("X2",0.5);
		//mapTest.put("X3",1.1);
		//System.out.println(clm1.testSse(mapTest));
		
		//*********************************
		
		//terminating
		System.out.println("Done.");
	}
}
