package edu.ucsf.YearsBetweenClimatologies;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This class appends the number of years between climatologies in a csv file.
 * @author jladau
 */


public class YearsBetweenClimatologiesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//tim0 = start time
		//tim1 = end time
		
		ArgumentIO arg1;
		DataIO dat1;
		LocalDate tim0;
		LocalDate tim1;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through rows
		dat1.appendToLastColumn(0,"YEARS_BETWEEN_CLIMATOLOGIES");
		for(int i=1;i<dat1.iRows;i++){
			tim0 = new LocalDate(dat1.getString(i, arg1.getValueString("sStartClimatologyHeader")));
			tim1 = new LocalDate(dat1.getString(i, arg1.getValueString("sEndClimatologyHeader")));
			dat1.appendToLastColumn(i,Years.yearsBetween(tim0, tim1).getYears());
		}
		
		//writing output
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}
