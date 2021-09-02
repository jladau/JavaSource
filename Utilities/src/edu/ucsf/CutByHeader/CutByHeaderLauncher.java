package edu.ucsf.CutByHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Cuts data file by specified headers
 * @author jladau
 */

public class CutByHeaderLauncher {

	public static void main(String[] rgsArgs){
		
		//dat1 = data
		//arg1 = arguments
		//lst1 = output
		//sbl1 = current output line
		//rgs1 = headers
		//set1 = set of headers to exclude
		
		ArrayList<String> lst1;
		DataIO dat1;
		ArgumentIO arg1;
		StringBuilder sbl1;
		String[] rgs1;
		HashSet<String> set1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lst1 = new ArrayList<String>(dat1.iRows+1);
		if(arg1.containsArgument("rgsHeadersToInclude")){	
			rgs1 = arg1.getValueStringArray("rgsHeadersToInclude");
			for(int i=0;i<dat1.iRows;i++){
				sbl1 = null;
				for(String s:rgs1){
					if(sbl1==null){
						sbl1 = new StringBuilder();
					}else{
						sbl1.append(",");
					}
					sbl1.append(dat1.getString(i, s));
				}
				lst1.add(sbl1.toString());
			}
		}else if(arg1.containsArgument("rgsHeadersToExclude")){
			set1 = new HashSet<String>(Arrays.asList(arg1.getValueStringArray("rgsHeadersToExclude")));
			for(int i=0;i<dat1.iRows;i++){
				sbl1 = null;
				for(int j=0;j<dat1.iCols;j++){
					if(!set1.contains(dat1.getString(0, j))){
						if(sbl1==null){
							sbl1 = new StringBuilder();
						}else{
							sbl1.append(",");
						}
						sbl1.append(dat1.getString(i, j));
					}		
				}
				lst1.add(sbl1.toString());
			}
		}
			
		//outputting results
		DataIO.writeToFile(lst1, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}