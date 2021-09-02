package edu.ucsf.InequalitySignificanceTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ResponseShuffler{

	/**List of response variables**/
	private ArrayList<Double> lstY;
	
	/**List of categories corresponding to response variables**/
	private ArrayList<String> lstCategories;
	
	/**List of unique values**/
	private ArrayList<Double> lstYUnique;
	
	/**Map from y vector row indices to unique category row indices**/
	private HashMap<Integer,Integer> mapIndices;
	
	public ResponseShuffler(ArrayList<Double> lstY, ArrayList<String> lstCategories){
		
		//map1 = map from unique category names to indices
		//i1 = counter
		
		HashMap<String,Integer> map1;
		int i1;
		
		this.lstY = new ArrayList<Double>(lstY);
		this.lstCategories = lstCategories;
		if(lstCategories!=null){
			map1 = new HashMap<String,Integer>(lstCategories.size());
			mapIndices = new HashMap<Integer,Integer>(lstY.size());
			lstYUnique = new ArrayList<Double>(lstCategories.size());
			i1 = 0;
			for(int i=0;i<lstY.size();i++){
				if(!map1.containsKey(lstCategories.get(i))){
					map1.put(lstCategories.get(i),i1);
					lstYUnique.add(lstY.get(i));
					i1++;
				}
				mapIndices.put(i,map1.get(lstCategories.get(i)));
			}
		}else {
			mapIndices = null;
			lstYUnique = null;
		}
	}
	
	public ArrayList<Double> nextShuffle(){
		
		//lst1 = output
		
		ArrayList<Double> lst1;
		
		if(lstCategories==null){
			Collections.shuffle(lstY);
			return lstY;
		}else{
			Collections.shuffle(lstYUnique);
			lst1 = new ArrayList<Double>(lstCategories.size());
			for(int i=0;i<lstCategories.size();i++){
				lst1.add(lstYUnique.get(mapIndices.get(i)));
			}
			return lst1;
		}
	}
}