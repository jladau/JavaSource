package edu.ucsf.ModelSelector;

import java.io.File;
import java.util.ArrayList;
import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.AllSubsetsModelSelection;
import edu.ucsf.base.ClusterIterator;
import edu.ucsf.base.LinearModel;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.sdm.TrainingData;

/**
 * Runs all-subsets model selection. Models are linear and are to predict response variables for a set of samples using predictors from a set of rasters. Models are vetted using cross validation.
 * @author Joshua Ladau, jladau@gmail.com
 */

public class ModelSelectorLauncher {
	 
	/**
	 * Writes file giving the best model for each number of predictors. In output file, each row represents a selected model. Columns give: 
	 * <p>
	 * <ol>
	 * <li> NUMBER_PREDICTORS = number of predictors, 
	 * <li> CV_R2 = cross validation r^2 (1-PRESS/TSS) 
	 * <li> PRESS = PRESS statistic
	 * <li> R2 = r^2
	 * <li> R2_ADJUSTED = adjusted r^2
	 * <li> BEST_OVERALL_MODEL = flag for whether the model is the best for the specified response variable
	 * <li> RESPONSE_VAR = response variable
	 * <li> RESPONSE_VAR_TRANSFORM = transform used on response variable
	 * <li> RESPONSE_VAR_UNITS = units of response variable
	 * <li> PREDICTORS = predictors
	 * <li> MODEL = parameter estimates and paths to rasters with each predictor
	 * <li> VARIABLE_IMPORTANCE_LMG = variable importance measured by lmg
	 * <li> VARIABLE_IMPORTANCE_STANDARDIZED_COEFFICIENTS = variable importance measured by standardized coefficients
	 * <li> TRAINING_DATA_VERT = elevation used for training data if different from date in BIOM file
	 * </ol>
	 * @param Pass arguments as --{argument name}={argument value}. Use "-h" or "--help" as first argument to see list of possible arguments.
	 **/
	
	public static void main(String rgsArgs[]) throws Exception{
		 
		//arg1 = arguments
		//bio1 = biom object
		//msl1 = model selection object
		//lstOut = output
		//trn1 = training data
		//iCounter = iteration counter
		//cit1 = cluster iterator
		//usg1 = usage object
		
		TrainingData trn1 = null;
		AllSubsetsModelSelection msl1 = null;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		int iCounter;
		ClusterIterator cit1;
		Usage usg1;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
				"BiomIO",
				"ClusterIterator",
				"TrainingData",
				"sOutputPath",
				"bPrintData",
				"iMaxPredictors",
				"bOnlyBestOverallModel",
				"dMaxVIF"});
		usg1.printUsage(rgsArgs);
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		
		//loading sample location data
		System.out.println("Loading BIOM file...");
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		
		//loading training data
		trn1 = new TrainingData(
				bio1,
				new DataIO(arg1.getValueString("sRasterListPath")),
				new DataIO(arg1.getValueString("sResponseVarsListPath")),
				(arg1.containsArgument("sTrainingDatesPath")) ? new DataIO(arg1.getValueString("sTrainingDatesPath")) : null,
				(arg1.containsArgument("dTrainingVert")) ? arg1.getValueDouble("dTrainingVert") : GeospatialRaster.NULL_VERT,
				arg1.getValueString("sResponseTransform"));		

		//updating maximum number of predictors
		if(!arg1.containsArgument("iMaxPredictors")){
			arg1.updateArgument("iMaxPredictors", trn1.getPredictors().size());
		}
		
		//updating flag for whether to output only best model
		if(!arg1.containsArgument("bOnlyBestOverallModel")){
			arg1.updateArgument("bOnlyBestOverallModel", true);
		}
		
		//updating maximum variance inflation factor
		if(!arg1.containsArgument("dMaxVIF")){
			arg1.updateArgument("dMaxVIF", 5.0);
		}
		
		//initializing cluster iterator
		cit1 = new ClusterIterator(
				arg1.getValueInt("iTaskID"),
				arg1.getValueInt("iTotalTasks"));
		
		//running model selection
		lstOut = new ArrayList<String>();
		lstOut.add(
				"NUMBER_PREDICTORS,"
				+ "CV_R2,"
				+ "PRESS,"
				+ "R2,R2_ADJUSTED,"
				+ "BEST_OVERALL_MODEL,"
				+ "RESPONSE_VAR,"
				+ "RESPONSE_VAR_TRANSFORM,"
				+ "RESPONSE_VAR_UNITS,"
				+ "PREDICTORS,"
				+ "MODEL,"
				+ "VARIABLE_IMPORTANCE_LMG,"
				+ "VARIABLE_IMPORTANCE_STANDARDIZED_COEFFICIENTS,"
				+ "NUMBER_OF_SAMPLES,"
				+ "TRAINING_DATA_VERT");
		iCounter = 1;
		for(String s:trn1.getResponseVars()){
			
			//running model selection
			System.out.println("Running model selection for " + s + " (taxon " + iCounter + " of " + trn1.getResponseVars().size() + ")...");
			iCounter++;
			
			//checking if continue
			cit1.next();
			if(!cit1.bInclude){
				continue;
			}
			
			//loading model selection
			msl1 = new AllSubsetsModelSelection(
					trn1.getPredictors(s),
					s,
					trn1.getResponseTransform(s),
					trn1.getUnits(s),
					new LinearModel(trn1.getDataTable(),s, trn1.getPredictors(s)),
					arg1.getValueInt("iMaxPredictors"), 
					arg1.getValueDouble("dMaxVIF"),
					trn1.getNestedPredictors(),
					trn1.getDoNotCheckVIF(),
					trn1.getExclusivePredictors(s));
			
			//saving results
			for(int k=0;k<=arg1.getValueInt("iMaxPredictors");k++){
				if(arg1.getValueBoolean("bOnlyBestOverallModel")==false || (arg1.getValueBoolean("bOnlyBestOverallModel")==true && msl1.getBestModel(k).bBestModelOverall)==true){
					lstOut.add(
							(msl1.getBestModel(k).toString() 
							+ "," + ((arg1.containsArgument("dTrainingVert")) ? arg1.getValueDouble("dTrainingVert") : Double.NaN)).replace(",null,",",na,").replace(",NaN",",na"));
				}
			}	
		}
		
		//outputting data table
		if(!arg1.containsArgument("bPrintData")){
			arg1.updateArgument("bPrintData", false);
		}
		if(arg1.getValueInt("iTaskID")==-9999 || arg1.getValueInt("iTotalTasks")==-9999){
			if(arg1.getValueBoolean("bPrintData")){
				DataIO.writeToFile(printData(trn1.getDataTable()), arg1.getValueString("sOutputPath").replace(".csv", ".data.csv"));
			}
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		}else{
			if(arg1.getValueInt("iTaskID")==arg1.getValueInt("iTotalTasks")){
				if(arg1.getValueBoolean("bPrintData")){
					DataIO.writeToFile(printData(trn1.getDataTable()), arg1.getValueString("sOutputPath").replace(".csv", ".data.csv"));
				}
			}
			DataIO.writeToFileWithCompletionFile(lstOut, arg1.getValueString("sOutputPath"), arg1.getValueInt("iTaskID"));
		}
			
		//terminating
		System.out.println("Done.");
	 }
	
	 private static ArrayList<String> printData(HashBasedTable<String,String,Double> tbl1){
		 
		 //lstOut = output data
		 //sbl1 = current line
		 //rgs1 = list of row ids
		 //rgs2 = list of headers
		 
		 ArrayList<String> lstOut;
		 StringBuilder sbl1;
		 String rgs1[];
		 String rgs2[];
		 
		 lstOut = new ArrayList<String>();
		 rgs1 = tbl1.rowKeySet().toArray(new String[tbl1.rowKeySet().size()]);
		 rgs2 = new String[rgs1.length];
		 for(int i=0;i<rgs1.length;i++){
			 rgs2[i] = (new File(rgs1[i])).getName();
		 }
		 lstOut.add("SampleID" + "," + Joiner.on(",").join(rgs2));
		 for(String s:tbl1.columnKeySet()){
			 sbl1 = new StringBuilder();
			 sbl1.append(s);
			 for(int i=0;i<rgs1.length;i++){
				 sbl1.append("," + tbl1.get(rgs1[i], s));
			 }
			 lstOut.add(sbl1.toString());
		 }
		 return lstOut;
	 }
}
