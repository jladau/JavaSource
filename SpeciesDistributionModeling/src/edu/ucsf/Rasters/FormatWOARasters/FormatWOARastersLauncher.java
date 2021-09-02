package edu.ucsf.Rasters.FormatWOARasters;

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

public class FormatWOARastersLauncher {

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
		//rng1 = new range of depth values (negative)
		//rngTime = date range
		
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		NetcdfReader ncr1;
		String sPath;
		GeospatialRaster ras1;
		LocalDate tim1;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		Range<Double> rng1;
		Range<LocalDate> rngTime;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading rasters
		sPath = arg1.getValueString("sRasterPath");
		ncr1 = new NetcdfReader(sPath,arg1.getValueString("sVariable"));
		
		//loading raster
		ras1 = new GeospatialRaster(ncr1.dLatResolution, ncr1.dLonResolution, ncr1.getLatRange(), ncr1.getLonRange(), ncr1.gmt1);
		ras1.gmt1.variable=arg1.getValueString("sVariableNameNew");
		ras1.gmt1.source=arg1.getValueString("sSource");
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; World Ocean Atlas 2013 version 2";
		ras1.gmt1.long_name=arg1.getValueString("sLongName");
		
		//adding vert values
		for(AxisElement<Double> axe1:ncr1.axsVert.getAxisElements()){
			rng1 = Range.closed(-1.*axe1.rngAxisValues.upperEndpoint(), -1.*axe1.rngAxisValues.lowerEndpoint());
			ras1.addVert(rng1);
		}
		
		//adding time values
		rngTime = Range.closed(new LocalDate("1955-01-01"), new LocalDate("2012-12-31"));
		ras1.addTime(rngTime);
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//writing output
		tim1 = rngTime.lowerEndpoint();
		for(AxisElement<Double> axeVert:ras1.axsVert.getAxisElements()){
		
			System.out.println("Writing to " + tim1 + ", depth " + axeVert.ID + "...");
			for(AxisElement<LocalDate> axeTime:ncr1.axsTime.getAxisElements()){
				
				//reading grid
				ncr1.loadGrid(axeTime.ID, -1.*axeVert.ID);
				
				//transferring grid to raster that is being written
				itr1 = ncr1.getLatLonIterator(axeTime.ID, -1.*axeVert.ID);
				while(itr1.hasNext()){
					cel1 = itr1.next();
					ras1.put(cel1.axeLat.ID, cel1.axeLon.ID, tim1, axeVert.ID, ncr1.get(cel1));
				}
				
				//writing raster
				ncw1.writeRaster(ras1, tim1, axeVert.ID);
				ncr1.remove(axeTime.ID, -1.*axeVert.ID);
				ras1.remove(tim1, axeVert.ID);
			}
		}
		
		//terminating
		ncr1.close();
		ncw1.close();
		System.out.println("Done.");
	}
}
