package edu.ucsf.Geospatial.LargestCapsInPolygon;

import java.util.ArrayList;

import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Finds the largest caps with specified center coordinates within a polyong
 * @author jladau
 *
 */

public class LargestCapsInPolygonLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//dat1 = data
		//arg1 = arguments
		//shp1 = shapefile
		//ply1 = polygon
		//cap1 = current cap
		//dLat = center latitude
		//dLon = center longitude
		//dStep = current radius step size
		//dRadius = current radius
		//lstOut = output
		//bContains = flag for whether polygon containss cap
		//bContinue = flag for whether to continue
		//i1 = iterations count
		
		boolean bContinue;
		int i1;
		SphericalCapEarth cap1;
		DataIO dat1;
		ArgumentIO arg1;
		ShapefileIO shp1;
		SphericalMultiPolygon ply1;
		double dLat;
		double dLon;
		double dStep;
		double dRadius;
		ArrayList<String> lstOut;
		boolean bContains;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		shp1 = new ShapefileIO(arg1.getValueString("sShapefilePath"), arg1.getValueString("sIDHeader"));
		if(shp1.loadFeature(arg1.getValueString("sPolygonID"))==1){
			throw new Exception("ERROR: Polygon not found.");
		}
		ply1 = shp1.getPolygon();
		if(dat1.hasHeader("Latitude")){
			dat1.setString(0, "Latitude", "LATITUDE");
		}
		if(dat1.hasHeader("Longitude")){
			dat1.setString(0, "Longitude", "LONGITUDE");
		}
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("ID_CAP,RADIUS,LATITUDE_CENTER,LONGITUDE_CENTER");
		
		//looping through points	
		for(int i=1;i<dat1.iRows;i++){
			dLat = dat1.getDouble(i, "LATITUDE");
			dLon = dat1.getDouble(i, "LONGITUDE");
			dRadius = 10000;
			dStep = dRadius;
			cap1 = new SphericalCapEarth(dRadius, dLat, dLon, 1234 + 7*i);
			bContains = ply1.contains(cap1);
			i1 = 0;
			if(ply1.contains(dLat,dLon)){
				do{
					if(bContains){
						dStep = Math.abs(dStep/2.);
					}else{
						dStep = -Math.abs(dStep/2.);
					}
					dRadius+=dStep;
					cap1 = new SphericalCapEarth(dRadius, dLat, dLon, 1234 + 7*i);
					bContains = ply1.contains(cap1);
					i1++;
					if(i1>1000){
						throw new Exception("ERROR: Maximum number of iterations exceeded.");
					}else{
						if(Math.abs(dStep)>0.01 || bContains==false){
							bContinue=true;
						}else{
							bContinue=false;
						}
					}
				}while(bContinue==true);
				lstOut.add(i + "," + cap1.radius() + "," + cap1.centerLatitude() + "," + cap1.centerLongitude());
			}
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
