package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;

import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.base.MultivariateHypergeometricDistribution;

public class Country_Simulation extends Country{

	/**Random number generator**/
	private Random rnd1;
	
	/**Secondary random number generator (for replicating infections simulations between iterations)**/
	private Random rnd2;
	
	/**Random seed**/
	private int iRandomSeed;
	
	public Country_Simulation(Country ctr1, int iRandomSeed) throws Exception{
		super(ctr1); 
		this.iRandomSeed = iRandomSeed;
		initialize(iRandomSeed);
	}
	
	private void initialize(int iRandomSeed) throws Exception{

		int i1;
		int iCounter;
		
		rnd1 = new Random(iRandomSeed);
		
		System.out.println("Simulating odds ratios...");
		simulateOddsRatios();
		System.out.println("Simulating numbers of tests...");
		iCounter = 0;
		do{
			i1 = simulateTests();
			iCounter++;
		}while(i1==1 && iCounter<100);
		if(iCounter==100){
			throw new Exception("ERROR: county level tests cannot be distributed");
		}
		
	}

	public void simulateInfectionsAndCases() throws Exception{
		System.out.println("Simulating numbers of infections and cases...");
		simulateCases();
	}
	
	private void simulateOddsRatios(){
		
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
			d1 = (double) (iTime - iMinTime);
			d1 = dRange/Math.sqrt(d1+11);
			put("odds_ratio",iTime,d1,"observation");
			for(State sta1:this.states()) {
				put(sta1.sFips,"odds_ratio",iTime,d1,"observation");
				for(County cty1:sta1.counties()) {
					put(cty1.sFips,sta1.sFips,"odds_ratio",iTime,d1,"observation");
				}
			}
		}
	}
	
	private void simulateCases() throws Exception{
		for(Integer iTime:times()){
			reset("cases_observed",iTime,"observation");
			reset("cases_total",iTime,"observation");
			for(State sta1:states()){
				sta1.reset("cases_observed",iTime,"observation");
				sta1.reset("cases_total",iTime,"observation");
			}
		}
		rnd2 = new Random(iRandomSeed + 17);		
		for(Integer iTime:times()){
			for(State sta1:states()){
				for(County cty1:sta1.counties()) {
					simulateCountyCases(cty1,sta1,iTime);
				}
			}
		}
	}
	
	private void simulateCountyCases(County cty1, State sta1, int iTime) throws Exception{
		
		//i1 = observed number of cases
		//dPr = current probability of case
		//iTRemaining = number of tests remaining
		//dCRemaining = total cases remaining
		//dNotCRemaining = total non-cases remaining
		//dW = odds ratio
		//dCasesTotalSimulated = total number of cases (simulated)
		//dMortality = mortality in current county
		//dPopulation = current population
		//i2 = counter
		
		double dCasesTotalSimulated;
		double dMortality;
		int i1;
		int iTRemaining;
		double dPr;
		double dW;
		double dCRemaining;
		double dNotCRemaining;
		double dPopulation;
		int i2;
		
		dMortality = cty1.get("mortality",iTime,"observation");
		dPopulation = cty1.get("population", iTime, "observation");
		i2 = 0;
		do {
			dCasesTotalSimulated = Math.floor(0.017*Math.pow(rnd2.nextDouble(),4)*dPopulation);		
			i2++;
		}while((dCasesTotalSimulated<dMortality) && i2<10000);
		if(i2==10000) {
			throw new Exception("ERROR: failed to simulate infections for county " + cty1.sFips + "...");
		}
		
		put(cty1.sFips,sta1.sFips,"cases_total",iTime,dCasesTotalSimulated,"observation");
		putSum(sta1.sFips,"cases_total",iTime,dCasesTotalSimulated,"observation");
		putSum("cases_total", iTime, dCasesTotalSimulated, "observation");
		i1 = 0;		
		iTRemaining = (int) cty1.get("tests",iTime,"observation");
		dCRemaining = cty1.get("cases_total",iTime,"observation");
		dNotCRemaining = cty1.get("population", iTime, "observation") -  dCRemaining;
		dW = cty1.get("odds_ratio", iTime, "observation");
	
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

	private int simulateTests(){
		
		for(Integer iTime:times()){
			for(State sta1:states()){
				if(simulateCountyTests(sta1,iTime)==1) {
					return 1;
				}
			}
		}
		return 0;
	}
	
	private int simulateCountyTests2(State sta1, int iTime) {
		
		//dStateTests = number of tests in the state
		//dConstantOfProportionality = constant of proportionality
		//mapTests = map from county fips to raw number of simulated tests
		//mapTestsNormalized = map from county fips to normalized number of simulated tests
		//d1 = current total number of tests
		//d3 = current total normalized number of tests
		//d2 = current number of tests
		//iCounties = number of counties
		//nrm1 = normal random variate generator
		//dStDev = standard deviation of error term
		//iTestsExtra = extra tests to be distributed (post rounding)
		//iStateTests = number of tests in state
		//i1 = counter
		//ran1 = random generator object
		
		JDKRandomGenerator ran1;
		int i1;
		double dStateTests;
		double dConstantOfProportionality;
		HashMap<String,Double> mapTests;
		HashMap<String,Double> mapTestsNormalized;
		double d1;
		double d2;
		double d3;
		int iCounties;
		NormalDistribution nrm1;
		double dStDev;
		int iStateTests;
		int iTestsExtra;
		
		dStateTests = sta1.get("tests",iTime,"observation");
		iStateTests = (int) sta1.get("tests",iTime,"observation");
		dConstantOfProportionality = 1;
		iCounties = sta1.counties().size();
		mapTests = new HashMap<String,Double>(iCounties);
		mapTestsNormalized = new HashMap<String,Double>(iCounties);
		dStDev = 0.5;
		ran1 = new JDKRandomGenerator();
		ran1.setSeed(rnd1.nextInt());
		nrm1 = new NormalDistribution(ran1, 0., dStDev);
		
		//simulating raw numbers of tests
		d1 = 0;
		for(County cty1:sta1.counties()) {
			d2 = dConstantOfProportionality*cty1.get("population",iTime,"observation")*Math.exp(nrm1.sample());
			mapTests.put(cty1.sFips,d2);
			d1+=d2;
		}
		
		//normalizing numbers of tests
		d3 = 0;
		for(County cty1:sta1.counties()) {
			d2 = Math.round(mapTests.get(cty1.sFips)/d1*dStateTests);
			if(d2>cty1.get("population",iTime,"observation")) {
				d2 = cty1.get("population",iTime,"observation");
			}
			d3+=d2;
			mapTestsNormalized.put(cty1.sFips,d2);
		}
		iTestsExtra = iStateTests - (int) d3;
		
		//********************
		//if(iTestsExtra>sta1.counties().size()) {
		//	System.out.println(sta1.counties().size() + "," + iTestsExtra);
		//}
		//********************
		
		i1 = 0;
		while(iTestsExtra!=0 && i1<100) {
			for(County cty1:sta1.counties()) {
				if(iTestsExtra > 0 && mapTestsNormalized.get(cty1.sFips)<cty1.get("population",iTime,"observation")) {
					d2 = mapTestsNormalized.get(cty1.sFips)+1;
					iTestsExtra--;
				}else if(iTestsExtra < 0 && mapTestsNormalized.get(cty1.sFips)>0) {
					d2 = mapTestsNormalized.get(cty1.sFips)-1;
					iTestsExtra++;
				}else {
					d2 = mapTestsNormalized.get(cty1.sFips);
				}
				mapTestsNormalized.put(cty1.sFips,d2);
				if(d2<0) {
					System.out.println("ERROR: simulated number of tests less than zero.");
				}
			}
			i1++;
		}
		if(i1 == 100) {
			return 1;
		}
		for(County cty1:sta1.counties()) {
			cty1.put("tests",iTime,mapTestsNormalized.get(cty1.sFips),"observation");
		}
		return 0;
	}
	
	private int simulateCountyTests1(State sta1, int iTime) {
		
		//iStateTests = total number of tests in state
		//lstPopulation = list of population for each county
		//mapCounties = map from county names to population list indices
		//iCounties = number of counties
		//lst1 = current hypergeometric sample
		//hyp1 = hypergeometric random variate
		
		MultivariateHypergeometricDistribution hyp1;
		int iStateTests;
		ArrayList<Integer> lstPopulation;
		HashMap<String,Integer> mapCounties;
		int iCounties;
		ArrayList<Integer> lst1;
		
		iStateTests = (int) sta1.get("tests",iTime,"observation");
		iCounties = sta1.counties().size();
		lstPopulation = new ArrayList<Integer>(iCounties);
		mapCounties = new HashMap<String,Integer>(iCounties);
		for(County cty1:sta1.counties()) {
			lstPopulation.add((int) cty1.get("population",iTime,"observation"));
			mapCounties.put(cty1.sFips,lstPopulation.size()-1);
		}
		hyp1 = new MultivariateHypergeometricDistribution(lstPopulation);
		lst1 = hyp1.sample(iStateTests);
		for(County cty1:sta1.counties()) {
			cty1.put("tests",iTime,lst1.get(mapCounties.get(cty1.sFips)),"observation");
		}
		return 0;
	}
	
	
	//TODO this should be constant between iterations --> load from file in second iteration onward because time consuming?
	private int simulateCountyTests(State sta1, int iTime) {
		
		//iStateTests = total number of tests in state
		//lst1 = list of counties
		//mapPopulationRemaining = "remaining" population for testing in each state
		//mapTests = current number of tests
		//s1 = current county fips
		//iCounties = number of counties
		//i1 = counter
		
		int i1;
		int iCounties;
		int iStateTests;
		ArrayList<String> lst1;
		HashMap_AdditiveInteger<String> mapPopulationRemaining;
		HashMap_AdditiveInteger<String> mapTests;
		String s1;
		
		iStateTests = (int) sta1.get("tests",iTime,"observation");
		iCounties = sta1.counties().size();
		lst1 = new ArrayList<String>(iCounties);
		mapPopulationRemaining = new HashMap_AdditiveInteger<String>(iCounties);
		mapTests = new HashMap_AdditiveInteger<String>(iCounties);
		for(County cty1:sta1.counties()) {
			lst1.add(cty1.sFips);
			mapPopulationRemaining.put(cty1.sFips,(int) cty1.get("population",iTime,"observation"));
			mapTests.put(cty1.sFips,0);
		}
		for(int i=0;i<iStateTests;i++) {
			
			i1 = 0;
			do{
				s1 = lst1.get((int) Math.floor(rnd1.nextDouble()*iCounties));
				i1++;
			}while(mapPopulationRemaining.get(s1)==0 && i1<1000);
			if(i1==1000) {
				System.out.println("ERROR: tests could not be simulated for state " + sta1.sFips + "...");
			}
			mapPopulationRemaining.putSum(s1,-1);
			mapTests.putSum(s1,1);
		}
		
		for(County cty1:sta1.counties()) {
			cty1.put("tests",iTime,mapTests.get(cty1.sFips),"observation");
		}
		return 0;
	}
	
	public void setAlphaTime(State sta1, double dAlphaTime){
		mapStates.get(sta1.sFips).dAlphaTime =  dAlphaTime;
	}
}