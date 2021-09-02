package edu.ucsf.base;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;

public class ClimatologyTest {
	
	/**climatologies to be sorted**/
	Climatology rgc1[];
	
	public ClimatologyTest(){
		initialize();
	}
	
	private void initialize(){
		rgc1 = new Climatology[3];
		rgc1[0] = new Climatology("1995-01-01", "1990-01-01", "1999-12-31");
		rgc1[1] = new Climatology("1985-01-01", "1980-01-01", "1989-12-31");
		rgc1[2] = new Climatology("1975-01-01", "1970-01-01", "1979-12-31");
	}
	
	@Test
	public void compareTo_Compared_CorrectlyCompared(){
		Arrays.sort(rgc1);
		assertEquals(rgc1[0],new Climatology("1975-01-01", "1970-01-01", "1979-12-31"));
		assertEquals(rgc1[1],new Climatology("1985-01-01", "1980-01-01", "1989-12-31"));
		assertEquals(rgc1[2],new Climatology("1995-01-01", "1990-01-01", "1999-12-31"));
		initialize();
	}
	
	@Test
	public void isBefore_Compared_CorrectlyCompared(){
		assertFalse(rgc1[0].isBefore(new Climatology("1975-01-01", "1970-01-01", "1979-12-31")));
		assertTrue(rgc1[0].isBefore(new Climatology("2010-01-01", "1970-01-01", "1979-12-31")));
	}
	
	@Test
	public void isAfter_Compared_CorrectlyCompared(){
		assertTrue(rgc1[0].isAfter(new Climatology("1975-01-01", "1970-01-01", "1979-12-31")));
		assertFalse(rgc1[0].isAfter(new Climatology("2010-01-01", "1970-01-01", "1979-12-31")));
	}
}
