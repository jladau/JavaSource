package edu.ucsf.PivotTableToFlatFile;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code converts a pivot table to a flat file.
 * @author jladau
 */

public class PivotTableToFlatFileLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//sbl1 = current output line
		//lstColsToKeep = columns to not flatten
		//lstColsToFlat = columns to flatten
		//s1 = current unflattened variable string
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut; ArrayList<String> lstColsToKeep; ArrayList<String> lstColsToFlat;
		StringBuilder sbl1;
		String s1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading columns to flatten and keep
		if(arg1.containsArgument("lstColumnsToFlatten")){	
			lstColsToFlat = arg1.getValueArrayList("lstColumnsToFlatten");
			lstColsToKeep = new ArrayList<String>();
			for(int j=0;j<dat1.iCols;j++){
				if(!lstColsToFlat.contains(dat1.getString(0, j))){
					lstColsToKeep.add(dat1.getString(0, j));
				}
			}
		}else{
			lstColsToKeep = arg1.getValueArrayList("lstColumnsNotToFlatten");
			lstColsToFlat = new ArrayList<String>();
			for(int j=0;j<dat1.iCols;j++){
				if(!lstColsToKeep.contains(dat1.getString(0, j))){
					lstColsToFlat.add(dat1.getString(0, j));
				}
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		sbl1 = new StringBuilder();
		for(int j=0;j<lstColsToKeep.size();j++){
			if(j>0){
				sbl1.append(",");
			}
			sbl1.append(lstColsToKeep.get(j));
		}
		sbl1.append(",FLAT_VAR_KEY,FLAT_VAR_VALUE");
		lstOut.add(sbl1.toString());
		
		//looping through data values
		for(int i=1;i<dat1.iRows;i++){
			
			//loading unflattened values
			sbl1 = new StringBuilder();
			for(int j=0;j<lstColsToKeep.size();j++){
				if(j>0){
					sbl1.append(",");
				}
				sbl1.append(dat1.getString(i, lstColsToKeep.get(j)));
			}
			s1 = sbl1.toString();
			
			//looping through flattening variables
			for(int j=0;j<lstColsToFlat.size();j++){
				lstOut.add(s1 + "," + lstColsToFlat.get(j) + "," + dat1.getString(i, lstColsToFlat.get(j)));
			}
		}
		
		//terminating
		if(arg1.containsArgument("sOutputPath")){
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
			System.out.println("Done.");
		}else{
			for(int i=0;i<lstOut.size();i++){
				System.out.println(lstOut.get(i));
			}
		}
	}
}