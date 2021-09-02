package edu.ucsf.Geospatial.LatitudeLongitudeRaster;

import org.apache.commons.math3.util.Precision;
import static edu.ucsf.base.CurrentDate.currentDate;
import com.google.common.collect.Range;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfWriter;

/**
 * This code makes rasters giving the latitude and longitude.
 * @author jladau
 */
public class LatitudeLongitudeRasterLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//rasLat = latitude raster
		//ncwLat = netcdf latitude writer
		//rasLon = longitude raster
		//ncwLon = netcdf longitude writer
			
		ArgumentIO arg1;
		GeospatialRaster rasLat;
		NetcdfWriter ncwLat = null;
		GeospatialRaster rasLon;
		NetcdfWriter ncwLon = null;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//initializing raster: latitude
		rasLat = new GeospatialRaster(0.5,0.5,Range.closed(-90.,90.),Range.closed(-180., 180.),new GeospatialRasterMetadata(
				"Longitude",
				"Gladstone Institutes, University of California San Francisco",
				"https://github.com/jladau/JavaSource",
				"Generated using Utilities.edu.ucsf.Geospatial.LatitudeLongitudeRaster.LatitudeLongitudeRasterLauncher.java",
				"Raster created on " + currentDate(),
				"Latitude",
				"degrees_north",
				"Latitude",
				"area: mean"));
		rasLat.addNullTime();
		rasLat.addNullVert();
		rasLon = new GeospatialRaster(0.5,0.5,Range.closed(-90.,90.),Range.closed(-180., 180.),new GeospatialRasterMetadata(
				"Longitude",
				"Gladstone Institutes, University of California San Francisco",
				"https://github.com/jladau/JavaSource",
				"Generated using Utilities.edu.ucsf.Geospatial.LatitudeLongitudeRaster.LatitudeLongitudeRasterLauncher.java",
				"Raster created on " + currentDate(),
				"Longitude",
				"degrees_east",
				"Longitude",
				"area: mean"));
		rasLon.addNullTime();
		rasLon.addNullVert();
		
		//initializing writer: latitude
		try{
			ncwLat = new NetcdfWriter(new GeospatialRaster[]{rasLat}, arg1.getValueString("sLatitudeOutputPath"));
			ncwLon = new NetcdfWriter(new GeospatialRaster[]{rasLon}, arg1.getValueString("sLongitudeOutputPath"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//outputting results: latitude
		for(double dLat = -89.75;dLat<90;dLat+=0.5){
			dLat=Precision.round(dLat, 3);
			for(double dLon = -179.75;dLon<180.;dLon+=0.5){
				dLon = Precision.round(dLon, 3);
				rasLat.put(dLat, dLon, GeospatialRaster.NULL_TIME,GeospatialRaster.NULL_VERT, dLat);
				rasLon.put(dLat, dLon, GeospatialRaster.NULL_TIME,GeospatialRaster.NULL_VERT, dLon);
			}
		}
		try {
			ncwLat.writeRaster(rasLat,GeospatialRaster.NULL_TIME,GeospatialRaster.NULL_VERT);
			ncwLon.writeRaster(rasLon,GeospatialRaster.NULL_TIME,GeospatialRaster.NULL_VERT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ncwLat.close();
		ncwLon.close();
		
		//terminating
		System.out.println("Done.");
	}
}
