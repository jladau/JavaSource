package edu.ucsf.Geospatial.SphericalCapsGivenTotal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import com.google.common.collect.HashMultimap;
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

public class SphericalCapsGivenTotalLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = centers of spherical caps
		//shp1 = shapefile
		//map1 = map from cap ids to cap objects
		//ply1 = current polygon
		//map2 = map from cap ids, polygon ids to areas of polygons within caps
		//lstOut = output
		//dat2 = map from shapefile polygon ids to variable of interest (to be held constant)
		//map3 = map from shapefile polygon ids to values of variable of interest
		//dTargetValue = target value
		//dEpsilon = allowable error
		//map5 = for each cap id returns intersecting polygons (at largest possible cap size)
		//dRadius = current radius
		//dStep = current radius step
		//dTotal = current total
		//cap1 = current cap
		//i1 = current iteration
		
		int i1;
		SphericalCapEarth cap1;
		double dTotal;
		ArgumentIO arg1;
		DataIO dat1;
		ShapefileIO shp1;
		HashMap<String,SphericalCapEarth> map1;
		SphericalMultiPolygon ply1;
		HashMap<String,Double> map2;
		ArrayList<String> lstOut;
		DataIO dat2;
		HashMap<String,Double> map3;
		double dTargetValue;
		double dEpsilon;
		HashMultimap<String,SphericalMultiPolygon> map5;
		double dRadius;
		double dStep;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sSphericalCapsPath"));
		dat2 = new DataIO(arg1.getValueString("sFactorValuesPath"));
		map3 = new HashMap<String,Double>(dat2.iRows);
		for(int i=1;i<dat2.iRows;i++){
			map3.put(dat2.getString(i, "POLYGON_ID"), dat2.getDouble(i, "POLYGON_VALUE"));
		}
		shp1 = new ShapefileIO(arg1.getValueString("sShapeFilePath"),arg1.getValueString("sIDHeader"));
		map1 = new HashMap<String,SphericalCapEarth>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, "ID_CAP"), new SphericalCapEarth(dat1.getDouble(i, "RADIUS"), dat1.getDouble(i, "LATITUDE_CENTER"), dat1.getDouble(i, "LONGITUDE_CENTER"),i+7*arg1.getValueInt("iRandomSeed")));
		}
		map2 = new HashMap<String,Double>(dat1.iRows*1000);
		lstOut = new ArrayList<String>(map2.size()+1);
		lstOut.add("ID_CAP,CAP_RADIUS,CAP_AREA,CAP_LATITUDE_CENTER,CAP_LONGITUDE_CENTER,ID_POLYGON,FRACTION_POLYGON_AREA");
		dTargetValue = arg1.getValueDouble("dTargetValue");
		dEpsilon = dTargetValue/1000.;
		map5 = HashMultimap.create(map1.size(),10);
		
		//finding fraction areas within largest possible caps
		while(shp1.hasNext()){
			shp1.next();
			System.out.println("Analyzing polygon " + shp1.getID() + "...");
			ply1 = shp1.getPolygon();
			ply1.setID(shp1.getID());
			ply1.area();
			for(String sCap:map1.keySet()){
				if(ply1.intersects(map1.get(sCap))){
					map5.put(sCap, ply1);
				}
			}
		}
		
		//finding correct sized caps
		for(String sCap:map1.keySet()){
			
			System.out.println("Analyzing cap " + sCap + "...");
			
			cap1 = map1.get(sCap);
			dTotal = totalValue(cap1, map5.get(sCap), map3);
			if(dTotal>dTargetValue){
				dRadius = cap1.radius();
				dStep = dRadius/2.;
				i1 = 0;
				while(Math.abs(dTotal-dTargetValue)>dEpsilon && i1<1000){
					if(dTotal>dTargetValue){
						dRadius-=dStep;
					}else{
						dRadius+=dStep;
					}
					cap1 = new SphericalCapEarth(dRadius,cap1.centerLatitude(),cap1.centerLongitude(),i1 + 1234);
					dTotal = totalValue(cap1, map5.get(sCap), map3);
					i1++;
					dStep=dStep/2.;
				}
				if(i1!=1000){
					lstOut.addAll(printComponents(cap1,map5.get(sCap),sCap));
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
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
	
	private static double totalValue(SphericalCapEarth cap1, Set<SphericalMultiPolygon> setPolygons, HashMap<String,Double> mapValues){
		
		//dArea = current area
		//dTotal = current total
		//dValue = value being added
		
		double dArea;
		double dTotal;
		double dValue;
		
		dTotal = 0;
		for(SphericalMultiPolygon ply1:setPolygons){
			dArea = ply1.area(cap1, true);
			if(dArea>0){
				if(dArea>ply1.area()){
					dArea=ply1.area();
				}
				if(!mapValues.containsKey(ply1.id())){
					dValue = 0.;
				}else{
					dValue = mapValues.get(ply1.id());
				}
				dTotal+=dValue*dArea/ply1.area();
			}
		}
		return dTotal;
	}
}