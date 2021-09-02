package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Adds MESS calculation to linear model
 * @author jladau
 *
 */

public class LinearModel_MESS extends LinearModel{

	/**Map from variable names to individual variable MESS objects**/
	private HashMap<String,SingleVariableMESS> mapMESSVar;
	
	
	public LinearModel_MESS(Table<String, String, Double> tblData, String sResponse, Set<String> setPredictors) throws Exception {
		
		//tbl1 = table of samples, predictor names, and values
		//rgd1 = current values
		//i1 = counter
		
		super(tblData, sResponse, setPredictors);
		
		HashBasedTable<String,String,Double> tbl1;
		double rgd1[];
		int i1;
		
		mapMESSVar = new HashMap<String,SingleVariableMESS>();
		fitModel(setPredictors);
		tbl1 = this.getPredictors();
		for(String t:tbl1.columnKeySet()){
			rgd1 = new double[tbl1.rowKeySet().size()];
			i1 = 0;
			for(String s:tbl1.rowKeySet()){
				rgd1[i1]=tbl1.get(s, t);
				i1++;
			}
			mapMESSVar.put(t, new SingleVariableMESS(rgd1));
		}
	}
	
	
	

	//TODO write unit test
	public HashMap<String,Double> findMESS(HashMap<String,Double> mapPredictors){
		
		//map1 = output
		//dMin = minimum
		//d1 = current value
		
		HashMap<String,Double> map1;
		double dMin;
		double d1;
	
		
		
		
		map1 = new HashMap<String,Double>(mapPredictors.size()+1);
		dMin = Double.MAX_VALUE;
		for(String s:mapPredictors.keySet()){
			d1 = mapMESSVar.get(s).getMESS(mapPredictors.get(s));
			map1.put(s, d1);
			if(dMin>d1){
				dMin = d1;
			}
		}
		map1.put("overall", dMin);
		return map1;
		
	}
	
	public double findPrediction(HashMap<String,Double> mapPredictors){
		return findMESS(mapPredictors).get("overall");
	}
	
	private class SingleVariableMESS{
		
		//emp1 = empirical distribution object
		//dMin = minimum value
		//dMax = maximum value
		
		private EmpiricalDistribution_Cumulative emp1;
		private double dMin;
		private double dMax;
		
		private SingleVariableMESS(double rgdSampledValues[]){
			
			//initializing list of values
			emp1 = new EmpiricalDistribution_Cumulative(rgdSampledValues);
			dMin = emp1.getSupportLowerBound();
			dMax = emp1.getSupportUpperBound();
		}
		
		/**
		 * Gets MESS for specified value
		 * @param dValue Value to find MESS for
		 */
		private double getMESS(double dValue){
			
			//dF = percent of reference points smaller than specified value
			
			double dF;
			
			dF = emp1.cumulativeProbability(dValue)-emp1.probability(dValue);
			
			if(dF==0){
				return (dValue-dMin)/(dMax-dMin)*100.;
			}else if(0 < dF && dF<=0.5){
				return 2.*dF*100;
			}else if(0.5 < dF && dF<1.){
				return 2.*(100.-100.*dF);
			}else if(dF==1.){
				return (dMax-dValue)/(dMax-dMin)*100.;
			}else{
				return Double.NaN;
			}
		}
		
		
	}
	
}
