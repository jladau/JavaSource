package edu.ucsf.EDFsWithinCategories;

import java.util.ArrayList;
import java.util.Collections;
import com.google.common.collect.ArrayListMultimap;
import edu.ucsf.base.EmpiricalDistribution_Cumulative;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code computes EDFs within categories.
 * @author jladau
 *
 */

public class EDFsWithinCategoriesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//map1 = map from categories to list of values
		//lstOut = output
		//edf1 = current empirical distribution function
		//lst1 = current list of values sorted
		//rgd1 = list of values at which to compute edf
		
		EmpiricalDistribution_Cumulative edf1;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayListMultimap<String,Double> map1;
		ArrayList<String> lstOut;
		ArrayList<Double> lst1;
		Double rgd1[];
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading values map
		map1 = ArrayListMultimap.create();
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, arg1.getValueString("sCategoryKey")), dat1.getDouble(i, arg1.getValueString("sValueKey")));
		}
		
		//loading output
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("CATEGORY,VALUE,EDF_CUMULATIVE");
		for(String s:map1.keySet()){
			lst1 = new ArrayList<Double>(map1.get(s));
			Collections.sort(lst1);
			edf1 = new EmpiricalDistribution_Cumulative(lst1);
			if(arg1.containsArgument("rgdValuesForEDF")){
				rgd1 = arg1.getValueDoubleArray("rgdValuesForEDF");
			}else{
				rgd1 = lst1.toArray(new Double[lst1.size()]);
			}
			for(int i=0;i<rgd1.length;i++){
				lstOut.add(s + "," + rgd1[i] + "," + edf1.cumulativeProbability(rgd1[i]));
			}
		}
		
		//writing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}