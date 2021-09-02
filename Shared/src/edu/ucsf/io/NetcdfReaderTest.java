package edu.ucsf.io;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.joda.time.LocalDate;
import org.junit.Test;

import edu.ucsf.geospatial.GeospatialRaster;

public class NetcdfReaderTest{
	
	/**Reader object**/
	private NetcdfReader ncf1;
	
	/**Reader object**/
	private NetcdfReader ncf2;
	
	/**Reader object**/
	private NetcdfReader ncf3;
	
	public NetcdfReaderTest(){
		initialize();
	}
	
	private void initialize(){
		ncf1 = new NetcdfReader("/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/sstMomeanWOA.nc");
		ncf2 = new NetcdfReader("/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/sstAnmeanBiooracle.nc");		
		ncf3 = new NetcdfReader("/home/jladau/Documents/Research/Data/Rasters/Environmental_Variables/latitudeAnmeanNA.nc");		
	}
	
	@Test
	public void getTimeUnits_TimeUnitsGotten_UnitsCorrect(){
		assertEquals("months",ncf1.getTimeUnits());
		assertTrue(ncf2.getTimeUnits()==null);
	}
	
	@Test
	public void getUnits_UnitsGotten_UnitsCorrect(){
		assertEquals("Degrees_Celsius",ncf1.gmt1.units);
		assertEquals("Degrees_C",ncf2.gmt1.units);
	}

	@Test
	public void getVariableName_NameGotten_NameCorrect(){
		assertEquals("Sea_Surface_Temperature",ncf1.gmt1.variable);
		assertEquals("Sea_surface_temperature_mean",ncf2.gmt1.variable);
	}
	
	@Test
	public void loadGrid_GridLoaded_GridCorrect(){
		
		//tim1 = time to use
		//dVert = vert to use
		
		LocalDate tim1;
		double dVert;
		
		tim1 = new LocalDate(9999,7,15);
		dVert = 50.;
		try {
			ncf1.loadGrid(tim1, dVert);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		assertEquals(-0.959563,ncf1.get(69.75,-177.25, tim1, dVert),0.000001);
		assertFalse(Double.isNaN(ncf1.get(-69.75,-177.25, tim1, dVert)));
		assertEquals(3.838187,ncf1.get(59.25,-52.25, tim1, dVert),0.000001);
		assertTrue(Double.isNaN(ncf1.get(67.25,-52.25, tim1, dVert)));
		ncf1.close();
		
		tim1 = GeospatialRaster.NULL_TIME;
		dVert = GeospatialRaster.NULL_VERT;
		try {
			ncf2.loadGrid(tim1, dVert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertTrue(Double.isNaN(ncf2.get(81.25, -175.75, tim1, dVert)));
		assertEquals(-0.759875,ncf2.get(80.75,-175.75, tim1, dVert),0.000001);
		assertEquals(10.810400,ncf2.get(49.25,-124.75, tim1, dVert),0.000001);
		assertFalse(Double.isNaN(ncf2.get(-69.75,-177.25, tim1, dVert)));
		ncf2.close();
		
		initialize();
	}
	
	@Test
	public void get_ValuesFromBIOMFileGotten_ValuesCorrect(){
		
		//bio1 = biom test file
		//map1 = returns value at specified sample
		//map2 = sample dates
		
		BiomIO bio1;
		HashMap<String,Double> map1=null;
		HashMap<String,LocalDate> map2;
		
		bio1=new BiomIO("/home/jladau/Documents/Research/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom");
		
		map2 = new HashMap<String,LocalDate>();
		for(int i=1;i<17;i++){
			map2.put("sample" + i, new LocalDate(9999,01,01));
		}
		
		try {
			map1 = ncf1.get(bio1, map2, 50.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(Double.isNaN(map1.get("sample1")));
		assertTrue(Double.isNaN(map1.get("sample2")));
		assertTrue(Double.isNaN(map1.get("sample3")));
		
		try {
			map1 = ncf3.get(bio1, map2, 50.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(45.25,map1.get("sample1"),0.0000001);
		assertEquals(45.25,map1.get("sample2"),0.0000001);
		assertEquals(45.25,map1.get("sample3"),0.0000001);
		assertEquals(45.25,map1.get("sample4"),0.0000001);
		assertEquals(46.25,map1.get("sample5"),0.0000001);
		assertEquals(46.25,map1.get("sample6"),0.0000001);
		assertEquals(46.25,map1.get("sample7"),0.0000001);
		assertEquals(46.25,map1.get("sample8"),0.0000001);
		assertEquals(47.25,map1.get("sample9"),0.0000001);
		assertEquals(47.25,map1.get("sample10"),0.0000001);
		assertEquals(47.25,map1.get("sample11"),0.0000001);
		assertEquals(47.25,map1.get("sample12"),0.0000001);
		assertEquals(48.25,map1.get("sample13"),0.0000001);
		assertEquals(48.25,map1.get("sample14"),0.0000001);
		assertEquals(48.25,map1.get("sample15"),0.0000001);
		assertEquals(48.25,map1.get("sample16"),0.0000001);
	}
	
}