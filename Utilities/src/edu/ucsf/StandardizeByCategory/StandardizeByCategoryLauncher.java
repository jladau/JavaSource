package edu.ucsf.StandardizeByCategory;

import java.util.ArrayList;
import com.google.common.collect.HashMultimap;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Standarizes (z-score) data within categories
 * @author jladau
 *
 */

public class StandardizeByCategoryLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//map1 = map from each category name to indices of rows for it
		//lst1 = list of values
		//dMean = current mean
		//dStDev = current standard deviation
	
		ArrayList<Double> lst1;
		double dMean;
		double dStDev;
		ArgumentIO arg1;
		DataIO dat1;
		HashMultimap<String,Integer> map1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		map1 = HashMultimap.create();
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, arg1.getValueString("sCategoryKey")),i);
		}
		dat1.appendToLastColumn(0, arg1.getValueString("sValueKey") + "_STANDARDIZED");
		
		//looping through categories
		for(String s:map1.keySet()){
			lst1 = new ArrayList<Double>();
			for(int i:map1.get(s)){
				lst1.add(dat1.getDouble(i,arg1.getValueString("sValueKey")));
			}
			dMean = ExtendedMath.mean(lst1);
			dStDev = ExtendedMath.standardDeviationP(lst1);
			dStDev = Math.sqrt(dStDev*dStDev*((double) lst1.size())/((double) lst1.size() - 1.));
			for(int i:map1.get(s)){
				dat1.appendToLastColumn(i,(dat1.getDouble(i,arg1.getValueString("sValueKey"))-dMean)/dStDev);
			}
		}
		
		//writing output
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");	
	}	
}