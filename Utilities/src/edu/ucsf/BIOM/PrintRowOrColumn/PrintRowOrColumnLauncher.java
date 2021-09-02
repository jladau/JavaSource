package edu.ucsf.BIOM.PrintRowOrColumn;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Prints a column from a BIOM file
 * @author jladau
 *
 */


public class PrintRowOrColumnLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//map1 = map of values
		//lstOut = output
		//sHeader = header
		
		String sHeader = null;
		ArgumentIO arg1;
		BiomIO bio1;
		HashMap<String,Double> map1 = null;
		ArrayList<String> lstOut;
		
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sDataPath"), arg1.getAllArguments());
		if(arg1.getValueString("sAxis").equals("observation")){	
			map1 = bio1.getItem(bio1.axsObservation, arg1.getValueString("sID"));
			sHeader = "SAMPLE," + arg1.getValueString("sID");
		}else if(arg1.getValueString("sAxis").equals("sample")){
			map1 = bio1.getItem(bio1.axsSample, arg1.getValueString("sID"));
			sHeader = "OBSERVATION," + arg1.getValueString("sID");
		}
		lstOut = new ArrayList<String>(map1.size()+1);
		lstOut.add(sHeader);
		for(String s:map1.keySet()){
			lstOut.add(s + "," + map1.get(s));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
}
