package edu.ucsf.geospatial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.ucsf.io.BiomIO;

//TODO write unit test

public class RawSurveyResults {

	/**Map from sample IDs to results**/
	private HashMap<String,RawSurveyResult> map1;
	
	public RawSurveyResults(BiomIO bio1){
		
		//dLat = latitude
		//dLon = longitude
		//setOccur = set of species that occur
		//i1 = counter
		
		int i1;
		double dLat;
		double dLon;
		HashSet<String> setOccur;
		
		map1 = new HashMap<String,RawSurveyResult>(bio1.axsSample.size());
		
		i1 = 0;
		for(String sSample:bio1.axsSample.getIDs()){
			
			i1++;
			if(i1 ==1 || (i1 % 100==0 && i1>0)){
				System.out.println("Loading survey result " + i1 + "...");
			}	
			dLat = Double.parseDouble(bio1.axsSample.getMetadata(sSample).get("latitude"));
			dLon = Double.parseDouble(bio1.axsSample.getMetadata(sSample).get("longitude"));
			setOccur = new HashSet<String>(bio1.getNonzeroValues(bio1.axsSample, sSample).keySet());
			map1.put(sSample, new RawSurveyResult(dLat, dLon, sSample, setOccur));	
		}
	}
	
	public Set<String> keySet(){
		return map1.keySet();
	}
	
	public RawSurveyResult get(String sSample){
		return map1.get(sSample);
	}

	public class RawSurveyResult{
	
		/**Latitude**/
		private double dLatitude;
		
		/**Longitude**/
		private double dLongitude;
		
		/**Set of observed species**/
		private HashSet<String> setSpecies;
		
		/**ID**/
		private String sID;
		
		public RawSurveyResult(double dLatitude, double dLongitude, String sSampleID, HashSet<String> setOccur){
			this.dLatitude = dLatitude;
			this.dLongitude = dLongitude;
			this.sID = sSampleID;
			setSpecies = setOccur;
		}
		
		public HashSet<String> speciesSet(){
			return setSpecies;
		}
		
		public double latitude(){
			return dLatitude;
		}
		
		public double longitude(){
			return dLongitude;
		}
		
		public String sampleID(){
			return sID;
		}
		
		public int size(){
			return setSpecies.size();
		}
	}
}
