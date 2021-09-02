package edu.ucsf.geospatial;

import java.util.ArrayList;

import org.junit.Test;

import com.google.common.collect.Range;

import edu.ucsf.geospatial.MaskGeospatialRaster;
import static edu.ucsf.geospatial.GeospatialRaster.*;
import static java.lang.Math.PI;
import static org.junit.Assert.*;


public class MaskGeospatialRasterTest {

	private GeospatialRaster ras5;
	private SphericalMultiPolygon plyMask;
	private double rgdCenter1[];
	private double rgdCenter2[];
	private EarthGeometry ert1;
	

	public MaskGeospatialRasterTest(){
		initialize();	
	}

	private void initialize(){
		
		//lst1 = list of vertices
		//rgd1 = current latitude and longtude
		
		ArrayList<Double[]> lst1;
		double rgd2[];
		
		rgdCenter1 = new double[]{20.,-100.};
		rgdCenter2 = new double[]{89,150.};
		ert1 = new EarthGeometry();
		
		lst1 = new ArrayList<Double[]>();
		for(double dTheta=0.1;dTheta<2*PI+0.1;dTheta+=0.01){
			rgd2 = ert1.findDestinationWGS84(rgdCenter1[0], rgdCenter1[1], dTheta*EarthGeometry.RAD_TO_DEG, 1000.);
			lst1.add(new Double[]{1.,rgd2[0],rgd2[1]});
		}
		for(double dTheta=0.1;dTheta<2*PI+0.1;dTheta+=0.01){
			rgd2 = ert1.findDestinationWGS84(rgdCenter2[0], rgdCenter2[1], dTheta*EarthGeometry.RAD_TO_DEG, 1000.);
			lst1.add(new Double[]{2.,rgd2[0],rgd2[1]});
		}
		plyMask = new SphericalMultiPolygon(lst1,1234,false);
		
		ras5 = new GeospatialRaster(1.0,1.0,Range.closed(-80., 80.),Range.closed(-170., 170.),new GeospatialRasterMetadata("Test raster","UCSF","Java source code", "Unit test", "2016-07-06", "Var5", "Units5", "Variable 5", "Units5"));
		try{
			ras5.addNullVert();
			ras5.addNullTime();
		}catch(Exception e1) {
			e1.printStackTrace();
		}
		for(double dLat=-79.5;dLat<80;dLat++){
			for(double dLon=-169.5;dLon<170.;dLon++){
				try{
					ras5.put(dLat, dLon, NULL_TIME,NULL_VERT, dLat+dLon);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	
	@Test
	public void applyMask_MaskApplied_ApplicationCorrect(){
		
		//rgd1 = first array of offsets
		//rgd2 = second array of offsets
		//b1 = flag for whether point should be NaN
		
		double rgd1[];
		double rgd2[];
		boolean b1;
		
		rgd1 = new double[]{0,0.5,-0.5};
		rgd2 = new double[]{0,0.5,-0.5};
		try{
			MaskGeospatialRaster.applyMask(plyMask, ras5, NULL_TIME, NULL_VERT);
		}catch(Exception e){
			e.printStackTrace();
		}
		for(double dLat=-79.5;dLat<80;dLat++){
			for(double dLon=-169.5;dLon<170.;dLon++){
				b1 = false;
				for(double d1:rgd1){
					for(double d2:rgd2){
						if(ert1.orthodromicDistanceWGS84(dLat + d1, dLon + d2, rgdCenter1[0], rgdCenter1[1])<1000 || ert1.orthodromicDistanceWGS84(dLat + d1, dLon + d2, rgdCenter2[0], rgdCenter2[1])<1000){
							assertEquals(dLat+dLon,ras5.get(dLat,dLon,NULL_TIME,NULL_VERT),0.00000001);
							b1 = true;
						}
					}
				}
				if(b1==false){
					assertEquals(Double.NaN,ras5.get(dLat,dLon,NULL_TIME,NULL_VERT),0.00000001);
				}
			}
		}
	}
}