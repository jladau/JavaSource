package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.HashMap;

public interface Regression{

	
	/**Coefficient estimates (both null and observed)**/
	public HashMap<String,Double> coefficients(HashMap<String,Double> mapUnmergedPredictors);
	
	/**Number of null iterations**/
	public int nullIterations();
	
	/**Data used for fitting model, merged by merging category**/
	public ArrayList<String> printMergedData(HashMap<String,Double> mapUnmergedPredictors);
	
	/**Raw data used for fitting model**/
	public ArrayList<String> printUnmergedData(HashMap<String,Double> mapUnmergedPredictors);
		
	/**Returns map from analysis type (null or observed) to performance measure**/
	public HashMap<String,Double> performance(HashMap<String,Double> mapResponses, String sDirection);
	
	/**Returns standardized effect size for performance measure**/
	public HashMap<String,Double> performanceSES(HashMap<String,Double> mapResponses, String sDirection);
	
	/**Returns performance measure for observed analysis or specified null iteration**/
	public double performance(HashMap<String,Double> mapUnmergedPredictors, String sDirection, int iNullIteration);
	
	/**Returns transformed (e.g., quantile) data**/
	public ArrayList<String> printTransformedData(HashMap<String,Double> mapUnmergedPredictors);
	
	/**Returns predicted values**/
	public ArrayList<String> predictedValues(HashMap<String,Double> mapUnmergedPredictors);
	
}