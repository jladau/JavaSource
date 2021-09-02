package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import edu.ucsf.io.DataIO;

/**
 * Parses inputs for niche modeling.
 * @author jladau
 */


public class SDMParser {

	/**
	 * Loads a list of climatology lengths from end dates
	 * @param rgsEndDates End dates to consider.
	 * @param iIntervalLength Length of climatologies (in years).
	 * @param rgsDateRange Range of allowable dates.
	 * @return List of climatologies in the form YYYY-MM-DD--YYYY-MM-DD
	 */
	public static ArrayList<String> loadClimatologyProjectionIntervals(String[] rgsEndDates, int iIntervalLength, String rgsDateRange[]){
		
		//lst1 = output
		//tim1 = current start time
		//tim2 = current end time
		//rgt1 = date range
		
		ArrayList<String> lst1;
		LocalDate tim1;
		LocalDate tim2;
		LocalDate[] rgt1;
		
		rgt1 = new LocalDate[]{new LocalDate(rgsDateRange[0]), new LocalDate(rgsDateRange[1])};
		lst1 = new ArrayList<String>();
		for(int i=0;i<rgsEndDates.length;i++){
			tim1 = new LocalDate(rgsEndDates[i]);
			tim2 = tim1.minusYears(iIntervalLength).plusDays(1);
			if(!tim2.isBefore(rgt1[0]) && !tim1.isAfter(rgt1[1])){
				lst1.add(tim2 + "--" + tim1);
			}
		}
		return lst1;
	}
	
	/**
	 * Loads predictors from model selection output.
	 * @param datModels Model selection output.
	 * @return Array with path to raster and variables to use.
	 */
	//TODO write unit test
	public static String[][] loadTrainingPredictors(DataIO datModels, int iRow){
		
		//rgs1 = output
		//rgs2 = model in split format
		//rgs3 = current predictor in split format
		//i1 = counter
		
		String rgs1[][] = null;
		String rgs2[];
		String rgs3[];
		int i1;
		
		rgs2 = datModels.getString(iRow,"MODEL").split(";");
		rgs1 = new String[rgs2.length][3];
		rgs1[0]=new String[]{"Raster_Path","Variable","Transform"};
		i1 = 1;
		for(int k=0;k<rgs2.length;k++){
			rgs3 = rgs2[k].split(":");
			if(!rgs3[0].equals("(Intercept)")){
				rgs1[i1]=rgs3;
				i1++;
			}
		}
		return rgs1;
	}
	
	
	/**
	 * Loads predictors from model selection output.
	 * @param datModels Model selection output.
	 * @param sResponseVar Response variable (sans transform).
	 * @return Array with path to raster and variables to use.
	 */
	/*
	public static String[][] loadTrainingPredictors(DataIO datModels, String sResponseVar){
		
		//rgs1 = output
		//rgs2 = model in split format
		//rgs3 = current predictor in split format
		//i1 = counter
		//s1 = current model
		
		String rgs1[][] = null;
		String rgs2[];
		String rgs3[];
		int i1;
		String s1;
		
		for(int i=1;i<datModels.iRows;i++){
			s1 = datModels.getString(i, "RESPONSE_VAR");
			if(s1.equals(sResponseVar)){
				rgs2 = datModels.getString(i,"MODEL").split(";");
				rgs1 = new String[rgs2.length-1][2];
				i1 = 0;
				for(int k=0;k<rgs2.length;k++){
					rgs3 = rgs2[k].split(":");
					if(!rgs3[0].equals("(Intercept)")){
						
						//*********************
						//if(rgs3[0]==null){
						//	System.out.println("HERE");
						//}
						//*********************
						
						rgs1[i1][0]=rgs3[0];
						rgs1[i1][1]=rgs3[1];
						i1++;
					}
				}
				break;
			}
		}
		return rgs1;
	}
	*/
	
	/**
	 * Returns a map between the training rasters/variables and the projection rasters/variables, which may be for a different climatology
	 * @param datModels Model selection output.
	 * @param iRow Modeling output row.
	 * @param sProjectionInterval Interval to project to. If null, then same interval as training data will be used.
	 * @return Map from training variable names to projection variable names.
	 */
	public static HashMap<String,String> loadClimatologyProjectionMap(DataIO datModels, int iRow, String sProjectionInterval){
		
		//map1 = predictors map
		//s2 = old date being replaced
		//rgsTrainingPredictors = training predictors
		
		HashMap<String,String> map1;
		String s2;
		String rgsTrainingPredictors[][];
		
		rgsTrainingPredictors = loadTrainingPredictors(datModels, iRow);
		map1 = new HashMap<String,String>(rgsTrainingPredictors.length);
		s2 = getTrainingDate(datModels, iRow);
		for(String[] rgs1:rgsTrainingPredictors){
			if(sProjectionInterval==null){
					map1.put(rgs1[0] + ":" + rgs1[1], rgs1[0] + ":" + rgs1[1]);
			}else{		
				map1.put(rgs1[0] + ":" + rgs1[1], rgs1[0] + ":" + rgs1[1].replace(s2, sProjectionInterval));
			}
		}
		return map1;
	}
	
	/** 
	 * Gets climatology date from a model, where raster variables are in the format <VarName>YYYY-MM-DD--YYYY-MM-DD
	 * @param datModels Model selection output.
	 * @param iRow Row to use.
	 * @return Climatology, in the format YYYY-MM-DD--YYYY-MM-DD
	 */
	public static String getTrainingDate(DataIO datModels, int iRow){
		return StringUtils.right(loadTrainingPredictors(datModels,iRow)[0][1],22);
	}
	
	/** 
	 * Gets climatology date from variable formatted as <VarName>_YYYY-MM-DD--YYYY-MM-DD
	 * @param sVariable Variable name
	 * @return Climatology, in the format YYYY-MM-DD--YYYY-MM-DD
	 */
	//TODO write unit test
	public static String getTrainingDate(String sVariable){
		return StringUtils.right(sVariable,22);
	}
	
	/** 
	 * Gets name of variable with climatology stripped <VarName>_YYYY-MM-DD--YYYY-MM-DD
	 * @param sVariable Variable
	 * @return Variable name
	 */
	//TODO write unit test
	public static String stripClimatology(String sVariable){
		return sVariable.replace("_" + StringUtils.right(sVariable,22),"");
	}
		
	
}
