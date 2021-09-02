package edu.ucsf.io;
import org.joda.time.LocalDate;
import org.junit.Test;
import com.google.common.collect.Range;
import static edu.ucsf.base.CurrentDate.currentDate;
import static edu.ucsf.geospatial.GeospatialRaster.NULL_TIME;
import static edu.ucsf.geospatial.GeospatialRaster.NULL_VERT;
import static org.junit.Assert.*;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;

/**
 * Writes netcdf using specified rasters
 * @author jladau
 */

public class NetcdfWriterTest {

	/**CDF writer**/
	private NetcdfWriter ncf1;
	
	/**Rasters being written**/
	private GeospatialRaster ras4;
	private GeospatialRaster ras1;
	private GeospatialRaster ras2;
	private GeospatialRaster ras3;
	
	/**Dates and verts**/
	private LocalDate rgt1[][];
	private double rgd1[][];

	public NetcdfWriterTest(){
		initialize();
	}

	private void initialize(){
		
		try{
			
			//initializing times and dates
			rgt1 = new  LocalDate[][]{
					{new LocalDate("2010-6-1"), new LocalDate("2010-6-30")},
					{new LocalDate("2011-7-1"), new LocalDate("2011-7-30")}};
			rgd1 = new double[][]{
					{-10.,-5.},
					{-5.,25.},
					{25.,100.}};
			
			//initializing rasters
			ras1 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Raster1", "Units1", "Variable 1", "Units1"));
			ras1.addNullTime();
			ras1.addNullVert();
			ras2 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Raster2", "Units2", "Variable 2", "Units2"));
			ras2.addNullVert();
			ras3 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Raster3", "Units3", "Variable 3", "Units3"));
			ras3.addNullTime();
			ras4 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", currentDate(), "Raster4", "Units4", "Variable 4", "Units4"));
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
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void write_RastersWritten_RastersCorrect(){
		
		//ncr1 = Netcdf reader
		
		NetcdfReader ncr1;
		
		try {
			ncf1 = new NetcdfWriter(new GeospatialRaster[]{ras1,ras2,ras3,ras4}, "/home/jladau/Desktop/temp.nc");
			ncf1.writeRaster(ras1,NULL_TIME,NULL_VERT);
			for(LocalDate tim1:ras2.axsTime.keySet()){
				ncf1.writeRaster(ras2, tim1, NULL_VERT);
			}
			for(Double dVert:ras3.axsVert.keySet()){
				ncf1.writeRaster(ras3,NULL_TIME,dVert);
			}
			for(Double dVert:ras4.axsVert.keySet()){
				for(LocalDate tim1:ras4.axsTime.keySet()){
					ncf1.writeRaster(ras4, tim1, dVert);
				}
			}
			ncf1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ncr1 = new NetcdfReader("/home/jladau/Desktop/temp.nc","Raster2");
		try {
			ncr1.loadGridAllVertsTimes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertTrue(ncr1.equals(ras2));
		ncr1.close();
		
		ncr1 = new NetcdfReader("/home/jladau/Desktop/temp.nc","Raster1");
		try {
			ncr1.loadGridAllVertsTimes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(ncr1.equals(ras1));
		ncr1.close();
		
		ncr1 = new NetcdfReader("/home/jladau/Desktop/temp.nc","Raster3");
		try {
			ncr1.loadGridAllVertsTimes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(ncr1.equals(ras3));
		ncr1.close();
		
		ncr1 = new NetcdfReader("/home/jladau/Desktop/temp.nc","Raster4");
		try {
			ncr1.loadGridAllVertsTimes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(ncr1.equals(ras4));
		ncr1.close();
	}
}
