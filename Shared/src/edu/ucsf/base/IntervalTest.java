package edu.ucsf.base;

import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for interval
 */
public class IntervalTest {
	
	/**List of test intervals.**/
	private ArrayList<Interval> lst1;
	
	public IntervalTest(){
		initialize();
	}
	
	private void initialize(){
		
		lst1 = new ArrayList<Interval>();
		
		lst1.add(new Interval(Double.NEGATIVE_INFINITY,-1.));;
		lst1.add(new Interval(-0.5,0.5));
		lst1.add(new Interval(1.,2.));
		lst1.add(new Interval(1.5,Double.POSITIVE_INFINITY));
		
		lst1.add(new Interval("-Infinity,-1"));
		lst1.add(new Interval("-0.5,0.5"));
		lst1.add(new Interval("1.5,Infinity"));
		
		lst1.add(new Interval(-0.5,Double.POSITIVE_INFINITY));
	}
	
	@Test
	public void contains_IntervalsTested_TestsCorrect(){
		assertTrue(lst1.get(7).contains(lst1.get(1)));
		assertFalse(lst1.get(0).contains(lst1.get(1)));
	}
	
	@Test
	public void contains_PointsTested_TestsCorrect(){
		assertTrue(lst1.get(0).contains(-2.));
		assertTrue(lst1.get(0).contains(-1.));
		assertTrue(lst1.get(1).contains(0.));
		assertTrue(lst1.get(3).contains(2.));
		assertFalse(lst1.get(0).contains(0.));
		assertFalse(lst1.get(1).contains(-2.));
		assertFalse(lst1.get(1).contains(2.));
		assertFalse(lst1.get(3).contains(0.));
	}

	@Test
	public void findSmallestSpanningInterval_IntervalsFound_IntervalsCorrect(){
		
		//int1 = output interval
	
		Interval int1;
		
		for(int i=1;i<4;i++){
			for(int j=0;j<i;j++){
				int1 = lst1.get(i).findSmallestSpanningInterval(lst1.get(j));
				assertEquals(new Interval(Math.min(lst1.get(i).dMin, lst1.get(j).dMin),Math.max(lst1.get(i).dMax, lst1.get(j).dMax)),int1);
			}
		}
	}
	
	@Test
	public void findSymmetricDifference_SymmetricDistanceIsFound_IntervalIsCorrect(){
		
		//lst1 = output intervals
		
		ArrayList<Interval> lst2;
		
		lst2 = lst1.get(0).findSymmetricDifference(lst1.get(1));
		assertEquals(2,lst2.size());
		lst2 = lst1.get(1).findSymmetricDifference(lst1.get(2));
		assertEquals(2,lst2.size());
		lst2 = lst1.get(2).findSymmetricDifference(lst1.get(3));
		assertEquals(new Interval(1.,1.5),lst2.get(0));
		assertEquals(new Interval(2.,Double.POSITIVE_INFINITY),lst2.get(1));
		assertEquals(2,lst2.size());
	}

	@Test
	public void isLessThan_Tested_IsCorrect(){
		
		assertTrue(lst1.get(1).isLessThan(1.));
		assertTrue(lst1.get(0).isLessThan(2.));
		assertFalse(lst1.get(1).isLessThan(-10.));
		assertFalse(lst1.get(0).isLessThan(-10.));
	}
	
	@Test
	public void equals_IntervalsAreEquals_ReturnsTrue(){

		assertTrue(lst1.get(0).equals(lst1.get(0)));
		assertTrue(lst1.get(1).equals(lst1.get(1)));
	
		assertTrue(lst1.get(0).equals(lst1.get(4)));
		assertTrue(lst1.get(1).equals(lst1.get(5)));
		assertTrue(lst1.get(3).equals(lst1.get(6)));
	}
}
