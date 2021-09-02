package edu.ucsf.RecordUpdater;

import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Updates records as specified in file listing id field, record id, field to update, and new value
 * @author jladau
 *
 */

public class RecordUpdaterLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//datUpdate = update file
		//mapRow = map from IDs to row indices in data file
		//sRecordIDField = unique identification field
		//sRecordID = record ID
		//sNewValueField = field to update
		//sNewValue = new value

		HashMap<String,Integer> mapRow;
		ArgumentIO arg1;
		DataIO dat1;
		DataIO datUpdate;
		String sRecordID;
		String sNewValueField;
		String sNewValue;
		String sRecordIDField;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		datUpdate = new DataIO(arg1.getValueString("sReplacementDataPath"));
		
		//loading row map
		sRecordIDField = arg1.getValueString("sRecordIDField");
		mapRow = new HashMap<String,Integer>();
		for(int i=1;i<dat1.iRows;i++){
			mapRow.put(dat1.getString(i, sRecordIDField), i);
		}
		
		//making updates
		for(int i=1;i<datUpdate.iRows;i++){
			sRecordID = datUpdate.getString(i, "RECORD_ID");
			sNewValueField = datUpdate.getString(i, "NEW_VALUE_FIELD");
			sNewValue = datUpdate.getString(i, "NEW_VALUE");
			if(mapRow.containsKey(sRecordID)){
				dat1.setString(mapRow.get(sRecordID), sNewValueField, sNewValue);
			}
		}
		
		//outputting results
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
