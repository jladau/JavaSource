package edu.ucsf.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.TreeMap;
import org.apache.commons.math3.util.Precision;
import org.joda.time.LocalDate;

import com.google.common.collect.Range;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;

/**
 * This class allows reading ESRI ascii raster files.
 * @author jladau
 */

//TODO write unit tests

public class ESRIAsciiReader extends GeospatialRaster{

	/**Number of columns**/
	private int iCols;
	
	/**Number of rows**/
	private int iRows;
	
	/**Lower left corner**/
	private double dXLLCorner=Double.NaN;
	
	/**Lower left corner**/
	private double dYLLCorner=Double.NaN;
	
	/**Upper right corner**/
	private double dXURCorner;
	
	/**Upper right corner**/
	private double dYURCorner;
	
	/**Cell size**/
	public double dCellSize;
	
	/**No data value**/
	private double dNoData;
	
	/**Latitude map from integer index to lower bound**/
	private TreeMap<Integer,Double> mapLat;
	
	/**Longitude map from integer index to lower bound**/
	private TreeMap<Integer,Double> mapLon;
	
	/**File reader**/
	private BufferedReader bfr1;
	
	/**Current latitude**/
	private double dLat;
	
	/**Current row**/
	private int iRow=-1;
	
	public ESRIAsciiReader(String sAsciiPath, Range<LocalDate> rngTime, GeospatialRasterMetadata gmt1) throws Exception{
		
		//rgs1 = current line in split format
		//dLon = longitude
		//dXLLCenter = lower left center
		//dYLLCenter = lower left center
		
		double dXLLCenter=Double.NaN; 
		double dYLLCenter=Double.NaN;
		double dLon;
		String rgs1[];
		
		bfr1 = new BufferedReader(new FileReader(sAsciiPath));
		
		//loading constants
		for(int i=0;i<6;i++){
			rgs1 = bfr1.readLine().trim().replaceAll(" +", " ").split(" ");
			if(rgs1[0].equals("ncols")){
				iCols = Integer.parseInt(rgs1[1]);
			}else if(rgs1[0].equals("nrows")){
				iRows = Integer.parseInt(rgs1[1]);
			}else if(rgs1[0].equals("xllcorner")){
				dXLLCorner = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("yllcorner")){
				dYLLCorner = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("cellsize")){
				dCellSize = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("NODATA_value")){
				dNoData = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("xllcenter")){
				dXLLCenter = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("yllcenter")){
				dYLLCenter = Double.parseDouble(rgs1[1]);
			}
		}
		if(Double.isNaN(dXLLCorner)){
			dXLLCorner=Precision.round(dXLLCenter-dCellSize/2.,8);
			dYLLCorner=Precision.round(dYLLCenter-dCellSize/2.,8);	
		}
			
			
		dXURCorner = dXLLCorner + dCellSize*((double) iCols);
		dYURCorner = dYLLCorner + dCellSize*((double) iRows);
		
		//initializing
		super.initialize(
				dCellSize, 
				dCellSize, 
				Range.closed(dYLLCorner, dYURCorner), 
				Range.closed(dXLLCorner, dXURCorner), 
				gmt1);
		
		//loading index maps
		mapLat = new TreeMap<Integer,Double>();
		dLat = dYLLCorner + dCellSize/2.;
		for(int i=iRows-1;i>=0;i--){
			mapLat.put(i,Precision.round(dLat, 4));
			dLat+=dCellSize;
		}
		mapLon = new TreeMap<Integer,Double>();
		dLon = dXLLCorner + dCellSize/2.;
		for(int j=0;j<iCols;j++){
			mapLon.put(j,Precision.round(dLon, 4));
			dLon+=dCellSize;
		}
		
		//initializing null vert and time
		this.addTime(rngTime);
		this.addNullVert();
		
		//initializing latitude
		dLat = dYLLCorner + dCellSize/2.;
	}
	
	public void loadNextLine() throws Exception{
		
		//rgs1 = current line in split format
		//dLon = longitude
		//d1 = current value
		//axeLat = current latitude axis element
		//axeLon = current longitude axis element
		
		AxisElement<Double> axeLat;
		AxisElement<Double> axeLon;
		double dLon;
		String rgs1[];
		double d1;
		
		//removing previous line
		if(iRow>0){
			axeLat = axsLat.getAxisElementContaining(dLat);
			for(double d:mapLon.values()){
				axeLon = axsLon.getAxisElementContaining(d);
				if(tblValue.contains(axeLat.iID, axeLon.iID, 0, 0)){
					tblValue.remove(axeLat.iID, axeLon.iID, 0, 0);
				}
			}
		}
		
		//loading next row
		iRow++;
		dLat = mapLat.get(iRow);
		System.out.println("Reading data from " + dLat + " degrees N...");
		rgs1 = bfr1.readLine().trim().replaceAll(" +", " ").split(" ");
		for(int j=0;j<rgs1.length;j++){
			d1 = Double.parseDouble(rgs1[j]);
			dLon = mapLon.get(j);
			if(d1!=dNoData){
				this.put(dLat, dLon, axsTime.firstKey(), GeospatialRaster.NULL_VERT, d1);
			}
		}
	}
	
	public boolean hasNext(){
		if(iRow+1<iRows){
			return true;
		}else{
			return false;
		}
	}
	
	public double currentLatitude(){
		return dLat;
	}
	
	/*
	
	public ESRIAsciiReader(String sAsciiPath) throws Exception{
		
		//rgs1 = current line in split format
		//dLat = latitude
		//dLon = longitude
		//d1 = current value
		
		double dLat;
		double dLon;
		String rgs1[];
		double d1;
		
		bfr1 = new BufferedReader(new FileReader(sAsciiPath));
		
		//loading constants
		for(int i=0;i<6;i++){
			rgs1 = bfr1.readLine().trim().replaceAll(" +", " ").split(" ");
			if(rgs1[0].equals("ncols")){
				iCols = Integer.parseInt(rgs1[1]);
			}else if(rgs1[0].equals("nrows")){
				iRows = Integer.parseInt(rgs1[1]);
			}else if(rgs1[0].equals("xllcorner")){
				dXLLCorner = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("yllcorner")){
				dYLLCorner = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("cellsize")){
				dCellSize = Double.parseDouble(rgs1[1]);
			}else if(rgs1[0].equals("NODATA_value")){
				dNoData = Double.parseDouble(rgs1[1]);
			}
		}
		dXURCorner = dXLLCorner + dCellSize*((double) iCols);
		dYURCorner = dYLLCorner + dCellSize*((double) iRows);
		
		//initializing
		super.initialize(
				dCellSize, 
				dCellSize, 
				Range.closed(dYLLCorner, dYURCorner), 
				Range.closed(dXLLCorner, dXURCorner), 
				null);
		
		//loading index maps
		mapLat = new TreeMap<Integer,Double>();
		dLat = dYLLCorner + dCellSize/2.;
		for(int i=iRows-1;i>=0;i--){
			mapLat.put(i,Precision.round(dLat, 4));
			dLat+=dCellSize;
		}
		mapLon = new TreeMap<Integer,Double>();
		dLon = dXLLCorner + dCellSize/2.;
		for(int j=0;j<iCols;j++){
			mapLon.put(j,Precision.round(dLon, 4));
			dLon+=dCellSize;
		}
		
		//initializing null vert and time
		this.addNullTime();
		this.addNullVert();
		
		//loading data
		dLat = dYLLCorner + dCellSize/2.;
		for(int i=iRows-1;i>=0;i--){
			
			System.out.println("Reading data from " + dLat + " degrees N...");
			
			rgs1 = dat1.getString(i+6, 0).trim().replaceAll(" +", " ").split(" ");
			dLon = dXLLCorner + dCellSize/2.;
			for(int j=0;j<rgs1.length;j++){
				d1 = Double.parseDouble(rgs1[j]);
				if(d1!=dNoData){
					this.put(dLat, dLon, GeospatialRaster.NULL_TIME, GeospatialRaster.NULL_VERT, d1);
				}
				dLon+=dCellSize;
			}
			dLat+=dCellSize;
		}
	}
	
	*/
}
