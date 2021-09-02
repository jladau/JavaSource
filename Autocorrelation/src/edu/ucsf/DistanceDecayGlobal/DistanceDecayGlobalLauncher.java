package edu.ucsf.DistanceDecayGlobal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class DistanceDecayGlobalLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//ert1 = earth geometry object
		//map1 = map from species ID to latitude and longitude of center of range
		//iSpecies = total number of species
		//rgd1 = current random point
		//rgd2 = current second random point
		//rnd1 = random number generator
		//iSamplePairs = total number of pairs of samples to consider
		//rgdDistances = distances to consider
		//i2 = distance counter
		//rgi1 = current intersection and union
		//dK = current constant value for calculating probability
		//lstOut = output
		//sStatistic = statistic
		//i1 = counter
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		EarthGeometry ert1;
		HashMap<String,Double[]> map1;
		int iSpecies;
		double rgd1[];
		double rgd2[];
		Random rnd1;
		int iSamplePairs;
		double rgdDistances[];
		int i2;
		int rgi1[];
		double dK;
		String sStatistic;
		int i1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		ert1 = new EarthGeometry();
		iSpecies = arg1.getValueInt("iSpecies");
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		iSamplePairs = arg1.getValueInt("iSamplePairs");
		rgdDistances = new double[]{10,100,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000};
		i2 = iSamplePairs/rgdDistances.length;
		dK = arg1.getValueDouble("dK");
		sStatistic = arg1.getValueString("sStatistic");
		
		//loading map with species ranges
		map1 = new HashMap<String,Double[]>(iSpecies);
		for(int i=0;i<iSpecies;i++){
			rgd1 = ert1.randomPoint();
			map1.put(Integer.toString(i), new Double[]{rgd1[0],rgd1[1]});
		}
		
		//initializing output
		lstOut = new ArrayList<String>(iSamplePairs);
		lstOut.add("DISTANCE,SIMILARITY");
		
		//looping through pairs of samples
		i1 = 0;
		for(double dDistance:rgdDistances){	
			for(int i=0;i<i2;i++){
				rgd1 = ert1.randomPoint();
				rgd2 = ert1.findDestinationWGS84(rgd1[0], rgd1[1], rnd1.nextDouble()*360., dDistance);
				rgi1 = findIntersectionUnion(rgd1, rgd2, map1, dK, ert1, rnd1);
				lstOut.add(dDistance + "," + turnover(rgi1,sStatistic));
				
				//updating progress
				i1++;
				if(i1 % 100 == 0){	
					System.out.println("Analyzing sample pair " + i1 + " of " + iSamplePairs + "...");
				}
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static double turnover(int rgi1[], String sStatistic){
		
		if(sStatistic.equals("sorenson")){
			return (2.*rgi1[0])/((double) (rgi1[0] + rgi1[1]));
		}else{
			return Double.NaN;
		}
	}
	
	
	/**
	 * Outputs intersection and union of communities at two points
	 */
	private static int[] findIntersectionUnion(double rgdSample1[], double rgdSample2[], HashMap<String,Double[]> mapRanges, double dK, EarthGeometry ert1, Random rnd1){
		
		//rgi1 = output (intersection, union)
		//b1 = occurrence in first sample
		//b2 = occurrence in second sample
		
		int rgi1[];
		boolean b1;
		boolean b2;
		
		rgi1 = new int[2];
		for(String s:mapRanges.keySet()){
			b1 = simulateOccurrence(rgdSample1, mapRanges.get(s), dK, ert1, rnd1);
			b2 = simulateOccurrence(rgdSample2, mapRanges.get(s), dK, ert1, rnd1);
			if(b1==true && b2==true){
				rgi1[0]++;
			}
			if(b1==true || b2==true){
				rgi1[1]++;
			}
		}
		return rgi1;
	}
	
	/**
	 * Simulates occurrence of a species according to a bernoulli random variable
	 */
	private static boolean simulateOccurrence(double rgdSampleLocation[], Double rgdRangeLocation[], double dK, EarthGeometry ert1, Random rnd1){
		
		//d1 = distance between sample location and range center
		//d2 = probability of occurrence
		
		double d1;
		double d2;
		
		d1 = EarthGeometry.orthodromicDistance(rgdSampleLocation[0], rgdSampleLocation[1], rgdRangeLocation[0], rgdRangeLocation[1]);
		d2 = dK/(dK + d1);
		if(rnd1.nextDouble()<d2){
			return true;
		}else{
			return false;
		}
	}
}
