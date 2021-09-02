package edu.ucsf.WOADateIntervalCodesToStrings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Converts date interval codes (in WOA format) to strings giving duration, start date, and end date
 * @author jladau
 */


public class WOADateIntervalCodesToStringsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//rgs1 = starting and ending codes
		//rgi1 = starting and ending years
		//lstOut = output
		//bfr1 = buffered reader
		//s1 = current line
		//iCol = column with codes
		//rgs2 = current line
		
		String rgs1[];
		String rgs2[];
		int rgi1[];
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		BufferedReader bfr1;
		String s1;
		int iCol = -9999;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("START_DATE,END_DATE,DURATION");
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		lstOut = new ArrayList<String>(1000);
		
		//looping through output lines
		try{
			
			bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sDataPath")));
			s1 = bfr1.readLine();
			rgs2 = s1.split(",");
			for(int i=0;i<rgs2.length;i++){
				if(rgs2[i].equals(arg1.getValueString("sCodeColumn"))){
					iCol = i;
					break;
				}
			}
			s1 = bfr1.readLine();
			while(s1!=null){
				rgs2 = s1.split(",");				
				s1 = bfr1.readLine();
				
				rgs1 = new String[2];
				rgs1[0] = rgs2[iCol].substring(0, 2);
				rgs1[1] = rgs2[iCol].substring(2, 4);
				rgi1 = new int[2];
				for(int k=0;k<rgs1.length;k++){
					if(rgs1[k].startsWith("A")){
						rgs1[k]=rgs1[k].replace("A","200");
					}else if(rgs1[k].startsWith("B")){
						rgs1[k]=rgs1[k].replace("B","201");
					}else{
						rgs1[k]="19"+rgs1[k];
					}
					rgi1[k]=Integer.parseInt(rgs1[k]);
				}
				lstOut.add(rgi1[0]+"-01-01," + rgi1[1]+"-12-31," + (rgi1[1]-rgi1[0]+1));
				
				if(lstOut.size()==1000){
					DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"),true);
					lstOut = new ArrayList<String>();
				}
			}
			if(lstOut.size()!=0){
				DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"),true);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//terminating
		System.out.println("Done.");
	}	
}
