package edu.ucsf.BIOM.PseudoIndicatorTaxa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.base.PseudoIndicatorTaxa;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class PseudoIndicatorTaxaLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstOut = output
		//itx1 = indicator taxa object
		//mapGroups = map from sample IDs to groups
		//dat1 = map from samples to groups
		//setGroups = set of groups
		
		PseudoIndicatorTaxa itx1;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstOut;
		HashMap<String,String> mapGroups;
		DataIO dat1;
		HashSet<String> setGroups;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"), arg1.getAllArguments());
		lstOut = new ArrayList<String>(bio1.axsObservation.size()+1);
		lstOut.add("TAXON,CLASS,ABUNDANCE_MEAN,PREVALENCE");
		dat1 = new DataIO(arg1.getValueString("sClassMapPath"));
		mapGroups = new HashMap<String,String>(dat1.iRows);
		setGroups = new HashSet<String>();
		for(int i=1;i<dat1.iRows;i++){
			mapGroups.put(dat1.getString(i, "SAMPLE"), dat1.getString(i, "CLASS"));
			setGroups.add(dat1.getString(i, "CLASS"));
		}
		
		//running analysis
		itx1 = new PseudoIndicatorTaxa(bio1,mapGroups);
		itx1.loadValues();
		for(String sTaxon:itx1.taxa()){
			for(String s:setGroups){
				lstOut.add(sTaxon + "," + s + "," + itx1.indicatorValue(sTaxon,s,"abundance") + "," + itx1.indicatorValue(sTaxon,s,"prevalence"));
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}