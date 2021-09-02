package edu.ucsf.geospatial;

import java.util.ArrayList;
import java.util.HashMap;
import org.joda.time.LocalDate;
import com.google.common.collect.Range;

/**
 * Object for an affine transformation on data from different dates
 * @author jladau
 *
 */
public class GeospatialRasterAffineTransformation extends GeospatialRaster{

	/**Maps dates to coefficients**/
	private HashMap<String,Double> mapCoeff;
	
	/**Set of dates**/
	private ArrayList<LocalDate> lstDates;
	
	/**Set of values for given dates**/
	private HashMap<String,Double> mapValue;
	
	public GeospatialRasterAffineTransformation(
			double dLatResolution, 
			double dLonResolution, 
			Range<Double> rngLat, 
			Range<Double> rngLon,
			GeospatialRasterMetadata gmt1){
		
			super(dLatResolution,dLonResolution,rngLat,rngLon,gmt1);
	}
	
	public void initializeTransformation(String sAffineTransformation){
		
		//rgs1 = current coefficient, date pair
		
		String rgs1[];
		
		mapCoeff = new HashMap<String,Double>();
		mapValue = new HashMap<String,Double>();
		lstDates = new ArrayList<LocalDate>();
		for(String s:sAffineTransformation.split(";")){
			rgs1 = s.split(":");
			mapCoeff.put(rgs1[0], Double.parseDouble(rgs1[1]));
			if(!rgs1[0].equals("constant")){
				lstDates.add(new LocalDate(rgs1[0]));
			}
		}
		if(!mapCoeff.containsKey("constant")){
			mapCoeff.put("constant", 0.);
		}
	}
	
	public void loadTransformation(GeospatialRaster ras1, double dVert, LocalDate timOut) throws Exception{
		
		//itr1 = lat-lon iterator
		//cel1 = current cell
		
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		
		itr1 = ras1.getLatLonIterator(lstDates.get(0), dVert);
		while(itr1.hasNext()){
			cel1 = itr1.next();
			for(LocalDate tim1:lstDates){
				mapValue.put(tim1.toString(), ras1.get(cel1.axeLat.ID,cel1.axeLon.ID,tim1,dVert));
			}
			this.put(cel1.axeLat.ID,cel1.axeLon.ID,timOut,GeospatialRaster.NULL_VERT,this.getValue());
		}
	}
	
	private double getValue(){
		
		//dOut = output
		
		double dOut;
		
		dOut = 0;
		for(String s:mapValue.keySet()){
			dOut+=mapValue.get(s)*mapCoeff.get(s);
		}
		dOut+=mapCoeff.get("constant");
		return dOut;
	}
	
	
}
