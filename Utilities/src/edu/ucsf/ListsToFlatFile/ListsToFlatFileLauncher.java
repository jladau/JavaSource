package edu.ucsf.ListsToFlatFile;

import java.util.ArrayList;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Converts data in lists (key1:value1;key2:value2,...) to a flat file
 * @author jladau
 */

public class ListsToFlatFileLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//rgs1 = current list in split format
		//sKey = current key
		//sValue = current value
		//lstOut = output
		//sbl1 = current output line
		
		ArgumentIO arg1;
		DataIO dat1;
		String rgs1[];
		String sKey;
		String sValue;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//initializing output
		lstOut = new ArrayList<String>(dat1.iRows*8);
		sbl1 = new StringBuilder();
		for(int j=0;j<dat1.iCols;j++){
			if(!dat1.getString(0, j).equals(arg1.getValueString("sListHeader"))){
				if(sbl1.length()>0){
					sbl1.append(",");
				}
				sbl1.append(dat1.getString(0, j));
			}
		}
		lstOut.add(sbl1.toString() + ",LIST_KEY,LIST_VALUE");
		
		//looping through rows
		for(int i=1;i<dat1.iRows;i++){
			
			//checking if list in line
			if(dat1.getString(i, arg1.getValueString("sListHeader")).equals("na")){
				continue;
			}
			
			//loading first part of output line
			sbl1 = new StringBuilder();
			for(int j=0;j<dat1.iCols;j++){
				if(!dat1.getString(0, j).equals(arg1.getValueString("sListHeader"))){
					if(sbl1.length()>0){
						sbl1.append(",");
					}
					sbl1.append(dat1.getString(i, j));
				}
			}
			
			//loading values
			rgs1 = dat1.getString(i, arg1.getValueString("sListHeader")).split(";");
			for(int k=0;k<rgs1.length;k++){
				sKey = rgs1[k].split("=")[0];
				sValue = rgs1[k].split("=")[1];
				lstOut.add(sbl1.toString() + "," + sKey + "," + sValue);
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
		
	}
}