package edu.ucsf.ResampleWithReplacement;

import java.util.ArrayList;
import java.util.Random;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class ResampleWithReplacementLauncher{

	public static void main(String rgsArgs[]) {
		
		//arg1 = arguments
		//dat1 = data
		//rnd1 = random number generator
		//lstOut = output
		//i1 = current row
		
		ArgumentIO arg1;
		DataIO dat1;
		Random rnd1;
		ArrayList<String> lstOut;
		int i1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("RESAMPLE_ID," + Joiner.on(",").join(dat1.getRow(0)));
		for(int i=1;i<dat1.iRows;i++){
			i1 = (int) Math.ceil(rnd1.nextDouble()*(dat1.iRows-1));
			lstOut.add(i + "," + Joiner.on(",").join(dat1.getRow(i1)));
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}