package edu.ucsf.geospatial;

import java.util.HashMap;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonIterator;

/**
 * Class for polygons on Earth's surface.
 * @author jladau
 */
public class ExteriorParallelSet0 {
	
	/**Map from radii to bounds**/
	private HashMap<Double,GeographicPointBounds> mapBounds;
	
	/**Polygon**/
	public SphericalMultiPolygon ply1;
	
	public ExteriorParallelSet0(SphericalMultiPolygon ply1) {
		this.ply1 = ply1;
		mapBounds = new HashMap<Double,GeographicPointBounds>();
	}
	
	//TODO write unit test
	/**
	 * Checks if polygon intersects bounds
	 * @param bds1 Bounds to check
	 */
	public boolean intersects(GeographicPointBounds bds1, double dRadius){
		
		//itr1 = iterator
		//edg1 = current edge
		
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1 = null;
		
		if(!this.getBounds(dRadius).intersects(bds1)){
			return false;
		}
		
		itr1 = ply1.iterator();
		while(itr1.hasNext()){
			edg1 = itr1.next();
			if(bds1.contains(edg1.dLatStart, edg1.dLonStart)){
				return true;
			}
		}
		if(edg1!=null){
			if(bds1.contains(edg1.dLatEnd, edg1.dLonEnd)){
				return true;
			}
		}
		
		
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds1.dLatitudeMin, bds1.dLongitudeMin, 1234))){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds1.dLatitudeMin, bds1.dLongitudeMax, 1234))){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds1.dLatitudeMax, bds1.dLongitudeMax, 1234))){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds1.dLatitudeMax, bds1.dLongitudeMin, 1234))){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds1.dLatitudeMin,0.5*(bds1.dLongitudeMin+bds1.dLongitudeMax), 1234))){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, 0.5*(bds1.dLatitudeMin+bds1.dLatitudeMax),bds1.dLongitudeMax, 1234))){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds1.dLatitudeMax,0.5*(bds1.dLongitudeMin+bds1.dLongitudeMax), 1234))){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, 0.5*(bds1.dLatitudeMin+bds1.dLatitudeMax),bds1.dLongitudeMin, 1234))){
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the bounds for the polygon
	 * @return Bounds for polygon
	 */
	public GeographicPointBounds getBounds(double dRadius){
			
		//bds1 = current bounds
		//rgd1 = latitude min, latitude max, longitude min, longitude max
		//d1 = maximum number of longidtunal degrees per kilometer
		
		GeographicPointBounds bds1;
		double rgd1[];
		double d1;
		
		if(!mapBounds.containsKey(dRadius)){
			rgd1 = new double[4];
			bds1 = ply1.getBounds();
			rgd1[0] = bds1.dLatitudeMin - dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
			rgd1[1] = bds1.dLatitudeMax + dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
			d1 = Math.min(
					EarthGeometry.orthodromicDistance(rgd1[0], 0, rgd1[0], 1), 
					EarthGeometry.orthodromicDistance(rgd1[1], 0, rgd1[1], 1));
			rgd1[2] = bds1.dLongitudeMin - dRadius/d1;
			rgd1[3] = bds1.dLongitudeMax + dRadius/d1;
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
			mapBounds.put(dRadius, new GeographicPointBounds(rgd1[0],rgd1[1],rgd1[2],rgd1[3]));
		}
		return mapBounds.get(dRadius);
	}
}
