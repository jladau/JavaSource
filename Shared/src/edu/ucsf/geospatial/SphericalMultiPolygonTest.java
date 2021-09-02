package edu.ucsf.geospatial;

import java.util.ArrayList;
import static java.lang.Math.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Class for polygons on Earth's surface.
 * @author jladau
 */
public class SphericalMultiPolygonTest {
	
	/**Polygon.**/
	private SphericalMultiPolygon ply1;
	
	/**Center of first circle.**/
	private double rgdCenter1[];
	
	/**Center of second circle.**/
	private double rgdCenter2[];
	
	/**Earth geometry object.**/
	private EarthGeometry ert1;
	
	public SphericalMultiPolygonTest(){
		initialize();
	}
	
	private void initialize(){
		
		//lst1 = list of vertices
		//rgd1 = current latitude and longitude
		
		ArrayList<Double[]> lst1;
		double rgd1[];
		
		rgdCenter1 = new double[]{20.,-100.};
		rgdCenter2 = new double[]{-60,150.};
		ert1 = new EarthGeometry();
		
		lst1 = new ArrayList<Double[]>();
		for(double dTheta=0.1;dTheta<2*PI+0.1;dTheta+=0.01){
			rgd1 = ert1.findDestinationWGS84(rgdCenter1[0], rgdCenter1[1], dTheta*EarthGeometry.RAD_TO_DEG, 1000.);
			lst1.add(new Double[]{1.,rgd1[0],rgd1[1]});
		}
		rgd1 = ert1.findDestinationWGS84(rgdCenter1[0], rgdCenter1[1], 0.1*EarthGeometry.RAD_TO_DEG, 1000.);
		lst1.add(new Double[]{1.,rgd1[0],rgd1[1]});
		for(double dTheta=0.1;dTheta<2*PI+0.1;dTheta+=0.01){
			rgd1 = ert1.findDestinationWGS84(rgdCenter2[0], rgdCenter2[1], dTheta*EarthGeometry.RAD_TO_DEG, 1000.);
			lst1.add(new Double[]{2.,rgd1[0],rgd1[1]});
		}
		rgd1 = ert1.findDestinationWGS84(rgdCenter2[0], rgdCenter2[1], 0.1*EarthGeometry.RAD_TO_DEG, 1000.);
		lst1.add(new Double[]{2.,rgd1[0],rgd1[1]});
		ply1 = new SphericalMultiPolygon(lst1,1234,false);
	}
	
	private boolean containsTrue(double dLat, double dLon){
		if(ert1.orthodromicDistanceWGS84(dLat, dLon, rgdCenter1[0], rgdCenter1[1])<=1000 || ert1.orthodromicDistanceWGS84(dLat, dLon, rgdCenter2[0], rgdCenter2[1])<=1000){
			return true;
		}else{
			return false;
		}
	}
	
	@Test
	public void contains_VerticesTested_TestsCorrect(){		
		
		for(double dLat = -90.;dLat<90.;dLat+=1){
			for(double dLon = -180.;dLon<180.;dLon+=1){
				if(containsTrue(dLat,dLon)){
					assertTrue(ply1.contains(dLat,dLon));
				}else{
					assertFalse(ply1.contains(dLat,dLon));
				}
			}
		}
	}

	@Test
	public void generateRandomPointsInPolygon_PointsGenerated_PointsWithinPolygon(){
		
		//rgd1 = generated points
		
		double rgd1[][];
		
		rgd1 = ply1.generateRandomPointsInPolygon(10000,1234);
		for(int i=0;i<rgd1.length;i++){
			assertTrue(containsTrue(rgd1[i][0],rgd1[i][1]));
		}
	}

	@Test
	public void print_PolygonPrinted_PolygonPrintedCorrectly(){
		
		//lst1 = printed version of polygon
		//i1 = counter
		//rgs1 = current line in split format
		//rgd1 = current correct line
		
		int i1;
		ArrayList<String> lst1;
		String rgs1[];
		double rgd1[];
		
		lst1 = ply1.print();
		assertEquals("ID,Latitude,Longitude",lst1.get(0));
		i1=1;
		for(double dTheta=0.1;dTheta<2*PI+0.1;dTheta+=0.01){
			rgd1 = ert1.findDestinationWGS84(rgdCenter1[0], rgdCenter1[1], dTheta*EarthGeometry.RAD_TO_DEG, 1000.);
			rgs1 = lst1.get(i1).split(",");
			assertEquals("1.0",rgs1[0]);	
			assertEquals(rgd1[0],Double.parseDouble(rgs1[1]),0.00001);
			assertEquals(rgd1[1],Double.parseDouble(rgs1[2]),0.00001);
			i1++;
		}
		rgd1 = ert1.findDestinationWGS84(rgdCenter1[0], rgdCenter1[1], 0.1*EarthGeometry.RAD_TO_DEG, 1000.);
		rgs1 = lst1.get(i1).split(",");
		assertEquals("1.0",rgs1[0]);	
		assertEquals(rgd1[0],Double.parseDouble(rgs1[1]),0.00001);
		assertEquals(rgd1[1],Double.parseDouble(rgs1[2]),0.00001);
		i1++;
	}
}
