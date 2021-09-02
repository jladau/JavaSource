package edu.ucsf.FlatFileToPivotTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.TableReformatter;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code converts a flat file to table.
 * @author jladau
 *
 */


public class FlatFileToPivotTableLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = flat file
		//tbl1 = flat file in table format
		//tblOut = output table
		//lst1 = headers
		//sbl1 = current line being output
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		HashBasedTable<Integer,String,String> tbl1;
		HashBasedTable<Integer,String,String> tblOut;
		ArrayList<String> lstHeaders;
		StringBuilder sbl1;
		ArrayList<String> lstOut;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		tbl1 = HashBasedTable.create(dat1.iRows,dat1.iCols);
		for(int i=1;i<dat1.iRows;i++){
			for(int j=0;j<dat1.iCols;j++){
				tbl1.put(i, dat1.getString(0, j), dat1.getString(i,j));
			}
		}
		
		//loading output table
		tblOut = TableReformatter.convertFlatToPivot(tbl1, arg1.getValueString("sValueHeader"), arg1.getValueStringArray("rgsExpandHeaders"));
		
		//formatting output for printing
		lstOut = new ArrayList<String>(tblOut.rowKeySet().size());
		lstHeaders = loadHeaders(tblOut.columnKeySet());
		Collections.sort(lstHeaders);
		sbl1 = new StringBuilder();
		for(String s:lstHeaders){
			if(sbl1.length()>0){
				sbl1.append(",");
			}
			sbl1.append(s);
		}
		lstOut.add(sbl1.toString());
		for(Integer i:tblOut.rowKeySet()){
			sbl1 = new StringBuilder();
			for(String s:lstHeaders){
				if(sbl1.length()>0){
					sbl1.append(",");
				}
				sbl1.append(tblOut.get(i, s));
			}
			lstOut.add(sbl1.toString());
		}
		
		//terminating
		if(arg1.containsArgument("sOutputPath")){
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
			System.out.println("Done.");
		}else{
			for(int i=0;i<lstOut.size();i++){
				System.out.println(lstOut.get(i));
			}
		}
	}
	
	private static ArrayList<String> loadHeaders(Set<String> setHeaders){
		
		//lst1 = first list of headers
		//lst2 = second list of headers (new)
		//lstOut = output
		
		ArrayList<String> lst1;
		ArrayList<String> lst2;
		ArrayList<String> lstOut;
		
		lst1 = new ArrayList<String>();
		for(String s:setHeaders){
			if(!s.contains("=")){
				lst1.add(s);
			}
		}
		Collections.sort(lst1);
		
		lst2 = new ArrayList<String>();
		for(String s:setHeaders){
			if(s.contains("=")){
				lst2.add(s);
			}
		}
		Collections.sort(lst2);
		
		lstOut = new ArrayList<String>(lst1.size()+lst2.size());
		for(String s:lst1){
			lstOut.add(s);
		}
		for(String s:lst2){
			lstOut.add(s);
		}
		Collections.sort(lstOut);
		return lstOut;
	}
}
