package edu.ucsf.base;

import org.joda.time.LocalDate;
import static org.junit.Assert.*;
import org.junit.Test;

import edu.ucsf.base.Property;

/**
 * This class allows for adding arbitrary properties to an object
 * @author jladau
 */

public class PropertyTest {

	/**Property object.**/
	private Property pty1;
	
	public PropertyTest(){	
		pty1 = new Property();
		pty1.put("b1", true);
		pty1.put("d1", 4.57);
		pty1.put("tim1", new LocalDate(2014,9,29));
		pty1.put("s1", "teststring");
		pty1.put("d2", 0.7);
		pty1.putSum("d2", 7.0);
	}
	
	@Test
	public void clone_IsRun_ClonesObject(){
		
		Property pty2;
		
		pty2 = pty1.clone();
		assertTrue(pty2.getBoolean("b1"));
		assertEquals(4.57, pty2.getDouble("d1"),0.000000001);
		assertEquals(new LocalDate(2014,9,29), pty2.getTime("tim1"));
		assertEquals("teststring",pty2.getString("s1"));
		assertEquals(7.7, pty2.getDouble("d2"),0.000000001);
	}
	
	@Test
	public void has_IsPresent_ReturnsTrue(){
		
		assertTrue(pty1.has("b1"));
		assertTrue(pty1.has("d1"));
		assertTrue(pty1.has("tim1"));
		assertTrue(pty1.has("s1"));
		assertTrue(pty1.has("d2"));
	}
	
	@Test
	public void has_IsAbsent_ReturnsFalse(){
		
		assertFalse(pty1.has("b2"));
		assertFalse(pty1.has("d3"));
		assertFalse(pty1.has("tim2"));
		assertFalse(pty1.has("s2"));
	}

	@Test
	public void getDouble_ValueIsGotten_ValueIsCorrect(){
		assertEquals(4.57, pty1.getDouble("d1"),0.000000001);
		assertEquals(7.7, pty1.getDouble("d2"),0.000000001);
	}
	
	@Test
	public void getString_ValueIsGotten_ValueIsCorrect(){
		assertEquals("teststring",pty1.getString("s1"));
	}
	
	@Test
	public void getTime_ValueIsGotten_ValueIsCorrect(){
		assertEquals(new LocalDate(2014,9,29), pty1.getTime("tim1"));
	}
	
	@Test
	public void getBoolean_ValueIsGotten_ValueIsCorrect(){
		assertTrue(pty1.getBoolean("b1"));
	}
}