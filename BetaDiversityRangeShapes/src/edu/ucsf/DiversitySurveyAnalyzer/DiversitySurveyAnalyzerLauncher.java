package edu.ucsf.DiversitySurveyAnalyzer;

import java.util.ArrayList;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.RawSurveyResults;
import edu.ucsf.geospatial.SphericalCapEarth_SamplingRegion;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Analyzes results from beta-diversity diversity survey.
 * @author jladau
 */

public class DiversitySurveyAnalyzerLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//datRegions = sampling regions
		//rgcRegions = sampling regions
		//lstOut = output
		//shp1 = shapefile io object
		//prd1 = prediction object
		//ply1 = current polygon
		//rss1 = raw survey results
		//dLGlobal = Global polygon perimeter
		//dFGlobal = Global polygon area
		//dGamma = current gamma diversity
		
		RawSurveyResults rss1;
		SphericalMultiPolygon ply1;
		ShapefileIO shp1;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		BiomIO bio1;
		DataIO datRegions;
		SphericalCapEarth_SamplingRegion[] rgcRegions;
		BetaDiversityPredictions prd1;
		double dLGlobal = Double.NaN;
		double dFGlobal = Double.NaN;
		double dGamma;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
				
		//loading biom file
		arg1.updateArgument("bCheckRarefied", false);
		arg1.updateArgument("bNormalize", false);
		bio1 = new BiomIO(arg1.getValueString("sBiomPath"),
				arg1.getAllArguments());
		
		//loading raw survey results
		rss1 = new RawSurveyResults(bio1);
		
		//loading regions
		System.out.println("Loading sampling regions...");
		datRegions = new DataIO(arg1.getValueString("sSamplingRegionsPath"));
		rgcRegions = new SphericalCapEarth_SamplingRegion[datRegions.iRows-1];
		for(int i=1;i<datRegions.iRows;i++){
			rgcRegions[i-1] = new SphericalCapEarth_SamplingRegion(
					datRegions.getDouble(i, "RADIUS"),
					datRegions.getDouble(i, "LATITUDE_CENTER"),
					datRegions.getDouble(i, "LONGITUDE_CENTER"),
					arg1.getValueDouble("dGlobalRadius"),
					i);
		}
	
		//loading range characteristics
		shp1 = new ShapefileIO(
				arg1.getValueString("sShapefilePath"),
				arg1.getValueString("sIDHeader"));
		while(shp1.hasNext()){
			shp1.next();
			System.out.println("Measuring range characteristics of " + shp1.getID() + "...");
			ply1 = shp1.getPolygon();
			if(arg1.getValueDouble("dGlobalRadius")>EarthGeometry.EARTH_CIRCUMFERENCE_SPHERE/4.){
				dLGlobal = ply1.perimeter(rgcRegions);
				dFGlobal = ply1.area(rgcRegions);
			}	
			for(int i=0;i<rgcRegions.length;i++){
				if(arg1.getValueDouble("dGlobalRadius")<=EarthGeometry.EARTH_CIRCUMFERENCE_SPHERE/4.){
					
					//TODO Global perimeter and area are being calculated across all Global locations here, not just the ones within the sampled area
					dLGlobal = ply1.perimeter(rgcRegions[i].capGlobal());
					dFGlobal = ply1.area(rgcRegions[i].capGlobal());
				}	
				rgcRegions[i].addPerimeterArea(ply1, dLGlobal, dFGlobal);
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(10000000);
		lstOut.add("SAMPLING_REGION,VALUE_TYPE,VALUE");
		for(int i=0;i<rgcRegions.length;i++){
			
			//characteristics of regions
			lstOut.add(i + ",region_latitude_center," + rgcRegions[i].centerLatitude());
			lstOut.add(i + ",region_longitude_center," + rgcRegions[i].centerLongitude());
			lstOut.add(i + ",region_radius," + rgcRegions[i].radius());
			lstOut.add(i + ",region_area," + rgcRegions[i].area());
			lstOut.add(i + ",region_perimeter," + rgcRegions[i].perimeter());
			
			//beta diversity
			if(i%100==0){	
				System.out.println("Loading beta-diversity for sampling region " + (i+1) + " of " + rgcRegions.length + "...");
			}
			rgcRegions[i].loadBetaDiversity(rss1);
			lstOut.add(i + ",beta_s_observed," + rgcRegions[i].beta_s());
			lstOut.add(i + ",beta_j_observed," + rgcRegions[i].beta_j());
			lstOut.add(i + ",beta_w_observed," + rgcRegions[i].beta_w());
			
			//alpha diversity
			lstOut.add(i + ",alpha_observed," + rgcRegions[i].alpha());
			
			//number of samples
			lstOut.add(i + ",unpaired_samples," + rgcRegions[i].numberOfUnpairedSamples());
			lstOut.add(i + ",paired_samples," + rgcRegions[i].numberOfPairedSamples());
			
			//outputting range characteristics, number of species, and predictions
			dGamma = (double) rgcRegions[i].numberOfSpecies();
			lstOut.add(i + ",regional_perimeter_mean_observed," + rgcRegions[i].perimeterRegional()/dGamma);
			lstOut.add(i + ",global_perimeter_mean_observed," + rgcRegions[i].perimeterGlobal()/dGamma);		
			lstOut.add(i + ",regional_area_mean_observed," + rgcRegions[i].areaRegional()/dGamma);
			lstOut.add(i + ",global_area_mean_observed," + rgcRegions[i].areaGlobal()/dGamma);
			lstOut.add(i + ",regional_perimeter_area_ratio_observed," + rgcRegions[i].perimeterRegional()/rgcRegions[i].areaRegional());
			lstOut.add(i + ",global_scaled_perimeter_area_ratio_observed," + rgcRegions[i].perimeterGlobalScaled()/rgcRegions[i].areaGlobalScaled());
			lstOut.add(i + ",total_species," + rgcRegions[i].numberOfSpecies());
			prd1 = new BetaDiversityPredictions(rgcRegions[i], 2.*arg1.getValueDouble("dPlotRadius"));
			for(String s:prd1.predictions().keySet()){
				lstOut.add(i + "," + s + "," + prd1.predictions().get(s));
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}		
}