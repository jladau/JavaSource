package edu.ucsf.MultipleQuantileRegression0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class MultipleQuantileRegressionLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data, ordered by x values
		//iWindowSize = window size
		//lstY = predictors
		//lstYWindowed = y windowed
		//lstYShuffle = y values, shuffled
		//dPercent = percent
		//qrg1 = quantile regression object
		//iNullIterations = number of null iterations
		//lstOut = output
		//lstOutData = output data
		//rgdPercents = percentages to compute
		//rnd1 = random number generator
		//dPercentStep = step size for printing data
		//mapQRD = map from response variable names to quantile regression data objects
		//rgsPredictors = predictors
		//bPrintStatistics = flag for whether to print all null statistics
		//mayRandomizedYWindowed = map from iteration numbers to y shuffle values
		//iCounter = counter
		//iTotal = total analyses
		//set1 = set of all headers
		//mapQRN = map from predictor names to quantile regression objects
		//qrdYNull = current null quantile regression data object
		//lstX0 = list of x values for first predictor
		//rgsResponses = responses
		//rgsResponsesCurrent = current responses
		//rgd2 = current responses
		//mqr1 = multiple quantile regression object
		//map1 = coefficient estimates
		//lstX = current x vector
		//mapResponses = map from indices to response vectors
		//ydt1 = y data
		//lstNullResponseSets = sets of null responses
		//lstRows = list of rows in random order
		//lstPredictors = list of predictors (for subset)
		//iPredictors = total number of predictors to consider
		//sPredictor = current predictor
		//qrd1 = current quantile regression data object
		//lstHeaders = response headers
		//sbl1 = current output
		//bStandardized = flag for standardized coefficients
		
		boolean bStandardized;
		StringBuilder sbl1;
		MultipleQuantileRegressionData qrd1;
		String sPredictor;
		YData ydt1;
		ArrayList<Double> lstX;
		int iCounter;
		int iTotal;
		HashSet<String> set1;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		int iWindowSize;
		ArrayList<Double> lstY;
		Double rgdPercent[];
		Random rnd1;
		String[] rgsPredictors = null;
		String[] rgsResponses;
		MultipleQuantileRegression mqr1;
		HashMap<String,Double> map1;
		int iNullIterations;
		ArrayList<HashSet<String>> lstNullResponseSets;
		ArrayList<Integer> lstRows;
		ArrayList<String> lstPredictors;
		int iPredictors;
		ArrayList<String> lstHeaders;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		iWindowSize = arg1.getValueInt("iWindowSize");
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		rgdPercent = arg1.getValueDoubleArray("rgdPercentages");	
		iNullIterations = arg1.getValueInt("iNullIterations");
		iPredictors = arg1.getValueInt("iPredictors");
		bStandardized = arg1.getValueBoolean("bStandardized");
		
		//loading y data
		rgsResponses = arg1.getValueStringArray("rgsResponses");
		ydt1 = new YData(dat1.iRows,rgsResponses.length);
		lstNullResponseSets = new ArrayList<HashSet<String>>(iNullIterations);
		for(int i=0;i<iNullIterations;i++){
			lstNullResponseSets.add(new HashSet<String>(rgsResponses.length));
		}
		for(String s:rgsResponses){
			lstY = dat1.getDoubleColumn(s);
			ydt1.addAll(s,lstY);
		}
		
		//loading output headers
		lstHeaders = new ArrayList<String>(rgsResponses.length);
		for(int i=0;i<rgsResponses.length;i++) {
			lstHeaders.add(rgsResponses[i] + "_COEFFICIENT");
		}
		
		//loading y data: null
		lstRows = new ArrayList<Integer>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			lstRows.add(i);
		}
		for(int i=1;i<=iNullIterations;i++){
			Collections.shuffle(lstRows,rnd1);
			for(String s:rgsResponses){
				for(int k=0;k<lstRows.size();k++){
					ydt1.add(k,s + "_RANDOMIZED_" + i,dat1.getDouble(lstRows.get(k),s));
				}
				lstNullResponseSets.get(i-1).add(s + "_RANDOMIZED_" + i);
			}
		}
		if(arg1.containsArgument("bRandomizeResponses") && arg1.getValueBoolean("bRandomizeResponses")){
			System.out.println("Randomizing responses...");
			ydt1.shuffle(rnd1);
		}
		
		//loading predictor names
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
		lstPredictors = new ArrayList<String>(rgsPredictors.length);
		for(String s:rgsPredictors){
			lstPredictors.add(s);
		}
			
		//initializing output
		lstOut = new ArrayList<String>(1);
		lstOut.add("PREDICTOR,REGRESSION_TYPE,REGRESSION_ID,PERCENT," + Joiner.on(",").join(lstHeaders));
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
	
		//looping through predictors
		Collections.shuffle(lstPredictors);
		if(iPredictors>lstPredictors.size()){
			iPredictors=lstPredictors.size();
		}
		iTotal = iPredictors*rgdPercent.length;
		iCounter=0;
		for(int k=0;k<lstPredictors.size();k++){
			lstOut = new ArrayList<String>(rgdPercent.length*(iNullIterations+1));
			if((k+1)>iPredictors){
				break;
			}
			sPredictor = lstPredictors.get(k);
			lstX = dat1.getDoubleColumn(sPredictor);
			if(arg1.containsArgument("bRandomizePredictors") && arg1.getValueBoolean("bRandomizePredictors")){
				Collections.shuffle(lstX);
			}
			qrd1 = new MultipleQuantileRegressionData(lstX, ydt1, iWindowSize, rnd1);
			for(Double dPercent:rgdPercent){
				iCounter++;
				System.out.println("Finding coefficients for predictor " + iCounter + " of " + iTotal + "...");
				qrd1.windowY(dPercent);
				mqr1 = new MultipleQuantileRegression(qrd1, bStandardized);
				mqr1.fit(rgsResponses);
				map1 = mqr1.coefficientEstimates();
				sbl1 = new StringBuilder();
				sbl1.append(sPredictor + ",observed,0," + dPercent);
				for(int i=0;i<rgsResponses.length;i++) {
					sbl1.append("," + map1.get(rgsResponses[i]));
				}
				lstOut.add(sbl1.toString());		
				for(int i=0;i<iNullIterations;i++){
					mqr1.fit(lstNullResponseSets.get(i));
					map1 = mqr1.coefficientEstimates();
					sbl1 = new StringBuilder();
					sbl1.append(sPredictor + ",randomized," + (i+1) + "," + dPercent);
					for(int l=0;l<rgsResponses.length;l++){
						sbl1.append("," + map1.get(rgsResponses[l] + "_RANDOMIZED_" + (i+1)));
					}
					lstOut.add(sbl1.toString());		
				}
			}
			DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"),true);
		}
		
		System.out.println("Done.");
	}
}