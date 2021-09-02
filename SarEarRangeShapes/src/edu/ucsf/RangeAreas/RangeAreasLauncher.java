package edu.ucsf.RangeAreas;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.common.collect.HashMultimap;
import edu.ucsf.geospatial.SphericalCapEarth_SamplingRegion_SarEar;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Analyzes results from beta-diversity diversity survey.
 * @author jladau
 */

public class RangeAreasLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//datRegions = sampling regions
		//rgcRegions = study regions
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
		//lstOut = output
		//sSpecies = species
		//mapOccurrences = returns species occurring in a region
		//mapRangeAreas = returns range areas for given region
		//dArea = current area
		//mapPlotRadii = radius plots to use for each region
		//lstOut2 = prevalence output
		
		double dArea;
		HashMultimap<String,String> mapOccurrences;
		ArrayList<String> lstOut;
		SphericalMultiPolygon ply1;
		ShapefileIO shp1;
		ArgumentIO arg1;
		DataIO datRegions;
		HashMap<String,SphericalCapEarth_SamplingRegion_SarEar> mapRegions;
		DataIO dat1;
		String sRegion;
		String sSpecies;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading observations
		System.out.println("Loading observations...");
		dat1 = new DataIO(arg1.getValueString("sObservationsPath"));
		
		//loading regions
		System.out.println("Loading study regions...");
		datRegions = new DataIO(arg1.getValueString("sSamplingRegionsPath"));
		mapRegions = new HashMap<String,SphericalCapEarth_SamplingRegion_SarEar>(datRegions.iRows-1);
		for(int i=1;i<datRegions.iRows;i++){
			System.out.println("Loading study region " + i + " of " + datRegions.iRows + "...");
			mapRegions.put(datRegions.getString(i, "REGION_ID"), new SphericalCapEarth_SamplingRegion_SarEar(
					datRegions.getDouble(i, "RADIUS"),
					datRegions.getDouble(i, "LATITUDE_CENTER"),
					datRegions.getDouble(i, "LONGITUDE_CENTER"),
					0,
					i,
					datRegions.getString(i, "REGION_ID"),
					null));
		}
	
		//loading occurrences
		mapOccurrences = HashMultimap.create(10000,100);
		for(int k=1;k<dat1.iRows;k++){	
			sRegion = dat1.getString(k, "STUDY_REGION_ID");
			sSpecies = dat1.getString(k, "SPECIES_ID");
			mapOccurrences.put(sSpecies,sRegion);
		}
		
		//initializing output
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("REGION_ID,SPECIES,RANGE_AREA_IN_STUDY_REGION");
		
		//outputting range characteristics
		shp1 = new ShapefileIO(
				arg1.getValueString("sShapefilePath"),
				arg1.getValueString("sIDHeader"));
		while(shp1.hasNext()){
			shp1.next();
			System.out.println("Measuring range areas of " + shp1.getID() + "...");
			ply1 = shp1.getPolygon();
			for(String s:mapOccurrences.get(shp1.getID())){	
				dArea = mapRegions.get(s).calculateArea(ply1);
				if(!Double.isNaN(dArea)){
					lstOut.add(s + "," + shp1.getID() + "," + dArea);
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}