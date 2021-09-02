package edu.ucsf.geospatial;

import java.util.HashSet;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.ucsf.base.MantelTest;
import edu.ucsf.io.BiomIO;

public class DistanceDecay {

	/**Returns the distance between a pair of samples**/
	private Table<String,String,Double> tblDistance;
	
	/**Returns the bray-curtis distance between a pair of samples**/
	private Table<String,String,Double> tblBrayCurtis;
	
	public DistanceDecay(BiomIO bio1){
		
		//ert1 = Earth geometry object
		//iCounter = counter
		
		EarthGeometry ert1;
		int iCounter=0;
		
		//looping through pairs of samples
		ert1 = new EarthGeometry();
		tblDistance = HashBasedTable.create(bio1.axsSample.size(),bio1.axsSample.size());
		tblBrayCurtis = HashBasedTable.create(bio1.axsSample.size(),bio1.axsSample.size());
		
		for(String s:bio1.axsSample.getIDs()){
			for(String t:bio1.axsSample.getIDs()){
				iCounter++;
				if(iCounter%10==0){
					System.out.println("Analyzing distance " + iCounter + "...");
				}
				
				if(s==t){
					tblDistance.put(s, t, 0.);
					tblBrayCurtis.put(s, t, 0.);
				}else{
					if(tblDistance.contains(t, s)){
						tblDistance.put(s, t, tblDistance.get(t, s));
						tblBrayCurtis.put(s, t, tblBrayCurtis.get(t, s));
					}else{
						tblDistance.put(s, t, ert1.orthodromicDistanceWGS84(
								Double.parseDouble(bio1.axsSample.getMetadata(s).get("latitude")), 
								Double.parseDouble(bio1.axsSample.getMetadata(s).get("longitude")), 
								Double.parseDouble(bio1.axsSample.getMetadata(t).get("latitude")), 
								Double.parseDouble(bio1.axsSample.getMetadata(t).get("longitude"))));
						tblBrayCurtis.put(s,t , 1.-getBrayCurtis(bio1,s,t));
					}
				}
			}
		}
	}
	
	public MantelTest runMantelTest(int iIterations){
		return new MantelTest(tblDistance,tblBrayCurtis,iIterations);
	}
	
	public double getDistance(String sSample1, String sSample2){
		return tblDistance.get(sSample1, sSample2);
	}
	
	public double getBrayCurtis(String sSample1, String sSample2){
		return tblBrayCurtis.get(sSample1, sSample2);
	}
	
	public HashSet<String[]> getSamplePairs(){
		
		//set1 = set of pairs in string format
		//setOut = output
		
		HashSet<String> set1;
		HashSet<String[]> setOut;
		
		set1 = new HashSet<String>();
		for(String s:tblDistance.rowKeySet()){
			for(String t:tblDistance.rowKeySet()){
				if(!s.equals(t) && !set1.contains(s + "," + t) && !set1.contains(t + "," + s)){
					set1.add(s + "," + t);
				}
			}
		}
		setOut = new HashSet<String[]>();
		for(String s:set1){
			setOut.add(s.split(","));
		}
		return setOut;
	}
	
	private double getBrayCurtis(BiomIO bio1, String sSample1, String sSample2){
		
		//dNum = numerator
		//dDen = denominator
		//d1 = current first value
		//d2 = current second value
		
		double dNum; double dDen; double d1; double d2;
		
		dNum = 0;
		dDen = 0;
		for(String s:bio1.axsObservation.getIDs()){
			d1 = bio1.getValueByIDs(s, sSample1);
			d2 = bio1.getValueByIDs(s, sSample2);
			dNum+=Math.abs(d1-d2);
			dDen+=(d1+d2);
		}
		if(dDen==0){
			return Double.NaN;
		}else{
			return dNum/dDen;
		}
	}
}
