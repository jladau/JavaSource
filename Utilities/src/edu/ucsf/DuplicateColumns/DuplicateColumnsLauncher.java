package edu.ucsf.DuplicateColumns;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Duplicates column in csv file
 * @author jladau
 *
 */

public class DuplicateColumnsLauncher {

	public static void main(String rgsArgs[]){
		
		//dat1 = data
		//arg1 = arguments
		//i1 = number of duplicates to make
		//s1 = column to duplicate
		
		DataIO dat1;
		ArgumentIO arg1;
		int i1;
		String s1;
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		i1 = arg1.getValueInt("iDuplicates");
		s1 = arg1.getValueString("sColumnToDuplicate");
		
		//creating duplicates
		for(int j=1;j<=i1;j++){
			dat1.appendToLastColumn(0, s1 + "_" + j);
		}
		for(int i=1;i<dat1.iRows;i++){
			for(int j=1;j<=i1;j++){
				dat1.appendToLastColumn(i, dat1.getString(i, s1));
			}
		}
		
		//printing output
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
