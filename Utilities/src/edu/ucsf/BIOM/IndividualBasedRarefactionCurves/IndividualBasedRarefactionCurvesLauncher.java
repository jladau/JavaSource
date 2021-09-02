package edu.ucsf.BIOM.IndividualBasedRarefactionCurves;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class IndividualBasedRarefactionCurvesLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstOut = output
		//lstN = subsample sizes
		//dPr = probability of read from given taxon
		//map1 = map from observations to nonzero values
		//d1 = current richness
		//dat1 = data with depths at which to calculate rarefaction curves
		//dReads = total number of reads
		//map2 = map from samples to total read counts
		
		BiomIO bio1;
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		ArrayList<Integer> lstN;
		double dPr;
		HashMap<String,Double> map1;
		double d1;
		DataIO dat1;
		double dReads;
		HashMap<String,Double> map2;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		map2 = bio1.sum(bio1.axsSample);
		dat1 = new DataIO(arg1.getValueString("sDepthsPath"));
		lstOut = new ArrayList<String>(bio1.axsSample.size()*dat1.iRows+1);
		lstOut.add("SAMPLE,NUMBER_READS,RICHNESS");
		
		//looping through samples
		for(String s:bio1.axsSample.getIDs()){
			
			System.out.println("Finding rarefaction curve for sample " + s + "...");
			
			//loading numbers of reads for computing read depths for rarefaction curve
			lstN = new ArrayList<Integer>(dat1.iRows);
			for(int i=1;i<dat1.iRows;i++){
				lstN.add(dat1.getInteger(i, "NUMBER_READS"));
			}
			
			//loading nonzero values
			map1 = bio1.getNonzeroValues(bio1.axsSample, s);
			
			//looping through read depths
			dReads = map2.get(s);
			for(Integer i:lstN){
				d1 = 0;
				for(String t:map1.keySet()){
					dPr = map1.get(t)/dReads;
					d1+=probabiltyOccur(i, dPr);
				}
				lstOut.add(s + "," + i + "," + d1);
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static double probabiltyOccur(int iN, double dPr){
		
		//d1 = output
		
		double d1;
		
		d1 = ((double) iN) * Math.log(1.-dPr);
		return 1.-Math.exp(d1);
	}
}