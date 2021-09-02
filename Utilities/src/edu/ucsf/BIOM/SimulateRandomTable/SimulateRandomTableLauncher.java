package edu.ucsf.BIOM.SimulateRandomTable;

import java.util.HashMap;
import java.util.Random;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Simulates an OTU table based on an existing table
 * @author jladau
 */

public class SimulateRandomTableLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom file
		
		ArgumentIO arg1;
		BiomIO bio1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"), arg1.getAllArguments());
		
		//getting random table with same occurrences
		//bio1 = randomTable(bio1, arg1.getValueInt("iRarefactionDepth"), arg1.getValueInt("iRandomSeed"));
		bio1 = randomTable1(bio1, arg1.getValueInt("iRarefactionDepth"), arg1.getValueInt("iRandomSeed"));
		
		//outputting results
		DataIO.writeToFile(bio1.printJSON(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static BiomIO randomTable1(BiomIO bio1, int iRarefactionDepth, int iRandomSeed) throws Exception{
		
		//rnd1 = random number generator
		
		Random rnd1;
		
		rnd1 = new Random(iRandomSeed);
		for(String s:bio1.axsSample.getIDs()){
			for(String t:bio1.axsObservation.getIDs()){
				bio1.setValue(t, s, (double) (Math.round(rnd1.nextDouble()*10)));
			}
		}
		bio1.rarefy(iRarefactionDepth, iRandomSeed+11);
		return bio1;
	}
	
	private static BiomIO randomTable(BiomIO bio1, int iRarefactionDepth, int iRandomSeed) throws Exception{
		
		//map1 = map of nonzero values
		//map2 = map of sums
		//rnd1 = random number generator
		//i1 = sum
		//d2 = multiplier
		
		Random rnd1;
		HashMap<String,Double> map1;
		HashMap<String,Double> map2;
		double d2;
		int i1;
		
		rnd1 = new Random(iRandomSeed);
		map2 = bio1.sum(bio1.axsSample);
		for(String s:bio1.axsSample.getIDs()){
			map1 = bio1.getNonzeroValues(bio1.axsSample, s);
			d2 = map2.get(s);
			i1 = 0;
			do{
				for(String t:map1.keySet()){
					bio1.setValue(t, s, (double) (Math.round(rnd1.nextDouble()*3.*d2/map1.size())));
					i1+=(int) bio1.getValueByIDs(t, s);
				}
			}while(i1<d2);
		}
		bio1.rarefy(iRarefactionDepth, iRandomSeed+11);
		return bio1;
	}
}