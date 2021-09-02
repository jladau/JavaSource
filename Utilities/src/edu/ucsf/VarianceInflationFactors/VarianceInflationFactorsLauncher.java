package edu.ucsf.VarianceInflationFactors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.ucsf.base.LinearModel;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class VarianceInflationFactorsLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//lin1 = linear model object
		//map1 = variance inflation factors
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		HashBasedTable<String,String,Double> tblData; 
		HashSet<String> setPredictors;
		HashMap<String,Double> map1;
		LinearModel lin1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading data table
		tblData = HashBasedTable.create();
		setPredictors = new HashSet<String>();
		for(int i=1;i<dat1.iRows;i++){
			for(String s:dat1.getHeaders()){
				tblData.put(s,Integer.toString(i),dat1.getDouble(i,s));
				setPredictors.add(s);
			}
			tblData.put("response",Integer.toString(i),1.*i);
		}
		lin1 = new LinearModel(tblData, "response", setPredictors);
		lin1.fitModel(setPredictors);
		
		//outputting results
		map1 = lin1.findVIF();
		lstOut = new ArrayList<String>(map1.size()+1);
		lstOut.add("VARIABLE,VIF");
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
}
