package edu.ucsf.base;

import static org.junit.Assert.*;
import java.util.HashSet;
import org.junit.Test;

/**
 * This class allows for adding arbitrary properties to an object
 * @author jladau
 */

public class OrderedPairTest {

	public OrderedPairTest(){			
	}
	
	@Test
	public void compared_IsRun_ComparisonIsCorrect(){
		
		//set1 = set of string pairs
		
		HashSet<OrderedPair<String>> set1;
		
		set1 = new HashSet<OrderedPair<String>>();
		set1.add(new OrderedPair<String>("a","b"));
		set1.add(new OrderedPair<String>("c","d"));
		set1.add(new OrderedPair<String>("e","f"));
		
		assertTrue(set1.contains(new OrderedPair<String>("a","b")));
		assertTrue(set1.contains(new OrderedPair<String>("e","f")));
		assertFalse(set1.contains(new OrderedPair<String>("b","a")));
		assertFalse(set1.contains(new OrderedPair<String>("c","a")));
		assertFalse(set1.contains(new OrderedPair<String>("a","c")));
		assertFalse(set1.contains(new OrderedPair<String>("a","a")));
	}
}