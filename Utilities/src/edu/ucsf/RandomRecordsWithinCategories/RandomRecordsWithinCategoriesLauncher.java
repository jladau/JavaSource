package edu.ucsf.RandomRecordsWithinCategories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.google.common.collect.HashMultimap;

import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class RandomRecordsWithinCategoriesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sID = record ID field
		//sCategory = category field
		//map1 = map from categories to selected records
		//lst1 = list of records in random order
		//map2 = map from records to categories
		//i1 = number of records per category
		//s1 = current category
		//s2 = current record id
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		String sID;
		String sCategory;
		HashMultimap<String,String> map1;
		ArrayList<String> lst1;
		HashMap<String,String> map2;
		int i1;
		String s1;
		String s2;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sID = arg1.getValueString("sRecordIDField");
		sCategory = arg1.getValueString("sCategoryField");
		i1 = arg1.getValueInt("iRecordsPerCategory");
		if(i1<0){
			i1 = findMaximumGroupSize(dat1,sCategory);
		}
		lst1 = new ArrayList<String>(dat1.iRows);
		map2 = new HashMap<String,String>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			lst1.add(dat1.getString(i, sID));
			map2.put(dat1.getString(i, sID), dat1.getString(i, sCategory));
		}
		Collections.shuffle(lst1);
		
		//loading records
		map1 = HashMultimap.create(100,i1);
		for(int i=0;i<lst1.size();i++){
			s2 = lst1.get(i);
			s1 = map2.get(s2);
			if(!map1.containsKey(s1)){
				map1.put(s1, s2);
			}else{
				if(map1.get(s1).size()<i1){
					map1.put(s1, s2);
				}
			}		
		}
		
		//outputting results
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(sID + "," + sCategory);
		for(String s:map1.keySet()){
			if(map1.get(s).size()==i1){
				for(String t:map1.get(s)){
					lstOut.add(t + "," + s);
				}
			}
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static int findMaximumGroupSize(DataIO dat1, String sCategory){
		
		//map1 = map from categories to counts
		//i1 = output
		
		HashMap_AdditiveInteger<String> map1;
		int i1;
		
		map1 = new HashMap_AdditiveInteger<String>(100);
		for(int i=1;i<dat1.iRows;i++){
			map1.putSum(dat1.getString(i, sCategory), 1);
		}
		i1 = Integer.MAX_VALUE;
		for(String s:map1.keySet()){
			if(map1.get(s)<i1){
				i1=map1.get(s);
			}
		}
		return i1;
	}
}