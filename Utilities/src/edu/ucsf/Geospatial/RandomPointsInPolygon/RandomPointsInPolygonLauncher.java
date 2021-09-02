package edu.ucsf.Geospatial.RandomPointsInPolygon;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Generates random points within polygon.
 * @author jladau
 */

public class RandomPointsInPolygonLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//shp1 = shapefile
		//rgd1 = random points
		//lstOut = output
		
		ArgumentIO arg1;
		ShapefileIO shp1;
		double rgd1[][];
		ArrayList<String> lstOut;
		
		arg1 = new ArgumentIO(rgsArgs);
		try{
			shp1 = new ShapefileIO(arg1.getValueString("sShapefilePath"), arg1.getValueString("sIDHeader"));
			if(shp1.loadFeature(arg1.getValueString("sPolygonID"))==1){
				throw new Exception("ERROR: Polygon not found.");
			}
			rgd1 = shp1.getPolygon().generateRandomPointsInPolygon(arg1.getValueInt("iPoints"), arg1.getValueInt("iRandomSeed"));
			lstOut = new ArrayList<String>(rgd1.length+1);
			lstOut.add("Latitude,Longitude");
			for(int i=0;i<rgd1.length;i++){
				lstOut.add(rgd1[i][0] + "," + rgd1[i][1]);
			}
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
}
