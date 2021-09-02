package edu.ucsf.FindThreshold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Finds row of data file where threshold is met (less than a 5% increase)
 * @author jladau
 *
 */

public class FindThresholdLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sOrderField = ordering field
		//sValueField = value field
		//map1 = map from ordering values to values
		//map2 = map from ordering values to row indices
		//lst1 = ordering values in increasing order
		//i1 = output row
		//lstOut = output
		//dF = final value
		//dO = original value
		//dThreshold = percent increase threshold
		
		double dThreshold;
		double dF;
		double dO;
		ArrayList<String> lstOut;
		HashMap<Double,Integer> map2;
		HashMap<Double,Double> map1;
		ArrayList<Double> lst1;
		String sOrderField;
		String sValueField;
		ArgumentIO arg1;
		DataIO dat1;
		int i1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sOrderField = arg1.getValueString("sOrderField");
		sValueField = arg1.getValueString("sValueField");
		map1 = new HashMap<Double,Double>(dat1.iRows);
		map2 = new HashMap<Double,Integer>(dat1.iRows);
		lst1 = new ArrayList<Double>(dat1.iRows);
		lstOut = new ArrayList<String>();
		dThreshold = arg1.getValueDouble("dThreshold");
		
		//loading data
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getDouble(i, sOrderField), dat1.getDouble(i, sValueField));
			map2.put(dat1.getDouble(i, sOrderField), i);
			lst1.add(dat1.getDouble(i, sOrderField));
		}
		
		//finding threshold
		Collections.sort(lst1);
		i1 = -9999;
		if(Math.abs(lst1.get(0))<0.000000001 && lst1.get(0)==lst1.get(lst1.size()-1)){
			i1 =0;
		}else{
			for(int i=0;i<lst1.size()-1;i++){
				dF = map1.get(lst1.get(i+1));
				dO = map1.get(lst1.get(i));
				if(dO>0 && dF>0 && percentChange(dO, dF)<dThreshold){
					i1 = map2.get(lst1.get(i));
					break;
				}
			}
			if(i1==-9999){
				i1 = map2.get(lst1.get((lst1.size() - 1)));
			}
		}
		
		//outputting results and terminating
		lstOut = new ArrayList<String>();
		lstOut.add(Joiner.on(",").join(dat1.getRow(0)));
		lstOut.add(Joiner.on(",").join(dat1.getRow(i1)));
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
		
	}
	
	private static double percentChange(double dStart, double dFinal){
		return (dFinal-dStart)/dStart;
	}
}
