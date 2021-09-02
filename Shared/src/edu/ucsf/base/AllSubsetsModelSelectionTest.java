package edu.ucsf.base;

import java.util.HashMap;
import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.Test;
import com.google.common.collect.HashBasedTable;

/**
 * Runs all subsets model selection based on PRESS statistic.
 * @author jladau
 */

public class AllSubsetsModelSelectionTest {
	
	/**Model selection object**/
	private AllSubsetsModelSelection msl1;
	
	/**Linear model object**/
	private LinearModel lnm1;
	
	public AllSubsetsModelSelectionTest(String sNanHandling) throws Exception{
		initialize(sNanHandling);
	}
	
	private void initialize(String sNanHandling) throws Exception{
		
		//rgs1 = data in string format
		//set1 = set of predictors
		//tbl1 = data tab;e
		
		String rgs1[][];
		HashSet<String> set1;
		HashBasedTable<String,String,Double> tbl1;
		
		rgs1 = new String[9][4];
		rgs1 = new String[][]{{"","pred1","pred2","pred3","resp1"},
				{"obs1","1","2","8","1"},
				{"obs2","2","2","5","3"},
				{"obs3","3","3","6","4"},
				{"obs4","4","4","9","9"},
				{"obs5","5","1","1","9"},
				{"obs6","6","2","9","12"},
				{"obs7","7","2","4","19"},
				{"obs8","8","3","2","1"}};
		
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
		msl1 = new AllSubsetsModelSelection(set1,"resp1","resp1transform","resp1units",lnm1,2,5.0,null,null,null);	
	}
	
	@Test
	public void getBestModel_ModelGotten_ModelCorrect(){
		
		//smr1 = current summary
		//set1 = set of predictors
		//mapBest = returns best model for number of predictors
		//mapPRESS = returns press for best model for number of predictors
		//d1 = best PRESS statistic
		
		AllSubsetsModelSelection.ModelSummary smr1;
		HashSet<String> set1;
		HashMap<Integer,HashSet<String>> mapBest;
		HashMap<Integer,Double> mapPRESS;
		double d1 = 0;
		
		mapBest = new HashMap<Integer,HashSet<String>>();
		mapPRESS = new HashMap<Integer,Double>();
		
		//zero predictor model
		set1 = new HashSet<String>();
		try{
			lnm1.fitModel(set1);
			mapBest.put(0, set1);
			d1 = lnm1.findPRESS();
			mapPRESS.put(0, d1);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//one predictor models
		set1 = new HashSet<String>();
		set1.add("pred1");
		try{
			lnm1.fitModel(set1);
			mapBest.put(1, set1);
			d1 = lnm1.findPRESS();
			mapPRESS.put(1, d1);
		}catch(Exception e){
			e.printStackTrace();
		}		
		
		set1 = new HashSet<String>();
		set1.add("pred2");
		try{
			lnm1.fitModel(set1);
			if(lnm1.findPRESS()<d1){
				mapBest.put(1, set1);
				d1 = lnm1.findPRESS();
				mapPRESS.put(1, d1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		set1 = new HashSet<String>();
		set1.add("pred3");
		try{
			lnm1.fitModel(set1);
			if(lnm1.findPRESS()<d1){
				mapBest.put(1, set1);
				d1 = lnm1.findPRESS();
				mapPRESS.put(1, d1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//two predictors models
		set1 = new HashSet<String>();
		set1.add("pred1");
		set1.add("pred2");
		try{
			lnm1.fitModel(set1);
			mapBest.put(2, set1);
			d1 = lnm1.findPRESS();
			mapPRESS.put(2, d1);
		}catch(Exception e){
			e.printStackTrace();
		}		
		
		set1 = new HashSet<String>();
		set1.add("pred1");
		set1.add("pred3");
		try{
			lnm1.fitModel(set1);
			if(lnm1.findPRESS()<d1){
				mapBest.put(2, set1);
				d1 = lnm1.findPRESS();
				mapPRESS.put(2, d1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		set1 = new HashSet<String>();
		set1.add("pred2");
		set1.add("pred3");
		try{
			lnm1.fitModel(set1);
			if(lnm1.findPRESS()<d1){
				mapBest.put(2, set1);
				d1 = lnm1.findPRESS();
				mapPRESS.put(2, d1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//checking models
		for(int i=0;i<=2;i++){
			smr1 = msl1.getBestModel(i);
			for(String s:mapBest.get(i)){
				assertTrue(smr1.mapCoefficientEstimates.keySet().contains(s));
			}
			assertEquals(smr1.dPRESS,mapPRESS.get(i),0.00001);
		}
	}
}
