package edu.ucsf.base;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Runs all subsets model selection based on PRESS statistic.
 * @author jladau
 */

public class AllSubsetsModelSelection {
	
	/**Returns the best model for the specified number of predictors**/
	private HashMap<Integer,ModelSummary> mapBestModels;
	
	public AllSubsetsModelSelection(
			Set<String> setPredictors, 
			String sResponse, 
			String sResponseTransform, 
			String sUnits, 
			LinearModel lnm1, 
			int iMaxPredictors, 
			double dMaxVIF, 
			BinaryRelation<String> binNestedPredictors,
			BinaryRelation<String> binDoNotCheckVIF,			
			HashSet<HashSet<String>> setExclusivePredictors) throws Exception{
		
		//setPower = power set
		//dPRESS = current press value
		//iBest = best model predictor count
		//dTSS = total sum of squares
		
		Set<Set<String>> setPower;
		double dPRESS;
		double dTSS;
		int iBest = -1;

		//loading power set
		if(setExclusivePredictors==null){
			setPower = Sets.powerSet(setPredictors);
		}else{
			setPower = powerSetFromExclusiveSets(setExclusivePredictors);
		}
			
		//filtering by nested predictors, if requested
		if(binNestedPredictors!=null){
			setPower = filterByNestedPredictors(setPower, binNestedPredictors);
		}

		//initializing map with best models
		dTSS = lnm1.findTSS();
		mapBestModels = new HashMap<Integer,ModelSummary>();
		for(int i=0;i<=iMaxPredictors;i++){
			mapBestModels.put(i, new ModelSummary());
			mapBestModels.get(i).sResponse=sResponse;
			mapBestModels.get(i).sResponseTransform=sResponseTransform;
			mapBestModels.get(i).sUnits = sUnits;
			mapBestModels.get(i).dTSS=dTSS;
			mapBestModels.get(i).iObservations=lnm1.getNumberOfObservations();
		}
		
		//looping through power set
		for(Set<String> set1:setPower){
			
			//checking if acceptable number of predictors
			if(set1.size()<=iMaxPredictors){	
				
				//fitting model
				lnm1.fitModel(set1);
				
				//checking variance inflation factor
				if(lnm1.checkVIF(dMaxVIF, binDoNotCheckVIF)){
				
					//checking if model is the best
					dPRESS = lnm1.findPRESS();
					if(dPRESS<mapBestModels.get(set1.size()).dPRESS){
						mapBestModels.get(set1.size()).dPRESS=dPRESS;
						mapBestModels.get(set1.size()).dR2=lnm1.findRSquared();
						mapBestModels.get(set1.size()).dR2Adjusted=lnm1.findAdjustedRSquared();
						mapBestModels.get(set1.size()).mapCoefficientEstimates=lnm1.findCoefficientEstimates();
						mapBestModels.get(set1.size()).mapLMG=lnm1.findLMG();
						mapBestModels.get(set1.size()).mapStandardizedCoefficients=lnm1.findStandardizedCoefficientEstimates();
					}
				}
			}
		}
		
		//finding best overall model and loading TSS
		dPRESS = Double.POSITIVE_INFINITY;
		for(Integer i:mapBestModels.keySet()){
			if(mapBestModels.get(i).dPRESS<dPRESS){
				dPRESS=mapBestModels.get(i).dPRESS;
				iBest = i;
			}
		}
		mapBestModels.get(iBest).bBestModelOverall=true;
	}
	
	/**
	 * Removes illegal combinations of predictors (useful when fitting a polynomial model, for instance)
	 * @param setPower Set of all sets of predictors to consider
	 * @param binNestedPredictors Gives set of predictors that must co-occur.
	 * @return Filtered power set
	 */
	private Set<Set<String>> filterByNestedPredictors(Set<Set<String>> setPower, BinaryRelation<String> binNestedPredictors){
		
		//setOut = output
		//b1 = flag for whether current set passes
		
		Set<Set<String>> setOut;
		boolean b1;
		
		//initializing output
		setOut = new HashSet<Set<String>>();
		
		//looping through set of sets of predictors
		for(Set<String> set1:setPower){
			b1=true;
			for(OrderedPair<String> orp1:binNestedPredictors.getOrderedPairs()){
				if(set1.contains(orp1.o1) && !set1.contains(orp1.o2)){
					b1=false;
					break;
				}
			}
			if(b1==true){
				setOut.add(new HashSet<String>(set1));
			}
		}
		
		//outputting result
		return setOut;
	}
	
	/**
	 * Removes illegal combinations of predictors (useful when fitting a polynomial model, for instance)
	 * @param setPower Set of all sets of predictors to consider
	 * @param binExclusivePredictors Gives sets of predictors that must not co-occur.
	 * @return Filtered power set
	 */
	private Set<Set<String>> filterByExclusivePredictors(Set<Set<String>> setPower, BinaryRelation<String> binExlcusivePredictors){
		
		//setOut = output
		//b1 = flag for whether current set passes
		
		Set<Set<String>> setOut;
		boolean b1;
		
		//initializing output
		setOut = new HashSet<Set<String>>();
		
		//looping through set of sets of predictors
		for(Set<String> set1:setPower){
			b1=true;
			for(OrderedPair<String> orp1:binExlcusivePredictors.getOrderedPairs()){
				if(set1.contains(orp1.o1) && set1.contains(orp1.o2)){
					b1=false;
					break;
				}
			}
			if(b1==true){
				setOut.add(new HashSet<String>(set1));
			}
		}
		
		//outputting result
		return setOut;
	}
	
	private Set<Set<String>> powerSetFromExclusiveSets(HashSet<HashSet<String>> setExclusiveSets){
		
		//lst1 = current list of sets
		//set3 = current set of predictors
		//setOut = output
		//i1 = size of output
		//set1 = set with empty value added
		
		HashSet<String> set1;
		int i1;
		ArrayList<Set<String>> lst1;
		Set<List<String>> set3;
		Set<Set<String>> setOut;
		
		i1 = 1;
		lst1 = new ArrayList<Set<String>>();
		for(HashSet<String> set:setExclusiveSets){
			i1=i1*(set.size()+1);
			set1 = new HashSet<String>(set);
			set1.add("null");
			lst1.add(set1);
		}
		set3 = Sets.cartesianProduct(lst1);
		setOut = new HashSet<Set<String>>(i1);		
		for(List<String> lst:set3){
			set1 = new HashSet<String>(lst);
			set1.remove("null");
			setOut.add(set1);
		}
		return setOut;
		
		/*
		setPower = Sets.powerSet(setExclusiveSets);
		for(Set<HashSet<String>> set1:setPower){
			lst1 = new ArrayList<Set<String>>();
			for(Set<String> set2:set1){
				lst1.add(set2);
			}
			set3 = Sets.cartesianProduct(lst1);
			for(List<String> lst:set3){
				setOut.add(new HashSet<String>(lst));
			}
			
			//*********************
			System.out.println(setOut.size());
			//*********************
			
		}
		setOut.add(new HashSet<String>());
		return setOut;*/
	}
	
	/**
	 * Returns best model
	 * @param iPredictors Number of predictors
	 * @return Best model.
	 */
	public ModelSummary getBestModel(int iPredictors){
		return mapBestModels.get(iPredictors);
	}
	
	/**
	 * Summary of model
	 * @author jladau
	 */
	public class ModelSummary{
		
		/**R^2 for model**/
		public double dR2 = Double.NaN;
		
		/**PRESS for model**/
		public double dPRESS = Double.POSITIVE_INFINITY;
		
		/**Adjusted R^2 for model**/
		public double dR2Adjusted = Double.NaN;
		
		/**Coefficient estimates: keys are predictors, values are estimates**/
		public HashMap<String,Double> mapCoefficientEstimates = null;
		
		/**Response variable**/
		public String sResponse = null;	
		
		/**Response variable transform (data are assumed to already be transformed)**/
		public String sResponseTransform = null;
		
		/**Response units**/
		public String sUnits;
		
		/**Flag for whether best overall model**/
		public boolean bBestModelOverall = false;
		
		/**Total sum of squares**/
		public double dTSS;
		
		/**LMG variable importance estimates**/
		public HashMap<String,Double> mapLMG;
		
		/**Standardized coefficient estimates**/
		public HashMap<String,Double> mapStandardizedCoefficients;
		
		/**Number of observations**/
		public int iObservations = -1;
		
		public String toString(){
			
			//sbl1 = output
			//b1 = flag for whether first entry
			
			boolean b1;
			StringBuilder sbl1;
			
			sbl1 = new StringBuilder();
			sbl1.append(mapCoefficientEstimates.size()-1);
			sbl1.append("," + (1.-dPRESS/dTSS));
			sbl1.append("," + dPRESS);
			sbl1.append("," + dR2);
			sbl1.append("," + dR2Adjusted);
			sbl1.append("," + bBestModelOverall);
			sbl1.append("," + sResponse);
			sbl1.append("," + sResponseTransform);
			sbl1.append("," + sUnits);
			sbl1.append(",(Intercept)");
			for(String s:mapCoefficientEstimates.keySet()){
				if(!s.equals("(Intercept)")){
					sbl1.append(";" + (new File(s)).getName());
				}
			}
			sbl1.append("," + "(Intercept):" + mapCoefficientEstimates.get("(Intercept)"));
			for(String s:mapCoefficientEstimates.keySet()){
				if(!s.equals("(Intercept)")){
					sbl1.append(";" + s + ":" + mapCoefficientEstimates.get(s));
				}
			}
			sbl1.append(",");
			if(mapLMG.size()==0){
				sbl1.append("na");
			}else{
				b1 = true;
				for(String s:mapLMG.keySet()){
					if(b1==false){
						sbl1.append(";");
					}
					b1=false;
					sbl1.append((new File(s)).getName() + "=" + mapLMG.get(s));
				}
			}
			sbl1.append(",");
			if(mapStandardizedCoefficients.size()==0){
				sbl1.append("na");
			}else{
				b1 = true;
				for(String s:mapStandardizedCoefficients.keySet()){
					if(b1==false){
						sbl1.append(";");
					}
					b1=false;
					sbl1.append((new File(s)).getName() + "=" + mapStandardizedCoefficients.get(s));
				}
			}
			sbl1.append(",");
			sbl1.append(iObservations);
			return sbl1.toString();
		}
	}
}
