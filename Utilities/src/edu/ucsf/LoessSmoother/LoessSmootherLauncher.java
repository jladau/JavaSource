package edu.ucsf.LoessSmoother;

import edu.ucsf.base.LoessInterpolation;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class LoessSmootherLauncher {

	public static void main(String rgsArgs[]){
		
		
		//arg1 = arguments
		//dat1 = data
		//rgdX = x values
		//rgdY = y values
		//rgdL = loess values
		
		ArgumentIO arg1;
		DataIO dat1;
		double rgdX[];
		double rgdY[];
		double rgdL[];
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgdX = new double[dat1.iRows-1];
		rgdY = new double[dat1.iRows-1];
		for(int i=1;i<dat1.iRows;i++){
			rgdX[i-1]=dat1.getDouble(i, arg1.getValueString("sXField"));
			rgdY[i-1]=dat1.getDouble(i, arg1.getValueString("sYField"));
		}
		rgdL=LoessInterpolation.smooth(rgdX, rgdY);
		dat1.appendToLastColumn(0,"LOESS_SMOOTH");
		for(int i=0;i<rgdL.length;i++){
			dat1.appendToLastColumn(i+1,rgdL[i]);
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
}
