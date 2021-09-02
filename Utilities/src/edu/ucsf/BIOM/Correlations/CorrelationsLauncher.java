package edu.ucsf.BIOM.Correlations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class CorrelationsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//map1 = current observation
		//iCounter = counter
		//dat1 = data with covariate
		//lst1 = covariate data
		//lst2 = current relative abundance data
		//lst3 = reference list for sample order
		//dMin = current minimum value
		//dMax = current maximum value
		//lstOut = output
		//d1 = current observed value
		//d2 = current null value
		//d3 = standardized effect size
		//lst4 = list of null values
		
		ArgumentIO arg1;
		BiomIO bio1;
		HashMap<String,Double> map1;
		int iCounter;
		DataIO dat1;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		ArrayList<String> lst3;
		ArrayList<Double> lst4;
		ArrayList<String> lstOut;
		double d1;
		double d2;
		double d3;
		double dMin;
		double dMax;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"), arg1.getAllArguments());
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(bio1.axsObservation.size()+1);
		lstOut.add("TAXON,SPEARMAN_OBSERVED,SPEARMAN_NULL_MIN,SPEARMAN_NULL_MAX,SES");
		
		//loading covariate data
		lst1 = new ArrayList<Double>(dat1.iRows);
		lst3 = new ArrayList<String>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			if(!dat1.getString(i, arg1.getValueString("sCovariateField")).equals("NA")){
				if(bio1.axsSample.getIDs().contains(dat1.getString(i, "SAMPLE"))){
					lst1.add(dat1.getDouble(i, arg1.getValueString("sCovariateField")));
					lst3.add(dat1.getString(i, "SAMPLE"));
				}
			}
		}
		
		//looping through taxa
		iCounter = 1;
		for(String sTaxon:bio1.axsObservation.getIDs()){
			System.out.println("Finding correlation for taxon " + iCounter + " of " + bio1.axsObservation.size() + "...");
			iCounter++;
			if(bio1.getNonzeroCount(bio1.axsObservation, sTaxon)<10){
				continue;
			}
			map1 = bio1.getItem(bio1.axsObservation, sTaxon);
			lst2 = new ArrayList<Double>(lst3.size());
			for(int i=0;i<lst3.size();i++){
				lst2.add(map1.get(lst3.get(i)));
			}
			d1 = ExtendedMath.spearman(lst1, lst2);
			dMin = Double.MAX_VALUE;
			dMax = -Double.MAX_VALUE;
			lst4 = new ArrayList<Double>(100);
			for(int i=0;i<100;i++){
				Collections.shuffle(lst2);
				d2 = ExtendedMath.spearman(lst1, lst2);
				if(d2<dMin){
					dMin = d2;
				}
				if(d2>dMax){
					dMax = d2;
				}
				lst4.add(d2);
			}
			if(!Double.isNaN(d1)){
				d3 = (d1 - ExtendedMath.mean(lst4))/ExtendedMath.standardDeviationP(lst4);
				lstOut.add(sTaxon + "," + d1 + "," + dMin + "," + dMax + "," + d3);
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}