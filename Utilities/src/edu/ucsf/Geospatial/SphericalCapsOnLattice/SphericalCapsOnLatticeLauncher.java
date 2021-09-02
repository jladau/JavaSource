package edu.ucsf.Geospatial.SphericalCapsOnLattice;

import java.util.ArrayList;

import com.google.common.collect.Range;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.SphericalCapEarth_SamplingRegion;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Loads spherical caps on a lattice
 * @author jladau
 *
 */

public class SphericalCapsOnLatticeLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dResolution = spacing between adjacent caps (in degrees)
		//dRadius = radius of spherical caps
		//lstOut = output
		//shp1 = merged shapefile reader (for selecting only regions overlapping ranges)
		//cap1 = current cap
		//plyMerged = merged polygon
		//ras1 = geospatial raster
		//itr1 = iterator
		//cel1 = current cell
		//i1 = counter
		//i2 = counter
		
		int i2;
		int i1;
		GeospatialRasterCell cel1;
		GeospatialRaster.LatLonIterator itr1;
		GeospatialRaster ras1;
		ArgumentIO arg1;
		double dResolution;
		double dRadius;
		SphericalMultiPolygon plyMerged;
		ShapefileIO shp1;
		ArrayList<String> lstOut;
		SphericalCapEarth_SamplingRegion cap1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dResolution = arg1.getValueDouble("dLatticeResolution");
		dRadius = arg1.getValueDouble("dRadius");
		if(arg1.containsArgument("sDissolvedShapefilePath")){
			shp1 = new ShapefileIO(
					arg1.getValueString("sDissolvedShapefilePath"),
					arg1.getValueString("sIDHeader"));
			shp1.next();
			plyMerged = shp1.getPolygon();
		}else{
			plyMerged = null;
		}
		ras1 = new GeospatialRaster(dResolution, dResolution, Range.closed(-90., 90.), Range.closed(-180.,180.), 
				new GeospatialRasterMetadata(
						null,
						null,
						null,
						null,
						null,
						null, 
						null, 
						null, 
						null));
		ras1.addNullTime();
		ras1.addNullVert();
		lstOut = new ArrayList<String>((int) (360*720/(dResolution*dResolution) + 10));
		lstOut.add("REGION_ID,RADIUS,LATITUDE_CENTER,LONGITUDE_CENTER,AREA,PERIMETER");
		
			
		//looping through raster cells
		itr1 = ras1.getLatLonIterator(GeospatialRaster.NULL_TIME, GeospatialRaster.NULL_VERT);
		i1 = 0;
		i2 = 1;
		while(itr1.hasNext()){
			System.out.println("Analyzing spherical cap " + i2 + "...");
			i2++;
			cel1 = itr1.next();
			cap1 = new SphericalCapEarth_SamplingRegion(
					dRadius,
					cel1.axeLat.ID,
					cel1.axeLon.ID,
					Double.NaN,
					1234);
			
			if(cap1.intersects180()){
				continue;
			}
			if(plyMerged!=null & !plyMerged.intersects(cap1)){
				continue;
			}
			i1++;
			lstOut.add(i1 + "," + dRadius + "," + cel1.axeLat.ID + "," + cel1.axeLon.ID + "," + cap1.area() + "," + cap1.perimeter());
		}
		
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}