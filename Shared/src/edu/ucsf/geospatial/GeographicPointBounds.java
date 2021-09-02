package edu.ucsf.geospatial;

/**
 * This class give a geographic bounding box
 * @author jladau
 */

import static edu.ucsf.geospatial.EarthGeometry.*;
import java.util.ArrayList;

import org.apache.commons.math3.util.Precision;

import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;

//TODO write unit tests

public class GeographicPointBounds {
	
	//dLatitudeMin = minimum latitude
	//dLatitudeMax = maximum latitude
	//dLongitudeMin = minimum longitude
	//dLongitudeMax = maximum longitude
	//dHeight = height (difference between latitude max and min)
	//dWidth = width (difference between longitude max and min)
	//bValid = true if valid bounding box, false otherwise
	//rgdArray = bounds in array format; entries in order are lon-min, lon-max, lat-min, lat-max
	//rnd1 = random number generator
	
	public double dLatitudeMin;
	public double dLatitudeMax;
	public double dLongitudeMin;
	public double dLongitudeMax;
	public double dHeight;
	public double dWidth;
	public boolean bValid;
	public double[] rgdArray;
	
	public GeographicPointBounds(){	
	}
	
	public String toString(){
		return dLatitudeMin + "," + dLatitudeMax + "," + dLongitudeMin + "," + dLongitudeMax;
	}
	
	public GeographicPointBounds merge(GeographicPointBounds bds1){
		
		return new GeographicPointBounds(
				Math.min(this.dLatitudeMin, bds1.dLatitudeMin),
				Math.max(this.dLatitudeMax, bds1.dLatitudeMax),
				Math.min(this.dLongitudeMin, bds1.dLongitudeMin),
				Math.max(this.dLongitudeMax, bds1.dLongitudeMax));
		
	}
	
	public ArrayList<Double[]> rasterPointsInBounds(double dResolution){
		
		//lst1 = output
		//dLatMin = minimum latitude
		//dLonMin = minimum longitude
		//dLonMid = longitude midpoint
		
		ArrayList<Double[]> lst1;
		double dLatMin = 0;
		double dLonMin = 0;
		double dLonMid;
		
		//initializing
		lst1 = new ArrayList<Double[]>((int) ((dLatitudeMax-dLatitudeMin)*(dLongitudeMax-dLongitudeMin)/(dResolution*dResolution)));
		
		//finding minimum latitude value on grid
		for(double dLat = -90+dResolution/2.;dLat<90;dLat+=0.5){
			dLat = Precision.round(dLat, 3);
			dLonMid = (dLongitudeMin + dLongitudeMax)/2.;
			if(this.contains(dLat, dLonMid)){
				dLatMin = dLat;
				break;
			}
		}
			
		//finding minimum longitude value on grid
		for(double dLon = -180 + dResolution/2.;dLon<180.;dLon+=0.5){
			dLon = Precision.round(dLon, 3);
			if(this.contains(dLatMin, dLon)){
				dLonMin = dLon;
				break;
			}
		}
		
		//finding grid
		for(double dLat = dLatMin; dLat<=dLatitudeMax;dLat+=0.5){
			dLat = Precision.round(dLat, 3);
			for(double dLon = dLonMin; dLon<=dLongitudeMax;dLon+=0.5){
				dLon = Precision.round(dLon, 3);
				lst1.add(new Double[]{dLat,dLon});
			}
		}
		return lst1;
	}
	
	
	public SphericalMultiPolygon toPolygon(int iPolygonID){
	
		//lst1 = list of vertices
		
		ArrayList<Double[]> lstPolygon;
		
		lstPolygon = new ArrayList<Double[]>();
		lstPolygon.add(new Double[]{(double) iPolygonID, this.dLatitudeMin, this.dLongitudeMin});
		lstPolygon.add(new Double[]{(double) iPolygonID, this.dLatitudeMax, this.dLongitudeMin});
		lstPolygon.add(new Double[]{(double) iPolygonID, this.dLatitudeMax, this.dLongitudeMax});
		lstPolygon.add(new Double[]{(double) iPolygonID, this.dLatitudeMin, this.dLongitudeMax});
		lstPolygon.add(new Double[]{(double) iPolygonID, this.dLatitudeMin, this.dLongitudeMin});
		
		
		return new SphericalMultiPolygon(lstPolygon, 1234, false);
	}
	
	public boolean equals(Object bds1){
		
		//bds2 = bds1 coerced to GeographicPointBounds
		
		GeographicPointBounds bds2;
		
		if(!(bds1 instanceof GeographicPointBounds)){
			return false;
		}
		
		bds2 = (GeographicPointBounds) bds1;
		if(bds2.dLatitudeMax!=this.dLatitudeMax){
			return false;
		}
		if(bds2.dLatitudeMin!=this.dLatitudeMin){
			return false;
		}
		if(bds2.dLongitudeMax!=this.dLongitudeMax){
			return false;
		}
		if(bds2.dLongitudeMin!=this.dLongitudeMin){
			return false;
		}
		return true;
	}
	
	public GeographicPointBounds(double dLatitudeMin, double dLatitudeMax, double dLongitudeMin, double dLongitudeMax){	
		this.dLatitudeMin = dLatitudeMin;
		this.dLatitudeMax = dLatitudeMax;
		this.dLongitudeMin = dLongitudeMin;
		this.dLongitudeMax = dLongitudeMax;
		this.dHeight = dLatitudeMax - dLatitudeMin;
		this.dWidth = dLongitudeMax - dLongitudeMin;
		rgdArray = new double[4];
		rgdArray[0] = dLongitudeMin;
		rgdArray[1] = dLongitudeMax;
		rgdArray[2] = dLatitudeMin;
		rgdArray[3] = dLatitudeMax;
		checkValidity();
	}
	
	/**
	 * Checks if current bounds intersect another set of bounds
	 */
	public boolean intersects(GeographicPointBounds bds1){
		
		if(bds1.dLatitudeMin > this.dLatitudeMax || bds1.dLatitudeMax < this.dLatitudeMin){
			return false;
		}
		
		if(bds1.dLongitudeMin > this.dLongitudeMax || bds1.dLongitudeMax < this.dLongitudeMin){
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Checks if point is in bounds
	 * @param dLat Latitude of point
	 * @param dLon Longitude of point
	 * @return True if point is within bounds, false otherwise
	 */
	public boolean contains(double dLat, double dLon){
		if(dLatitudeMin<=dLat){
			if(dLat<=dLatitudeMax){
				if(dLongitudeMin<=dLon){
					if(dLon<=dLongitudeMax){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	//TODO write unit test
	/**
	 * Checks if bounds intersects edge (line segment)
	 * @param edg1 Edge to consider
	 * @return True if intersection, false otherwise
	 */
	public boolean intersects(SphericalPolygonEdge edg1){
		
		//i1 = number of vertices above line
		
		int i1;
		
		//checking if bounds intersect
		if(!this.intersects(edg1.bounds())){
			return false;
		}
		
		//checking if line passes through bounds
		i1 = 0;
		i1+=EarthGeometry.northOfLine(dLatitudeMin, dLongitudeMin, edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd);
		i1+=EarthGeometry.northOfLine(dLatitudeMin, dLongitudeMax, edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd);
		i1+=EarthGeometry.northOfLine(dLatitudeMax, dLongitudeMin, edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd);
		i1+=EarthGeometry.northOfLine(dLatitudeMax, dLongitudeMax, edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd);
		if(i1 == -4 || i1 ==4){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Counts vertices of edge that are in bounds
	 * @param edg1 Edge
	 * @return Returns number of vertices that are in bounds: 0,1,2
	 */
	/*
	public int countVerticeInBounds(PolygonEdge edg1){
		
		if(this.isPointInBounds(edg1.dLatStart, edg1.dLonStart)){
			if(this.isPointInBounds(edg1.dLatEnd, edg1.dLonEnd)){
				return 2;
			}else{
				return 1;
			}
		}else{
			if(this.isPointInBounds(edg1.dLatEnd, edg1.dLonEnd)){
				return 1;
			}else{
				return 0;
			}
		}
	}
	*/
	
	//TODO write unit test
	/**
	 * Finds a random (uniformly distributed) point within bounds
	 * @return (Latitude,Longitude) of point.
	 */
	public double[] findRandomPoint(){
		
		//rgdBounds = polygon bounds
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgdOutput = output
		
		double dLat; double dLng; double d3 = 0; double d4 = 0;
		double rgdOutput[];
		
		//loading intermediate values
		d3 = Math.cos(DEG_TO_RAD*dLatitudeMin+Math.PI/2.);
		d4 = Math.cos(DEG_TO_RAD*dLatitudeMax+Math.PI/2.)-d3;	
			
		//loading Lat and Lng
		dLng = Math.random()*(dLongitudeMax-dLongitudeMin)+dLongitudeMin;
		dLat = Math.acos(d3+d4*Math.random());
		dLat = RAD_TO_DEG*dLat-90.;
	
		//outputting results
		rgdOutput = new double[2];
		rgdOutput[0]=dLat;
		rgdOutput[1]=dLng;
		
		//returning result
		return rgdOutput;
	}
	
	/**
	 * @return Edges of point bounds
	 */
	/*
	public HashSet<SphericalPolygonEdge> edgeSet(){
		
		//set1 = output
		
		HashSet<SphericalPolygonEdge> set1;
		
		set1 = new HashSet<SphericalPolygonEdge>(4);
		
		set1.add(new SphericalPolygonEdge("1", this.dLatitudeMin, this.dLatitudeMin, this.dLongitudeMin, this.dLongitudeMax));
		set1.add(new SphericalPolygonEdge("1", this.dLatitudeMin, this.dLatitudeMax, this.dLongitudeMax, this.dLongitudeMax));
		set1.add(new SphericalPolygonEdge("1", this.dLatitudeMax, this.dLatitudeMax, this.dLongitudeMax, this.dLongitudeMin));
		set1.add(new SphericalPolygonEdge("1", this.dLatitudeMax, this.dLatitudeMin, this.dLongitudeMin, this.dLongitudeMin));
		
		return set1;
	}
	*/
	
	//TODO write unit test
	/**
	 * Finds the area of the region within the given bounds.
	 * @return Area.
	 */
	public double area(){
		
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
	
		double d3; double d4;
		
		//loading d3 and d4
		d3 = Math.cos(DEG_TO_RAD*dLatitudeMin+Math.PI/2.);
		d4 = Math.cos(DEG_TO_RAD*dLatitudeMax+Math.PI/2.)-d3;
		
		//returning result
		return Math.abs(EARTH_RADIUS*EARTH_RADIUS*DEG_TO_RAD*(dLongitudeMax-dLongitudeMin)*d4);
	}
	
	private void checkValidity(){
		
		if(dLatitudeMin<-90){
			bValid=false;
			return;
		}
		if(dLatitudeMax>90){
			bValid=false;
			return;
		}
		if(dLongitudeMin<-180){
			bValid=false;
			return;
		}
		if(dLongitudeMax>180){
			bValid=false;
			return;
		}
		if(dHeight<0 || dHeight>180){
			bValid=false;
			return;
		}
		if(dWidth<0 || dWidth>360){
			bValid=false;
			return;
		}
		bValid=true;
	}

	/**
	 * Returns the area of the interior parallel set of the bounds at the specified radius
	 */
	/*
	public double findInteriorParallelSetArea(double dRadius){
		
		//dWidth = cell width to consider
		//dHeight = cell height to consider
		//dCellArea = current cell area
		//dLat = current latitude
		//dLon = current longitude
		//dLat0 = starting latitude
		//dLon0 = starting longitude
		//dOut = output
		//sph1 = spherical geometry object
		
		EarthGeometry sph1;
		double dLon0; double dLat0; double dWidth; double dHeight; double dCellArea; double dLat; double dLon; double dOut;
		
		//loading spherical geometry object
		sph1 = new EarthGeometry();
		
		//loading cell width and height
		dWidth=(this.dLongitudeMax-this.dLongitudeMin)/100.;
		dHeight=(this.dLatitudeMax-this.dLatitudeMin)/100.;
				
		//looping through cells
		dOut=0;
		dLat0 = this.dLatitudeMin+dHeight/2.;
		dLon0 = this.dLongitudeMin+dWidth/2.;
		for(int i=0;i<100;i++){
			dLat=dLat0 + ((double) i)*dHeight;
			dCellArea=EarthGeometry.findAreaCell(dLat, 0, dWidth, dHeight);
			for(int j=0;j<100;j++){
				dLon=dLon0 + ((double)j)*dWidth;
				if(sph1.checkDiskInBounds(dLat, dLon, dRadius, this.rgdArray)==1){
					dOut+=dCellArea;
				}
			}
		}
		
		//outputting result
		return dOut;
	}
	*/
	
	/**
	 * Returns the center (latitude,longitude) of the bounds
	 */
	
	public double[] getCenter(){
		
		//rgdOut = output
		
		double rgdOut[];
		
		rgdOut = new double[2];
		rgdOut[0]=(this.dLatitudeMin+this.dLatitudeMax)/2.;
		rgdOut[1]=(this.dLongitudeMin+this.dLongitudeMax)/2.;
		return rgdOut;
	}
	
	/**
	 * Returns the center latitude of the bounds
	 */
	
	public double getCenterLatitude(){
		return (this.dLatitudeMin+this.dLatitudeMax)/2.;
	}
	
	/**
	 * Returns the center longitude of the bounds
	 */
	
	public double getCenterLongitude(){
		return (this.dLongitudeMin+this.dLongitudeMax)/2.;
	}
	
	
	/*
	/**
	 * Finds bounds that are a given distance from current bounds
	 * @return Array of bounds giving inner bounding region.
	 */
	/*
	public ArrayList<GeographicPointBounds> findInnerBounds(double dDistance){
		
		//lst1 = output
		//dLonMin = minimum longitude
		//dLonMax = maximum longitude
		//sph1 = spherical geometry object
		//dLat = current latitude
		//dDistanceDeg = distance in degrees
		
		ArrayList<GeographicPointBounds> lst1;
		double dLonMin; double dLonMax; double dLat; double dDistanceDeg;
		SphericalGeometry sph1;
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//initializing output
		lst1 = new ArrayList<GeographicPointBounds>();
		
		//initializing dLat
		dLat=dLatitudeMin;
		
		//loading distance in degrees
		dDistanceDeg = dDistance/SphericalGeometry.LAT_DISTANCE_SPHERE;
		
		//looping through bounds
		for(double d=dLatitudeMin+dDistanceDeg;d<dLatitudeMax-dDistanceDeg;d+=0.5){
			
			//finding longitude 
			dLonMin=sph1.findDestination(d, this.dLongitudeMin, Math.PI/2., dDistance)[1];
			dLonMax=sph1.findDestination(d, this.dLongitudeMax, 3.*Math.PI/2., dDistance)[1];
			
			//loading new bounds
			lst1.add(new GeographicPointBounds(d,d+1.,dLonMin,dLonMax));
			
			//updating latitude
			dLat=d+1.;
		}
		
		//adding final bounds
		if(dLat<dLatitudeMax-dDistanceDeg){
			dLonMin=sph1.findDestination(dLat, this.dLongitudeMin, Math.PI/2., dDistance)[1];
			dLonMax=sph1.findDestination(dLat, this.dLongitudeMax, Math.PI, dDistance)[1];
			lst1.add(new GeographicPointBounds(dLat,dLatitudeMax-dDistanceDeg,dLonMin,dLonMax));
		}
		
		//outputting results
		return lst1;
	}
	*/
}
