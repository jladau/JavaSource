package edu.ucsf.Geospatial.FixedValueRegions;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.NetcdfReader;

public class FixedValueRegionsRaster {

	/**Variable name**/
	public String sVar;
	
	/**Time**/
	public LocalDate tim1;
	
	/**CDF reader object**/
	public NetcdfReader cdf1;
	
	/**Operation to use**/
	public String sOperation;
	
	/**Target value**/
	private double dTargetValue;
	
	/**Mean**/
	private double dMean = Double.NaN;
	
	/**Standard deviation**/
	private double dStDev = Double.NaN;
	
	public FixedValueRegionsRaster(NetcdfReader cdf1, String sVar, LocalDate tim1, String sOperation, double dTargetValue){
		this.sVar = sVar;
		this.cdf1 = cdf1;
		this.tim1 = tim1;
		this.sOperation = sOperation;
		this.dTargetValue = dTargetValue;
	}
	
	public double unstandardize(double d1){
		return d1*dStDev+dMean;
	}
	
	public double targetValue(){
		return dTargetValue;
	}
	
	public double targetValueStandardized(){
		return (dTargetValue-dMean)/dStDev;
	}
	
	public double getStandardizedValue(GeospatialRasterCell cel1){
		
		//d1 = raw value
		
		double d1;
		
		d1 = cdf1.get(cel1);
		if(!Double.isNaN(d1)){
			return (d1-dMean)/dStDev;
		}else{
			return d1;
		}
	}
	
	public double get(GeospatialRasterCell cel1){
		return cdf1.get(cel1);
	}
	
	public void loadMeanStdev(SphericalMultiPolygon ply1, int iRandomSeed){
		
		//rgd1 = random points in polygon
		//lst1 = list of values
		//d1 = candidate value
		
		double rgd1[][];
		ArrayList<Double> lst1;
		double d1;
		
		rgd1 = ply1.generateRandomPointsInPolygon(500, iRandomSeed);
		lst1 = new ArrayList<Double>(rgd1.length);
		for(int i=0;i<rgd1.length;i++){
			d1 = cdf1.get(rgd1[i][0], rgd1[i][1], tim1, GeospatialRaster.NULL_VERT);
			if(!Double.isNaN(d1)){
				lst1.add(d1);
			}
		}
		dMean = ExtendedMath.mean(lst1);
		dStDev = ExtendedMath.standardDeviationP(lst1);
	}
	
	
	
	public void updateTime(LocalDate tim1){
		this.tim1 = tim1;
	}
}
