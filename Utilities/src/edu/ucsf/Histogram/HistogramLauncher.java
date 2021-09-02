package edu.ucsf.Histogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class HistogramLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sField = field with values
		//bLog = flag for log bins
		//iBins = number of bins
		//lst1 = list of values (raw)
		//lst2 = list of values (transformed)
		//dStep = step size
		//map1 = map from bin floors to frequencies
		//d1 = current bin floor
		//d2 = current floor value
		//d3 = current upper bound for bin
		//iFrequency = current frequency
		//dMax = maximum value
		//lstOut = output
		//iTotal = total
		
		ArgumentIO arg1;
		DataIO dat1;
		String sField;
		boolean bLog;
		int iBins;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		double dStep;
		TreeMap<Double,Integer> map1;
		double d1;
		double d2;
		double d3;
		int iFrequency;
		double dMax;
		ArrayList<String> lstOut;
		int iTotal;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sField = arg1.getValueString("sValueField");
		bLog = arg1.getValueBoolean("bLogBins");
		iBins = arg1.getValueInt("iBins");
		
		//loading step size
		lst1 = dat1.getDoubleColumn(sField);
		Collections.sort(lst1);
		if(bLog==true){
			lst2 = new ArrayList<Double>(lst1.size());
			for(int i=0;i<lst1.size();i++){
				lst2.add(Math.log10(lst1.get(i)));
			}
		}else{
			lst2 = lst1;
		}
		dMax = lst2.get(lst2.size()-1);
		dStep = (dMax-lst2.get(0))/((double) iBins);
		
		//initializing frequency map
		map1 = new TreeMap<Double,Integer>();
		d1 = lst2.get(0);
		do{
			if(bLog==true){
				map1.put(Math.pow(10, d1), 0);
			}else{
				map1.put(d1, 0);
			}
			d1+=dStep;
		}while(d1<dMax);
		
		//loading frequencies
		for(double d:lst1){
			d2 = map1.floorKey(d);
			iFrequency = map1.get(d2);
			iFrequency++;
			map1.put(d2, iFrequency);
		}
		iTotal = lst1.size();
		
		//outputting results
		lstOut = new ArrayList<String>();
		lstOut.add("BIN,BIN_FLOOR,BIN_CEILING,BIN_MEAN,FREQUENCY,PROPORTION");
		for(double d:map1.keySet()){
			
			
			if(d!=map1.lastKey()){
				d3 = map1.higherKey(d);
			}else{
				d3 = lst1.get(lst1.size()-1);	
			}
			lstOut.add(d + " -- " + d3 + "," + d + "," + d3 + "," + (d+d3)/2. + "," + map1.get(d) + "," + map1.get(d)/((double) iTotal));
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
		
		
	}
	
	
}
