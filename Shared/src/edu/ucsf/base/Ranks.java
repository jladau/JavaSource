package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.HashMultimap;

public class Ranks {

	public static ArrayList<Double> ranksAverage(ArrayList<Double> lst1){
		
		//map1 = map from values to non-averaged ranks
		//map2 = map from values to averaged ranks
		//lst2 = input values sorted
		//lst3 = output
		//d1 = current sum of ranks
		
		HashMultimap<Double,Integer> map1;
		HashMap<Double,Double> map2;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		double d1;
		
		lst2 = new ArrayList<Double>(lst1);
		Collections.sort(lst2);
		map1 = HashMultimap.create(lst2.size(), lst2.size());
		map1.put(lst2.get(0), 1);
		for(int i=1;i<lst2.size();i++){
			map1.put(lst2.get(i), i+1);
		}
		map2 = new HashMap<Double,Double>(map1.size());
		for(Double d:map1.keySet()){
			if(map1.get(d).size()==1){
				map2.put(d, (double) firstElement(map1.get(d)));
			}else{
				d1 = 0;
				for(Integer i:map1.get(d)){
					d1+=(double) i;
				}
				map2.put(d, d1/((double) map1.get(d).size()));
			}
		}
		lst3 = new ArrayList<Double>(lst2.size());
		for(int i=0;i<lst1.size();i++){
			lst3.add(map2.get(lst1.get(i)));
		}
		return lst3;
	}
	
	public static ArrayList<Double> ranksFloor(ArrayList<Double> lst1){
		
		//map1 = map from values to ranks
		//lst2 = input values sorted
		//lst3 = output
		//i1 = current rank
		
		HashMap<Double,Integer> map1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		int i1;
		
		lst2 = new ArrayList<Double>(lst1);
		Collections.sort(lst2);
		map1 = new HashMap<Double,Integer>(lst2.size());
		map1.put(lst2.get(0), 1);
		i1=1;
		for(int i=1;i<lst2.size();i++){
			
			//***********************
			//System.out.println(lst2.get(i)-lst2.get(i-1));
			//System.out.println(lst2.get(i)!=lst2.get(i-1));
			//System.out.println(lst2.get(i)==lst2.get(i-1));	
			//System.out.println(lst2.get(i).equals(lst2.get(i-1)));	
			//System.out.println(Math.abs(lst2.get(i)-lst2.get(i-1))>0);
			//***********************
			
			if(!lst2.get(i).equals(lst2.get(i-1))){
				i1 = i+1;
			}
			if(!map1.containsKey(lst2.get(i))){
				map1.put(lst2.get(i), i1);
			}
		}
		lst3 = new ArrayList<Double>(lst2.size());
		for(int i=0;i<lst1.size();i++){
			lst3.add((double) map1.get(lst1.get(i)));
		}
		return lst3;
	}
	
	private static Integer firstElement(Set<Integer> set1){
		
		for(Integer i:set1){
			return i;
		}
		return Integer.MIN_VALUE;
	}
}
