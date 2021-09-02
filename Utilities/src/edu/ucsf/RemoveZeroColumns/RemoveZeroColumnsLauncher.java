package edu.ucsf.RemoveZeroColumns;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.unittested.base.ExtendedMath;

/**
 * Removes columns that are all zeroes
 * @author jladau
 */

public class RemoveZeroColumnsLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lst1 = current column
		//tbl1 = output
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<Double> lst1;
		HashBasedTable<Integer,String,Double> tbl1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through columns
		tbl1 = HashBasedTable.create(dat1.iRows, dat1.iCols);
		for(String s:dat1.getHeaders()){
			lst1 = dat1.getDoubleColumn(s);
			if(ExtendedMath.sumOfPowers(lst1,2)>0.000000001){
			for(int i=0;i<lst1.size();i++){
					tbl1.put(i+1,s,lst1.get(i));
				}
			}
		}
		
		//outputting results
		DataIO.writeToFile(tbl1,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}