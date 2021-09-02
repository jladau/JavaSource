package edu.ucsf.BIOM.MetagenomeMergeTables;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.MetagenomesIO;

public class MetagenomeMergeTablesLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = first table
		//bio2 = second table
		//mio1 = merged table
		
		ArgumentIO arg1;
		BiomIO bio1;
		BiomIO bio2;
		MetagenomesIO mio1;
		
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sMetagenomePath1"));
		bio2 = new BiomIO(arg1.getValueString("sMetagenomePath2"));
		mio1 = new MetagenomesIO(bio1.merge(bio2),0.);
		DataIO.writeToFile(
				mio1.printJSON(), 
				arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
