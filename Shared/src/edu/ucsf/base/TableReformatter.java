package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.HashBasedTable;


/**
 * This class contains code for turning flat format tables to regular format tables.
 * @author jladau
 *
 */

public class TableReformatter{

	public static HashBasedTable<Integer,String,String> convertFlatToPivot(HashBasedTable<Integer,String,String> tblFlat, String sValueHeader, String[] rgsExpandHeaders){
		
		//tblOut = expanded table
		//sbl1 = current index
		//sVal = current value
		//mapOutRow(sClassificationVars) = returns new row index for specified set of classification variables
		//iCounter = current new row
		//lstKeepHeaders = array of headers to keep
		//iOutRow = output row
		//sNewHeader = new header
		//mapOutRowNoCross(sClassificationVars) = returns row index when no cross classification variable is specified
		
		HashBasedTable<Integer,String,String> tblOut;
		StringBuilder sbl1;
		String sVal;
		String sNewHeader;
		HashMap<String,Integer> mapOutRow;
		HashMap_AdditiveInteger<String> mapOutRowNoCross;
		int iCounter;
		int iOutRow;
		ArrayList<String> lstKeepHeaders;
		
		//initializing variables
		tblOut = HashBasedTable.create();
		iCounter=1;
		mapOutRow = new HashMap<String,Integer>();
		mapOutRowNoCross = new HashMap_AdditiveInteger<String>();
		
		//loading headers of columns for variables to keep in cross classification
		lstKeepHeaders = new ArrayList<String>(tblFlat.columnKeySet());
		lstKeepHeaders.remove(sValueHeader);
		for(String s:rgsExpandHeaders){
			lstKeepHeaders.remove(s);
		}
		
		//looping through rows of input table
		for(Integer iInputRow:tblFlat.rowKeySet()){
			
			//building new header
			sbl1 = new StringBuilder();
			for(String s:rgsExpandHeaders){
				if(sbl1.length()>0){
					sbl1.append(";");
				}
				sbl1.append(s + "=" + tblFlat.get(iInputRow, s));
			}
			sNewHeader = sbl1.toString();
			
			//loading value
			sVal = tblFlat.get(iInputRow, sValueHeader);
			
			//loading output
			if(lstKeepHeaders.size()>0){
			
				//loading string for cross classification variables to keep
				sbl1 = new StringBuilder();
				for(String s:lstKeepHeaders){
					sbl1.append(";" + tblFlat.get(iInputRow,s));
				}
				
				//updating output table and map for cross classification variables if necessary
				if(!mapOutRow.containsKey(sbl1.toString())){
					mapOutRow.put(sbl1.toString(), iCounter);
					for(String s:lstKeepHeaders){
						tblOut.put(iCounter, s, tblFlat.get(iInputRow,s));
					}
					iCounter++;
				}
			
				//loading output row
				iOutRow = mapOutRow.get(sbl1.toString());
				
			}else{
				
				if(!mapOutRowNoCross.containsKey(sbl1.toString())){
					mapOutRowNoCross.put(sbl1.toString(), 1);
				}
				iOutRow = mapOutRowNoCross.get(sbl1.toString());
				mapOutRowNoCross.putSum(sbl1.toString(), 1);
			}
			
			//outputting new result
			tblOut.put(iOutRow, sNewHeader, sVal);
		}
		return tblOut;
	}

}
