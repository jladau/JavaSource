package edu.ucsf.base;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;

import com.google.common.collect.HashMultimap;

/**
 * Runs loess interpolation
 * @author jladau
 */

public class LoessInterpolation {

	//TODO write unit test
	public static double[] smooth(double rgdX[], double rgdY[]){
		
		//lss1 = loess object
		//rgdOut = output
		//map1 = map from original x-values to rows in rgdX
		//map2 = map from original x-values to y-values
		//rgdX2 = deduplicated x matrix, sorted
		//i1 = index
		//rgd1 = initial fitted values
		//d1 = current value
		
		HashMultimap<Double,Integer> map1;
		HashMultimap<Double,Double> map2;
		LoessInterpolator lss1;
		double rgdOut[];
		double rgdX2[];
		double rgdY2[];
		double rgd1[];
		int i1;
		double d1;
		
		//initializing variables
		lss1 = new LoessInterpolator(0.3,4);
		map1 = HashMultimap.create();
		map2 = HashMultimap.create();
		
		//loading map with original indices
		for(int i=0;i<rgdX.length;i++){
			map1.put(rgdX[i], i);
			map2.put(rgdX[i], rgdY[i]);
		}
		
		//loading de-duplicated, sorted x matrix and accompanying y matrix
		rgdX2=new double[map1.keySet().size()];
		i1=0;
		for(double d:map1.keySet()){
			rgdX2[i1]=d;
			i1++;
		}
		Arrays.sort(rgdX2);
		rgdY2 = new double[rgdX2.length];
		for(int i=0;i<rgdX2.length;i++){
			rgdY2[i] = mean(map2.get(rgdX2[i]));
		}
		
		//finding smoothed values
		rgd1 = lss1.smooth(rgdX2, rgdY2);
		rgdOut = new double[rgdX.length];
		for(int i=0;i<rgdX2.length;i++){
			d1 = rgdX2[i];
			for(int k:map1.get(d1)){
				rgdOut[k] = rgd1[i];
			}
		}
		return rgdOut;
	}
	
	private static double mean(Set<Double> set1){
		
		//d1 = output
		
		double d1;
		
		d1=0;
		for(double d:set1){
			d1+=d;
		}
		return d1/((double) set1.size());	
	}
}
