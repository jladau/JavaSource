package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.Random;

public class Country_Simulation0 extends Country{

	public Country_Simulation0(int iStartTime, int iEndTime, ArrayList<Integer> lstTimes){
		super(iStartTime, iEndTime, lstTimes);
	}
	
	public Country_Simulation0(Country ctr1){
		super(ctr1); 
	}
	
	
	private void initialize(){
		
		
		
		
		
	}
	
	public void countryOmega(){
		
		//iMaxTime = maximum time
		//iMinTime = minimum time
		//dRange = time range
		//d1 = current value
		
		double dRange;
		double d1;
		int iMaxTime;
		int iMinTime;
		
		iMaxTime = -Integer.MAX_VALUE;
		iMinTime = Integer.MAX_VALUE;
		for(Integer iTime:times()) {
			if(iTime>iMaxTime){
				iMaxTime = iTime;
			}
			if(iTime<iMinTime){
				iMinTime = iTime;
			}
		}
		dRange = (double) (iMaxTime-iMinTime);
		for(Integer iTime:times()){
			reset("odds_ratio",iTime,"observation");
			//put("odds_ratio",iTime,1000-Math.abs(iTime-2458909),"observation");
			d1 = (double) (iTime - iMinTime+1);
			put("odds_ratio",iTime,dRange/Math.sqrt(d1+10),"observation");
		}
	}
	
	public void countryObservedCases(int iRandomSeed, String sOddsRatioType){
		
		//i1 = observed number of cases
		//dPr = current probability of case
		//iTRemaining = number of tests remaining
		//dCRemaining = total cases remaining
		//dNotCRemaining = total non-cases remaining
		//dW = odds ratio
		//rnd1 = random number generator
		
		int i1;
		int iTRemaining;
		double dPr;
		double dW;
		double dCRemaining;
		double dNotCRemaining;
		Random rnd1;
		
		for(Integer iTime:times()){
			i1 = 0;		
			rnd1 = new Random(iRandomSeed);
			iTRemaining = (int) get("tests",iTime,"observation");
			dCRemaining = get("cases_total",iTime,"estimate");
			dNotCRemaining = get("population", iTime, "observation") -  dCRemaining;
			dW = get("odds_ratio", iTime, sOddsRatioType);
	
			while(iTRemaining>0){
				dPr = dW*dCRemaining/(dW*dCRemaining+dNotCRemaining);
				if(rnd1.nextDouble()<dPr){
					i1++;
					dCRemaining--;
				}else {
					dNotCRemaining--;
				}
				iTRemaining--;
			}
			put("cases_observed",iTime,(double) i1,"observation");
		}
	}
	
	public void stateCases(int iRandomSeed, String sOddsRatioType){
		
		//rnd1 = random number generator
		
		Random rnd1;
		
		rnd1 = new Random(iRandomSeed);
		for(Integer iTime:times()){
			reset("cases_observed",iTime,"observation");
			reset("cases_total",iTime,"observation");
			for(State sta1:states()){
				stateCases(sta1,iTime,rnd1,sOddsRatioType);
			}
		}
	}
	
	public void countyCases(int iRandomSeed, String sOddsRatioType){
		
		//rnd1 = random number generator
		
		Random rnd1;
		
		rnd1 = new Random(iRandomSeed);
		for(Integer iTime:times()){
			reset("cases_observed",iTime,"observation");
			reset("cases_total",iTime,"observation");
			for(State sta1:states()){
				sta1.reset("cases_observed",iTime,"observation");
				sta1.reset("cases_total",iTime,"observation");
			}
		}
		for(Integer iTime:times()){
			for(State sta1:states()){
				for(County cty1:sta1.counties()) {
					countyCases(cty1,sta1,iTime,rnd1,sOddsRatioType);
				}
			}
		}
	}
	
	private void countyCases(County cty1, State sta1, int iTime, Random rnd1, String sOddsRatioType){
		
		//i1 = observed number of cases
		//dPr = current probability of case
		//iTRemaining = number of tests remaining
		//dCRemaining = total cases remaining
		//dNotCRemaining = total non-cases remaining
		//dW = odds ratio
		//d1 = total number of cases (simulated)
		
		double d1;
		int i1;
		int iTRemaining;
		double dPr;
		double dW;
		double dCRemaining;
		double dNotCRemaining;
		
		d1 = Math.round(0.1*Math.pow(rnd1.nextDouble(),4)*cty1.get("population", iTime, "observation"));
		put(cty1.sFips,sta1.sFips,"cases_total",iTime,d1,"observation");
		putSum(sta1.sFips,"cases_total",iTime,d1,"observation");
		putSum("cases_total", iTime, d1, "observation");
		i1 = 0;		
		iTRemaining = (int) cty1.get("tests",iTime,"estimate");
		dCRemaining = cty1.get("cases_total",iTime,"observation");
		dNotCRemaining = cty1.get("population", iTime, "observation") -  dCRemaining;
		dW = cty1.get("odds_ratio", iTime, sOddsRatioType);

		while(iTRemaining>0){
			dPr = dW*dCRemaining/(dW*dCRemaining+dNotCRemaining);
			if(rnd1.nextDouble()<dPr){
				i1++;
				dCRemaining--;
			}else {
				dNotCRemaining--;
			}
			iTRemaining--;
		}
		put(cty1.sFips,sta1.sFips,"cases_observed",iTime,(double) i1,"observation");
		putSum(sta1.sFips,"cases_observed",iTime,(double) i1,"observation");
		putSum("cases_observed",iTime,(double) i1, "observation");
	}
	
	
	private void stateCases(State sta1, int iTime, Random rnd1, String sOddsRatioType){
		
		//i1 = observed number of cases
		//dPr = current probability of case
		//iTRemaining = number of tests remaining
		//dCRemaining = total cases remaining
		//dNotCRemaining = total non-cases remaining
		//dW = odds ratio
		//d1 = total number of cases (simulated)
		
		double d1;
		int i1;
		int iTRemaining;
		double dPr;
		double dW;
		double dCRemaining;
		double dNotCRemaining;
		
		d1 = Math.round(0.1*Math.pow(rnd1.nextDouble(),4)*sta1.get("population", iTime, "observation"));
		put(sta1.sFips,"cases_total",iTime,d1,"observation");
		i1 = 0;		
		iTRemaining = (int) sta1.get("tests",iTime,"observation");
		dCRemaining = sta1.get("cases_total",iTime,"observation");
		dNotCRemaining = sta1.get("population", iTime, "observation") -  dCRemaining;
		dW = sta1.get("odds_ratio", iTime, sOddsRatioType);

		while(iTRemaining>0){
			dPr = dW*dCRemaining/(dW*dCRemaining+dNotCRemaining);
			if(rnd1.nextDouble()<dPr){
				i1++;
				dCRemaining--;
			}else {
				dNotCRemaining--;
			}
			iTRemaining--;
		}
		put(sta1.sFips,"cases_observed",iTime,(double) i1,"observation");
		putSum("cases_observed",iTime,(double) i1, "observation");
	}
	
	public void setAlphaTime(State sta1, double dAlphaTime){
		mapStates.get(sta1.sFips).dAlphaTime =  dAlphaTime;
	}

	/*
	public void simulateObservedCasesPerCounty(int iRandomSeed){
		
		//i1 = counter
		
		int i1;
		
		i1 = 0;
		for(String sStateFips:this.mapStates.keySet()){
			mapStates.get(sStateFips).simulateObservedCasesPerCounty(iRandomSeed + i1);
			i1+=17;
		}
		for(Integer iTime:setTimes){
			reset("cases_observed",iTime,"observation");
			for(State_Simulation sta1:statesSimulated()){
				putSum("cases_observed",iTime,sta1.get("cases_total",iTime),"observation");
			}
		}
	}

	public void simulateTotalCasesPerCounty(double dVariance, int iRandomSeed){
		
		//TODO need to offset time for cases and mortality
		
		for(State_Simulation sta1:statesSimulated()){
			sta1.simulateTotalCasesPerCounty(dVariance,iRandomSeed);
		}
		for(Integer iTime:setTimes){
			reset("cases_total",iTime);
			for(State_Simulation sta1:statesSimulated()){
				putSum("cases_total",iTime,sta1.get("cases_total",iTime));
			}
		}
	}

	public void distributeTestsRandomlyAmongCounties(Random rnd1){
		for(String s:this.mapStates.keySet()) {
			mapStates.get(s).distributeTestsRandomlyAmongCounties(rnd1);
		}
	}

	public void setAlphaCounty(County cty1, double dAlphaCounty){
		
		//sStateFips = state fips
		
		String sStateFips;
		
		sStateFips = cty1.sFips.substring(0,2);
		mapStates.get(sStateFips).mapCounties.get(cty1.sFips).dAlphaCounty =  dAlphaCounty;
	}
	*/
	
	/*
	public void simulateObservedCasesPerCounty(int iRandomSeed){
		for(Integer iTime:times()){
			reset("cases_observed",iTime,"observation");
			for(County cty1:counties()){
				simulateObservedCasesPerCounty(cty1,iTime,iRandomSeed+19*iRandomSeed);
			}
		}
	}
	
	private void simulateObservedCasesPerCounty(County cty1, int iTime, int iRandomSeed){
		
		//i1 = observed number of cases
		//dPr = current probability of case
		//iTRemaining = number of tests remaining
		//dCRemaining = total cases remaining
		//dNotCRemaining = total non-cases remaining
		//dW = odds ratio
		//rnd1 = random number generator
		
		int i1;
		int iTRemaining;
		double dPr;
		double dW;
		double dCRemaining;
		double dNotCRemaining;
		Random rnd1;
		
		i1 = 0;
		iTRemaining = (int) cty1.get("tests",iTime);
		dCRemaining = cty1.get("cases_total",iTime);
		dNotCRemaining = cty1.get("population", iTime,"observation") -  dCRemaining;
		dW = cty1.get("odds_ratio", iTime);
		rnd1 = new Random(iRandomSeed);
		while(iTRemaining>0){
			dPr = dW*dCRemaining/(dW*dCRemaining+dNotCRemaining);
			if(rnd1.nextDouble()<dPr){
				i1++;
				dCRemaining--;
			}else {
				dNotCRemaining--;
			}
			iTRemaining--;
		}
		put(cty1.sFips,"cases_observed",iTime,(double) i1,"observation");
		putSum("cases_observed",iTime,(double) i1,"observation");
	}
	
	public void distributeTestsRandomlyAmongCounties(Random rnd1){
		for(Integer iTime:times()){
			distributeTestsRandomlyAmongCounties(iTime,rnd1);
		}
	}
	
	private void distributeTestsRandomlyAmongCounties(int iTime, Random rnd1){
		
		//i1 = number of tests remaining
		//i2 = current number of tests being distributed
		
		int i1;
		int i2;
		
		i1 = (int) Math.round(get("tests",iTime));
		reset("tests",iTime);
		for(County cty1: this.counties()){
			i2 = (int) Math.ceil(Math.max(0,cty1.get("cases_observed",iTime,"observation")));
			i2 = (int) Math.ceil(Math.max(i2,cty1.get("mortality",iTime,"observation")));
			put(cty1.sFips,"tests", iTime, (double) i2);
			i1-=i2;
		}
		while(i1>0){
			for(County cty1:this.counties()){
				i2 = (int) (rnd1.nextDouble()*0.2*i1 + cty1.get("tests",iTime));
				put(cty1.sFips,"tests", iTime, (double) i2);
				i1-=i2;
				if(i1<=0){
					break;
				}
			}
		}
		for(County cty1:this.counties()){
			putSum("tests",iTime,cty1.get("tests",iTime));
		}
	}
	
	public void simulateTotalCasesPerCounty(double dVariance, int iRandomSeed){
		
		//d1 = current normal variate
		//d2 = current value
		
		double d1;
		double d2;
		
		for(Integer iTime:times()){
			reset("cases_total",iTime);
			for(County cty1:counties()){
				d2 = Math.pow(dAlphaTime, (double)(iTime-this.dStartTime))*cty1.dAlphaCounty+1;
				d1 = ExtendedMath.normalRandomVector(0,dVariance,1)[0];
				while(d2+d1<1){
					d1 = ExtendedMath.normalRandomVector(0,dVariance,1)[0];
				}
				d2=cty1.get("mortality",iTime,"observation")*(d2+d1);
				put(cty1.sFips,"cases_total",iTime,d2);
				putSum("cases_total",iTime,d2);
			}
		}
	}
	*/
}