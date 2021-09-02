package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class State extends Region{

	/**Alpha value**/
	public double dAlphaTime;
	
	/**Counties within state (keys are fips)**/
	private HashMap<String,County> mapCounties;
	
	public State(String sFips, int iStartTime, int iEndTime, ArrayList<Integer> lstTimes){
		super(sFips, iStartTime, iEndTime, lstTimes);
		mapCounties = new HashMap<String,County>(300);
	}

	public State(String sFips, int iStartTime, int iEndTime, HashSet<Integer> setTimes){
		super(sFips, iStartTime, iEndTime, setTimes);
		mapCounties = new HashMap<String,County>(300);
	}
	
	public HashMap<String,County> countyMap(){
		return mapCounties;
	}
	
	public void putSum(String sCountyFips, String sVariable, int iTime, double dValue, String sType){
		if(!mapCounties.containsKey(sCountyFips)) {
			mapCounties.put(sCountyFips,new County(sCountyFips, iStartTime, iEndTime, setTimes));
		}
		mapCounties.get(sCountyFips).putSum(sVariable, iTime, dValue, sType);
	}
	
	public void put(String sCountyFips, String sVariable, int iTime, double dValue, String sType){

		//updating county variable
		if(!mapCounties.containsKey(sCountyFips)) {
			mapCounties.put(sCountyFips,new County(sCountyFips, iStartTime, iEndTime, setTimes));
		}
		mapCounties.get(sCountyFips).put(sVariable,iTime,dValue,sType);
	}
	
	public HashSet<County> counties(){
		return new HashSet<County>(mapCounties.values());
	}	
	
	public ArrayList<String> countyFips(){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = new ArrayList<String>(mapCounties.keySet());
		for(County cty1:counties()){
			lst1.add(cty1.sFips);
		}
		return lst1;
	}
	
	public County county(String sCountyFips){
		return mapCounties.get(sCountyFips);
	}
	

}