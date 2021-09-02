package edu.ucsf.IdenticalObservationNoise;

import java.util.HashMap;

import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code adds noise to identical observations -- useful for making boxplots with gnuplot
 * @author jladau
 */

public class IdenticalObservationNoiseLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//map1(sCategory) = returns map giving the frequency of each observation
		//sCategory = current category
		//dValue = current value
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<String,HashMap_AdditiveDouble<Double>> map1;
		String sCategory;
		double dValue;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through data
		map1 = new HashMap<String,HashMap_AdditiveDouble<Double>>();
		for(int i=1;i<dat1.iRows;i++){
			sCategory = dat1.getString(i, arg1.getValueString("sCategoryHeader"));
			dValue = dat1.getDouble(i, arg1.getValueString("sValueHeader"));
			if(!map1.containsKey(sCategory)){
				map1.put(sCategory, new HashMap_AdditiveDouble<Double>());
			}
			map1.get(sCategory).putSum(dValue,1.);
		}
		
		//outputting changes
		for(int i=1;i<dat1.iRows;i++){
			sCategory = dat1.getString(i, arg1.getValueString("sCategoryHeader"));
			dValue = dat1.getDouble(i, arg1.getValueString("sValueHeader"));
			if(map1.get(sCategory).get(dValue)>1.){
				dat1.setString(i, arg1.getValueString("sValueHeader"), Double.toString(dValue+(Math.random()-0.5)*arg1.getValueDouble("dOffset")));
			}
		}
		
		//printing output
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");	
	}
}
