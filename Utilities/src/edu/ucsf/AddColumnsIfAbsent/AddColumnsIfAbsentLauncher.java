package edu.ucsf.AddColumnsIfAbsent;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class AddColumnsIfAbsentLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//rgs1 = desired headers
		
		ArgumentIO arg1;
		DataIO dat1;
		String rgs1[];
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgs1 = arg1.getValueStringArray("rgsDesiredHeaders");
		
		for(String s:rgs1){
			if(!dat1.hasHeader(s)){
				dat1.appendToLastColumn(0, s);
				for(int i=1;i<dat1.iRows;i++){
					dat1.appendToLastColumn(i, "NA");
				}
			}
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
