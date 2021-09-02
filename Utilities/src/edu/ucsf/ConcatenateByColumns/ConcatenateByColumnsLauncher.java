package edu.ucsf.ConcatenateByColumns;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Concatenates two data files by matching columns
 * @author jladau
 */

public class ConcatenateByColumnsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = first data file
		//dat2 = second data file
		//map1 = map from headers to column indices
		//i1 = column counter
		//i2 = row counter
		//rgsOut = output
		//s1 = current value
		//lstOut = output
		//sbl1 = current output line
		
		StringBuilder sbl1;
		ArrayList<String> lstOut;
		String s1;
		ArgumentIO arg1;
		DataIO dat1;
		DataIO dat2;
		HashMap<String,Integer> map1;
		int i1;
		int i2;
		String rgsOut[][];
		
		//initializing arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath1"));
		dat2 = new DataIO(arg1.getValueString("sDataPath2"));
		
		//loading map
		map1 = new HashMap<String,Integer>();
		i1 = 0;
		for(int j=0;j<dat1.iCols;j++){
			map1.put(dat1.getString(0, j), i1);
			i1++;
		}
		for(int j=0;j<dat2.iCols;j++){
			if(!map1.containsKey(dat2.getString(0, j))){
				map1.put(dat2.getString(0, j), i1);
				i1++;
			}
		}
		
		//loading output
		rgsOut = new String[dat1.iRows + dat2.iRows-1][i1];
		for(String s:map1.keySet()){
			rgsOut[0][map1.get(s)] = s;
		}
		i2=1;
		for(int i=1;i<dat1.iRows;i++){
			for(int j=0;j<dat1.iCols;j++){
				s1 = dat1.getString(i, j);
				i1 = map1.get(dat1.getString(0,j));
				rgsOut[i2][i1] = s1;
			}
			i2++;
		}
		for(int i=1;i<dat2.iRows;i++){
			for(int j=0;j<dat2.iCols;j++){
				s1 = dat2.getString(i, j);
				i1 = map1.get(dat2.getString(0,j));
				rgsOut[i2][i1] = s1;
			}
			i2++;
		}
		for(int i=1;i<rgsOut.length;i++){
			for(int j=0;j<rgsOut[0].length;j++){
				if(rgsOut[i][j]==null){
					rgsOut[i][j]="NA";
				}
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>(rgsOut.length);
		for(int i=0;i<rgsOut.length;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<rgsOut[0].length;j++){
				sbl1.append(rgsOut[i][j]);
				if(j<rgsOut[0].length-1){
					sbl1.append(",");
				}
			}
			lstOut.add(sbl1.toString());
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}