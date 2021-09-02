package edu.ucsf.io;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf
.io.ShapefileIO;

public class ShapefileIOTest {

	/**Shapefile object**/
	private ShapefileIO shp1;
	
	@Test
	public void hasNext_Tested_CorrectResult(){
		
		try {
			shp1 = new ShapefileIO("/home/jladau/Desktop/Data/Continent_Maps/continent.shp","CONTINENT");
			while(shp1.hasNext()){
				shp1.next();
				if(shp1.hasNext()){
					assert(!shp1.getID().equals("Europe"));
				}else{
					assert(!shp1.getID().equals("Europe"));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void next_ShapefileRead_AllFeaturesRead(){
		
		//rgs1 = correct set of features
		//i1 = counter
		
		String rgs1[];
		int i1;
		
		rgs1 = new String[]{"Africa","Asia","Australia","North America","Oceania","South America","Antarctica","Europe"};
		try {
			shp1 = new ShapefileIO("/home/jladau/Desktop/Data/Continent_Maps/continent.shp","CONTINENT");
			i1=0;
			while(shp1.hasNext()){
				shp1.next();
				i1++;
			}
			assertEquals(i1,rgs1.length);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void getID_ShapefileRead_CorrectIDsReturned(){
		
		//rgs1 = correct set of features
		//i1 = counter
		
		String rgs1[];
		int i1;
		
		rgs1 = new String[]{"Africa","Asia","Australia","North America","Oceania","South America","Antarctica","Europe"};
		try {
			shp1 = new ShapefileIO("/home/jladau/Desktop/Data/Continent_Maps/continent.shp","CONTINENT");
			i1=0;
			while(shp1.hasNext()){
				shp1.next();
				assertEquals(rgs1[i1],shp1.getID());
				i1++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	@Test
	public void loadFeature_FeatureLoaded_CorrectFeatureLoaded(){
		
		//rgs1 = new String[]{"Africa","Asia","Australia","North America","Oceania","South America","Antarctica","Europe"};
		try {
			shp1 = new ShapefileIO("/home/jladau/Desktop/Data/Continent_Maps/continent.shp","CONTINENT");
			
			assertEquals(0,shp1.loadFeature("Asia"));
			assertEquals("Asia",shp1.getID());
			assertEquals(0,shp1.loadFeature("South America"));
			assertEquals("South America",shp1.getID());
			assertEquals(1,shp1.loadFeature("Africa"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void getPolygon_PolygonObtained_PolygonCorrect(){
		
		//rgdTest = test points, row 0 with latitude, row 1 with longitude
		//i1 = counter
		//ply1 = current polygon
		//lst1 = list of point in polygon
		
		double rgdTest[][];
		int i1;
		SphericalMultiPolygon ply1;
		ArrayList<String> lst1 = null;
		
		rgdTest = new double[][]{
				{0.,40.,-20.,40.,-45.,-30.,-80.,50.},
				{30.,100.,130.,-110.,170.,-60.,0.,5.}};
		try {
			shp1 = new ShapefileIO("/home/jladau/Desktop/Data/Continent_Maps/continent.shp","CONTINENT");
			i1=0;
			while(shp1.hasNext()){
				shp1.next();
				ply1 = shp1.getPolygon();
				for(int i=0;i<rgdTest[0].length;i++){
					if(i==i1){
						assertTrue(ply1.contains(rgdTest[0][i], rgdTest[1][i]));
					}else{
						assertFalse(ply1.contains(rgdTest[0][i], rgdTest[1][i]));
					}
				}
				i1++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//also checking map of ocean
		try {
			shp1 = new ShapefileIO("/home/jladau/Documents/Research/Data/GIS_Baseline_Shapefiles/ne_50m_ocean/ne_50m_ocean.shp","featurecla");
			shp1.loadFeature("Ocean");
			ply1 = shp1.getPolygon();
			lst1 = new ArrayList<String>(44000);
			for(double dLat=90.;dLat>-90;dLat--){
				for(double dLon=-180; dLon<180;dLon++){
					if(ply1.contains(dLat, dLon)){
						lst1.add(dLon + "," + dLat + "," + 1);
					}
				}
			}
			DataIO.writeToFile(lst1, "/home/jladau/Output/ShapefileIOTest.csv");
		}catch(Exception e){
			e.printStackTrace();
		}
		assertEquals(43405,lst1.size());
		
	}
}

