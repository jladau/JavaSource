package edu.ucsf.DataToBinary;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Converts continuous data in file to binary values.
 * @author jladau
 */

public class DataToBinaryLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data io object
		
		ArgumentIO arg1;
		DataIO dat1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getDouble(i, arg1.getValueString("sDataHeader"))<arg1.getValueDouble("dPresenceThreshold")){
				dat1.setString(i, arg1.getValueString("sDataHeader"), "0");
			}else{
				dat1.setString(i, arg1.getValueString("sDataHeader"), "1");
			}
		}
		DataIO.writeToFile(dat1.getWriteableData(),arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
