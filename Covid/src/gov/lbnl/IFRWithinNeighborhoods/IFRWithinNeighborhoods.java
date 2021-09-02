package gov.lbnl.IFRWithinNeighborhoods;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

public class IFRWithinNeighborhoods{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//mapIFR = map from county fips to ifr values
		//datSampleLocations = sample locations
		//cap1 = spherical cap object
		//dRadius = neighborhood size (km)
		//iPoints = number of points per neighborhood
		//sSample = current sample
		//dLat = current latitude
		//dLon = current longitude
		//lst1 = list of random points
		//shp1 = shapefile
		//map1 = map from polygon ids to polygons (from shapefiles)
		//ply1 = current polygon
		//lstOut = output
		//i1 = current number of points added
		
		int i1;
		ArrayList<String> lstOut;
		SphericalMultiPolygon ply1;
		ArrayList<Double[]> lst1;
		ArgumentIO arg1;
		HashMap<String,Double> mapIFR;
		DataIO datSampleLocations;
		SphericalCapEarth cap1;
		double dRadius;
		int iPoints;
		String sSample;
		double dLat;
		double dLon;
		ShapefileIO shp1;
		HashMap<String,SphericalMultiPolygon> map1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		mapIFR = (new DataIO(arg1.getValueString("sCountyIFRMapPath"))).getDoubleMap(new String[] {"COUNTY_FIPS"},new String[] {"COUNTY_RATE"});
		datSampleLocations = new DataIO(arg1.getValueString("sSampleLocationPath"));
		iPoints = arg1.getValueInt("iPointsPerNeighborhood");
		dRadius = arg1.getValueDouble("dNeighborhoodRadius");
		shp1 = new ShapefileIO(arg1.getValueString("sCountyShapefilePath"),"GEOID");
		map1 = new HashMap<String,SphericalMultiPolygon>(4000);
		do{
			shp1.next();
			ply1 = shp1.getPolygon();
			map1.put(shp1.getID(),ply1);
		}while(shp1.hasNext());
		lstOut = new ArrayList<String>(datSampleLocations.iRows*iPoints);
		lstOut.add("SAMPLE_ID,NEIGHBORHOOD_POINT_ID,NEIGHBORHOOD_POINT_LATITUDE,NEIGHBORHOOD_POINT_LONGITUDE,NEIGHBORHOOD_POINT_COUNTY_FIPS,NEIGHBORHOOD_POINT_IFR");
		
		//looping through points
		for(int i=1;i<datSampleLocations.iRows;i++) {
			
			
			sSample = datSampleLocations.getString(i,"SAMPLE_ID");
			
			System.out.println("Finding IFR values for sample " + i + " of " + datSampleLocations.iRows + "...");
			
			cap1 = new SphericalCapEarth(
					dRadius, 
					datSampleLocations.getDouble(i,"LATITUDE"), 
					datSampleLocations.getDouble(i,"LONGITUDE"), 
					i*7+17);
			i1 = 0;
			lst1 = cap1.randomPointsInCap(10*iPoints);
			i1 = 0;
			for(int k=0;k<lst1.size();k++){
				dLat = lst1.get(k)[0];
				dLon = lst1.get(k)[1];
				for(String s:map1.keySet()) {
					if(map1.get(s).contains(dLat,dLon) && mapIFR.containsKey(s)) {
						lstOut.add(sSample + "," +
								i1 + "," +
								dLat + "," +
								dLon + "," +
								s + "," +
								mapIFR.get(s));
						i1++;
						break;
					}
				}
				if(i1==iPoints) {
					break;
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
