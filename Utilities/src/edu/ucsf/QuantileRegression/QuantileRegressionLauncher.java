package edu.ucsf.QuantileRegression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import edu.ucsf.base.QuantileRegression;
import edu.ucsf.base.QuantileRegressionData;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class QuantileRegressionLauncher{

	
	public static void main(String rgsArgs[]){
		
		//dat1 = data
		//arg1 = arguments
		//setPredictors = predictors
		//setResponses = responses
		//iNullIterations = number of null iterations
		//iWindowSize = window size
		//rgdPercentages = quantiles to use
		//lstPermutation = current permutation
		//rnd1 = random number generator
		//qrd1 = quantile regression data
		//qrg1 = quantile regression object
		//lstOut = output
		
		ArrayList<String> lstOut;
		QuantileRegression qrg1;
		QuantileRegressionData qrd1;
		ArgumentIO arg1;
		DataIO dat1;
		HashSet<String> setPredictors;
		HashSet<String> setResponses;
		int iNullIterations;
		int iWindowSize;
		Double rgdPercentages[];
		Random rnd1;
		ArrayList<Integer> lstPermutation;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		if(arg1.containsArgument("setXHeaders")){
			setPredictors = arg1.getValueHashSet("setXHeaders");
		}else{
			setPredictors = dat1.getHeaders();
			for(String s:arg1.getValueHashSet("setNotXHeaders")){
				setPredictors.remove(s);
			}
		}
		if(arg1.containsArgument("setYHeaders")){
			setResponses = arg1.getValueHashSet("setYHeaders");
		}else{
			setResponses = dat1.getHeaders();
			for(String s:arg1.getValueHashSet("setNotYHeaders")){
				setResponses.remove(s);
			}
		}
		iNullIterations = arg1.getValueInt("iNullIterations");
		iWindowSize = arg1.getValueInt("iWindowSize");
		rgdPercentages = arg1.getValueDoubleArray("rgdPercentages");	
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		
		//initializing quantile regression data
		//TODO random seed is hard coded here
		qrd1 = new QuantileRegressionData(dat1.toDoubleTable(), setPredictors, setResponses, iWindowSize);
		
		//initializing output
		lstOut = new ArrayList<String>(1 + setPredictors.size()+setResponses.size());
		lstOut.add("PREDICTOR,RESPONSE,PERCENT,NULL_ITERATION,SLOPE,PEARSON");
		
		//finding observed (non-randomized results)
		lstPermutation = new ArrayList<Integer>(dat1.iRows-1);
		for(int i=1;i<dat1.iRows;i++){
			lstPermutation.add(i-1);
		}
		for(String sPredictor:setPredictors){
			for(String sResponse:setResponses){
				qrd1.loadYWindows(lstPermutation,sPredictor,sResponse);
				for(Double dPercent:rgdPercentages){
					qrg1 = new QuantileRegression(qrd1,dPercent);
					lstOut.add(sPredictor + "," + sResponse + "," + dPercent + ",-1," + qrg1.slope() + "," + qrg1.correlation());
				}
			}
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		
		//finding randomized results
		lstOut = new ArrayList<String>(10000);
		for(int i=0;i<iNullIterations;i++){
			System.out.println("Null iteration " + (i+1) + " of " + iNullIterations + "...");
			
			//TODO write an option for permuting within categories here
			Collections.shuffle(lstPermutation,rnd1);
			
			for(String sPredictor:setPredictors){
				for(String sResponse:setResponses){
					qrd1.loadYWindows(lstPermutation,sPredictor,sResponse);
					for(Double dPercent:rgdPercentages){
						qrg1 = new QuantileRegression(qrd1,dPercent);
						lstOut.add(sPredictor + "," + sResponse + "," + dPercent + "," + (i+1) + "," + qrg1.slope() + "," + qrg1.correlation());
						if(lstOut.size()>9999){
							DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"),true);
							lstOut = new ArrayList<String>(10000);
						}
					}
				}
			}
		}
		if(lstOut.size()>0) {
			DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"),true);
		}
		
		//terminating
		System.out.println("Done.");
	}	
}