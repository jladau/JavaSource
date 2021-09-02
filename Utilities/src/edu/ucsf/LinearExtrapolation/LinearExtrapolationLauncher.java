package edu.ucsf.LinearExtrapolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.LinearModel;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Finds predicted values for a set of points given a linear model.
 * @author jladau
 */

public class LinearExtrapolationLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//datTrain = training data
		//datPred = data for predictions
		//lnm1 = linear model
		//tbl1 = data table
		//set1 = set of predictors
		//sPredictor = predictor
		//sResponse = response
		//map1 = current predictor map
		//lstOut = output
		
		String sPredictor;
		String sResponse;
		ArgumentIO arg1;
		DataIO datTrain;
		DataIO datPred;
		LinearModel lnm1;
		HashBasedTable<String,String,Double> tbl1;		
		HashSet<String> set1;
		HashMap<String,Double> map1;
		ArrayList<String> lstOut;
		
		//initializing arguments
		arg1 = new ArgumentIO(rgsArgs);
		datTrain = new DataIO(arg1.getValueString("sTrainingDataPath"));
		datPred = new DataIO(arg1.getValueString("sPredictionDataPath"));
		sPredictor = arg1.getValueString("sPredictor");
		sResponse = arg1.getValueString("sResponse");
		lstOut = new ArrayList<String>(datTrain.iRows + datPred.iRows + 1);
		lstOut.add(sPredictor + ",OBSERVED_RESPONSE_VALUE,PREDICTED_RESPONSE_VALUE");
		
		//loading model
		set1 = new HashSet<String>();
		set1.add("x");
		tbl1 = HashBasedTable.create();
		for(int i=1;i<datTrain.iRows;i++){
			tbl1.put("x", Integer.toString(i), datTrain.getDouble(i, sPredictor));
			tbl1.put("y", Integer.toString(i), datTrain.getDouble(i, sResponse));
		}
		lnm1 = new LinearModel(tbl1,"y",set1);
		lnm1.fitModel(set1);
		
		//finding predictions
		for(int i=1;i<datTrain.iRows;i++){
			map1 = new HashMap<String,Double>();
			map1.put("x", datTrain.getDouble(i, sPredictor));
			lstOut.add(datTrain.getDouble(i, sPredictor) + "," + datTrain.getDouble(i, sResponse) + "," + lnm1.findPrediction(map1));
		}
		
		for(int i=1;i<datPred.iRows;i++){
			map1 = new HashMap<String,Double>();
			map1.put("x", datPred.getDouble(i, sPredictor));
			lstOut.add(datPred.getDouble(i, sPredictor) + ",NA," + lnm1.findPrediction(map1));
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
