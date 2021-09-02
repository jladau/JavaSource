package edu.ucsf.ColumnSums;

import java.util.ArrayList;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.unittested.base.ExtendedMath;

/**
 * Removes columns that are all zeroes
 * @author jladau
 */

public class ColumnSumsLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lst1 = current column
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		ArrayList<Double> lst1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through columns
		lstOut = new ArrayList<String>(dat1.iCols);
		for(String s:dat1.getHeaders()){
			lst1 = dat1.getDoubleColumn(s);
			lstOut.add(s + "," + ExtendedMath.sum(lst1));
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}