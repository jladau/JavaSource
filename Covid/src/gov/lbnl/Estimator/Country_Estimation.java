package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.LinearModel;

public class Country_Estimation extends Country{

	private static final double INFECTION_FATALITY_RATE=0.005;
	
	public Country_Estimation(int iStartTime, int iEndTime, ArrayList<Integer> lstTimes){
		super(iStartTime, iEndTime, lstTimes);
		for(int iTime:times()){	
			this.put("infection_fatality_rate",iTime,INFECTION_FATALITY_RATE,"observed");
		}
	}
	
	public Country_Estimation(Country ctr1){
		super(ctr1);
		for(int iTime:times()){	
			this.put("infection_fatality_rate",iTime,INFECTION_FATALITY_RATE,"observed");
		}
	}
	
	public void initialize() {
		countryCasesTotal();
		allOddsRatios();
		stateCasesTotal("estimate");
	}
	
	public void allOddsRatios(){
		
		//d1 = current estimate
		//dK = observed cases
		//dC = total cases
		//dT = tests
		//dP = population
		
		double d1;
		double dK;
		double dC;
		double dT;
		double dP;
	
		for(Integer iTime:setTimes){
			dK = this.get("cases_observed",iTime,"observation");
			dC = this.get("cases_total",iTime,"estimate");
			dT = this.get("tests",iTime,"observation");
			dP = this.get("population",iTime,"observation");
			d1 = Math.log(1.-dK/dC)/Math.log(1.-(dT-dK)/(dP-dC));		
			if(Double.isNaN(d1)){
				d1=0;
			}
			put("odds_ratio",iTime,d1,"estimate");
			for(State sta1:states()){
				this.put(sta1.sFips,"odds_ratio",iTime,d1,"estimate");
				for(County cty1:sta1.counties()){
					this.put(cty1.sFips,sta1.sFips,"odds_ratio",iTime,d1,"estimate");
				}
			}
		}
	}

	public void countryCasesTotal(){
		for(int iTime:times()){
			this.put("cases_total",iTime,this.get("mortality",iTime,"observation")/INFECTION_FATALITY_RATE,"estimate");
		}	
	}
	
	public void countryFractionPopulationInfected(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		map1 = this.fractionPopulationInfected(iWindowSize);
		for(Integer i:map1.keySet()){
			this.put("fraction_population_infected",i,map1.get(i),"estimate");
		}
	}
	
	public void countryFractionCasesObserved(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		map1 = this.fractionCasesObserved(iWindowSize);
		for(Integer i:map1.keySet()){
			this.put("fraction_cases_observed",i,map1.get(i),"estimate");
		}
	}

	public void countyCasesTotal(String sOddsRatioType){
		
		//d1 = current estimate
		
		double d1;
		
		for(Integer iTime:setTimes){
			reset("cases_total",iTime,"estimate");
			for(State sta1:states()){
				for(County cty1:sta1.counties()) {
					d1 = regionCasesTotal(cty1,iTime,sOddsRatioType,"observation");
					this.put(cty1.sFips, sta1.sFips, "cases_total", iTime, d1, "estimate");
					this.putSum("cases_total",iTime,d1,"estimate");
				}
			}
		}
	}
	
	public void stateCasesTotal(String sOddsRatioType){
		
		//d1 = current estimate
		
		double d1;
		
		for(Integer iTime:setTimes){
			reset("cases_total",iTime,"estimate");
			for(State sta1:states()){
				d1 = regionCasesTotal(sta1,iTime,sOddsRatioType,"observation");
				this.put(sta1.sFips, "cases_total", iTime, d1, "estimate");
				this.putSum("cases_total",iTime,d1,"estimate");
			}
		}
	}

	public void stateCasesTotal(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		for(State sta1:states()) {
			map1 = sta1.casesTotal(iWindowSize);
			for(Integer i:map1.keySet()){
				this.put(sta1.sFips,"cases_total",i,map1.get(i),"estimate");
			}
		}
	}
	
	
	public void stateFractionPopulationInfected(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		for(State sta1:states()) {
			map1 = sta1.fractionPopulationInfected(iWindowSize);
			for(Integer i:map1.keySet()){
				this.put(sta1.sFips,"fraction_population_infected",i,map1.get(i),"estimate");
			}
		}
	}
	
	public void stateFractionCasesObserved(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		map1 = this.fractionCasesObserved(iWindowSize);
		
		for(State sta1:states()) {
			map1 = sta1.fractionCasesObserved(iWindowSize);
			for(Integer i:map1.keySet()){
				this.put(sta1.sFips,"fraction_cases_observed",i,map1.get(i),"estimate");
			}
		}
	}

	public void stateInfectionFatalityRates(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		for(State sta1:states()) {
			map1 = sta1.infectionFatalityRates(iWindowSize);
			for(Integer i:map1.keySet()){
				this.put(sta1.sFips,"infection_fatality_rate",i,map1.get(i),"estimate");
			}
		}
	}	
	
	public void countyTests() throws Exception{
		
		//i1 = number of states with at least one test
		//tbl1 = current data table for inferences
		//lnm1 = linear model
		//set1 = set of predictors
		//map1 = map of current coefficient estimates
		//d1 = current total number of tests for state
		//d2 = current number of tests for county
		//map2 = map from county ids to total tests (pre-normalization)
		
		HashMap<String,Double> map1;
		HashMap<String,Double> map2;	
		int i1;
		HashBasedTable<String,String,Double> tbl1;
		HashSet<String> set1;
		LinearModel lnm1;
		double d1;
		double d2;
		
		
		//loading probabilities of nonzero tests
		for(Integer iTime:times()){
			i1 = 0;
			for(State sta1:states()){
				if(sta1.get("tests",iTime,"observation")>0){
					i1++;
				}
			}
			this.put("probability_nonzero_tests",iTime,((double) i1)/50.,"observation");
		}
		
		set1 = new HashSet<String>();
		set1.add("population_log");
		set1.add("cases_observed_log");
		
		for(Integer iTime:times()){
		
			//loading regression coefficients for predicting numbers of tests
			tbl1 = HashBasedTable.create(3,50);
			for(State sta1:states()){
				if(sta1.get("tests",iTime,"observation")>0){
					tbl1.put("tests_log",sta1.sFips,Math.log(sta1.get("tests",iTime,"observation")));
					tbl1.put("population_log",sta1.sFips,Math.log(sta1.get("population",iTime,"observation")));
					tbl1.put("cases_observed_log",sta1.sFips,Math.log(sta1.get("cases_observed",iTime,"observation")+1));
				}
			}
			
			if(tbl1.rowKeySet().size()==0){
				this.put("nu_0",iTime,Double.NaN,"estimate");
				this.put("nu_c",iTime,Double.NaN,"estimate");
				this.put("nu_p",iTime,Double.NaN,"estimate");
				continue;
			}
			
			//fitting regression
			lnm1 = new LinearModel(tbl1, "tests_log", set1);
			lnm1.fitModel(set1);
			map1 = lnm1.findCoefficientEstimates();
			
			//saving coefficient estimates
			this.put("nu_0",iTime,Math.exp(map1.get("(Intercept)")),"estimate");
			this.put("nu_c",iTime,map1.get("cases_observed_log"),"estimate");
			this.put("nu_p",iTime,map1.get("population_log"),"estimate");
		}
		
		//finding test estimates for each county
		for(State sta1:states()){
			for(Integer iTime:times()){
				
				d1 = 0;
				map2 = new HashMap<String,Double>(300);
				for(County cty1:sta1.counties()){
					d2 = this.get("nu_0",iTime,"estimate")
							*Math.pow(cty1.get("cases_observed",iTime,"observation")+1,this.get("nu_c",iTime,"estimate"))
							*Math.pow(cty1.get("population",iTime,"observation"),this.get("nu_p",iTime,"estimate"));
					d2 = d2*this.get("probability_nonzero_tests",iTime,"observation");
					d1+=d2;
					map2.put(cty1.sFips,d2);
				}
				for(County cty1:sta1.counties()){
					d2 = map2.get(cty1.sFips)*sta1.get("tests",iTime,"observation")/d1;
					if(d2<cty1.get("cases_observed",iTime,"observation")){
						d2 = cty1.get("cases_observed",iTime,"observation");
					}
					this.put(cty1.sFips,sta1.sFips,"tests",iTime,d2,"estimate");
					this.putSum(sta1.sFips,"tests",iTime,d2,"estimate");
					this.putSum("tests",iTime,d2,"estimate");
				}
			}
		}
	}
	
	public void countyCumulativeMortality(){
		
		//i1 = cumulative mortality
		//lstTimes = times in ascending order
		
		int i1;
		ArrayList<Integer> lstTimes;
		
		lstTimes = new ArrayList<Integer>(setTimes);
		Collections.sort(lstTimes);
		for(State sta1:states()) {	
			for(County cty1: sta1.counties()){
				i1 = 0;
				for(Integer iTime:lstTimes){
					i1+=cty1.get("mortality",iTime,"observation");
					this.put(cty1.sFips,sta1.sFips,"mortality_cumulative",iTime,(double) i1,"observation");
					this.put(cty1.sFips,sta1.sFips,"mortality_cumulative_per_capita",iTime,(double) i1/cty1.get("population",iTime,"observation"),"observation");
				}
			}
		}
	}
	
	public void countyCasesTotal(String sOddsRatioType, String sTestsType){
		
		//d1 = current estimate
		
		double d1;
		
		for(Integer iTime:setTimes){
			for(State sta1:states()) {
				for(County cty1:sta1.counties()){
					d1 = regionCasesTotal(cty1,iTime,sOddsRatioType, sTestsType);
					this.put(cty1.sFips,sta1.sFips,"cases_total",iTime,d1,"estimate");
					
					//*******************************
					//if(cty1.sFips.equals("01001")){
					//	System.out.println("xxxx" + "," + cty1.sFips + "," + iTime + "," + cty1.get("cases_total",iTime,"observation") + "," + cty1.get("cases_total",iTime,"estimate"));
					//}
					//*******************************
				}
			}
		}
	}

	public void countyCasesTotal(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		for(State sta1:states()) {
			for(County cty1:sta1.counties()){	
				map1 = cty1.casesTotal(iWindowSize);
				for(Integer i:map1.keySet()){
					this.put(cty1.sFips,sta1.sFips,"cases_total",i,map1.get(i),"estimate");
				}
			}
		}
	}
	
	public void countyFractionPopulationInfected(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		for(State sta1:states()) {
			for(County cty1:sta1.counties()){	
				map1 = cty1.fractionPopulationInfected(iWindowSize);
				for(Integer i:map1.keySet()){
					this.put(cty1.sFips,sta1.sFips,"fraction_population_infected",i,map1.get(i),"estimate");
				}
			}
		}
	}

	public void countyFractionCasesObserved(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		map1 = this.fractionCasesObserved(iWindowSize);
		
		for(State sta1:states()){
			for(County cty1:sta1.counties()){
				map1 = cty1.fractionCasesObserved(iWindowSize);
				for(Integer i:map1.keySet()){
					this.put(cty1.sFips,sta1.sFips,"fraction_cases_observed",i,map1.get(i),"estimate");
				}
			}
		}
	}

	public void countryInfectionFatalityRates(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		map1 = this.infectionFatalityRates(iWindowSize);		
		for(Integer i:map1.keySet()){
			this.put("infection_fatality_rate",i,map1.get(i),"estimate");
		}
	}
	
	public void countyInfectionFatalityRates(int iWindowSize){
		
		//map1 = current fraction of cases observed map
		
		HashMap<Integer,Double> map1;
		
		for(State sta1:states()) {
			for(County cty1:sta1.counties()){	
				//*************************************
				map1 = cty1.infectionFatalityRates(iWindowSize);
				//map1 = cty1.infectionFatalityRates();
				//*************************************
				
				for(Integer i:map1.keySet()){
					this.put(cty1.sFips,sta1.sFips,"infection_fatality_rate",i,map1.get(i),"estimate");
				}
			}
		}
	}
	
	private double regionCasesTotal(Region rgn1, int iTime, String sOddsRatioType, String sTestsType){
		
		//dK = cases observed
		//dT = tests
		//dP = population
		//dW = odds ratio
		//d1 = output
		
		double dK;
		double dT;
		double dP;
		double dW;
		double d1;
		
		dK=rgn1.get("cases_observed",iTime,"observation");
		dT=rgn1.get("tests",iTime,sTestsType);
		dP=rgn1.get("population",iTime,"observation");
		dW=rgn1.get("odds_ratio",iTime, sOddsRatioType);
		
		d1 = solveForCasesTotal(dK, dT, dP, dW);
		
		if(Double.isInfinite(d1) || d1<0 || Double.isNaN(d1)){
			d1 = 0;
		}
		if(d1<rgn1.get("mortality",iTime,"observation")) {
			d1 = rgn1.get("mortality",iTime,"observation");
		}
		if(d1<rgn1.get("cases_observed",iTime,"observation")){
			d1 = rgn1.get("cases_observed",iTime,"observation");
		}
		if(d1>rgn1.get("population",iTime,"observation")){
			d1 = rgn1.get("population",iTime,"observation");
		}
		
		//***********************************
		//if(Integer.parseInt(rgn1.sFips)>1000) {
		//if(rgn1.sFips.equals("01001")){
		//	System.out.println("second" + "," + rgn1.sFips + "," + iTime + "," + rgn1.get("cases_total",iTime,"observation") + "," + d1);
		//}
		//***********************************
		
		return d1;
	}

	private double solveForCasesTotal(double dK, double dT, double dP, double dW){
	
		//dC0 = current interval start
		//dC1 = current interval end
		//d0 = start value (at dK)
		//d1 = final value (at dP)
		//dStep = current step size
		//dCCurrent = current value of C
		//d3 = current function value
		//dThreshold = threshold
		//i1 = counter
		//iMaxIterations = maximum iterations
		
		int i1;
		int iMaxIterations;
		double dC0;
		double dC1;
		double d0;
		double d1;
		double dStep;
		double dCCurrent;
		double d3;
		double dThreshold;
		
		if(dK==0){
			return 0;
		}
		if(dT<=dK){
			return Double.NaN;
		}
		if(dW==0){
			return Double.NaN;
		}
		
		dThreshold = 0.00000001;
		iMaxIterations = 1000;
		
		dC0 = dK;
		dC1 = dP-1;
		d0 = casesTotalObjectiveFunction(dK, dT, dP, dW, dC0);
		d1 = casesTotalObjectiveFunction(dK, dT, dP, dW, dC1);
		
		if(Math.signum(d0)==Math.signum(d1)){
			System.out.println("Warning: same sign.");
			System.out.println(dC0 + "," + d0 + "," + dC1 + "," + d1);
			System.out.println(dK + "," + dT + "," + dP + "," + dW);
			return Double.NaN;
		}
		i1 = 0;
		
		do{
			dStep = (dC1-dC0)*0.5;
			dCCurrent = dC0+dStep;
			d3 = casesTotalObjectiveFunction(dK, dT, dP, dW, dCCurrent);
			if(Math.signum(d3)!=Math.signum(d0)){
				dC1 = dCCurrent;
				d1 = d3;
			}else if(Math.signum(d3)!=Math.signum(d1)){
				dC0 = dCCurrent;
				d0 = d3;
			}
			i1++;
		}while((dC1-dC0)>dThreshold && i1<iMaxIterations);
		if(i1>=iMaxIterations){
			System.out.println("Warning: maximum iterations exceeded.");
			return Double.NaN;
		}
		return (dC1+dC0)/2.;
	}

	private double casesTotalObjectiveFunction(double dK, double dT, double dP, double dW, double dC){
		
		//d1 = output
		
		double d1;
		
		d1 = dK/dC - 1 + Math.pow(1-(dT-dK)/(dP-dC),dW);
		return d1;
	}

	@Deprecated
	public void countyTestsPredictor(String sOddsRatioType){
		
		//dP = current population
		//dM = current mortality
		//dK = current cases observed
		//dW = current odds ratio
		//d1 = partial value
		
		//double d1;
		double dP;
		double dM;
		double dK;
		double dW;
		
		for(State sta1:states()) {
			for(County cty1: sta1.counties()){
				for(Integer iTime:setTimes){
					dP = cty1.get("population",iTime,"observation");
					
					
					dM = cty1.get("mortality",iTime,"observation");
					dK = cty1.get("cases_observed",iTime,"observation");
					dW = cty1.get("odds_ratio",iTime,sOddsRatioType);
				
					//*************************************
					//Taylor approximation for phi
					this.put(cty1.sFips,sta1.sFips,"tests_predictor_slope",iTime,dP*dP*(1.-Math.pow(1.-dK/dP,1./dW))/(dM+1.),"observation");
					this.put(cty1.sFips,sta1.sFips,"tests_predictor_constant",iTime,dK - dP*(1.-Math.pow(1.-dK/dP,1./dW)),"observation");
					this.putSum(sta1.sFips,"tests_predictor_constant",iTime,dK - dP*(1.-Math.pow(1.-dK/dP,1./dW)),"observation");
			
					//Taylor approximation for nu
					//d1 = (dP-dM-1.)*(1.-Math.pow(1.-dK/dP,1./dW));
					//this.put(cty1.sFips,sta1.sFips,"tests_predictor_slope",iTime,d1,"observation");
					//this.put(cty1.sFips,sta1.sFips,"tests_predictor_constant",iTime,d1*Math.log(dP/(dM+1)-1.) + dK,"observation");
					//this.putSum(sta1.sFips,"tests_predictor_constant",iTime,d1*Math.log(dP/(dM+1)-1.) + dK,"observation");
					//*************************************				
				}
			}
		}
	}

	@Deprecated
	public void countyInfectionFatalityRatesC(String sStateFips) throws Exception{
		
		//tbl1 = data table
		//sta1 = state of interest
		//ifr1 = inferred rates object
		//tbl2 = coefficient estimates
		//d1 = current estimate
		//dM = current mortality
		
		HashBasedTable<Integer,String,Double> tbl1;
		State sta1;
		InferredRates ifr1;
		HashBasedTable<Integer,String,Double> tbl2;
		double d1;
		//double dM;
		
		//TODO report average CFRs or M/(M+1) infection fatality rates (which will often be zero)?
		
		sta1 = mapStates.get(sStateFips);
		tbl1 = this.formatDataTableCFRC(sta1);
		
		//finding estimates
		ifr1 = new InferredRates(tbl1, sta1.countyFips(), "cases_total", iStartTime, iEndTime);
		ifr1.inferRates(0.99,1.01,0.0005);
		tbl2 = ifr1.coefficients();
		
		//saving infection fatality rate estimates
		for(String s1:tbl2.columnKeySet()){
			for(String sCountyFips:s1.split(";")) {
				for(Integer iTime:tbl2.rowKeySet()){
					//dM = sta1.county(sCountyFips).get("mortality",iTime,"observation");
					//d1 = dM/(dM+1.)*1./tbl2.get(iTime,s1);
					d1 = 1./tbl2.get(iTime,s1);
					this.put(sCountyFips,sStateFips,"infection_fatality_rate",iTime,d1,"estimate");
				}
			}
		}
	}
	
	@Deprecated
	private HashBasedTable<Integer,String,Double> formatDataTableCFRC(State sta1){
		
		//tbl1 = data table
		//map2 = for a given time returns sum of mortality squared
		//map1 = for a given time returns the sum of mortality
		//map3 = for a given time gives the sum of partial test_predictor variables
		//d1 = current value
		//dOffset = term to add for zero values
		//b1 = flag for whether NaN found
		
		double d1;
		HashBasedTable<Integer,String,Double> tbl1;
		HashMap_AdditiveDouble<Integer> map1;
		HashMap_AdditiveDouble<Integer> map2;
		double dOffset;
		boolean b1;
		
		dOffset = 1.;
		
		tbl1 = HashBasedTable.create(sta1.times().size(),sta1.counties().size()+1);
		map1 = new HashMap_AdditiveDouble<Integer>(this.times().size());
		map2 = new HashMap_AdditiveDouble<Integer>(this.times().size());
		for(int iTime:sta1.times()){
			for(County cty1:sta1.counties()){
				d1 = cty1.get("mortality",iTime,"observation") + dOffset;	
				map1.putSum(iTime,d1);
				map2.putSum(iTime,d1*d1);
			}
		}
		
		for(Integer iTime:sta1.times()){
			
			//checking if time has valid predictors
			b1 = false;
			for(County cty1:sta1.counties()){
				d1 = (cty1.get("mortality",iTime,"observation")+dOffset)/map2.get(iTime);
				if(Double.isNaN(d1) || Double.isInfinite(d1)){
					b1 = true;
					break;
				}
			}
		
			//exiting if no valid predictors
			if(b1==true){
				continue;
			}
			
			//saving response variable
			d1 = sta1.get("cases_total",iTime,"estimate")/map2.get(iTime);
			tbl1.put(iTime,"cases_total",d1);
			
			//saving predictors
			for(County cty1:sta1.counties()){
				d1 = (cty1.get("mortality",iTime,"observation")+dOffset)/map2.get(iTime);
				tbl1.put(iTime,cty1.sFips,d1);
			}
		}
		return tbl1;
	}
	
	@Deprecated
	public void countyInfectionFatalityRatesT(String sStateFips) throws Exception{
		
		//tbl1 = data table
		//sta1 = state of interest
		//ifr1 = inferred rates object
		//tbl2 = coefficient estimates
		//d1 = current estimate
		//dM = current mortality
		
		HashBasedTable<Integer,String,Double> tbl1;
		State sta1;
		InferredRates ifr1;
		HashBasedTable<Integer,String,Double> tbl2;
		double d1;
		//double dM;
		
		//TODO report average CFRs or M/(M+1) infection fatality rates (which will often be zero)?
		
		sta1 = mapStates.get(sStateFips);
		tbl1 = this.formatDataTableCFRT(sta1);
		
		//finding estimates
		ifr1 = new InferredRates(tbl1, sta1.countyFips(), "tests", iStartTime, iEndTime);
		ifr1.inferRates(0.99,1.01,0.0005);
		tbl2 = ifr1.coefficients();
		
		//saving infection fatality rate estimates
		for(String s1:tbl2.columnKeySet()){
			for(String sCountyFips:s1.split(";")) {
				for(Integer iTime:tbl2.rowKeySet()){
					d1 = tbl2.get(iTime,s1);
					this.put(sCountyFips,sStateFips,"infection_fatality_rate",iTime,d1,"estimate");
				}
			}
		}
	}

	@Deprecated
	private HashBasedTable<Integer,String,Double> formatDataTableCFRT(State sta1){
		
		//tbl1 = data table
		//d1 = current value
		//b1 = flag for whether NaN found
		
		double d1;
		HashBasedTable<Integer,String,Double> tbl1;
		boolean b1;
		
		tbl1 = HashBasedTable.create(sta1.times().size(),sta1.counties().size()+1);
		for(Integer iTime:sta1.times()){
			
			//checking if time has valid predictors
			b1 = false;
			for(County cty1:sta1.counties()){
				if(Double.isNaN(cty1.get("tests_predictor_slope",iTime,"observation"))){
					b1 = true;
					break;
				}
			}
		
			//exiting if no valid predictors
			if(b1==true){
				continue;
			}
			
			//saving response variable
			d1 = sta1.get("tests",iTime,"observation") - sta1.get("tests_predictor_constant",iTime,"observation");
			tbl1.put(iTime,"tests",d1);
			
			//saving predictors
			for(County cty1:sta1.counties()){
				d1 = cty1.get("tests_predictor_slope",iTime,"observation");
				tbl1.put(iTime,cty1.sFips,d1);
			}
		}
				
		return tbl1;
	}
}