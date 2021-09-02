package edu.ucsf.base;

import static org.junit.Assert.*;
import java.util.HashSet;
import org.junit.Test;

/**
 * This class allows for adding arbitrary properties to an object
 * @author jladau
 */

public class SemiOrderedPairTest {

	public SemiOrderedPairTest(){			
	}
	
	@Test
	public void compared_IsRun_ComparisonIsCorrect(){
		
		//set1 = set of string pairs
		
		HashSet<SemiOrderedPair<String>> set1;
		
		set1 = new HashSet<SemiOrderedPair<String>>();
		set1.add(new SemiOrderedPair<String>("a","b"));
		set1.add(new SemiOrderedPair<String>("c","d"));
		set1.add(new SemiOrderedPair<String>("e","f"));
		
		assertTrue(set1.contains(new SemiOrderedPair<String>("a","b")));
		assertTrue(set1.contains(new SemiOrderedPair<String>("b","a")));
		assertFalse(set1.contains(new SemiOrderedPair<String>("c","a")));
		assertFalse(set1.contains(new SemiOrderedPair<String>("a","c")));
		assertFalse(set1.contains(new SemiOrderedPair<String>("a","a")));
	}
}