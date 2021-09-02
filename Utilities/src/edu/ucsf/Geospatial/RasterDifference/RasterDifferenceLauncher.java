package edu.ucsf.Geospatial.RasterDifference;

import org.joda.time.LocalDate;
import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.NetcdfWriter;

/**
 * Computes difference between two rasters
 * @author jladau
 */
public class RasterDifferenceLauncher {

public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//ncr1 = first netcdf reader
		//ncr2 = second netcdf reader
		//sPath = raster path
		//ras1 = output raster
		//itr1 = lat-lon iterator
		//cel1 = current cell
		//rgt1 = times to use
		//rgd1 = verts to use
	
		LocalDate[] rgt1;
		double[] rgd1;
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		NetcdfReader ncr1;
		NetcdfReader ncr2;
		GeospatialRaster ras1;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading rasters
		ncr1 = new NetcdfReader(arg1.getValueString("sRasterPath1"), arg1.getValueString("sVariable1"));
		ncr2 = new NetcdfReader(arg1.getValueString("sRasterPath2"), arg1.getValueString("sVariable2"));
		
		//loading output raster
		ras1 = new GeospatialRaster(ncr1.dLatResolution, ncr1.dLonResolution, ncr1.getLatRange(), ncr1.getLonRange(), ncr1.gmt1);
		ras1.gmt1.variable=arg1.getValueString("sVariableNameNew");
		ras1.gmt1.source="Difference: " + ncr1.gmt1.source + " - " + ncr2.gmt1.source;
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; " + ncr1.gmt1.history + " - " + ncr2.gmt1.history;;
		ras1.gmt1.long_name="Difference: " + ncr1.gmt1.long_name + " - " + ncr2.gmt1.long_name;
		ras1.gmt1.cell_methods=ncr1.gmt1.cell_methods;
		ras1.gmt1.institution="Difference: " + ncr1.gmt1.institution + " - " + ncr2.gmt1.institution;
		ras1.gmt1.references="Difference: " + ncr1.gmt1.references + " - " + ncr2.gmt1.references;
		ras1.gmt1.title="Difference: " + ncr1.gmt1.title + " - " + ncr2.gmt1.title;
		
		//adding null output time and vert
		ras1.addNullVert();
		ras1.addTime(arg1.getValueTime("timDifferenceOutputTime"), arg1.getValueTime("timDifferenceOutputTime"));
		if(arg1.containsArgument("timZeroOutputTime")){
			ras1.addTime(arg1.getValueTime("timZeroOutputTime"), arg1.getValueTime("timZeroOutputTime"));
		}
			
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//loading times and verts to read from
		rgt1 = new LocalDate[2];
		if(arg1.containsArgument("timRaster1")){
			rgt1[0] = arg1.getValueTime("timRaster1");
		}else{
			rgt1[0] = GeospatialRaster.NULL_TIME;
		}
		if(arg1.containsArgument("timRaster2")){
			rgt1[1] = arg1.getValueTime("timRaster2");
		}else{
			rgt1[1] = GeospatialRaster.NULL_TIME;
		}
		rgd1 = new double[2];
		if(arg1.containsArgument("dVertRaster1")){
			rgd1[0] = arg1.getValueDouble("dVertRaster1");
		}else{
			rgd1[0] = GeospatialRaster.NULL_VERT;
		}
		if(arg1.containsArgument("dVertRaster2")){
			rgd1[1] = arg1.getValueDouble("dVertRaster2");
		}else{
			rgd1[1] = GeospatialRaster.NULL_VERT;
		}
		ncr1.loadGrid(rgt1[0], rgd1[0]);
		ncr2.loadGrid(rgt1[1], rgd1[1]);
		
		//transferring grid to raster that is being written
		itr1 = ncr1.getLatLonIterator(rgt1[0], rgd1[0]);
		while(itr1.hasNext()){
			cel1 = itr1.next();
			ras1.put(
					cel1.axeLat.ID, 
					cel1.axeLon.ID, 
					arg1.getValueTime("timDifferenceOutputTime"), 
					GeospatialRaster.NULL_VERT, 
					ncr1.get(cel1)-ncr2.get(cel1.axeLat.ID, cel1.axeLon.ID, rgt1[1], rgd1[1]));
		}
		
		//writing raster
		ncw1.writeRaster(ras1, arg1.getValueTime("timDifferenceOutputTime"), GeospatialRaster.NULL_VERT);
		
		//writing 0 raster
		if(arg1.containsArgument("timZeroOutputTime")){
			ras1.clear(arg1.getValueTime("timDifferenceOutputTime"), GeospatialRaster.NULL_VERT);
			itr1 = ncr1.getLatLonIterator(rgt1[0], rgd1[0]);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				ras1.put(
						cel1.axeLat.ID, 
						cel1.axeLon.ID, 
						arg1.getValueTime("timZeroOutputTime"), 
						GeospatialRaster.NULL_VERT, 
						0.);
			}
			
			//writing raster
			ncw1.writeRaster(ras1, arg1.getValueTime("timZeroOutputTime"), GeospatialRaster.NULL_VERT);
		}
			
		//terminating
		ncw1.close();
		ncr1.close();
		ncr2.close();
		System.out.println("Done.");
	}
}
