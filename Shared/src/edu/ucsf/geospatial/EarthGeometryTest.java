package edu.ucsf.geospatial;

import static org.junit.Assert.*;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

import static edu.ucsf.geospatial.EarthGeometry.*;

public class EarthGeometryTest {

	/**List of points to use for testing.**/
	//private double rgd1[][];
	
	/**EarthGeometry object.**/
	private EarthGeometry ert1;
	
	public EarthGeometryTest(){
		//rgd1 = new double[][]{
		//		{45.,70.,-15.,-89.},
		//		{120.,-80.,30.,-179.}};
		ert1 = new EarthGeometry();
	}

	@Test
	public void findAreaCell_AreaFound_AreaCorrect(){
		assertEquals(25297.,EarthGeometry.findAreaCell(47, 100, 1, 3),5);
		assertEquals(6368.010,EarthGeometry.findAreaCell(-59, 170, 1, 1),5);	
		assertEquals(1023.693,EarthGeometry.findAreaCell(-85, 0, 1.9, 0.5),5);
	}
	
	@Test
	public void findDestinationPointWGS84_DestinationsFound_DestinationsCorrect(){
		
		//rgd1 = current predicted destination
		
		double rgd1[];
		
		rgd1 = ert1.findDestinationWGS84(0., 0., 44.56139, 10790.);
		assertEquals(45.08228794,rgd1[0],0.5);
		assertEquals(99.92888556,rgd1[1],0.5);
		
		rgd1 = ert1.findDestinationWGS84(-20., -179., 300., 10000.);
		assertEquals(28.16004135,rgd1[0],1.);
		assertEquals(102.26529889,rgd1[1],1.);
	}
	
	@Test
	public void generateRandomPoints_PointsGenerate_PointsWithinSpecifiedBounds(){
		
		//rgd1 = points
		//env1 = bounds
		
		double rgd1[][];
		Envelope env1;
		
		env1 = new Envelope(-100,-80,20,45);
		rgd1 = generateRandomPoints(env1,1000,1234);
		for(int i=0;i<rgd1.length;i++){
			assertTrue(env1.contains(rgd1[i][1],rgd1[i][0]));
		}
		env1 = new Envelope(-100,-80,0,90);
		rgd1 = generateRandomPoints(env1,1000,1234);
		for(int i=0;i<rgd1.length;i++){
			assertTrue(env1.contains(rgd1[i][1],rgd1[i][0]));
		}
	}
	
	@Test
	public void findLatitudeOfOrthodromeIntersection_LatitudesFound_LatitudesCorrect(){
		assertEquals(90.,findLatitudeOfOrthodromeIntersection(45,-100,50,80,10),0.0001);
		assertEquals(-80.1489,findLatitudeOfOrthodromeIntersection(45,-100,45,100,0),0.0001);
		assertEquals(-80.1489,findLatitudeOfOrthodromeIntersection(45,-110,45,90,-10),0.0001);
		assertEquals(80.1489,findLatitudeOfOrthodromeIntersection(45,-100,45,100,180),0.0001);
		assertEquals(0.,findLatitudeOfOrthodromeIntersection(45,-100,-45,100,180),0.0001);
	}
	
	@Test
	public void checkBearingInRange_BearingChecked_BearingCorrect(){
		assertTrue(checkBearingInRange(3., 280., 10.));
		assertTrue(checkBearingInRange(10., 3., 20.));
		assertTrue(checkBearingInRange(3., 280., 10.));
		assertTrue(checkBearingInRange(10., 3., 20.));
		assertTrue(checkBearingInRange(3., -50., 10.));
		assertTrue(checkBearingInRange(-25., -50., 10.));
		assertTrue(checkBearingInRange(0.,0.,360.));
		assertTrue(checkBearingInRange(360.,0.,360.));
		
		assertFalse(checkBearingInRange(13., 280., 10.));
		assertFalse(checkBearingInRange(-10., 3., 20.));
		assertFalse(checkBearingInRange(15., 280., 10.));
		assertFalse(checkBearingInRange(0., 3., 20.));
		assertFalse(checkBearingInRange(-100., -50., 10.));	
		assertFalse(checkBearingInRange(25., -50., 10.));
	}

	@Test
	public void orthodromicDistanceWGS84_DistanceFound_DistanceCorrect(){
		assertEquals(ert1.orthodromicDistanceWGS84(45, 120, 70, -80),7125,100);
		assertEquals(ert1.orthodromicDistanceWGS84(45, 120, -15, 30),11180,100);
		assertEquals(ert1.orthodromicDistanceWGS84(45, 120, -89, -179),14960,100);
		assertEquals(ert1.orthodromicDistanceWGS84(70, -80, -15, 30),12330,100);
		assertEquals(ert1.orthodromicDistanceWGS84(70, -80, -89, -179),17810,100);
		assertEquals(ert1.orthodromicDistanceWGS84(-15, 30, -89, -179),8437,100);
	}
	
	@Test
	public void orthodromicDistance_DistanceFound_DistanceCorrect(){
		assertEquals(orthodromicDistance(45, 120, 70, -80),7125,10);
		assertEquals(orthodromicDistance(45, 120, -15, 30),11180,10);
		assertEquals(orthodromicDistance(45, 120, -89, -179),14960,10);
		assertEquals(orthodromicDistance(70, -80, -15, 30),12330,10);
		assertEquals(orthodromicDistance(70, -80, -89, -179),17810,10);
		assertEquals(orthodromicDistance(-15, 30, -89, -179),8437,10);
	}
	

	@Test
	public void initialGeodesicBearing_BearingFound_BearingCorrect(){
		assertEquals(initialGeodesicBearing(45, 120, 70, -80),7.473611,0.001);
		assertEquals(initialGeodesicBearing(45, 120, -15, 30),259.2714,0.001);
		assertEquals(initialGeodesicBearing(70, -80, -15, 30),76.26111,0.001);
	}
	
	@Test
	public void initialGeodesicBearingWGS84_BearingFound_BearingCorrect(){
		assertEquals(ert1.initialGeodesicBearingWGS84(45, 120, 70, -80),7.473611,1);
		assertEquals(ert1.initialGeodesicBearingWGS84(45, 120, -15, 30),259.2714,1);
		assertEquals(ert1.initialGeodesicBearingWGS84(70, -80, -15, 30),76.26111,1);
	}
}
