package edu.ucsf.ListsToPivotTable;

import java.util.HashMap;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Converts data in lists (key1:value1;key2:value2,...) to a pivot table
 * @author jladau
 */

public class ListsToPivotTableLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//mapList(list key) = array of values for specified key
		//rgs1 = current list in split format
		//sKey = current key
		//sValue = current value
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<String,String[]> mapListKeys;
		String rgs1[];
		String sKey;
		String sValue;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading set of list keys
		mapListKeys = new HashMap<String,String[]>();
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getString(i, arg1.getValueString("sListHeader")).equals("na")){
				continue;
			}
			rgs1 = dat1.getString(i, arg1.getValueString("sListHeader")).split(";");
			for(int k=0;k<rgs1.length;k++){
				sKey = rgs1[k].split("=")[0];
				if(!mapListKeys.containsKey(sKey)){
					mapListKeys.put(sKey,new String[dat1.iRows]);
					mapListKeys.get(sKey)[0]= arg1.getValueString("sListHeader") + "=" + sKey;
					for(int j=1;j<dat1.iRows;j++){
						mapListKeys.get(sKey)[j]="0";
					}
				}
			}
		}
		
		//loading values
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getString(i, arg1.getValueString("sListHeader")).equals("na")){
				continue;
			}
			rgs1 = dat1.getString(i, arg1.getValueString("sListHeader")).split(";");
			for(int k=0;k<rgs1.length;k++){
				sKey = rgs1[k].split("=")[0];
				sValue = rgs1[k].split("=")[1];
				mapListKeys.get(sKey)[i]=sValue;
			}
		}
		
		//appending and outputting results
		for(String s:mapListKeys.keySet()){
			dat1.appendToLastColumn(mapListKeys.get(s));
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
		
	}
}