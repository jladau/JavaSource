package edu.ucsf.io;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Units tests for ArgumentIO object.
 * @author jladau
 *
 */

public class ArgumentIOTest {

	/**Arguments in string array format.**/
	private String[] rgsArgs;
	
	/**Arguments object.**/
	private ArgumentIO arg1;
	
	/**
	 * Constructor
	 */
	public ArgumentIOTest(){
		
		rgsArgs=new String[]{
			"--s1=teststring",
			"--i1=7",
			"--d1=382.4234",
			"--b1=true",
			"--rgd1=2.1,3.4,5.3",
			"--rgi1=3,5,4",
			"--rgs1=a,b,c,d",
			"--lst1=4,5,d",
			"--tim1=2014-08-24T99:99:99",
			"--set1=a,b,4",
			"--rgt1=2013-07-11,1995-11-19"
			};
		arg1 = new ArgumentIO(rgsArgs);
		
	}
	
	@Test
	public void containsArgument_ArgumentPresentOrAbsent_ReturnsTrueOrFalse(){
		assertTrue(arg1.containsArgument("s1"));
		assertFalse(arg1.containsArgument("s2"));
	}
	
	@Test
	public void printArguments_ArgumentsPrinted_PrintingIsCorrect(){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = arg1.printArguments();
		assertEquals(rgsArgs.length,lst1.size());
		for(int i=0;i<rgsArgs.length;i++){
			assertTrue(lst1.contains(rgsArgs[i].replace("--","")));
		}
	}
	
	@Test
	public void getValue_ValueGotten_ValueCorrect(){

		assertArrayEquals(new String[]{"a","b","c","d"},arg1.getValueStringArray("rgs1"));
		
		assertEquals("teststring",arg1.getValueString("s1"));
		
		assertEquals(7,arg1.getValueInt("i1"));
		
		assertEquals(382.4234,arg1.getValueDouble("d1"),0.0000001);
		
		assertEquals(2.1,arg1.getValueDoubleArray("rgd1")[0],0.0000001);
		assertEquals(3.4,arg1.getValueDoubleArray("rgd1")[1],0.0000001);
		assertEquals(5.3,arg1.getValueDoubleArray("rgd1")[2],0.0000001);
		
		assertEquals(3,arg1.getValueIntegerArray("rgi1")[0],0.0000001);
		assertEquals(5,arg1.getValueIntegerArray("rgi1")[1],0.0000001);
		assertEquals(4,arg1.getValueIntegerArray("rgi1")[2],0.0000001);
		
		assertEquals("4",arg1.getValueArrayList("lst1").get(0));
		assertEquals("5",arg1.getValueArrayList("lst1").get(1));
		assertEquals("d",arg1.getValueArrayList("lst1").get(2));
		
		assertEquals(new LocalDate("2013-07-11"),arg1.getValueTimeArray("rgt1")[0]);
		assertEquals(new LocalDate("1995-11-19"),arg1.getValueTimeArray("rgt1")[1]);
		
		assertEquals(new LocalDate(2014,8,24),arg1.getValueTime("tim1"));
		
		assertEquals(3,arg1.getValueHashSet("set1").size());
		assertTrue(arg1.getValueHashSet("set1").contains("a"));
		assertTrue(arg1.getValueHashSet("set1").contains("b"));
		assertTrue(arg1.getValueHashSet("set1").contains("4"));
		
		assertTrue(arg1.getValueBoolean("b1"));
	}
	
	@Test
	public void updateArgument_ValueUpdated_ValueCorrect(){
		
		arg1.updateArgument("b1", false);
		assertFalse(arg1.getValueBoolean("b1"));
		assertEquals("false",arg1.getAllArguments().get("b1"));
		
		arg1.updateArgument("b2", false);
		assertFalse(arg1.getValueBoolean("b2"));
		assertEquals("false",arg1.getAllArguments().get("b2"));
		
		arg1.updateArgument("s1","teststring2");
		assertEquals("teststring2",arg1.getValueString("s1"));
		assertEquals("teststring2",arg1.getAllArguments().get("s1"));
		
		arg1.updateArgument("s2","teststring2");
		assertEquals("teststring2",arg1.getValueString("s2"));
		assertEquals("teststring2",arg1.getAllArguments().get("s2"));
		
		arg1.updateArgument("d1", 3.42343243);
		assertEquals(3.42343243,arg1.getValueDouble("d1"),0.00000001);
		assertEquals("3.42343243",arg1.getAllArguments().get("d1"));
		
		arg1.updateArgument("d2", 3.42343243);
		assertEquals(3.42343243,arg1.getValueDouble("d2"),0.00000001);
		assertEquals("3.42343243",arg1.getAllArguments().get("d2"));
		
		arg1.updateArgument("i1", 99);
		assertEquals(99,arg1.getValueInt("i1"));
		assertEquals("99",arg1.getAllArguments().get("i1"));
		
		arg1.updateArgument("i2", 99);
		assertEquals(99,arg1.getValueInt("i2"));
		assertEquals("99",arg1.getAllArguments().get("i2"));
		
		arg1.updateArgument("rgs1", new String[]{"x","y","z"});
		assertArrayEquals(new String[]{"x","y","z"},arg1.getValueStringArray("rgs1"));
		assertEquals("x,y,z",arg1.getAllArguments().get("rgs1"));
	
		arg1.updateArgument("rgs2", new String[]{"x","y","z"});
		assertArrayEquals(new String[]{"x","y","z"},arg1.getValueStringArray("rgs2"));
		assertEquals("x,y,z",arg1.getAllArguments().get("rgs2"));
		
		arg1 = new ArgumentIO(rgsArgs);
	}
}
