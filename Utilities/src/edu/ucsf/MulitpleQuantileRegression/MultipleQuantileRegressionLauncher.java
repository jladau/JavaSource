package edu.ucsf.MulitpleQuantileRegression;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.Printer;

public class MultipleQuantileRegressionLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//mqd1 = multiple quantile regression data
		//mqm1 = multiple quantile regression model selection
		//lstOut = output
		//datVars = list of variables to consider
		//lstPredictors = list of predictors
		
		//TODO bivariate data needs to remain the same regardless of interactions
		
		ArgumentIO arg1;
		DataIO dat1;
		MultipleQuantileRegressionData mqd1;
		MultipleQuantileRegressionModelSelection mqm1;
		ArrayList<String> lstOut;
		DataIO datVars;
		ArrayList<String> lstPredictors;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		datVars = new DataIO(arg1.getValueString("sVarsPath"));
		mqd1 = new MultipleQuantileRegressionData(
				dat1.toStringTable(),
				arg1.getValueString("sSampleIDHeader"), 
				arg1.getValueString("sResponse"),
				arg1.getValueInt("iNeighborhoodSize"),
				arg1.getValueDouble("dPercentile"));
		mqm1 = new MultipleQuantileRegressionModelSelection(mqd1);
		for(int i=1;i<datVars.iRows;i++) {
			System.out.println("Analysis " + i + " of " + (datVars.iRows-1) + "...");
			lstPredictors = new ArrayList<String>(datVars.iCols);
			for(int j=0;j<datVars.iCols;j++) {
				lstPredictors.add(datVars.getString(i,j));
			}
			//try {
					mqm1.allSubsets(lstPredictors);
				//if(mqm1.bestModel().cvR2()>0) {
					//System.out.println(mqm1.bestModel().toString());
				//}
			//}catch(Exception e) {
			//}
		}
			
		//****************************************
		//Print.print(lstOut);
		//Print.print(mqm1.printBestModelData());
		//****************************************
		
		
		//mqd1.loadNeighborhoods(arg1.getValueArrayList("lstPredictors"));
		//mqr1 = new MultipleQuantileRegression(mqd1);
		
		//********************************
		//System.out.println(mqr1.crossValidationR2());
		//********************************		
		
		//terminating
		System.out.println("Done.");
	}
}