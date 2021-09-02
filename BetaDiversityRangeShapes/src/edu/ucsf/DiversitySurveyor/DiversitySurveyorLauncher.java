package edu.ucsf.DiversitySurveyor;

import java.util.ArrayList;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

public class DiversitySurveyorLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shp1 = shapefile io object
		//lstOut = output list
		//rgcPlots = sampling plots
		//geo1 = earth geometry object
		//sbl1 = current output line
		//shp2 = merged shapefil object
		//plyMerged = merged polygon
		//plyCurrent = current polygon
		//rgb1 = contains true if location is in merged polygon, false otherwise -- corresponds to rgcPlots
		//plyLand = land polygon
		
		SphericalMultiPolygon plyLand;
		StringBuilder sbl1;
		EarthGeometry geo1;
		SphericalCapEarth[][] rgcPlots;
		boolean[][] rgb1;
		ArgumentIO arg1;
		ShapefileIO shp1;
		ShapefileIO shp2;
		ArrayList<String> lstOut;
		SphericalMultiPolygon plyMerged;
		SphericalMultiPolygon plyCurrent;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading merged shapefile
		shp2 = new ShapefileIO(
				arg1.getValueString("sDissolvedShapefilePath"),
				arg1.getValueString("sIDHeader"));
		shp2.next();
		plyMerged = shp2.getPolygon();
		
		shp1 = new ShapefileIO(
				arg1.getValueString("sLandShapefilePath"),
				arg1.getValueString("sLandIDHeader"));
		shp1.next();
		plyLand= shp1.getPolygon();
		
		//loading shapefile
		shp1 = new ShapefileIO(
				arg1.getValueString("sShapefilePath"),
				arg1.getValueString("sIDHeader"));

		//loading sampling plots
		geo1 = new EarthGeometry();
		rgcPlots = new SphericalCapEarth[arg1.getValueInt("iPlots")][2];
		rgb1 = new boolean[arg1.getValueInt("iPlots")][2];
		for(int i=0;i<rgcPlots.length;i++){
			
			if(i%100==0){
				System.out.println("Loading sampling plot " + (i+1) + " of " + rgcPlots.length + "...");
			}
			
			do{	
				rgcPlots[i][0] = new SphericalCapEarth(
						arg1.getValueDouble("dPlotRadius"),
						geo1.randomPoint(),
						i*11+19);
				rgcPlots[i][1] = rgcPlots[i][0].randomAdjacentCap();
			}while(!plyLand.contains(rgcPlots[i][0].centerLatitude(), rgcPlots[i][0].centerLongitude()) || !plyLand.contains(rgcPlots[i][1].centerLatitude(), rgcPlots[i][1].centerLongitude()));
			if(plyMerged.contains(rgcPlots[i][0].centerLatitude(), rgcPlots[i][0].centerLongitude())){
				rgb1[i][0]=true;
			}else{
				rgb1[i][0]=false;
			}
			if(plyMerged.contains(rgcPlots[i][1].centerLatitude(), rgcPlots[i][1].centerLongitude())){
				rgb1[i][1]=true;
			}else{
				rgb1[i][1]=false;
			}
		}
		
		//outputting plot metadata
		lstOut = new ArrayList<String>(rgcPlots.length*2+1);
		lstOut.add("SampleID,Latitude,Longitude,Day,Month,Year,Radius");
		for(int i=0;i<rgcPlots.length;i++){
			for(int j=0;j<2;j++){
				lstOut.add(
						i + "-" + j
						+ "," + rgcPlots[i][j].centerLatitude()
						+ "," + rgcPlots[i][j].centerLongitude()
						+ "," + "09,09,9999"
						+ "," + arg1.getValueDouble("dPlotRadius"));
			}
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace(".csv","_metadata.csv"));
		
		//initializing otu table output
		lstOut = new ArrayList<String>(1);
		sbl1 = new StringBuilder();
		sbl1.append("#OTU ID");
		for(int i=0;i<rgcPlots.length;i++){
			for(int j=0;j<2;j++){
				sbl1.append("\t" + i + "-" + j);
			}
		}
		sbl1.append("\ttaxonomy");
		lstOut.add(sbl1.toString());
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace(".csv","_samples.csv"));
		
		//looping through species
		while(shp1.hasNext()){
			
			//getting next shape
			shp1.next();
			plyCurrent = shp1.getPolygon();
			
			//updating progress
			System.out.println("Sampling " + shp1.getID() + "...");
			
			//initializing output
			lstOut = new ArrayList<String>(1);
			sbl1 = new StringBuilder(rgcPlots.length*2*2+250);
			sbl1.append(shp1.getID().replace(" ", "_"));
			
			//looping through sampling plots
			for(int i=0;i<rgcPlots.length;i++){
				for(int j=0;j<2;j++){
			
					//checking if plot is within merged polygon
					if(rgb1[i][j]==false){
						sbl1.append("\t0");
						continue;
					}else{
					
						//checking if bounds intersect
						if(!plyCurrent.getBounds().contains(rgcPlots[i][j].centerLatitude(), rgcPlots[i][j].centerLongitude())){
							sbl1.append("\t0");
						}else{
						
							//checking for occurrence
							if(plyCurrent.contains(rgcPlots[i][j].centerLatitude(), rgcPlots[i][j].centerLongitude())){
								sbl1.append("\t1");
							}else{
								sbl1.append("\t0");
							}
						}
					}
				}
			}
			
			//appending taxonomic information
			if(shp1.getID().split(" ").length==0){
				sbl1.append("\tk__na;p__na;c__na;o__na;f__na;g__na;s__na");
			}else{
				if(shp1.getID().split(" ").length==1){
					sbl1.append("\tk__na;p__na;c__na;o__na;f__na;g__" + shp1.getID() + ";s__na");
				}else{
					sbl1.append("\tk__na;p__na;c__na;o__na;f__na;g__" + shp1.getID().split(" ")[0] + ";s__" + shp1.getID().split(" ")[1]);
				}
			}
			
			//printing output
			lstOut.add(sbl1.toString());
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace(".csv","_samples.csv"), true);
		}
		
		//terminating
		System.out.println("Done.");
	}
}