package com.joshualadau.deprecated;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code loads prints sizes given aspect ratios and desired areas
 * @author jladau
 */

public class PrintSizeCalculatorLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = image ids, heights, and widths
		//sbl1 = current output line
		//dAR = current aspect ratio
		//dArea = current area
		//dHeight = height
		//dWidth = width
		
		ArgumentIO arg1;
		DataIO dat1;
		StringBuilder sbl1;
		double dAR;
		double dArea;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sSizesPath"));
		dat1.appendToLastColumn(0, "PRINT_DIMENSIONS");
		for(int i=1;i<dat1.iRows;i++){
			sbl1=new StringBuilder();
			dAR = dat1.getDouble(i, "IMAGE_HEIGHT")/dat1.getDouble(i, "IMAGE_WIDTH");
			for(String s:arg1.getValueStringArray("rgsAreas")){
				dArea = Double.parseDouble(s);
				if(!sbl1.toString().equals("")){
					sbl1.append(";");
				}
				sbl1.append(findPrintDimensions(dArea,dAR));
			}
			dat1.appendToLastColumn(i, sbl1.toString());
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static String findPrintDimensions(double dArea, double dAspectRatio){
		
		//dHeight = height in decimal format
		//dWidth = width in decimal format
		//rgd1 = list of integer heights and widths
		//dMinDiff = minimum difference
		//iMinDiff = index of minimum difference
		
		double dHeight;
		double dWidth;
		double dMinDiff;
		double rgd1[][];
		int iMinDiff=-1;
		
		dHeight = Math.sqrt(dArea)*Math.sqrt(dAspectRatio);
		dWidth = Math.sqrt(dArea)/Math.sqrt(dAspectRatio);
		rgd1 = new double[2][4];
		rgd1[0] = new double[]{Math.floor(dHeight),Math.floor(dHeight),Math.ceil(dHeight),Math.ceil(dHeight)};
		rgd1[1] = new double[]{Math.floor(dWidth),Math.ceil(dWidth),Math.floor(dWidth),Math.ceil(dWidth)};
		dMinDiff=Double.MAX_VALUE;
		for(int j=0;j<rgd1[0].length;j++){
			if(Math.abs(dArea-rgd1[0][j]*rgd1[1][j])<dMinDiff){
				dMinDiff=Math.abs(dArea-rgd1[0][j]*rgd1[1][j]);
				iMinDiff=j;
			}
		}
		return (rgd1[0][iMinDiff] + "x" + rgd1[1][iMinDiff]).replace(".0", ""); 
	}
}
