package edu.ucsf.base;

import java.util.Set;
import static org.junit.Assert.*;

import org.joda.time.LocalDate;
import org.junit.Test;

/**
 * Implementation of a sparse table with three axes.
 * @author jladau
 */

public class HashBasedTable3DTest{

	//tbl1 = table
	
	HashBasedTable3D<LocalDate, Double, Integer> tbl1;
	
	
	public HashBasedTable3DTest(){
		initialize();
	}

	private void initialize(){
		
		tbl1 = new HashBasedTable3D<LocalDate, Double, Integer>();
		
		tbl1.put(new LocalDate(2010,9,15), 12.5, 89, 0.);
		tbl1.put(new LocalDate(2010,9,15), 13.5, 89, 1.);
		tbl1.put(new LocalDate(2010,9,15), 12.5, 97, 2.);
		tbl1.put(new LocalDate(2010,9,15), 13.5, 97, 3.);
		
		tbl1.put(new LocalDate(2010,10,15), 12.5, 89, 4.);
		tbl1.put(new LocalDate(2010,10,15), 13.5, 89, 5.);
		tbl1.put(new LocalDate(2010,10,15), 12.5, 97, 6.);
		tbl1.put(new LocalDate(2010,10,15), 13.5, 97, 7.);
		
		tbl1.put(new LocalDate(2010,12,15), 12.5, 89, 8.);
		tbl1.put(new LocalDate(2010,12,15), 13.5, 89, 9.);
		tbl1.put(new LocalDate(2010,12,15), 12.5, 97, 10.);
		tbl1.put(new LocalDate(2010,12,15), 13.5, 97, 11.);
	}
	
	@Test
	public void remove_ValueRemoved_ValueGone(){
		
		assertTrue(tbl1.contains(new LocalDate(2010,9,15), 13.5, 89));
		tbl1.remove(new LocalDate(2010,9,15), 13.5, 89);
		assertFalse(tbl1.contains(new LocalDate(2010,9,15), 13.5, 89));
		assertEquals(11,tbl1.size(),0.00000001);
		tbl1.remove(new LocalDate(2010,9,15), 12.5, 89);
		tbl1.remove(new LocalDate(2010,10,15), 12.5, 89);
		tbl1.remove(new LocalDate(2010,10,15), 13.5, 89);
		tbl1.remove(new LocalDate(2010,12,15), 12.5, 89);
		assertTrue(tbl1.key3Set().contains(89));
		tbl1.remove(new LocalDate(2010,12,15), 13.5, 89);
		assertFalse(tbl1.key3Set().contains(89));
		assertEquals(6,tbl1.size(),0.00000001);
		initialize();
	}
	
	@Test
	public void put_ValueAdded_ValueIncluded(){
		tbl1.put(new LocalDate(2010,7,15), 12.5, 89, 0.);
		assertTrue(tbl1.contains(new LocalDate(2010,7,15), 12.5, 89));
		initialize();
	}
	
	@Test
	public void get_ValueObtained_ValueObtainedCorrectly(){
		assertEquals(tbl1.get(new LocalDate(2010,12,15), 12.5, 89),8.,0.00000001);
		assertTrue(Double.isNaN(tbl1.get(new LocalDate(2010,12,16), 12.5, 89)));
	}

	@Test
	public void contains_Checked_CorrectResult(){
		assertTrue(tbl1.contains(new LocalDate(2010,12,15), 12.5, 89));
		assertFalse(tbl1.contains(new LocalDate(2010,12,16), 12.5, 89));
		assertFalse(tbl1.contains(new LocalDate(2010,12,15), 17.5, 89));
		assertFalse(tbl1.contains(new LocalDate(2010,12,15), 12.5, 899));
	}
	
	public void size_SizeFound_SizeCorrect(){
		assertEquals(12,tbl1.size());
	}
	
	@Test
	public void key1Set_SetObtained_SetCorrect(){
		
		//set1 = set being checked
		
		Set<LocalDate> set1;
		
		set1 = tbl1.key1Set();
		assertEquals(set1.size(),3);
		assertTrue(set1.contains(new LocalDate(2010,12,15)));
		assertTrue(set1.contains(new LocalDate(2010,10,15)));
		assertTrue(set1.contains(new LocalDate(2010,9,15)));
	}

	@Test
	public void key2Set_SetObtained_SetCorrect(){
		
		//set1 = set being checked
		
		Set<Double> set1;
		
		set1 = tbl1.key2Set();
		assertEquals(set1.size(),2);
		assertTrue(set1.contains(12.5));
		assertTrue(set1.contains(13.5));
	}
	
	@Test
	public void key3Set_SetObtained_SetCorrect(){
		
		//set1 = set being checked
		
		Set<Integer> set1;
		
		set1 = tbl1.key3Set();
		assertEquals(set1.size(),2);
		assertTrue(set1.contains(89));
		assertTrue(set1.contains(97));
	}

}
