package edu.ucsf.LinearModels.ProjectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.LinearModel;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.sdm.ResponseTransform;

/**
 * Projects models to sampled locations.
 * @author Joshua Ladau, jladau@gmail.com
 */

public class ProjectModelLauncher {

	/**
	 * Writes file giving observed values and predicted values for each sample.
	 * @param Pass arguments as --{argument name}={argument value}. Use "-h" or "--help" as first argument to see list of possible arguments.
	 **/
	public static void main(String[] rgsArgs) throws Exception{
		
		//datProjectionPoints = projection points
		//mapPredictors = projection points
		//arg1 = arguments
		//ncrRaster = raster
		//datModels = selected models
		//datData = data
		//itr1 = time iterator 
		//lnm1 = linear model object
		//prj1 = projector
		//plyMask = masking polygon (if requested)
		//prj1 = current projector
		//prd1 = projection data
		//sResponseVar = response variable
		//lstOut = sample predictions output
		//d2 = current projection
		//lstProjectionPoints = list of projection points
		//tbl1 = data table
		//setPredictors = set of predictors
		//rgs1 = set of predictors
		
		HashMap<String,Double> mapPredictors;
		DataIO datProjectionPoints;
		String[] rgs1;
		HashBasedTable<String,String,Double> tbl1;
		HashSet<String> setPredictors;
		double d2;
		ArgumentIO arg1;
		DataIO datModels;
		DataIO datData;
		LinearModel lnm1;
		String sResponseVar;
		ArrayList<String> lstOut;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading selected model
		datModels = new DataIO(arg1.getValueString("sSelectedModelsPath"));
		
		//loading projection data
		datProjectionPoints = new DataIO(arg1.getValueString("sProjectionPointsPath"));
		
		//loading data
		datData = new DataIO(arg1.getValueString("sDataPath"));
		
		//initializing output
		lstOut = new ArrayList<String>(datProjectionPoints.iRows*datModels.iRows);
		lstOut.add("SAMPLE_ID,RESPONSE_VAR,PREDICTORS,PREDICTED_VALUE,OBSERVED_VALUE");	
		
		//loading data table
		tbl1 = loadTable(datData);
		
		//looping through input lines
		for(int iInputLine=1;iInputLine<datModels.iRows;iInputLine++){
		
			//loading response var
			sResponseVar=datModels.getString(iInputLine, "RESPONSE_VAR");
			
			//looping through models
			System.out.println("Projecting " + sResponseVar + "...");
			
			//checking if model contains more than intercept
			rgs1 = datModels.getString(iInputLine,"PREDICTORS").split(";");
			if(rgs1.length==1){
				continue;
			}
			
			//loading current set of predictors
			setPredictors = new HashSet<String>(rgs1.length);
			for(String s:rgs1){
				if(!s.equals("(Intercept)")){
					setPredictors.add(s);
				}
			}
			
			//loading and fitting model
			lnm1 = new LinearModel(
					tbl1,
					sResponseVar, 
					setPredictors);
			lnm1.fitModel(setPredictors);
			
			//looping through projection points
			for(int i=1;i<datProjectionPoints.iRows;i++){
				
				mapPredictors = new HashMap<String,Double>(datProjectionPoints.iCols);
				for(String s:setPredictors){
					mapPredictors.put(s, datProjectionPoints.getDouble(i, s));
				}
				
				//writing projection
				d2 = lnm1.findPrediction(mapPredictors);
				lstOut.add(
						datProjectionPoints.getString(i, "SAMPLE_ID") + "," +
						sResponseVar + "," +
						datModels.getString(iInputLine,"PREDICTORS") + "," +
						d2 + "," +
						datProjectionPoints.getString(i, sResponseVar));
			}
		}
	
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
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