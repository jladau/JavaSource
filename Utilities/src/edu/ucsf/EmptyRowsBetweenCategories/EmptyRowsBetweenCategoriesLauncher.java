package edu.ucsf.EmptyRowsBetweenCategories;

import java.util.ArrayList;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Inserts blank row between rows that do not match.
 * @author jladau
 *
 */

public class EmptyRowsBetweenCategoriesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//sCategory = category field
		//sbl1 = blank row
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		String sCategory;
		StringBuilder sbl1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows+100);
		sCategory = arg1.getValueString("sCategoryField");
		sbl1 = new StringBuilder();
		for(int i=1;i<dat1.iCols;i++){
			sbl1.append(",");
		}
		
		//looping through data rows
		lstOut.add(Joiner.on(",").join(dat1.getRow(0)));
		for(int i=1;i<dat1.iRows;i++){
			if(i!=1 && !dat1.getString(i, sCategory).equals(dat1.getString(i-1, sCategory))){
				lstOut.add(sbl1.toString());
			}
			lstOut.add(Joiner.on(",").join(dat1.getRow(i)));
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");	
	}
}
