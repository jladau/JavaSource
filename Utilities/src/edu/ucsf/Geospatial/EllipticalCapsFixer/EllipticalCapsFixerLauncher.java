package edu.ucsf.Geospatial.EllipticalCapsFixer;

import java.util.ArrayList;

import edu.ucsf.geospatial.EllipticalCapEarth;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class EllipticalCapsFixerLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//cap1 = elliptical cap;
		//ply1 = polygon
		//lstOut = output
		//lst1 = list of random points
		
		ArgumentIO arg1;
		EllipticalCapEarth cap1;
		ArrayList<String> lstOut;
		ArrayList<Double[]> lst1;
		
		lstOut = new ArrayList<String>(10);
		cap1 = new EllipticalCapEarth(50,-90,35,-100,2000);
		lstOut.add(WktIO.header());
		
		lstOut.add(WktIO.toWKT(cap1.toPolygon(300), "1"));
		lst1 = cap1.randomPointsInCap();
		DataIO.writeToFile(lstOut, "/home/jladau/Desktop/temp.wkt");
		lstOut = new ArrayList<String>(lst1.size()+1);
		lstOut.add("LATITUDE,LONGITUDE");
		for(int i=0;i<lst1.size();i++){
			lstOut.add(lst1.get(i)[0] + "," + lst1.get(i)[1]);
		}
		DataIO.writeToFile(lstOut, "/home/jladau/Desktop/temp.csv");
		System.out.println(cap1.area());
		System.out.println("Done.");
	}
	
	
}
