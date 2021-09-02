package edu.ucsf.QuantileRegressionOld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import edu.ucsf.base.QuantileRegression;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class QuantileRegressionLauncher{

	/*
	
	public static void main0(String rgsArgs[]) {
		
		//arg1 = arguments
		//dat1 = data, ordered by x values
		//iWindowSize = window size
		//lstY = predictors
		//lstYWindowed = y windowed
		//lstRandomizedY = y values, shuffled
		//dPercent = percent
		//qrg1 = quantile regression object
		//iNullIterations = number of null iterations
		//lstOut = output
		//lstOutData = output data
		//rgdPercents = percentages to compute
		//rnd1 = random number generator
		//dPercentStep = step size for printing data
		//mapQR = map from response variable names to quantile regression objects
		//rgsPredictors = predictors
		//bPrintStatistics = flag for whether to print all null statistics
		//mayRandomizedYWindowed = map from iteration numbers to y shuffle values
		//iCounter = counter
		//iTotal = total analyses
		//set1 = set of all headers
	
		HashSet<String> set1;
		int iCounter;
		int iTotal;
		boolean bPrintStatistics;
		ArrayList<String> lstOut;
		ArrayList<String> lstOutData = null;
		ArgumentIO arg1;
		DataIO dat1;
		int iWindowSize;
		ArrayList<Double> lstY;
		ArrayList<Double> lstYWindowed;
		ArrayList<Double> lstRandomizedY;
		//QuantileRegression qrg1;
		int iNullIterations;
		Double rgdPercent[];
		Random rnd1;
		double dPercentStep;
		HashMap<String,QuantileRegression> mapQR;
		String[] rgsPredictors = null;
		HashMap<Integer,ArrayList<Double>> mapRandomizedYWindowed;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		iWindowSize = arg1.getValueInt("iWindowSize");
		iNullIterations = arg1.getValueInt("iNullIterations");
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		rgdPercent = arg1.getValueDoubleArray("rgdPercentages");	
		if(rgdPercent.length>1){
			dPercentStep=rgdPercent[1]-rgdPercent[0];
		}else{
			dPercentStep=0.5;
		}
		if(!arg1.containsArgument("bPrintStatistics")){
			bPrintStatistics=true;
		}else {
			bPrintStatistics = arg1.getValueBoolean("bPrintStatistics");
		}
		
		//loading predictors and response
		lstY = dat1.getDoubleColumn(arg1.getValueString("sYHeader"));
		if(arg1.containsArgument("bRandomizeResponses") && arg1.getValueBoolean("bRandomizeResponses")){
			System.out.println("Randomizing responses...");
			Collections.shuffle(lstY);
		}
		lstRandomizedY = new ArrayList<Double>(lstY);
		if(arg1.containsArgument("rgsXHeaders")){
			rgsPredictors = arg1.getValueStringArray("rgsXHeaders");
		}else if(arg1.containsArgument("rgsNotXHeaders")){
			set1=dat1.getHeaders();
			for(String s:arg1.getValueStringArray("rgsNotXHeaders")){
				if(set1.contains(s)) {
					set1.remove(s);
				}
			}
			rgsPredictors = set1.toArray(new String[set1.size()]);
		}
		
		//initializing output
		lstOut = new ArrayList<String>(100);
		lstOut.add("PREDICTOR,PERCENT,OBSERVED_VALUE,SES,PROBABILITY_GTE,PROBABILITY_LTE");
		if(!arg1.containsArgument("bPrintStatistics") || arg1.getValueBoolean("bPrintStatistics")==true){
			lstOutData = new ArrayList<String>(1+(iNullIterations+1)*rgdPercent.length+1);
			lstOutData.add(QuantileRegression.printHeader());
		}
		
		//initializing quantile regression objects
		mapQR = new HashMap<String,QuantileRegression>(rgsPredictors.length);
		for(String sPredictor:rgsPredictors){
			mapQR.put(sPredictor,new QuantileRegression(dat1.getDoubleColumn(sPredictor), lstY, iWindowSize, rnd1, iNullIterations, bPrintStatistics));
		}
		
		//running regressions
		iCounter=0;
		iTotal = rgdPercent.length*rgsPredictors.length;
		for(Double dPercent:rgdPercent){		
			mapRandomizedYWindowed = new HashMap<Integer,ArrayList<Double>>(iNullIterations);
			for(int i=0;i<iNullIterations;i++){
				Collections.shuffle(lstRandomizedY,rnd1);
				mapRandomizedYWindowed.put(i,QuantileRegression.windowRandomizedY(lstRandomizedY,iWindowSize,dPercent));
			}
			for(String sPredictor:rgsPredictors) {
				iCounter++;
				System.out.println("Running analysis " + iCounter + " of " + iTotal + "...");
				lstYWindowed = mapQR.get(sPredictor).windowY(dPercent);
				mapQR.get(sPredictor).clear();
				mapQR.get(sPredictor).fit(lstYWindowed,"observed");
				for(int i=0;i<iNullIterations;i++){
					mapQR.get(sPredictor).fit(mapRandomizedYWindowed.get(i),"null");
				}
				lstOut.add(
						sPredictor + "," + 
					    dPercent + "," + 
						mapQR.get(sPredictor).observed() + "," + 
					    mapQR.get(sPredictor).ses() + "," + 
						mapQR.get(sPredictor).dProbabilityGTE() + "," + 
					    mapQR.get(sPredictor).dProbabilityLTE());
				if(!arg1.containsArgument("bPrintStatistics") || arg1.getValueBoolean("bPrintStatistics")==true){
					lstOutData.addAll(mapQR.get(sPredictor).print(sPredictor,dPercent,dPercentStep));
				}
			}
		}
			
		//outputting results
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		if(!arg1.containsArgument("bPrintStatistics") || arg1.getValueBoolean("bPrintStatistics")==true){	
			DataIO.writeToFile(lstOutData,arg1.getValueString("sOutputPath").replace(".csv","-data.csv"));
		}
		System.out.println("Done.");
	}
	
	*/
}