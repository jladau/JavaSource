package edu.ucsf.BIOM.PairwiseTaxonCorrelations;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class PairwiseTaxonCorrelationsLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstTaxa = ordered list of taxa
		//lstSamples = ordered list of samples
		//lst1 = list of values for first taxon
		//lst2 = list of values for second taxon
		//d1 = current correlation
		//lstOut = output
		//iCounter = counter
		//iTotal = total pairs
		//mapPrevalence = prevalences
		
		HashMap<String,Integer> mapPrevalence;
		int iCounter;
		int iTotal;
		ArrayList<String> lstOut;
		double d1;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstTaxa;
		ArrayList<String> lstSamples;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		lstTaxa = new ArrayList<String>(bio1.axsObservation.getIDs());
		lstSamples = new ArrayList<String>(bio1.axsSample.getIDs());
		lstOut = new ArrayList<String>(lstTaxa.size()*lstTaxa.size());
		lstOut.add("TAXON_1,TAXON_2,SPEARMAN");
		
		//filtering by prevalences
		mapPrevalence = prevalences(bio1);
		for(String s:mapPrevalence.keySet()){
			if(mapPrevalence.get(s)<20) {
				lstTaxa.remove(s);
			}
		}
		
		//looping through pairs of taxa
		iCounter = 1;
		iTotal = lstTaxa.size()*(lstTaxa.size()-1)/2;
		for(int i=1;i<lstTaxa.size();i++) {
			lst1 = toArrayList(bio1.getItem(bio1.axsObservation,lstTaxa.get(i)), lstSamples);
			for(int k=0;k<i;k++) {
				if(iCounter % 1000 == 0) {
					System.out.println("Finding correlation for taxa pair " + iCounter + " of " + iTotal + "...");
				}
				iCounter++;
				lst2 = toArrayList(bio1.getItem(bio1.axsObservation,lstTaxa.get(k)), lstSamples);
				d1 = ExtendedMath.spearman(lst1,lst2);
				if(Math.abs(d1)>0.2){
					lstOut.add(lstTaxa.get(i) + "," + lstTaxa.get(k) + "," + d1);
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static ArrayList<Double> toArrayList(HashMap<String,Double> map1, ArrayList<String> lstSamples){
		
		//lst1 = output
		
		ArrayList<Double> lst1;
		
		lst1 = new ArrayList<Double>(lstSamples.size());
		for(int i=0;i<lstSamples.size();i++){
			lst1.add(map1.get(lstSamples.get(i)));
		}
		return lst1;
	}
	
	private static HashMap<String,Integer> prevalences(BiomIO bio1){
		
		//map1 = output
		//map2 = current set of occurrences
		
		HashMap<String,Integer> map1;
		
		map1 = new HashMap<String,Integer>(bio1.axsObservation.size());
		for(String s:bio1.axsObservation.getIDs()) {
			map1.put(s,bio1.getNonzeroCount(bio1.axsObservation,s));
		}
		return map1;
	}
}
