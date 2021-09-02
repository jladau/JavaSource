package edu.ucsf.Validation.SingleSphericalCap;

import java.util.ArrayList;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;

/**
 * Runs analysis with range set as single spherical cap
 * @author jladau
 */


public class SingleSphericalCapLauncher {

	public static void main(String rgsArgs[]){
		
		//capRange = range cap
		//plyRange = range polygon
		//capRegion = current sampling region
		//rgd1 = current random coordinates
		//geo1 = earth geometry object
		//dRangeRadius = range radius
		//dRegionRadius = region radius
		//lstLocalPerim = list of local perimeter values
		//lstLocalArea = list of local area values
		//iRegion = number of regions
		
		double dRangeRadius;
		double dRegionRadius;
		EarthGeometry geo1;
		SphericalMultiPolygon plyRange;
		SphericalCapEarth capRegion = null;
		SphericalCapEarth capRange;
		double rgd1[];
		ArrayList<Double> lstLocalPerim;
		ArrayList<Double> lstLocalArea;
		int iRegions;
		
		//loading variables
		iRegions = 5000;
		dRangeRadius = 10018.75;
		dRegionRadius = 2000.;
		capRange = new SphericalCapEarth(dRangeRadius, 0., 0., 1234);
		plyRange = capRange.toPolygon(360);
		geo1 = new EarthGeometry();
		lstLocalPerim = new ArrayList<Double>(iRegions);
		lstLocalArea = new ArrayList<Double>(iRegions);
		
		//**********************
		//System.out.println(plyRange.area() + "," + capRange.area());
		//System.out.println(plyRange.perimeter() + "," + capRange.perimeter());
		
		//if(plyRange.area()>0){
		//	return;
		//}
		//**********************
		
		//looping through sampling regions
		System.out.println("REGION,LATITUDE,LONGITUDE,PERIMETER_LOCAL,AREA_LOCAL");
		for(int i=0;i<iRegions;i++){
			do{
				rgd1 = geo1.randomPoint();
				capRegion = new SphericalCapEarth(dRegionRadius, rgd1[0], rgd1[1], 2345);
			}while(!capRegion.intersects(capRange));
			
			//outputting local range area and local perimeter
			lstLocalArea.add(plyRange.area(capRegion));
			lstLocalPerim.add(plyRange.perimeter(capRegion));
			
			//outputting results
			System.out.println(i + "," + capRegion.centerLatitude() + "," + capRegion.centerLongitude() + "," + lstLocalPerim.get(lstLocalPerim.size()-1) + "," + lstLocalArea.get(lstLocalArea.size()-1));
		}
		
		//outputting sampling region characteristics and range characterisitcs
		System.out.println("");
		System.out.println("RANGE_AREA_GLOBAL," + plyRange.area());
		System.out.println("RANGE_PERIMETER_GLOBAL," + plyRange.perimeter());
		System.out.println("REGION_AREA_GLOBAL," + capRegion.area());
		System.out.println("REGION_PERIMETER_GLOBAL," + capRegion.perimeter());
		System.out.println("RANGE_AREA_LOCAL_MEAN," + ExtendedMath.mean(lstLocalArea));
		System.out.println("RANGE_AREA_LOCAL_STDEVP," + ExtendedMath.standardDeviationP(lstLocalArea));
		System.out.println("RANGE_AREA_LOCAL_PREDICTED," + plyRange.area()*capRegion.area()/(plyRange.area()+capRegion.area()+plyRange.perimeter()*capRegion.perimeter()/(2.*Math.PI)));
		System.out.println("RANGE_PERIMETER_LOCAL_MEAN," + ExtendedMath.mean(lstLocalPerim));
		System.out.println("RANGE_PERIMETER_LOCAL_STDEVP," + ExtendedMath.standardDeviationP(lstLocalPerim));
		System.out.println("RANGE_PERIMETER_LOCAL_PREDICTED," + plyRange.perimeter()*capRegion.area()/(plyRange.area()+capRegion.area()+plyRange.perimeter()*capRegion.perimeter()/(2.*Math.PI)));
		System.out.println("LOCAL_COUNT," + lstLocalArea.size());
		
		System.out.println("");
		System.out.println("Done.");
	}
	
	
}
