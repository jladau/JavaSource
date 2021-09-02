package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * This class adds functions to hashmap
 * @author jladau
 */

public class HashMap_AdditiveIntegerTest{

	/**Map for testing**/
	private HashMap_AdditiveInteger<String> map1;
	
	public HashMap_AdditiveIntegerTest(){
		initialize();
	}
	
	private void initialize(){
		map1 = new HashMap_AdditiveInteger<String>();
		map1.put("s1", 11);
	}
	
	@Test
	public void putSum_SumPut_CorrectlyAdded(){
		map1.putSum("s1", 3);
		assertEquals(14,map1.get("s1"),0.00000001);
		map1.putSum("s2", 19);
		assertEquals(19,map1.get("s2"),0.00000001);
		initialize();
	}
}
