package edu.ucsf.TransposeTable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code transposes a table
 * @author jladau
 *
 */

public class TransposeTableLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//lst1 = list of string builders
		//lstOut = output
		//rgs1 = current line in split format
		//bfr1 = buffered file reader
		//s1 = current line
		
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		String rgs1[];
		ArrayList<StringBuilder> lst1 = null;
		BufferedReader bfr1;
		String s1;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		
		try{
			
			//initializing output	
			bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sTablePath")));
			rgs1 = bfr1.readLine().split(",");
			lst1 = new ArrayList<StringBuilder>(rgs1.length);
			for(int i=0;i<rgs1.length;i++){
				lst1.add(new StringBuilder());
				lst1.get(i).append(rgs1[i]);
			}
			
			//loading additional lines
			while((s1=bfr1.readLine())!=null){
				rgs1 = s1.split(",");
				for(int i=0;i<rgs1.length;i++){
					lst1.get(i).append("," + rgs1[i]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//loading output
		lstOut = new ArrayList<String>(lst1.size());
		for(int i=0;i<lst1.size();i++){
			lstOut.add(lst1.get(i).toString());
		}
		
		//printing output
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}

	public static void main0(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//tbl1 = data in table form
		//sbl1 = current output line
		//rgs1 = column header
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		Table<String,String,String> tbl1;
		ArrayList<String> lstOut;
		String rgs1[];
		StringBuilder sbl1;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sTablePath"));
		
		//loading data in table format
		tbl1 = HashBasedTable.create(dat1.iCols,dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			for(int j=1;j<dat1.iCols;j++){
				tbl1.put(dat1.getString(0, j), dat1.getString(i, 0),  dat1.getString(i, j));
			}
		}
		
		//loading new header
		rgs1 = tbl1.columnKeySet().toArray(new String[tbl1.columnKeySet().size()]);
		sbl1 = new StringBuilder();
		sbl1.append(dat1.getString(0, 0));
		sbl1.append("," + Joiner.on(",").join(rgs1));
		lstOut = new ArrayList<String>();
		lstOut.add(sbl1.toString());
		for(String s:tbl1.rowKeySet()){
			sbl1 = new StringBuilder();
			sbl1.append(s);
			for(String t:rgs1){
				sbl1.append("," + tbl1.get(s, t));
			}
			lstOut.add(sbl1.toString());
		}
		
		//printing output
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}	
}
