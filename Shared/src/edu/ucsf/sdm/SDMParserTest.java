package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.google.common.base.Joiner;

import static org.junit.Assert.*;
import edu.ucsf.io.DataIO;

/**
 * Parses inputs for niche modeling.
 * @author jladau
 */


public class SDMParserTest {

	/**Models data**/
	private DataIO datModels;
	
	/**Row to use**/
	private int iRow;
	
	/**Projection interval**/
	private String sProjectionInterval;
	
	/**Correct array**/
	private String rgs2[][];
	
	public SDMParserTest(){
		initialize();
	}
	
	private void initialize(){
		
		//rgs1 = input data in string array format
		
		String rgs1[][];
		
		rgs1 = new String[][]{"START_DATE,END_DATE,DURATION,CLIMATOLOGY,NUMBER_PREDICTORS,CV_R2,PRESS,R2,R2_ADJUSTED,BEST_OVERALL_MODEL,RESPONSE_VAR,PREDICTORS,MODEL,TRAINING_DATA_DATE,TRAINING_DATA_VERT,BIOM_FILE_PATH".split(","),
				"1965-01-01,1965-12-31,1,6565,2,0.27081708071034427,0.02700931608435487,0.3306939205304048,0.3072094966893665,true,Shannon[log10],(Intercept);cru_ts3.23.1901.2014.cld.dat.climatologies.nc:cld_1965-01-01--1965-12-31;cru_ts3.23.1901.2014.pre.dat.climatologies.nc:pre_1965-01-01--1965-12-31,(Intercept):0.7583483183537502;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.cld.dat.climatologies.nc:cld_1965-01-01--1965-12-31:-0.006078708465378469;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.pre.dat.climatologies.nc:pre_1965-01-01--1965-12-31:5.424452741666359E-4,na,na,/netapp/home/jladau/Data/Microbial_Community_Samples/Soil.Tibet.Bacteria.Yu.biom".split(","),
				"1974-01-01,1974-12-31,1,7474,3,0.13814562936471497,79.62026494582148,0.23816119832191784,0.19734840537487774,true,k__Archaea;p__Crenarchaeota[logit],(Intercept);cru_ts3.23.1901.2014.dtr.dat.climatologies.nc:dtr_1974-01-01--1974-12-31;cru_ts3.23.1901.2014.pet.dat.climatologies.nc:pet_1974-01-01--1974-12-31;cru_ts3.23.1901.2014.frs.dat.climatologies.nc:frs_1974-01-01--1974-12-31,(Intercept):28.674517860307144;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.dtr.dat.climatologies.nc:dtr_1974-01-01--1974-12-31:-0.6817950272765693;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.pet.dat.climatologies.nc:pet_1974-01-01--1974-12-31:-5.925018199184736;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.frs.dat.climatologies.nc:frs_1974-01-01--1974-12-31:-0.47297623364074726,na,na,/netapp/home/jladau/Data/Microbial_Community_Samples/Soil.Tibet.Bacteria.Yu.biom".split(","),
				"1971-01-01,1971-12-31,1,7171,3,0.45620714921831074,180.07416643081424,0.538905760752062,0.514204283649494,true,k__Archaea;p__Euryarchaeota[logit],(Intercept);cru_ts3.23.1901.2014.dtr.dat.climatologies.nc:dtr_1971-01-01--1971-12-31;cru_ts3.23.1901.2014.pet.dat.climatologies.nc:pet_1971-01-01--1971-12-31;cru_ts3.23.1901.2014.cld.dat.climatologies.nc:cld_1971-01-01--1971-12-31,(Intercept):-18.06701765330346;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.dtr.dat.climatologies.nc:dtr_1971-01-01--1971-12-31:1.2573266749222172;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.pet.dat.climatologies.nc:pet_1971-01-01--1971-12-31:5.024307106762397;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.cld.dat.climatologies.nc:cld_1971-01-01--1971-12-31:-0.47331311949377053,na,na,/netapp/home/jladau/Data/Microbial_Community_Samples/Soil.Tibet.Bacteria.Yu.biom".split(","),
				"1989-01-01,1989-12-31,1,8989,4,0.4099111804452612,9.707479274503402,0.5279649057040452,0.4936350806643395,true,k__Bacteria;p__Acidobacteria[logit],(Intercept);cru_ts3.23.1901.2014.pre.dat.climatologies.nc:pre_1989-01-01--1989-12-31;cru_ts3.23.1901.2014.dtr.dat.climatologies.nc:dtr_1989-01-01--1989-12-31;cru_ts3.23.1901.2014.pet.dat.climatologies.nc:pet_1989-01-01--1989-12-31;cru_ts3.23.01.1901.2014.wet.dat.climatologies.nc:wet_1989-01-01--1989-12-31,(Intercept):5.206066296713323;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.pre.dat.climatologies.nc:pre_1989-01-01--1989-12-31:0.059067545190267016;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.dtr.dat.climatologies.nc:dtr_1989-01-01--1989-12-31:-0.18229322708857804;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.pet.dat.climatologies.nc:pet_1989-01-01--1989-12-31:-2.8243547036783494;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.01.1901.2014.wet.dat.climatologies.nc:wet_1989-01-01--1989-12-31:-0.14759407036709143,na,na,/netapp/home/jladau/Data/Microbial_Community_Samples/Soil.Tibet.Bacteria.Yu.biom".split(","),
				"1997-01-01,1997-12-31,1,9797,1,0.30837901648731914,23.6519333303711,0.3492644825672233,0.33804490468045134,true,k__Bacteria;p__Armatimonadetes[logit],(Intercept);cru_ts3.23.01.1901.2014.wet.dat.climatologies.nc:wet_1997-01-01--1997-12-31,(Intercept):-4.077651609547552;/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.01.1901.2014.wet.dat.climatologies.nc:wet_1997-01-01--1997-12-31:-0.19867185275204685,na,na,/netapp/home/jladau/Data/Microbial_Community_Samples/Soil.Tibet.Bacteria.Yu.biom".split(",")
				};
		datModels = new DataIO(rgs1);
		iRow = 2;
		sProjectionInterval="1999-07-12--2003-01-09";
		rgs2 = new String[][]{
				{"/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.dtr.dat.climatologies.nc","dtr_1974-01-01--1974-12-31"},
				{"/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.pet.dat.climatologies.nc","pet_1974-01-01--1974-12-31"},
				{"/netapp/home/jladau/Documents/Research/Data/Rasters/CRU_Historical_Climate/cru_ts3.23.1901.2014.frs.dat.climatologies.nc","frs_1974-01-01--1974-12-31"}};
	}
	
	
	@Test
	public void loadClimatologyProjectionIntervals(){
		
		//lst1 = output
		//rgs2 = correct output
		
		ArrayList<String> lst1;
		String rgs2[];
		
		lst1 = SDMParser.loadClimatologyProjectionIntervals(new String[]{"1955-12-31","1973-12-31","2010-12-31"}, 10, new String[]{"1950-01-01","2010-12-15"});
		rgs2 = new String[]{
				"1964-01-01--1973-12-31",
				"2001-01-01--2010-12-31"};
		for(int i=0;i<lst1.size();i++){
			assertEquals(lst1.get(i),rgs2[i]);
		}
	}
	
	@Test
	public void loadTrainingPredictors_PredictorsLoaded_LoadedCorrectly(){
		
		//rgs1 = training predictors
		//rgs2 = correct result
		
		String rgs1[][];
		
		rgs1 = SDMParser.loadTrainingPredictors(datModels, iRow);
		for(int i=0;i<rgs2.length;i++){
			for(int j=0;j<rgs2[0].length;j++){
				assertEquals(rgs1[i][j],rgs2[i][j]);
			}
		}
	}
	
	@Test
	public void loadClimatologyProjectionMap_MapLoaded_LoadedCorrectly(){
		
		//map1 = output map
		//map2 = correct map
		
		HashMap<String,String> map1;
		HashMap<String,String> map2;
		
		map1 = new HashMap<String,String>();
		for(int i=0;i<rgs2.length;i++){
			map1.put(Joiner.on(":").join(rgs2[i]), rgs2[i][0] + ":" + rgs2[i][1].replace("1974-01-01--1974-12-31","1999-07-12--2003-01-09"));
		}
		map2 = SDMParser.loadClimatologyProjectionMap(datModels, iRow, sProjectionInterval);
		assertEquals(map2.size(),map1.size());
		for(String s:map1.keySet()){
			assertEquals(map1.get(s),map2.get(s));
		}
	}
	
	public void getTrainingDate_TrainingDateGotten_DateCorrect(){
		assertEquals(SDMParser.getTrainingDate(datModels, iRow),"1974-01-01--1974-12-31");
	}
}
