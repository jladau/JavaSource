package edu.ucsf.ModelSampleProjector;

import java.util.ArrayList;
import edu.ucsf.base.LinearModel;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.sdm.ProjectionData;
import edu.ucsf.sdm.ProjectionData.ProjectionPoint;
import edu.ucsf.sdm.ProjectionData_Differences;
import edu.ucsf.sdm.Projector;
import edu.ucsf.sdm.ResponseTransform;
import edu.ucsf.sdm.TrainingData;
import edu.ucsf.sdm.TrainingData_Differences;

/**
 * Projects models to sampled locations.
 * @author Joshua Ladau, jladau@gmail.com
 */

public class ModelSampleProjectorLauncher {

	/**
	 * Writes file giving observed values and predicted values for each sample.
	 * @param Pass arguments as --{argument name}={argument value}. Use "-h" or "--help" as first argument to see list of possible arguments.
	 **/
	public static void main(String[] rgsArgs) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//ncrRaster = raster
		//datModels = selected models
		//itr1 = time iterator 
		//trn1 = training data	
		//lnm1 = linear model object
		//prj1 = projector
		//plyMask = masking polygon (if requested)
		//prj1 = current projector
		//prd1 = projection data
		//sResponseVar = response variable
		//lstOut = sample predictions output
		//usg1 = usage object
		//d2 = current projection
		//lstProjectionPoints = list of projection points
		//d3 = model performance
		
		double d2;
		Usage usg1;
		SphericalMultiPolygon plyMask = null;
		ArgumentIO arg1;
		BiomIO bio1;
		DataIO datModels;
		TrainingData trn1 = null;
		LinearModel lnm1;
		ProjectionData prd1 = null;
		Projector prj1;
		String sResponseVar;
		ArrayList<String> lstOut;
		ArrayList<ProjectionPoint> lstProjectionPoints;
		double d3;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
				"BiomIO",
				"TrainingData",
				"ProjectionData",
				"sOutputPath",
				"sSelectedModelsPath",
				"bApplyInverse"
				});
		usg1.printUsage(rgsArgs);
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading selected model
		datModels = new DataIO(arg1.getValueString("sSelectedModelsPath"));
		
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
		if(arg1.getValueBoolean("bProjectDifferences")){
			trn1 = new TrainingData_Differences(trn1, arg1.getValueString("sResponseDifferenceTransform"), bio1);
			if(arg1.getValueBoolean("bApplyInverse")){
				throw new Exception ("Inverse response transformation cannot be applied in conjuction with difference projections. Set bApplyInverse equal to false.");
			}
		}
		
		//loading projection data
		prd1 = new ProjectionData(
				trn1,
				new DataIO(arg1.getValueString("sRasterListPath")),
				arg1.containsArgument("dProjectionVert") ? arg1.getValueDouble("dProjectionVert") : GeospatialRaster.NULL_VERT);
		if(arg1.getValueBoolean("bProjectDifferences")){
			prd1 = new ProjectionData_Differences(prd1, arg1.getValueString("sResponseDifferenceTransform"));
		}
		
		//initializing output
		lstOut = new ArrayList<String>((trn1.getDataTable().columnKeySet().size()*trn1.getDataTable().columnKeySet().size()/2+trn1.getDataTable().columnKeySet().size())*datModels.iRows+10);
		if(arg1.getValueBoolean("bProjectDifferences")){
			lstOut.add("SAMPLE_ID_1,SAMPLE_ID_2,VALUE_TYPE,DATE_ALIAS_1,DATE_ALIAS_2,RESPONSE_VAR,VALUE,R2_ADJUSTED");
		}else{
			lstOut.add("SAMPLE_ID,VALUE_TYPE,DATE_ALIAS,RESPONSE_VAR,VALUE,CV_R2");	
		}
		
		//looping through input lines
		for(int iInputLine=1;iInputLine<datModels.iRows;iInputLine++){
		
			//loading response var
			sResponseVar=datModels.getString(iInputLine, "RESPONSE_VAR");
			
			//looping through models
			System.out.println("Projecting " + sResponseVar + "...");
			
			//checking if model contains more than intercept
			if(datModels.getString(iInputLine,"MODEL").split(";").length==1){
				continue;
			}
			
			//loading and fitting model
			lnm1 = new LinearModel(trn1.getDataTable(), sResponseVar, trn1.getPredictors(sResponseVar));
			lnm1.fitModel(trn1.getPredictors(sResponseVar));
			d3 = lnm1.findAdjustedRSquared();
			
			//initializing projector
			prj1 = new Projector(
					datModels.getString(iInputLine, "RESPONSE_VAR"),
					datModels.getString(iInputLine,"RESPONSE_VAR_UNITS"),
					datModels.getString(iInputLine, "RESPONSE_VAR"),
					"area: mean",
					plyMask,
					lnm1,
					prd1,
					datModels.getString(iInputLine,"RESPONSE_VAR_TRANSFORM"),
					arg1.getValueBoolean("bApplyInverse"));

			for(String s:trn1.getDataTable().columnKeySet()){
				d2 = trn1.getDataTable().get(sResponseVar, s);
				if(arg1.getValueBoolean("bApplyInverse")){	
					d2=ResponseTransform.applyInverse(d2, sResponseVar, datModels.getString(iInputLine,"RESPONSE_VAR_TRANSFORM"));
				}
				if(Double.isNaN(d2)){
					continue;
				}
				
				if(arg1.getValueBoolean("bProjectDifferences")){
					lstOut.add(
						s
						+ ",observation,current,current" 
						+ "," + sResponseVar
						+ "," + d2
						+ "," + d3
					);
				}else{
					lstOut.add(
						s
						+ ",observation,current" 
						+ "," + sResponseVar
						+ "," + d2
						+ "," + d3
					);
				}
			}
				
			//loading projection points
			lstProjectionPoints = prd1.loadAllPossibleProjectionPoints(sResponseVar, bio1);
			
			//looping through projection points
			for(ProjectionPoint ppt1:lstProjectionPoints){
			
				//writing projection
				d2 = prj1.readPrediction(prd1.getProjectionData(ppt1));
				lstOut.add(
						ppt1.sID
						+ ",prediction," + ppt1.sDateAlias + ((prd1.getProjectionVert()==GeospatialRaster.NULL_VERT) ? "" : ":" + prd1.getProjectionVert())
						+ "," + sResponseVar 
						+ "," + d2
						+ "," + d3);
			}
		}
	
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(prd1.getPrintableData(trn1, bio1), arg1.getValueString("sOutputPath").replace("csv", "data.csv"));
		prd1.close();
		System.out.println("Done.");		
	}
}