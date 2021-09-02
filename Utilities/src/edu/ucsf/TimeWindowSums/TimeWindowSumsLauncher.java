package edu.ucsf.TimeWindowSums;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.HashMultimap;

import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TimeWindowSumsLauncher{

	public static void main(String[] rgsArgs){
		
		//arg1 = arguments
		//dat1 = data
		//iStartTime = start time
		//iEndTime = end time
		//iWindowSize = window size
		//map1 = map from window start times to times within window
		//map2 = map from times to windows
		//i1 = current window start time
		//map3 = map from category, window start times to sums
		//s1 = current category, window start
		//sCategory = category
		//sValue = value
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		int iStartTime;
		int iEndTime;
		int iWindowSize;
		int i1;
		HashMultimap<Integer,Integer> map1;
		HashMap<Integer,Integer> map2;
		HashMap_AdditiveDouble<String> map3;
		String s1;
		String sCategory;
		String sValue;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		iWindowSize = arg1.getValueInt("iWindowSize");
		iStartTime = arg1.getValueInt("iStartTime");
		iEndTime = arg1.getValueInt("iEndTime");
		sCategory = arg1.getValueString("sCategory");
		sValue = arg1.getValueString("sValue");
		
		//loading time windows
		map1 = HashMultimap.create(dat1.iRows,iWindowSize);
		map2 = new HashMap<Integer,Integer>(dat1.iRows);
		i1 = iStartTime;
		for(int i=iStartTime;i<=iEndTime;i++){
			if((i-iStartTime) % iWindowSize == 0){
				i1 = i;
			}
			map1.put(i1,i);
			map2.put(i,i1);
		}
		
		//looping through data
		map3 = new HashMap_AdditiveDouble<String>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			if(!map2.containsKey(dat1.getInteger(i,"TIME"))) {
				continue;
			}
			i1 = map2.get(dat1.getInteger(i,"TIME"));
			s1 = dat1.getString(i,sCategory) + "," + i1;
			map3.putSum(s1,dat1.getDouble(i,sValue));
		}
		
		//outputting results
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(sCategory + ",TIME," + sValue);
		for(String s:map3.keySet()){
			lstOut.add(s + "," + map3.get(s));
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}
