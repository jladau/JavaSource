package edu.ucsf.io;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for DataIO class.
 */
public class DataIOTest {

	/**Data for testing.**/
	private ArrayList<String> lstData;
	
	/**Data for testing in matrix format.**/
	private String[][] rgsData;
	
	/**Directory for testing IO.**/
	private String sIODir;
	
	/**DataIO object.**/
	private DataIO dat1;
	
	/**
	 * Constructor
	 */
	public DataIOTest(){
		
		//rgs1 = data in string format
		
		String rgs1[];
		
		//loading data
		rgs1 = new String[]{
				"header0,header1,header2,header3,header4",
				"1,\"a\",5.5,c,r",
				"2,\"b\",6.54,f,s",
				"3,\"c\",4.444,g,t"};
		lstData = new ArrayList<String>();
		rgsData = new String[4][5];
		for(int i=0;i<rgs1.length;i++){
			lstData.add(rgs1[i]);
			rgsData[i]=rgs1[i].replace("\"","").split(",");
		}
		
		//loading IO directory
		sIODir="/home/jladau/Output";
		
		//writing file
		DataIO.writeToFile(lstData, sIODir +"/DataIOTest.csv");
		
		//initializing object
		dat1 = new DataIO(sIODir +"/DataIOTest.csv");
	}

	private void restore(){
		dat1 = new DataIO(sIODir +"/DataIOTest.csv");
	}
	
	@Test
	public void appendToLastColumn_DataAreAppended_TableIsCorrect(){
		
		//rgs1 = data to append
		
		String rgs1[];
		
		for(int i=0;i<4;i++){
			dat1.appendToLastColumn(i, "test");
			assertEquals("test",dat1.getString(i,5));
		}
		assertEquals(dat1.iCols,-9999);
		for(int i=0;i<4;i++){
			dat1.appendToLastColumn(i, 3.1979);
			assertEquals("3.1979",dat1.getString(i,6));
		}
		for(int i=0;i<4;i++){
			dat1.appendToLastColumn(i, 7);
			assertEquals("7",dat1.getString(i,7));
		}
		rgs1 = "q,q,q,q".split(",");
		dat1.appendToLastColumn(rgs1);
		for(int i=0;i<4;i++){
			assertEquals("q",dat1.getString(i, 8));
		}
		restore();
	}
	
	@Test
	public void getDoubleColumn_ColumnIsGotten_ColumnIsCorrect(){
		
		//lst1 = column
		
		ArrayList<Double> lst1;
		
		lst1 = dat1.getDoubleColumn("header2");
		for(int i=0;i<lst1.size();i++){
			assertEquals(Double.parseDouble(rgsData[i+1][2]),lst1.get(i),0.000000001);
		}
	}
	
	@Test
	public void getString_StringIsGotten_StringIsCorrect(){
		for(int i=1;i<4;i++){
			for(int j=0;j<5;j++){
				assertEquals(rgsData[i][j],dat1.getString(i, j));
				assertEquals(rgsData[i][j],dat1.getString(i, "header" + j));
			}
		}
	}
	
	@Test
	public void getDouble_DoubleIsGotten_DoubleIsCorrect(){
		for(int i=1;i<4;i++){
			assertEquals(Double.parseDouble(rgsData[i][2]),dat1.getDouble(i, 2),0.0000001);
			assertEquals(Double.parseDouble(rgsData[i][2]),dat1.getDouble(i, "header2"),0.0000001);
		}
	}
	
	@Test
	public void getInteger_IntegerIsGotten_IntegerIsCorrect(){
		for(int i=1;i<4;i++){
			assertEquals(Integer.parseInt(rgsData[i][0]),dat1.getInteger(i, 0));
			assertEquals(Integer.parseInt(rgsData[i][0]),dat1.getInteger(i, "header0"));
		}
	}
	
	@Test
	public void setString_StringIsSet_CorrectValueIsSet(){
		dat1.setString(3,4,"teststring");
		assertEquals("teststring",dat1.getString(3,4));
		restore();
	}

	@Test
	public void getWriteableData_DataGotten_DataAreCorrect(){
		for(int i=0;i<dat1.getWriteableData().size();i++){
			assertEquals(lstData.get(i).replace("\"",""),dat1.getWriteableData().get(i));
		}
	}
	
	@Test
	public void writeToFile_FileIsWrittten_WritingIsCorrect(){
		DataIO.writeToFile(lstData, sIODir +"/DataIOTest.csv");
		dat1 = new DataIO(sIODir +"/DataIOTest.csv");
		for(int i=0;i<4;i++){
			for(int j=0;j<5;j++){
				assertEquals(rgsData[i][j],dat1.getString(i,j));
			}
		}
		restore();
	}
	
	@Test
	public void writeToFile_FileIsAppended_WritingIsCorrect(){
		DataIO.writeToFile(lstData, sIODir +"/DataIOTest.csv");
		DataIO.writeToFile(lstData, sIODir +"/DataIOTest.csv",true);
		dat1 = new DataIO(sIODir +"/DataIOTest.csv");
		for(int i=0;i<8;i++){
			for(int j=0;j<5;j++){
				assertEquals(rgsData[i % 4][j],dat1.getString(i,j));
			}
		}
		DataIO.writeToFile(lstData, sIODir +"/DataIOTest.csv");
		restore();
	}
	
	@Test
	public void writeToFileWithCompletionFile_FileIsWritten_CorrectFilesAreOutput(){
		
		//fil1 = completion file
		
		File fil1;
		
		DataIO.writeToFileWithCompletionFile(lstData,sIODir +"/DataIOTest.csv",7);
		fil1 = new File(sIODir +"/DataIOTest.csv_7.complete");
		assertTrue(fil1.exists());
		fil1 = new File(sIODir +"/DataIOTest.csv_7");
		assertTrue(fil1.exists());
		DataIO.writeToFileWithCompletionFile(lstData,sIODir +"/DataIOTest2.csv",true,7);
		fil1 = new File(sIODir +"/DataIOTest2.csv_7.complete");
		assertTrue(fil1.exists());
		fil1 = new File(sIODir +"/DataIOTest2.csv_7");
		assertTrue(fil1.exists());
		restore();
	}
	
	@Test
	public void writeCompletionFile_FileIsWritten_CompletionFileIsWritten(){
		
		//fil1 = completion file
		
		File fil1;
		
		DataIO.writeCompletionFile(sIODir +"/DataIOTest.csv", 7);
		fil1 = new File(sIODir +"/DataIOTest.csv_7.complete");
		assertTrue(fil1.exists());
	}
}
