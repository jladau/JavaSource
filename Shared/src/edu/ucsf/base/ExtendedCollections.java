package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 * Extended functions for collections.
 * @author jladau
 */

public class ExtendedCollections {

	/**equal
	 * Checks if array and ArrayList have the same entries. Order does not matter.
	 * @param rgt1 Array of values.
	 * @param lst1 List of values.
	 * @return True if entries are the same, false otherwise. 
	 */
	public static <T> boolean equivalent(T[] rgt1, ArrayList<T> lst1){
		if(rgt1.length!=lst1.size()){
			return false;
		}
		for(T t:rgt1){
			if(!lst1.contains(t)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if array and ArrayList have the same entries. Order does not matter.
	 * @param rgt1 Array of values.
	 * @param set1 Set of values.
	 * @return True if entries are the same, false otherwise. 
	 */
	public static <T> boolean equivalent(T[] rgt1, Set<T> set1){
		if(rgt1.length!=set1.size()){
			return false;
		}
		for(T t:rgt1){
			if(!set1.contains(t)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if array and ArrayList have the same entries. Order does not matter.
	 * @param lst1 List of values.
	 * @param set1 Set of values.
	 * @return True if entries are the same, false otherwise. 
	 */
	public static <T> boolean equivalent(ArrayList<T> lst1, Set<T> set1){
		if(lst1.size()!=set1.size()){
			return false;
		}
		for(T t:lst1){
			if(!set1.contains(t)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if values in two sets are approximately equal.
	 * @param set1 First set.
	 * @param set2 Second set.
	 * @param dTolerance Tolerance for differences in values.
	 * @return True if approximately equal; false otherwise.
	 */
	
	//TODO write unit test
	
	public static boolean equalsApproximately(Set<Double> set1, Set<Double> set2, double dTolerance){
		
		//rgd1 = first set in array format
		//rgd2 = second set in array format
		
		Double rgd1[];
		Double rgd2[];
		
		if(set1.size()!=set2.size()){
			return false;
		}
		rgd1 = set1.toArray(new Double[set1.size()]);
		rgd2 = set2.toArray(new Double[set2.size()]);
		Arrays.sort(rgd1);
		Arrays.sort(rgd2);
		for(int i=0;i<rgd1.length;i++){
			if(Math.abs(rgd1[i]-rgd2[i])>dTolerance){
				return false;
			}
		}
		return true;
	}
	
	
}
