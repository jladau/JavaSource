package gov.lbnl.CumulativeToDaily;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class CumulativeToDailyLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//tbl1 = output
		
		ArgumentIO arg1;
		DataIO dat1;
		HashBasedTable<Integer,String,Double> tbl1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		tbl1 = HashBasedTable.create(dat1.iRows,dat1.iCols);
		for(String s:dat1.getHeaders()){
			for(int i=2;i<dat1.iRows;i++){
				tbl1.put(i-1,s,dat1.getDouble(i,s)-dat1.getDouble(i-1,s));
			}
		}
		DataIO.writeToFile((new DataIO(tbl1)).getWriteableData(),arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
