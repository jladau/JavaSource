package edu.ucsf.SamplingIllustration;

import java.util.ArrayList;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.DataIO;

public class SamplingIllustrationLauncher {

	public static void main(String rgsArgs[]){
		
		//dRegionRadius = sampling region radius
		//dPlotRadius = sampling plot radius
		//rgdRegionCenter = region center
		//rgdPlot1Center = sampling plot 1 center
		//dPlot2Direction = direction to plot 2
		//cap1 = current cap
		//lstOut = output
		//ert1 = earth geometry object
		//sOutputPath = output path
		
		EarthGeometry ert1;
		ArrayList<String> lstOut;
		double dRegionRadius = 300;
		double dPlotRadius = 50;
		double rgdRegionCenter[] = new double[]{7, -67};
		double rgdPlot1Center[] = new double[]{8.4, -68};
		double dPlot2Direction = 200.;
		SphericalCapEarth cap1;
		String sOutputPath = "/home/jladau/Desktop/sampling_illustration_figure.wkt";
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add(WktIO.header());
		
		//outputting region
		cap1 = new SphericalCapEarth(dRegionRadius, rgdRegionCenter, 1234);
		lstOut.add(WktIO.toWKT(cap1.toPolygon(360), "study_region"));
		cap1 = new SphericalCapEarth(dRegionRadius-dPlotRadius, rgdRegionCenter, 1234);
		lstOut.add(WktIO.toWKT(cap1.toPolygon(360), "study_region_interior_set"));
			
		//outputting sampling plots
		ert1 = new EarthGeometry();
		cap1 = new SphericalCapEarth(dPlotRadius, rgdPlot1Center, 1234);
		lstOut.add(WktIO.toWKT(cap1.toPolygon(360), "sampling_plot_1"));
		cap1 = new SphericalCapEarth(dPlotRadius, ert1.findDestinationWGS84(rgdPlot1Center[0], rgdPlot1Center[1], dPlot2Direction, 2.*dPlotRadius), 1234);
		lstOut.add(WktIO.toWKT(cap1.toPolygon(360), "sampling_plot_2"));
		
		//terminating
		DataIO.writeToFile(lstOut, sOutputPath);
		System.out.println("Done.");
		
	}
	
	
}
