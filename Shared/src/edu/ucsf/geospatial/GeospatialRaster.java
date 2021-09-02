package edu.ucsf.geospatial;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.math3.util.Precision;
import org.joda.time.LocalDate;
import com.google.common.collect.Range;
import edu.ucsf.base.HashBasedTable4D;

/**
 * Class for implementing geospatial raster without temporal data.
 * @author jladau
 */

public class GeospatialRaster{
	
	/**Value for null time**/
	public static final LocalDate NULL_TIME=new LocalDate("9999-09-09");
	
	/**Value for null vertical**/
	public static final double NULL_VERT=Double.MAX_VALUE;
	
	/**Backing table: keys are latitude, longitude, time, vert indices; values are values in raster**/
	protected HashBasedTable4D<Integer,Integer,Integer,Integer> tblValue;
	
	/**Latitude range**/
	protected Range<Double> rngLat;
	
	/**Longitude range**/
	protected Range<Double> rngLon;
	
	/**Cell size in the latitude direction**/
	public double dLatResolution = Double.NaN;
	
	/**Cell size in the longitude direction**/
	public double dLonResolution = Double.NaN;
	
	/**Axes**/
	public Axis<Double> axsLat = null;
	public Axis<Double> axsLon = null;
	public TimeAxis axsTime = null;
	public Axis<Double> axsVert = null;
	
	/**Time counter**/
	private int iTimeID=0;
	
	/**Vert counter**/
	private int iVertID=0;
	
	/**Latitude counter**/
	private int iLatID=0;
	
	/**Longitude counter**/
	private int iLonID=0;
	
	/**Unique ID: for ensuring that iterator indices match**/
	private int iID;
	
	/**Metadata**/
	public GeospatialRasterMetadata gmt1;
	
	public GeospatialRaster(
			double dLatResolution, 
			double dLonResolution, 
			Range<Double> rngLat, 
			Range<Double> rngLon, 
			GeospatialRasterMetadata gmt1){
		
		initialize(dLatResolution, 
				dLonResolution, 
				rngLat, 
				rngLon, 
				gmt1);
	}
	
	public GeospatialRaster(
			double dLatResolution, 
			double dLonResolution, 
			Range<Double> rngLat, 
			Range<Double> rngLon,
			SphericalMultiPolygon plyMask,
			GeospatialRasterMetadata gmt1){
		
		if(plyMask==null){
			initialize(
					dLatResolution, 
					dLonResolution, 
					rngLat, 
					rngLon, 
					gmt1);
		}else{
			initialize(
					dLatResolution, 
					dLonResolution, 
					plyMask.latitudeRange().intersection(rngLat), 
					plyMask.longitudeRange().intersection(rngLon), 
					gmt1);
		}
	}
	
	protected GeospatialRaster(){
		
		//initializing backing table
		tblValue = new HashBasedTable4D<Integer,Integer,Integer,Integer>();
	}
	
	public String toString(){
		return gmt1.variable;
	}
	
	public static ArrayList<GeographicPointBounds> globalBoundsGrid(double dCellSize) throws Exception{
		
		//ras1 = geospatial raster
		//itr1 = iterator
		//lstOut = output
		//cel1 = current cell
		
		ArrayList<GeographicPointBounds> lstOut;
		GeospatialRasterCell cel1;
		LatLonIterator itr1;
		GeospatialRaster ras1;
		
		
		//loading list of bounds
		ras1 = new GeospatialRaster(
				dCellSize,
				dCellSize,
				Range.closed(-90., 90.),
				Range.closed(-180., 180.),
				new GeospatialRasterMetadata(null, null, null, null, null, null, null, null, null));
		ras1.addNullTime();
		ras1.addNullVert();
		itr1 = ras1.getLatLonIterator(GeospatialRaster.NULL_TIME, GeospatialRaster.NULL_VERT);
		lstOut = new ArrayList<GeographicPointBounds>((int) (360/dCellSize*180/dCellSize));
		while(itr1.hasNext()){
			cel1 = itr1.next();
			lstOut.add(new GeographicPointBounds(
					cel1.axeLat.rngAxisValues.lowerEndpoint(),
					cel1.axeLat.rngAxisValues.upperEndpoint(),
					cel1.axeLon.rngAxisValues.lowerEndpoint(),
					cel1.axeLon.rngAxisValues.upperEndpoint()));
		}
		return lstOut;
		
	}
	
	
	protected void initialize(
			double dLatResolution, 
			double dLonResolution, 
			Range<Double> rngLat, 
			Range<Double> rngLon,
			GeospatialRasterMetadata gmt1){
		
		//d1 = current lower bound
		//d2 = current upper bound
		
		double d1;
		double d2;
		
		//loading id
		iID = (int) System.currentTimeMillis();
		
		//saving variables
		this.gmt1 = new GeospatialRasterMetadata(gmt1);
		this.dLatResolution = dLatResolution;
		this.dLonResolution = dLonResolution;
		this.rngLat = rngLat;
		this.rngLon = rngLon;
		
		//loading axes
		axsLat = new Axis<Double>();
		axsLon = new Axis<Double>();
		axsTime = new TimeAxis();
		axsVert = new Axis<Double>();
		for(int i=0;i<(rngLat.upperEndpoint()-rngLat.lowerEndpoint())/dLatResolution+5;i++){
			d1 = Precision.round(i*dLatResolution + rngLat.lowerEndpoint(),9);
			if(d1>rngLat.upperEndpoint()-dLatResolution){
				break;
			}
			d2 = Precision.round((i+1)*dLatResolution + rngLat.lowerEndpoint(),9);
			try {
				this.addLat(d1, d2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for(int i=0;i<(rngLon.upperEndpoint()-rngLon.lowerEndpoint())/dLonResolution+5;i++){
			d1 = Precision.round(i*dLonResolution + rngLon.lowerEndpoint(),9);
			if(d1>rngLon.upperEndpoint()-dLonResolution){
				break;
			}
			d2 = Precision.round((i+1)*dLonResolution + rngLon.lowerEndpoint(),9);
			try{
				this.addLon(d1, d2);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//initializing backing table
		tblValue = new HashBasedTable4D<Integer,Integer,Integer,Integer>();
	}

	protected void addLat(double dLatStart, double dLatEnd) throws Exception{
		if(axsLat==null){axsLat=new Axis<Double>();}
		if(axsLat.containsKey(dLatStart)){
			throw new Exception("Axis key already exists.");
		}
		axsLat.put(dLatStart, new AxisElement<Double>(Range.closed(dLatStart,dLatEnd),iLatID,Precision.round((dLatStart+dLatEnd)/2.,9)));
		iLatID++;
	}
	
	protected void addLon(double dLonStart, double dLonEnd) throws Exception{
		if(axsLon==null){axsLon=new Axis<Double>();}
		if(axsLon.containsKey(dLonStart)){
			throw new Exception("Axis key already exists.");
		}
		axsLon.put(dLonStart, new AxisElement<Double>(Range.closed(dLonStart,dLonEnd),iLonID,Precision.round((dLonStart+dLonEnd)/2.,9)));
		iLonID++;
	}
	
	public Range<Double> getLatRange(){
		return rngLat;
	}
	
	public Range<Double> getLonRange(){
		return rngLon;
	}
	
	public void addTime(LocalDate timStart, LocalDate timEnd) throws Exception{
		if(axsTime==null){axsTime=new TimeAxis();}
		if(axsTime.containsKey(timStart)){
			throw new Exception("Axis key already exists.");
		}
		axsTime.put(timStart, new AxisElement<LocalDate>(Range.closed(timStart,timEnd),iTimeID, timEnd));
		iTimeID++;
	}
	
	public void addNullTime() throws Exception{
		if(axsTime==null){axsTime=new TimeAxis();}
		if(axsTime.containsKey(NULL_TIME)){
			throw new Exception("Axis key already exists.");
		}
		axsTime.put(NULL_TIME, new AxisElement<LocalDate>(Range.closed(NULL_TIME,NULL_TIME),iTimeID, NULL_TIME));
		iTimeID++;
	}
	
	public void addVert(double dVertStart, double dVertEnd) throws Exception{
		if(dVertStart==NULL_VERT){addNullVert(); return;}
		if(axsVert==null){axsVert=new Axis<Double>();}
		if(axsVert.containsKey(dVertStart)){
			throw new Exception("Axis key already exists.");
		}
		axsVert.put(dVertStart, new AxisElement<Double>(Range.closed(dVertStart, dVertEnd),iVertID,Precision.round((dVertStart+dVertEnd)/2.,9)));
		iVertID++;
	}
	
	public void addVert(Range<Double> rngVert) throws Exception{
		this.addVert(rngVert.lowerEndpoint(),rngVert.upperEndpoint());
	}
	
	public void addTime(Range<LocalDate> rngTime) throws Exception{
		this.addTime(rngTime.lowerEndpoint(),rngTime.upperEndpoint());
	}
	
	public void addNullVert() throws Exception{
		if(axsVert==null){axsVert=new Axis<Double>();}
		if(axsVert.containsKey(NULL_VERT)){
			throw new Exception("Axis key already exists.");
		}
		axsVert.put(NULL_VERT, new AxisElement<Double>(Range.closed(NULL_VERT, NULL_VERT),iVertID,NULL_VERT));
		iVertID++;
	}
	
	public boolean hasVert(){
		if(!axsVert.containsKey(NULL_VERT)){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean hasTime(){
		if(!axsTime.containsKey(NULL_TIME)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Puts value in raster.
	 * @param dLat Latitude to write to.
	 * @param dLon Longitude to write to.
	 * @param tim1 Time to write to.
	 * @param dVert Vertical value.
	 * @param dValue Value.
	 */
	public void put(double dLat, double dLon, LocalDate tim1, double dVert, double dValue) throws Exception{
		
		//axeLat = latitude axis element
		//axeLon = longitude axis element
		//axeTime = time axis element
		//axeVert = vert axis element
		
		AxisElement<Double> axeLat;
		AxisElement<Double> axeLon;
		AxisElement<LocalDate> axeTime;
		AxisElement<Double> axeVert;
		
		//loading and checking axis elements containing values
		axeLat = axsLat.getAxisElementContaining(dLat);
		axeLon = axsLon.getAxisElementContaining(dLon);
		axeVert = axsVert.getAxisElementContaining(dVert);
		axeTime = axsTime.getAxisElementContaining(tim1);
		
		//writing value
		tblValue.put(axeLat.iID, axeLon.iID, axeTime.iID, axeVert.iID, dValue);
	}
	
	public void put(GeospatialRasterCell cel1, double dValue) throws Exception{
		if(iID==cel1.iRasterID){
			tblValue.put(cel1.axeLat.iID, cel1.axeLon.iID, cel1.axeTime.iID, cel1.axeVert.iID, dValue);
		}else{
			this.put(cel1.axeLat.ID, cel1.axeLon.ID, cel1.axeTime.ID, cel1.axeVert.ID, dValue);
		}
	}
	
	public double get(double dLat, double dLon, LocalDate tim1, double dVert){
		
		//axeLat = latitude axis element
		//axeLon = longitude axis element
		//axeTime = time axis element
		//axeVert = vert axis element
		
		AxisElement<Double> axeLat;
		AxisElement<Double> axeLon;
		AxisElement<LocalDate> axeTime;
		AxisElement<Double> axeVert;
		
		try{
		
			//loading axis elements containing values
			axeLat = axsLat.getAxisElementContaining(dLat);
			axeLon = axsLon.getAxisElementContaining(dLon);
			axeVert = axsVert.getAxisElementContaining(dVert);
			axeTime = axsTime.getAxisElementContaining(tim1);
		}catch(Exception e){		
			return Double.NaN;
		}
		
		//getting value
		return tblValue.get(axeLat.iID, axeLon.iID, axeTime.iID, axeVert.iID);
	
	}
	
	public GeospatialRasterCell cellContaining(double dLat, double dLon, LocalDate tim1, double dVert){
		
		//axeLat = latitude axis element
		//axeLon = longitude axis element
		//axeTime = time axis element
		//axeVert = vert axis element
		
		AxisElement<Double> axeLat;
		AxisElement<Double> axeLon;
		AxisElement<LocalDate> axeTime;
		AxisElement<Double> axeVert;
		
		try{
		
			//loading axis elements containing values
			axeLat = axsLat.getAxisElementContaining(dLat);
			axeLon = axsLon.getAxisElementContaining(dLon);
			axeVert = axsVert.getAxisElementContaining(dVert);
			axeTime = axsTime.getAxisElementContaining(tim1);
			
			//returning value
			return new GeospatialRasterCell(axeLat, axeLon, axeVert, axeTime);
		}catch(Exception e){		
			return null;
		}
	}
	

	
	

	public double get(GeospatialRasterCell rcl1){
		if(iID == rcl1.iRasterID){
			return tblValue.get(rcl1.axeLat.iID, rcl1.axeLon.iID, rcl1.axeTime.iID, rcl1.axeVert.iID);
		}else{
			return get(rcl1.axeLat.ID, rcl1.axeLon.ID, rcl1.axeTime.ID, rcl1.axeVert.ID);
		}
	}
	
	public Set<TimeVert> getAllTimeVertCombinations(){
		
		HashSet<TimeVert> set1;
		
		set1 = new HashSet<TimeVert>();
		for(LocalDate tim1:axsTime.keySet()){
			for(double dVert:axsVert.keySet()){
				set1.add(new TimeVert(tim1,dVert));
			}
		}
		return set1;
	}
	
	public boolean equals(Object obj1){
		
		//ras1 = object coerced to raster
		
		GeospatialRaster ras1;
		
		//checking if geospatial raster
		if(!(obj1 instanceof GeospatialRaster)){
			return false;
		}else{
			ras1 = (GeospatialRaster) obj1;
		}
		
		//checking fields
		if(!ras1.gmt1.equals(gmt1)){
			return false;
		}
		if(ras1.dLatResolution!=dLatResolution){
			return false;
		}
		if(ras1.dLonResolution!=dLonResolution){
			return false;
		}
		if(!ras1.axsLat.equals(axsLat)){
			return false;
		}
		if(!ras1.axsLon.equals(axsLon)){
			return false;
		}
		if(!ras1.axsTime.equals(axsTime)){
			return false;
		}
		if(!ras1.axsVert.equals(axsVert)){
			return false;
		}
		
		if(!ras1.tblValue.equals(tblValue)){
			return false;
		}
		
		return true;
	}
	
	
	public LatLonIterator getLatLonIterator(LocalDate tim1, double dVert) throws Exception{
		return new LatLonIterator(tim1, dVert);
	}
	
	/**
	 * Clears all entries
	 */
	public void clearAll(){
		initialize(
				dLatResolution, 
				dLonResolution, 
				rngLat, 
				rngLon, 
				gmt1);
	}
	
	/**Removes all entries for given time and vert but keeps vert and time entry even if empty**/
	public void clear(LocalDate tim1, double dVert){
		
		//itr1 = latitude/longitude iterator
		//rcl1 = current raster cell
		
		LatLonIterator itr1;
		GeospatialRasterCell rcl1;
		
		//checking if raster has specified time and vert
		if(!axsTime.hasAxisElementContaining(tim1)){
			return;
		}
		if(!axsVert.hasAxisElementContaining(dVert)){
			return;
		}
		
		//checking if raster should be cleared entirely
		if(axsTime.size()==1 && axsVert.size()==1){
			clearAll();
			return;
		}
		
		//looping through latitude and longitude combinations
		try {
			itr1 = getLatLonIterator(tim1,dVert);
			while(itr1.hasNext()){
				rcl1=itr1.next();
				if(tblValue.contains(rcl1.axeLat.iID, rcl1.axeLon.iID, rcl1.axeTime.iID, rcl1.axeVert.iID)){
					tblValue.remove(rcl1.axeLat.iID, rcl1.axeLon.iID, rcl1.axeTime.iID, rcl1.axeVert.iID);
				}
			}
		}catch(Exception e){
		}
		
		
	}
	
	/**Removes all entries for given time and vert and removes vert/time entries if they are empty**/
	public void remove(LocalDate tim1, double dVert){
		
		//clearing
		clear(tim1,dVert);
		
		//removing axis elements if necessary
		if(axsTime.size()==1){
			axsVert.remove(axsVert.floorKey(dVert));
		}
		if(axsVert.size()==1){
			axsTime.remove(axsTime.floorKey(tim1));
		}
	}
	
	//TODO write unit test for case of overlapping climatologies
	@SuppressWarnings("serial")
	public class TimeAxis extends Axis<LocalDate>{
		
		/**Flag for whether intervals overlap (for climatology data)**/
		private boolean bOverlap = false;
		
		public AxisElement<LocalDate> put(LocalDate timStart, AxisElement<LocalDate> axe1){
		
			//rng1 = range of overlap
			
			Range<LocalDate> rng1;
			
			//checking for overlap
			for(AxisElement<LocalDate> axe2:this.values()){
				if(axe1.rngAxisValues.isConnected(axe2.rngAxisValues)){
					rng1 = axe1.rngAxisValues.intersection(axe2.rngAxisValues);
					if(!rng1.isEmpty()){
						if(!rng1.lowerEndpoint().equals(rng1.upperEndpoint())){
							bOverlap=true;
							break;
						}
					}
				}
			}
			
			//adding to axis
			super.put(timStart, axe1);
			
			return axe1;
		}
		
		public AxisElement<LocalDate> getAxisElementContaining(LocalDate key) throws Exception{
			
			//tim1 = lookup time
			
			LocalDate tim1;
			
			if(bOverlap==false){
				return super.getAxisElementContaining(key);
			}else{
				if(hasAxisElementContaining(key)){
					tim1 = new LocalDate(this.firstKey().getYear(),key.getMonthOfYear(),key.getDayOfMonth());
					return super.getAxisElementContaining(tim1);
				}else{
					throw new Exception("Axis element containing key not found.");
				}
			}
		}
	}
	
	@SuppressWarnings({ "serial", "rawtypes" })
	public class Axis<T extends Comparable> extends TreeMap<T, AxisElement<T>>{
		
		public HashSet<AxisElement<T>> getAxisElements(){
			
			//set1 = output
			
			HashSet<AxisElement<T>> setOut;
			
			setOut = new HashSet<AxisElement<T>>();
			for(T t:this.keySet()){
				setOut.add(this.get(t));
			}
			return setOut;
		}
		
		public boolean hasAxisElementContaining(T key){
			
			//axe1 = axis element
			
			AxisElement<T> axe1;
			
			try{
				axe1 = get(floorKey(key));
				return axe1.contains(key);
			}catch(Exception e){
				return false;
			}
		}
		
		public AxisElement<T> getAxisElementContaining(T key) throws Exception{
			
			//axe1 = axis element
			
			AxisElement<T> axe1;
			
			
			try{
				axe1 = get(floorKey(key));	
			}catch(Exception e){
				throw new Exception("Axis element containing key not found (" + key + ").");
			}
			if(axe1.contains(key)){
				return axe1;
			}else{
				throw new Exception("Axis element containing key not found (" + key + ").");
			}
		}
		
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj1){
			
			//axs1 = object coerced to treemap
			
			Axis<T> axs1;
			
			if(!(obj1 instanceof TreeMap<?,?>)){
				return false;
			}else{
				
				axs1 = (Axis<T>) obj1;
				
				if(axs1.size()!=this.size()){
					return false;
				}
				for(T t:this.keySet()){
					if(!axs1.containsKey(t)){
						return false;
					}else{
						if(!this.get(t).equals(axs1.get(t))){
							return false;
						}
					}
				}
				return true;
			}
		}
	}
	
	public class TimeVert{
		
		public LocalDate tim1;
		public double dVert;
		
		public TimeVert(LocalDate tim1, double dVert){
			this.tim1 = tim1;
			this.dVert = dVert;
		}
	}
	
	
	
	public class GeospatialRasterCell{
		
		public AxisElement<Double> axeLat;
		public AxisElement<Double> axeLon;
		public AxisElement<Double> axeVert;
		public AxisElement<LocalDate> axeTime;
		private int iRasterID;
		
		public GeospatialRasterCell(AxisElement<Double> axeLat, AxisElement<Double> axeLon, AxisElement<Double> axeVert, AxisElement<LocalDate> axeTime){
			this.iRasterID = iID;
			this.axeLat=axeLat;
			this.axeLon=axeLon;
			this.axeVert=axeVert;
			this.axeTime=axeTime;
		}
		
		
		public ArrayList<GeospatialRasterCell> adjacentCells(){
			
			//rgd1 = center point
			
			//lst1 = output
			//axeLat = latitude axis element
			//axeLon = longitude axis element
			
			double rgd1[];
			ArrayList<GeospatialRasterCell> lst1;
			AxisElement<Double> axeLat;
			AxisElement<Double> axeLon;
			
			lst1 = new ArrayList<GeospatialRasterCell>(4);
			rgd1 = this.centerPoint();
			try{
				axeLat = axsLat.getAxisElementContaining(rgd1[0] + dLatResolution);
				axeLon = axsLon.getAxisElementContaining(rgd1[1]);
				lst1.add(new GeospatialRasterCell(axeLat, axeLon, this.axeVert, this.axeTime));
			}catch(Exception e){		
			}
			try{
				axeLat = axsLat.getAxisElementContaining(rgd1[0] - dLatResolution);
				axeLon = axsLon.getAxisElementContaining(rgd1[1]);
				lst1.add(new GeospatialRasterCell(axeLat, axeLon, this.axeVert, this.axeTime));
			}catch(Exception e){		
			}
			try{
				axeLat = axsLat.getAxisElementContaining(rgd1[0]);
				axeLon = axsLon.getAxisElementContaining(rgd1[1] + dLonResolution);
				lst1.add(new GeospatialRasterCell(axeLat, axeLon, this.axeVert, this.axeTime));
			}catch(Exception e){		
			}
			try{
				axeLat = axsLat.getAxisElementContaining(rgd1[0]);
				axeLon = axsLon.getAxisElementContaining(rgd1[1] - dLonResolution);
				lst1.add(new GeospatialRasterCell(axeLat, axeLon, this.axeVert, this.axeTime));
			}catch(Exception e){		
			}
			return lst1;
		}
		
		public String toString(){
			
			double rgd1[];
			
			rgd1 = this.centerPoint();
			return rgd1[0] + "," + rgd1[1];
			//return axeLat.toString() + ";" + axeLon.toString() + "," + axeVert.toString() + "," + axeTime.toString();
		}
		
		public double[] centerPoint(){
			return new double[]{0.5*(axeLat.rngAxisValues.lowerEndpoint()+axeLat.rngAxisValues.upperEndpoint()), 0.5*(axeLon.rngAxisValues.lowerEndpoint()+axeLon.rngAxisValues.upperEndpoint())};
		}
		
		public double area(){
			return toGeographicPointBounds().area();
		}
		
		public GeographicPointBounds toGeographicPointBounds(){
			return new GeographicPointBounds(axeLat.rngAxisValues.lowerEndpoint(), axeLat.rngAxisValues.upperEndpoint(), axeLon.rngAxisValues.lowerEndpoint(), axeLon.rngAxisValues.upperEndpoint());
		}
		
		public boolean equals(Object obj1){
			
			//cel1 = object coerced to raster cell
			
			GeospatialRasterCell cel1;
			
			if(obj1 == this){
				return true;
			}
			if(!(obj1 instanceof GeospatialRasterCell)){
				return false;
			}
			cel1 = (GeospatialRasterCell) obj1;
			if(cel1.axeLat == this.axeLat){
				if(cel1.axeLon == this.axeLon){
					if(cel1.axeTime == this.axeTime){
						if(cel1.axeVert == this.axeVert){
							return true;
						}
					}
				}
			}
			return false;
			
		}
		
		public int hashCode(){
			return 7*axeLat.hashCode() + 19*axeLon.hashCode() + 23*axeTime.hashCode() + 37*axeVert.hashCode();
		}
		
	}
	
	@SuppressWarnings("rawtypes") 
	public class AxisElement<T extends Comparable> implements Comparable{

		/**Range of values covered by element**/
		public Range<T> rngAxisValues;
		
		/**Unique integer ID for element**/
		public int iID;
		
		/**Type identifier for element**/
		public T ID;
		
		public AxisElement(Range<T> rngAxisValues, int iID, T ID){
			this.rngAxisValues = rngAxisValues;
			this.iID = iID;
			this.ID = ID;
		}
		
		public String toString(){
			return "(" + rngAxisValues.lowerEndpoint() + ", " + ID + ", " + rngAxisValues.upperEndpoint() + "): " + iID;
		}
		
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj1){
			
			//axe1 = object coerced to axis element
			
			AxisElement<T> axe1;
			
			if(!(obj1 instanceof AxisElement)){
				return false;
			}else{
				axe1 = (AxisElement<T>) obj1;
				if(!axe1.rngAxisValues.equals(rngAxisValues)){
					return false;
				}
				if(axe1.iID!=iID){
					return false;
				}
				if(!axe1.ID.equals(ID)){
					return false;
				}
				return true;
			}
		}
		
		public int hashCode(){
			return rngAxisValues.hashCode() + 7*iID;
		}
		
		public boolean contains(T value){
			return rngAxisValues.contains(value);
		}
		
		@SuppressWarnings("unchecked")
		public int compareTo(Object obj1){
			
			//axe1 = object coerced to axis element
			
			AxisElement<T> axe1;
			
			if(!(obj1 instanceof AxisElement)){
				return -1;
			}else{
				axe1 = (AxisElement<T>) obj1;
				return rngAxisValues.lowerEndpoint().compareTo(axe1.rngAxisValues.lowerEndpoint());	
			}
		}
		
	}
	
	public class LatLonIterator implements Iterator<GeospatialRasterCell>{
		
		/**Time axis element**/
		private AxisElement<LocalDate> axeTime;
		
		/**Vert axis element**/
		private AxisElement<Double> axeVert;
		
		/**Latitude keys**/
		private Set<AxisElement<Double>> setLatKeys;
		
		/**Longitude keys**/
		private Set<AxisElement<Double>> setLonKeys;
		
		/**Latitude iterator**/
		private Iterator<AxisElement<Double>> itrLat;
		
		/**Longitude iterator**/
		private Iterator<AxisElement<Double>> itrLon;
		
		/**Current longitude**/
		private AxisElement<Double> axeLon;
		
		/**Current latitude**/
		private AxisElement<Double> axeLat;
		
		/**Time**/
		private LocalDate tim1;
		
		/**Vert**/
		private double dVert;
		
		/**
		 * Constructor.
		*/
		public LatLonIterator(LocalDate tim1, double dVert) throws Exception{
			this.dVert = dVert;
			this.tim1 = tim1;
			axeVert = axsVert.getAxisElementContaining(dVert);
			axeTime = axsTime.getAxisElementContaining(tim1);
			setLatKeys = axsLat.getAxisElements();
			setLonKeys = axsLon.getAxisElements();
			itrLat = setLatKeys.iterator();
			itrLon = setLonKeys.iterator();
			axeLon = itrLon.next();
		}
		
		/**
		 * Constructor.
		*/
		public LatLonIterator(LocalDate tim1, double dVert, Set<AxisElement<Double>> setLatKeys) throws Exception{
			this.dVert = dVert;
			this.tim1 = tim1;
			axeVert = axsVert.getAxisElementContaining(dVert);
			axeTime = axsTime.getAxisElementContaining(tim1);
			this.setLatKeys = setLatKeys;
			setLonKeys = axsLon.getAxisElements();
			itrLat = setLatKeys.iterator();
			itrLon = setLonKeys.iterator();
			axeLon = itrLon.next();
		}
		
		
		@Override
		public boolean hasNext() {
			if(itrLon.hasNext() || itrLat.hasNext()){
				return true;
			}else{
				return false;
			}
		}
	
		@Override
		public GeospatialRasterCell next(){
			if(!itrLat.hasNext()){
				itrLat = setLatKeys.iterator();
				axeLon = itrLon.next(); 
			}
			axeLat = itrLat.next();
			return new GeospatialRasterCell(axeLat,axeLon,axeVert,axeTime);
		}
	
		@Override
		public void remove(){
			if(tblValue.contains(axeLat.iID, axeLon.iID, axsTime.get(tim1).iID, axsVert.get(dVert).iID)){
				tblValue.remove(axeLat.iID, axeLon.iID, axsTime.get(tim1).iID, axsVert.get(dVert).iID);
			}
		}
	}
}
