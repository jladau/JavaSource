package edu.ucsf.CSVJoiner;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Joins csv files
 * @author jladau
 */

public class CSVJoinerLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = first data file
		//dat2 = second data file
		//lstOut = output
		//sKey1 = first key
		//sKey2 = second key
		//map2 = map from key2 values to rows
		//i2 = current lookup row
		
		int i2;
		ArgumentIO arg1;
		DataIO dat1;
		DataIO dat2;
		ArrayList<String> lstOut;
		String sKey1;
		String sKey2;
		HashMap<String,Integer> map2;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath1"));
		dat2 = new DataIO(arg1.getValueString("sDataPath2"));
		
		sKey1 = arg1.getValueString("sKey1");
		sKey2 = arg1.getValueString("sKey2");
		
		map2 = new HashMap<String,Integer>(dat2.iRows);
		for(int i=1;i<dat2.iRows;i++){
			map2.put(dat2.getString(i, sKey2), i);
		}
		
		lstOut = new ArrayList<String>(dat1.iRows+1);
		lstOut.add(Joiner.on(",").join(dat1.getRow(0)) + "," + Joiner.on(",").join(dat2.getRow(0)));
		for(int i=1;i<dat1.iRows;i++){
			if(map2.containsKey(dat1.getString(i, sKey1))){
				i2 = map2.get(dat1.getString(i, sKey1));
				lstOut.add(Joiner.on(",").join(dat1.getRow(i)) + "," + Joiner.on(",").join(dat2.getRow(i2)));
			}
		}
		
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}
