package edu.ucsf.base;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Function defined by a series of x,y values. Values are calculated by linear interpolation. Values at less than the minimum input x are assumed to be equal to the minimum x value; those above the maximum x are assumed to be equal to the maximum x value
 * @author jladau
 */


public class Function {

	/**Map from x values to y values**/
	private TreeMap<Double,Double> map1;
	
	/**Domain**/
	private Interval intDomain;
	
	public Function(ArrayList<Double> lstX, ArrayList<Double> lstY){
		initialize(lstX, lstY, new Interval(-Double.MAX_VALUE,Double.MAX_VALUE));
	}
	
	public Function(ArrayList<Double> lstX, ArrayList<Double> lstY, Interval intDomain){
		initialize(lstX, lstY, intDomain);
	}
	
	private void initialize(ArrayList<Double> lstX, ArrayList<Double> lstY, Interval intDomain){
		map1 = new TreeMap<Double,Double>();
		for(int i=0;i<lstX.size();i++){
			map1.put(lstX.get(i), lstY.get(i));
		}
		this.intDomain = intDomain;
	}
	
	public double image(double dX){
		
		//dX1 = lower bound
		//dX2 = upper bound
		//dY1 = lower bound
		//dY2 = upper bound
		
		double dX1;
		double dX2;
		double dY1;
		double dY2;
		
		if(!intDomain.contains(dX)){
			return Double.NaN;
		}
		
		if(dX<map1.firstKey()){
			return map1.get(map1.firstKey());
		}
		if(dX>map1.lastKey()){
			return map1.get(map1.lastKey());
		}
		dX1 = map1.floorKey(dX);
		dX2 = map1.ceilingKey(dX);
		dY1 = map1.get(map1.floorKey(dX));
		dY2 = map1.get(map1.ceilingKey(dX));
		try{
			return ExtendedMath.linearInterpolation(dX, dX1, dY1, dX2, dY2);
		}catch(Exception e){
			return Double.NaN;
		}
	}
	
	public boolean inDomain(Double dX){
		return intDomain.contains(dX);
	}
}
