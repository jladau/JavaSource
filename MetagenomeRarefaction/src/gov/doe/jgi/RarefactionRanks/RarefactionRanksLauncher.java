package gov.doe.jgi.RarefactionRanks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Finds ranks of rarefaction curves
 * @author jladau
 */

public class RarefactionRanksLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments

		ArgumentIO arg1;
		
		arg1 = new ArgumentIO(rgsArgs);
		if(arg1.getValueString("sMode").equals("from-biom-table")){
			ranksFromBIOM(arg1);
		}else if(arg1.getValueString("sMode").equals("from-rarefaction-curves")){
			ranksFromCurves(arg1);
		}
		System.out.println("Done.");
	}
	
	public static void ranksFromCurves(ArgumentIO arg1) throws Exception{
		
		//dat1 = rarefaction curves
		//map1 = map from read depths to list of samples
		//map2 = map from read depths to list of richnesses (order corresponding to map1)
		//i1 = current number of reads
		//lstOut = output
		//lst1 = ranks
		//lst2 = samples
		//lst3 = richnesses
		//d1 = total number of observations
		
		ArrayListMultimap<Integer,String> map1;
		ArrayListMultimap<Integer,Double> map2;
		DataIO dat1;
		int i1;
		ArrayList<Double> lst1;
		ArrayList<String> lst2;
		ArrayList<Double> lst3;
		ArrayList<String> lstOut;
		double d1;
		
		//loading variables
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		map1 = ArrayListMultimap.create(125,2500);
		map2 = ArrayListMultimap.create(125,2500);
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("SAMPLE_ID,READ_DEPTH,RICHNESS,RICHNESS_RANK,RICHNESS_PERCENTILE_RANK");
		
		//loading maps with values
		for(int i=1;i<dat1.iRows;i++){
			i1 = dat1.getInteger(i, "NUMBER_READS");
			map1.put(i1, dat1.getString(i, "METAGENOME"));
			map2.put(i1, dat1.getDouble(i, "RICHNESS"));
		}
		
		//looping through read depths
		for(Integer i:map1.keySet()){
			lst1 = ranksAverage(new ArrayList<Double>(map2.get(i)));
			//lst1 = ranksFloor(new ArrayList<Double>(map2.get(i)));
			lst2 = new ArrayList<String>(map1.get(i));
			lst3 = new ArrayList<Double>(map2.get(i));
			d1 = (double) lst1.size();
			for(int k=0;k<lst2.size();k++){
				lstOut.add(lst2.get(k) + "," + i + "," + lst3.get(k) + "," + lst1.get(k) + "," + lst1.get(k)/d1);
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
	}

	public static void ranksFromBIOM(ArgumentIO arg1) throws Exception{
		
		//arg1 = arguments
		//lstOut = output
		//bio1 = biom table
		//mapRichness = richness map
		//map1 = map of sample relative abundances
		//i1 = current counter
		//i2 = total count
		//lst2 = list of read depths to consider
		//i3 = current number of reads being input
		//map2 = current richness ranks
		
		HashMap<String,Double> map2;
		ArrayList<Integer> lst2;
		ArrayList<String> lstOut;
		BiomIO bio1;
		HashMap<String,HashSet<Double>> map1;
		int i1;
		int i2;
		int i3;
		
		//loading variables
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		System.out.println("Normalizing to relative abundance...");
		bio1.normalize();
		map1 = new HashMap<String,HashSet<Double>>(bio1.axsSample.size());
		i2 = bio1.axsSample.size();
		i1 = 1;
		for(String s:bio1.axsSample.getIDs()){
			System.out.println("Loading non-zero values for sample " + i1 + " of " + i2 + "...");
			i1++;
			map1.put(s, loadRelativeAbundances(bio1.getNonzeroValues(bio1.axsSample, s)));
		}
		lstOut = new ArrayList<String>(100*bio1.axsSample.size()+1);
		lstOut.add("SAMPLE_ID,READ_DEPTH,RICHNESS_RANK");
		
		//loading read depths to consider
		lst2 = new ArrayList<Integer>(100);
		for(int i=0;i<100;i++){
			i3 = (int) (Math.floor(Math.pow(1.088, (double) i))) + 1;
			if(lst2.size()==0 || i3>lst2.get(lst2.size()-1)){
				lst2.add(i3);
			}
		}
		
		//looping through read depths
		for(int i:lst2){
			map2 = richnessRanks(i, map1);
			for(String s:map2.keySet()){
				lstOut.add(s + "," + i + "," + map2.get(s));
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));	
	}

	private static HashMap<String,Double> richnessRanks(int iReads, HashMap<String,HashSet<Double>> map1){
		
		//d1 = current richness
		//lst1 = list of richnesses
		//map4 = output
		//lst2 = list of sample names
		//lst3 = list of ranks
		
		double d1;
		ArrayList<Double> lst1;
		HashMap<String,Double> map4;
		ArrayList<String> lst2;
		ArrayList<Double> lst3;
		
		lst1 = new ArrayList<Double>(map1.size());
		lst2 = new ArrayList<String>(map1.size());
		for(String s:map1.keySet()){
			d1 = richness((double) iReads, map1.get(s));
			lst1.add(d1);
			lst2.add(s);
		}
		lst3 = ranksAverage(lst1);
		map4 = new HashMap<String,Double>(lst2.size());
		for(int i=0;i<lst2.size();i++){
			map4.put(lst2.get(i), lst3.get(i));
		}
		return map4;
	}
	
	private static ArrayList<Double> ranksAverage(ArrayList<Double> lst1){
		
		//map1 = map from values to non-averaged ranks
		//map2 = map from values to averaged ranks
		//lst2 = input values sorted
		//lst3 = output
		//d1 = current sum of ranks
		
		HashMultimap<Double,Integer> map1;
		HashMap<Double,Double> map2;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		double d1;
		
		lst2 = new ArrayList<Double>(lst1);
		Collections.sort(lst2);
		map1 = HashMultimap.create(lst2.size(), lst2.size());
		map1.put(lst2.get(0), 1);
		for(int i=1;i<lst2.size();i++){
			map1.put(lst2.get(i), i+1);
		}
		map2 = new HashMap<Double,Double>(map1.size());
		for(Double d:map1.keySet()){
			if(map1.get(d).size()==1){
				map2.put(d, (double) firstElement(map1.get(d)));
			}else{
				d1 = 0;
				for(Integer i:map1.get(d)){
					d1+=(double) i;
				}
				map2.put(d, d1/((double) map1.get(d).size()));
			}
		}
		lst3 = new ArrayList<Double>(lst2.size());
		for(int i=0;i<lst1.size();i++){
			lst3.add(map2.get(lst1.get(i)));
		}
		return lst3;
	}
	
	private static Integer firstElement(Set<Integer> set1){
		
		for(Integer i:set1){
			return i;
		}
		return Integer.MIN_VALUE;
	}
	
	
	private static ArrayList<Double> ranksFloor(ArrayList<Double> lst1){
		
		//map1 = map from values to ranks
		//lst2 = input values sorted
		//lst3 = output
		//i1 = current rank
		
		HashMap<Double,Integer> map1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		int i1;
		
		lst2 = new ArrayList<Double>(lst1);
		Collections.sort(lst2);
		map1 = new HashMap<Double,Integer>(lst2.size());
		map1.put(lst2.get(0), 1);
		i1=1;
		for(int i=1;i<lst2.size();i++){
			
			//***********************
			//System.out.println(lst2.get(i)-lst2.get(i-1));
			//System.out.println(lst2.get(i)!=lst2.get(i-1));
			//System.out.println(lst2.get(i)==lst2.get(i-1));	
			//System.out.println(lst2.get(i).equals(lst2.get(i-1)));	
			//System.out.println(Math.abs(lst2.get(i)-lst2.get(i-1))>0);
			//***********************
			
			if(!lst2.get(i).equals(lst2.get(i-1))){
				i1 = i+1;
			}
			if(!map1.containsKey(lst2.get(i))){
				map1.put(lst2.get(i), i1);
			}
		}
		lst3 = new ArrayList<Double>(lst2.size());
		for(int i=0;i<lst1.size();i++){
			lst3.add((double) map1.get(lst1.get(i)));
		}
		return lst3;
	}
	
	private static HashSet<Double> loadRelativeAbundances(HashMap<String,Double> map1){
		
		//set1 = output
		
		HashSet<Double> set1;
		
		set1 = new HashSet<Double>(map1.size());
		for(String u:map1.keySet()){
			if(!u.equals("unclassified")){
				set1.add(map1.get(u));
			}
		}
		return set1;
	}
	
	private static double richness(double dReads, HashSet<Double> set1){
		
		//d1 = sum
		//d2 = current term
		
		double d1;
		double d2;
		
		d1 = 0;
		for(double d:set1){
			d2 = dReads*Math.log(1.-d);
			d1+= 1. - Math.exp(d2);
		}
		return d1;
	}	
}