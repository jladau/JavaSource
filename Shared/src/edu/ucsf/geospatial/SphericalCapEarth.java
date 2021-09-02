package edu.ucsf.geospatial;

import static java.lang.Math.*;

import java.util.ArrayList;
import java.util.Random;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;
import static edu.ucsf.geospatial.EarthGeometry.*;

/**
 * Spherical cap object
 * @author jladau
 */

//TODO write unit tests

public class SphericalCapEarth {

	/**Number of random points to use**/
	private static final int RANDOM_POINTS=2000;
	
	/**Step size in degrees for finding bounds and random polygon**/
	private static final double STEP_SIZE=1.;
	
	/**Radius of cap**/
	private double dRadius;
	
	/**Latitude of center of cap**/
	private double dLatCenter;
	
	/**Longitude of center of cap**/
	private double dLonCenter;
	
	/**Angle of half of spherical cap**/
	private double dTheta;
	
	/**Earth geometry object**/
	private EarthGeometry geo1;
	
	/**Random number generator**/
	private Random rnd1;
	
	/**Bounds**/
	private GeographicPointBounds bds1;
	
	/**Area**/
	private double dArea=Double.NaN;
	
	/**Perimeter**/
	private double dPerimeter=Double.NaN;
	
	/**Collection of random points in cap**/
	private ArrayList<Double[]> lstRandom = null;
	
	/**Cap in polygon format**/
	private SphericalMultiPolygon ply1 = null;
	
	public SphericalCapEarth(double dRadius, double dLatCenter, double dLonCenter, int iRandomSeed){
		initialize(dRadius, dLatCenter, dLonCenter, iRandomSeed);
	}
	
	public SphericalCapEarth(double dRadius, double[] rgdCenter, int iRandomSeed){
		initialize(dRadius, rgdCenter[0], rgdCenter[1], iRandomSeed);
	}
	
	private void initialize(double dRadius, double dLatCenter, double dLonCenter, int iRandomSeed){
		this.dRadius = dRadius;
		this.dLatCenter = dLatCenter;
		this.dLonCenter = dLonCenter;
		dTheta = dRadius/EARTH_RADIUS;
		geo1 = new EarthGeometry();
		rnd1 = new Random(iRandomSeed);
		this.bds1=null;
	}
	
	/**
	 * Loads set of random caps contained within current cap
	 * @param dRadius Radius of subset caps
	 * @return Set of random caps.
	 */
	public ArrayList<SphericalCapEarth> randomCaps(double dRadius, int iCaps, int iRandomSeed){
	
		//lstOut = output
		//lst = random latitudes and longitudes for centers of new caps
		//cap1 = cap in which centers must be contains
		
		ArrayList<SphericalCapEarth> lstOut;
		ArrayList<Double[]> lst1;
		SphericalCapEarth cap1;
		
		lstOut = new ArrayList<SphericalCapEarth>(iCaps);
		cap1 = new SphericalCapEarth(this.radius()-dRadius, dLatCenter, dLonCenter, iRandomSeed);
		lst1 = cap1.randomPointsInCap(iCaps);
		for(Double rgd1[]:lst1){
			lstOut.add(new SphericalCapEarth(dRadius, rgd1[0], rgd1[1], iRandomSeed+lstOut.size()));
		}
		return lstOut;
	}
	
	private void loadBounds(){
	
		//rgd1 = min latitude, max latitude, min longitude, max longitude
		//rgd2 = current boundary point
		
		double rgd2[];
		double rgd1[];
		
		rgd1 = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE};
		if(geo1.orthodromicDistanceWGS84(-90., 0., dLatCenter, dLonCenter)<=dRadius){
			rgd1[0]=-90.;
			rgd1[2]=-180.;
			rgd1[3]=180.;
		}else if(geo1.orthodromicDistanceWGS84(90., 0., dLatCenter, dLonCenter)<=dRadius){
			rgd1[1]=90.;
			rgd1[2]=-180.;
			rgd1[3]=180.;
		}	
		for(double d=0;d<360;d+=STEP_SIZE){
			rgd2=geo1.findDestinationWGS84(dLatCenter, dLonCenter, d, dRadius);
			if(rgd2[0]<rgd1[0]){
				rgd1[0]=rgd2[0];
			}
			if(rgd2[0]>rgd1[1]){
				rgd1[1]=rgd2[0];
			}
			if(rgd2[1]<rgd1[2]){
				rgd1[2]=rgd2[1];
			}
			if(rgd2[1]>rgd1[3]){
				rgd1[3]=rgd2[1];
			}
		}
		if(rgd1[2]<-170 && rgd1[3]>170){
			rgd1[2]=-180.;
			rgd1[3]=180.;
		}
		bds1 = new GeographicPointBounds(rgd1[0],rgd1[1],rgd1[2],rgd1[3]);	
	}
	
	/**
	 * Checks if spherical cap contains point.
	 * @param dLat Latitude of point to check.
	 * @param dLon Longitude of point to check.
	 * @return True if point is contains, false otherwise.
	 */
	public boolean contains(double dLat, double dLon){
		if(!this.boundsApproximate().contains(dLat, dLon)){
			return false;
		}	
		if(Math.abs((dLat-dLatCenter)*EarthGeometry.LAT_DISTANCE_SPHERE)<dRadius &&  geo1.orthodromicDistanceWGS84(dLat, dLon, dLatCenter, dLonCenter)<=dRadius){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if spherical cap contains point using approximate distance calculation (for speed).
	 * @param dLat Latitude of point to check.
	 * @param dLon Longitude of point to check.
	 * @return True if point is contains, false otherwise.
	 */
	public boolean containsApproximate(double dLat, double dLon){
		if(!this.boundsApproximate().contains(dLat, dLon)){
			return false;
		}	
		if(Math.abs((dLat-dLatCenter)*EarthGeometry.LAT_DISTANCE_SPHERE)<dRadius &&  EarthGeometry.orthodromicDistance(dLat, dLon, dLatCenter, dLonCenter)<=dRadius){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if spherical cap contains point.
	 * @param dLat Latitude of point to check.
	 * @param dLon Longitude of point to check.
	 * @return True if point is contains, false otherwise.
	 */
	public boolean containsCheckBounds(double dLat, double dLon){
		if(bds1==null){
			loadBounds();
		}
		if(Math.abs((dLat-dLatCenter)*EarthGeometry.LAT_DISTANCE_SPHERE)<dRadius && bds1.contains(dLat, dLon) &&  geo1.orthodromicDistanceWGS84(dLat, dLon, dLatCenter, dLonCenter)<=dRadius){
			return true;
		}else{
			return false;
		}
	}
	
	public SphericalMultiPolygon getPolygon(){
		if(ply1==null){
			ply1 = toPolygon(360);
		}
		return ply1;
	}

	public SphericalMultiPolygon getPolygon(int iVertices){
		if(ply1==null){
			ply1 = toPolygon(iVertices);
		}
		return ply1;
	}

	public SphericalMultiPolygon toPolygon(int iVertices){
		
		//rgd2 = current boundary point
		//lst1 = polygon in list format
		//d1 = small radius (if radius is 0)
		//dStepSize = step size
		
		double dStepSize;
		double d1;
		double rgd2[];
		ArrayList<Double[]> lst1;
		
		dStepSize = 360./((double) iVertices);
		lst1 = new ArrayList<Double[]>((int) (360/dStepSize)+2);
		if(dRadius<0.000000001){
			d1=0.00000001;
		}else{
			d1=dRadius;
		}
		for(double d=0;d<360;d+=dStepSize){	
			rgd2=geo1.findDestinationWGS84(dLatCenter, dLonCenter, d, d1);
			lst1.add(new Double[]{1.,rgd2[0], rgd2[1]});
		}
		lst1.add(new Double[]{lst1.get(0)[0], lst1.get(0)[1], lst1.get(0)[2]});	
		return new SphericalMultiPolygon(lst1,1234,false);
	}
	
	public GeographicPointBounds boundsApproximate(){
		
		//rgd1 = latitude min, latitude max, longitude min, longitude max
		//d1 = maximum number of longitudinal degrees per kilometer
		
		double rgd1[];
		double d1;
		
		if(bds1==null){
			rgd1 = new double[4];
			rgd1[0] = this.centerLatitude() - dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
			rgd1[1] = this.centerLatitude() + dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
			d1 = Math.min(
					geo1.orthodromicDistanceWGS84(rgd1[0], 0, rgd1[0], 1), 
					geo1.orthodromicDistanceWGS84(rgd1[1], 0, rgd1[1], 1));
			rgd1[2] = this.centerLongitude() - dRadius/d1;
			rgd1[3] = this.centerLongitude() + dRadius/d1;
			if(rgd1[0]<-90){
				rgd1[0]=-90;
			}
			if(rgd1[1]>90){
				rgd1[1]=90;
			}
			if(rgd1[2]<-180){
				rgd1[2]=-180;
			}
			if(rgd1[3]>180){
				rgd1[3]=180;
			}
			bds1 = new GeographicPointBounds(rgd1[0],rgd1[1],rgd1[2],rgd1[3]);
		}
		return bds1;
	}
	
	/**
	 * Gets bounding box
	 */
	public GeographicPointBounds getBounds(){
		if(bds1==null){
			loadBounds();
		}
		return bds1;
	}
	
	/**
	 * Sets bounding box
	 */
	public void setBounds(GeographicPointBounds bds1){
		this.bds1 = bds1;
	}
	
	public ArrayList<String> print(){
		
		//rgd2 = current boundary point
		//lstOut = output
		
		double rgd2[];
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>((int) (360/STEP_SIZE+10));
		lstOut.add("LATITUDE,LONGITUDE");
		for(double d=0;d<360;d+=STEP_SIZE){
			rgd2=geo1.findDestinationWGS84(dLatCenter, dLonCenter, d, dRadius);
			lstOut.add(rgd2[0] + "," + rgd2[1]);
		}
		return lstOut;
	}
	
	/**
	 * Checks if random point from polygon is contained in cap
	 * @param ply1 Polygon
	 */
	
	public boolean containsRandomPoint(SphericalMultiPolygon ply1){
		
		//rgd1 = point from boundary of polygon
		
		double rgd1[];
		
		rgd1 = ply1.getFirstBoundaryPoint();
		return this.contains(rgd1[0], rgd1[1]);
	}
	
	/**
	 * Checks if polygon is contained in cap
	 * @param ply1 Polygon
	 */
	public boolean contains(SphericalMultiPolygon ply1){
		
		//itr1 = polygon iterator
		//edg1 = current edge
		
		SphericalMultiPolygon.SphericalPolygonIterator itr1;
		SphericalMultiPolygon.SphericalPolygonEdge edg1 = null;
		
		if(!this.getBounds().intersects(ply1.getBounds())){
			return false;
		}
		itr1 = ply1.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(!this.contains(edg1.dLatStart,edg1.dLonStart)){
				return false;
			}
		}
		if(!this.contains(edg1.dLatEnd, edg1.dLonEnd)){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Generates a random adjacent non-intersecting cap
	 * @return Spherical cap adjacent to current cap. Direction between centers is random. Same size as current cap.
	 */
	public SphericalCapEarth randomAdjacentCap(){
		
		//d1 = direction
		//rgd1 = center of new cap
		
		double rgd1[];
		
		rgd1 = geo1.findDestinationWGS84(dLatCenter, dLonCenter, 360*rnd1.nextDouble(), dRadius*2.);
		return new SphericalCapEarth(
				dRadius,
				rgd1[0],
				rgd1[1],
				rnd1.nextInt());
	}
	
	/**
	 * Checks if current cap contains second cap
	 * @param cap1 Cap being checked
	 * @return True if cap being checked is a subset of current cap, false otherwise.
	 */
	public boolean contains(SphericalCapEarth cap1){
		
		//d1 = distance between centers of caps
		
		double d1;
		
		d1 = geo1.orthodromicDistanceWGS84(this.dLatCenter, this.dLonCenter, cap1.centerLatitude(), cap1.centerLongitude());
		
		if(d1<dRadius-cap1.radius()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if cap intersects 180 degrees W
	 */
	public boolean intersects180(){
		
		//rgd1 = previous coordinates
		//rgd2 = current coordinates
		
		double rgd1[]=null;
		double rgd2[]=null;
		
		for(double d=0;d<360;d+=STEP_SIZE){
			rgd2=geo1.findDestinationWGS84(dLatCenter, dLonCenter, d, dRadius);
			if(rgd1!=null){
				if(rgd1[1]>170 && rgd2[1]<-170){
					return true;
				}
				if(rgd1[1]<-170 && rgd2[1]>170){
					return true;
				}
			}
			rgd1 = rgd2;
		}	
		return false;
	}
	
	/**
	 * Checks if cap intersects another cap.
	 * @param cap1 Cap to check.
	 * @return True if intersection, false otherwise.
	 */
	public boolean intersects(SphericalCapEarth cap1){
		if(geo1.orthodromicDistanceWGS84(this.dLatCenter, this.dLonCenter, cap1.dLatCenter, cap1.dLonCenter)<(this.dRadius + cap1.radius())){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Checks if cap intersects specified edge
	 * @param edg1 Edge to check
	 * @return True if edge is intersected; false otherwise.
	 */
	public boolean intersects(SphericalPolygonEdge edg1){
		
		//a = distance from start vertex to center
		//b = distance from second vertex to center
		//c = edge length
		//s = (a+b+c)/2
		//h = height of triangle (closest distance of edge to center)
		
		double a;
		double b;
		double c;
		double s;
		double h;
		
		//checking endpoints
		if(this.contains(edg1.dLatStart,edg1.dLonStart)){
			return true;
		}
		if(this.contains(edg1.dLatEnd, edg1.dLonEnd)){
			return true;
		}
		
		
		//TODO bug here:
	
		//loading distances
		
		//****************************
		a = geo1.orthodromicDistanceWGS84(edg1.dLatStart,edg1.dLonStart, this.centerLatitude(), this.centerLongitude());
		b = geo1.orthodromicDistanceWGS84(edg1.dLatEnd,edg1.dLonEnd, this.centerLatitude(), this.centerLongitude());
		//a = geo1.orthodromicDistanceWGS84(edg1.dLatStart,edg1.dLonStart, -17.5, -79.5);
		//b = geo1.orthodromicDistanceWGS84(edg1.dLatEnd,edg1.dLonEnd, -17.5, -79.5);
		//****************************
		c = edg1.length();
		
		//checking distances
		if(a<this.radius()){
			return true;
		}
		if(b<this.radius()){
			return true;
		}
		
		//checking for obtuse angles
		if(!ExtendedMath.isAngleObtuse(b, c, a)){
			if(!ExtendedMath.isAngleObtuse(a, c, b)){
				
				//checking distance
				s = 0.5*(a+b+c);
				h = (2./c)*Math.sqrt(s*(s-a)*(s-b)*(s-c));
				if(h<this.radius()){
					return true;
				}else{
					return false;
				}
			}	
		}
		return false;
		
	}
	
	/**
	 * Checks if cap intersects specified edge
	 * @param edg1 Edge to check
	 * @return True if edge is intersected; false otherwise.
	 */
	public boolean intersects0(SphericalPolygonEdge edg1){
		
		//rgd1 = latitude, longitude vector
		//d1 = current multiplier
		//rgd2 = current set of distances
		
		double rgd1[];
		double rgd2[];
		double d1;
		
		//checking endpoints
		if(this.contains(edg1.dLatStart,edg1.dLonStart)){
			return true;
		}
		if(this.contains(edg1.dLatEnd, edg1.dLonEnd)){
			return true;
		}
		
		//checking along length of edge
		d1 = 0.5;
		rgd1 = new double[]{edg1.dLatEnd - edg1.dLatStart, edg1.dLonEnd - edg1.dLonStart};
		for(int i=0;i<5;i++){
			rgd2 = new double[]{
					geo1.orthodromicDistanceWGS84(this.centerLatitude(), this.centerLongitude(), edg1.dLatStart, edg1.dLonStart),
					geo1.orthodromicDistanceWGS84(this.centerLatitude(), this.centerLongitude(), edg1.dLatStart + d1*rgd1[0], edg1.dLonStart + d1*rgd1[1])};
			
			//updating multiplier
			if(rgd2[1]<this.radius()){
				return true;
			}
			if(rgd2[1]<rgd2[0]){
				d1 = d1 + 0.5*d1;
			}else{
				d1 = d1 - 0.5*d1;
			}
		}
		return false;
	}
	
	/**
	 * @return Radius of spherical cap.
	 */
	public double radius(){
		return dRadius;
	}
	
	/**
	 * @return Radius for cap of given area
	 */
	public static double radius(double dArea){
		return EARTH_RADIUS*acos(1.-dArea/(2.*PI*EARTH_RADIUS_SQUARED));
	}
	
	/**
	 * @return Latitude of center of spherical cap.
	 */
	public double centerLatitude(){
		return dLatCenter;
	}
	
	/**
	 * @return Longitude of center of spherical cap.
	 */	
	public double centerLongitude(){
		return dLonCenter;
	}
	
	/**
	 * @return Area of spherical cap.
	 */
	public double area(){
		if(Double.isNaN(dArea)){
			dArea = 2.*PI*EARTH_RADIUS_SQUARED*(1.-cos(dTheta));
		}
		return dArea;
	}
	
	/**
	 * @return Perimeter of spherical cap.
	 */
	public double perimeter(){
		
		if(Double.isNaN(dPerimeter)){
			dPerimeter = 2.*PI*EARTH_RADIUS*sin(dTheta);
		}
		return dPerimeter;
	}
	
	public ArrayList<Double[]> randomPointsInCap(){
		return randomPointsInCap(RANDOM_POINTS);
	}
	
	
	/**
	 * Returns list of random points in cap.
	 */
	public ArrayList<Double[]> randomPointsInCap(int iRandomPoints){
		
		//rgd1 = current random point
		
		double rgd1[];
		
		if(bds1==null){
			loadBounds();
		}
		if(this.lstRandom==null){
			lstRandom = new ArrayList<Double[]>(iRandomPoints);
			for(int i=0;i<iRandomPoints;i++){
				do{
					rgd1 = bds1.findRandomPoint();
				}while(!this.contains(rgd1[0],rgd1[1]));	
				lstRandom.add(new Double[]{rgd1[0], rgd1[1]});
			}
		}
		return lstRandom;
	}
}
