package edu.ucsf.Rasters.FormatWOARastersMomean;

import org.joda.time.LocalDate;
import com.google.common.collect.Range;
import com.google.common.io.Files;

import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.AxisElement;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.NetcdfWriter;

public class FormatWOARastersMomeanLauncher {

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
		//datPaths = paths to rasters
		//sMonth = current month
		
		String sMonth;
		DataIO datPaths;
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
		
		//loading paths to rasters
		datPaths = new DataIO(arg1.getValueString("sRasterListPath"));
		
		//loading first raster for initializing output
		sPath = datPaths.getString(0, 0);
		ncr1 = new NetcdfReader(sPath,arg1.getValueString("sVariable"));
		
		//loading raster
		ras1 = new GeospatialRaster(ncr1.dLatResolution, ncr1.dLonResolution, ncr1.getLatRange(), ncr1.getLonRange(), ncr1.gmt1);
		ras1.gmt1.variable=arg1.getValueString("sVariableNameNew");
		ras1.gmt1.source=arg1.getValueString("sSource");
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; World Ocean Atlas 2013 version 2";
		ras1.gmt1.long_name=arg1.getValueString("sLongName");
		ras1.gmt1.cell_methods="area: mean depth:mean time: mean within years time: mean over years";
		
		//adding vert values
		for(AxisElement<Double> axe1:ncr1.axsVert.getAxisElements()){
			rng1 = Range.closed(-1.*axe1.rngAxisValues.upperEndpoint(), -1.*axe1.rngAxisValues.lowerEndpoint());
			ras1.addVert(rng1);
		}
		
		//adding time values
		for(int i=1;i<=12;i++){
			rngTime = Range.closed(new LocalDate(arg1.getValueInt("iStartYear"),i,1), new LocalDate(arg1.getValueInt("iEndYear"),i,1));
			ras1.addTime(rngTime);
		}
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//looping through rasters
		for(int i=0;i<datPaths.iRows;i++){
		
			//loading reader
			ncr1 = new NetcdfReader(datPaths.getString(i, 0),arg1.getValueString("sVariable"));
			
			//loading time
			sMonth = Files.getNameWithoutExtension(ncr1.sPath).split("_")[2].substring(1,3);
			tim1 = new LocalDate(arg1.getValueInt("iStartYear"), Integer.parseInt(sMonth), 15);
			
			//updating progress
			System.out.print("Writing to " + tim1);
			
			//writing output
			for(AxisElement<Double> axeVert:ras1.axsVert.getAxisElements()){
			
				//reading grid
				ncr1.loadGrid(ncr1.axsTime.firstKey(), -1.*axeVert.ID);
			
				//updating progress
				System.out.print(".");
				
				//transferring grid to raster that is being written
				itr1 = ncr1.getLatLonIterator(ncr1.axsTime.firstKey(), -1.*axeVert.ID);
				while(itr1.hasNext()){
					cel1 = itr1.next();
					ras1.put(cel1.axeLat.ID, cel1.axeLon.ID, tim1, axeVert.ID, ncr1.get(cel1));
				}
				
				//writing raster
				ncw1.writeRaster(ras1, tim1, axeVert.ID);
				ncr1.clear(ncr1.axsTime.firstKey(), -1.*axeVert.ID);
				ras1.clear(tim1, axeVert.ID);
			}
			
			//updating progress
			System.out.println("");
		}
		
		//terminating
		ncr1.close();
		ncw1.close();
		System.out.println("Done.");
	}
}
