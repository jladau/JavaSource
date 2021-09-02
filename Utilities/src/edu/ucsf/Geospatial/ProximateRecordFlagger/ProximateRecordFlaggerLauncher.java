package edu.ucsf.Geospatial.ProximateRecordFlagger;

import java.util.ArrayList;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Flags records that are close to a specified set of coordinates
 * @author jladau
 */

public class ProximateRecordFlaggerLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//datCoords = coordinates to check against
		//geo1 = EarthGeometry object
		//sRecordIDField = record ID field
		//rgd1 = lat1, lon1, lat2, lon2
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		DataIO datCoords;
		EarthGeometry geo1;
		String sRecordIDField;
		double rgd1[];
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		datCoords = new DataIO(arg1.getValueString("sCoordinatesPath"));
		geo1 = new EarthGeometry();
		sRecordIDField = arg1.getValueString("sRecordIDField");
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("RECORD_ID,LATITUDE_RECORD,LONGITUDE_RECORD,LATITUDE_PROXIMATE_POINT,LONGITUDE_PROXIMATE_POINT");
		
		//looping through data and checking if coordinates are close
		for(int i=1;i<dat1.iRows;i++){
			for(int j=1;j<datCoords.iRows;j++){
				rgd1 = new double[]{dat1.getDouble(i, "LATITUDE"), dat1.getDouble(i, "LONGITUDE"), datCoords.getDouble(j, "LATITUDE"), datCoords.getDouble(j, "LONGITUDE")};
				
				try{
					if(geo1.orthodromicDistanceWGS84(rgd1[0], rgd1[1], rgd1[2], rgd1[3])<arg1.getValueDouble("dNeighborhood")){
						lstOut.add(dat1.getString(i, sRecordIDField) + "," + rgd1[0] + "," + rgd1[1] + "," + rgd1[2] + "," + rgd1[3]);
					}
				}catch(Exception e){
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");	
	}
}
