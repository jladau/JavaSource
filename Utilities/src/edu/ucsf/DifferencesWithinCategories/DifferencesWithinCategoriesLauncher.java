package edu.ucsf.DifferencesWithinCategories;

import java.util.ArrayList;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates differences between all observations within categories in flat file.
 * @author jladau
 *
 */

public class DifferencesWithinCategoriesLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//tbl1 = map from categories, observations to values
		//sObsID = current observation ID
		//sObsID2 = current second observation ID
		//sCatID = current category ID
		//dValue = current value
		//dValue2 = current second value
		//lstOut = output
		//i1 = number of output rows
		//sOperation = operation: "difference" or "absdifference"
		//lstObs = current list of observations
		
		String sObsID;
		String sObsID2;
		String sCatID;
		double dValue;
		double dValue2;
		ArgumentIO arg1;
		DataIO dat1;
		HashBasedTable<String,String,Double> tbl1;
		ArrayList<String> lstOut;
		int i1;
		String sOperation;
		ArrayList<String> lstObs;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		tbl1 = HashBasedTable.create();
		if(arg1.getValueString("sOperation").equals("difference")){
			sOperation="DIFFERENCE";
		}else if(arg1.getValueString("sOperation").equals("absdifference")){
			sOperation="ABS_DIFFERENCE";
		}else{
			throw new Exception("Invalid comparison operation.");
		}
		
		//creating table of values
		for(int i=1;i<dat1.iRows;i++){
			sObsID = dat1.getString(i, arg1.getValueStringArray("rgsIDColumns"));
			sCatID = dat1.getString(i, arg1.getValueString("sCategoryColumn"));
			dValue = dat1.getDouble(i, arg1.getValueString("sValueColumn"));
			tbl1.put(sCatID, sObsID, dValue);
		}
		
		//initializing output
		i1 = 1;
		for(String s:tbl1.rowKeySet()){
			i1 += tbl1.row(s).size()*(tbl1.row(s).size()-1)/2;
		}
		lstOut = new ArrayList<String>(i1);
		lstOut.add(
				arg1.getValueString("sCategoryColumn") + "," +
				Joiner.on("_1,").join(arg1.getValueStringArray("rgsIDColumns")) + "_1" + "," +
				Joiner.on("_2,").join(arg1.getValueStringArray("rgsIDColumns")) + "_2" + "," +
				arg1.getValueString("sValueColumn") + "_" + sOperation
				);
		
		//outputting results
		for(String s:tbl1.rowKeySet()){
			lstObs = new ArrayList<String>(tbl1.row(s).keySet());
			for(int i=1;i<lstObs.size();i++){
				sObsID = lstObs.get(i);
				dValue = tbl1.get(s, sObsID);
				for(int j=0;j<i;j++){
					sObsID2 = lstObs.get(j);
					dValue2 = tbl1.get(s, sObsID2);
					if(sObsID.compareTo(sObsID2)<0){
						lstOut.add(
								s
								+ "," + sObsID
								+ "," + sObsID2
								+ "," + compare(sOperation, dValue, dValue2));
					}else{
						lstOut.add(
								s
								+ "," + sObsID2
								+ "," + sObsID
								+ "," + compare(sOperation, dValue2, dValue));
					}
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static double compare(String sOperation, double dValue1, double dValue2){
		
		if(sOperation.equals("DIFFERENCE")){
			return dValue1-dValue2;
		}else if(sOperation.equals("ABS_DIFFERENCE")){
			return Math.abs(dValue1-dValue2);
		}else{
			return Double.NaN;
		}
	}
}
