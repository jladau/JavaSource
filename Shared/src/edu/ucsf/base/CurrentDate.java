package edu.ucsf.base;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Prints current date.
 * @author jladau
 */

public class CurrentDate {

	public static String currentDate(){
		
		//dtf1 = date format object
		//dat1 = date object
		
		DateFormat dtf1;
		Date dat1;
		
		//initializing date objects
		dtf1 = new SimpleDateFormat("yyyy-MM-dd");
		dat1 = new Date();
		
		//returning result
		return dtf1.format(dat1);
	}
}
