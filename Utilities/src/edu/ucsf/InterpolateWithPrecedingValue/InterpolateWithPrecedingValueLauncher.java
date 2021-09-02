package edu.ucsf.InterpolateWithPrecedingValue;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class InterpolateWithPrecedingValueLauncher {
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		
		ArgumentIO arg1;
		DataIO dat1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		for(int j=0;j<dat1.iCols;j++){
			for(int i=1;i<dat1.iRows;i++){
				if(dat1.getString(i, j).equals("NA")){
					dat1.setString(i, j, dat1.getString(i-1, j));
				}
			}
		}
		
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	

}
