package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.Test;

import edu.ucsf.base.ExtendedCollections;

/**
 * Extended functions for collections.
 * @author jladau
 */

public class ExtendedCollectionsTest {

	private Integer rgi1[];
	private String rgs1[];
	private String rgs2[];
	private Double rgd1[];
	private ArrayList<String> lstString;
	private ArrayList<String> lstString2;
	private ArrayList<Double> lstDouble;
	private ArrayList<Integer> lstInteger;
	private HashSet<String> setString;
	private HashSet<String> setString2;
	private HashSet<Double> setDouble;
	private HashSet<Integer> setInteger;
	
	public ExtendedCollectionsTest(){
		rgi1 = new Integer[]{1,2,4,5};
		rgs1 = new String[]{"a","b","c","d"};
		rgs2 = new String[]{"a","b","e","d"};
		rgd1 = new Double[]{1.1,2.1,4.4,5.9};
		lstString = new ArrayList<String>();
		lstString2 = new ArrayList<String>();
		lstDouble = new ArrayList<Double>();
		lstInteger = new ArrayList<Integer>();
		setString = new HashSet<String>();
		setString2 = new HashSet<String>();
		setDouble = new HashSet<Double>();
		setInteger = new HashSet<Integer>();
		for(String s:rgs1){
			lstString.add(s);
			setString.add(s);
		}
		for(String s:rgs2){
			lstString2.add(s);
			setString2.add(s);
		}
		for(double d:rgd1){
			lstDouble.add(d);
			setDouble.add(d);
		}
		for(int i:rgi1){
			lstInteger.add(i);
			setInteger.add(i);
		}
	}
	
	@Test
	public void equivalent_CollectionsAreEqual_ReturnsTrue(){
		assertTrue(ExtendedCollections.equivalent(lstString, setString));
		assertTrue(ExtendedCollections.equivalent(rgs1, lstString));
		assertTrue(ExtendedCollections.equivalent(rgs1, setString));
		assertTrue(ExtendedCollections.equivalent(lstInteger, setInteger));
		assertTrue(ExtendedCollections.equivalent(rgi1, lstInteger));
		assertTrue(ExtendedCollections.equivalent(rgi1, setInteger));
		assertTrue(ExtendedCollections.equivalent(lstDouble, setDouble));
		assertTrue(ExtendedCollections.equivalent(rgd1, lstDouble));
		assertTrue(ExtendedCollections.equivalent(rgd1, setDouble));
	}
	
	@Test
	public void equivalent_CollectionsAreNotEqual_ReturnsFalse(){
		assertFalse(ExtendedCollections.equivalent(lstString, setString2));
		assertFalse(ExtendedCollections.equivalent(rgs1, lstString2));
		assertFalse(ExtendedCollections.equivalent(rgs1, setString2));
		assertFalse(ExtendedCollections.equivalent(lstString2, setString));
		assertFalse(ExtendedCollections.equivalent(rgs2, lstString));
		assertFalse(ExtendedCollections.equivalent(rgs2, setString));
	}
	
	
}
