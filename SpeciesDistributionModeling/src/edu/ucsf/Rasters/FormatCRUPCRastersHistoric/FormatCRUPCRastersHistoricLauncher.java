package edu.ucsf.Rasters.FormatCRUPCRastersHistoric;

import java.util.HashSet;

import org.joda.time.LocalDate;

import com.google.common.collect.Range;
import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfWriter;

/**
 * This code formats historic CRU rasters
 * @author jladau
 */

public class FormatCRUPCRastersHistoricLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//sPath = raster path
		//ras1 = output raster
		//dat1 = file with PC data
		//rngLat = latitude range
		//rngLon = longitude range
		//rng1 = temporary range
		//setDates = set of climatology start and end dates (comma-separated)
		//rgs1 = current date in split format
		
		String rgs1[];
		HashSet<String> setDates;
		Range<Double> rngLat;
		Range<Double> rngLon;
		Range<Double> rng1;
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		DataIO dat1;
		String sPath;
		GeospatialRaster ras1;
		
		//outputting warning
		System.out.println("NOTE: time intervals cannot be overlapping. Use only the end year for specifying climatologies");
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//initializing rasters
		sPath = arg1.getValueString("sDataPath");
		dat1 = new DataIO(sPath);
		
		//loading latitude and longitude range	
		//TODO need to add a little bit to each range on either side: buffer
		rng1 = Range.encloseAll(dat1.getDoubleColumn("LATITUDE"));
		rngLat = Range.closed(Math.floor(rng1.lowerEndpoint()), Math.ceil(rng1.upperEndpoint())); 
		rng1 = Range.encloseAll(dat1.getDoubleColumn("LONGITUDE"));	
		rngLon = Range.closed(Math.floor(rng1.lowerEndpoint()), Math.ceil(rng1.upperEndpoint())); 
		
		//TODO add dLatResolution, dLonResolution arguments to script
		ras1 = new GeospatialRaster(
				arg1.getValueDouble("dLatResolution"), 
				arg1.getValueDouble("dLonResolution"), 
				rngLat, 
				rngLon, 
				new GeospatialRasterMetadata(null, null, null, null, null, null, null, null, null));
		ras1.gmt1.variable=arg1.getValueString("sVariable");
		ras1.gmt1.source="http://catalogue.ceda.ac.uk/uuid/5dca9487dc614711a3a933e44a933ad3";
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; Climate Research Unit TS v. 3.23";
		ras1.gmt1.long_name=arg1.getValueString("sLongName");
		ras1.gmt1.cell_methods="area: mean time: mean (interval: " + arg1.getValueInt("iClimatologyLengthYears") + " years)";
		ras1.gmt1.institution="Climatic Research Unit (CRU)";
		ras1.gmt1.references="University of East Anglia Climatic Research Unit; Harris, I.(.; Jones, P.D. (2015): CRU TS3.23: Climatic Research Unit (CRU) Time-Series (TS) Version 3.23 of High Resolution Gridded Data of Month-by-month Variation in Climate (Jan. 1901- Dec. 2014). Centre for Environmental Data Analysis, 09 November 2015. doi:10.5285/4c7fdfa6-f176-4c58-acee-683d5e9d2ed5. http://dx.doi.org/10.5285/4c7fdfa6-f176-4c58-acee-683d5e9d2ed5";
		ras1.gmt1.title=arg1.getValueString("sTitle");
		ras1.gmt1.units="na";
		
		
		//adding vert value
		ras1.addNullVert();
		
		//adding times
		setDates = new HashSet<String>();
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getInteger(i, "CLIMATOLOGY_LENGTH_YEARS")==arg1.getValueInt("iClimatologyLengthYears")){
				setDates.add(dat1.getString(i, "CLIMATOLOGY_START_DATE") + "," + dat1.getString(i, "CLIMATOLOGY_END_DATE"));
			}
		}
		for(String s:setDates){
			rgs1 = s.split(",");
			ras1.addTime(Range.closed(new LocalDate(rgs1[0]), new LocalDate(rgs1[1])));
		}
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));				
		
		//looping through times
		//TODO need to add a column in data file with start times: CLIMATOLOGY_START_DATE and CLIMATOLOGY_END_DATE
		//TODO need to supply iClimatologyLengthYears argument
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getInteger(i, "CLIMATOLOGY_LENGTH_YEARS")==arg1.getValueInt("iClimatologyLengthYears")){
				ras1.put(
						dat1.getDouble(i, "LATITUDE"), 
						dat1.getDouble(i, "LONGITUDE"), 
						new LocalDate(dat1.getString(i, "CLIMATOLOGY_START_DATE")), 
						GeospatialRaster.NULL_VERT, 
						dat1.getDouble(i, arg1.getValueString("sVariable")));
			}
		}
		
		//writing raster
		for(String s:setDates){
			ncw1.writeRaster(ras1, new LocalDate(s.split(",")[0]), GeospatialRaster.NULL_VERT);
		}
		
		//terminating
		ncw1.close();
		System.out.println("Done.");
	}
}