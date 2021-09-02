package edu.ucsf.geospatial;


import java.util.HashMap;
import java.util.HashSet;

/**
 * This class contains code for a sampling location
 * @author jladau
 *
 */

//TODO write unit tests

public class SamplingLocation {

	//dRadius = radius of sampling location
	//dLat = latitude of center of sampling location
	//dLon = longitude of center of sampling location
	//setSpecies = set of species observed in sampling location
	//dSpecies = number of species observed at sampling location
	//dIntersection = number of species shared with another (unspecified) location
	//dSymDifference = number of unique species
	
	public double dSpecies;
	public double dIntersection;
	public double dSymDifference;
	public double dRadius;
	public double dLat;
	public double dLon;
	public HashSet<String> setSpecies = null;
	
	/**
	 * Constructor
	 */
	public SamplingLocation(double dLat, double dLon, double dRadius){
		this.dLat = dLat;
		this.dLon = dLon;
		this.dRadius = dRadius;
	}
	
	/**
	 * Loads the set of species occurring in sampling location
	 * @param mapRange(sSpecies) = returns the range for the given species
	 */
	public void loadSpecies(HashMap<String,SphericalMultiPolygon> mapRange, GeographicPointBounds bds1, HashSet<String> setEverywhereSpecies, HashSet<String> setNowhereSpecies){
		
		//initializing variables
		dSpecies = -9999;
		dIntersection = -9999;
		dSymDifference = -9999;
		
		//initializing set of species
		setSpecies = new HashSet<String>(1000);
		
		//looping through species
		for(String s:mapRange.keySet()){
		
			//checking if nowhere or everywhere
			if(setEverywhereSpecies.contains(s)){
				setSpecies.add(s);
				continue;
			}
			if(setNowhereSpecies.contains(s)){
				continue;
			}
			
			//checking if sampling bounds overlap range bounds
			if(mapRange.get(s).getBounds().intersects(bds1)==false){
				continue;
			}
			
			//updating species set
			if(mapRange.get(s).contains(dLat, dLon)){
				setSpecies.add(s);
			}
		}
		
		//saving number of species
		dSpecies = (double) setSpecies.size();
	}
	
	/**
	 * Compares species observed at location to those at another sampling location
	 * @param slc1 Sampling location for comparison
	 */
	public void compareToSamplingLocation(SamplingLocation slc1){
		
		//checking that both sets of species initialized
		if(this.setSpecies==null || slc1.setSpecies==null){
			dIntersection = -9999;
			dSymDifference = -9999;
			return;
		}
		
		//initializing variables
		dIntersection = 0;
		dSymDifference = 0;
		
		//loading values
		for(String s:this.setSpecies){
			if(slc1.setSpecies.contains(s)){
				dIntersection++;
			}else{
				dSymDifference++;
			}
		}
		for(String s:slc1.setSpecies){
			if(!this.setSpecies.contains(s)){
				dSymDifference++;
			}
		}
	}
	
	/**
	 * Clears the set of species that were observed
	 */
	public void clearSpeciesSet(){
		setSpecies=null;
	}
}
