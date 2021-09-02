package edu.ucsf.Geospatial.SphericalCapRadii;

import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates spherical cap radii given spherical cap areas
 * @author jladau
 */

public class SphericalCapRadiiLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sAreaField = field for area
		
		ArgumentIO arg1;
		DataIO dat1;
		String sAreaField;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sAreaField = arg1.getValueString("sAreaField");
		dat1.appendToLastColumn(0, "SPHERICAL_CAP_RADIUS");
		for(int i=1;i<dat1.iRows;i++){
			dat1.appendToLastColumn(i, SphericalCapEarth.radius(dat1.getDouble(i, sAreaField)));
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
