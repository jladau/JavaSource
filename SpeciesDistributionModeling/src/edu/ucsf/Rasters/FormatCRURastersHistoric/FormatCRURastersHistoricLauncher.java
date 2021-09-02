package edu.ucsf.Rasters.FormatCRURastersHistoric;

import com.google.common.collect.Range;
import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.NetcdfWriter;

/**
 * This code formats historic CRU rasters
 * @author jladau
 */

public class FormatCRURastersHistoricLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//ncr1 = netcdf readers
		//sPath = raster path
		//ras1 = output raster
		//itr1 = lat-lon iterator
		//cel1 = current cell
		
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		NetcdfReader ncr1;
		String sPath;
		GeospatialRaster ras1;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		
		//outputting warning
		System.out.println("NOTE: time intervals cannot be overlapping. Use only the end year for specifying climatologies");
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//initializing rasters
		sPath = arg1.getValueString("sRasterPath");
		ncr1 = new NetcdfReader(sPath);
		
		//loading raster
		ras1 = new GeospatialRaster(ncr1.dLatResolution, ncr1.dLonResolution, ncr1.getLatRange(), ncr1.getLonRange(), ncr1.gmt1);
		ras1.gmt1.variable=arg1.getValueString("sVariable");
		ras1.gmt1.source="http://catalogue.ceda.ac.uk/uuid/5dca9487dc614711a3a933e44a933ad3";
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; Climate Research Unit TS v. 3.23";
		ras1.gmt1.long_name=arg1.getValueString("sLongName");
		ras1.gmt1.cell_methods="area: mean time: mean (interval: " + arg1.getValueInt("iClimatologyYears") + " years)";
		ras1.gmt1.institution="Climatic Research Unit (CRU)";
		ras1.gmt1.references="University of East Anglia Climatic Research Unit; Harris, I.(.; Jones, P.D. (2015): CRU TS3.23: Climatic Research Unit (CRU) Time-Series (TS) Version 3.23 of High Resolution Gridded Data of Month-by-month Variation in Climate (Jan. 1901- Dec. 2014). Centre for Environmental Data Analysis, 09 November 2015. doi:10.5285/4c7fdfa6-f176-4c58-acee-683d5e9d2ed5. http://dx.doi.org/10.5285/4c7fdfa6-f176-4c58-acee-683d5e9d2ed5";
		ras1.gmt1.title=arg1.getValueString("sTitle");
		
		//closing reader
		ncr1.close();
		
		//adding vert value
		ras1.addNullVert();
		
		//adding times
		for(int i=0;i<arg1.getValueTimeArray("rgtStartTimesNew").length;i++){
		
			//adding time
			ras1.addTime(Range.closed(arg1.getValueTimeArray("rgtStartTimesNew")[i], arg1.getValueTimeArray("rgtEndTimesNew")[i]));
		}
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));				
		
		//looping through times
		for(int i=0;i<arg1.getValueTimeArray("rgtStartTimesNew").length;i++){
		
			//updating progress
			System.out.println("Outputting data for " + arg1.getValueTimeArray("rgtStartTimesNew")[i] + "...");
			
			//loading grid
			ncr1 = new NetcdfReader(sPath, arg1.getValueString("sVariable") + "_" + arg1.getValueTimeArray("rgtStartTimesOld")[i] + "--" + arg1.getValueTimeArray("rgtEndTimesOld")[i]);
			ncr1.loadGrid(GeospatialRaster.NULL_TIME, GeospatialRaster.NULL_VERT);
			
			//transferring grid to raster that is being written
			itr1 = ncr1.getLatLonIterator(GeospatialRaster.NULL_TIME, GeospatialRaster.NULL_VERT);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				ras1.put(cel1.axeLat.ID, cel1.axeLon.ID, arg1.getValueTimeArray("rgtStartTimesNew")[i], GeospatialRaster.NULL_VERT, ncr1.get(cel1));
			}
		
			//closing reader
			ncr1.close();
			Thread.sleep(500);
		
			//writing raster
			ncw1.writeRaster(ras1, arg1.getValueTimeArray("rgtStartTimesNew")[i], GeospatialRaster.NULL_VERT);
		
			//clearing raster
			ras1.clear(arg1.getValueTimeArray("rgtStartTimesNew")[i], GeospatialRaster.NULL_VERT);
		}
		
		//terminating
		ncw1.close();
		ncr1.close();
		System.out.println("Done.");
	}
}