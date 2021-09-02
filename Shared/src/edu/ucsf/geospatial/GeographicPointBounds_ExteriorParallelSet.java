package edu.ucsf.geospatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;

/**
 * Class for polygons on Earth's surface.
 * @author jladau
 */
public class GeographicPointBounds_ExteriorParallelSet extends GeographicPointBounds{
	
	/**Map from radii to bounds**/
	private HashMap<Double,GeographicPointBounds> mapBoundsApprox;
	
	/**Initial (non-exterior set) bounds**/
	private GeographicPointBounds bds0;
	
	/**Flag for whether contained in polygon**/
	public boolean bContainedInPolygon = false;
	
	/**Set of polygon edges to consider**/
	public HashSet<SphericalPolygonEdge> setEdgesToConsider = null;
	
	public GeographicPointBounds_ExteriorParallelSet(GeographicPointBounds bds1) {
		super(bds1.dLatitudeMin, bds1.dLatitudeMax, bds1.dLongitudeMin, bds1.dLongitudeMax);
		this.bds0 = bds1;
		mapBoundsApprox = new HashMap<Double,GeographicPointBounds>();
	}
	
	public GeographicPointBounds_ExteriorParallelSet(double dLatitudeMin, double dLatitudeMax, double dLongitudeMin, double dLongitudeMax) {
		super(dLatitudeMin, dLatitudeMax, dLongitudeMin, dLongitudeMax);
		this.bds0 = new GeographicPointBounds(dLatitudeMin, dLatitudeMax, dLongitudeMin, dLongitudeMax);
		mapBoundsApprox = new HashMap<Double,GeographicPointBounds>();
	}
	
	//TODO write unit test
	/**
	 * Checks if polygon intersects bounds
	 * @param bds1 Bounds to check
	 */
	public boolean intersects(SphericalMultiPolygon ply1, double dRadius){
		
		//bds1 = approximate bounds
		
		GeographicPointBounds bds1;
		
		if(ply1.intersects(bds0)){
			return true;
		}
		bds1 = this.getApproximateBounds(dRadius);
		if(!ply1.intersects(bds1)){
			return false;
		}
		
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.dLatitudeMin, bds0.dLongitudeMin, 1234), setEdgesToConsider)){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.dLatitudeMin, bds0.dLongitudeMax, 1234), setEdgesToConsider)){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.dLatitudeMax, bds0.dLongitudeMax, 1234), setEdgesToConsider)){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.dLatitudeMax, bds0.dLongitudeMin, 1234), setEdgesToConsider)){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.dLatitudeMin, bds0.getCenterLongitude(), 1234), setEdgesToConsider)){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.getCenterLatitude(), bds0.dLongitudeMax, 1234), setEdgesToConsider)){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.dLatitudeMax, bds0.getCenterLongitude(), 1234), setEdgesToConsider)){
			return true;
		}
		if(ply1.intersects(new SphericalCapEarth(dRadius, bds0.getCenterLatitude(), bds0.dLongitudeMin, 1234), setEdgesToConsider)){
			return true;
		}
		return false;
	}
	
	//TODO write unit test
	/**
	 * Subdivides current bounds into four equal size bounds
	 * @return List of four bounds
	 */
	public ArrayList<GeographicPointBounds_ExteriorParallelSet> subdivide(){
		
		//lst1 = output
		
		ArrayList<GeographicPointBounds_ExteriorParallelSet> lst1;
		
		lst1 = new ArrayList<GeographicPointBounds_ExteriorParallelSet>();
		lst1.add(new GeographicPointBounds_ExteriorParallelSet(
				this.dLatitudeMin,
				this.getCenterLatitude(),
				this.dLongitudeMin,
				this.getCenterLongitude()));
		lst1.add(new GeographicPointBounds_ExteriorParallelSet(
				this.dLatitudeMin,
				this.getCenterLatitude(),
				this.getCenterLongitude(),
				this.dLongitudeMax));
		lst1.add(new GeographicPointBounds_ExteriorParallelSet(
				this.getCenterLatitude(),
				this.dLatitudeMax,
				this.getCenterLongitude(),
				this.dLongitudeMax));
		lst1.add(new GeographicPointBounds_ExteriorParallelSet(
				this.getCenterLatitude(),
				this.dLatitudeMax,
				this.dLongitudeMin,
				this.getCenterLongitude()));
		return lst1;
	}
	
	//TODO write unit test
	/**
	 * Returns the bounds for the polygon
	 * @return Bounds for polygon
	 */
	public GeographicPointBounds getApproximateBounds(double dRadius){
			
		//rgd1 = latitude min, latitude max, longitude min, longitude max
		//d1 = maximum number of longitudinal degrees per kilometer
		
		double rgd1[];
		double d1;
		
		if(!mapBoundsApprox.containsKey(dRadius)){
			rgd1 = new double[4];
			rgd1[0] = bds0.dLatitudeMin - dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
			rgd1[1] = bds0.dLatitudeMax + dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
			d1 = Math.min(
					EarthGeometry.orthodromicDistance(rgd1[0], 0, rgd1[0], 1), 
					EarthGeometry.orthodromicDistance(rgd1[1], 0, rgd1[1], 1));
			rgd1[2] = bds0.dLongitudeMin - dRadius/d1;
			rgd1[3] = bds0.dLongitudeMax + dRadius/d1;
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
			mapBoundsApprox.put(dRadius, new GeographicPointBounds(rgd1[0],rgd1[1],rgd1[2],rgd1[3]));
		}
		return mapBoundsApprox.get(dRadius);
	}
}
