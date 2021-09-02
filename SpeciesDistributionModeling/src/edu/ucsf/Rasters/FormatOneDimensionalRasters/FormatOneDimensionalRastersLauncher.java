package edu.ucsf.Rasters.FormatOneDimensionalRasters;

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

public class FormatOneDimensionalRastersLauncher {

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
		//rngTime = date range
		
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		NetcdfReader ncr1;
		String sPath;
		GeospatialRaster ras1;
		LocalDate tim1;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
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
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; " + arg1.getValueString("sHistory");
		ras1.gmt1.long_name=arg1.getValueString("sLongName");
		ras1.gmt1.institution=arg1.getValueString("sInstitution");
		ras1.gmt1.cell_methods=arg1.getValueString("sCellMethods");
		ras1.gmt1.references=arg1.getValueString("sReferences");
		ras1.gmt1.title=arg1.getValueString("sTitle");
		ras1.gmt1.units=arg1.getValueString("sUnits");
		
		//adding time values
		rngTime = Range.closed(arg1.getValueTime("timClimatologyStart"), arg1.getValueTime("timClimatologyEnd"));
		ras1.addTime(rngTime);
		
		//adding vert dimension
		ras1.addNullVert();
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//writing output
		tim1 = rngTime.lowerEndpoint();
		System.out.println("Writing to " + tim1 + "...");
		for(AxisElement<LocalDate> axeTime:ncr1.axsTime.getAxisElements()){
			
			//reading grid
			ncr1.loadGrid(axeTime.ID, GeospatialRaster.NULL_VERT);
			
			//transferring grid to raster that is being written
			itr1 = ncr1.getLatLonIterator(axeTime.ID,GeospatialRaster.NULL_VERT);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				ras1.put(cel1.axeLat.ID, cel1.axeLon.ID, tim1, GeospatialRaster.NULL_VERT, ncr1.get(cel1));
			}
			
			//writing raster
			ncw1.writeRaster(ras1, tim1, GeospatialRaster.NULL_VERT);
			ncr1.remove(axeTime.ID, GeospatialRaster.NULL_VERT);
			ras1.remove(tim1, GeospatialRaster.NULL_VERT);
		}
		
		//terminating
		ncr1.close();
		ncw1.close();
		System.out.println("Done.");
	}
}
