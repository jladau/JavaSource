package edu.ucsf.geospatial;

import static org.junit.Assert.*;
import static edu.ucsf.geospatial.GeospatialRaster.NULL_VERT;
import static edu.ucsf.geospatial.GeospatialRaster.NULL_TIME;
import static edu.ucsf.base.CurrentDate.currentDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.joda.time.LocalDate;
import org.junit.Test;
import com.google.common.collect.Range;
import edu.ucsf.base.ExtendedCollections;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;

public class GeospatialRasterTest{

	//ras1 = raster without temporal data
	//ras2 = raster with temporal data
	//ras3 = raster with vertical data
	//ras4 = raster with temporal and vertical data
	//ras5 = masked raster
	//rgt1 = date array
	//rgd1 = vertical value array
	
	private GeospatialRaster ras1;
	private GeospatialRaster ras2;
	private GeospatialRaster ras3;
	private GeospatialRaster ras4;
	private LocalDate rgt1[][];
	private double rgd1[][];
	
	public GeospatialRasterTest(){
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	private void initialize() throws Exception{
		

		
		
		//initializing times and dates
		rgt1 = new  LocalDate[][]{
				{new LocalDate("2010-6-1"), new LocalDate("2010-6-30")},
				{new LocalDate("2011-7-1"), new LocalDate("2011-7-30")}};
		rgd1 = new double[][]{
				{-10.,-5.},
				{-5.,25.},
				{25.,100.}};
		
		//initializing rasters
		ras1 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Var1", "Units1", "Variable 1", "Units1"));
		ras1.addNullTime();
		ras1.addNullVert();
		ras2 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Var2", "Units2", "Variable 2", "Units2"));
		ras2.addNullVert();
		ras3 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Var3", "Units3", "Variable 3", "Units3"));
		ras3.addNullTime();
		ras4 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Var4", "Units4", "Variable 4", "Units4"));
		for(int i=0;i<rgt1.length;i++){
			ras2.addTime(rgt1[i][0], rgt1[i][1]);
			ras4.addTime(rgt1[i][0], rgt1[i][1]);
		}
		for(int i=0;i<rgd1.length;i++){
			ras3.addVert(rgd1[i][0], rgd1[i][1]);
			ras4.addVert(rgd1[i][0], rgd1[i][1]);
		}
		
		for(double dLat=10.05;dLat<20.;dLat+=0.1){
			for(double dLon=-99.5;dLon<-80;dLon+=1.0){
				ras1.put(dLat, dLon, NULL_TIME, NULL_VERT, dLat*dLon);
				for(int i=0;i<rgt1.length;i++){
					ras2.put(dLat, dLon, rgt1[i][1], NULL_VERT, dLat*dLon*(i+1));
					for(int j=0;j<rgd1.length;j++){
						ras4.put(dLat, dLon, rgt1[i][1], rgd1[j][1]-0.00001, dLat*dLon*(i+1)+rgd1[j][1]);
					}
				}
				for(int j=0;j<rgd1.length;j++){
					ras3.put(dLat, dLon, NULL_TIME, rgd1[j][1]-0.00001, dLat*dLon+rgd1[j][1]);
					
					//*****************************
					//System.out.println(dLat + "," + dLon + "," + (rgd1[j][1]-0.00001) + "," + (dLat*dLon+rgd1[j][1]));
					//*****************************
					
				}
			}
		}
	}
	
	@Test
	public void remove_EntryRemoved_EntryGone(){
		assertTrue(ras2.axsTime.size()==2);
		ras2.remove(new LocalDate("2010-6-15"), NULL_VERT);
		assertTrue(ras2.axsTime.size()==1);
		try {
			initialize();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void clearAll_EverythingCleared_NothingLeft(){
		
		assertEquals(10.05*-99.5,ras1.get(10.05, -99.5, NULL_TIME, NULL_VERT),0.000000001);
		ras1.clearAll();
		assertTrue(Double.isNaN(ras1.get(10.05, -99.5,NULL_TIME,NULL_VERT)));
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void put_ValuePlaced_PlacementCorrect(){
		
		//tim1 = current time
		//dVert = current vert
		
		LocalDate tim1;
		double dVert;
		
		tim1 = new LocalDate("2010-6-15");
		dVert = 11.;
		
		try {
			ras1.put(10.07, -90.25, NULL_TIME, NULL_VERT, 17.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(17.,ras1.get(10.07, -90.25, NULL_TIME, NULL_VERT),0.000001);
		assertEquals(17.,ras1.get(10.01, -90.88, NULL_TIME, NULL_VERT),0.000001);
		assertNotEquals(17.,ras1.get(11.01, -90.88, NULL_TIME, NULL_VERT),0.000001);
		
		try {
			ras2.put(10.07, -90.25, tim1, NULL_VERT, 17.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(17.,ras2.get(10.07, -90.25, tim1, NULL_VERT),0.000001);
		assertEquals(17.,ras2.get(10.01, -90.88, tim1, NULL_VERT),0.000001);
		assertNotEquals(17.,ras2.get(11.01, -90.88, tim1, NULL_VERT),0.000001);
		
		try {
			ras3.put(10.07, -90.25, NULL_TIME, dVert, 17.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(17.,ras3.get(10.07, -90.25, NULL_TIME, dVert),0.000001);
		assertEquals(17.,ras3.get(10.01, -90.88, NULL_TIME, dVert),0.000001);
		assertNotEquals(17.,ras3.get(11.01, -90.88, NULL_TIME, dVert),0.000001);
		assertNotEquals(17.,ras3.get(10.07, -90.25, NULL_TIME, 55.),0.000001);
		
		try {
			ras4.put(10.07, -90.25, tim1, dVert, 17.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(17.,ras4.get(10.07, -90.25, tim1, dVert),0.000001);
		assertEquals(17.,ras4.get(10.01, -90.88, tim1, dVert),0.000001);
		assertNotEquals(17.,ras4.get(11.01, -90.88, tim1, dVert),0.000001);
		assertNotEquals(17.,ras4.get(10.07, -90.25, tim1, 55.),0.000001);
		assertNotEquals(17.,ras4.get(10.07, -90.25, new LocalDate("2012-6-15"), -1.),0.000001);
		assertNotEquals(17.,ras4.get(10.07, -90.25, new LocalDate("2012-5-15"), dVert),0.000001);
		
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	@Test
	public void get_AllValuesGotten_ValuesCorrect(){
		
		//rgd1 = array of values
		//iRow = row
		//iCol = column
		
		double rgd1[][];
		int iRow; int iCol;
		
		rgd1 = ras1.get();
		iRow=0;
		for(double dLat=19.95;dLat>10.;dLat-=0.1){
			iCol=0;
			for(double dLon=-99.5;dLon<-80;dLon+=1.0){
				assertEquals(dLat*dLon,rgd1[iRow][iCol],0.00001);
				iCol++;
			}
			iRow++;
		}
	}
	*/
	
	@Test	
	public void get_ValueGotten_CorrectValueGotten(){
		
		//tim1 = current time
		//dVert = current vert
		
		LocalDate tim1;
		double dVert;
		
		assertEquals(10.05*-99.5,ras1.get(10.05, -99.5, NULL_TIME, NULL_VERT),0.000000001);
		assertEquals(10.05*-99.5,ras1.get(10.01, -99.75, NULL_TIME, NULL_VERT),0.000000001);
		assertEquals(19.95*-80.5,ras1.get(19.95, -80.5, NULL_TIME, NULL_VERT),0.000000001);
		assertEquals(19.95*-80.5,ras1.get(19.97, -80.1, NULL_TIME, NULL_VERT),0.000000001);
		assertEquals(Double.NaN,ras1.get(21, -110., NULL_TIME, NULL_VERT),0.000000001);
		assertEquals(Double.NaN,ras1.get(21, -90., NULL_TIME, NULL_VERT),0.000000001);
		
		tim1 = new LocalDate("2010-6-15");
		assertEquals(10.05*-99.5,ras2.get(10.05, -99.5, tim1, NULL_VERT),0.000000001);
		assertEquals(10.05*-99.5,ras2.get(10.01, -99.75, tim1, NULL_VERT),0.000000001);
		assertEquals(19.95*-80.5,ras2.get(19.95, -80.5, tim1, NULL_VERT),0.000000001);
		assertEquals(19.95*-80.5,ras2.get(19.97, -80.1, tim1, NULL_VERT),0.000000001);
		assertEquals(Double.NaN,ras2.get(21, -110, tim1, NULL_VERT),0.000000001);
		assertEquals(Double.NaN,ras2.get(21, -90., tim1, NULL_VERT),0.000000001);
		tim1 = new LocalDate("2011-07-15");
		assertNotEquals(10.05*-99.5,ras2.get(10.05, -99.5, tim1, NULL_VERT),0.000000001);
		tim1 = new LocalDate("2010-6-15");
		assertEquals(10.05*-99.5,ras2.get(10.05, -99.5, tim1, NULL_VERT),0.000000001);
		tim1 = new LocalDate("2011-6-15");
		assertEquals(Double.NaN,ras2.get(10.05, -99.5, tim1, NULL_VERT),0.000000001);
		
		dVert = -9.;
		assertEquals(10.05*-99.5-5.,ras3.get(10.05, -99.5, NULL_TIME, dVert),0.000000001);
		assertEquals(10.05*-99.5-5.,ras3.get(10.01, -99.75, NULL_TIME, dVert),0.000000001);
		assertEquals(19.95*-80.5-5.,ras3.get(19.95, -80.5, NULL_TIME, dVert),0.000000001);
		assertEquals(19.95*-80.5-5.,ras3.get(19.97, -80.1, NULL_TIME, dVert),0.000000001);
		assertEquals(Double.NaN,ras3.get(21, -110, NULL_TIME, dVert),0.000000001);
		assertEquals(Double.NaN,ras3.get(21, -90., NULL_TIME, dVert),0.000000001);
		dVert = 100.;
		assertNotEquals(10.05*-99.5-10.,ras3.get(10.05, -99.5, NULL_TIME, dVert),0.000000001);
		assertEquals(10.05*-99.5+100.,ras3.get(10.05, -99.5, NULL_TIME, dVert),0.000000001);
		dVert=24.;
		assertEquals(10.05*-99.5+25.,ras3.get(10.05, -99.5, NULL_TIME, dVert),0.000000001);
		dVert=26.;
		assertEquals(10.05*-99.5+100.,ras3.get(10.05, -99.5, NULL_TIME, dVert),0.000000001);
		
		tim1 = new LocalDate(new LocalDate("2011-7-15"));
		dVert=-9.;
		assertEquals(10.05*-99.5*2.-5.,ras4.get(10.05, -99.5, tim1, dVert),0.000000001);
		assertEquals(10.05*-99.5*2.-5.,ras4.get(10.01, -99.75, tim1, dVert),0.000000001);
		assertEquals(19.95*-80.5*2.-5.,ras4.get(19.95, -80.5, tim1, dVert),0.000000001);
		assertEquals(19.95*-80.5*2.-5.,ras4.get(19.97, -80.1, tim1, dVert),0.000000001);
		assertEquals(Double.NaN,ras4.get(21, -110, tim1, dVert),0.000000001);
		assertEquals(Double.NaN,ras4.get(21, -90., tim1, dVert),0.000000001);
		dVert=100.;
		tim1 = new LocalDate(new LocalDate("2010-6-15"));
		assertNotEquals(10.05*-99.5*2.-10.,ras4.get(10.05, -99.5, tim1, dVert),0.000000001);
		assertEquals(10.05*-99.5+100.,ras4.get(10.05, -99.5, tim1, dVert),0.000000001);
	}

	@Test
	public void hasNext_NextChecked_CorrectResult(){
		
		GeospatialRaster.LatLonIterator itr1=null;
		
		try {
			itr1 = ras1.getLatLonIterator(NULL_TIME, NULL_VERT);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for(int i=0;i<10*10*20;i++){
			assertTrue(itr1.hasNext());
			itr1.next();
		}
		assertFalse(itr1.hasNext());
		
		try {
			itr1 = ras2.getLatLonIterator(new LocalDate("2010-6-15"),NULL_VERT);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for(int i=0;i<10*10*20;i++){
			assertTrue(itr1.hasNext());
			itr1.next();
		}
		assertFalse(itr1.hasNext());
		
		try {
			itr1 = ras3.getLatLonIterator(NULL_TIME,50.);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for(int i=0;i<10*10*20*1;i++){
			
			assertTrue(itr1.hasNext());
			itr1.next();
		}
		assertFalse(itr1.hasNext());
		
		try {
			itr1 = ras4.getLatLonIterator(new LocalDate("2010-6-15"), 0.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0;i<10*10*20*1*1;i++){
			assertTrue(itr1.hasNext());
			itr1.next();
		}
		assertFalse(itr1.hasNext());
	}

	@Test
	public void next_NextCellObtained_NextCellCorrect() {
		
		GeospatialRaster.LatLonIterator itr1 = null;
		GeospatialRasterCell cel1;
		
		try {
			itr1 = ras1.getLatLonIterator(NULL_TIME, NULL_VERT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(int i=0;i<10*10*20;i++){
			assertTrue(itr1.hasNext());
			cel1 = itr1.next();
			if(cel1.axeLat.iID==0 && cel1.axeLon.iID==0){
				assertEquals(10.,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-100.,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(cel1.axeTime.ID.equals(NULL_TIME));
			}
			if(cel1.axeLat.iID==99 && cel1.axeLat.iID==19){
				assertEquals(10.,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-81,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(cel1.axeTime.ID.equals(NULL_TIME));
			}
		}
		
		try {
			itr1 = ras2.new LatLonIterator(new LocalDate("2010-6-15"), NULL_VERT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0;i<10*10*20;i++){
			assertTrue(itr1.hasNext());
			cel1 = itr1.next();
			if(cel1.axeLat.iID==0 && cel1.axeLon.iID==0){
				assertEquals(10.,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-100.,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(!cel1.axeTime.ID.equals(NULL_TIME));
			}
			if(cel1.axeLat.iID==99 && cel1.axeLon.iID==19){
				assertEquals(19.9,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-81,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(!cel1.axeTime.ID.equals(NULL_TIME));
			}
		}
		
		try {
			itr1 = ras3.new LatLonIterator(NULL_TIME,50.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0;i<10*10*20;i++){
			assertTrue(itr1.hasNext());
			cel1 = itr1.next();
			if(cel1.axeLat.iID==0 && cel1.axeLon.iID==0){
				assertEquals(10.,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-100.,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(cel1.axeTime.ID.equals(NULL_TIME));
			}
			if(cel1.axeLat.iID==99 && cel1.axeLon.iID==19){
				assertEquals(19.9,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-81,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(cel1.axeTime.ID.equals(NULL_TIME));;
			}
		}
		
		try {
			itr1 = ras4.new LatLonIterator(new LocalDate("2010-6-15"), 0.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0;i<10*10*20;i++){
			assertTrue(itr1.hasNext());
			cel1 = itr1.next();
			if(cel1.axeLat.iID==0 && cel1.axeLon.iID==0){
				assertEquals(10.,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-100.,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(!cel1.axeTime.ID.equals(NULL_TIME));
			}
			if(cel1.axeLat.iID==99 && cel1.axeLon.iID==19){
				assertEquals(19.9,cel1.axeLat.rngAxisValues.lowerEndpoint(),0.0000001);
				assertEquals(-81,cel1.axeLon.rngAxisValues.lowerEndpoint(),0.0000001);
				assertTrue(!cel1.axeTime.ID.equals(NULL_TIME));
			}
		}
		
	}
	
	@Test
	public void getLatitudes_LatitudesGotten_LatitudesCorrect(){
		
		//set1 = set of longitudes
		
		Set<Double> set1;
		
		set1 = new HashSet<Double>();
		for(double dLat=10.0;dLat<20.;dLat+=0.1){
			set1.add(dLat);
		}
		assertTrue(ExtendedCollections.equalsApproximately(set1,ras1.axsLat.keySet(),0.000000001));
	}

	@Test
	public void getLongitudes_LongitudesGotten_LongitudesCorrect(){
		
		//set1 = set of longitudes
		
		Set<Double> set1;
		
		set1 = new HashSet<Double>();
		for(double dLon=-100;dLon<-80;dLon+=1.0){
			set1.add(dLon);
		}
		assertTrue(ExtendedCollections.equalsApproximately(set1,ras1.axsLon.keySet(),0.000000001));
	}
	
	@Test
	public void getVerts_VertsGotten_VertsCorrect(){
		
		//rgd2 = list of verts
		
		Double rgd2[];
		
		rgd2 = ras4.axsVert.keySet().toArray(new Double[ras4.axsVert.size()-1]);
		assertEquals(rgd1.length,rgd2.length);
		Arrays.sort(rgd2);
		for(int i=0;i<rgd1.length;i++){
			assertEquals(rgd1[i][0],rgd2[i],0.00000001);
		}
	}
	
	@Test
	public void getTimes_TimesGotten_TimesCorrect(){
		
		//rgt2 = list of times
		
		LocalDate rgt2[];
		
		rgt2 = ras4.axsTime.keySet().toArray(new LocalDate[ras4.axsTime.size()-1]);
		assertEquals(rgt1.length,rgt2.length);
		Arrays.sort(rgt2);
		for(int i=0;i<rgt1.length;i++){
			assertEquals(rgt1[i][0],rgt2[i]);
		}
	}
	
	@Test
	public void equals_Checked_Correct(){
		assertTrue(ras1.equals(ras1));
		assertFalse(ras1.equals(ras2));
		assertFalse(ras1.equals(ras3));
		assertTrue(ras4.equals(ras4));	
		assertFalse(ras2.equals(ras3));
	}
}
