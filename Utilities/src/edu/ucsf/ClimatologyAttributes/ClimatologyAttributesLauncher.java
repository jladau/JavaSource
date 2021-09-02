package edu.ucsf.ClimatologyAttributes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import com.google.common.collect.Range;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code appends the midpoints of climatologies to the last column of a data file.
 * @author jladau
 *
 */

public class ClimatologyAttributesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//s1 = current climatology
		//rng1 = current climatology
		//iDays = number of days between endpoints
		//bfr1 = reader
		//iCol = climatology column
		//rgs1 = current line in split format
		//prt1 = print writer
		//sLine = current line
		
		PrintWriter prt1;
		Range<LocalDate> rng1;
		ArgumentIO arg1;
		String s1;
		int iDays;
		BufferedReader bfr1;
		String sLine;
		String rgs1[];
		int iCol = -9999;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		
		try{
			
			//initializing input and output
			bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sDataPath")));
			prt1 = new PrintWriter(new FileWriter(arg1.getValueString("sOutputPath"), false));
			
			//initializing header
			sLine = bfr1.readLine();
			prt1.println(sLine + "," 
					+ arg1.getValueString("sClimatologyCol") + "_MIDPOINT," 
					+ arg1.getValueString("sClimatologyCol") + "_STARTPOINT," 
					+ arg1.getValueString("sClimatologyCol") + "_ENDPOINT," 
					+ arg1.getValueString("sClimatologyCol") + "_LENGTH");
			
			//finding column with climatology information
			rgs1 = sLine.split(",");
			for(int j=0;j<rgs1.length;j++){
				if(rgs1[j].equals(arg1.getValueString("sClimatologyCol"))){
					iCol=j;
					break;
				}
			}
			
			//looping through rows
			sLine = bfr1.readLine();
			do{
				
				//loading current data
				rgs1 = sLine.split(",");
				s1 = rgs1[iCol].replace("--", ";");
				rng1 = Range.closed(new LocalDate(s1.split(";")[0]), new LocalDate(s1.split(";")[1]));
				iDays = Days.daysBetween(rng1.lowerEndpoint(), rng1.upperEndpoint()).getDays();
				
				//outputting current data
				prt1.println(sLine
						+ "," + rng1.lowerEndpoint().plusDays(iDays/2).toString() 
						+ "," + rng1.lowerEndpoint()
						+ "," + rng1.upperEndpoint()
						+ "," + (Years.yearsBetween(rng1.lowerEndpoint(), rng1.upperEndpoint()).getYears()+1));
				sLine = bfr1.readLine();
			}while(sLine!=null);
			
			//closing
			bfr1.close();
			prt1.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	
		//terminating
		System.out.println("Done.");
	}

	public static void main0(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data with climatologies
		//s1 = current climatology
		//rng1 = current climatology
		//iDays = number of days between endpoints
		
		Range<LocalDate> rng1;
		ArgumentIO arg1;
		DataIO dat1;
		String s1;
		int iDays;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through climatologies and appending midpoints
		dat1.appendToLastColumn(0,arg1.getValueString("sClimatologyCol") + "_MIDPOINT," + arg1.getValueString("sClimatologyCol") + "_STARTPOINT," + arg1.getValueString("sClimatologyCol") + "_ENDPOINT," + arg1.getValueString("sClimatologyCol") + "_LENGTH");
		for(int i=1;i<dat1.iRows;i++){
			s1 = dat1.getString(i,arg1.getValueString("sClimatologyCol")).replace("--", ";");
			rng1 = Range.closed(new LocalDate(s1.split(";")[0]), new LocalDate(s1.split(";")[1]));
			iDays = Days.daysBetween(rng1.lowerEndpoint(), rng1.upperEndpoint()).getDays();
			dat1.appendToLastColumn(i, rng1.lowerEndpoint().plusDays(iDays/2).toString() 
					+ "," + rng1.lowerEndpoint()
					+ "," + rng1.upperEndpoint()
					+ "," + (Years.yearsBetween(rng1.lowerEndpoint(), rng1.upperEndpoint()).getYears()+1));
		}
		
		//writing output
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}
