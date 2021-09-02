package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;

import edu.ucsf.io.DataIO;

public class DistanceMatrixModelSelection{
	
	/**Map from number of predictors to best model for number of predictors**/
	public HashMap<String,CrossValidationResults> mapCV;
	
	public DistanceMatrixModelSelection(DataIO datMatrices, String sVertexField1, String sVertexField2, String rgsCandidatePredictors[], String sResponse, String sPriorityPredictor) throws Exception{
		
		//crv1 = cross validation object
		//cvr1 = cross validation results
		//setPredictors = set of predictors
		//setPower = power set
		//i1 = counter
		//dms0 = single-predictor model selection object if priority predictor is specified
		
		CrossValidationResults cvr1;
		Set<Set<String>> setPower;
		DistanceMatrixRegressionCrossValidation crv1;
		Set<String> setPredictors;
		int i1;
		DistanceMatrixModelSelection dms0 = null;
		
		//initializing variables
		crv1 = new DistanceMatrixRegressionCrossValidation(datMatrices, sVertexField1, sVertexField2);
		setPredictors = new HashSet<String>();
		for(String s:rgsCandidatePredictors){
			setPredictors.add(s);
		}
		
		//looping through subsets of candidate predictors and loading cross validation results
		setPower = Sets.powerSet(setPredictors);
		mapCV = new HashMap<String,CrossValidationResults>();
		i1=0;
		for(Set<String> set1:setPower){
			i1++;
			System.out.println("Running model selection for model " + i1 + " (size " + set1.size() + ")...");
			if(crv1.checkVIF(set1, sResponse)){
				cvr1 = crv1.findCrossValidation(set1, sResponse);
				if(!mapCV.containsKey(Integer.toString(set1.size())) || mapCV.get(Integer.toString(set1.size())).dPearson<cvr1.dPearson){
					mapCV.put(Integer.toString(set1.size()), cvr1);
				}
			}
		}
		
		//adding priority variable if necessary
		if(sPriorityPredictor!=null){
			if(mapCV.containsKey("1")){
				for(String s:mapCV.get("1").setPredictors){
					if(!s.equals(sPriorityPredictor)){
						dms0 = new DistanceMatrixModelSelection(datMatrices, sVertexField1, sVertexField2, new String[]{sPriorityPredictor}, sResponse, null);
						mapCV.put("0.5", dms0.mapCV.get("1"));
					}
				}
			}
		}
	}

	public ArrayList<String> print(){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = new ArrayList<String>();
		lst1.add(mapCV.get("1").toStringHeader());
		for(String s:mapCV.keySet()){
			lst1.add(s + "," + mapCV.get(s).toStringNoPredictorCount());
		}
		return lst1;
	}
	
	
	/**
	 * This object takes as an input a set of distance matrices and calculates PRESS for a specified model using cross validation.
	 * @author jladau
	 *
	 */
	public class DistanceMatrixRegressionCrossValidation {
	
		/**Keys are vertex ids, values are rows of data matrix**/
		private HashMultimap<String,Integer> mapRows;
		
		/**List of vertex pairs**/
		private ArrayList<String[]> lstVertexPairs;
		
		/**Data**/
		private DataIO datMatrices;
		
		public DistanceMatrixRegressionCrossValidation(DataIO datMatrices, String sVertexField1, String sVertexField2){
			
			//saving variables
			this.datMatrices = datMatrices;
			
			//loading row map and list of vertex pairs
			mapRows = HashMultimap.create((int) Math.sqrt(datMatrices.iRows), (int) Math.sqrt(datMatrices.iRows));
			lstVertexPairs = new ArrayList<String[]>();
			for(int i=1;i<datMatrices.iRows;i++){
				mapRows.put(datMatrices.getString(i, sVertexField1), i);
				mapRows.put(datMatrices.getString(i, sVertexField2), i);
				lstVertexPairs.add(new String[]{datMatrices.getString(i, sVertexField1), datMatrices.getString(i, sVertexField2)});
			}
		}
		
		public boolean checkVIF(Set<String> setPredictors, String sResponse){
			
			//lnm1 = linear model object
			//tblData = current data table
			
			HashBasedTable<String,String,Double> tblData = null;
			LinearModel lnm1;
			
			//loading data
			tblData = HashBasedTable.create(setPredictors.size()+1,datMatrices.iRows);
			for(int k=1;k<datMatrices.iRows;k++){
				for(String s:setPredictors){
					tblData.put(s, Integer.toString(k), datMatrices.getDouble(k, s));
				}
				tblData.put(sResponse, Integer.toString(k), datMatrices.getDouble(k, sResponse));
			}
		
			//fitting model
			try{
				lnm1 = new LinearModel(tblData, sResponse, setPredictors);
				lnm1.fitModel(setPredictors);
				return lnm1.checkVIF(5, null);
			}catch(Exception e){
				return false;
			}
		}
		
		public CrossValidationResults findCrossValidation(Set<String> setPredictors, String sResponse) throws Exception{
			
			//sVertex1 = current first vertex
			//sVertex2 = current second vertex
			//tblData = current data table
			//mapPred = values for prediction
			//lnm1 = linear model object
			//d1 = current prediction
			//mapObservations = map from pairs of vertices to observations
			//mapPredictions = map from pairs of vertices to cross-validation predictions
			
			HashMap<String,Double> mapObservations;
			HashMap<String,Double> mapPredictions;
			double d1;
			LinearModel lnm1;
			String sVertex1;
			String sVertex2;
			HashBasedTable<String,String,Double> tblData;
			HashMap<String,Double> mapPred;
	
			//initializing variables
			mapObservations = new HashMap<String,Double>(datMatrices.iRows);
			mapPredictions = new HashMap<String,Double>(datMatrices.iRows);
			
			//looping through all pairs of observations
			for(String rgs1[]:lstVertexPairs){
			
				//loading vertices
				sVertex1 = rgs1[0];
				sVertex2 = rgs1[1];
					
				//loading data
				tblData = HashBasedTable.create(setPredictors.size()+1,datMatrices.iRows);
				mapPred = new HashMap<String,Double>();
				for(int k=1;k<datMatrices.iRows;k++){
					if(!mapRows.get(sVertex1).contains(k)){
						if(!mapRows.get(sVertex2).contains(k)){
							for(String s:setPredictors){
								tblData.put(s, Integer.toString(k), datMatrices.getDouble(k, s));
							}
							tblData.put(sResponse, Integer.toString(k), datMatrices.getDouble(k, sResponse));
						}
					}
					if(mapRows.get(sVertex1).contains(k)){
						if(mapRows.get(sVertex2).contains(k)){
							for(String s:setPredictors){
								mapPred.put(s, datMatrices.getDouble(k, s));
							}
							mapPred.put(sResponse, datMatrices.getDouble(k, sResponse));
						}
					}
				}
				
				//fitting model
				lnm1 = new LinearModel(tblData, sResponse, setPredictors);
				lnm1.fitModel(setPredictors);
				
				//finding prediction
				d1 = lnm1.findPrediction(mapPred);
				
				//saving results
				mapObservations.put(sVertex1 + "," + sVertex2, mapPred.get(sResponse));
				mapPredictions.put(sVertex1 + "," + sVertex2, d1);
				
				//****************************
				//System.out.println(d1 + "," + mapPred.get(sResponse));
				//****************************
				
				
			}
			
			//*****************************
			//for(String s:mapPredictions.keySet()){
			//	System.out.println(s + "," + mapObservations.get(s) + "," + mapPredictions.get(s));
			//}
			//for(String s:setPredictors){
			//	System.out.println(s);
			//}
			//*****************************
			
			
			return new CrossValidationResults(mapObservations, mapPredictions, setPredictors, sResponse);
		}
	}
	
	public class CrossValidationResults{
		
		/**Pearson correlation**/
		public double dPearson;
		
		/**Predicted values**/
		public ArrayList<Double> lstPredictions;
		
		/**Observed values**/
		public ArrayList<Double> lstObservations;
		
		/**Map from pairs of vertices to observations**/
		public HashMap<String,Double> mapObservations;
		
		/**Map from pairs of vertices to cross-validation predictions**/
		public HashMap<String,Double> mapPredictions;
		
		/**Set of predictors**/
		public Set<String> setPredictors;
		
		/**Response variable**/
		public String sResponse;
		
		public CrossValidationResults(HashMap<String,Double> mapObservations, HashMap<String,Double> mapPredictions, Set<String> setPredictors, String sResponse){
		
			this.mapPredictions = mapPredictions;
			this.mapObservations = mapObservations;
			this.setPredictors = setPredictors;
			this.sResponse = sResponse;
			loadObservationsAndPredictions();
			dPearson = ExtendedMath.pearson(lstObservations, lstPredictions);
			if(dPearson<0){
				dPearson=0;
			}
		}
		
		private void loadObservationsAndPredictions(){
			
			lstPredictions = new ArrayList<Double>(mapPredictions.size());
			lstObservations = new ArrayList<Double>(mapObservations.size());
			for(String s:mapObservations.keySet()){
				lstObservations.add(mapObservations.get(s));
				lstPredictions.add(mapPredictions.get(s));
			}
		}
		
		public String toString(){
			return setPredictors.size() + "," + sResponse + "," + Joiner.on(";").join(setPredictors) + "," + dPearson;
		}
		
		public String toStringNoPredictorCount(){
			return sResponse + "," + Joiner.on(";").join(setPredictors) + "," + dPearson;
		}
		
		public String toStringHeader(){
			return "NUMBER_PREDICTORS,RESPONSE,PREDICTORS,CV_PEARSON";
		}
		
	}
}