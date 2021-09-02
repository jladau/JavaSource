package edu.ucsf.geospatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import com.google.common.collect.Range;
import com.vividsolutions.jts.geom.Envelope;

import edu.ucsf.base.ExtendedMath;

import static edu.ucsf.geospatial.EarthGeometry.*;
import static java.lang.Math.*;
import static com.google.common.math.DoubleMath.*;

/**
 * Class for polygons on Earth's surface.
 * @author jladau
 */
public class SphericalMultiPolygon {
	
	/**EarthGeometry object.**/
	//private EarthGeometry ert1;
	
	/**Number of random points to use**/
	private static final int RANDOM_POINTS=2000;

	/**SphericalPolygonIntervalTree for edges.**/
	private SphericalPolygonIntervalTree int1;

	/**List of edges.**/
	private ArrayList<SphericalPolygonEdge> lstEdges;

	/**Bounding box.**/
	private Envelope env1;

	/**Perimeter**/
	private double dPerimeter = Double.NaN;

	/**Area**/
	private double dArea = Double.NaN;

	/**Collection of random points in polygon**/
	private ArrayList<Double[]> lstRandomPointsInPolygon = null;
	
	/**Flag if from shapefile**/
	private boolean bFromShapefile;
	
	/**ID**/
	private String sID = null;
	
	/**
	 * Constructor
	 * @param rgdMultiPolygon Polygon in double format: first column polygon id, second column with latitude, third column with longitude
	 * @param iRandomSeed Random seed for adding noise to locations
	 */
	public SphericalMultiPolygon(ArrayList<Double[]> lstPolygon, int iRandomSeed, boolean bFromShapefile){
		
		//dOffset = small random offset so that test rays in interior point algorithm do not hit endpoints of edges
		//rnd1 = random number generator
		//i1 = coordinate of beginning of current polygon
		
		double dOffset;
		Random rnd1;
		int i1;
		
		//initializing bounds
		env1 = new Envelope();
		
		//initializing list of edges
		lstEdges = new ArrayList<SphericalPolygonEdge>();
		
		//saving shapefile flag
		this.bFromShapefile=bFromShapefile;
		
		//initializing random number generator
		rnd1 = new Random(iRandomSeed);
		
		//loading offset to prevent boundary cases
		dOffset = 0.0000000001*rnd1.nextDouble();
		
		//looping through edges
		i1=0;
		for(int i=1;i<lstPolygon.size();i++){
			
			//loading edge: same polygon
			if(lstPolygon.get(i)[0].doubleValue() == lstPolygon.get(i-1)[0].doubleValue()){
				this.addEdge(new SphericalPolygonEdge(
						Double.toString(lstPolygon.get(i)[0]), 
						lstPolygon.get(i-1)[1]+dOffset,
						lstPolygon.get(i)[1]+dOffset,
						lstPolygon.get(i-1)[2]+dOffset,
						lstPolygon.get(i)[2]+dOffset));
			}
				
			//closing loops if not from shapefile
			if(!bFromShapefile){
			
				//different polygon: loading closing edge
				if(lstPolygon.get(i)[0].doubleValue() != lstPolygon.get(i-1)[0].doubleValue()){
					if(lstPolygon.get(i-1)[1]+dOffset!=lstPolygon.get(i1)[1]+dOffset || lstPolygon.get(i-1)[2]+dOffset!=lstPolygon.get(i1)[2]+dOffset){
						this.addEdge(new SphericalPolygonEdge(
								Double.toString(lstPolygon.get(i-1)[0]),
								lstPolygon.get(i-1)[1]+dOffset,
								lstPolygon.get(i1)[1]+dOffset,
								lstPolygon.get(i-1)[2]+dOffset,
								lstPolygon.get(i1)[2]+dOffset));
					}
					i1=i;
				}	
				
				//last vertex: loading closing edge if necessary
				if(i==lstPolygon.size()-1){
					if(lstPolygon.get(i)[1]+dOffset!=lstPolygon.get(i1)[1]+dOffset || lstPolygon.get(i)[2]+dOffset!=lstPolygon.get(i1)[2]+dOffset){
						this.addEdge(new SphericalPolygonEdge(
								Double.toString(lstPolygon.get(i)[0]),
								lstPolygon.get(i)[1]+dOffset,
								lstPolygon.get(i1)[1]+dOffset,
								lstPolygon.get(i)[2]+dOffset,
								lstPolygon.get(i1)[2]+dOffset));
						i1=i;
					}
				}
			}
		}
	}
	
	public void setID(String sID){
		this.sID = sID;
	}
	
	public String id(){
		return sID;
	}
	
	
	/**
	 * Adds an edge.
	 * @param edg1 Edge being added.
	 */
	private void addEdge(SphericalPolygonEdge edg1){
		
		//checking if zero length
		if(edg1.bIsPoint){
			return;
		}
		
		//adding edge
		lstEdges.add(edg1);
		
		//expanding envelope
		env1.expandToInclude(new Envelope(edg1.getLonMinimum(),edg1.getLonMaximum(),edg1.getLatMinimum(),edg1.getLatMaximum()));
	}
	
	//TODO write unit test
	/**
	 * Returns number of edges
	 */
	public int edgeCount(){
		return lstEdges.size();
	}
	
	//TODO write unit test
	/**
	 * Calculates area
	 */
	public double area(){
		
		//rgd1 = current random point
		//i1 = count within polygon
		//bds1 = current bounds
		
		double rgd1[];
		int i1;
		GeographicPointBounds bds1;
		
		if(Double.isNaN(dArea)){
			lstRandomPointsInPolygon = new ArrayList<Double[]>(RANDOM_POINTS);
			bds1 = this.getBounds();
			i1=0;
			for(int i=0;i<RANDOM_POINTS;i++){
				rgd1 = bds1.findRandomPoint();
				if(this.contains(rgd1[0], rgd1[1])){
					i1++;
					lstRandomPointsInPolygon.add(new Double[]{rgd1[0],rgd1[1]});
				}
			}
			dArea = bds1.area()*((double) i1)/((double) RANDOM_POINTS);
		}
		return dArea;
	}
	
	//TODO write unit test
	/**
	 * Calculates area within union of spherical caps.
	 * @param rgc1 Spherical caps in which to calculate area.
	 */
	public double area(SphericalCapEarth[] rgc1){
		
		//i1 = count within cap or polygon
		//lst1 = list of caps intersecting range
		
		int i1;
		ArrayList<SphericalCapEarth> lst1;
		
		i1=0;
		
		lst1 = new ArrayList<SphericalCapEarth>(rgc1.length);
		for(SphericalCapEarth cap1:rgc1){
			if(this.intersects(cap1)){
				lst1.add(cap1);
			}
		}
		
		if(this.lstRandomPointsInPolygon==null){
			area();
		}
		for(Double rgd1[]:lstRandomPointsInPolygon){
			for(SphericalCapEarth cap1:lst1){
				if(cap1.contains(rgd1[0],rgd1[1])){
					i1++;
					break;
				}
			}
		}
		return area()*((double) i1)/((double) lstRandomPointsInPolygon.size());
	}

	//TODO write unit test
	/**
	 * Calculates perimeter within union of spherical caps.
	 * @param rgc1 Spherical caps in which to calculate area.
	 * @param bRecalculate Flag for whether to recalculate
	 */
	@Deprecated
	public double perimeter(SphericalCapEarth[] rgc1){
	
		//lst1 = list of caps intersecting range
		//d1 = output
		//bStart = flag for whether first vertex is within a cap
		//bEnd = flag for whether second vertex is within a cap
		
		ArrayList<SphericalCapEarth> lst1;
		double d1;
		boolean bStart;
		boolean bEnd;
		
		lst1 = new ArrayList<SphericalCapEarth>(rgc1.length);
		for(SphericalCapEarth cap1:rgc1){
			if(this.intersects(cap1)){
				lst1.add(cap1);
			}
		}
		
		//adding up edge lengths
		d1 = 0;
		for(SphericalPolygonEdge edg1:lstEdges){
			bStart = false;
			bEnd = false;
			for(SphericalCapEarth cap1:lst1){
				if(bStart==false && cap1.contains(edg1.dLatStart, edg1.dLonStart)){
					bStart = true;
				}
				if(bEnd==false && cap1.contains(edg1.dLatEnd, edg1.dLonEnd)){
					bEnd = true;
				}
				if(bEnd==true && bStart==true){
					break;
				}
			}
			if(bStart==true && bEnd==true){
				d1+=edg1.length();
			}else if(bStart==true ^ bEnd==true){
				d1+=edg1.length()/2.;
			}
		}		
		return d1;
	}
	
	//TODO write unit test
	/**
	 * Calculates area within spherical cap.
	 * @param cap1 Spherical cap in which to calculate area.
	 */
	public double area(SphericalCapEarth cap1){
		return area(cap1, false);
	}
		
	//TODO write unit test
	/**
	 * Calculates area within spherical cap.
	 * @param cap1 Spherical cap in which to calculate area.
	 * @param bApproximate Flag for whether to use approximate distances (for speed)
	 */
	public double area(SphericalCapEarth cap1, boolean bApproximate){
	
		//i1 = count within cap or polygon
		//lst1 = list of random points in cap
		
		int i1;
		ArrayList<Double[]> lst1;
		
		i1=0;
		
		if(this.area()<cap1.area()){
			if(this.lstRandomPointsInPolygon==null){
				area();
			}
			if(bApproximate==false){
				for(Double rgd1[]:lstRandomPointsInPolygon){
					if(cap1.contains(rgd1[0],rgd1[1])){
						i1++;
					}
				}
			}else{
				for(Double rgd1[]:lstRandomPointsInPolygon){
					if(cap1.containsApproximate(rgd1[0],rgd1[1])){
						i1++;
					}
				}
			}
			return area()*((double) i1)/((double) lstRandomPointsInPolygon.size());
		}else{
			lst1 = cap1.randomPointsInCap();
			for(Double rgd1[]:lst1){
				if(this.contains(rgd1[0], rgd1[1])){
					i1++;
				}
			}
			return cap1.area()*((double) i1)/((double) lst1.size());
		}
	}
	
	//TODO write unit test
	/**
	 * Calculates area within elliptical cap.
	 * @param cap1 Elliptical cap in which to calculate area.
	 * @param bApproximate Flag for whether to use approximate distances (for speed)
	 */
	public double area(EllipticalCapEarth cap1, boolean bApproximate) throws Exception{
	
		//i1 = count within cap or polygon
		//lst1 = list of random points in cap
		
		int i1;
		ArrayList<Double[]> lst1;
		
		i1=0;
		
		if(!this.getBounds().intersects(cap1.boundsApproximate())){
			return 0.;
		}
		if(this.area()<cap1.area()){
			if(this.lstRandomPointsInPolygon==null){
				area();
			}
			if(bApproximate==false){
				for(Double rgd1[]:lstRandomPointsInPolygon){
					if(cap1.contains(rgd1[0],rgd1[1])){
						i1++;
					}
				}
			}else{
				for(Double rgd1[]:lstRandomPointsInPolygon){
					if(cap1.containsApproximate(rgd1[0],rgd1[1])){
						i1++;
					}
				}
			}
			return area()*((double) i1)/((double) lstRandomPointsInPolygon.size());
		}else{
			lst1 = cap1.randomPointsInCap();
			for(Double rgd1[]:lst1){
				if(this.contains(rgd1[0], rgd1[1])){
					i1++;
				}
			}
			return cap1.area()*((double) i1)/((double) lst1.size());
		}
	}
	
	
	//TODO write unit test
	/**
	 * Calculates area of intersection with another polygon.
	 * @param ply1 Polygon with which to calculate area.
	 */
	public double area(SphericalMultiPolygon ply1){
	
		//i1 = count within cap or polygon
		
		int i1;
		
		i1=0;
		
		if(this.area()<=ply1.area()){
			if(this.lstRandomPointsInPolygon==null){
				area();
			}
			for(Double rgd1[]:lstRandomPointsInPolygon){
				if(ply1.contains(rgd1[0],rgd1[1])){
					i1++;
				}
			}
			return area()*((double) i1)/((double) lstRandomPointsInPolygon.size());
		}else{
			return ply1.area(this);
		}
	}
	
	//TODO write unit test
	/**
	 * Smoothes boundary of polygon
	 * @param iWindowSize Window size (in number of vertices to left and right of current vertex to consider
	 * @return Smoothed polygon
	 */
	public SphericalMultiPolygon smooth(int iWindowSize){
		
		//lstOut = output
		//lst1 = list of vertices
		//lstLat = current list of latitudes to consider
		//lstLon = current list of longitudes to consider
		//rgd1 = current vertex
		//mapRings = rings
		//i1 = number of lines of vertices
		//i2 = current index
		//d1 = current ring number
		
		double d1;
		int i2;
		int i1;
		HashMap<String,SphericalMultiPolygon> mapRings;
		ArrayList<Double[]> lstOut;
		ArrayList<Double[]> lst1;
		ArrayList<Double> lstLat;
		ArrayList<Double> lstLon;
		Double rgd1[] = null;
		
		//initializing
		lstOut = new ArrayList<Double[]>(this.edgeCount()+10);
		mapRings = this.getRings();
		d1 = 0;

		//looping through rings
		for(SphericalMultiPolygon ply1:mapRings.values()){
		
			//loading vertices
			lst1 = ply1.vertices();
			i1 = lst1.size()-1;
			d1++;
			
			//looping through vertices
			for(int i=0;i<lst1.size();i++){
				
				//loading current set of vertices
				lstLat = new ArrayList<Double>(iWindowSize*2+1);
				lstLon = new ArrayList<Double>(iWindowSize*2+1);
				for(int k=-iWindowSize;k<=iWindowSize;k++){
					i2 = (i + k)%i1;
					if(i2<0){
						do{
							i2+= i1;
						}while(i2<0);
					}
					
					rgd1 = lst1.get(i2);
					lstLat.add(rgd1[1]);
					lstLon.add(rgd1[2]);
				}
				lstOut.add(new Double[]{d1, ExtendedMath.mean(lstLat), ExtendedMath.mean(lstLon)});
			}
		}
		return new SphericalMultiPolygon(lstOut, 1234, false);
	}
	
	//TODO write unit test
	/**
	 * Returns set of vertices
	 */
	public ArrayList<Double[]> vertices(){
		
		//itr1 = iterator
		//edg1 = current edge
		//edg0 = previous edge
		//lstOut = output
		//i2 = ring
		
		int i2;
		ArrayList<Double[]> lstOut;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1 = null;
		SphericalPolygonEdge edg0 = null;
		
		itr1 = this.iterator();
		lstOut = new ArrayList<Double[]>(this.edgeCount());
		i2 = 0;
		edg1 = itr1.next();
		lstOut.add(new Double[]{(double) i2, edg1.dLatStart,edg1.dLonStart});
		while(itr1.hasNext()){
			edg0 = edg1;
			edg1 = itr1.next();
			if(!edg1.connectedTo(edg0)){
				i2++;
			}
			lstOut.add(new Double[]{(double) i2, edg1.dLatStart,edg1.dLonStart});
		}
		return lstOut;
	}
	
	/**
	 * Checks whether point is in given polygon.
	 * Assumes that south pole is not within polygon.
	 * @param dLat Latitude of test point.
	 * @param dLon Longitude of test point.
	  * @return False if point is outside of polygon; true if it is within polygon.
	 */
	public boolean contains(double dLat, double dLon){
		 
		//i1 = output
		//i2 = count of intersections
		//dX1 = current first longitude
		//dY1 = current first latitude
		//dX2 = current second longitude
		//dY2 = current second latitude
		//dXR = longitude in radians
		//dYR = latitude in radians
		//lst1 = list of edges bounding point	
		
		ArrayList<SphericalPolygonEdge> lst1;
		int i2=0;
		double dX1; double dY1; double dX2; double dY2; double dXR; double dYR;
		
		//checking if within bounds
		if(dLon<env1.getMinX()){
			return false;
		}
		if(dLon>env1.getMaxX()){
			return false;
		}
		lst1 = getIntersectingEdgesLongitude(dLon);
		
		//looping though edges intersecting dLon
		if(lst1!=null){
			for(int i=0;i<lst1.size();i++){
				 
				//checking if latitudes allow for intersection
				if(lst1.get(i).getLatMinimum()>dLat){
					continue;
				}
			 
				//checking if latitudes imply intersection
				if(lst1.get(i).getLatMaximum()<dLat){
					i2+=lst1.get(i).iWinding;
					continue;
				}
			 
				//loading current latitude and longitude
				dX1 = lst1.get(i).dLonStart;
				dY1 = lst1.get(i).dLatStart;
				dX2 = lst1.get(i).dLonEnd;
				dY2 = lst1.get(i).dLatEnd;
			 
				//converting to radians
				dX1=dX1*DEG_TO_RAD;
				dY1=dY1*DEG_TO_RAD;
				dX2=dX2*DEG_TO_RAD;
				dY2=dY2*DEG_TO_RAD;
				 
				//loading dXR and DYR
				dXR = dLon*DEG_TO_RAD;
				dYR = dLat*DEG_TO_RAD;
			 
				//checking for intersection
				if(findLatitudeOfOrthodromeIntersection(dY1,dX1,dY2,dX2,dXR)<=dYR){
					i2+=lst1.get(i).iWinding;
				}
			}
		}
		
		//outputting result
		if(i2!=0){
			return true;
		}else{ 
			return false;
		}
	}
	
	//TODO write unit test
	/**
	 * Checks if polygon contains spherical cap.
	 * @param cap1 Cap to check
	 * @return True if all vertices are within cap, false otherwise. Note the long edges can pose a problem.
	 */
	public boolean contains(SphericalCapEarth cap1){
		
		//bdsCap = spherical cap bounds
		
		GeographicPointBounds bdsCap;
		
		//checking if bounds intersect
		bdsCap = cap1.getBounds();
		if(!bdsCap.intersects(this.getBounds())){
			return false;
		}
		
		//checking if any points from cap are within range
		if(!this.containsRandomPoint(cap1)){
			return false;
		}
		
		//looping through edges
		for(SphericalPolygonEdge edg1:lstEdges){
			if(cap1.contains(edg1.dLatStart, edg1.dLonStart)){
				return false;
			}
		}
		if(cap1.contains(lstEdges.get(lstEdges.size()-1).dLatEnd, lstEdges.get(lstEdges.size()-1).dLonEnd)){
			return false;
		}
		return true;
	}
	
	//TODO write unit test
	/**
	 * Checks if two polygons intersect
	 */
	public boolean intersects(SphericalMultiPolygon ply1){
		if(intersectsVertex(ply1)){
			return true;
		}
		if(ply1.intersectsVertex(this)){
			return true;
		}
		return false;
	}
	
	//TODO write unit test
	/**
	 * Checks if polygon contains vertex on the boundary of another polygon
	 */
	public boolean intersectsVertex(SphericalMultiPolygon ply1){
		
		//bdsPolygon = bounds for polygon
		//itr1 = edge iterator
		//edg1 = current edge
		
		GeographicPointBounds bdsPolygon;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1;
		
		//checking if bounds intersect
		bdsPolygon = ply1.getBounds();
		if(!bdsPolygon.intersects(this.getBounds())){
			return false;
		}
		
		//iterating though edges
		itr1 = ply1.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(!this.contains(edg1.dLatStart,edg1.dLonStart)){
				return false;
			}
		}
		return true;
	}
	
	//TODO write unit test
	/**
	 * Checks if polygon contains a point from spherical cap
	 * @param cap1 Cap to check.
	 * @return True if point contained; false otherwise.
	 */
	
	public boolean containsRandomPoint(SphericalCapEarth cap1){
		return this.contains(cap1.centerLatitude(), cap1.centerLongitude());
	}
	
	/**
	 * Generates specified number of random points within polygon.
	 * @param iPoints Number of points to generate.
	 * @param iRandomSeed Random seed
	 * @return Array of random points within polygon. First column with latitudes, second column with longitudes.
	 */
	public double[][] generateRandomPointsInPolygon(int iPoints, int iRandomSeed){
		
		//rgdOut = output
		//rgd1 = current set of candidate random points
		//iRow = current output row
		//iCounter = counter for updating random seed
		
		double rgdOut[][];
		double rgd1[][];
		int iRow;
		int iCounter;
		
		rgdOut = new double[iPoints][2];
		iRow = 0;
		iCounter = 0;
		while(iRow<rgdOut.length){
			rgd1 = generateRandomPoints(env1,2,iRandomSeed + iCounter*89);
			iCounter++;
			for(int i=0;i<rgd1.length;i++){
				if(this.contains(rgd1[i][0], rgd1[i][1])){
					rgdOut[iRow][0]=rgd1[i][0];
					rgdOut[iRow][1]=rgd1[i][1];
					iRow++;
					if(iRow>=rgdOut.length){
						break;
					}
				}
			}
		}
		return rgdOut;
	}

	/**
	 * Returns the bounds for the polygon
	 * @return Bounds for polygon
	 */
	public GeographicPointBounds getBounds(){
		
		//rngLat = latitude range
		//rngLon = longitude range
		
		Range<Double> rngLat;
		Range<Double> rngLon;
		
		rngLat = this.latitudeRange();
		rngLon = this.longitudeRange();
		return new GeographicPointBounds(rngLat.lowerEndpoint(), rngLat.upperEndpoint(), rngLon.lowerEndpoint(), rngLon.upperEndpoint());
	}
	
	//TODO write unit test
	public double[] getFirstBoundaryPoint(){
		return new double[]{lstEdges.get(0).dLatStart, lstEdges.get(0).dLonStart};
	}

	/**
	 * Gets list of edges intersecting given longitude
	 * @param dLon Longitude of interest
	 * @return List of edges intersecting given longitude
	 */
	private ArrayList<SphericalPolygonEdge> getIntersectingEdgesLongitude(double dLon){
		
		//loading interval tree if necessary
		if(int1==null){
			int1 = new SphericalPolygonIntervalTree(lstEdges);
		}
		
		//outputting result
		return int1.findEdgesLongitude(dLon);
	}
	
	//TODO write unit test
	/**
	 * Gets rings from polygon
	 * @return Map with rings. IDs match edge ids.
	 */
	public HashMap<String, SphericalMultiPolygon> getRings(){
		
		//map1 = output
		//itr1 = iterator
		//edg1 = current edge
		//map2 = map to arraylists with polygon edges
		
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1;
		HashMap<String,SphericalMultiPolygon> map1;
		HashMap<String,ArrayList<Double[]>> map2;
		
		//loading vertices of rings
		map2 = new HashMap<String,ArrayList<Double[]>>(this.lstEdges.size());
		itr1 = new SphericalPolygonIterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(!map2.containsKey(edg1.sID)){
				map2.put(edg1.sID, new ArrayList<Double[]>());
			}
			map2.get(edg1.sID).add(new Double[]{1.,edg1.dLatStart, edg1.dLonStart});
			if(!itr1.hasNext()){
				map2.get(edg1.sID).add(new Double[]{1.,edg1.dLatEnd, edg1.dLonEnd});
			}
		}
		
		//converting rings to polygons
		map1 = new HashMap<String,SphericalMultiPolygon>();
		for(String s:map2.keySet()){
			map1.put(s, new SphericalMultiPolygon(map2.get(s),1234,bFromShapefile));
		}
		return map1;
	}
	
	//TODO write unit test
	/**
	 * Loads set of edges contained within bounds
	 */
	public HashSet<SphericalPolygonEdge> intersectingEdges(GeographicPointBounds bds1){
		
		//setOut = output
		//itr1 = iterator
		//edg1 = current edge
		
		HashSet<SphericalPolygonEdge> setOut;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1 = null;
		
		setOut = new HashSet<SphericalPolygonEdge>(this.edgeCount());
		itr1 = this.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(bds1.intersects(edg1)){
				setOut.add(edg1);
			}
		}
		return setOut;
	}
	
	//TODO write unit test
	/**
	 * Loads set of edges contained within spherical cap
	 */
	public HashSet<SphericalPolygonEdge> intersectingEdges(SphericalCapEarth cap1){
		
		//setOut = output
		//itr1 = iterator
		//edg1 = current edge
		
		HashSet<SphericalPolygonEdge> setOut;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1 = null;
		
		setOut = new HashSet<SphericalPolygonEdge>(this.edgeCount());
		itr1 = this.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(cap1.intersects(edg1)){
				setOut.add(edg1);
			}
		}
		return setOut;
	}

	//TODO write unit test
	/**
	 * Loads set of vertices contained within spherical cap
	 */
	public HashSet<Double[]> intersectingVertices(SphericalCapEarth cap1){
		
		//setOut = output
		//itr1 = iterator
		//edg1 = current edge
		
		HashSet<Double[]> setOut;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1 = null;
		
		setOut = new HashSet<Double[]>(this.edgeCount());
		itr1 = this.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(cap1.contains(edg1.dLatStart, edg1.dLonStart)){
				setOut.add(new Double[]{edg1.dLatStart, edg1.dLonStart});
			}
		}
		if(cap1.contains(edg1.dLatEnd, edg1.dLonEnd)){
			if(cap1.contains(edg1.dLatEnd, edg1.dLonEnd)){
				setOut.add(new Double[]{edg1.dLatEnd, edg1.dLonEnd});
			}
		}
		return setOut;
	}
	
	public boolean intersects(SphericalCapEarth cap1){
		return intersects(cap1, null);
	}
	
	public boolean intersects(SphericalCapEarth cap1, HashSet<SphericalPolygonEdge> setCandidateEdges){
		
		//itr1 = iterator
		
		SphericalPolygonIterator itr1;


		//checking if bounds allow intersection
		if(!this.getBounds().intersects(cap1.boundsApproximate())){
			return false;
		}
		
		//checking for subsets
		if(this.containsRandomPoint(cap1)){
			return true;
		}
		if(cap1.containsRandomPoint(this)){
			return true;
		}
		
		//looping through edges
		if(setCandidateEdges!=null){
			for(SphericalPolygonEdge edg:setCandidateEdges){
				if(cap1.intersects(edg)){
					return true;
				}
			}
		}else{
			itr1 = this.iterator();
			while(itr1.hasNext()){
				if(cap1.intersects(itr1.next())){
					return true;
				}
			}
		}
		
		//no intersection found
		return false;
		
	}
	
	//TODO write unit test
	/**
	 * Checks if polygon intersects bounds
	 * @param bds1 Bounds to check
	 */
	public boolean intersects(GeographicPointBounds bds1){
		
		//itr1 = iterator
		//edg1 = current edge
		
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1 = null;
		
		//checking for bounds intersection
		if(!this.getBounds().intersects(bds1)){
			return false;
		}
		
		//checking for subsets
		if(this.contains(bds1.dLatitudeMin, bds1.dLongitudeMin)){
			return true;
		}
		
		//checking if any edges intersect
		itr1 = this.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(bds1.intersects(edg1)){
				return true;
			}
		}
		return false;
	}

	//TODO write unit test
	/**
	 * Checks if polygon contains bounds
	 * @param bds1 Bounds to check
	 */
	public boolean contains(GeographicPointBounds bds1){
		
		//itr1 = iterator
		//edg1 = current edge
		
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1 = null;
		
		//checking for bounds intersection
		if(!this.getBounds().intersects(bds1)){
			return false;
		}
		
		//checking for subsets
		if(!this.contains(bds1.dLatitudeMin, bds1.dLongitudeMin)){
			return false;
		}

		//checking if any edges intersect
		itr1 = this.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(bds1.intersects(edg1)){
				return false;
			}
		}
		return true;
	}
	
	
	//TODO write unit test
	/**
	 * Gets iterator for edges
	 */
	public SphericalPolygonIterator iterator(){
		return new SphericalPolygonIterator();
	}

	/**
	 * Gets list of edges intersecting given latitude
	 * @param dLat Latitude of interest
	 * @return List of edges intersecting given Latitude
	 */
	/*
	private ArrayList<SphericalPolygonEdge> getIntersectingEdgesLatitude(double dLat){
		
		//loading interval tree if necessary
		if(int1==null){
			int1 = new SphericalPolygonIntervalTree(lstEdges);
		}
		
		//outputting result
		return int1.findEdgesLatitude(dLat);
	}
	*/

	/**
	 * Returns the range of latitudes occupied by the polygon
	 */
	//TODO write unit test
	public Range<Double> latitudeRange(){
		if(env1.getMinY()<env1.getMaxY()){
			return Range.closed(env1.getMinY(), env1.getMaxY());
		}else{
			return Range.closed(env1.getMaxY(), env1.getMinY());
		}
	}

	/**
	 * Returns the range of longitude occupied by the polygon
	 */
	//TODO write unit test
	public Range<Double> longitudeRange(){
		if(env1.getMinX() < env1.getMaxX()){
			return Range.closed(env1.getMinX(), env1.getMaxX());
		}else{
			return Range.closed(env1.getMaxX(), env1.getMinX());
		}
	}
	
	
	//TODO write unit test
	/**
	 * Calculates perimeter
	 */
	public double perimeter(){
		if(Double.isNaN(dPerimeter)){
			dPerimeter=0;
			for(SphericalPolygonEdge edg1:lstEdges){
				dPerimeter+=edg1.length();
			}
		}
		return dPerimeter;
	}
	
	//TODO write unit test
	//TODO using half of edges crossing boundary: improved algorithm possible.
	/**
	 * Calculates perimeter within spherical cap.
	 * @param cap1 Spherical cap in which to calculate perimeter.
	 */
	public double perimeter(SphericalCapEarth cap1){
		
		//d1 = output
		//bdsCap = spherical cap bounds
		
		GeographicPointBounds bdsCap;
		double d1;
		
		//checking if bounds intersect
		bdsCap = cap1.getBounds();
		if(!bdsCap.intersects(this.getBounds())){
			return 0.;
		}
		
		//adding up edge lengths
		d1 = 0;
		for(SphericalPolygonEdge edg1:lstEdges){
			if(bdsCap.contains(edg1.dLatStart, edg1.dLonStart) || bdsCap.contains(edg1.dLatEnd, edg1.dLonEnd)){
				if(cap1.contains(edg1.dLatStart, edg1.dLonStart)){
					if(cap1.contains(edg1.dLatEnd, edg1.dLonEnd)){
						d1+=edg1.length();
					}else{
						d1+=this.edgeLengthWithinCap(cap1, edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd);
					}
				}else{
					if(cap1.contains(edg1.dLatEnd, edg1.dLonEnd)){
						d1+=this.edgeLengthWithinCap(cap1, edg1.dLatEnd, edg1.dLonEnd, edg1.dLatStart, edg1.dLonStart);
					}
				}
			}
		}	
		return d1;
	}
	
	private double edgeLengthWithinPolygon(SphericalMultiPolygon ply1, double dLatInside, double dLonInside, double dLatOutside, double dLonOutside){
		
		//rgd1 = current coordinate along edge
		//d1 = current step size
		//d2 = current parametric value
		
		double rgd1[] = null;
		double d1;
		double d2;
		
		d1 = 0.5;
		d2 = 0.5;
		for(int i=0;i<8;i++){
			rgd1 = new double[]{
				dLatInside + (dLatOutside-dLatInside)*d2,	
				dLonInside + (dLonOutside-dLonInside)*d2};
			d1 = 0.5*d1;
			if(!ply1.contains(rgd1[0],rgd1[1])){
				d2-=d1;
			}else{
				d2+=d1;
			}
		}
		return EarthGeometry.orthodromicDistance(dLatInside, dLonInside, rgd1[0], rgd1[1]);
	}
	
	
	
	private double edgeLengthWithinCap(SphericalCapEarth cap1, double dLatInside, double dLonInside, double dLatOutside, double dLonOutside){
		
		//rgd1 = current coordinate along edge
		//d1 = current step size
		//d2 = current parametric value
		
		double rgd1[] = null;
		double d1;
		double d2;
		
		d1 = 0.5;
		d2 = 0.5;
		for(int i=0;i<8;i++){
			rgd1 = new double[]{
				dLatInside + (dLatOutside-dLatInside)*d2,	
				dLonInside + (dLonOutside-dLonInside)*d2};
			d1 = 0.5*d1;
			if(!cap1.contains(rgd1[0],rgd1[1])){
				d2-=d1;
			}else{
				d2+=d1;
			}
		}
		return EarthGeometry.orthodromicDistance(dLatInside, dLonInside, rgd1[0], rgd1[1]);
	}
	
	
	//TODO write unit test
	//TODO using half of edges crossing boundary: improved algorithm possible.
	/**
	 * Calculates perimeter within another polygon
	 * @param ply1 Polygon.
	 */
	public double perimeter(SphericalMultiPolygon ply1){
		
		//d1 = output
		//bdsPly = spherical cap bounds
		
		GeographicPointBounds bdsPly;
		double d1;
		
		//checking if bounds intersect
		bdsPly = ply1.getBounds();
		if(!bdsPly.intersects(this.getBounds())){
			return 0.;
		}
		
		//adding up edge lengths
		d1 = 0;
		for(SphericalPolygonEdge edg1:lstEdges){
			if(bdsPly.contains(edg1.dLatStart, edg1.dLonStart) || bdsPly.contains(edg1.dLatEnd, edg1.dLonEnd)){
				if(ply1.contains(edg1.dLatStart, edg1.dLonStart)){
					if(ply1.contains(edg1.dLatEnd, edg1.dLonEnd)){
						d1+=edg1.length();
					}else{
						d1+=this.edgeLengthWithinPolygon(ply1, edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd);
					}
				}else{
					if(ply1.contains(edg1.dLatEnd, edg1.dLonEnd)){
						d1+=this.edgeLengthWithinPolygon(ply1, edg1.dLatEnd, edg1.dLonEnd, edg1.dLatStart, edg1.dLonStart);
					}
				}
			}
		}	
		return d1;
	}
	
	
	/**
	 * Prints map
	 * @return
	 */
	
	public ArrayList<String> print(){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(lstEdges.size()+1);
		lstOut.add("ID,Latitude,Longitude");
		for(int i=0;i<lstEdges.size();i++){
			if(i==0 || !lstEdges.get(i).sID.equals(lstEdges.get(i-1).sID)){
				lstOut.add(lstEdges.get(i).sID + "," + lstEdges.get(i).dLatStart + "," + lstEdges.get(i).dLonStart);
			}
			lstOut.add(lstEdges.get(i).sID + "," + lstEdges.get(i).dLatEnd + "," + lstEdges.get(i).dLonEnd);
		}
		return lstOut;
	}

	/**
	 * Class gives edges of polygon
	 * @author jladau
	 */
	public class SphericalPolygonEdge {
	
		//dLatStart = starting latitude
		//dLatEnd = ending latitude
		//dLonStart = starting longitude
		//dLonEnd = ending longitude
		//dLength = length of edge in km
		//iWinding = winding number of edge
		//sID = edge id (optional field)
		//bIsPoint = flag for whether edge has length greater than 0
		//bds1 = bounds
		
		private GeographicPointBounds bds1 = null;
		private double dLength = Double.NaN;
		public double dLatStart;
		public double dLatEnd;
		public double dLonStart;
		public double dLonEnd;
		public int iWinding;
		public String sID=null;
		public boolean bIsPoint;
		
		/**
		 * Constructor.
		 * @param sID ID
		 * @param dLatStart Starting latitude
		 * @param dLatEnd Ending latitude
		 * @param dLonStart Starting longitude
		 * @param dLonEnd Ending longitude
		 */
		public SphericalPolygonEdge(String sID, double dLatStart, double dLatEnd, double dLonStart, double dLonEnd){	
			
			//loading start and end points of edge
			this.dLatStart = dLatStart;
			this.dLatEnd = dLatEnd;
			this.dLonStart = dLonStart;
			this.dLonEnd = dLonEnd;
			this.sID = sID;
			
			//initializing length of edge
			//this.dLength = Double.NaN;
			if(fuzzyEquals(dLatStart,dLatEnd,0.00000000000001) && fuzzyEquals(dLonStart,dLonEnd,0.00000000000001)){
				bIsPoint=true;
			}else{
				bIsPoint=false;
			}
			
			//loading winding number
			if(dLonStart<dLonEnd){
				iWinding = 1;
			}else{
				iWinding = -1;
			}
		}
		
		/**
		 * Redefines edge equality so that edges are equal iff they have the same endpoints
		 */
		public boolean equals(Object edg1){
			
			//edg2 = edg1 cast to edge
			
			SphericalPolygonEdge edg2;
			
			//checking if edg1 is an Edge, returning false if not
			if(!(edg1 instanceof SphericalPolygonEdge)){
				return false;
			}
			
			//casting edg1 to edge
			edg2 = (SphericalPolygonEdge) edg1;
			
			//checking for same starting and ending locations
			if(edg2.dLatEnd==this.dLatEnd && edg2.dLatStart==this.dLatStart && edg2.dLonEnd==this.dLonEnd && edg2.dLonStart==this.dLonStart){
				return true;
			}
			if(edg2.dLatEnd==this.dLatStart && edg2.dLatStart==this.dLatEnd && edg2.dLonEnd==this.dLonStart && edg2.dLonStart==this.dLonEnd){
				return true;
			}
			return false;
		}
		
		public boolean connectedTo(SphericalPolygonEdge edg1){
			
			if(fuzzyEquals(dLatStart, edg1.dLatEnd, 0.000001)){
				if(fuzzyEquals(dLonStart, edg1.dLonEnd, 0.000001)){
					return true;
				}
			}
			if(fuzzyEquals(dLatEnd, edg1.dLatStart, 0.000001)){
				if(fuzzyEquals(dLonEnd, edg1.dLonStart, 0.000001)){
					return true;
				}
			}
			return false;
		}
		
		public String orientation(){
			
			if(fuzzyEquals(dLatStart, dLatEnd, 0.000001)){
				return "horizontal";
			}else if(fuzzyEquals(dLonStart, dLonEnd, 0.000001)){
				return "vertical";
			}else{
				return "diagonal";
			}
		}
		
		public double[] midpoint(){
			return new double[]{0.5*(dLatStart+dLatEnd),0.5*(dLonStart + dLonEnd)};
		}
		
		public GeographicPointBounds bounds(){
			
			if(bds1==null){
				if(dLatStart<dLatEnd){
					if(dLonStart<dLonEnd){
						bds1 = new GeographicPointBounds(dLatStart, dLatEnd, dLonStart, dLonEnd);
					}else{
						bds1 = new GeographicPointBounds(dLatStart, dLatEnd, dLonEnd, dLonStart);	
					}
				}else{
					if(dLonStart<dLonEnd){
						bds1 = new GeographicPointBounds(dLatEnd, dLatStart, dLonStart, dLonEnd);
					}else{
						bds1 = new GeographicPointBounds(dLatEnd, dLatStart, dLonEnd, dLonStart);	
					}
				}
			}
			return bds1;
		}
		
		public double getLatMaximum(){
			return max(dLatStart,dLatEnd);
		}
		
		public double getLatMinimum(){
			return min(dLatStart,dLatEnd);
		}
		
		public double getLonMaximum(){
			return max(dLonStart,dLonEnd);
		}
		
		public double getLonMinimum(){
			return min(dLonStart,dLonEnd);
		}
		
		//TODO write unit test
		/**
		 * Returns length of edge
		 * @return
		 */
		public double length(){
			if(Double.isNaN(dLength)){
				dLength = EarthGeometry.orthodromicDistance(dLatStart, dLonStart, dLatEnd, dLonEnd);
			}
			return dLength;
		}
		
		//TODO write unit test
		public String toString(){
			return dLatStart + "," + dLonStart + ":" + dLatEnd + "," + dLonEnd;
		}
	}

	/**
	 * Object for looking up edges that overlap a given point.
	 * @author jladau
	 */
	private class SphericalPolygonIntervalTree {
	
		//mapIntervalLat(dLatitude) = interval tree of latitudes, returns edges present at given interval endpoint excluding those that end at the endpoint.
		//mapIntervalLon(dLongitude) = interval tree of longitudes, returns edges present at given interval endpoint excluding those that end at the endpoint.
		//dMaxLat = maximum latitude
		//dMaxLon = maximum longitude
		
		private double dMaxLat = -9999;
		private double dMaxLon = -9999;
		private TreeMap<Double,ArrayList<SphericalPolygonEdge>> mapIntervalLat;
		private TreeMap<Double,ArrayList<SphericalPolygonEdge>> mapIntervalLon;
		
		/**
		 * constructor
		 * @param lstEdges List of edges to consider
		 */
		public SphericalPolygonIntervalTree(ArrayList<SphericalPolygonEdge> lstEdges){
			
			//loading longitude trees
			loadLongitudeTree(lstEdges);
			
			//loading latitude trees
			loadLatitudeTree(lstEdges);
		}
		
		/**
		 * Finds edges that overlap specified longitude
		 * @param dLon Longitude of interest
		 * @return List of edges that overlap specified longitude
		 */
		public ArrayList<SphericalPolygonEdge> findEdgesLongitude(Double dLon){
			
			//d1 = floor key
			
			Double d1;
			
			//checking if any edges are within bounds
			if(dLon<mapIntervalLon.firstKey() || dLon>dMaxLon){
				return null;
			}else{
				d1 = mapIntervalLon.floorKey(dLon);
				return mapIntervalLon.get(d1);
			}
		}
		
		/**
		 * Finds edges that overlap specified latitude
		 * @param dLat Latitude of interest
		 * @return List of edges that overlap specified latitude
		 */
		/*
		public ArrayList<SphericalPolygonEdge> findEdgesLatitude(Double dLat){
			
			//d1 = floor key
			
			Double d1;
			
			//checking if any edges are within bounds
			if(dLat<mapIntervalLat.firstKey() || dLat>dMaxLat){
				return null;
			}else{
				d1 = mapIntervalLat.floorKey(dLat);
				return mapIntervalLat.get(d1);
			}
		}
		*/
		
		/**
		 * Loads longitude tree
		 * @param lstEdges List of edges in polygon.  Edges that cross 180 are assumed to have enpoints inserted at that maerdian.
		 */
		private void loadLatitudeTree(ArrayList<SphericalPolygonEdge> lstEdges){
			
			//edg1 = current edge
			//lst1 = current list of edges
			//dLatStart = current starting latitude
			//dLatEnd = current ending latitude
			//set1 = set of edge start and end locations
			//set2 = subset of set1 between specified endpoints
			
			ArrayList<SphericalPolygonEdge> lst1;
			SphericalPolygonEdge edg1;
			Double dLatStart; Double dLatEnd;
			TreeSet<Double> set1; 
			Set<Double> set2;
			
			//initializing treemaps
			mapIntervalLat = new TreeMap<Double,ArrayList<SphericalPolygonEdge>>();
			
			//initializing treeset
			set1 = new TreeSet<Double>();
			
			//looping through edges to load end treemap
			for(int i=0;i<lstEdges.size();i++){
				
				//loading current edge
				edg1 = lstEdges.get(i);
				dLatStart = (Double) edg1.dLatStart;
				dLatEnd = (Double) edg1.dLatEnd;
				
				//saving start and end points
				set1.add(dLatStart);
				set1.add(dLatEnd);
				
				//updating maximum latitude
				if(dMaxLat<dLatStart){
					dMaxLat=dLatStart;
				}
				if(dMaxLat<dLatEnd){
					dMaxLat=dLatEnd;
				}
			}
			
			//loading interval map
			for(int i=0;i<lstEdges.size();i++){
				
				//loading current edge
				edg1 = lstEdges.get(i);
				dLatStart = (Double) edg1.dLatStart;
				dLatEnd = (Double) edg1.dLatEnd;
				
				//getting subset of endpoints between current endpoints
				if(dLatStart<dLatEnd){	
					set2 = set1.subSet(dLatStart, true, dLatEnd, false);
				}else{
					set2 = set1.subSet(dLatEnd, true, dLatStart, false);
				}
			
				//looping through elements of subset and updating interval map
				for(Double d : set2){
				
					//loading list of edges at given point
					if(mapIntervalLat.containsKey(d)){
						
						//loading current list of edges
						mapIntervalLat.get(d).add(edg1);
					}else{
					
						//initializing list edges
						lst1 = new ArrayList<SphericalPolygonEdge>(5);
						lst1.add(edg1);
					
						//saving list of edges
						mapIntervalLat.put(d, lst1);
					}
				}
			}
		}
	
		/**
		 * Loads longitude tree
		 * @param lstEdges List of edges in polygon.  Edges that cross 180 are assumed to have endpoints inserted at that merdian.
		 */
		private void loadLongitudeTree(ArrayList<SphericalPolygonEdge> lstEdges){
			
			//edg1 = current edge
			//lst1 = current list of edges
			//dLonStart = current starting longitude
			//dLonEnd = current ending longitude
			//set1 = set of edge start and end locations
			//set2 = subset of set1 between specified endpoints
			
			ArrayList<SphericalPolygonEdge> lst1;
			SphericalPolygonEdge edg1;
			Double dLonStart; Double dLonEnd;
			TreeSet<Double> set1; 
			Set<Double> set2;
			
			//initializing treemaps
			mapIntervalLon = new TreeMap<Double,ArrayList<SphericalPolygonEdge>>();
			
			//initializing treeset
			set1 = new TreeSet<Double>();
			
			//looping through edges to load start and end treemaps
			for(int i=0;i<lstEdges.size();i++){
				
				//loading current edge
				edg1 = lstEdges.get(i);
				dLonStart = (Double) edg1.dLonStart;
				dLonEnd = (Double) edg1.dLonEnd;
				
				//saving start and end points
				set1.add(dLonStart);
				set1.add(dLonEnd);
				
				//saving start and end points to interval map
				mapIntervalLon.put(dLonStart,null);
				mapIntervalLon.put(dLonEnd,null);
				
				//updating maximum latitude
				if(dMaxLon<dLonStart){
					dMaxLon=dLonStart;
				}
				if(dMaxLon<dLonEnd){
					dMaxLon=dLonEnd;
				}
			}
			
			//loading interval map
			for(int i=0;i<lstEdges.size();i++){
				
				//loading current edge
				edg1 = lstEdges.get(i);
				dLonStart = (Double) edg1.dLonStart;
				dLonEnd = (Double) edg1.dLonEnd;
				
				//getting subset of endpoints between current endpoints
				if(dLonStart<dLonEnd){	
					set2 = set1.subSet(dLonStart, true, dLonEnd, false);
				}else{
					set2 = set1.subSet(dLonEnd, true, dLonStart, false);
				}
			
				//looping through elements of subset and updating interval map
				for(Double d : set2){
				
					//loading list of edges at given point
					if(mapIntervalLon.get(d)!=null){
					
						//loading current list of edges
						mapIntervalLon.get(d).add(edg1);
					}else{
					
						//initializing list edges
						lst1 = new ArrayList<SphericalPolygonEdge>(5);
						lst1.add(edg1);
					
						//saving list of edges
						mapIntervalLon.put(d, lst1);
					}
				}
			}
		}
	}

	public class SphericalPolygonIterator implements Iterator<SphericalPolygonEdge>{
		
		/**Backing iterator**/
		private Iterator<SphericalPolygonEdge> itr1;
		
		
		public SphericalPolygonIterator(){
			itr1 = lstEdges.iterator();
		}
		
		public boolean hasNext(){
			return itr1.hasNext();
		}
		
		public SphericalPolygonEdge next(){
			return itr1.next();
		}
		
		public void remove(){
		}
	}
	
}
