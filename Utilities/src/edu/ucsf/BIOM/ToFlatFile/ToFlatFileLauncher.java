package edu.ucsf.BIOM.ToFlatFile;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class ToFlatFileLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstOut = output
		//i1 = number of nonzero entries
		//map1 = nonzero counts per sample
		//map2 = current nonzero vector
		//bZero = flag for whether to include zero values
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		BiomIO bio1;
		int i1;
		HashMap<String,Integer> map1;
		HashMap<String,Double> map2;
		boolean bZero;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"), arg1.getAllArguments());
		bZero = arg1.getValueBoolean("bOutputZeros");
		
		if(bZero==false){
		
			//loading output
			i1 = 1;
			map1 = bio1.getNonzeroCounts(bio1.axsSample);
			for(String sSample:map1.keySet()) {
				i1+=map1.get(sSample);
			}
			
			//outputting results
			lstOut = new ArrayList<String>(i1);
			lstOut.add("SAMPLE,OBSERVATION,VALUE");
			for(String sSample:map1.keySet()) {
				map2 = bio1.getNonzeroValues(bio1.axsSample,sSample);
				for(String sObservation:map2.keySet()){
					lstOut.add(sSample + "," + sObservation + "," + map2.get(sObservation));
				}
			}
		}else{
			lstOut = new ArrayList<String>(bio1.axsObservation.size()*bio1.axsSample.size()+1);
			lstOut.add("SAMPLE,OBSERVATION,VALUE");
			for(String sSample:bio1.axsSample.getIDs()) {
				for(String sObservation:bio1.axsObservation.getIDs()) {
					lstOut.add(sSample + "," + sObservation + "," + bio1.getValueByIDs(sObservation,sSample));
				}
			}
		}
			
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
