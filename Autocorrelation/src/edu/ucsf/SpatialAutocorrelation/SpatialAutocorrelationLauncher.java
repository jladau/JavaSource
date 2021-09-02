package edu.ucsf.SpatialAutocorrelation;

import java.util.ArrayList;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import edu.ucsf.base.ClusterIterator;
import edu.ucsf.base.Permutation;
import edu.ucsf.geospatial.SpatialAutocorrelation;
import edu.ucsf.geospatial.SpatialWeightsMatrix;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * This class implements spatial autocorrelation analyses for OTU tables.
 * @author Joshua Ladau, jladau@gmail.com
 */
public class SpatialAutocorrelationLauncher {

	@SuppressWarnings("unchecked")
	public static void main(String rgsArgs[]) throws Exception{
		
		//iCounter = counter
		//sMode = latitude-longitude or euclidean
		//lstOut = output
		//rgp1 = random permutations
		//spw1 = spatial weights matrix
		//arg1 = arguments
		//spa1 = geospatial statistics object
		//cit1 = cluster iterator
		//bio1 = biom object
		//usg1 = usage object
		
		int iCounter;
		String sMode;
		ArrayList<String> lstOut=null;
		Permutation<Integer> rgp1[];
		SpatialWeightsMatrix spw1 = null;
		ArgumentIO arg1;
		SpatialAutocorrelation spa1;
		ClusterIterator cit1;
		BiomIO bio1;
		Usage usg1;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
				"BiomIO",
				"sOutputPath",
				"ClusterIterator",
				"SpatialAutocorrelation"});
		usg1.printUsage(rgsArgs);
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading defaults
		if(!arg1.containsArgument("sAutocorrelationAnalysis")){
			arg1.updateArgument("sAutocorrelationAnalysis", "MoransI");
		}
		if(!arg1.containsArgument("sSpatialWeighting")){
			arg1.updateArgument("sSpatialWeighting", "binary");
		}
		if(!arg1.containsArgument("iPrevalenceMinimum")){
			arg1.updateArgument("iPrevalenceMinimum", 10);
		}
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", true);
		}
		if(!arg1.containsArgument("bOutputData")){
			arg1.updateArgument("bOutputData", true);
		}
		if(!arg1.containsArgument("bOutputDistances")){
			arg1.updateArgument("bOutputDistances", false);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", false);
		}
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
		}
		if(!arg1.containsArgument("rgsDirectionNeighborhoods")){
			arg1.updateArgument("rgsDirectionNeighborhoods", (Strings.repeat("0-360,",arg1.getValueStringArray("rgsDistanceNeighborhoods").length-1) + "0-360").split(","));
		}
		if(!arg1.containsArgument("rgsTimeNeighborhoods")){
			arg1.updateArgument("rgsTimeNeighborhoods", (Strings.repeat("0-12,",arg1.getValueStringArray("rgsDistanceNeighborhoods").length-1) + "0-12").split(","));
		}else{
			arg1.updateArgument("rgsRequiredSampleMetadata", "datetime");
		}
		
		//loading data
		System.out.println("Loading data...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//loading mode and filtering samples without metadata
		System.out.println("Filtering samples without location metadata...");
		if(bio1.axsSample.hasMetadataField("latitude") && bio1.axsSample.hasMetadataField("longitude")){
			sMode="latitude-longitude";
			try{
				bio1.filterByNoMetadata(new String[]{"latitude","longitude"}, bio1.axsSample);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(bio1.axsSample.hasMetadataField("x") && bio1.axsSample.hasMetadataField("y")){
			sMode="euclidean";
			try{
				bio1.filterByNoMetadata(new String[]{"x","y"}, bio1.axsSample);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{	
			System.out.println("Error: spatial metadata not found. Exiting.");
			return;
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("Taxon" +
				",Morans I" +
				",Randomized p-value" +
				",Z-score" +
				",Distance neighborhood (km)" +
				",Direction neighborhood (degrees)" +
				",Time neighborhood (months)" +
				",P-value iterations" +
				",Number non-zero weights" +
				",Fraction observations with neighbors" +
				",Taxon prevalence (number of samples)" +
				",Taxon mean relative abundance");
		
		//initializing cluster iterator
		cit1 = new ClusterIterator(arg1.getValueInt("iTaskID"),arg1.getValueInt("iTotalTasks"));
		
		//looping through distances
		for(int k=0;k<arg1.getValueStringArray("rgsDistanceNeighborhoods").length;k++){
			
			//initializing spatial weights matrix
			try {
				spw1 = 	new SpatialWeightsMatrix(bio1,
						Range.closed(Double.parseDouble(arg1.getValueStringArray("rgsDistanceNeighborhoods")[k].split("-")[0]),Double.parseDouble(arg1.getValueStringArray("rgsDistanceNeighborhoods")[k].split("-")[1])),
						Range.closed(Double.parseDouble(arg1.getValueStringArray("rgsDirectionNeighborhoods")[k].split("-")[0]),Double.parseDouble(arg1.getValueStringArray("rgsDirectionNeighborhoods")[k].split("-")[1])),
						Range.closed(Double.parseDouble(arg1.getValueStringArray("rgsTimeNeighborhoods")[k].split("-")[0]),Double.parseDouble(arg1.getValueStringArray("rgsTimeNeighborhoods")[k].split("-")[1])),
						arg1.getValueString("sSpatialWeighting"), 
						sMode,
						false);
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
			
			//loading permutations
			rgp1 = new Permutation[arg1.getValueInt("iInitialScreeningIterations")];
			for(int i=0;i<arg1.getValueInt("iInitialScreeningIterations");i++){
				rgp1[i]=new Permutation<Integer>(spw1.getVertexIDs());
				rgp1[i].loadRandomPermutation();	
			}
			
			//looping through values
			iCounter=0;
			for(String s:bio1.axsObservation.getIDs()){
			
				//updating progress
				iCounter++;
				System.out.println("Analyzing " + s + ", taxon " + iCounter + " of " + bio1.axsObservation.size() + ", iteration " + (k+1) + " of " + arg1.getValueStringArray("rgsDistanceNeighborhoods").length + "...");
			
				//updating cluster iterator
				cit1.next();
				if(cit1.bInclude==false){
					continue;
				}
				
				//loading values
				for(String t:bio1.axsSample.getIDs()){
					spw1.getVertex(bio1.axsSample.getIndex(t)).put("dValue", bio1.getValueByIDs(s, t));
				}
				
				//loading geospatial statistics object
				spa1 = new SpatialAutocorrelation(spw1);
				
				//running analysis
				if(arg1.getValueString("sAutocorrelationAnalysis").startsWith("MoransI")){
					spa1.calculateMoransIMCMC(rgp1,arg1.getValueInt("iMCMCIterations"),arg1.getValueInt("iMCMCChains"),true);
					if(Double.isInfinite(spa1.mrn1.dObsMoransI)){
						spa1.mrn1.dObsMoransI=Double.NaN;
						spa1.mrn1.dZMoransI=Double.NaN;
						spa1.mrn1.dPValueMoransI=Double.NaN;
						spa1.mrn1.iPValueIterations=0;
					}
					
					lstOut.add(s + "," + spa1.mrn1.dObsMoransI + 
							"," + spa1.mrn1.dPValueMoransI + 
							"," + spa1.mrn1.dZMoransI + 
							"," + spw1.rngDistances.lowerEndpoint() + "-" + spw1.rngDistances.upperEndpoint() +  
							"," + spw1.rngDirections.lowerEndpoint() + "-" + spw1.rngDirections.upperEndpoint() +
							"," + spw1.rngTimeDifferences.lowerEndpoint() + "-" + spw1.rngTimeDifferences.upperEndpoint() +
							"," + spa1.mrn1.iPValueIterations + 
							"," + spw1.iNonzeroWeights + 
							"," + spw1.dFractionWithNeighbors + 
							"," + bio1.getNonzeroCount(bio1.axsObservation, s) + 
							"," + bio1.getMean(bio1.axsObservation, s));
				}else if(arg1.getValueString("sAutocorrelationAnalysis").equals("MoranScatter")){
					lstOut=spa1.calculateMoranScatterPlot();
					break;
				}	
			}
		}
		 
		//printing results and writing completion file
		if(arg1.getValueInt("iTaskID")==-9999 || arg1.getValueInt("iTotalTasks")==-9999){	
			if(arg1.getValueBoolean("bOutputData")){
				DataIO.writeToFile(outputDataTable(bio1), arg1.getValueString("sOutputPath").replace(".csv",".data.csv"));
			}
			if(arg1.getValueBoolean("bOutputDistances")){
				//gph1 = loadGraph(bio1, sMode, spw1, true);
				//DataIO.writeToFile(outputDistances(gph1), arg1.getValueString("sOutputPath").replace(".csv",".distances.csv"));
			}
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		}else{
			if(arg1.getValueBoolean("bOutputData") && arg1.getValueInt("iTaskID")==arg1.getValueInt("iTotalTasks")){	
				DataIO.writeToFile(outputDataTable(bio1), arg1.getValueString("sOutputPath").replace(".csv",".data.csv"));
			}
			if(arg1.getValueBoolean("bOutputDistances")){
				//gph1 = loadGraph(bio1, sMode, spw1, true);
				//DataIO.writeToFileWithCompletionFile(outputDistances(gph1), arg1.getValueString("sOutputPath").replace(".csv",".distances.csv"), arg1.getValueInt("iTaskID"));
			}
			DataIO.writeToFileWithCompletionFile(lstOut, arg1.getValueString("sOutputPath"), arg1.getValueInt("iTaskID"));
		}
		
		//terminating
		System.out.println("Done.");
	}
	
	/**
	 * Outputs data time differences and geographic distances
	 * @param gph1 Graph with distances
	 * @return List of distances and time differences
	 */
	@SuppressWarnings("unused")
	private static ArrayList<String> outputDistances(SpatialWeightsMatrix spw1){
		
		//lstOut = output
		//lstTime = list of time differences
		//lstDist = list of differences
		
		ArrayList<String> lstOut;
		ArrayList<Double> lstTime;
		ArrayList<Double> lstDist;
		
		lstTime = spw1.getEdgeProperties("dTimeDifference");
		lstDist = spw1.getEdgeProperties("dLength");
		lstOut = new ArrayList<String>(lstTime.size()+1);
		lstOut.add("Geographic distance" +
				",Time difference");
		for(int i=0;i<lstDist.size();i++){
			lstOut.add(lstDist.get(i) + "," + lstTime.get(i));
		}
		return lstOut;
	}
	
	/**
	 * Outputs table of data that was analyzed
	 * @param bio1 BIOM object
	 */
	private static ArrayList<String> outputDataTable(BiomIO bio1){
		
		//lstOut = output
		//sbl1 = current output line
		
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		lstOut = new ArrayList<String>(bio1.axsSample.size()+1);
		sbl1 = new StringBuilder();
		sbl1.append("SampleID,Latitude,Longitude,DateTime");
		for(int j=0;j<bio1.axsObservation.size();j++){
			sbl1.append("," + bio1.axsObservation.getID(j));
		}
		lstOut.add(sbl1.toString());
		for(int i=0;i<bio1.axsSample.size();i++){
			sbl1 = new StringBuilder();
			sbl1.append(bio1.axsSample.getID(i) + "," + bio1.axsSample.getMetadata(i).get("latitude") + "," + bio1.axsSample.getMetadata(i).get("longitude") + "," + bio1.axsSample.getMetadata(i).get("datetime"));
			for(int j=0;j<bio1.axsObservation.size();j++){
				sbl1.append("," + bio1.getValueByIndices(j, i));
			}
			lstOut.add(sbl1.toString());
		}
		return lstOut;
	}
}
