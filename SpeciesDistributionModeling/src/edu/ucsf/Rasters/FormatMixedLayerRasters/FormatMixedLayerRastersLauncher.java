package edu.ucsf.Rasters.FormatMixedLayerRasters;

import org.joda.time.LocalDate;

import com.google.common.collect.Range;

import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.AxisElement;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.NetcdfWriter;

/**
 * This code formats mixed layer depth rasters
 * @author jladau
 */

public class FormatMixedLayerRastersLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//ncr1 = netcdf reader
		//sPath = path to input file
		//ras1 = output raster
		//sCode = current date code
		//tim1 = current time
		//itr1 = lat-lon iterator
		//cel1 = current cell
		//dLon = corrected longitude
		//sStartYear = starting year
		//sEndYear = ending year
		
		String sStartYear;
		String sEndYear;
		double dLon;
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		NetcdfReader ncr1;
		String sPath;
		GeospatialRaster ras1;
		LocalDate tim1;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading rasters
		sPath = arg1.getValueString("sRasterPath");
		ncr1 = new NetcdfReader(sPath,arg1.getValueString("sVariable"));
		
		//loading raster
		ras1 = new GeospatialRaster(ncr1.dLatResolution, ncr1.dLonResolution, Range.closed(-89.0, 89.0), Range.closed(-181.0, 181.0), ncr1.gmt1);
		ras1.gmt1.variable=arg1.getValueString("sVariableNameNew");
		ras1.gmt1.source=arg1.getValueString("sSource");
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; " + arg1.getValueString("sHistory");
		ras1.gmt1.long_name=arg1.getValueString("sLongName");
		ras1.gmt1.institution=arg1.getValueString("sInstitution");
		ras1.gmt1.cell_methods=arg1.getValueString("sCellMethods");
		ras1.gmt1.references=arg1.getValueString("sReferences");
		ras1.gmt1.title=arg1.getValueString("sTitle");
		ras1.gmt1.units=arg1.getValueString("sUnits");
		
		//adding time values
		sStartYear = arg1.getValueString("sStartYear");
		sEndYear = arg1.getValueString("sEndYear");
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-01-01"), new LocalDate(sEndYear + "-01-31")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-02-01"), new LocalDate(sEndYear + "-02-28")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-03-01"), new LocalDate(sEndYear + "-03-31")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-04-01"), new LocalDate(sEndYear + "-04-30")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-05-01"), new LocalDate(sEndYear + "-05-31")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-06-01"), new LocalDate(sEndYear + "-06-30")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-07-01"), new LocalDate(sEndYear + "-07-31")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-08-01"), new LocalDate(sEndYear + "-08-31")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-09-01"), new LocalDate(sEndYear + "-09-30")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-10-01"), new LocalDate(sEndYear + "-10-31")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-11-01"), new LocalDate(sEndYear + "-11-30")));
		ras1.addTime(Range.closed(new LocalDate(sStartYear + "-12-01"), new LocalDate(sEndYear + "-12-31")));
		
		//adding vert dimensions
		for(int i=0;i<arg1.getValueDoubleArray("rgdVertUpper").length;i++){
			ras1.addVert(Range.closed(arg1.getValueDoubleArray("rgdVertLower")[i], arg1.getValueDoubleArray("rgdVertUpper")[i]));
		}
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//looping through months
		for(AxisElement<LocalDate> axeTime:ncr1.axsTime.getAxisElements()){
			
			//reading grid
			ncr1.loadGrid(axeTime.ID, GeospatialRaster.NULL_VERT);
			
			//loading time for writing
			tim1 = new LocalDate(Integer.parseInt(sStartYear),axeTime.ID.monthOfYear().get(),15);
			
			//looping through depths
			for(AxisElement<Double> axeVert:ras1.axsVert.getAxisElements()){
				
				System.out.println("Writing data to " + tim1 + ", depth " + axeVert.ID + "...");
				
				itr1 = ncr1.getLatLonIterator(axeTime.ID, GeospatialRaster.NULL_VERT);
				while(itr1.hasNext()){
					cel1 = itr1.next();
					if(cel1.axeLat.ID<=89){
						if(ncr1.get(cel1)!=1000000000){
							if(Math.abs(cel1.axeLon.ID-180.)<0.1){
								ras1.put(cel1.axeLat.ID, -180, tim1, axeVert.ID, axeVert.ID+Math.abs(ncr1.get(cel1)));
								ras1.put(cel1.axeLat.ID, 180, tim1, axeVert.ID, axeVert.ID+Math.abs(ncr1.get(cel1)));
							}else{
							
								if(cel1.axeLon.ID>180){
									dLon = cel1.axeLon.ID-360.;
								}else{
									dLon = cel1.axeLon.ID;
								}
								ras1.put(cel1.axeLat.ID, dLon, tim1, axeVert.ID, axeVert.ID+Math.abs(ncr1.get(cel1)));
							}
						}
					}
				}
				
				//writing raster
				ncw1.writeRaster(ras1, tim1, axeVert.ID);
				ras1.remove(tim1, axeVert.ID);	
			}
			
			//clearing grid
			ncr1.remove(axeTime.ID, GeospatialRaster.NULL_VERT);
		}
		
		//terminating
		ncr1.close();
		ncw1.close();
		System.out.println("Done.");
	}
}
