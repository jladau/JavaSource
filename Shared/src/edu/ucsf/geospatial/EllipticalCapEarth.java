package edu.ucsf.geospatial;

import java.util.ArrayList;

/**
 * Code for defining an eliptical cap on the Earth, defined as the region equidistant from two foci
 * @author jladau
 *
 */

public class EllipticalCapEarth {

	/**Number of random points to use**/
	private static final int RANDOM_POINTS=1000;
	
	///**Bounds**/
	//private GeographicPointBounds bds1;
	
	///**Step size in degrees for finding bounds and random polygon**/
	//private static final double STEP_SIZE=1.;
	
	/**Total distance**/
	private double dDistance;
	
	/**Latitude of first focus**/
	private double dLat1;
	
	/**Longitude of first focus**/
	private double dLon1;
	
	/**Latitude of second focus**/
	private double dLat2;
	
	/**Longitude of second focus**/
	private double dLon2;
	
	/**Earth geometry object**/
	private EarthGeometry ert1;
	
	/**Collection of random points in cap**/
	private ArrayList<Double[]> lstRandom = null;
	
	/**Approximate bounds**/
	private GeographicPointBounds bdsApprox;
	
	/**Area**/
	private double dArea;
	
	public EllipticalCapEarth(double dLatitude1, double dLongitude1, double dLatitude2, double dLongitude2, double dDistanceTotal) throws Exception{
		
		dLat1 = dLatitude1;
		dLon1 = dLongitude1;
		dLat2 = dLatitude2;
		dLon2 = dLongitude2;
		dDistance = dDistanceTotal;
		ert1 = new EarthGeometry();
		bdsApprox = null;
		dArea = Double.NaN;
		if(ert1.orthodromicDistanceWGS84(dLat1, dLon1, dLat2, dLon2)>dDistance){
			throw new Exception("Ellipse distance must be at least distance between foci.");
		}
	}
	
	public GeographicPointBounds boundsApproximate(){
		
		//rgd1 = latitude min, latitude max, longitude min, longitude max
		//d1 = maximum number of longitudinal degrees per kilometer
		//dRadius = radius for finding bounds
		//rgd2 = center points
		//rgb1 = bounds
		
		GeographicPointBounds[] rgb1;
		double rgd1[];
		double rgd2[][];
		double d1;
		double dRadius;
		
		if(bdsApprox==null){
			rgd2 = new double[][]{{dLat1,dLon1},{dLat2,dLon2}};
			rgb1 = new GeographicPointBounds[2];
			for(int i=0;i<2;i++){	
				dRadius = dDistance/2.;
				rgd1 = new double[4];
				rgd1[0] = rgd2[i][0] - dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
				rgd1[1] = rgd2[i][0] + dRadius/EarthGeometry.LAT_DISTANCE_SPHERE;
				d1 = Math.min(
						ert1.orthodromicDistanceWGS84(rgd1[0], 0, rgd1[0], 1), 
						ert1.orthodromicDistanceWGS84(rgd1[1], 0, rgd1[1], 1));
				rgd1[2] = rgd2[i][1] - dRadius/d1;
				rgd1[3] = rgd2[i][1] + dRadius/d1;
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
				rgb1[i] = new GeographicPointBounds(rgd1[0],rgd1[1],rgd1[2],rgd1[3]);
			}
			bdsApprox = rgb1[0].merge(rgb1[1]);
		}
		return bdsApprox;
	}
	
	
	public boolean contains(double dLatitude, double dLongitude){
		if(!this.boundsApproximate().contains(dLatitude, dLongitude)){
			return false;
		}
		if(ert1.orthodromicDistanceWGS84(dLat1, dLon1, dLatitude, dLongitude) + ert1.orthodromicDistanceWGS84(dLat2, dLon2, dLatitude, dLongitude) < dDistance){
			return true;
		}else{
			return false;
		}
	}

	public boolean containsApproximate(double dLatitude, double dLongitude){
		if(!this.boundsApproximate().contains(dLatitude, dLongitude)){
			return false;
		}
		if(EarthGeometry.orthodromicDistance(dLat1, dLon1, dLatitude, dLongitude) + EarthGeometry.orthodromicDistance(dLat2, dLon2, dLatitude, dLongitude) < dDistance){
			return true;
		}else{
			return false;
		}
	}
	
	public double[] findBoundaryPoint(double dBearing, int iFocus, double dEpsilon) throws Exception{
		
		//rgd1 = focus latitude and longitude
		//rgd2 = second focus latitude and longitude
		//rgd3 = current candidate point latitude and longitude
		//d1 = current distance
		//d2 = current total distance
		//i1 = number of iterations
		//dStep = current step
		
		double rgd1[];
		double rgd2[];
		double rgd3[];
		double d1;
		double d2;
		double dStep;
		int i1;
		
		if(iFocus==1){
			rgd1 = new double[]{dLat1, dLon1};
			rgd2 = new double[]{dLat2, dLon2};
		}else{
			rgd1 = new double[]{dLat2, dLon2};
			rgd2 = new double[]{dLat1, dLon1};
		}
		
		
		d1 = dDistance/2.;
		dStep = d1/2.;
		i1 = 0;
		do{
			rgd3=ert1.findDestinationWGS84(rgd1[0], rgd1[1], dBearing, d1);
			d2 = d1 + ert1.orthodromicDistanceWGS84(rgd3[0],rgd3[1],rgd2[0],rgd2[1]);
			if(d2>dDistance){
				d1-=dStep;
			}else{
				d1+=dStep;
			}
			dStep/=2.;
			i1++;
		}while(Math.abs(d2-dDistance)>dEpsilon && i1<500);
		if(i1>=500){
			throw new Exception("Boundary point finding algorithm did not converge.");
		}
		return rgd3;
	}
	
	/**
	 * Returns list of random points in cap.
	 */
	public ArrayList<Double[]> randomPointsInCap() throws Exception{
		
		//rgd1 = current random point
		//i1 = counter
		
		double rgd1[];
		int i1;
		
		if(bdsApprox==null){
			boundsApproximate();
		}
		if(this.lstRandom==null){
			lstRandom = new ArrayList<Double[]>(RANDOM_POINTS);
			i1 = 0;
			for(int i=0;i<RANDOM_POINTS;i++){
				do{
					rgd1 = bdsApprox.findRandomPoint();
					i1++;
				}while(!this.contains(rgd1[0],rgd1[1]));	
				lstRandom.add(new Double[]{rgd1[0], rgd1[1]});
			}
			dArea = bdsApprox.area()*((double) lstRandom.size())/((double) i1);
		}
		return lstRandom;
	}
	
	public double area() throws Exception{
		if(Double.isNaN(dArea)){
			randomPointsInCap();
		}
		return dArea;
	}
	
	/*
	private void loadBounds() throws Exception{
		
		//rgd1 = min latitude, max latitude, min longitude, max longitude
		//rgd2 = current boundary point
		
		double rgd2[];
		double rgd1[];
		
		rgd1 = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE};
		if(this.contains(-90,  0)){
			rgd1[0]=-90.;
			rgd1[2]=-180.;
			rgd1[3]=180.;
		}else if(this.contains(90, 0)){
			rgd1[1]=90.;
			rgd1[2]=-180.;
			rgd1[3]=180.;
		}
		for(double d=0;d<360;d+=STEP_SIZE){
			rgd2=this.findBoundaryPoint(d, 1, 0.1);
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
	*/
	
	
	
	public SphericalMultiPolygon toPolygon(int iVertices) throws Exception{
		
		//rgd2 = current boundary point
		//lst1 = polygon in list format
		//dStepSize = step size
		
		double dStepSize;
		double rgd2[];
		ArrayList<Double[]> lst1;
		
		dStepSize = 360./((double) iVertices);
		lst1 = new ArrayList<Double[]>((int) (360/dStepSize)+2);
		for(double d=0;d<360;d+=dStepSize){	
			rgd2=this.findBoundaryPoint(d, 1, 0.001);
			lst1.add(new Double[]{1.,rgd2[0], rgd2[1]});
		}
		lst1.add(new Double[]{lst1.get(0)[0], lst1.get(0)[1], lst1.get(0)[2]});	
		return new SphericalMultiPolygon(lst1,1234,false);
	}
}
