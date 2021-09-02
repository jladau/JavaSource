package edu.ucsf.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.util.Precision;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import com.google.common.collect.Range;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;

/**
 * This class reads netcdf files
 * @author jladau
 */
public class NetcdfReader extends GeospatialRaster{

	/**NetCDF file for reading**/
	private NetcdfFile ncf1 = null;
	
	/**Internal time object**/
	private InternalTime itm1 = null;
	
	/**Variable being read**/
	private Variable var1 = null;
	
	/**Shape (for extracting one record at a time)**/
	private int rgiShape[];
	
	/**Null value**/
	public double dNullValue;
	
	/**Path**/
	public String sPath;
	
	/**String for identifying vert (typically "vert" or "depth")**/
	private String sVertString;
	
	public NetcdfReader(String sPath, String sVar){
		super();
		
		//initializing axes
		initialize(sPath,sVar);
	}
	
	public NetcdfReader(String sPath){
		this(sPath,null);
	}
	
	/**
	 * Gets list of plottable variables.
	 * @return List of variables names, excluding latitude, longitude, etc
	 */
	public ArrayList<String> getPlottableVars(){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>();
		for(String sVar:getVars()){
			if(!sVar.equals("lat") 
				&& !sVar.equals("lat_bnds")	
				&& !sVar.equals("lon")
				&& !sVar.equals("lon_bnds")
				&& !sVar.equals("time") 
				&& !sVar.equals("climatology_bounds")
				&& !sVar.equals("vert") 
				&& !sVar.equals("vert_bnds")
				&& !sVar.equals("depth") 
				&& !sVar.equals("depth_bnds")){
				lstOut.add(sVar);
			}
		}
		return lstOut;
	}
	
	/**
	 * Returns raster values at sampling locations
	 * @param bio1 Biom file
	 * @param mapTimesToUse Map between sample IDs and dates to use for each sample. Null if use sample dates in biom file.
	 * @param dVertToUse Elevation to use. NULL_VERT if use elevation value from biom file for given sample.
	 * @return Map linking sample IDs to values
	 */
	public HashMap<String,Double> get(BiomIO bio1, HashMap<String,LocalDate> mapTimesToUse, double dVertToUse) throws Exception{
		
		//mapOut = output
		//dLat = latitude
		//dLon = longitude
		//tim1 = time to use
		//dVert = current vert
		
		double dLat;
		double dLon;
		HashMap<String,Double> mapOut;
		LocalDate tim1 = null;
		double dVert = Double.NaN;
		
		mapOut = new HashMap<String,Double>(bio1.axsSample.size());
		for(int i=0;i<bio1.axsSample.size();i++){
			
			//loading latitude and longitude
			dLat = Double.parseDouble(bio1.axsSample.getMetadata(i).get("latitude"));
			dLon = Double.parseDouble(bio1.axsSample.getMetadata(i).get("longitude"));
			
			//loading time
			if(this.hasTime()){
				if(mapTimesToUse!=null){
					tim1 = mapTimesToUse.get(bio1.axsSample.getID(i));
				}else{
					tim1 = new LocalDate(bio1.axsSample.getMetadata(i).get("datetime").substring(0,10));
				}
			}else{
				tim1 = NULL_TIME;
			}
			
			//loading vert
			if(this.hasVert()){
				if(dVertToUse==NULL_VERT){
					dVert = Double.parseDouble(bio1.axsSample.getMetadata(i).get("elevation"));
				}else{
					dVert = dVertToUse;
				}
			}else{
				dVert = NULL_VERT;
			}
			
			//saving value
			try{
				mapOut.put(bio1.axsSample.getID(i), readValue(dLat, dLon, tim1, dVert));	
			}catch(Exception e){
				mapOut.put(bio1.axsSample.getID(i), Double.NaN);	
			}
		}	
		
		return mapOut;
	}
	
	/**
	 * Loads grid
	 */
	public void loadGrid(LocalDate tim1, double dVert) throws Exception{
		
		//rgd1 = array of values
		//iRow = current row
		//iCol = current column
		//itrLat = latitude iterator
		//itrLon = longitude iterator
		//dLat = latitude
		//dLon = longitude
		
		double dLat;
		double dLon;
		Iterator<Double> itrLat;
		Iterator<Double> itrLon;
		double rgd1[][];
		int iRow=0;
		int iCol=0;
		
		rgd1 = loadValues(tim1, dVert);
		itrLat = axsLat.keySet().iterator();
		while(itrLat.hasNext()){
			dLat = itrLat.next();
			itrLon = axsLon.keySet().iterator();
			iCol=0;
			while(itrLon.hasNext()){
				dLon = itrLon.next();
				if(Math.abs(rgd1[iRow][iCol]-this.dNullValue)<0.000001){
					put(dLat,dLon,tim1,dVert,Double.NaN);
				}else{
					put(dLat,dLon,tim1,dVert,rgd1[iRow][iCol]);	
				}
				iCol++;
			}
			iRow++;
		}
	}
	
	/**
	 * Loads all grids for all times and verts
	 */
	//TODO write unit test
	public void loadGridAllVertsTimes() throws Exception{
		for(LocalDate tim1:axsTime.keySet()){
			for(double dVert:axsVert.keySet()){
				loadGrid(tim1,dVert);
			}
		}
	}

	/**
	 * Reads values of current variable for given time and vert setting
	 * @return Value at given arguments
	 */
	private double[][] loadValues(LocalDate tim1, double dVert) throws Exception{
		
		//bClose = flag for whether to close file
		//i2 = origin position
		//rgiOrigin = lookup array
		//ary1 = current data array
		//rgdOut = output
		//iCols = number of columns
		//iRows = number of rows
		//rgdData = data array
		//rgf1 = output in float form
		
		int i1; int i2; int iRows; int iCols;
		int rgiOrigin[] = null;
		double rgdOut[][] = null;
		float rgf1[][];
		
		//initializing shape
		for(int i=0;i<rgiShape.length;i++){
			rgiShape[i]=1;
		}
		
		//loading arguments
		if(hasVert() && hasTime()){rgiOrigin = new int[4];}
		else if(!hasVert() && hasTime()){rgiOrigin = new int[3];}
		else if(hasVert() && !hasTime()){rgiOrigin = new int[3];}
		else if(!hasVert() && !hasTime()){rgiOrigin = new int[2];}
		
		//loading latitude
		i1 = var1.findDimensionIndex("lat");
		rgiOrigin[i1] = 0;
		iRows = axsLat.size();
		rgiShape[i1]=iRows;
		
		//loading longitude
		i1 = var1.findDimensionIndex("lon");
		rgiOrigin[i1] = 0;
		iCols = axsLon.size();
		rgiShape[i1]=iCols;
		
		if(hasVert()){
			
			//loading vert
			i1 = var1.findDimensionIndex(sVertString);
			if(i1>=0){
				i2 = axsVert.getAxisElementContaining(dVert).iID;
				rgiOrigin[i1] = i2;
			}
		}
		
		if(hasTime()){	
		
			//loading time
			i1 = var1.findDimensionIndex("time");
			if(i1>=0){
				i2 = axsTime.getAxisElementContaining(tim1).iID;
				rgiOrigin[i1] = i2;
			}
		}
		
		//looking up value and loading result in array format
		try{
			rgdOut = (double[][]) var1.read(rgiOrigin, rgiShape).reduce().copyToNDJavaArray();
		}catch(Exception e){
			rgf1 = (float[][]) var1.read(rgiOrigin, rgiShape).reduce().copyToNDJavaArray();
			rgdOut = new double[rgf1.length][rgf1[0].length];
			for(int i=0;i<rgf1.length;i++){
				for(int j=0;j<rgf1[0].length;j++){
					rgdOut[i][j] = Double.parseDouble(Float.toString(rgf1[i][j]));
				}
			}
		}
		
		//outputting result
		return rgdOut;
	}
	
	public double readValue(GeospatialRasterCell cel1) throws Exception{
		return readValue(cel1.axeLat.ID,cel1.axeLon.ID,cel1.axeTime.ID,cel1.axeVert.ID);
	}
	
	public double readValue(double dLat, double dLon, LocalDate tim1, double dVert) throws Exception{
		
		//bClose = flag for whether to close file
		//i2 = lookup index of current argument
		//rgiOrigin = lookup array
		//ary1 = current data array
		//dOut = output value
		
		int i1; int i2;
		int rgiOrigin[] = null;
		Array ary1 = null;
		double dOut;
		
		//loading arguments
		if(hasVert() && hasTime()){rgiOrigin = new int[4];}
		else if(!hasVert() && hasTime()){rgiOrigin = new int[3];}
		else if(hasVert() && !hasTime()){rgiOrigin = new int[3];}
		else if(!hasVert() && !hasTime()){rgiOrigin = new int[2];}
		
		//loading latitude
		i1 = var1.findDimensionIndex("lat");
		i2 = axsLat.getAxisElementContaining(dLat).iID;
		rgiOrigin[i1] = i2;
		
		//loading longitude
		i1 = var1.findDimensionIndex("lon");
		i2 = axsLon.getAxisElementContaining(dLon).iID;
		rgiOrigin[i1] = i2;
		
		if(hasVert()){
			
			//loading vert
			i1 = var1.findDimensionIndex(sVertString);
			if(i1>=0){
				
				//*******************
				//System.out.println(dVert);
				//System.out.println(this.sPath);
				//*******************
				
				i2 = axsVert.getAxisElementContaining(dVert).iID;
				rgiOrigin[i1] = i2;
			}
		}
		
		if(hasTime()){	
		
			//loading time
			i1 = var1.findDimensionIndex("time");
			if(i1>=0){
				i2 = axsTime.getAxisElementContaining(tim1).iID;
				rgiOrigin[i1] = i2;
			}
		}
		
		//loading shape
		for(int i=0;i<rgiShape.length;i++){
			rgiShape[i]=1;
		}
		
		//looking up value and loading result in array format
		ary1 = var1.read(rgiOrigin, rgiShape).reduce();
	
		//outputting result
		dOut = ary1.getDouble(0);
		if(Math.abs((dOut-dNullValue)/dNullValue) < 0.000001){
			return Double.NaN;
		}else{
			return dOut;
		}
	}

	
	/**
	 * Gets a list of variables in the NetCDF file
	 * @return A string array giving the list of variables.
	 */
	private String[] getVars(){
		
		//rgs1 = output
		//lst1 = list of variables
		//itr1 = list iterator
		//i1 = number of variables
		//rgs1 = output
		//var2 = current variable
		
		Variable var2;
		int i=0;
		List<Variable> lst1;
		Iterator<Variable> itr1;
		int i1=0;
		String rgs1[];
		
		//loading list of variables
		lst1 = this.ncf1.getVariables();
		
		//looping through variables
		for(itr1 = lst1.iterator(); itr1.hasNext();){
			itr1.next();
			i1++;
		}
		
		//outputting results
		rgs1 = new String[i1];
		for(itr1 = lst1.iterator(); itr1.hasNext();){
			var2 = (Variable) itr1.next();
			rgs1[i] = var2.getShortName();
			i++;
		}
		
		//outputting result
		return rgs1;	
	}

	
	/**
	 * Closes NetCDF file.
	 */
	public void close(){
		try{
		    this.ncf1.close();
		}catch(Exception e) {
		    e.printStackTrace();
		}
	}
	
	public String getTimeUnits(){
		try{	
			return itm1.sTimeUnits;
		}catch(Exception e){
			return null;
		}
	}
	
	private void initializeVertString(){
		for(String sVar:getVars()){
			if(sVar.equals("depth")){
				sVertString = "depth";
				return;
			}
			if(sVar.equals("vert")){
				sVertString = "vert";
				return;
			}
		}
	}
	
	/**
	 * Initializes reader
	 * @param sPath Path to NetCDF file
	 * @param sVar Variable; null for first variable
	 */
	private void initialize(String sPath, String sVar){
		
		//num1 = error value (not coerced)
		
		Number num1;
		
		//opening file
		try {
			
			//initializing reader and internal time object
			ncf1 = NetcdfFile.open(sPath);
			try{
				itm1 = new InternalTime(ncf1.findVariable("time").getUnitsString());
			}catch(Exception e){
			}
			
			//initializing vert string
			initializeVertString();
			
			//loading first variable if no variable specified
			if(sVar==null){
				sVar = this.getPlottableVars().get(0);
			}
			var1 = ncf1.findVariable(sVar);
			
			//loading axes
			for(String s:var1.getDimensionsString().split(" ")){
				try{
					loadAxis(s);
				}catch(Exception e){
					loadAxisNoBounds(s);
				}
			}
			if(axsVert==null){this.addNullVert();}
			if(axsTime==null){this.addNullTime();}
			
			//initializing rgiShape
			rgiShape = var1.getShape();
			
			//loading variable attibutes
			gmt1 = new GeospatialRasterMetadata(null,null,null,null,null,null,null,null,null);
			try{gmt1.units = var1.getUnitsString();}catch(Exception e){;}
			try{gmt1.variable = sVar;}catch(Exception e){;}
			try{gmt1.long_name = var1.findAttribute("long_name").getStringValue();}catch(Exception e){;}
			try{gmt1.cell_methods = var1.findAttribute("cell_methods").getStringValue();}catch(Exception e){;}
			for(String s1:gmt1.getGlobalAttributes().keySet()){
				try{gmt1.putGlobal(s1, ncf1.findGlobalAttribute(s1).getStringValue());}catch(Exception e){;}
			}
			
			//loading null value
			try{
				num1 = var1.findAttributeIgnoreCase("missing_value").getNumericValue();
			}catch(Exception e){
				try{
					num1 = var1.findAttributeIgnoreCase("_FillValue").getNumericValue();
				}catch(Exception e1){
					throw new Exception("Missing value not found.");
				}
			}		
			if(num1 instanceof Double){
				dNullValue=(Double) num1;
			}else if(num1 instanceof Integer){
				dNullValue=(double) ((Integer) num1);
			}else if(num1 instanceof Float){
				dNullValue=Double.parseDouble(Float.toString((Float) num1));
			}else if(num1 instanceof Short){
				dNullValue=Double.parseDouble(Short.toString((Short) num1));
			}else if(num1 instanceof Byte){
				dNullValue=Double.parseDouble(Byte.toString((Byte) num1));
			}else{
				throw new Exception("Missing value could not be parsed.");
			}
			
			//saving path
			this.sPath = sPath;
		}catch(Exception e) {
			e.printStackTrace();
	    }
	}
	
	private void loadAxis(String sVar) throws Exception{
		
		//varBnds = bounds variable
		//rgdBounds = bounds array
		//rgfBounds2 = bounds array in float format (rank 2)
		//rgfBounds1 = bounds array in float format (rank 1)
		//rgdBounds1 = bounds array in double format (rank 1)
		//sType = data type
		//iRank = rank
		//iRow = output row
		
		Variable varBnds = null;
		double[][] rgdBounds = null;
		float[][] rgfBounds2 = null;
		float[] rgfBounds1 = null;
		double[] rgdBounds1 = null;
		String sType;
		int iRank;
		int iRow;
		
		//loading bounds variable
		if(sVar.equals("time")){varBnds=ncf1.findVariable("climatology_bounds");}
		if(sVar.equals(sVertString)){varBnds=ncf1.findVariable(sVertString + "_bnds");}
		if(sVar.equals("lat")){varBnds=ncf1.findVariable("lat_bnds");}
		if(sVar.equals("lon")){varBnds=ncf1.findVariable("lon_bnds");}
		
		//loading data array
		sType = varBnds.read().reduce().getDataType().toString();
		iRank = varBnds.read().reduce().getRank();
		if(sType.equals("double") && iRank == 2){
			rgdBounds = (double[][]) varBnds.read().reduce().copyToNDJavaArray();
		}
		if(sType.equals("double") && iRank == 1){
			rgdBounds1 = (double[]) varBnds.read().reduce().copyToNDJavaArray();
			rgdBounds = new double[rgdBounds1.length/2][2];
			iRow = 0;
			for(int i=0;i<rgdBounds1.length;i+=2){
				rgdBounds[iRow][0]=rgdBounds1[i];
				rgdBounds[iRow][1]=rgdBounds1[i+1];
				iRow++;
			}
		}
		if(sType.equals("float") && iRank==2){
			rgfBounds2 = (float[][]) varBnds.read().reduce().copyToNDJavaArray();
			rgdBounds = new double[rgfBounds2.length][rgfBounds2[0].length];
			for(int i=0;i<rgfBounds2.length;i++){
				for(int j=0;j<rgfBounds2[0].length;j++){
					rgdBounds[i][j]=Precision.round(rgfBounds2[i][j],3);
				}
			}
		}
		if(sType.equals("float") && iRank == 1){
			rgfBounds1 = (float[]) varBnds.read().reduce().copyToNDJavaArray();
			rgdBounds = new double[rgfBounds1.length/2][2];
			iRow = 0;
			for(int i=0;i<rgfBounds1.length;i+=2){
				rgdBounds[iRow][0]=Precision.round(rgfBounds1[i],3);
				rgdBounds[iRow][1]=Precision.round(rgfBounds1[i+1],3);
				iRow++;
			}
		}
		
		//loading output
		if(sVar.equals("time")){

			for(int i=0;i<rgdBounds.length;i++){
				addTime(itm1.getExternalTime(rgdBounds[i][0]),itm1.getExternalTime(rgdBounds[i][1]));
			}
		}else{
			
			//looping through array
			for(int i=0;i<rgdBounds.length;i++){	
				
				rgdBounds[i][0]=Precision.round(rgdBounds[i][0],9);
				rgdBounds[i][1]=Precision.round(rgdBounds[i][1],9);
				
				if(sVar.equals(sVertString)){
					addVert(rgdBounds[i][0],rgdBounds[i][1]);
				}else if(sVar.equals("lat")){
					addLat(rgdBounds[i][0],rgdBounds[i][1]);
					
					//loading latitude resolution
					if(i==0){dLatResolution=Precision.round(rgdBounds[i][1]- rgdBounds[i][0],9);}
				
					//loading latitude range
					if(i==0){
						rngLat = Range.closed(rgdBounds[i][0], rgdBounds[i][1]);
					}else{
						rngLat = rngLat.span(Range.closed(rgdBounds[i][0], rgdBounds[i][1]));
					}
				
				}else if(sVar.equals("lon")){
					addLon(rgdBounds[i][0],rgdBounds[i][1]);
					
					//loading longitude resolution
					if(i==0){dLonResolution=Precision.round(rgdBounds[i][1]- rgdBounds[i][0],9);}
					
					//loading longitude range
					if(i==0){
						rngLon = Range.closed(rgdBounds[i][0], rgdBounds[i][1]);
					}else{
						rngLon = rngLon.span(Range.closed(rgdBounds[i][0], rgdBounds[i][1]));
					}
				}
			}
		}	
	}
	
	private void loadAxisNoBounds(String sVar) throws Exception{
		
		//var1 = current variable
		//rgf1 = data array
		//rgd1 = data array in double format
		//rgi1 = data array in integer format
		
		Variable var1;
		float rgf1[] = null;
		double rgd1[] = null;
		int rgi1[] = null;
		
		//loading variable (if present in file)
		var1 = ncf1.findVariable(sVar);
		
		//loading data array
		try {
			rgf1 = (float[]) var1.read().reduce().copyTo1DJavaArray();
		}catch(Exception e){
			try{
				rgd1 = (double[]) var1.read().reduce().copyTo1DJavaArray();
				rgf1 = new float[rgd1.length];
				for(int i=0;i<rgd1.length;i++){
					rgf1[i]= (float) rgd1[i];
				}
			}catch(Exception f){
				try{
					rgi1 = (int[]) var1.read().reduce().copyTo1DJavaArray();
					rgf1 = new float[rgi1.length];
					for(int i=0;i<rgi1.length;i++){
						rgf1[i]= (float) rgi1[i];
					}
				}catch(Exception g){
					f.printStackTrace();
				}
			};
		}
		
		//loading output
		if(sVar.equals("time")){
	
			for(int i=0;i<rgf1.length;i++){
				addTime(itm1.getExternalTime(rgf1[i]),itm1.getExternalTime(rgf1[i]));
			}
		}else{
			
			//looping through array
			for(int i=0;i<rgf1.length;i++){	
				
				rgf1[i]=Precision.round(rgf1[i],3);
				
				if(sVar.equals(sVertString)){
					addVert(rgf1[i],rgf1[i]);
				}else if(sVar.equals("lat")){
					if(i==0){dLatResolution=Precision.round(rgf1[i+1]- rgf1[i],9);}
					addLat(Precision.round(rgf1[i]-dLatResolution/2.,9),Precision.round(rgf1[i]+dLatResolution/2.,9));
					
					//loading latitude range
					if(i==0){
						rngLat = Range.closed(rgf1[i]-dLatResolution/2., rgf1[i]+dLatResolution/2.);
					}else{
						rngLat = rngLat.span(Range.closed(rgf1[i]-dLatResolution/2., rgf1[i]+dLatResolution/2.));
					}
				}else if(sVar.equals("lon")){
					if(i==0){dLonResolution=Precision.round(rgf1[i+1]- rgf1[i],9);}
					addLon(Precision.round(rgf1[i]-dLonResolution/2.,9),Precision.round(rgf1[i]+dLonResolution/2.,9));
					
					//loading longitude range
					if(i==0){
						rngLon = Range.closed(rgf1[i]-dLonResolution/2., rgf1[i]+dLonResolution/2.);
					}else{
						rngLon = rngLon.span(Range.closed(rgf1[i]-dLonResolution/2., rgf1[i]+dLonResolution/2.));
					}
				}
			}
		}	
	}

	/**
	 * class converts external time values (months or dates) to internal time values.  Also
	 * @author jladau
	 *
	 */
	private class InternalTime{
		
		//cal0 = start date for calendar
		//sTimeUnits = time units
		
		private DateTime dtm0 = null;
		public String sTimeUnits;
		
		/**
		 * Constructor
		 * @param sTimeUnits Time units.  Currently supported values are "months" and "days since YYYY-MM-DD".
		 */
		public InternalTime(String sTimeUnits){
			
			//loading time units
			try{
				if(sTimeUnits.startsWith("month") || sTimeUnits.startsWith("Month")){
					this.sTimeUnits="months";
				}else if(sTimeUnits.startsWith("days since ")){	
					this.sTimeUnits=sTimeUnits;
					this.initializeStartCalendar(sTimeUnits.replace("days since ","").substring(0, 10));
				}else{
					sTimeUnits=null;
					dtm0=null;
				}
			}catch(Exception e){
				sTimeUnits = null;
				dtm0=null;
			}
		}
		
		/**
		 * Gets external time string for give internal time value
		 * @param dInternalTime Internal time value
		 * @return String representing external time value
		 */
		public LocalDate getExternalTime(double dInternalTime){
			
			//dtmOut = output calendar
			
			DateTime dtmOut;
			
			try{
				if(sTimeUnits.equals("months")){
					return new LocalDate(9999,(int) dInternalTime,15);
				}else if(sTimeUnits.startsWith("days since ")){
					dtmOut = new DateTime(dtm0.year().get(),dtm0.monthOfYear().get(),dtm0.dayOfMonth().get(),0,0);
					dtmOut = dtmOut.plusDays((int) dInternalTime);
					return new LocalDate(dtmOut.year().get(),dtmOut.monthOfYear().get(),dtmOut.dayOfMonth().get());
				}else{
					return new LocalDate(9999,1,1);
				}
			}catch(Exception e){
				return new LocalDate(9999,1,1);
			}
		}
		
		
		/**
		 * Finds internal time value for given time
		 * @param tim1 Time to look up
		 * @return	Internal time value
		 */
		/*
		public double getInternalTime(LocalDate tim1){
		
			//checking units
			if(sTimeUnits.equals("months")){
				return tim1.monthOfYear().get();
			}else if(sTimeUnits.startsWith("days since")){
			
				//checking if initial calendar loaded
				if(dtm0==null){
					return -9999;
				}else{
					return Days.daysBetween(dtm0.toLocalDate(), tim1).getDays();
				}
			}else{
				return -9999;
			}
		}
		*/
		
		/**
		 * @param sStartDate Start date in string format
		 */
		private void initializeStartCalendar(String sStartDate){
			
			//rgsStartDate = start date in split format
			//iYear = year
			//iMonth = month
			//iDay = day
			
			String rgsStartDate[];
			int iYear; int iMonth; int iDay;
			
			rgsStartDate = sStartDate.split("-");
			iYear = Integer.parseInt(rgsStartDate[0]);
			iMonth = Integer.parseInt(rgsStartDate[1]);
			iDay = Integer.parseInt(rgsStartDate[2]);
			dtm0 = new DateTime(iYear,iMonth,iDay,0,0);
		}
	}
}