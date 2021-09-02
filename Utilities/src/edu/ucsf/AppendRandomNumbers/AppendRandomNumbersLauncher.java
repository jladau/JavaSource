package edu.ucsf.AppendRandomNumbers;

import java.util.Random;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Appends random numbers
 * @author jladau
 *
 */

public class AppendRandomNumbersLauncher{

	public static void main(String rgsArgs[]) {
		
		//arg1 = arguments
		//dat1 = data
		//rnd1 = random number generator
		//rgd1 = random values
		
		ArgumentIO arg1;
		DataIO dat1;
		Random rnd1;
		double[] rgd1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		
		rgd1 = new double[dat1.iRows-1];
		for(int i=0;i<dat1.iRows-1;i++){
			rgd1[i]=rnd1.nextDouble();
		}
		
		dat1.appendToLastColumn(rgd1,"RANDOM_VALUE");
		DataIO.writeToFile(dat1.getWriteableData(),arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}