package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.unittested.base.ExtendedMath;

/**
 * Transforms response variable, or applies inverse transformation for projections
 * @author jladau
 *
 */

//TODO write unit tests

public class ResponseTransform {

	public static HashMap<String,Double> apply(HashMap<String,Double> mapDiversity, String sVar, String sResponseTransform) throws Exception{
		
		//dMean = mean
		//mapOut = output
		
		HashMap<String,Double> mapOut;
		double dMean;
		
		if(sResponseTransform.equals("squareddeviation")){
			dMean = ExtendedMath.mean(new ArrayList<Double>(mapDiversity.values()));
		}else{
			dMean = Double.NaN;
		}
		mapOut = new HashMap<String,Double>(mapDiversity.size());
		for(String s:mapDiversity.keySet()){
			mapOut.put(s, apply(mapDiversity.get(s), sVar, sResponseTransform, dMean));
		}
		return mapOut;
	}
	
	private static double apply(double d1, String sVar, String sResponseTransform, double dMean) throws Exception{
		
		if(sResponseTransform.equals("squareddeviation")){
			return Math.log10((d1-dMean)*(d1-dMean));
		}
		if(sVar.equals("Richness")){
			return Math.log10(d1+0.0000001);
		}else if(sVar.equals("Shannon")){
			return Math.log10(d1+0.0000001);
		}else{
			if(sResponseTransform==null || sResponseTransform.equals("logit")){
				if(d1==0 || d1==1){
					return Double.NaN;
				}else{
					return Math.log(d1/(1.-d1));
				}
			}else if(sResponseTransform.equals("identity")){
				return d1;
			}else if(sResponseTransform.equals("log") || sResponseTransform.equals("log10")){
				return Math.log10(d1+0.0000001);
			}else{
				throw new Exception("No response variable transform specified.");
			}
		}
	}
	
	public static double applyInverse(double d1, String sVar, String sResponseTransform) throws Exception{
		
		if(sVar.equals("Richness")){
			return Math.pow(10, d1)-0.0000001;
		}else if(sVar.equals("Shannon")){
			return Math.pow(10, d1)-0.0000001;
		}else{
			if(sResponseTransform==null || sResponseTransform.equals("logit")){
				return 1./(1.+Math.exp(-d1));
			}else if(sResponseTransform.equals("identity")){
				return d1;
			}else if(sResponseTransform.equals("log") || sResponseTransform.equals("log10")){
				return Math.pow(10, d1)-0.0000001;
			}else{
				throw new Exception("No response variable inverse transform specified.");
			}
		}
	}
	
}
