package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;

/**
 * Object for storing projection data
 * @author jladau
 */

public class ProjectionData {

	/**Array of rasters**/
	protected NetcdfReader rgn1[];
	
	/**Returns the index for a specified predictor**/
	protected HashMap<String,Integer> mapIndex;
	
	/**For a given raster and output date alias, returns the date to use for making projections**/
	protected HashBasedTable<String,String,LocalDate> tblProjectionDates;
	
	/**Projection vert**/
	protected double dProjectionVert;
	
	/**Training data**/
	protected TrainingData trn1;
	
	public ProjectionData(ProjectionData prd1){
		this.rgn1 = prd1.rgn1;
		this.mapIndex = prd1.mapIndex;
		this.tblProjectionDates = prd1.tblProjectionDates;
		this.dProjectionVert = prd1.dProjectionVert;
		this.trn1 = prd1.trn1;
	}
	
	public ProjectionData(TrainingData trn1, DataIO datRasterPaths, double dProjectionVert) throws Exception{
		 
		//i1 = counter
		//rgsAliases = current set of date aliases
		//rgsDates = dates to use
		
		int i1;
		String rgsAliases[];
		String rgsDates[];
		
		//initializing netcdf reader objects
		rgn1 = new NetcdfReader[trn1.getPredictors().size()];
		mapIndex = new HashMap<String,Integer>(trn1.getPredictors().size());
		i1=0;
		for(String s:trn1.getPredictors()){
			
			//loading rasters
			rgn1[i1] = new NetcdfReader(s.split(":")[0],s.split(":")[1]);
			mapIndex.put(s,i1);
			i1++;
		}
		
		//initializing projection dates
		if(datRasterPaths.hasHeader("PROJECTION_DATES") && datRasterPaths.hasHeader("PROJECTION_DATE_ALIASES")){
			tblProjectionDates = HashBasedTable.create();
			for(int i=1;i<datRasterPaths.iRows;i++){
				rgsAliases=datRasterPaths.getString(i, "PROJECTION_DATE_ALIASES").split(";");
				rgsDates=datRasterPaths.getString(i, "PROJECTION_DATES").split(";");
				for(int k=0;k<rgsDates.length;k++){
					tblProjectionDates.put(trn1.getPredictorName(i), rgsAliases[k], new LocalDate(rgsDates[k]));
				}		
			}
		}else{
			tblProjectionDates = null;
		}
		
		//loading projection vert
		this.dProjectionVert=dProjectionVert;
		
		//saving training data
		this.trn1 = trn1;
	}
	
	//*************************************
	//TODO needs to be updated
	//*************************************
	public ArrayList<String> getPrintableData(TrainingData trn1, BiomIO bio1) throws Exception{
		
		//lstOut = output
		//sbl1 = current line
		//lstSampleIDs = sample IDs
		//lstLat = latitudes
		//lstLon = longitudes
		//lstData = current predictors
		//timCurrent = time to use
		//dVertCurrent = vert to use
		
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		ArrayList<String> lstSampleIDs;
		ArrayList<Double> lstLat;
		ArrayList<Double> lstLon;
		ArrayList<Double> lstData;
		LocalDate timCurrent;
		double dVertCurrent;
		
		System.out.println("Warning: predictors in projection data output are not being transformed. Code needs to be updated.");
		
		//initializing samples lists
		lstSampleIDs = new ArrayList<String>();
		lstLat = new ArrayList<Double>();
		lstLon = new ArrayList<Double>();
		for(String s:bio1.axsSample.getIDs()){
			lstSampleIDs.add(s);
			lstLat.add(Double.parseDouble(bio1.axsSample.getMetadata(s).get("latitude")));
			lstLon.add(Double.parseDouble(bio1.axsSample.getMetadata(s).get("longitude")));
		}
		
		//writing output
		lstOut = new ArrayList<String>(tblProjectionDates.rowKeySet().size()+1);
		lstOut.add("RASTER,PROJECTION_DATA_DATE_ALIAS,PROJECTION_DATA_DATE," + Joiner.on(",").join(lstSampleIDs));
		for(String t:this.tblProjectionDates.rowKeySet()){
			for(String s:this.tblProjectionDates.columnKeySet()){
				sbl1 = new StringBuilder();
				sbl1.append(t + "," + s + "," + tblProjectionDates.get(t, s));
				lstData = new ArrayList<Double>();
				for(int i=0;i<lstSampleIDs.size();i++){
					dVertCurrent = this.getRaster(t).hasVert() ? dProjectionVert : GeospatialRaster.NULL_VERT;
					timCurrent = this.getRaster(t).hasTime() ? this.getProjectionDate(t, s) : GeospatialRaster.NULL_TIME;
					lstData.add(getRaster(t).readValue(lstLat.get(i),lstLon.get(i),timCurrent,dVertCurrent));
				}
				sbl1.append("," + Joiner.on(",").join(lstData));
				lstOut.add(sbl1.toString());
			}	
		}
		return lstOut;
	}
	
	public ProjectionDatum getProjectionData(ProjectionPoint ppt1) throws Exception{
		
		//pdm1 = values for prediction
		//timCurrent = time to use
		//dVertCurrent = vert to use
		//d1 = current value
		
		double d1;
		LocalDate timCurrent;
		double dVertCurrent;
		ProjectionDatum pdm1;
		
		pdm1 = new ProjectionDatum(getPredictors().size());
		
		for(String s:getPredictors()){
			dVertCurrent = getRaster(s).hasVert() ? ppt1.dVert : GeospatialRaster.NULL_VERT;
			timCurrent = getRaster(s).hasTime() ? getProjectionDate(s, ppt1.sDateAlias) : GeospatialRaster.NULL_TIME;	
			
			//************************************************
			//System.out.println(ppt1.dLat + "," + ppt1.dLon + "," + timCurrent + "," + dVertCurrent);
			//************************************************
			
			d1 = getRaster(s).readValue(ppt1.dLat, ppt1.dLon,timCurrent,dVertCurrent);
			
			//************************
			//System.out.println(s);
			//************************
			
			
			d1 = PredictorTransform.apply(d1, s.split(":")[2]);
			pdm1.put(s, d1);
		}
		return pdm1;
	}
	
	public HashSet<String> getProjectionDateAliases(){
		if(tblProjectionDates==null){
			return null;
		}else{
			return new HashSet<String>(tblProjectionDates.columnKeySet());
		}
	}
	
	public LocalDate getProjectionDate(String sPredictor, String sDateAlias){
		if(tblProjectionDates==null){
			return null;
		}else{
			return tblProjectionDates.get(sPredictor, sDateAlias);
		}
	}
	
	public double getProjectionVert(){
		return dProjectionVert;
	}
	
	public NetcdfReader[] getRasterArray(){
		return rgn1;
	}
	
	public NetcdfReader getRaster(String sPredictor){
		return rgn1[mapIndex.get(sPredictor)];
	}
	
	public Set<String> getPredictors(){
		return mapIndex.keySet();
	}
	
	public void close(){
		for(NetcdfReader ncr1:rgn1){
			ncr1.close();
		}
	}
	
	@SuppressWarnings("serial")
	public class ProjectionDatum extends HashMap<String,Double>{
		
		public ProjectionDatum(int iSize){
			super(iSize);
		}
	}
	
	public class ProjectionPoint{
	
		public double dLat;
		public double dLon;
		public String sDateAlias;
		public double dVert;
		public String sID;
		
		protected ProjectionPoint(){
		}
		
		public ProjectionPoint(double dLat, double dLon, String sDateAlias, double dVert, String sID){
			this.dLat = dLat;
			this.dLon = dLon;
			this.sDateAlias = sDateAlias;
			this.dVert = dVert;
			this.sID = sID;
		}
	}
	
	public ArrayList<ProjectionPoint> loadAllPossibleProjectionPoints(String sResponseVariable, BiomIO bio1){
		
		//lstOut = output
		//setDateAliases = set of date aliases
		//setSamples = set of samples
		
		ArrayList<ProjectionPoint> lstOut;
		HashSet<String> setDateAliases;
		HashSet<String> setSamples;
		
		setSamples = trn1.getNonNanSamples(sResponseVariable);
		setDateAliases = getProjectionDateAliases();
		lstOut = new ArrayList<ProjectionPoint>(setSamples.size()*setDateAliases.size());
		for(String sSample:setSamples){
			for(String sDateAlias:setDateAliases){
				lstOut.add(new ProjectionPoint(
						Double.parseDouble(bio1.axsSample.getMetadata(sSample).get("latitude")), 
						Double.parseDouble(bio1.axsSample.getMetadata(sSample).get("longitude")), 
						sDateAlias, 
						dProjectionVert,
						sSample));
			}
		}
		return lstOut;
	}
}