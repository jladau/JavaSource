package edu.ucsf.ConcatenateByHeader;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Concatenates two csv files so that headers match.
 * @author jladau
 *
 */

public class ConcatenateByHeaderLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = argument
		//dat1 = data 1
		//dat2 = data 2
		//lst1 = list of header elements from first file
		//lstOut = output
		//sbl1 = current output line
			
		ArgumentIO arg1;
		DataIO dat1;
		DataIO dat2;
		ArrayList<String> lst1;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath1"));
		dat2 = new DataIO(arg1.getValueString("sDataPath2"));
		
		//loading lists of header elements
		lst1 = new ArrayList<String>(dat1.iCols + dat2.iCols);
		for(int j=0;j<dat1.iCols;j++){
			lst1.add(dat1.getString(0, j));
		}
		for(int j=0;j<dat2.iCols;j++){
			if(!lst1.contains(dat2.getString(0, j))){
				lst1.add(dat2.getString(0, j));
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(dat1.iRows + dat2.iRows);
		sbl1 = new StringBuilder();
		for(int j=0;j<lst1.size();j++){
			if(j>0){
				sbl1.append(",");
			}
			sbl1.append(lst1.get(j));
		}
		lstOut.add(sbl1.toString());
		for(int i=1;i<dat1.iRows;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<lst1.size();j++){
				if(j>0){
					sbl1.append(",");
				}
				sbl1.append(dat1.getString(i, lst1.get(j)));
			}
			lstOut.add(sbl1.toString());
		}
		for(int i=1;i<dat2.iRows;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<lst1.size();j++){
				if(j>0){
					sbl1.append(",");
				}
				sbl1.append(dat2.getString(i, lst1.get(j)));
			}
			lstOut.add(sbl1.toString());
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}