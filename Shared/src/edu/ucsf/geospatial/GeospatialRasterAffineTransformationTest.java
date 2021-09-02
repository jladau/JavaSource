package edu.ucsf.geospatial;

import org.junit.Test;

import com.google.common.collect.Range;

import static edu.ucsf.geospatial.GeospatialRaster.NULL_VERT;
import static org.junit.Assert.*;

import org.joda.time.LocalDate;


/**
 * Contains utilities for summarizing geosptial rasters
 * @author jladau
 */

public class GeospatialRasterAffineTransformationTest {

	private GeospatialRaster ras2;
	
	public GeospatialRasterAffineTransformationTest(){
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
	public void loadTransformation_TransformationLoaded_TransformationCorrect() throws Exception{
		
		//afn1 = affine transformation
		
		GeospatialRasterAffineTransformation afn1;
		
		afn1 = new GeospatialRasterAffineTransformation(ras2.dLatResolution, ras2.dLonResolution, ras2.getLatRange(), ras2.getLonRange(), ras2.gmt1);
		afn1.addNullVert();
		afn1.addTime(new LocalDate("2016-12-23"),new LocalDate("2016-12-23"));
		afn1.initializeTransformation("constant:1;2010-05-15:3;2011-07-12:-3");
		afn1.loadTransformation(ras2, GeospatialRaster.NULL_VERT, new LocalDate("2016-12-23"));
		assertEquals(afn1.get(10.15, -98.5, new LocalDate("2016-12-23"), GeospatialRaster.NULL_VERT),3000.3250000000003,0.00000001);
		assertEquals(afn1.get(15.15, -88.5, new LocalDate("2016-12-23"), GeospatialRaster.NULL_VERT),4023.3250000000003,0.00000001);
	}
}
