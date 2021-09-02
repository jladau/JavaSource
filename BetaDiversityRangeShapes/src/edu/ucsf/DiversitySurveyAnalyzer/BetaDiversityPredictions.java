package edu.ucsf.DiversitySurveyAnalyzer;

import edu.ucsf.geospatial.SphericalCapEarth_SamplingRegion;
import static java.lang.Math.*;

import java.util.HashMap;

/**
 * Class for generating predictions
 * @author jladau
 */

public class BetaDiversityPredictions {

	/**Observed Regional perimeter**/
	private double l;
	
	/**Observed Regional area**/
	private double f;
	
	/**Total number of species in region**/
	private double gamma;
	
	/**Scaled Global range area**/
	private double F_Global;
	
	/**Scaled Global range perimeter**/
	private double L_Global;
	
	/**Area of the sampled region**/
	private double a;
	
	/**Distance between pairs of sampling plots**/
	private double d;
	
	/**Map of predictions**/
	private HashMap<String,Double> mapPredictions;
	
	public BetaDiversityPredictions(SphericalCapEarth_SamplingRegion reg1, double dSamplingPlotDistance){
		
		//loading variables
		l = reg1.perimeterRegional();
		f = reg1.areaRegional();
		gamma = reg1.numberOfSpecies();
		a = reg1.area();
		d = dSamplingPlotDistance;
		F_Global = reg1.areaGlobalScaled();
		L_Global = reg1.perimeterGlobalScaled();
		this.loadPredictions();
	}
	
	private double prediction(String sVariable, String sCondition){
		
		//Regional range given Global range
		if(sVariable.equals("Regional_perimeter_mean") && sCondition.equals("global_range")){
			return L_Global/gamma;
		}
		if(sVariable.equals("Regional_area_mean") && sCondition.equals("global_range")){
			return F_Global/gamma;
		}
		
		//Beta-diversity given Regional range attributes
		if(sVariable.equals("beta_w") && sCondition.equals("regional_range")){
			return a*gamma/f;
		}	
		if(sVariable.equals("beta_s") && sCondition.equals("regional_range")){
			return min(1., l*d/(f*PI));
		}
		if(sVariable.equals("beta_j") && sCondition.equals("regional_range")){
			return min(1., 2/((PI*f)/(d*l)+1));
		}
		
		//Beta-diversity given Global range attributes
		if(sVariable.equals("beta_w") && sCondition.equals("global_range")){
			return a*gamma/F_Global;
		}	
		if(sVariable.equals("beta_s") && sCondition.equals("global_range")){
			return min(1., L_Global*d/(F_Global*PI));
		}
		if(sVariable.equals("beta_j") && sCondition.equals("global_range")){
			return min(1., 2/((PI*F_Global)/(d*L_Global)+1));
		}
		
		return Double.NaN;
	}
	
	
	private void loadPredictions(){
		
		//rgs1 = values being predicted
		//rgs2 = conditions

		String rgs1[];
		String rgs2[];
		
		rgs1 = new String[]{"beta_w", "beta_s", "beta_j", "Regional_area_mean", "Regional_perimeter_mean"};
		rgs2 = new String[]{"regional_range", "global_range"};
		mapPredictions = new HashMap<String,Double>();
		for(String s:rgs1){
			for(String t:rgs2){
				if(hasPrediction(s,t)){
					mapPredictions.put(s + "_given_" + t, prediction(s,t));
				}
			}
		}
	}
	
	public HashMap<String,Double> predictions(){
		return mapPredictions;
	}
	
	
	private boolean hasPrediction(String sVariable, String sCondition){
		
		//Regional range given Global range
		if(sVariable.equals("Regional_perimeter_mean") && sCondition.equals("global_range")){
			return true;
		}
		if(sVariable.equals("Regional_area_mean") && sCondition.equals("global_range")){
			return true;
		}
		
		//Beta-diversity given Regional range attributes
		if(sVariable.equals("beta_w") && sCondition.equals("regional_range")){
			return true;
		}	
		if(sVariable.equals("beta_s") && sCondition.equals("regional_range")){
			return true;
		}
		if(sVariable.equals("beta_j") && sCondition.equals("regional_range")){
			return true;
		}
		
		//Beta-diversity given Global range attributes
		if(sVariable.equals("beta_w") && sCondition.equals("global_range")){
			return true;
		}	
		if(sVariable.equals("beta_s") && sCondition.equals("global_range")){
			return true;
		}
		if(sVariable.equals("beta_j") && sCondition.equals("global_range")){
			return true;
		}	
		return false;
	}
}
