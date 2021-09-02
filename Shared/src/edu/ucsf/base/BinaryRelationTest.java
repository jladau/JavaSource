package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * This class allows for adding arbitrary properties to an object
 * @author jladau
 */

public class BinaryRelationTest {

	private BinaryRelation<String> bin1;
	private BinaryRelation<String> bin2;
	
	
	public BinaryRelationTest(){	
		initialize();
	}
	
	private void initialize(){
		
		bin1 = new BinaryRelation<String>();
		bin1.addOrderedPair("a","b");
		bin1.addOrderedPair("b","c");
		bin1.addOrderedPair("a","c");
		bin1.addOrderedPair("d","b");
		bin1.addOrderedPair("d","c");
		
		bin2 = new BinaryRelation<String>();
		bin2.addOrderedPair("a","b");
		bin2.addOrderedPair("b","c");
		bin2.addOrderedPair("d","b");
		bin2.addOrderedPair("d","c");
		bin2.addOrderedPair("a","a");
		bin2.addOrderedPair("b","b");
		bin2.addOrderedPair("c","c");
		bin2.addOrderedPair("d","d");
		
	}
	
	@Test
	public void isTransitive_IsRun_IsCorrect(){
		assertTrue(bin1.isTransitive());
		assertFalse(bin2.isTransitive());
	}
	
	@Test
	public void isSymmetric_IsRun_IsCorrect(){
		assertFalse(bin1.isSymmetric());
		assertFalse(bin2.isSymmetric());
	}
	
	@Test
	public void isReflexive_IsRun_IsCorrect(){
		assertFalse(bin1.isReflexive());
		assertTrue(bin2.isReflexive());
	}
	
	@Test
	public void isStrictOrdering_IsRun_IsCorrect(){
		assertTrue(bin1.isStrictOrdering());
		assertFalse(bin2.isStrictOrdering());
	}
	
	@Test
	public void isPartialOrdering_IsRun_IsCorrect(){
		assertFalse(bin1.isPartialOrdering());
		assertFalse(bin2.isPartialOrdering());
	}
	
	@Test
	public void findTransitiveReduction_IsRun_IsCorrect() throws Exception{
		
		BinaryRelation<String> bin3;
		
		bin3 = new BinaryRelation<String>();
		bin3.addOrderedPair("a","b");
		bin3.addOrderedPair("b","c");
		bin3.addOrderedPair("d","b");
		
		assertTrue(bin1.findTransitiveReduction().equals(bin3));
	}
	
	
	
}