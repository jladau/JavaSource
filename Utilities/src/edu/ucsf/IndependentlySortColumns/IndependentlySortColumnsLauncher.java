package edu.ucsf.IndependentlySortColumns;

import java.util.ArrayList;
import java.util.Arrays;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Independently sorts columns
 * @author jladau
 */

public class IndependentlySortColumnsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
			
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//rgs1 = current array
		//rgl1 = stringbuilder (for output)
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		String rgs1[];
		StringBuilder[] rgl1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgl1 = new StringBuilder[dat1.iRows];
		for(int i=0;i<rgl1.length;i++){
			rgl1[i] = new StringBuilder();
		}
		
		//looping through columns
		for(int j=0;j<dat1.iCols;j++){
			if(j>0){
				for(int i=0;i<dat1.iRows;i++){
					rgl1[i].append(",");
				}
			}
			rgl1[0].append(dat1.getString(0, j));
			rgs1 = new String[dat1.iRows-1];
			for(int i=1;i<dat1.iRows;i++){
				rgs1[i-1]=dat1.getString(i, j);
			}
			Arrays.sort(rgs1);
			for(int i=0;i<rgs1.length;i++){
				rgl1[i+1].append(rgs1[i]);
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(rgl1.length);
		for(int i=0;i<rgl1.length;i++){
			lstOut.add(rgl1[i].toString());
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}