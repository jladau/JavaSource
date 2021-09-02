package edu.ucsf.Geospatial.RasterValueExtractor;

import org.joda.time.LocalDate;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;

/**
 * This code extracts values from a raster and appends them to a text file.
 * @author jladau
 */

public class RasterValueExtractorLauncher {

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
		if(cdf1.hasVert()){
			if(!Double.isNaN(arg1.getValueDouble("dVert"))){
				dVert = arg1.getValueDouble("dVert");
			}else{
				throw new Error("Raster has vert axis, but vert not supplied.");
			}
		}
		if(cdf1.hasTime() && arg1.getValueTime("timDate")!=null){
			if(arg1.containsArgument("timDate")){
				tim1 = arg1.getValueTime("timDate");
			}else{
				throw new Error("Raster has time axis, but time not supplied.");
			}
		}
		
		//looping through location and appending values
		dat1.appendToLastColumn(0, arg1.getValueString("sRasterVariable"));
		for(int i=1;i<dat1.iRows;i++){
			dLat = dat1.getDouble(i, arg1.getValueString("sLatitudeHeader"));
			dLon = dat1.getDouble(i, arg1.getValueString("sLongitudeHeader"));
			dat1.appendToLastColumn(i,cdf1.readValue(dLat, dLon, tim1, dVert));
		}
		
		//terminating
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		cdf1.close();
		System.out.println("Done.");
	}
}
