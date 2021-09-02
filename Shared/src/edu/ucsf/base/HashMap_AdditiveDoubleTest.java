package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * This class adds functions to hashmap
 * @author jladau
 */

public class HashMap_AdditiveDoubleTest{

	/**Map for testing**/
	private HashMap_AdditiveDouble<String> map1;
	
	public HashMap_AdditiveDoubleTest(){
		initialize();
	}
	
	private void initialize(){
		map1 = new HashMap_AdditiveDouble<String>();
		map1.put("s1", 2.2);
	}
	
	@Test
	public void putSum_SumPut_CorrectlyAdded(){
		map1.putSum("s1", 0.4);
		assertEquals(2.6,map1.get("s1"),0.00000001);
		map1.putSum("s2", 11.9);
		assertEquals(11.9,map1.get("s2"),0.00000001);
		initialize();
	}
}
