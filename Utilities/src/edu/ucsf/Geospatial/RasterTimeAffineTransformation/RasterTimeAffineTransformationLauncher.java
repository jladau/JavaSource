package edu.ucsf.Geospatial.RasterTimeAffineTransformation;

import org.joda.time.LocalDate;

import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterAffineTransformation;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.NetcdfWriter;

/**
 * This code transforms one or more time layers via an affine transformation and outputs the transformation to a new raster
 * @author jladau
 */

public class RasterTimeAffineTransformationLauncher {

public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//ncr1 = first netcdf reader
		//sPath = raster path
		//afn1 = current affine transformation
		//timOut = current output time
		//dVert = vert to read from
	
		LocalDate timOut;
		GeospatialRasterAffineTransformation afn1;
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		NetcdfReader ncr1;
		double dVert;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading raster
		ncr1 = new NetcdfReader(arg1.getValueString("sRasterPath"), arg1.getValueString("sVariable"));
		ncr1.loadGridAllVertsTimes();
				
		//loading output raster
		afn1 = new GeospatialRasterAffineTransformation(ncr1.dLatResolution, ncr1.dLonResolution, ncr1.getLatRange(), ncr1.getLonRange(), ncr1.gmt1);
		afn1.gmt1.variable=arg1.getValueString("sVariableNameNew");
		afn1.gmt1.source=ncr1.gmt1.source;
		afn1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; " + ncr1.gmt1.history;
		afn1.gmt1.long_name=arg1.getValueString("sOutputLongName");
		afn1.gmt1.cell_methods=ncr1.gmt1.cell_methods;
		afn1.gmt1.institution=ncr1.gmt1.institution;
		afn1.gmt1.references=ncr1.gmt1.references;
		afn1.gmt1.title=arg1.getValueString("sOutputTitle");
		
		//adding output times and verts
		afn1.addNullVert();
		for(LocalDate tim1:arg1.getValueTimeArray("rgtOutputTimes")){
			afn1.addTime(tim1,tim1);
		}
			
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{afn1}, arg1.getValueString("sOutputPath"));
		
		//loading vert
		if(arg1.containsArgument("dVert")){
			dVert = arg1.getValueDouble("dVert");
		}else{
			dVert = GeospatialRaster.NULL_VERT;
		}
		
		//looping through affine transformations
		for(int i=0;i<arg1.getValueStringArray("rgsAffineTransformations").length;i++){
			afn1.initializeTransformation(arg1.getValueStringArray("rgsAffineTransformations")[i]);
			timOut = arg1.getValueTimeArray("rgtOutputTimes")[i];
			afn1.loadTransformation(ncr1, dVert, timOut);
			ncw1.writeRaster(afn1,timOut, GeospatialRaster.NULL_VERT);
			afn1.clear(timOut, GeospatialRaster.NULL_VERT);
		}
			
		//terminating
		ncw1.close();
		ncr1.close();
		System.out.println("Done.");
	}	
}
