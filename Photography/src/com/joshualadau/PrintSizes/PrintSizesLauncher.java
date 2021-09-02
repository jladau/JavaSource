package com.joshualadau.PrintSizes;

import java.util.ArrayList;
import java.util.TreeMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code appends print sizes matching aspect ratios to specified list of photo dimensions.
 * @author jladau
 */

public class PrintSizesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datPicSizes = picture size data
		//datPrintSizes = print size data
		//map1(dAspectRatio) = returns list of print sizes for given aspect ratio.
		//sDim = current dimensions
		//dAR = current aspect ratio
		//sbl1 = current output line
		//lst1 = list of aspect ratios
		
		ArgumentIO arg1;
		DataIO datPicSizes;
		DataIO datPrintSizes;
		TreeMap<Double,String> map1;
		String sDim;
		double dAR;
		StringBuilder sbl1;
		ArrayList<Double> lst1;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		datPicSizes = new DataIO(arg1.getValueString("sPictureSizePath"));
		datPrintSizes = new DataIO(arg1.getValueString("sPrintSizePath"));
		
		//loading map with aspect ratios
		map1 = new TreeMap<Double,String>();
		lst1 = new ArrayList<Double>();
		for(int i=1;i<datPrintSizes.iRows;i++){
			sDim = datPrintSizes.getString(i, "HEIGHT") + "x" + datPrintSizes.getString(i, "WIDTH");
			dAR = Math.round(datPrintSizes.getDouble(i, "ASPECT_RATIO")*1000.)/1000.;
			if(dAR<1){
				dAR=1./dAR;
			}
			if(!map1.containsKey(dAR)){
				map1.put(dAR,"\"Suggested print sizes: " + sDim);
				lst1.add(dAR);
			}else{
				if(!map1.get(dAR).contains(sDim)){
					sDim = map1.get(dAR) + ", " + sDim;
					map1.put(dAR, sDim);
				}
			}
		}
		
		//updating ending characters
		for(double d:lst1){
			sDim=map1.get(d);
			sDim=sDim + ".\"";
			map1.put(d, sDim);
		}
		
		//appending columns to picture size output
		datPicSizes.appendToLastColumn(0,"ASPECT_RATIO_OBSERVED,ASPECT_RATIO_LB,DELTA_LB,PRINT_SIZES_LB,ASPECT_RATIO_UB,DELTA_UB,PRINT_SIZES_UB");
		for(int i=1;i<datPicSizes.iRows;i++){
			dAR=datPicSizes.getDouble(i, "IMAGE_HEIGHT")/datPicSizes.getDouble(i, "IMAGE_WIDTH");
			if(dAR<1){
				dAR=1./dAR;
			}
			sbl1 = new StringBuilder();
			sbl1.append(dAR);
			if(dAR<map1.firstKey()){
				sbl1.append(",na,na");
			}else{
				sbl1.append("," + map1.floorKey(dAR) + "," + Math.round((dAR-map1.floorKey(dAR))*1000.)/1000. + "," + map1.get(map1.floorKey(dAR)));
			}
			if(dAR>map1.lastKey()){
				sbl1.append(",na,na");
			}else{
				sbl1.append("," + map1.ceilingKey(dAR) + "," + Math.round((map1.ceilingKey(dAR)-dAR)*1000.)/1000. + "," + map1.get(map1.ceilingKey(dAR)));
			}
			datPicSizes.appendToLastColumn(i,sbl1.toString());
		}
		
		//terminating
		DataIO.writeToFile(datPicSizes.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}
