package edu.ucsf.sdm;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Training data for SDM modeling object.
 * @author jladau
 */


public class TrainingData_DifferencesTest {

	/**Training data object**/
	private TrainingData trn1;
	
	/**Training data differences object: log odds**/
	private TrainingData_Differences trd1;
	
	/**Training data differences object: abs log odds**/
	private TrainingData_Differences trd2;
	
	/**Samples**/
	private String rgsSamples[];
	
	/**Response variables**/
	private String rgsResponseVars[];
	
	/**Response values**/
	private double rgdResponses[][];
	
	public TrainingData_DifferencesTest() throws Exception{
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
				
		trd1 = new TrainingData_Differences(trn1, "logoddsratio", bio1);
		trd2 = new TrainingData_Differences(trn1, "abslogoddsratio", bio1);
	}
	
	@Test
	public void getDataTable_Table1Gotten_TableCorrect(){
		
		//tbl1 = table from training data object
		//tbl2 = correct table
		//d1 = first value
		//d2 = second value
		//rgd1 = environmental data being loaded
		//sSample1 = first sample
		//sSample2 = second sample
		//s1 = output
		
		String s1;
		HashBasedTable<String,String,Double> tbl1;
		HashBasedTable<String,String,Double> tbl2;
		double d1;
		double d2;
		double rgd1[];
		String sSample1;
		String sSample2;
		
		tbl1 = trd1.getDataTable();
		
		tbl2 = HashBasedTable.create();
		for(int i=0;i<rgsResponseVars.length;i++){
			for(int j=1;j<rgsSamples.length;j++){
				sSample1 = rgsSamples[j];
				for(int k=0;k<j;k++){
					sSample2 = rgsSamples[k];
					if(sSample1.compareTo(sSample2)<0){
						s1 = sSample1 + "," + sSample2;
						d1 = rgdResponses[i][j];
						d2 = rgdResponses[i][k];
					}else{
						s1 = sSample2 + "," + sSample1;
						d2 = rgdResponses[i][j];
						d1 = rgdResponses[i][k];
					}
					if(d1==0 || d1==1 || d2==0 || d2==1){
						tbl2.put(rgsResponseVars[i], s1, Double.NaN);
					}else{
						tbl2.put(rgsResponseVars[i], s1, Math.log(d2/(1-d2)) - Math.log(d1/(1-d1)));
					}
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
		for(int j=1;j<rgsSamples.length;j++){
			sSample1 = rgsSamples[j];
			for(int k=0;k<j;k++){
				sSample2 = rgsSamples[k];
				if(sSample1.compareTo(sSample2)<0){
					tbl2.put(
							"/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc:Latitude:identity:sample_specific_date", 
							sSample1 + "," + sSample2, 
							rgd1[k] - rgd1[j]);
				}else{
					tbl2.put(
							"/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc:Latitude:identity:sample_specific_date", 
							sSample2 + "," + sSample1, 
							rgd1[j] - rgd1[k]);	
				}
			}
		}
		
		assertEquals(tbl2.rowKeySet().size(),tbl1.rowKeySet().size());
		assertEquals(tbl2.columnKeySet().size(),tbl1.columnKeySet().size());
		for(String s:tbl1.rowKeySet()){
			for(String t:tbl1.columnKeySet()){
				assertEquals(tbl1.get(s, t),tbl2.get(s, t),0.0000001);
			}
		}
	}
	
	@Test
	public void getDataTable_Table2Gotten_TableCorrect(){
		
		//tbl1 = table from training data object
		//tbl2 = correct table
		//d1 = first value
		//d2 = second value
		//rgd1 = environmental data being loaded
		//sSample1 = first sample
		//sSample2 = second sample
		//s1 = output
		
		String s1;
		HashBasedTable<String,String,Double> tbl1;
		HashBasedTable<String,String,Double> tbl2;
		double d1;
		double d2;
		double rgd1[];
		String sSample1;
		String sSample2;
		
		tbl1 = trd2.getDataTable();
		
		tbl2 = HashBasedTable.create();
		for(int i=0;i<rgsResponseVars.length;i++){
			for(int j=1;j<rgsSamples.length;j++){
				sSample1 = rgsSamples[j];
				for(int k=0;k<j;k++){
					sSample2 = rgsSamples[k];
					if(sSample1.compareTo(sSample2)<0){
						s1 = sSample1 + "," + sSample2;
						d1 = rgdResponses[i][j];
						d2 = rgdResponses[i][k];
					}else{
						s1 = sSample2 + "," + sSample1;
						d2 = rgdResponses[i][j];
						d1 = rgdResponses[i][k];
					}
					if(d1==0 || d1==1 || d2==0 || d2==1){
						tbl2.put(rgsResponseVars[i], s1, Double.NaN);
					}else{
						tbl2.put(rgsResponseVars[i], s1, Math.abs(Math.log(d2/(1-d2)) - Math.log(d1/(1-d1))));
					}
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
		for(int j=1;j<rgsSamples.length;j++){
			sSample1 = rgsSamples[j];
			for(int k=0;k<j;k++){
				sSample2 = rgsSamples[k];
				if(sSample1.compareTo(sSample2)<0){
					tbl2.put(
							"/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc:Latitude:identity:sample_specific_date", 
							sSample1 + "," + sSample2, 
							Math.abs(rgd1[k] - rgd1[j]));
				}else{
					tbl2.put(
							"/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc:Latitude:identity:sample_specific_date", 
							sSample2 + "," + sSample1, 
							Math.abs(rgd1[j] - rgd1[k]));	
				}
			}
		}
		
		assertEquals(tbl2.rowKeySet().size(),tbl1.rowKeySet().size());
		assertEquals(tbl2.columnKeySet().size(),tbl1.columnKeySet().size());
		for(String s:tbl1.rowKeySet()){
			for(String t:tbl1.columnKeySet()){
				assertEquals(tbl1.get(s, t),tbl2.get(s, t),0.0000001);
			}
		}
	}
}