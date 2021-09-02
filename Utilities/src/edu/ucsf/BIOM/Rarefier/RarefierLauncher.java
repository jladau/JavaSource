package edu.ucsf.BIOM.Rarefier;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * This code rarefies an OTU table
 * @author jladau
 *
 */

public class RarefierLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments object
		//bio1 = biom object
		
		ArgumentIO arg1;
		BiomIO bio1;
		
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		DataIO.writeToFile(bio1.printJSON(), arg1.getValueString("sOutputPath"));
		/*
		if(bio1.axsObservation.hasMetadataField("taxonomy")){	
			DataIO.writeToFile(bio1.printTableWithTaxonomy(), arg1.getValueString("sOutputPath"));
		}else{
			DataIO.writeToFile(bio1.printTable(), arg1.getValueString("sOutputPath"));
		}
		*/
		System.out.println("Done.");
	}	
}
