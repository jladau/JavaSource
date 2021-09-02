package edu.ucsf.geospatial;

import static com.google.common.math.DoubleMath.*;
import static java.lang.Math.*;

import java.awt.geom.Point2D;
import java.util.Random;

import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Implements various spherical geometry algorithms.
 * @author jladau
 */

public class EarthGeometry {

	public static final double RAD_TO_DEG = 57.2957795130823;
	public static final double DEG_TO_RAD = 0.01745329251994;
	public static final double EARTH_RADIUS = 6371;
	public static final double EARTH_RADIUS_SQUARED = 40589641;
	public static final double LAT_DISTANCE_SPHERE = 111.194926644559;
	public static final double EARTH_AREA = 510072000;
	public static final double EARTH_AREA_SPHERE = 510064471.909788;
	public static final double EARTH_CIRCUMFERENCE_EQUATORIAL = 40075.017;
	public static final double EARTH_CIRCUMFERENCE_MERIDIONAL = 40007.86;
	public static final double EARTH_CIRCUMFERENCE_MEAN = 40041.4385;
	public static final double EARTH_CIRCUMFERENCE_SPHERE = 40030.1735920411;
	
	/**Geodetic calculator object**/
	private GeodeticCalculator gcl1;
	
	/**Random number generator**/
	private Random rnd1;
	
	/**
	 * Constructor: use non-static methods to improve efficiency for GeoTools functions.
	 */
	public EarthGeometry(){
		gcl1 = new GeodeticCalculator();
		rnd1 = new Random(56789);
	}

	/**
	 * Constructor: use non-static methods to improve efficiency for GeoTools functions.
	 */
	public EarthGeometry(int iRandomSeed){
		gcl1 = new GeodeticCalculator();
		rnd1 = new Random(iRandomSeed);
	}
	
	//TODO write unit test
	/**
	 * Finds random point
	 * @return Latitude and longitude of random point
	 */
	public double[] randomPoint(){
		
		//d1 = first random value
		//d2 = second random value
		
		double d1;
		double d2;
		
		d1 = RAD_TO_DEG*(2.*PI*rnd1.nextDouble()-PI);
		d2 = RAD_TO_DEG*acos(2.*rnd1.nextDouble()-1.) - 90.;
		
		return new double[]{d2,d1};
	}
	
	/**
	 * Finds the destination along orthodrome from specified location.
	 * @param dLat Starting latitude.
	 * @param dLon Starting longitude.
	 * @param dBearing Initial bearing.
	 * @param dDistance Distance traveled (in km).
	 * @return (Latitude,Longitude) of destination.
	 */
	public double[] findDestinationWGS84(double dLat, double dLon, double dBearing, double dDistance){
		
		//pnt1 = output point
		//d1 = adjusted bearing
		
		Point2D pnt1;
		double d1;
		
		d1 = dBearing;
		d1 = d1 % 360;
		if(d1>180.){
			d1 = d1-360;
		}
		
		gcl1.setStartingGeographicPoint(dLon, dLat);
		gcl1.setDirection(d1, dDistance*1000.);
		pnt1 = gcl1.getDestinationGeographicPoint();
		return new double[]{pnt1.getY(),pnt1.getX()};			
	}
	
	/**
	 * Finds a random (uniformly distributed) point on a sphere within bounds.
	 * @param env2 Envelope within which point is to be contained.
	 * @parem iPoints Number of random points to generate
	 * @param iRandomSeed Random seed
	 * @return (Latitude,Longitude) of point.
	 */
	public static double[][] generateRandomPoints(Envelope env1, int iPoints, int iRandomSeed){
		
		//rgdBounds = polygon bounds
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//env2 = sampling bounds in radians
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgdOutput = output
		//rnd1 = random number generator
		
		double dLat; double dLng; double d2 = 0; double d3 = 0; double d4 = 0;
		double rgdOutput[][];
		Envelope env2;
		Random rnd1;
		
		//initializing matrix of bounds
		env2 = new Envelope(env1.getMinX()*DEG_TO_RAD,env1.getMaxX()*DEG_TO_RAD,env1.getMinY()*DEG_TO_RAD,env1.getMaxY()*DEG_TO_RAD);
		
		//loading intermediate values
		d2 = env2.getMaxX()-env2.getMinX();
		d3 = cos(env2.getMinY() + PI/2.);  
		d4 = cos(env2.getMaxY() + PI/2.) - d3;
			
		//initializing output
		rgdOutput = new double[iPoints][2];
		rnd1 = new Random(iRandomSeed);
		for(int i=0;i<iPoints;i++){
		
			//loading Lat and Lng
			dLng = rnd1.nextDouble()*d2 + env2.getMinX();
			dLng = RAD_TO_DEG*dLng;
			dLat = acos(d3+d4*rnd1.nextDouble());
			dLat = RAD_TO_DEG*dLat-90.;
		
			//outputting results
			rgdOutput[i][0]=dLat;
			rgdOutput[i][1]=dLng;
		}
			
		//returning result
		return rgdOutput;	
	}
	
	
	/**
	 * Finds the latitude at which the great circle connecting two points intersects a given meridian
	 * @param dLat1 Latitude of first point (in degrees).
	 * @param dLng1 Longitude of first point (in degrees).
	 * @param dLat2 Latitude of second point (in degrees).
	 * @param dLng2 Longitude of second point (in degrees).
	 * @param dLng Meridian being checked (in degrees).
	 * @return Latitude of intersection.
	 */
	 public static double findLatitudeOfOrthodromeIntersection(double dLat1, double dLng1, double dLat2, double dLng2, double dLng){
		 
		 //d1 = output
		 
		 double d1;
		 
		 //computing intersection
		 d1 = Math.atan((Math.tan(dLat1*DEG_TO_RAD)*Math.sin(dLng*DEG_TO_RAD - dLng2*DEG_TO_RAD) - Math.tan(dLat2*DEG_TO_RAD)*Math.sin(dLng*DEG_TO_RAD - dLng1*DEG_TO_RAD)) / Math.sin(dLng1*DEG_TO_RAD - dLng2*DEG_TO_RAD));
		 
		 //outputting result
		 return d1*RAD_TO_DEG;
	 }
	
	/**
	 * Checks if bearing is within range.
	 * @param dBearing Bearing (in degrees)
	 * @param dRangeLB Lower bound of range (in degrees). Note: this can be larger than upper bound if range covers 0.
	 * @param dRangeUB Upper bound of range (in degrees). Note: this can be smaller than lower bound if range covers 0.
	 * @return True if bearing is within range; false otherwise.
	 */
	public static boolean checkBearingInRange(double dBearing, double dRangeLB, double dRangeUB){
		
		dBearing = convertBearingTo360(dBearing);
		dRangeLB = convertBearingTo360(dRangeLB);
		dRangeUB = convertBearingTo360(dRangeUB);
		
		if(dBearing==-9999){
			return true;
		}
		
		if(dRangeLB<dRangeUB){
			if(dRangeLB<=dBearing && dBearing<=dRangeUB){
				return true;
			}else{
				return false;
			}
		}else{
			if(dRangeUB<=dBearing && dBearing<=dRangeLB){
				return false;
			}else{
				return true;
			}	
		}
	}
	
	/**
	 * Finds the distance between a pair of points.
	 * @param dLat1 Latitude of first point.
	 * @param dLon1 Longitude of first point.
	 * @param dLat2 Latitude of second point.
	 * @param dLon2 Longitude of second point.
	 * @return Distance between points.
	 */
	public static double orthodromicDistance(double dLat1, double dLon1, double dLat2, double dLon2){
		double dLat = DEG_TO_RAD*(dLat2-dLat1); 
		double dLon = DEG_TO_RAD*(dLon2-dLon1); 
		double a = Math.sin(dLat/2.) * Math.sin(dLat/2.) + Math.cos(DEG_TO_RAD*dLat1) * Math.cos(DEG_TO_RAD*dLat2) * Math.sin(dLon/2.) * Math.sin(dLon/2.);
		return EARTH_RADIUS * 2. * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	}
	
	/**
	 * Finds the area of a rectangular cell with given center, width, and height.
	 * @param dLat Latitude of center of cell.
	 * @param dLng Longitude of center of cell.
	 * @param dHeight Height of cell (in degrees).
	 * @param dWidth Width of cell (in degrees).
	 * @return Area of cell in (in km^2).
	 */
	public static double findAreaCell(double dLat, double dLng, double dWidth, double dHeight){
		
		//d1 = length (in km) of first base of cell
		//d2 = length (in km) of second base of cell
		//dHeight2 = height divided by 2
		//dWidth2 = width divided by 2
		
		//double d1; double d2; double dHeight2; double dWidth2;
		
		/*
		//loading height and width divided by 2	
		dHeight2 = dHeight/2.;
		dWidth2 = dWidth/2.;
		
		//loading lengths of bases of cell
		if(dLat-dHeight2>-90){
			d1 = orthodromicDistance(dLat-dHeight2, dLng-dWidth2, dLat-dHeight2, dLng+dWidth2);
		}else{
			d1=0;
		}
		if(dLat+dHeight2<90){
			d2 = orthodromicDistance(dLat+dHeight2, dLng-dWidth2, dLat+dHeight2, dLng+dWidth2);
		}else{
			d2 = 0;
		}
			
		//finding area
		return LAT_DISTANCE_SPHERE*dHeight*(d1+d2)/2.;
		
		*/

		return EARTH_RADIUS*EARTH_RADIUS*DEG_TO_RAD*dWidth*(Math.sin(DEG_TO_RAD*(dLat+dHeight/2.))-Math.sin(DEG_TO_RAD*(dLat-dHeight/2.)));
		
		
		
	}
	
	/**
	 * Finds the distance between a pair of points assuming ellipsoid model.
	 * @param dLat1 Latitude of first point.
	 * @param dLon1 Longitude of first point.
	 * @param dLat2 Latitude of second point.
	 * @param dLon2 Longitude of second point.
	 * @return Distance between points.
	 */
	public double orthodromicDistanceWGS84(double dLat1, double dLon1, double dLat2, double dLon2){
		
		if(dLon1<=-180){
			dLon1=-179.99999999;
		}
		if(dLon1>=180){
			dLon1=179.99999999;
		}
		if(dLon2<=-180){
			dLon2=-179.99999999;
		}
		if(dLon2>=180){
			dLon2=179.99999999;
		}
		
		if(dLat1<=-90){
			dLat1=-89.99999999;
		}
		if(dLat1>=90){
			dLat1=89.99999999;
		}
		if(dLat2<=-90){
			dLat2=-89.99999999;
		}
		if(dLat2>=90){
			dLat2=89.99999999;
		}
		
		
		gcl1.setStartingGeographicPoint(dLon1, dLat1);
		gcl1.setDestinationGeographicPoint(dLon2, dLat2);
		try{
			return gcl1.getOrthodromicDistance()/1000.;
		}catch(Exception e){
			return orthodromicDistance(dLat1, dLon1, dLat2, dLon2);
		}
	}
	
	/**
	 * Finds initial bearing of great circle path between two points. Output is between 0 and 360 degrees. Assumes ellipsoid model.
	 * @param dLat0 Latitude of starting point.
	 * @param dLon0 Longitude of starting point.
	 * @param dLat1 Latitude of ending point.
	 * @param dLon1 Longitude of ending point.
	 * @return Bearing in degrees.
	 */
	public double initialGeodesicBearingWGS84(double dLat1, double dLon1, double dLat2, double dLon2){
		
		//dOut = output
		
		double dOut;
		
		//checking that start and destination points are different
		if(!arePointsDifferent(dLat1, dLon1, dLat2, dLon2)){
			return Double.NaN;
		}
		
		//loading output
		gcl1.setStartingGeographicPoint(dLon1, dLat1);
		gcl1.setDestinationGeographicPoint(dLon2, dLat2);
		dOut = gcl1.getAzimuth();
		
		//checking that output is in the interval [0,360]
		return convertBearingTo360(dOut);
	}
	
	
	/**
	 * Finds initial bearing of great circle path between two points. Output is between 0 and 360 degrees.
	 * @param dLat0 Latitude of starting point.
	 * @param dLon0 Longitude of starting point.
	 * @param dLat1 Latitude of ending point.
	 * @param dLon1 Longitude of ending point.
	 * @return Bearing in degrees.
	 */
	public static double initialGeodesicBearing(double dLat1, double dLon1, double dLat2, double dLon2){
		
		//dOut = output
		//d1 = difference between longitudes
		 
		double d1;
		double dOut;
		
		//checking that start and destination points are different
		if(!arePointsDifferent(dLat1, dLon1, dLat2, dLon2)){
			return Double.NaN;
		}
		
		//loading output
		d1 = dLon2 - dLon1;
		d1 = d1*DEG_TO_RAD;
		dOut=RAD_TO_DEG*atan2(Math.sin(d1)*cos(DEG_TO_RAD*dLat2), cos(DEG_TO_RAD*dLat1)*sin(DEG_TO_RAD*dLat2)-sin(DEG_TO_RAD*dLat1)*cos(DEG_TO_RAD*dLat2)*cos(d1));
		
		//checking that output is in the interval [0,360]
		return convertBearingTo360(dOut);
	}
	
	/**
	 * Converts a bearing to the interval [0,360].
	 * @param dBearing Bearing to convert. 
	 * @return Bearing in the interval [0,360]: for instance, 710 would map to 10.
	 */
	private static double convertBearingTo360(double dBearing){
		
		//dOut = output
		
		double dOut;
		
		dOut = dBearing;
		
		while(dOut>360){
			dOut=dOut-360.;
		}
		while(dOut<0){
			dOut=dOut+360.;
		}
		
		return dOut;
	}
	
	/**
	 * Checks if points are different.
	 * @param dLat0 Latitude of starting point.
	 * @param dLon0 Longitude of starting point.
	 * @param dLat1 Latitude of ending point.
	 * @param dLon1 Longitude of ending point.
	 * @return True if points are different; false if they are the same.
	 */
	private static boolean arePointsDifferent(double dLat0, double dLon0, double dLat1, double dLon1){
		if(fuzzyEquals(dLat0,dLat1,0.0000000000001) && fuzzyEquals(dLon0,dLon1,0.0000000000001)){
			return false;
		}else{
			return true;
		}
	}
	
	//TODO write unit test
	
	public static int northOfLine(double dLat, double dLon, double dLat0, double dLon0, double dLat1, double dLon1){
		
		//d1 = comparison value
		
		double d1;
		
		d1 = (dLon - dLon0)/(dLon1-dLon0)*(dLat1-dLat0)+dLat0;
		if(dLat > d1){
			return 1;
		}else if(dLat==d1){
			return 0;
		}else{
			return -1;
		}
	}
}
