package edu.ucsf.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import edu.ucsf.geospatial.GeospatialRaster.AxisElement;

/**
 * Writes netcdf using specified rasters
 * @author jladau
 */

public class NetcdfWriter {

	/**Writer**/
	private NetcdfFileWriter cdf1;
	
	/**Flag for whether there is a time dimension**/
	//private boolean bHasTime;
	
	/**Flag for whether there is a vert dimension**/
	//private boolean bHasVert;
	
	/**Index for time**/
	private HashMap<LocalDate,Integer> mapTimeIndex = null;

	/**Index for time bounds**/
	private HashMap<LocalDate,Integer> mapTimeBoundsIndex = null;
	
	/**Index for vert**/
	private HashMap<Double,Integer> mapVertIndex = null;
	
	/**Index for vert bounds**/
	private HashMap<Double,Integer> mapVertBoundsIndex = null;
	
	/**Time variable**/
	private Variable varTime = null;
	
	/**Climatology bounds variable**/
	private Variable varTimeBounds = null;
	
	/**Vert variable**/
	private Variable varVert = null;
	
	/**Vert variable bounds**/
	private Variable varVertBounds = null;
	
	/**Map from variable names to variable ids**/
	private HashMap<String,Variable> mapVar = null;
	
	/**Rasters**/
	private GeospatialRaster[] rgr1;
	
	/**Set of times**/
	private ArrayList<AxisElement<LocalDate>> lstTimes;
	
	/**Set of verts**/
	private ArrayList<AxisElement<Double>> lstVerts;
	
	public NetcdfWriter(GeospatialRaster[] rgr1, String sOutputPath) throws Exception{
		cdf1 = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, sOutputPath);
		this.rgr1 = rgr1;
		initialize();
	}

	//public void initialize(double[] rgdLat, double[] rgdLon, String[] rgsVar, String[] rgsVarUnits, double rgdVert[], String sVertUnits, LocalDate rgtTime[]) throws Exception{
	private void initialize() throws Exception{
		
		//varLat = latitude variable
		//varLon = longitude variable
		//rgd1 = current latitude/longitude dimension
		//rgd2 = current latitude/longitude bounds
		//varLatBounds = latitude bounds variable
		//varLonBounds = longitude bounds variable
		//sDims = dims string variable
		
		Variable varLat;
		Variable varLon;
		Variable varLatBounds;
		Variable varLonBounds;
		double rgd1[];
		double rgd2[][];
		String sDims = null;
		
		//loading general information
		for(Entry<String, String> ent1:rgr1[0].gmt1.getGlobalAttributes().entrySet()){
			cdf1.addGroupAttribute(null, new Attribute(ent1.getKey(), ent1.getValue()));	
		}
		
		//loading dimensions
		loadTimesVerts();
		cdf1.addDimension(null, "lat", rgr1[0].axsLat.size());
		cdf1.addDimension(null, "lon", rgr1[0].axsLon.size());
	    if(hasTime()){cdf1.addUnlimitedDimension("time");}
	    if(hasVert()){cdf1.addDimension(null,"vert",lstVerts.size());}
		cdf1.addDimension(null, "nv", 2);
	    
	    //defining latitude variable
	    varLat = cdf1.addVariable(null, "lat", DataType.DOUBLE, "lat");
	    varLat.addAttribute(new Attribute("units", "degrees_north"));
	    varLat.addAttribute(new Attribute("standard_name", "latitude"));
	    varLat.addAttribute(new Attribute("long_name", "latitude"));
	    varLat.addAttribute(new Attribute("standard_name", "latitude"));
	    varLat.addAttribute(new Attribute("axis", "Y"));
	    varLat.addAttribute(new Attribute("bounds", "lat_bnds"));
	    
	    //defining latitude bounds variable
	    varLatBounds = cdf1.addVariable(null, "lat_bnds", DataType.DOUBLE, "lat nv");
		
	    //defining longitude variable
	    varLon = cdf1.addVariable(null, "lon", DataType.DOUBLE, "lon");
	    varLon.addAttribute(new Attribute("units", "degrees_east"));
	    varLon.addAttribute(new Attribute("standard_name", "longitude"));
	    varLon.addAttribute(new Attribute("long_name", "longitude"));
	    varLon.addAttribute(new Attribute("standard_name", "longitude"));
	    varLon.addAttribute(new Attribute("axis", "X"));
	    varLon.addAttribute(new Attribute("bounds", "lon_bnds"));
	    
	    //defining longitude bounds variable
	    varLonBounds = cdf1.addVariable(null, "lon_bnds", DataType.DOUBLE, "lon nv");
		
	    if(hasTime()){
	    
		    //defining time variable
		    varTime = cdf1.addVariable(null, "time", DataType.DOUBLE, "time");
		    varTime.addAttribute( new Attribute("units", "days since 1970-01-01"));
		    varTime.addAttribute(new Attribute("calendar", "gregorian"));
		    varTime.addAttribute(new Attribute("axis", "T"));
		    varTime.addAttribute(new Attribute("long_name", "time"));
		    varTime.addAttribute(new Attribute("standard_name", "time"));
		    varTime.addAttribute(new Attribute("climatology", "climatology_bounds"));
		    
		    //defining time bounds variable
		    varTimeBounds = cdf1.addVariable(null, "climatology_bounds", DataType.DOUBLE, "time nv");
	    }
		   
	    if(hasVert()){
	    
		    //defining vert variable
		    varVert = cdf1.addVariable(null, "vert", DataType.DOUBLE, "vert");
	    	varVert.addAttribute(new Attribute("units", "meters"));
	    	varVert.addAttribute(new Attribute("positive", "up"));
	    	varVert.addAttribute( new Attribute("axis", "Z"));
		    varVert.addAttribute(new Attribute("bounds", "vert_bnds"));
	    	
		    //defining vert bounds
		    varVertBounds = cdf1.addVariable(null, "vert_bnds", DataType.DOUBLE, "vert nv");
	    }
		    
	    //defining data variables
		mapVar = new HashMap<String,Variable>(rgr1.length);
	    for(int k=0;k<rgr1.length;k++){
	    	if(rgr1[k].hasVert() && rgr1[k].hasTime()){
	    		sDims = "time vert lat lon";
	    	}else if(rgr1[k].hasVert() && !rgr1[k].hasTime()){
	    		sDims = "vert lat lon";
	    	}else if(!rgr1[k].hasVert() && rgr1[k].hasTime()){
	    		sDims = "time lat lon";
	    	}else if(!rgr1[k].hasVert() && !rgr1[k].hasTime()){
	    		sDims = "lat lon";
	    	}
	    	mapVar.put(rgr1[k].gmt1.variable, cdf1.addVariable(null, rgr1[k].gmt1.variable, DataType.DOUBLE, sDims));
	    	mapVar.get(rgr1[k].gmt1.variable).addAttribute(new Attribute("units", rgr1[k].gmt1.units));
	    	mapVar.get(rgr1[k].gmt1.variable).addAttribute(new Attribute("missing_value", -9999));
	    	mapVar.get(rgr1[k].gmt1.variable).addAttribute(new Attribute("long_name", rgr1[k].gmt1.long_name));
	    	mapVar.get(rgr1[k].gmt1.variable).addAttribute(new Attribute("cell_methods", rgr1[k].gmt1.cell_methods));
		}
	    
	    //creating the file
	    try{
			cdf1.create();
		}catch(Exception e) {
			e.printStackTrace();
		}
	    
	    //writing latitude and latitude bounds dimension
	    rgd1 = ArrayUtils.toPrimitive(rgr1[0].axsLat.keySet().toArray(new Double[rgr1[0].axsLat.size()]));
	    Arrays.sort(rgd1);
	    rgd2 = new double[rgd1.length][2];
	    for(int i=0;i<rgd1.length;i++){
	    	rgd2[i][0] = Math.round(rgd1[i]*10000000.)/10000000.;
	    	rgd2[i][1] = Math.round((rgd1[i]+rgr1[0].dLatResolution)*10000000.)/10000000.;
	    	rgd1[i]=Math.round((rgd1[i]+rgr1[0].dLatResolution/2.)*10000000.)/10000000.;
	    }
	    cdf1.write(varLat, Array.factory(rgd1));
	    cdf1.write(varLatBounds, Array.factory(rgd2));
	    
	    //writing longitude and longitude bounds dimension
	    rgd1 = ArrayUtils.toPrimitive(rgr1[0].axsLon.keySet().toArray(new Double[rgr1[0].axsLon.size()]));
	    Arrays.sort(rgd1);
	    rgd2 = new double[rgd1.length][2];
	    for(int i=0;i<rgd1.length;i++){
	    	rgd2[i][0] = Math.round(rgd1[i]*10000000.)/10000000.;
	    	rgd2[i][1] = Math.round((rgd1[i]+rgr1[0].dLonResolution)*10000000.)/10000000.;
	    	rgd1[i]=Math.round((rgd1[i]+rgr1[0].dLonResolution/2.)*10000000.)/10000000.;
	    }
	    cdf1.write(varLon, Array.factory(rgd1));
	    cdf1.write(varLonBounds, Array.factory(rgd2));
	    
		//initializing time and vert index maps
		mapTimeIndex = new HashMap<LocalDate,Integer>();
		mapTimeBoundsIndex = new HashMap<LocalDate,Integer>();
		for(int i=0;i<lstTimes.size();i++){
			mapTimeIndex.put(lstTimes.get(i).ID, i);
			mapTimeBoundsIndex.put(lstTimes.get(i).rngAxisValues.lowerEndpoint(), i);
			mapTimeBoundsIndex.put(lstTimes.get(i).rngAxisValues.upperEndpoint(), i);
		}
		mapVertIndex = new HashMap<Double,Integer>();
		mapVertBoundsIndex = new HashMap<Double,Integer>();
		for(int i=0;i<lstVerts.size();i++){
			mapVertIndex.put(lstVerts.get(i).ID, i);
			mapVertBoundsIndex.put(lstVerts.get(i).rngAxisValues.lowerEndpoint(), i);
			mapVertBoundsIndex.put(lstVerts.get(i).rngAxisValues.upperEndpoint(), i);
		}
		
		//writing time and vert information
		if(hasTime()){this.writeTime();}
		if(hasVert()){this.writeVert();}
	}
	
	private boolean hasTime(){
		if(lstTimes.size()>0){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean hasVert(){
		if(lstVerts.size()>0){
			return true;
		}else{
			return false;
		}
	}
	
	private void loadTimesVerts(){
		
		lstTimes = new ArrayList<AxisElement<LocalDate>>();
		lstVerts = new ArrayList<AxisElement<Double>>();
		for(int i=0;i<rgr1.length;i++){
			for(AxisElement<LocalDate> axe1:rgr1[i].axsTime.values()){
				if(!lstTimes.contains(axe1) && !axe1.ID.equals(GeospatialRaster.NULL_TIME)){
					lstTimes.add(axe1);
				}
			}
			for(AxisElement<Double> axe1:rgr1[i].axsVert.values()){
				if(!lstVerts.contains(axe1) && axe1.ID!=GeospatialRaster.NULL_VERT){
					lstVerts.add(axe1);
				}
			}
		}
	}

	/**
	 * Writes a geospatial raster using set vert and time values.
	 */
	public void writeRaster(GeospatialRaster ras1, LocalDate tim1, double dVert) throws Exception{
		writeRaster(ras1,tim1,dVert,Double.NaN);
	}
	
	/**
	 * Writes a geospatial raster using set vert, time, and latitude values.
	 */
	public void writeRaster(GeospatialRaster ras1, LocalDate tim1, double dVert, double dLat) throws Exception{
		
		//ary4 = data being written
		//aryTime = time array
		//aryVert = vert array
		//ind1 = index
		//rgiOrigin = origin array
		//rgiTimeOrigin = time origin
		//rgiVertOrigin = cert origin
		//itr1 = raster iterator
		//itr2 = index iterator
		//iVertIndex = vert index
		//iTimeIndex = time index
		//aryClim = climatology array
		//rgiClimOrigin = climatology origin
		//axeTime = time axis element
		//axeVert = vert axis element
		//cel1 = current cell
		//iLatOrigin = origin to use for latitude
		//iLatSize = size of latitude axis
		//setLatKeys = set of latitude keys to iterate over
		
		Set<AxisElement<Double>> setLatKeys;
		AxisElement<LocalDate> axeTime;
		AxisElement<Double> axeVert;
		ArrayDouble.D4 ary4;
		ArrayDouble.D3 ary3;
		ArrayDouble.D2 ary2;	
		int rgiOrigin[];
		GeospatialRaster.LatLonIterator itr1;
		IndexIterator itr2;
		int iVertIndex = 0;
		int iTimeIndex = 0;
		GeospatialRasterCell cel1;	
		int iLatOrigin;
		int iLatSize;
		
		//loading cell vert and time
		axeTime = ras1.axsTime.getAxisElementContaining(tim1);
		axeVert = ras1.axsVert.getAxisElementContaining(dVert);
	
		//loading latitude origin and latitude size
		if(Double.isNaN(dLat)){
			iLatOrigin = 0;
			iLatSize = ras1.axsLat.size();
			setLatKeys = ras1.axsLat.getAxisElements();
		}else{
			iLatOrigin = ras1.axsLat.getAxisElementContaining(dLat).iID;
			iLatSize = 1;
			setLatKeys = new HashSet<AxisElement<Double>>();
			setLatKeys.add(ras1.axsLat.getAxisElementContaining(dLat));
		}
		
		if(ras1.hasVert() && ras1.hasTime()){
			
			//writing data
			iVertIndex = mapVertIndex.get(axeVert.ID);
			iTimeIndex=mapTimeIndex.get(axeTime.ID);
			ary4 = new ArrayDouble.D4(1, 1, iLatSize, ras1.axsLon.size());
			itr2 = ary4.getIndexIterator();
			while(itr2.hasNext()){
				itr2.next();
				itr2.setDoubleCurrent(-9999.);
			}
			rgiOrigin = new int[]{iTimeIndex,iVertIndex,iLatOrigin,0};
			itr1 = ras1.new LatLonIterator(tim1, dVert, setLatKeys);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				if(Double.isNaN(dLat)){	
					ary4.set(0, 0, cel1.axeLat.iID, cel1.axeLon.iID, ras1.get(cel1));
				}else{
					ary4.set(0, 0, 0, cel1.axeLon.iID, ras1.get(cel1));
				}
			}
			cdf1.write(mapVar.get(ras1.gmt1.variable), rgiOrigin, ary4);
		}else if(!ras1.hasVert() && ras1.hasTime()){
			
			//writing data
			iTimeIndex=mapTimeIndex.get(axeTime.ID);
			ary3 = new ArrayDouble.D3(1, iLatSize, ras1.axsLon.size());
			itr2 = ary3.getIndexIterator();
			while(itr2.hasNext()){
				itr2.next();
				itr2.setDoubleCurrent(-9999.);
			}
			rgiOrigin = new int[]{iTimeIndex,iLatOrigin,0};
			itr1 = ras1.new LatLonIterator(tim1, dVert, setLatKeys);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				if(Double.isNaN(dLat)){
					ary3.set(0, cel1.axeLat.iID, cel1.axeLon.iID, ras1.get(cel1));
				}else{
					ary3.set(0, 0, cel1.axeLon.iID, ras1.get(cel1));
				}
			}
			cdf1.write(mapVar.get(ras1.gmt1.variable), rgiOrigin, ary3);
		}else if(ras1.hasVert() && !ras1.hasTime()){
			
			//writing data
			iVertIndex = mapVertIndex.get(axeVert.ID);
			ary3 = new ArrayDouble.D3(1, iLatSize, ras1.axsLon.size());
			itr2 = ary3.getIndexIterator();
			while(itr2.hasNext()){
				itr2.next();
				itr2.setDoubleCurrent(-9999.);
			}
			rgiOrigin = new int[]{iVertIndex,iLatOrigin,0};
			itr1 = ras1.new LatLonIterator(tim1, dVert, setLatKeys);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				if(Double.isNaN(dLat)){
					ary3.set(0, cel1.axeLat.iID, cel1.axeLon.iID, ras1.get(cel1));	
				}else{
					ary3.set(0, 0, cel1.axeLon.iID, ras1.get(cel1));
				}
			}
			cdf1.write(mapVar.get(ras1.gmt1.variable), rgiOrigin, ary3);
		}else if(!ras1.hasVert() && !ras1.hasTime()){
			
			//writing data
			ary2 = new ArrayDouble.D2(iLatSize, ras1.axsLon.size());
			itr2 = ary2.getIndexIterator();
			while(itr2.hasNext()){
				itr2.next();
				itr2.setDoubleCurrent(-9999.);
			}
			rgiOrigin = new int[]{iLatOrigin,0};
			itr1 = ras1.new LatLonIterator(tim1, dVert, setLatKeys);
			while(itr1.hasNext()){
				cel1 = itr1.next();
				if(Double.isNaN(dLat)){
					ary2.set(cel1.axeLat.iID, cel1.axeLon.iID, ras1.get(cel1));
				}else{
					ary2.set(0, cel1.axeLon.iID, ras1.get(cel1));
				}
			}
			cdf1.write(mapVar.get(ras1.gmt1.variable), rgiOrigin, ary2);
		}	
	}

	/**
	 * Writes time and vert to raster.
	 */
	private void writeTime() throws Exception{
		
		//ary4 = data being written
		//aryTime = time array
		//aryVert = vert array
		//ind1 = index
		//rgiOrigin = origin array
		//rgiTimeOrigin = time origin
		//rgiVertOrigin = vert origin
		//itr1 = raster iterator
		//itr2 = index iterator
		//iVertIndex = vert index
		//iTimeIndex = time index
		//aryClim = climatology array
		//rgiBoundsOrigin = bounds climatology origin
		//axeTime = time axis element
		//axeVert = vert axis element
		//cel1 = current cell
		
		Array aryTime = null;
		ArrayDouble.D2 aryTimeBounds = null;
		int rgiTimeOrigin[] = null;
		int rgiBoundsOrigin[] = null;
		
		//writing time data
    	aryTime = Array.factory(DataType.INT, new int[]{lstTimes.size()});
    	for(int i=0;i<lstTimes.size();i++){
    		aryTime.setDouble(i, getInternalTime(lstTimes.get(i).ID));
    	}
    	rgiTimeOrigin = new int[]{0};	
    	cdf1.write(varTime, rgiTimeOrigin, aryTime);
    	
    	//writing time bounds data
    	rgiBoundsOrigin = new int[]{0,0};	
    	aryTimeBounds = new ArrayDouble.D2(lstTimes.size(), 2);
    	for(int i=0;i<lstTimes.size();i++){
    		aryTimeBounds.set(i, 0, getInternalTime(lstTimes.get(i).rngAxisValues.lowerEndpoint()));
    		aryTimeBounds.set(i, 1, getInternalTime(lstTimes.get(i).rngAxisValues.upperEndpoint()));
    	}
    	cdf1.write(varTimeBounds, rgiBoundsOrigin, aryTimeBounds);

	}
	
	/**
	 * Writes time and vert to raster.
	 */
	private void writeVert() throws Exception{
		
		//ary4 = data being written
		//aryTime = time array
		//aryVert = vert array
		//ind1 = index
		//rgiOrigin = origin array
		//rgiTimeOrigin = time origin
		//rgiVertOrigin = vert origin
		//itr1 = raster iterator
		//itr2 = index iterator
		//iVertIndex = vert index
		//iTimeIndex = time index
		//aryClim = climatology array
		//rgiBoundsOrigin = bounds climatology origin
		//axeTime = time axis element
		//axeVert = vert axis element
		//cel1 = current cell
		
		Array aryVert = null;
		ArrayDouble.D2 aryVertBounds = null;
		int rgiVertOrigin[] = null;
		int rgiBoundsOrigin[] = null;
		
		//writing vert data
		aryVert = Array.factory(DataType.INT, new int[]{lstVerts.size()});
		for(int i=0;i<lstVerts.size();i++){
			aryVert.setDouble(i, lstVerts.get(i).ID);
		}
		rgiVertOrigin = new int[]{0};	
		cdf1.write(varVert, rgiVertOrigin, aryVert);
		
		//writing vert bounds data
		rgiBoundsOrigin = new int[]{0,0};	
		aryVertBounds = new ArrayDouble.D2(lstVerts.size(), 2);
		for(int i=0;i<lstVerts.size();i++){
			aryVertBounds.set(i, 0, lstVerts.get(i).rngAxisValues.lowerEndpoint());
			aryVertBounds.set(i, 1, lstVerts.get(i).rngAxisValues.upperEndpoint());
		}
		cdf1.write(varVertBounds, rgiBoundsOrigin, aryVertBounds);
		
	}

	/**
	 * Closes writer
	 */
	public void close(){
		try{
			cdf1.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets internal time
	 * @param tim1 Time to find
	 * @return Days from start of current epoch
	 */
	private double getInternalTime(LocalDate tim1){
		return Days.daysBetween(new LocalDate(1970,1,1),tim1).getDays();
	}
}
