package gov.lbnl.SimulateData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class ConditionalDistribution0{

	/**Random number generator**/
	private Random rnd1;
	
	/**Slope of bound**/
	private double dSlope;
	
	/**Intercept of bound**/
	private double dIntercept;
	
	/**List of x values**/
	private ArrayList<Double> lstX;
	
	/**List of initial y values**/
	private ArrayList<Double> lstY0;
	
	/**Map from indices to y values**/
	private HashMap<Integer,Double> mapY;
	
	/**Number of values**/
	private double dRows;
	
	/**Map from indices to simulated values (for computing percentiles)**/
	private HashMap<Integer,double[]> mapSimulatedValues;
	
	public ConditionalDistribution0(int iIterations, double dBoundIntercept, double dBoundSlope, ArrayList<Double> lstX, ArrayList<Double> lstY) {
		
		
		rnd1 = new Random();
		this.dSlope = dBoundSlope;
		this.dIntercept = dBoundIntercept;
		this.lstX = lstX;
		this.lstY0 = lstY;
		mapY = new HashMap<Integer,Double>(lstY.size());
		for(int i=0;i<lstY.size();i++) {
			mapY.put(i,lstY.get(i));
		}
		this.dRows = (double) lstX.size();
		for(int i=0;i<10000;i++) {
			next();
		}
		
		//******************************
		mapSimulatedValues = new HashMap<Integer,double[]>(lstX.size());
		for(int i=0;i<lstX.size();i++) {
			mapSimulatedValues.put(i,new double[iIterations]);
		}
		for(int i=0;i<iIterations;i++) {
			next();
			for(Integer k:mapY.keySet()) {
				mapSimulatedValues.get(k)[i] = mapY.get(k);
			}
		}
		//******************************
	}
	
	private void next(){
		
		//i1 = first index being swapped
		//i2 = second index being swapped
		//df0 = joint likelihood of initial arrangement
		//df1 = joint likelihood of proposed arrangement
		//bAccept = flag for whether to accept swap
		//d1 = first value being swapped
		//d2 = second value being swapped
		
		int i1;
		int i2;
		double df0;
		double df1;
		boolean bAccept;
		double d1;
		double d2;
		
		i1 = (int) Math.floor(rnd1.nextDouble()*dRows);
		do {
			i2 = (int) Math.floor(rnd1.nextDouble()*dRows);
		}while(i2==i1);
		
		
		//***********************
		//i1 = 0;
		//i2 = 10;
		//***********************
		
		
		
		df0 = density(lstX.get(i1),mapY.get(i1))*density(lstX.get(i2),mapY.get(i2));
		df1 = density(lstX.get(i1),mapY.get(i2))*density(lstX.get(i2),mapY.get(i1));
		bAccept = false;
		if(df0==0 && df1>0) {
			bAccept = true;
		}else if(df0>0) {
			if(rnd1.nextDouble()<df1/df0){
				bAccept = true;
			}
		}
		
		//**********************
		//if(i1==0 || i2==0) {
		//	System.out.println("HERE");
		//}
		//**********************
		
		
		
		if(bAccept == true) {
			d1 = mapY.get(i1);
			d2 = mapY.get(i2);
			mapY.put(i1,d2);
			mapY.put(i2,d1);
		}
	}
	
	//***********************************
	private double density2(double dX, double dY) {
		
		//d1 = value of upper bound
		//d2 = value of lower bound
		
		double d1;
		double d2;
		
		d1 = dIntercept + dX*dSlope;
		d2 = -0.002+dX*0.004;
		if(dY>d1 || dY<d2) {
			return 0;
		}else {
			return 1/(d1-d2);
		}
	}
	
	private double density(double dX, double dY) {
	
		//d1 = value of bound
		
		double d1;
		
		d1 = dIntercept + dX*dSlope;
		if(dY>d1) {
			return 0;
		}else {
			return 1/d1;
		}
	}
	
	private double density1(double dX, double dY) {
		
		//d1 = value of bound
		
		double d1;
		
		d1 = dIntercept + dX*dSlope;
		if(dY>d1) {
			return 0;
		}else {
			return Math.sqrt(dY)/d1;
		}
	}
	//***********************************	
	
	public ArrayList<Double> y(){
		
		//lstY = output
		
		ArrayList<Double> lstY;
		
		lstY = new ArrayList<Double>(mapY.size());
		for(int i=0;i<lstX.size();i++) {
			lstY.add(mapY.get(i));
		}
		return lstY;	
	}
	
	public ArrayList<String> print0(){
		
		//lst1 = output
		//pct1 = percentile object
		//lst2 = list of values less than current bound
		//lst3 = list of sorted y-values
		//rgd1 = values less than current bound (for percentile)
		//d1 = current bound
		//dPercentile = current percentile
		
		Percentile pct1;
		ArrayList<String> lst1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		double rgd1[];
		double d1;
		double dPercentile;
		
		lst1 = new ArrayList<String>(lstX.size() + 1);
		lst1.add("BETA_DIVERSITY,RESPONSE_OBSERVED,RESPONSE_SIMULATED,POPULATION_75_QUANTILE");
		pct1 = new Percentile(75.);
		lst3 = new ArrayList<Double>(mapY.values());
		Collections.sort(lst3);
		for(int i=0;i<lstX.size();i++){
			lst2 = new ArrayList<Double>(lst3.size());
			d1 = lstX.get(i)*dSlope + dIntercept;
			for(int k=0;k<lst3.size();k++){
				if(lst3.get(k)<=d1) {
					lst2.add(lst3.get(k));
				}else {
					break;
				}
			}
			rgd1 = new double[lst2.size()];
			for(int k=0;k<lst2.size();k++) {
				rgd1[k] = lst2.get(k);
			}
			dPercentile = pct1.evaluate(rgd1);
			lst1.add(lstX.get(i) + "," + lstY0.get(i) + "," + mapY.get(i) + "," + dPercentile);
		}
		return lst1;
	}
	
	public ArrayList<String> print(){
		
		//lst1 = output
		//pct1 = percentile object
		//lst2 = list of values less than current bound
		//lst3 = list of sorted y-values
		//rgd1 = values less than current bound (for percentile)
		//d1 = current bound
		//dPercentile = current percentile
		
		Percentile pct1;
		ArrayList<String> lst1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		double rgd1[];
		double d1;
		double dPercentile;
		
		lst1 = new ArrayList<String>(lstX.size() + 1);
		lst1.add("BETA_DIVERSITY,RESPONSE_OBSERVED,RESPONSE_SIMULATED,POPULATION_75_QUANTILE");
		pct1 = new Percentile(75.);
		lst3 = new ArrayList<Double>(mapY.values());
		Collections.sort(lst3);
		for(int i=0;i<lstX.size();i++){
			dPercentile = pct1.evaluate(mapSimulatedValues.get(i));
			lst1.add(lstX.get(i) + "," + lstY0.get(i) + "," + mapY.get(i) + "," + dPercentile);
		}
		return lst1;
	}
}
