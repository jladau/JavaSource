package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Region0{

	/**Fips**/
	public String sFips;
	
	/**Starting time**/
	public int iStartTime;
	
	/**Ending time**/
	public int iEndTime;
	
	/**All times**/
	protected HashSet<Integer> setTimes;
	
	/**Estimates**/
	public Data datEst;
	
	/**Observed values**/
	public Data datObs;
	
	public Region0(String sFips, int iStartTime, int iEndTime, HashSet<Integer> setTimes) {
		initialize(sFips, iStartTime, iEndTime, setTimes);
	}
	
	public Region0(String sFips, int iStartTime, int iEndTime, ArrayList<Integer> lstTimes){
		initialize(sFips, iStartTime, iEndTime, new HashSet<Integer>(lstTimes));
	}
		
	public Region0(Region0 rgn1) {
		this.sFips = rgn1.sFips;
		this.iStartTime = rgn1.iStartTime;
		this.iEndTime = rgn1.iEndTime;	
		setTimes = new HashSet<Integer>(rgn1.setTimes);
		datEst = rgn1.datEst;
		datObs = rgn1.datObs;
	}
	
	private void initialize(String sFips, int iStartTime, int iEndTime, HashSet<Integer> setTimes) {
		
		this.sFips = sFips;
		this.iStartTime = iStartTime;
		this.iEndTime = iEndTime;
		
		//*************************		
		this.setTimes = new HashSet<Integer>(setTimes);
		//setTimes = new HashSet<Integer>(iEndTime-iStartTime+1);
		//for(int i=iStartTime;i<=iEndTime;i++){
		//	setTimes.add(i);
		//}
		//*************************
		
		datEst = new Data();
		datObs = new Data();
	}

	public void put(String sVariable, int iTime, double dValue, String sType){
		if(sType.equals("estimate")){
			datEst.put(sVariable,iTime,dValue);
		}else if(sType.equals("observation")){
			datObs.put(sVariable,iTime,dValue);
		}
	}
	
	public void putSum(String sVariable, int iTime, double dValue, String sType){
		if(sType.equals("estimate")){
			datEst.putSum(sVariable,iTime,dValue);
		}else if(sType.equals("observation")){
			datObs.putSum(sVariable,iTime,dValue);
		}
	}
	
	public double get(String sVariable, int iTime, String sType){
		if(sType.equals("estimate")){
			return datEst.get(sVariable,iTime);
		}else if(sType.equals("observation")){
			return datObs.get(sVariable,iTime);
		}else{
			return Double.NaN;
		}
	}
	
	@Deprecated
	public HashMap<Integer,Double> infectionFatalityRates(){
		
		//d2 = current sum
		//i1 = current count
		//map1 = output
		
		double d2;
		int i1;
		HashMap<Integer,Double> map1;
		
		map1 = new HashMap<Integer,Double>(1);
		d2 = 0;
		i1 = 0;
		for(Integer iTime:times()){
			if(this.get("cases_total",iTime,"estimate")>0){
				d2+=this.get("mortality",iTime,"observation")/this.get("cases_total",iTime,"estimate");
				i1++;
			}
		}
		map1.put(iStartTime,d2/((double) i1));
		return map1;
	}
	
	public HashMap<Integer,Double> infectionFatalityRates(int iWindowSize){
		
		//d1 = current estimate
		//mapC = map from start times to number of total cases within windows
		//mapM = map from start times to total mortality within windows
		//map1 = output
		
		double d1;
		HashMap<Integer,Double> mapC;
		HashMap<Integer,Double> mapM;
		HashMap<Integer,Double> map1;
		
		mapC = this.sumsWithinWindows("cases_total","estimate",iWindowSize);
		mapM = this.sumsWithinWindows("mortality","observation",iWindowSize);
		map1 = new HashMap<Integer,Double>(mapC.size());
		for(Integer i:mapC.keySet()){
			if(mapC.get(i)>0){
				d1 = mapM.get(i)/mapC.get(i);
			}else {
				d1 = Double.NaN;
			}
			map1.put(i,d1);
		}
		return map1;
	}
	
	public HashMap<Integer,Double> casesTotal(int iWindowSize){
		
		//d1 = current estimate
		//mapC = map from start times to number of total cases within windows
		//map1 = output
		
		double d1;
		HashMap<Integer,Double> mapC;
		HashMap<Integer,Double> map1;
		
		mapC = this.sumsWithinWindows("cases_total","estimate",iWindowSize);
		map1 = new HashMap<Integer,Double>(mapC.size());
		for(Integer i:mapC.keySet()){
			d1 = mapC.get(i);
			if(d1>=this.get("population",i,"observation")){
				d1=this.get("population",i,"observation");
			}
			map1.put(i,d1);
		}
		return map1;
	}
	
	
	public HashMap<Integer,Double> fractionPopulationInfected(int iWindowSize){
		
		//d1 = current estimate
		//mapC = map from start times to number of total cases within windows
		//map1 = output
		
		double d1;
		HashMap<Integer,Double> mapC;
		HashMap<Integer,Double> map1;
		
		mapC = this.sumsWithinWindows("cases_total","estimate",iWindowSize);
		map1 = new HashMap<Integer,Double>(mapC.size());
		for(Integer i:mapC.keySet()){
			d1 = mapC.get(i)/this.get("population",i,"observation");
			if(d1>=1){
				d1=1;
			}
			map1.put(i,d1);
		}
		return map1;
	}
	
	public HashMap<Integer,Double> fractionCasesObserved(int iWindowSize){
		
		//d1 = current estimate
		//mapC = map from start times to number of total cases within windows
		//mapK = map from start times to number of observed cases within windows
		//map1 = output
		
		double d1;
		HashMap<Integer,Double> mapC;
		HashMap<Integer,Double> mapK;
		HashMap<Integer,Double> map1;
		
		mapC = this.sumsWithinWindows("cases_total","estimate",iWindowSize);
		mapK = this.sumsWithinWindows("cases_observed","observation",iWindowSize);
		map1 = new HashMap<Integer,Double>(mapC.size());
		for(Integer i:mapC.keySet()){
			if(mapC.get(i)>0){
				d1 = mapK.get(i)/mapC.get(i);
			}else {
				d1 = Double.NaN;
			}
			map1.put(i,d1);
		}
		return map1;
	}
	
	private HashMap<Integer,Double> sumsWithinWindows(String sVariable, String sType, int iWindowSize){
		
		//d1 = current sum
		//lstTimes = list of times in ascending order
		//map1 = output
		//i1 = start time
		
		int i1;
		HashMap<Integer,Double> map1;
		double d1;
		ArrayList<Integer> lstTimes;
		
		lstTimes = new ArrayList<Integer>(times());
		Collections.sort(lstTimes);
		map1 = new HashMap<Integer,Double>(lstTimes.size());
		if(iWindowSize<1){
			d1 = 0.;
			for(Integer iTime:lstTimes){
				d1 += this.get(sVariable,iTime,sType);
			}
			if(sVariable.equals("cases_total")){
				if(d1>this.get("population",iStartTime,"observation")){
					d1 = this.get("population",iStartTime,"observation");
				}
			}
			
			map1.put(iStartTime,d1);
		}else {
			for(Integer iTime:lstTimes){
				d1 = 0;
				i1 = iTime+iWindowSize-1;
				if(iEndTime<i1) {
					break;
				}
				for(int i=iTime;i<=i1;i++){
					d1 += this.get(sVariable,iTime,sType);
				}
				if(sVariable.equals("cases_total")){
					if(d1>this.get("population",iStartTime,"observation")){
						d1 = this.get("population",iStartTime,"observation");
					}
				}				
				map1.put(iTime,d1);
			}
		}
		return map1;
	}
	
	public void reset(String sVariable, int iTime, String sType){
		if(sType.equals("estimate")){
			datEst.reset(sVariable,iTime);
		}else if(sType.equals("observation")){
			datObs.reset(sVariable,iTime);
		}
	}
	
	public boolean contains(String sVariable, int iTime, String sType){
		if(sType.equals("estimate")){
			return datEst.contains(sVariable,iTime);
		}else if(sType.equals("observation")){
			return datObs.contains(sVariable,iTime);
		}else{
			return false;
		}
	}
	
	//public Data data(int iTime){
	//	return mapData.get(iTime);
	//}
	
	public HashSet<Integer> times(){
		return setTimes;
	}
	
	public class DataPoint{
		
		/**Mortality**/
		public double dM=Double.NaN;
		
		/**Mortality cumulative**/
		public double dMCumulative=Double.NaN;
		
		/**Mortality cumulative per capita**/
		public double dL=Double.NaN;
		
		/**Observed cases**/
		public double dK=Double.NaN;
		
		/**Total cases**/
		public double dC=Double.NaN;

		/**Infection fatality rate**/
		public double dPhi=Double.NaN;
		
		/**Fraction of cases observed**/
		public double dPsi=Double.NaN;
		
		/**Tests**/
		public double dT=Double.NaN;

		/**Population**/
		public double dP=Double.NaN;
		
		/**Fraction population infected**/
		public double dF=Double.NaN;
		
		/**Odds ratio of testing an infected versus uninfected person**/
		public double dW=Double.NaN;
		
		/**Tests predictor slope term: P^2/M+1*(1-(1-K/P)^(1/W))**/
		public double dH=Double.NaN;
		
		/**Tests predictor constant term: K - P*(1-(1-K/P)^(1/W)**/
		public double dI=Double.NaN;
		
		/**Probability of at least one test**/
		public double dPrTests=Double.NaN;
		
		/**Population coefficient in model T = nu_0 * (C+1)^nu_c * P^nu_P (conditional on T>0)**/
		public double dNuP=Double.NaN;
		
		/**Observed cases coefficient in model T = nu_0 * (C+1)^nu_c * P^nu_P (conditional on T>0)**/
		public double dNuC=Double.NaN;
		
		/**Intercept coefficient in model T = nu_0 * (C+1)^nu_c * P^nu_P (conditional on T>0)**/
		public double dNu0=Double.NaN;
		
		public DataPoint(){	
		}	
	}
	
	public class Data{

		/**Map from times to data**/
		private HashMap<Integer,DataPoint> mapData;
		
		public Data(){
			mapData = new HashMap<Integer,DataPoint>(365);
		}
		
		public void put(String sVariable, int iTime, double dValue){
			
			if(iTime<iStartTime || iEndTime<iEndTime){
				return;
			}
			
			if(!mapData.containsKey(iTime)){
				mapData.put(iTime,new DataPoint());
			}
			if(sVariable.equals("mortality")){
				mapData.get(iTime).dM = dValue;
			}else if(sVariable.equals("mortality_cumulative")){
				mapData.get(iTime).dMCumulative = dValue;
			}else if(sVariable.equals("mortality_cumulative_per_capita")){
				mapData.get(iTime).dL = dValue;
			}else if(sVariable.equals("cases_observed")){
				mapData.get(iTime).dK = dValue;
			}else if(sVariable.equals("cases_total")){
				mapData.get(iTime).dC = dValue;
			}else if(sVariable.equals("infection_fatality_rate")){
				mapData.get(iTime).dPhi = dValue;
			}else if(sVariable.equals("tests")){
				mapData.get(iTime).dT = dValue;
			}else if(sVariable.equals("population")){
				mapData.get(iTime).dP = dValue;
			}else if(sVariable.equals("odds_ratio")){
				mapData.get(iTime).dW = dValue;
			}else if(sVariable.equals("fraction_cases_observed")){
				mapData.get(iTime).dPsi = dValue;
			}else if(sVariable.equals("fraction_population_infected")){
				mapData.get(iTime).dF = dValue;
			}else if(sVariable.equals("tests_predictor_slope")){
				mapData.get(iTime).dH = dValue;
			}else if(sVariable.equals("tests_predictor_constant")){
				mapData.get(iTime).dI = dValue;
			}else if(sVariable.equals("nu_0")){
				mapData.get(iTime).dNu0 = dValue;
			}else if(sVariable.equals("nu_c")){
				mapData.get(iTime).dNuC = dValue;
			}else if(sVariable.equals("nu_p")){
				mapData.get(iTime).dNuP = dValue;
			}else if(sVariable.equals("probability_nonzero_tests")){
				mapData.get(iTime).dPrTests = dValue;
			}
		}
		
		public void putSum(String sVariable, int iTime, double dValue){
			
			if(iTime<iStartTime || iEndTime<iEndTime){
				return;
			}
			
			if(!mapData.containsKey(iTime)){
				mapData.put(iTime,new DataPoint());
			}
			if(sVariable.equals("mortality")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dM += dValue;
			}else if(sVariable.equals("mortality_cumulative")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dMCumulative += dValue;
			}else if(sVariable.equals("mortality_cumulative_per_capita")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dL += dValue;
			}else if(sVariable.equals("cases_observed")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dK += dValue;
			}else if(sVariable.equals("cases_total")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dC += dValue;
			}else if(sVariable.equals("tests")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dT += dValue;
			}else if(sVariable.equals("population")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dP += dValue;
			}else if(sVariable.equals("tests_predictor_constant")){
				initializeZero(sVariable,iTime);
				mapData.get(iTime).dI += dValue;
			}
		}
		
		private void initializeZero(String sVariable, int iTime){
			if(Double.isNaN(get(sVariable,iTime))){
				put(sVariable,iTime,0);
			}
		}
		
		public double get(String sVariable, int iTime){
			
			if(!mapData.containsKey(iTime)){
				return Double.NaN;
			}
			
			if(iTime<iStartTime || iEndTime<iEndTime){
				return Double.NaN;
			}
			
			if(sVariable.equals("mortality")){
				return mapData.get(iTime).dM;
			}else if(sVariable.equals("mortality_cumulative")){
				return mapData.get(iTime).dMCumulative;
			}else if(sVariable.equals("mortality_cumulative_per_capita")){
				return mapData.get(iTime).dL;
			}else if(sVariable.equals("cases_observed")){
				return mapData.get(iTime).dK;
			}else if(sVariable.equals("cases_total")){
				return mapData.get(iTime).dC;
			}else if(sVariable.equals("infection_fatality_rate")){
				return mapData.get(iTime).dPhi;
			}else if(sVariable.equals("tests")){
				return mapData.get(iTime).dT;
			}else if(sVariable.equals("population")){
				return mapData.get(iTime).dP;
			}else if(sVariable.equals("odds_ratio")){
				return mapData.get(iTime).dW;
			}else if(sVariable.equals("fraction_cases_observed")){
				return mapData.get(iTime).dPsi;
			}else if(sVariable.equals("fraction_population_infected")) {
				return mapData.get(iTime).dF;	
			}else if(sVariable.equals("tests_predictor_slope")) {
				return mapData.get(iTime).dH;	
			}else if(sVariable.equals("tests_predictor_constant")) {
				return mapData.get(iTime).dI;	
			}else if(sVariable.equals("nu_0")){
				return mapData.get(iTime).dNu0;
			}else if(sVariable.equals("nu_c")){
				return mapData.get(iTime).dNuC;
			}else if(sVariable.equals("nu_p")){
				return mapData.get(iTime).dNuP;
			}else if(sVariable.equals("probability_nonzero_tests")){
				return mapData.get(iTime).dPrTests;
			}else{
				return Double.NaN;
			}
		}
		
		public void reset(String sVariable, int iTime){
			
			if(!mapData.containsKey(iTime)){
				return;
			}
			
			if(iTime<iStartTime || iEndTime<iEndTime){
				return;
			}
			
			if(sVariable.equals("mortality")){
				mapData.get(iTime).dM = Double.NaN;
			}else if(sVariable.equals("mortality_cumulative")){
				mapData.get(iTime).dMCumulative = Double.NaN;
			}else if(sVariable.equals("mortality_cumulative_per_capita")){
				mapData.get(iTime).dL = Double.NaN;
			}else if(sVariable.equals("cases_observed")){
				mapData.get(iTime).dK = Double.NaN;
			}else if(sVariable.equals("cases_total")){
				mapData.get(iTime).dC = Double.NaN;
			}else if(sVariable.equals("infection_fatality_rate")){
				mapData.get(iTime).dPhi = Double.NaN;
			}else if(sVariable.equals("tests")){
				mapData.get(iTime).dT = Double.NaN;
			}else if(sVariable.equals("population")){
				mapData.get(iTime).dP = Double.NaN;
			}else if(sVariable.equals("odds_ratio")){
				mapData.get(iTime).dW = Double.NaN;
			}else if(sVariable.equals("fraction_cases_observed")){
				mapData.get(iTime).dPsi = Double.NaN;
			}else if(sVariable.equals("fraction_population_infected")){
				mapData.get(iTime).dF = Double.NaN;
			}else if(sVariable.equals("tests_predictor_slope")){
				mapData.get(iTime).dH = Double.NaN;
			}else if(sVariable.equals("tests_predictor_constant")){
				mapData.get(iTime).dI = Double.NaN;
			}else if(sVariable.equals("nu_0")){
				mapData.get(iTime).dNu0 = Double.NaN;
			}else if(sVariable.equals("nu_c")){
				mapData.get(iTime).dNuC = Double.NaN;
			}else if(sVariable.equals("nu_p")){
				mapData.get(iTime).dNuP = Double.NaN;
			}else if(sVariable.equals("probability_nonzero_tests")){
				mapData.get(iTime).dPrTests = Double.NaN;
			}
		}
		
		public boolean contains(String sVariable, int iTime){
			if(Double.isNaN(get(sVariable,iTime))){
				return false;
			}else{
				return true;
			}
		}
	}
}