package edu.ucsf.Geospatial.ShapefileToRaster;

import static edu.ucsf.base.CurrentDate.currentDate;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.Range;

import edu.ucsf.geospatial.GeographicPointBounds;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfWriter;
import edu.ucsf.io.ShapefileIO;

/**
 * Converts a shapefile to a raster. All polygons are assumed to be non-overlapping.
 * @author jladau
 *
 */

public class ShapefileToRasterLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shp1 = shapefile
		//ply1 = current polygon
		//bds1 = bounds for current polygon
		//ras1 = raster
		//dResolution = resolution of raster
		//ncw1 = netcdf writer
		//lst1 = current list of raster values
		//map1 = map of polygon values
		//dat1 = map of polygon values
		//dValue = current value
		//dLat = current latitude
		//dLon = current longitude
		
		double dValue;
		DataIO dat1;
		ArgumentIO arg1;
		ShapefileIO shp1;
		SphericalMultiPolygon ply1;
		GeographicPointBounds bds1;
		GeospatialRaster ras1;
		double dResolution;
		NetcdfWriter ncw1;
		ArrayList<Double[]> lst1;
		HashMap<String,Double> map1;
		double dLat;
		double dLon;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		shp1 = new ShapefileIO(arg1.getValueString("sShapeFilePath"),arg1.getValueString("sIDHeader"));
		dResolution = arg1.getValueDouble("dResolution");
		
		//loading map of polygon values
		dat1 = new DataIO(arg1.getValueString("sPolygonValueMapPath"));
		map1 = new HashMap<String,Double>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, arg1.getValueString("sPolygonNameField")), dat1.getDouble(i, arg1.getValueString("sPolygonValueField")));
		}
		
		//initializing output
		ras1 = new GeospatialRaster(dResolution,dResolution,Range.closed(-90.,90.),Range.closed(-180., 180.),new GeospatialRasterMetadata(
				arg1.getValueString("sRasterTitle"),
				"Lawrence Berkeley National Laboratory",
				"https://github.com/jladau/JavaSource",
				"Generated using Utilities.edu.ucsf.Geospatial.LatitudeLongitudeRaster.LatitudeLongitudeRasterLauncher.java",
				"Raster created on " + currentDate(),
				arg1.getValueString("sRasterVariable"),
				arg1.getValueString("sRasterUnits"),
				arg1.getValueString("sRasterVariableLongName"),
				arg1.getValueString("sRasterCellMethods")));
		ras1.addNullTime();
		ras1.addNullVert();
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//looping through polygons
		while(shp1.hasNext()){
			shp1.next();
			System.out.println("Analyzing polygon " + shp1.getID() + "...");
			ply1 = shp1.getPolygon();
			bds1 = ply1.getBounds();
			lst1 = bds1.rasterPointsInBounds(dResolution);
			
			//looping through points to check which are in polygon and outputting
			for(int i=0;i<lst1.size();i++){
				if(ply1.contains(lst1.get(i)[0],lst1.get(i)[1])){
					if(map1.containsKey(shp1.getID())){
						dValue = map1.get(shp1.getID());
					}else{
						if(arg1.getValueBoolean("bMissingValueZero")){
							dValue = 0;
						}else{
							continue;
						}
					}		
					dLat = lst1.get(i)[0];
					dLon = lst1.get(i)[1];
					if(arg1.getValueBoolean("bNormalizeByArea")){
						dValue = dValue * (new GeographicPointBounds(dLat-dResolution/2., dLat+dResolution/2., dLon-dResolution/2., dLon+dResolution/2.)).area()/ply1.area();;
					}
					ras1.put(dLat, dLon, GeospatialRaster.NULL_TIME,GeospatialRaster.NULL_VERT, dValue);
				}
			}
		}
		
		//outputting results
		ncw1.writeRaster(ras1,GeospatialRaster.NULL_TIME,GeospatialRaster.NULL_VERT);
		ncw1.close();
		
		//terminating
		System.out.println("Done.");
	}
}