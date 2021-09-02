package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Iterator for running process in parallel
 * @author jladau
 */

public class CurrentDateTest{

	public CurrentDateTest(){
	}
	
	@Test
	public void currentDate_IsRun_CorrectDate(){
		assertEquals(CurrentDate.currentDate(),"2016-07-07");
	}
}
