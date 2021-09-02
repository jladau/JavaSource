package edu.ucsf.ReorderColumns;

import java.util.ArrayList;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code reorders columns in a text file according to a specified ordering
 * @author jladau
 */

public class ReorderColumnsLauncher {

	public static void main(String rgsArgs[]){
		
		//dat1 = data
		//arg1 = arguments
		//rgs1 = updated header order
		//lstOut = output
		//sbl1 = current output line
		
		ArgumentIO arg1;
		DataIO dat1;
		String rgs1[];
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgs1 = arg1.getValueStringArray("rgsNewHeaderOrdering");
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(Joiner.on(",").join(rgs1));
		for(int i=1;i<dat1.iRows;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<rgs1.length;j++){
				if(j>0){
					sbl1.append(",");
				}
				sbl1.append(dat1.getString(i, rgs1[j]));
			}
			lstOut.add(sbl1.toString());
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
