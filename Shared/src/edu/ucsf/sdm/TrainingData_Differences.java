package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.HashBasedTable3D;
import edu.ucsf.io.BiomIO;

/**
 * Training data for use when modeling differences between communities
 * @author jladau
 *
 */
public class TrainingData_Differences extends TrainingData{

	/**Returns predictor variable value or response variable value for specified (sVariable,sSample1, sSample2)**/
	private HashBasedTable3D<String,String,String> tblDiffs;
	
	/**Initial version of data table**/
	private HashBasedTable<String,String,Double> tbl0;
	
	public TrainingData_Differences(TrainingData trn1, String sResponseDifferenceTransform, BiomIO bio1) throws Exception{
		
		super(trn1);
		
		//lst1 = list of samples
		//d1 = first value
		//d2 = second value
		
		double d1;
		double d2;
		ArrayList<String> lst1;
		
		tblDiffs = new HashBasedTable3D<String,String,String>();
		lst1 = new ArrayList<String>(tbl1.columnKeySet());
		
		//loading difference table for other variables
		for(String sVariable:tbl1.rowKeySet()){
			for(int i=1;i<lst1.size();i++){
				for(int k=0;k<i;k++){
					d1 = tbl1.get(sVariable, lst1.get(i));
					d2 = tbl1.get(sVariable, lst1.get(k));
					
					//response variable
					if(setResponseVars.contains(sVariable)){
						d1 = ResponseTransform.applyInverse(d1, sVariable, getResponseTransform(sVariable));
						d2 = ResponseTransform.applyInverse(d2, sVariable,  getResponseTransform(sVariable));
						tblDiffs.put(
								sVariable, 
								lst1.get(i),
								lst1.get(k), 
								ResponseTransformDifferences.apply(lst1.get(i), "current", d1, lst1.get(k), "current", d2, sResponseDifferenceTransform));
					
					//predictor
					}else{
						tblDiffs.put(
								sVariable, 
								lst1.get(i), 
								lst1.get(k), 
								PredictorTransformDifferences.apply(lst1.get(i), "current", d1, lst1.get(k), "current", d2, sResponseDifferenceTransform));
					}
				}
			}
		}
		
		//loading data table
		loadDataTable();
	}
	
	//TODO write unit test
	public HashBasedTable3D<String,String,String> getDataTable3D(){
		return tblDiffs;
	}
	
	private void loadDataTable(){
		tbl0 = tbl1;
		tbl1 = HashBasedTable.create();
		for(String sVariable:tblDiffs.key1Set()){
			for(String sSample1:tblDiffs.key2Set()){
				for(String sSample2:tblDiffs.key3Set()){
					if(tblDiffs.contains(sVariable, sSample1, sSample2)){
						tbl1.put(sVariable, PredictorTransformDifferences.getOrderedID(sSample1, "current", sSample2, "current"), tblDiffs.get(sVariable, sSample1, sSample2));
					}
				}
			}
		}
	}	
	
	/**
	 * Returns list of samples that do not have NaN values for specified variable
	 * @param sVariable Variable to use for checking for NaN variables
	 * @return List of samples
	 */
	public HashSet<String> getNonNanSamples(String sVariable){
		
		//set1 = output
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(tbl0.columnKeySet().size());
		for(String s:tbl0.columnKeySet()){
			if(!Double.isNaN(tbl0.get(sVariable, s))){
				set1.add(s);
			}
		}
		return set1;
	}
	
	
}