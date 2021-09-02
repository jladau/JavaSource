package edu.ucsf.Geospatial.ControlledValueRegions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import com.google.common.collect.HashMultimap;

import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Finds spherical caps such that total value from shapefile is constant within caps
 * @author jladau
 *
 */

public class ControlledValueRegionsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = centers of spherical caps
		//shp1 = shapefile
		//map1 = map from cap ids to cap objects
		//ply1 = current polygon
		//map2 = map from cap ids, polygon ids to areas of polygons within caps
		//lstOut = output
		//dat2 = map from shapefile polygon ids to variable of interest (to be held constant)
		//map3 = map from shapefile polygon ids to polygon values
		//dTargetValue = target value
		//dEpsilon = allowable error
		//map5 = map from polygon ids to polygons
		//dRadius = current radius
		//dStep = current radius step
		//dTotal = current total
		//cap1 = current cap
		//i1 = current iteration
		//dat3 = target values for each variable
		//map4 = map from variable names to target values
		
		int i1;
		SphericalCapEarth cap1;
		double dTotal;
		ArgumentIO arg1;
		DataIO dat1;
		DataIO dat3;
		ShapefileIO shp1;
		HashMap<String,SphericalCapEarth> map1;
		SphericalMultiPolygon ply1;
		HashMap<String,Double> map2;
		ArrayList<String> lstOut;
		DataIO dat2;
		HashMap<String,PolygonProperty> map3;
		double dTargetValue;
		double dEpsilon;
		HashMap<String,SphericalMultiPolygon> map5;
		double dRadius;
		double dStep;
		HashMap<String,Double> map4;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);

		shp1 = new ShapefileIO(arg1.getValueString("sShapeFilePath"),arg1.getValueString("sIDHeader"));
		
		//loading polygon attributes
		dat2 = new DataIO(arg1.getValueString("sFactorValuesPath"));
		map3 = new HashMap<String,PolygonProperty>(dat2.iRows);
		for(int i=1;i<dat2.iRows;i++){
			if(!map3.containsKey(dat2.getString(i, "POLYGON_ID"))){
				map3.put(dat2.getString(i, "POLYGON_ID"), new PolygonProperty(10));
			}
			map3.get(dat2.getString(i, "POLYGON_ID")).addValue(dat2.getString(i, "VARIABLE"), dat2.getDouble(i, "VALUE"), dat2.getString(i, "OPERATION"));
		}
		
		//loading initial spherical caps
		dat1 = new DataIO(arg1.getValueString("sStartingCoordinatesPath"));
		map1 = new HashMap<String,SphericalCapEarth>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(Integer.toString(i), new SphericalCapEarth(
					100, 
					dat1.getDouble(i, "LATITUDE"), 
					dat1.getDouble(i, "LONGITUDE"),
					i+7*arg1.getValueInt("iRandomSeed")));
		}
		
		//loading target values
		dat3 = new DataIO(arg1.getValueString("sTargetValuesPath"));
		map4 = new HashMap<String,Double>(dat3.iRows);
		for(int i=1;i<dat3.iRows;i++){
			map4.put(dat3.getString(i, "VARIABLE"), dat3.getDouble(i, "TARGET_VALUE"));
		}
		
		//loading polygons
		map5 = new HashMap<String,SphericalMultiPolygon>(5000);
		while(shp1.hasNext()){
			shp1.next();
			System.out.println("Loading " + shp1.getID() + "...");
			ply1 = shp1.getPolygon();
			ply1.setID(shp1.getID());
			ply1.area();
			map5.put(ply1.id(), ply1);
		}
		
		map2 = new HashMap<String,Double>(dat1.iRows*1000);
		lstOut = new ArrayList<String>(map2.size()+1);
		lstOut.add("ID_CAP,CAP_RADIUS,CAP_AREA,CAP_LATITUDE_CENTER,CAP_LONGITUDE_CENTER,ID_POLYGON,FRACTION_POLYGON_AREA");
		dTargetValue = arg1.getValueDouble("dTargetValue");
		dEpsilon = dTargetValue/1000.;
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static void minimize(SphericalCapEarth cap1, String sAxis, HashMap<String,SphericalMultiPolygon> mapPolygons, HashMap<String,PolygonProperty> mapValues, HashMap<String,Double> mapTargetValues){
		
		
		
		
	}
	
	
	private static HashMap<String,Double> values(SphericalCapEarth cap1, HashMap<String,SphericalMultiPolygon> mapPolygons, HashMap<String,PolygonProperty> mapValues){
		
		//dArea = current area
		//map1 = total values
		//prt1 = polygon properties
		
		double dArea;
		HashMap_AdditiveDouble<String> map1;
		PolygonProperty prt1;
		
		map1 = new HashMap_AdditiveDouble<String>(mapValues.size());
		for(SphericalMultiPolygon ply1:mapPolygons.values()){
			dArea = ply1.area(cap1, true);
			if(dArea>0){
				if(dArea>ply1.area()){
					dArea=ply1.area();
				}
				if(mapValues.containsKey(ply1.id())){
					prt1 = mapValues.get(ply1.id());
					for(String s:prt1.keySet()){
						map1.putSum(s, prt1.value(s)*dArea/ply1.area());
					}
				}
			}
		}
		return map1;
	}


	private static ArrayList<String> printComponents(SphericalCapEarth cap1, Set<SphericalMultiPolygon> setPolygons, String sCapID){
		
		//lst1 = output
		//dArea = current area
		
		ArrayList<String> lstOut;
		double dArea;
		
		lstOut = new ArrayList<String>(setPolygons.size());
		for(SphericalMultiPolygon ply1:setPolygons){
			dArea = ply1.area(cap1, true);
			if(dArea>0){
				if(dArea>ply1.area()){
					dArea=ply1.area();
				}
				lstOut.add(
						sCapID + "," + 
						cap1.radius() + "," + 
						cap1.area() + "," + 
						cap1.centerLatitude() + "," +
						cap1.centerLongitude() + "," +
						ply1.id() + "," + 
						dArea/ply1.area());
			}
		}
		return lstOut;
	}
}