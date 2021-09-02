package edu.ucsf.Geospatial.IdenticalLocationNoise;

import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code adds noise to observations from identical locations
 * @author jladau
 */

public class IdenticalLocationNoiseLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//map1 = returns the frequency of a given latitude/longitude combination
		//dLatNoise = latitude with noise
		//dLonNoise = longitude with noise
		//dLatNoiseFactor = latitude with noise factor
		//dLonNoiseFactor = longitude with noise factor
		//sLatLon = current latitude, longitude combination
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMap_AdditiveInteger<String> map1;
		double dLatNoise; double dLonNoise; double dLatNoiseFactor; double dLonNoiseFactor;
		String sLatLon;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading frequencies
		map1 = new HashMap_AdditiveInteger<String>();
		for(int i=1;i<dat1.iRows;i++){
			sLatLon = dat1.getString(i, "LATITUDE") + "," + dat1.getString(i, "LONGITUDE");
			map1.putSum(sLatLon, 1);
		}
		
		//appending locations with noise added
		dLatNoiseFactor = arg1.getValueDouble("dLatitudeNoiseFactor");
		dLonNoiseFactor = arg1.getValueDouble("dLongitudeNoiseFactor");
		dat1.appendToLastColumn(0, "LATITUDE_NOISE,LONGITUDE_NOISE");
		for(int i=1;i<dat1.iRows;i++){
			sLatLon = dat1.getString(i, "LATITUDE") + "," + dat1.getString(i, "LONGITUDE");
			if(map1.get(sLatLon)>1){	
				dLatNoise=dat1.getDouble(i, "LATITUDE") + (Math.random()-0.5)*dLatNoiseFactor;;
				dLonNoise=dat1.getDouble(i, "LONGITUDE") + (Math.random()-0.5)*dLonNoiseFactor;
			}else{
				dLatNoise=dat1.getDouble(i, "LATITUDE");
				dLonNoise=dat1.getDouble(i, "LONGITUDE");
			}
			dat1.appendToLastColumn(i,dLatNoise + "," + dLonNoise);
		}
		
		//printing results
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}
