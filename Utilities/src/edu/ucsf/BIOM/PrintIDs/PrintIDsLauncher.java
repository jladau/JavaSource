package edu.ucsf.BIOM.PrintIDs;

import java.util.ArrayList;
import java.util.HashSet;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Prints IDs from a BIOM file
 * @author jladau
 *
 */

public class PrintIDsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstOut = output
		//sHeader = header
		//set1 = set of IDs
		
		HashSet<String> setIDs = null;
		String sHeader = null;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sDataPath"),arg1.getAllArguments());
		if(arg1.getValueString("sAxis").equals("observation")){	
			setIDs = bio1.axsObservation.getIDs();
			sHeader = "OBSERVATION";
		}else if(arg1.getValueString("sAxis").equals("sample")){
			setIDs = bio1.axsSample.getIDs();
			sHeader = "SAMPLE";
		}
		lstOut = new ArrayList<String>(setIDs.size()+1);
		lstOut.add(sHeader);
		for(String s:setIDs){
			lstOut.add(s);
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
