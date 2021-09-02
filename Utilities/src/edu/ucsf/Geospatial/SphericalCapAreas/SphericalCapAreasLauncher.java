package edu.ucsf.Geospatial.SphericalCapAreas;

import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates spherical cap radii given spherical cap areas
 * @author jladau
 */

public class SphericalCapAreasLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sRadiusField = field for area
		//cap1 = current cap
		
		SphericalCapEarth cap1;
		ArgumentIO arg1;
		DataIO dat1;
		String sRadiusField;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sRadiusField = arg1.getValueString("sRadiusField");
		dat1.appendToLastColumn(0, "SPHERICAL_CAP_AREA");
		for(int i=1;i<dat1.iRows;i++){
			cap1 = new SphericalCapEarth(dat1.getDouble(i, sRadiusField), 0., 0., 1234);
			dat1.appendToLastColumn(i, cap1.area());
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
