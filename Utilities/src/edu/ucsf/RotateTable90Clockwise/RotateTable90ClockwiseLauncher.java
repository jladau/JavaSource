package edu.ucsf.RotateTable90Clockwise;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Rotates a table 90 degrees clockwise
 * @author jladau
 */

public class RotateTable90ClockwiseLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data table
		//rgs1 = output
		//lstOut = output
		//sbl1 = current output line
		
		ArgumentIO arg1;
		DataIO dat1;
		String rgs1[][];
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgs1 = new String[dat1.iCols][dat1.iRows];
		for(int i=0;i<dat1.iRows;i++){
			for(int j=0;j<dat1.iCols;j++){
				rgs1[j][dat1.iRows-i-1]=dat1.getString(i, j);
			}
		}
		lstOut = new ArrayList<String>(rgs1.length);
		for(int i=0;i<rgs1.length;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<rgs1[i].length;j++){
				if(j>0){
					sbl1.append(",");
				}
				sbl1.append(rgs1[i][j]);
			}
			lstOut.add(sbl1.toString());
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}