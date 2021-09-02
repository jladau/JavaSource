package edu.ucsf.SequentialIntegers;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class SequentialIntegersLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//iMax = maximum
		//iMin = minimum
		//iSteps = number of steps
		//lstOut = output
		//dMax = maximum transformed
		//dMin = minimum transformed
		//dA = base
		//dB = constant
		//dStep = step size
		//d1 = current value
		//i1 = previous value
		//i2 = current value
		
		ArgumentIO arg1;
		int iMax;
		int iMin;
		int iSteps;
		ArrayList<String> lstOut;
		double dA;
		double dB;
		double dMax;
		double dMin;
		double dStep;
		double d1;
		int i1;
		int i2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		iMin = arg1.getValueInt("iMinimum");
		iMax = arg1.getValueInt("iMaximum");
		iSteps = arg1.getValueInt("iSteps");
		lstOut = new ArrayList<String>(iSteps + 1);
		lstOut.add("VALUE");
		
		//loading values
		dA = Math.sqrt(((double) iMin)/2.);
		dB = 2.;
		dMin = 2.;
		dMax = 2.*Math.log(((double) iMax)/2.)/Math.log(((double) iMin)/2.);
		dStep = (dMax-dMin)/((double) (iSteps-1));
		
		//iterating
		i1 = 0;
		for(int i=0;i<iSteps;i++){
			d1 = dMin + ((double) i)*dStep;
			d1 = dB*Math.pow(dA, d1);
			i2 = (int) Math.round(d1);
			if(i2!=i1){
				lstOut.add(Integer.toString(i2));
				i1 = i2;
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
}
