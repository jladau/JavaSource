package edu.ucsf.SamplingRegionSelector;

import java.util.ArrayList;
import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.SphericalCapEarth_SamplingRegion;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * Selects sampling regions for analysis
 * @author jladau
 */

public class SamplingRegionSelectorLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//lstOut = output
		//geo1 = earth geometry object
		//rgd1 = current center
		//shp1 = merged shapefile reader (for selecting only regions overlapping ranges)
		//cap1 = current cap
		//plyMerged = merged polygon
		//plyLand = land polygon
		
		SphericalMultiPolygon plyMerged;
		SphericalMultiPolygon plyLand;
		ShapefileIO shp1;
		double rgd1[];
		EarthGeometry geo1;
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		SphericalCapEarth_SamplingRegion cap1;
		
		arg1 = new ArgumentIO(rgsArgs);
		
		shp1 = new ShapefileIO(
				arg1.getValueString("sDissolvedShapefilePath"),
				arg1.getValueString("sIDHeader"));
		shp1.next();
		plyMerged = shp1.getPolygon();
		
		if(arg1.containsArgument("sLandShapefilePath")){
			shp1 = new ShapefileIO(
					arg1.getValueString("sLandShapefilePath"),
					arg1.getValueString("sLandIDHeader"));
			shp1.next();
			plyLand= shp1.getPolygon();
		}else{
			plyLand=null;
		}
		
		lstOut = new ArrayList<String>(arg1.getValueInt("iRegions")+1);
		lstOut.add("REGION_ID,RADIUS,LATITUDE_CENTER,LONGITUDE_CENTER,AREA,PERIMETER");
		if(!arg1.containsArgument("iRandomSeed")){	
			geo1 = new EarthGeometry();
		}else{
			geo1 = new EarthGeometry(arg1.getValueInt("iRandomSeed"));
		}
		for(int i=1;i<=arg1.getValueInt("iRegions");i++){
			
			System.out.println("Selecting study region " + i + " of " + arg1.getValueInt("iRegions") + "...");
			
			do{
				rgd1 = geo1.randomPoint();
				cap1 = new SphericalCapEarth_SamplingRegion(
						arg1.getValueDouble("dRadius"),
						rgd1[0],
						rgd1[1],
						Double.NaN,
						1234);
			}while(cap1.intersects180() || !plyMerged.intersects(cap1) || (plyLand!=null && !plyLand.contains(cap1)));
			//}while(!plyLand.contains(cap1));
			
			lstOut.add(
					i + "," +
					arg1.getValueDouble("dRadius") + "," +
					rgd1[0] + "," +
					rgd1[1] + "," +
					cap1.area() + "," +
					cap1.perimeter());
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
