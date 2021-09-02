package edu.ucsf.Nestedness.ComparisonSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;

import edu.ucsf.io.BiomIO;

public class BiomIOMetadata {

	/**Map from axis elements to metadata values**/
	private HashMap<String,String> mapMetadata;
	
	public BiomIOMetadata(BiomIO.Axis axs1, String sMetadataField){
		 mapMetadata = new HashMap<String,String>();
		 for(String s:axs1.getIDs()){
			 mapMetadata.put(s, axs1.getMetadata(s).get(sMetadataField));
		 }
	}
	
	/**
	 * Gets mapping from metadata values to axis objects
	 */
	public HashMultimap<String,String> toHashMultimap(){
		
		//mapOut = output
		
		HashMultimap<String, String> mapOut;
		
		mapOut = HashMultimap.create();
		for(String s:mapMetadata.keySet()){
			mapOut.put(mapMetadata.get(s), s);
		}
		return mapOut;
	}
	
	/**
	 * Gets metadata associated with specified key
	 * @param sKey Key for lookup
	 */
	public String getMetadata(String sKey){
		return mapMetadata.get(sKey);
	}
	
	/**
	 * Gets mapping from metadata values to axis objects
	 * @param rgsMetadataKeysToInclude Metadata keys to include
	 */
	public HashMultimap<String,String> toHashMultimap(String[] rgsMetadataKeysToInclude){
		
		//mapOut = output
		//set1 = set of metadata keys to include
		
		HashMultimap<String, String> mapOut;
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		for(String s:rgsMetadataKeysToInclude){
			set1.add(s);
		}
		mapOut = HashMultimap.create();
		for(String s:mapMetadata.keySet()){
			if(set1.contains(mapMetadata.get(s))){
				mapOut.put(mapMetadata.get(s), s);
			}
		}
		return mapOut;
	}
	
	public ArrayList<String> getMetadataValues(){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(mapMetadata.size());
		for(String s:mapMetadata.values()){
			if(!lstOut.contains(s)){
				lstOut.add(s);
			}
		}
		return lstOut;
	}
	
	public ArrayList<String> getKeys(){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(mapMetadata.size());
		for(String s:mapMetadata.keySet()){
			lstOut.add(s);
		}
		return lstOut;
	}
	
}
