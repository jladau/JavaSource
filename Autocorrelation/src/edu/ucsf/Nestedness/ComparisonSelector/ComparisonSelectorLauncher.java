package edu.ucsf.Nestedness.ComparisonSelector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import com.google.common.collect.HashMultimap;
import edu.ucsf.base.MultipartiteGraph;
import edu.ucsf.base.SemiOrderedPair;
import edu.ucsf.base.SimpleGraph;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Generates subsets for use in nestedness analyses.
 * @author jladau
 */

public class ComparisonSelectorLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//gph1 = graph under consideration
		//lstOut = output
		//iEdges = pairs to consider for each graph
		//mapOrder = returns ordering for each vertex
		//iGraphID = graph ID
		//sNestednessAxis = axis to use
		//bim1 = biom metadata object
		//map1 = map from categories to axis objects
		//i1 = counter for ordering
		//bio2 = second biom object being added
		//usg1 = usage object
		
		Usage usg1;
		HashMultimap<String,String> map1;
		String sNestednessAxis;
		HashMap<String,Integer> mapOrder;
		int iEdges;
		MultipartiteGraph<String> gph1;
		ArgumentIO arg1;
		BiomIO bio1;
		BiomIO bio2;
		ArrayList<String> lstOut;
		int iGraphID;
		BiomIOMetadata bim1;
		int i1;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
			"BiomIO",
			"rgsBIOMPaths",
			"sOutputPath",
			"rgsSampleMetadataFields",
			"sComparisonMode",
			"iRandomSeed",
			"sMetadataField",
			"sNestednessAxis",
			"iNestednessPairs"});
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
		
		//loading otu table;
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
		
		
		//loading number of edges
		iEdges = arg1.getValueInt("iNestednessPairs");
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("GRAPH_ID,GRAPH_TYPE,AXIS,VERTEX_1,VERTEX_2,VERTEX_1_CLASSIFICATION,VERTEX_2_CLASSIFICATION");
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//loading axis
		sNestednessAxis = arg1.getValueString("sNestednessAxis");
		if(sNestednessAxis.equals("sample")){
			bim1 = new BiomIOMetadata(bio1.axsSample,arg1.getValueString("sMetadataField"));
		}else if(sNestednessAxis.equals("observation")){
			bim1 = new BiomIOMetadata(bio1.axsObservation,arg1.getValueString("sMetadataField"));
		}else{
			throw new Exception("Axis not specified.");
		}
		
		//overall nesteness
		if(arg1.getValueString("sComparisonMode").equals("overall")){
			
			lstOut = new ArrayList<String>(iEdges);
			map1 = HashMultimap.create();
			for(String s:bim1.getKeys()){
				map1.put("all", s);
			}
			
			//relatively small number of samples/observations
			if(map1.size()<1500){
				gph1 = new MultipartiteGraph<String>(map1);
				gph1.loadRandomSubsetOfCompleteGraphComplement(iEdges, arg1.getValueInt("iRandomSeed"));
				writeToOutput(
						1, 
						gph1, 
						"overall", 
						sNestednessAxis, 
						bim1, 
						lstOut);
			
			//large number of samples/observations	
			}else{
				writeToOutput(
						1, 
						map1.values(),
						iEdges, 
						"overall", 
						sNestednessAxis, 
						bim1, 
						lstOut);	
			}
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
			
		//nestedness within and between sample types
		}else if(arg1.getValueString("sComparisonMode").equals("bytypes")){
			lstOut = new ArrayList<String>(iEdges*2);
			gph1 = new MultipartiteGraph<String>(bim1.toHashMultimap());
			gph1.loadRandomSubsetOfCompleteGraph(iEdges, arg1.getValueInt("iRandomSeed"));
			
			//TODO output types here
			writeToOutput(
					1, 
					gph1, 
					"betweentypes", 
					sNestednessAxis,  
					bim1, 
					lstOut);
			gph1.loadRandomSubsetOfCompleteGraphComplement(iEdges, arg1.getValueInt("iRandomSeed"));
			writeToOutput(
					2, 
					gph1, 
					"withintypes", 
					sNestednessAxis,  
					bim1, 
					lstOut);
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
			
		//nestedness between each pair of sample types
		}else if(arg1.getValueString("sComparisonMode").equals("betweeneachpairoftypes")){
			
			//looping through pairs of samples
			iGraphID = 0;
			for(String s:bim1.getMetadataValues()){
				for(String t:bim1.getMetadataValues()){
					if(!s.equals(t)){
						
						//clearing output
						lstOut = new ArrayList<String>(iEdges);
						
						//loading graph
						map1 = bim1.toHashMultimap(new String[]{s,t});
						gph1 = new MultipartiteGraph<String>(map1);
						gph1.loadRandomSubsetOfCompleteGraph(iEdges, arg1.getValueInt("iRandomSeed"));
						
						//ordering edges
						mapOrder = new HashMap<String,Integer>(gph1.setVertices.size());
						i1 =0;
						for(String u:map1.get(s)){
							mapOrder.put(u, i1);
							i1++;
						}
						for(String v:map1.get(t)){
							mapOrder.put(v, i1);
							i1++;
						}
						gph1.orderGraph(mapOrder);
						
						//writing output
						writeToOutput(
								iGraphID, 
								gph1, 
								"betweentypes",
								sNestednessAxis, 
								bim1,
								lstOut);
						DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
						
						//updating graph id
						iGraphID++;
					}
				}
			}
			
		//nestedness within each pair of sample types	
		}else if(arg1.getValueString("sComparisonMode").equals("withineachtype")){
			
			iGraphID = 0;
			for(String s:bim1.getMetadataValues()){
				
				//clearing output
				lstOut = new ArrayList<String>(iEdges);
				
				//loading graph
				map1 = bim1.toHashMultimap(new String[]{s});
				gph1 = new MultipartiteGraph<String>(map1);
				gph1.loadRandomSubsetOfCompleteGraphComplement(iEdges, arg1.getValueInt("iRandomSeed"));
				
				//writing output
				writeToOutput(
						iGraphID, 
						gph1, 
						"withineachtype",
						sNestednessAxis, 
						bim1,
						lstOut);
				DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
				
				//updating graph id
				iGraphID++;
			}
			
		}
		
		//terminating
		System.out.println("Done.");
	}
	
	private static void writeToOutput(int iGraphID, SimpleGraph<String> gph1, String sGraphType, String sNestednessAxis, BiomIOMetadata bim1, ArrayList<String> lstOut){
		for(SemiOrderedPair<String> sop1:gph1.setEdges){
			lstOut.add(iGraphID + "," + sGraphType + "," + sNestednessAxis + "," + sop1.o1 + "," + sop1.o2 + "," + bim1.getMetadata(sop1.o1) + "," + bim1.getMetadata(sop1.o2));
		}
	}
	
	private static void writeToOutput(int iGraphID, Collection<String> colKeys, int iEdges, String sGraphType, String sNestednessAxis, BiomIOMetadata bim1, ArrayList<String> lstOut){
		
		//set1 = set of semi-ordered pairs
		//rnd1index = random number generator
		//lst1 = list of keys
		//s1 = first key
		//s2 = second key
		
		String s1;
		String s2;
		Random rnd1;
		HashSet<SemiOrderedPair<String>> set1;
		ArrayList<String> lst1;
		
		rnd1 = new Random(1234);
		lst1 = new ArrayList<String>(colKeys.size());
		for(String s:colKeys){
			lst1.add(s);
		}
		set1 = new HashSet<SemiOrderedPair<String>>(iEdges);
		do{
			s1 = lst1.get(rnd1.nextInt(lst1.size()));
			s2 = lst1.get(rnd1.nextInt(lst1.size()));
			if(!s1.equals(s2)){
				set1.add(new SemiOrderedPair<String>(s1,s2));
			}
		}while(set1.size()<iEdges);
		for(SemiOrderedPair<String> sop1:set1){
			lstOut.add(iGraphID + "," + sGraphType + "," + sNestednessAxis + "," + sop1.o1 + "," + sop1.o2 + "," + bim1.getMetadata(sop1.o1) + "," + bim1.getMetadata(sop1.o2));
		}
	}
	
	
}
