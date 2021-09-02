package edu.ucsf.Climatologies.IntervalListGenerator;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.sdm.SDMParser;

/**
 * This code generates a list of climatology intervals for use with when projecting models.
 * @author jladau
 */

public class IntervalListGeneratorLauncher {

	/**
	 * Writes a list of climatologies meeting specified criteria.
	 * @param Arguments pass as --{argument name}={argument value}. Name-value pairs are:
	 * 				
	 * 				<p>
	 * 				<li>rgsClimatologyEndpoints [list of strings] = Endpoints of climatology intervals. Endpoints should be listed in the form YYYY-MM-DD.		
	 * 				<p>
	 * 				<li>iClimatologyLength [integer] = Length of climatologies (in years).
	 * 				<p>
	 * 				<li>rgsDateRange [list of strings] = Earliest and latest climatology date to consider. In the format YYYY-MM-DD.
	 *				</ul>
	 **/
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//lst1 = list of projection intervals
		
		ArgumentIO arg1;
		ArrayList<String> lst1;
		
		arg1 = new ArgumentIO(rgsArgs);
		lst1 = SDMParser.loadClimatologyProjectionIntervals(
				arg1.getValueStringArray("rgsClimatologyEndpoints"), 
				arg1.getValueInt("iClimatologyLength"),
				arg1.getValueStringArray("rgsDateRange"));
		DataIO.writeToFile(lst1, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
