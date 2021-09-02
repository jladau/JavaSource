package edu.ucsf.Ranks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class RanksLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sValueCol = value column
		//sCategoryCol = category column
		//lst1 = list of ranks
		//map1 = map from category to row indices
		//map2 = map from category to values
		//lst2 = current list of indices
		
		ArgumentIO arg1;
		DataIO dat1;
		String sValueCol;
		String sCategoryCol;
		ArrayList<Integer> lst1;
		ArrayList<Integer> lst2;
		ArrayListMultimap<String,Integer> map1;
		ArrayListMultimap<String,Double> map2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sValueCol = arg1.getValueString("sRankingValueField");
		if(arg1.containsArgument("sCategoryField")){
			sCategoryCol = arg1.getValueString("sCategoryField");
		}else{
			sCategoryCol = null;
		}
		
		//loading ranks
		if(sCategoryCol==null){
			lst1 = ranks(dat1.getDoubleColumn(sValueCol));
			dat1.appendToLastColumn(0, "RANK");
			for(int i=0;i<lst1.size();i++){
				dat1.appendToLastColumn(i+1,lst1.get(i));
			}
			
		}else{
			map1 = ArrayListMultimap.create(dat1.iRows,10);
			map2 = ArrayListMultimap.create(dat1.iRows,10);
			for(int i=1;i<dat1.iRows;i++){
				map1.put(dat1.getString(i, sCategoryCol), i);
				map2.put(dat1.getString(i, sCategoryCol), dat1.getDouble(i, sValueCol));
			}
			dat1.appendToLastColumn(0, "RANK");
			for(String s:map1.keySet()){
				lst2 = new ArrayList<Integer>(map1.get(s));
				lst1 = ranks(new ArrayList<Double>(map2.get(s)));
				for(int k=0;k<lst2.size();k++){
					dat1.appendToLastColumn(lst2.get(k), lst1.get(k));
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static ArrayList<Integer> ranks(ArrayList<Double> lst1){
		
		//map1 = map from values to ranks
		//lst2 = input values sorted
		//lst3 = output
		//itr1 = iterator
		
		HashMultimap<Double,Integer> map1;
		ArrayList<Double> lst2;
		ArrayList<Integer> lst3;
		Iterator<Integer> itr1;
		
		lst2 = new ArrayList<Double>(lst1);
		Collections.sort(lst2);
		map1 = HashMultimap.create(lst2.size(), lst2.size());
		for(int i=0;i<lst2.size();i++){
			map1.put(lst2.get(i), i+1);
		}
		lst3 = new ArrayList<Integer>(lst2.size());
		for(int i=0;i<lst1.size();i++){
			itr1 = map1.get(lst1.get(i)).iterator();
			lst3.add(itr1.next());
			itr1.remove();
		}
		return lst3;
	}
}
