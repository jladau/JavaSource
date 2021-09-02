package edu.ucsf.Rasters.AcsiiToNetcdf;

import org.joda.time.LocalDate;

import com.google.common.collect.Range;
import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ESRIAsciiReader;
import edu.ucsf.io.NetcdfWriter;

/**
 * Converts arcgis ascii files to netcdf
 * @author jladau
 */

public class AsciiToNetcdfLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//ras1 = output raster
		//sCode = current date code
		//rngTime = date range
		//datRasters = rasters paths and date ranges
		//gmt1 = geospatial metadata object
		//usg1 = usage object
		
		Usage usg1;
		DataIO datRasters;
		ArgumentIO arg1;
		NetcdfWriter ncw1=null;
		ESRIAsciiReader asc1;
		GeospatialRaster ras1;
		Range<LocalDate> rngTime;
		GeospatialRasterMetadata gmt1;
		
		//loading usage
		usg1 = new Usage(new String[]{
			"AsciiToNetcdf",
			"sOutputPath"
			});
		usg1.printUsage(rgsArgs);
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading raster data
		datRasters = new DataIO(arg1.getValueString("sRasterDataPath"));
		
		//initializing ascii reader
		gmt1 = new GeospatialRasterMetadata(
				arg1.getValueString("sNetCDFTitle"),
				arg1.getValueString("sNetCDFInstitution"),
				arg1.getValueString("sNetCDFReferences"),
				arg1.getValueString("sNetCDFSource"),
				"Formatted on " + CurrentDate.currentDate() + "; " + arg1.getValueString("sNetCDFHistory"),
				arg1.getValueString("sNetCDFVariable"),
				arg1.getValueString("sNetCDFUnits"),
				arg1.getValueString("sNetCDFLongName"),
				arg1.getValueString("sNetCDFCellMethods"));
		asc1 = new ESRIAsciiReader(
				datRasters.getString(1, "ASCII_RASTER_PATH"), 
				Range.closed(new LocalDate(datRasters.getString(1, "CLIMATOLOGY_START_DATE")), new LocalDate(datRasters.getString(1, "CLIMATOLOGY_END_DATE"))), 
				gmt1);
		
		//initializing array of rasters
		ras1 = new GeospatialRaster(
				asc1.dCellSize, 
				asc1.dCellSize, 
				asc1.getLatRange(), 
				asc1.getLonRange(), 
				gmt1);
		ras1.addNullVert();
		for(int i=1;i<datRasters.iRows;i++){
			
			//adding time and vert values
			rngTime = Range.closed(new LocalDate(datRasters.getString(i, "CLIMATOLOGY_START_DATE")), new LocalDate(datRasters.getString(i, "CLIMATOLOGY_END_DATE")));
			ras1.addTime(rngTime);
		}
		
		//initializing writer
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//writing data
		for(int i=1;i<datRasters.iRows;i++){			
			asc1 = new ESRIAsciiReader(
					datRasters.getString(i, "ASCII_RASTER_PATH"), 
					Range.closed(new LocalDate(datRasters.getString(i, "CLIMATOLOGY_START_DATE")), new LocalDate(datRasters.getString(i, "CLIMATOLOGY_END_DATE"))), 
					gmt1);
			while(asc1.hasNext()){	
				asc1.loadNextLine();
				ncw1.writeRaster(asc1, new LocalDate(datRasters.getString(i, "CLIMATOLOGY_START_DATE")), GeospatialRaster.NULL_VERT, asc1.currentLatitude());
			}
		}
		
		//terminating
		ncw1.close();
		System.out.println("Done.");
	}
}
