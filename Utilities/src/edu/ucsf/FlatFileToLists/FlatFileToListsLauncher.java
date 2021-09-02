package edu.ucsf.FlatFileToLists;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Converts a two-column flat file with repeated to keys to a flat file where each key is repeated once, with values as a list
 * @author jladau
 *
 */

public class FlatFileToListsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//map1 = map from keys to values
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMultimap<String,String> map1;
		ArrayList<String> lstOut;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		map1 = HashMultimap.create();
		
		//loading map
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, arg1.getValueString("sKeyField")), dat1.getString(i, arg1.getValueString("sValueField")));
		}
		
		//outputting results
		lstOut = new ArrayList<String>(map1.size());
		lstOut.add(arg1.getValueString("sKeyField") + "," + arg1.getValueString("sValueField"));
		for(String s:map1.keySet()){
			lstOut.add(s + "," + Joiner.on("; ").join(map1.get(s)));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}
