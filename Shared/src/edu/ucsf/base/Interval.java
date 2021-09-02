package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Closed interval on the real line.
 * @author jladau
 */
public class Interval {

	/**Lower bound.**/
	public double dMin;
	
	/**Upper bound.**/
	public double dMax;
	
	/**Length.**/
	public double dLength;
	
	/**
	 * Constructor.
	 * @param dMin Lower bound.
	 * @param dMax Upper bound.
	 */
	public Interval(double dMin, double dMax){
		initialize(dMin,dMax);
	}
	
	/**
	 * Constructor.
	 * @param sInterval Comma-delimited interval; e.g., "-2,3". Use "-Infinity" or "Infinity" to denote unbounded intervals.
	 */
	public Interval(String sInterval){
		
		//rgs1 = split interval
		
		String[] rgs1;
		
		rgs1 = sInterval.split(",");
		initialize(Double.parseDouble(rgs1[0]),Double.parseDouble(rgs1[1]));	
	}
	
	/**
	 * Initializes variables.
	 * @param dMin Lower bound.
	 * @param dMax Upper bound.
	 */
	private void initialize(double dMin, double dMax){
		if(dMin<dMax){
			this.dMin=dMin;
			this.dMax=dMax;
		}else{
			this.dMin=dMax;
			this.dMax=dMin;
		}
		this.dLength=dMax-dMin;
	}
	
	/**
	 * Checks if value is in interval.
	 * @param dValue Value to check.
	 */
	public boolean contains(double dValue){
		if(dMin<=dValue && dValue<=dMax){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if a second interval is a subset of another interval.
	 * @param int1 Interval to check.
	 */
	public boolean contains(Interval int1){
		if(!contains(int1.dMin)){
			return false;
		}
		if(!contains(int1.dMax)){
			return false;
		}
		return true;
	}
	
	public boolean equals(Object o1){
		
		//int1 = object coerced to interval
		
		Interval int1;
		
		if(!(o1 instanceof Interval)){
			return false;
		}else{
			int1 = (Interval) o1;
		}
		
		if(int1.dMin!=dMin){
			return false;
		}
		if(int1.dMax!=dMax){
			return false;
		}
		assert int1.dLength == dLength;
		
		return true;
		
	}
	
	/**
	 * Finds smallest interval containing current interval and another interval.
	 * @param int1 Second interval to consider.
	 * @return Smallest interval.
	 */
	public Interval findSmallestSpanningInterval(Interval int1){
		
		//dMinLocal = minimum value
		//dMaxLocal = maximum value
		
		double dMinLocal; double dMaxLocal;
		
		if(this.dMin<int1.dMin){
			dMinLocal = this.dMin;
		}else{
			dMinLocal = int1.dMin;
		}
		if(this.dMax>int1.dMax){
			dMaxLocal = this.dMax;
		}else{
			dMaxLocal = int1.dMax;
		}
		return new Interval(dMinLocal,dMaxLocal);
	}
	
	
	/**
	 * Finds the symmetric difference with another interval.
	 * @param int1 Second interval to consider.
	 * @return List of intervals representing symmetric difference.
	 */
	public ArrayList<Interval> findSymmetricDifference(Interval int1){
		
		//lstOut = output
		//rgd1 = list of end points
		//dEpsilon = offset for checking set membership
		
		ArrayList<Interval> lstOut;
		double rgd1[];
		double dEpsilon;
		
		//loading epsilon
		dEpsilon = 0.0000000001;
		
		//loading list of values and sorting
		rgd1  = new double[4];
		rgd1[0] = this.dMin;
		rgd1[1] = this.dMax;
		rgd1[2] = int1.dMin;
		rgd1[3] = int1.dMax;
		Arrays.sort(rgd1);
		
		//outputting results
		lstOut = new ArrayList<Interval>();
		for(int i=0;i<3;i++){
			if(this.contains(rgd1[i]+dEpsilon) ^ int1.contains(rgd1[i]+dEpsilon)){
				lstOut.add(new Interval(rgd1[i],rgd1[i+1]));
			}
		}
		return lstOut;
	}
	
	/**
	 * Checks if all values in interval are less than specified value.
	 * @param dValue Value to check.
	 * @return True if interval is less than value; false otherwise.
	 */
	public boolean isLessThan(double dValue){
		if(this.dMax<dValue){
			return true;
		}else{
			return false;
		}
	}
	
	public String toString(){
		return dMin + "," + dMax;
	}
}
