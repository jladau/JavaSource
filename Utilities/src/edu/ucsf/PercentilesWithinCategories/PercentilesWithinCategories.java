package edu.ucsf.PercentilesWithinCategories;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class PercentilesWithinCategories{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//map1 = map from categories to data
		//sCategory = category column
		//sValue = value column
		//lstOut = output
		//rgd1 = current data
		//dPercentile = current percentile
		//pct1 = percentile object
		//d1 = percent to use
		//i1 = counter
		
		int i1;
		Percentile pct1;
		ArgumentIO arg1;
		DataIO dat1;
		HashMultimap<String,Double> map1;
		String sCategory;
		String sValue;
		ArrayList<String> lstOut;
		double rgd1[];
		double d1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategory = arg1.getValueString("sCategoryHeader");
		sValue = arg1.getValueString("sValueHeader");
		lstOut = new ArrayList<String>(dat1.iRows);
		d1 = arg1.getValueDouble("dPercent");
		
		//loading data map
		map1 = HashMultimap.create(dat1.iRows,1000);
		for(int i=1;i<dat1.iRows;i++) {
			map1.put(dat1.getString(i,sCategory),dat1.getDouble(i,sValue));
		}
		
		//outputting results
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(sCategory + "," + sValue + "_PERCENTILE");
		for(String s:map1.keySet()){
			rgd1 = new double[map1.get(s).size()];
			i1 = 0;
			for(Double d:map1.get(s)) {
				rgd1[i1]=d;
				i1++;
			}
			pct1 = new Percentile(d1);
			pct1.setData(rgd1);
			lstOut.add(s + "," + pct1.evaluate());
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
