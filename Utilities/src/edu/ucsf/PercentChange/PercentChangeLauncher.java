package edu.ucsf.PercentChange;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class PercentChangeLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//sCategory = category field
		//sValue = value field
		//sIndex = index field
		//d0 = initial value
		//d1 = final value
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		String sCategory;
		String sValue;
		String sIndex;
		double d0;
		double d1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategory = arg1.getValueString("sCategoryField");
		sValue = arg1.getValueString("sValueField");
		sIndex = arg1.getValueString("sIndexField");
		
		//initializing output
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("CATEGORY,INDEX_START,INDEX_END,PERCENT_CHANGE");
		
		//looping through rows
		for(int i=2;i<dat1.iRows;i++){
			if(dat1.getString(i,sCategory).equals(dat1.getString(i-1,sCategory))){
				d0 = dat1.getDouble(i,sValue);
				d1 = dat1.getDouble(i-1,sValue);
				if(d0==d1){
					lstOut.add(dat1.getString(i,sCategory) + "," + dat1.getString(i-1,sIndex) + "," + dat1.getString(i,sIndex) + "," + 0);
				}else{
					if(d0>0){
						lstOut.add(dat1.getString(i,sCategory) + "," + dat1.getString(i-1,sIndex) + "," + dat1.getString(i,sIndex) + "," + (d1-d0)/d0);
					}
				}
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
