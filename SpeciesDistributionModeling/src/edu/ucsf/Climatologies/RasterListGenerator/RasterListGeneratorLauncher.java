package edu.ucsf.Climatologies.RasterListGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Sets;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.sdm.SDMParser;

/**
 * Generates a set of raster lists covering all climatologies in a set of rasters. Raster lists can then be used in the model selector.
 * @author jladau
 *
 */

public class RasterListGeneratorLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//set1 = set of shared climatologies
		//set2 = current set of climatologies
		//ncr1 = current reader
		//map1(sPath) = returns variable prefix
		//lstOut = current output
		
		ArgumentIO arg1;
		HashSet<String> set1 = null;
		HashSet<String> set2;
		HashMap<String,String> map1;
		NetcdfReader ncr1;
		ArrayList<String> lstOut;

		//loading climatologies and variables
		arg1 = new ArgumentIO(rgsArgs);
		map1 = new HashMap<String,String>();
		for(String s:arg1.getValueStringArray("rgsRasterPaths")){
			ncr1 = new NetcdfReader(s);
			if(set1==null){
				set1 = new HashSet<String>();
				for(String t:ncr1.getPlottableVars()){
					set1.add(SDMParser.getTrainingDate(t));
				}
			}else{
				set2 = new HashSet<String>();
				for(String t:ncr1.getPlottableVars()){
					set2.add(SDMParser.getTrainingDate(t));
				}
				set1 = Sets.intersection(set1, set2).copyInto(set1);
			}
			map1.put(s,SDMParser.stripClimatology(ncr1.getPlottableVars().get(0)));
			ncr1.close();
		}
		
		//outputting results
		for(String sClimatology:set1){
			lstOut = new ArrayList<String>(map1.size());
			for(String sRasterPath:map1.keySet()){
				lstOut.add(sRasterPath + "," + map1.get(sRasterPath) + "_" + sClimatology);
			}
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputDir") + "/" + sClimatology + ".csv");
		}
		
		//terminating
		System.out.println("Done.");	
	}	
}