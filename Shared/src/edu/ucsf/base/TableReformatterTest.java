package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.common.collect.HashBasedTable;

public class TableReformatterTest {

	/**Flat table**/
	private HashBasedTable<Integer,String,String> tblFlat;
	
	/**Expanded table**/
	private HashBasedTable<Integer,String,String> tblExpanded;
	
	public TableReformatterTest(){
		initialize();
	}
	
	private void initialize(){
		
		//rgs1 = flat file;
		//iFlatRow = output line
		//iTableRow = output line
		
		String rgs1[][];
		int iFlatRow;
		int iTableRow;
		
		tblFlat = HashBasedTable.create();
		tblExpanded = HashBasedTable.create();
		
		rgs1 = new String[17][5];
		rgs1[0] = new String[]{"A","B","C","D","VAL"};
		iFlatRow = 1;
		iTableRow = 0;
		for(int a=0;a<2;a++){
			for(int b=0;b<2;b++){
				iTableRow++;
				tblExpanded.put(iTableRow, "A", Integer.toString(a));
				tblExpanded.put(iTableRow, "B", Integer.toString(b));
				for(int c=0;c<2;c++){
					for(int d=0;d<2;d++){
						
						tblFlat.put(iFlatRow, "A", Integer.toString(a));
						tblFlat.put(iFlatRow, "B", Integer.toString(b));
						tblFlat.put(iFlatRow, "C", Integer.toString(c));
						tblFlat.put(iFlatRow, "D", Integer.toString(d));
						tblFlat.put(iFlatRow, "VALUE", Integer.toString(a+b+c+d));
						iFlatRow++;
						
						tblExpanded.put(iTableRow, "C=" + c + ";" + "D=" + d, Integer.toString(a+b+c+d));
					}
				}
			}
		}
	}
	
	@Test
	public void convertFlatFileToTable_TableConverted_ConversionCorrect(){
		
		//sValueHeader = header for value column
		//rgsExpandHeaders = categories to expand by
		//tbl1 = expanded table
		
		String sValueHeader;
		String[] rgsExpandHeaders;
		HashBasedTable<Integer,String,String> tbl1;
		
		sValueHeader = "VALUE";
		rgsExpandHeaders = new String[]{"C","D"};
		tbl1 = TableReformatter.convertFlatToPivot(tblFlat, sValueHeader, rgsExpandHeaders);
		
		assertEquals(tbl1.rowKeySet().size(),tblExpanded.rowKeySet().size());
		assertEquals(tbl1.columnKeySet().size(),tblExpanded.columnKeySet().size());
		for(Integer i:tbl1.rowKeySet()){
			for(String s:tbl1.columnKeySet()){
				assertEquals(tbl1.get(i, s),tblExpanded.get(i, s));
			}
		}
	}
}