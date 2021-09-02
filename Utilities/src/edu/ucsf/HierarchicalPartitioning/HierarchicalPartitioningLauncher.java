package edu.ucsf.HierarchicalPartitioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.HierarchicalPartitioning;
import edu.ucsf.base.LinearModel;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Performs hierarchical paritioning.
 * @author jladau
 *
 */


public class HierarchicalPartitioningLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//lnm1 = linear model object
		//tblData = data for linear model
		//rgsPredictors = predictors
		//sResponse = response
		//setPredictors = predictors
		//map1 = map from variables to lmg values
		//lstOut = output
		
		String rgsPredictors[];
		String sResponse;
		ArgumentIO arg1;
		DataIO dat1;
		LinearModel lnm1;
		HashBasedTable<String,String,Double> tblData;
		HashSet<String> setPredictors;
		HashMap<String,Double> map1;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgsPredictors = arg1.getValueStringArray("rgsPredictors");
		setPredictors = new HashSet<String>();
		for(String s:rgsPredictors){
			setPredictors.add(s);
		}
		sResponse = arg1.getValueString("sResponse");
		tblData = HashBasedTable.create(rgsPredictors.length+1,dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			for(String s:rgsPredictors){
				tblData.put(s, Integer.toString(i), dat1.getDouble(i, s));
			}
			tblData.put(sResponse, Integer.toString(i), dat1.getDouble(i, sResponse));				
		}
		
		//loading model
		lnm1 = new LinearModel(tblData, sResponse, setPredictors);
		
		//loading hierarchical partitioning results
		map1 = (new HierarchicalPartitioning(lnm1, rgsPredictors, sResponse, 1000)).getPatitioning();
		
		//outputting results
		lstOut = new ArrayList<String>();
		lstOut.add("VARIABLE,LMG");
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}
