package edu.ucsf.AppendResponseVarsToRasterList;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Appends response variables to raster list associated with each predictors.
 * @author jladau
 */
public class AppendResponseVarsToRasterListLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datRasters = raster list data file
		//datModels = selected models
		//mapResponseVars(sPredictor) = list of response variables associated with each predictor
		//sPredictors = predictors
		//sResponse = current response variable
		//bDateSpecific = flag for whether predictors should be considered date specific		
		//sPredictor = current predictor
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO datRasters;
		DataIO datModels;
		HashMultimap<String,String> mapResponseVars;
		String sPredictors;
		String sResponse;
		boolean bDateSpecific;
		String sPredictor;
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		datRasters = new DataIO(arg1.getValueString("sRasterListPath"));
		datModels = new DataIO(arg1.getValueString("sSelectedModelsPath"));
		mapResponseVars = HashMultimap.create();
		bDateSpecific = arg1.getValueBoolean("bDateSpecific");
		
		//loading response variables associated with each response variable
		for(int i=1;i<datModels.iRows;i++){
			sResponse=datModels.getString(i, "RESPONSE_VAR");
			sPredictors=datModels.getString(i, "MODEL");
			for(String s:sPredictors.split(";")){
				if(s.startsWith("(Intercept)")){
					continue;
				}
				if(bDateSpecific==false){
					mapResponseVars.put(Joiner.on(":").join(Arrays.copyOfRange(s.split(":"),0,3)), sResponse);
				}else{
					mapResponseVars.put(Joiner.on(":").join(Arrays.copyOfRange(s.split(":"),0,4)),sResponse);
				}
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>(datRasters.iRows);
		lstOut.add(Joiner.on(",").join(datRasters.getRow(0)) + ",RESPONSE_VARIABLES");
		
		//writing output
		for(int i=1;i<datRasters.iRows;i++){
			sPredictor = datRasters.getString(i, "RASTER_PATH") + ":" + datRasters.getString(i, "VARIABLE") + ":" + datRasters.getString(i, "TRANSFORM");
			if(bDateSpecific){
				sPredictor += ":" + datRasters.getString(i, "TRAINING_DATE");
			}
			if(mapResponseVars.containsKey(sPredictor)){
				lstOut.add(
						Joiner.on(",").join(datRasters.getRow(i))
						+ "," + Joiner.on(":").join(mapResponseVars.get(sPredictor)));
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}