package edu.ucsf.Nestedness.Calculator;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.base.NestednessNODF;
import edu.ucsf.base.NestednessOrderedNODF;
import edu.ucsf.base.SemiOrderedPair;
import edu.ucsf.base.SimpleGraph;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class CalculatorLauncher {


	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//dTime = start time
		//nes1 = nestedness object
		//mapSamplesGraphs = keys are IDs, values are graphs for samples to consider
		//mapObservationsGraphs = keys are IDs, values are graphs for samples to consider
		//datGraphs = graph data
		//lstOut = output
		//iCounter = counter for updating progress
		//iEdges = number of edges
		//bio2 = second biom object being added
		//usg1 = usage object
		
		Usage usg1;
		int iEdges;
		int iCounter;
		ArgumentIO arg1;
		BiomIO bio1;
		NestednessNODF nes1;
		HashMap<String,SimpleGraph<String>> mapSamplesGraphs;
		HashMap<String,SimpleGraph<String>> mapObservationsGraphs;
		DataIO datGraphs;
		ArrayList<String> lstOut;
		BiomIO bio2;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
			"bSimulate",
			"BiomIO",
			"rgsBIOMPaths",
			"sComparisonsPath",
			"bOrderedNODF",
			"sNestednessAxis",
			"sNestednessNullModel",
			"iNullModelIterations",
			"sOutputPath",
			"iRandomSeed"});
		usg1.printUsage(rgsArgs);
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading defaults
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", true);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", false);
		}
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
		}
		if(!arg1.containsArgument("bSimulate")){
			arg1.updateArgument("bSimulate", false);
		}
		
		//loading otu table
		System.out.println("Loading data...");
		if(arg1.containsArgument("sBIOMPath")){	
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		}else{
			bio1 = new BiomIO(arg1.getValueStringArray("rgsBIOMPaths")[0], arg1.getAllArguments());
			for(int i=1;i<arg1.getValueStringArray("rgsBIOMPaths").length;i++){
				bio2 = new BiomIO(arg1.getValueStringArray("rgsBIOMPaths")[i], arg1.getAllArguments());
				bio1 = bio1.merge(bio2);
			}
		}
		
		//******************************
		//bio1.collapse("empo_3", bio1.axsSample, false);
		//******************************
		
		//loading graphs
		datGraphs = new DataIO(arg1.getValueString("sComparisonsPath"));
		mapSamplesGraphs = new HashMap<String,SimpleGraph<String>>();
		mapObservationsGraphs = new HashMap<String,SimpleGraph<String>>();
		
		for(int i=1;i<datGraphs.iRows;i++){
			if(!mapSamplesGraphs.containsKey(datGraphs.getString(i, "GRAPH_ID"))){
				mapSamplesGraphs.put(datGraphs.getString(i, "GRAPH_ID"), new SimpleGraph<String>());
				mapObservationsGraphs.put(datGraphs.getString(i, "GRAPH_ID"), new SimpleGraph<String>());
			}
			if(datGraphs.getString(i, "AXIS").equals("sample")){
				mapSamplesGraphs.get(datGraphs.getString(i, "GRAPH_ID")).addEdge(new SemiOrderedPair<String>(datGraphs.getString(i, "VERTEX_1"),datGraphs.getString(i, "VERTEX_2")));
			}else if(datGraphs.getString(i, "AXIS").equals("observation")){
				mapObservationsGraphs.get(datGraphs.getString(i, "GRAPH_ID")).addEdge(new SemiOrderedPair<String>(datGraphs.getString(i, "VERTEX_1"),datGraphs.getString(i, "VERTEX_2")));
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>(mapSamplesGraphs.size());
		if(arg1.getValueBoolean("bSimulate")){
			lstOut.add("GRAPH_ID,GRAPH_EDGE_COUNT,NODF_OBSERVED,NODF_NULL_MEAN,NODF_NULL_STDEV,NODF_SES,PR_LT_OBSERVED,PR_ET_OBSERVED,PR_GT_OBSERVED");
		}else{
			lstOut.add("GRAPH_ID,GRAPH_EDGE_COUNT,NODF_OBSERVED,NODF_NULL_MEAN,NODF_NULL_STDEV,NODF_SES");
		}
			
		//initializing nodf object
		if(arg1.getValueBoolean("bOrderedNODF")){
			nes1 = new NestednessOrderedNODF(bio1, 1234);
		}else{
			nes1 = new NestednessNODF(bio1, 1234);
		}
		
		//looping through graphs
		iCounter = 0;
		for(String sGraph:mapSamplesGraphs.keySet()){
		
			//*************************
			if(sGraph.equals("13")){
				System.out.println("HERE.");
			}
			//*************************
			
			
			//updating progress
			iCounter++;
			System.out.println("Analyzing graph " + iCounter + " of " + mapSamplesGraphs.size() + "...");
			
			//loading number of edges
			if(arg1.getValueString("sNestednessAxis").equals("sample")){
				iEdges=mapSamplesGraphs.get(sGraph).setEdges.size();
			}else{
				iEdges=mapObservationsGraphs.get(sGraph).setEdges.size();
			}
			
			//loading nestedness object
			nes1.setGraphs(mapSamplesGraphs.get(sGraph), mapObservationsGraphs.get(sGraph));
			if(arg1.getValueBoolean("bSimulate")){			
				nes1.loadNODFSimulated(arg1.getValueString("sNestednessAxis"), arg1.getValueString("sNestednessNullModel"), arg1.getValueInt("iNullModelIterations"), arg1.getValueInt("iRandomSeed"));
				
				//outputting results
				lstOut.add(sGraph 
						+ "," + iEdges
						+ "," + nes1.ndf1.dObserved
						+ "," + nes1.ndf1.dSimulatedMean
						+ "," + nes1.ndf1.dSimulatedStDev
						+ "," + nes1.ndf1.dStandardizedEffect
						+ "," + nes1.ndf1.dPrLT
						+ "," + nes1.ndf1.dPrET
						+ "," + nes1.ndf1.dPrGT);
			}else{
				nes1.loadNODF(arg1.getValueString("sNestednessAxis"), arg1.getValueString("sNestednessNullModel"));
				
				//outputting results
				lstOut.add(sGraph 
						+ "," + iEdges
						+ "," + nes1.ndf1.dObserved
						+ "," + nes1.ndf1.dExpectation
						+ "," + Math.sqrt(nes1.ndf1.dVariance)
						+ "," + nes1.ndf1.dStandardizedEffect);
			}
		}
			
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}