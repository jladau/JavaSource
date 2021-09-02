package edu.ucsf.BIOM.PrintMetadata;

import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class PrintMetadataLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//usg1 = usage object
		
		Usage usg1;
		ArgumentIO arg1;
		BiomIO bio1;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
			"BiomIO",
			"sAxis",
			"sOutputPath"});
		usg1.printUsage(rgsArgs);
		
		arg1 = new ArgumentIO(rgsArgs);
		if(arg1.containsArgument("sBIOMPath")){
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		}else{
			bio1 = new BiomIO(arg1.getValueString("sDataPath"),arg1.getAllArguments());
		}
		if(arg1.getValueString("sAxis").equals("observation")){	
			DataIO.writeToFile(bio1.printMetadata(bio1.axsObservation), arg1.getValueString("sOutputPath"));
		}else if(arg1.getValueString("sAxis").equals("sample")){
			DataIO.writeToFile(bio1.printMetadata(bio1.axsSample), arg1.getValueString("sOutputPath"));
		}
		
		System.out.println("Done.");
	}
}
