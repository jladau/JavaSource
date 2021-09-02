package edu.ucsf.Nestedness.Grapher;

import java.util.ArrayList;
import edu.ucsf.base.NestednessGrapher;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class GrapherLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//bio2 = second biom object being added
		//dTime = start time
		//lstOut = output
		//usg1 = usage object
		
		Usage usg1;
		ArgumentIO arg1;
		BiomIO bio1;
		BiomIO bio2;
		ArrayList<String> lstOut;

		//initializing usage object
		usg1 = new Usage(new String[]{
			"BiomIO",
			"rgsBIOMPaths",
			"sOutputPath",
			"rgsSampleMetadataFields"});
		usg1.printUsage(rgsArgs);
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);

		//loading defaults
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", false);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", true);
		}
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
		}
		
		//loading data
		System.out.println("Loading data...");
		if(arg1.containsArgument("sBIOMPath")){	
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		}else{
			bio1 = new BiomIO(arg1.getValueStringArray("rgsBIOMPaths")[0], arg1.getAllArguments());
			for(int i=1;i<arg1.getValueStringArray("rgsBIOMPaths").length;i++){
				bio2 = new BiomIO(arg1.getValueStringArray("rgsBIOMPaths")[i], arg1.getAllArguments());
				bio1 = bio1.merge(bio2);
			}
		}
		bio1.filterByMinimumPrevalence(1);
		
		//loading graph
		lstOut = NestednessGrapher.getNestednessGraph(bio1, arg1.getValueStringArray("rgsSampleMetadataFields"));
		
		//writing results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}