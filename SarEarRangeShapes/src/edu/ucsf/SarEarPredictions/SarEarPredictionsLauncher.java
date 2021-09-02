package edu.ucsf.SarEarPredictions;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Joiner;
import edu.ucsf.geospatial.SphericalCapEarth_SamplingRegion_SarEar;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Analyzes results from beta-diversity diversity survey.
 * @author jladau
 */

public class SarEarPredictionsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//datRegions = sampling regions
		//mapRegions = map from sampling region ids to sampling regions
		//lstOut = output
		//shp1 = shapefile io object
		//prd1 = prediction object
		//ply1 = current polygon
		//rss1 = raw survey results
		//dLGlobal = global polygon perimeter
		//dFGlobal = global polygon area
		//dGamma = gamma diversity
		//dat1 = data
		//iRegionIndex = current region index
		//dSmoothingRadius = smoothing radius
		
		SphericalMultiPolygon ply1;
		ShapefileIO shp1;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO datRegions;
		HashMap<String,SphericalCapEarth_SamplingRegion_SarEar> mapRegions;
		double dLGlobal = Double.NaN;
		double dFGlobal = Double.NaN;
		//double dGamma;
		DataIO dat1;
		String sRegion;
		double dSmoothingRadius;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading observations
		System.out.println("Loading observations...");
		dat1 = new DataIO(arg1.getValueString("sObservationsPath"));
		
		//loading smoothing radius
		dSmoothingRadius = arg1.getValueDouble("dSmoothingRadius");
		
		//loading regions
		System.out.println("Loading study regions...");
		datRegions = new DataIO(arg1.getValueString("sSamplingRegionsPath"));
		mapRegions = new HashMap<String,SphericalCapEarth_SamplingRegion_SarEar>(datRegions.iRows-1);
		for(int i=1;i<datRegions.iRows;i++){
			System.out.println("Loading study region " + i + " of " + datRegions.iRows + "...");
			
			sRegion = datRegions.getString(i, "REGION_ID");
			
			///System.out.println(sRegion);
			
			mapRegions.put(sRegion, new SphericalCapEarth_SamplingRegion_SarEar(
					datRegions.getDouble(i, "RADIUS"),
					datRegions.getDouble(i, "LATITUDE_CENTER"),
					datRegions.getDouble(i, "LONGITUDE_CENTER"),
					dSmoothingRadius,
					i,
					sRegion,
					null));
		}
	
		//loading range characteristics
		shp1 = new ShapefileIO(
				arg1.getValueString("sShapefilePath"),
				arg1.getValueString("sIDHeader"));
		while(shp1.hasNext()){
			shp1.next();
			System.out.println("Measuring range characteristics of " + shp1.getID() + "...");
			
			ply1 = shp1.getPolygon();
			dLGlobal = ply1.perimeter();
			dFGlobal = ply1.area();	
			//dLGlobal = ply1.perimeter(rgcRegions);
			//dFGlobal = ply1.area(rgcRegions);	
			for(String s:mapRegions.keySet()){
				mapRegions.get(s).savePerimeterArea(ply1, dLGlobal, dFGlobal, shp1.getID());
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(1000000);
		lstOut.add(Joiner.on(",").join(dat1.getRow(0)));
		for(int k=1;k<dat1.iRows;k++){
			
			if(k%100 == 0){
				System.out.println("Outputting results for observation " + (k) + "...");
			}
			sRegion = dat1.getString(k, "STUDY_REGION_ID");
			
			if(mapRegions.containsKey(sRegion)){
				if(arg1.getValueString("sPredictionMode").equals("euclidean")){	
					dat1.setString(k, "PREVALENCE", Double.toString(
						mapRegions.get(sRegion).prevalencePredictionEuclidean(
								dat1.getDouble(k, "SAMPLING_PLOT_RADIUS"), 
								dat1.getString(k, "SPECIES_ID"),
								arg1.getValueString("sPredictionScale"))));
					
				}else if(arg1.getValueString("sPredictionMode").equals("spherical")){
					dat1.setString(k, "PREVALENCE", Double.toString(
							mapRegions.get(sRegion).prevalencePredictionSpherical(
									dat1.getDouble(k, "SAMPLING_PLOT_RADIUS"), 
									dat1.getString(k, "SPECIES_ID"),
									arg1.getValueString("sPredictionScale"))));
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}		
}