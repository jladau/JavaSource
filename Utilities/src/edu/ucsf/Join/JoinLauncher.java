package edu.ucsf.Join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code appends data to a given file by matching data from a source file to it
 * @author jladau
 */

public class JoinLauncher {

	public static void main(String rgsArgs[]){
		
		//setUnused = set of values that were not appended
		//arg1 = arguments
		//dat0 = data set to be appended to
		//dat1 = data set to append
		//map1(sID) = returns value to be appended for given id
		//rgsKeys0 = header keys in data set to be appended to
		//rgsKeys1 = header keys in data set to append
		//rgsValues1 = header value in data set to append
		//lstValues1 = header value in data set to append
		//lstOut = output
		//sbl1 = no match output line
		
		String[] rgsKeys0; 
		String[] rgsKeys1; 
		//String[] rgsValues1;
		ArgumentIO arg1;
		DataIO dat0; DataIO dat1;
		HashMap<String,String> map1;
		HashSet<String> setUnused;
		ArrayList<String> lstValues1;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		sbl1 = new StringBuilder();
		
		//loading initial data set
		dat0 = new DataIO(arg1.getValueString("sDataPath1"));
		dat1 = new DataIO(arg1.getValueString("sDataPath2"));
		
		//loading headers
		rgsKeys0 = arg1.getValueStringArray("rgsKeys1");
		rgsKeys1 = arg1.getValueStringArray("rgsKeys2");
		
		//loading values to append
		lstValues1=new ArrayList<String>(dat1.iCols);
		for(int j=0;j<dat1.iCols;j++){
			if(!ArrayUtils.contains(rgsKeys1, dat1.getString(0, j))){
				lstValues1.add(dat1.getString(0, j));
			}
		}
		
		//loading no match string
		sbl1.append("NA");
		for(int i=1;i<lstValues1.size();i++){
			sbl1.append(",NA");
		}
		
		//loading map of values to be appended and set of unused ids
		map1 = new HashMap<String,String>();
		setUnused = new HashSet<String>();
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, rgsKeys1),  dat1.getString(i,lstValues1));
			setUnused.add(dat1.getString(i, rgsKeys1));
		}
		
		//appending values
		//lstOut = new ArrayList<String>(dat0.iRows);
		System.out.println(Joiner.on(",").join(dat0.getRow(0)) + "," + Joiner.on(",").join(lstValues1));
		for(int i=1;i<dat0.iRows;i++){
			
			//*************************
			//System.out.println(dat0.getString(i, rgsKeys0));
			//System.out.println("");
			//for(String s:map1.keySet()){
			//	System.out.println(s);
			//}
			//System.out.println("");
			//*************************
			
			if(map1.containsKey(dat0.getString(i, rgsKeys0))){
				System.out.println(Joiner.on(",").join(dat0.getRow(i)) + "," + map1.get(dat0.getString(i, rgsKeys0)));
				//lstOut.add(Joiner.on(",").join(dat0.getRow(i)) + "," + map1.get(dat0.getString(i, rgsKeys0)));
				setUnused.remove(dat0.getString(i, rgsKeys0));
			}else if(arg1.containsArgument("bOutputAll")){
				System.out.println(Joiner.on(",").join(dat0.getRow(i)) + "," + sbl1.toString());
			}
		}
		
		//outputting results
		//DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		//System.out.println("Done.");
		
		/*
		
		//appending values
		lstOut = new ArrayList<String>(dat0.iRows);
		lstOut.add(Joiner.on(",").join(dat0.getRow(0)) + "," + Joiner.on(",").join(lstValues1));
		for(int i=1;i<dat0.iRows;i++){
			if(map1.containsKey(dat0.getString(i, rgsKeys0))){
				lstOut.add(Joiner.on(",").join(dat0.getRow(i)) + "," + map1.get(dat0.getString(i, rgsKeys0)));
				setUnused.remove(dat0.getString(i, rgsKeys0));
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
		
		*/
	}
}