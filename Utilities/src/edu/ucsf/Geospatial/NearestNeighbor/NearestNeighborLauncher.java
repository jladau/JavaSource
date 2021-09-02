package edu.ucsf.Geospatial.NearestNeighbor;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class NearestNeighborLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//s1 = category to find nearest neighbors for
		//s2 = category of neighbors
		//map1 = map from focal ids to latitudes and longitudes
		//map2 = map from neighbor ids to latitudes and longitudes
		//lstOut = output
		//ert1 = earth geometry object
		//d1 = current nearest neighbor distance
		//d2 = current candidate distance
		//dLat = current focal latitude
		//dLon = current focal longitude
		//dLat2 = latitude of nearest neighbor
		//dLon2 = longitude of nearest neighbor
		//s3 = id of current nearest neighbor
		//i1 = counter
		
		double dLat;
		double dLon;
		double dLat2;
		double dLon2;
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<String,Double[]> map1;
		HashMap<String,Double[]> map2;
		String s1;
		String s2;
		String s3;
		ArrayList<String> lstOut;
		EarthGeometry ert1;
		double d1;
		double d2;
		int i1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		s1 = arg1.getValueString("sFocalCategory");
		s2 = arg1.getValueString("sNeighborCategory");
		map1 = new HashMap<String,Double[]>(dat1.iRows);
		map2 = new HashMap<String,Double[]>(dat1.iRows);
		i1 = 0;
		for(int i=1;i<dat1.iRows;i++){
			if(!Double.isNaN(dat1.getDouble(i, "LATITUDE")) && !Double.isNaN(dat1.getDouble(i, "LONGITUDE"))){
				if(dat1.getString(i, "CATEGORY").equals(s1)){
					map1.put(dat1.getString(i, "ID") + "-" + i1, new Double[]{dat1.getDouble(i, "LATITUDE"),dat1.getDouble(i, "LONGITUDE")});
				}else if(dat1.getString(i, "CATEGORY").equals(s2)){
					map2.put(dat1.getString(i, "ID") + "-" + i1, new Double[]{dat1.getDouble(i, "LATITUDE"),dat1.getDouble(i, "LONGITUDE")});
				}
			}
			i1++;
		}
		
		//finding distances and outputting results
		lstOut = new ArrayList<String>(map1.size()+1);
		ert1 = new EarthGeometry();
		lstOut.add("ID_FOCAL,LATITUDE_FOCAL,LONGITUDE_FOCAL,ID_NEAREST_NEIGHBOR,NEAREST_NEIGHBOR_LATITUDE,NEAREST_NEIGHBOR_LONGITUDE,DISTANCE_KM");
		for(String s:map1.keySet()){
			d1 = Double.MAX_VALUE;
			s3 = null;
			dLat2 = Double.NaN;
			dLon2 = Double.NaN;
			dLat = map1.get(s)[0];
			dLon = map1.get(s)[1];
			for(String t:map2.keySet()){
				d2 = ert1.orthodromicDistanceWGS84(dLat, dLon, map2.get(t)[0], map2.get(t)[1]);
				if(d2<d1){
					d1 = d2;
					s3 = t;
					dLat2 = map2.get(t)[0];
					dLon2 = map2.get(t)[1];
				}
			}
			lstOut.add(s + "," + dLat + "," + dLon + "," + s3 + "," + dLat2 + "," + dLon2 + "," + d1);
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
