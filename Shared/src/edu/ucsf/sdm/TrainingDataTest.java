package edu.ucsf.sdm;

import static org.junit.Assert.*;

import java.util.HashSet;
import org.junit.Test;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Training data for SDM modeling object.
 * @author jladau
 */


public class TrainingDataTest {

	/**Training data object**/
	private TrainingData trn1;
	
	/**Samples**/
	private String rgsSamples[];
	
	/**Response variables**/
	private String rgsResponseVars[];
	
	/**Response values**/
	private double rgdResponses[][];
	
	public TrainingDataTest() throws Exception{
		initialize();
	}
	
	private void initialize() throws Exception{
		
		//bio1 = biom object
		//datRasterPaths = raster paths
		//rgs1 = data being loaded
		//datResponseVars
		//rgsDates = list of dates
		
		String rgsDates[][];
		BiomIO bio1;
		DataIO datRasterPaths;
		DataIO datResponseVars;
		String rgs1[][];
		
		rgsDates = new String[17][2];
		rgsDates[0] = new String[]{"SampleID","Date"};
		for(int i=1;i<17;i++){
			rgsDates[i] = new String[]{"sample" + (i) ,  "9999-01-01"};
		}
		
		bio1=new BiomIO("/home/jladau/Desktop/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom");
		
		rgs1 = new String[][]{
			{"RASTER_PATH","TRANSFORM","VARIABLE"},
			{"/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc","identity","null"}};
		
		datRasterPaths = new DataIO(rgs1);
		rgs1 = new String[][]{{"otu1"},{"otu2"}};
		datResponseVars = new DataIO(rgs1);
		trn1 = new TrainingData(bio1,datRasterPaths,datResponseVars,new DataIO(rgsDates),0,"logit");
		
		rgsSamples = new String[]{"sample1","sample2","sample3","sample4","sample5","sample6","sample7","sample8","sample9","sample10","sample11","sample12","sample13","sample14","sample15","sample16"};
		rgsResponseVars = new String[]{"otu1","otu2"};
		rgdResponses = new double[][]{
				{0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8},
				{0.25,0.5,0.75,0,0.25,0.5,0.75,0,0.25,0.5,0.75,0,0.25,0.5,0.75,0}};
	}
	
	@Test
	public void getDataTable_TableGotten_TableCorrect(){
		
		//tbl1 = table from training data object
		//tbl2 = correct table
		//d1 = value being loaded
		//rgd1 = environmental data being loaded
		
		HashBasedTable<String,String,Double> tbl1;
		HashBasedTable<String,String,Double> tbl2;
		double d1 = 0;
		double rgd1[];
		
		tbl1 = trn1.getDataTable();
		
		tbl2 = HashBasedTable.create();
		for(int i=0;i<rgsResponseVars.length;i++){
			for(int j=0;j<rgsSamples.length;j++){
				if(rgdResponses[i][j]==0){
					tbl2.put(rgsResponseVars[i], rgsSamples[j], Double.NaN);
				}else if(rgdResponses[i][j]==1){
					tbl2.put(rgsResponseVars[i], rgsSamples[j], Double.NaN);
				}else{
					d1 = rgdResponses[i][j];
					tbl2.put(rgsResponseVars[i], rgsSamples[j], Math.log(d1/(1-d1)));
				}
			}
		}
		rgd1 = new double[]{
				45.25,
				45.25,
				45.25,
				45.25,
				46.25,
				46.25,
				46.25,
				46.25,
				47.25,
				47.25,
				47.25,
				47.25,
				48.25,
				48.25,
				48.25,
				48.25};
		for(int j=0;j<rgsSamples.length;j++){
			tbl2.put("/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc:Latitude:identity:sample_specific_date", rgsSamples[j], rgd1[j]);
		}
		
		assertEquals(tbl1,tbl2);
		assertEquals(tbl2.rowKeySet().size(),tbl1.rowKeySet().size());
		assertEquals(tbl2.columnKeySet().size(),tbl1.columnKeySet().size());
		for(String s:tbl1.rowKeySet()){
			for(String t:tbl1.columnKeySet()){
				assertEquals(tbl1.get(s, t),tbl2.get(s, t),0.0000001);
			}
		}
	}
	
	@Test
	public void getPredictors_PredictorsGotten_PredictorsCorrect(){
	
		//set1 = set of predictors
		
		HashSet<String> set1;
		
		set1 = trn1.getPredictors();
		assertEquals(set1.size(),1);
		assertTrue(set1.contains("/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc:Latitude:identity:sample_specific_date"));
	}
	
	@Test
	public void getResponseVars_ResponseVarsGotten_ResponseVarsCorrect(){
		
		//set1 = set of response variables
		
		HashSet<String> set1;
		
		set1 = trn1.getResponseVars();
		
		assertEquals(set1.size(),rgsResponseVars.length);
		for(int i=0;i<rgsResponseVars.length;i++){
			assertTrue(set1.contains(rgsResponseVars[i]));
		}
	}
	
	@Test
	public void getResponseTransform_ResponseTransformGotten_ResponseTransformCorrect(){	
		for(String s:trn1.getResponseVars()){
			assertEquals(trn1.getResponseTransform(s),"logit");
		}	
	}
}
