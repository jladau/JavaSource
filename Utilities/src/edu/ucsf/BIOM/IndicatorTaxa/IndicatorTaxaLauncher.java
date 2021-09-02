package edu.ucsf.BIOM.IndicatorTaxa;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.base.IndicatorTaxa;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class IndicatorTaxaLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstOut = output
		//itx1 = indicator taxa object
		//mapGroups = map from sample IDs to groups
		//dat1 = map from samples to groups
		
		IndicatorTaxa itx1;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		HashMap<String,String> mapGroups;
		DataIO dat1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"), arg1.getAllArguments());
		lstOut = new ArrayList<String>(bio1.axsObservation.size()+1);
		lstOut.add("TAXON,VALUE,CLASS,PVALUE");
		dat1 = new DataIO(arg1.getValueString("sClassMapPath"));
		mapGroups = new HashMap<String,String>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			mapGroups.put(dat1.getString(i, "SAMPLE"), dat1.getString(i, "CLASS"));
		}
		
		//running analysis
		itx1 = new IndicatorTaxa(bio1,mapGroups);
		itx1.loadValues(arg1.getValueInt("iIterations"));
		for(String sTaxon:itx1.taxa()){
			lstOut.add(sTaxon + "," + itx1.get(sTaxon).toString());
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}