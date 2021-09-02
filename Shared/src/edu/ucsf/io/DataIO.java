package edu.ucsf.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.joda.time.LocalDate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;

/**
 * Data object for efficiently reading and writing data from a file.
 * @author Joshua Ladau <br/>
 * 		   jladau@gmail.com
 */
public class DataIO {

	/**Column headers object.**/
	private ColumnHeaders clmHeaders;
	
	/**Data file.**/
	public ArrayList<ArrayList<String>> lstData;
	
	/**Number of rows of data.**/
	public int iRows;
	
	/**Number of columns of data.**/
	public int iCols;
	
	/**
	 * Constructor
	 * @param sDataPath Path to data.
	 */
	public DataIO(String sDataPath){
		
		//bfr1 = buffered reader
		//lst1 = list of data with string arrays
		//s1 = current line
		//i1 = number of lines
		
		BufferedReader bfr1;
		ArrayList<String[]> lst1;
		String s1;
		int i1;
		
		try {
			
			bfr1 = new BufferedReader(new FileReader(sDataPath));
			i1=0;
			while((s1=bfr1.readLine())!=null){
				i1++;
			}
			lst1 = new ArrayList<String[]>(i1+1);
			bfr1 = new BufferedReader(new FileReader(sDataPath));
			i1=0;
			
			
			while((s1=bfr1.readLine())!=null){
				
				//*********************
				//if(s1.split(",", -1).length==1){
				//	System.out.println(s1);
				//}
				//*********************
				
				lst1.add(s1.split(",", -1));
			}
			lstData = new ArrayList<ArrayList<String>>(lst1.size());
			for(int i=0;i<lst1.size();i++){
				lstData.add(new ArrayList<String>(lst1.get(i).length));
				for(int j=0;j<lst1.get(i).length;j++){
					lstData.get(i).add(lst1.get(i)[j].trim());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//loading number of rows and columns
		iRows = lstData.size();
		iCols = lstData.get(0).size();
		
		//removing quotes
		removeQuotes();
		
		//loading column headers
		clmHeaders = new ColumnHeaders(lstData);
	}
	
	//TODO write unit test
	/**
	 * Gets row
	 * @param iRow Row to get
	 * @return Requested row in array format
	 */
	public String[] getRow(int iRow){
		
		//rgs1 = output
		
		String rgs1[];
		
		rgs1 = new String[this.iCols];
		for(int j=0;j<this.iCols;j++){
			rgs1[j]=this.getString(iRow, j);
		}
		return rgs1;
	}
	
	//TODO write unit test
	/**
	 * Reads file without any breaking of lines by a delimeter
	 * @param sDataPath Path to data.
	 */
	public static ArrayList<String> readFileNoDelimeter(String sDataPath) throws Exception{
		
		//lstOut = output
		//bfr1 = buffered reader
		//s1 = current line
		
		ArrayList<String> lstOut;
		BufferedReader bfr1;
		String s1;
		
		lstOut = new ArrayList<String>(10000);
		bfr1 = new BufferedReader(new FileReader(sDataPath));
		while((s1=bfr1.readLine())!=null){
			lstOut.add(s1.trim());
		}
		bfr1.close();
		return lstOut;
	}
	
	/**
	 * Removes quotes from all entries in data file
	 */
	private void removeQuotes(){
		for(int i=0;i<iRows;i++){
			for(int j=0;j<iCols;j++){
				try{
					set(i,j,get(i,j).replace("\"",""));
				}catch(Exception e){
				}
			}
		}
	}
	
	/**
	 * Constructor
	 * @param lstData Data in string array format.
	 */
	public DataIO(ArrayList<ArrayList<String>> lstData){
		
		//loading data
		this.lstData = lstData;
		
		//loading number of rows and columns
		iRows = lstData.size();
		iCols = lstData.get(0).size();
		
		//loading column headers
		clmHeaders = new ColumnHeaders(lstData);
	}

	/**
	 * Constructor
	 * @param rgsData Data in string array format.
	 */
	public DataIO(String[][] rgsData){
		
		this.lstData = new ArrayList<ArrayList<String>>();
		for(int i=0;i<rgsData.length;i++){
			lstData.add(new ArrayList<String>());
			for(int j=0;j<rgsData[0].length;j++){
				lstData.get(lstData.size()-1).add(rgsData[i][j]);
			}
		}
		
		//loading number of rows and columns
		iRows = lstData.size();
		iCols = lstData.get(0).size();
		
		//loading column headers
		clmHeaders = new ColumnHeaders(lstData);
	}
	
	/**
	 * Constructor
	 * @param tbl1 Data in table format
	 */
	public DataIO(HashBasedTable<?,String,Double> tbl1){
			
		for(Object o:tbl1.rowKeySet()){
			if(o instanceof String){
				initializeTableString(tbl1);
			}else if(o instanceof Integer){
				initializeTableInteger(tbl1);
			}
			break;
		}
	}
	
	private void initializeTableString(HashBasedTable<?,String,Double> tbl1){
		
		//lst1 = list of row headers
		//lst2 = list of column headers
		
		ArrayList<String> lst1;
		ArrayList<String> lst2;
		
		lst1 = new ArrayList<String>(tbl1.rowKeySet().size());
		for(Object o:tbl1.rowKeySet()){
			lst1.add((String) o);
		}
		lst2 = new ArrayList<String>(tbl1.columnKeySet());
		this.lstData = new ArrayList<ArrayList<String>>();
		lstData.add(lst2);
		for(String s:lst1){
			lstData.add(new ArrayList<String>());
			for(int j=0;j<lst2.size();j++){
				lstData.get(lstData.size()-1).add(Double.toString(tbl1.get(s,lst2.get(j))));
			}
		}
		
		//loading number of rows and columns
		iRows = lstData.size();
		iCols = lstData.get(0).size();
		
		//loading column headers
		clmHeaders = new ColumnHeaders(lstData);
	}
	
	private void initializeTableInteger(HashBasedTable<?,String,Double> tbl1){
		
		//lst1 = list of row headers
		//lst2 = list of column headers
		//iMax = maximum value
		//iMin = minimum value
		
		ArrayList<Integer> lst1;
		ArrayList<String> lst2;
		int iMax;
		int iMin;
		
		lst1 = new ArrayList<Integer>(tbl1.rowKeySet().size());
		for(Object o:tbl1.rowKeySet()){
			lst1.add((Integer) o);
		}
		lst2 = new ArrayList<String>(tbl1.columnKeySet());
		this.lstData = new ArrayList<ArrayList<String>>();
		lstData.add(lst2);
		iMax=-Integer.MAX_VALUE;
		iMin=Integer.MAX_VALUE;
		for(Integer i:lst1){
			if(i>iMax){
				iMax = i;
			}
			if(i<iMin){
				iMin = i;
			}
		}
		for(int i=iMin;i<=iMax;i++){
			if(tbl1.rowKeySet().contains(i)){
				lstData.add(new ArrayList<String>());
				for(int j=0;j<lst2.size();j++){
					lstData.get(lstData.size()-1).add(Double.toString(tbl1.get(i,lst2.get(j))));
				}
			}
		}
		
		//loading number of rows and columns
		iRows = lstData.size();
		iCols = lstData.get(0).size();
		
		//loading column headers
		clmHeaders = new ColumnHeaders(lstData);
	}
	
	/**
	 * Constructor
	 * @param rgsData Data in string array format.
	 */
	public DataIO(String[] rgsData){
		
		this.lstData = new ArrayList<ArrayList<String>>();
		for(int i=0;i<rgsData.length;i++){
			lstData.add(new ArrayList<String>());
			lstData.get(lstData.size()-1).add(rgsData[i]);
		}
		
		//loading number of rows and columns
		iRows = lstData.size();
		iCols = lstData.get(0).size();
		
		//loading column headers
		clmHeaders = new ColumnHeaders(lstData);
	}
	
	/**
	 * Appends value to last column
	 * @param sValue Value to append
	 */
	public void appendToLastColumn(int iRow, String sValue){
		lstData.get(iRow).add(sValue);
		iCols = -9999;
	}
	
	/**
	 * Removes rows
	 * @param lstRowsToRemove List of rows to remove
	 */
	
	public void removeRows(ArrayList<Integer> lstRowsToRemove) {
		Collections.sort(lstRowsToRemove);
		for(int i=lstRowsToRemove.size()-1;i>=0;i--) {
			lstData.remove((int) lstRowsToRemove.get(i));
		}
		iRows-=lstRowsToRemove.size();
	}
	
	/**
	 * Appends values to last column
	 * @param rgs1 Data to append
	 */
	public void appendToLastColumn(String rgs1[]){
		for(int i=0;i<iRows;i++){
			lstData.get(i).add(rgs1[i]);
		}
		iCols = -9999;
	}
	
	/**
	 * Appends values to last column
	 * @param rgs1 Data to append
	 */
	public void appendToLastColumn(double rgd1[], String sHeader){
		
		lstData.get(0).add(sHeader);
		for(int i=1;i<iRows;i++){
			lstData.get(i).add(Double.toString(rgd1[i-1]));
		}
		iCols = -9999;
	}
	
	
	/**
	 * Appends value to last column
	 * @param dValue Value to append
	 */
	public void appendToLastColumn(int iRow, double dValue){
		lstData.get(iRow).add(Double.toString(dValue));
		iCols = -9999;
	}
	
	/**
	 * Appends value to last column
	 * @param iValue Value to append
	 */
	public void appendToLastColumn(int iRow, int iValue){
		lstData.get(iRow).add(Integer.toString(iValue));
		iCols = -9999;
	}
	
	/**
	 * Gets specified column (minus the header) in arraylist format
	 * @param sColumn Column to look up
	 * @return Column (minus header)
	 */
	public ArrayList<Integer> getIntegerColumn(String sColumn){
		
		//lst1 = output
		
		ArrayList<Integer> lst1;
		
		lst1 = new ArrayList<Integer>(iRows-1);
		for(int i=1;i<iRows;i++){
			lst1.add(this.getInteger(i, sColumn));
		}
		return lst1;
	}
	
	
	/**
	 * Gets specified column (minus the header) in arraylist format
	 * @param sColumn Column to look up
	 * @return Column (minus header)
	 */
	public ArrayList<Double> getDoubleColumn(String sColumn){
		
		//lst1 = output
		
		ArrayList<Double> lst1;
		
		lst1 = new ArrayList<Double>(iRows-1);
		for(int i=1;i<iRows;i++){
			lst1.add(this.getDouble(i, sColumn));
		}
		return lst1;
	}
	
	/**
	 * Creates a string map using the specified key and value columns
	 * @param rgsKeys Key headers to use
	 * @param rgsValues Value headers to use
	 * @return Map from keys (comma-separated) to values
	 */
	public HashMap<String,Double> getDoubleMap(String[] rgsKeys, String[] rgsValues){
		
		//lstKeys = key list
		//lstValues = value list
		//map1 = output
		
		ArrayList<String> lstKeys;
		ArrayList<String> lstValues;
		HashMap<String,Double> map1;
		
		lstKeys = this.getStringColumns(rgsKeys);
		lstValues = this.getStringColumns(rgsValues);
		map1 = new HashMap<String,Double>(lstKeys.size());
		for(int i=0;i<lstKeys.size();i++) {
			try{	
				map1.put(lstKeys.get(i),Double.parseDouble(lstValues.get(i)));
			}catch(Exception e) {
				
			}
		}
		return map1;
	}
	
	/**
	 * Creates a string map using the specified key and value columns
	 * @param rgsKeys Key headers to use
	 * @param rgsValues Value headers to use
	 * @return Map from keys (comma-separated) to values
	 */
	public HashMap<String,String> getStringMap(String[] rgsKeys, String[] rgsValues){
		
		//lstKeys = key list
		//lstValues = value list
		//map1 = output
		
		ArrayList<String> lstKeys;
		ArrayList<String> lstValues;
		HashMap<String,String> map1;
		
		lstKeys = this.getStringColumns(rgsKeys);
		lstValues = this.getStringColumns(rgsValues);
		map1 = new HashMap<String,String>(lstKeys.size());
		for(int i=0;i<lstKeys.size();i++) {
			map1.put(lstKeys.get(i),lstValues.get(i));
		}
		return map1;
	}
	
	/**
	 * Creates a string multimap using the specified key and value columns
	 * @param rgsKeys Key headers to use
	 * @param rgsValues Value headers to use
	 * @return Map from keys (comma-separated) to values
	 */
	public HashMultimap<String,String> getStringMultimap(String[] rgsKeys, String[] rgsValues){
		
		//lstKeys = key list
		//lstValues = value list
		//map1 = output
		
		ArrayList<String> lstKeys;
		ArrayList<String> lstValues;
		HashMultimap<String,String> map1;
		
		lstKeys = this.getStringColumns(rgsKeys);
		lstValues = this.getStringColumns(rgsValues);
		map1 = HashMultimap.create(lstKeys.size(),10);
		for(int i=0;i<lstKeys.size();i++) {
			map1.put(lstKeys.get(i),lstValues.get(i));
		}
		return map1;
	}
	
	/**
	 * Gets specified columns (minus the header) in arraylist format
	 * @param rgsColumns Columns to look up
	 * @return Columns (minus header)
	 */
	public ArrayList<String> getStringColumns(String[] rgsColumns){
		
		//lst1 = output
		//sbl1 = current row
		
		ArrayList<String> lst1;
		StringBuilder sbl1;
		
		lst1 = new ArrayList<String>(iRows-1);
		for(int i=1;i<iRows;i++){
			sbl1 = new StringBuilder();
			sbl1.append(this.getString(i, rgsColumns[0]));
			for(int k=1;k<rgsColumns.length;k++) {
				sbl1.append("," + this.getString(i, rgsColumns[k]));
			}
			lst1.add(sbl1.toString());
		}
		return lst1;
	}
	
	/**
	 * Gets specified column (minus the header) in arraylist format
	 * @param sColumn Column to look up
	 * @return Column (minus header)
	 */
	public ArrayList<String> getStringColumn(String sColumn){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = new ArrayList<String>(iRows-1);
		for(int i=1;i<iRows;i++){
			lst1.add(this.getString(i, sColumn));
		}
		return lst1;
	}
	
	//TODO write unit test
	/**
	 * Gets time from specified row and column (identified by header)
	 * @param iRow Row
	 * @param sColumn Column (identified by header)
	 */
	
	public LocalDate getTime(int iRow, String sColumn){
		
		//s1 = current value in string format
		
		String s1;
		
		if(!clmHeaders.containsKey(sColumn)){
			return null;
		}else{
			s1 = this.getString(iRow, sColumn);
			return (new LocalDate(Integer.parseInt(s1.split("-")[0]),Integer.parseInt(s1.split("-")[1]),Integer.parseInt(s1.split("-")[2].substring(0,2))));
		}
	}
	
	/**
	 * Gets string value from specified row and column (identified by header)
	 * @param iRow Row
	 * @param sColumn Column (identified by header)
	 */
	public String getString(int iRow, String sColumn){
		if(!clmHeaders.containsKey(sColumn)){
			return null;
		}else{
			return getString(iRow,clmHeaders.get(sColumn));
		}
	}

	//TODO write unit test
	/**
	 * Gets values in specified columns
	 * @param iRow Row
	 * @param lstColumns Columns (identified by header)
	 * @return Concatenated columns
	 */
	public String getString(int iRow, ArrayList<String> lstColumns){
		
		//sbl1 = output
		
		StringBuilder sbl1;
		
		sbl1 = new StringBuilder();
		for(int i=0;i<lstColumns.size();i++){
			if(i>0){
				sbl1.append(",");
			}
			sbl1.append(getString(iRow,lstColumns.get(i)));
		}
		return sbl1.toString();
	}
	
	
	//TODO write unit test
	/**
	 * Gets values in specified columns
	 * @param iRow Row
	 * @param rgsColumns Columns (identified by header)
	 * @return Concatenated columns
	 */
	public String getString(int iRow, String[] rgsColumns){
		
		//sbl1 = output
		
		StringBuilder sbl1;
		
		sbl1 = new StringBuilder();
		for(int i=0;i<rgsColumns.length;i++){
			if(i>0){
				sbl1.append(",");
			}
			sbl1.append(getString(iRow,rgsColumns[i]));
		}
		return sbl1.toString();
	}
	
	/**
	 * Gets string value from specified row and column
	 * @param iRow Row
	 * @param iCol Column
	 */
	public String getString(int iRow, int iCol){
		return get(iRow,iCol);
	}
	
	/**
	 * Gets string value from specified row and column.
	 * @param iRow Row
	 * @param iCol Column
	 */
	private String get(int iRow, int iCol){
		return lstData.get(iRow).get(iCol);
	}
	
	/**
	 * Sets string value from specified row and column.
	 * @param iRow Row
	 * @param iCol Column
	 */
	private String set(int iRow, int iCol, String sValue){
		return lstData.get(iRow).set(iCol,sValue);
	}
	
	
	/**
	 * Gets double value from specified row and column (identified by header)
	 * @param iRow Row
	 * @param sColumn Column (identified by header)
	 */
	public double getDouble(int iRow, String sColumn){
		if(!clmHeaders.containsKey(sColumn)){
			return Double.NaN;
		}
		return getDouble(iRow,clmHeaders.get(sColumn));
	}
	
	/**
	 * Gets double value from specified row and column (identified by header)
	 * @param iRow Row
	 * @param iCol Column
	 */
	public double getDouble(int iRow, int iCol){
		
		//dOut = output
		
		double dOut;
		
		try{
			dOut=Double.parseDouble(get(iRow,iCol));
			if(dOut==-9999){
				dOut=Double.NaN;
			}
		}catch(Exception e){
			dOut=Double.NaN;
		}
		return dOut;	
	}
	
	/**
	 * Gets integer value from specified row and column (identified by header)
	 * @param iRow Row
	 * @param sColumn Column (identified by header)
	 */
	public int getInteger(int iRow, String sColumn){
		return getInteger(iRow,clmHeaders.get(sColumn));
	}

	/**
	 * Gets integer value from specified row and column (identified by header)
	 * @param iRow Row
	 * @param iCol Column
	 */
	public int getInteger(int iRow, int iCol){
		
		//iOut = output
		//s1 = value in string format
		
		int iOut;
		String s1;
		
		try{
			s1 = get(iRow,iCol);
			if(s1.endsWith(".0")){
				s1=s1.replace(".0", "");
			}
			iOut=Integer.parseInt(s1);
		}catch(Exception e){
			iOut=-9999;
		}
		return iOut;
	}
	
	//TODO write unit test
	/**
	 * Checks if data has header
	 * @return True if header is present, false if not
	 */
	public boolean hasHeader(String sHeader){
		return clmHeaders.containsKey(sHeader);
	}
	
	//TODO write unit test
	/**
	 * Gets headers
	 */
	public HashSet<String> getHeaders(){
		return new HashSet<String>(clmHeaders.setHeaders);
	}
	
	/**
	 * Sets string value from specified row and column
	 * @param iRow Row
	 * @param iCol Column
	 */
	public void setString(int iRow, int iCol, String sValue){
		set(iRow,iCol,sValue);
	}

	/**
	 * Sets string value from specified row and column (identified by header)
	 * @param iRow Row
	 * @param sColumn Column (identified by header)
	 */
	public void setString(int iRow, String sColumn, String sValue){
		if(clmHeaders.containsKey(sColumn)){
			setString(iRow,clmHeaders.get(sColumn),sValue);
		}
	}
	
	public HashBasedTable<Integer,String,String> toStringTable(){
		
		//tbl1 = output
		
		HashBasedTable<Integer,String,String> tbl1;
		
		tbl1 = HashBasedTable.create(this.iRows,this.iCols);
		for(int i=1;i<this.iRows;i++){
			for(String s:this.getHeaders()){
				tbl1.put(i-1,s,this.getString(i,s));
			}
		}
		return tbl1;
	}

	public HashBasedTable<Integer,String,Double> toDoubleTable(){
		
		//tbl1 = output
		
		HashBasedTable<Integer,String,Double> tbl1;
		
		tbl1 = HashBasedTable.create(this.iRows,this.iCols);
		for(int i=1;i<this.iRows;i++){
			for(String s:this.getHeaders()){
				try {	
					tbl1.put(i-1,s,Double.parseDouble(this.getString(i,s)));
				}catch(Exception e) {
					
				}
			}
		}
		return tbl1;
	}
	
	/**
	 * Returns data in format that can easily be written using static write functions
	 * @return Data in writeable array list format.
	 */
	public ArrayList<String> getWriteableData(){
		
		//lst1 = output
		//sbl1 = current line
		
		ArrayList<String> lst1;
		StringBuilder sbl1;
		
		lst1 = new ArrayList<String>(iRows);
		for(int i=0;i<iRows;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<lstData.get(0).size();j++){
				if(j>0){
					sbl1.append(",");
				}
				sbl1.append(get(i,j));
			}
			lst1.add(sbl1.toString());
		}
		return lst1;
	}
	
	/**
	 * Writes output to a specified path.
	 * @param lstOutput Output.
	 * @param sOutputPath Path to write output.
	 */
	public static void writeToFile(ArrayList<String> lstOutput, String sOutputPath){
		writeToFile(lstOutput,sOutputPath,false);
	}
	
	/**
	 * Writes output to a specified path.
	 * @param ltblOutput Output.
	 * @param sOutputPath Path to write output.
	 */
	public static void writeToFile(HashBasedTable<Integer,String,Double> tblOutput, String sOutputPath){
		writeToFile((new DataIO(tblOutput)).getWriteableData(), sOutputPath);
	}
	
	/**
	 * Writes output to a specified path with the option of appending to an existing file.
	 * @param lstOutput Output.
	 * @param sOutputPath Path to write output.
	 * @param bAppend Flag for whether to append output. 
	 */
	public static void writeToFile(ArrayList<String> lstOutput, String sOutputPath, boolean bAppend){
		
		//prt1 = print writer
		
		PrintWriter prt1;
		
		try{
			
			//initializing output
			prt1 = new PrintWriter(new FileWriter(sOutputPath, bAppend));
			
			//looping through output array
			for(int i=0; i<lstOutput.size(); i++){
				prt1.println(lstOutput.get(i));
			}
			prt1.close();
		}catch(IOException e){
			  System.err.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Writes output to a specified path and also writes an empty file signifying completion of a specified task.
	 * @param lstOutput Output.
	 * @param sOutputPath Path to write output.
	 * @param iTaskID Task ID to append to output.
	 */
	public static void writeToFileWithCompletionFile(ArrayList<String> lstOutput, String sOutputPath, int iTaskID){
		writeToFile(lstOutput,sOutputPath + "_" + iTaskID,false);
		writeCompletionFile(sOutputPath,iTaskID);
	}
	
	/**
	 * Writes output to a specified path and also writes an empty file signifying completion of a specified task. Can append output to existing file.
	 * @param lstOutput Output.
	 * @param sOutputPath Path to write output.
	 * @param bAppend 
	 * @param iTaskID Task ID to append to output.
	 */
	public static void writeToFileWithCompletionFile(ArrayList<String> lstOutput, String sOutputPath, boolean bAppend, int iTaskID){
		writeToFile(lstOutput,sOutputPath + "_" + iTaskID,bAppend);
		writeCompletionFile(sOutputPath,iTaskID);
	}
	
	/**
	 * Writes an empty file signifying completion of a task.
	 * @param sOutputPath Output file path.
	 * @param iTaskID Task ID.
	 */
	public static void writeCompletionFile(String sOutputPath, int iTaskID){
		writeToFile(new ArrayList<String>(),sOutputPath + "_" + iTaskID + ".complete",false);
	}
	
	/**
	 * This object loads column headers from a data file (first row is assumed to be headers)
	 * @author jladau
	 *
	 */
	private class ColumnHeaders {

		//mapCol(sHeader) = returns the index of the column with the given header
		
		/**Keys are header names (strings). Returns the index of the column with the given header.**/
		private HashMap<String,Integer> mapCol;
		
		/**List of all headers**/
		private HashSet<String> setHeaders;
		
		/**
		 * Constructor
		 * @param rgsData Data file with headers in first row
		 */
		private ColumnHeaders(ArrayList<ArrayList<String>> lstData){
			
			//initializing column map
			mapCol = new HashMap<String,Integer>();
			setHeaders = new HashSet<String>();
			
			//looping through columns to load headers
			for(int j=0;j<lstData.get(0).size();j++){
				mapCol.put(lstData.get(0).get(j), j);
				mapCol.put(lstData.get(0).get(j).toLowerCase(), j);
				setHeaders.add(lstData.get(0).get(j));
			}
		}
		
		/**
		 * Looks up column index of header.
		 * @param sKey Header value.
		 * @return Column index of header.
		 */
		private int get(String sKey){
			return mapCol.get(sKey);
		}
		
		/**
		 * Checks whether column map contains a specified key.
		 * @param sKey Key to look up.
		 * @return True if key is in map; false otherwise.
		 */
		private boolean containsKey(String sKey){
			if(mapCol.containsKey(sKey)){
				return true;
			}else{
				return false;
			}
		}
	}
}
