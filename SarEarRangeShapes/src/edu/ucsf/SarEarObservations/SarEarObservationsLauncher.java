package edu.ucsf.SarEarObservations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;

import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalCapEarth_SamplingRegion_SarEar;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

//TODO Two modules: one for finding observed richnesses and prevalences, another for finding predicted richnesses and prevalences.

public class SarEarObservationsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shp1 = shapefile io object
		//shp2 = merged shapefile object
		//plyMerged = merged polygon
		//datRegions = sampling regions
		//rgcRegions = sampling regions
		
		ArgumentIO arg1;
		ShapefileIO shp1;
		ShapefileIO shp2;
		SphericalMultiPolygon plyMerged;
		DataIO datRegions;
		SphericalCapEarth_SamplingRegion_SarEar[] rgcRegions;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading merged shapefile
		shp2 = new ShapefileIO(
				arg1.getValueString("sDissolvedShapefilePath"),
				arg1.getValueString("sIDHeader"));
		shp2.next();
		plyMerged = shp2.getPolygon();
		
		//loading shapefile
		shp1 = new ShapefileIO(
				arg1.getValueString("sShapefilePath"),
				arg1.getValueString("sIDHeader"));

		//loading study regions
		System.out.println("Loading study regions...");
		datRegions = new DataIO(arg1.getValueString("sSamplingRegionsPath"));
		rgcRegions = new SphericalCapEarth_SamplingRegion_SarEar[datRegions.iRows-1];
		for(int i=1;i<datRegions.iRows;i++){
			if(datRegions.hasHeader("SAMPLING_PLOT_RADII")){
				rgcRegions[i-1] = new SphericalCapEarth_SamplingRegion_SarEar(
						datRegions.getDouble(i, "RADIUS"),
						datRegions.getDouble(i, "LATITUDE_CENTER"),
						datRegions.getDouble(i, "LONGITUDE_CENTER"),
						0.,
						i,
						datRegions.getString(i, "REGION_ID"),
						correctSamplingPlotRadii(datRegions.getString(i, "SAMPLING_PLOT_RADII").split(";"), datRegions.getDouble(i, "RADIUS")));
			}else{
				rgcRegions[i-1] = new SphericalCapEarth_SamplingRegion_SarEar(
						datRegions.getDouble(i, "RADIUS"),
						datRegions.getDouble(i, "LATITUDE_CENTER"),
						datRegions.getDouble(i, "LONGITUDE_CENTER"),
						0.,
						i,
						datRegions.getString(i, "REGION_ID"),
						correctSamplingPlotRadii(arg1.getValueStringArray("rgsPlotRadii"), datRegions.getDouble(i, "RADIUS")));
			}
			rgcRegions[i-1].loadMergedIntersection(plyMerged);
		}

		//outputting results
		DataIO.writeToFile(
				loadResults(shp1, rgcRegions),
				arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
	
	private static String[] correctSamplingPlotRadii(String rgsRadii[], double dStudyRegionRadius){
		
		//rgsOut = output
		//d1 = current radius
		//rgs1 = current entry split
		
		String rgsOut[];
		double d1;
		String rgs1[];
		
		rgsOut = new String[rgsRadii.length];
		for(int i=0;i<rgsRadii.length;i++){
			rgs1 = rgsRadii[i].split(":");
			d1 = Double.parseDouble(rgs1[1]);
			if(d1>dStudyRegionRadius-0.00001){
				d1 = dStudyRegionRadius-0.00001;
				rgs1[1] = Double.toString(d1);
			}
			rgsOut[i] = rgs1[0] + ":" + rgs1[1];
		}
		return rgsOut;
	}
	
	private static ArrayList<String> loadResults(
			ShapefileIO shp1,
			SphericalCapEarth_SamplingRegion_SarEar[] rgcRegions){

		//lstOut = output list
		//rgi1 = list of richnesses
		//lstOut = output
		//mapPrev = map from (sampling region id, plot area, species id) --> prevalence
		//s1 = base key
		//mapPolygon = map from species IDs to polygons
		//mapPlots = map from plot radius to list of plots
		//mapCount = map from (sampling region id, plot area, species id) --> plot count
		//setCandidateEdges = set of candidate vertices
		
		HashSet<SphericalPolygonEdge> setCandidateEdges;
		String s1;
		ArrayList<String> lstOut;
		HashMap_AdditiveDouble<String> mapPrev;
		HashMap_AdditiveInteger<String> mapCount;
		HashMap<String,SphericalMultiPolygon> mapPolygon;
		HashMultimap<Double,SphericalCapEarth> mapPlots;
		
		//initializing maps
		mapPrev = new HashMap_AdditiveDouble<String>(rgcRegions.length*10000);
		mapCount = new HashMap_AdditiveInteger<String>(rgcRegions.length*10000);
		mapPolygon = new HashMap<String,SphericalMultiPolygon>(11000);
		
		//loading polygons
		while(shp1.hasNext()){	
			shp1.next();
			System.out.println("Loading range of " + shp1.getID() + "...");
			if(shp1.getPolygon().edgeCount()>0){
				mapPolygon.put(shp1.getID(), shp1.getPolygon());
			}
		}
		
		//looping through study regions
		for(int k=0;k<rgcRegions.length;k++){
			
			//updating progress
			System.out.println("Surveying diversity in study region " + (k+1) + "...");
			
			//checking if study region intersects any ranges
			if(rgcRegions[k].intersectsMerged()){
				
				//loading sampling plots
				System.out.println("Loading sampling plots...");
				mapPlots = loadSamplingPlots(rgcRegions[k].getPlotRadii(), rgcRegions[k]);
				
				//looping through ranges
				for(String s:mapPolygon.keySet()){
					
					//checking if study region intersects current range
					if(mapPolygon.get(s).intersects(rgcRegions[k])){
						setCandidateEdges = mapPolygon.get(s).intersectingEdges(rgcRegions[k]);
						
						//updating progress
						System.out.println("Analyzing " + s + "...");
						
						//looping through sampling plots
						for(double dRadius:mapPlots.keySet()){
							s1 = rgcRegions[k].getID() + "," + dRadius + "," + s;
							
							//initializing prevalence count
							mapPrev.put(s1, 0.);
							
							//looping through plots
							for(SphericalCapEarth capPlot:mapPlots.get(dRadius)){
								if(mapPolygon.get(s).intersects(capPlot, setCandidateEdges)){
									mapPrev.putSum(s1, 1.);	
								}
								mapCount.putSum(s1, 1);
							}
						}
					}
				}
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>(mapPrev.size() + 10);
		lstOut.add("STUDY_REGION_ID,SAMPLING_PLOT_RADIUS,SPECIES_ID,PREVALENCE");
		
		//outputting results
		for(String s:mapPrev.keySet()){
			lstOut.add(s + "," + mapPrev.get(s)/((double) mapCount.get(s)));
		}
		return lstOut;
	}	
	
	private static HashMultimap<Double,SphericalCapEarth> loadSamplingPlots(String[] rgsPlotRadii, SphericalCapEarth_SamplingRegion_SarEar capStudyRegion){
		
		//rgs1 = current radii count split
		//lstPlots = list of plots
		//iPlots = maximum number of plots
		//mapOut = output
		
		int iPlots;
		String rgs1[];
		HashMultimap<Double,SphericalCapEarth> mapOut;
		
		iPlots=-1*Integer.MAX_VALUE;
		for(String s:rgsPlotRadii){
			rgs1 = s.split(":");
			if(Integer.parseInt(rgs1[0])>iPlots){
				iPlots=Integer.parseInt(rgs1[0]);
			}
		}
		mapOut = HashMultimap.create(rgsPlotRadii.length, iPlots);
		for(String s:rgsPlotRadii){
			rgs1 = s.split(":");
			mapOut.putAll(
					Double.parseDouble(rgs1[1]),
					capStudyRegion.randomCaps(Double.parseDouble(rgs1[1]), Integer.parseInt(rgs1[0]), 1234));
		}
		return mapOut;
	}
}