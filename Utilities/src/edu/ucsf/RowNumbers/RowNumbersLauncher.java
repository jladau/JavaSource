package edu.ucsf.RowNumbers;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class RowNumbersLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		
		ArgumentIO arg1;
		DataIO dat1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		dat1.setString(0,0,"ROW_NUMBER," + dat1.getString(0,0));
		for(int i=1;i<dat1.iRows;i++){
			dat1.setString(i,0,i + "," + dat1.getString(i,0));
		}
		DataIO.writeToFile(dat1.getWriteableData(),arg1.getValueString("sOutputPath"));
	}
}
