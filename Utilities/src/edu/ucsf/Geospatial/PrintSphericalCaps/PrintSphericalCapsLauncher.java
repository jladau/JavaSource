package edu.ucsf.Geospatial.PrintSphericalCaps;

import java.util.ArrayList;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Prints spherical caps
 * @author jladau
 *
 */

public class PrintSphericalCapsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//lstOut = output
		//dR = current radius
		//ply1 = cap in polygon format
		//dat1 = data with spherical caps
		
		SphericalMultiPolygon ply1;
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		double dR;
		DataIO dat1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(WktIO.header());
		
		//looping through caps
		for(int i=1;i<dat1.iRows;i++){
		
			//loading radius
			//dR = rnd1.nextDouble()*arg1.getValueDouble("dMaximumRadius");
			dR = dat1.getDouble(i, "RADIUS");

			//loading spherical cap
			ply1 = (new SphericalCapEarth(dR, dat1.getDouble(i, "LATITUDE_CENTER"), dat1.getDouble(i, "LONGITUDE_CENTER"), 1234 + i)).getPolygon();
			
			//outputting spherical cap
			lstOut.add(WktIO.toWKT(ply1, Integer.toString(i)));
		}
		
		//writing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
