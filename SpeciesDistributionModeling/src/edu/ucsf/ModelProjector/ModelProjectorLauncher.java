package edu.ucsf.ModelProjector;

import java.util.ArrayList;

import org.joda.time.LocalDate;
import edu.ucsf.base.LinearModel;
import edu.ucsf.base.LinearModel_MESS;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.TimeVert;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.geospatial.MaskGeospatialRaster;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfWriter;
import edu.ucsf.io.ShapefileIO;
import edu.ucsf.sdm.ProjectionData;
import edu.ucsf.sdm.ProjectionData_Differences;
import edu.ucsf.sdm.Projector;
import edu.ucsf.sdm.TrainingData;
import edu.ucsf.sdm.TrainingData_Differences;

/**
 * Writes map of projected values.
 * @param Pass arguments as --{argument name}={argument value}. Use "-h" or "--help" as first argument to see list of possible arguments.
 **/

public class ModelProjectorLauncher {

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
		//usg1 = usage object
		//tim1 = current time
		//ncw1 = netcdf writer
		//iInputLine = input line
		//shpMask = masking shapefile
		//sSuffix = output suffix (for mess)
		
		String sSuffix;
		LocalDate tim1;
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
		NetcdfWriter ncw1;
		int iInputLine;
		ShapefileIO shpMask = null;
		
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
		
		//loading mask
		if(arg1.containsArgument("sMaskPath") && arg1.containsArgument("sMaskFeature")){
			
			//loading masking shapefile
			shpMask = new ShapefileIO(arg1.getValueString("sMaskPath"),arg1.getValueString("sMaskFeature"));
			
			//loading masking polygon
			shpMask.loadFeature(arg1.getValueString("sMaskFeatureID"));
			plyMask = shpMask.getPolygon();
		}
		
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
		
		//loading input line
		iInputLine=arg1.getValueInt("iInputLine");
		
		//loading response var
		sResponseVar=datModels.getString(iInputLine, "RESPONSE_VAR");
		
		//looping through models
		System.out.println("Projecting " + sResponseVar + "...");
		
		//checking if model contains more than intercept
		if(datModels.getString(iInputLine,"MODEL").split(";").length==1){
			System.out.println("Model cannot be projected: exiting");
			return;
		}
		
		//loading and fitting model
		if(arg1.getValueBoolean("bMESS")==false){
			lnm1 = new LinearModel(trn1.getDataTable(), sResponseVar, trn1.getPredictors(sResponseVar));
			lnm1.fitModel(trn1.getPredictors(sResponseVar));
			sSuffix = "";
		}else{
			lnm1 = new LinearModel_MESS(trn1.getDataTable(), sResponseVar, trn1.getPredictors(sResponseVar));
			sSuffix = " MESS";
		}
			
		//initializing projector
		prj1 = new Projector(
				datModels.getString(iInputLine, "RESPONSE_VAR") + sSuffix,
				datModels.getString(iInputLine,"RESPONSE_VAR_UNITS") + sSuffix,
				datModels.getString(iInputLine, "RESPONSE_VAR") + sSuffix,
				"area: mean",
				plyMask,
				lnm1,
				prd1,
				datModels.getString(iInputLine,"RESPONSE_VAR_TRANSFORM") + sSuffix,
				arg1.getValueBoolean("bApplyInverse"));
		prj1.addNullVert();
		for(String s:prd1.getProjectionDateAliases()){
			System.out.println("Projecting to " + s + "...");
			tim1 = new LocalDate(s);
			prj1.addTime(tim1, tim1);
			prj1.loadGrid(s, GeospatialRaster.NULL_VERT);
			MaskGeospatialRaster.applyMask(plyMask,prj1,tim1, GeospatialRaster.NULL_VERT);
		}

		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{prj1}, arg1.getValueString("sOutputPath"));

		//writing output
		for(String s:prd1.getProjectionDateAliases()){
			tim1 = new LocalDate(s);
			ncw1.writeRaster(prj1,tim1,GeospatialRaster.NULL_VERT);
		}
		
		//terminating
		ncw1.close();
		prd1.close();
		System.out.println("Done.");		
	}

	public static void main0(String[] rgsArgs) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//ncrRaster = raster
		//datModels = selected models
		//itr1 = time iterator 
		//trn1 = training data	
		//lnm1 = linear model object
		//prj1 = projector
		//ncw1 = netcdf writer
		//shpMask = shapfile (for masking if requested)
		//plyMask = masking polygon (if requested)
		//prj1 = current projector
		//prd1 = projection data
		//sResponseVar = response variable
		//iInputLine = input line
		//rgtProjectionTimes = projection times
		//dProjectionVert = projection vert
		//lstOut = sample predictions output
		//usg1 = usage object
		
		ShapefileIO shpMask = null;
		SphericalMultiPolygon plyMask = null;
		ArgumentIO arg1;
		BiomIO bio1;
		DataIO datModels;
		TrainingData trn1 = null;
		LinearModel lnm1;
		NetcdfWriter ncw1;
		ProjectionData prd1 = null;
		Projector prj1;
		String sResponseVar;
		int iInputLine;
		LocalDate[] rgtProjectionTimes;
		double dProjectionVert;
		ArrayList<String> lstOut;
		Usage usg1;
		
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
		
		//loading input line
		iInputLine=1;
		if(arg1.containsArgument("iInputLine")){
			iInputLine=arg1.getValueInt("iInputLine");
		}
		
		//loading selected model
		datModels = new DataIO(arg1.getValueString("sSelectedModelsPath"));
		
		//loading response var
		sResponseVar=datModels.getString(iInputLine, "RESPONSE_VAR");
		
		//loading sample location data
		System.out.println("Loading BIOM file...");
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		
		//loading mask
		if(arg1.containsArgument("sMaskPath") && arg1.containsArgument("sMaskFeature")){
			
			//loading masking shapefile
			shpMask = new ShapefileIO(arg1.getValueString("sMaskPath"),arg1.getValueString("sMaskFeature"));
			
			//loading masking polygon
			shpMask.loadFeature(arg1.getValueString("sMaskFeatureID"));
			plyMask = shpMask.getPolygon();
		}
	
		//loading training data
		trn1 = new TrainingData(
				bio1,
				new DataIO(arg1.getValueString("sRasterListPath")),
				new DataIO(new String[]{sResponseVar}),
				(arg1.containsArgument("sTrainingDatesPath")) ? new DataIO(arg1.getValueString("sTrainingDatesPath")) : null,
				(arg1.containsArgument("dTrainingVert")) ? arg1.getValueDouble("dTrainingVert") : GeospatialRaster.NULL_VERT,
				arg1.getValueString("sResponseTransform"));
		
		//loading projection data
		prd1 = new ProjectionData(
				trn1,
				new DataIO(arg1.getValueString("sRasterListPath")),
				arg1.containsArgument("dProjectionVert") ? arg1.getValueDouble("dProjectionVert") : GeospatialRaster.NULL_VERT);
	
		//looping through models
		System.out.println("Initializing output...");
		
		//checking if model contains more than intercept
		if(datModels.getString(iInputLine,"MODEL").split(";").length==1){
			System.out.println("Done.");
			return;
		}
		
		//loading and fitting model
		lnm1 = new LinearModel(trn1.getDataTable(), sResponseVar, trn1.getPredictors());
		lnm1.fitModel(trn1.getPredictors());
		
		//loading projection times
		if(!arg1.containsArgument("rgtProjectionTimes")){
			rgtProjectionTimes = new LocalDate[]{GeospatialRaster.NULL_TIME};
		}else{
			rgtProjectionTimes = arg1.getValueTimeArray("rgtProjectionTimes");
		}
		
		//loading projection vert
		if(arg1.containsArgument("dProjectionVert")){
			dProjectionVert = arg1.getValueDouble("dProjectionVert");
		}else{
			dProjectionVert = GeospatialRaster.NULL_VERT;
		}
		
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
		prj1.addVert(dProjectionVert,dProjectionVert);
		for(LocalDate tim1:rgtProjectionTimes){
			prj1.addTime(tim1,tim1);
		}
			
		//writing output map
		if(!arg1.containsArgument("sOutputMode") || arg1.getValueString("sOutputMode").equals("map")){
			ncw1 = new NetcdfWriter(new GeospatialRaster[]{prj1}, arg1.getValueString("sOutputPath"));
			for(TimeVert tvt1:prj1.getAllTimeVertCombinations()){	
				
				//***********************
				System.out.println(tvt1.tim1.toString() + "," + tvt1.dVert);
				//***********************
				
				prj1.loadGrid(tvt1.tim1.toString(),tvt1.dVert);
				MaskGeospatialRaster.applyMask(plyMask,prj1,tvt1.tim1,tvt1.dVert);
				ncw1.writeRaster(prj1,tvt1.tim1,tvt1.dVert);
				prj1.remove(tvt1.tim1,tvt1.dVert);
			}	
			ncw1.close();
			
		//writing predictions for sampling locations only	
		}else if(arg1.getValueString("sOutputMode").equals("samplepredictions")){	
			lstOut = new ArrayList<String>();
			lstOut.add("SAMPLE_ID,KEY,VALUE");
			for(String s:bio1.axsSample.getIDs()){
				lstOut.add(
					s
					+ "," + "observation"
					+ "," + trn1.getDataTable().get(sResponseVar, s)
				);
			}
			for(TimeVert tvt1:prj1.getAllTimeVertCombinations()){	
				for(String s:bio1.axsSample.getIDs()){
					lstOut.add(
							s
							+ ",prediction:" + tvt1.tim1 + ((tvt1.dVert==GeospatialRaster.NULL_VERT) ? "" : ":" + tvt1.dVert)
							+ "," + prj1.readPrediction(
									Double.parseDouble(bio1.axsSample.getMetadata(s).get("latitude")), 
									Double.parseDouble(bio1.axsSample.getMetadata(s).get("longitude")), 
									tvt1.tim1.toString(), 
									tvt1.dVert)
							);
				}
			}
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		}
		
		//terminating
		prd1.close();
		System.out.println("Done.");		
	}
}