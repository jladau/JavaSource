package edu.ucsf.JoinTables;

import java.util.HashMap;
import java.util.HashSet;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code appends data to a given file by matching data from a source file to it
 * @author jladau
 */

public class JoinTablesLauncher {

	public static void main(String rgsArgs[]){
		
		//setUnused = set of values that were not appended
		//arg1 = arguments
		//dat0 = data set to be appended to
		//dat1 = data set to append
		//map1(sID) = returns value to be appended for given id
		//rgsKeys0 = header keys in data set to be appended to
		//rgsKeys1 = header keys in data set to append
		//rgsValues1 = header value in data set to append
		
		String[] rgsKeys0; 
		String[] rgsKeys1; 
		String[] rgsValues1;
		ArgumentIO arg1;
		DataIO dat0; DataIO dat1;
		HashMap<String,String> map1;
		HashSet<String> setUnused;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading initial data set
		dat0 = new DataIO(arg1.getValueString("sBaseDataPath"));
		dat1 = new DataIO(arg1.getValueString("sAppendDataPath"));
		
		//loading headers
		if(arg1.containsArgument("sBaseKey") && arg1.containsArgument("sAppendKey")){	
			rgsKeys0 = new String[]{arg1.getValueString("sBaseKey")};
			rgsKeys1 = new String[]{arg1.getValueString("sAppendKey")};
		}else{
			rgsKeys0 = arg1.getValueStringArray("rgsBaseKeys");
			rgsKeys1 = arg1.getValueStringArray("rgsAppendKeys");
		}
		if(arg1.containsArgument("sAppendValue")){
			rgsValues1 = new String[]{arg1.getValueString("sAppendValue")};
		}else{
			rgsValues1 = arg1.getValueStringArray("rgsAppendValues");
		}
		
		//loading map of values to be appended and set of unused ids
		map1 = new HashMap<String,String>();
		setUnused = new HashSet<String>();
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, rgsKeys1),  dat1.getString(i,rgsValues1));
			
			//*********************
			//System.out.println(dat1.getString(i, rgsKeys1));
			//*********************
			
			setUnused.add(dat1.getString(i, rgsKeys1));
		}
		
		//appending values
		dat0.appendToLastColumn(0, Joiner.on(",").join(rgsValues1));
		for(int i=1;i<dat0.iRows;i++){
			if(!map1.containsKey(dat0.getString(i, rgsKeys0))){
				
				//*********************
				//System.out.println(dat0.getString(i, rgsKeys0));
				//*********************
				
				
				dat0.appendToLastColumn(i,"-9999");
			}else{
				if(!arg1.containsArgument("bAppendFirstOnly") 
						|| arg1.getValueBoolean("bAppendFirstOnly")==false 
						|| (arg1.getValueBoolean("bAppendFirstOnly")==true && setUnused.contains(dat0.getString(i, rgsKeys0)))){
					
					dat0.appendToLastColumn(i,map1.get(dat0.getString(i, rgsKeys0)));
					setUnused.remove(dat0.getString(i, rgsKeys0));
				}else{
					dat0.appendToLastColumn(i,map1.get(dat0.getString(i, "")));
				}
			}
		}
		
		//outputting results
		DataIO.writeToFile(dat0.getWriteableData(), arg1.getValueString("sOutputPath"));
	}
}
