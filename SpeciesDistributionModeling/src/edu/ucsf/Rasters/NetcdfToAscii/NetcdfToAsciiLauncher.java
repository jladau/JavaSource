package edu.ucsf.Rasters.NetcdfToAscii;

import java.util.ArrayList;
import java.util.Iterator;
import org.joda.time.LocalDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;

public class NetcdfToAsciiLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncr1 = netcdf reader
		//sPath = path to input file
		//sCode = current date code
		//tim1 = current time
		//lstOut = output
		//sbl1 = current output line
		//itrLat = latitude iterator
		//itrLon = longitude iterator
		//dLat = current latitude		
		//dLon = current longitude
		//tim1 = time to use
		//dVert = vert to use
		//dValue = current value
		
		ArgumentIO arg1;
		NetcdfReader ncr1;
		String sPath;
		LocalDate tim1;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		Iterator<Double> itrLat;
		Iterator<Double> itrLon;
		double dLat;
		double dLon;
		double dVert;
		double dValue;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading rasters
		sPath = arg1.getValueString("sRasterPath");
		ncr1 = new NetcdfReader(sPath,arg1.getValueString("sVariable"));
		if(ncr1.hasTime()){
			if(ncr1.hasVert()){
				tim1 = arg1.getValueTime("timTime");
				dVert = arg1.getValueDouble("dVert");
			}else{
				tim1 = arg1.getValueTime("timTime");
				dVert = GeospatialRaster.NULL_VERT;
			}
		}else{
			if(ncr1.hasVert()){
				tim1 = GeospatialRaster.NULL_TIME;
				dVert = arg1.getValueDouble("dVert");
			}else{
				tim1 = GeospatialRaster.NULL_TIME;
				dVert = GeospatialRaster.NULL_VERT;
			}
		}
		ncr1.loadGrid(tim1, dVert);
				
		//initializing output
		lstOut = new ArrayList<String>(6+ncr1.axsLat.size());
		lstOut.add("NCOLS " + ncr1.axsLon.size());
		lstOut.add("NROWS " + ncr1.axsLat.size());
		lstOut.add("XLLCENTER " + ncr1.axsLat.firstKey());
		lstOut.add("YLLCENTER " + ncr1.axsLon.firstKey());
		lstOut.add("CELLSIZE " + ncr1.dLatResolution);
		lstOut.add("NODATA_VALUE -9999");
		
		//outputting data
		itrLat = ncr1.axsLat.descendingKeySet().iterator();
		while(itrLat.hasNext()){
			itrLon = ncr1.axsLon.keySet().iterator();
			dLat = itrLat.next();
			System.out.println("Outputting data for latitude " + dLat + "...");
			sbl1 = new StringBuilder();
			while(itrLon.hasNext()){
				dLon = itrLon.next();
				dValue = ncr1.readValue(dLat,dLon,tim1,dVert);
				if(Double.isNaN(dValue)){
					sbl1.append("-9999 ");
				}else{
					sbl1.append(dValue + " ");
				}
			}
			lstOut.add(sbl1.toString().trim());
		}
		
		//printing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//terminating
		ncr1.close();
		System.out.println("Done.");
	}
}
