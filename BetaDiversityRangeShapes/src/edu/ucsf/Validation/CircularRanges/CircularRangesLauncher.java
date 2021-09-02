package edu.ucsf.Validation.CircularRanges;
import java.util.ArrayList;
import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * This code generates spherical caps for use as ranges for validating code.
 * @author jladau
 *
 */

public class CircularRangesLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//iRanges = number of ranges to generate
		//lstOut = output
		//sLandPath = path to land shapefile
		//sLandID = header ID to use for shapefile
		//shp1 = land shapefile reader
		//plyLand = land polygon
		//cap1 = current cap
		//dMaxRadius = maximum cap radius
		//dRadius = current radius
		//rgd1 = current center of spherical cap
		//erg1 = earth geometry object
		//sOutputPath = output path
		//bLandOnly = flag for caps on land only
		
		int iRanges;
		ArrayList<String> lstOut;
		String sLandPath;
		String sLandID;
		ShapefileIO shp1;
		SphericalMultiPolygon plyLand;
		SphericalCapEarth cap1;
		double dMaxRadius;
		double dRadius;
		double rgd1[];
		EarthGeometry erg1;
		String sOutputPath;
		boolean bLandOnly;
		
		//loading variables
		iRanges = 2000;
		lstOut = new ArrayList<String>(iRanges+1);
		lstOut.add(WktIO.header());
		bLandOnly = false;
		if(bLandOnly){
			sLandPath = "/home/jladau/Desktop/Data/GIS_Baseline_Shapefiles/ne_50m_land/ne_50m_land_dissolved.shp";
			sLandID = "scalerank";
			shp1 = new ShapefileIO(sLandPath, sLandID);
			shp1.next();
			plyLand = shp1.getPolygon();
		}else{
			plyLand=null;
		}
		dMaxRadius = 400.;
		erg1 = new EarthGeometry();
		sOutputPath = "/home/jladau/Desktop/validation_ranges.wkt";
		
		//loading polygons
		for(int i=0;i<iRanges;i++){
			System.out.println("Loading polygon " + (i+1) + " of " + iRanges + "..."); 
			do{
				dRadius = Math.random()*dMaxRadius;
				rgd1 = erg1.randomPoint();
				cap1 = new SphericalCapEarth(dRadius, rgd1[0], rgd1[1], 1234);
			}while((plyLand!=null && !plyLand.contains(cap1)) || cap1.getBounds().dLatitudeMin<-70. || (cap1.getBounds().dLongitudeMin==-180. && cap1.getBounds().dLongitudeMax==180.));
			lstOut.add(WktIO.toWKT(cap1.toPolygon(360), Integer.toString(i+1)));
		}
		
		//writing output
		DataIO.writeToFile(lstOut, sOutputPath);
		System.out.println("Done.");
	}
	
	
	
}
