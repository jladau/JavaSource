package edu.ucsf.geospatial;

import com.google.common.collect.HashBasedTable;
import static java.lang.Math.*;

import java.util.ArrayList;

import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.DataIO;

import static edu.ucsf.geospatial.EarthGeometry.*;

/**
 * Sampling region for beta-diversity range shapes analysis
 * @author jladau
 */

public class SphericalCapEarth_SamplingRegion_SarEar extends SphericalCapEarth_SamplingRegion{
	
	/**Set of global perimeter and area values**/
	//private HashSet<Double[]> setGlobal;
	
	/**Set of regional perimeter and area values**/
	//private HashSet<Double[]> setRegional;
	
	/**Cap for calculating interior parallel set area and perimeter**/
	private SphericalCapEarth cap1 = null;
	
	/**Table  of perimeters and areas: rows are species names, columns are radii. First entry is perimeter, second entry is area**/
	private HashBasedTable<String,String,Double[]> tbl1;
	
	/**Smoothing radius**/
	private double dSmoothingRadius;
	
	/**ID**/
	private String sID;
	
	/**Cap as polygon**/
	private SphericalMultiPolygon plyCap;
	
	/**Flag for whether merged polygon is intersected**/
	private boolean bIntersectsMerged;
	
	/**Sampling plot radii**/
	private String[] rgsPlotRadii;
	
	public SphericalCapEarth_SamplingRegion_SarEar(
			double dSamplingRegionRadius, 
			double dLatCenter, 
			double dLonCenter, 
			double dSmoothingRadius, 
			int iRandomSeed, 
			String sID,
			String rgsPlotRadii[]){
		super(dSamplingRegionRadius, dLatCenter, dLonCenter, Double.NaN, iRandomSeed);
		tbl1 = HashBasedTable.create(10000,2);
		cap1 = new SphericalCapEarth(dSamplingRegionRadius - dSmoothingRadius, dLatCenter, dLonCenter, iRandomSeed);
		plyCap = cap1.toPolygon(360);
		this.dSmoothingRadius = dSmoothingRadius;
		this.sID = sID;
		this.bIntersectsMerged = false;
		this.rgsPlotRadii = rgsPlotRadii;
	}
	
	public String[] getPlotRadii(){
		return rgsPlotRadii;
	}
	
	public void loadMergedIntersection(SphericalMultiPolygon plyMerged){
		if(plyMerged.intersects(this)){
			this.bIntersectsMerged=true;
		}else{
			this.bIntersectsMerged=false;
		}
	}
	
	public boolean intersectsMerged(){
		return bIntersectsMerged;
	}
	
	public double calculateArea(SphericalMultiPolygon ply1){
		return ply1.area(cap1);
	}
	
	public void savePerimeterArea(SphericalMultiPolygon ply1, double dGlobalPerimeter, double dGlobalArea, String sSpecies){
		
		if(ply1.intersects(cap1)){		
			
			tbl1.put(sSpecies, "global", new Double[]{dGlobalPerimeter, dGlobalArea, 0.});
			double d1 = ply1.area(cap1);
			if(abs(d1-cap1.area())<1){
				tbl1.put(sSpecies, "regional", new Double[]{0., cap1.area(), cap1.perimeter()});	
			}else{
				
				//*******************************
				//tbl1.put(sSpecies, "regional", new Double[]{ply1.perimeter(plyCap), d1, plyCap.perimeter(ply1)});	
				tbl1.put(sSpecies, "regional", new Double[]{ply1.perimeter(cap1), d1, plyCap.perimeter(ply1)});	
				//*******************************
			}
			iNumberOfSpecies++;
		}
	}
	
	public double areaRegional(String sSpecies){
		return tbl1.get(sSpecies, "regional")[1];
	}
	
	public double prevalencePredictionEuclidean(double dRadius, String sSpecies, String sScale) throws Exception{
		
		//d1 = output
		//dF = area of region
		//dL = perimeter of region
		//dR = difference between radius and smoothing radius
		//rgd1 = current observation
		//dValue = output value
		//dc = cutoff value
		
		double dc;
		double dValue = Double.NaN;
		double dF;
		double dL;
		double dR;
		Double rgd1[];
		double c2;
		double c0;
		double c1;
		
		dL = cap1.perimeter();
		dF = cap1.area();
		
		dR = dRadius - dSmoothingRadius;
		if(!tbl1.contains(sSpecies, sScale)){
			return 0.;
		}
		rgd1 = tbl1.get(sSpecies, sScale);

		//global estimator
		if(sScale.equals("global")){
			dValue = (rgd1[1] + rgd1[0]*dR + PI*dR*dR)/(rgd1[1] + dF + rgd1[0]*dL/(2.*PI));
		
		//regional estimator
		}else if(sScale.equals("regional")){
			
			c2 = rgd1[2];
			c0 = rgd1[0];
			c1 = rgd1[1];
			dc = (-(c2-dL-c0) - sqrt((c0-c2+dL)*(c0-c2+dL) + 4.*(c1-dF)*PI))/(2.*PI);
			
			if(Double.isNaN(dc)){
				dc=Double.MAX_VALUE;
			}
			if(dR>dc){
				dValue=1.;
			}else{
				//***********************
				dValue = (rgd1[1] + dR*(rgd1[0] - rgd1[2]))/(dF - dL*dR + PI*dR*dR);				
				//System.out.println(rgd1[1] + "," + rgd1[0] + "," + rgd1[2]);
				//TODO if rgd1[0] or rgd1[2] are off by as little as 10, that could account for the bias that is observed
				//TODO exterior parallel sets are at a radius of approximately 10 km more than stated nominally for 50 km single spherical cap: check exterior parallel set code.
				//TODO exterior parallel sets are at a radius of approximately 10 km less than stated nominally for 50 km amphibians: check exterior parallel set code.
				//TODO exterior parallel sets that are either too large or too small can both give overestimated sar predictions
				//***********************
			}
			if(dValue>1){
				dValue=1;
			}else if(dValue<0){
				dValue=0;
			}
		}	
		return dValue;
	}
	
	
	public double prevalencePredictionSpherical(double dRadius, String sSpecies, String sScale) throws Exception{
		
		//dF0Cap = cap area
		//dL0Cap = cap perimeter
		//dF0RangeC = range area complement
		//dL0RangeC = range perimeter complement
		//dFRCap = cap area at radius r
		//dFRRange = range area at radius r
		//d1 = output
		//dR = difference between radius and smoothing radius
		//rgd1 = current observation
		//dValue = output value
		
		double dF0Cap;
		double dL0Cap;
		double dFRCap;
		double dF0RangeC;
		double dL0RangeC;
		double dFRRangeC;
		double dR;
		double dValue = Double.NaN;
		Double rgd1[];
		
		dR = dRadius - dSmoothingRadius;
		dR = -1.*dR;
		
		dL0Cap = cap1.perimeter();
		dF0Cap = cap1.area();
		
		if(!tbl1.contains(sSpecies, sScale)){
			return 0.;
		}
		rgd1 = tbl1.get(sSpecies, sScale);
		dF0RangeC = dF0Cap - rgd1[1];
		if(dF0RangeC<0){
			dF0RangeC = 0.;
		}
		dL0RangeC = rgd1[0] + (dL0Cap-rgd1[2]);
		if(dL0RangeC<0){
			dL0RangeC = 0.;
		}
		
		//global estimator
		if(sScale.equals("global")){
			throw new Exception("Global spherical predictions not currently supported...");
		
		//regional estimator
		}else if(sScale.equals("regional")){
			
			//see page 322 of Santalo
			dFRRangeC = dF0RangeC*cos(dR/EARTH_RADIUS) + dL0RangeC*EARTH_RADIUS*sin(dR/EARTH_RADIUS)+2.*PI*EARTH_RADIUS_SQUARED*(1.-cos(dR/EARTH_RADIUS));
			
			//loading cap area at radius r
			dFRCap = dF0Cap*cos(dR/EARTH_RADIUS) + dL0Cap*EARTH_RADIUS*sin(dR/EARTH_RADIUS)+2.*PI*EARTH_RADIUS_SQUARED*(1.-cos(dR/EARTH_RADIUS));
			
			//loading result
			dValue = 1 - dFRRangeC/dFRCap;
			if(dValue>1){
				dValue=1;
			}else if(dValue<0){
				dValue=1;
			}
		}	
		return dValue;
	}
	
	public String getID(){
		return sID;
	}
}
