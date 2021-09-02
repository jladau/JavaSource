package edu.ucsf.Geospatial.RasterToText;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.geospatial.MaskGeospatialRaster;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.ShapefileIO;

/**
 * Converts raster to flat text file
 * @author jladau
 *
 */

public class RasterToTextLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncf1 = netcdf object
		//dVert = vert value
		//tim1 = time value
		//lstOut = output
		//itr1 = raster iterator
		//shpMask = masking shapefile
		//plyMask = masking polygon (if requested)
		//cel1 = current cell
		//dValue = current value
		
		double dVert = Double.NaN;
		LocalDate tim1 = null;
		ArgumentIO arg1;
		NetcdfReader cdf1;
		ArrayList<String> lstOut;
		ShapefileIO shpMask = null;
		SphericalMultiPolygon plyMask = null;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		double dValue;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		cdf1 = new NetcdfReader(arg1.getValueString("sRasterPath"), arg1.getValueString("sRasterVariable"));
		if(cdf1.hasVert()){
			if(!Double.isNaN(arg1.getValueDouble("dVert"))){
				dVert = arg1.getValueDouble("dVert");
			}else{
				throw new Error("Raster has vert axis, but vert not supplied.");
			}
		}else{
			dVert = GeospatialRaster.NULL_VERT;
		}
		if(cdf1.hasTime() && arg1.getValueTime("timDate")!=null){
			if(arg1.containsArgument("timDate")){
				tim1 = arg1.getValueTime("timDate");
			}else{
				throw new Error("Raster has time axis, but time not supplied.");
			}
		}
		
		//loading mask
		if(arg1.containsArgument("sMaskPath") && arg1.containsArgument("sMaskFeature")){
			
			//loading masking shapefile
			shpMask = new ShapefileIO(arg1.getValueString("sMaskPath"),arg1.getValueString("sMaskFeature"));
			
			//loading masking polygon
			shpMask.loadFeature(arg1.getValueString("sMaskFeatureID"));
			plyMask = shpMask.getPolygon();
		}
		
		//initializing output
		lstOut = new ArrayList<String>(10000);
		lstOut.add("LATITUDE,LONGITUDE,VALUE");
		
		//loading grid
		cdf1.loadGrid(tim1, dVert);
		
		//applying mask
		MaskGeospatialRaster.applyMask(plyMask,cdf1,tim1,dVert);
		
		//looping through grid cells and outputting values
		itr1 = cdf1.getLatLonIterator(tim1, dVert);
		while(itr1.hasNext()){
			cel1 = itr1.next();
			dValue = cdf1.get(cel1);
			if(!Double.isNaN(dValue)){
				lstOut.add(cel1.axeLat.ID + "," + cel1.axeLon.ID + "," + dValue);
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		cdf1.close();
		System.out.println("Done.");
	}
}
