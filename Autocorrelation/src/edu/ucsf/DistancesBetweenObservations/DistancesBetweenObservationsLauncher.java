package edu.ucsf.DistancesBetweenObservations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * This class implements spatial autocorrelation analyses for OTU tables.
 * @author Joshua Ladau, jladau@gmail.com
 */
public class DistancesBetweenObservationsLauncher {

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
		//map1 = map from observation IDs to counts
		//mapLat = map from sample IDs to latitudes
		//mapLon = map from sample IDs to longitudes
		//mapDistances = distances map
		
		HashMap<String,Double> mapDistances;
		HashMap<String,Double> mapLat;
		HashMap<String,Double> mapLon;
		HashMap<String,Double> map1;
		int iCounter;
		ArrayList<String> lstOut=null;
		ArgumentIO arg1;
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
		if(!arg1.containsArgument("iPrevalenceMinimum")){
			arg1.updateArgument("iPrevalenceMinimum", 10);
		}
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", true);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", false);
		}
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
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
			try{
				bio1.filterByNoMetadata(new String[]{"latitude","longitude"}, bio1.axsSample);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{	
			System.out.println("Error: spatial metadata not found. Exiting.");
			return;
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("TAXON" +
				",DISTANCE_OBSERVED_MEAN" +
				",DISTANCE_RANDOM_SUBSET_MEAN" +
				",DISTANCE_RANDOM_SUBSET_STDEV" +
				",DISTANCE_RANDOM_SUBSET_5_PERCENTILE" +
				",DISTANCE_RANDOM_SUBSET_95_PERCENTILE" +
				",N_OCCURRENCES" +
				",N_OBSERVATIONS");
		
		//looping through values
		iCounter=0;
		for(String s:bio1.axsObservation.getIDs()){
		
			//updating progress
			iCounter++;
			System.out.println("Analyzing " + s + ", taxon " + iCounter + " of " + bio1.axsObservation.size() + "...");
			
			//loading counts
			map1 = bio1.getItem(bio1.axsObservation, s);
			
			//loading coordinates
			mapLat = new HashMap<String,Double>(bio1.axsSample.size());
			mapLon = new HashMap<String,Double>(bio1.axsSample.size());
			for(String t:bio1.axsSample.getIDs()){
				mapLat.put(t, Double.parseDouble(bio1.axsSample.getMetadata(t).get("latitude")));
				mapLon.put(t, Double.parseDouble(bio1.axsSample.getMetadata(t).get("longitude")));
			}
			
			//loading distances
			mapDistances = distances(map1, mapLat, mapLon, 1000);
			
			//outputting results
			lstOut.add(s +
					"," + mapDistances.get("DISTANCE_OBSERVED_MEAN") +
					"," + mapDistances.get("DISTANCE_RANDOM_SUBSET_MEAN") +
					"," + mapDistances.get("DISTANCE_RANDOM_SUBSET_STDEV") +
					"," + mapDistances.get("DISTANCE_RANDOM_SUBSET_5_PERCENTILE") +
					"," + mapDistances.get("DISTANCE_RANDOM_SUBSET_95_PERCENTILE") +
					"," + mapDistances.get("N_OCCURRENCES") +
					"," + map1.size());
		}
			
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static HashMap<String,Double> distances(HashMap<String,Double> mapAbundances, HashMap<String,Double> mapLat, HashMap<String,Double> mapLon, int iRandomSubsets){
		
		//ert1 = earth geometry object
		//lst1 = current set of samples
		//d1 = total distance
		//d2 = total count
		//mapOut = output
		//iOccurrences = number of occurrences
		//lst2 = list of all location names
		//lst3 = list of random sample mean distances
		
		ArrayList<Double> lst3;
		EarthGeometry ert1;
		ArrayList<String> lst1;
		double d1;
		double d2;
		HashMap<String,Double> mapOut;
		int iOccurrences;
		ArrayList<String> lst2;;
		
		//initializing variables
		ert1 = new EarthGeometry();
		mapOut = new HashMap<String,Double>();
		lst3 = new ArrayList<Double>(iRandomSubsets);
		
		//loading list of distances for samples where there are occcurrences
		lst1 = new ArrayList<String>(mapAbundances.size());
		lst2 = new ArrayList<String>(mapAbundances.size());
		for(String s:mapAbundances.keySet()){
			if(mapAbundances.get(s)>0){
				lst1.add(s);
			}
			lst2.add(s);
		}
		iOccurrences = lst1.size();
		d1=0.;
		d2=0.;
		for(int i=1;i<lst1.size();i++){
			for(int j=0;j<i;j++){
				d1+=ert1.orthodromicDistanceWGS84(mapLat.get(lst1.get(i)), mapLon.get(lst1.get(i)), mapLat.get(lst1.get(j)), mapLon.get(lst1.get(j)));
				d2++;
			}
		}
		mapOut.put("DISTANCE_OBSERVED_MEAN", d1/d2);
		mapOut.put("N_OCCURRENCES", (double) lst1.size());
		
		//looping through random subsets of the same size
		for(int k=0;k<iRandomSubsets;k++){
			
			//*******************
			System.out.println(k);
			//*******************
			
			Collections.shuffle(lst2);
			
			lst1 = new ArrayList<String>(iOccurrences);
			for(int i=0;i<iOccurrences;i++){
				lst1.add(lst2.get(i));
			}
			d1=0.;
			d2=0.;
			for(int i=1;i<lst1.size();i++){
				for(int j=0;j<i;j++){
					d1+=ert1.orthodromicDistanceWGS84(mapLat.get(lst1.get(i)), mapLon.get(lst1.get(i)), mapLat.get(lst1.get(j)), mapLon.get(lst1.get(j)));
					d2++;
				}
			}
			lst3.add(d1/d2);
		}
		mapOut.put("RANDOM_SUBSET_COUNT", (double) iRandomSubsets);
		mapOut.put("DISTANCE_RANDOM_SUBSET_MEAN", ExtendedMath.mean(lst3));
		mapOut.put("DISTANCE_RANDOM_SUBSET_STDEV", ExtendedMath.standardDeviationP(lst3)*Math.sqrt((double) iRandomSubsets)/Math.sqrt((double) iRandomSubsets -1.));
		Collections.sort(lst3);
		mapOut.put("DISTANCE_RANDOM_SUBSET_5_PERCENTILE", lst3.get((int) Math.floor(0.05*((double) iRandomSubsets))));
		mapOut.put("DISTANCE_RANDOM_SUBSET_95_PERCENTILE", lst3.get((int) Math.floor(0.95*((double) iRandomSubsets))));
		
		//outputting results
		return mapOut;
	}	
}