package edu.ucsf.RegionalDiversityCalculator;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.ArrayListMultimap;

import edu.ucsf.base.ExtendedMath;

public class BetaDiversityRasterCell {

	/**Latitude**/
	private double dLatitude;
	
	/**Longitude**/
	private double dLongitude;
	
	/**ID**/
	private int iID;
	
	/**Values for each variable**/
	private ArrayListMultimap<String,Double> map1;
	
	public BetaDiversityRasterCell(double dLatitude, double dLongitude, int iID){
		this.dLatitude=dLatitude;
		this.dLongitude=dLongitude;
		this.iID=iID;
		map1 = ArrayListMultimap.create(20,100);
	}
	
	public void put(String sKey, double dValue){
		map1.put(sKey, dValue);
	}
	
	public boolean hasData(){
		if(map1.size()>0){
			return true;
		}else{
			return false;
		}
	}
	
	public HashMap<String,Double> getMeans(){
		
		//map1 = output
		
		HashMap<String,Double> mapOut;
		
		if(map1.size()==0){
			return null;
		}
		mapOut = new HashMap<String,Double>();
		for(String s:map1.keySet()){
			mapOut.put(s, ExtendedMath.mean(new ArrayList<Double>(map1.get(s))));
		}
		return mapOut;
	}
	
	public int samplingRegionCount(){
		for(String s:map1.keySet()){
			return map1.get(s).size();
		}
		return 0;
	}
	
	public double latitude(){
		return dLatitude;
	}
	
	public double longitude(){
		return dLongitude;
	}
	
	public int id(){
		return iID;
	}
	
	
}
