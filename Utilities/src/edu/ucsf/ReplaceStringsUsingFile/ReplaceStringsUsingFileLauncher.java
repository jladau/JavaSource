package edu.ucsf.ReplaceStringsUsingFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Replaces multiple strings in file using old string --> new string mappings from another file
 * @author Joshua Ladau, jladau@gmail.com
 */

public class ReplaceStringsUsingFileLauncher {
	
	/**
	 * Writes file with replaced strings.
	 * @param Pass arguments as --{argument name}={argument value}. Use "-h" or "--help" as first argument to see list of possible arguments.
	 **/
	public static void main(String rgsArgs[]) throws Exception{
		
		//dat1 = data file with old string to new string mapping
		//arg1 = arguments
		//map1 = map from old strings to new strings
		//bfr1 = reader for file to which replacements are being made
		//s1 = current line
		//lstOut = output
		//usg1 = usage object
		//sNew = new string
		//sOld = old string
		
		String sNew;
		String sOld;
		Usage usg1;
		ArrayList<String> lstOut;
		BufferedReader bfr1;
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<Integer,String[]> map1;
		String s1;
		
		//initializing usage object
		usg1 = new Usage(new String[]{
				"sReplacementMapPath",
				"sOutputPath",
				"sInputPath",
				"bReplaceFirstOnly"});
		usg1.printUsage(rgsArgs);
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sReplacementMapPath"));
		map1 = new HashMap<Integer,String[]>(dat1.iRows-1);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(i, new String[]{dat1.getString(i, "OLD_STRING"), dat1.getString(i, "NEW_STRING")});
		}
		lstOut = new ArrayList<String>(10000);
		bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sInputPath")));
		while((s1=bfr1.readLine())!=null){	
			for(int i=1;i<dat1.iRows;i++){
				if(!map1.containsKey(i)){
					continue;
				}
				sOld = map1.get(i)[0];
				sNew = map1.get(i)[1];
				if(arg1.getValueBoolean("bReplaceFirstOnly") && s1.contains(sOld)){
					s1 = s1.replace(sOld, sNew);
					map1.remove(i);
					break;	
				}else{
					s1 = s1.replace(sOld, sNew);
				}
			}
			lstOut.add(s1);
		}
		bfr1.close();
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}