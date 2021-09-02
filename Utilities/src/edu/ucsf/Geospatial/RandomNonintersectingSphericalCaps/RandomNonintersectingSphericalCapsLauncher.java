package edu.ucsf.Geospatial.RandomNonintersectingSphericalCaps;

import java.util.ArrayList;

import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Generates random points within polygon.
 * @author jladau
 */

public class RandomNonintersectingSphericalCapsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shp1 = shapefile
		//rgd1 = random points
		//lstOut = output
		//lst1 = list of spherical caps
		//iCounter = number of tries
		//cap1 = current candidate cap
		//bSave = flag for whether to save current cap
		//ply1 = polygon
		//i1 = previous number of caps found
		//lstBoundingCaps = list of initial caps
		//datBoundingCaps = list of initial caps
		//bBounding = flag for whether bounding cap found
		//bOverlapping = flag for whether to allow caps to overlap
		
		boolean bOverlapping;
		boolean bBounding;
		SphericalMultiPolygon ply1;
		boolean bSave;
		ArgumentIO arg1;
		ShapefileIO shp1;
		double rgd1[][];
		ArrayList<String> lstOut;
		ArrayList<SphericalCapEarth> lst1;
		int iCounter;
		SphericalCapEarth cap1;
		int i1;
		ArrayList<SphericalCapEarth> lstBoundingCaps;
		DataIO datBoundingCaps;
		
		arg1 = new ArgumentIO(rgsArgs);
		if(!arg1.containsArgument("bOverlapping")){
			bOverlapping=false;
		}else{
			bOverlapping = arg1.getValueBoolean("bOverlapping");
		}
		lst1 = new ArrayList<SphericalCapEarth>(arg1.getValueInt("iCaps"));
		shp1 = new ShapefileIO(arg1.getValueString("sShapefilePath"), arg1.getValueString("sIDHeader"));
		if(shp1.loadFeature(arg1.getValueString("sPolygonID"))==1){
			throw new Exception("ERROR: Polygon not found.");
		}
		ply1 = shp1.getPolygon();
		iCounter = 0;
		if(arg1.containsArgument("sBoundingCapsPath")){
			datBoundingCaps = new DataIO(arg1.getValueString("sBoundingCapsPath"));
			lstBoundingCaps = new ArrayList<SphericalCapEarth>(datBoundingCaps.iRows);
			for(int i=1;i<datBoundingCaps.iRows;i++){
				lstBoundingCaps.add(new SphericalCapEarth(
						datBoundingCaps.getDouble(i, "RADIUS"),
						datBoundingCaps.getDouble(i, "LATITUDE_CENTER"),
						datBoundingCaps.getDouble(i, "LONGITUDE_CENTER"),
						arg1.getValueInt("iRandomSeed")*11));
			}
		}else{
			lstBoundingCaps=null;
		}
		
		do{	
			i1 = lst1.size();
			rgd1 = ply1.generateRandomPointsInPolygon(2000, arg1.getValueInt("iRandomSeed"));
			for(int i=0;i<rgd1.length;i++){
				cap1 = new SphericalCapEarth(arg1.getValueDouble("dRadius"), rgd1[i][0], rgd1[i][1], 1234 + 7*i);
				if(!ply1.contains(cap1)){
					continue;
				}
				if(lstBoundingCaps!=null){
					bBounding = false;
					for(SphericalCapEarth cap2:lstBoundingCaps){
						if(cap2.contains(cap1)){
							bBounding=true;
							break;
						}
					}
					if(bBounding==false){
						continue;
					}
				}
				bSave=true;
				if(bOverlapping==false){
					for(SphericalCapEarth cap2:lst1){
						if(cap1.intersects(cap2)){
							bSave = false;
							break;
						}
					}
				}
				if(bSave==true){
					lst1.add(cap1);
					if(lst1.size()==arg1.getValueInt("iCaps")){
						break;
					}
				}	
			}
			iCounter++;
		}while(lst1.size()<arg1.getValueInt("iCaps") && iCounter<10 && lst1.size()>i1);
		
		
		lstOut = new ArrayList<String>(rgd1.length+1);
		lstOut.add("ID_CAP,RADIUS,LATITUDE_CENTER,LONGITUDE_CENTER");
		iCounter = 1;
		for(SphericalCapEarth cap2:lst1){
			lstOut.add(iCounter + "," + arg1.getValueDouble("dRadius") + "," + cap2.centerLatitude() + "," + cap2.centerLongitude());
			iCounter++;
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
