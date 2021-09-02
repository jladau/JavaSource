package edu.ucsf.MultipleQuantileRegression0;

import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.MultipleQuantileRegression0.MultipleQuantileRegression;
import edu.ucsf.base.LinearModel;

public class MultipleQuantileRegression{

	/**Linear model object**/
	private LinearModel lnm1;
	
	/**Coefficient estimates**/
	private HashMap<String,Double> mapCoefficients;
	
	/**Flag for whether standardized coefficient estimates**/
	private boolean bStandardized;
	
	public MultipleQuantileRegression(MultipleQuantileRegressionData qrd1, boolean bStandardized) throws Exception{
		lnm1 = new LinearModel(qrd1.toLinearModelData(),qrd1.predictor(),qrd1.responses());
		this.bStandardized = bStandardized;
	}
	
	public void fit(HashSet<String> setResponses) throws Exception{
		lnm1.fitModel(setResponses);
		if(bStandardized){
			mapCoefficients = lnm1.findStandardizedCoefficientEstimates();
		}else {
			mapCoefficients = lnm1.findCoefficientEstimates();
		}
	}

	public void fit(String[] rgsResponses) throws Exception{
		
		//set1 = set of response variables
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(rgsResponses.length);
		for(String s:rgsResponses){
			set1.add(s);
		}
		fit(set1);
	}
	
	public HashMap<String,Double> coefficientEstimates(){
		return mapCoefficients;
	}

}

	
