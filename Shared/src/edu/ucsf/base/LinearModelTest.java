package edu.ucsf.base;


import java.util.HashMap;
import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.Test;
import com.google.common.collect.HashBasedTable;

/**
 * Fits linear model using apache math module.
 * @author jladau
 */

public class LinearModelTest{

	/**Linear model object**/
	private LinearModel lnm1;
	
	public LinearModelTest() throws Exception{
		initialize();
	}
	
	private void initialize() throws Exception{
		
		//rgs1 = data in string format
		//tbl1 = data table
		//set1 = set of predictor variables
		
		HashSet<String> set1;
		String rgs1[][];
		HashBasedTable<String,String,Double> tbl1;
		
		rgs1 = new String[][]{
				{"","pred1","pred2","pred3","resp1"},
				{"obs1","1","2","8","1"},
				{"obs2","2","2","5","3"},
				{"obs3","3","3","6","4"},
				{"obs4","4","4","9","9"}	
				};
		
		tbl1 = HashBasedTable.create();
		for(int i=1;i<rgs1.length;i++){
			for(int j=1;j<rgs1[0].length;j++){
				tbl1.put(rgs1[0][j],rgs1[i][0], Double.parseDouble(rgs1[i][j]));
			}
		}
		set1 = new HashSet<String>();
		set1.add("pred1");
		set1.add("pred2");
		set1.add("pred3");
		
		
		lnm1 = new LinearModel(tbl1, "resp1", set1);
		
	}
	
	@Test
	public void findPredictedValues_ValuesFound_ValuesCorrect(){
		
		//map1 = map of observed values
		//set1 = set of predictors
		
		HashSet<String> set1;
		HashMap<String,Double> map1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		map1 = lnm1.findPredictedValues();
		assertEquals(map1.get("obs1"),1,0.0000001);
		assertEquals(map1.get("obs2"),2.3333333,0.000001);
		assertEquals(map1.get("obs3"),5.3333333,0.000001);
		assertEquals(map1.get("obs4"),8.3333333,0.000001);
	}
	
	@Test
	public void resampleParametric_Resampled_ResamplingCorrect(){
		
		//set1 = set of predictors
		//tbl1 = table of resamples
		
		HashSet<String> set1;
		HashBasedTable<String,Integer,Double> tbl1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tbl1 = lnm1.resampleParametric(10);
		assertTrue(tbl1.rowKeySet().size()==4);
		assertTrue(tbl1.columnKeySet().size()==10);
	}
	
	@Test
	public void resampleNonparametric_Resampled_ResamplingCorrect(){
		
		//map1 = map of observed values
		//set1 = set of predictors
		//tbl1 = table of resamples
		//rgd1 = residuals
		
		HashSet<String> set1;
		HashMap<String,Double> map1;
		HashBasedTable<String,Integer,Double> tbl1;
		double rgd1[];
		
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel( set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tbl1 = lnm1.resampleNonparametric(10);
		map1 = lnm1.findPredictedValues();
		rgd1 = new double[]{0,0.666666666,-1.333333,0.666666666};
		assertTrue(tbl1.rowKeySet().size()==4);
		assertTrue(tbl1.columnKeySet().size()==10);
		for(String s:tbl1.rowKeySet()){
			for(Integer i:tbl1.columnKeySet()){
				assertTrue(
						Math.abs(tbl1.get(s, i)-map1.get(s)-rgd1[0])<0.000001 ||
						Math.abs(tbl1.get(s, i)-map1.get(s)-rgd1[1])<0.000001 ||
						Math.abs(tbl1.get(s, i)-map1.get(s)-rgd1[2])<0.000001 ||
						Math.abs(tbl1.get(s, i)-map1.get(s)-rgd1[3])<0.000001);
			}
		}
	}
	
	@Test
	public void checkVIF_VIFChecked_CheckFalse(){
		
		//set1 = set of predictors
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=3;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			assertFalse(lnm1.checkVIF(5,null));
			assertFalse(lnm1.checkVIF(126,null));
			assertTrue(lnm1.checkVIF(127,null));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void findLMG_LMGFound_LMGCorrect() throws Exception{
		
		//set1 = set of predictors
		//map1 = map of lmg values
		
		HashSet<String> set1;
		HashMap<String,Double> map1;
		
		
		//rgs1 = data in string format
		//tbl1 = data table
		
		String rgs1[][];
		HashBasedTable<String,String,Double> tbl1;
		
		rgs1 = new String[][]{
				{"","pred1","pred2","pred3","resp1"},
				{"obs1","1","2","8","1"},
				{"obs2","2","2","5","3"},
				{"obs3","3","3","6","4"},
				{"obs4","4","4","9","9"},
				{"obs5","3","11","0","12"}	
				};
		
		tbl1 = HashBasedTable.create();
		for(int i=1;i<rgs1.length;i++){
			for(int j=1;j<rgs1[0].length;j++){
				tbl1.put(rgs1[0][j],rgs1[i][0], Double.parseDouble(rgs1[i][j]));
			}
		}
		set1 = new HashSet<String>();
		for(int i=1;i<=3;i++){
			set1.add("pred" + i);
		}
		lnm1 = new LinearModel(tbl1, "resp1", set1);
		
		
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		map1 = lnm1.findLMG();
		
		assertEquals(0.3552797607357073,map1.get("pred1"),0.00001);
		assertEquals(0.4835826286037356,map1.get("pred2"),0.00001);
		assertEquals(0.16113761066055712,map1.get("pred3"),0.00001);
		
		initialize();
		
	}
	
	@Test
	public void findRSquared_R2Found_R2Correct(){
		
		//set1 = set of predictors
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(0.9233,lnm1.findRSquared(),0.0001);
		
		set1 = new HashSet<String>();
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(0.,lnm1.findRSquared(),0.0001);
	}
	
	@Test
	public void findAdjustedRSquared_R2Found_R2Correct(){
	
		//set1 = set of predictors
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		assertEquals(0.7698,lnm1.findAdjustedRSquared(),0.0001);
		
		set1 = new HashSet<String>();
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(0.,lnm1.findAdjustedRSquared(),0.0001);
	}

	@Test
	public void findTSS_TSSFound_TSSCorrect(){
		assertEquals(34.75,lnm1.findTSS(),0.0000001);
	}
	
	@Test
	public void findPRESS(){
		
		//set1 = set of predictors
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=1;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(18.48073,lnm1.findPRESS(),0.0001);

		set1 = new HashSet<String>();
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(61.777777777777,lnm1.findPRESS(),0.0000001);
	}
	
	@Test
	public void findCoefficientEstimates(){
		
		//set1 = set of predictors
		//map1 = coefficient estimates
		
		HashMap<String,Double> map1;
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		map1 = lnm1.findCoefficientEstimates();
		assertEquals(-3.667,map1.get("(Intercept)"),0.001);
		assertEquals(1.333,map1.get("pred1"),0.001);
		assertEquals(1.667,map1.get("pred2"),0.001);
 	}
	
	@Test
	public void findPrediction_PredictionFound_PredictionCorrect(){
		
		//map1 = map of predictors
		//set1 = set of predictors
		
		HashMap<String,Double> map1;
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){
			set1.add("pred" + i);
		}
		try {
			lnm1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		map1 = new HashMap<String,Double>();
		map1.put("pred1", 11.);
		map1.put("pred2", -19.);
		assertEquals(lnm1.findPrediction(map1),-20.666666,0.001);
		map1.put("pred2", Double.NaN);
		assertTrue(Double.isNaN(lnm1.findPrediction(map1)));
	}
}
