package edu.ucsf.Geospatial.RasterValues;

import org.joda.time.LocalDate;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;

/**
 * This code extracts values from a raster and appends them to a text file.
 * @author jladau
 */

public class RasterValuesLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data file
		//ncf1 = netcdf object
		//dLat = latitude
		//dLon = longitude
		//dVert = vert value
		//tim1 = time value
		
		double dVert = Double.NaN;
		LocalDate tim1 = null;
		ArgumentIO arg1;
		DataIO dat1;
		NetcdfReader cdf1;
		double dLat;
		double dLon;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		cdf1 = new NetcdfReader(arg1.getValueString("sRasterPath"), arg1.getValueString("sRasterVariable"));
		
		//looping through location and appending values
		dat1.appendToLastColumn(0, arg1.getValueString("sRasterVariable"));
		for(int i=1;i<dat1.iRows;i++){
			dLat = dat1.getDouble(i, "LATITUDE");
			dLon = dat1.getDouble(i, "LONGITUDE");
			try{
				tim1 = new LocalDate(dat1.getString(i, "DATE"));
			}catch(Exception e){
				tim1 = null;
			}
			dat1.appendToLastColumn(i,cdf1.readValue(dLat, dLon, tim1, 0));
		}
		
		//terminating
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		cdf1.close();
		System.out.println("Done.");
	}
}
