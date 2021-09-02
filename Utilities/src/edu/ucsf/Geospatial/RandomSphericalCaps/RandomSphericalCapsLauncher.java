package edu.ucsf.Geospatial.RandomSphericalCaps;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.Envelope;
import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Creates randomly placed and sized spherical caps up to a specified radius
 * @author jladau
 *
 */

public class RandomSphericalCapsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//lstOut = output
		//rgd1 = random center locations
		//dR = current radius
		//env1 = envelope
		//ply1 = cap in polygon format
		
		SphericalMultiPolygon ply1;
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		double rgd1[][];
		double dR;
		Envelope env1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		lstOut = new ArrayList<String>(arg1.getValueInt("iCaps")+1);
		lstOut.add(WktIO.header());
		env1 = new Envelope(-45., 45., -45., 45.);
		
		//loading latitude and longitude
		rgd1 = EarthGeometry.generateRandomPoints(env1, arg1.getValueInt("iCaps"), 1234);
	
		//generating spherical caps
		for(int i=0;i<rgd1.length;i++){
			
			//loading radius
			//dR = rnd1.nextDouble()*arg1.getValueDouble("dMaximumRadius");
			dR = arg1.getValueDouble("dRadius");

			//loading spherical cap
			ply1 = (new SphericalCapEarth(dR, rgd1[i], 1234 + i)).getPolygon();
			
			//outputting spherical cap
			lstOut.add(WktIO.toWKT(ply1, Integer.toString(i)));
		}
		
		//writing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
