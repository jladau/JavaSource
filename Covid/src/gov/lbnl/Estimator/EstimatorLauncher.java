package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class EstimatorLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = all data, including mortality, population (county), tests (state), odds_ratio (country) 
		//cts1 = country simulation object
		//cte1 = country estimation object
		//sMode = type of estimate
		//mapObs = for county, state, and country returns output observations
		//mapEst = for county, state, and country returns output estimates
		//iWindowSize = window size for calculating ratios
		//bTimeIntegrated = flag for whether to output time integrated results
		
		int iWindowSize;
		ArgumentIO arg1;
		DataIO dat1;
		Country_Simulation cts1;
		Country_Estimation cte1 = null;
		String sMode;
		HashMap<String,String[]> mapObs;
		HashMap<String,String[]> mapEst;
		boolean bTimeIntegrated;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sMode = arg1.getValueString("sMode");
		mapObs = new HashMap<String,String[]>();
		mapEst = new HashMap<String,String[]>();
		iWindowSize = arg1.getValueInt("iWindowSize");
		if(arg1.containsArgument("bTimeIntegrated")) {
			bTimeIntegrated = arg1.getValueBoolean("bTimeIntegrated");
		}else {
			bTimeIntegrated = false;
		}
		
		//loading data
		cte1 = new Country_Estimation(arg1.getValueInt("iStartTime"),arg1.getValueInt("iEndTime"), dat1.getIntegerColumn("TIME"));
		cte1.loadData(dat1);
		
		//initializing output
		mapEst.put("county",new String[]{""});
		mapEst.put("state",new String[]{""});
		mapEst.put("country",new String[]{""});
		mapObs.put("county",new String[]{""});
		mapObs.put("state",new String[]{""});
		mapObs.put("country",new String[]{""});
		
		//selecting analysis
		if(sMode.equals("all_estimates")) {
		
			//initializing
			cte1.initialize();
			
			//loading county total cases
			if(arg1.containsArgument("sCountyTests") && arg1.getValueString("sCountyTests").equals("observation")) {
				cte1.countyCasesTotal("estimate","observation");
			}else{
				cte1.countyTests();
				cte1.countyCasesTotal("estimate","estimate");
			}
			
			cte1.stateCasesTotal(iWindowSize);
			cte1.countyCasesTotal(iWindowSize);
			cte1.stateInfectionFatalityRates(iWindowSize);
			cte1.countyInfectionFatalityRates(iWindowSize);
			
			mapEst.put("country",new String[]{"cases_total","odds_ratio"});
			mapEst.put("state",new String[]{"infection_fatality_rate", "cases_total"});
			mapEst.put("county",new String[]{"infection_fatality_rate", "cases_total", "tests"});
	
			mapObs.put("country",new String[] {"infection_fatality_rate","cases_observed","tests","mortality"});
			mapObs.put("state",new String[] {"cases_observed","tests","mortality"});
			mapObs.put("county",new String[] {"cases_observed","mortality"});
			
		}else if(sMode.equals("infection_fatality_rate")) {
			
			//initializing
			cte1.initialize();
			
			//loading county total cases
			if(arg1.containsArgument("sCountyTests") && arg1.getValueString("sCountyTests").equals("observation")) {
				cte1.countyCasesTotal("estimate","observation");
			}else{
				cte1.countyTests();
				cte1.countyCasesTotal("estimate","estimate");
			}
			
			cte1.stateInfectionFatalityRates(iWindowSize);
			cte1.countyInfectionFatalityRates(iWindowSize);
			
			mapEst.put("state",new String[]{"infection_fatality_rate"});
			mapEst.put("county",new String[]{"infection_fatality_rate"});
			
		}else if(sMode.equals("validation_simulation")) {

			
			mapObs.put("county",new String[]{"cases_observed", "cases_total", "tests", "mortality"});	
			mapObs.put("state",new String[]{"cases_observed","cases_total","tests", "mortality"});	
			mapObs.put("country",new String[]{"cases_observed","cases_total","tests", "mortality"});
			
			mapEst.put("county",new String[]{"tests", "cases_total", "infection_fatality_rate"});		
			mapEst.put("state",new String[]{"tests", "cases_total", "infection_fatality_rate"});
			mapEst.put("country",new String[]{"tests", "cases_total", "infection_fatality_rate","tests"});
			
			cts1 = new Country_Simulation(cte1, arg1.getValueInt("iRandomSeed"));
			
			for(int i=1;i<=arg1.getValueInt("iIterations");i++) {
				
				System.out.println("Simulation iteration " + i + " of " + arg1.getValueInt("iIterations") + "...");
				
				cts1.simulateInfectionsAndCases();
				cte1 = new Country_Estimation(cts1);
				cte1.initialize();
				cte1.countyTests();
				cte1.stateCasesTotal("estimate");
				cte1.countyCasesTotal("estimate","estimate");
				cte1.stateInfectionFatalityRates(iWindowSize);
				cte1.countyInfectionFatalityRates(iWindowSize);
				cte1.countryInfectionFatalityRates(iWindowSize);
				
				if(i==1) {
					DataIO.writeToFile(printIteration(mapObs, cte1, i, "observation",true),arg1.getValueString("sOutputPath"));
				}else {
					DataIO.writeToFile(printIteration(mapObs, cte1, i, "observation",false), arg1.getValueString("sOutputPath"), true);
				}
				DataIO.writeToFile(printIteration(mapEst, cte1, i, "estimate",false), arg1.getValueString("sOutputPath"), true);
			}
			return;
		}
	
		//outputting results
		DataIO.writeToFile(
				cte1.print(
						mapObs.get("county"),
						mapObs.get("state"),
						mapObs.get("country"),
						"observation",
						true,
						bTimeIntegrated),
				arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(
				cte1.print(
						mapEst.get("county"),
						mapEst.get("state"),
						mapEst.get("country"),
						"estimate",
						false,
						bTimeIntegrated),
				arg1.getValueString("sOutputPath"),true);
		System.out.println("Done.");
	}
	
	private static ArrayList<String> printIteration(HashMap<String,String[]> mapValues, Country_Estimation cte1, int iIteration, String sType, boolean bHeader){
		
		//lst1 = raw output
		//lst2 = output with or without header and iteration appended
		
		ArrayList<String> lst1;
		ArrayList<String> lst2;
		
		lst1 = cte1.print(
				mapValues.get("county"),
				mapValues.get("state"),
				mapValues.get("country"),
				sType,
				true,
				true);
		lst2 = new ArrayList<String>(lst1.size());
		if(bHeader==true) {
			lst2.add("ITERATION," + lst1.get(0));
		}
		for(int i=1;i<lst1.size();i++) {
			lst2.add(iIteration + "," + lst1.get(i));
		}
		return lst2;
	}
	
	
}