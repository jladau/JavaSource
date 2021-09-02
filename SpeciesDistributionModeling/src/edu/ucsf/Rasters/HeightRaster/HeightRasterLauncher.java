package edu.ucsf.Rasters.HeightRaster;

import com.google.common.collect.Range;
import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.GeospatialRaster.AxisElement;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfWriter;

public class HeightRasterLauncher {

public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//ras1 = output raster
		//sCode = current date code
		//itr1 = lat-lon iterator
		//cel1 = current cell
		
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		GeospatialRaster ras1;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading raster
		ras1 = new GeospatialRaster(
				1.0, 
				1.0, 
				Range.closed(-90.0, 90.0), 
				Range.closed(-180.0, 180.0), 
				new GeospatialRasterMetadata(
						"Height above sea level",
						"Gladstone Institutes, University of California San Francis",
						"https://github.com/jladau/JavaSource",
						"Generated using Utilities.edu.ucsf.Geospatial.DepthRaster.DepthRasterLauncher.java",
						"Formatted on " + CurrentDate.currentDate(),
						"height",
						"meters",
						"Height above sea level",
						"area: mean"
					));
		
		//adding time values
		ras1.addNullTime();
		
		//adding vert dimensions
		for(int i=0;i<arg1.getValueDoubleArray("rgdVertUpper").length;i++){
			ras1.addVert(Range.closed(arg1.getValueDoubleArray("rgdVertLower")[i], arg1.getValueDoubleArray("rgdVertUpper")[i]));
		}
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//looping through depths
		for(AxisElement<Double> axeVert:ras1.axsVert.getAxisElements()){
			
			System.out.println("Writing data to height " + axeVert.ID + "...");
			
			itr1 = ras1.getLatLonIterator(GeospatialRaster.NULL_TIME, axeVert.ID);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				ras1.put(cel1.axeLat.ID, cel1.axeLon.ID, GeospatialRaster.NULL_TIME, axeVert.ID, axeVert.ID);
			}
			
			//writing raster
			ncw1.writeRaster(ras1, GeospatialRaster.NULL_TIME, axeVert.ID);
			ras1.remove(GeospatialRaster.NULL_TIME, axeVert.ID);
			if(ras1.axsTime.size()==0){	
				ras1.addNullTime();
			}
		}
		
		//terminating
		ncw1.close();
		System.out.println("Done.");
}	
}
