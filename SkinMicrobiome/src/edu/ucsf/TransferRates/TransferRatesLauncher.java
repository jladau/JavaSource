package edu.ucsf.TransferRates;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TransferRatesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//d1 = number shared
		//d2 = number on hand
		
		ArgumentIO arg1;
		DataIO dat1;
		double d1;
		double d2;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		dat1.appendToLastColumn(0,"FRACTION_OTUS_PALM_TO_MATERIAL,NUMBER_OTUS_PALM_TO_MATERIAL");
		for(int i=1;i<dat1.iRows;i++){
			d1 = dat1.getDouble(i, "SHARED_RICHNESS");
			if(dat1.getString(i, "SURFACE_TYPE_1").equals("Rpalm") && !dat1.getString(i, "SURFACE_TYPE_2").equals("Rpalm")){
				d2 = dat1.getDouble(i, "SAMPLE_1_EXCLUSIVE_RICHNESS") + d1;
			}else if(!dat1.getString(i, "SURFACE_TYPE_1").equals("Rpalm") && dat1.getString(i, "SURFACE_TYPE_2").equals("Rpalm")){
				d2 = dat1.getDouble(i, "SAMPLE_2_EXCLUSIVE_RICHNESS") + d1;
			}else{
				d1 = 0;
				d2 = 1;
			}
			dat1.appendToLastColumn(i, d1/d2 + "," + d1);
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
