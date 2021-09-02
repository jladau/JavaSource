package edu.ucsf.Geospatial.DistancesBetweenLocations;

import java.util.ArrayList;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates the distances between all pairs of samples
 * @author jladau
 */

public class DistancesBetweenLocationsLauncher {

	public static void main(String rgsArgs[]){
		
		//geo1 = earth geometry object
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//dLat1 = current latitude 1
		//dLon1 = current longitude 1
		//dLat2 = current latitude 2
		//dLon2 = current longitude 2
		//s1 = first sample
		//s2 = second sample
		
		double dLat1;
		double dLon1;
		EarthGeometry geo1;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		double dLat2;
		double dLon2;
		String s1;
		String s2;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows*dat1.iRows);
		lstOut.add("ROW_1,ROW_2,SAMPLES_CATEGORY,DISTANCE");
		geo1 = new EarthGeometry();
		
		//looping through pairs of samples
		for(int i=2;i<dat1.iRows;i++){
			dLat1 = dat1.getDouble(i, "LATITUDE");
			dLon1 = dat1.getDouble(i, "LONGITUDE");
			if(arg1.containsArgument("sCategoryHeader")){	
				s1 = dat1.getString(i, arg1.getValueString("sCategoryHeader"));
			}else{
				s1 = "null";
			}
			for(int j=1;j<i;j++){
				dLat2 = dat1.getDouble(j, "LATITUDE");
				dLon2 = dat1.getDouble(j, "LONGITUDE");				
				if(arg1.containsArgument("sCategoryHeader")){	
					s2 = dat1.getString(j, arg1.getValueString("sCategoryHeader"));
				}else{
					s2 = "null";
				}
				if(s1.equals(s2)){
					try{
						lstOut.add(i + "," + j + "," + s1 + "," + EarthGeometry.orthodromicDistance(dLat1, dLon1, dLat2, dLon2));
					}catch(Exception e){
						System.out.println("Error on lines " + i + "," + j + ": " + dat1.getString(i, "LATITUDE") + "," + dat1.getString(i, "LONGITUDE") + "," + dat1.getString(j, "LATITUDE") + "," + dat1.getString(j, "LONGITUDE"));
					}
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
