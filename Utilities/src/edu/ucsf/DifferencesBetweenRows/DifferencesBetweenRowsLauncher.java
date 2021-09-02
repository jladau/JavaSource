package edu.ucsf.DifferencesBetweenRows;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class DifferencesBetweenRowsLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sValue = value field
		//sCategory = category field
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		String sValue;
		String sCategory;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategory = arg1.getValueString("sCategoryField");
		sValue = arg1.getValueString("sValueField");
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("START_ROW,CATEGORY,VALUE_DIFFERENCE");
		
		//looping through data
		for(int i=2;i<dat1.iRows;i++){
			if(dat1.getString(i,sCategory).equals(dat1.getString(i-1,sCategory))){
				lstOut.add((i-1) + "," + dat1.getString(i,sCategory) + "," + (dat1.getDouble(i,sValue)-dat1.getDouble(i-1,sValue)));
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}