package edu.ucsf.base;

import java.util.HashMap;
import java.util.HashSet;


/**
 * This code implements hierarchical partitioning for assessing the relative importance of variables in regression
 * @author jladau
 */

public class HierarchicalPartitioning {

	//rgsPredictors = predictors
	//lnm1 = linear model object
	//per1 = permutation object
	//mapLMG(sVariable) = returns hierarchical partitioning value for variable
	
	private String[] rgsPredictors;
	private LinearModel lnm1;
	private Permutation<String> per1;
	public HashMap_AdditiveDouble<String> mapLMG;
	
	/**
	 * Constructor
	 * @param dat1 Data
	 * @param sResponse Response variable
	 * @param rgsPredictions Predictors
	 * @param sTransform Transform to use for response and predictors.
	 */
	public HierarchicalPartitioning(LinearModel lnm1, String rgsPredictors[], String sResponse, double dIterations) throws Exception{
		
		//dCount = number of iterations applied
		//map1(SVariable) = current marginal r^2 values
		//set1 = set of predictors
		
		HashMap<String,Double> map1;
		double dCount=0;
		HashSet<String> set1;
		
		//saving data
		this.rgsPredictors=rgsPredictors;
	
		//initializing fit lm object
		this.lnm1=lnm1;
	
		//initializing output
		mapLMG = new HashMap_AdditiveDouble<String>();
		
		//checking if more than one permutation needs to be considered
		if(rgsPredictors.length>1){
		
			//initializing permutation object
			per1 = new Permutation<String>(ArrayOperations.convertArrayToArrayList(rgsPredictors));
			
			//checking if random permutations should be used
			if(rgsPredictors.length>5){
			
				//looping through iterations
				for(int i=0;i<dIterations;i++){
					per1.loadRandomPermutation();
					map1=this.calculateMarginalR2();
					for(String s:map1.keySet()){
						mapLMG.putSum(s, map1.get(s));
					}
					dCount++;
				}
				
			//5 or fewer predictors: looking at all permutations	
			}else{
				
				//looping through all permutations
				do{
					
					//loading next permutation
					per1.nextPermutation();
					map1=this.calculateMarginalR2();
					for(String s:map1.keySet()){
						mapLMG.putSum(s, map1.get(s));
					}
					dCount++;
				}while(per1.hasNext());
			}
				
			//finding means
			for(String s:mapLMG.keySet()){
				mapLMG.put(s, mapLMG.get(s)/dCount);
			}
		}else{
			set1 = new HashSet<String>();
			set1.add(rgsPredictors[0]);
			lnm1.fitModel(set1);
			mapLMG.put(rgsPredictors[0], lnm1.findRSquared());
		}
	}
	
	/**
	 * Gets partitioning.
	 */
	public HashMap<String,Double> getPatitioning(){
		return mapLMG;
	}
	
	/**
	 * Calculates R^2 values for each predictor using current permutation
	 * @return Map giving variables and their marginal R^2 values
	 */
	private HashMap<String,Double> calculateMarginalR2() throws Exception{
		
		//map1 = output
		//set1 = current set of predictors
		//dR2Previous = previous r^2
		//dR2Current = current r^2
		
		double dR2Previous; double dR2Current;
		HashSet<String> set1;
		HashMap<String,Double> map1;
		
		//initializing output
		map1 = new HashMap<String,Double>(rgsPredictors.length);
		
		//looping through variables
		dR2Previous=0;
		for(int i=0;i<rgsPredictors.length;i++){
			
			//loading current predictors
			set1 = new HashSet<String>();
			for(int k=0;k<=i;k++){
				set1.add(per1.getImage(rgsPredictors[k]));
			}
			
			//fitting model
			lnm1.fitModel(set1);
			
			//loading current r^2
			dR2Current=lnm1.findRSquared();
			
			//saving result
			map1.put(per1.getImage(rgsPredictors[i]), dR2Current-dR2Previous);
			
			//updating r^2
			dR2Previous=dR2Current;
		}
		
		//returning result
		return map1;
	}
}