package edu.ucsf.CSVToGnumeric;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Writes data from csv file to gnumeric xml file, useful for updating graphs
 * @author jladau
 */

public class CSVToGnumericLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//datCSV = data file
		//arg1 = arguments
		//bfrXML = buffered reader
		//lstOut = output
		//s1 = current line being read
		//iLines = number of lines
		//b1 = flag for whether within data block
		//lstData = data block being written
		
		ArrayList<String> lstOut;
		DataIO datCSV;
		ArgumentIO arg1;
		BufferedReader bfr1;
		String s1;
		int iLines;
		boolean b1;
		ArrayList<String> lstData;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datCSV = new DataIO(arg1.getValueString("sCSVPath"));

		//finding number of lines
		bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sGnumericPath")));
		iLines=0;
		while((s1=bfr1.readLine())!=null){
			iLines++;
		}
		bfr1.close();
		
		//loading data
		lstData = toXML(datCSV);
		
		//initializing output
		lstOut = new ArrayList<String>(iLines);
		b1 = false;
		bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sGnumericPath")));
		while((s1=bfr1.readLine())!=null){
			if(!s1.contains("<gnm:Cell Row=") && !s1.contains("</gnm:Cell>")){
				
				//checking if data block should be written
				if(b1==true){
					for(int i=0;i<lstData.size();i++){
						lstOut.add(lstData.get(i));
					}
					b1=false;
				}
				
				//outputting current line
				lstOut.add(s1);
			}else{
				if(b1==false){
					b1=true;
				}
			}
		}
		bfr1.close();
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static ArrayList<String> toXML(DataIO datCSV){
		
		//lstOut = output
		//sValueType = current value type
		//VALUE_STRING = string value
		//VALUE_FLOAT = float value
		//VALUE_BOOLEAN = boolean value
		//VALUE_STRING = string value
		//tim1 = test time
		
		String VALUE_STRING = "60";
		String VALUE_FLOAT = "40";
		String VALUE_BOOLEAN = "20";
		String VALUE_DATE="40\" ValueFormat=\"yyyy-mm-dd";
		ArrayList<String> lstOut;
		String sValueType = null;
		LocalDate tim1;
		
		//initializing output
		lstOut = new ArrayList<String>(datCSV.iRows*datCSV.iCols);
		
		//writing output
		for(int i=0;i<datCSV.iRows;i++){
			for(int j=0;j<datCSV.iCols;j++){
				if(i==0){
					sValueType = VALUE_STRING;
				}else{
					try{
						if(datCSV.getString(i,j).toLowerCase().equals("true") || datCSV.getString(i,j).toLowerCase().equals("false")){
							sValueType = VALUE_BOOLEAN;
						}else{
							try{
								Double.parseDouble(datCSV.getString(i, j));
								sValueType = VALUE_FLOAT;
							}catch(Exception f){	
								try{
									tim1 = new LocalDate(datCSV.getString(i, j));
									sValueType = VALUE_DATE;
									datCSV.setString(i, j, Integer.toString(Days.daysBetween(new LocalDate("1900-01-01"), tim1).getDays()+2));
								}catch(Exception e){
									sValueType = VALUE_STRING;
								}
							}	
						}
					}catch(Exception e){
						continue;
					}
				}
				lstOut.add(
						"<gnm:Cell Row=\"" +
						i +
						"\" Col=\"" +
						j +
						"\" ValueType=\"" +
						sValueType + 
						"\">" +
						datCSV.getString(i, j) +
						"</gnm:Cell>");
			}
		}
		
		//returning results
		return lstOut;
	}
}