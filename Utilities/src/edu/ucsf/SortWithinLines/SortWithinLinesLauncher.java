package edu.ucsf.SortWithinLines;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.base.Joiner;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Sorts entries within each line of a file.
 * @author jladau
 *
 */

public class SortWithinLinesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data object
		//rgs1 = current row
		//lstOut = output
		
		DataIO dat1;
		ArgumentIO arg1;
		String rgs1[];
		ArrayList<String> lstOut;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows);
		rgs1 = new String[dat1.iCols];
		for(int j=0;j<dat1.iCols;j++){
			rgs1[j]=dat1.getString(0, j);
		}
		lstOut.add(Joiner.on(",").join(rgs1));
		
		//loading sorted output
		for(int i=1;i<dat1.iRows;i++){
			rgs1 = new String[dat1.iCols];
			for(int j=0;j<dat1.iCols;j++){
				rgs1[j]=dat1.getString(i, j);
			}
			Arrays.sort(rgs1);
			lstOut.add(Joiner.on(",").join(rgs1));	
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}