package edu.ucsf.BIOM.CSVtoJSON;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class CSVToJSONLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//lstOut = output
		
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		
		arg1 = new ArgumentIO(rgsArgs);
		lstOut = BiomIO.printJSON(
				new DataIO(arg1.getValueString("sDataMatrixPath")), 
				new DataIO(arg1.getValueString("sSampleMetadataPath")), 
				new DataIO(arg1.getValueString("sObservationMetadataPath")),
				arg1.getValueString("sType"));
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}