package gov.lbnl.UpdateVariables;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class UpdateVariablesLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datResponses = response data
		//datPredictors = predictors data (unmerged)
		//datPredictorsMerged = predictors data (merged)
		//datEstimates = estimates
		//tblEstimates = estimates in table format
		//tbl1 = map from row ids and response variable IDs to new response value
		//tbl2 = map from row ids and predictor ids to new predictors
		//d1 = current response value to be removed
		//set1 = set of predictors that were merged previously
		//set2 = set of predictors that were already used
		//lst1 = current vector of mortalities
		//d2 = current maximum mean mortality
		//d3 = current mean mortality
		//d4 = value of current merged predictors
		//s1 = current predictor with maximum mean mortality
		//datPredictorsUsed = list of used predictors
		
		ArgumentIO arg1;
		DataIO datResponses;
		DataIO datPredictors;
		DataIO datPredictorsMerged;
		DataIO datEstimates;
		DataIO datPredictorsUsed;
		HashBasedTable<String,String,Double> tblEstimates;
		HashBasedTable<Integer,String,Double> tbl1;
		HashBasedTable<Integer,String,Double> tbl2;
		double d1;
		double d2;
		double d3;
		double d4;
		String s2;
		HashSet<String> set1;
		HashSet<String> set2;
		ArrayList<Double> lst1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datResponses = new DataIO(arg1.getValueString("sResponsePath"));
		datPredictors = new DataIO(arg1.getValueString("sRawPredictorsPath"));
		datPredictorsMerged = new DataIO(arg1.getValueString("sMergedPredictorsPath"));
		datPredictorsUsed = new DataIO(arg1.getValueString("sUsedPredictorsPath"));
		datEstimates = new DataIO(arg1.getValueString("sEstimatesPath"));
		tblEstimates = HashBasedTable.create(datEstimates.iRows,datEstimates.iCols);
		for(String s:datEstimates.getHeaders()){
			if(!s.equals("REGION_ID")){
				for(int i=1;i<datEstimates.iRows;i++){
					tblEstimates.put(datEstimates.getString(i,"REGION_ID"),s,datEstimates.getDouble(i,s));
				}
			}
		}
		
		//loading values to be removed
		tbl1 = HashBasedTable.create(datEstimates.iRows,datEstimates.iCols);
		for(int i=1;i<datPredictorsMerged.iRows;i++){
			for(String sResponse:tblEstimates.columnKeySet()){	
				d1 = 0;
				for(String sPredictor:datPredictorsMerged.getHeaders()){
					if(!sPredictor.equals("MERGED")){
						d1+=datPredictorsMerged.getDouble(i,sPredictor)/tblEstimates.get(sPredictor,sResponse);
					}
				}
				tbl1.put(i,sResponse,datResponses.getDouble(i,sResponse)-d1);
			}
		}
		
		//loading set of available predictors
		set2 = new HashSet<String>(datPredictorsUsed.iCols);
		for(int i=1;i<datPredictorsUsed.iRows;i++){
			set2.add(datPredictorsUsed.getString(i,"REGION_ID"));
		}
		set1 = new HashSet<String>(datPredictors.iCols);
		for(String s:datPredictors.getHeaders()){
			if(!set2.contains(s)){
				set1.add(s);
			}
		}
		
		if(set1.size()>1){
		
			//finding predictors to merge
			d2 = -Double.MAX_VALUE;
			s2 = null;
			for(String s:set1){
				lst1 = datPredictors.getDoubleColumn(s);
				d3 = ExtendedMath.mean(lst1);
				if(d3>d2){
					d2 = d3;
					s2 = s;
				}
			}
			
			//merging predictors
			tbl2 = HashBasedTable.create(datPredictors.iRows,2);
			for(int i=1;i<datPredictors.iRows;i++){
				d4 = 0;
				for(String s:set1){
					if(s.equals(s2)){
						tbl2.put(i,s,datPredictors.getDouble(i,s));
					}else{
						d4+=datPredictors.getDouble(i,s);
					}
				}
				tbl2.put(i,"MERGED",d4);
			}
			
		}else{
			
			//outputting predictor
			tbl2 = HashBasedTable.create(datPredictors.iRows,2);
			for(int i=1;i<datPredictors.iRows;i++){
				for(String s:set1){
					tbl2.put(i,s,datPredictors.getDouble(i,s));
				}
			}
		}
			
		//outputting results
		DataIO.writeToFile((new DataIO(tbl1)).getWriteableData(),arg1.getValueString("sResponseOutputPath"));
		DataIO.writeToFile((new DataIO(tbl2)).getWriteableData(),arg1.getValueString("sPredictorOutputPath"));
		System.out.println("Done.");
	}	
}