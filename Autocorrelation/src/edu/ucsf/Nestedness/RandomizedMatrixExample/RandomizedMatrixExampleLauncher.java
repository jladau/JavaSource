package edu.ucsf.Nestedness.RandomizedMatrixExample;

import java.util.HashMap;

import edu.ucsf.base.NestednessNODF;
import edu.ucsf.base.NestednessOrderedNODF;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class RandomizedMatrixExampleLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//dTime = start time
		//nes1 = nestedness object
		//bio2 = second biom object being added
		//usg1 = usage object
		
		Usage usg1;
		ArgumentIO arg1;
		BiomIO bio1;
		NestednessNODF nes1;
		//DataIO datGraphs;
		BiomIO bio2;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
			"BiomIO",
			"rgsBIOMPaths",
			"bOrderedNODF",
			"sNestednessNullModel",
			"sOutputPath",
			"iRandomSeed"});
		usg1.printUsage(rgsArgs);
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading defaults
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", true);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", false);
		}
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
		}
		if(!arg1.containsArgument("bSimulate")){
			arg1.updateArgument("bSimulate", false);
		}
		
		//loading otu table
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
		
		//initializing nodf object
		if(arg1.getValueBoolean("bOrderedNODF")){
			nes1 = new NestednessOrderedNODF(bio1, 1234);
		}else{
			nes1 = new NestednessNODF(bio1, 1234);
		}
		
		//getting randomized matrix
		bio1 = nes1.randomizedMatrix(arg1.getValueString("sNestednessNullModel"), arg1.getValueInt("iRandomSeed"), bio1);
					
		//outputting results
		//DataIO.writeToFile(bio1.printTable(), arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(bio1.printJSON(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}