package edu.ucsf.Geospatial.SphericalCapsAreaShapefile;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Finds the fraction of polygon areas within spherical caps; useful for finding total values of variables within caps
 * @author jladau
 *
 */


public class SphericalCapsAreaShapefileLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = centers of spherical caps
		//shp1 = shapefile
		//map1 = map from cap ids to cap objects
		//ply1 = current polygon
		//dArea = current area of intersection
		//map2 = map from cap ids, polygon ids to areas of polygons within caps
		//lstOut = output
		//dAreaPolygon = polygon area
		
		ArgumentIO arg1;
		DataIO dat1;
		ShapefileIO shp1;
		HashMap<String,SphericalCapEarth> map1;
		SphericalMultiPolygon ply1;
		double dArea;
		HashMap<String,Double> map2;
		ArrayList<String> lstOut;
		double dAreaPolygon;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sSphericalCapsPath"));
		shp1 = new ShapefileIO(arg1.getValueString("sShapeFilePath"),arg1.getValueString("sIDHeader"));
		map1 = new HashMap<String,SphericalCapEarth>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, "ID_CAP"), new SphericalCapEarth(dat1.getDouble(i, "RADIUS"), dat1.getDouble(i, "LATITUDE_CENTER"), dat1.getDouble(i, "LONGITUDE_CENTER"),i+7*arg1.getValueInt("iRandomSeed")));
		}
		map2 = new HashMap<String,Double>(dat1.iRows*1000);
		lstOut = new ArrayList<String>(map2.size()+1);
		lstOut.add("ID_CAP,CAP_RADIUS,CAP_AREA,CAP_LATITUDE_CENTER,CAP_LONGITUDE_CENTER,ID_POLYGON,FRACTION_POLYGON_AREA");
		
		//finding fraction areas within caps
		while(shp1.hasNext()){
			shp1.next();
			System.out.println("Analyzing polygon " + shp1.getID() + "...");
			ply1 = shp1.getPolygon();
			dAreaPolygon = ply1.area();
			for(String sCap:map1.keySet()){
				dArea = ply1.area(map1.get(sCap), true);
				if(dArea>0){
					if(dArea>dAreaPolygon){
						dArea=dAreaPolygon;
					}
					lstOut.add(
							sCap + "," + 
							map1.get(sCap).radius() + "," + 
							map1.get(sCap).area() + "," + 
							map1.get(sCap).centerLatitude() + "," +
							map1.get(sCap).centerLongitude() + "," +
							shp1.getID() + "," + 
							dArea/dAreaPolygon);
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
