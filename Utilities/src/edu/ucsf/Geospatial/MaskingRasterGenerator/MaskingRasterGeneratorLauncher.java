package edu.ucsf.Geospatial.MaskingRasterGenerator;

import java.nio.file.Paths;
import static edu.ucsf.base.CurrentDate.currentDate;
import com.google.common.collect.Range;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfWriter;
import edu.ucsf.io.ShapefileIO;
import static edu.ucsf.geospatial.GeospatialRaster.NULL_TIME;
import static edu.ucsf.geospatial.GeospatialRaster.NULL_VERT;


/**
 * This code creates a raster from a shapefile useful for masking points that are outside of the shapefile
 * @author jladau
 *
 */

public class MaskingRasterGeneratorLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shpMask = shapfile (for masking if requested)
		//plyMask = masking polygon (if requested)
		//ncw1 = netcdf writer
		//dLatMin = min latitude
		//dLatMax = max latitude
		//dLonMin = min longitude
		//dLonMax = max longitude
		//sMaskTitle = mask title
		//itr1 = latitude/longitude iterator
		//cel1 = current cell
		
		String sMaskTitle;
		ArgumentIO arg1;
		ShapefileIO shpMask = null;
		SphericalMultiPolygon plyMask = null;
		NetcdfWriter ncw1;
		GeospatialRaster gsr1;
		double dLatMin;
		double dLatMax;
		double dLonMin;
		double dLonMax;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading masking shapefile
		shpMask = new ShapefileIO(arg1.getValueString("sMaskPath"),arg1.getValueString("sMaskFeature"));
			
		//loading mask title
		sMaskTitle = Paths.get(arg1.getValueString("sMaskPath")).getFileName().toString().replace(".shp", "");
		
		//loading masking polygon
		shpMask.loadFeature(arg1.getValueString("sMaskFeatureID"));
		plyMask = shpMask.getPolygon();
		
		//initializing output
		dLatMin = plyMask.latitudeRange().lowerEndpoint();
		dLatMin = Math.max(dLatMin-5., -90.);
		dLatMax = plyMask.latitudeRange().upperEndpoint();
		dLatMax = Math.min(dLatMax+5., 90.);
		dLonMin = plyMask.longitudeRange().lowerEndpoint();
		dLonMin = Math.max(dLonMin-5., -180.);
		dLonMax = plyMask.longitudeRange().upperEndpoint();
		dLonMax = Math.min(dLonMax+5., 180.);
		gsr1 = new GeospatialRaster(arg1.getValueDouble("dOutputResolution"), arg1.getValueDouble("dOutputResolution"), Range.closed(dLatMin, dLatMax), Range.closed(dLonMin, dLonMax), 
				new GeospatialRasterMetadata(
						sMaskTitle + " mask",
						arg1.getValueString("sMaskInstitution"),
						arg1.getValueString("sMaskReferences"),
						"Generated from shapefile using Utilities.edu.ucsf.MaskingRasterGenerator.MaskingRasterGeneratorLauncher.",
						"Raster created on " + currentDate() + "; shapefile downloaded on " + arg1.getValueString("sMaskDownloadDate") + "; shapefile version " + arg1.getValueString("sMaskVersion"),
						"Mask",
						"null",
						sMaskTitle + " mask",
						"area: maximum"));
		gsr1.addNullTime();
		gsr1.addNullVert();
		
		//loading rasters
		itr1 = gsr1.getLatLonIterator(NULL_TIME, NULL_VERT);
		while(itr1.hasNext()){
			cel1 = itr1.next();
			if(plyMask.contains(cel1.axeLat.rngAxisValues.lowerEndpoint(), cel1.axeLon.rngAxisValues.lowerEndpoint())){
				gsr1.put(cel1.axeLat.ID, cel1.axeLon.ID, NULL_TIME, NULL_VERT, 0);
			}else if(plyMask.contains(cel1.axeLat.rngAxisValues.lowerEndpoint(), cel1.axeLon.rngAxisValues.upperEndpoint())){
				gsr1.put(cel1.axeLat.ID, cel1.axeLon.ID, NULL_TIME, NULL_VERT, 0);
			}else if(plyMask.contains(cel1.axeLat.rngAxisValues.upperEndpoint(), cel1.axeLon.rngAxisValues.lowerEndpoint())){
				gsr1.put(cel1.axeLat.ID, cel1.axeLon.ID, NULL_TIME, NULL_VERT, 0);
			}else if(plyMask.contains(cel1.axeLat.rngAxisValues.upperEndpoint(), cel1.axeLon.rngAxisValues.upperEndpoint())){
				gsr1.put(cel1.axeLat.ID, cel1.axeLon.ID, NULL_TIME, NULL_VERT, 0);
			}	
		}
			
		//writing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{gsr1}, arg1.getValueString("sOutputPath"));
		ncw1.writeRaster(gsr1,NULL_TIME,NULL_VERT);
		ncw1.close();	
		
		//terminating
		System.out.println("Done.");
	}
}
