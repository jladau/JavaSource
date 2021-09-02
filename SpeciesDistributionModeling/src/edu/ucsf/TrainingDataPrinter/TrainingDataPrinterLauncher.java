package edu.ucsf.TrainingDataPrinter;

import java.util.ArrayList;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.sdm.TrainingData;

/**
 * Runs all-subsets model selection. Models are linear and are to predict response variables for a set of samples using predictors from a set of rasters. Models are vetted using cross validation.
 * @author Joshua Ladau, jladau@gmail.com
 */

public class TrainingDataPrinterLauncher {
	 
	
	/**
	 * Writes file giving training data.
	 **/
	
	public static void main(String rgsArgs[]) throws Exception{
		 
		//arg1 = arguments
		//bio1 = biom object
		//trn1 = training data
		//lstOut = current output
		//datRasterLists = list of raster list paths
		//sRasterListPath = current raster list path
		
		String sRasterListPath;
		TrainingData trn1 = null;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		DataIO datRasterLists;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading sample location data
		System.out.println("Loading BIOM file...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("SAMPLE_ID,PREDICTOR,VALUE");
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//looping through sets of raster lists
		datRasterLists = new DataIO(arg1.getValueString("sRasterListPath"));
		for(int i=0;i<datRasterLists.iRows;i++){
		
			//loading raster list path
			sRasterListPath=datRasterLists.getString(i, 0);
			
			//Updating progress
			System.out.println("Loading data for " + sRasterListPath + "...");
			
			//loading training data
			trn1 = new TrainingData(
					bio1,
					new DataIO(arg1.getValueString("sRasterListPath")),
					new DataIO(arg1.getValueString("sResponseVarsListPath")),
					(arg1.containsArgument("sTrainingDatesPath")) ? new DataIO(arg1.getValueString("sTrainingDatesPath")) : null,
					(arg1.containsArgument("dTrainingVert")) ? arg1.getValueDouble("dTrainingVert") : GeospatialRaster.NULL_VERT,
					arg1.getValueString("sResponseTransform"));	
			
			//formatting data for output
			lstOut = formatData(trn1);

			//appending output
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"), true);	
		}
			
		//terminating
		System.out.println("Done.");
	 }
	
	 private static ArrayList<String> formatData(TrainingData trn1){
		 
		 //lstOut = output data
		 
		 ArrayList<String> lstOut;
		 
		 lstOut = new ArrayList<String>(trn1.getDataTable().size());
		 for(String sSample:trn1.getDataTable().columnKeySet()){
			 for(String sPred:trn1.getPredictors()){
				 lstOut.add(sSample + "," + sPred + "," + trn1.getDataTable().get(sPred, sSample));
			 }
		 }
		 return lstOut;
	 }
}