package edu.ucsf.Geospatial.RectangleMinusHalfCap;

import java.util.ArrayList;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Creates a rectangle minus half of a spherical cap
 * @author jladau
 *
 */

public class RectangleMinusHalfCapLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//cap1 = cap
		//geo1 = earth geometry object
		//rgd2 = current point on boundary
		//lst1 = polygon output
		//dLatTop = latitude of top
		//ply1 = region in polygon format
		//lstOut = output
		
		ArgumentIO arg1;
		SphericalCapEarth cap1;
		EarthGeometry geo1;
		double rgd2[] = null;
		ArrayList<Double[]> lst1;
		double dLatTop;
		SphericalMultiPolygon ply1;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		cap1 = new SphericalCapEarth(
				arg1.getValueDouble("dRadius"),
				arg1.getValueDouble("dLatCenter"),
				arg1.getValueDouble("dLonCenter"),
				1234);
		geo1 = new EarthGeometry();
		lst1 = new ArrayList<Double[]>(1000);
		
		//looping through boundary of half spherical cap
		dLatTop = geo1.findDestinationWGS84(cap1.centerLatitude(), cap1.centerLongitude(), 0., cap1.radius())[0];
		for(double d=360;d>180;d-=0.5){	
			rgd2=geo1.findDestinationWGS84(cap1.centerLatitude(), cap1.centerLongitude(), d, cap1.radius());
			lst1.add(new Double[]{1.,rgd2[0], rgd2[1]});
		}
		for(double dLon=rgd2[1];dLon>-179;dLon-=0.5){
			lst1.add(new Double[]{1.,rgd2[0], dLon});
		}
		lst1.add(new Double[]{1.,rgd2[0], -179.});
		for(double dLat=rgd2[0];dLat<dLatTop;dLat+=0.5){
			lst1.add(new Double[]{1., dLat, -179.}); 
		}
		lst1.add(new Double[]{1., dLatTop, -179.});
		for(double dLon=-179.;dLon<rgd2[1];dLon+=0.5){
			lst1.add(new Double[]{1., dLatTop, dLon});
		}
		
		//loading as polygon
		ply1 = new SphericalMultiPolygon(lst1,1234,false);
		
		//outputting results
		lstOut = new ArrayList<String>();
		lstOut.add(WktIO.header());
		lstOut.add(WktIO.toWKT(ply1, Integer.toString(1)));
		
		//writing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
