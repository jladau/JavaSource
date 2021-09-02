package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import edu.ucsf.io.DataIO;

public class Country0 extends Region{

	/**Map from state names to states**/
	public HashMap<String,State> mapStates;
	
	public Country0(int iStartTime, int iEndTime, ArrayList<Integer> lstTimes){
		super(null, iStartTime, iEndTime, lstTimes);
	}
	
	public Country0(Country0 ctr1){
		super(ctr1);
		this.mapStates = new HashMap<String,State>(ctr1.stateMap());
	}
	
	public HashMap<String,State> stateMap(){
		return mapStates;
	}
	
	public void putSum(String sStateFips, String sVariable, int iTime, double dValue, String sType){
		
		//updating state variable
		if(!mapStates.containsKey(sStateFips)) {
			mapStates.put(sStateFips,new State(sStateFips, iStartTime, iEndTime, setTimes));
		}
		mapStates.get(sStateFips).putSum(sVariable,iTime,dValue,sType);
	}

	public void putSum(String sCountyFips, String sStateFips, String sVariable, int iTime, double dValue, String sType){
		
		//updating state variable
		if(!mapStates.containsKey(sStateFips)) {
			mapStates.put(sStateFips,new State(sStateFips, iStartTime, iEndTime, setTimes));
		}
		mapStates.get(sStateFips).putSum(sCountyFips,sVariable,iTime,dValue,sType);
	}
	
	public void put(String sStateFips, String sVariable, int iTime, double dValue, String sType){

		//updating state variable
		if(!mapStates.containsKey(sStateFips)) {
			mapStates.put(sStateFips,new State(sStateFips, iStartTime, iEndTime, setTimes));
		}
		mapStates.get(sStateFips).put(sVariable,iTime,dValue,sType);
	}
	
	public void put(String sCountyFips, String sStateFips, String sVariable, int iTime, double dValue, String sType){
		
		//updating state variable
		if(!mapStates.containsKey(sStateFips)) {
			mapStates.put(sStateFips, new State(sStateFips, iStartTime, iEndTime, setTimes));
		}
		mapStates.get(sStateFips).put(sCountyFips, sVariable,iTime,dValue, sType);
	}
	
	
	//TODO have column in input data file listing type -- observation or estimate?
	public void loadData(DataIO dat1){
		
		//sStateFips = current state fips
		//sCountyFips = current county fips
		//sVariable = variable
		//iTime = time
		//dValue = value
		//setVarsCounty = set of variables
		//setVarsState = set of variables
		//setVarsCountry = set of variables
		
		String sCountyFips;
		String sStateFips;
		String sVariable;
		int iTime;
		double dValue;
		HashSet<String> setVarsCounty;
		HashSet<String> setVarsCountry;
		HashSet<String> setVarsState;
		
		datEst = new Data();
		datObs = new Data();
		mapStates = new HashMap<String,State>(50);
		setTimes = new HashSet<Integer>(setTimes.size());
		setVarsCountry = new HashSet<String>(10);
		setVarsState = new HashSet<String>(10);
		setVarsCounty = new HashSet<String>(10);

		//********************************
		for(int i=1;i<dat1.iRows;i++){
			sVariable = dat1.getString(i,"VARIABLE");
			if(!sVariable.equals("population")){
				iTime =  dat1.getInteger(i,"TIME");
				if(iStartTime<=iTime && iTime<=iEndTime){
					setTimes.add(iTime);
				}
			}
		}
		//********************************
		
		for(int i=1;i<dat1.iRows;i++){
			
			sStateFips = dat1.getString(i,"STATE_FIPS");
			sCountyFips = dat1.getString(i,"COUNTY_FIPS");
			sVariable = dat1.getString(i,"VARIABLE");
			if(!sVariable.equals("population")){
				iTime =  dat1.getInteger(i,"TIME");
				
				//********************************
				if(iStartTime<=iTime && iTime<=iEndTime){
					setTimes.add(iTime);
				}
				//********************************				
				
			}else{
				iTime = iStartTime;
			}
			dValue = dat1.getDouble(i,"VALUE");
			
			//county-level variable
			if(!sCountyFips.equals("NA")){
				
				//************************
				//if(sCountyFips.equals("06085")) {
				//	System.out.println(sVariable + "," + iTime + "," + dValue);
				//}
				//************************
				
				put(sCountyFips,sStateFips,sVariable,iTime,dValue,"observation");
				putSum(sStateFips,sVariable,iTime,dValue,"observation");
				putSum(sVariable,iTime,dValue,"observation");
				setVarsCounty.add(sVariable);
				setVarsState.add(sVariable);
				setVarsCountry.add(sVariable);
			}else{
				
				//state-level variable
				if(!sStateFips.equals("NA")){
					put(sStateFips,sVariable,iTime,dValue,"observation");
					putSum(sVariable,iTime,dValue,"observation");
					setVarsState.add(sVariable);
					setVarsCountry.add(sVariable);
					
				//country-level variable	
				}else{
					put(sVariable,iTime,dValue,"observation");
					setVarsCountry.add(sVariable);
				}
			}
		}
		for(String sVar:setVarsCountry){
			for(Integer i:times()){
				if(!this.contains(sVar,i,"observation")){
					this.put(sVar,i,0,"observation");
				}
			}
		}
		for(String sVar:setVarsState){
			for(Integer i:times()){
				for(State sta1:states()){
					if(!sta1.contains(sVar,i,"observation")){
						put(sta1.sFips,sVar,i,0,"observation");
					}
				}
			}
		}
		for(String sVar:setVarsCounty){
			for(Integer i:times()){
				for(State sta1:states()){
					for(County cty1:sta1.counties()){
						if(!cty1.contains(sVar,i,"observation")){
							put(cty1.sFips,sta1.sFips,sVar,i,0,"observation");
						}
					}
				}
			}
		}
		
		percolateOddsRatios();
		percolatePopulation();
		
	}
	
	public ArrayList<String> print(
			String rgsCountyVariables[], 
			String rgsStateVariables[], 
			String rgsCountryVariables[],
			String sType,
			boolean bHeader){
		
		//lstOut = output
		//d1 = value
		//set1 = set of times to consider
		
		ArrayList<String> lstOut;
		double d1;
		HashSet<Integer> set1;
		
		lstOut = new ArrayList<String>(setTimes.size()*(rgsCountyVariables.length+rgsStateVariables.length)+1);
		if(bHeader){	
			lstOut.add("COUNTY_FIPS,STATE_FIPS,VARIABLE,TYPE,TIME,VALUE");
		}
		
		//**************************
		//for(Integer i:setTimes){
		//	System.out.println(i);
		//}
		//**************************
		
		set1 = new HashSet<Integer>(setTimes);
		if(!setTimes.contains(iStartTime)){
			set1.add(iStartTime);
		}
		for(Integer iTime:set1){
			for(State sta1:states()){
				for(County cty1:sta1.counties()){
					
					//**************************
					//System.out.println("--------------------");
					//for(Integer i:cty1.times()){
					//	System.out.println(i);
					//}
					//**************************
					
					for(String sVar:rgsCountyVariables){	
						if(cty1.contains(sVar,iTime,sType)){
							d1 = cty1.get(sVar,iTime,sType);
							lstOut.add(cty1.sFips + "," + sta1.sFips + "," + sVar + "," + sType + "," + iTime + "," + d1);
						}
					}
				}
				for(String sVar:rgsStateVariables){
					if(sta1.contains(sVar,iTime,sType)){
						d1 = sta1.get(sVar,iTime,sType);
						lstOut.add("NA" + "," + sta1.sFips + "," + sVar + "," + sType + "," + iTime + "," + d1);
					}
				}
			}
			for(String sVar:rgsCountryVariables){
				if(this.contains(sVar,iTime,sType)){
					d1 = this.get(sVar,iTime,sType);
					lstOut.add("NA" + "," + "NA" + "," + sVar + "," + sType + "," + iTime + "," + d1);
				}
			}
		}
		return lstOut;
	}
	
	private void percolateOddsRatios(){
		if(contains("odds_ratio",iStartTime,"observation")){
			for(Integer iTime:times()){
				for(State sta1:states()){
					put(sta1.sFips,"odds_ratio",iTime,get("odds_ratio",iTime,"observation"),"observation");
					for(County cty1:sta1.counties()){
						put(cty1.sFips, sta1.sFips, "odds_ratio", iTime, get("odds_ratio",iTime,"observation"),"observation");
					}
				}
			}
		}
	}

	public void percolatePopulation(){
		if(contains("population",iStartTime,"observation")){
			for(Integer iTime:times()){
				put("population",iTime,get("population",iStartTime,"observation"),"observation");
				for(State sta1:states()){
					put(sta1.sFips, "population",iTime,sta1.get("population",iStartTime,"observation"),"observation");
					for(County cty1:sta1.counties()){
						put(cty1.sFips, sta1.sFips, "population", iTime, cty1.get("population",iStartTime,"observation"),"observation");
					}
				}
			}
		}
	}
	
	public HashSet<County> counties(){
		
		//setOut = output
		
		HashSet<County> setOut;
		
		setOut = new HashSet<County>(3500);
		for(State sta1:states()){
			setOut.addAll(mapStates.get(sta1.sFips).counties());
		}
		return setOut;
	}
	
	public HashSet<State> states(){
		return new HashSet<State>(mapStates.values());
	}
	
	public void putTests(String sFips, int iTests, int iTime, String sType){
		
		//sStateFips = state fips
		
		String sStateFips;
		
		if(sFips.length()==2){
			sStateFips=sFips;
			put(sStateFips,"tests",iTime,(double) iTests, sType);
			putSum("tests",iTime,(double) iTests, sType);
		}else if(sFips.length()==5){
			sStateFips = sFips.substring(0,2);
			put(sFips,sStateFips,"tests",iTime,(double) iTests, sType);
			putSum(sStateFips,"tests",iTime,(double) iTests, sType);
			putSum("tests",iTime,(double) iTests, sType);
		}
	}
}