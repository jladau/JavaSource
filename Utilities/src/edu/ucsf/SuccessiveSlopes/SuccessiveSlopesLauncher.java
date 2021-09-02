package edu.ucsf.SuccessiveSlopes;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates slopes between successive rows
 * @author jladau
 *
 */

public class SuccessiveSlopesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//sbl1 = current output line
		//sXField = x field
		
		String sXField;
		StringBuilder sbl1;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sXField = arg1.getValueString("sXField");
		
		//initializing output
		lstOut = new ArrayList<String>(dat1.iRows);
		sbl1 = new StringBuilder();
		for(String s:arg1.getValueStringArray("rgsIndexFields")){
			if(sbl1.length()>0){
				sbl1.append(",");
			}
			sbl1.append(s + "_1," + s + "_2");
		}
		for(String s:arg1.getValueStringArray("rgsYFields")){
			if(sbl1.length()>0){
				sbl1.append(",");
			}
			sbl1.append(s + "_SLOPE");
		}
		lstOut.add(sbl1.toString());
		
		//looping through rows of data
		for(int i=2;i<dat1.iRows;i++){
			
			sbl1 = new StringBuilder();
			for(String s:arg1.getValueStringArray("rgsIndexFields")){
				if(sbl1.length()>0){
					sbl1.append(",");
				}
				sbl1.append(dat1.getString(i-1, s) + "," + dat1.getString(i, s));
			}
			for(String s:arg1.getValueStringArray("rgsYFields")){
				if(sbl1.length()>0){
					sbl1.append(",");
				}
				sbl1.append((dat1.getDouble(i, s)-dat1.getDouble(i-1, s))/(dat1.getDouble(i, sXField)-dat1.getDouble(i-1, sXField)));
			}
			lstOut.add(sbl1.toString());
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}