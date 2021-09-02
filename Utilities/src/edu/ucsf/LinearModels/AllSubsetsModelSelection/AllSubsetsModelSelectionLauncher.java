package edu.ucsf.LinearModels.AllSubsetsModelSelection;

import java.util.ArrayList;
import java.util.HashSet;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.AllSubsetsModelSelection;
import edu.ucsf.base.LinearModel;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Runs all-subsets model selection. Models are linear and are to predict response variables for a set of samples using predictors from a set of rasters. Models are vetted using cross validation.
 * @author Joshua Ladau, jladau@gmail.com
 */

public class AllSubsetsModelSelectionLauncher {
	 
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
		//iCounter = iteration counter
		//setPredictors = predictors
		
		HashSet<String> setPredictors;
		AllSubsetsModelSelection msl1 = null;
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		int iCounter;
			
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//updating maximum number of predictors
		if(!arg1.containsArgument("iMaxPredictors")){
			arg1.updateArgument("iMaxPredictors", arg1.getValueArrayList("lstPredictors").size());
		}
		
		//updating flag for whether to output only best model
		if(!arg1.containsArgument("bOnlyBestOverallModel")){
			arg1.updateArgument("bOnlyBestOverallModel", true);
		}
		
		//updating maximum variance inflation factor
		if(!arg1.containsArgument("dMaxVIF")){
			arg1.updateArgument("dMaxVIF", 5.0);
		}
		
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
		setPredictors = new HashSet<String>(arg1.getValueArrayList("lstPredictors"));
		for(String s:arg1.getValueArrayList("lstResponseVars")){
				
			//running model selection
			System.out.println("Running model selection for " + s + " (response variable " + iCounter + " of " + arg1.getValueArrayList("lstResponseVars").size() + ")...");
			iCounter++;
			
			//loading model selection
			msl1 = new AllSubsetsModelSelection(
					setPredictors,
					s,
					"identity",
					"NA",
					new LinearModel(
							loadTable(new DataIO(arg1.getValueString("sDataPath"))),
							s, 
							setPredictors),
					arg1.getValueInt("iMaxPredictors"), 
					arg1.getValueDouble("dMaxVIF"),
					null,
					null,
					null);
			
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
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
			
		//terminating
		System.out.println("Done.");
	}
	
	private static HashBasedTable<String,String,Double> loadTable(DataIO dat1){
		
		//tbl1 = output
		
		HashBasedTable<String,String,Double> tbl1;
		
		tbl1 = HashBasedTable.create();
		for(int j=0;j<dat1.iCols;j++){
			if(!dat1.getString(0, j).equals("SAMPLE_ID")){
				for(int i=1;i<dat1.iRows;i++){
					tbl1.put(dat1.getString(0,j), dat1.getString(i, "SAMPLE_ID"), dat1.getDouble(i, j));
				}
			}
		}
		return tbl1;
	}	
}