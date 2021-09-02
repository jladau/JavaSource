package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;

public class EmpiricalDistribution_CumulativeTest{

	public EmpiricalDistribution_CumulativeTest(){
	}
	
	@Test
	public void cumulativeProbability_ProbabilityGotten_ProbabilityCorrect(){
		
		//emp1 = empirical distribution object
		
		EmpiricalDistribution_Cumulative emp1;
		
		emp1 = new EmpiricalDistribution_Cumulative(new double[]{1,2,3,4});
		assertEquals(0,emp1.cumulativeProbability(0.5),0.000001);
		assertEquals(0.25,emp1.cumulativeProbability(1.5),0.000001);
		assertEquals(0.5,emp1.cumulativeProbability(2.5),0.000001);
		assertEquals(0.75,emp1.cumulativeProbability(3.5),0.000001);
		assertEquals(1,emp1.cumulativeProbability(4.5),0.000001);
		assertEquals(0.25,emp1.cumulativeProbability(1),0.000001);
		assertEquals(0.5,emp1.cumulativeProbability(2),0.000001);
	}	
}
