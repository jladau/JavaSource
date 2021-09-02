package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.base.MutableBetaDiversity;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

//TODO rename response data to include covariate data
//TODO figure out why so many zero values for observation are being output

public class BetaDiversityAssociationLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//datSamplePairs = list of pairs of samples to consider
		//lstSamplePairs = list of sample pairs
		//lstSamplePairAliases = list of sample pair aliases (if applicable; for bootstrapping)
		//mapSamplePairAliases = map from sample pair aliases to sample pairs (if applicable; for bootstrapping)
		//datResponses = response variable map from sample_1 names to response values
		//setIncl = set of included taxa
		//sSample1 = sample 1 header
		//sSample2 = sample 2 header
		//sResponse = response header
		//sMergedHeader = merged sample ID header
		//lstY = y values
		//sMetric = beta-diversity statistic to use, 'bray_curtis', 'jaccard'
		//sMode = type of analysis to run
		//mapShortNames = map from taxon names tp short names
		//datGroups = taxon groups data
		//mapResponses = response map
		//lstRowsToRemove = list of rows to remove
		//dad1 = beta-diversity association data
		//lstOut = output
		//mapCovariates = map from sample pairs to covariate maps
		//setGroupsToConsider = set of groups to consider
		//bRange = flag for whether to find range of percentiles (e.g., interquartile range)
		
		boolean bRange;
		HashSet<String> setGroupsToConsider;
		ArrayList<String> lstOut = null;
		ArrayList<Integer> lstRowsToRemove;
		HashMap<String,Double> mapResponses;
		HashMap<String,String> mapShortNames;
		String sMode;
		ArrayList<Double> lstY;
		ArrayList<String> lstSamplePairs;
		ArrayList<String> lstSamplePairAliases;
		HashMap<String,String> mapSamplePairAliases;
		ArgumentIO arg1;
		DataIO datResponses;
		BiomIO bio1;
		String sSample1;
		String sSample2;
		String sResponse;
		String sMergeHeader;
		String sMetric;
		DataIO datGroups;
		BetaDiversityAssociationData dad1;
		HashMap<String,HashMap<String,Double>> mapCovariates;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		datResponses = new DataIO(arg1.getValueString("sResponseDataPath"));
		sSample1 = arg1.getValueString("sSample1Header");
		sSample2 = arg1.getValueString("sSample2Header");
		sResponse = arg1.getValueString("sResponse");
		sMergeHeader = arg1.getValueString("sMergeHeader");
		sMetric = arg1.getValueString("sMetric");
		sMode = arg1.getValueString("sMode");
		
		//loading list of taxa to group
		datGroups = new DataIO(arg1.getValueString("sTaxonGroupsMapPath"));

		//loading short names map
		mapShortNames = datGroups.getStringMap(new String[] {"TAXON_ID"},new String[] {"TAXON_ID_SHORT"});
		
		//removing pairs of samples that do not appear in biom file
		lstRowsToRemove = new ArrayList<Integer>(datResponses.iRows);
		for(int i=1;i<datResponses.iRows;i++) {
			if(!bio1.axsSample.getIDs().contains(datResponses.getString(i,sSample1)) || !bio1.axsSample.getIDs().contains(datResponses.getString(i,sSample2))) {
				lstRowsToRemove.add(i);
			}
		}
		datResponses.removeRows(lstRowsToRemove);
		
		//loading response vector
		lstY = datResponses.getDoubleColumn(sResponse);
		
		//loading list of groups to consider
		setGroupsToConsider = new HashSet<String>(datGroups.iRows);
		for(int i=1;i<datGroups.iRows;i++) {
			if(!datGroups.hasHeader("INCLUDE") || datGroups.getString(i,"INCLUDE").equals("true")) {
				setGroupsToConsider.add(datGroups.getString(i,"GROUP"));
			}
		}
		
		//loading sample pairs and responses
		lstSamplePairs = datResponses.getStringColumns(new String[]{sSample1, sSample2});
		mapResponses = new HashMap<String,Double>(lstSamplePairs.size());
		if(datResponses.hasHeader("SAMPLE_PAIR_ALIAS")) {
			lstSamplePairAliases = datResponses.getStringColumn("SAMPLE_PAIR_ALIAS");
			for(int i=0;i<lstSamplePairs.size();i++){
				mapResponses.put(lstSamplePairAliases.get(i), lstY.get(i));
			}
			mapSamplePairAliases = new HashMap<String,String>(lstSamplePairAliases.size());
			for(int k=0;k<lstSamplePairAliases.size();k++) {
				mapSamplePairAliases.put(lstSamplePairAliases.get(k),lstSamplePairs.get(k));
			}
		}else{
			for(int i=0;i<lstSamplePairs.size();i++){
				mapResponses.put(lstSamplePairs.get(i), lstY.get(i));
			}
			mapSamplePairAliases = null;
		}
				
		//loading covariates
		mapCovariates = new HashMap<String,HashMap<String,Double>>(datResponses.getHeaders().size());
		for(String sCovariate:datResponses.getHeaders()) {
			if(sCovariate.startsWith("COVARIATE")){
				mapCovariates.put(sCovariate, datResponses.getDoubleMap(new String[] {sSample1,sSample2}, new String[] {sCovariate}));
			}
		}
		
		//loading flag for whether to examine range vs percentile
		if(arg1.containsArgument("bRange")) {
			bRange = arg1.getValueBoolean("bRange");
		}else {
			bRange = false;
		}
		
		//loading data
		dad1 = new BetaDiversityAssociationData(
				mapResponses, 
				datResponses.getStringMap(new String[] {sSample1,sSample2}, new String[] {sMergeHeader}), 
				new MutableBetaDiversity(bio1, lstSamplePairs, sMetric, mapShortNames, mapSamplePairAliases),
				arg1.getValueInt("iNullIterations"),
				arg1.getValueInt("iPartitioningOrders"),
				datGroups.getStringMultimap(new String[] {"GROUP"},new String[] {"TAXON_ID_SHORT"}),
				arg1.getValueDouble("dObservedValueThreshold"),
				arg1.getValueInt("iWindowSize"),
				bio1,
				mapShortNames,
				mapCovariates,
				arg1.getValueDoubleArray("rgdThresholds"),
				arg1.getValueString("sRegressionType"),
				setGroupsToConsider,
				bRange);
		
		//selecting analysis
		if(sMode.equals("taxon_ids")) {
			System.out.println("Outputting taxon IDs...");
			lstOut = dad1.printTaxonIDs();
		
		}else if(sMode.equals("sample_ids")) {
			System.out.println("Outputting sample IDs...");
			lstOut = dad1.printSampleIDs();
		
		}else if(sMode.equals("effects")) {
			System.out.println("Running effects analysis...");
			dad1.loadInteractions();
			lstOut = dad1.int1.effects();
		
		}else if(sMode.equals("covariate_significance")) {
			System.out.println("Covariate performance significance...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.significancePerformanceCovariates();
	
		}else if(sMode.equals("significance")) {
			System.out.println("Overall performance significance...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.significancePerformanceSelectedTaxa();
		
		}else if(sMode.equals("significance_slope")) {
			System.out.println("Overall slope significance...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.significanceSlopeSelectedTaxa();
		
		}else if(sMode.equals("print_predicted_values")) {
			System.out.println("Printing predicted values...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.printPredictions();
			
		}else if(sMode.equals("print_data_merged")) {
			System.out.println("Printing data...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.printAllData("merged");
		
		}else if(sMode.equals("print_data_unmerged")) {
			System.out.println("Printing data...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.printAllData("unmerged");

		}else if(sMode.equals("print_transformed_data")) {
			System.out.println("Printing transformed data...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.printTransformedData();

		}else if(sMode.equals("coefficients")) {
			System.out.println("Finding coefficients...");
			dad1.loadInteractions();
			lstOut = dad1.int1.coefficients();
		
		}else if(sMode.equals("two_factor_interactions")) {
			System.out.println("Running two factor interaction analysis...");
			dad1.loadInteractions();
			lstOut = dad1.int1.twoFactorInteractions();
		
		}else if(sMode.equals("hierarchical_partitioning")) {
			System.out.println("Running hierarchical partitioning analysis...");
			dad1.loadHierarchicalPartitioning();
			lstOut = dad1.hir1.partition();
		}
		
		/*
		}else if(sMode.equals("forward_model_selection")) {
			System.out.println("Forward model selection...");
			dad1.loadForwardModelSelection();
			lstOut = dad1.fwm1.forwardModelSelection();

		}else if(sMode.equals("covariate_data")) {
			System.out.println("Covariate data...");
			dad1.loadSignificance();
			lstOut = dad1.sig1.printDataCovariates();

		}else if(sMode.equals("print_regression_data")) {
			System.out.println("Regression data...");
			dad1.loadSignificance();
			//***********************************
			lstOut = dad1.sig1.printDataSelectedTaxa();
			//lstOut = dad1.sig1.printDataAllTaxa();
			//***********************************
			
		}else if(sMode.equals("print_predictor_data_all_subsets")) {
			System.out.println("Printing data...");
			dad1.loadInteractions();
			lstOut = dad1.int1.printDataAllSubsets();
		
		}else if(sMode.equals("print_predictor_data")) {
			System.out.println("Printing data...");
			dad1.loadInteractions();
			lstOut = dad1.int1.printData();
		*/
				
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}