package edu.ucsf.base;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.commons.math3.random.EmpiricalDistribution;

/**
 * Extends empirical distribution so that cumulative probabilities are calculated correctly.
 * @author jladau
 */

public class EmpiricalDistribution_Cumulative extends EmpiricalDistribution{

	private static final long serialVersionUID = 1L;
	
	/**Treemap with empirical probabilities**/
	private TreeMap<Double,Double> map1;
	
	public EmpiricalDistribution_Cumulative(double rgd1[]){
		super(rgd1.length+10);
		initialize(rgd1);
	}
	
	public EmpiricalDistribution_Cumulative(ArrayList<Double> lst1){
		super(lst1.size()+10);
		initialize(lst1);
	}
	
	private void initialize(double rgd1[]){
		super.load(rgd1);
		map1 = new TreeMap<Double,Double>();
		for(int i=0;i<rgd1.length;i++){
			map1.put(rgd1[i], super.cumulativeProbability(rgd1[i]));
		}
	}
	
	private void initialize(ArrayList<Double> lst1){
		
		//rgd1 = arraylist in double format
		
		double rgd1[];
		
		rgd1 = new double[lst1.size()];
		for(int i=0;i<lst1.size();i++){
			rgd1[i]=lst1.get(i);
		}
		initialize(rgd1);
	}
	
	
	public double cumulativeProbability(double d1){
		if(d1<map1.firstKey()){
			return 0;
		}else{
			return map1.get(map1.floorKey(d1));
		}
	}
}
