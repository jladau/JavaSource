package edu.ucsf.AppendIntegerIDs;

import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code appends unique integer IDs to lines in a data file
 * @author jladau
 *
 */

public class AppendIntegerIDsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data file with IDs being appended
		//i1 = current ID
		//map1 = from data values to IDs
		//sField = field by which to add ids
		
		ArgumentIO arg1;
		DataIO dat1;
		int i1;
		HashMap<String,Integer> map1;
		String sField;
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		i1 = 1;
		map1 = new HashMap<String,Integer>();
		sField = arg1.getValueString("sField");
		
		//loading map of values
		for(int i=1;i<dat1.iRows;i++){
			if(!map1.containsKey(dat1.getString(i, sField))){
				map1.put(dat1.getString(i, sField), i1);
				i1++;
			}
		}
		
		//outputting results
		dat1.appendToLastColumn(0, sField + "_INTEGER_ID");
		for(int i=1;i<dat1.iRows;i++){
			dat1.appendToLastColumn(i, map1.get(dat1.getString(i, sField)));
		}
		
		//terminating
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
