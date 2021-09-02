package gov.lbnl.Estimator;

import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class EstimatorLauncher0{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = all data, including mortality, population (county), tests (state), odds_ratio (country) 
		//cts1 = country simulation object
		//cte1 = country estimation object
		//sMode = type of estimate
		//mapObs = for county, state, and country returns output observations
		//mapEst = for county, state, and country returns output estimates
		//iWindowSize = window size for calculating ratios
		
		int iWindowSize;
		ArgumentIO arg1;
		DataIO dat1;
		Country_Simulation0 cts1;
		Country_Estimation cte1 = null;
		String sMode;
		HashMap<String,String[]> mapObs;
		HashMap<String,String[]> mapEst;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sMode = arg1.getValueString("sMode");
		mapObs = new HashMap<String,String[]>();
		mapEst = new HashMap<String,String[]>();
		iWindowSize = arg1.getValueInt("iWindowSize");
		
		//loading data
		cts1 = new Country_Simulation0(arg1.getValueInt("iStartTime"),arg1.getValueInt("iEndTime"), dat1.getIntegerColumn("TIME"));
		cts1.loadData(dat1);
		
		//************************
		//for(State sta1:cts1.stateMap().values()) {
		//	for(County cty1:sta1.counties()) {
		//		System.out.println(cty1.sFips + "," + cty1.setTimes.size());
		//	}
		//}
		//************************
		
		//initializing output
		mapEst.put("county",new String[]{""});
		mapEst.put("state",new String[]{""});
		mapEst.put("country",new String[]{""});
		mapObs.put("county",new String[]{""});
		mapObs.put("state",new String[]{""});
		mapObs.put("country",new String[]{""});
		
		//initializing
		cte1 = new Country_Estimation(cts1);
		cte1.countryCasesTotal();
		cte1.allOddsRatios();
		cte1.stateCasesTotal("estimate");
		
		//loading county total cases
		if(arg1.containsArgument("sCountyTests") && arg1.getValueString("sCountyTests").equals("observation")) {
			cte1.countyCasesTotal("estimate","observation");
		}else{
			cte1.countyTests();
			cte1.countyCasesTotal("estimate","estimate");
		}
		
		//selecting analysis
		if(sMode.equals("all_estimates")) {
			
			cte1.stateCasesTotal(iWindowSize);
			cte1.countyCasesTotal(iWindowSize);
			cte1.stateInfectionFatalityRates(iWindowSize);
			cte1.countyInfectionFatalityRates(iWindowSize);
			
			mapEst.put("country",new String[]{"cases_total","odds_ratio"});
			mapEst.put("state",new String[]{"infection_fatality_rate", "cases_total"});
			mapEst.put("county",new String[]{"infection_fatality_rate", "cases_total"});
	
		}else if(sMode.equals("infection_fatality_rate")) {
			
			cte1.stateInfectionFatalityRates(iWindowSize);
			cte1.countyInfectionFatalityRates(iWindowSize);
			
			mapEst.put("state",new String[]{"infection_fatality_rate"});
			mapEst.put("county",new String[]{"infection_fatality_rate"});
			
		}else if(sMode.equals("country_odds_ratio_simulation")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cts1 = new Country_Simulation0(cte1);
			cts1.countryOmega();
			cts1.countryObservedCases(1234,"observation");
			cte1 = new Country_Estimation(cts1);
			cte1.allOddsRatios();
			
			mapEst.put("country",new String[]{"odds_ratio"});
			mapObs.put("country",new String[]{"odds_ratio"});	
			
		}else if(sMode.equals("county_infections_simulation")) {
			
			//TODO with aggregated output, country-level odds ratios need to be output separately (time-dependent)
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			
			//simulating observed numbers of cases in each county
			cts1.countyCases(arg1.getValueInt("iRandomSeed"),"estimate");
			
			//loading total case estimates
			cte1 = new Country_Estimation(cts1);
			cte1.allOddsRatios();
			cte1.countyTests();
			cte1.countyCasesTotal("estimate","estimate");
			
			//mapEst.put("county",new String[]{"tests", "cases_total"});
			mapObs.put("county",new String[]{"cases_observed", "cases_total", "tests", "odds_ratio"});	
			mapObs.put("state",new String[]{"cases_observed","cases_total","tests", "odds_ratio"});	
			mapObs.put("country",new String[]{"cases_observed","cases_total","tests", "odds_ratio"});	
			
			
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	
			
		}else if(sMode.equals("fraction_population_infected")) {
			
			cte1.countryFractionPopulationInfected(iWindowSize);
			cte1.stateFractionPopulationInfected(iWindowSize);
			cte1.countyFractionPopulationInfected(iWindowSize);
			
			mapEst.put("country",new String[]{"fraction_population_infected"});
			mapEst.put("state",new String[]{"fraction_population_infected"});
			mapEst.put("county",new String[]{"fraction_population_infected"});
			
		}else if(sMode.equals("cases_total")) {
				
			cte1.countryCasesTotal();
			cte1.stateCasesTotal(iWindowSize);
			cte1.countyCasesTotal(iWindowSize);
			
			mapEst.put("country",new String[]{"cases_total"});
			mapEst.put("state",new String[]{"cases_total"});
			mapEst.put("county",new String[]{"cases_total"});
		
		}else if(sMode.equals("fraction_cases_observed")) {
			
			cte1.stateFractionCasesObserved(iWindowSize);
			cte1.countyFractionCasesObserved(iWindowSize);
			cte1.countryFractionCasesObserved(iWindowSize);
			
			mapEst.put("country",new String[]{"fraction_cases_observed"});
			mapEst.put("state",new String[]{"fraction_cases_observed"});
			mapEst.put("county",new String[]{"fraction_cases_observed"});
			
		}else if(sMode.equals("tests_prediction")){
	
			mapObs.put("state",new String[]{"tests","cases_observed","population"});
	
		}else if(sMode.equals("odds_ratios_through_time")) {
	
			mapEst.put("country",new String[]{"odds_ratio"});

		}else if(sMode.equals("country_odds_ratio_simulation")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cts1 = new Country_Simulation0(cte1);
			cts1.countryOmega();
			cts1.countryObservedCases(1234,"observation");
			cte1 = new Country_Estimation(cts1);
			cte1.allOddsRatios();
			
			mapEst.put("country",new String[]{"odds_ratio"});
			mapObs.put("country",new String[]{"odds_ratio"});

		}else if(sMode.equals("state_total_cases_simulation")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			
			//simulating observed numbers of cases in each county
			cts1.stateCases(arg1.getValueInt("iRandomSeed"),"estimate");
			
			//loading total case estimates
			cte1 = new Country_Estimation(cts1);
			
			//adding the following two lines seems to increase bias of the estimator. why?
			cte1.allOddsRatios();
			cte1.stateCasesTotal("estimate");
			
			mapEst.put("state",new String[]{"cases_total"});
			mapObs.put("state",new String[]{"cases_total"});
		
		}else if(sMode.equals("county_total_cases_simulation")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			
			//simulating observed numbers of cases in each county
			cts1.countyCases(arg1.getValueInt("iRandomSeed"),"estimate");
			
			//loading total case estimates
			cte1 = new Country_Estimation(cts1);
			cte1.stateCasesTotal("estimate");
			cte1.countyTests();
			cte1.countyCasesTotal("estimate");
			
			mapEst.put("county",new String[]{"cases_total"});
			mapObs.put("county",new String[]{"cases_total"});
		}
		
		//outputting results
		DataIO.writeToFile(
				cte1.print(
						mapObs.get("county"),
						mapObs.get("state"),
						mapObs.get("country"),
						"observation",
						true,
						false),
				arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(
				cte1.print(
						mapEst.get("county"),
						mapEst.get("state"),
						mapEst.get("country"),
						"estimate",
						false,
						false),
				arg1.getValueString("sOutputPath"),true);
		System.out.println("Done.");
	}

	/*
	public static void main0(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = all data, including mortality, population (county), tests (state), odds_ratio (country) 
		//cts1 = country simulation object
		//cte1 = country estimation object
		//sMode = type of estimate
		//mapObs = for county, state, and country returns output observations
		//mapEst = for county, state, and country returns output estimates
		
		ArgumentIO arg1;
		DataIO dat1;
		Country_Simulation cts1;
		Country_Estimation cte1 = null;
		String sMode;
		HashMap<String,String[]> mapObs;
		HashMap<String,String[]> mapEst;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sMode = arg1.getValueString("sMode");
		mapObs = new HashMap<String,String[]>();
		mapEst = new HashMap<String,String[]>();
		
		//loading data
		cts1 = new Country_Simulation(arg1.getValueInt("iStartTime"),arg1.getValueInt("iEndTime"));
		cts1.loadData(dat1);
		
		//initializing output
		mapEst.put("county",new String[]{""});
		mapEst.put("state",new String[]{""});
		mapEst.put("country",new String[]{""});
		mapObs.put("county",new String[]{""});
		mapObs.put("state",new String[]{""});
		mapObs.put("country",new String[]{""});
		
		//selecting analysis
		if(sMode.equals("state_infection_fatality_rates")) {
			
			cte1 = new Country_Estimation(cts1);
			
			//cte1.stateCasesTotal("observation");
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			cte1.stateCasesTotal("estimate");
			cte1.stateInfectionFatalityRates();
			
			mapEst.put("state",new String[]{"infection_fatality_rate"});
		
		}else if(sMode.equals("state_infection_fatality_rates_overall")) {
			
			cte1 = new Country_Estimation(cts1);
			
			//cte1.stateCasesTotal("observation");
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			cte1.stateCasesTotal("estimate");
			cte1.stateInfectionFatalityRatesOverall();
			cte1.stateFractionPopulationInfected();
			
			mapEst.put("state",new String[]{"infection_fatality_rate"});
		
		}else if(sMode.equals("county_infection_fatality_rates")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			cte1.countyTests();
			cte1.countyCasesTotal("estimate");
			cte1.countyInfectionFatalityRates(-1);
	
			mapEst.put("county",new String[]{"infection_fatality_rate"});
			
		}else if(sMode.equals("population_testing")) {
			
			cte1 = new Country_Estimation(cts1);
			
			mapObs.put("state",new String[]{"population","tests","mortality","cases_observed"});
				
			
			
		}else if(sMode.equals("fraction_population_infected")) {
			
			cte1 = new Country_Estimation(cts1);
			
			//cte1.stateCasesTotal("observation");
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			cte1.stateCasesTotal("estimate");
			cte1.stateFractionPopulationInfected();
			
			mapEst.put("state",new String[]{"fraction_population_infected"});
		
		}else if(sMode.equals("state_cases_total")) {
				
			cte1 = new Country_Estimation(cts1);
			
			//cte1.stateCasesTotal("observation");
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			cte1.stateCasesTotal("estimate");
			cte1.stateInfectionFatalityRates();
			
			mapEst.put("state",new String[]{"cases_total"});
		
		}else if(sMode.equals("state_fraction_cases_observed")) {
			
			cte1 = new Country_Estimation(cts1);
			
			//cte1.stateCasesTotal("observation");
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			cte1.stateCasesTotal("estimate");
			cte1.stateFractionCasesObserved();
			
			mapEst.put("state",new String[]{"fraction_cases_observed"});
			
		}else if(sMode.equals("odds_ratios_through_time")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			
			mapEst.put("country",new String[]{"odds_ratio"});
	
		}else if(sMode.equals("country_odds_ratio_simulation")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cts1 = new Country_Simulation(cte1);
			cts1.countryOmega();
			cts1.countryObservedCases(1234,"observation");
			cte1 = new Country_Estimation(cts1);
			cte1.allOddsRatios();
			
			mapEst.put("country",new String[]{"odds_ratio"});
			mapObs.put("country",new String[]{"odds_ratio"});
	
		}else if(sMode.equals("county_infection_fatality_rates_0")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			cte1.stateCasesTotal("estimate");
			cte1.countyTestsPredictor("estimate");
			
			for(State sta1:cte1.states()){
				System.out.println("Estimating rates for state " + sta1.sFips + "...");
				try {
					cte1.countyInfectionFatalityRatesC(sta1.sFips);
					//cte1.countyInfectionFatalityRatesT(sta1.sFips);
				}catch(Exception e){}
			}
		
			mapEst.put("county",new String[]{"infection_fatality_rate"});
		
		}else if(sMode.equals("state_total_cases_simulation")) {
			
			cte1 = new Country_Estimation(cts1);
			cte1.countryCasesTotal();
			cte1.allOddsRatios();
			
			//simulating observed numbers of cases in each county
			cts1.stateCases(arg1.getValueInt("iRandomSeed"),"estimate");
			
			//loading total case estimates
			cte1 = new Country_Estimation(cts1);
			cte1.stateCasesTotal("estimate");
			
			mapEst.put("state",new String[]{"cases_total"});
			mapObs.put("state",new String[]{"cases_total"});
		
		}
		
		//outputting results
		DataIO.writeToFile(
				cte1.print(
						mapObs.get("county"),
						mapObs.get("state"),
						mapObs.get("country"),
						"observation",
						true),
				arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(
				cte1.print(
						mapEst.get("county"),
						mapEst.get("state"),
						mapEst.get("country"),
						"estimate",
						false),
				arg1.getValueString("sOutputPath"),true);
		System.out.println("Done.");
	}
	*/
}