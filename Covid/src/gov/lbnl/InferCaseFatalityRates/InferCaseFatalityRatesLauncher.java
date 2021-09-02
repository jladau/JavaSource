package gov.lbnl.InferCaseFatalityRates;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.HashBasedTable;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class InferCaseFatalityRatesLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//tbl1 = table from times, data fields to values
		//ifr1 = inferred rates object
		//map1 = mean transformed coefficient values
		//rgi1 = time bounds
		//lstOut = output
		//tblOut = output
		
		HashBasedTable<Integer,String,Double> tbl1;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		HashBasedTable<Integer,String,Double> tblOut;
		InferredRates ifr1;
		//HashMap<String,Double> map1;
		//int rgi1[];
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading data table
		tbl1 = HashBasedTable.create(dat1.iRows,dat1.iCols);
		for(int i=1;i<dat1.iRows;i++){
			for(String s:dat1.getHeaders()) {
				if(!s.equals("TIME")){
					tbl1.put(dat1.getInteger(i,"TIME"),s,dat1.getDouble(i,s));
				}
			}
		}
		
		//inferring rates
		ifr1 = new InferredRates(
				tbl1, 
				arg1.getValueArrayList("lstPredictors"), 
				arg1.getValueString("sResponse"),
				0,
				Integer.MAX_VALUE);
		ifr1.inferRates(
				arg1.getValueDouble("dSlopeMinimum"),
				arg1.getValueDouble("dSlopeMaximum"),
				arg1.getValueDouble("dSlopeStep"));
		
		//outputting results
		tblOut = ifr1.coefficients();
		lstOut = new ArrayList<String>(tblOut.rowKeySet().size()*tblOut.columnKeySet().size()+1);
		lstOut.add("REGION_ID,TIME,COEFFICIENT_ESTIMATE");
		for(String s:tblOut.columnKeySet()){
			for(Integer i:tblOut.rowKeySet()){
				lstOut.add(s + "," + i + "," + tblOut.get(i,s));
			}
		}
		/*
		//outputting results
		map1 = ifr1.meanCoefficients(arg1.getValueDouble("dOffset"),arg1.getValueBoolean("bInverse"));
		rgi1 = ifr1.timeBounds();
		lstOut = new ArrayList<String>(map1.size()+1);
		lstOut.add("REGION_ID,TIME_START,TIME_END,COEFFICIENT_ESTIMATE");
		for(String s:map1.keySet()){
			lstOut.add(s + "," + rgi1[0] + "," + rgi1[1] + "," + map1.get(s));
		}
		*/
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Slope estimate: " + ifr1.slope());
		System.out.println("Done.");
		
	}	
}
