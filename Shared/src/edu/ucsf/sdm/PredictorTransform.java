package edu.ucsf.sdm;

/**
 * Transforms predictor variables
 * @author jladau
 */

public class PredictorTransform {

	public static double apply(double dValue, String sPredictorTransform) throws Exception{
		
		//dOut = output
		
		double dOut = Double.NaN;
		
		if(!Double.isNaN(dValue)){
			if(sPredictorTransform.equals("log10")){
				dOut = Math.log10(dValue);
			}else if(sPredictorTransform.equals("abs")){
				dOut = Math.abs(dValue);
			}else if(sPredictorTransform.equals("log10abs")){
				dOut = Math.log10(Math.abs(dValue));
			}else if(sPredictorTransform.equals("identity")){
				dOut = dValue;
			}
		}
		if(Double.isNaN(dOut)){
			throw new Exception("Predictor transform failed.");
		}
		return dOut;
	}
}
