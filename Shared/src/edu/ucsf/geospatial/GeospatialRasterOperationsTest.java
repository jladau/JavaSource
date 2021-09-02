package edu.ucsf.geospatial;

import org.joda.time.LocalDate;
import org.junit.Test;
import static edu.ucsf.geospatial.GeospatialRaster.*;
import com.google.common.collect.Range;

import static org.junit.Assert.*;


/**
 * Contains utilities for summarizing geosptial rasters
 * @author jladau
 */

public class GeospatialRasterOperationsTest {

	private GeospatialRaster ras2;
	
	public GeospatialRasterOperationsTest(){
		initialize();
	}
	
	private void initialize(){
		
		LocalDate rgt1[];
		
		ras2 = new GeospatialRaster(0.1,1.0,Range.closed(10., 20.),Range.closed(-100., -80.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", "2016-07-06", "Var4", "Units4", "Variable 4", "Units4"));
		rgt1 = new LocalDate[]{new LocalDate(2010,5,15), new LocalDate(2011,7,12)};
		for(LocalDate tim1:rgt1){
			try {
				ras2.addTime(tim1, tim1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			ras2.addNullVert();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(double dLat=10.05;dLat<20.;dLat+=0.1){
			for(double dLon=-99.5;dLon<-80;dLon+=1.0){
				for(int i=0;i<rgt1.length;i++){
					try {
						ras2.put(dLat, dLon, rgt1[i], NULL_VERT, dLat*dLon*(i+1));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Test
	public void calculateAreaAboveThreshold_AreaCalculated_AreaCorrect(){
		assertEquals(1217.459,GeospatialRasterOperations.calculateAreaAboveThreshold(ras2, new LocalDate(2011,7,12), NULL_VERT, -1618.06),0.1);
	}
	
	@Test
	public void calculateNonErrorArea_AreaCalculated_AreaCorrect(){
		assertEquals(2385571.049,GeospatialRasterOperations.calculateNonErrorArea(ras2, new LocalDate(2011,7,12), NULL_VERT),0.1);
	}
	
}
