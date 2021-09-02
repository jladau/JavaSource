package edu.ucsf.UpdateRecordsInCSV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class UpdateRecordsInCSVLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datOld = old data to be updated
		//datNew = new data to be input
		//rgsRowIDs = row id headers
		//tblOld = map from row ids, column ids to old data
		//tblNew = map from row ids, column ids to new data
		//lst1 = list of headers that are not row ids in old data
		//lst2 = list of headers that are not row ids in new data
		//set1 = set of row id headers
		//sbl1 = current row id
		//s1 = current row id
		//map1 = map from row identifiers to row indexes in old data
		//s2 = current entry
		//lstUpdate = updated values
		
		ArgumentIO arg1;
		DataIO datOld;
		DataIO datNew;
		String rgsRowIDs[];
		HashBasedTable<String,String,String> tblOld;
		HashBasedTable<String,String,String> tblNew;
		ArrayList<String> lst1;
		ArrayList<String> lst2;
		HashSet<String> set1;
		StringBuilder sbl1;
		String s1;
		String s2;
		HashMap<String,Integer> map1;
		ArrayList<String> lstUpdate;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datOld = new DataIO(arg1.getValueString("sOldDataPath"));
		datNew = new DataIO(arg1.getValueString("sUpdateDataPath"));
		rgsRowIDs = arg1.getValueStringArray("rgsRowIDHeaders");
		tblOld = HashBasedTable.create(datOld.iRows,datOld.iCols);
		tblNew = HashBasedTable.create(datNew.iRows,datNew.iCols);
		set1 = new HashSet<String>();
		for(String s:rgsRowIDs){
			set1.add(s);
		}
		map1 = new HashMap<String,Integer>(datOld.iRows);
		
		//loading non-id row headers
		lst1 = new ArrayList<String>(datOld.iCols);
		for(int j=0;j<datOld.iCols;j++){
			if(!set1.contains(datOld.getString(0, j))){
				lst1.add(datOld.getString(0, j));
			}
		}
		lst2 = new ArrayList<String>(datNew.iCols);
		for(int j=0;j<datNew.iCols;j++){
			if(!set1.contains(datNew.getString(0, j))){
				lst2.add(datNew.getString(0, j));
			}
		}
		
		//loading data into tables
		for(int i=1;i<datOld.iRows;i++){
			sbl1 = new StringBuilder();
			for(int k=0;k<rgsRowIDs.length;k++){
				if(k>0){
					sbl1.append(";");
				}
				sbl1.append(datOld.getDouble(i, rgsRowIDs[k]));
			}
			s1 = sbl1.toString();
			map1.put(s1, i);
			for(int j=0;j<datOld.iCols;j++){
				if(!set1.contains(datOld.getString(0, j))){
					tblOld.put(s1, datOld.getString(0, j), datOld.getString(i, j));
				}
			}
		}
		for(int i=1;i<datNew.iRows;i++){
			sbl1 = new StringBuilder();
			for(int k=0;k<rgsRowIDs.length;k++){
				if(k>0){
					sbl1.append(";");
				}
				sbl1.append(datNew.getDouble(i, rgsRowIDs[k]));
			}
			s1 = sbl1.toString();
			for(int j=0;j<datNew.iCols;j++){
				if(!set1.contains(datNew.getString(0, j))){
					tblNew.put(s1, datNew.getString(0, j), datNew.getString(i, j));
				}
			}
		}
		
		//updating records as necessary
		lstUpdate = new ArrayList<String>(tblOld.size()+1);
		lstUpdate.add("ROW_IDENTIFIER,FIELD,OLD_VALUE,NEW_VALUE");
		for(String s:tblOld.rowKeySet()){
			for(String t:tblOld.columnKeySet()){
				s2 = tblOld.get(s, t);
				
				//****************************
				System.out.println(s + "," + t + "," + tblOld.get(s, t) + "," + tblNew.get(s, t));
				//****************************
				
				if(tblNew.contains(s, t) && !tblNew.get(s, t).equals(s2)){
					datOld.setString(map1.get(s), t, tblNew.get(s, t));
					lstUpdate.add(s + "," + t + "," + s2 + "," + tblNew.get(s, t));
				}
			}
		}
		
		//outputting results
		DataIO.writeToFile(datOld.getWriteableData(), arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(lstUpdate, arg1.getValueString("sOutputPath").replace(".csv","-updated-records.csv"));
		System.out.println("Done.");
		
		
		
		
	}
}
